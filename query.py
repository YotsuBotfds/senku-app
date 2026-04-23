#!/usr/bin/env python3
"""Interactive RAG query loop against the Senku knowledge base."""

import argparse
import json
import io
import logging
import os
import re
import sqlite3
import subprocess
import sys
import time
from collections import Counter
from dataclasses import dataclass
from typing import Callable, Literal

try:
    import termios
except ImportError:  # Windows does not provide termios.
    termios = None

# Force UTF-8 stdout on Windows to avoid cp1252 encoding errors
if sys.platform == "win32":
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding="utf-8", errors="replace")

import chromadb
import requests
from rich.console import Console
from rich.markdown import Markdown
from rich.panel import Panel

import config
import special_case_builders
from confidence_label_contract import resolve_confidence_presentation
from deterministic_special_case_registry import DETERMINISTIC_SPECIAL_CASE_SPECS
from guide_catalog import all_guide_ids, get_anchor_related_link_weights
from lmstudio_utils import (
    classify_lm_request_error,
    embedding_models_to_try,
    is_retryable_lm_request,
    normalize_lm_studio_url,
    should_try_embedding_fallback,
)
from metadata_helpers import normalize_metadata_tag, normalize_tags
from token_estimation import estimate_tokens

console = Console()
logger = logging.getLogger(__name__)

LM_STUDIO_MAX_RETRIES = 4
LM_STUDIO_RETRY_BASE_DELAY = 1.5
HYBRID_RRF_K = 60
HYBRID_RRF_MAX_BONUS = 0.08
HYBRID_LEXICAL_ONLY_BONUS = 0.015
LEXICAL_DISTANCE_FLOOR = 0.90
LEXICAL_DISTANCE_RANGE = 0.22
METADATA_RERANK_DELTA_MIN = -0.30
METADATA_RERANK_DELTA_MAX = 0.30
ENABLE_ANCHOR_PRIOR = True
ANCHOR_BASE_BONUS = 0.08
ANCHOR_PRIOR_MAX_BONUS = 0.10

ConfidenceLabel = Literal["high", "medium", "low"]
AnswerMode = Literal["confident", "uncertain_fit", "abstain"]


def copy_to_clipboard(text):
    """Copy text to system clipboard."""
    try:
        if sys.platform == "darwin":
            cmd = ["pbcopy"]
        elif sys.platform == "win32":
            cmd = ["clip"]
        else:
            cmd = ["xclip", "-selection", "clipboard"]
        subprocess.run(cmd, input=text.encode("utf-8"), check=True)
        return True
    except (FileNotFoundError, subprocess.CalledProcessError):
        return False


def should_retry_lm_request(exc):
    """Return True for transient LM Studio request failures."""
    return is_retryable_lm_request(exc)


def post_json_with_retry(
    url,
    payload,
    *,
    timeout,
    stream=False,
    context="LM Studio request",
    max_attempts=LM_STUDIO_MAX_RETRIES,
    retry_log=None,
):
    """POST JSON to LM Studio with retry/backoff for transient failures."""
    delay = LM_STUDIO_RETRY_BASE_DELAY

    for attempt in range(1, max_attempts + 1):
        try:
            resp = requests.post(url, json=payload, stream=stream, timeout=timeout)
            resp.raise_for_status()
            return resp
        except requests.RequestException as exc:
            failure = classify_lm_request_error(exc)
            if retry_log is not None:
                retry_log.append(
                    {
                        "attempt": attempt,
                        "category": failure["category"],
                        "status_code": failure["status_code"],
                        "retryable": failure["retryable"],
                        "message": failure["message"],
                        "context": context,
                    }
                )
            if attempt == max_attempts or not should_retry_lm_request(exc):
                raise

            console.print(
                f"[yellow]{context} failed on attempt {attempt}/{max_attempts}: "
                f"{failure['category']} ({exc}). Retrying in {delay:.1f}s...[/yellow]"
            )
            time.sleep(delay)
            delay = min(delay * 2, 20)


def _post_embeddings_with_fallback(inputs, model, *, timeout, context, base_url=None):
    """POST embeddings, retrying with a compatible model alias when needed."""
    lm_studio_url = normalize_lm_studio_url(base_url or config.LM_STUDIO_URL)
    url = f"{lm_studio_url}/embeddings"
    models = embedding_models_to_try(model)
    last_exc = None

    for index, candidate in enumerate(models):
        delay = LM_STUDIO_RETRY_BASE_DELAY
        for attempt in range(1, LM_STUDIO_MAX_RETRIES + 1):
            try:
                resp = requests.post(
                    url,
                    json={"input": inputs, "model": candidate},
                    timeout=timeout,
                )
                resp.raise_for_status()
                return resp
            except requests.RequestException as exc:
                last_exc = exc
                if index < len(models) - 1 and should_try_embedding_fallback(exc):
                    console.print(
                        f"[yellow]{context} failed for embedding model {candidate}: {exc}. "
                        f"Falling back to {models[index + 1]}...[/yellow]"
                    )
                    break

                if attempt == LM_STUDIO_MAX_RETRIES or not should_retry_lm_request(exc):
                    raise

                console.print(
                    f"[yellow]{context} failed on attempt {attempt}/{LM_STUDIO_MAX_RETRIES}: "
                    f"{exc}. Retrying in {delay:.1f}s...[/yellow]"
                )
                time.sleep(delay)
                delay = min(delay * 2, 20)

    raise last_exc


def embed_query(text, model=None, base_url=None):
    """Embed a single query string."""
    model = model or config.EMBED_MODEL
    resp = _post_embeddings_with_fallback(
        [text],
        model,
        timeout=30,
        context="Embedding request",
        base_url=base_url,
    )
    try:
        return resp.json()["data"][0]["embedding"]
    finally:
        resp.close()


def embed_batch(texts, model=None, base_url=None):
    """Embed multiple query strings in a single API call."""
    model = model or config.EMBED_MODEL
    resp = _post_embeddings_with_fallback(
        texts,
        model,
        timeout=30,
        context="Embedding batch",
        base_url=base_url,
    )
    try:
        return [item["embedding"] for item in resp.json()["data"]]
    finally:
        resp.close()


# Domain keywords for multi-axis query detection. If a query touches 2+
# domains, it gets decomposed into sub-queries for broader retrieval.
_DOMAIN_KEYWORDS = {
    "medical": {
        "wound",
        "wounded",
        "bleed",
        "bleeding",
        "broken",
        "fracture",
        "infection",
        "fever",
        "vomiting",
        "throwing up",
        "poisoning",
        "ate",
        "swallowed",
        "burn",
        "burned",
        "breathing",
        "unconscious",
        "seizure",
        "pain",
        "hurt",
        "injured",
        "bite",
        "sting",
        "pregnant",
        "delivery",
        "choking",
        "sick",
        "diarrhea",
        "swelling",
        "tooth",
        "antibiotics",
        "medicine",
        "medical",
        "chest pain",
        "chest pressure",
        "cardiac",
        "heart attack",
        "heart",
        "cpr",
        "pulse",
        "stroke",
        "tia",
        "transient ischemic attack",
        "mini stroke",
        "face droop",
        "facial droop",
        "slurred speech",
        "trouble speaking",
        "one-sided weakness",
        "one-sided numbness",
        "numbness on one side",
        "dehydrated",
        "pee",
        "peeing",
        "urine",
        "urinary",
        "urinate",
        "urination",
        "dysuria",
        "urgency",
        "frequency",
        "bladder",
        "burning when i pee",
        "burns when i pee",
    },
    "psychological": {
        "alone",
        "panic",
        "scared",
        "cant think",
        "losing it",
        "morale",
        "stress",
        "overwhelmed",
        "crying",
        "giving up",
        "nightmares",
        "sleep",
        "hostile",
        "paranoid",
        "grief",
        "died",
        "death",
    },
    "shelter": {
        "shelter",
        "cold",
        "wet",
        "rain",
        "hypothermia",
        "freeze",
        "freezing",
        "wind",
        "exposed",
        "roof",
        "insulation",
        "collapsed",
        "trapped",
        "building",
        "tarp",
        "lean-to",
        "lean to",
    },
    "water": {
        "water",
        "dehydrated",
        "thirsty",
        "purify",
        "filter",
        "boil",
        "clean water",
        "no water",
        "contaminated",
        "well",
        "stream",
        "bucket",
        "buckets",
        "rainwater",
    },
    "fire": {
        "fire",
        "warmth",
        "dark",
        "getting dark",
        "night",
        "light",
        "cook",
        "signal fire",
        "smoke",
        "fuel",
        "lighter",
        "matches",
        "gasoline",
        "gas can",
    },
    "food": {
        "food",
        "hungry",
        "starving",
        "forage",
        "hunt",
        "fish",
        "trap",
        "edible",
        "rations",
        "crops",
        "livestock",
        "garden",
        "harvest",
    },
    "navigation": {
        "lost",
        "navigate",
        "compass",
        "direction",
        "map",
        "trail",
        "stars",
        "signal",
        "rescue",
    },
    "defense": {
        "threat",
        "intruder",
        "weapon",
        "fortify",
        "perimeter",
        "guard",
        "armed",
        "strangers",
        "stealing",
    },
    "construction": {
        "house",
        "home",
        "cabin",
        "hut",
        "canoe",
        "boat",
        "watercraft",
        "bridge",
        "roof",
        "wall",
        "foundation",
        "frame",
        "framing",
        "loom",
        "oven",
        "kiln",
        "shelter construction",
        "boat building",
    },
}

_HUMAN_MEDICAL_KEYWORDS = {
    "wound",
    "puncture",
    "burn",
    "burned",
    "bite",
    "bleeding",
    "shortness of breath",
    "bleed",
    "splinter",
    "infection",
    "infected",
    "fever",
    "swelling",
    "seizure",
    "choking",
    "vomiting",
    "diarrhea",
    "dehydrated",
    "pain",
    "injured",
    "broken",
    "fracture",
    "tooth",
    "cut",
    "laceration",
    "sting",
    "pee",
    "peeing",
    "urine",
    "urinary",
    "urinate",
    "urination",
    "dysuria",
    "urgency",
    "frequency",
    "bladder",
    "burning when i pee",
    "burns when i pee",
    "asthma",
    "anaphylaxis",
    "wheezing",
    "wheeze",
    "throat swelling",
    "hives",
    "blue lips",
    "can barely talk",
    "stroke",
    "tia",
    "transient ischemic attack",
    "mini stroke",
    "face droop",
    "facial droop",
    "slurred speech",
    "trouble speaking",
    "trouble finding words",
    "one-sided weakness",
    "one-sided numbness",
    "numbness on one side",
}

_ACUTE_SYMPTOM_MARKERS = {
    "chest pain",
    "chest pressure",
    "shortness of breath",
    "trouble breathing",
    "difficulty breathing",
    "breathing trouble",
    "coffee grounds",
    "coffee ground vomit",
    "coffee ground emesis",
    "melena",
    "black tarry stool",
    "black tarry stools",
    "bright red vomit",
    "dark clots",
    "stroke",
    "tia",
    "transient ischemic attack",
    "mini stroke",
    "face droop",
    "facial droop",
    "slurred speech",
    "trouble speaking",
    "trouble finding words",
    "cannot get words out",
    "can't get words out",
    "one-sided weakness",
    "weakness on one side",
    "one-sided numbness",
    "numbness on one side",
    "sudden weakness",
    "sudden numbness",
    "confusion",
    "fainting",
    "passing out",
    "seizure",
    "severe headache",
    "stiff neck",
    "severe bleeding",
    "vomiting blood",
    "black stool",
    "allergic reaction",
    "anaphylaxis",
    "asthma",
    "wheezing",
    "wheeze",
    "throat swelling",
    "tongue swelling",
    "lip swelling",
    "blue lips",
    "can barely talk",
}

_MENTAL_HEALTH_CRISIS_QUERY_MARKERS = {
    "panic attack",
    "hearing voices",
    "voice telling",
    "hallucination",
    "hallucinations",
    "paranoid",
    "paranoia",
    "stopped drinking",
    "alcohol withdrawal",
    "withdrawal",
    "shaking and agitated",
    "dissociating",
    "dissociation",
    "flashback",
    "flashbacks",
    "reliving what happened",
    "not acting normal",
    "won't eat",
    "wont eat",
    "won't get out of bed",
    "wont get out of bed",
    "violent event",
    "something is very wrong",
    "stress or a crisis",
    "just insomnia or mental health crisis",
    "normal rules do not apply",
    "special mission",
    "acting invincible",
    "nothing can hurt",
    "unsafe choices",
    "making unsafe choices",
    "won't stop moving",
    "will not stop moving",
    "barely eating",
    "hardly eating",
}

_MANIA_SLEEP_CRISIS_QUERY_MARKERS = {
    "days without sleep",
    "no sleep for days",
    "no sleep",
    "does not need sleep",
    "do not need sleep",
    "doesn't need sleep",
    "don't need sleep",
    "awake for days",
    "has not slept for",
    "no sleep for",
    "hasn't slept",
    "has not slept",
    "not sleeping",
    "barely sleeping",
    "barely slept",
    "hardly slept",
    "not really slept",
}

_MANIA_ACTIVATION_CRISIS_QUERY_MARKERS = {
    "racing thoughts",
    "spending wildly",
    "agitated",
    "agitation",
    "acting invincible",
    "invincible",
    "talking nonstop",
    "talking non stop",
    "talking fast",
    "nonstop pacing",
    "pacing nonstop",
    "pacing all night",
    "keeps pacing",
    "reckless",
    "grandiose",
    "impossible to slow down",
    "can't slow down",
    "cannot slow down",
    "mission-driven behavior",
    "sudden mission-driven behavior",
    "special mission",
    "special mission tonight",
    "normal rules do not apply",
    "rules do not apply",
    "unsafe nighttime wandering",
    "unsafe choices",
    "making unsafe choices",
    "won't stop moving",
    "will not stop moving",
    "won't stop talking",
    "will not stop talking",
    "nothing can hurt",
    "like nothing can hurt",
    "nothing can hurt him",
    "nothing can hurt her",
    "nothing can hurt them",
    "can't be hurt",
    "cannot be hurt",
    "can not be hurt",
    "untouchable",
    "not eating",
    "will not eat",
    "barely eating",
    "barely eaten",
    "hardly eating",
    "hardly eaten",
    "won't sit down",
    "will not sit down",
    "won't sit still",
    "will not sit still",
    "walk outside at night",
    "trying to walk outside at night",
    "keeps trying to walk outside at night",
    "trying to leave",
    "leave with no plan",
    "trying to leave with no plan",
    "won't let anyone slow him down",
    "won't let anyone slow her down",
    "won't let anyone slow them down",
    "will not let anyone slow him down",
    "will not let anyone slow her down",
    "will not let anyone slow them down",
    "rearranging everything",
}

_MANIA_HIGH_RISK_OBSERVER_QUERY_MARKERS = {
    "acting invincible",
    "invincible",
    "won't stop moving",
    "will not stop moving",
    "won't stop talking",
    "will not stop talking",
    "talking nonstop",
    "talking non stop",
    "talking fast",
    "keeps pacing",
    "nothing can hurt",
    "like nothing can hurt",
    "nothing can hurt him",
    "nothing can hurt her",
    "nothing can hurt them",
    "can't be hurt",
    "cannot be hurt",
    "can not be hurt",
    "untouchable",
    "special mission",
    "special mission tonight",
    "normal rules do not apply",
    "rules do not apply",
    "barely eating",
    "barely eaten",
    "hardly eating",
    "hardly eaten",
    "won't sit down",
    "will not sit down",
    "won't sit still",
    "will not sit still",
    "walk outside at night",
    "trying to walk outside at night",
    "keeps trying to walk outside at night",
    "trying to leave",
    "leave with no plan",
    "trying to leave with no plan",
    "won't let anyone slow him down",
    "won't let anyone slow her down",
    "won't let anyone slow them down",
    "will not let anyone slow him down",
    "will not let anyone slow her down",
    "will not let anyone slow them down",
}

_PSYCHOSIS_LIKE_CRISIS_QUERY_MARKERS = {
    "psychosis",
    "psychotic",
    "hearing voices",
    "voice telling",
    "hallucination",
    "hallucinations",
    "paranoid",
    "paranoia",
    "delusion",
    "delusional",
}

_CARDIAC_EMERGENCY_MARKERS = {
    "heart attack",
    "cardiac arrest",
    "cardiac emergency",
    "no pulse",
    "pulseless",
    "cpr",
}

_COLLAPSE_UNRESPONSIVE_MARKERS = {
    "collapse",
    "collapsed",
    "collapsing",
    "unresponsive",
    "unconscious",
    "passed out",
    "passing out",
    "fainted",
    "fainting",
}

_STROKE_TIA_MARKERS = {
    "stroke",
    "tia",
    "transient ischemic attack",
    "mini stroke",
}

_FAST_SIGN_MARKERS = {
    "face droop",
    "facial droop",
    "face drooping",
    "facial drooping",
    "slurred speech",
    "speech is slurred",
    "trouble speaking",
    "trouble finding words",
    "difficulty finding words",
    "word-finding difficulty",
    "word finding difficulty",
    "cannot get words out",
    "can't get words out",
    "words are coming out wrong",
    "one-sided weakness",
    "weakness on one side",
    "one-sided numbness",
    "numbness on one side",
    "one side is weak",
    "one side of the body is weak",
    "one side of the body is numb",
    "arm weakness",
    "arm numbness",
    "leg weakness",
    "leg numbness",
    "one arm feels weak",
    "one arm is weak",
    "one arm is numb",
    "one leg is weak",
    "one leg is numb",
}

_TRANSIENT_NEURO_EPISODE_MARKERS = {
    "transient",
    "resolved",
    "symptoms resolved",
    "went away",
    "got better",
    "back to normal",
    "might return",
    "brief episode",
}

_CARDIAC_OVERLAP_SIGNAL_MARKERS = {
    "chest pain",
    "chest pressure",
    "chest tightness",
    "shortness of breath",
    "trouble breathing",
    "difficulty breathing",
    "pounding heart",
    "heart pounding",
    "racing heart",
    "left arm pain",
    "jaw pain",
    "pain going into the left arm",
    "pain going into the jaw",
    "heart attack",
    "cardiac emergency",
}

_HOUSEHOLD_CHEMICAL_HAZARD_MARKERS = {
    "bleach",
    "ammonia",
    "drain cleaner",
    "toilet bowl cleaner",
    "oven cleaner",
    "muriatic acid",
    "pool acid",
    "sulfuric acid",
    "sulphuric acid",
    "battery acid",
    "car battery acid",
    "lead-acid battery",
    "lead acid battery",
    "lye",
    "caustic",
    "corrosive",
    "poison control",
    "poisoning",
    "chemical burn",
    "chemical in eye",
    "chemical in my eye",
    "chemical on skin",
    "chemical on my skin",
    "acid in eye",
    "acid in my eye",
    "acid on skin",
    "acid on my skin",
    "battery acid in eye",
    "battery acid in my eye",
    "battery acid on skin",
    "battery acid on my skin",
    "swallowed cleaner",
    "swallowed bleach",
    "swallowed ammonia",
    "swallowed drain cleaner",
    "swallowed oven cleaner",
    "swallowed toilet bowl cleaner",
    "inhaled bleach",
    "inhaled ammonia",
    "mixed cleaners",
    "mix bleach",
    "mixing bleach",
    "mixed bleach and ammonia",
    "chlorine gas",
    "chloramine gas",
}

_CORROSIVE_HOUSEHOLD_CHEMICAL_SOURCE_MARKERS = {
    "bleach",
    "ammonia",
    "cleaner",
    "cleaning product",
    "drain cleaner",
    "toilet bowl cleaner",
    "oven cleaner",
    "muriatic acid",
    "pool acid",
    "sulfuric acid",
    "sulphuric acid",
    "battery acid",
    "car battery acid",
    "lead-acid battery",
    "lead acid battery",
    "lye",
    "caustic",
    "corrosive",
}

_CHEMICAL_EXPOSURE_ROUTE_MARKERS = {
    "in eye",
    "in my eye",
    "in the eye",
    "in eyes",
    "in my eyes",
    "eye exposure",
    "eye burn",
    "burning eye",
    "on skin",
    "on my skin",
    "on the skin",
    "skin exposure",
    "skin burn",
    "burning skin",
    "chemical burn",
    "acid burn",
    "splashed",
    "splash",
    "sprayed on",
    "got on",
    "swallowed",
    "drank",
    "drink it",
    "ingested",
    "ate",
    "in mouth",
    "mouth burn",
    "mouth exposure",
    "inhaled",
    "breathed in",
    "fumes",
    "vapors",
    "vapours",
    "mixed cleaners",
    "mix bleach",
    "mixing bleach",
    "coughing",
    "chest tightness",
    "trouble breathing",
    "shortness of breath",
    "wheezing",
}

_CHEMICAL_EYE_ROUTE_MARKERS = {
    "in eye",
    "in my eye",
    "in the eye",
    "in eyes",
    "in my eyes",
    "eye exposure",
    "eye burn",
    "burning eye",
    "got in my eye",
    "got in the eye",
    "splashed in my eye",
    "splashed in the eye",
    "sprayed in my eye",
    "still burns after rinsing",
}

_HOUSEHOLD_CHEMICAL_INHALATION_SOURCE_MARKERS = {
    "bleach",
    "ammonia",
    "cleaner",
    "cleaners",
    "cleaning product",
    "drain cleaner",
    "toilet bowl cleaner",
    "oven cleaner",
    "paint thinner",
    "solvent",
    "solvents",
    "mineral spirits",
    "turpentine",
    "acetone",
    "varnish",
    "stain",
    "staining product",
    "chlorine gas",
    "chloramine gas",
}

_CHEMICAL_INHALATION_ROUTE_MARKERS = {
    "inhaled",
    "breathed in",
    "fumes",
    "vapors",
    "vapours",
    "mixed cleaners",
    "mix bleach",
    "mixing bleach",
    "coughing",
    "chest tightness",
    "shortness of breath",
    "trouble breathing",
    "difficulty breathing",
    "wheezing",
    "feel sick",
    "feels sick",
    "headache",
    "dizzy",
    "dizziness",
    "nausea",
    "nauseous",
}

_CHEMICAL_EYE_GUIDE_METADATA_MARKERS = {
    "eye injuries",
    "emergency ophthalmology",
    "ophthalmology",
    "chemical burns",
    "ocular exposure",
    "irrigation protocol",
    "corneal",
}

_LAB_SAFETY_METADATA_MARKERS = {
    "micro-scale chemistry lab safety",
    "chemistry lab safety",
    "lab safety",
    "chemistry-lab-protocols-safety",
    "sop",
    "sops",
}

_HOUSEHOLD_CHEMICAL_EXPOSURE_METADATA_MARKERS = {
    "toxicology",
    "poison",
    "poison control",
    "chemical exposure",
    "decontamination",
    "unknown ingestion",
    "household cleaner",
    "household chemical",
    "corrosive",
    "inhaled poisons",
    "eye irrigation",
}

_COOKSTOVE_CO_METADATA_MARKERS = {
    "cookstoves",
    "indoor heating",
    "woodstove",
    "draft troubleshooting",
    "smoke comes back into the room",
    "ventilation and indoor air",
    "smoke inhalation",
    "fire-gas exposure",
    "carbon monoxide",
    "chimney",
    "heater",
}

_URINARY_QUERY_MARKERS = {
    "pee",
    "peeing",
    "urine",
    "urinary",
    "urinate",
    "urination",
    "dysuria",
    "urgency",
    "frequency",
    "bladder",
    "bladder pressure",
    "uti",
    "burning when i pee",
    "burns when i pee",
    "need to pee",
    "need to pee often",
    "pee often",
    "peeing often",
    "frequent urination",
}

_URINARY_METADATA_MARKERS = {
    "urinary",
    "urination",
    "urinate",
    "urinary tract",
    "uti",
    "bladder",
    "frequency",
    "urgency",
    "dysuria",
}

_BOWEL_RECTAL_DISTRACTOR_MARKERS = {
    "constipation",
    "hemorrhoid",
    "hemorrhoids",
    "rectal",
    "anal",
    "stool",
    "bowel",
    "passing stool",
    "passing gas",
}

_EXPLICIT_VETERINARY_QUERY_MARKERS = {
    "veterinary",
    "livestock",
    "working dog",
    "draft animal",
    "animal husbandry",
    "farrier",
    "hoof",
    "rumen",
    "herd",
    "flock",
    "foal",
    "calf",
    "my horse",
    "our horse",
    "my mule",
    "our mule",
    "my donkey",
    "our donkey",
    "my goat",
    "our goat",
    "my cow",
    "our cow",
    "my cattle",
    "our cattle",
    "my sheep",
    "our sheep",
    "my chicken",
    "our chicken",
    "my dog is",
    "our dog is",
    "my cat is",
    "our cat is",
}

_VETERINARY_METADATA_MARKERS = {
    "veterinary",
    "livestock",
    "working dog",
    "draft animal",
    "animal husbandry",
    "farrier",
    "hoof",
    "rumen",
    "foal",
    "calf",
    "mare",
    "stallion",
    "oxen",
    "ox ",
    "mule",
    "donkey",
    "harness",
    "pack animal",
}

_MEDICAL_METADATA_MARKERS = {
    "first aid",
    "wound",
    "burn",
    "bleeding",
    "hemorrhage",
    "infection",
    "bite wound",
    "rabies",
    "trauma",
    "emergency",
    "fracture",
    "surgery",
    "poison",
    "airway",
    "stroke",
    "tia",
    "transient ischemic attack",
    "neurologic",
    "neurological",
    "cardiac",
}

_CONSERVATIVE_MEDICAL_METADATA_MARKERS = {
    "first aid",
    "wound management",
    "burn treatment",
    "hemorrhage control",
    "infection control",
    "fracture management",
    "stabilization",
    "monitoring",
    "first aid essentials",
    "wound assessment and cleaning",
    "evacuation",
    "triage",
    "stroke red flags",
    "neurologic red flags",
    "transient ischemic attack",
}

_MENTAL_HEALTH_CRISIS_HIGH_ACUITY_METADATA_MARKERS = {
    "mental health crisis",
    "mental health crises",
    "psychosis",
    "hallucination",
    "paranoia",
    "mania",
    "manic",
    "unsafe behavior",
    "urgent evaluation",
    "emergency mental health",
    "crisis escalation",
    "continuous supervision",
    "do not leave alone",
    "behavior change",
    "agitated",
    "agitation",
    "disorganized",
    "delusion",
    "delusional",
    "hearing voices",
    "grandiose",
    "grandiosity",
    "reckless",
    "not sleeping",
    "sleep deprivation",
    "bizarre behavior",
    "acting invincible",
    "won't stop moving",
    "not eating",
    "barely eating",
    "hardly eating",
    "barely sleeping",
    "hardly slept",
    "special mission",
    "normal rules do not apply",
    "emergency psychiatric",
    "psychiatric emergency",
    "behavioral emergency",
    "remove keys",
    "remove weapons",
    "crisis red flags",
}

_MENTAL_HEALTH_CRISIS_SUPPORT_METADATA_MARKERS = {
    "psychological first aid",
    "peer support",
    "panic attack",
    "grief",
    "self-care",
    "self care",
    "calming",
    "grounding",
    "coping",
    "stress",
    "anxiety",
    "withdrawal",
    "dissociation",
    "flashback",
    "journaling",
    "journal",
    "sleep hygiene",
    "bedtime routine",
    "daily routine",
    "routine self-care",
    "routine stability",
    "orientation",
    "redirect",
    "redirection",
    "simple task",
    "sit down",
    "self-management",
    "self management",
    "basic function",
    "basic functioning",
}

_MENTAL_HEALTH_CRISIS_SLEEP_SELF_MANAGEMENT_METADATA_MARKERS = {
    "sleep & insomnia management",
    "sleep and insomnia management",
    "sleep hygiene",
    "bedtime routine",
    "daily routine",
    "routine self-care",
    "routine stability",
    "routine reset",
    "maintain routine",
    "maintaining routine",
    "basic function",
    "basic functioning",
    "journal",
    "journaling",
    "how to handle racing thoughts and worry loops",
    "daily habits that lower baseline anxiety",
    "simple relaxation methods",
    "if you can't sleep because of stress and noise",
}

_MENTAL_HEALTH_CRISIS_DISTRACTOR_METADATA_MARKERS = {
    "dementia",
    "wandering",
    "wandering prevention",
    "safer searching",
    "routine stability",
    "caregiver communication",
    "orientation",
    "tracking for security",
    "animal tracking",
    "building inspection",
    "home sick care",
    "post-death",
    "death notification",
    "palliative care",
    "managing death",
    "self-defense fundamentals",
    "earthquake response",
}

_GERIATRIC_OR_COGNITIVE_DECLINE_QUERY_MARKERS = {
    "elder",
    "elderly",
    "older adult",
    "older adults",
    "older person",
    "older people",
    "senior",
    "seniors",
    "geriatric",
    "aging parent",
    "aging mother",
    "aging father",
    "grandma",
    "grandmother",
    "grandpa",
    "grandfather",
    "dementia",
    "alzheimer",
    "alzheimers",
    "memory loss",
    "cognitive decline",
    "cognitive impairment",
    "cognitively declining",
    "age-related decline",
    "age related decline",
}

_ELDER_COGNITIVE_METADATA_MARKERS = {
    "elder care",
    "elder-care",
    "elderly",
    "older adult",
    "older adults",
    "senior",
    "geriatric",
    "aging parent",
    "age-related",
    "age related",
    "dementia",
    "alzheimer",
    "alzheimers",
    "memory loss",
    "cognitive decline",
    "cognitive impairment",
    "wandering",
}

_ALCOHOL_WITHDRAWAL_POSITIVE_METADATA_MARKERS = {
    "withdrawal",
    "alcohol withdrawal",
    "delirium tremens",
    "detox",
    "addiction",
    "substance use",
}

_INVASIVE_MEDICAL_METADATA_MARKERS = {
    "field surgery",
    "surgery",
    "debridement",
    "wound closure",
    "suturing",
    "suture",
    "amputation",
    "anesthesia",
    "surgical",
}

_EXPLICIT_INVASIVE_QUERY_MARKERS = {
    "suture",
    "sutures",
    "stitch",
    "stitches",
    "debridement",
    "debride",
    "surgery",
    "surgical",
    "operate",
    "operation",
    "amputation",
    "amputate",
    "anesthesia",
    "anesthetic",
    "close the wound",
    "pack the wound",
}

_GENERIC_PUNCTURE_QUERY_MARKERS = {
    "puncture wound",
    "puncture",
    "stepped on a nail",
    "nail wound",
}

_CHARCOAL_SAND_WATER_FILTER_QUERY_MARKERS = {
    "charcoal sand water filter",
    "charcoal sand filter",
    "build a charcoal sand water filter",
    "make a charcoal sand water filter",
}

_REUSED_CONTAINER_WATER_QUERY_MARKERS = {
    "reused containers",
    "reused container",
    "salvaged containers",
    "salvaged container",
    "reused bottles",
    "reused bottle",
    "used bottles",
    "used bottle",
    "old bottle",
    "old bottles",
    "old soda bottle",
    "old soda bottles",
    "plastic bottle",
    "plastic bottles",
    "old jug",
    "old jugs",
    "milk jug",
    "milk jugs",
    "store water safely in reused containers",
}

_WATER_WITHOUT_FUEL_QUERY_MARKERS = {
    "without fuel",
    "no fuel",
    "no fuel for boiling",
    "can't boil",
    "cannot boil",
    "without boiling",
    "no way to boil",
    "fuel is scarce",
    "fuel is limited",
}

_WATER_WITHOUT_FUEL_SPECIAL_CASE_EXCLUSION_MARKERS = {
    "stay quiet",
    "keep warm",
    "getting dark",
    "wounded person",
    "wounded",
    "well is contaminated",
    "the well is contaminated",
    "collapsed well",
    "people are sick",
    "two people are sick",
    "group of",
    "30 people",
}

_PUNCTURE_SPECIAL_CASE_EXCLUSION_MARKERS = {
    "antibiotic",
    "antibiotics",
    "infected",
    "infection",
    "pus",
    "red streak",
    "fever",
    "foreign body",
    "splinter",
    "embedded",
    "remove",
    "extraction",
    "eye",
    "eyeball",
    "chest",
    "abdomen",
    "belly",
    "joint",
    "mouth",
    "face",
    "rabies",
    "tetanus",
    "stitch",
    "stitches",
}

_SEVERE_BLEED_QUERY_MARKERS = {
    "severe bleeding",
    "uncontrolled bleeding",
    "won't stop bleeding",
    "spurting",
    "gushing",
    "arterial",
    "hemorrhage",
    "blood everywhere",
    "mangled",
    "amputation",
}

_PUNCTURE_CONSERVATIVE_METADATA_MARKERS = {
    "wound hygiene",
    "infection prevention",
    "first aid",
    "wound management",
    "tetanus",
    "special wound considerations",
    "first aid & emergency response",
    "first aid essentials",
    "wound assessment and cleaning",
    "wound hygiene, infection prevention & field sanitation",
    "wound cleaning",
    "do not probe",
    "infection monitoring",
    "chronic wound care",
    "quick wound assessment guide",
}

_PUNCTURE_DISTRACTOR_METADATA_MARKERS = {
    "animal bite wound care & rabies post-exposure protocols",
    "animal bite",
    "bite wound",
    "rabies",
    "eye injuries",
    "ophthalmology",
    "emergency dental procedures",
    "oral anatomy",
    "diabetic foot",
    "diabetes management",
}

_HERBAL_METADATA_MARKERS = {
    "herbal",
    "honey",
    "natural medicine",
}

_EXPLICIT_NATURAL_REMEDY_QUERY_MARKERS = {
    "herbal",
    "natural",
    "plant",
    "honey",
    "no antibiotics",
    "no medicine",
    "no medical supplies",
}

_SUPPLY_CONFLICT_TRIGGER_MARKERS = {
    "arguing",
    "dispute",
    "conflict",
    "hoard",
    "hoarding",
    "fighting",
    "avoid violence",
    "fair process",
    "who should control",
}

_SUPPLY_RESOURCE_MARKERS = {
    "supplies",
    "supply",
    "food",
    "water",
    "resource",
    "resources",
    "ration",
    "rations",
    "inventory",
}

_LOW_BUREAUCRACY_CONFLICT_METADATA_MARKERS = {
    "de-escalation",
    "mediation",
    "mediat",
    "active listening",
    "cooling-off",
    "conflict resolution",
    "fairness",
    "decision-making processes",
    "group morale",
    "transparent",
    "rotate",
    "public ledger",
    "visible inventory",
    "shared values",
}

_FORMAL_ADJUDICATION_METADATA_MARKERS = {
    "court",
    "judge",
    "judges",
    "jury",
    "hearing",
    "binding verdict",
    "adjudication",
    "property rights",
    "land law",
    "inheritance",
    "compensation bond",
    "formal governance",
    "overseer",
    "project leader",
    "appeal",
    "formal investigation",
}

_EXPLICIT_FORMAL_GOVERNANCE_QUERY_MARKERS = {
    "court",
    "judge",
    "jury",
    "tribunal",
    "hearing",
    "legal",
    "law",
    "property rights",
    "binding verdict",
}

_SUPPLY_CONFLICT_SPECIAL_CASE_EXCLUSION_MARKERS = (
    _EXPLICIT_FORMAL_GOVERNANCE_QUERY_MARKERS
    | {
        "theft",
        "thief",
        "stolen",
        "steal",
        "crime",
        "criminal",
        "punish",
        "punishment",
        "sentence",
        "trial",
        "police",
        "investigate",
        "investigation",
        "evidence",
        "armed",
        "weapon",
        "shoot",
        "attack",
        "assault",
        "raid",
        "murder",
        "killed",
    }
)

_THEATER_METADATA_MARKERS = {
    "theater",
    "storytelling",
    "performance",
}

_VEHICLE_QUERY_MARKERS = {
    "car",
    "vehicle",
    "truck",
    "engine",
    "starter",
    "jump start",
    "jumper",
    "flat tire",
    "tire",
    "spare",
    "alternator",
    "battery",
    "batteries",
    "automotive",
    "mobility",
    "transport",
}

_CAR_SPECIFIC_QUERY_MARKERS = {
    "car",
    "vehicle",
    "truck",
    "engine",
    "starter",
    "jump start",
    "jumper",
    "spare",
    "spare tire",
    "no spare",
    "flat tire",
    "alternator",
    "car battery",
    "automotive",
    "roadside",
}

_BATTERY_QUERY_MARKERS = {
    "battery",
    "batteries",
    "lead-acid",
    "car battery",
    "alternator",
    "12v",
}

_BICYCLE_METADATA_MARKERS = {
    "bicycle",
    "bike",
    "cycle",
    "tube patch",
    "inner tube",
}

_VEHICLE_METADATA_MARKERS = {
    "vehicle",
    "car",
    "automotive",
    "fleet",
    "mobility",
    "tire",
    "starter",
    "battery",
    "alternator",
    "repair",
}

_SPLINTER_QUERY_MARKERS = {
    "splinter",
    "foreign body",
    "embedded metal",
    "embedded glass",
    "embedded wood",
}

_SPLINTER_MEDICAL_METADATA_MARKERS = {
    "foreign body",
    "splinter",
    "puncture",
    "wound management",
    "first aid",
    "debridement",
}

_EYE_METADATA_MARKERS = {
    "eye",
    "ophthalmology",
    "corneal",
    "vision",
}

_ANIMAL_BITE_QUERY_MARKERS = {
    "animal bite",
    "dog bite",
    "cat bite",
    "rabies",
    "bitten by",
}

_FLAT_TIRE_QUERY_MARKERS = {
    "flat tire",
    "spare tire",
    "no spare",
    "tire plug",
    "roadside",
}

_GLASS_QUERY_MARKERS = {
    "make glass",
    "making glass",
    "glass from scratch",
    "glassmaking",
}

_GLASS_POSITIVE_METADATA_MARKERS = {
    "glassmaking",
    "glass making",
    "raw materials",
    "furnace construction",
    "silica",
    "soda ash",
    "annealing",
    "forming techniques",
    "glass-ceramics",
    "glass-making-raw-materials",
}

_GLASS_DISTRACTOR_METADATA_MARKERS = {
    "hourglass",
    "flat glass cutting",
    "flat glass reuse",
    "window glass",
    "plastics, rubber & glass identification",
    "industrial applications",
    "waterglass",
    "sodium silicate",
}

_PAPER_INK_QUERY_MARKERS = {
    "paper and ink",
    "make paper",
    "making paper",
    "make ink",
    "making ink",
}

_PAPER_POSITIVE_METADATA_MARKERS = {
    "papermaking",
    "paper making",
    "pulp",
    "sheet formation",
    "sizing",
    "writing surfaces",
    "rag paper",
}

_INK_POSITIVE_METADATA_MARKERS = {
    "ink making",
    "ink & pigment chemistry",
    "carbon black",
    "iron gall",
    "binder",
    "gum arabic",
    "pigment",
}

_PAPER_INK_DISTRACTOR_METADATA_MARKERS = {
    "printing press",
    "movable type",
    "newsletter",
}

_BOW_ARROW_QUERY_MARKERS = {
    "bow and arrow",
    "bow and arrows",
    "make a bow",
    "make arrows",
    "bowstring",
    "fletching",
    "self-bow",
    "self bow",
}

_BOW_ARROW_POSITIVE_METADATA_MARKERS = {
    "bow making",
    "arrow shaft",
    "bowstring",
    "fletching",
    "tillering",
    "self-bow",
    "self bow",
    "archery",
    "point hafting",
}

_BOW_ARROW_DISTRACTOR_METADATA_MARKERS = {
    "toy making",
    "entertainment & culture",
}

_NO_GEAR_FISHING_QUERY_MARKERS = {
    "without fishing gear",
    "no fishing gear",
    "without gear",
    "no hook",
    "no hooks",
    "no rod",
    "no net",
}

_NO_GEAR_FISHING_POSITIVE_METADATA_MARKERS = {
    "primitive fishing",
    "fishing techniques",
    "fish trap",
    "passive fish trap",
    "weir",
    "basket trap",
    "spearing",
    "hand fishing",
}

_NO_GEAR_FISHING_DISTRACTOR_METADATA_MARKERS = {
    "sporting goods stores",
    "rural scavenging",
}

_NATURAL_DYE_QUERY_MARKERS = {
    "dye from plants",
    "dyes from plants",
    "plant dyes",
    "natural dye",
    "natural dyes",
    "dye bath",
}

_NATURAL_DYE_POSITIVE_METADATA_MARKERS = {
    "natural dyes",
    "plant-based dyes",
    "mordant",
    "dye bath",
    "color fastness",
    "indigo",
    "woad",
    "extraction methods",
}

_NATURAL_DYE_DISTRACTOR_METADATA_MARKERS = {
    "ink making",
    "paint",
}

_WELD_WITHOUT_WELDER_QUERY_MARKERS = {
    "weld without a welder",
    "without a welder",
    "without welder",
    "no welder",
    "join metal without a welder",
    "join steel without a welder",
    "join iron without a welder",
    "join metal without electricity",
    "join metal without welding machine",
}

_WELD_POSITIVE_METADATA_MARKERS = {
    "forge welding",
    "fire welding",
    "brazing",
    "soldering",
    "oxy-acetylene",
    "joint integrity",
    "joining techniques",
    "welding metallurgy & joint integrity",
    "basic forge operation",
    "forging & metalwork",
    "metalworking & blacksmithing",
}

_WELD_DISTRACTOR_METADATA_MARKERS = {
    "inspection",
    "testing weld integrity",
    "quality control",
    "converting alternators to welders",
    "post-weld heat treatment",
    "anti-counterfeit",
    "minting",
    "coin",
    "foundry",
    "casting",
    "charcoal & fuels",
}

_CLAY_OVEN_QUERY_MARKERS = {
    "clay oven",
    "bread oven",
    "cob oven",
}

_CLAY_OVEN_POSITIVE_METADATA_MARKERS = {
    "clay bread oven construction",
    "cob oven",
    "thermal mass",
    "dome construction",
    "curing",
    "brick bread",
    "pizza ovens",
}

_ROMAN_CONCRETE_QUERY_MARKERS = {
    "roman concrete",
    "ancient romans make concrete",
}

_ROMAN_CONCRETE_POSITIVE_METADATA_MARKERS = {
    "roman concrete",
    "opus caementicium",
    "pozzolan",
    "pozzolanic",
    "slaking lime",
    "ancient construction",
    "volcanic ash",
}

_ROMAN_CONCRETE_DISTRACTOR_METADATA_MARKERS = {
    "testing",
    "quality assurance",
    "slump",
    "cylinder testing",
    "reading archaeological sites",
    "conclusion",
}

_SMOKE_MEAT_QUERY_MARKERS = {
    "smoke meat",
    "smoking meat",
    "meat to preserve",
}

_SMOKE_MEAT_POSITIVE_METADATA_MARKERS = {
    "smoking & curing meat",
    "cold smoking",
    "hot smoking",
    "brining",
    "smokehouse",
    "food-preservation",
    "jerky",
}

_RAFT_LAKE_QUERY_MARKERS = {
    "raft to cross a lake",
    "raft across a lake",
    "cross a lake",
}

_RAFT_LAKE_POSITIVE_METADATA_MARKERS = {
    "improvised flotation",
    "boat building",
    "waterway transport",
    "raft",
    "buoyancy",
    "lashing",
}

_RAFT_LAKE_DISTRACTOR_METADATA_MARKERS = {
    "road building",
    "water rescue",
    "rope-assisted crossing",
}

_STARTER_BUILD_QUERY_MARKERS = {
    "how do i build",
    "how do we build",
    "how can i build",
    "how can we build",
    "how to build",
    "build a",
    "build an",
    "construct a",
    "construct an",
    "make a",
    "make an",
}

_HOUSE_BUILD_QUERY_MARKERS = {
    "house",
    "home",
    "cabin",
    "hut",
    "homestead",
}

_HOUSE_BUILD_POSITIVE_METADATA_MARKERS = {
    "primitive shelter construction techniques",
    "accessible shelter",
    "waterproofing and sealants",
    "window and door assembly",
    "passive solar design",
    "shelter construction",
    "primitive shelter",
    "cabin",
    "hut",
    "site selection",
    "drainage",
    "foundation",
    "floor system",
    "wall framing",
    "roof framing",
    "roofing",
    "weatherproofing",
    "rainproofing",
    "insulation",
    "ventilation",
    "earth-sheltering",
    "window frame",
    "weatherstripping",
    "threshold",
    "site drainage",
    "roof waterproofing",
    "construction & carpentry",
    "doors & window construction",
    "wall construction",
    "foundations",
    "rainproofing and water shedding",
    "shelter site selection & hazard assessment",
}

_HOUSE_BUILD_DISTRACTOR_METADATA_MARKERS = {
    "population census",
    "household surveys",
    "record organization",
    "record storage",
    "demographics",
    "vital records",
    "urban sociology",
    "housing policy",
    "household",
    "cabinet",
    "greenhouse",
    "medicine cabinet",
    "kitchen cabinet",
    "accessible shelter",
    "universal design",
    "salvage",
    "demolition",
    "building materials salvage",
    "salvage & recovery",
    "roofing materials",
}

_SMALL_WATERCRAFT_QUERY_MARKERS = {
    "canoe",
    "boat",
    "watercraft",
    "dugout",
    "coracle",
    "kayak",
    "rowboat",
    "skiff",
}

_SMALL_WATERCRAFT_POSITIVE_METADATA_MARKERS = {
    "small watercraft construction",
    "transportation & infrastructure",
    "shipbuilding & boats",
    "waterproofing and sealants",
    "boat building",
    "watercraft",
    "dugout canoe",
    "plank canoe",
    "hull",
    "ribs",
    "planking",
    "caulking",
    "buoyancy",
    "paddle",
    "oar",
    "sealing",
    "hollowed log",
    "boat caulking",
    "shoreline test",
    "boat building & watercraft",
}

_SMALL_WATERCRAFT_DISTRACTOR_METADATA_MARKERS = {
    "water rescue",
    "ferry operations",
    "shipping",
    "harbor",
    "portage trails",
    "bridge approaches",
    "road building",
    "rescue doctrine",
    "commercial shipping",
}

_WATER_PURIFICATION_QUERY_MARKERS = {
    "purify water",
    "water purification",
    "clean water",
    "safe drinking water",
    "water filter",
    "filter water",
    "filtration",
    "charcoal sand water filter",
    "charcoal sand filter",
    "make water safe to drink",
}

_WATER_PURIFICATION_POSITIVE_METADATA_MARKERS = {
    "water purification",
    "survival water purification selection guide",
    "filtration systems",
    "chemical treatment",
    "water safety basics",
    "combining purification methods",
    "testing water quality",
    "solar disinfection",
    "distillation",
    "boiling",
    "water still contaminated troubleshooter",
    "sanitation & public health",
    "chlorine & bleach production",
    "desalination systems",
    "simple water treatment methods",
    "water finding & emergency purification",
    "water supply protection",
    "disinfection & sterilization",
    "biosand filter",
    "water storage",
}

_WATER_PURIFICATION_DISTRACTOR_METADATA_MARKERS = {
    "alkali & soda production",
    "aquatic biology",
    "fisheries",
    "irrigation",
    "greywater recycling",
    "waterway",
    "canal",
    "lock",
    "harbor",
    "boat building",
    "camp outbreak",
    "dysentery operations",
    "case tracking",
    "latrine inspection",
    "handwashing enforcement",
    "food-service restrictions",
    "charcoal & fuels",
    "activated charcoal",
    "water source checks",
}

_WATER_STORAGE_QUERY_MARKERS = {
    "store water",
    "water storage",
    "hold water",
    "keep water",
    "reused containers",
    "reused container",
    "safe containers",
    "container sanitation",
    "water drum",
    "water barrel",
    "water bucket",
    "water bottle",
    "water jug",
    "ration water",
}

_WATER_STORAGE_POSITIVE_METADATA_MARKERS = {
    "water storage",
    "water storage systems",
    "rationing protocol",
    "sanitation & public health",
    "water system design and distribution",
    "container construction",
    "gravity-fed distribution",
    "rainwater harvesting",
    "water harvesting systems",
    "water storage sanitation",
    "water supply protection",
    "distribution system components",
    "clean container",
    "contamination prevention",
    "container selection and preparation",
    "food-grade plastic",
    "food safe",
    "sealed container",
}

_WATER_STORAGE_DISTRACTOR_METADATA_MARKERS = {
    "alkali & soda production",
    "aquatic biology",
    "fisheries",
    "irrigation",
    "waterway",
    "boat building",
    "harbor",
    "shipping",
    "desalination",
    "camp outbreak",
    "dysentery operations",
    "case tracking",
    "latrine inspection",
}

_FIRE_IN_RAIN_QUERY_MARKERS = {
    "start a fire in the rain",
    "start a fire in rain",
    "fire in wet conditions",
    "fire in winter conditions",
    "wet weather fire",
    "wet wood fire",
    "start a fire when everything is wet",
    "start a fire with wet wood",
}

_FIRE_IN_RAIN_POSITIVE_METADATA_MARKERS = {
    "fire by friction methods",
    "fire in wet conditions",
    "fire in winter conditions",
    "fire starting",
    "tinder preparation",
    "fire bundle",
    "wood species selection",
    "kindling",
    "char cloth",
    "feather stick",
    "wet wood",
    "survival basics",
    "winter survival systems",
    "swamp & wetland survival systems",
}

_FIRE_IN_RAIN_DISTRACTOR_METADATA_MARKERS = {
    "fire suppression",
    "wildland fire",
    "fire safety and compartmentalization",
    "firearms",
    "heat illness",
    "smoke inhalation",
    "brick making",
    "anti-counterfeit",
}

_CAST_WITHOUT_FOUNDRY_QUERY_MARKERS = {
    "cast metal without a foundry",
    "without a foundry",
    "without foundry",
    "no foundry",
}

_CAST_WITHOUT_FOUNDRY_POSITIVE_METADATA_MARKERS = {
    "sand casting",
    "lost-wax",
    "casting techniques",
    "refractory mold",
    "non-ferrous",
    "casting and solidification",
    "forge/furnace",
}

_FAIR_TRIAL_QUERY_MARKERS = {
    "fair trial",
    "trial with no lawyers",
    "no lawyers or judges",
    "community trial",
    "lay tribunal",
}

_FAIR_TRIAL_POSITIVE_METADATA_MARKERS = {
    "trial procedure",
    "criminal justice procedures for small communities",
    "evidence standards",
    "judicial independence",
    "dispute resolution hierarchy",
    "restorative justice",
    "community courts",
    "rule of law",
}

_FAIR_TRIAL_DISTRACTOR_METADATA_MARKERS = {
    "property law",
    "market organization",
    "federation",
    "democratic governance",
}

_SOAP_RESTOCK_QUERY_MARKERS = {
    "ran out of soap",
    "make more soap",
    "make more",
}

_SOAPMAKING_QUERY_MARKERS = {
    "make soap",
    "making soap",
    "soap making",
    "soap from animal fat",
    "ash and animal fat",
    "ash & animal fat",
    "saponification",
    "wood ash lye",
    "lye water",
    "tallow soap",
    "lard soap",
}

_SOAP_POSITIVE_METADATA_MARKERS = {
    "making soap",
    "soap making",
    "saponification",
    "lye",
    "ash & animal fat",
    "ash soap",
    "hot process",
    "cold process",
}

_SOAP_DISTRACTOR_METADATA_MARKERS = {
    "candle",
    "candles",
    "lighting production",
}

_BRAIN_TANNING_QUERY_MARKERS = {
    "tan leather with brains",
    "brain tanning",
    "tan a hide with brains",
}

_BRAIN_TANNING_POSITIVE_METADATA_MARKERS = {
    "brain tanning",
    "hide tanning",
    "buckskin",
    "smoke finish",
    "brain emulsion",
    "oil tanning",
}

_BRAIN_TANNING_DISTRACTOR_METADATA_MARKERS = {
    "vegetable tanning",
    "bark tanning",
    "applications:",
    "conclusion",
    "cattle & large stock processing",
}

_HOT_BURIAL_QUERY_MARKERS = {
    "body for burial in hot weather",
    "burial in hot weather",
    "preserve a body",
    "hot weather burial",
}

_HOT_BURIAL_POSITIVE_METADATA_MARKERS = {
    "mortuary science",
    "preparation of the body",
    "burial practices",
    "grave management",
    "immediate post-death care",
    "death care",
}

_HOT_BURIAL_DISTRACTOR_METADATA_MARKERS = {
    "cold weather survival",
    "avalanche",
    "pandemic",
    "mass casualty",
}

_BODY_PART_MARKERS = (
    "hand",
    "finger",
    "thumb",
    "palm",
    "wrist",
    "arm",
    "leg",
    "foot",
    "heel",
    "skin",
)

_RETRIEVAL_CANDIDATE_MULTIPLIER = 3
_RETRIEVAL_CANDIDATE_CAP = 72
_RERANK_SECTION_SOFT_CAP = 2
_RERANK_FAMILY_SOFT_CAP = 4

_GENERIC_QUERY_FILLER_WORDS = {
    "a",
    "an",
    "and",
    "are",
    "can",
    "could",
    "do",
    "does",
    "did",
    "for",
    "how",
    "i",
    "if",
    "in",
    "is",
    "it",
    "me",
    "my",
    "of",
    "should",
    "someone",
    "something",
    "that",
    "the",
    "this",
    "to",
    "we",
    "what",
    "when",
    "where",
    "why",
    "you",
}

_INVENTORY_LEAD_WORDS = {
    "a",
    "an",
    "one",
    "two",
    "three",
    "four",
    "five",
    "half-full",
    "half",
    "full",
    "empty",
    "spare",
    "single",
    "extra",
    "some",
    "several",
}

_INVENTORY_NEGATION_WORDS = {
    "no",
    "not",
    "without",
    "broken",
    "empty",
    "missing",
    "contaminated",
}

_DOMAIN_CATEGORY_TARGETS = {
    "medical": {"medical", "biology", "chemistry", "survival"},
    "psychological": {"medical", "society", "culture-knowledge", "survival"},
    "shelter": {"building", "survival", "resource-management", "textiles-fiber-arts"},
    "water": {"utility", "resource-management", "chemistry", "survival", "building"},
    "fire": {"survival", "chemistry", "building", "utility", "power-generation"},
    "food": {"agriculture", "biology", "survival", "resource-management"},
    "navigation": {"transportation", "sciences", "communications", "survival"},
    "defense": {"defense", "society", "survival"},
    "construction": {
        "building",
        "transportation",
        "crafts",
        "utility",
        "resource-management",
        "survival",
    },
}

_SCENARIO_STOPWORDS = _GENERIC_QUERY_FILLER_WORDS | {
    "all",
    "also",
    "around",
    "back",
    "before",
    "from",
    "into",
    "just",
    "more",
    "need",
    "now",
    "out",
    "over",
    "please",
    "still",
    "than",
    "then",
    "there",
    "through",
    "too",
    "up",
    "very",
}

_CONSTRAINT_MARKERS = (
    "no ",
    "without ",
    "limited ",
    "only ",
    "can't ",
    "cannot ",
    "won't ",
    "nearest ",
    "before ",
    "getting dark",
    "below freezing",
    "running low",
    "collapsed",
    "contaminated",
    "destroyed",
    "failed",
    "approaching",
)

_HAZARD_MARKERS = {
    "bleeding": "bleeding",
    "wounded": "injury",
    "injured": "injury",
    "unconscious": "unconscious patient",
    "vomiting": "vomiting",
    "throwing up": "vomiting",
    "seizure": "seizure",
    "poisoning": "poisoning",
    "snake bite": "snake bite",
    "bite": "bite injury",
    "contaminated": "contamination",
    "wildfire": "wildfire",
    "flood": "flooding",
    "storm": "storm damage",
    "hurricane": "storm damage",
    "monsoon": "heavy rain",
    "freezing": "freezing exposure",
    "cold": "cold exposure",
    "wet": "wet exposure",
    "dark": "darkness",
    "fire": "fire risk",
    "smoke": "smoke exposure",
    "armed": "security threat",
    "strangers": "security threat",
}

_PEOPLE_PATTERNS = (
    r"\b\d+\s+people\b",
    r"\b\d+\s+survivors\b",
    r"\bpregnant woman\b",
    r"\bwounded person\b",
    r"\bunconscious\b",
    r"\bdehydrated child\b",
    r"\bchild\b",
    r"\bkid\b",
    r"\badult\b",
    r"\bgroup\b",
    r"\bperson\b",
    r"\bpeople\b",
)

_ENVIRONMENT_MARKERS = (
    "storm",
    "rain",
    "wildfire",
    "flood",
    "valley",
    "monsoon",
    "winter",
    "freezing",
    "cold",
    "heat",
    "night",
    "dark",
    "river",
    "well",
    "camp",
)

_CONTAINER_MARKERS = {
    "bucket",
    "buckets",
    "container",
    "containers",
    "drum",
    "drums",
    "bottle",
    "bottles",
    "barrel",
    "barrels",
    "jug",
    "jugs",
}

_EVACUATE_MARKERS = {
    "evacuate",
    "leave",
    "move",
    "get to safety",
    "shelter in place",
    "stay put",
    "stay here",
    "travel now",
}

_SHELTER_DECISION_MARKERS = {
    "shelter",
    "shelter in place",
    "stay put",
    "stay here",
    "sleep tonight",
}

_WEATHER_MARKERS = {
    "storm",
    "rain",
    "wildfire",
    "flood",
    "monsoon",
    "hurricane",
    "cold",
    "freezing",
}

_SIGNALING_QUERY_MARKERS = {
    "signal a rescue plane",
    "signal rescue plane",
    "signal a plane",
    "rescue plane",
    "signal rescue",
    "signaling",
}

_TRIAGE_MARKERS = {
    "one person",
    "two people",
    "three people",
    "multiple patients",
    "triage",
    "who gets treated first",
    "what order",
    "prioritize them",
}

_SESSION_CONTEXT_HINTS = (
    "what now",
    "what next",
    "and then",
    "what about",
    "how long",
    "should we",
    "do we",
    "next step",
    "next steps",
)

_ANCHOR_FOLLOW_UP_HINTS = _SESSION_CONTEXT_HINTS + (
    "after that",
    "for children",
    "for child",
    "how many times",
    "how often",
)

_ANCHOR_RESET_MARKERS = (
    "unrelated:",
    "new question:",
    "switching topics:",
    "different question:",
)

_ANCHOR_DEICTIC_TOKENS = {
    "after",
    "before",
    "it",
    "next",
    "that",
    "the",
    "this",
    "those",
}

_SYSTEM_BEHAVIOR_QUERY_MARKERS = (
    "what should the answer do",
    "what should the response do",
    "how should the answer",
    "how should the response",
    "drifting into",
    "muddled in the answer",
    "muddled in the response",
    "citation hygiene",
    "wrong citation",
)

_OFF_TOPIC_ENTERTAINMENT_MARKERS = {
    "movie",
    "movies",
    "film",
    "films",
    "tv",
    "television",
    "show",
    "shows",
    "song",
    "songs",
    "album",
    "albums",
    "band",
    "bands",
    "actor",
    "actors",
    "actress",
    "actresses",
    "celebrity",
    "celebrities",
}

_ABSTRACT_OFF_TOPIC_MARKERS = {
    "meaning of life",
    "purpose of life",
    "why are we here",
    "what is life about",
}

_NONSENSE_OFF_TOPIC_MARKERS = {
    "quick brown fox jumps over the lazy dog",
}

_UNDERSPECIFIED_STUB_QUERIES = {
    "help",
    "water",
}

_BROAD_SURVEY_QUERY_MARKERS = {
    "tell me everything about",
    "everything about",
    "all about",
}

_BROAD_SURVEY_TOPIC_MARKERS = {
    "medical care",
    "medicine",
    "healthcare",
}

_HAZARDOUS_UNSUPPORTED_QUERY_MARKERS = {
    "nuclear reactor",
    "nuclear power plant",
    "nuclear bomb",
    "nuclear weapon",
}

_UNKNOWN_MEDICATION_MARKERS = {
    "pill",
    "pills",
    "tablet",
    "tablets",
    "capsule",
    "capsules",
    "medication",
    "medications",
    "medicine",
    "medicines",
}

_UNKNOWN_MEDICATION_UNCERTAINTY_MARKERS = {
    "unmarked",
    "unlabeled",
    "unknown",
    "no label",
    "no labels",
    "no markings",
    "without original packaging",
    "found some pills",
}

_ANTIBIOTIC_SYNTHESIS_MARKERS = {
    "make penicillin",
    "extract penicillin",
    "produce penicillin",
    "brew penicillin",
    "grow penicillin",
    "make antibiotics",
}

_DENTAL_INFECTION_SPECIAL_CASE_MARKERS = {
    "tooth is infected",
    "tooth infection",
    "tooth abscess",
    "dental abscess",
}

_FACIAL_SWELLING_MARKERS = {
    "face is swelling",
    "face swelling",
    "jaw swelling",
    "cheek swelling",
    "swelling in my face",
}

_NONPHARMA_PAIN_SPECIAL_CASE_MARKERS = {
    "no painkillers",
    "no pain meds",
    "no pain medicine",
    "no pain medication",
}

_GENERIC_SEIZURE_SPECIAL_CASE_MARKERS = {
    "someone is having a seizure",
    "having a seizure",
    "has a seizure",
    "first seizure",
    "first seizure in an adult",
    "convulsing",
    "convulsion",
}

_SEIZURE_SPECIAL_CASE_EXCLUSION_MARKERS = {
    "status epilepticus",
    "midazolam",
    "lorazepam",
    "diazepam",
    "benzodiazepine",
    "dose",
    "dosage",
    "dosing",
    "pediatric",
    "child",
    "infant",
    "febrile",
    "pregnant",
    "eclampsia",
    "poison",
    "overdose",
    "withdrawal",
}

_GENERIC_SEVERE_BURN_SPECIAL_CASE_MARKERS = {
    "burned badly",
    "badly burned",
    "severe burn",
    "serious burn",
}

_CHEST_TRAUMA_SPECIAL_CASE_TRAUMA_MARKERS = {
    "chest trauma",
    "chest wound",
    "stab wound to the chest",
    "stabbed in the chest",
    "stabbing to the chest",
    "penetrating chest injury",
    "penetrating chest trauma",
    "blunt chest trauma",
    "hard hit to the chest",
    "hit to the chest",
    "after a fall",
    "after blunt chest trauma",
}

_CHEST_TRAUMA_SPECIAL_CASE_SIGNS = {
    "trouble breathing",
    "difficulty breathing",
    "shortness of breath",
    "bubbling air",
    "sucking chest wound",
    "collapsed lung",
    "pneumothorax",
    "tension pneumothorax",
    "one side is not moving",
    "one side isnt moving",
    "one side isn't moving",
    "trachea seems shifted",
    "tracheal deviation",
    "jvd",
    "neck vein distension",
}

_ABUSE_IMMEDIATE_SAFETY_RELATIONSHIP_MARKERS = {
    "my partner",
    "my ex",
    "the person who hurt me",
    "someone at home is hurting them",
    "sexually assaulted",
    "sexual assault",
    "abuser",
}

_ABUSE_IMMEDIATE_SAFETY_DANGER_MARKERS = {
    "won't let me leave",
    "wont let me leave",
    "keeps taking my phone",
    "taking my phone",
    "tracking my phone",
    "they are tracking my phone",
    "kill themselves if i leave",
    "will kill themselves if i leave",
    "have to go back tonight",
    "in the house right now",
    "bleeding",
}

_GENERIC_BROKEN_ARM_SPECIAL_CASE_MARKERS = {
    "set a broken arm",
    "reset a broken arm",
    "broken arm",
}

_GENERIC_BROKEN_LEG_SPECIAL_CASE_MARKERS = {
    "set a broken leg",
    "reset a broken leg",
    "broken leg",
}

_GENERIC_HYPOTHERMIA_SPECIAL_CASE_MARKERS = {
    "signs of hypothermia",
    "symptoms of hypothermia",
    "hypothermia signs",
}

_FRACTURE_SPECIAL_CASE_EXCLUSION_MARKERS = {
    "dislocated",
    "dislocation",
    "finger",
    "toe",
    "triage",
    "three people",
    "multiple patients",
    "compound scenario",
    "can't breathe",
    "cant breathe",
}

_ANIMAL_BITE_SPECIAL_CASE_EXCLUSION_MARKERS = {
    "snake",
    "venomous",
    "amoxicillin",
    "doxycycline",
    "antibiotic",
    "pep dose",
    "post-exposure prophylaxis",
    "rabies vaccine",
}

_GENERIC_JELLYFISH_SPECIAL_CASE_MARKERS = {
    "jellyfish sting",
    "stung by a jellyfish",
    "peeing on a jellyfish",
}

_SOLAR_WATER_SPECIAL_CASE_MARKERS = {
    "purify water with nothing but sunlight",
    "purify water with only sunlight",
    "nothing but sunlight",
    "only sunlight",
}

_GENERIC_HYPOTHERMIA_SPECIAL_CASE_EXCLUSION_MARKERS = {
    "frostbite",
    "cold water",
    "immersion",
    "ice water",
    "afterdrop",
    "rewarming rate",
    "infant",
    "toddler",
    "child",
    "baby",
}

_ZERO_RESOURCE_KNIFE_SPECIAL_CASE_MARKERS = {
    "knife out of literally nothing",
    "knife from literally nothing",
    "knife out of nothing",
    "knife from nothing",
}

_ZERO_RESOURCE_KNIFE_SPECIAL_CASE_EXCLUSION_MARKERS = {
    "steel",
    "forge",
    "scrap",
    "obsidian",
    "stainless",
    "combat",
    "throwing",
    "kitchen knife",
    "knife steel",
    "heat treat",
    "tempering",
}

_WELD_SPECIAL_CASE_EXCLUSION_MARKERS = {
    "aluminum",
    "stainless",
    "cast iron",
    "pressure vessel",
    "pressure pipe",
    "gas tank",
    "structural beam",
    "car frame",
    "roll cage",
}

_PAPER_INK_SPECIAL_CASE_EXCLUSION_MARKERS = {
    "printing press",
    "newspaper",
    "typeset",
    "archival",
    "iron gall",
    "fountain pen",
    "calligraphy",
    "bookbinding",
    "oak gall",
}

_UNTRAINED_CHILDBIRTH_MARKERS = {
    "deliver a baby",
    "give birth",
    "childbirth",
    "labor",
}

_UNTRAINED_MARKERS = {
    "no medical training",
    "not trained",
    "untrained",
}

_UNKNOWN_CHILD_INGESTION_UNKNOWN_MARKERS = {
    "ate something",
    "ate somethign",
    "swallowed something",
    "drank something",
    "got into something",
    "got into cleaner",
    "got into the cleaner",
    "got into some cleaner",
    "ingested something",
}

_CHILD_QUERY_MARKERS = {
    "kid",
    "child",
    "toddler",
    "baby",
    "infant",
}

_RELEVANT_SCOPE_MARKERS = {
    "survival",
    "shelter",
    "water",
    "fire",
    "injury",
    "wound",
    "bleeding",
    "fracture",
    "storm",
    "generator",
    "car",
    "vehicle",
    "supplies",
    "resource",
    "group",
    "community",
    "evacuate",
    "salvage",
    "rescue",
    "medical",
    "answer",
    "response",
    "citation",
}


def _text_has_marker(text, markers):
    """Return True if any marker appears in text."""
    lower = text.lower()
    for marker in markers:
        if " " in marker:
            if marker in lower:
                return True
        elif re.search(r"\b" + re.escape(marker) + r"\b", lower):
            return True
    return False


def _has_explicit_geriatric_or_cognitive_decline_evidence(question):
    """Return True when the query explicitly indicates elder/cognitive-decline context."""
    lower = question.lower()
    if _text_has_marker(lower, _GERIATRIC_OR_COGNITIVE_DECLINE_QUERY_MARKERS):
        return True

    age_mentions = re.findall(
        r"\b(\d{2,3})\s*(?:-| )?year(?:s)?(?:-| )old\b|\b(\d{2,3})\s*(?:yo|y/o)\b",
        lower,
    )
    for first, second in age_mentions:
        age_text = first or second
        if age_text and int(age_text) >= 65:
            return True
    return False


def _is_explicit_veterinary_query(question):
    """Detect queries that are actually about treating an animal patient."""
    return _text_has_marker(question, _EXPLICIT_VETERINARY_QUERY_MARKERS)


def _is_human_medical_query(question):
    """Detect human-medical questions that should suppress veterinary bleed-through."""
    return _text_has_marker(
        question, _HUMAN_MEDICAL_KEYWORDS
    ) and not _is_explicit_veterinary_query(question)


def _is_acute_symptom_query(question):
    """Detect acute symptom prompts that should stay on verified medical sources."""
    lower = question.lower()
    return _is_human_medical_query(question) and any(
        marker in lower for marker in _ACUTE_SYMPTOM_MARKERS
    )


def _is_mental_health_crisis_query(question):
    """Detect acute mental-health crisis prompts that need crisis-guide routing."""
    lower = question.lower()
    return any(marker in lower for marker in _MENTAL_HEALTH_CRISIS_QUERY_MARKERS) or _is_mania_or_psychosis_like_query(
        question
    )


def _is_mania_or_psychosis_like_query(question):
    """Detect mania/psychosis-like crisis phrasing, including messy observer language."""
    lower = question.lower()
    mania_activation_hits = sum(
        1 for marker in _MANIA_ACTIVATION_CRISIS_QUERY_MARKERS if marker in lower
    )
    mania_observer_hits = sum(
        1 for marker in _MANIA_HIGH_RISK_OBSERVER_QUERY_MARKERS if marker in lower
    )
    has_mania_sleep_marker = any(
        marker in lower for marker in _MANIA_SLEEP_CRISIS_QUERY_MARKERS
    )
    has_food_refusal_marker = any(
        marker in lower for marker in ("won't eat", "wont eat", "not eating", "will not eat")
    )
    has_observer_risk_marker = any(
        marker in lower
        for marker in (
            "i'm worried",
            "i am worried",
            "worried about",
            "should i be worried",
            "do i need to call",
            "do i call",
            "my husband",
            "my wife",
            "my partner",
            "my son",
            "my daughter",
            "my brother",
            "my sister",
            "my friend",
        )
    )
    has_behavior_acceleration_marker = any(
        marker in lower
        for marker in (
            "keeps pacing",
            "pacing all night",
            "won't sit still",
            "will not sit still",
            "talking fast",
            "trying to leave",
            "leave with no plan",
            "won't let anyone slow",
            "will not let anyone slow",
            "can't slow down",
            "cannot slow down",
            "unsafe choices",
            "making unsafe choices",
        )
    )
    has_unsafe_behavior_marker = any(
        marker in lower
        for marker in (
            "unsafe behavior",
            "unsafe behaviour",
            "dangerous behavior",
            "dangerous behaviour",
            "unsafe choices",
            "making unsafe choices",
            "unsafe nighttime wandering",
            "walk outside at night",
            "trying to walk outside at night",
            "keeps trying to walk outside at night",
            "trying to leave",
            "leave with no plan",
            "trying to leave with no plan",
        )
    )
    has_grandiosity_or_invulnerability_marker = any(
        marker in lower
        for marker in (
            "acting invincible",
            "invincible",
            "nothing can hurt",
            "like nothing can hurt",
            "can't be hurt",
            "cannot be hurt",
            "can not be hurt",
            "untouchable",
            "special mission",
            "rules do not apply",
        )
    )
    has_sleep_or_food_drop_marker = has_mania_sleep_marker or any(
        marker in lower
        for marker in (
            "barely eating",
            "barely eaten",
            "hardly eating",
            "hardly eaten",
            "barely sleeping",
            "barely slept",
            "hardly slept",
            "not really slept",
        )
    )
    has_sudden_behavior_change_marker = any(
        marker in lower
        for marker in (
            "was normal yesterday",
            "changed fast",
            "changed quickly",
            "suddenly started",
            "something is very wrong",
        )
    )
    has_sleep_or_food_impairment_marker = (
        has_mania_sleep_marker or has_food_refusal_marker or has_sleep_or_food_drop_marker
    )
    return any(marker in lower for marker in _PSYCHOSIS_LIKE_CRISIS_QUERY_MARKERS) or (
        mania_observer_hits >= 1
        or mania_activation_hits >= 2
        or (has_mania_sleep_marker and (mania_activation_hits >= 1 or mania_observer_hits >= 1))
        or (has_food_refusal_marker and (mania_activation_hits >= 1 or mania_observer_hits >= 1))
        or (
            has_observer_risk_marker
            and has_sleep_or_food_impairment_marker
            and has_unsafe_behavior_marker
        )
        or (
            has_behavior_acceleration_marker
            and (
                has_grandiosity_or_invulnerability_marker
                or has_sleep_or_food_drop_marker
                or has_sudden_behavior_change_marker
            )
        )
    )


def _is_cardiac_emergency_query(question):
    """Detect collapse and cardiac-emergency prompts that need first-aid routing."""
    lower = question.lower()
    has_collapse_or_unresponsive = any(
        marker in lower for marker in _COLLAPSE_UNRESPONSIVE_MARKERS
    )
    has_stroke_or_neuro_signs = _has_stroke_tia_routing_signal(question)
    has_direct_cardiac_terms = any(
        marker in lower for marker in _CARDIAC_EMERGENCY_MARKERS
    )
    return _is_human_medical_query(question) and (
        has_direct_cardiac_terms
        or (
            has_collapse_or_unresponsive
            and (
                has_stroke_or_neuro_signs
                or "chest pain" in lower
                or "chest pressure" in lower
            )
        )
    )


def _is_acute_overlap_collapse_query(question):
    """Detect collapse prompts that also mention stroke/TIA signs or chest pain."""
    lower = question.lower()
    has_stroke_or_neuro_signs = _has_stroke_tia_routing_signal(question)
    return _is_human_medical_query(question) and any(
        marker in lower for marker in _COLLAPSE_UNRESPONSIVE_MARKERS
    ) and (
        has_stroke_or_neuro_signs
        or "chest pain" in lower
        or "chest pressure" in lower
    )


def _is_noncollapse_stroke_cardiac_overlap_query(question):
    """Detect mixed stroke/TIA plus cardiac-warning prompts before collapse occurs."""
    lower = question.lower()
    has_stroke_or_neuro_signs = _has_stroke_tia_routing_signal(question)
    has_single_fast_or_neuro_sign = any(
        marker in lower for marker in _FAST_SIGN_MARKERS
    )
    has_clear_cardiac_overlap_signs = any(
        marker in lower for marker in _CARDIAC_OVERLAP_SIGNAL_MARKERS
    )
    has_cardiac_signs = has_clear_cardiac_overlap_signs or any(
        marker in lower for marker in _CARDIAC_EMERGENCY_MARKERS
    )
    has_collapse_or_unresponsive = any(
        marker in lower for marker in _COLLAPSE_UNRESPONSIVE_MARKERS
    )
    return (
        _is_human_medical_query(question)
        and (
            has_stroke_or_neuro_signs
            or (
                has_single_fast_or_neuro_sign
                and has_clear_cardiac_overlap_signs
            )
        )
        and has_cardiac_signs
        and not has_collapse_or_unresponsive
    )


def _is_bridge_demoted_acute_query(question):
    """Detect acute medical complaints where bridge guides should rerank uniformly lower."""
    lower = question.lower()
    if any(
        marker in lower
        for marker in (
            "prepare",
            "ready",
            "readiness",
            "set up",
            "setup",
            "plan",
            "planning",
            "before",
            "capability",
        )
    ):
        return False
    return (
        _is_acute_symptom_query(question)
        or _is_acute_overlap_collapse_query(question)
        or _is_noncollapse_stroke_cardiac_overlap_query(question)
    )


def _is_household_chemical_hazard_query(question):
    """Detect household chemical exposure or mixing prompts that should route to toxicology."""
    lower = question.lower()
    has_corrosive_exposure = _text_has_marker(
        lower, _CORROSIVE_HOUSEHOLD_CHEMICAL_SOURCE_MARKERS
    ) and _text_has_marker(lower, _CHEMICAL_EXPOSURE_ROUTE_MARKERS)
    has_gi_bleed_signal = _text_has_marker(lower, _ACUTE_SYMPTOM_MARKERS) or (
        "stool" in lower and "black" in lower and "sticky" in lower
    )
    if "food poisoning" in lower and has_gi_bleed_signal and not has_corrosive_exposure:
        return False
    return _text_has_marker(lower, _HOUSEHOLD_CHEMICAL_HAZARD_MARKERS) or (
        has_corrosive_exposure
    )


def _is_corrosive_household_chemical_exposure_query(question):
    """Detect actual corrosive/household-chemical exposures that need emergency-first structure."""
    lower = question.lower()
    return _text_has_marker(lower, _CORROSIVE_HOUSEHOLD_CHEMICAL_SOURCE_MARKERS) and _text_has_marker(
        lower, _CHEMICAL_EXPOSURE_ROUTE_MARKERS
    )


def _is_household_chemical_eye_query(question):
    """Detect household chemical prompts where the complaint is eye-first."""
    lower = question.lower()
    return _text_has_marker(
        lower, _CORROSIVE_HOUSEHOLD_CHEMICAL_SOURCE_MARKERS
    ) and _text_has_marker(lower, _CHEMICAL_EYE_ROUTE_MARKERS)


def _is_household_chemical_inhalation_query(question):
    """Detect household chemical inhalation prompts that should avoid stove/CO distractors."""
    lower = question.lower()
    return _text_has_marker(
        lower, _HOUSEHOLD_CHEMICAL_INHALATION_SOURCE_MARKERS
    ) and _text_has_marker(lower, _CHEMICAL_INHALATION_ROUTE_MARKERS)


def _is_urinary_query(question):
    """Detect urinary complaint-first prompts that need stronger medical routing."""
    return _text_has_marker(question, _URINARY_QUERY_MARKERS)


def _has_major_bleeding_signal(question):
    """Detect prompts that justify hemorrhage-first retrieval bias."""
    return _text_has_marker(question, _SEVERE_BLEED_QUERY_MARKERS)


def _is_generic_puncture_query(question):
    """Detect generic puncture-wound prompts that should avoid surgery-heavy drift."""
    return _text_has_marker(
        question, _GENERIC_PUNCTURE_QUERY_MARKERS
    ) and not _text_has_marker(question, _ANIMAL_BITE_QUERY_MARKERS)


def _is_supply_conflict_query(question):
    """Detect group resource-dispute prompts that need de-escalation first."""
    lower = question.lower()
    return any(marker in lower for marker in _SUPPLY_CONFLICT_TRIGGER_MARKERS) and any(
        marker in lower for marker in _SUPPLY_RESOURCE_MARKERS
    )


def _is_broad_governance_query(question):
    """Detect broad governance-design prompts that need tighter answer shape."""
    lower = question.lower()
    return ("government" in lower or "governance" in lower) and any(
        marker in lower for marker in ("survivors", "community", "people", "population")
    )


def _is_post_collapse_recovery_query(question):
    """Detect ultra-broad post-collapse recovery prompts that need scoped retrieval and answer shape."""
    lower = question.lower()
    return any(
        marker in lower
        for marker in (
            "we survived the apocalypse now what",
            "we survived the collapse now what",
            "society collapsed now what",
            "after the collapse what now",
        )
    )


def _mentions_attack_or_radiation(question):
    """Return True when the user explicitly mentions attack/radiation/fallout conditions."""
    lower = question.lower()
    return any(
        marker in lower
        for marker in (
            "nuclear",
            "radiation",
            "fallout",
            "dirty bomb",
            "chemical attack",
            "biological attack",
            "attack",
            "war",
        )
    )


def _is_weather_prep_query(question, frame=None):
    """Detect short-deadline weather prep prompts that benefit from time-boxing."""
    lower = question.lower()
    scenario = frame or build_scenario_frame(question)
    return bool(scenario.get("deadline")) and any(
        marker in lower for marker in _WEATHER_MARKERS
    )


def _is_signs_and_treatment_query(question):
    """Detect symptom-recognition plus treatment prompts."""
    lower = question.lower()
    return ("what are the signs of" in lower or "what are signs of" in lower) and (
        "how do i treat" in lower
        or "how do we treat" in lower
        or "what do i do" in lower
    )


def _is_rescue_signaling_query(question):
    """Detect rescue-signaling prompts that should prioritize a short top-yield list."""
    return _text_has_marker(question, _SIGNALING_QUERY_MARKERS)


def _is_glassmaking_query(question):
    """Detect prompts about making new glass rather than salvaging or cutting it."""
    lower = question.lower()
    return "glass" in lower and _text_has_marker(lower, _GLASS_QUERY_MARKERS)


def _is_paper_ink_query(question):
    """Detect prompts that want both paper and ink production basics."""
    lower = question.lower()
    return (
        "paper" in lower
        and "ink" in lower
        and _text_has_marker(lower, _PAPER_INK_QUERY_MARKERS)
    )


def _is_bow_arrow_query(question):
    """Detect bow-and-arrow construction prompts."""
    lower = question.lower()
    return "bow" in lower and _text_has_marker(lower, _BOW_ARROW_QUERY_MARKERS)


def _is_no_gear_fishing_query(question):
    """Detect fishing prompts that explicitly exclude normal gear."""
    lower = question.lower()
    return "fish" in lower and _text_has_marker(lower, _NO_GEAR_FISHING_QUERY_MARKERS)


def _is_natural_dye_query(question):
    """Detect plant-based natural dyeing prompts."""
    lower = question.lower()
    return _text_has_marker(lower, _NATURAL_DYE_QUERY_MARKERS)


def _is_weld_without_welder_query(question):
    """Detect welding prompts that explicitly lack a welder."""
    lower = question.lower()
    return (
        ("weld" in lower or "join" in lower or "braze" in lower or "solder" in lower)
        and any(
            term in lower
            for term in ("metal", "steel", "iron", "copper", "brass", "welder")
        )
    ) and _text_has_marker(lower, _WELD_WITHOUT_WELDER_QUERY_MARKERS)


def _is_clay_oven_query(question):
    """Detect clay/cob oven construction prompts."""
    lower = question.lower()
    return "oven" in lower and _text_has_marker(lower, _CLAY_OVEN_QUERY_MARKERS)


def _is_loom_weaving_query(question):
    """Detect starter prompts about building a loom and weaving cloth."""
    lower = question.lower()
    return "loom" in lower and any(
        marker in lower for marker in ("weave", "weaving", "cloth", "fabric")
    )


def _is_roman_concrete_query(question):
    """Detect Roman-concrete / ancient-concrete history prompts."""
    lower = question.lower()
    return "concrete" in lower and _text_has_marker(
        lower, _ROMAN_CONCRETE_QUERY_MARKERS
    )


def _is_smoke_meat_query(question):
    """Detect meat-smoking preservation prompts."""
    lower = question.lower()
    return "meat" in lower and _text_has_marker(lower, _SMOKE_MEAT_QUERY_MARKERS)


def _is_raft_lake_query(question):
    """Detect simple raft-building prompts for a lake crossing."""
    lower = question.lower()
    return (
        "raft" in lower
        and "lake" in lower
        and _text_has_marker(lower, _RAFT_LAKE_QUERY_MARKERS)
    )


def _has_build_verb(question_lower):
    """Return True when the prompt is explicitly about building or making a project."""
    return _text_has_marker(question_lower, _STARTER_BUILD_QUERY_MARKERS) or (
        any(word in question_lower for word in (" build ", " construct ", " make "))
        or question_lower.startswith(("build ", "construct ", "make "))
    )


def _is_house_build_query(question):
    """Detect broad starter prompts about building a house, cabin, or hut."""
    lower = question.lower()
    return _has_build_verb(lower) and _text_has_marker(
        lower, _HOUSE_BUILD_QUERY_MARKERS
    )


def _is_small_watercraft_query(question):
    """Detect starter prompts about building a canoe or other small watercraft."""
    lower = question.lower()
    if _is_raft_lake_query(lower):
        return False
    return _has_build_verb(lower) and _text_has_marker(
        lower, _SMALL_WATERCRAFT_QUERY_MARKERS
    )


def _is_water_purification_query(question):
    """Detect broad practical prompts about making water safe to drink."""
    lower = question.lower()
    return "water" in lower and _text_has_marker(
        lower, _WATER_PURIFICATION_QUERY_MARKERS
    )


def _is_water_storage_query(question):
    """Detect practical prompts about storing water in containers."""
    lower = question.lower()
    return "water" in lower and _text_has_marker(lower, _WATER_STORAGE_QUERY_MARKERS)


def _is_cast_without_foundry_query(question):
    """Detect metal-casting prompts that explicitly lack a foundry."""
    lower = question.lower()
    return (
        "cast" in lower
        and "metal" in lower
        and _text_has_marker(lower, _CAST_WITHOUT_FOUNDRY_QUERY_MARKERS)
    )


def _is_fair_trial_query(question):
    """Detect low-institution trial-design prompts."""
    lower = question.lower()
    return "trial" in lower and _text_has_marker(lower, _FAIR_TRIAL_QUERY_MARKERS)


def _is_soap_restock_query(question):
    """Detect prompts about replacing missing soap."""
    lower = question.lower()
    return "soap" in lower and _text_has_marker(lower, _SOAP_RESTOCK_QUERY_MARKERS)


def _is_soapmaking_query(question):
    """Detect practical soapmaking prompts, including animal-fat variants."""
    lower = question.lower()
    if "soap" not in lower:
        return False
    return _text_has_marker(lower, _SOAPMAKING_QUERY_MARKERS) or _is_soap_restock_query(
        lower
    )


def _is_brain_tanning_query(question):
    """Detect direct brain-tanning prompts."""
    lower = question.lower()
    return _text_has_marker(lower, _BRAIN_TANNING_QUERY_MARKERS)


def _is_hot_weather_burial_query(question):
    """Detect temporary body-preservation prompts in heat."""
    lower = question.lower()
    return ("body" in lower or "burial" in lower) and _text_has_marker(
        lower, _HOT_BURIAL_QUERY_MARKERS
    )


def _is_bridge_span_query(question):
    """Detect broad bridge-building prompts over a named river span."""
    lower = question.lower()
    return (
        "bridge" in lower
        and "river" in lower
        and any(marker in lower for marker in ("20 foot", "20-foot", "span", "across"))
    )


def _is_new_well_query(question):
    """Detect prompts about replacing a collapsed well."""
    lower = question.lower()
    return (
        "well" in lower
        and any(marker in lower for marker in ("collapsed", "caved in"))
        and any(
            marker in lower for marker in ("dig a new one", "new well", "replace it")
        )
    )


def _is_group_role_assignment_query(question):
    """Detect broad role-allocation prompts for mid-sized groups."""
    lower = question.lower()
    return any(
        marker in lower
        for marker in ("decide who does what", "assign roles", "who should do what")
    ) and any(marker in lower for marker in ("people", "survivors", "group"))


def _is_supply_hiding_query(question):
    """Detect prompts about concealing supplies from discovery."""
    lower = question.lower()
    return (
        "supplies" in lower
        and any(marker in lower for marker in ("hide", "hidden", "stash", "cache"))
        and any(
            marker in lower
            for marker in (
                "cant be found",
                "can't be found",
                "not be found",
                "not found",
            )
        )
    )


def _is_survivor_governance_setup_query(question):
    """Detect temporary-governance setup prompts for survivor groups."""
    lower = question.lower()
    return _is_broad_governance_query(lower) and any(
        marker in lower for marker in ("set up", "setup", "establish", "build")
    )


def _is_deadline_monsoon_query(question, frame=None):
    """Detect short-deadline monsoon prep prompts that need a hard triage structure."""
    lower = question.lower()
    scenario = frame or build_scenario_frame(question)
    return bool(scenario.get("deadline")) and "monsoon" in lower


def _is_generic_puncture_special_case(question):
    """Detect plain puncture-care prompts suitable for a deterministic template."""
    lower = question.lower()
    return (
        _is_generic_puncture_query(lower)
        and not _has_major_bleeding_signal(lower)
        and not _text_has_marker(lower, _EXPLICIT_INVASIVE_QUERY_MARKERS)
        and not _text_has_marker(lower, _PUNCTURE_SPECIAL_CASE_EXCLUSION_MARKERS)
    )


def _is_charcoal_sand_water_filter_special_case(question):
    """Detect narrow starter prompts about building a charcoal-sand water filter."""
    lower = question.lower()
    return _text_has_marker(lower, _CHARCOAL_SAND_WATER_FILTER_QUERY_MARKERS)


def _is_reused_container_water_special_case(question):
    """Detect narrow prompts about storing drinking water in reused containers."""
    lower = question.lower()
    return (
        "water" in lower
        and _text_has_marker(lower, _REUSED_CONTAINER_WATER_QUERY_MARKERS)
        and not _is_rusty_metal_drum_water_special_case(lower)
    )


def _is_low_bureaucracy_supply_special_case(question):
    """Detect ordinary supply disputes that should avoid governance drift."""
    lower = question.lower()
    return _is_supply_conflict_query(lower) and not _text_has_marker(
        lower, _SUPPLY_CONFLICT_SPECIAL_CASE_EXCLUSION_MARKERS
    )


def _is_system_behavior_query(question):
    """Detect prompts asking how the assistant should behave, not domain content."""
    lower = question.lower()
    if any(marker in lower for marker in _SYSTEM_BEHAVIOR_QUERY_MARKERS):
        return True
    return any(token in lower for token in ("answer", "response", "assistant")) and any(
        token in lower for token in ("drift", "muddled", "citation", "scope", "focus")
    )


def _is_clearly_off_topic(question):
    """Detect obvious entertainment/pop-culture prompts that should short-circuit."""
    if _is_system_behavior_query(question):
        return False
    lower = question.lower()
    if _text_has_marker(
        lower, _ABSTRACT_OFF_TOPIC_MARKERS | _NONSENSE_OFF_TOPIC_MARKERS
    ):
        return True
    has_entertainment = (
        _text_has_marker(lower, _OFF_TOPIC_ENTERTAINMENT_MARKERS)
        or "science fiction" in lower
    )
    if not has_entertainment:
        return False
    return not (
        _detect_domains(question) or _text_has_marker(lower, _RELEVANT_SCOPE_MARKERS)
    )


def _is_underspecified_stub_query(question):
    """Detect ultra-short prompts that need clarification rather than retrieval."""
    return question.strip().lower() in _UNDERSPECIFIED_STUB_QUERIES


def _is_broad_survey_query(question):
    """Detect overbroad survey requests that should be narrowed first."""
    lower = question.lower()
    return any(
        marker in lower for marker in _BROAD_SURVEY_QUERY_MARKERS
    ) and _text_has_marker(lower, _BROAD_SURVEY_TOPIC_MARKERS)


def _is_hazardous_unsupported_query(question):
    """Detect out-of-scope hazardous build requests that should be refused."""
    return _text_has_marker(question, _HAZARDOUS_UNSUPPORTED_QUERY_MARKERS)


def _is_unknown_medication_special_case(question):
    """Detect unlabeled/unknown medication prompts that need a hard stop."""
    lower = question.lower()
    return _text_has_marker(lower, _UNKNOWN_MEDICATION_MARKERS) and _text_has_marker(
        lower, _UNKNOWN_MEDICATION_UNCERTAINTY_MARKERS
    )


def _is_unknown_bottle_ingestion_special_case(question):
    """Detect unlabeled or unknown bottle ingestion that needs poison-control-first guidance."""
    lower = question.lower()
    has_bottle_signal = "bottle" in lower or "container" in lower or "jar" in lower
    has_ingestion_signal = any(
        marker in lower
        for marker in (
            "swallowed",
            "drank",
            "gulped",
            "took a mouthful",
            "mouthful",
            "sip",
            "ingested",
        )
    )
    has_unknown_signal = any(
        marker in lower
        for marker in (
            "unlabeled",
            "unlabelled",
            "unmarked",
            "unknown",
            "no label",
            "not labeled",
            "not labelled",
            "before identification",
            "dont know what it was",
            "don't know what it was",
        )
    )
    return has_bottle_signal and has_ingestion_signal and has_unknown_signal


def _is_antibiotic_synthesis_special_case(question):
    """Detect antibiotic manufacturing/extraction prompts that should be blocked."""
    lower = question.lower()
    return _text_has_marker(lower, _ANTIBIOTIC_SYNTHESIS_MARKERS) or (
        "antibiotic" in lower
        and ("without a pharmacy" in lower or "from mold" in lower)
    )


def _is_dental_infection_special_case(question):
    """Detect dental infection prompts with facial swelling that need conservative guidance."""
    lower = question.lower()
    return _text_has_marker(
        lower, _DENTAL_INFECTION_SPECIAL_CASE_MARKERS
    ) and _text_has_marker(lower, _FACIAL_SWELLING_MARKERS)


def _is_nonpharma_pain_special_case(question):
    """Detect no-painkiller prompts that should avoid empty or uncited answers."""
    return _text_has_marker(question, _NONPHARMA_PAIN_SPECIAL_CASE_MARKERS)


def _is_generic_seizure_special_case(question):
    """Detect generic seizure prompts that need conservative hands-off guidance."""
    lower = question.lower()
    return _text_has_marker(
        lower, _GENERIC_SEIZURE_SPECIAL_CASE_MARKERS
    ) and not _text_has_marker(lower, _SEIZURE_SPECIAL_CASE_EXCLUSION_MARKERS)


def _is_generic_severe_burn_special_case(question):
    """Detect generic severe-burn prompts that should avoid procedure drift."""
    lower = question.lower()
    has_generic_marker = _text_has_marker(
        lower, _GENERIC_SEVERE_BURN_SPECIAL_CASE_MARKERS
    )
    has_high_risk_burn_phrase = any(
        marker in lower
        for marker in (
            "burned and blistered",
            "burn is white and leathery",
            "white and leathery",
            "burns on the face",
            "burn on the face",
            "singed eyelashes",
            "wraps all the way around",
            "wraps around the arm",
            "circumferential burn",
            "skin is numb",
            "burn looks small but the skin is numb",
        )
    )
    return has_generic_marker or has_high_risk_burn_phrase


def _is_chest_trauma_breathing_special_case(question):
    """Detect penetrating/blunt chest-trauma prompts with breathing-failure signs."""
    lower = question.lower()
    has_trauma_context = _text_has_marker(
        lower, _CHEST_TRAUMA_SPECIAL_CASE_TRAUMA_MARKERS
    ) or (
        "chest" in lower and any(term in lower for term in ("stab", "stabb", "wound"))
    )
    has_breathing_or_tension_signal = _text_has_marker(
        lower, _CHEST_TRAUMA_SPECIAL_CASE_SIGNS
    )
    return has_trauma_context and has_breathing_or_tension_signal


def _is_abuse_immediate_safety_special_case(question):
    """Detect coercive abuse or survivor-safety prompts that need immediate-danger routing."""
    lower = question.lower()
    has_relationship_or_assault_context = _text_has_marker(
        lower, _ABUSE_IMMEDIATE_SAFETY_RELATIONSHIP_MARKERS
    ) or (
        "child" in lower
        and "home" in lower
        and any(term in lower for term in ("hurting", "hurt", "unsafe"))
    )
    has_immediate_danger_marker = _text_has_marker(
        lower, _ABUSE_IMMEDIATE_SAFETY_DANGER_MARKERS
    )
    return has_relationship_or_assault_context and has_immediate_danger_marker


def _is_generic_broken_arm_special_case(question):
    """Detect narrow broken-arm setting prompts suitable for a conservative template."""
    lower = question.lower()
    return _text_has_marker(
        lower, _GENERIC_BROKEN_ARM_SPECIAL_CASE_MARKERS
    ) and not _text_has_marker(lower, _FRACTURE_SPECIAL_CASE_EXCLUSION_MARKERS)


def _is_generic_broken_leg_special_case(question):
    """Detect narrow broken-leg setting prompts suitable for a conservative template."""
    lower = question.lower()
    return _text_has_marker(
        lower, _GENERIC_BROKEN_LEG_SPECIAL_CASE_MARKERS
    ) and not _text_has_marker(lower, _FRACTURE_SPECIAL_CASE_EXCLUSION_MARKERS)


def _is_generic_hypothermia_special_case(question):
    """Detect plain hypothermia signs-and-treatment prompts that want a bounded field answer."""
    lower = question.lower()
    return (
        "hypothermia" in lower
        and _text_has_marker(lower, _GENERIC_HYPOTHERMIA_SPECIAL_CASE_MARKERS)
        and any(
            marker in lower
            for marker in (
                "how do i treat",
                "how do we treat",
                "what do i do",
                "treat it",
            )
        )
        and not _text_has_marker(
            lower, _GENERIC_HYPOTHERMIA_SPECIAL_CASE_EXCLUSION_MARKERS
        )
    )


def _is_generic_animal_bite_special_case(question):
    """Detect ordinary animal-bite prompts that should stay on wash/open/escalate."""
    lower = question.lower()
    return _text_has_marker(lower, _ANIMAL_BITE_QUERY_MARKERS) and not _text_has_marker(
        lower, _ANIMAL_BITE_SPECIAL_CASE_EXCLUSION_MARKERS
    )


def _is_generic_jellyfish_special_case(question):
    """Detect jellyfish-sting prompts that benefit from a simple corrective template."""
    lower = question.lower()
    return _text_has_marker(lower, _GENERIC_JELLYFISH_SPECIAL_CASE_MARKERS) or (
        "jellyfish" in lower
        and any(term in lower for term in ("sting", "stung", "urine", "pee"))
    )


def _is_solar_water_special_case(question):
    """Detect sunlight-only water-purification prompts that need a bounded answer."""
    lower = question.lower()
    return (
        "purify water" in lower
        and "sunlight" in lower
        and _text_has_marker(lower, _SOLAR_WATER_SPECIAL_CASE_MARKERS)
    )


def _is_water_without_fuel_special_case(question):
    """Detect plain no-fuel water-purification prompts that need a bounded fallback plan."""
    lower = question.lower()
    has_water_intent = "water" in lower and any(
        term in lower
        for term in (
            "purify",
            "purification",
            "disinfect",
            "safe to drink",
            "drink safely",
            "make safe",
        )
    )
    return (
        has_water_intent
        and _text_has_marker(lower, _WATER_WITHOUT_FUEL_QUERY_MARKERS)
        and "sunlight" not in lower
        and not _text_has_marker(
            lower, _WATER_WITHOUT_FUEL_SPECIAL_CASE_EXCLUSION_MARKERS
        )
    )


def _is_zero_resource_knife_special_case(question):
    """Detect plain zero-resource knife prompts that should default to stone-first guidance."""
    lower = question.lower()
    return (
        "knife" in lower
        and _text_has_marker(lower, _ZERO_RESOURCE_KNIFE_SPECIAL_CASE_MARKERS)
        and not _text_has_marker(
            lower, _ZERO_RESOURCE_KNIFE_SPECIAL_CASE_EXCLUSION_MARKERS
        )
    )


def _is_weld_without_welder_special_case(question):
    """Detect plain no-welder joining prompts that benefit from a bounded starter answer."""
    lower = question.lower()
    return _is_weld_without_welder_query(lower) and not _text_has_marker(
        lower, _WELD_SPECIAL_CASE_EXCLUSION_MARKERS
    )


def _is_generic_paper_ink_special_case(question):
    """Detect generic paper-and-ink starter prompts that should stay low-tech and compact."""
    lower = question.lower()
    return (
        _is_paper_ink_query(lower)
        and "how do i make paper and ink" in lower
        and not _text_has_marker(lower, _PAPER_INK_SPECIAL_CASE_EXCLUSION_MARKERS)
    )


def _is_radio_from_scrap_special_case(question):
    """Detect plain working-radio-from-scrap prompts that want a crystal-set starter answer."""
    lower = question.lower()
    return "build a working radio from scrap" in lower and not any(
        marker in lower
        for marker in ("transmitter", "ham", "walkie-talkie", "broadcast station")
    )


def _is_factory_salvage_special_case(question):
    """Detect broad abandoned-factory salvage prompts that want a priority hierarchy."""
    lower = question.lower()
    return "abandoned factory" in lower and "salvage first" in lower


def _is_fire_in_rain_special_case(question):
    """Detect plain fire-in-rain prompts that want a compact wet-weather ignition plan."""
    lower = question.lower()
    return (
        ("fire" in lower or "ignite" in lower)
        and any(
            marker in lower for marker in ("rain", "wet", "soaked", "damp", "storm")
        )
        and _text_has_marker(lower, _FIRE_IN_RAIN_QUERY_MARKERS)
    )


def _is_closed_room_fire_question(question):
    """Detect closed-bedroom / hallway / enclosed-room fire-or-smoke prompts that need evacuation-first wording."""
    lower = question.lower()
    has_fire_signal = "fire" in lower
    has_smoke_signal = "smoke" in lower or "smoky" in lower
    has_room_signal = any(
        term in lower
        for term in (
            "bedroom",
            "closed room",
            "closed-room",
            "closed bedroom",
            "closed-bedroom",
            "enclosed room",
            "enclosed-room",
            "enclosed bedroom",
            "enclosed-bedroom",
            "sealed room",
            "sealed-room",
            "hallway",
        )
    )
    return has_room_signal and (has_fire_signal or has_smoke_signal)


def _is_enclosed_room_fire_smoke_question(question):
    """Detect enclosed-room smoke-back or fire-plus-smoke prompts that need evacuation-first wording."""
    lower = question.lower()
    has_fire_signal = "fire" in lower
    has_smoke_signal = "smoke" in lower or "smoky" in lower
    has_enclosed_signal = any(
        marker in lower
        for marker in (
            "room",
            "indoors",
            "inside",
            "enclosed",
            "building",
            "house",
            "cabin",
            "tent",
            "smoke back",
            "smoke-back",
            "smoke backing",
            "backdraft",
            "backing into",
        )
    )
    return has_fire_signal and has_smoke_signal and has_enclosed_signal


def _is_brain_tanning_special_case(question):
    """Detect plain brain-tanning prompts that want a compact yes-and-how answer."""
    lower = question.lower()
    return "tan leather with brains" in lower or (
        "brain" in lower and "tan" in lower and "leather" in lower
    )


def _is_glassmaking_starter_special_case(question):
    """Detect the broad generic glassmaking starter prompt."""
    lower = question.lower().strip()
    return lower in {"how do i make glass", "how do i make glass from scratch"}


def _is_age_ten_skills_special_case(question):
    """Detect the plain age-10 first-skills prompt."""
    lower = question.lower().strip()
    return lower == "what skills should a 10 year old learn first"


def _is_candles_for_light_special_case(question):
    """Detect broad candle-making prompts that want a compact lighting starter."""
    lower = question.lower()
    return (
        "candles" in lower
        and any(marker in lower for marker in ("make", "making"))
        and "light" in lower
    )


def _is_metal_splinter_special_case(question):
    """Detect narrow metal-splinter prompts suitable for a conservative template."""
    lower = question.lower()
    return (
        "metal" in lower
        and "splinter" in lower
        and any(marker in lower for marker in ("hand", "finger", "palm", "skin"))
        and not _text_has_marker(lower, _EYE_METADATA_MARKERS)
        and not _has_major_bleeding_signal(lower)
    )


def _is_rescue_plane_signal_special_case(question):
    """Detect plain rescue-plane signaling prompts that want a bounded field checklist."""
    lower = question.lower()
    return (
        _is_rescue_signaling_query(lower)
        and any(
            marker in lower
            for marker in ("rescue plane", "signal a plane", "signal rescue")
        )
        and not any(
            marker in lower
            for marker in ("radio", "beacon", "helicopter", "at night only")
        )
    )


def _is_deadline_monsoon_special_case(question):
    """Detect short-deadline monsoon prep prompts that want a bounded week plan."""
    lower = question.lower()
    scenario = build_scenario_frame(question)
    return (
        (
            bool(scenario.get("deadline"))
            or any(
                marker in lower
                for marker in ("in a week", "one week", "7 days", "next week")
            )
        )
        and "monsoon" in lower
        and any(
            marker in lower
            for marker in (
                "what do we need to prepare",
                "what should we prepare",
                "starts in",
            )
        )
    )


def _is_heat_wave_group_illness_special_case(question):
    """Detect heat-wave prompts where multiple people are getting sick."""
    lower = question.lower()
    has_heat_signal = any(
        marker in lower
        for marker in (
            "100 degrees",
            "100 degree",
            "over 100",
            "heat wave",
            "extreme heat",
        )
    )
    has_group_illness_signal = any(
        marker in lower
        for marker in (
            "people are getting sick",
            "everyone is getting sick",
            "multiple people are getting sick",
            "group is getting sick",
            "people keep getting sick",
        )
    )
    return has_heat_signal and has_group_illness_signal


def _is_molten_metal_safety_special_case(question):
    """Detect generic molten-metal handling prompts that need a bounded safety checklist."""
    lower = question.lower()
    return "molten metal" in lower and any(
        marker in lower
        for marker in (
            "safely handle",
            "handle safely",
            "safe to handle",
            "work safely",
        )
    )


def _is_unknown_building_chemical_special_case(question):
    """Detect abandoned-building hazmat triage prompts that should avoid long catalogs."""
    lower = question.lower()
    return (
        "abandoned building" in lower
        and "chemical" in lower
        and any(
            marker in lower
            for marker in (
                "identify whats dangerous",
                "identify what's dangerous",
                "which are dangerous",
                "what is dangerous",
            )
        )
    )


def _is_restart_electricity_bootstrap_special_case(question):
    """Detect very broad electricity-rebuild prompts that want a staged bootstrap answer."""
    lower = question.lower()
    return "restart electricity" in lower and "nothing" in lower


def _is_long_range_no_electronics_message_special_case(question):
    """Detect long-range no-electronics messaging prompts that need a bounded transport plan."""
    lower = question.lower()
    return (
        "message" in lower
        and "no electronics" in lower
        and any(marker in lower for marker in ("50 miles", "fifty miles"))
    )


def _is_wounded_dark_water_special_case(question):
    """Detect the specific multi-constraint wound/no-water/nightfall prompt."""
    lower = question.lower()
    return (
        "wounded person" in lower
        and "no clean water" in lower
        and any(
            marker in lower
            for marker in ("getting dark", "dark in", "2 hours", "two hours")
        )
    )


def _is_unknown_child_ingestion_special_case(question):
    """Detect unknown child-ingestion prompts that need a conservative poisoning triage answer."""
    lower = question.lower()
    has_unknown_ingestion_marker = _text_has_marker(
        lower, _UNKNOWN_CHILD_INGESTION_UNKNOWN_MARKERS
    )
    has_vomiting_marker = any(
        marker in lower for marker in ("throwing up", "vomiting", "threw up")
    )
    has_child_cleaner_ingestion_marker = any(
        marker in lower
        for marker in ("got into cleaner", "got into the cleaner", "got into some cleaner")
    )
    return (
        _text_has_marker(lower, _CHILD_QUERY_MARKERS)
        and has_unknown_ingestion_marker
        and (has_vomiting_marker or has_child_cleaner_ingestion_marker)
    )


def _is_buried_supply_cache_special_case(question):
    """Detect narrow buried-cache prompts that want a compact storage/concealment checklist."""
    lower = question.lower()
    return "bury supplies" in lower and any(
        marker in lower
        for marker in (
            "stay hidden",
            "survive weather",
            "survive the weather",
            "stay dry",
        )
    )


def _is_hide_supplies_special_case(question):
    """Detect generic hide-supplies prompts that want a compact cache-concealment answer."""
    lower = question.lower()
    return (
        "supplies" in lower
        and "hide" in lower
        and any(
            marker in lower
            for marker in ("cant be found", "can't be found", "not be found")
        )
    )


def _is_snake_bite_special_case(question):
    """Detect generic human snake-bite prompts that need a bounded emergency template."""
    lower = question.lower()
    return (
        "snake bite" in lower
        and not _is_explicit_veterinary_query(lower)
        and any(
            marker in lower
            for marker in ("swelling", "swollen", "help", "what do i do")
        )
    )


def _is_bear_near_campsite_special_case(question):
    """Detect bear-near-campsite prompts that need immediate camp protocol guidance."""
    lower = question.lower()
    return (
        "bear" in lower
        and any(
            marker in lower
            for marker in (
                "campsite",
                "camp site",
                "tent",
                "near camp",
                "at camp",
                "my camp",
                "in camp",
            )
        )
        and not _is_explicit_veterinary_query(lower)
    )


def _is_snake_in_yard_special_case(question):
    """Detect snake-in-yard prompts that need safe distance and identification guidance."""
    lower = question.lower()
    return (
        "snake" in lower
        and any(
            marker in lower
            for marker in (
                "yard",
                "house",
                "home",
                "porch",
                "garage",
                "near my",
                "in my",
                "garden",
                "driveway",
            )
        )
        and "bite" not in lower
        and "bitten" not in lower
        and not _is_explicit_veterinary_query(lower)
    )


def _is_animal_acting_strange_special_case(question):
    """Detect animal-acting-strange prompts that may indicate rabies risk."""
    lower = question.lower()
    return (
        any(
            marker in lower
            for marker in (
                "animal acting strange",
                "animal acting weird",
                "acting strange",
                "acting weird",
            )
        )
        or (
            any(term in lower for term in ("raccoon", "skunk", "fox", "bat", "animal"))
            and any(
                marker in lower
                for marker in (
                    "foaming at the mouth",
                    "foaming",
                    "rabid",
                    "rabies",
                    "nocturnal during the day",
                    "stumbling",
                    "circling",
                    "approaching people",
                    "not afraid of humans",
                    "no fear of humans",
                )
            )
        )
    ) and not _is_explicit_veterinary_query(lower)


def _is_star_navigation_special_case(question):
    """Detect plain star-navigation prompts that want a compact orientation answer."""
    lower = question.lower()
    return (
        "navigate by stars" in lower
        or "navigate using stars" in lower
        or "find direction by stars" in lower
    )


def _is_cement_from_scratch_special_case(question):
    """Detect broad cement-from-scratch prompts that should stay on a lime-first path."""
    lower = question.lower()
    return (
        "cement" in lower
        and "from scratch" in lower
        and any(marker in lower for marker in ("make", "mix", "produce"))
    )


def _is_roman_concrete_special_case(question):
    """Detect plain Roman-concrete starter prompts that benefit from a bounded answer."""
    lower = question.lower()
    return (
        _is_roman_concrete_query(lower)
        and any(marker in lower for marker in ("how did", "how do", "make", "made"))
        and not any(
            marker in lower
            for marker in (
                "compare",
                "difference",
                "versus",
                "vs",
                "modern",
                "portland",
                "marine",
                "underwater",
            )
        )
    )


def _is_urine_hydration_special_case(question):
    """Detect urine-drinking survival prompts that need a short corrective answer."""
    lower = question.lower()
    return (
        "drink" in lower
        and "urine" in lower
        and any(
            marker in lower
            for marker in (
                "no water",
                "without water",
                "if theres no water",
                "if there's no water",
            )
        )
    )


def _is_smelt_iron_special_case(question):
    """Detect broad iron-smelting prompts that want a compact bloomery outline."""
    lower = question.lower()
    return "smelt iron" in lower and "raw ore" in lower


def _is_water_wheel_power_special_case(question):
    """Detect broad water-wheel power prompts that want a compact starter plan."""
    lower = question.lower()
    return "water wheel" in lower and "power" in lower


def _is_plant_fiber_rope_special_case(question):
    """Detect broad plant-fiber rope prompts that want a compact cordage starter plan."""
    lower = question.lower()
    return (
        "rope" in lower
        and "plant fiber" in lower
        and any(marker in lower for marker in ("make", "making", "from"))
    )


def _is_imminent_violent_fight_special_case(question):
    """Detect mediation prompts where the immediate issue is stopping violence."""
    lower = question.lower()
    return (
        "mediate" in lower
        and "fight" in lower
        and any(
            marker in lower
            for marker in ("turn violent", "about to turn violent", "getting violent")
        )
    )


def _is_stone_tools_starter_special_case(question):
    """Detect broad stone-tool replacement prompts that want a compact knapping starter answer."""
    lower = question.lower()
    return (
        "tools are broken" in lower
        and "stone" in lower
        and any(
            marker in lower
            for marker in ("make new ones", "new ones from stone", "from stone")
        )
    )


def _is_group_work_refusal_special_case(question):
    """Detect broad morale-collapse prompts where the group is giving up on work."""
    lower = question.lower()
    return "refusing to work" in lower and any(
        marker in lower
        for marker in (
            "people in our group",
            "our group",
            "people are giving up",
            "giving up",
        )
    )


def _is_teach_kids_survival_special_case(question):
    """Detect parental survival-teaching prompts that want a bounded family-preparedness answer."""
    lower = question.lower()
    return "teach my kids to survive" in lower and any(
        marker in lower
        for marker in (
            "if something happens to me",
            "if i die",
            "if i'm gone",
            "if im gone",
        )
    )


def _is_winter_wild_plants_special_case(question):
    """Detect broad winter-foraging prompts that need a cautious class-based answer."""
    lower = question.lower()
    return (
        "winter" in lower
        and "wild plants" in lower
        and any(marker in lower for marker in ("eat", "can we eat", "what wild plants"))
    )


def _is_trauma_nightmares_special_case(question):
    """Detect trauma-nightmare prompts that need a compact sleep recovery plan."""
    lower = question.lower()
    return (
        "nightmares" in lower
        and any(
            marker in lower for marker in ("cant sleep", "can't sleep", "cannot sleep")
        )
        and any(
            marker in lower
            for marker in (
                "what happened",
                "after what happened",
                "after the attack",
                "after the accident",
            )
        )
    )


def _is_building_fortification_special_case(question):
    """Detect broad home/building fortification prompts that need passive-defense framing."""
    lower = question.lower()
    return (
        "fortify" in lower
        and "building" in lower
        and any(
            marker in lower
            for marker in (
                "intruders",
                "against intruders",
                "against raiders",
                "against attackers",
            )
        )
    )


def _is_armed_strangers_approaching_special_case(question):
    """Detect approaching-armed-stranger prompts that want a bounded first-contact plan."""
    lower = question.lower()
    return (
        "armed strangers" in lower
        and any(
            marker in lower
            for marker in (
                "approaching",
                "coming toward",
                "coming at us",
                "coming our way",
            )
        )
        and any(
            marker in lower
            for marker in ("what do we do", "what should we do", "what now")
        )
    )


def _is_dead_body_disposal_special_case(question):
    """Detect direct dead-body disposal prompts that need a bounded sanitation answer."""
    lower = question.lower()
    return (
        "dead body" in lower
        and any(marker in lower for marker in ("dispose", "disposal", "handle"))
        and any(marker in lower for marker in ("prevent disease", "safely", "safe"))
    )


def _is_hot_weather_burial_special_case(question):
    """Detect plain hot-weather burial-preservation prompts that benefit from a bounded answer."""
    lower = question.lower()
    return _is_hot_weather_burial_query(lower) and not any(
        marker in lower
        for marker in (
            "multiple bodies",
            "mass casualty",
            "pandemic",
            "cremate",
            "cremation",
            "embalm",
            "embalming",
        )
    )


def _is_cast_without_foundry_special_case(question):
    """Detect plain starter casting prompts that benefit from a bounded answer."""
    lower = question.lower()
    return _is_cast_without_foundry_query(lower) and not any(
        marker in lower
        for marker in (
            "iron",
            "steel",
            "aluminum",
            "aluminium",
            "compare",
            "difference",
        )
    )


def _is_group_garbage_management_special_case(question):
    """Detect broad community garbage-management prompts that want a compact public-health starter."""
    lower = question.lower()
    return (
        "waste" in lower
        and "garbage" in lower
        and any(
            marker in lower
            for marker in ("group of 30", "30 people", "group of", "camp of")
        )
    )


def _is_food_store_rats_special_case(question):
    """Detect concrete rat-in-food-storage prompts that want a short exclusion/sanitation answer."""
    lower = question.lower()
    has_rat_signal = any(
        marker in lower for marker in ("rats", "rat", "rodent", "rodents")
    )
    has_storage_signal = any(
        marker in lower
        for marker in (
            "food stores",
            "food store",
            "stored food",
            "grain store",
            "grain stores",
            "pantry",
        )
    )
    has_action_signal = any(
        marker in lower
        for marker in (
            "getting into",
            "got into",
            "in our",
            "what do i do",
            "how do i stop",
            "how do i keep",
        )
    )
    return has_rat_signal and has_storage_signal and has_action_signal


def _is_vegetable_garden_starter_special_case(question):
    """Detect broad vegetable-garden-from-scratch prompts that want a bounded first-season plan."""
    lower = question.lower()
    return (
        "vegetable garden" in lower
        and any(marker in lower for marker in ("from nothing", "from scratch"))
        and any(marker in lower for marker in ("start", "starting", "begin"))
    )


def _is_seal_now_or_dry_more_special_case(question):
    """Detect preserved-food prompts asking whether to seal now or keep drying first."""
    lower = question.lower()
    has_seal_signal = any(
        marker in lower
        for marker in (
            "seal now",
            "seal it now",
            "seal this now",
            "jar it now",
            "bag it now",
        )
    )
    has_dry_signal = any(
        marker in lower
        for marker in (
            "dry more first",
            "dry it more",
            "dry it longer",
            "keep drying",
            "needs more drying",
        )
    )
    return has_seal_signal and has_dry_signal


def _is_flat_tire_no_spare_special_case(question):
    """Detect flat-tire prompts that need a short repair-vs-replace triage answer."""
    lower = question.lower()
    return (
        "flat tire" in lower
        and any(marker in lower for marker in ("no spare", "without a spare"))
        and any(marker in lower for marker in ("fix", "repair", "do i do"))
    )


def _is_day_navigation_without_instruments_special_case(question):
    """Detect daytime no-map/no-compass navigation prompts that want a bounded orientation answer."""
    lower = question.lower()
    return (
        any(
            marker in lower
            for marker in (
                "without a map or compass",
                "without map or compass",
                "no map or compass",
            )
        )
        and any(
            marker in lower for marker in ("during the day", "daytime", "in daylight")
        )
        and any(
            marker in lower for marker in ("navigate", "find direction", "orientation")
        )
    )


def _is_group_hygiene_disease_prevention_special_case(question):
    """Detect broad group-cleanliness prompts that want a compact disease-prevention system."""
    lower = question.lower()
    return any(
        marker in lower
        for marker in ("group clean", "keep a group clean", "keep people clean")
    ) and any(
        marker in lower
        for marker in (
            "stop disease from spreading",
            "prevent disease from spreading",
            "stop illness from spreading",
        )
    )


def _is_clear_water_safe_to_drink_special_case(question):
    """Detect clear-water safety prompts that need a short corrective answer."""
    lower = question.lower()
    if any(
        marker in lower
        for marker in (
            "taste",
            "tastes",
            "flat",
            "stale",
            "off taste",
            "stored",
            "barrel",
            "drum",
            "tank",
            "jug",
            "container",
            "sitting",
            "sat",
            "old",
            "left out",
        )
    ):
        return False
    return (
        any(marker in lower for marker in ("looks clear", "looks clean"))
        and any(
            marker in lower
            for marker in ("safe to drink", "okay to drink", "can i drink")
        )
        and "water" in lower
    )


def _is_bad_water_survival_special_case(question):
    """Detect broad bad-water survival prompts that want a bounded triage answer."""
    lower = question.lower()
    return "bad water" in lower and any(
        marker in lower for marker in ("not die", "stay alive", "survive")
    )


def _is_group_water_ration_math_special_case(question):
    """Detect concrete group water math prompts that want a bounded ration calculation."""
    lower = question.lower()
    has_water_amount = any(
        marker in lower
        for marker in (
            "gallons of water",
            "gallon of water",
            "liters of water",
            "litres of water",
        )
    )
    has_group_signal = any(marker in lower for marker in ("people", "person"))
    has_duration_signal = any(
        marker in lower
        for marker in (
            "how long can we last",
            "how long will it last",
            "how long does it last",
        )
    )
    has_ration_signal = any(
        marker in lower
        for marker in (
            "what ration should we set",
            "what ration should we use",
            "what ration",
            "ration should",
        )
    )
    return (
        has_water_amount
        and has_group_signal
        and has_duration_signal
        and has_ration_signal
    )


def _is_alone_wet_cold_help_special_case(question):
    """Detect panic-style solo wet/cold prompts that need immediate hypothermia triage."""
    lower = question.lower()
    return (
        "alone" in lower
        and "wet" in lower
        and "cold" in lower
        and any(
            marker in lower
            for marker in ("cant think straight", "can't think straight")
        )
        and any(marker in lower for marker in ("help", "please help"))
    )


def _is_limited_bandage_material_special_case(question):
    """Detect constrained-bandage prompts that need a compact dressing-allocation plan."""
    lower = question.lower()
    has_bandage_signal = any(
        marker in lower
        for marker in (
            "bandage material",
            "bandage",
            "bandages",
            "clean dressing",
            "dressings",
        )
    )
    has_constraint_signal = any(
        marker in lower
        for marker in ("only enough", "limited", "for a day", "one day", "running out")
    )
    has_injury_signal = any(
        marker in lower for marker in ("injured person", "wound", "bleeding", "injury")
    )
    has_action_signal = any(
        marker in lower
        for marker in (
            "how should we use it",
            "how should we use them",
            "how do we use it",
            "what do we do",
        )
    )
    return (
        has_bandage_signal
        and has_constraint_signal
        and has_injury_signal
        and has_action_signal
    )


def _is_quiet_warm_water_low_fuel_special_case(question):
    """Detect the exact quiet/warmth/water/fuel conflict prompt that wants a bounded overnight plan."""
    lower = question.lower()
    return (
        "stay quiet" in lower
        and "keep warm" in lower
        and "purify water" in lower
        and any(
            marker in lower
            for marker in ("almost no fuel", "very little fuel", "no fuel")
        )
    )


def _is_crop_fungus_safety_special_case(question):
    """Detect direct crop-fungus edibility prompts that want a bounded food-safety answer."""
    lower = question.lower()
    return (
        "fungus" in lower
        and "crops" in lower
        and any(
            marker in lower for marker in ("safe to eat", "okay to eat", "can we eat")
        )
    )


def _is_compound_farm_winter_failure_special_case(question):
    """Detect compound farm-failure prompts that need a short triage plan."""
    lower = question.lower()
    return (
        "crops failed" in lower
        and any(
            marker in lower
            for marker in (
                "livestock is sick",
                "livestock are sick",
                "animals are sick",
            )
        )
        and "winter" in lower
    )


def _is_no_insulation_before_winter_special_case(question):
    """Detect near-winter no-insulation prompts that want a bounded retrofit sprint."""
    lower = question.lower()
    return (
        "winter" in lower
        and any(marker in lower for marker in ("weeks away", "week away"))
        and any(marker in lower for marker in ("no insulation", "without insulation"))
    )


def _is_night_watch_rotation_special_case(question):
    """Detect generic night-watch-rotation prompts that want a compact staffing plan."""
    lower = question.lower()
    return "night watch rotation" in lower or (
        "night watch" in lower
        and any(
            marker in lower
            for marker in ("set up", "setup", "organize", "schedule", "rotation")
        )
    )


def _is_generic_choking_help_special_case(question):
    """Detect direct choking-help prompts that want a compact airway response."""
    lower = question.lower()
    if "choking" not in lower:
        return False
    has_help_phrase = any(
        marker in lower
        for marker in (
            "dont know the heimlich",
            "don't know the heimlich",
            "what do i do",
            "help",
            "what is the first move",
            "do we call now",
            "does the first step change",
            "start cpr",
        )
    )
    has_high_risk_variant = any(
        marker in lower
        for marker in (
            "still coughing",
            "still talking",
            "cannot speak",
            "can't speak",
            "cannot cough",
            "can't cough",
            "cannot breathe",
            "can't breathe",
            "turning blue",
            "pregnant",
            "infant",
            "gagging",
            "collapsed",
            "unresponsive",
        )
    )
    return has_help_phrase or has_high_risk_variant


def _is_pregnant_no_supplies_transport_special_case(question):
    """Detect low-resource pregnancy-plus-transport prompts that need a bounded travel plan."""
    lower = question.lower()
    return (
        "pregnant woman" in lower
        and any(
            marker in lower
            for marker in ("no medical supplies", "without medical supplies")
        )
        and any(marker in lower for marker in ("days walk", "day walk", "nearest town"))
    )


def _is_supply_movement_efficiency_special_case(question):
    """Detect broad supply-hauling-efficiency prompts that want a compact logistics answer."""
    lower = question.lower()
    has_supply_signal = any(
        marker in lower
        for marker in ("move supplies", "haul supplies", "carry supplies")
    )
    has_distance_signal = any(
        marker in lower for marker in ("farther", "further", "longer distance")
    )
    has_effort_signal = any(
        marker in lower
        for marker in ("less effort", "less work", "more easily", "easier")
    )
    return has_supply_signal and has_distance_signal and has_effort_signal


def _is_group_warmth_fuel_special_case(question):
    """Detect overnight warmth prompts that need a bounded heat-conservation plan."""
    lower = question.lower()
    has_warmth_signal = any(
        marker in lower
        for marker in ("keep a group warm", "keep everyone warm", "keep the group warm")
    )
    has_time_signal = any(
        marker in lower
        for marker in ("for the night", "overnight", "through the night")
    )
    has_fuel_signal = any(
        marker in lower
        for marker in (
            "without burning through all our fuel",
            "without using all our fuel",
            "without wasting all our fuel",
        )
    )
    return has_warmth_signal and has_time_signal and has_fuel_signal


def _is_recent_partner_loss_shutdown_special_case(question):
    """Detect recent-partner-loss prompts with major functional shutdown."""
    lower = question.lower()
    has_loss_signal = any(
        marker in lower
        for marker in (
            "my partner died",
            "my wife died",
            "my husband died",
            "my spouse died",
        )
    )
    has_recent_signal = any(
        marker in lower for marker in ("last week", "few days ago", "recently")
    )
    has_shutdown_signal = any(
        marker in lower
        for marker in (
            "cant get out of bed",
            "can't get out of bed",
            "cant function",
            "can't function",
        )
    )
    return has_loss_signal and has_recent_signal and has_shutdown_signal


def _is_dry_river_find_water_special_case(question):
    """Detect dried-up river prompts that want a compact water-location plan."""
    lower = question.lower()
    has_dry_source_signal = any(
        marker in lower
        for marker in (
            "river dried up",
            "river dried",
            "dry river",
            "dry streambed",
            "dried-up river",
        )
    )
    has_find_water_signal = any(
        marker in lower
        for marker in (
            "find water now",
            "where do i find water",
            "where do we find water",
        )
    )
    return has_dry_source_signal and has_find_water_signal


def _is_freezing_night_firewood_special_case(question):
    """Detect overnight firewood-estimate prompts that want a compact planning number."""
    lower = question.lower()
    return (
        "firewood" in lower
        and "freezing night" in lower
        and any(marker in lower for marker in ("how much", "need"))
    )


def _is_stealth_warmth_fire_special_case(question):
    """Detect warmth-plus-concealment fire prompts that need a compact tradeoff answer."""
    lower = question.lower()
    has_fire_signal = "fire" in lower and "warmth" in lower
    has_concealment_signal = any(
        marker in lower
        for marker in (
            "dont want to be seen",
            "don't want to be seen",
            "dont want to be noticed",
            "don't want to be noticed",
        )
    )
    return has_fire_signal and has_concealment_signal


def _is_overloaded_caregiver_crying_children_special_case(question):
    """Detect caregiver-overload prompts with persistently crying kids."""
    lower = question.lower()
    has_child_distress_signal = any(
        marker in lower
        for marker in (
            "kids wont stop crying",
            "kids won't stop crying",
            "children wont stop crying",
            "children won't stop crying",
        )
    )
    has_caregiver_overload_signal = any(
        marker in lower
        for marker in ("im losing it", "i'm losing it", "cant take it", "can't take it")
    )
    return has_child_distress_signal and has_caregiver_overload_signal


def _is_unknown_mushroom_edibility_special_case(question):
    """Detect direct unknown-mushroom edibility prompts that should default to refusal-plus-safety."""
    lower = question.lower()
    return any(
        marker in lower
        for marker in (
            "can i eat this mushroom",
            "can we eat this mushroom",
            "is this mushroom safe to eat",
        )
    )


def _is_flash_flood_valley_special_case(question):
    """Detect flash-flood-in-a-valley prompts that need an immediate evacuation answer."""
    lower = question.lower()
    return (
        "flash flood warning" in lower
        and "valley" in lower
        and any(marker in lower for marker in ("what do we do", "what now", "help"))
    )


def _is_targeted_groundwater_search_special_case(question):
    """Detect groundwater-location prompts that want a compact site-selection answer."""
    lower = question.lower()
    return "find water underground" in lower and any(
        marker in lower
        for marker in (
            "without digging randomly",
            "without digging everywhere",
            "without guessing",
        )
    )


def _is_multi_casualty_collapse_triage_special_case(question):
    """Detect specific collapse triage prompts with breathing trouble, immobility, and fracture."""
    lower = question.lower()
    return (
        "collapse" in lower
        and any(
            marker in lower for marker in ("cant breathe well", "can't breathe well")
        )
        and any(marker in lower for marker in ("cant walk", "can't walk"))
        and "broken arm" in lower
    )


def _is_contaminated_well_no_fuel_special_case(question):
    """Detect contaminated-well prompts with sick people and no fuel for boiling."""
    lower = question.lower()
    return (
        any(marker in lower for marker in ("well is contaminated", "contaminated well"))
        and any(
            marker in lower
            for marker in ("no fuel for boiling", "without fuel for boiling")
        )
        and "sick" in lower
    )


def _is_collapsed_new_well_special_case(question):
    """Detect collapsed-well replacement prompts that need a bounded construction answer."""
    lower = question.lower()
    return (
        "well" in lower
        and "collapsed" in lower
        and any(
            marker in lower
            for marker in (
                "dig a new one",
                "new well",
                "replace it",
                "replace the well",
            )
        )
    )


def _is_classic_acs_special_case(question):
    """Detect classic acute-coronary-syndrome symptom clusters."""
    lower = question.lower()
    has_chest_symptom = any(
        marker in lower for marker in ("chest pressure", "chest pain")
    )
    has_exertion_trigger = any(
        marker in lower
        for marker in (
            "after exertion",
            "with exertion",
            "during exertion",
            "after exercise",
            "with exercise",
            "during exercise",
        )
    )
    has_radiating_pain = any(
        marker in lower
        for marker in (
            "left arm",
            "arm pain",
            "pain going into the arm",
            "jaw pain",
            "pain going into the jaw",
        )
    )
    has_breathing_distress = any(
        marker in lower
        for marker in (
            "shortness of breath",
            "trouble breathing",
            "hard to catch my breath",
            "hard to breathe",
        )
    )
    has_sweating = "sweating" in lower
    has_nausea = "nausea" in lower or "nauseated" in lower
    associated_count = sum(
        1
        for flag in (
            has_radiating_pain,
            has_breathing_distress,
            has_sweating,
            has_nausea,
        )
        if flag
    )
    return has_chest_symptom and (associated_count >= 2 or has_exertion_trigger)


def _is_stroke_cardiac_overlap_special_case(question):
    """Detect FAST-sign prompts that also include cardiac-emergency features."""
    lower = question.lower()
    has_collapse_signal = any(
        marker in lower for marker in _COLLAPSE_UNRESPONSIVE_MARKERS
    ) or any(
        marker in lower for marker in ("barely responding", "not responding")
    )
    return _is_noncollapse_stroke_cardiac_overlap_query(question) or (
        _is_acute_overlap_collapse_query(question) and has_collapse_signal
    )


def _has_stroke_tia_routing_signal(question):
    """Detect direct or transient FAST-like stroke/TIA presentations for routing."""
    lower = question.lower()
    has_direct_stroke_terms = any(marker in lower for marker in _STROKE_TIA_MARKERS)
    has_face = any(
        marker in lower for marker in ("face droop", "face drooping", "facial droop")
    )
    has_one_sided_weakness_or_numbness = any(
        marker in lower
        for marker in (
            "arm weakness",
            "arm numbness",
            "leg weakness",
            "leg numbness",
            "one arm feels weak",
            "one arm is weak",
            "one arm is numb",
            "one leg is weak",
            "one leg is numb",
            "one side is weak",
            "one-sided weakness",
            "one-sided numbness",
            "weakness on one side",
            "numbness on one side",
            "one side of the body is weak",
            "one side of the body is numb",
        )
    )
    has_speech_or_word_finding_issue = any(
        marker in lower
        for marker in (
            "slurred speech",
            "speech is slurred",
            "trouble speaking",
            "trouble finding words",
            "difficulty finding words",
            "word-finding difficulty",
            "word finding difficulty",
            "cannot get words out",
            "can't get words out",
            "words are coming out wrong",
            "cannot speak clearly",
        )
    )
    fast_bucket_count = sum(
        (
            has_face,
            has_one_sided_weakness_or_numbness,
            has_speech_or_word_finding_issue,
        )
    )
    has_transient_language = any(
        marker in lower for marker in _TRANSIENT_NEURO_EPISODE_MARKERS
    )
    return has_direct_stroke_terms or fast_bucket_count >= 2 or (
        has_transient_language and fast_bucket_count >= 1
    )


def _is_classic_stroke_fast_special_case(question):
    """Detect classic or transient FAST-positive stroke/TIA clusters without overlap noise."""
    lower = question.lower()
    has_cardiac_or_collapse = any(
        marker in lower for marker in _CARDIAC_OVERLAP_SIGNAL_MARKERS
    ) or any(marker in lower for marker in _CARDIAC_EMERGENCY_MARKERS) or any(
        marker in lower
        for marker in (
            "collapse",
            "collapsed",
            "barely responding",
            "unresponsive",
        )
    )
    return _has_stroke_tia_routing_signal(question) and not has_cardiac_or_collapse


def _is_head_injury_clear_fluid_special_case(question):
    """Detect possible CSF-leak phrasing after head trauma."""
    lower = question.lower()
    has_clear_fluid = any(
        marker in lower
        for marker in (
            "clear fluid is coming from the nose",
            "clear fluid from the nose",
            "clear fluid from the ear",
            "clear fluid from the ears",
            "clear fluid from the nose or ears",
        )
    )
    has_head_trauma = any(
        marker in lower
        for marker in (
            "after a fall",
            "after the fall",
            "after a head injury",
            "after hitting their head",
            "after hitting the head",
            "after a head bump",
            "hit my head",
            "hit their head",
            "head injury",
            "head bump",
        )
    )
    return has_clear_fluid and has_head_trauma


def _is_superglue_wound_special_case(question):
    """Detect direct superglue-for-wounds prompts that want a narrow closure answer."""
    lower = question.lower()
    return "superglue" in lower and any(
        marker in lower for marker in ("close a wound", "close wound", "close this cut")
    )


def _is_rusty_metal_drum_water_special_case(question):
    """Detect rusty-drum water-storage prompts that want a cautious container answer."""
    lower = question.lower()
    return any(
        marker in lower for marker in ("rusty metal drum", "rusty drum")
    ) and any(
        marker in lower for marker in ("store water", "water storage", "hold water")
    )


def _is_group_food_theft_special_case(question):
    """Detect direct food-theft-inside-the-group prompts."""
    lower = question.lower()
    return "stealing food" in lower and any(
        marker in lower
        for marker in ("from the group", "from our group", "what do we do")
    )


def _is_new_group_paranoia_hostility_special_case(question):
    """Detect new-group-contact prompts complicated by internal paranoia/hostility."""
    lower = question.lower()
    return (
        any(
            marker in lower
            for marker in ("another group of survivors", "new group of survivors")
        )
        and "paranoid" in lower
        and "hostile" in lower
    )


def _is_untrained_childbirth_special_case(question):
    """Detect untrained childbirth prompts that need a conservative template."""
    lower = question.lower()
    return _text_has_marker(lower, _UNTRAINED_CHILDBIRTH_MARKERS) and _text_has_marker(
        lower, _UNTRAINED_MARKERS
    )


@dataclass(frozen=True)
class DeterministicSpecialCaseRule:
    """Deterministic control-path rule with a canonical validation prompt."""

    rule_id: str
    predicate: Callable[[str], bool]
    builder: Callable[..., str] | None
    sample_prompt: str
    builder_name: str
    priority: int
    promotion_status: str
    promotion_notes: str | None
    lexical_signature_terms: tuple[str, ...]


def _log_warn_event(event_name, **fields):
    """Emit a structured warning that unit tests and operators can both see."""
    payload = {"event": event_name, **dict(sorted(fields.items()))}
    logger.warning(
        json.dumps(payload, sort_keys=True),
        extra={"event_name": event_name, "telemetry": payload},
    )


def _build_deterministic_builder_missing_debug_note(rule_id, builder_name):
    """Render the debug-only fallback note for missing deterministic builders."""
    return (
        f"Debug note: deterministic rule '{rule_id}' matched, but builder "
        f"'{builder_name}' is unavailable; falling back to retrieval."
    )


def _resolve_deterministic_special_case_rules():
    """Build the live deterministic rule objects from the declarative registry."""
    rules = []
    for spec in DETERMINISTIC_SPECIAL_CASE_SPECS:
        predicate = globals().get(spec.predicate_name)
        if predicate is None:
            predicate = getattr(special_case_builders, spec.predicate_name, None)
        if predicate is None:
            raise RuntimeError(
                f"Deterministic special-case registry references unknown symbol "
                f"{spec.predicate_name!r} for rule {spec.rule_id!r}"
            )
        builder = getattr(special_case_builders, spec.builder_name, None)
        rules.append(
            DeterministicSpecialCaseRule(
                spec.rule_id,
                predicate,
                builder,
                spec.sample_prompt,
                spec.builder_name,
                spec.priority,
                spec.promotion_status,
                spec.promotion_notes,
                spec.lexical_signature_terms,
            )
        )
    return tuple(rules)


_DETERMINISTIC_SPECIAL_CASE_RULES = _resolve_deterministic_special_case_rules()
_DETERMINISTIC_SPECIAL_CASE_SPECS_BY_ID = {
    spec.rule_id: spec for spec in DETERMINISTIC_SPECIAL_CASE_SPECS
}

_DETERMINISTIC_SPECIAL_CASE_BUILDERS = {
    rule.rule_id: rule.builder
    for rule in _DETERMINISTIC_SPECIAL_CASE_RULES
    if rule.builder is not None
}

_ACTIVE_DETERMINISTIC_SEMANTIC_EXCLUSION_MARKERS = {
    "generic_puncture": (
        "animal",
        "bite",
        "bit",
        "bit me",
        "bitten",
        "dog",
        "cat",
        "face",
        "joint",
        "mouth",
        "infected",
        "infection",
        "fever",
        "extract",
        "remove",
        "severe bleeding",
        "uncontrolled bleeding",
        "hemorrhage",
        "spurting",
    ),
    "charcoal_sand_water_filter_starter": (),
    "reused_container_water": (
        "bleach",
        "fuel",
        "solvent",
        "solvents",
        "pesticide",
        "pesticides",
        "paint",
        "rust",
        "rusty",
        "chemical",
        "chemicals",
        "barrel",
        "drum",
        "metal drum",
        "steel barrel",
        "detergent",
        "cleaner",
        "cleaning chemical",
        "cleaning detergent",
    ),
    "water_without_fuel": (
        "sunlight",
        "well is contaminated",
        "the well is contaminated",
        "people are sick",
        "two people are sick",
        "wounded",
        "wounded person",
    ),
    "fire_in_rain": (
        "keep a fire going",
        "stay warm",
        "without starting a fire",
    ),
    "weld_without_welder_starter": (
        "screws and bolts",
        "wood",
        "wooden",
        "boards",
    ),
    "metal_splinter": (
        "eye",
        "eyeball",
        "vision",
        "severe bleeding",
        "uncontrolled bleeding",
        "hemorrhage",
        "spurting",
    ),
    "candles_for_light": (
        "buy candles",
        "burn for",
        "burn time",
        "how long do candles burn",
    ),
    "glassmaking_starter": (
        "make a glass",
        "repair",
        "cracked",
        "bottle",
    ),
}


def get_deterministic_special_case_rules():
    """Expose deterministic rules for validation scripts."""
    return _DETERMINISTIC_SPECIAL_CASE_RULES


def _passes_deterministic_semantic_gate(question, rule):
    """Apply rule-specific exclusion checks for active Android-promoted rules."""
    if rule.promotion_status != "active":
        return True
    exclusion_markers = _ACTIVE_DETERMINISTIC_SEMANTIC_EXCLUSION_MARKERS.get(
        rule.rule_id
    )
    if exclusion_markers is None:
        return True
    return not _text_has_marker(question, exclusion_markers)


def _lexical_signature_size(rule):
    """Return the explicit lexical signature size used for equal-priority ties."""
    return len(rule.lexical_signature_terms)


def _select_deterministic_special_case_rule(matches, *, log_first_defined_tie):
    """Pick one deterministic rule using priority, lexical signature, then order."""
    if not matches:
        return None, None

    winning_priority = max(rule.priority for rule in matches)
    priority_matches = [
        rule for rule in matches if rule.priority == winning_priority
    ]
    if len(priority_matches) == 1:
        return priority_matches[0], "priority"

    winning_signature_size = max(
        _lexical_signature_size(rule) for rule in priority_matches
    )
    signature_matches = [
        rule
        for rule in priority_matches
        if _lexical_signature_size(rule) == winning_signature_size
    ]
    if len(signature_matches) == 1:
        return signature_matches[0], "lexical_signature"

    winner = signature_matches[0]
    if log_first_defined_tie:
        _log_warn_event(
            "deterministic_priority_tie",
            lexical_signature_size=winning_signature_size,
            priority=winning_priority,
            tied_rule_ids=",".join(rule.rule_id for rule in signature_matches),
            winner_rule_id=winner.rule_id,
        )
    return winner, "first_defined"


def _match_deterministic_special_case(question):
    """Return the semantic rule name for a deterministic special case when matched."""
    matches = [
        rule
        for rule in _DETERMINISTIC_SPECIAL_CASE_RULES
        if rule.predicate(question) and _passes_deterministic_semantic_gate(question, rule)
    ]
    winning_rule, _ = _select_deterministic_special_case_rule(
        matches,
        log_first_defined_tie=True,
    )
    return winning_rule.rule_id if winning_rule else None


def get_deterministic_special_case_overlaps():
    """Return canonical-prompt overlap records for deterministic predicates."""
    overlaps = []
    for source_rule in _DETERMINISTIC_SPECIAL_CASE_RULES:
        matches = [
            rule
            for rule in _DETERMINISTIC_SPECIAL_CASE_RULES
            if rule.predicate(source_rule.sample_prompt)
        ]
        if len(matches) < 2:
            continue
        winner, winner_reason = _select_deterministic_special_case_rule(
            matches,
            log_first_defined_tie=False,
        )
        overlaps.append(
            {
                "source_rule_id": source_rule.rule_id,
                "sample_prompt": source_rule.sample_prompt,
                "matches": [
                    {
                        "rule_id": rule.rule_id,
                        "lexical_signature_size": _lexical_signature_size(rule),
                        "priority": rule.priority,
                        "promotion_status": rule.promotion_status,
                    }
                    for rule in matches
                ],
                "winner_reason": winner_reason,
                "winner_rule_ids": [winner.rule_id] if winner is not None else [],
            }
        )
    return overlaps


def classify_special_case(question):
    """Return the special-case routing labels for a question when applicable."""
    if _is_system_behavior_query(question):
        return "system-behavior", "system_behavior"
    if _is_underspecified_stub_query(question):
        return "underspecified", question.strip().lower().replace(" ", "_")
    if _is_broad_survey_query(question):
        return "broad-survey", "broad_survey"
    if _is_hazardous_unsupported_query(question):
        return "hazardous-unsupported", "hazardous_unsupported"
    if _is_clearly_off_topic(question):
        return "off-topic", "off_topic"
    detail = _match_deterministic_special_case(question)
    if detail:
        return "deterministic", detail
    return None, None


def build_special_case_response(
    question, *, debug_enabled=False, fallback_note_collector=None
):
    """Return a deterministic non-RAG response for obvious special cases."""
    decision_path, decision_detail = classify_special_case(question)
    if decision_path == "system-behavior":
        return special_case_builders._build_system_behavior_response(question)
    if decision_path == "underspecified":
        return special_case_builders._build_stub_query_response(question)
    if decision_path == "broad-survey":
        return special_case_builders._build_broad_survey_response()
    if decision_path == "hazardous-unsupported":
        return special_case_builders._build_hazardous_unsupported_response()
    if decision_path == "off-topic":
        return (
            "That is outside this knowledge base. I can help with survival, first aid, "
            "water, shelter, fire, salvage, or group decision-making instead."
        )
    if decision_path == "deterministic":
        builder = _DETERMINISTIC_SPECIAL_CASE_BUILDERS.get(decision_detail)
        if builder:
            return builder()
        spec = _DETERMINISTIC_SPECIAL_CASE_SPECS_BY_ID.get(decision_detail)
        builder_name = spec.builder_name if spec is not None else "<unknown>"
        _log_warn_event(
            "deterministic.builder_missing",
            builder_name=builder_name,
            decision_path="deterministic",
            fallback="retrieval",
            rule_id=decision_detail,
        )
        if debug_enabled and fallback_note_collector is not None:
            fallback_note_collector.append(
                _build_deterministic_builder_missing_debug_note(
                    decision_detail,
                    builder_name,
                )
            )
    return None


def _metadata_rerank_chunk_id(meta):
    """Return a stable chunk identifier for reranker debug logs."""
    for key in ("chunk_id", "id", "guide_id", "source_file", "slug"):
        value = str((meta or {}).get(key, "")).strip()
        if value:
            return value
    return "unknown"


def _normalized_metadata_tags(meta):
    """Return normalized metadata tags from either CSV strings or iterables."""
    raw_tags = (meta or {}).get("tags", "")
    return set(normalize_tags(raw_tags))


def _is_bridge_guide_metadata(meta):
    """Detect ingest-tagged bridge guides, with a tags fallback for older rows."""
    bridge_value = (meta or {}).get("bridge")
    if isinstance(bridge_value, bool):
        return bridge_value

    normalized_bridge = normalize_metadata_tag(bridge_value)
    if normalized_bridge:
        return normalized_bridge == "bridge-guide"

    return "bridge-guide" in _normalized_metadata_tags(meta)


def _apply_metadata_rerank_delta(
    cumulative, branch_name, branch_delta, chunk_id, debug_enabled=None
):
    """Apply one reranker branch delta with clamping and optional debug logging."""
    if not branch_delta:
        return cumulative
    next_cumulative = max(
        METADATA_RERANK_DELTA_MIN,
        min(METADATA_RERANK_DELTA_MAX, cumulative + float(branch_delta)),
    )
    applied_delta = next_cumulative - cumulative
    if debug_enabled is None:
        debug_enabled = logger.isEnabledFor(logging.DEBUG)
    if debug_enabled:
        logger.debug(
            "rerank_delta chunk=%s branch=%s delta=%.3f cumulative=%.3f",
            chunk_id,
            branch_name,
            applied_delta,
            next_cumulative,
        )
    return next_cumulative


def _metadata_rerank_delta(question, meta):
    """Return a distance adjustment based on prompt intent and metadata."""
    question_lower = question.lower()
    meta_text = " ".join(
        [
            meta.get("guide_title", ""),
            meta.get("section_heading", ""),
            meta.get("slug", ""),
            meta.get("description", ""),
            meta.get("category", ""),
            meta.get("related", ""),
            meta.get("tags", ""),
            meta.get("source_file", ""),
        ]
    ).lower()
    category = meta.get("category", "")
    delta = 0.0
    debug_enabled = logger.isEnabledFor(logging.DEBUG)
    chunk_id = _metadata_rerank_chunk_id(meta)

    def apply_delta(branch_name, branch_delta):
        nonlocal delta
        delta = _apply_metadata_rerank_delta(
            delta, branch_name, branch_delta, chunk_id, debug_enabled
        )

    if _is_bridge_guide_metadata(meta) and _is_bridge_demoted_acute_query(question):
        apply_delta("acute_bridge_guide_uniform", 0.06)

    if _is_mental_health_crisis_query(question):
        mania_or_psychosis_like = _is_mania_or_psychosis_like_query(question)
        if mania_or_psychosis_like and meta.get("guide_id") == "GD-918":
            apply_delta("mental_health_gd918_mania", 0.35)
        explicit_geriatric_or_cognitive = (
            _has_explicit_geriatric_or_cognitive_decline_evidence(question)
        )
        elder_cognitive_metadata = _text_has_marker(
            meta_text, _ELDER_COGNITIVE_METADATA_MARKERS
        )
        has_high_acuity_mental_health_metadata = _text_has_marker(
            meta_text, _MENTAL_HEALTH_CRISIS_HIGH_ACUITY_METADATA_MARKERS
        )
        has_support_mental_health_metadata = _text_has_marker(
            meta_text, _MENTAL_HEALTH_CRISIS_SUPPORT_METADATA_MARKERS
        )
        has_sleep_self_management_metadata = _text_has_marker(
            meta_text, _MENTAL_HEALTH_CRISIS_SLEEP_SELF_MANAGEMENT_METADATA_MARKERS
        )
        has_distractor_mental_health_metadata = _text_has_marker(
            meta_text, _MENTAL_HEALTH_CRISIS_DISTRACTOR_METADATA_MARKERS
        )
        has_routine_self_management_metadata = any(
            marker in meta_text
            for marker in (
                "maintain basic function",
                "basic function and routine",
                "basic function",
                "predictable routine",
                "maintain routine",
                "routine reset",
                "sleep hygiene",
                "self-management",
                "self management",
                "predictable meals",
                "meals and sleep",
                "sleep patterns",
                "reduce anxiety",
                "routine self-care",
                "routine self care",
                "journaling",
            )
        )
        if category in {"medical", "society"}:
            apply_delta("mental_health_category_medical_society", -0.035)
        elif category in {
            "survival",
            "building",
            "salvage",
            "defense",
            "communications",
            "resource-management",
        }:
            apply_delta("mental_health_category_fieldcraft", 0.08)

        if has_high_acuity_mental_health_metadata:
            apply_delta(
                "mental_health_high_acuity",
                -0.24 if mania_or_psychosis_like else -0.08,
            )
        if has_support_mental_health_metadata:
            if mania_or_psychosis_like:
                apply_delta(
                    "mental_health_support_mania",
                    0.18 if not has_high_acuity_mental_health_metadata else 0.02,
                )
            else:
                apply_delta("mental_health_support_nonmania", -0.04)
        if mania_or_psychosis_like and has_sleep_self_management_metadata:
            apply_delta(
                "mental_health_sleep_self_management",
                0.42 if not has_high_acuity_mental_health_metadata else 0.18,
            )
        if mania_or_psychosis_like and has_routine_self_management_metadata:
            apply_delta(
                "mental_health_routine_self_management",
                0.28 if not has_high_acuity_mental_health_metadata else 0.12,
            )
        if has_distractor_mental_health_metadata:
            if not (explicit_geriatric_or_cognitive and elder_cognitive_metadata):
                apply_delta(
                    "mental_health_distractor",
                    0.36 if mania_or_psychosis_like else 0.18,
                )
        if (
            mania_or_psychosis_like
            and elder_cognitive_metadata
            and not explicit_geriatric_or_cognitive
        ):
            apply_delta("mental_health_elder_cognitive_mismatch", 0.34)
        if (
            mania_or_psychosis_like
            and has_high_acuity_mental_health_metadata
            and (
                has_support_mental_health_metadata
                or has_sleep_self_management_metadata
                or has_routine_self_management_metadata
            )
        ):
            apply_delta("mental_health_high_acuity_self_management_overlap", -0.08)

        if any(
            marker in question_lower
            for marker in ("withdrawal", "stopped drinking", "alcohol")
        ):
            if _text_has_marker(meta_text, _ALCOHOL_WITHDRAWAL_POSITIVE_METADATA_MARKERS):
                apply_delta("mental_health_alcohol_withdrawal_positive", -0.08)

    if _is_household_chemical_eye_query(question):
        if _text_has_marker(meta_text, _CHEMICAL_EYE_GUIDE_METADATA_MARKERS):
            apply_delta("household_chemical_eye_positive", -0.12)
        if _text_has_marker(meta_text, _LAB_SAFETY_METADATA_MARKERS):
            apply_delta("household_chemical_eye_lab_safety_distractor", 0.14)

    if _is_household_chemical_inhalation_query(question):
        if _text_has_marker(meta_text, _HOUSEHOLD_CHEMICAL_EXPOSURE_METADATA_MARKERS):
            apply_delta("household_chemical_inhalation_positive", -0.06)
        if _text_has_marker(meta_text, _COOKSTOVE_CO_METADATA_MARKERS):
            apply_delta("household_chemical_inhalation_cookstove_distractor", 0.16)

    if _is_human_medical_query(question):
        if category == "medical":
            apply_delta("medical_category_medical", -0.03)
        elif category == "survival":
            apply_delta("medical_category_survival", -0.01)
        elif category in {
            "agriculture",
            "transportation",
            "crafts",
            "metalworking",
            "power-generation",
            "salvage",
            "building",
        }:
            apply_delta("medical_category_distractor", 0.055)

        if _text_has_marker(meta_text, _VETERINARY_METADATA_MARKERS):
            apply_delta("medical_veterinary_distractor", 0.18)

        if _text_has_marker(meta_text, _MEDICAL_METADATA_MARKERS):
            apply_delta("medical_positive_metadata", -0.02)

        if _is_urinary_query(question_lower):
            if _text_has_marker(meta_text, _URINARY_METADATA_MARKERS):
                apply_delta("urinary_positive_metadata", -0.06)
            if _text_has_marker(meta_text, _BOWEL_RECTAL_DISTRACTOR_MARKERS):
                apply_delta("urinary_bowel_distractor", 0.08)

        gi_bleed_markers = (
            "coffee grounds",
            "coffee ground vomit",
            "black tarry stool",
            "black tarry stools",
            "black stool",
            "black stools",
            "bright red vomit",
            "vomit blood",
            "vomiting blood",
            "dark clots",
        )
        if any(marker in question_lower for marker in gi_bleed_markers):
            if _text_has_marker(
                meta_text,
                (
                    "gastrointestinal bleeding",
                    "gi bleed",
                    "upper gi bleed",
                    "hematemesis",
                    "melena",
                    "coffee ground",
                    "vomiting blood",
                    "black tarry stool",
                    "black stool",
                ),
            ):
                apply_delta("gi_bleed_positive_metadata", -0.09)
            if any(
                term in meta_text
                for term in (
                    "common ailments",
                    "common ailment",
                    "common-ailments",
                    "food safety",
                    "food-safety",
                    "food poisoning",
                    "hemorrhoid",
                    "hemorrhoids",
                    "reflux",
                    "acid reflux",
                    "heartburn",
                    "gerd",
                )
            ):
                apply_delta("gi_bleed_distractor", 0.24)

        if _is_acute_symptom_query(question):
            if category not in {"medical", "survival"}:
                apply_delta("acute_symptom_nonmedical", 0.06)
            if any(
                term in meta_text
                for term in (
                    "first aid",
                    "emergency",
                    "triage",
                    "red flag",
                    "evacuation",
                )
            ):
                apply_delta("acute_symptom_first_aid", -0.04)

        if _is_acute_overlap_collapse_query(question):
            if category not in {"medical", "survival"}:
                apply_delta("acute_overlap_nonmedical", 0.08)
            if any(
                term in meta_text
                for term in (
                    "first aid",
                    "emergency",
                    "cpr",
                    "cardiac",
                    "heart attack",
                    "stroke",
                    "red flag",
                    "triage",
                )
            ):
                apply_delta("acute_overlap_emergency_positive", -0.07)
            elif category == "medical":
                apply_delta("acute_overlap_medical_nontriage", 0.03)

        if _is_noncollapse_stroke_cardiac_overlap_query(question):
            if category not in {"medical", "survival"}:
                apply_delta("stroke_cardiac_overlap_nonmedical", 0.08)
            if any(
                term in meta_text
                for term in (
                    "first aid",
                    "emergency",
                    "cpr",
                    "cardiac",
                    "heart attack",
                    "stroke",
                    "tia",
                    "transient ischemic attack",
                    "neurologic",
                    "red flag",
                    "triage",
                )
            ):
                apply_delta("stroke_cardiac_overlap_emergency_positive", -0.08)
            elif category == "medical":
                apply_delta("stroke_cardiac_overlap_medical_nontriage", 0.03)

        if _is_household_chemical_hazard_query(question):
            if category not in {"medical", "survival"}:
                apply_delta("household_chemical_nonmedical", 0.06)
            if any(
                term in meta_text
                for term in (
                    "poison",
                    "toxicology",
                    "corrosive",
                    "acid",
                    "battery acid",
                    "chemical burn",
                    "eye irrigation",
                    "decontamination",
                    "poison control",
                    "chemical exposure",
                    "hazmat",
                    "evacuation",
                )
            ):
                apply_delta("household_chemical_emergency_positive", -0.05)

        if not _text_has_marker(question_lower, _EXPLICIT_INVASIVE_QUERY_MARKERS):
            if _text_has_marker(meta_text, _INVASIVE_MEDICAL_METADATA_MARKERS):
                apply_delta("invasive_medical_penalty", 0.11)
            if _text_has_marker(meta_text, _CONSERVATIVE_MEDICAL_METADATA_MARKERS):
                apply_delta("conservative_medical_bonus", -0.03)

        if _is_generic_puncture_query(question_lower):
            if (
                "trauma hemorrhage control" in meta_text
                and not _has_major_bleeding_signal(question_lower)
            ):
                apply_delta("puncture_trauma_distractor", 0.09)
            if "hemorrhage control" in meta_text and not _has_major_bleeding_signal(
                question_lower
            ):
                apply_delta("puncture_hemorrhage_distractor", 0.05)
            if _text_has_marker(meta_text, _PUNCTURE_CONSERVATIVE_METADATA_MARKERS):
                apply_delta("puncture_conservative_metadata", -0.05)
            if any(
                term in meta_text
                for term in ("wound management", "first aid essentials")
            ):
                apply_delta("puncture_first_aid_metadata", -0.035)
            if _text_has_marker(meta_text, _PUNCTURE_DISTRACTOR_METADATA_MARKERS):
                apply_delta("puncture_distractor_metadata", 0.10)
            if any(term in meta_text for term in ("animal bite", "rabies")):
                apply_delta("puncture_animal_bite_distractor", 0.07)
            if _text_has_marker(
                meta_text, _HERBAL_METADATA_MARKERS
            ) and not _text_has_marker(
                question_lower, _EXPLICIT_NATURAL_REMEDY_QUERY_MARKERS
            ):
                apply_delta("puncture_herbal_distractor", 0.08)

        if _text_has_marker(question_lower, _ANIMAL_BITE_QUERY_MARKERS):
            if category in {"agriculture", "transportation"}:
                apply_delta("animal_bite_nonmedical_category", 0.04)
            if any(
                term in meta_text
                for term in (
                    "rabies",
                    "bite wound",
                    "animal bites as medical emergencies",
                )
            ):
                apply_delta("animal_bite_positive_metadata", -0.02)

        if _text_has_marker(question_lower, _SPLINTER_QUERY_MARKERS):
            if any(term in meta_text for term in _SPLINTER_MEDICAL_METADATA_MARKERS):
                apply_delta("splinter_positive_metadata", -0.035)
            elif category in {"metalworking", "salvage", "crafts", "building"}:
                apply_delta("splinter_trade_distractor", 0.05)

            if "bite wound" in meta_text:
                apply_delta("splinter_bite_distractor", 0.05)

            if not _text_has_marker(
                question_lower, _EYE_METADATA_MARKERS
            ) and _text_has_marker(meta_text, _EYE_METADATA_MARKERS):
                apply_delta("splinter_eye_distractor", 0.08)

    if _text_has_marker(question_lower, _VEHICLE_QUERY_MARKERS):
        if category in {
            "transportation",
            "building",
            "salvage",
            "power-generation",
            "crafts",
        }:
            apply_delta("vehicle_relevant_category", -0.015)
        elif category in {"medical", "agriculture"}:
            apply_delta("vehicle_irrelevant_category", 0.03)

        if _text_has_marker(
            question_lower, _CAR_SPECIFIC_QUERY_MARKERS
        ) and _text_has_marker(meta_text, _BICYCLE_METADATA_MARKERS):
            apply_delta("car_bicycle_distractor", 0.18)

        if _text_has_marker(meta_text, _VEHICLE_METADATA_MARKERS):
            apply_delta("vehicle_positive_metadata", -0.025)

    if _text_has_marker(question_lower, _BATTERY_QUERY_MARKERS):
        if category in {"salvage", "power-generation", "transportation"}:
            apply_delta("battery_relevant_category", -0.02)
        if "battery" in meta_text:
            apply_delta("battery_positive_metadata", -0.02)

    if _is_supply_conflict_query(question_lower):
        if category in {"society", "resource-management"}:
            apply_delta("supply_conflict_core_category", -0.025)
        elif category == "defense":
            if _text_has_marker(meta_text, {"de-escalation", "negotiation"}):
                apply_delta("supply_conflict_defense_deescalation", -0.01)
            else:
                apply_delta("supply_conflict_defense_force_bias", 0.035)

        if _text_has_marker(meta_text, _LOW_BUREAUCRACY_CONFLICT_METADATA_MARKERS):
            apply_delta("supply_conflict_low_bureaucracy_metadata", -0.05)

        if not _text_has_marker(
            question_lower, _EXPLICIT_FORMAL_GOVERNANCE_QUERY_MARKERS
        ):
            if _text_has_marker(meta_text, _FORMAL_ADJUDICATION_METADATA_MARKERS):
                apply_delta("supply_conflict_formal_adjudication_distractor", 0.12)

        if _text_has_marker(meta_text, _THEATER_METADATA_MARKERS):
            apply_delta("supply_conflict_theater_distractor", 0.16)

    if _is_post_collapse_recovery_query(question_lower):
        if category in {
            "survival",
            "medical",
            "society",
            "agriculture",
            "resource-management",
        }:
            apply_delta("recovery_core_categories", -0.025)
        elif category == "defense" and not _mentions_attack_or_radiation(
            question_lower
        ):
            apply_delta("recovery_defense_distractor", 0.10)
            if any(
                term in meta_text
                for term in (
                    "fallout",
                    "nbc defense",
                    "post-attack",
                    "fortification",
                    "nuclear survival",
                )
            ):
                apply_delta("recovery_attack_context_distractor", 0.08)

    if _is_supply_hiding_query(question_lower):
        if category == "resource-management":
            apply_delta("supply_hiding_resource_management", -0.06)
        elif category in {"defense", "survival"}:
            apply_delta("supply_hiding_defense_survival", -0.015)
        elif category in {
            "crafts",
            "textiles-fiber-arts",
            "transportation",
            "metalworking",
        }:
            apply_delta("supply_hiding_trade_distractor", 0.05)

        if any(
            term in meta_text
            for term in (
                "storage & material management",
                "supply caches",
                "hidden supply caches",
            )
        ):
            apply_delta("supply_hiding_cache_positive", -0.08)
        if any(
            term in meta_text
            for term in ("waterproof container", "decoy cache", "cache security")
        ):
            apply_delta("supply_hiding_container_positive", -0.04)

    if _is_glassmaking_query(question_lower):
        if category in {"crafts", "chemistry"}:
            apply_delta("glassmaking_core_categories", -0.025)
        elif category in {"building", "salvage"}:
            apply_delta("glassmaking_building_salvage_distractor", 0.02)

        if _text_has_marker(meta_text, _GLASS_POSITIVE_METADATA_MARKERS):
            apply_delta("glassmaking_positive_metadata", -0.055)
        if _text_has_marker(meta_text, _GLASS_DISTRACTOR_METADATA_MARKERS):
            apply_delta("glassmaking_distractor_metadata", 0.12)

    if _is_bow_arrow_query(question_lower):
        if category == "defense":
            apply_delta("bow_arrow_defense", -0.03)
        elif category == "society":
            apply_delta("bow_arrow_society_distractor", 0.10)

        if _text_has_marker(meta_text, _BOW_ARROW_POSITIVE_METADATA_MARKERS):
            apply_delta("bow_arrow_positive_metadata", -0.055)
        if _text_has_marker(meta_text, _BOW_ARROW_DISTRACTOR_METADATA_MARKERS):
            apply_delta("bow_arrow_distractor_metadata", 0.18)

    if _is_no_gear_fishing_query(question_lower):
        if category in {"survival", "agriculture", "biology"}:
            apply_delta("no_gear_fishing_core_categories", -0.025)

        if _text_has_marker(meta_text, _NO_GEAR_FISHING_POSITIVE_METADATA_MARKERS):
            apply_delta("no_gear_fishing_positive_metadata", -0.05)
        if _text_has_marker(meta_text, _NO_GEAR_FISHING_DISTRACTOR_METADATA_MARKERS):
            apply_delta("no_gear_fishing_distractor_metadata", 0.18)
        if any(
            term in meta_text
            for term in ("open water", "shore-based fishing", "marine biology")
        ):
            apply_delta("no_gear_fishing_open_water_distractor", 0.10)
        if any(
            term in meta_text
            for term in (
                "feeding fish",
                "harvesting & processing",
                "pond construction",
                "water quality testing",
            )
        ):
            apply_delta("no_gear_fishing_aquaculture_distractor", 0.06)

    if _is_natural_dye_query(question_lower):
        if category in {"textiles-fiber-arts", "crafts"}:
            apply_delta("natural_dye_core_categories", -0.025)

        if _text_has_marker(meta_text, _NATURAL_DYE_POSITIVE_METADATA_MARKERS):
            apply_delta("natural_dye_positive_metadata", -0.05)
        if _text_has_marker(meta_text, _NATURAL_DYE_DISTRACTOR_METADATA_MARKERS):
            apply_delta("natural_dye_distractor_metadata", 0.05)

    if _is_weld_without_welder_query(question_lower):
        if category == "metalworking":
            apply_delta("weld_core_category", -0.03)
        elif category == "power-generation":
            apply_delta("weld_power_distractor", 0.02)

        if _text_has_marker(meta_text, _WELD_POSITIVE_METADATA_MARKERS):
            apply_delta("weld_positive_metadata", -0.055)
        if _text_has_marker(meta_text, _WELD_DISTRACTOR_METADATA_MARKERS):
            apply_delta("weld_distractor_metadata", 0.12)

    if _is_roman_concrete_query(question_lower):
        if category in {"building", "culture-knowledge"}:
            apply_delta("roman_concrete_core_categories", -0.02)
        if _text_has_marker(meta_text, _ROMAN_CONCRETE_POSITIVE_METADATA_MARKERS):
            apply_delta("roman_concrete_positive_metadata", -0.055)
        if _text_has_marker(meta_text, _ROMAN_CONCRETE_DISTRACTOR_METADATA_MARKERS):
            apply_delta("roman_concrete_distractor_metadata", 0.09)

    if _is_smoke_meat_query(question_lower):
        if category in {"agriculture", "survival"}:
            apply_delta("smoke_meat_core_categories", -0.02)
        if _text_has_marker(meta_text, _SMOKE_MEAT_POSITIVE_METADATA_MARKERS):
            apply_delta("smoke_meat_positive_metadata", -0.05)

    if _is_raft_lake_query(question_lower):
        if category in {"transportation", "survival"}:
            apply_delta("raft_lake_core_categories", -0.025)
        elif category == "building":
            apply_delta("raft_lake_building_distractor", 0.015)

        if _text_has_marker(meta_text, _RAFT_LAKE_POSITIVE_METADATA_MARKERS):
            apply_delta("raft_lake_positive_metadata", -0.05)
        if _text_has_marker(meta_text, _RAFT_LAKE_DISTRACTOR_METADATA_MARKERS):
            apply_delta("raft_lake_distractor_metadata", 0.10)

    if _is_house_build_query(question_lower):
        if category in {"building", "survival", "resource-management", "utility"}:
            apply_delta("house_build_core_categories", -0.025)
        elif category in {"medical", "society"}:
            apply_delta("house_build_medical_society_distractor", 0.08)

        if _text_has_marker(meta_text, _HOUSE_BUILD_POSITIVE_METADATA_MARKERS):
            apply_delta("house_build_positive_metadata", -0.055)
        if _text_has_marker(meta_text, _HOUSE_BUILD_DISTRACTOR_METADATA_MARKERS):
            apply_delta("house_build_distractor_metadata", 0.18)

    if _is_small_watercraft_query(question_lower):
        if category in {"transportation", "building", "survival"}:
            apply_delta("small_watercraft_core_categories", -0.025)
        elif category in {"medical", "society"}:
            apply_delta("small_watercraft_medical_society_distractor", 0.08)

        if _text_has_marker(meta_text, _SMALL_WATERCRAFT_POSITIVE_METADATA_MARKERS):
            apply_delta("small_watercraft_positive_metadata", -0.055)
        if _text_has_marker(meta_text, _SMALL_WATERCRAFT_DISTRACTOR_METADATA_MARKERS):
            apply_delta("small_watercraft_distractor_metadata", 0.12)

    if _is_water_purification_query(question_lower):
        if category in {
            "survival",
            "utility",
            "medical",
            "resource-management",
            "building",
            "chemistry",
        }:
            apply_delta("water_purification_core_categories", -0.025)
        elif category in {"biology", "agriculture"}:
            apply_delta("water_purification_bio_ag_distractor", 0.06)

        if _text_has_marker(meta_text, _WATER_PURIFICATION_POSITIVE_METADATA_MARKERS):
            apply_delta("water_purification_positive_metadata", -0.055)
        if _text_has_marker(meta_text, _WATER_PURIFICATION_DISTRACTOR_METADATA_MARKERS):
            apply_delta("water_purification_distractor_metadata", 0.12)

    if _is_water_storage_query(question_lower):
        if category in {
            "survival",
            "resource-management",
            "utility",
            "medical",
            "building",
        }:
            apply_delta("water_storage_core_categories", -0.025)
        elif category in {"biology", "agriculture", "chemistry"}:
            apply_delta("water_storage_bio_ag_chem_distractor", 0.05)

        if _text_has_marker(meta_text, _WATER_STORAGE_POSITIVE_METADATA_MARKERS):
            apply_delta("water_storage_positive_metadata", -0.05)
        if _text_has_marker(meta_text, _WATER_STORAGE_DISTRACTOR_METADATA_MARKERS):
            apply_delta("water_storage_distractor_metadata", 0.12)

    if _is_fire_in_rain_special_case(question_lower):
        if category == "survival":
            apply_delta("fire_in_rain_survival", -0.03)
        elif category in {"building", "defense", "medical"}:
            apply_delta("fire_in_rain_other_category_distractor", 0.05)

        if _text_has_marker(meta_text, _FIRE_IN_RAIN_POSITIVE_METADATA_MARKERS):
            apply_delta("fire_in_rain_positive_metadata", -0.055)
        if _text_has_marker(meta_text, _FIRE_IN_RAIN_DISTRACTOR_METADATA_MARKERS):
            apply_delta("fire_in_rain_distractor_metadata", 0.12)

    if _is_closed_room_fire_question(question_lower):
        if category in {"building", "medical"}:
            apply_delta("closed_room_fire_building_medical_distractor", 0.05)
        elif category == "survival":
            apply_delta("closed_room_fire_survival", -0.03)

    if _is_cast_without_foundry_query(question_lower):
        if category == "metalworking":
            apply_delta("cast_without_foundry_core_category", -0.03)
        if _text_has_marker(meta_text, _CAST_WITHOUT_FOUNDRY_POSITIVE_METADATA_MARKERS):
            apply_delta("cast_without_foundry_positive_metadata", -0.05)

    if _is_fair_trial_query(question_lower):
        if category == "society":
            apply_delta("fair_trial_society", -0.03)

        if _text_has_marker(meta_text, _FAIR_TRIAL_POSITIVE_METADATA_MARKERS):
            apply_delta("fair_trial_positive_metadata", -0.055)
        if _text_has_marker(meta_text, _FAIR_TRIAL_DISTRACTOR_METADATA_MARKERS):
            apply_delta("fair_trial_distractor_metadata", 0.10)

    if _is_soapmaking_query(question_lower):
        if category in {"crafts", "chemistry", "medical"}:
            apply_delta("soapmaking_core_categories", -0.02)

        if _text_has_marker(meta_text, _SOAP_POSITIVE_METADATA_MARKERS):
            apply_delta("soapmaking_positive_metadata", -0.04)
        if _text_has_marker(meta_text, _SOAP_DISTRACTOR_METADATA_MARKERS):
            apply_delta("soapmaking_distractor_metadata", 0.06)

    if _is_brain_tanning_query(question_lower):
        if category in {"crafts", "textiles-fiber-arts"}:
            apply_delta("brain_tanning_core_categories", -0.02)

        if _text_has_marker(meta_text, _BRAIN_TANNING_POSITIVE_METADATA_MARKERS):
            apply_delta("brain_tanning_positive_metadata", -0.05)
        if _text_has_marker(meta_text, _BRAIN_TANNING_DISTRACTOR_METADATA_MARKERS):
            apply_delta("brain_tanning_distractor_metadata", 0.08)

    if _is_hot_weather_burial_query(question_lower):
        if category in {"medical", "society"}:
            apply_delta("hot_burial_core_categories", -0.025)
        elif category == "survival":
            apply_delta("hot_burial_survival_distractor", 0.02)

        if _text_has_marker(meta_text, _HOT_BURIAL_POSITIVE_METADATA_MARKERS):
            apply_delta("hot_burial_positive_metadata", -0.05)
        if _text_has_marker(meta_text, _HOT_BURIAL_DISTRACTOR_METADATA_MARKERS):
            apply_delta("hot_burial_distractor_metadata", 0.10)

    return delta


def _detect_domains(text):
    """Return set of domain names that have keyword matches in text."""
    lower = text.lower()
    matched = set()
    for domain, keywords in _DOMAIN_KEYWORDS.items():
        for kw in keywords:
            # Multi-word keywords use substring match, single words use word boundary
            if " " in kw:
                if kw in lower:
                    matched.add(domain)
                    break
            else:
                if re.search(r"\b" + re.escape(kw) + r"\b", lower):
                    matched.add(domain)
                    break
    return matched


def _split_at_question_restart(question):
    """Split at mid-query question-word restarts (what/how/where/when/why + do/should/can)."""
    pattern = r"(?<=\s)((?:what|how|where|when|why)\s+(?:do|should|can|is|are)\s)"
    parts = re.split(pattern, question, flags=re.IGNORECASE)
    if len(parts) <= 1:
        return []
    # Re-join the split pattern with its following text
    segments = [parts[0].strip()]
    for i in range(1, len(parts), 2):
        rejoined = (parts[i] + (parts[i + 1] if i + 1 < len(parts) else "")).strip()
        if rejoined:
            segments.append(rejoined)
    return [s for s in segments if len(s.split()) >= 3]


# Verbs that signal a clause is query-bearing (asking or describing something
# actionable) rather than just stating a resource/context constraint.
_QUERY_VERBS = {
    "is",
    "are",
    "was",
    "were",
    "have",
    "has",
    "had",
    "need",
    "want",
    "cant",
    "wont",
    "should",
    "must",
    "do",
    "does",
    "did",
    "dont",
    "build",
    "make",
    "fix",
    "treat",
    "find",
    "move",
    "get",
    "start",
    "stop",
    "help",
    "keep",
    "run",
    "go",
    "set",
    "cut",
    "dig",
    "kill",
    "eat",
    "drink",
    "boil",
    "burn",
    "use",
    "collect",
    "preserve",
    "protect",
    "repair",
    "filter",
    "purify",
    "evacuate",
    "survive",
    "lost",
    "broke",
    "broken",
    "collapsed",
    "failed",
    "spreading",
    "approaching",
    "trapped",
    "contaminated",
    "destroyed",
    "injured",
    "bleeding",
    "dying",
    "sick",
}

_MEANINGFUL_QUERY_VERBS = _QUERY_VERBS - {
    "is",
    "are",
    "was",
    "were",
    "have",
    "has",
    "had",
    "need",
    "want",
    "do",
    "does",
    "did",
}


def _looks_like_inventory_fragment(clause):
    """Return True for resource inventory fragments that should stay context-only."""
    stripped = clause.lower().strip()
    words = re.findall(r"[a-z0-9-]+", stripped)
    if not words:
        return False

    if re.match(r"^(?:we|i)\s+(?:have|got)\b", stripped):
        if set(words) & _INVENTORY_NEGATION_WORDS:
            return False
        return not bool(set(words) & _MEANINGFUL_QUERY_VERBS)

    if len(words) <= 4 and words[0] in _INVENTORY_LEAD_WORDS:
        return not bool(set(words) & _MEANINGFUL_QUERY_VERBS)

    return False


def _is_query_bearing(clause):
    """Check if a comma-split clause contains a verb or domain keyword,
    indicating it's an actual information need rather than a bare context
    fragment like 'we have tools'."""
    if _looks_like_inventory_fragment(clause):
        return False

    words = set(clause.lower().split())
    # Has a query verb?
    if words & _QUERY_VERBS:
        return True
    # No verb but has a strong domain keyword? (e.g., "no clean water")
    return len(_detect_domains(clause)) > 0


def _has_meaningful_restart_content(clause):
    """Reject restart fragments that are just scaffolding like 'what do I do'."""
    if _detect_domains(clause):
        return True

    words = re.findall(r"[a-z0-9']+", clause.lower())
    content_words = [word for word in words if word not in _GENERIC_QUERY_FILLER_WORDS]
    if not content_words:
        return False

    meaningful_verbs = _QUERY_VERBS - {
        "is",
        "are",
        "was",
        "were",
        "have",
        "has",
        "had",
        "need",
        "want",
        "can",
        "cant",
        "should",
        "must",
        "do",
        "does",
        "did",
        "dont",
    }
    return len(content_words) >= 2 or bool(set(content_words) & meaningful_verbs)


def _unique_ordered(items):
    """Return unique non-empty strings in first-seen order."""
    seen = set()
    ordered = []
    for item in items:
        normalized = item.strip()
        if not normalized:
            continue
        key = normalized.lower()
        if key in seen:
            continue
        seen.add(key)
        ordered.append(normalized)
    return ordered


def _is_generic_asset_placeholder(text):
    """Return True for vague placeholder phrases that are not real assets."""
    normalized = " ".join(re.findall(r"[a-z0-9']+", (text or "").lower()))
    return normalized in {
        "what i have",
        "what we have",
        "what ive got",
        "what we've got",
        "what weve got",
        "what we got",
        "what i got",
        "what i ve got",
        "what we ve got",
    }


def _split_scenario_clauses(question):
    """Split a scenario prompt into meaningful user-written clauses."""
    comma_parts = [p.strip() for p in question.split(",")]
    comma_parts = [
        re.sub(r"^(?:and|but|or|also)\s+", "", p).strip() for p in comma_parts
    ]
    restart_parts = [
        re.sub(r"^(?:and|but|or|also)\s+", "", p).strip()
        for p in _split_at_question_restart(question)
    ]
    return _unique_ordered(
        [part for part in comma_parts + restart_parts if len(part.split()) >= 2]
    )


def _content_tokens(text):
    """Extract lightweight content tokens for overlap checks."""
    return {
        token
        for token in re.findall(r"[a-z0-9-]+", text.lower())
        if len(token) >= 3 and token not in _SCENARIO_STOPWORDS
    }


def _extract_deadline(question):
    """Return a compact deadline/time-pressure phrase when present."""
    patterns = (
        r"\bin\s+(\d+\s+(?:minutes?|hours?|days?|weeks?))\b",
        r"\b(\d+\s+(?:minutes?|hours?|days?|weeks?)\s+walk)\b",
        r"\b(before dark)\b",
        r"\b(tonight|tomorrow|today)\b",
        r"\bstarts in\s+(\d+\s+(?:hours?|days?|weeks?))\b",
    )
    lower = question.lower()
    for pattern in patterns:
        match = re.search(pattern, lower)
        if match:
            return match.group(1)
    return None


def _extract_assets(clauses):
    """Return user-stated assets/resources for the current turn."""
    assets = []
    for clause in clauses:
        lower = clause.lower()
        cleaned = clause.strip(" .")
        if re.match(r"^(?:we|i)\s+(?:have|got)\b", lower):
            asset = re.sub(
                r"^(?:we|i)\s+(?:have|got)\s+", "", cleaned, flags=re.IGNORECASE
            )
            candidate = asset or cleaned
            if not _is_generic_asset_placeholder(candidate):
                assets.append(candidate)
            continue
        if _looks_like_inventory_fragment(clause):
            if not _is_generic_asset_placeholder(cleaned):
                assets.append(cleaned)
            continue
        match = re.search(r"\bwith\s+([^,.]+)", clause, flags=re.IGNORECASE)
        if match:
            candidate = match.group(1).strip()
            if not _is_generic_asset_placeholder(candidate):
                assets.append(candidate)
    return _unique_ordered(assets)


def _extract_constraints(clauses, deadline):
    """Return user-stated constraints or limiting factors."""
    constraints = []
    for clause in clauses:
        lower = clause.lower()
        if any(marker in lower for marker in _CONSTRAINT_MARKERS):
            constraints.append(clause.strip(" ."))
    if deadline:
        constraints.append(f"time pressure: {deadline}")
    return _unique_ordered(constraints)


def _extract_hazards(question):
    """Return salient hazards called out by the user."""
    lower = question.lower()
    hazards = [label for marker, label in _HAZARD_MARKERS.items() if marker in lower]
    return _unique_ordered(hazards)


def _extract_people(question):
    """Return people/patient descriptors from the current turn."""
    people = []
    lower = question.lower()
    for pattern in _PEOPLE_PATTERNS:
        for match in re.finditer(pattern, lower):
            people.append(match.group(0))
    return _unique_ordered(people)


def _extract_environment(question):
    """Return environment/context signals from the question."""
    lower = question.lower()
    return _unique_ordered(
        [marker for marker in _ENVIRONMENT_MARKERS if marker in lower]
    )


def _derive_objectives(question, clauses):
    """Return objective clauses for coverage tracking and review."""
    objectives = []
    for clause in clauses:
        if _looks_like_inventory_fragment(clause):
            continue
        if _is_query_bearing(clause) or _detect_domains(clause):
            objective_domains = sorted(_detect_domains(clause))
            objectives.append(
                {
                    "text": clause.strip(),
                    "domains": objective_domains,
                    "tokens": sorted(_content_tokens(clause)),
                }
            )
    if not objectives:
        if _looks_like_inventory_fragment(question):
            return []
        objectives.append(
            {
                "text": question.strip(),
                "domains": sorted(_detect_domains(question)),
                "tokens": sorted(_content_tokens(question)),
            }
        )

    question_domains = sorted(_detect_domains(question))
    for objective in objectives:
        if not objective["domains"]:
            objective["domains"] = question_domains
    return objectives


def build_scenario_frame(question):
    """Parse user-facing scenario structure without changing decomposition."""
    clauses = _split_scenario_clauses(question)
    deadline = _extract_deadline(question)
    frame = {
        "question": question,
        "clauses": clauses,
        "domains": sorted(_detect_domains(question)),
        "objectives": _derive_objectives(question, clauses),
        "assets": _extract_assets(clauses),
        "constraints": _extract_constraints(clauses, deadline),
        "hazards": _extract_hazards(question),
        "people": _extract_people(question),
        "environment": _extract_environment(question),
        "deadline": deadline,
    }
    frame["safety_critical"] = _scenario_frame_is_safety_critical(frame)
    return frame


def empty_session_state():
    """Return the default structured session state."""
    return {
        "assets": [],
        "constraints": [],
        "hazards": [],
        "people": [],
        "environment": [],
        "deadline": None,
        "active_objectives": [],
        "anchor_guide_id": "",
        "anchor_turn_index": None,
        "turn_count": 0,
    }


def _copy_session_state(session_state):
    """Return a detached copy of the structured session state."""
    state = empty_session_state()
    if not session_state:
        return state
    for key in state:
        value = session_state.get(key)
        if isinstance(state[key], list):
            state[key] = list(value or [])
        else:
            state[key] = value
    return state


def merge_frame_with_session(frame, session_state):
    """Merge current-turn structure onto prior session context for prompting/review."""
    merged = dict(frame)
    state = _copy_session_state(session_state)
    for key in ("assets", "constraints", "hazards", "people", "environment"):
        merged[key] = _unique_ordered(state.get(key, []) + frame.get(key, []))
    merged["deadline"] = frame.get("deadline") or state.get("deadline")
    merged["session_active_objectives"] = list(state.get("active_objectives", []))
    return merged


def update_session_state(session_state, frame):
    """Persist user-provided scenario facts into structured session state."""
    state = _copy_session_state(session_state)
    frame = frame or {}
    for key in ("assets", "constraints", "hazards", "people", "environment"):
        state[key] = _unique_ordered(state[key] + frame.get(key, []))[:12]
    if frame.get("deadline"):
        state["deadline"] = frame["deadline"]
    if frame.get("objectives"):
        state["active_objectives"] = _unique_ordered(
            [obj["text"] for obj in frame["objectives"]] + state["active_objectives"]
        )[:8]
    return state


def _session_state_is_empty(session_state):
    """Return True when the structured session state has no stored facts."""
    state = session_state or {}
    return not any(
        state.get(key)
        for key in (
            "assets",
            "constraints",
            "hazards",
            "people",
            "environment",
            "active_objectives",
        )
    ) and not state.get("deadline")


def _render_session_state_text(session_state):
    """Render session state as compact text for prompts/debug."""
    if _session_state_is_empty(session_state):
        return ""

    state = session_state or {}
    parts = []
    if state.get("active_objectives"):
        parts.append("active objectives: " + "; ".join(state["active_objectives"][:4]))
    if state.get("people"):
        parts.append("people: " + ", ".join(state["people"][:4]))
    if state.get("assets"):
        parts.append("assets: " + ", ".join(state["assets"][:4]))
    if state.get("constraints"):
        parts.append("constraints: " + "; ".join(state["constraints"][:4]))
    if state.get("hazards"):
        parts.append("hazards: " + ", ".join(state["hazards"][:4]))
    if state.get("deadline"):
        parts.append("deadline: " + state["deadline"])
    return " | ".join(parts)


def _should_use_session_context(question, frame, session_state):
    """Return True when retrieval should widen a vague follow-up using session state."""
    if _session_state_is_empty(session_state):
        return False
    lower = question.lower().strip()
    if any(lower.startswith(marker) for marker in _ANCHOR_RESET_MARKERS):
        return False
    if any(hint in lower for hint in _SESSION_CONTEXT_HINTS):
        return True
    return len(_content_tokens(question)) <= 4 and not frame.get("domains")


def _anchor_decay(turns_since_anchor):
    """Return the decay multiplier for the thread-anchor prior."""
    return {0: 1.0, 1: 0.6, 2: 0.3}.get(turns_since_anchor, 0.0)


def _is_anchor_reset_query(question):
    """Return True when the user explicitly resets the active topic thread."""
    lower = (question or "").strip().lower()
    return any(lower.startswith(marker) for marker in _ANCHOR_RESET_MARKERS)


def _is_short_deictic_follow_up(question):
    """Return True for short follow-ups that refer back to the current thread."""
    tokens = re.findall(r"[a-z0-9-]+", (question or "").lower())
    return 0 < len(tokens) <= 10 and any(
        token in _ANCHOR_DEICTIC_TOKENS for token in tokens
    )


def _is_anchor_follow_up(question):
    """Return True when the query should inherit the previous thread anchor."""
    lower = (question or "").strip().lower()
    if not lower or _is_anchor_reset_query(lower):
        return False
    if any(hint in lower for hint in _ANCHOR_FOLLOW_UP_HINTS):
        return True
    return _is_short_deictic_follow_up(lower)


def _anchor_prior_context(question, session_state):
    """Return stored anchor metadata for this follow-up, or None."""
    if not ENABLE_ANCHOR_PRIOR or not session_state or not _is_anchor_follow_up(question):
        return None

    anchor_guide_id = str(session_state.get("anchor_guide_id") or "").strip()
    anchor_turn_index = session_state.get("anchor_turn_index")
    turn_count = session_state.get("turn_count")
    if not anchor_guide_id or anchor_turn_index is None:
        return None

    try:
        turns_since_anchor = max(0, int(turn_count or 0) - 1 - int(anchor_turn_index))
    except (TypeError, ValueError):
        return None

    decay = _anchor_decay(turns_since_anchor)
    if decay <= 0.0:
        return None

    return {
        "anchor_guide_id": anchor_guide_id,
        "decay": decay,
        "turn": int(turn_count or 0),
    }


def _apply_anchor_prior(seen, question, session_state):
    """Apply the configured thread-anchor prior to fused RRF scores."""
    context = _anchor_prior_context(question, session_state)
    if not context:
        return

    anchor_guide_id = context["anchor_guide_id"]
    related_weights = get_anchor_related_link_weights(anchor_guide_id)
    for entry in seen.values():
        guide_id = str(entry["meta"].get("guide_id") or "").strip()
        if not guide_id:
            continue
        if guide_id == anchor_guide_id:
            weight = 1.0
        else:
            weight = related_weights.get(guide_id, 0.0)
        if weight <= 0.0:
            continue

        bonus = min(
            ANCHOR_PRIOR_MAX_BONUS,
            max(0.0, ANCHOR_BASE_BONUS * context["decay"] * weight),
        )
        if bonus <= 0.0:
            continue

        entry["rrf_score"] += bonus
        logger.debug(
            "anchor_prior turn=%s anchor_gid=%s chunk_gid=%s base=%.2f decay=%.2f weight=%.2f bonus=%.3f",
            context["turn"],
            anchor_guide_id,
            guide_id,
            ANCHOR_BASE_BONUS,
            context["decay"],
            weight,
            bonus,
        )


def _primary_result_guide_id(results):
    """Return the top-ranked guide id from the current result bundle."""
    metadatas = (results or {}).get("metadatas", [[]])
    if not metadatas or not metadatas[0]:
        return ""
    return str(metadatas[0][0].get("guide_id") or "").strip()


def _record_anchor_turn(session_state, question, anchor_guide_id):
    """Persist the latest answered turn for future anchor-prior checks."""
    state = _copy_session_state(session_state)
    turn_index = int(state.get("turn_count") or 0)
    state["turn_count"] = turn_index + 1
    if _is_anchor_reset_query(question):
        state["anchor_guide_id"] = ""
        state["anchor_turn_index"] = None
    cleaned_anchor = str(anchor_guide_id or "").strip()
    if cleaned_anchor:
        state["anchor_guide_id"] = cleaned_anchor
        state["anchor_turn_index"] = turn_index
    return state


def _format_objective_brief(objective):
    """Return a compact label for an objective."""
    return objective.get("text", "").strip() or "unnamed objective"


def _matching_domain_keywords(question, domain):
    """Return matched keywords for one domain in stable, useful order."""
    lower = question.lower()
    hits = []
    for kw in sorted(
        _DOMAIN_KEYWORDS[domain],
        key=lambda value: (-len(_content_tokens(value)), -len(value), value),
    ):
        if " " in kw:
            if kw in lower:
                hits.append(kw)
        elif re.search(r"\b" + re.escape(kw) + r"\b", lower):
            hits.append(kw)
    return _unique_ordered(hits)


def _focus_clause_tokens(hits):
    """Return normalized content tokens for one focus clause."""
    tokens = set()
    for hit in hits:
        tokens.update(_content_tokens(hit))
    return tokens


def _lexical_overlap_ratio(left_text, right_text):
    """Return the lexical overlap ratio between two texts."""
    left_tokens = _content_tokens(left_text)
    right_tokens = _content_tokens(right_text)
    if not left_tokens or not right_tokens:
        return 0.0
    overlap = len(left_tokens & right_tokens)
    return overlap / max(1, min(len(left_tokens), len(right_tokens)))


def _dedupe_domain_focus_sub_queries(question, domains):
    """Return stable domain-focus sub-queries with covered clauses removed."""
    candidates = []
    for domain in sorted(domains):
        hits = _matching_domain_keywords(question, domain)
        if not hits:
            continue
        focus_tokens = _focus_clause_tokens(hits)
        if not focus_tokens:
            continue
        candidates.append(
            {
                "domain": domain,
                "hits": hits,
                "focus_tokens": focus_tokens,
            }
        )

    if not candidates:
        return []

    candidates.sort(
        key=lambda item: (
            -len(item["focus_tokens"]),
            -len(item["hits"]),
            item["domain"],
        )
    )

    kept = []
    covered_tokens = set()
    for candidate in candidates:
        if candidate["focus_tokens"] <= covered_tokens:
            continue
        kept.append(candidate)
        covered_tokens.update(candidate["focus_tokens"])

    filtered = []
    filtered_texts = []
    for candidate in kept:
        text = f"{question} (focus: {', '.join(candidate['hits'])})"
        if any(_lexical_overlap_ratio(text, existing) >= 0.80 for existing in filtered_texts):
            continue
        filtered.append(candidate)
        filtered_texts.append(text)

    filtered.sort(key=lambda item: item["domain"])
    return [f"{question} (focus: {', '.join(item['hits'])})" for item in filtered]


def decompose_query(question):
    """Break compound queries into sub-queries for multi-axis retrieval.

    Uses three strategies in order:
    1. Comma splitting (explicit clause boundaries, query-bearing filter)
    2. Domain detection (medical + shelter + water = 3 sub-queries)
    3. Question-word restart detection ("monsoon... what do we need")
    """
    if _is_mental_health_crisis_query(question):
        return [question]

    # Strategy 1: Comma splitting with query-bearing filter
    parts = [p.strip() for p in question.split(",")]
    parts = [re.sub(r"^(?:and|but|or|also)\s+", "", p).strip() for p in parts]
    parts = [p for p in parts if len(p.split()) >= 2 and _is_query_bearing(p)]
    if len(parts) > 1:
        return [question] + parts

    # Strategy 2: Domain detection - if query spans 2+ survival domains,
    # generate a focused sub-query for each matched domain
    domains = _detect_domains(question)
    if len(domains) >= 2:
        sub_queries = _dedupe_domain_focus_sub_queries(question, domains)
        if sub_queries:
            return [question] + sub_queries

    # Strategy 3: Question-word restart detection
    clause_splits = [
        clause
        for clause in _split_at_question_restart(question)
        if _has_meaningful_restart_content(clause)
    ]
    if len(clause_splits) >= 2:
        return [question] + clause_splits

    return [question]


def merge_results(result_sets, top_k, *, question="", session_state=None):
    """Merge vector/lexical result sets using reciprocal-rank fusion."""
    seen = {}
    for results in result_sets:
        retrieval_kind = results.get("_retrieval_kind", "vector")
        for id_, doc, meta, dist in zip(
            results["ids"][0],
            results["documents"][0],
            results["metadatas"][0],
            results["distances"][0],
        ):
            if id_ not in seen:
                seen[id_] = {
                    "doc": doc,
                    "meta": dict(meta),
                    "best_distance": dist,
                    "rrf_score": 0.0,
                    "vector_hits": 0,
                    "lexical_hits": 0,
                }
            entry = seen[id_]
            rank = results["ids"][0].index(id_)
            entry["rrf_score"] += 1.0 / (HYBRID_RRF_K + rank + 1)
            if retrieval_kind == "lexical":
                entry["lexical_hits"] += 1
            else:
                entry["vector_hits"] += 1
            if dist < entry["best_distance"]:
                entry["best_distance"] = dist
                entry["doc"] = doc
                entry["meta"] = dict(meta)

    _apply_anchor_prior(seen, question, session_state)

    sorted_items = sorted(
        seen.items(),
        key=lambda item: (-item[1]["rrf_score"], item[1]["best_distance"]),
    )[:top_k]

    return {
        "ids": [[id_ for id_, _ in sorted_items]],
        "documents": [[item["doc"] for _, item in sorted_items]],
        "metadatas": [
            [
                {
                    **item["meta"],
                    "_rrf_score": round(item["rrf_score"], 6),
                    "_vector_hits": item["vector_hits"],
                    "_lexical_hits": item["lexical_hits"],
                }
                for _, item in sorted_items
            ]
        ],
        "distances": [[item["best_distance"] for _, item in sorted_items]],
    }


def _objective_category_targets(objective):
    """Return likely category targets for an objective."""
    targets = set()
    for domain in objective.get("domains", []):
        targets.update(_DOMAIN_CATEGORY_TARGETS.get(domain, {domain}))
    return targets


def _objective_match_score(objective, doc, meta):
    """Return a lightweight relevance score between an objective and a result."""
    score = 0
    category = meta.get("category", "")
    meta_text = " ".join(
        [
            meta.get("guide_title", ""),
            meta.get("section_heading", ""),
            meta.get("slug", ""),
            meta.get("description", ""),
            meta.get("tags", ""),
            category,
        ]
    ).lower()
    doc_text = doc.lower()

    if category in _objective_category_targets(objective):
        score += 3

    overlap = set(objective.get("tokens", [])) & (
        _content_tokens(meta_text) | _content_tokens(doc_text)
    )
    score += min(len(overlap), 3)

    text = objective.get("text", "").lower()
    if text and (text in meta_text or text in doc_text):
        score += 2

    return score


def _support_signal_from_score(score):
    """Map a numeric objective-match score to a review-friendly label."""
    if score >= 5:
        return "direct"
    if score >= 3:
        return "related"
    if score >= 1:
        return "weak"
    return "peripheral"


def _annotate_row(question, objective_rows, doc, meta, dist):
    """Annotate a retrieved row for reranking, coverage, and prompt labeling."""
    objective_scores = {}
    matched_objectives = []
    for index, objective in enumerate(objective_rows):
        score = _objective_match_score(objective, doc, meta)
        objective_scores[index] = score
        if score >= 3:
            matched_objectives.append(_format_objective_brief(objective))

    max_score = max(objective_scores.values(), default=0)
    retrieval_bonus = min(
        float(meta.get("_rrf_score", 0.0)) * 1.8, HYBRID_RRF_MAX_BONUS
    )
    if meta.get("_lexical_hits") and not meta.get("_vector_hits"):
        retrieval_bonus += HYBRID_LEXICAL_ONLY_BONUS
    adjusted = (
        dist
        + _metadata_rerank_delta(question, meta)
        - min(max_score * 0.01, 0.05)
        - retrieval_bonus
    )
    family = meta.get("guide_id", "") or meta.get("source_file", "")
    section_key = (
        meta.get("guide_title", ""),
        meta.get("section_heading", ""),
    )

    return {
        "adjusted": adjusted,
        "distance": dist,
        "doc": doc,
        "id": meta.get("id"),
        "meta": meta,
        "objective_scores": objective_scores,
        "matched_objectives": matched_objectives,
        "support_signal": _support_signal_from_score(max_score),
        "family": family,
        "section_key": section_key,
    }


def _finalize_objective_coverage(rows, objectives):
    """Return objective coverage labels using the final reranked results."""
    coverage = []
    for index, objective in enumerate(objectives):
        best_score = 0
        best_distance = None
        best_row = None
        for row in rows:
            score = row["objective_scores"].get(index, 0)
            if score > best_score or (
                score == best_score
                and (best_distance is None or row["distance"] < best_distance)
            ):
                best_score = score
                best_distance = row["distance"]
                best_row = row

        if best_score >= 5 and best_distance is not None and best_distance < 1.0:
            status = "covered"
        elif best_score >= 3:
            status = "weak"
        else:
            status = "missing"

        coverage.append(
            {
                "objective": _format_objective_brief(objective),
                "status": status,
                "best_score": best_score,
                "best_distance": None
                if best_distance is None
                else round(best_distance, 4),
                "best_source": (
                    f"{best_row['meta'].get('guide_title', '')} -> {best_row['meta'].get('section_heading', '')}"
                    if best_row
                    else None
                ),
            }
        )
    return coverage


def _build_rerank_timing_payload(elapsed_seconds, chunk_count, top_k, selected_count):
    """Summarize rerank latency for debug and bench review metadata."""
    total_ms = max(float(elapsed_seconds), 0.0) * 1000.0
    chunk_count = max(int(chunk_count or 0), 0)
    return {
        "top_k": max(int(top_k or 0), 0),
        "chunk_count": chunk_count,
        "selected_count": max(int(selected_count or 0), 0),
        "total_ms": round(total_ms, 3),
        "avg_ms_per_chunk": round(total_ms / chunk_count, 3) if chunk_count else 0.0,
    }


def rerank_results(question, results, top_k, scenario_frame=None):
    """Reorder retrieved chunks using metadata-aware soft penalties/bonuses."""
    rerank_started_at = time.perf_counter()
    debug_enabled = logger.isEnabledFor(logging.DEBUG)
    objectives = (scenario_frame or {}).get("objectives", [])
    exclude_gd_918_for_mania_psychosis = _is_mania_or_psychosis_like_query(question)
    rows = []
    for id_, doc, meta, dist in zip(
        results["ids"][0],
        results["documents"][0],
        results["metadatas"][0],
        results["distances"][0],
    ):
        if exclude_gd_918_for_mania_psychosis and meta.get("guide_id") == "GD-918":
            continue
        row = _annotate_row(question, objectives, doc, {**meta, "id": id_}, dist)
        rows.append(row)

    rows.sort(key=lambda row: (row["adjusted"], row["distance"]))

    selected = []
    selected_ids = set()
    family_counts = Counter()
    section_counts = Counter()
    preserved_objectives = []
    for index, objective in enumerate(objectives):
        candidates = [
            row
            for row in rows
            if row["objective_scores"].get(index, 0) >= 5
            and row["id"] not in selected_ids
        ]
        if not candidates:
            continue
        chosen = candidates[0]
        selected.append(chosen)
        selected_ids.add(chosen["id"])
        family_counts[chosen["family"]] += 1
        section_counts[chosen["section_key"]] += 1
        preserved_objectives.append(_format_objective_brief(objective))

    overflow_rows = []
    for row in rows:
        if row["id"] in selected_ids:
            continue
        if section_counts[row["section_key"]] >= _RERANK_SECTION_SOFT_CAP or (
            row["family"] and family_counts[row["family"]] >= _RERANK_FAMILY_SOFT_CAP
        ):
            overflow_rows.append(row)
            continue
        selected.append(row)
        selected_ids.add(row["id"])
        family_counts[row["family"]] += 1
        section_counts[row["section_key"]] += 1
        if len(selected) >= top_k:
            break

    for row in overflow_rows:
        if len(selected) >= top_k:
            break
        if row["id"] in selected_ids:
            continue
        selected.append(row)
        selected_ids.add(row["id"])

    selected = selected[:top_k]
    selected.sort(key=lambda row: (row["adjusted"], row["distance"]))
    coverage = _finalize_objective_coverage(selected, objectives)
    rerank_timing = _build_rerank_timing_payload(
        time.perf_counter() - rerank_started_at,
        len(rows),
        top_k,
        len(selected),
    )
    if debug_enabled:
        logger.debug(
            "rerank_timing top_k=%s chunks=%s selected=%s total_ms=%.3f avg_ms_per_chunk=%.3f",
            rerank_timing["top_k"],
            rerank_timing["chunk_count"],
            rerank_timing["selected_count"],
            rerank_timing["total_ms"],
            rerank_timing["avg_ms_per_chunk"],
        )
    annotations = [
        {
            "support_signal": row["support_signal"],
            "matched_objectives": row["matched_objectives"],
            "family": row["family"],
        }
        for row in selected
    ]

    return {
        "ids": [[row["id"] for row in selected]],
        "documents": [[row["doc"] for row in selected]],
        "metadatas": [
            [{k: v for k, v in row["meta"].items() if k != "id"} for row in selected]
        ],
        "distances": [[row["distance"] for row in selected]],
        "_senku_review": {
            "objective_coverage": coverage,
            "result_annotations": annotations,
            "preserved_objectives": preserved_objectives,
            "rerank_timing": rerank_timing,
        },
    }


def _retrieval_candidate_limit(top_k):
    """Return the initial candidate pool size before reranking."""
    return min(
        max(top_k * _RETRIEVAL_CANDIDATE_MULTIPLIER, top_k), _RETRIEVAL_CANDIDATE_CAP
    )


def _append_retrieval_spec(specs, seen, *, text, category, limit):
    """Add a retrieval spec unless the text/category pair is already present."""
    key = (text, category or "")
    if key in seen:
        return
    seen.add(key)
    specs.append(
        {
            "text": text,
            "category": category,
            "limit": limit,
        }
    )


def _body_part_hint(question_lower):
    """Return a simple body-part hint for targeted medical retrieval, if present."""
    for marker in _BODY_PART_MARKERS:
        if re.search(r"\b" + re.escape(marker) + r"\b", question_lower):
            return marker
    return "skin"


def _looks_like_multi_person_triage(question, frame):
    """Return True when the prompt appears to involve multiple competing patients."""
    lower = question.lower()
    return any(marker in lower for marker in _TRIAGE_MARKERS) or (
        "medical" in frame.get("domains", []) and len(frame.get("people", [])) >= 2
    )


def _supplemental_retrieval_specs(
    question, top_k, category=None, scenario_frame=None, session_state=None
):
    """Return extra retrieval lanes for queries that benefit from category hints."""
    if category:
        return []

    candidate_limit = _retrieval_candidate_limit(top_k)
    supplemental_limit = min(candidate_limit, max(top_k, 24))
    question_lower = question.lower()
    frame = scenario_frame or build_scenario_frame(question)
    specs = []
    gi_bleed_markers = (
        "coffee grounds",
        "coffee ground vomit",
        "black tarry stool",
        "black tarry stools",
        "black stool",
        "black stools",
        "bright red vomit",
        "vomit blood",
        "vomiting blood",
        "dark clots",
    )

    if any(marker in question_lower for marker in gi_bleed_markers):
        specs.extend(
            [
                {
                    "text": (
                        "upper gi bleed hematemesis melena coffee ground vomit "
                        "bright red blood dark clots black tarry stool emergency "
                        "shock airway urgent hospital escalation"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} possible gastrointestinal bleeding bleed-first "
                        "triage airway active bleeding shock urgent escalation "
                        "before hydration or food poisoning advice"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_mental_health_crisis_query(question):
        mania_or_psychosis_like = _is_mania_or_psychosis_like_query(question)
        if mania_or_psychosis_like:
            specs.extend(
                [
                    {
                        "text": "acute mania psychosis emergency close supervision do not leave them alone remove keys weapons medications urgent same day mental health evaluation no sleep reckless grandiose invincible paranoid hallucinations mission-driven behavior unsafe nighttime wandering",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "observer reports no sleep barely slept pacing all night talking nonstop will not stop moving acting invincible special mission reckless spending unsafe driving psychosis mania emergency supervision means restriction",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": f"{question} urgent supervision do not leave them alone remove keys emergency mental health mania psychosis",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "recognizing mental health crises urgent supervision psychosis mania paranoia hallucinations emergency help",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                ]
            )
        else:
            specs.extend(
                [
                    {
                        "text": question,
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "mental health crisis psychosis paranoia hallucinations mania no sleep nonstop pacing talking nonstop mission-driven behavior acting invincible unsafe nighttime wandering will not stop moving not eating urgent supervision escalation",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "acute mania psychosis no sleep barely slept racing thoughts agitated reckless grandiose unsafe driving pressured speech nonstop pacing sudden mission-driven behavior remove keys close supervision containment emergency mental health nothing can hurt him",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "recognizing mental health crises urgent supervision psychosis mania paranoia hallucinations emergency help",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                ]
            )

        if any(
            marker in question_lower
            for marker in ("withdrawal", "stopped drinking", "alcohol")
        ):
            specs.extend(
                [
                    {
                        "text": "alcohol withdrawal shaking agitation delirium tremens when to seek emergency help",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "addiction withdrawal management alcohol withdrawal tremor agitation hallucinations seizure risk",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                ]
            )

        if any(
            marker in question_lower
            for marker in (
                "died",
                "death",
                "grief",
                "won't eat",
                "wont eat",
                "won't get out of bed",
                "wont get out of bed",
            )
        ):
            specs.append(
                {
                    "text": "grief shutdown not eating not getting out of bed when to seek urgent help",
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )

    if _is_human_medical_query(question):
        if not _is_mental_health_crisis_query(question):
            specs.append(
                {
                    "text": question,
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )

        if _is_acute_symptom_query(question):
            specs.extend(
                [
                    {
                        "text": "medical emergency red flags first aid triage evacuation",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "common ailments recognition care red flags emergency symptoms",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "first aid red flags seek emergency help",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "acute symptom warning signs when to seek help medical",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                ]
            )

        if _is_acute_overlap_collapse_query(question):
            specs.append(
                {
                    "text": "collapse unresponsive chest pain face droop slurred speech one-sided weakness first aid cardiac emergency cpr stroke",
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )

        if _is_noncollapse_stroke_cardiac_overlap_query(question):
            specs.append(
                {
                    "text": "stroke tia transient ischemic attack sudden neurologic deficit face droop slurred speech one-sided weakness one-sided numbness chest pain chest pressure shortness of breath first aid cardiac emergency cpr",
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )

        if _is_household_chemical_hazard_query(question):
            specs.extend(
                [
                    {
                        "text": "household chemical poisoning poison control bleach ammonia corrosive exposure battery acid",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "chemical burn corrosive skin eye exposure first aid flush water poison control",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "toxicology poisoning response household cleaners inhalation ingestion decontamination",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "battery acid sulfuric acid eye skin exposure poison control first aid",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "cleaning chemical mixing warning bleach ammonia acids lye",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                ]
            )

        if _is_urinary_query(question_lower):
            specs.extend(
                [
                    {
                        "text": "urinary tract infection urinary frequency urgency burning urination bladder pressure home care",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "frequent urination when to worry urinary urgency bladder symptoms",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                ]
            )

        if _text_has_marker(question_lower, _SPLINTER_QUERY_MARKERS):
            body_part = _body_part_hint(question_lower)
            specs.extend(
                [
                    {
                        "text": f"retained metal in {body_part} wound first aid",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "remove splinter from skin",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                ]
            )

        if _text_has_marker(question_lower, _ANIMAL_BITE_QUERY_MARKERS):
            specs.append(
                {
                    "text": "animal bite wound rabies first aid",
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )

        if _is_generic_puncture_query(question_lower):
            specs.extend(
                [
                    {
                        "text": "puncture wound first aid do not probe keep open infection prevention",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "puncture wound tetanus risk and infection monitoring",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "first aid wound management puncture wound cleaning irrigation dressing",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "wound hygiene infection prevention field sanitation puncture wound",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                ]
            )

    if _is_household_chemical_eye_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "chemical eye burn bleach cleaner eye splash irrigation "
                        "ophthalmology ocular exposure flush water emergency"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} chemical eye burn ocular exposure "
                        "irrigation ophthalmology emergency"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_household_chemical_inhalation_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "solvent fumes paint thinner mineral spirits turpentine "
                        "acetone inhalation toxicology poison control fresh air "
                        "decontamination"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        "mixed cleaners bleach ammonia chlorine gas chemical "
                        "inhalation chest tightness coughing poison control "
                        "fresh air"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _text_has_marker(question_lower, _VEHICLE_QUERY_MARKERS):
        specs.append(
            {
                "text": question,
                "category": "transportation",
                "limit": supplemental_limit,
            }
        )

        if _text_has_marker(question_lower, _FLAT_TIRE_QUERY_MARKERS):
            specs.append(
                {
                    "text": "tire plug temporary vehicle repair",
                    "category": "transportation",
                    "limit": supplemental_limit,
                }
            )

        if _text_has_marker(question_lower, _BATTERY_QUERY_MARKERS):
            specs.extend(
                [
                    {
                        "text": question,
                        "category": "salvage",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": question,
                        "category": "power-generation",
                        "limit": supplemental_limit,
                    },
                ]
            )

    if (
        "shelter" in frame.get("domains", [])
        and frame.get("deadline")
        and any(marker in question_lower for marker in _WEATHER_MARKERS)
    ):
        specs.extend(
            [
                {
                    "text": "rapid emergency shelter for storm cold rain before nightfall",
                    "category": "building",
                    "limit": supplemental_limit,
                },
                {
                    "text": "immediate shelter priorities for exposure and weather",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
            ]
        )

    if "water" in frame.get("domains", []) and (
        _text_has_marker(question_lower, _CONTAINER_MARKERS)
        or any(
            _text_has_marker(asset, _CONTAINER_MARKERS)
            for asset in frame.get("assets", [])
        )
    ):
        specs.extend(
            [
                {
                    "text": "collect and store safe water with buckets drums containers",
                    "category": "utility",
                    "limit": supplemental_limit,
                },
                {
                    "text": "water storage sanitation with improvised containers",
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_water_purification_query(question_lower):
        specs.extend(
            [
                {
                    "text": "water purification filtration systems boiling chemical treatment safe drinking water",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "charcoal sand water filter prefilter water purification disinfection safe storage",
                    "category": "utility",
                    "limit": supplemental_limit,
                },
                {
                    "text": "biosand filter construction activated charcoal coarse sand gravel safe drinking water",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "sanitation public health contaminated water disease prevention disinfection",
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": "simple water treatment methods water system design distribution source quality",
                    "category": "building",
                    "limit": supplemental_limit,
                },
                {
                    "text": "chlorine bleach production water disinfection sanitation",
                    "category": "chemistry",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_water_storage_query(question_lower):
        specs.extend(
            [
                {
                    "text": "safe water storage container sanitation reused bottles drums buckets jugs",
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
                {
                    "text": "water storage systems rationing protocol clean container seal rotate",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "water storage container selection preparation food grade clean container seal",
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
                {
                    "text": "sanitation public health water container cleaning contamination prevention",
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": "water system design distribution clean container storage rotation",
                    "category": "building",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _text_has_marker(question_lower, _EVACUATE_MARKERS) and _text_has_marker(
        question_lower, _SHELTER_DECISION_MARKERS
    ):
        specs.extend(
            [
                {
                    "text": "evacuate versus shelter in place decision under disaster constraints",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "group movement versus staying put risk tradeoffs",
                    "category": "society",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _looks_like_multi_person_triage(question, frame):
        specs.append(
            {
                "text": "multiple patient disaster triage priorities field care",
                "category": "medical",
                "limit": supplemental_limit,
            }
        )

    if _is_supply_conflict_query(question_lower):
        specs.extend(
            [
                {
                    "text": "de-escalation and fair process for group conflict over supplies",
                    "category": "society",
                    "limit": supplemental_limit,
                },
                {
                    "text": "transparent rationing and rotating responsibilities to reduce supply disputes",
                    "category": "society",
                    "limit": supplemental_limit,
                },
                {
                    "text": "mediate resource dispute avoid violence cooling-off protocol",
                    "category": "society",
                    "limit": supplemental_limit,
                },
                {
                    "text": "shared custody public ledger two-person inventory check rotating steward supplies",
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_post_collapse_recovery_query(question_lower):
        specs.extend(
            [
                {
                    "text": "first 72 hours after collapse community stabilization shelter water sanitation",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "post-collapse disease control sanitation food handling isolation",
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": "community organization records education knowledge transfer after collapse",
                    "category": "society",
                    "limit": supplemental_limit,
                },
                {
                    "text": "first season food production and preservation after collapse",
                    "category": "agriculture",
                    "limit": supplemental_limit,
                },
                {
                    "text": "rationing storage and essential supply organization after collapse",
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_house_build_query(question_lower):
        specs.extend(
            [
                {
                    "text": "simple one room house site drainage foundation frame roof weatherproofing",
                    "category": "building",
                    "limit": supplemental_limit,
                },
                {
                    "text": "cabin hut construction floor wall roof insulation ventilation",
                    "category": "building",
                    "limit": supplemental_limit,
                },
                {
                    "text": "shelter construction weatherproofing drainage insulation low resource dwelling",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "primitive shelter site selection drainage rainproofing semi permanent cabin",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "window door assembly weatherstripping timber wall sill threshold",
                    "category": "building",
                    "limit": supplemental_limit,
                },
                {
                    "text": "roof waterproofing foundation drainage sealants low resource structure",
                    "category": "building",
                    "limit": supplemental_limit,
                },
                {
                    "text": "building materials salvage framing lumber roofing windows doors",
                    "category": "building",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_small_watercraft_query(question_lower):
        specs.extend(
            [
                {
                    "text": "dugout canoe construction tree selection hollowing sealing paddle",
                    "category": "transportation",
                    "limit": supplemental_limit,
                },
                {
                    "text": "small watercraft construction plank canoe ribs planking caulking",
                    "category": "transportation",
                    "limit": supplemental_limit,
                },
                {
                    "text": "boat building buoyancy hull stability waterproofing shoreline test",
                    "category": "building",
                    "limit": supplemental_limit,
                },
                {
                    "text": "transportation hollowed log dugout canoe bark canoe paddle",
                    "category": "transportation",
                    "limit": supplemental_limit,
                },
                {
                    "text": "boat caulking pitch resin seams waterproof hull",
                    "category": "building",
                    "limit": supplemental_limit,
                },
                {
                    "text": "tree selection straight trunk cedar pine cottonwood canoe",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_fire_in_rain_special_case(question_lower):
        specs.extend(
            [
                {
                    "text": "fire in wet conditions tinder kindling dry inner wood windbreak",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "fire by friction methods tinder preparation fire bundle char cloth",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "winter survival fire in wet cold conditions feather sticks dry core fuel",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_closed_room_fire_question(question_lower):
        has_stove_heater_chimney_terms = any(
            term in question_lower
            for term in (
                "stove",
                "heater",
                "chimney",
            )
        )
        specs.extend(
            [
                {
                    "text": "closed bedroom fire evacuation smoke inhalation fire suppression",
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": "fire safety compartmentalization enclosed room evacuation bedroom fire",
                    "category": "building",
                    "limit": supplemental_limit,
                },
                {
                    "text": "smoke inhalation carbon monoxide fire gas exposure emergency",
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )
        if has_stove_heater_chimney_terms:
            specs.append(
                {
                    "text": "stove chimney heater fire carbon monoxide venting",
                    "category": "building",
                    "limit": supplemental_limit,
                }
            )

    if _is_bridge_span_query(question_lower):
        specs.extend(
            [
                {
                    "text": "simple 20 foot footbridge abutments decking load test",
                    "category": "transportation",
                    "limit": supplemental_limit,
                },
                {
                    "text": "bridge approaches abutments drainage washout prevention simple span",
                    "category": "transportation",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_new_well_query(question_lower):
        specs.extend(
            [
                {
                    "text": "hand dug well siting casing cover apron contamination prevention",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "well excavation shoring cave-in prevention hand dug well",
                    "category": "building",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_group_role_assignment_query(question_lower):
        specs.extend(
            [
                {
                    "text": "50 person community role assignment essential teams rotation records",
                    "category": "society",
                    "limit": supplemental_limit,
                },
                {
                    "text": "skills census labor assignment oversight for village scale group",
                    "category": "society",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_supply_hiding_query(question_lower):
        specs.extend(
            [
                {
                    "text": "hide supplies supply cache waterproof decoy cache multiple small caches recovery",
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
                {
                    "text": "storage management hidden supply caches waterproof containers decoy cache tradeoffs",
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
                {
                    "text": "cache security concealment casual theft multiple small caches",
                    "category": "defense",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_glassmaking_query(question_lower):
        specs.extend(
            [
                {
                    "text": "glassmaking from scratch silica sand soda ash furnace annealing",
                    "category": "chemistry",
                    "limit": supplemental_limit,
                },
                {
                    "text": "glassmaking from scratch glass furnace forming annealing",
                    "category": "crafts",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_bow_arrow_query(question_lower):
        specs.extend(
            [
                {
                    "text": "self bow construction tillering bowstring arrow shaft fletching",
                    "category": "defense",
                    "limit": supplemental_limit,
                },
                {
                    "text": "bow making arrow making shaft straightening point hafting",
                    "category": "defense",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_no_gear_fishing_query(question_lower):
        specs.extend(
            [
                {
                    "text": "primitive fishing without gear hand fishing fish traps weirs spearing",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "passive fish trap construction weir basket trap without hooks",
                    "category": "agriculture",
                    "limit": supplemental_limit,
                },
                {
                    "text": "fishing methods stealth hand capture primitive no rod no net",
                    "category": "biology",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_natural_dye_query(question_lower):
        specs.extend(
            [
                {
                    "text": "natural dye extraction plant dyes mordant dye bath color fastness",
                    "category": "textiles-fiber-arts",
                    "limit": supplemental_limit,
                },
                {
                    "text": "plant-based dyes mordant alum tannin indigo woad",
                    "category": "crafts",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_weld_without_welder_query(question_lower):
        specs.extend(
            [
                {
                    "text": "forge welding brazing soldering oxy-acetylene no electric welder",
                    "category": "metalworking",
                    "limit": supplemental_limit,
                },
                {
                    "text": "forge welding joining techniques without electricity",
                    "category": "metalworking",
                    "limit": supplemental_limit,
                },
                {
                    "text": "oxy-acetylene brazing soldering joint integrity",
                    "category": "chemistry",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_roman_concrete_query(question_lower):
        specs.extend(
            [
                {
                    "text": "roman concrete pozzolan slaked lime volcanic ash opus caementicium",
                    "category": "building",
                    "limit": supplemental_limit,
                },
                {
                    "text": "ancient construction pozzolanic lime volcanic ash roman concrete",
                    "category": "culture-knowledge",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_smoke_meat_query(question_lower):
        specs.extend(
            [
                {
                    "text": "smoking curing meat cold smoking hot smoking brining jerky",
                    "category": "agriculture",
                    "limit": supplemental_limit,
                },
                {
                    "text": "food preservation smoking drying meat smokehouse hardwood safety",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_raft_lake_query(question_lower):
        specs.extend(
            [
                {
                    "text": "log raft buoyancy lashing paddle calm lake crossing",
                    "category": "transportation",
                    "limit": supplemental_limit,
                },
                {
                    "text": "improvised flotation raft bundle logs lashings shoreline test",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_cast_without_foundry_query(question_lower):
        specs.extend(
            [
                {
                    "text": "sand casting lost-wax simple furnace non-ferrous metal without foundry",
                    "category": "metalworking",
                    "limit": supplemental_limit,
                },
                {
                    "text": "small-scale casting refractory mold crucible forge non-ferrous",
                    "category": "metalworking",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_fair_trial_query(question_lower):
        specs.extend(
            [
                {
                    "text": "community trial procedure lay panel evidence records appeal",
                    "category": "society",
                    "limit": supplemental_limit,
                },
                {
                    "text": "criminal justice procedures for small communities evidence standard lay tribunal",
                    "category": "society",
                    "limit": supplemental_limit,
                },
                {
                    "text": "restorative justice versus adjudication small community due process",
                    "category": "society",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_soapmaking_query(question_lower):
        specs.extend(
            [
                {
                    "text": "soap making from animal fat ash lye saponification curing safety",
                    "category": "crafts",
                    "limit": supplemental_limit,
                },
                {
                    "text": "wood ash lye animal fat soapmaking caustic safety rinse with water",
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_brain_tanning_query(question_lower):
        specs.extend(
            [
                {
                    "text": "brain tanning hide prep brain emulsion smoke finish buckskin",
                    "category": "crafts",
                    "limit": supplemental_limit,
                },
                {
                    "text": "brain tanning soft buckskin hide working drying smoke",
                    "category": "textiles-fiber-arts",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_hot_weather_burial_query(question_lower):
        specs.extend(
            [
                {
                    "text": "post-death care hot weather cooling wrapping identification burial timing",
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": "temporary body preservation before burial hot weather insects odor",
                    "category": "society",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _should_use_session_context(question, frame, session_state):
        session_text = _render_session_state_text(session_state)
        if session_text:
            specs.append(
                {
                    "text": f"{question} context: {session_text}",
                    "category": None,
                    "limit": supplemental_limit,
                }
            )

    return specs


def _lexical_terms(text):
    """Return a compact list of lexical search terms for FTS queries."""
    tokens = []
    seen = set()
    for token in re.findall(r"[a-z0-9-]+", text.lower()):
        if token in seen:
            continue
        if len(token) < 2 and not token.isdigit():
            continue
        if token in _SCENARIO_STOPWORDS:
            continue
        seen.add(token)
        tokens.append(token)
    return tokens[:12]


def _build_lexical_query(text):
    """Convert free text into a permissive FTS query."""
    terms = _lexical_terms(text)
    if not terms:
        return None
    return " OR ".join(f'"{term}"' for term in terms)


def _empty_retrieval_results(kind="vector"):
    """Return an empty retrieval result payload."""
    return {
        "ids": [[]],
        "documents": [[]],
        "metadatas": [[]],
        "distances": [[]],
        "_retrieval_kind": kind,
    }


def _lexical_rank_distance(rank, limit):
    """Map lexical rank to a Chroma-like pseudo-distance for hybrid reranking."""
    if limit <= 1:
        return LEXICAL_DISTANCE_FLOOR
    return LEXICAL_DISTANCE_FLOOR + (rank / max(limit - 1, 1)) * LEXICAL_DISTANCE_RANGE


def query_lexical_index(text, limit, category=None):
    """Run an FTS5 query over the lexical shadow index."""
    if not os.path.exists(config.LEXICAL_DB_PATH):
        return _empty_retrieval_results(kind="lexical")

    lexical_query = _build_lexical_query(text)
    if not lexical_query:
        return _empty_retrieval_results(kind="lexical")

    sql = """
        SELECT
            m.chunk_id,
            m.document,
            m.source_file,
            m.guide_id,
            m.guide_title,
            m.slug,
            m.description,
            m.category,
            m.difficulty,
            m.last_updated,
            m.version,
            m.liability_level,
            m.tags,
            m.related,
            m.section_id,
            m.section_heading,
            bm25(
                lexical_chunks_fts,
                1.0, 2.2, 2.0, 2.4, 2.4, 1.8, 1.6, 0.8, 0.5
            ) AS lexical_score
        FROM lexical_chunks_fts
        JOIN lexical_chunk_meta AS m
            ON m.chunk_id = lexical_chunks_fts.chunk_id
        WHERE lexical_chunks_fts MATCH ?
    """
    params = [lexical_query]
    if category:
        sql += " AND m.category = ?"
        params.append(category)
    sql += " ORDER BY lexical_score LIMIT ?"
    params.append(limit)

    try:
        with sqlite3.connect(config.LEXICAL_DB_PATH) as conn:
            conn.row_factory = sqlite3.Row
            rows = conn.execute(sql, params).fetchall()
    except sqlite3.Error:
        return _empty_retrieval_results(kind="lexical")

    if not rows:
        return _empty_retrieval_results(kind="lexical")

    ids = []
    documents = []
    metadatas = []
    distances = []
    for rank, row in enumerate(rows):
        ids.append(row["chunk_id"])
        documents.append(row["document"])
        metadatas.append(
            {
                "source_file": row["source_file"],
                "guide_id": row["guide_id"],
                "guide_title": row["guide_title"],
                "slug": row["slug"],
                "description": row["description"],
                "category": row["category"],
                "difficulty": row["difficulty"],
                "last_updated": row["last_updated"],
                "version": row["version"],
                "liability_level": row["liability_level"],
                "tags": row["tags"],
                "related": row["related"],
                "section_id": row["section_id"],
                "section_heading": row["section_heading"],
            }
        )
        distances.append(_lexical_rank_distance(rank, limit))

    return {
        "ids": [ids],
        "documents": [documents],
        "metadatas": [metadatas],
        "distances": [distances],
        "_retrieval_kind": "lexical",
    }


def _results_family_diversity(results):
    """Return the count of distinct guide families in a result set."""
    families = {
        meta.get("source_file", "") or meta.get("guide_id", "")
        for meta in results.get("metadatas", [[]])[0]
        if meta
    }
    return len(families)


def _review_metadata(
    results, specs, sub_queries, scenario_frame, current_frame, session_state_used
):
    """Build retrieval metadata for review/debug/bench tooling."""
    review = dict(results.get("_senku_review", {}))
    category_distribution = Counter(
        meta.get("category", "unknown") for meta in results.get("metadatas", [[]])[0]
    )
    annotations = review.get("result_annotations", [])
    support_summary = Counter(
        annotation.get("support_signal", "peripheral") for annotation in annotations
    )
    retrieval_mix = Counter()
    for meta in results.get("metadatas", [[]])[0]:
        if meta.get("_vector_hits") and meta.get("_lexical_hits"):
            retrieval_mix["hybrid"] += 1
        elif meta.get("_lexical_hits"):
            retrieval_mix["lexical_only"] += 1
        else:
            retrieval_mix["vector_only"] += 1

    return {
        "sub_queries": sub_queries,
        "scenario_frame": scenario_frame,
        "current_frame": current_frame,
        "session_state_used": session_state_used,
        "objective_coverage": review.get("objective_coverage", []),
        "preserved_objectives": review.get("preserved_objectives", []),
        "result_annotations": annotations,
        "rerank_timing": review.get("rerank_timing", {}),
        "category_distribution": dict(category_distribution),
        "guide_family_diversity": _results_family_diversity(results),
        "retrieval_specs": specs,
        "support_signals": dict(support_summary),
        "retrieval_mix": dict(retrieval_mix),
    }


def _normalize_confidence_label(label) -> ConfidenceLabel:
    """Collapse the wider presentation contract to the Wave B transport enum."""
    normalized = resolve_confidence_presentation(label).label
    if normalized == "medium":
        return "medium"
    if normalized in {"low", "uncertain-fit", "abstain"}:
        return "low"
    return "high"


def build_confidence_system_instruction(label) -> str:
    """Return the prompt instruction that mirrors the Wave B confidence label."""
    if label is None:
        return ""
    normalized = _normalize_confidence_label(label)
    instruction = f"The answer confidence is {normalized}."
    if normalized == "low":
        instruction += " If confidence is low, note the gap in the first sentence."
    return instruction


def _confidence_label(reranked, scenario_frame) -> ConfidenceLabel:
    """Classify answer confidence from rerank strength, support, and abstain signals."""
    frame = scenario_frame or {}
    question = (frame.get("question") or "").strip()
    rows = _abstain_top_rows(reranked)
    if not rows:
        return "low"

    if question:
        should_abstain, _ = _should_abstain(reranked, question)
        if should_abstain:
            return "low"
        query_tokens = _content_tokens(question)
    else:
        query_tokens = set()

    review = reranked.get("_senku", {}) or reranked.get("_senku_review", {})
    annotations = review.get("result_annotations", [])
    objective_coverage = review.get("objective_coverage", [])

    rrf_scores = []
    overlap_counts = []
    vector_similarities = []
    metadata_deltas = []
    lexical_hit_counts = []

    for doc, meta, dist in rows:
        meta = meta or {}
        try:
            rrf_scores.append(float(meta.get("_rrf_score", 0.0) or 0.0))
        except (TypeError, ValueError):
            rrf_scores.append(0.0)
        overlap_tokens = (
            _abstain_row_overlap_tokens(query_tokens, doc, meta) if query_tokens else set()
        )
        overlap_counts.append(len(overlap_tokens))
        vector_similarities.append(_abstain_row_vector_similarity(meta, dist))
        metadata_deltas.append(
            _metadata_rerank_delta(question, meta) if question else 0.0
        )
        lexical_hit_counts.append(int(meta.get("_lexical_hits", 0) or 0))

    top_rrf_score = rrf_scores[0] if rrf_scores else 0.0
    second_rrf_score = rrf_scores[1] if len(rrf_scores) > 1 else 0.0
    top_rrf_gap = top_rrf_score - second_rrf_score
    top_overlap = overlap_counts[0] if overlap_counts else 0
    max_overlap = max(overlap_counts, default=0)
    max_vector_similarity = max(vector_similarities, default=0.0)
    top_metadata_delta = metadata_deltas[0] if metadata_deltas else 0.0
    direct_support = sum(
        1 for annotation in annotations if annotation.get("support_signal") == "direct"
    )
    related_support = sum(
        1
        for annotation in annotations
        if annotation.get("support_signal") == "related"
    )
    covered_objectives = sum(
        1 for coverage in objective_coverage if coverage.get("status") == "covered"
    )
    weak_objectives = sum(
        1 for coverage in objective_coverage if coverage.get("status") == "weak"
    )

    if (
        top_rrf_score >= 0.03
        and top_rrf_gap >= 0.003
        and top_overlap >= 2
        and max_vector_similarity >= 0.68
        and top_metadata_delta <= 0.03
    ):
        return "high"
    if (
        (direct_support or covered_objectives)
        and top_overlap >= 1
        and max_vector_similarity >= 0.62
        and top_metadata_delta <= 0.04
    ):
        return "high"
    if (
        max_overlap >= 3
        and max_vector_similarity >= 0.58
        and top_metadata_delta <= 0.06
    ):
        return "high"
    if (
        top_rrf_score >= 0.018
        and (
            max_overlap >= 1
            or max_vector_similarity >= 0.52
            or top_metadata_delta <= 0.10
        )
    ):
        return "medium"
    if direct_support or related_support or covered_objectives or weak_objectives:
        return "medium"
    if max_overlap >= 1 or max_vector_similarity >= 0.45 or any(lexical_hit_counts):
        return "medium"
    return "low"


def _normalized_rrf_strength(raw_score: float) -> float:
    """Clamp raw RRF support to a stable 0..1 strength scale for mode branching."""
    try:
        score = float(raw_score or 0.0)
    except (TypeError, ValueError):
        score = 0.0
    if score <= 0.0:
        return 0.0
    return min(score / 0.03, 1.0)


def _average_rrf_strength(rows) -> float:
    """Average the normalized RRF support across the top retrieval rows."""
    if not rows:
        return 0.0
    strengths = []
    for _doc, meta, _dist in rows:
        meta = meta or {}
        strengths.append(_normalized_rrf_strength(meta.get("_rrf_score", 0.0)))
    return sum(strengths) / len(strengths)


def _has_primary_owner_support(results) -> bool:
    """Return True when retrieval review signals direct support for the query family."""
    review = results.get("_senku", {}) or results.get("_senku_review", {})
    annotations = review.get("result_annotations", [])
    objective_coverage = review.get("objective_coverage", [])
    return any(
        annotation.get("support_signal") == "direct" for annotation in annotations
    ) or any(
        coverage.get("status") == "covered" for coverage in objective_coverage
    )


def _resolve_answer_mode(
    reranked, scenario_frame, confidence_label
) -> AnswerMode:
    """Resolve the deterministic answer branch from the post-rerank retrieval state."""
    frame = scenario_frame or {}
    question = (frame.get("question") or "").strip()
    if not question:
        return "confident"

    should_abstain, _ = _should_abstain(reranked, question)
    if should_abstain:
        return "abstain"

    rows = _abstain_top_rows(reranked)
    if not rows:
        return "confident"

    average_rrf_strength = _average_rrf_strength(rows)
    _doc, top_meta, top_dist = rows[0]
    top_vector_similarity = _abstain_row_vector_similarity(top_meta or {}, top_dist)
    if average_rrf_strength < 0.65:
        return "uncertain_fit"
    if 0.45 <= top_vector_similarity <= 0.67:
        return "uncertain_fit"
    if (
        _scenario_frame_is_safety_critical(frame)
        and not _has_primary_owner_support(reranked)
        and confidence_label in {"medium", "low"}
    ):
        return "uncertain_fit"
    return "confident"


def retrieve_results(
    question, collection, top_k, category=None, session_state=None, lm_studio_url=None
):
    """Run retrieval with category-aware supplemental lanes and metadata reranking."""
    current_frame = build_scenario_frame(question)
    scenario_frame = merge_frame_with_session(current_frame, session_state)
    session_state_used = _should_use_session_context(
        question, current_frame, session_state
    )
    sub_queries = decompose_query(question)
    candidate_limit = _retrieval_candidate_limit(top_k)
    specs = []
    seen = set()

    for sub_query in sub_queries:
        _append_retrieval_spec(
            specs,
            seen,
            text=sub_query,
            category=category,
            limit=candidate_limit,
        )

    for spec in _supplemental_retrieval_specs(
        question, top_k, category, scenario_frame, session_state
    ):
        _append_retrieval_spec(
            specs,
            seen,
            text=spec["text"],
            category=spec["category"],
            limit=spec["limit"],
        )

    embeddings = embed_batch([spec["text"] for spec in specs], base_url=lm_studio_url)
    if len(embeddings) != len(specs):
        raise RuntimeError(
            f"Embedding response count mismatch: expected {len(specs)} embeddings, got {len(embeddings)}"
        )
    result_sets = []
    for spec, emb in zip(specs, embeddings):
        query_kwargs = {
            "query_embeddings": [emb],
            "n_results": spec["limit"],
            "include": ["documents", "metadatas", "distances"],
        }
        if spec["category"]:
            query_kwargs["where"] = {"category": spec["category"]}
        vector_results = collection.query(**query_kwargs)
        vector_results["_retrieval_kind"] = "vector"
        result_sets.append(vector_results)

        lexical_results = query_lexical_index(
            spec["text"], spec["limit"], spec["category"]
        )
        if lexical_results["ids"][0]:
            result_sets.append(lexical_results)

    results = merge_results(
        result_sets,
        candidate_limit,
        question=question,
        session_state=session_state,
    )

    reranked = rerank_results(question, results, top_k, scenario_frame=scenario_frame)
    reranked["_senku"] = _review_metadata(
        reranked,
        specs,
        sub_queries,
        scenario_frame,
        current_frame,
        session_state_used,
    )
    reranked["_senku"]["confidence_label"] = _confidence_label(
        reranked, scenario_frame
    )
    reranked["_senku"]["confidence_instruction"] = build_confidence_system_instruction(
        reranked["_senku"]["confidence_label"]
    )
    reranked["_senku"]["answer_mode"] = _resolve_answer_mode(
        reranked,
        scenario_frame,
        reranked["_senku"]["confidence_label"],
    )
    return reranked, sub_queries, reranked["_senku"]


def _fix_mojibake(text):
    """Fix double-encoded UTF-8 produced by the LLM.

    When the model outputs UTF-8 bytes as individual Unicode code points
    (e.g., U+00C2 U+00B0 instead of U+00B0 for deg), re-encode them back
    to the correct characters.
    """
    try:
        # Encode each char as its raw byte value (latin-1 is 1:1 for U+0000-U+00FF),
        # then decode the resulting bytes as UTF-8 to recover the intended characters.
        return text.encode("latin-1").decode("utf-8")
    except (UnicodeDecodeError, UnicodeEncodeError):
        # Not all chars are in the latin-1 range, or the bytes don't form valid UTF-8.
        # Fall back to fixing only the safe 2-byte sequences (A-circumflex + char).
        return re.sub(
            r"\u00c2([\u00a0-\u00bf])",
            lambda m: m.group(1),
            text,
        )


def _relevance_tag(distance):
    """Convert cosine distance to a relevance label."""
    if distance < 0.8:
        return "high"
    elif distance < 1.0:
        return "medium"
    return "low"


def _format_frame_section(title, items, *, limit=4):
    """Render a compact bullet-style frame section."""
    if not items:
        return ""
    display = items[:limit]
    extra = "" if len(items) <= limit else f"; +{len(items) - limit} more"
    return f"- {title}: " + "; ".join(display) + extra


def _objective_coverage_lines(review):
    """Render coverage labels for prompt/review panels."""
    lines = []
    for item in review.get("objective_coverage", []):
        label = item.get("objective", "unnamed objective")
        status = item.get("status", "unknown")
        if item.get("best_source") and status != "missing":
            lines.append(f"{label} [{status}] via {item['best_source']}")
        else:
            lines.append(f"{label} [{status}]")
    return lines


def _prompt_mode_notes(mode, review, question=None):
    """Return extra prompt guidance for the selected output mode."""
    normalized = (mode or "default").strip().lower()
    notes = []
    question_lower = (question or "").lower()
    missing = [
        item["objective"]
        for item in review.get("objective_coverage", [])
        if item.get("status") == "missing"
    ]
    if normalized == "review":
        notes.append(
            "- Name any missing or weakly supported objectives before extrapolating."
        )
        notes.append("- Make the coverage check visible, but keep it short.")
    elif normalized == "demo":
        notes.append(
            "- Keep the answer compact and front-load the highest-value actions."
        )
        notes.append("- Avoid debug exposition unless it changes the recommendation.")
    elif normalized == "public-safe":
        notes.append(
            "- Use plain, conservative wording and avoid stylized voice flourishes."
        )
    if any(
        marker in question_lower
        for marker in (
            "coffee grounds",
            "coffee ground vomit",
            "black tarry stool",
            "black tarry stools",
            "black stool",
            "black stools",
            "bright red vomit",
            "vomit blood",
            "vomiting blood",
            "dark clots",
        )
    ):
        notes.append(
            "- Treat these symptoms as a possible GI bleed. Lead with airway, "
            "ongoing bleeding, and shock checks plus urgent escalation before "
            "hydration, food poisoning, or routine GI self-care advice."
        )
        notes.append(
            "- If immediate actions are needed, use a compact numbered list with "
            "bleed-first triage and emergency help up front."
        )
    if missing:
        notes.append(
            "- For objectives marked missing, give only the closest grounded guidance and say where support is thin."
        )
    return notes


def build_prompt(
    question, results, mode="default", session_state=None, prompt_token_limit=None
):
    """Build the context-augmented prompt from retrieved chunks."""
    review = results.get("_senku", {})
    annotations = review.get("result_annotations", [])
    context_blocks = []
    distances = results.get("distances", [[]])[0]
    for i, (doc, meta) in enumerate(
        zip(results["documents"][0], results["metadatas"][0])
    ):
        meta = meta or {}
        dist = distances[i] if i < len(distances) else 1.0
        relevance = _relevance_tag(dist)
        annotation = annotations[i] if i < len(annotations) else {}
        support_signal = annotation.get("support_signal", "peripheral")
        matched_objectives = annotation.get("matched_objectives", [])
        liability_level = (meta.get("liability_level", "") or "").strip()
        header = (
            f"[Guide: {meta.get('guide_title', 'Unknown guide')} ({meta.get('guide_id', 'unknown')}) "
            f"| Section: {meta.get('section_heading', '')} "
            f"| Category: {meta.get('category', 'unknown')} "
            f"| Difficulty: {meta.get('difficulty', 'unknown')} "
            f"{f'| Liability: {liability_level} ' if liability_level else ''}"
            f"| Relevance: {relevance} "
            f"| Support: {support_signal}]"
        )
        if matched_objectives:
            header += f"\n[Objective Match: {', '.join(matched_objectives[:3])}]"
        context_blocks.append(f"{header}\n{doc}")

    frame = review.get("scenario_frame", {})
    frame_lines = [
        _format_frame_section(
            "Objectives", [item.get("text", "") for item in frame.get("objectives", [])]
        ),
        _format_frame_section("Assets", frame.get("assets", [])),
        _format_frame_section("Constraints", frame.get("constraints", [])),
        _format_frame_section("Hazards", frame.get("hazards", [])),
        _format_frame_section("People", frame.get("people", [])),
    ]
    if frame.get("deadline"):
        frame_lines.append(f"- Deadline: {frame['deadline']}")
    coverage_lines = _objective_coverage_lines(review)
    prompt_notes = _prompt_mode_notes(mode, review, question)
    prompt_token_limit = prompt_token_limit or _prompt_token_limit(
        gen_model=config.GEN_MODEL,
        gen_url=config.LM_STUDIO_URL,
    )
    compact_litert_notes = (
        prompt_token_limit is not None and prompt_token_limit <= 4096
    )
    mental_health_crisis_query = _is_mental_health_crisis_query(question)
    noncollapse_stroke_cardiac_overlap_query = (
        _is_noncollapse_stroke_cardiac_overlap_query(question)
    )
    mania_or_psychosis_like_query = mental_health_crisis_query and (
        _is_mania_or_psychosis_like_query(question)
    )
    compact_litert_emergency_trim = compact_litert_notes and (
        _is_acute_symptom_query(question)
        or _is_cardiac_emergency_query(question)
        or noncollapse_stroke_cardiac_overlap_query
        or mental_health_crisis_query
    )
    if mental_health_crisis_query:
        prompt_notes = [
            "- Mental-health crisis contract: keep the opening block decisively crisis-oriented. Lead with close supervision/do not leave them alone, means restriction, and urgent same-day or emergency escalation before any calming, routine, sleep/basic-function, or generic stress-management advice.",
            *prompt_notes,
        ]
        if mania_or_psychosis_like_query:
            prompt_notes = [
                "- Do not let vague or observer-language framing downgrade mania/psychosis-like red flags into stress, insomnia, elder-care, or basic-function coaching. If the prompt includes no sleep or barely sleeping plus agitation, reckless behavior, invincible/grandiose language, nonstop movement/talking, sudden behavior change, or mission-like thinking, treat it as crisis-owner guidance.",
                *prompt_notes,
            ]
    if _is_generic_puncture_query(question) and not _has_major_bleeding_signal(
        question
    ):
        prompt_notes.append(
            "- For a generic puncture wound without severe bleeding: keep the answer on pressure if bleeding, irrigation, leaving the tract open, dressing changes, tetanus risk, infection monitoring, and evacuation red flags."
        )
        prompt_notes.append(
            "- Do not mention tourniquet timing, cavity packing, herbal remedies, or antibiotic names/doses unless the user's scenario explicitly requires them."
        )
    if _is_supply_conflict_query(question):
        prompt_notes.append(
            "- Prefer reversible de-escalation, transparent temporary rules, shared observation, and role rotation before formal adjudication, courts, or punishment."
        )
        prompt_notes.append(
            "- For ordinary intra-group supply disputes, prefer temporary shared custody, public inventory logs, two-person verification, and short review periods over permanent authority structures, bonds, or appeals."
        )
    if _is_post_collapse_recovery_query(question):
        prompt_notes.append(
            "- Treat this as first-phase recovery only: stabilize people, secure water/sanitation, secure shelter/food, assign roles/records. Do not drift into war, fallout, or ideological theory unless the user explicitly mentioned them."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections in this order: stabilize people, water and sanitation, food/shelter/heat, organization and knowledge. Keep each section to 1-2 sentences."
        )
    if _is_broad_governance_query(question):
        prompt_notes.append(
            "- For broad governance design, give a compact starter structure only: emergency authority, representation, basic rules/accountability, and review/transition. Do not write a constitution or full political taxonomy."
        )
    if _is_survivor_governance_setup_query(question):
        prompt_notes.append(
            "- For survivor governance setup, use exactly 5 short numbered sections in this order: mission summary, immediate authority, representation, accountability/records, transition trigger."
        )
        prompt_notes.append(
            "- Keep each numbered section to 1-2 sentences with no sub-bullets, no examples list, and no ideological comparison."
        )
    if _is_weather_prep_query(question, frame):
        prompt_notes.append(
            "- For weather preparation with a deadline, use a short time-boxed checklist anchored to the deadline and stop after the most protective actions."
        )
    if _is_deadline_monsoon_query(question, frame):
        prompt_notes.append(
            "- For monsoon preparation, structure the answer as a 7-day triage plan with exactly 5 numbered sections: protect people, protect water/food, protect shelter/drainage, protect tools/animals/crops, during-storm watchouts."
        )
        prompt_notes.append(
            "- Keep it to immediate first-week actions only. Do not expand into crop-by-crop theory, full flood engineering, or long stockpile catalogs."
        )
    if _is_signs_and_treatment_query(question):
        prompt_notes.append(
            "- For signs-and-treatment questions, keep the recognition bands brief, then move quickly to the treatment steps. Do not expand into a physiology lecture."
        )
    if compact_litert_notes:
        prompt_notes.append(
            "- Lead with the first useful action or check; if the scenario is urgent or hazardous, answer that danger branch first; keep routing explanation minimal once grounded."
        )
    else:
        prompt_notes.append(
            "- Lead with the first useful action or check, not background explanation."
        )
        prompt_notes.append(
            "- If the question includes an immediate safety, poisoning, electrical, fire, medical, or exposure risk, answer the dangerous branch first before routine repair or optimization."
        )
        prompt_notes.append(
            "- When the guide support is already tightly focused on the user's problem, do not spend multiple sentences explaining sourcing or alternate guide ownership."
        )
    if _is_acute_symptom_query(question):
        if compact_litert_notes:
            prompt_notes.append(
                "- Acute symptoms: use verified medical sources only, lead with red flags and escalation, and avoid speculation."
            )
        else:
            prompt_notes.append(
                "- For acute symptom questions, stay on verified medical sources only: common-ailments, first-aid, medications, and focused symptom guides. Ignore non-medical sources."
            )
            prompt_notes.append(
                "- Lead with red flags and escalation criteria before any comfort care, and do not speculate beyond the grounded medical guides."
            )
    if noncollapse_stroke_cardiac_overlap_query:
        if compact_litert_notes:
            prompt_notes.append(
                "- Mixed stroke/TIA plus cardiac warning signs: open with emergency help now, then give only the immediate waiting-for-EMS actions and the CPR/AED-if-they-collapse branch."
            )
        else:
            prompt_notes.append(
                "- For mixed stroke/TIA plus cardiac-warning presentations, force immediate emergency-first routing: open with emergency help now, then cover FAST/cardiac watchouts while waiting, then the CPR/AED branch only if they become unresponsive or pulseless."
            )
            prompt_notes.append(
                "- Do not drift into outpatient differential talk, routine monitoring-only advice, food/drink advice, or broad home-care guidance before emergency escalation."
            )
    if _is_cardiac_emergency_query(question):
        if compact_litert_notes:
            prompt_notes.append(
                "- Cardiac/collapse emergencies: use first aid, CPR, stroke red flags, and emergency escalation first; keep routing explanation minimal."
            )
        else:
            prompt_notes.append(
                "- For cardiac/collapse emergencies, stay on first aid, CPR, stroke red flags, and emergency escalation. Ignore unrelated medical detail."
            )
    if mental_health_crisis_query:
        if compact_litert_notes:
            prompt_notes.append(
                "- Mental-health crisis: open with a numbered immediate-action block. The first three steps must be close supervision/do not leave them alone, means restriction, and urgent escalation. Do not open with assess/monitor-only phrasing, calming-only/calm-presence wording, validation or de-escalation wording, redirect-to-task, sit-down/routine coaching, maintain-routine/basic-function coaching, meals-or-sleep resets, food-or-sleep troubleshooting, journaling, sleep-hygiene, or routine self-care before escalation."
            )
            prompt_notes.append(
                "- If messy observer wording could be read as stress, insomnia, or anxiety but the prompt also includes no sleep or barely sleeping plus agitation, reckless behavior, invincible/grandiose language, nonstop movement/talking, sudden behavior change, or mission-like thinking, treat it as a crisis-owner prompt and keep the opening block on supervision, means restriction, and escalation only."
            )
        else:
            prompt_notes.append(
                "- For mental-health crisis questions, the first action block is close supervision + means restriction + escalation only: keep the person with a trusted adult, do not leave them alone, reduce access to vehicles, weapons, keys, medicines, and other obvious means of harm, and escalate now to emergency or urgent mental-health help."
            )
            prompt_notes.append(
                "- If ambiguous observer-language prompts could be read as stress, insomnia, or anxiety but they also include no sleep or barely sleeping plus agitation, reckless behavior, invincible/grandiose language, nonstop movement/talking, sudden behavior change, or mission-like thinking, resolve that ambiguity toward crisis-owner primacy rather than routine self-management."
            )
            prompt_notes.append(
                "- Do not front-load calming exercises, calm-presence-only scripts, validation-only or de-escalation detours, redirecting the person to a task, asking them to sit down, maintain-routine/basic-function coaching, meals/sleep resets, nutrition/sleep troubleshooting, grief processing, sleep-hygiene, routine reset, or other basic-function/self-management coaching when mania-, psychosis-, or dangerous activation-like behavior is present. Mention those only after stabilization and escalation if they are still relevant."
            )
            prompt_notes.append(
                "- Unless the question explicitly gives older-adult, dementia, or cognitive-decline evidence, do not frame these activation-like crisis prompts as elder-care wandering or sleep/self-management problems."
            )
    if _is_household_chemical_hazard_query(question):
        if compact_litert_notes:
            prompt_notes.append(
                "- Chemical exposure: separate people from the source, ventilate if safe, flush eye/skin exposure with water, and escalate to Poison Control or EMS before home remedies."
            )
        else:
            prompt_notes.append(
                "- For household chemical hazard questions, treat the issue as poisoning / chemical exposure first: separate people from the source, ventilate if safe, and avoid mixing cleaners."
            )
            prompt_notes.append(
                "- If mixed cleaners are involved and there is coughing, chest tightness, wheeze, or trouble breathing, answer with immediate fresh-air separation and Poison Control/EMS escalation first; do not use wait-and-see or 'if symptoms worsen' framing."
            )
            prompt_notes.append(
                "- For corrosive exposures including battery acid, drain cleaner, oven cleaner, lye, or other strong acids/alkalis, start water flushing/decontamination right away, remove contaminated clothing, and do not try to neutralize the chemical."
            )
            prompt_notes.append(
                "- Prioritize poison-control / emergency guidance, chemical burn first aid, and evacuation thresholds. Do not suggest home remedies that could worsen exposure."
            )
    if _is_enclosed_room_fire_smoke_question(question):
        if compact_litert_notes:
            prompt_notes.append(
                "- Enclosed-room smoke or fire: evacuate first, call emergency services, and do not re-enter to work the fire."
            )
        else:
            prompt_notes.append(
                "- For enclosed-room smoke-back or fire-plus-smoke questions, answer evacuation-first: get everyone out, call emergency services, and only then discuss fire control if it can be done without re-entering the space. Do not say 'stop feeding the fire first' before evacuation."
            )
    if _is_closed_room_fire_question(question):
        if compact_litert_notes:
            prompt_notes.append(
                "- Closed-room fire: evacuate and call emergency services first; do not say to fight the fire before getting out."
            )
        else:
            prompt_notes.append(
                "- For a closed bedroom or enclosed-room fire, lead with evacuation and emergency services first. Do not say 'stop the fire first' before evacuation, and do not pivot to cookstove or chimney guidance unless the user mentions a stove, heater, or chimney."
            )
    if _is_rescue_signaling_query(question):
        prompt_notes.append(
            "- For rescue signaling, give the top 4 highest-yield methods in priority order and stop there."
        )
    if _is_glassmaking_query(question):
        prompt_notes.append(
            "- For making glass from scratch, keep the answer centered on raw materials, furnace heat, forming, and annealing. Do not drift into optics, hourglasses, or flat-glass salvage."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections: raw materials, furnace/crucible setup, melt/refine, forming/annealing. No sub-bullets, no specialty-glass survey, and no waterglass or industrial side uses."
        )
    if _is_paper_ink_query(question):
        prompt_notes.append(
            "- Split the answer evenly between paper and ink: first paper pulp/sheet/sizing, then ink pigment/binder/storage. Do not drift into printing presses, typography, or newsletter production."
        )
        prompt_notes.append(
            "- Keep it to exactly 5 short numbered sections: fibers, pulping/sheeting, sizing, simple carbon ink, stronger archival ink or storage note."
        )
    if _is_no_gear_fishing_query(question):
        prompt_notes.append(
            "- For no-gear fishing, give only the top 4 field-expedient methods: hand capture, passive traps/weirs, improvised spear, and improvised line only if basic cordage/hook material is available."
        )
        prompt_notes.append(
            "- Do not expand into marine/open-water fishing, sustainability policy, or broad legal commentary."
        )
        prompt_notes.append(
            "- Keep each method to 1-2 sentences and do not use sub-bullets."
        )
    if _is_raft_lake_query(question):
        prompt_notes.append(
            "- For a raft to cross a lake, keep it to a starter crossing plan only: buoyancy/load, frame/lashing, propulsion/stability, and shoreline testing."
        )
        prompt_notes.append(
            "- Do not drift into road building, rescue doctrine, or full boatbuilding theory. Use exactly 4 short numbered sections with no sub-bullets."
        )
    if _is_house_build_query(question):
        prompt_notes.append(
            "- For a house or cabin prompt, choose one simple low-tech starter dwelling path from the notes and keep the answer on site/drainage, floor/foundation, walls/frame, and roof/weatherproofing."
        )
        prompt_notes.append(
            "- If the notes split across shelter and subsystem guides, synthesize one coherent starter cabin path instead of listing unrelated building topics."
        )
        prompt_notes.append(
            "- Do not drift into census, settlement planning, abstract architecture theory, or broad housing policy. Use exactly 4 short numbered sections with no sub-bullets."
        )
    if _is_small_watercraft_query(question):
        prompt_notes.append(
            "- For a canoe or small boat, choose the simplest grounded build path from the notes, preferably dugout or basic plank construction when supported. Keep it on hull, shaping, sealing, and test-launch limits."
        )
        prompt_notes.append(
            "- Choose one primary build path rather than mixing multiple boat families together. Mention an alternative only briefly if the material requirements are different."
        )
        prompt_notes.append(
            "- Do not drift into rescue doctrine, shipping, or broad transportation theory. Use exactly 4 short numbered sections: choose hull type/material, shape the hull, seal/fit out, shoreline test and limits."
        )
    if _is_water_purification_query(question):
        prompt_notes.append(
            "- For water purification, keep filtration, disinfection, and storage clearly separated. If the notes only support pre-filtration, say that it does not by itself make water microbiologically safe."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections: source triage, prefilter, disinfect, safe storage. Do not drift into alkali production, fisheries, irrigation, or broad waterway engineering."
        )
    if _is_water_storage_query(question):
        prompt_notes.append(
            "- For water storage, stay focused on container vetting, cleaning, filling/sealing, and rotation or inspection. Prefer known food-safe or freshly cleaned containers when the notes support that."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections: pick container, clean/sanitize, fill/seal, rotate/check. Do not drift into alkali chemistry, aquatic biology, or irrigation systems."
        )
    if _is_fire_in_rain_special_case(question):
        prompt_notes.append(
            "- For wet-weather fire starting, give one dry-core ignition plan only: shelter the work area, harvest dry inner material, prepare tinder/kindling, then grow the fire carefully."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections with no sub-bullets. Do not drift into fire suppression, wildfire management, or broad fire-safety theory."
        )
    if _is_weld_without_welder_query(question):
        prompt_notes.append(
            "- For joining metal without an electric welder, choose one grounded low-tech joining family from the notes, such as forge welding, brazing, or soldering, based on heat and material limits."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections: choose method, prep joint, heat/join, inspect limits. Do not drift into foundry, minting, or abstract metallurgy."
        )
    if _is_fair_trial_query(question):
        prompt_notes.append(
            "- For a fair trial without lawyers or judges, go straight to lay-panel procedure. Do not lead with mediation, property law, federation models, or ideological theory unless the user asked for them."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections: panel selection, hearing order, evidence/record rules, review/appeal. No sub-bullets."
        )
    if _is_clay_oven_query(question):
        prompt_notes.append(
            "- For a clay oven, give one simple starter build only: materials mix, base/floor, dome/opening, drying/first firing. Do not branch into multiple oven families unless the user asked for them."
        )
        prompt_notes.append(
            "- Keep it to exactly 4 short numbered sections with no sub-bullets."
        )
    if _is_soapmaking_query(question):
        prompt_notes.append(
            "- For soapmaking, keep it practical: one safe ash-lye-and-fat starter path plus, only when relevant, one immediate stopgap cleaning fallback. Do not drift into candle making or a full chemistry lecture."
        )
        prompt_notes.append(
            "- For lye safety, tell the user to flush skin or eyes with lots of water if exposed. Do not recommend neutralizing caustic splashes on skin with vinegar or applying lye water directly to skin for washing."
        )
        if _is_soap_restock_query(question):
            prompt_notes.append(
                "- Use exactly 4 short numbered sections: immediate stopgap cleaning, lye water, fat/oil prep, mix/cure. No sub-bullets."
            )
        else:
            prompt_notes.append(
                "- Use exactly 4 short numbered sections: make lye water, render/clean the fat, mix to trace, mold/cure. No sub-bullets."
            )
    if _is_weld_without_welder_query(question):
        prompt_notes.append(
            "- For joining metal without an electric welder, rank the options from lowest-resource to highest-resource and keep the answer on the simplest viable path."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections: choose join type, prep surfaces, do the join, cool/test/safety. No sub-bullets and no long taxonomy of welding processes."
        )
    if _is_bridge_span_query(question):
        prompt_notes.append(
            "- For a bridge over a modest river span, decide the crossing type first, then give the simplest workable structure, anchoring/abutments, and testing. Do not expand into multiple bridge classes unless the user asked for vehicle or animal loads."
        )
        prompt_notes.append(
            "- Keep the answer to exactly 4 short numbered sections: crossing choice, main span, supports/anchoring, testing/load limits."
        )
    if _is_new_well_query(question):
        prompt_notes.append(
            "- For replacing a collapsed well, lead with cave-in risk and contamination prevention. Keep it to site choice, shoring/casing, reaching water, and protecting the finished well."
        )
        prompt_notes.append(
            "- Do not expand into side variants like beach wells or exotic water tables unless the user asked for them."
        )
    if _is_group_role_assignment_query(question):
        prompt_notes.append(
            "- For role allocation in a mid-sized group, give a compact operating structure only: skills census, essential teams, named leads, and review/rotation. Do not drift into elections, ideology, or full governance theory unless asked."
        )
        prompt_notes.append(
            "- Keep it to exactly 4 short numbered sections with no sub-bullets."
        )
    if _is_supply_hiding_query(question):
        prompt_notes.append(
            "- For hiding supplies, focus on split stores, concealment, weatherproofing, and recovery discipline. Do not drift into traps, perimeter defense, or unrelated camouflage craft unless the user asked for them."
        )
        prompt_notes.append(
            "- Keep it to exactly 4 short numbered sections: split stores, site choice/concealment, moisture protection, recovery/decoy discipline."
        )
    if _is_roman_concrete_query(question):
        prompt_notes.append(
            "- For Roman concrete, explain the lime-plus-pozzolan mechanism briefly, then give a compact 4-step process: make lime, prepare pozzolan, mix, cure."
        )
        prompt_notes.append(
            "- Do not expand into modern QA/testing procedures, long historical comparisons, or archaeology sidebars."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections with no sub-bullets, keep ratio guidance to one starter range only, and emphasize compression use rather than broad structural theory."
        )
    if _is_cast_without_foundry_query(question):
        prompt_notes.append(
            "- For casting without a foundry, keep the answer at low-tech starter level: choose an easier metal, simple melt setup, mold prep/drying, pour/cool. Do not branch into project catalogs or commercial foundry taxonomy."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections with no sub-bullets, and prefer non-ferrous metals over steel unless the user explicitly asks for iron or steel."
        )
    if _is_brain_tanning_query(question):
        prompt_notes.append(
            "- For brain tanning, answer yes/no first, then stay on the practical starter path only: hide prep, brain emulsion, working/drying, smoke finish."
        )
        prompt_notes.append(
            "- Do not drift into bark tanning, general leather applications, or butchering detail. Use exactly 4 short numbered sections with no sub-bullets."
        )
    if _is_hot_weather_burial_query(question):
        prompt_notes.append(
            "- For preserving a body before burial in hot weather, stay on immediate practical care only: cooling, wrapping/identification, short storage, burial timing."
        )
        prompt_notes.append(
            "- Do not drift into cold-weather, avalanche, pandemic, or mass-casualty procedures unless the user explicitly asked for them. Use exactly 4 short numbered sections with no sub-bullets."
        )
        prompt_notes.append(
            "- Do not suggest placing the body in a drinking-water source or contaminating a potable stream. Prefer shade, airflow, ice if available, insect control, wrapping, and burial timing."
        )
    if _is_bow_arrow_query(question):
        prompt_notes.append(
            "- For making a bow and arrows, stay on one simple self-bow path plus matching starter arrows. Do not survey multiple bow classes or advanced tuning theory."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections: stave/string materials, bow shaping/tillering, arrow making, safety/testing/storage. No sub-bullets."
        )
    if _is_loom_weaving_query(question):
        prompt_notes.append(
            "- For building a loom and weaving cloth, stay with a simple starter frame loom instead of surveying every loom class."
        )
        prompt_notes.append(
            "- Use exactly 4 short numbered sections: frame build, warp/weft setup, weaving workflow, finishing/first improvements. No sub-bullets and no advanced floor-loom mechanics."
        )
    session_text = _render_session_state_text(session_state)
    if session_text and review.get("session_state_used"):
        prompt_notes.append(
            "- This is a follow-up. Unless the user clearly changes topic, keep the same subject, constraints, and unresolved risk in view without re-explaining the whole background."
        )
        prompt_notes.append(
            "- Reuse prior thread context to answer the new question directly; only restate earlier facts when they change the next action."
        )
        frame_lines.append(f"- Session Context: {session_text}")
    if compact_litert_emergency_trim:
        context_blocks = context_blocks[:4]
        frame_lines = [
            line
            for line in frame_lines
            if line.startswith(("- Objectives", "- Hazards", "- People"))
        ][:3]
        coverage_lines = coverage_lines[:1]
        prompt_notes = [
            note
            for note in prompt_notes
            if "acute symptoms" in note.lower()
            or "cardiac/collapse emergencies" in note.lower()
            or "mental-health crisis" in note.lower()
            or "first action block" in note.lower()
        ]
    reference = "\n---\n".join(context_blocks)
    frame_text = "\n".join(line for line in frame_lines if line)
    coverage_text = "\n".join(f"- {line}" for line in coverage_lines)
    extra_notes = "\n".join(prompt_notes)
    extra_notes_block = f"{extra_notes}\n" if extra_notes else ""
    mental_health_contract_line = ""
    if _is_mental_health_crisis_query(question):
        mental_health_contract_line = (
            "- For mental-health crisis prompts, the first immediate-action block "
            "must stay on close supervision, means restriction, and escalation "
            "only, in that order. Do not open with calming-only/calm-presence "
            "scripting, validation or de-escalation wording, redirect-to-task, "
            "sit-down/routine coaching, maintain-routine/basic-function "
            "coaching, meals-or-sleep resets, nutrition-or-sleep management, "
            "journaling, sleep-hygiene, routine-reset, and other basic-function "
            "or self-management steps until after stabilization and escalation. "
            "If ambiguous observer-language prompts include no sleep or barely "
            "sleeping plus agitation, reckless behavior, invincible/grandiose "
            "language, nonstop movement/talking, sudden behavior change, or "
            "mission-like thinking, resolve that ambiguity toward crisis-owner "
            "primacy instead of stress, insomnia, or routine self-management. "
            "Unless the prompt explicitly gives age or cognitive-decline "
            "evidence, do not reframe the scenario as elder-care wandering.\n"
        )

    prompt = (
        f"Scenario Frame:\n{frame_text or '- none'}\n\n"
        f"Coverage Check:\n{coverage_text or '- none'}\n\n"
        f"Guide Excerpts:\n---\n{reference}\n---\n\n"
        f"Using the guides above, answer the following question. "
        f"Apply relevant principles even if no guide mentions the user's "
        f"exact scenario by name. If the question is vague, prioritize: "
        f"shelter, water, fire, food, signaling.\n\n"
        f"Response requirements:\n"
        f"- Lead with the most useful immediate action, check, or conclusion in the first sentence.\n"
        f"- If the user asks what to do right now, what to worry about first, or before anything else, or the scenario is clearly urgent/safety-critical, answer with a compact 3-4 step numbered immediate-action list. Put the first concrete action first, keep each step imperative and specific, and do not stop after a warning sentence or lead paragraph.\n"
        f"- If there is an immediate danger, use the same compact list pattern when it fits: Do first, Avoid, Escalate if, then brief supporting steps.\n"
        f"{mental_health_contract_line}"
        f"- Start with the core mechanism, failure mode, or constraint.\n"
        f"- For procedures, move from lowest-resource actions to more advanced options.\n"
        f"- For conceptual questions, explain the system-design logic and safeguards.\n"
        f"- If the user asks for order, answer as a numbered plan and account for major listed assets/constraints.\n"
        f"- Default to one short summary plus at most 4 numbered steps or sections unless a prompt note below asks for a different structure or more are necessary for safety.\n"
        f"- Stop after the smallest complete answer; do not add appendices, exhaustive variant lists, or long catalogs unless the user asks for depth.\n"
        f"- For very broad build/planning questions, give the first viable path and the main failure modes instead of a full curriculum.\n"
        f"- Once the answer is already grounded in a focused guide, stay with the recommendation instead of explaining the sourcing process.\n"
        f"- Use the minimum citations needed to ground each paragraph or numbered step; usually 1 guide ID is enough, and use 2 only when a step truly combines distinct sources.\n"
        f"- Prefer non-invasive stabilization over advanced field procedures unless the scenario clearly supports them.\n"
        f"- Stay tightly scoped to the question; avoid adjacent upgrades unless they are necessary.\n"
        f"- Keep citations inline as [GD-xxx], merge duplicates, and do not invent bracketed labels.\n"
        f"- Put at least one inline citation in every substantive paragraph or numbered step; do not save citations for the end.\n"
        f"{extra_notes_block}\n"
        f"Question: {question}"
    )
    return _add_citation_allowlist_contract(
        prompt,
        results,
        mode=mode,
        prompt_token_limit=prompt_token_limit,
    )


def _extract_gd_ids(text):
    """Extract recoverable GD citation IDs from noisy model output."""
    citations = []
    seen = set()
    patterns = re.findall(r"GD[-/]\d{1,3}(?:\s*[/,]\s*(?:GD[-/])?\d{1,3})*", text or "")
    for raw_group in patterns:
        for digits in re.findall(r"\d{1,3}", raw_group):
            normalized = f"GD-{int(digits):03d}"
            if normalized in seen:
                continue
            seen.add(normalized)
            citations.append(normalized)
    return citations


def _normalize_citation_group(match):
    """Deduplicate and normalize a single bracketed GD citation group."""
    citations = _extract_gd_ids(match.group(0))
    if not citations:
        return ""
    return "[" + ", ".join(citations) + "]"


MAX_INLINE_CITATIONS_PER_LINE = 2
MAX_INLINE_CITATIONS_PER_STEP_LINE = 1
_WARNING_RESIDUAL_BRACKET_PATTERN = re.compile(r"\[([^\[\]\n]{1,80})\]")
_WARNING_RESIDUAL_CITATION_PATTERN = re.compile(
    r"^(?:GD[-/]\d{1,3})(?:\s*,\s*GD[-/]\d{1,3})*$",
    re.IGNORECASE,
)
_WARNING_RESIDUAL_PREFIXES = (
    "instructional mandate",
    "instructional constraint",
    "instructional warning",
    "instructional advisory",
    "instructional note",
    "system instruction",
    "system warning",
    "system advisory",
    "control instruction",
    "control warning",
    "safety instruction",
    "safety mandate",
    "safety advisory",
    "safety note",
    "safety warning",
    "safety constraint",
)
_WARNING_RESIDUAL_EXACT_LABELS = {
    "warning",
    "caution",
    "advisory",
    "instruction",
}
_WARNING_RESIDUAL_TRAIL_MARKERS = (
    "implied",
    "label",
    "labels",
    "residue",
    "hazard",
    "hazards",
    "risk",
    "risks",
    "process",
    "processes",
)


def _compress_citations_on_line(line):
    """Collapse repeated citation groups on one output line into one small cluster."""
    citation_groups = re.findall(r"\[(?:GD-\d{3}(?:,\s*GD-\d{3})*)\]", line)
    if not citation_groups:
        return line

    citations = []
    seen = set()
    for group in citation_groups:
        for citation in re.findall(r"GD-\d{3}", group):
            if citation in seen:
                continue
            seen.add(citation)
            citations.append(citation)

    max_citations = MAX_INLINE_CITATIONS_PER_LINE
    if re.match(r"^\s*(?:\d+\.\s+|[-*]\s+)", line):
        max_citations = MAX_INLINE_CITATIONS_PER_STEP_LINE

    if len(citation_groups) == 1 and len(citations) <= max_citations:
        return line

    body = re.sub(r"\s*\[(?:GD-\d{3}(?:,\s*GD-\d{3})*)\]", "", line).rstrip()
    body = re.sub(r"\s+([,.;:!?])", r"\1", body)
    if not body:
        return "[" + ", ".join(citations[:max_citations]) + "]"

    clipped = citations[:max_citations]
    return f"{body} [" + ", ".join(clipped) + "]"


def _rewrite_line_citations(line, citations):
    """Rewrite one line with a normalized set of inline citations."""
    body = re.sub(r"\s*\[(?:GD-\d{3}(?:,\s*GD-\d{3})*)\]", "", line).rstrip()
    body = re.sub(r"\s+([,.;:!?])", r"\1", body)
    if not citations:
        return body
    if not body:
        return "[" + ", ".join(citations) + "]"
    return f"{body} [" + ", ".join(citations) + "]"


def _compress_citations_across_numbered_steps(text):
    """Remove repeated citations inside one numbered-step block while keeping new sources."""
    lines = text.splitlines()
    if not lines:
        return text

    output = []
    in_step = False
    step_seen = set()

    for line in lines:
        if re.match(r"^\s*\d+\.\s+", line) or re.match(r"^\s*#{1,6}\s+\d+\.\s+", line):
            in_step = True
            step_seen = set(_extract_gd_ids(line))
            output.append(line)
            continue

        if in_step and not line.strip():
            in_step = False
            step_seen = set()
            output.append(line)
            continue

        if in_step and re.match(r"^\s*#{1,6}\s+", line):
            in_step = False
            step_seen = set()

        if in_step:
            citations = _extract_gd_ids(line)
            if citations:
                keep = [citation for citation in citations if citation not in step_seen]
                step_seen.update(citations)
                if keep != citations:
                    output.append(_rewrite_line_citations(line, keep))
                    continue

        output.append(line)

    return "\n".join(output)


_FORBIDDEN_RESPONSE_PHRASE_REPLACEMENTS = (
    ("the provided notes", "the guides"),
    ("the retrieved notes", "the guides"),
    ("the supplied context", "the guide support"),
    ("based on the provided information", "based on the guides"),
)


def _match_replacement_case(source, replacement):
    if source.isupper():
        return replacement.upper()
    if source[:1].isupper():
        return replacement[:1].upper() + replacement[1:]
    return replacement


def _scrub_retrieval_mechanism_language(text):
    """Rewrite retrieval-mechanism phrasing into guide-facing language."""
    cleaned = text
    for phrase, replacement in _FORBIDDEN_RESPONSE_PHRASE_REPLACEMENTS:
        cleaned = re.sub(
            re.escape(phrase),
            lambda match: _match_replacement_case(match.group(0), replacement),
            cleaned,
            flags=re.IGNORECASE,
        )
    return cleaned


def _is_warning_residual_bracket(label):
    """Return True for bracketed control/warning residue that is not a citation."""
    normalized = re.sub(r"\s+", " ", (label or "").strip()).lower()
    if not normalized:
        return False
    if _WARNING_RESIDUAL_CITATION_PATTERN.fullmatch(normalized):
        return False
    if "gd-" in normalized or "gd/" in normalized:
        return False
    if normalized in _WARNING_RESIDUAL_EXACT_LABELS:
        return True
    if any(normalized.startswith(prefix) for prefix in _WARNING_RESIDUAL_PREFIXES):
        return True
    return normalized.startswith(("warning ", "advisory ", "caution ")) and any(
        marker in normalized for marker in _WARNING_RESIDUAL_TRAIL_MARKERS
    )


def _strip_warning_residual_brackets(text):
    """Remove stale bracketed warning/instruction labels while keeping real citations."""
    if not text:
        return text

    def _rewrite(match):
        label = match.group(1)
        if _is_warning_residual_bracket(label):
            return ""
        return match.group(0)

    cleaned = _WARNING_RESIDUAL_BRACKET_PATTERN.sub(_rewrite, text)
    cleaned = re.sub(r"\[\s*\]", "", cleaned)
    cleaned = re.sub(r"\(\s*\)", "", cleaned)
    cleaned = re.sub(r"[ \t]+([,.;:!?])", r"\1", cleaned)
    cleaned = re.sub(r"[ \t]+\n", "\n", cleaned)
    cleaned = re.sub(r"[ ]{2,}", " ", cleaned)
    return cleaned


def normalize_response_text(text):
    """Normalize common model-output citation/pathology issues."""
    if not text:
        return text

    normalized = _fix_mojibake(text)
    normalized = _strip_warning_residual_brackets(normalized)
    normalized = re.sub(
        r"\bGD[-/](\d{1,3})\b",
        lambda match: f"GD-{int(match.group(1)):03d}",
        normalized,
    )
    normalized = re.sub(
        r"\[(?:[^\]]*GD[^\]]*)\]", _normalize_citation_group, normalized
    )
    normalized = "\n".join(
        _compress_citations_on_line(line) for line in normalized.splitlines()
    )
    normalized = _compress_citations_across_numbered_steps(normalized)
    normalized = _drop_unknown_guide_citations(normalized)
    normalized = re.sub(
        r"(\[(?:GD-\d{3}(?:,\s*GD-\d{3})*)\])(?:\s*\1)+", r"\1", normalized
    )
    normalized = _scrub_retrieval_mechanism_language(normalized)
    normalized = re.sub(r"[ \t]+\n", "\n", normalized)
    normalized = re.sub(r"\n{3,}", "\n\n", normalized)
    normalized = re.sub(r"[ ]{2,}", " ", normalized)
    return normalized.strip()


def _drop_unknown_guide_citations(text):
    """Remove citations that do not exist in the live guide catalog."""
    valid_guide_ids = all_guide_ids()
    invalid_ids = set()

    def _rewrite_group(match):
        citations = re.findall(r"GD-\d{3}", match.group(0))
        keep = []
        for citation in citations:
            if citation in valid_guide_ids:
                if citation not in keep:
                    keep.append(citation)
            else:
                invalid_ids.add(citation)
        if not keep:
            return ""
        return "[" + ", ".join(keep) + "]"

    cleaned = re.sub(r"\[(?:[^\]]*GD[^\]]*)\]", _rewrite_group, text)
    remaining_invalid = set(re.findall(r"GD-\d{3}", cleaned)) - valid_guide_ids
    invalid_ids.update(remaining_invalid)
    for invalid_id in sorted(invalid_ids):
        cleaned = re.sub(rf"\b{re.escape(invalid_id)}\b", "", cleaned)
        _log_warn_event("citation_hallucination", guide_id=invalid_id)

    cleaned = re.sub(r"\[\s*\]", "", cleaned)
    cleaned = re.sub(r"\(\s*\)", "", cleaned)
    cleaned = re.sub(r"\s+([,.;:!?])", r"\1", cleaned)
    return cleaned


def _duplicate_citation_count(text):
    """Return the number of repeated guide citations in a response."""
    counts = Counter(re.findall(r"GD-\d+", text or ""))
    return sum(count - 1 for count in counts.values() if count > 1)


def print_review_summary(results, session_state=None):
    """Show structured retrieval/debug signals without dumping raw chunks."""
    review = results.get("_senku", {})
    frame = review.get("scenario_frame", {})
    coverage = review.get("objective_coverage", [])
    category_distribution = review.get("category_distribution", {})
    support_signals = review.get("support_signals", {})
    lines = []

    if frame.get("objectives"):
        lines.append("Objectives:")
        for objective in frame["objectives"][:5]:
            lines.append(f"  - {_format_objective_brief(objective)}")
    if frame.get("assets"):
        lines.append("Assets: " + ", ".join(frame["assets"][:5]))
    if frame.get("constraints"):
        lines.append("Constraints: " + "; ".join(frame["constraints"][:5]))
    if frame.get("hazards"):
        lines.append("Hazards: " + ", ".join(frame["hazards"][:5]))
    if frame.get("deadline"):
        lines.append("Deadline: " + frame["deadline"])
    if coverage:
        lines.append("Coverage:")
        for item in coverage[:5]:
            source = f" via {item['best_source']}" if item.get("best_source") else ""
            lines.append(f"  - {item['objective']}: {item['status']}{source}")
    if category_distribution:
        category_line = ", ".join(
            f"{name}={count}"
            for name, count in sorted(
                category_distribution.items(),
                key=lambda item: (-item[1], item[0]),
            )[:6]
        )
        lines.append("Categories: " + category_line)
    if review.get("guide_family_diversity") is not None:
        lines.append(f"Guide family diversity: {review['guide_family_diversity']}")
    if support_signals:
        support_line = ", ".join(
            f"{name}={count}"
            for name, count in sorted(
                support_signals.items(),
                key=lambda item: (-item[1], item[0]),
            )
        )
        lines.append("Support signals: " + support_line)
    if review.get("preserved_objectives"):
        lines.append(
            "Preserved objectives: " + "; ".join(review["preserved_objectives"])
        )
    if review.get("session_state_used"):
        lines.append("Session context retrieval: on")
    if not _session_state_is_empty(session_state):
        lines.append("Session state: " + _render_session_state_text(session_state))

    console.print(
        Panel(
            "\n".join(lines) if lines else "No review metadata.",
            title="Review",
            border_style="blue",
        )
    )


def print_review_postflight(results, response_text):
    """Show response-grounding hygiene after generation."""
    duplicate_citations = _duplicate_citation_count(response_text)
    cited_ids = set(re.findall(r"GD-\d+", response_text))
    retrieved_ids = {
        meta.get("guide_id")
        for meta in results.get("metadatas", [[]])[0]
        if meta.get("guide_id")
    }
    uncited_retrieved = max(len(retrieved_ids - cited_ids), 0)
    lines = [
        f"Cited guides: {len(cited_ids)}",
        f"Retrieved-only guides: {uncited_retrieved}",
        f"Duplicate citations: {duplicate_citations}",
    ]
    console.print(Panel("\n".join(lines), title="Postflight", border_style="cyan"))


def print_sources(results, response_text="", debug=False):
    """Print source guides used in the response.

    If response_text is provided, only show sources whose guide_id was cited.
    """
    cited_ids = set(re.findall(r"GD-\d+", response_text)) if response_text else None

    console.print("\n[bold]Sources:[/bold]")
    seen = set()
    metadatas = results["metadatas"][0]

    for meta in metadatas:
        if cited_ids and meta["guide_id"] not in cited_ids:
            continue
        key = (meta["guide_title"], meta["section_heading"])
        if key not in seen:
            seen.add(key)
            console.print(
                f"  - [cyan]{meta['guide_title']}[/cyan] -> {meta['section_heading']} "
                f"[dim]({meta['category']})[/dim]"
            )

    if not debug:
        return

    # Check for related guide connections among results (debug only)
    result_slugs = set()
    for meta in metadatas:
        source = meta["source_file"]
        if source.endswith(".md"):
            result_slugs.add(source[:-3])

    seen_connections = set()
    for meta in metadatas:
        if not meta.get("related"):
            continue
        related = set(meta["related"].split(","))
        connections = related & result_slugs
        source_slug = (
            meta["source_file"][:-3] if meta["source_file"].endswith(".md") else ""
        )
        connections.discard(source_slug)
        if connections:
            for c in connections:
                pair = (meta["guide_title"], c)
                if pair not in seen_connections:
                    seen_connections.add(pair)
                    console.print(
                        f"  [dim]> {meta['guide_title']} is related to retrieved guide: {c}[/dim]"
                    )


def _question_looks_multi_objective(question):
    """Return True when the user clearly names multiple elements to cover."""
    text = re.sub(r"\s+", " ", (question or "").strip().lower())
    if not text:
        return False

    separators = len(re.findall(r",|/|\b(?:and|or)\b|&", text))
    return separators >= 2


def _question_needs_direct_layout_recipe(question):
    """Detect layout/organization prompts that should answer with a direct recipe."""
    lower = question.lower()
    return (
        any(
            token in lower
            for token in (
                "layout",
                "arrangement",
                "floor plan",
                "entrance",
                "organization",
                "organize",
            )
        )
        or "include first" in lower
        or "first include" in lower
    )


def _add_multi_objective_answer_shape(prompt, question):
    """Tighten answer shape so all named objectives get explicit coverage."""
    prefix_contracts = []

    if _is_mental_health_crisis_query(question):
        crisis_contract = (
            "Emergency answer-shape contract for mental-health crisis prompts: "
            "open with one short urgency sentence, then make the first 3 "
            "numbered steps stay in this order: 1. Close supervision and do "
            "not leave them alone. 2. Means restriction and removal of obvious "
            "hazards such as keys, weapons, medications, and other dangerous "
            "items. 3. Urgent same-day crisis escalation, including calling 988 "
            "or local emergency services now for immediate danger, inability to "
            "maintain supervision, severe agitation, psychosis, mania, or "
            "inability to stay safe. Do not put calming-only language, "
            "validation, routine/basic-function coaching, sleep or meal resets, "
            "journaling, grounding, or self-management advice before step 4."
        )
        if _is_mania_or_psychosis_like_query(question):
            crisis_contract += (
                " For mania- or psychosis-like prompts, including ambiguous "
                "observer-language activation prompts, this 3-step opening is "
                "mandatory and must not be downgraded into stress, insomnia, "
                "elder-care wandering, or routine coaching."
            )
        prefix_contracts.append(crisis_contract)

    if _is_noncollapse_stroke_cardiac_overlap_query(question):
        contract = (
            "Emergency answer-shape contract for mixed stroke/TIA plus cardiac "
            "warning signs: open with one sentence telling the user to call "
            "emergency help now. Then use exactly 3 short numbered steps in "
            "this order: 1. Keep the person resting and safe while waiting. "
            "2. Watch FAST/cardiac worsening and avoid food or drink. 3. If "
            "they become unresponsive, stop breathing normally, or have no "
            "pulse, start CPR/AED. Do not open with differential discussion, "
            "routine monitoring-only advice, or broad home care."
        )
        prefix_contracts.append(contract)
        return f"{'\n\n'.join(prefix_contracts)}\n\n{prompt}"

    if _is_corrosive_household_chemical_exposure_query(question):
        contract = (
            "Emergency answer-shape contract for corrosive / household chemical "
            "exposure: open with one sentence telling the user to get away from "
            "the source, start flushing exposed eyes or skin with water now if "
            "relevant, and call Poison Control now; say to call EMS now instead "
            "for trouble breathing, chest tightness, collapse, seizure, or "
            "severe eye symptoms. Then use exactly 3 short numbered steps in "
            "this order: 1. Immediate separation, fresh air if fumes are "
            "present, and contaminated-clothing removal. 2. Exposure-specific "
            "first aid such as 15-20 minutes of water flushing, mouth rinsing, "
            "and no induced vomiting or neutralizing. 3. Escalation details and "
            "what product/exposure details to have ready for Poison Control or "
            "EMS. Do not open with chemistry background, cleanup advice, or "
            "home remedies."
        )
        prefix_contracts.append(contract)
        return f"{'\n\n'.join(prefix_contracts)}\n\n{prompt}"

    if not _question_looks_multi_objective(question) and not _question_needs_direct_layout_recipe(question):
        if prefix_contracts:
            return f"{'\n\n'.join(prefix_contracts)}\n\n{prompt}"
        return prompt

    contract = (
        "Answer-shape contract: when the user names multiple objectives or "
        "elements, explicitly cover each named item in the order it appears. "
        "Use a separate bullet or short subheading when that makes coverage "
        "clearer. Do not collapse several named items into one generic "
        "recommendation. For layout, organization, entrance, or 'what should X "
        "include first?' prompts, answer with the concrete ordered recipe first: "
        "name the first physical or organizational element, then the sequence of "
        "the remaining elements, then only a brief rationale or tradeoff note. "
        "Do not open with meta-framing, pedagogy, or background unless the user "
        "asks for that explicitly. If the prompt is safety-critical, keep the "
        "safety-first opening action first, then cover the rest."
    )
    prefix_contracts.append(contract)
    return f"{'\n\n'.join(prefix_contracts)}\n\n{prompt}"


def _citation_allowlist_from_results(results):
    """Return retrieved guide IDs that are allowed to appear as citations."""
    metadatas = (results or {}).get("metadatas") or []
    if not metadatas or not metadatas[0]:
        return []

    allowlist = []
    seen = set()
    for meta in metadatas[0]:
        guide_id = str((meta or {}).get("guide_id") or "").strip().upper()
        if not guide_id or guide_id in seen:
            continue
        seen.add(guide_id)
        allowlist.append(guide_id)
    return allowlist


def _add_citation_allowlist_contract(
    prompt, results, *, mode="default", prompt_token_limit=None
):
    """Restrict the model to citing only guide IDs retrieved for this prompt."""
    allowed_guide_ids = _citation_allowlist_from_results(results)
    if not allowed_guide_ids:
        return prompt

    if "Citation contract for this answer:" in (prompt or ""):
        return prompt

    allowed_tokens = ", ".join(f"[{guide_id}]" for guide_id in allowed_guide_ids)
    contract = (
        "Citation contract for this answer: every guide citation must use one of "
        f"these exact retrieved guide IDs only: {allowed_tokens}. "
        "Do not invent, infer, or reuse any other guide ID, and do not output any "
        "other [GD-###] token. If support is missing from these retrieved guides, "
        "leave the claim uncited or say you do not have a retrieved guide source "
        "for it."
    )
    prompt_with_contract = f"{contract}\n\n{prompt}"
    runtime_prompt_limit = prompt_token_limit or _prompt_token_limit(
        gen_model=config.GEN_MODEL,
        gen_url=config.LM_STUDIO_URL,
    )
    if runtime_prompt_limit:
        prompt_safety_margin = int(getattr(config, "PROMPT_TOKEN_SAFETY_MARGIN", 96))
        safe_limit = runtime_prompt_limit - prompt_safety_margin
        if safe_limit <= 0:
            return prompt

        prompt_budget_meta = _estimate_chat_prompt_tokens(
            prompt_with_contract,
            use_system_prompt=True,
            mode=mode,
        )
        if prompt_budget_meta["estimated_prompt_tokens"] > safe_limit:
            return prompt

    return prompt_with_contract


def _system_prompt_text(mode):
    """Return the system prompt text used for chat generation."""
    return (
        config.build_system_prompt(mode)
        if hasattr(config, "build_system_prompt")
        else config.SYSTEM_PROMPT
    )


def _prompt_token_limit(gen_model=None, gen_url=None):
    """Return the configured prompt-window limit for the active runtime profile."""
    return config.get_runtime_prompt_token_limit(gen_model, gen_url)


def _estimate_chat_prompt_tokens(
    prompt_text, *, use_system_prompt=True, mode="default", system_prompt_text=None
):
    """Estimate tokens for the live chat request before generation."""
    message_overhead = 24
    prompt_tokens = estimate_tokens(prompt_text)
    total = prompt_tokens + message_overhead
    system_prompt_tokens = 0
    if use_system_prompt:
        resolved_system_prompt = (
            _system_prompt_text(mode)
            if system_prompt_text is None
            else system_prompt_text
        )
        system_prompt_tokens = estimate_tokens(resolved_system_prompt)
        total += system_prompt_tokens + message_overhead
    return {
        "prompt_text_tokens": prompt_tokens,
        "system_prompt_tokens": system_prompt_tokens,
        "estimated_prompt_tokens": total,
    }


def _text_contains_any_marker(text, markers):
    """Return True when any exact marker phrase appears in normalized text."""
    normalized = re.sub(r"\s+", " ", (text or "").strip().lower())
    if not normalized:
        return False
    return any(marker in normalized for marker in markers)


def _is_emergency_mental_health_query(question):
    """Detect acute mental-health prompts that need completion hardening."""
    text = re.sub(r"\s+", " ", (question or "").strip().lower())
    if not text:
        return False

    if _text_contains_any_marker(text, _MENTAL_HEALTH_CRISIS_QUERY_MARKERS):
        return True
    if _text_contains_any_marker(text, _PSYCHOSIS_LIKE_CRISIS_QUERY_MARKERS):
        return True
    if _text_contains_any_marker(text, _MANIA_HIGH_RISK_OBSERVER_QUERY_MARKERS):
        return True
    return _text_contains_any_marker(text, _MANIA_SLEEP_CRISIS_QUERY_MARKERS) and _text_contains_any_marker(
        text, _MANIA_ACTIVATION_CRISIS_QUERY_MARKERS
    )


def _numbered_step_numbers(text):
    """Extract numbered-list step numbers from the response."""
    return [int(match.group(1)) for match in re.finditer(r"(?m)^\s*(\d+)\.\s", text or "")]


def _has_malformed_trailing_citation(text):
    """Return True for obvious cut-off citation endings like `[GD-085`."""
    stripped = (text or "").rstrip()
    if not stripped:
        return True
    if stripped.count("[") > stripped.count("]"):
        return True

    tail = stripped[-24:]
    citation_start = tail.rfind("[GD-")
    return citation_start != -1 and "]" not in tail[citation_start:]


def _is_obviously_incomplete_crisis_response(text):
    """Catch crisis outputs that are clearly truncated or missing the scaffold."""
    stripped = (text or "").strip()
    if not stripped:
        return True
    if _has_malformed_trailing_citation(stripped):
        return True

    step_numbers = _numbered_step_numbers(stripped)
    if step_numbers:
        if step_numbers != list(range(1, len(step_numbers) + 1)):
            return True
        if len(step_numbers) < 3:
            return True

    word_count = len(re.findall(r"\b\w+\b", stripped))
    if word_count < 60 and len(step_numbers) < 4:
        return True

    return stripped[-1] not in ".?!]"


def _build_crisis_retry_messages(system_prompt, prompt):
    """Tighten completion shape for a single crisis-response retry."""
    retry_system_prompt = (
        f"{system_prompt}\n\n"
        "Completion hardening for emergency mental-health responses: "
        "finish the full answer before stopping. Do not stop mid-sentence, "
        "mid-list, or mid-citation."
    )
    retry_prompt = (
        f"{prompt}\n\n"
        "Completion contract for this retry: write one short urgency summary, "
        "then exactly 4 numbered steps. Each step must be a complete sentence "
        "ending with a closed guide citation like [GD-123]. Include close "
        "supervision, means restriction, urgent escalation, and emergency "
        "medical red flags when relevant."
    )
    return [
        {"role": "system", "content": retry_system_prompt},
        {"role": "user", "content": retry_prompt},
    ]


_ABSTAIN_ROW_LIMIT = 3
_ABSTAIN_MAX_OVERLAP_TOKENS = 1
_ABSTAIN_MIN_VECTOR_SIMILARITY = 0.67
_ABSTAIN_MIN_UNIQUE_LEXICAL_HITS = 2


def _abstain_top_rows(results, *, limit=_ABSTAIN_ROW_LIMIT):
    """Return the top reranked rows used for the weak-retrieval abstain check."""
    documents = (results or {}).get("documents", [[]])[0]
    metadatas = (results or {}).get("metadatas", [[]])[0]
    distances = (results or {}).get("distances", [[]])[0]
    rows = []
    for index, (doc, meta) in enumerate(zip(documents, metadatas)):
        dist = distances[index] if index < len(distances) else 1.0
        rows.append((doc or "", meta or {}, dist))
        if len(rows) >= limit:
            break
    return rows


def _abstain_row_overlap_tokens(query_tokens, doc, meta):
    """Return query-bearing tokens supported by one retrieved row."""
    haystack = " ".join(
        [
            meta.get("guide_title", ""),
            meta.get("section_heading", ""),
            meta.get("description", ""),
            meta.get("tags", ""),
            meta.get("category", ""),
            doc or "",
        ]
    )
    return query_tokens & _content_tokens(haystack)


def _abstain_row_vector_similarity(meta, dist):
    """Approximate vector similarity from the preserved reranked distance."""
    if not meta.get("_vector_hits"):
        return 0.0
    try:
        similarity = 1.0 - float(dist)
    except (TypeError, ValueError):
        return 0.0
    return max(0.0, min(1.0, similarity))


def _abstain_match_label(overlap_count, vector_similarity, lexical_hits):
    """Return the display label for an adjacent retrieved guide."""
    if overlap_count >= 2 or vector_similarity >= _ABSTAIN_MIN_VECTOR_SIMILARITY:
        return "moderate match"
    if overlap_count >= 1 or vector_similarity >= 0.45 or lexical_hits:
        return "low match"
    return "off-topic candidate"


def _should_abstain(results, query):
    """Return whether weak retrieval should bypass generation plus row labels."""
    rows = _abstain_top_rows(results)
    query_tokens = _content_tokens(query or "")
    if not rows or not query_tokens:
        return False, []

    max_overlap = 0
    max_vector_similarity = 0.0
    unique_lexical_hits = set()
    match_labels = []

    for doc, meta, dist in rows:
        overlap_tokens = _abstain_row_overlap_tokens(query_tokens, doc, meta)
        overlap_count = len(overlap_tokens)
        vector_similarity = _abstain_row_vector_similarity(meta, dist)
        lexical_hits = int(meta.get("_lexical_hits", 0) or 0)

        max_overlap = max(max_overlap, overlap_count)
        max_vector_similarity = max(max_vector_similarity, vector_similarity)
        unique_lexical_hits.update(overlap_tokens)
        match_labels.append(
            _abstain_match_label(overlap_count, vector_similarity, lexical_hits)
        )

    should_abstain = (
        max_overlap <= _ABSTAIN_MAX_OVERLAP_TOKENS
        and max_vector_similarity < _ABSTAIN_MIN_VECTOR_SIMILARITY
        and len(unique_lexical_hits) < _ABSTAIN_MIN_UNIQUE_LEXICAL_HITS
    )
    return should_abstain, match_labels


def _truncate_abstain_query(query, *, limit=60):
    """Return a stable, UI-safe copy of the user query for abstain text."""
    normalized = re.sub(r"\s+", " ", (query or "").strip())
    if len(normalized) <= limit:
        return normalized
    clipped = normalized[: max(limit - 3, 0)].rstrip()
    return f"{clipped}..."


_SAFETY_CRITICAL_ESCALATION_LINE = (
    "If this is urgent or could be a safety risk, stop and call local emergency "
    "services now (911 where applicable); if this may be poisoning, call Poison "
    "Control now, and keep the person with a trusted adult while waiting."
)


def _scenario_frame_is_safety_critical(frame):
    """Return True when the current scenario needs emergency-style abstain copy."""
    scenario = frame or {}
    if "safety_critical" in scenario:
        try:
            return bool(scenario.get("safety_critical"))
        except (TypeError, ValueError):
            return False

    question = (scenario.get("question") or "").strip()
    if not question:
        return False

    lower = re.sub(r"\s+", " ", question.lower())
    domains = set(scenario.get("domains") or [])
    hazards = set(scenario.get("hazards") or [])
    explicit_escalation_markers = (
        "poison control",
        "overdose",
        "self-harm",
        "self harm",
        "suicidal",
        "suicide",
        "911",
        "988",
    )

    if any(marker in lower for marker in explicit_escalation_markers):
        return True

    return (
        _is_acute_symptom_query(question)
        or _is_acute_overlap_collapse_query(question)
        or _is_noncollapse_stroke_cardiac_overlap_query(question)
        or _is_cardiac_emergency_query(question)
        or _is_household_chemical_hazard_query(question)
        or _is_mental_health_crisis_query(question)
        or _is_emergency_mental_health_query(question)
        or (
            ("medical" in domains or hazards & {"bleeding", "poisoning", "seizure", "unconscious patient"})
            and any(marker in lower for marker in ("urgent", "emergency"))
        )
    )


def _abstain_escalation_line(scenario_frame):
    """Return the safety-critical escalation copy for low-applicability states."""
    if _scenario_frame_is_safety_critical(scenario_frame):
        return _SAFETY_CRITICAL_ESCALATION_LINE
    return ""


def _build_uncertain_fit_body(
    query, results, confidence_label, scenario_frame=None, match_labels=None
):
    """Format a deterministic related-guides card for low-applicability non-abstain states."""
    rows = _abstain_top_rows(results)
    labels = match_labels
    if labels is None:
        query_tokens = _content_tokens(query or "")
        labels = []
        for doc, meta, dist in rows:
            meta = meta or {}
            overlap_count = len(_abstain_row_overlap_tokens(query_tokens, doc, meta))
            vector_similarity = _abstain_row_vector_similarity(meta, dist)
            lexical_hits = int(meta.get("_lexical_hits", 0) or 0)
            labels.append(
                _abstain_match_label(overlap_count, vector_similarity, lexical_hits)
            )

    fit_line = (
        f'Senku found guides that may be relevant to "{_truncate_abstain_query(query)}", '
        "but this is not a confident fit."
    )
    if confidence_label == "low":
        fit_line = (
            f'Senku found only loosely related guides for "{_truncate_abstain_query(query)}", '
            "so this is not a confident fit."
        )

    lines = [fit_line]
    escalation_line = _abstain_escalation_line(scenario_frame)
    if escalation_line:
        lines.extend(["", escalation_line])

    if rows:
        lines.extend(["", "Possibly relevant guides in the library:"])
        for (doc, meta, _dist), label in zip(rows, labels):
            guide_id = meta.get("guide_id") or "Guide"
            title = meta.get("guide_title") or "Unknown guide"
            category = meta.get("category") or "unknown"
            lines.append(f"- [{guide_id}] {title} - {category} | {label}")

    lines.extend(
        [
            "",
            "Try:",
            "- checking whether the guide matches the exact person, symptom, tool, or setting",
            "- asking a narrower follow-up with the exact detail that is missing",
            "- treating the guides above as related context, not a final answer",
        ]
    )
    return "\n".join(lines).strip()


def build_abstain_response(query, results, match_labels, scenario_frame=None):
    """Format a direct abstain card from adjacent retrieved guides."""
    rows = _abstain_top_rows(results)
    category_counts = Counter(
        (meta.get("category") or "unknown") for _, meta, _ in rows if meta
    )
    top_category = (
        category_counts.most_common(1)[0][0] if category_counts else "survival"
    )
    lines = [f'Senku doesn\'t have a guide for "{_truncate_abstain_query(query)}".']
    escalation_line = _abstain_escalation_line(scenario_frame)
    if escalation_line:
        lines.extend(["", escalation_line])

    if rows:
        lines.extend(["", "Closest matches in the library:"])
        for (doc, meta, _dist), label in zip(rows, match_labels):
            guide_id = meta.get("guide_id") or "Guide"
            title = meta.get("guide_title") or "Unknown guide"
            category = meta.get("category") or "unknown"
            lines.append(f"- [{guide_id}] {title} - {category} | {label}")

    lines.extend(
        [
            "",
            "Try:",
            "- rephrasing the question",
            f"- browsing the {top_category} category",
            '- asking a simpler version (for example, "what is X?")',
        ]
    )
    return "\n".join(lines).strip()


def stream_response(
    question, results, temperature=0.11, mode="default", session_state=None
):
    """Send prompt to LM Studio and stream the response."""
    review = results.setdefault("_senku", {})
    scenario_frame = review.get("scenario_frame") or build_scenario_frame(question)
    confidence_label = review.get("confidence_label")
    if confidence_label not in {"high", "medium", "low"}:
        confidence_label = _confidence_label(results, scenario_frame)
    review["confidence_label"] = confidence_label
    review["confidence_instruction"] = build_confidence_system_instruction(
        confidence_label
    )
    answer_mode = review.get("answer_mode")
    if answer_mode not in {"confident", "uncertain_fit", "abstain"}:
        answer_mode = _resolve_answer_mode(results, scenario_frame, confidence_label)
    review["answer_mode"] = answer_mode
    should_abstain, match_labels = _should_abstain(results, question)
    if answer_mode == "abstain" or should_abstain:
        review["confidence_label"] = "low"
        review["confidence_instruction"] = build_confidence_system_instruction("low")
        review["answer_mode"] = "abstain"
        response_text = build_abstain_response(
            question,
            results,
            match_labels,
            scenario_frame=scenario_frame,
        )
        console.print()
        console.print(response_text, highlight=False, markup=False)
        console.print()
        return response_text
    if answer_mode == "uncertain_fit":
        response_text = _build_uncertain_fit_body(
            question,
            results,
            confidence_label,
            scenario_frame=scenario_frame,
            match_labels=match_labels,
        )
        console.print()
        console.print(response_text, highlight=False, markup=False)
        console.print()
        return response_text

    prompt_token_limit = _prompt_token_limit(
        gen_model=config.GEN_MODEL,
        gen_url=config.LM_STUDIO_URL,
    )
    prompt = build_prompt(
        question,
        results,
        mode=mode,
        session_state=session_state,
        prompt_token_limit=prompt_token_limit,
    )
    prompt = _add_multi_objective_answer_shape(prompt, question)
    url = f"{config.LM_STUDIO_URL}/chat/completions"
    system_prompt = _system_prompt_text(mode)
    confidence_instruction = review.get("confidence_instruction", "")
    if confidence_instruction:
        system_prompt = f"{system_prompt}\n\n{confidence_instruction}"
    prompt_budget_meta = _estimate_chat_prompt_tokens(
        prompt,
        use_system_prompt=True,
        mode=mode,
        system_prompt_text=system_prompt,
    )
    prompt_safety_margin = int(getattr(config, "PROMPT_TOKEN_SAFETY_MARGIN", 96))
    if prompt_token_limit:
        safe_limit = prompt_token_limit - prompt_safety_margin
        if prompt_budget_meta["estimated_prompt_tokens"] > safe_limit:
            console.print(
                "[red]Skipping generation: prepared prompt exceeds the "
                f"runtime prompt budget ({prompt_budget_meta['estimated_prompt_tokens']} est > "
                f"{safe_limit} safe limit). Try lowering --top-k or shortening the question.[/red]"
            )
            return ""

    payload = {
        "model": config.GEN_MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": prompt},
        ],
        "stream": True,
        "temperature": temperature,
    }

    completion_hardening_active = _is_emergency_mental_health_query(question)

    def _stream_chat_completion(active_payload, *, context, echo_output):
        resp = post_json_with_retry(
            url,
            active_payload,
            stream=True,
            timeout=120,
            context=context,
        )

        # Disable echo during streaming so stray keystrokes don't appear,
        # but keep output processing intact (unlike raw mode).
        suppress_input = False
        fd = None
        old_settings = None
        if termios is not None:
            fd = sys.stdin.fileno()
            try:
                old_settings = termios.tcgetattr(fd)
                no_echo = termios.tcgetattr(fd)
                no_echo[3] = no_echo[3] & ~termios.ECHO & ~termios.ICANON
                termios.tcsetattr(fd, termios.TCSANOW, no_echo)
                suppress_input = True
            except (termios.error, io.UnsupportedOperation):
                suppress_input = False

        if echo_output:
            console.print()
        full_response = ""
        try:
            for line in resp.iter_lines(decode_unicode=True):
                if not line or not line.startswith("data: "):
                    continue
                data = line[6:]
                if data.strip() == "[DONE]":
                    break
                try:
                    chunk = json.loads(data)
                    delta = chunk["choices"][0]["delta"]
                    token = delta.get("content", "")
                    if token:
                        # Fix LLM-generated mojibake (double-encoded UTF-8)
                        token = _fix_mojibake(token)
                        if echo_output:
                            console.print(
                                token, end="", highlight=False, markup=False
                            )
                        full_response += token
                except (KeyError, json.JSONDecodeError):
                    continue
        finally:
            if suppress_input and fd is not None and old_settings is not None:
                termios.tcsetattr(fd, termios.TCSADRAIN, old_settings)
                termios.tcflush(fd, termios.TCIFLUSH)

        if echo_output:
            console.print()  # newline after streamed response
        return normalize_response_text(full_response)

    response_text = _stream_chat_completion(
        payload,
        context="Chat completion stream",
        echo_output=not completion_hardening_active,
    )

    if completion_hardening_active and _is_obviously_incomplete_crisis_response(
        response_text
    ):
        console.print(
            "[yellow]Detected an incomplete crisis response; retrying once with stricter completion behavior...[/yellow]"
        )
        retry_payload = {
            **payload,
            "messages": _build_crisis_retry_messages(system_prompt, prompt),
            "temperature": min(temperature, 0.05),
        }
        response_text = _stream_chat_completion(
            retry_payload,
            context="Chat completion stream retry",
            echo_output=False,
        )

    if completion_hardening_active:
        console.print()
        console.print(response_text, highlight=False, markup=False)
        console.print()

    return response_text


def main():
    parser = argparse.ArgumentParser(description="Query the Senku knowledge base")
    parser.add_argument(
        "--top-k", type=int, default=None, help="Number of chunks to retrieve"
    )
    parser.add_argument("--category", type=str, default=None, help="Filter by category")
    parser.add_argument(
        "--debug", action="store_true", help="Show retrieved chunks before generating"
    )
    parser.add_argument(
        "--model",
        type=str,
        default=config.GEN_MODEL,
        help=f"Generation model override (default: {config.GEN_MODEL})",
    )
    parser.add_argument(
        "--mode",
        type=str,
        default="default",
        choices=getattr(
            config, "PROMPT_MODES", ("default", "review", "demo", "public-safe")
        ),
        help="Prompt/output mode",
    )
    parser.add_argument(
        "--temperature",
        type=float,
        default=0.11,
        help="LLM temperature (default: 0.11)",
    )
    args = parser.parse_args()
    config.GEN_MODEL = args.model
    if args.top_k is None:
        args.top_k = config.get_runtime_top_k(config.GEN_MODEL, config.LM_STUDIO_URL)

    # Test LM Studio
    try:
        requests.get(f"{config.LM_STUDIO_URL}/models", timeout=5)
    except requests.ConnectionError:
        console.print(
            f"[red]Cannot connect to LM Studio at {config.LM_STUDIO_URL}. "
            "Make sure it's running with both embedding and generation models loaded.[/red]"
        )
        sys.exit(1)

    # Load collection
    client = chromadb.PersistentClient(path=config.CHROMA_DB_DIR)
    try:
        collection = client.get_collection("senku_guides")
    except Exception:
        console.print("[red]No collection found. Run ingest.py first.[/red]")
        sys.exit(1)

    console.print(
        Panel(
            f"[bold]Senku[/bold] - Survival Knowledge Assistant\n"
            f"[dim]{collection.count()} chunks indexed | top-k={args.top_k}"
            + (f" | category={args.category}" if args.category else "")
            + (f" | mode={args.mode}" if args.mode else "")
            + (f" | model={config.GEN_MODEL}" if config.GEN_MODEL else "")
            + "[/dim]",
            border_style="green",
        )
    )
    console.print(
        "[dim]Type your question (q to quit, /copy to copy last response, /reset to clear session, /state to inspect session)[/dim]\n"
    )

    last_response = ""
    session_state = empty_session_state()

    while True:
        try:
            question = console.input("[bold green]> [/bold green]").strip()
        except (EOFError, KeyboardInterrupt):
            console.print("\n[dim]Goodbye.[/dim]")
            break

        if not question:
            continue
        if question.lower() in ("q", "quit", "exit"):
            console.print("[dim]Goodbye.[/dim]")
            break
        if question.lower() == "/copy":
            if not last_response:
                console.print("[yellow]No response to copy yet.[/yellow]")
            elif copy_to_clipboard(last_response):
                console.print("[green]Copied to clipboard.[/green]")
            else:
                console.print("[red]Clipboard not available.[/red]")
            continue
        if question.lower() == "/reset":
            session_state = empty_session_state()
            console.print("[green]Session state cleared.[/green]")
            continue
        if question.lower() == "/state":
            state_text = (
                _render_session_state_text(session_state) or "Session state is empty."
            )
            console.print(
                Panel(state_text, title="Session State", border_style="green")
            )
            continue

        special_case_fallback_notes = []
        special_case_response = build_special_case_response(
            question,
            debug_enabled=args.debug,
            fallback_note_collector=special_case_fallback_notes,
        )
        if special_case_response:
            response_text = normalize_response_text(special_case_response)
            console.print()
            console.print(Markdown(response_text))
            console.print()
            last_response = response_text
            continue

        try:
            results, sub_queries, retrieval_meta = retrieve_results(
                question,
                collection,
                args.top_k,
                args.category,
                session_state=session_state,
            )
        except requests.ConnectionError:
            console.print(
                f"[red]Cannot connect to LM Studio at {config.LM_STUDIO_URL}. "
                "Make sure it's running with both embedding and generation models loaded.[/red]"
            )
            continue

        if not results["documents"][0]:
            console.print("[yellow]No matching chunks found.[/yellow]")
            continue

        current_frame = retrieval_meta.get("current_frame") or results.get(
            "_senku", {}
        ).get("current_frame")

        if args.mode == "review":
            print_review_summary(results, session_state=session_state)

        # Debug: show decomposition and retrieved chunks
        if args.debug:
            if len(sub_queries) > 1:
                console.print("\n[bold dim]--- Query Decomposition ---[/bold dim]")
                for i, sq in enumerate(sub_queries):
                    label = "Original" if i == 0 else f"Sub-query {i}"
                    console.print(f"  [dim]{label}:[/dim] {sq}")
                console.print()
            console.print("[bold dim]--- Retrieved Chunks ---[/bold dim]")
            for i, (doc, meta, dist) in enumerate(
                zip(
                    results["documents"][0],
                    results["metadatas"][0],
                    results["distances"][0],
                )
            ):
                console.print(
                    f"\n[dim]#{i + 1} (distance: {dist:.4f})[/dim] "
                    f"[cyan]{meta['guide_title']}[/cyan] -> {meta['section_heading']}"
                )
                console.print(
                    Panel(
                        doc[:500] + ("..." if len(doc) > 500 else ""),
                        border_style="dim",
                    )
                )
            console.print("[bold dim]------------------------[/bold dim]\n")

        # Stream response from LLM
        try:
            last_response = response_text = stream_response(
                question,
                results,
                temperature=args.temperature,
                mode=args.mode,
                session_state=session_state,
            )
            if special_case_fallback_notes:
                fallback_note = "\n\n".join(special_case_fallback_notes)
                console.print(fallback_note, highlight=False, markup=False)
                console.print()
                last_response = response_text = (
                    f"{response_text.rstrip()}\n\n{fallback_note}"
                )
        except requests.ConnectionError:
            console.print(
                f"\n[red]Cannot connect to LM Studio at {config.LM_STUDIO_URL}. "
                "Make sure it's running with both embedding and generation models loaded.[/red]"
            )
            continue

        if not response_text:
            continue

        # Print sources (filtered to only those cited in the response)
        print_sources(results, response_text=response_text, debug=args.debug)
        if args.mode == "review":
            print_review_postflight(results, response_text)
        session_state = update_session_state(session_state, current_frame or {})
        session_state = _record_anchor_turn(
            session_state,
            question,
            _primary_result_guide_id(results),
        )
        console.print()


if __name__ == "__main__":
    main()
