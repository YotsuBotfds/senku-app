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
from functools import lru_cache
from typing import Literal

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
import query_abstain_policy as _abstain_policy
import query_completion_hardening as _completion_hardening
import query_citation_policy as _citation_policy
import query_answer_card_runtime as _answer_card_runtime
import query_prompt_runtime as _prompt_runtime
import query_response_normalization as _response_normalization
import query_scenario_frame as _scenario_frame_helpers
from confidence_label_contract import resolve_confidence_presentation
from deterministic_special_case_registry import DETERMINISTIC_SPECIAL_CASE_SPECS
from deterministic_special_case_router import (
    DeterministicSpecialCaseRule,
    _build_deterministic_builder_missing_debug_note,
    _lexical_signature_size,
    _passes_deterministic_semantic_gate,
    _resolve_deterministic_special_case_rules as _router_resolve_deterministic_special_case_rules,
    _select_deterministic_special_case_rule as _router_select_deterministic_special_case_rule,
    get_deterministic_special_case_overlaps as _router_get_deterministic_special_case_overlaps,
)
from guide_catalog import all_guide_ids, get_anchor_related_link_weights
try:
    from guide_answer_card_contracts import (
        build_evidence_packet as _build_guide_evidence_packet,
        compose_card_backed_answer as _compose_guide_card_backed_answer,
        load_answer_cards as _load_guide_answer_cards,
    )
except Exception:  # pragma: no cover - optional runtime pilot data.
    _build_guide_evidence_packet = None
    _compose_guide_card_backed_answer = None
    _load_guide_answer_cards = None
from lmstudio_utils import (
    classify_lm_request_error,
    embedding_models_to_try,
    is_retryable_lm_request,
    normalize_lm_studio_url,
    should_try_embedding_fallback,
)
from metadata_helpers import normalize_metadata_tag, normalize_tags
from query_routing_predicates import (
    _ADHESIVE_BINDER_QUERY_MARKERS,
    _ANXIETY_CRISIS_EXPLAIN_CRISIS_MARKERS,
    _ANXIETY_CRISIS_EXPLAIN_QUERY_MARKERS,
    _BUILDING_HABITABILITY_QUERY_MARKERS,
    _BUILDING_HABITABILITY_RISK_MARKERS,
    _CHEMICAL_EXPOSURE_ROUTE_MARKERS,
    _CHEMICAL_EYE_ROUTE_MARKERS,
    _CHEMICAL_INHALATION_ROUTE_MARKERS,
    _is_chemical_spill_sick_exposure_query,
    _CORROSIVE_HOUSEHOLD_CHEMICAL_SOURCE_MARKERS,
    _DRY_MEAT_FISH_CONTAMINATION_HAZARD_MARKERS,
    _DRY_MEAT_FISH_CONTAMINATION_QUERY_MARKERS,
    _FOOD_STORAGE_CONTAINER_QUERY_MARKERS,
    _HOUSEHOLD_CHEMICAL_HAZARD_MARKERS,
    _HOUSEHOLD_CHEMICAL_INHALATION_SOURCE_MARKERS,
    _MAINTENANCE_RECORD_QUERY_MARKERS,
    _MAINTENANCE_RECORD_RECORD_MARKERS,
    _MARKET_SPACE_LAYOUT_QUERY_MARKERS,
    _MARKET_TAX_REVENUE_QUERY_MARKERS,
    _MESSAGE_AUTH_QUERY_MARKERS,
    _ROOF_ACTIVE_RAIN_REPAIR_QUERY_MARKERS,
    _ROOF_ACTIVE_RAIN_REPAIR_RISK_MARKERS,
    _UNKNOWN_LOOSE_CHEMICAL_POWDER_QUERY_FORM_MARKERS,
    _UNKNOWN_LOOSE_CHEMICAL_POWDER_QUERY_HAZARD_MARKERS,
    _UNKNOWN_LOOSE_CHEMICAL_POWDER_QUERY_UNKNOWN_MARKERS,
    _is_active_rain_roof_repair_query,
    _is_adhesive_binder_query,
    _is_anxiety_crisis_explainer_query,
    _is_building_habitability_safety_query,
    _is_canned_fruit_soft_spot_query,
    _is_cooked_rice_power_outage_spoilage_query,
    _is_corrosive_household_chemical_exposure_query,
    _is_dry_meat_fish_contamination_query,
    _is_food_storage_container_query,
    _is_household_chemical_eye_query,
    _is_household_chemical_inhalation_query,
    _is_indoor_combustion_co_smoke_query,
    _is_industrial_chemical_smell_boundary_query,
    _is_maintenance_record_query,
    _is_market_space_layout_query,
    _is_market_tax_revenue_query,
    _is_message_auth_query,
    _is_posted_order_verification_query,
    _is_precursor_feedstock_exposure_boundary_query,
    _is_salt_jars_hot_humid_setup_query,
    _is_simple_courier_note_auth_query,
    _is_stretcher_access_query,
    _is_unknown_chemical_skin_burn_query,
    _is_unknown_leaking_chemical_container_query,
    _is_unknown_loose_chemical_powder_query,
    _is_unlabeled_sealed_drum_safety_triage_query,
)
from query_routing_text import text_has_marker as _text_has_marker
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
RAG_OWNER_RERANK_HINTS_PATH = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "data",
    "rag_owner_rerank_hints.json",
)
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
    lm_studio_url = normalize_lm_studio_url(
        base_url or getattr(config, "EMBED_URL", config.LM_STUDIO_URL)
    )
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
    "blood in stool",
    "bloody stool",
    "shortness of breath",
    "bleed",
    "splinter",
    "infection",
    "infected",
    "fever",
    "swelling",
    "swollen",
    "seizure",
    "choking",
    "vomiting",
    "vomited blood",
    "threw up blood",
    "stool",
    "stools",
    "diarrhea",
    "dehydrated",
    "pain",
    "cramping",
    "hurts",
    "dizzy",
    "faint",
    "fainting",
    "pale",
    "numb",
    "numbness",
    "tingle",
    "tingling",
    "ankle",
    "calf",
    "forearm",
    "fingers",
    "toes",
    "injured",
    "broken",
    "fracture",
    "cough",
    "coughing",
    "sore throat",
    "body aches",
    "rash",
    "rashes",
    "skin irritation",
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
    "face looks droopy",
    "face is droopy",
    "slurred speech",
    "speech is weird",
    "speech sounds weird",
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
    "face looks droopy",
    "face is droopy",
    "slurred speech",
    "speech is weird",
    "speech sounds weird",
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
    "face looks droopy",
    "face is droopy",
    "slurred speech",
    "speech is slurred",
    "speech is weird",
    "speech sounds weird",
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
    "chest feels wrong",
    "wrong in my chest",
    "something feels wrong in my chest",
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

_CARDIAC_FIRST_QUERY_MARKERS = {
    "panic attack or heart attack",
    "heart attack or panic attack",
    "chest pain",
    "chest pressure",
    "chest tightness",
    "pressure in my chest",
    "tightness in my chest",
    "shortness of breath",
    "trouble breathing",
    "difficulty breathing",
    "jaw pain",
    "arm pain",
    "left arm pain",
    "pain in my arm",
    "pain in my jaw",
    "dread",
    "sense of doom",
    "something feels very wrong in my chest",
    "something feels wrong in my chest",
    "exertion",
    "exertional",
    "after exertion",
    "when walking",
}

_CARDIAC_FIRST_POSITIVE_METADATA_MARKERS = {
    "acute coronary",
    "acute-coronary-cardiac-emergencies",
    "heart attack",
    "myocardial infarction",
    "chest pain",
    "chest pressure",
    "angina",
    "cardiac emergency",
    "first aid",
    "emergency response",
    "cpr",
    "aed",
}

_CARDIAC_FIRST_DISTRACTOR_METADATA_MARKERS = {
    "anxiety",
    "panic",
    "stress & daily self-care",
    "sleep hygiene",
    "grief",
    "headache",
    "migraine",
    "menopause",
    "common ailments",
    "routine self-care",
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
    "toxicology-poisoning-response",
    "poison",
    "poison control",
    "chemical exposure",
    "chemical safety",
    "chemical-safety",
    "decontamination",
    "unknown ingestion",
    "household cleaner",
    "household chemical",
    "corrosive",
    "inhaled poisons",
    "eye irrigation",
    "mixed cleaners",
    "incompatible cleaners",
    "bleach ammonia",
    "bleach vinegar",
    "chlorine gas",
    "chloramine",
}

_HOUSEHOLD_CHEMICAL_INHALATION_DISTRACTOR_METADATA_MARKERS = {
    "emergency dental",
    "dental procedures",
    "personal hygiene",
    "grooming",
    "bathing without plumbing",
    "hemorrhoids",
    "food safety",
    "kitchen hygiene",
    "sterilization methods",
    "chemical sterilization",
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

_INDOOR_COMBUSTION_CO_OWNER_METADATA_MARKERS = {
    "smoke inhalation, carbon monoxide",
    "smoke-inhalation-carbon-monoxide-fire-gas-exposure",
    "cookstoves, indoor heating",
    "cookstoves-indoor-heating-safety",
    "toxicology-poisoning-response",
    "carbon monoxide poisoning",
}

_HOT_WATER_HEATING_DISTRACTOR_METADATA_MARKERS = {
    "hot water systems",
    "hot-water-systems-bathing-cleaning",
    "bathing",
    "cleaning & sanitation",
    "cleaning and sanitation",
    "when a heating setup becomes dangerous",
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

_VAGINAL_SYMPTOM_QUERY_MARKERS = {
    "vaginal itching",
    "vaginal itch",
    "itching and discharge",
    "vaginal discharge",
    "discharge",
    "itching",
    "itchy",
    "yeast infection",
    "bv",
    "bacterial vaginosis",
}

_HEMATURIA_QUERY_MARKERS = {
    "blood in the urine",
    "blood in urine",
    "bloody urine",
    "pee blood",
    "peeing blood",
    "blood when i pee",
    "red urine",
    "urine is red",
    "hematuria",
}

_ALLERGEN_EXPOSURE_MARKERS = {
    "bee sting",
    "wasp sting",
    "hornet sting",
    "yellow jacket",
    "sting",
    "stings",
    "stung",
    "peanut",
    "peanuts",
    "tree nut",
    "nuts",
    "shellfish",
    "food",
    "ate",
    "eating",
    "medicine",
    "medication",
    "new medicine",
    "antibiotic",
    "amoxicillin",
    "pain pill",
    "pill",
}

_ANAPHYLAXIS_RED_ZONE_MARKERS = {
    "throat feels tight",
    "throat tight",
    "tight throat",
    "throat closing",
    "throat swelling",
    "tongue swelling",
    "tongue is swelling",
    "tongue are swelling",
    "lips and tongue",
    "lip swelling",
    "lips swelling",
    "mouth swelling",
    "swallowing is getting harder",
    "swallowing getting harder",
    "harder to swallow",
    "trouble swallowing",
    "difficulty swallowing",
    "voice sounds strange",
    "strange voice",
    "breathing feels off",
    "breathing is off",
    "wheezing",
    "wheeze",
    "breathing trouble",
    "trouble breathing",
    "difficulty breathing",
    "shortness of breath",
    "chest tightness",
    "face swelling",
    "facial swelling",
    "blue lips",
    "can barely talk",
    "rescue inhaler is not helping",
    "rescue inhaler not helping",
    "inhaler is not helping",
    "inhaler not helping",
    "dizzy",
    "dizziness",
    "faint",
    "fainting",
    "feeling faint",
    "weakness",
    "vomiting",
    "whole body hives",
}

_UPPER_AIRWAY_SWELLING_DANGER_MARKERS = {
    "harsh noisy breath",
    "harsh noisy breathing",
    "noisy breath",
    "noisy breathing",
    "muffled voice",
    "voice sounds muffled",
    "muffled speech",
    "words sound strange",
    "word sounds strange",
    "harder to talk",
    "hard to talk",
    "trouble talking",
    "hoarse voice",
    "voice sounds hoarse",
    "voice change",
    "upper-airway noise",
    "upper airway noise",
    "sound is more in the throat",
    "sound more in the throat",
    "sound is in the throat",
    "sound in the throat",
    "throat than the chest",
    "throat is closing",
    "throat closing",
    "airway swelling",
    "real airway swelling",
    "throat-tightness",
    "throat tightness",
    "tongue feels thick",
    "tongue feels bigger",
    "thick tongue",
    "blue lips",
    "cannot speak more than a word",
    "can't speak more than a word",
    "cant speak more than a word",
}

_UPPER_AIRWAY_CONTEXT_MARKERS = {
    "what matters first",
    "what do i do first",
    "after exposure",
    "exposure",
    "rescue inhaler",
    "panic hyperventilation",
    "breathing fast after a scare",
    "after a scare",
    "asthma flare",
    "wheezing",
    "wheeze",
    "lip swelling",
    "lips swelling",
    "lip and tongue swelling",
    "lips getting bigger",
    "lip getting bigger",
    "mouth swelling",
    "tongue swelling",
    "tongue feels thick",
    "tongue feels bigger",
    "face swelling",
    "facial swelling",
}

_SMOKE_CHEMICAL_AIRWAY_SOURCE_MARKERS = {
    "smoke",
    "fire",
    "carbon monoxide",
    " co ",
    "bleach",
    "ammonia",
    "cleaner",
    "chemical",
    "solvent",
    "fume",
    "fumes",
    "gas leak",
    "chlorine",
}

_MEDICATION_ALLERGY_MARKERS = {
    "medicine",
    "medication",
    "new medicine",
    "antibiotic",
    "amoxicillin",
    "pill",
    "drug",
    "pain pill",
}

_ALLERGY_SKIN_MARKERS = {
    "hives",
    "urticaria",
    "itchy welts",
    "itchy rash",
    "rash",
}

_ALLERGY_SWELLING_MARKERS = {
    "facial swelling",
    "face swelling",
    "swollen face",
    "lip swelling",
    "lips swelling",
    "lips and tongue",
    "lips and tongue are swelling",
    "tongue swelling",
    "mouth swelling",
}

_SOAP_RASH_QUERY_MARKERS = {
    "soap",
    "new soap",
    "body wash",
    "detergent",
    "cleanser",
}

_BREATHING_FINE_MARKERS = {
    "breathing is fine",
    "breathing fine",
    "can breathe",
    "no trouble breathing",
    "no breathing trouble",
    "breath normally",
}

_COMMON_AILMENTS_COMPLAINT_QUERY_MARKERS = {
    "cough",
    "coughing",
    "sore throat",
    "cold",
    "flu",
    "fever",
    "body aches",
    "rash",
    "rashes",
    "skin irritation",
    "itchy",
    "itching",
    "new soap",
    "mild reaction",
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
}

_COMMON_AILMENTS_GATEWAY_QUERY_MARKERS = {
    "which guide should i start with",
    "what guide should i start with",
    "which guide should i use",
    "what guide should i use",
    "where should i start",
    "start with",
    "home-care first",
    "home care first",
    "rest at home first",
    "watch at home",
    "watch this at home",
    "mild reaction",
    "something urgent",
    "more urgent",
    "urgent help",
    "when should i worry",
    "when to worry",
}

_COMMON_AILMENTS_GATEWAY_METADATA_MARKERS = {
    "common ailments",
    "common-ailments-recognition-care",
    "recognition & basic home care",
    "recognition and basic home care",
    "when something is just a cold",
    "when something is \"just a cold\"",
    "when to seek professional medical care",
    "red flag summary",
}

_MAINTENANCE_RECORD_POSITIVE_METADATA_MARKERS = {
    "basic record-keeping",
    "basic-record-keeping",
    "maintenance logs",
    "repair history",
    "failure logs",
    "repeat failure prevention",
    "lessons learned from breakdowns",
    "consequences of poor record-keeping",
    "design simple record-keeping systems",
}

_MAINTENANCE_RECORD_DISTRACTOR_METADATA_MARKERS = {
    "hydroelectric",
    "seasonal operation",
    "predictive maintenance",
    "records & archives",
    "archives",
    "archival",
    "cataloging",
    "census",
    "vital records",
    "accounting",
    "ledger",
    "tax",
    "trade",
}

_ANXIETY_CRISIS_POSITIVE_METADATA_MARKERS = {
    "crisis red flags",
    "urgent help",
    "thoughts of suicide",
    "thoughts of self-harm",
    "unable to stay safe",
    "988",
    "recognizing mental health crises",
    "professional help",
    "chest pain, fainting",
    "severe trouble breathing",
}

_MARKET_TAX_REVENUE_POSITIVE_METADATA_MARKERS = {
    "taxation & public revenue systems",
    "taxation-revenue-systems",
    "tax assessment",
    "tax collection",
    "revenue collection",
    "public revenue",
    "market fees",
    "tax notices",
    "fairness principles",
    "assessment and collection",
    "public budgeting",
}

_MARKET_SPACE_LAYOUT_POSITIVE_METADATA_MARKERS = {
    "marketplace trade space",
    "marketplace-trade-space-basics",
    "stalls",
    "walking lane",
    "walking lanes",
    "foot traffic",
    "loading edge",
    "blocked corners",
    "inside the market footprint",
    "market steward",
    "stall assignment",
}

_MARKET_SPACE_LAYOUT_DISTRACTOR_METADATA_MARKERS = {
    "taxation",
    "taxation-revenue-systems",
    "public revenue",
    "assessment and collection",
    "community bulletin",
    "community-bulletin-notice-systems",
    "town crier",
    "price-setting",
    "exchange rates",
}

_BUILDING_HABITABILITY_POSITIVE_METADATA_MARKERS = {
    "building inspection and habitability checklist",
    "building-inspection-habitability-checklist",
    "do not enter checklist",
    "entry hazard checklist",
    "pre-entry inspection",
    "visible damage triage",
    "can we go in",
    "can we live here",
    "can we store things here",
    "safe to occupy",
    "soft floor",
    "floor that feels soft or bouncy",
    "what to check from outside first",
    "enter only for short salvage",
}

_BUILDING_HABITABILITY_DISTRACTOR_METADATA_MARKERS = {
    "roof leak emergency repair",
    "temporary fixes versus durable repairs",
    "after the storm",
    "mold prevention",
    "spotting mold",
    "moldy clothes",
    "simple home repairs",
}

_FOCUSED_COMMON_SYMPTOM_METADATA_MARKERS = {
    "cough, cold",
    "cough-cold-sore-throat-home-care",
    "common rashes",
    "common-rashes-skin-irritation",
    "skin irritation",
    "contact dermatitis",
    "seasonal allergies",
    "hay fever",
}

_GI_BLEED_QUERY_MARKERS = {
    "coffee grounds",
    "coffee ground vomit",
    "coffee ground emesis",
    "black tarry stool",
    "black tarry stools",
    "black sticky stool",
    "black sticky stools",
    "black stool",
    "black stools",
    "sticky like tar",
    "tarry",
    "melena",
    "bright red blood with bowel movements",
    "bright red blood in stool",
    "blood with bowel movements",
    "blood in stool",
    "bloody stool",
    "bloody stools",
    "bright red blood in vomit",
    "bright red blood in the vomit",
    "red blood in vomit",
    "red blood in the vomit",
    "blood in vomit",
    "blood in the vomit",
    "bright red vomit",
    "vomit blood",
    "vomiting blood",
    "vomited blood",
    "threw up blood",
    "throwing up blood",
    "dark clots",
    "hematemesis",
}

_GI_BLEED_SHOCK_OR_PAIN_MARKERS = {
    "dizzy",
    "dizziness",
    "weak",
    "weakness",
    "pale",
    "faint",
    "fainting",
    "almost fainted",
    "passed out",
    "passing out",
    "cold clammy",
    "rapid pulse",
    "stomach pain",
    "abdominal pain",
    "belly pain",
    "heavy drinking",
    "drinking",
    "alcohol",
}

_GI_BLEED_POSITIVE_METADATA_MARKERS = {
    "acute abdominal emergencies",
    "acute-abdominal-emergencies",
    "gastrointestinal bleeding",
    "gi bleed",
    "upper gi bleed",
    "hematemesis",
    "melena",
    "coffee ground",
    "vomiting blood",
    "black tarry stool",
    "black stool",
    "bleeding emergencies",
    "surgical abdomen",
}

_GI_BLEED_DISTRACTOR_METADATA_MARKERS = {
    "common ailments",
    "common ailment",
    "common-ailments",
    "food safety",
    "food-safety",
    "food poisoning",
    "gastroenteritis",
    "diarrhea",
    "dehydration",
    "constipation",
    "hemorrhoid",
    "hemorrhoids",
    "nosebleed",
    "nosebleeds",
    "reflux",
    "acid reflux",
    "heartburn",
    "gerd",
    "home sick care",
    "home-sick-care",
    "airflow",
    "ventilation",
}

_SURGICAL_ABDOMEN_EXPLICIT_QUERY_MARKERS = {
    "surgical abdomen",
    "acute abdomen",
    "acute abdominal emergency",
    "acute abdominal emergencies",
    "bowel obstruction",
    "intestinal obstruction",
    "appendicitis",
    "pain is sharp on one side and getting worse",
    "sharp on one side and getting worse",
    "upper belly pain straight through to the back",
    "upper abdominal pain straight through to the back",
    "belly pain straight through to the back",
    "stomach pain straight through to the back",
}

_SURGICAL_ABDOMEN_GUARDING_QUERY_MARKERS = {
    "guarding",
    "rigid belly",
    "rigid abdomen",
    "belly is rigid",
    "abdomen is rigid",
    "hard belly",
    "belly is hard",
    "belly getting hard",
    "belly is getting hard",
    "abdomen is hard",
    "stomach is hard",
    "swollen and hard",
    "board-like",
    "board like",
}

_SURGICAL_ABDOMEN_RLQ_QUERY_MARKERS = {
    "right lower belly",
    "lower right belly",
    "right lower abdomen",
    "lower right abdomen",
    "right lower abdominal",
    "lower right abdominal",
    "rlq",
}

_SURGICAL_ABDOMEN_RLQ_RED_FLAG_MARKERS = {
    "will not walk upright",
    "won't walk upright",
    "wont walk upright",
    "cannot walk upright",
    "can't walk upright",
    "hurts to walk",
    "pain with walking",
    "pain when walking",
    "pain with movement",
    "pain with every bump",
    "fever",
    "nausea",
    "nauseated",
    "vomiting",
    "guarding",
}

_SURGICAL_ABDOMEN_OBSTRUCTION_VOMITING_MARKERS = {
    "vomiting",
    "throwing up",
    "keep vomiting",
    "repeated vomiting",
    "nonstop nausea",
    "green stuff",
    "throwing up green stuff",
    "vomiting foul brown material",
    "foul brown material",
    "cannot keep anything down",
    "can't keep anything down",
    "cant keep anything down",
    "cannot keep fluids down",
    "can't keep fluids down",
}

_SURGICAL_ABDOMEN_OBSTRUCTION_NO_OUTPUT_MARKERS = {
    "nothing is coming out",
    "no stool",
    "no gas",
    "no bowel movement",
    "no bowel movement or gas",
    "not passing stool",
    "not passing gas",
    "cannot pass stool",
    "can't pass stool",
    "cant pass stool",
    "cannot pass gas",
    "can't pass gas",
    "cant pass gas",
    "not pooped",
    "has not pooped",
    "haven't pooped",
    "havent pooped",
    "not farted",
    "has not farted",
    "haven't farted",
    "havent farted",
    "pooped or farted",
}

_SURGICAL_ABDOMEN_DISTENTION_MARKERS = {
    "swollen belly",
    "belly is swollen",
    "swollen abdomen",
    "abdomen is swollen",
    "belly is swelling up",
    "bloating",
    "bloated",
    "distended",
    "distention",
    "hard belly",
    "belly is hard",
    "belly getting hard",
    "belly is getting hard",
    "abdomen is hard",
    "hard abdomen",
    "stomach is hard",
}

_SURGICAL_ABDOMEN_FOCAL_TENDER_MARKERS = {
    "one spot is very tender",
    "one spot very tender",
    "one very tender spot",
    "localized tender",
    "focal tenderness",
    "very tender spot",
}

_SURGICAL_ABDOMEN_POSITIVE_METADATA_MARKERS = {
    "acute abdominal emergencies",
    "acute-abdominal-emergencies",
    "surgical abdomen",
    "bowel obstruction",
    "appendicitis",
    "peritoneal signs",
    "guarding",
    "rigidity",
    "rigid belly",
    "hard or rigid belly",
    "focal peritoneal tenderness",
}

_SURGICAL_ABDOMEN_DISTRACTOR_METADATA_MARKERS = {
    "common ailments",
    "common-ailments",
    "home sick care",
    "home-sick-care",
    "heartburn",
    "reflux",
    "sour stomach",
    "constipation",
    "digestive regularity",
    "hemorrhoid",
    "hemorrhoids",
    "earache",
    "ear care",
    "ear pain",
    "child nutrition",
    "nutrition",
    "back pain",
    "routine back pain",
    "musculoskeletal self-care",
    "gastroenteritis",
    "diarrhea",
    "dehydration",
    "routine constipation",
    "routine stomach",
}

_FOOD_STORAGE_CONTAINER_POSITIVE_METADATA_MARKERS = {
    "food storage packaging",
    "food-storage-packaging",
    "food preservation",
    "food-preservation",
    "storage containers",
    "storage-containers-vessels",
    "fermentation and pickling",
    "fermentation-pickling",
    "salted fish",
    "dried beans",
    "dried herbs",
    "food-grade",
    "sealed container",
    "cool dry",
}

_FOOD_STORAGE_CONTAINER_DISTRACTOR_METADATA_MARKERS = {
    "salt production",
    "salt-production",
    "salt storage",
    "salt purity",
    "warehousing",
    "inventory",
    "hot water",
    "water storage",
    "spices, seasonings",
    "spices-seasonings",
    "herb cultivation",
    "drying & dehydration techniques",
    "drying-dehydration-techniques",
}

_DRY_MEAT_FISH_CONTAMINATION_POSITIVE_METADATA_MARKERS = {
    "drying & dehydration techniques",
    "drying-dehydration-techniques",
    "solar drying",
    "screen",
    "screens",
    "racks",
    "raised rack",
    "cheesecloth",
    "food preservation",
    "food-preservation",
    "smoking & curing meat",
    "smoking-curing-meat",
}

_DRY_MEAT_FISH_CONTAMINATION_DISTRACTOR_METADATA_MARKERS = {
    "traditional women's trades",
    "traditional food preservation heritage",
    "spices",
    "herbs",
    "seed saving",
}

_GYN_EMERGENCY_QUERY_MARKERS = {
    "gynecologic emergency",
    "gynecological emergency",
    "gyn emergency",
    "early pregnancy",
    "early pregnancy bleeding",
    "bleeding in early pregnancy",
    "first trimester",
    "pregnancy bleeding",
    "pregnant and bleeding",
    "pregnancy test",
    "positive test",
    "missed period",
    "missed a period",
    "missed their period",
    "missed her period",
    "missed my period",
    "late period",
    "maybe 6 weeks",
    "6 weeks",
    "miscarriage",
    "might be pregnant",
    "possible pregnancy",
    "could be pregnant",
    "pregnant",
}

_GYN_EMERGENCY_RED_FLAG_MARKERS = {
    "one-sided lower belly pain",
    "one sided lower belly pain",
    "one-sided pelvic pain",
    "one sided pelvic pain",
    "one-sided pain",
    "one sided pain",
    "cramping on one side",
    "sharp on one side",
    "pelvic pain",
    "severe pelvic pain",
    "lower belly pain",
    "belly pain",
    "abdominal pain",
    "shoulder pain",
    "shoulder-tip pain",
    "shoulder tip pain",
    "heavy bleeding",
    "vaginal bleeding",
    "bleeding",
    "dizzy",
    "dizziness",
    "faint",
    "fainting",
    "feeling faint",
    "almost fainted",
    "almost passed out",
    "passed out",
    "getting worse",
    "pale",
}

_GYN_EMERGENCY_POSITIVE_METADATA_MARKERS = {
    "gynecological emergencies",
    "gynecological-emergencies-womens-health",
    "gynecologic emergency",
    "women's health",
    "ectopic pregnancy",
    "early pregnancy bleeding",
    "ovarian torsion",
    "severe hemorrhage",
    "acute abdominal emergencies",
    "acute-abdominal-emergencies",
}

_GYN_EMERGENCY_DISTRACTOR_METADATA_MARKERS = {
    "postpartum",
    "uterine massage",
    "menstrual pain",
    "period cramps",
    "menorrhagia",
    "sti recognition",
    "sexually transmitted",
    "common vaginal infections",
    "vaginal infections",
    "cough",
    "cold",
    "sore throat",
    "heartburn",
    "reflux",
    "hemorrhoid",
    "hemorrhoids",
    "direct pressure",
}

_CRUSH_COMPARTMENT_SOURCE_MARKERS = {
    "crush",
    "crushed",
    "pinned",
    "pinned under",
    "heavy object",
    "under weight",
    "compartment syndrome",
}

_CRUSH_COMPARTMENT_SYMPTOM_MARKERS = {
    "pain is getting worse",
    "pain keeps building",
    "pain out of proportion",
    "out of proportion",
    "swollen tight",
    "swelling fast",
    "skin feels tight",
    "tight shiny",
    "feels hard",
    "calf feels hard",
    "numb",
    "numbness",
    "tingle",
    "tingling",
    "fingers tingle",
    "toes",
    "moves my toes",
    "unbearable",
}

_CRUSH_COMPARTMENT_POSITIVE_METADATA_MARKERS = {
    "crush injuries",
    "crush syndrome",
    "compartment syndrome",
    "shock, bleeding",
    "shock bleeding",
    "trauma stabilization",
    "orthopedics",
    "fracture management",
    "neurovascular assessment",
    "splinting",
    "troubleshooting",
    "complications and when to stop",
}

_CRUSH_COMPARTMENT_DISTRACTOR_METADATA_MARKERS = {
    "foot and nail care",
    "my feet hurt from walking",
    "back pain",
    "musculoskeletal self-care",
    "common ailments",
    "minor conditions",
    "bug bites",
    "sting",
    "itch relief",
    "physical rehabilitation",
    "occupational therapy",
    "prosthetics",
    "diabetic foot",
    "nsaid",
    "ibuprofen",
    "rest",
}

_SEROTONIN_SOURCE_QUERY_MARKERS = {
    "antidepressant",
    "antidepressants",
    "ssri",
    "snri",
    "maoi",
    "serotonin medicine",
    "serotonergic",
    "tramadol",
    "linezolid",
    "cough medicine",
    "cough syrup",
    "dextromethorphan",
    "medicine reaction",
    "medication reaction",
    "changing meds",
    "changed meds",
    "after changing meds",
    "new med",
    "new medication",
}

_SEROTONIN_SYMPTOM_QUERY_MARKERS = {
    "shaking",
    "sweaty",
    "sweating",
    "trembling",
    "tremor",
    "diarrhea",
    "fever",
    "clonus",
    "jerking",
    "legs keep jerking",
    "rigid",
    "rigid muscles",
    "muscles are rigid",
    "very hot",
    "overheated",
    "overheating",
    "twitching",
    "restless",
    "cannot stop moving",
    "can't stop moving",
    "agitated",
    "confusion",
    "confused",
}

_SEROTONIN_POSITIVE_METADATA_MARKERS = {
    "toxicology and poisoning response",
    "toxicology-poisoning-response",
    "toxidromes",
    "toxidromes-field-poisoning",
    "serotonin syndrome",
    "serotonergic",
    "toxidrome",
    "poison control",
    "poisoning",
    "common toxidromes",
    "supportive care and monitoring",
}

_SEROTONIN_DISTRACTOR_METADATA_MARKERS = {
    "common ailments",
    "gastrointestinal illness",
    "diarrhea, vomiting",
    "dehydration",
    "anxiety",
    "stress & daily self-care",
    "panic",
    "menopause",
    "midlife women's health",
    "dementia",
    "elder dementia",
    "routine stability",
    "constipation",
    "allergic reactions",
    "anaphylaxis",
    "pregnant",
    "pregnancy",
    "miscarriage",
    "palliative",
    "end-of-life",
}

_MENINGITIS_RASH_FEVER_MARKERS = {
    "fever",
    "high fever",
    "temperature",
}

_MENINGITIS_RASH_DANGER_MARKERS = {
    "purple rash",
    "purplish rash",
    "dark rash",
    "dark red rash",
    "bruise-like rash",
    "bruiselike rash",
    "non-blanching",
    "nonblanching",
    "does not fade",
    "doesn't fade",
    "little purple dots",
    "purple dots",
    "spots on the legs",
    "spreading purplish rash",
    "petechial",
    "petechiae",
    "purpura",
}

_MENINGITIS_RASH_NEURO_MARKERS = {
    "stiff neck",
    "neck is stiff",
    "rigid neck",
    "neck is rigid",
    "neck stiffness",
    "neck rigidity",
    "bad headache",
    "severe headache",
    "headache",
    "photophobia",
    "light hurts",
    "lights hurt",
    "hurts to look at light",
    "throwing up",
    "vomiting",
    "confusion",
    "confused",
    "acting confused",
    "hard to wake",
    "sleepy",
    "unusual sleepiness",
}

_MENINGITIS_RASH_POSITIVE_METADATA_MARKERS = {
    "sepsis recognition",
    "sepsis-recognition-antibiotic-protocols",
    "meningitis",
    "meningococcemia",
    "meningococcal",
    "non-blanching",
    "nonblanching",
    "petechial",
    "petechiae",
    "purpuric",
    "purple/dark/bruise-like rash",
    "fever with dark, purple",
    "fever + rash",
    "infant & child care",
    "infant-child-care",
    "childhood illness recognition",
    "early warning: sentinel symptoms",
}

_MENINGITIS_RASH_DISTRACTOR_METADATA_MARKERS = {
    "common ailments",
    "common-ailments-recognition-care",
    "minor conditions",
    "bug bites",
    "bug-bites-stings-itch-relief",
    "poison ivy",
    "contact rash",
    "common rashes",
    "common-rashes-skin-irritation",
    "infectious disease management",
    "troubleshooting",
    "antipyretic",
    "fever management",
    "disease surveillance systems",
    "health officer",
    "contact tracing",
    "quarantine",
    "isolation",
    "measles",
}

_SPINAL_INJURY_POSITIVE_METADATA_MARKERS = {
    "orthopedics & fracture management",
    "orthopedics-fractures",
    "fracture identification",
    "spinal precautions",
    "possible spinal injury",
    "may have a spinal injury",
    "do not move neck injury",
    "support head and neck",
    "spine precautions",
    "keep warm without moving spine",
    "first aid & emergency response",
    "first-aid",
    "emergency stabilization",
}

_SPINAL_INJURY_DISTRACTOR_METADATA_MARKERS = {
    "back pain & musculoskeletal self-care",
    "back-pain-musculoskeletal-self-care",
    "common musculoskeletal back pain",
    "simple muscle strain",
    "stiff neck",
    "routine rest",
    "water purification",
    "questionable water",
    "contaminated water",
    "search and rescue",
    "communications",
    "router",
    "offline search",
    "cabin",
    "shelter construction",
}

_AIRWAY_OBSTRUCTION_POSITIVE_METADATA_MARKERS = {
    "first aid & emergency response",
    "first-aid",
    "emergency airway management",
    "emergency-airway-management",
    "choking and airway management",
    "foreign body airway obstruction",
    "airway obstruction",
    "choking",
    "back blows",
    "abdominal thrust",
    "abdominal thrusts",
    "chest thrust",
    "chest thrusts",
    "food bolus",
    "food-bolus",
    "dysphagia",
    "cannot swallow",
    "drooling",
}

_AIRWAY_OBSTRUCTION_DISTRACTOR_METADATA_MARKERS = {
    "unknown ingestion",
    "unknown-ingestion",
    "unknown-ingestion-child-poisoning-triage",
    "swallowed substances",
    "poison control",
    "poisoning",
    "toxicology",
    "food allergy",
    "allergic reaction",
    "allergic reactions",
    "anaphylaxis",
    "routine panic",
    "anxiety",
    "anatomy-basics-body-systems",
    "respiratory anatomy",
}

_NEWBORN_SEPSIS_POSITIVE_METADATA_MARKERS = {
    "sepsis recognition",
    "sepsis-recognition-antibiotic-protocols",
    "infant & child care",
    "infant-child-care",
    "pediatric emergencies",
    "newborn",
    "neonatal",
    "poor feeding",
    "hard to wake",
    "low temperature",
    "fever or low temperature",
    "serious infection",
}

_ABDOMINAL_TRAUMA_POSITIVE_METADATA_MARKERS = {
    "acute abdominal emergencies",
    "acute-abdominal-emergencies",
    "shock bleeding trauma stabilization",
    "shock-bleeding-trauma-stabilization",
    "abdominal trauma",
    "blunt abdominal trauma",
    "handlebar injury",
    "handlebar",
    "left side pain",
    "belly pain after fall",
    "fell and belly pain",
    "solid organ injury",
    "hard belly",
    "rigid belly",
    "internal bleeding",
    "hemorrhage",
}

_INFECTED_WOUND_POSITIVE_METADATA_MARKERS = {
    "wound hygiene",
    "wound hygiene, infection prevention",
    "wound-hygiene",
    "infection prevention",
    "first aid",
    "sepsis recognition",
    "sepsis-recognition-antibiotic-protocols",
    "red streak",
    "red streaks",
    "pus",
    "spreading redness",
    "field sanitation",
}

_INFECTED_WOUND_DISTRACTOR_METADATA_MARKERS = {
    "bug bites",
    "stings",
    "itch relief",
    "poison ivy",
    "contact rash",
    "common rashes",
    "routine skin irritation",
    "routine self-care",
}

_EYE_GLOBE_INJURY_QUERY_MARKERS = {
    "metal chip",
    "grinding",
    "flying debris",
    "high-speed debris",
    "high speed debris",
    "embedded",
    "stuck",
    "still feels stuck",
    "poking out",
    "pull it out",
    "scratched my eye",
    "stick scratched",
    "wood chip",
    "glass shard",
    "vision is darker",
    "darker vision",
    "vision change",
    "vision changes",
    "blurry vision",
    "vision is blurry",
    "see halos",
    "halos",
    "hit in the eye",
    "eye hit",
    "hit by a rock",
    "rock hit",
    "eye injury",
    "hurts to open",
    "hard to open",
}

_EYE_GLOBE_INJURY_POSITIVE_METADATA_MARKERS = {
    "eye injuries",
    "emergency ophthalmology",
    "eye-injuries-emergency-care",
    "penetrating injury",
    "do not remove",
    "embedded",
    "high-speed",
    "poking out",
    "globe injury",
    "blunt trauma",
    "hyphema",
    "eye patching and shield",
    "shield without pressure",
    "vision change",
    "darker vision",
}

_EYE_GLOBE_INJURY_DISTRACTOR_METADATA_MARKERS = {
    "red eye",
    "pink eye",
    "eye irritation home care",
    "eye-irritation-pink-eye-home-care",
    "safe flushing",
    "styes",
    "eyelid bumps",
    "mild red-eye",
    "optics & vision care",
    "optics",
    "direct pressure",
    "hemorrhage control",
    "wound management",
}

_RETINAL_DETACHMENT_EYE_QUERY_MARKERS = {
    "flashes and floaters",
    "floaters and flashes",
    "bright flashes",
    "new shower of floaters",
    "shower of floaters",
    "dark curtain",
    "gray curtain",
    "curtain over one eye",
    "curtain falling over one eye",
    "shadow creeping",
    "side vision",
    "lost part of vision",
    "half my vision went dark",
    "sudden vision loss",
    "sudden loss of vision",
    "vision loss in one eye",
    "loss of vision in one eye",
    "painless sudden vision loss",
}

_RETINAL_DETACHMENT_EYE_POSITIVE_METADATA_MARKERS = {
    "eye injuries",
    "emergency ophthalmology",
    "eye-injuries-emergency-care",
    "sudden vision loss",
    "vision loss is emergency",
    "retinal",
    "retinal detachment",
    "optic nerve",
    "optics & vision care",
    "optics-vision",
    "flashes",
    "floaters",
    "dark curtain",
}

_RETINAL_DETACHMENT_EYE_DISTRACTOR_METADATA_MARKERS = {
    "red eye",
    "pink eye",
    "eye irritation",
    "eye-irritation-pink-eye-home-care",
    "safe flushing",
    "styes",
    "eyelid bumps",
    "vision correction",
    "vision-correction-optometry",
    "glasses",
    "routine eye exam",
    "headaches",
    "headaches-basic-care",
    "migraine",
    "astronomy",
    "night sky",
    "observation skills",
    "navigation",
    "signaling",
    "fire",
}

_ELECTRICAL_HAZARD_QUERY_MARKERS = {
    "shocked",
    "electric shock",
    "electrical shock",
    "cannot let go",
    "can't let go",
    "collapsed near electrical",
    "exposed live wire",
    "live wire",
    "downed power line",
    "downed line",
    "wire across driveway",
    "sparking outlet",
    "outlet sparked",
    "wet outlet",
    "wet outlets",
    "outlet got wet",
    "outlets got wet",
    "water through outlet",
    "water through outlets",
    "water through light fixture",
    "water through fixtures",
    "water near outlet",
    "water near outlets",
    "standing water near panel",
    "standing water near breaker",
    "standing water near breaker box",
    "electrical damage inside",
    "electrical hazard before roof repair",
    "wet breaker box",
    "breaker box after flood",
    "before touching anything",
}

_DOWNED_POWER_LINE_QUERY_MARKERS = {
    "downed power line",
    "downed line",
    "power line across",
    "wire across driveway",
    "line across driveway",
}

_ELECTRICAL_HAZARD_POSITIVE_METADATA_MARKERS = {
    "electrical safety",
    "electrical-safety-hazard-prevention",
    "electrical shock",
    "electric shock",
    "live wire",
    "downed power line",
    "wet breaker",
    "wet outlet",
    "outlet got wet",
    "outlets got wet",
    "water through outlet",
    "water through light fixture",
    "standing water near panel",
    "electrical damage inside",
    "sparking outlet",
    "de-energize",
    "do not touch",
    "cpr",
    "aed",
}

_ELECTRICAL_HAZARD_DISTRACTOR_METADATA_MARKERS = {
    "home repair",
    "simple home repairs",
    "storm damage",
    "roof leak emergency repair",
    "roof patch",
    "patch roof",
    "seismic",
    "earthquake",
    "wiring project",
    "test functionality",
    "appliance repair",
}

_DROWNING_COLD_WATER_QUERY_MARKERS = {
    "drowning right now",
    "someone is drowning",
    "face down",
    "silent in the water",
    "motionless in the water",
    "went under water",
    "underwater",
    "cold-water rescue",
    "cold water rescue",
    "gasping hard",
    "pulled from the water",
    "after being pulled from the water",
    "after water rescue",
    "water rescue",
    "coughing and short of breath",
    "fell through ice",
    "went under the ice",
    "under ice",
    "ice rescue",
}

_POST_RESCUE_DROWNING_BREATHING_MARKERS = {
    "pulled from the water",
    "after being pulled from the water",
    "after water rescue",
    "water rescue",
    "coughing",
    "short of breath",
    "chest pain",
    "confusion",
}

_DROWNING_COLD_WATER_POSITIVE_METADATA_MARKERS = {
    "drowning prevention",
    "drowning-prevention-water-safety",
    "cold water survival",
    "cold-water-survival",
    "reach throw row go",
    "reach-throw",
    "rescue priorities",
    "post-rescue",
    "ice rescue",
    "hypothermia",
    "rescue breathing",
    "cpr",
}

_DROWNING_COLD_WATER_DISTRACTOR_METADATA_MARKERS = {
    "drowning red flags",
    "headache",
    "home sick care",
    "general hypothermia",
    "boat building",
    "water storage",
}

_NOSEBLEED_URGENT_QUERY_MARKERS = {
    "nosebleed",
    "nosebleeds",
    "blood from my nose",
    "bleeding from nose",
    "lean forward",
}

_NOSEBLEED_URGENT_RED_FLAG_MARKERS = {
    "urgent help",
    "get urgent help",
    "urgent medical help",
    "will not stop",
    "won't stop",
    "wont stop",
    "20 minutes",
    "30 minutes",
    "pouring",
    "down throat",
    "blood thinners",
    "blood thinner",
    "warfarin",
    "anticoagulant",
    "dizzy",
    "pale",
    "weak",
    "faint",
    "repeated heavy",
    "same day",
}

_NOSEBLEED_URGENT_POSITIVE_METADATA_MARKERS = {
    "nosebleeds",
    "nosebleeds-basic-care",
    "epistaxis",
    "blood down throat",
    "blood thinners",
    "repeated heavy nosebleeds",
    "urgent medical attention",
    "lean forward",
    "firm pressure",
}

_NOSEBLEED_URGENT_DISTRACTOR_METADATA_MARKERS = {
    "emergency dental",
    "tooth",
    "headache",
    "migraine",
    "drowning",
    "rectal",
    "gi bleed",
    "hemorrhoid",
}

_MAJOR_BLOOD_LOSS_SHOCK_BLOOD_MARKERS = {
    "losing blood",
    "lost blood",
    "blood loss",
    "after losing blood",
    "after blood loss",
    "bled a lot",
    "lost a lot of blood",
    "bleeding slowed",
    "bleeding slowed but",
    "heavy bleeding",
    "severe bleeding",
    "uncontrolled bleeding",
}

_MAJOR_BLOOD_LOSS_SHOCK_MARKERS = {
    "shock",
    "pale",
    "dizzy",
    "dizziness",
    "weak",
    "faint",
    "fainting",
    "clammy",
    "cold skin",
    "confused",
    "confusion",
    "altered mental",
    "rapid pulse",
}

_MAJOR_BLOOD_LOSS_SHOCK_POSITIVE_METADATA_MARKERS = {
    "trauma hemorrhage control",
    "trauma-hemorrhage-control",
    "shock-bleeding-trauma-stabilization",
    "shock recognition",
    "shock-recognition-resuscitation",
    "hemorrhagic shock",
    "hemorrhage control",
    "blood loss",
    "tourniquet",
    "wound packing",
    "shock management",
}

_MAJOR_BLOOD_LOSS_SHOCK_DISTRACTOR_METADATA_MARKERS = {
    "nosebleed",
    "nosebleeds",
    "nosebleeds-basic-care",
    "epistaxis",
    "lean forward",
    "pinch the soft",
    "hemorrhoid",
    "hemorrhoids",
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

_VAGINAL_SYMPTOM_METADATA_MARKERS = {
    "common vaginal infections",
    "vaginal infections",
    "vaginal itching",
    "vaginal discharge",
    "itching & discharge",
    "itching and discharge",
    "yeast infection",
    "vaginitis",
    "bacterial vaginosis",
    "reproductive-health",
    "gynecological",
    "sti",
}

_HEMATURIA_DISTRACTOR_METADATA_MARKERS = {
    "cough",
    "cold",
    "sore throat",
    "asthma",
    "respiratory",
    "nosebleed",
    "nosebleeds",
    "rectal",
    "hemorrhoid",
    "hemorrhoids",
    "gi bleed",
    "stool",
    "microscopy",
    "magnification",
    "medical diagnostics",
    "sti recognition",
    "sexually transmitted",
    "urgent red flags",
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
    "dog bit",
    "cat bite",
    "cat bit",
    "bite on",
    "bite wound",
    "deep bite",
    "punctured deeply",
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

_ADHESIVE_BINDER_POSITIVE_METADATA_MARKERS = {
    "adhesives binders formulation",
    "adhesives-binders-formulation",
    "glue adhesives",
    "glue-adhesives",
    "adhesive selection",
    "binder families",
    "hide glue",
    "casein",
    "starch paste",
    "pine pitch",
}

_ADHESIVE_BINDER_DISTRACTOR_METADATA_MARKERS = {
    "soap",
    "bleach",
    "fuel",
    "dye",
    "chemical exposure",
}

_MESSAGE_AUTH_POSITIVE_METADATA_MARKERS = {
    "message authentication",
    "message-authentication-courier",
    "courier protocols",
    "challenge-response",
    "challenge response",
    "wax seals",
    "tamper-evident",
    "tamper evidence",
    "chain of custody",
    "verify a notice",
    "posted orders",
}

_MESSAGE_AUTH_DISTRACTOR_METADATA_MARKERS = {
    "wildfire",
    "evacuation planning",
    "emergency dental",
    "essential medications",
    "water purification",
    "forensic investigation",
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

_RUNOFF_INFANT_FORMULA_INFANT_MARKERS = {
    "baby",
    "infant",
    "newborn",
    "formula",
    "baby formula",
    "infant formula",
}

_RUNOFF_INFANT_FORMULA_WATER_MARKERS = {
    "roof runoff",
    "rainwater",
    "rain barrel",
    "clean barrel",
    "barrel",
    "flood-contaminated",
    "flood contaminated",
    "flood-affected well",
    "flood affected well",
    "flooded well",
    "well may be flood",
    "well is flood",
}

_RUNOFF_INFANT_FORMULA_UNCERTAINTY_MARKERS = {
    "no test kit",
    "without a test kit",
    "cant test",
    "can't test",
    "cannot test",
    "may be contaminated",
    "might be contaminated",
    "flood-contaminated",
    "flood contaminated",
    "flood-affected",
    "flood affected",
}

_RUNOFF_INFANT_FORMULA_USE_MARKERS = {
    "boil",
    "boiling",
    "make safe",
    "safe to use",
    "use it",
    "drink",
    "tonight",
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
    "bad tooth pain",
    "tooth pain",
    "tooth is infected",
    "tooth infection",
    "tooth abscess",
    "dental abscess",
    "abscess",
    "from a tooth",
    "dental",
}

_FACIAL_SWELLING_MARKERS = {
    "face is swelling",
    "face swelling",
    "jaw swelling",
    "swelling under the jaw",
    "cheek swelling",
    "mouth is swelling",
    "mouth swelling",
    "tongue feels pushed up",
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

_SEIZURE_RED_FLAG_SPECIAL_CASE_MARKERS = {
    "5 minutes",
    "6 minutes",
    "more than 5 minutes",
    "over 5 minutes",
    "back to back",
    "never fully woke",
    "never fully woke up",
    "without waking",
    "without waking up",
    "did not wake up",
    "didn't wake up",
    "confused and sleepy",
    "sleepy after the seizure",
    "confused after the seizure",
    "alcohol withdrawal",
    "head injury",
}

_SEIZURE_SPECIAL_CASE_EXCLUSION_MARKERS = {
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
}

_GENERIC_SEVERE_BURN_SPECIAL_CASE_MARKERS = {
    "burned badly",
    "badly burned",
    "severe burn",
    "serious burn",
    "needs a burn center",
    "need a burn center",
    "burn center",
    "needs a clinic",
    "need a clinic",
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
    "someone who hurt me",
    "someone at home is hurting them",
    "person they live with",
    "sexually assaulted",
    "sexual assault",
    "assaulted",
    "abuser",
}

_ABUSE_IMMEDIATE_SAFETY_DANGER_MARKERS = {
    "won't let me leave",
    "wont let me leave",
    "try to leave",
    "try to go",
    "stands in front of the door",
    "hides my keys",
    "keeps taking my phone",
    "taking my phone",
    "scared to use my phone",
    "scared to tell anyone",
    "tracking device",
    "tracking my phone",
    "they are tracking my phone",
    "seems to know where i am",
    "kill themselves if i leave",
    "will kill themselves if i leave",
    "they will kill themselves",
    "threatens suicide if i leave",
    "have to go back tonight",
    "not safe going back",
    "in the house right now",
    "in the next room right now",
    "bleeding",
    "pain and bleeding",
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
    panic_only = "panic attack" in lower and not _text_has_marker(
        lower,
        {
            "suicide",
            "self-harm",
            "self harm",
            "can't stay safe",
            "cannot stay safe",
            "hearing voices",
            "hallucination",
            "hallucinations",
            "psychosis",
            "paranoid",
            "paranoia",
            "not acting normal",
            "unsafe choices",
            "won't stop moving",
            "will not stop moving",
        },
    )
    if panic_only and _is_cardiac_first_query(question):
        return False
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
    cardiac_first = _is_cardiac_first_query(question)
    has_collapse_or_unresponsive = any(
        marker in lower for marker in _COLLAPSE_UNRESPONSIVE_MARKERS
    )
    has_stroke_or_neuro_signs = _has_stroke_tia_routing_signal(question)
    has_direct_cardiac_terms = any(
        marker in lower for marker in _CARDIAC_EMERGENCY_MARKERS
    )
    return (_is_human_medical_query(question) or cardiac_first) and (
        cardiac_first
        or
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


def _is_cardiac_first_query(question):
    """Detect chest-symptom/panic-overlap prompts that must stay cardiac-first."""
    lower = question.lower()
    has_cardiac_marker = _text_has_marker(lower, _CARDIAC_FIRST_QUERY_MARKERS)
    if "panic" in lower and _text_has_marker(lower, {"heart attack", "chest"}):
        return True
    return has_cardiac_marker and (
        "chest" in lower
        or "heart attack" in lower
        or "jaw pain" in lower
        or "arm pain" in lower
        or "shortness of breath" in lower
        or "exertion" in lower
        or "dread" in lower
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
    return (
        _text_has_marker(lower, _HOUSEHOLD_CHEMICAL_HAZARD_MARKERS)
        or has_corrosive_exposure
        or _is_unknown_chemical_skin_burn_query(question)
    )


def _is_urinary_query(question):
    """Detect urinary complaint-first prompts that need stronger medical routing."""
    return _text_has_marker(question, _URINARY_QUERY_MARKERS)


def _is_urinary_vaginal_overlap_query(question):
    """Detect urinary complaints mixed with vaginal itching/discharge symptoms."""
    lower = question.lower()
    return _is_urinary_query(lower) and _text_has_marker(
        lower, _VAGINAL_SYMPTOM_QUERY_MARKERS
    )


def _is_hematuria_query(question):
    """Detect visible-blood-in-urine prompts as a urinary red-flag lane."""
    lower = question.lower()
    return _is_urinary_query(lower) and _text_has_marker(
        lower, _HEMATURIA_QUERY_MARKERS
    )


def _is_anaphylaxis_red_zone_special_case(question):
    """Detect allergen-linked airway, breathing, or circulation red flags."""
    lower = question.lower()
    if _text_has_marker(
        lower,
        {
            "only nausea and no swelling or breathing trouble",
            "no swelling or breathing trouble",
            "no swelling and no breathing trouble",
            "no breathing trouble or swelling",
            "no breathing trouble and no swelling",
        },
    ):
        return False
    has_allergen = _text_has_marker(lower, _ALLERGEN_EXPOSURE_MARKERS)
    has_red_zone = _text_has_marker(lower, _ANAPHYLAXIS_RED_ZONE_MARKERS)
    if has_allergen and has_red_zone:
        return True

    has_skin_swelling_airway_overlap = (
        _text_has_marker(lower, _ALLERGY_SKIN_MARKERS)
        and _text_has_marker(lower, _ALLERGY_SWELLING_MARKERS)
        and _text_has_marker(
            lower,
            {
                "breathing trouble",
                "trouble breathing",
                "difficulty breathing",
                "wheezing",
                "wheeze",
                "can barely talk",
                "blue lips",
            },
        )
    )
    if has_skin_swelling_airway_overlap:
        return True

    has_explicit_anaphylaxis_overlap = "anaphylaxis" in lower and _text_has_marker(
        lower,
        {
            "asthma",
            "panic",
            "wheezing",
            "wheeze",
            "breathing trouble",
            "trouble breathing",
            "rescue inhaler",
            "inhaler",
        },
    )
    if has_explicit_anaphylaxis_overlap:
        return True

    has_explicit_allergic_reaction_overlap = _text_has_marker(
        lower, {"allergic reaction", "allergy reaction"}
    ) and _text_has_marker(
        lower,
        {
            "wheezing",
            "wheeze",
            "throat tight",
            "throat feels tight",
            "breathing trouble",
            "trouble breathing",
            "lip",
            "tongue",
            "swelling",
            "first dose",
        },
    )
    if has_explicit_allergic_reaction_overlap:
        return True

    return _text_has_marker(
        lower,
        {
            "throat swelling or just an asthma flare",
            "throat swelling or asthma",
            "throat swelling versus asthma",
            "throat swelling vs asthma",
        },
    )


def _is_upper_airway_swelling_danger_special_case(question):
    """Detect noisy upper-airway, throat-closing, or swelling-vs-panic/asthma prompts."""
    lower = question.lower()
    if _text_has_marker(lower, _SMOKE_CHEMICAL_AIRWAY_SOURCE_MARKERS):
        return False
    return _text_has_marker(
        lower, _UPPER_AIRWAY_SWELLING_DANGER_MARKERS
    ) and _text_has_marker(lower, _UPPER_AIRWAY_CONTEXT_MARKERS)


def _is_facial_swelling_anxiety_screen_special_case(question):
    """Detect face swelling framed as anxiety while breathing is currently normal."""
    lower = question.lower()
    return (
        _text_has_marker(lower, {"face swelling", "facial swelling", "face is swelling", "swollen face"})
        and _text_has_marker(lower, {"anxiety", "panic", "just anxiety", "routine anxiety"})
        and _text_has_marker(lower, {"breathing is still okay", "breathing still okay", "breathing is okay", "breathing okay", "breathing is fine", "can breathe"})
    )


def _is_medication_allergy_swelling_special_case(question):
    """Detect medication-linked face/lip/tongue swelling prompts."""
    lower = question.lower()
    return (
        _text_has_marker(lower, _MEDICATION_ALLERGY_MARKERS)
        and _text_has_marker(lower, _ALLERGY_SWELLING_MARKERS)
    )


def _is_medicine_hives_skin_only_special_case(question):
    """Detect hives after a medicine without anaphylaxis red-zone wording."""
    lower = question.lower()
    return (
        _text_has_marker(lower, _MEDICATION_ALLERGY_MARKERS)
        and _text_has_marker(lower, _ALLERGY_SKIN_MARKERS)
        and not _text_has_marker(lower, _ANAPHYLAXIS_RED_ZONE_MARKERS)
    )


def _is_soap_rash_breathing_fine_special_case(question):
    """Detect skin-only soap/contact-rash prompts with breathing explicitly normal."""
    lower = question.lower()
    return (
        _text_has_marker(lower, _SOAP_RASH_QUERY_MARKERS)
        and _text_has_marker(lower, _ALLERGY_SKIN_MARKERS)
        and _text_has_marker(lower, _BREATHING_FINE_MARKERS)
        and not _text_has_marker(lower, _ANAPHYLAXIS_RED_ZONE_MARKERS)
    )


def _is_common_ailments_gateway_query(question):
    """Detect broad mild-vs-urgent symptom prompts that should start at common ailments."""
    lower = question.lower()
    if _is_acute_symptom_query(question):
        return False
    return _text_has_marker(
        lower, _COMMON_AILMENTS_COMPLAINT_QUERY_MARKERS
    ) and _text_has_marker(lower, _COMMON_AILMENTS_GATEWAY_QUERY_MARKERS)


def _is_gi_bleed_emergency_query(question):
    """Detect GI-bleed presentations that should not route to routine GI or visible-bleed care."""
    lower = question.lower()
    if _text_has_marker(lower, _GI_BLEED_QUERY_MARKERS):
        return True
    if (
        "dangerous bleeding" in lower
        and _text_has_marker(lower, {"hemorrhoids", "hemorrhoid", "reflux"})
    ):
        return True
    if (
        "bleed" in lower
        and _text_has_marker(
            lower,
            {
                "minor stomach issue",
                "stomach issue",
                "minor stomach",
                "minor gi issue",
                "routine stomach",
            },
        )
    ):
        return True
    return (
        ("black" in lower and ("sticky" in lower or "tar" in lower))
        or (
            "bright red blood" in lower
            and _text_has_marker(lower, {"bowel movement", "bowel movements", "stool"})
            and _text_has_marker(lower, _GI_BLEED_SHOCK_OR_PAIN_MARKERS)
        )
    )


def _is_surgical_abdomen_emergency_query(question):
    """Detect red-zone abdominal patterns that should route to acute abdomen ownership."""
    lower = question.lower()
    if _is_gi_bleed_emergency_query(question) or _is_gyn_emergency_query(question):
        return False
    if _text_has_marker(lower, _SURGICAL_ABDOMEN_EXPLICIT_QUERY_MARKERS):
        return True
    has_belly_context = _text_has_marker(
        lower, {"belly", "abdomen", "abdominal", "stomach"}
    )
    if _text_has_marker(
        lower,
        {
            "no hard belly or guarding",
            "no hard belly and no guarding",
            "without hard belly or guarding",
            "without hard belly and without guarding",
        },
    ) or (
        _text_has_marker(lower, {"no hard belly", "without hard belly", "no rigid belly"})
        and _text_has_marker(lower, {"no guarding", "without guarding"})
    ):
        return False
    if has_belly_context and _text_has_marker(
        lower, _SURGICAL_ABDOMEN_GUARDING_QUERY_MARKERS
    ):
        return True
    if _text_has_marker(lower, _SURGICAL_ABDOMEN_RLQ_QUERY_MARKERS) and _text_has_marker(
        lower, _SURGICAL_ABDOMEN_RLQ_RED_FLAG_MARKERS
    ):
        return True
    if (
        _text_has_marker(lower, _SURGICAL_ABDOMEN_OBSTRUCTION_VOMITING_MARKERS)
        and _text_has_marker(lower, _SURGICAL_ABDOMEN_OBSTRUCTION_NO_OUTPUT_MARKERS)
        and _text_has_marker(lower, _SURGICAL_ABDOMEN_DISTENTION_MARKERS)
    ):
        return True
    if _text_has_marker(
        lower,
        {"foul brown material", "foul brown vomit", "feculent", "fecal vomiting"},
    ) and _text_has_marker(lower, _SURGICAL_ABDOMEN_OBSTRUCTION_NO_OUTPUT_MARKERS):
        return True
    if _text_has_marker(lower, {"blockage", "obstruction"}) and _text_has_marker(
        lower, _SURGICAL_ABDOMEN_OBSTRUCTION_NO_OUTPUT_MARKERS
    ) and _text_has_marker(lower, {"pain comes in waves", "cramping", "severe cramping", "bloating", "bloated"}):
        return True
    if has_belly_context and _text_has_marker(
        lower, {"had surgery before", "prior surgery", "previous surgery"}
    ) and _text_has_marker(
        lower, _SURGICAL_ABDOMEN_OBSTRUCTION_NO_OUTPUT_MARKERS
    ) and _text_has_marker(lower, {"swollen abdomen", "abdomen is swollen", "nonstop nausea", "vomiting"}):
        return True
    if has_belly_context and _text_has_marker(
        lower, {"severe stomach pain", "severe abdominal pain", "severe belly pain"}
    ) and _text_has_marker(
        lower, _SURGICAL_ABDOMEN_OBSTRUCTION_VOMITING_MARKERS
    ) and _text_has_marker(lower, {"bloating", "bloated", "distention", "distended"}):
        return True
    if has_belly_context and _text_has_marker(
        lower, {"severe abdominal pain with fainting", "severe belly pain with fainting", "severe stomach pain with fainting"}
    ):
        return True
    if _text_has_marker(
        lower,
        {
            "stomach pain plus passing out",
            "belly pain plus passing out",
            "abdominal pain plus passing out",
            "stomach pain and passing out",
            "belly pain and passing out",
            "abdominal pain and passing out",
        },
    ):
        return True
    if _text_has_marker(
        lower,
        {
            "pain started suddenly after eating and keeps getting worse",
            "pain started suddenly after eating and is getting worse",
            "sudden pain after eating and keeps getting worse",
        },
    ):
        return True
    if has_belly_context and _text_has_marker(lower, {"severe cramping", "cramping"}) and _text_has_marker(
        lower, {"bloating", "bloated", "distention", "distended"}
    ) and _text_has_marker(lower, _SURGICAL_ABDOMEN_OBSTRUCTION_VOMITING_MARKERS):
        return True
    return (
        "fever" in lower
        and has_belly_context
        and _text_has_marker(lower, _SURGICAL_ABDOMEN_FOCAL_TENDER_MARKERS)
    )


def _is_electrical_hazard_query(question):
    """Detect electrical danger prompts that should route to hazard-first guidance."""
    lower = question.lower()
    return _text_has_marker(lower, _ELECTRICAL_HAZARD_QUERY_MARKERS)


def _is_downed_power_line_query(question):
    """Detect downed-line prompts that require no-approach utility response."""
    lower = question.lower()
    return _text_has_marker(lower, _DOWNED_POWER_LINE_QUERY_MARKERS)


def _is_drowning_cold_water_query(question):
    """Detect active drowning/cold-water/ice rescue prompts."""
    lower = question.lower()
    return _text_has_marker(lower, _DROWNING_COLD_WATER_QUERY_MARKERS)


def _is_post_rescue_drowning_breathing_query(question):
    """Detect delayed breathing symptoms after water rescue."""
    lower = question.lower()
    return (
        _text_has_marker(lower, {"pulled from the water", "after water rescue", "water rescue"})
        and _text_has_marker(lower, {"coughing", "short of breath", "chest pain", "confusion"})
    )


def _is_post_rescue_drowning_breathing_special_case(question):
    """Detect after-rescue drowning breathing problems that need deterministic escalation."""
    lower = question.lower()
    has_after_rescue_context = _text_has_marker(
        lower,
        {
            "pulled from the water",
            "pulled them from the water",
            "pulled him from the water",
            "pulled her from the water",
            "after being pulled from the water",
            "after water rescue",
            "water rescue",
            "rescued from the water",
            "being rescued from the water",
            "cold-water rescue",
            "cold water rescue",
            "submersion incident",
            "submersion",
            "inhaled water",
            "inhaling water",
            "water inhalation",
            "at the pool",
        },
    )
    has_breathing_problem = _text_has_marker(
        lower,
        {
            "not breathing normally",
            "not breathing",
            "abnormal breathing",
            "breathing is abnormal",
            "coughing",
            "cough",
            "cough got worse",
            "keep coughing",
            "short of breath",
            "breathing trouble",
            "chest pain",
            "confusion",
            "sleepy",
            "sleepiness",
            "lethargic",
            "worsening breathing",
            "looked fine",
            "feel fine",
            "seem okay",
            "seems okay",
        },
    )
    return has_after_rescue_context and has_breathing_problem


def _is_active_drowning_rescue_special_case(question):
    """Detect active drowning/cold-water/ice rescue prompts before post-rescue care."""
    return _is_drowning_cold_water_query(
        question
    ) and not _is_post_rescue_drowning_breathing_special_case(question)


def _is_urgent_nosebleed_query(question):
    """Detect nosebleeds with urgent red flags."""
    lower = question.lower()
    return _text_has_marker(lower, _NOSEBLEED_URGENT_QUERY_MARKERS) and (
        _text_has_marker(lower, _NOSEBLEED_URGENT_RED_FLAG_MARKERS)
    )


def _is_major_blood_loss_shock_query(question):
    """Detect blood-loss shock prompts that should not route to nosebleed care."""
    lower = question.lower()
    if _is_urgent_nosebleed_query(lower):
        return False
    has_blood_loss = _text_has_marker(lower, _MAJOR_BLOOD_LOSS_SHOCK_BLOOD_MARKERS)
    has_shock = _text_has_marker(lower, _MAJOR_BLOOD_LOSS_SHOCK_MARKERS)
    return has_blood_loss and has_shock


def _is_gyn_emergency_query(question):
    """Detect gynecologic or early-pregnancy red flags that need emergency ownership."""
    lower = question.lower()
    if _text_has_marker(
        lower, {"gynecologic emergency", "gynecological emergency", "gyn emergency"}
    ):
        return True
    if _text_has_marker(lower, {"gynecologic", "gynecological", "gyn"}) and _text_has_marker(
        lower, {"emergency first-action", "emergency first action", "emergency path", "what matters first"}
    ):
        return True
    if _text_has_marker(lower, {"period cramps", "period cramp"}) and _text_has_marker(
        lower, {"or an emergency", "or emergency", "what do i do first", "what matters first"}
    ):
        return True
    if not _is_human_medical_query(question):
        return False
    if _text_has_marker(lower, {"spotting only", "only spotting"}) and _text_has_marker(
        lower, {"no pain", "without pain"}
    ) and _text_has_marker(
        lower, {"no dizziness", "or dizziness", "no faint", "no faintness", "or faintness"}
    ):
        return False
    has_gyn_context = _text_has_marker(lower, _GYN_EMERGENCY_QUERY_MARKERS)
    has_red_flag = _text_has_marker(lower, _GYN_EMERGENCY_RED_FLAG_MARKERS)
    has_pelvic_bleed_pair = (
        _text_has_marker(lower, {"pelvic pain", "severe pelvic pain"})
        and _text_has_marker(lower, {"heavy bleeding", "vaginal bleeding", "bleeding"})
    )
    return (has_gyn_context and has_red_flag) or has_pelvic_bleed_pair


def _is_crush_compartment_query(question):
    """Detect crush/compartment-syndrome warning clusters without requiring exact guide language."""
    lower = question.lower()
    source_hits = sum(
        1 for marker in _CRUSH_COMPARTMENT_SOURCE_MARKERS if marker in lower
    )
    symptom_hits = sum(
        1 for marker in _CRUSH_COMPARTMENT_SYMPTOM_MARKERS if marker in lower
    )
    passive_stretch = "hurts badly" in lower and "toes" in lower and "move" in lower
    tight_swelling = (
        ("swollen" in lower or "swelling" in lower)
        and ("tight" in lower or "shiny" in lower)
    )
    return (
        "compartment syndrome" in lower
        or (source_hits >= 1 and symptom_hits >= 1)
        or passive_stretch
        or ("pain out of proportion" in lower)
        or ("out of proportion" in lower and ("tight" in lower or "tighter" in lower))
        or (tight_swelling and ("badly" in lower or "unbearable" in lower))
    )


def _is_serotonin_syndrome_query(question):
    """Detect medication-triggered serotonin-syndrome/toxidrome prompts."""
    lower = question.lower()
    has_source = _text_has_marker(lower, _SEROTONIN_SOURCE_QUERY_MARKERS)
    has_symptoms = _text_has_marker(lower, _SEROTONIN_SYMPTOM_QUERY_MARKERS)
    return has_source and has_symptoms


def _is_serotonin_syndrome_special_case(question):
    """Detect high-specificity serotonin-syndrome/toxidrome prompts."""
    lower = question.lower()
    has_source = _text_has_marker(lower, _SEROTONIN_SOURCE_QUERY_MARKERS)
    if not has_source:
        return False
    high_specificity = _text_has_marker(
        lower,
        {
            "clonus",
            "hyperreflexia",
            "cannot stop moving",
            "can't stop moving",
            "jerking",
            "legs keep jerking",
            "rigid",
            "rigid muscles",
            "muscles are rigid",
            "twitching",
            "twitching all over",
        },
    )
    symptom_hits = sum(
        1 for marker in _SEROTONIN_SYMPTOM_QUERY_MARKERS if marker in lower
    )
    return high_specificity or symptom_hits >= 2


def _is_meningitis_rash_emergency_query(question):
    """Detect fever plus non-blanching/purple rash or meningitis red flags."""
    lower = question.lower()
    has_fever = _text_has_marker(lower, _MENINGITIS_RASH_FEVER_MARKERS)
    has_danger_rash = _text_has_marker(lower, _MENINGITIS_RASH_DANGER_MARKERS)
    has_neuro_or_meningitis_sign = _text_has_marker(
        lower, _MENINGITIS_RASH_NEURO_MARKERS
    )
    neuro_hit_count = sum(
        1 for marker in _MENINGITIS_RASH_NEURO_MARKERS if marker in lower
    )
    has_neck_stiffness = _text_has_marker(
        lower,
        {
            "stiff neck",
            "neck is stiff",
            "rigid neck",
            "neck is rigid",
            "neck stiffness",
            "neck rigidity",
        },
    )
    has_meningitis_companion = _text_has_marker(
        lower,
        {
            "headache",
            "bad headache",
            "severe headache",
            "photophobia",
            "light hurts",
            "lights hurt",
            "hurts to look at light",
            "vomiting",
            "throwing up",
            "confusion",
            "confused",
            "sleepy",
            "hard to wake",
        },
    )
    has_classic_meningitis_cluster = has_fever and (
        has_neck_stiffness or (has_neuro_or_meningitis_sign and has_meningitis_companion)
    )
    has_plain_rash = _text_has_marker(lower, {"rash", "spots", "dots"})
    has_child_context = _text_has_marker(
        lower, {"child", "kid", "toddler", "infant", "baby"}
    )
    has_hard_to_wake = _text_has_marker(
        lower,
        {
            "hard to wake",
            "hard to wake up",
            "will not wake",
            "won't wake",
            "cannot wake",
            "can't wake",
        },
    )
    has_severe_sick_appearance = _text_has_marker(
        lower,
        {
            "very sick",
            "very sick-looking",
            "sick-looking",
            "looks very sick",
            "seems very sick",
        },
    )
    return has_fever and (
        has_danger_rash
        or (
            has_neuro_or_meningitis_sign
            and _text_has_marker(lower, {"rash", "spots", "dots"})
        )
    ) or (has_danger_rash and neuro_hit_count >= 2) or has_classic_meningitis_cluster or (
        has_child_context and has_plain_rash and has_hard_to_wake
    ) or (
        has_danger_rash and has_severe_sick_appearance
    )


def _is_meningitis_rash_retrieval_query(question):
    """Detect retrieval-only child stiff-neck plus dangerous-rash variants."""
    if _is_meningitis_rash_emergency_query(question):
        return True
    lower = question.lower()
    has_child_context = _text_has_marker(
        lower, {"child", "kid", "toddler", "infant", "baby"}
    )
    has_neck_stiffness = _text_has_marker(
        lower,
        {
            "stiff neck",
            "neck is stiff",
            "rigid neck",
            "neck is rigid",
            "neck stiffness",
            "neck rigidity",
        },
    )
    return (
        has_child_context
        and has_neck_stiffness
        and _text_has_marker(lower, _MENINGITIS_RASH_DANGER_MARKERS)
    )


def _is_meningitis_vs_viral_query(question):
    """Detect non-red-flag meningitis-vs-viral comparison prompts."""
    lower = question.lower()
    has_meningitis_context = _text_has_marker(
        lower,
        {
            "meningitis",
            "meningococcemia",
            "meningococcal",
            "sepsis",
        },
    )
    has_routine_illness_context = _text_has_marker(
        lower,
        {
            "viral",
            "viral illness",
            "virus",
            "flu",
            "cold",
            "routine illness",
            "common illness",
        },
    )
    has_boundary_language = _text_has_marker(lower, {" or ", "versus", "vs ", "v. "})
    return has_meningitis_context and has_routine_illness_context and has_boundary_language


def _is_public_health_response_query(question):
    lower = question.lower()
    return _text_has_marker(
        lower,
        {
            "outbreak",
            "cluster",
            "community",
            "public health",
            "surveillance",
            "health officer",
            "contact tracing",
            "quarantine",
            "isolation",
            "reporting",
            "reportable",
        },
    )


def _is_eye_globe_injury_query(question):
    """Detect embedded, penetrating, high-speed, or vision-change eye trauma."""
    lower = question.lower()
    if "eye" not in lower and "vision" not in lower:
        return False
    has_eye_context = "eye" in lower or "vision" in lower
    has_trauma_or_vision_marker = _text_has_marker(
        lower, _EYE_GLOBE_INJURY_QUERY_MARKERS
    )
    has_embedded_object = (
        has_eye_context
        and _text_has_marker(lower, {"stuck", "embedded", "poking out", "pull it out"})
    )
    has_high_speed_debris = has_eye_context and _text_has_marker(
        lower, {"metal chip", "grinding", "flying debris", "high-speed debris", "high speed debris"}
    )
    has_vision_change_after_trauma = (
        _text_has_marker(
            lower,
            {
                "hit in the eye",
                "eye hit",
                "hit by a rock",
                "rock hit",
                "eye injury",
                "flying debris",
            },
        )
        and _text_has_marker(
            lower,
            {
                "vision is darker",
                "darker vision",
                "vision change",
                "vision changes",
                "vision is blurry",
                "blurry vision",
                "see halos",
                "halos",
                "hurts to open",
                "hard to open",
                "severe pain",
            },
        )
    )
    return (
        has_trauma_or_vision_marker
        and (has_embedded_object or has_high_speed_debris or has_vision_change_after_trauma)
    )


def _is_retinal_detachment_eye_emergency_query(question):
    """Detect sudden monocular curtain/floaters/flashes vision-loss emergencies."""
    lower = question.lower()
    if _is_eye_globe_injury_query(question):
        return False
    if _text_has_marker(lower, {"face droop", "slurred speech", "one-sided weakness", "arm weakness"}):
        return False
    if _text_has_marker(lower, {"signal flashes", "lost trail", "trail is lost", "bright signal"}):
        return False
    if "both eyes" in lower and _text_has_marker(lower, {"usual migraine aura", "migraine aura"}):
        return False
    has_eye_or_vision = _text_has_marker(lower, {"eye", "vision", "floaters", "flashes"})
    has_pattern = _text_has_marker(lower, _RETINAL_DETACHMENT_EYE_QUERY_MARKERS)
    has_one_eye = _text_has_marker(lower, {"one eye", "monocular", "in one eye"})
    has_curtain_or_shadow = _text_has_marker(lower, {"curtain", "shadow", "went dark", "vision went dark"})
    has_flashes_or_floaters = _text_has_marker(lower, {"flashes", "floaters", "bright flashes", "shower of floaters"})
    has_vision_loss = _text_has_marker(
        lower,
        {
            "vision loss",
            "lost part of vision",
            "half my vision",
            "vision went dark",
            "went dark",
            "dark curtain",
            "gray curtain",
            "getting worse",
        },
    )
    return has_eye_or_vision and (
        has_pattern
        or (has_one_eye and has_vision_loss)
        or (has_flashes_or_floaters and (has_curtain_or_shadow or has_vision_loss))
    )


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


def _is_runoff_infant_formula_boundary_query(question):
    """Detect infant-formula prompts involving questionable runoff or flood water."""
    lower = question.lower()
    has_infant_formula_context = (
        "formula" in lower
        and _text_has_marker(lower, _RUNOFF_INFANT_FORMULA_INFANT_MARKERS)
    )
    has_questionable_source = _text_has_marker(
        lower, _RUNOFF_INFANT_FORMULA_WATER_MARKERS
    )
    has_uncertainty = _text_has_marker(
        lower, _RUNOFF_INFANT_FORMULA_UNCERTAINTY_MARKERS
    )
    has_use_pressure = _text_has_marker(lower, _RUNOFF_INFANT_FORMULA_USE_MARKERS)
    has_water_context = any(term in lower for term in ("water", "well", "runoff", "rain"))
    return (
        has_infant_formula_context
        and has_questionable_source
        and has_uncertainty
        and has_use_pressure
        and has_water_context
    )


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
    has_dental_source = _text_has_marker(
        lower, _DENTAL_INFECTION_SPECIAL_CASE_MARKERS
    )
    has_swelling = _text_has_marker(lower, _FACIAL_SWELLING_MARKERS)
    has_airway_or_deep_space_red_flag = _text_has_marker(
        lower,
        {
            "hurts to swallow",
            "trouble swallowing",
            "difficulty swallowing",
            "drooling",
            "tongue feels pushed up",
            "cannot open it well",
            "can't open it well",
            "cannot open my mouth",
            "can't open my mouth",
            "airway at risk",
            "fever and drooling",
        },
    )
    explicit_airway_uncertainty = _text_has_marker(
        lower, {"can this wait", "airway at risk"}
    )
    return (
        (has_dental_source and has_swelling)
        or (has_dental_source and has_airway_or_deep_space_red_flag)
        or (has_swelling and has_airway_or_deep_space_red_flag)
        or explicit_airway_uncertainty
    )


def _is_nonpharma_pain_special_case(question):
    """Detect no-painkiller prompts that should avoid empty or uncited answers."""
    return _text_has_marker(question, _NONPHARMA_PAIN_SPECIAL_CASE_MARKERS)


def _is_generic_seizure_special_case(question):
    """Detect generic seizure prompts that need conservative hands-off guidance."""
    lower = question.lower()
    has_seizure = "seizure" in lower or _text_has_marker(
        lower, _GENERIC_SEIZURE_SPECIAL_CASE_MARKERS
    )
    has_generic_marker = _text_has_marker(lower, _GENERIC_SEIZURE_SPECIAL_CASE_MARKERS)
    has_red_flag_marker = has_seizure and _text_has_marker(
        lower, _SEIZURE_RED_FLAG_SPECIAL_CASE_MARKERS
    )
    return (has_generic_marker or has_red_flag_marker) and not _text_has_marker(
        lower, _SEIZURE_SPECIAL_CASE_EXCLUSION_MARKERS
    )


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
    has_exit_blocking_context = _text_has_marker(
        lower,
        {
            "hides my keys",
            "stands in front of the door",
            "blocking the door",
            "blocks the door",
            "door when i try to leave",
            "when i try to leave",
        },
    ) and _text_has_marker(lower, {"leave", "go", "door", "keys"})
    return (
        has_relationship_or_assault_context and has_immediate_danger_marker
    ) or has_exit_blocking_context


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
    blocked_bedroom_egress_signal = (
        _text_has_marker(lower, {"blocked", "blocked door", "door is blocked", "exit is blocked"})
        and _text_has_marker(lower, {"bedroom", "sleeping room", "upstairs bedroom", "second-floor bedroom"})
        and _text_has_marker(lower, {"door", "exit", "hallway"})
        and _text_has_marker(lower, {"window", "escape", "another exit", "go out", "upstairs", "second-floor"})
    )
    return (has_room_signal and (has_fire_signal or has_smoke_signal)) or blocked_bedroom_egress_signal


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


_INDOOR_CO_EXPOSURE_SOURCE_MARKERS = {
    "heater",
    "stove",
    "wood stove",
    "woodstove",
    "charcoal burner",
    "charcoal indoors",
    "charcoal",
    "coal burner",
    "generator",
    "fireplace",
    "combustion",
}

_INDOOR_CO_EXPOSURE_SYMPTOM_MARKERS = {
    "headache",
    "nausea",
    "nauseated",
    "dizzy",
    "dizziness",
    "weak",
    "sleepy",
    "confused",
    "confusion",
    "flu",
    "flu-like",
    "feels like flu",
    "gets better outside",
    "better outside",
    "fresh air",
}

_INDOOR_CO_EXPOSURE_CONTEXT_MARKERS = {
    "indoors",
    "inside",
    "same room",
    "room",
    "house",
    "cabin",
    "tent",
    "sleeping",
    "slept",
    "woke up",
    "wake up",
    "near",
    "with the heater on",
    "no visible smoke",
    "does not look smoky",
    "doesn't look smoky",
}

_INDOOR_CO_EXPOSURE_EXPLICIT_MARKERS = {
    "carbon monoxide",
    "co poisoning",
    "co alarm",
    "carbon monoxide alarm",
}


def _is_indoor_combustion_co_exposure_special_case(question):
    """Detect symptomatic indoor combustion or carbon-monoxide alarm prompts."""
    lower = question.lower()
    if _is_household_chemical_inhalation_query(lower):
        return False
    if _is_charcoal_sand_water_filter_special_case(lower):
        return False

    has_explicit_co = _text_has_marker(lower, _INDOOR_CO_EXPOSURE_EXPLICIT_MARKERS)
    has_symptom = _text_has_marker(lower, _INDOOR_CO_EXPOSURE_SYMPTOM_MARKERS)
    has_source = _text_has_marker(lower, _INDOOR_CO_EXPOSURE_SOURCE_MARKERS)
    has_context = _text_has_marker(lower, _INDOOR_CO_EXPOSURE_CONTEXT_MARKERS)
    has_group_signal = _text_has_marker(
        lower,
        {
            "we all",
            "several people",
            "everyone",
            "people are",
            "people in",
            "same room",
        },
    )

    if has_explicit_co and (has_symptom or "alarm went off" in lower):
        return True
    return has_source and has_symptom and (has_context or has_group_signal)


_SMOKE_AIRWAY_BURN_SOURCE_MARKERS = {
    "fire",
    "smoke",
    "smoke exposure",
    "after the fire",
    "after a fire",
    "fire-gas",
    "fire gas",
}

_SMOKE_AIRWAY_BURN_DANGER_MARKERS = {
    "hoarse voice",
    "hoarseness",
    "voice sounds different",
    "voice change",
    "soot in the mouth",
    "soot in mouth",
    "soot in the mouth and nose",
    "soot in the nose",
    "soot on the face",
    "singed nose hairs",
    "singed nasal hair",
    "face is burned",
    "face burned",
    "facial burns",
    "burns after the fire",
    "airway is in danger",
    "airway danger",
    "keep coughing",
    "repeated coughing",
    "coughing after smoke exposure",
}


def _is_smoke_airway_burn_danger_special_case(question):
    """Detect smoke/fire airway-burn danger prompts."""
    lower = question.lower()
    if _text_has_marker(
        lower, {"face is burned", "face burned", "facial burns"}
    ) and _text_has_marker(
        lower, {"breathing is okay", "breathing okay", "airway is in danger", "airway danger"}
    ):
        return True
    return _text_has_marker(
        lower, _SMOKE_AIRWAY_BURN_SOURCE_MARKERS
    ) and _text_has_marker(
        lower, _SMOKE_AIRWAY_BURN_DANGER_MARKERS
    )


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
    has_child_marker = _text_has_marker(lower, _CHILD_QUERY_MARKERS)
    has_unknown_ingestion_marker = _text_has_marker(
        lower, _UNKNOWN_CHILD_INGESTION_UNKNOWN_MARKERS
    )
    has_child_unknown_medication_ingestion = (
        has_child_marker
        and _text_has_marker(
            lower,
            {
                "swallowed",
                "took",
                "ate",
                "ingested",
            },
        )
        and _text_has_marker(
            lower,
            {
                "unknown pill",
                "unknown pills",
                "unknown medicine",
                "unknown medication",
                "unknown tablet",
                "unknown tablets",
                "unknown capsule",
                "unknown capsules",
            },
        )
    )
    has_vomiting_marker = any(
        marker in lower for marker in ("throwing up", "vomiting", "threw up")
    )
    has_child_cleaner_ingestion_marker = any(
        marker in lower
        for marker in ("got into cleaner", "got into the cleaner", "got into some cleaner")
    )
    if has_child_unknown_medication_ingestion:
        return True
    return (
        has_child_marker
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
    exposure_terms = (
        "bite",
        "bit",
        "scratch",
        "scratched",
        "saliva",
        "open wound",
        "not sure if it bit",
        "unsure if it bit",
        "cannot verify",
        "can't verify",
        "ran off",
    )
    rabies_risk_animals = ("bat", "raccoon", "skunk", "fox", "wild animal", "unknown animal")
    domestic_uncertain = any(term in lower for term in ("dog", "cat", "kitten"))
    rabies_exposure_risk = (
        (
            any(term in lower for term in rabies_risk_animals)
            or (domestic_uncertain and any(term in lower for term in ("acting strange", "foaming", "cannot verify", "can't verify", "ran off")))
            or "rabies" in lower
        )
        and any(term in lower for term in exposure_terms)
    )
    bat_living_space_risk = "bat" in lower and any(
        term in lower
        for term in (
            "woke up",
            "in the room",
            "living space",
            "bedroom",
            "not sure if it bit",
            "unsure if it bit",
        )
    )
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
        or rabies_exposure_risk
        or bat_living_space_risk
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
    has_choking_context = "choking" in lower or _text_has_marker(
        lower,
        {
            "food stuck",
            "stuck in the throat",
            "swallowed wrong",
            "food went down wrong",
            "after dinner",
            "after a bite",
            "bite of food",
            "after one bite",
            "one bite",
            "choked on",
            "choking on",
            "something in their mouth",
            "something in his mouth",
            "something in her mouth",
            "something in the mouth",
            "object in their mouth",
            "object in his mouth",
            "object in her mouth",
            "object in the mouth",
            "thing in their mouth",
            "thing in his mouth",
            "thing in her mouth",
        },
    )
    if not has_choking_context:
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
            "coughing hard",
            "wheezing after dinner",
            "cannot talk",
            "can't talk",
            "unable to talk",
            "no words",
            "cannot speak",
            "can't speak",
            "cannot get words out",
            "cannot cough",
            "can't cough",
            "cannot breathe",
            "can't breathe",
            "turning blue",
            "clutching his throat",
            "clutching her throat",
            "clutching their throat",
            "clutching throat",
            "weak noises",
            "still breathing a little",
            "breathing a little",
            "barely breathing",
            "silent cough",
            "weak cough",
            "is choking on",
            "choking on a",
            "choking on food",
            "choking on meat",
            "choking on bread",
            "choking on grape",
            "choking on a grape",
            "drooling and cannot swallow",
            "drooling",
            "cannot swallow",
            "can't swallow",
            "cannot swallow normally",
            "can't swallow normally",
            "cant swallow normally",
            "swallow normally",
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
            "since the death",
            "after the death",
            "since they died",
        )
    )
    has_recent_signal = any(
        marker in lower
        for marker in (
            "last week",
            "few days ago",
            "recently",
            "since the death",
            "after the death",
        )
    )
    has_shutdown_signal = any(
        marker in lower
        for marker in (
            "cant get out of bed",
            "can't get out of bed",
            "wont get out of bed",
            "won't get out of bed",
            "will not get out of bed",
            "cant function",
            "can't function",
            "wont eat",
            "won't eat",
            "will not eat",
            "not eating",
        )
    )
    return has_loss_signal and has_recent_signal and has_shutdown_signal


def _is_psychosis_paranoia_immediate_safety_special_case(question):
    """Detect hearing-voices/paranoia prompts that need crisis ordering."""
    lower = question.lower()
    has_psychosis_signal = _text_has_marker(
        lower,
        {
            "hearing voices",
            "hears voices",
            "voice telling",
            "voices telling",
            "hallucination",
            "hallucinations",
        },
    )
    has_paranoia_signal = _text_has_marker(
        lower,
        {
            "paranoid",
            "paranoia",
            "thinks people are after",
            "thinks someone is after",
        },
    )
    return has_psychosis_signal and has_paranoia_signal


def _is_mania_no_sleep_immediate_safety_special_case(question):
    """Detect no-sleep activation clusters that need crisis ordering."""
    lower = question.lower()
    has_sleep_or_food_impairment = _text_has_marker(
        lower,
        {
            "has not slept",
            "hasn't slept",
            "not slept",
            "not really slept",
            "barely slept",
            "hardly slept",
            "no sleep",
            "no sleep for",
            "awake for days",
            "awake for",
            "not need sleep",
            "do not need sleep",
            "does not need sleep",
            "not sleeping",
            "not slept for",
            "haven't slept",
            "not had sleep",
            "insomnia",
            "has not eaten",
            "hasn't eaten",
            "not eaten",
            "will not eat",
            "won't eat",
            "hardly eaten",
            "has hardly eaten",
            "barely eating",
            "barely ate",
        },
    )
    has_activation_or_risk = _text_has_marker(
        lower,
        {
            "talking nonstop",
            "will not stop talking",
            "won't stop talking",
            "racing thoughts",
            "pacing all night",
            "keeps pacing",
            "talking fast",
            "rearranging everything",
            "trying to leave",
            "will not stop moving",
            "won't stop moving",
            "can't stop moving",
            "impossible to slow down",
            "will not let anyone slow",
            "won't let anyone slow",
            "normal rules do not apply",
            "special mission",
            "nothing can hurt",
            "unsafe choices",
            "walking outside at night",
            "keep trying to walk outside",
            "risky plans",
            "reckless spending",
            "spending recklessly",
            "spending wildly",
            "acting invincible",
            "invincible",
            "paranoid",
            "grandiose",
            "grand and",
            "reckless behavior",
            "reckless plans",
            "driving around",
            "making risky",
        },
    )
    has_mania_risk_signature = _text_has_marker(
        lower,
        {
            "acting invincible",
            "normal rules do not apply",
            "nothing can hurt",
            "special mission",
            "unsafe choices",
            "walking outside at night",
            "trying to leave",
            "trying to leave with no plan",
            "rearranging everything",
            "will not let anyone slow",
            "impossible to slow down",
            "spending recklessly",
            "spending wildly",
        },
    )
    if not has_activation_or_risk or (
        not has_sleep_or_food_impairment and not has_mania_risk_signature
    ):
        return False

    ordinary_stress_or_anxiety_context = _text_has_marker(
        lower,
        {"stress", "stressed", "anxiety", "anxious", "routine anxiety", "just anxiety"},
    ) and not _text_has_marker(
        lower,
        {
            "special mission",
            "normal rules do not apply",
            "nothing can hurt",
            "acting invincible",
            "will not let anyone slow",
            "impossible to slow down",
            "unsafe choices",
            "trying to leave",
            "spending recklessly",
            "spending wildly",
        },
    )
    return not ordinary_stress_or_anxiety_context


def _is_alcohol_withdrawal_agitated_special_case(question):
    """Detect dangerous alcohol/benzodiazepine withdrawal prompts."""
    lower = question.lower()
    has_withdrawal_signal = _text_has_marker(
        lower,
        {
            "stopped drinking",
            "stopping alcohol",
            "stopped alcohol",
            "quit drinking",
            "quitting drinking",
            "last drink",
            "alcohol withdrawal",
            "withdrawing from alcohol",
            "withdrawal after drinking",
            "during withdrawal",
            "benzo withdrawal",
            "benzodiazepine withdrawal",
            "from benzo",
            "from benzodiazepine",
            "dts",
            "dt's",
            "delirium tremens",
        },
    )
    has_danger_signal = _text_has_marker(
        lower,
        {
            "shaking badly",
            "shaking",
            "tremor",
            "tremors",
            "agitated",
            "agitation",
            "feverish",
            "fever",
            "confused",
            "confusion",
            "seeing things",
            "started seeing things",
            "hallucination",
            "hallucinations",
            "seizure",
            "seizures",
            "safe to leave alone",
            "not safe to leave alone",
            "dts",
            "dt's",
            "delirium tremens",
            "panic",
        },
    )
    return has_withdrawal_signal and has_danger_signal


def _is_trauma_dissociation_after_violence_special_case(question):
    """Detect post-violence dissociation/reliving prompts needing safety first."""
    lower = question.lower()
    has_trauma_signal = _text_has_marker(
        lower,
        {
            "after the attack",
            "after an attack",
            "violent event",
            "after a violent event",
            "after violence",
            "after being attacked",
        },
    )
    has_altered_state_signal = _text_has_marker(
        lower,
        {
            "dissociating",
            "dissociation",
            "reliving what happened",
            "flashback",
            "flashbacks",
            "not acting normal",
            "not acting normally",
            "out of it",
        },
    )
    return has_trauma_signal and has_altered_state_signal


def _is_suicide_immediate_safety_special_case(question):
    """Detect suicide/self-harm prompts that need immediate safety ordering."""
    lower = question.lower()
    coercive_partner_threat = _text_has_marker(
        lower,
        {
            "if i leave",
            "if we leave",
            "if you leave",
            "if i go",
            "if we go",
            "if you go",
            "to stop me from leaving",
            "to keep me from leaving",
        },
    ) and _text_has_marker(lower, {"partner", "ex", "boyfriend", "girlfriend", "spouse"})
    if coercive_partner_threat:
        return False

    return _text_has_marker(
        lower,
        {
            "wish they were dead",
            "wish i were dead",
            "wish i was dead",
            "wishes they were dead",
            "want to die",
            "wants to die",
            "wanted to die",
            "kill themselves",
            "kill himself",
            "kill herself",
            "kill myself",
            "suicidal",
            "suicide",
            "self-harm",
            "self harm",
            "hurt themselves",
            "hurt himself",
            "hurt herself",
            "hurt myself",
            "pills ready",
            "have pills ready",
            "has pills ready",
            "giving away their things",
            "giving away his things",
            "giving away her things",
            "writing goodbye messages",
            "goodbye messages",
            "goodbye note",
            "goodbye notes",
            "not safe to leave alone",
            "safe to leave alone tonight",
        },
    )


def _is_violence_to_others_immediate_safety_special_case(question):
    """Detect threats of violence toward others needing separation/escalation."""
    lower = question.lower()
    return _text_has_marker(
        lower,
        {
            "hurt someone",
            "hurt somebody",
            "hurt others",
            "hurt another person",
            "attack someone",
            "attack somebody",
            "attack another person",
            "attacking someone",
            "killing a person",
            "kill a person",
            "kill someone",
            "kill somebody",
            "harm someone",
            "harm somebody",
            "harm others",
            "threatening people",
            "threatening others",
            "danger to others",
            "may hurt others",
            "might hurt others",
            "might hurt somebody",
            "might hurt someone",
            "voices are telling them to attack",
            "voices telling them to attack",
            "going to hurt someone",
        },
    )


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


def _is_exertional_syncope_chest_emergency_special_case(question):
    """Detect exertional fainting/blackout with cardiac or post-collapse red flags."""
    lower = question.lower()
    has_exertion_trigger = _text_has_marker(
        lower,
        {
            "carrying water uphill",
            "uphill",
            "chopping wood",
            "climbing stairs",
            "hard work",
            "during hard work",
            "during exertion",
            "with exertion",
            "after exertion",
            "walking fast",
            "while walking fast",
            "working hard",
            "after hard work",
        },
    )
    has_syncope_signal = _text_has_marker(
        lower,
        {
            "passed out",
            "fainted",
            "fainting",
            "almost fainted",
            "nearly blacking out",
            "nearly blacked out",
            "brief blackout",
            "blackout",
            "collapsed",
            "collapse",
            "came around",
            "woke up fast",
        },
    )
    has_cardiac_red_flag = _text_has_marker(
        lower,
        {
            "heart problem",
            "heart problem after",
            "chest still hurts",
            "chest hurts",
            "chest pain",
            "chest pressure",
            "feel pressure",
            "chest tightness",
            "tight in the chest",
            "tightness in the chest",
            "shortness of breath",
            "heart is racing",
            "heart racing",
            "racing heart",
        },
    )
    has_post_collapse_neuro_signal = _text_has_marker(
        lower,
        {
            "jerked once",
            "jerk once",
            "came around confused",
            "woke up confused",
            "confused after",
        },
    )
    return has_exertion_trigger and has_syncope_signal and (
        has_cardiac_red_flag or has_post_collapse_neuro_signal
    )


def _is_panic_hyperventilation_tingling_special_case(question):
    """Detect clean hyperventilation/tingling panic prompts without chest red flags."""
    lower = question.lower()
    has_hyperventilation_signal = _text_has_marker(
        lower,
        {
            "hyperventilating",
            "hyperventilation",
            "breathing too fast",
            "fast breathing",
        },
    )
    has_panic_pattern_signal = _text_has_marker(
        lower,
        {
            "heart is racing",
            "racing heart",
            "hands are tingling",
            "tingling hands",
            "fingers are tingling",
            "tingling fingers",
        },
    )
    has_cardiac_red_flag = _text_has_marker(
        lower,
        {
            "chest pressure",
            "chest pain",
            "chest tightness",
            "jaw pain",
            "arm pain",
            "shortness of breath",
            "fainting",
            "passed out",
            "exertion",
            "after exercise",
            "with exercise",
        },
    )
    return has_hyperventilation_signal and has_panic_pattern_signal and not has_cardiac_red_flag


def _is_respiratory_distress_panic_overlap_special_case(question):
    """Detect panic/asthma overlap where breathing danger must come first."""
    lower = question.lower()
    if _text_has_marker(lower, _ALLERGEN_EXPOSURE_MARKERS):
        return False
    has_overlap_signal = _text_has_marker(
        lower,
        {
            "panic attack or an asthma attack",
            "panic or asthma",
            "panic or real breathing trouble",
            "can't tell if this is panic",
            "cant tell if this is panic",
            "panicking",
            "after getting stressed",
            "throat feels tight",
            "throat tight",
            "can't get a satisfying breath",
            "cant get a satisfying breath",
        },
    )
    has_respiratory_signal = _text_has_marker(
        lower,
        {
            "asthma attack",
            "wheezing",
            "wheeze",
            "chest tightness",
            "real breathing trouble",
            "breathing trouble",
            "throat feels tight",
            "throat tight",
            "can't get a satisfying breath",
            "cant get a satisfying breath",
            "rescue inhaler is not helping",
            "rescue inhaler not helping",
            "inhaler is not helping",
            "inhaler not helping",
        },
    )
    return has_overlap_signal and has_respiratory_signal


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
    if "heat stroke" in lower:
        return False
    if any(
        marker in lower
        for marker in (
            "food stuck",
            "stuck in the throat",
            "choking",
            "choked",
            "cannot swallow",
            "can't swallow",
            "drooling",
            "clutching his throat",
            "clutching her throat",
            "clutching their throat",
        )
    ):
        return False
    has_direct_stroke_terms = _text_has_marker(lower, _STROKE_TIA_MARKERS)
    has_face = any(
        marker in lower
        for marker in (
            "face droop",
            "face drooping",
            "facial droop",
            "face looks droopy",
            "face is droopy",
            "one side of the face looks droopy",
            "one side of the face",
        )
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
            "one arm are weak",
            "one arm weak",
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
            "speech is weird",
            "speech sounds weird",
            "weird speech",
            "trouble speaking",
            "trouble finding words",
            "trouble understanding simple words",
            "trouble understanding words",
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
    has_stroke_vs_glucose_context = _text_has_marker(
        lower,
        {
            "low blood sugar or stroke",
            "diabetic confusion or a stroke",
            "diabetic confusion or stroke",
            "got sugar",
            "after not eating",
            "skipped meals",
            "sweaty shaky",
            "sweaty and shaky",
            "looks drunk",
            "look drunk",
            "not going away",
        },
    )
    has_altered_context = _text_has_marker(
        lower, {"confused", "confusion", "slurring", "slurred", "acting drunk"}
    )
    has_stroke_vision_headache = _text_has_marker(
        lower,
        {
            "sudden vision loss with a bad headache",
            "sudden vision loss and a bad headache",
            "vision loss with a bad headache",
            "vision loss with face droop",
            "vision loss and slurred speech",
        },
    )
    return has_direct_stroke_terms or fast_bucket_count >= 1 or has_stroke_vision_headache or (
        has_transient_language and fast_bucket_count >= 1
    ) or (
        fast_bucket_count >= 1
        and has_stroke_vs_glucose_context
        and (has_altered_context or "stroke" in lower)
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


def _is_adult_head_injury_red_flag_special_case(question):
    """Detect adult head-injury red flags that need trauma-first ownership."""
    lower = question.lower()
    if _is_head_injury_clear_fluid_special_case(lower):
        return False
    has_head_trauma = any(
        marker in lower
        for marker in (
            "hit my head",
            "hit their head",
            "hit the head",
            "hit head",
            "hit my head lightly",
            "hit head lightly",
            "slipped hit head",
            "bonked head",
            "bonked my head",
            "bonked their head",
            "hitting their head",
            "head injury",
            "minor head injury",
            "head bump",
            "bumped my head",
            "bumped their head",
            "after a fall",
            "after the fall",
            "small fall",
            "fall yesterday",
            "after a head injury",
            "after hitting their head",
            "after hitting the head",
            "after a head bump",
            "fall and",
            "fell and",
        )
    )
    has_red_flag = any(
        marker in lower
        for marker in (
            "keep vomiting",
            "keeps vomiting",
            "vomiting",
            "vomit",
            "nauseated",
            "more nauseated",
            "getting more nauseated",
            "nausea",
            "blacked out",
            "blackout",
            "passed out",
            "lost consciousness",
            "became confused",
            "still confused",
            "confused",
            "unequal pupil",
            "unequal pupils",
            "one pupil looks bigger",
            "pupil looks bigger",
            "pupils are different",
            "sleep after",
            "can they sleep",
            "sleepy",
            "hard to wake",
            "hard to wake up",
            "worsening headache",
            "worse headache",
            "headache is getting worse",
            "headache getting worse",
            "blood thinners",
            "blood thinner",
            "warfarin",
            "anticoagulant",
            "anticoagulated",
            "getting worse after a head injury",
            "seizure",
            "weakness",
            "clear fluid",
        )
    )
    return has_head_trauma and has_red_flag


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


def _log_warn_event(event_name, **fields):
    """Emit a structured warning that unit tests and operators can both see."""
    payload = {"event": event_name, **dict(sorted(fields.items()))}
    logger.warning(
        json.dumps(payload, sort_keys=True),
        extra={"event_name": event_name, "telemetry": payload},
    )


def _resolve_deterministic_special_case_rules():
    """Build the live deterministic rule objects from the declarative registry."""
    return _router_resolve_deterministic_special_case_rules(
        DETERMINISTIC_SPECIAL_CASE_SPECS,
        predicate_namespaces=(globals(), vars(special_case_builders)),
        builder_namespace=vars(special_case_builders),
    )


_DETERMINISTIC_SPECIAL_CASE_RULES = _resolve_deterministic_special_case_rules()
_DETERMINISTIC_SPECIAL_CASE_SPECS_BY_ID = {
    spec.rule_id: spec for spec in DETERMINISTIC_SPECIAL_CASE_SPECS
}

_DETERMINISTIC_SPECIAL_CASE_BUILDERS = {
    rule.rule_id: rule.builder
    for rule in _DETERMINISTIC_SPECIAL_CASE_RULES
    if rule.builder is not None
}


def get_deterministic_special_case_rules():
    """Expose deterministic rules for validation scripts."""
    return _DETERMINISTIC_SPECIAL_CASE_RULES


def _select_deterministic_special_case_rule(matches, *, log_first_defined_tie):
    """Pick one deterministic rule using priority, lexical signature, then order."""
    return _router_select_deterministic_special_case_rule(
        matches,
        log_first_defined_tie=log_first_defined_tie,
        warn_event=_log_warn_event,
    )


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
    return _router_get_deterministic_special_case_overlaps(
        _DETERMINISTIC_SPECIAL_CASE_RULES
    )


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


@lru_cache(maxsize=1)
def _load_rag_owner_rerank_hints():
    """Load narrow owner rerank hints from a tracked manifest."""
    with open(RAG_OWNER_RERANK_HINTS_PATH, "r", encoding="utf-8") as handle:
        payload = json.load(handle)

    rules = payload.get("rules", [])
    if not isinstance(rules, list):
        raise ValueError("rag owner rerank hints manifest must contain a rules list")

    normalized_rules = []
    for index, rule in enumerate(rules):
        guide_id = str(rule.get("guide_id") or "").strip().upper()
        branch = str(rule.get("branch") or "").strip()
        marker_groups = rule.get("question_marker_groups")
        if not guide_id or not branch or not isinstance(marker_groups, list):
            raise ValueError(f"invalid rag owner rerank hint at index {index}")
        normalized_groups = []
        for group in marker_groups:
            if not isinstance(group, list) or not group:
                raise ValueError(f"invalid marker group for rag owner hint {branch}")
            normalized_groups.append({str(marker).lower() for marker in group})
        normalized_rules.append(
            {
                "guide_id": guide_id,
                "branch": branch,
                "delta": float(rule["delta"]),
                "question_marker_groups": tuple(normalized_groups),
            }
        )
    return tuple(normalized_rules)


def _apply_rag_owner_rerank_hints(question_lower, guide_id, apply_delta):
    for rule in _load_rag_owner_rerank_hints():
        if guide_id != rule["guide_id"]:
            continue
        if all(
            _text_has_marker(question_lower, marker_group)
            for marker_group in rule["question_marker_groups"]
        ):
            apply_delta(rule["branch"], rule["delta"])


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
            meta.get("aliases", ""),
            meta.get("routing_cues", ""),
            meta.get("applicability", ""),
            meta.get("source_file", ""),
        ]
    ).lower()
    guide_id = str(meta.get("guide_id") or "").strip().upper()
    category = meta.get("category", "")
    delta = 0.0
    debug_enabled = logger.isEnabledFor(logging.DEBUG)
    chunk_id = _metadata_rerank_chunk_id(meta)

    def apply_delta(branch_name, branch_delta):
        nonlocal delta
        delta = _apply_metadata_rerank_delta(
            delta, branch_name, branch_delta, chunk_id, debug_enabled
        )

    _apply_rag_owner_rerank_hints(question_lower, guide_id, apply_delta)

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

    if _is_eye_globe_injury_query(question):
        if _text_has_marker(meta_text, _EYE_GLOBE_INJURY_POSITIVE_METADATA_MARKERS):
            apply_delta("eye_globe_injury_positive_metadata", -0.13)
        if _text_has_marker(meta_text, _EYE_GLOBE_INJURY_DISTRACTOR_METADATA_MARKERS):
            apply_delta("eye_globe_injury_distractor_metadata", 0.16)

    if _is_retinal_detachment_eye_emergency_query(question):
        if _text_has_marker(meta_text, _RETINAL_DETACHMENT_EYE_POSITIVE_METADATA_MARKERS):
            apply_delta("retinal_detachment_eye_positive_metadata", -0.16)
        if _text_has_marker(meta_text, _RETINAL_DETACHMENT_EYE_DISTRACTOR_METADATA_MARKERS):
            apply_delta("retinal_detachment_eye_distractor_metadata", 0.22)
        if category not in {"medical"}:
            apply_delta("retinal_detachment_eye_nonmedical_distractor", 0.12)

    if _is_household_chemical_inhalation_query(question):
        if _text_has_marker(meta_text, _HOUSEHOLD_CHEMICAL_EXPOSURE_METADATA_MARKERS):
            apply_delta("household_chemical_inhalation_positive", -0.12)
        if _text_has_marker(meta_text, _COOKSTOVE_CO_METADATA_MARKERS):
            apply_delta("household_chemical_inhalation_cookstove_distractor", 0.16)
        if _text_has_marker(
            meta_text, _HOUSEHOLD_CHEMICAL_INHALATION_DISTRACTOR_METADATA_MARKERS
        ):
            apply_delta("household_chemical_inhalation_unrelated_medical_distractor", 0.18)

    if _is_indoor_combustion_co_smoke_query(question):
        if _text_has_marker(meta_text, _INDOOR_COMBUSTION_CO_OWNER_METADATA_MARKERS):
            apply_delta("indoor_combustion_co_owner_metadata", -0.16)
        elif _text_has_marker(meta_text, _COOKSTOVE_CO_METADATA_MARKERS):
            apply_delta("indoor_combustion_co_related_metadata", -0.06)
        if _text_has_marker(meta_text, _HOT_WATER_HEATING_DISTRACTOR_METADATA_MARKERS):
            apply_delta("indoor_combustion_co_hot_water_distractor", 0.18)

    if _is_chemical_spill_sick_exposure_query(question):
        if _text_has_marker(
            meta_text,
            {
                "chemical safety",
                "chemical-safety",
                "toxicology",
                "poison",
                "chemical & industrial accident response",
                "chemical-industrial-accident-response",
                "hazard recognition",
                "first aid",
            },
        ):
            apply_delta("chemical_spill_sick_exposure_positive", -0.14)
        if _text_has_marker(
            meta_text,
            {
                "chemistry fundamentals",
                "industrial chemistry technology tree",
                "feedstock",
                "raw material",
                "chemical synthesis",
            },
        ):
            apply_delta("chemical_spill_sick_process_distractor", 0.14)

    if _is_unknown_chemical_skin_burn_query(question):
        if _text_has_marker(meta_text, _HOUSEHOLD_CHEMICAL_EXPOSURE_METADATA_MARKERS):
            apply_delta("unknown_chemical_skin_burn_toxicology_positive", -0.13)
        if _text_has_marker(
            meta_text,
            {"poison ivy", "contact rash", "common rashes", "skin irritation", "bug bites"},
        ):
            apply_delta("unknown_chemical_skin_burn_rash_distractor", 0.20)

    if _is_poisoning_unknown_ingestion_card_query(question):
        if _text_has_marker(meta_text, _HOUSEHOLD_CHEMICAL_EXPOSURE_METADATA_MARKERS):
            apply_delta("poisoning_unknown_ingestion_owner_metadata", -0.14)
        if _text_has_marker(
            meta_text,
            {
                "food allergy",
                "allergy and anaphylaxis",
                "routine medication",
                "medication schedule",
                "side effects",
                "hives",
                "common ailments",
                "routine stomach",
            },
        ):
            apply_delta("poisoning_unknown_ingestion_routine_distractor", 0.12)

    if _is_industrial_chemical_smell_boundary_query(question):
        if _text_has_marker(
            meta_text,
            {
                "chemical & industrial accident response",
                "chemical-industrial-accident-response",
                "chemical safety",
                "chemical-safety",
                "hazmat",
                "industrial accident",
                "public exposure",
            },
        ):
            apply_delta("industrial_chemical_smell_safety_positive", -0.15)
        if _text_has_marker(
            meta_text,
            {
                "chemical fuel salvage",
                "chemical-fuel-salvage",
                "chemical degradation identification",
                "fuel salvage",
                "chemistry fundamentals",
            },
        ):
            apply_delta("industrial_chemical_smell_sniff_or_fundamentals_distractor", 0.16)

    if _is_precursor_feedstock_exposure_boundary_query(question):
        if _text_has_marker(
            meta_text,
            {
                "chemical safety",
                "chemical-safety",
                "toxicology",
                "poison",
                "chemical & industrial accident response",
                "chemical-industrial-accident-response",
                "chemistry fundamentals",
                "industrial chemistry technology tree",
                "feedstock",
                "raw material",
            },
        ):
            apply_delta("precursor_feedstock_exposure_boundary_positive", -0.08)
        if _text_has_marker(meta_text, {"food", "market", "communications", "courier"}):
            apply_delta("precursor_feedstock_exposure_boundary_distractor", 0.10)

    if _is_unlabeled_sealed_drum_safety_triage_query(question):
        if _text_has_marker(
            meta_text,
            {
                "chemical safety",
                "chemical-safety",
                "chemical & industrial accident response",
                "chemical-industrial-accident-response",
                "unknown chemicals",
                "never guess",
                "hazardous waste",
                "hazmat",
            },
        ):
            apply_delta("unlabeled_sealed_drum_safety_positive", -0.14)
        if _text_has_marker(
            meta_text,
            {
                "petroleum refining",
                "bitumen",
                "road repair",
                "waterproofing",
                "disaster triage",
                "mci",
                "field hospital",
            },
        ):
            apply_delta("unlabeled_sealed_drum_reuse_or_medical_distractor", 0.22)

    if _is_unknown_loose_chemical_powder_query(question):
        if _text_has_marker(meta_text, _HOUSEHOLD_CHEMICAL_EXPOSURE_METADATA_MARKERS):
            apply_delta("unknown_powder_toxicology_positive", -0.10)
        if _text_has_marker(
            meta_text,
            {
                "food spoilage",
                "food safety",
                "urban survival",
                "scavenging",
                "waste cleanup",
                "powdered food",
            },
        ):
            apply_delta("unknown_powder_handling_distractor", 0.16)

    if _is_common_ailments_gateway_query(question):
        if _text_has_marker(meta_text, _COMMON_AILMENTS_GATEWAY_METADATA_MARKERS):
            apply_delta("common_ailments_gateway_positive", -0.14)
        elif _text_has_marker(meta_text, _FOCUSED_COMMON_SYMPTOM_METADATA_MARKERS):
            apply_delta("common_ailments_gateway_focused_symptom_secondary", 0.035)

    if _is_maintenance_record_query(question):
        if _text_has_marker(meta_text, _MAINTENANCE_RECORD_POSITIVE_METADATA_MARKERS):
            apply_delta("maintenance_record_positive_metadata", -0.14)
        if _text_has_marker(meta_text, _MAINTENANCE_RECORD_DISTRACTOR_METADATA_MARKERS):
            apply_delta("maintenance_record_distractor_metadata", 0.14)

    if _is_anxiety_crisis_explainer_query(question):
        if _text_has_marker(meta_text, _ANXIETY_CRISIS_POSITIVE_METADATA_MARKERS):
            apply_delta("anxiety_crisis_positive_metadata", -0.12)

    if _is_food_storage_container_query(question):
        if _text_has_marker(meta_text, _FOOD_STORAGE_CONTAINER_POSITIVE_METADATA_MARKERS):
            apply_delta("food_storage_container_positive_metadata", -0.11)
        if _text_has_marker(meta_text, _FOOD_STORAGE_CONTAINER_DISTRACTOR_METADATA_MARKERS):
            if _is_salt_jars_hot_humid_setup_query(question) or _text_has_marker(
                question.lower(), {"preservation setup"}
            ):
                apply_delta("food_storage_container_salt_setup_distractor_metadata", 0.26)
            else:
                apply_delta("food_storage_container_distractor_metadata", 0.13)
        if _is_salt_jars_hot_humid_setup_query(question):
            if _text_has_marker(
                meta_text,
                {
                    "food preservation",
                    "food-preservation",
                    "food storage packaging",
                    "food-storage-packaging",
                },
            ):
                apply_delta("salt_jars_setup_owner_metadata", -0.12)
            if _text_has_marker(
                meta_text,
                {
                    "salt production",
                    "salt-production",
                    "salt storage",
                    "salt packaging",
                    "drying & dehydration techniques",
                    "drying-dehydration-techniques",
                    "spices-seasonings",
                    "spices, seasonings",
                },
            ):
                apply_delta("salt_jars_setup_secondary_distractor_metadata", 0.18)

    if _is_dry_meat_fish_contamination_query(question):
        if _text_has_marker(meta_text, _DRY_MEAT_FISH_CONTAMINATION_POSITIVE_METADATA_MARKERS):
            apply_delta("dry_meat_fish_contamination_positive_metadata", -0.12)
        if _text_has_marker(meta_text, _DRY_MEAT_FISH_CONTAMINATION_DISTRACTOR_METADATA_MARKERS):
            apply_delta("dry_meat_fish_contamination_distractor_metadata", 0.14)

    if _is_cooked_rice_power_outage_spoilage_query(question):
        if _text_has_marker(meta_text, {"food spoilage", "food-spoilage-assessment", "food safety"}):
            apply_delta("cooked_rice_power_outage_spoilage_positive", -0.12)
        if _text_has_marker(meta_text, {"shelf life of rice", "dry goods viability", "rice storage"}):
            apply_delta("cooked_rice_power_outage_storage_distractor", 0.16)

    if _is_market_space_layout_query(question):
        if _text_has_marker(meta_text, _MARKET_SPACE_LAYOUT_POSITIVE_METADATA_MARKERS):
            apply_delta("market_space_layout_positive_metadata", -0.12)
        if _text_has_marker(meta_text, _MARKET_SPACE_LAYOUT_DISTRACTOR_METADATA_MARKERS):
            apply_delta("market_space_layout_distractor_metadata", 0.08)

    if _is_market_tax_revenue_query(question):
        if _text_has_marker(meta_text, _MARKET_TAX_REVENUE_POSITIVE_METADATA_MARKERS):
            apply_delta("market_tax_revenue_positive_metadata", -0.12)

    if _is_adhesive_binder_query(question):
        if _text_has_marker(meta_text, _ADHESIVE_BINDER_POSITIVE_METADATA_MARKERS):
            apply_delta("adhesive_binder_positive_metadata", -0.13)
        if _text_has_marker(meta_text, _ADHESIVE_BINDER_DISTRACTOR_METADATA_MARKERS):
            apply_delta("adhesive_binder_distractor_metadata", 0.08)

    if _is_message_auth_query(question):
        if _text_has_marker(meta_text, _MESSAGE_AUTH_POSITIVE_METADATA_MARKERS):
            apply_delta("message_auth_positive_metadata", -0.16)
        if _text_has_marker(meta_text, _MESSAGE_AUTH_DISTRACTOR_METADATA_MARKERS):
            apply_delta("message_auth_distractor_metadata", 0.14)

    if _is_building_habitability_safety_query(question):
        if _text_has_marker(meta_text, _BUILDING_HABITABILITY_POSITIVE_METADATA_MARKERS):
            apply_delta("building_habitability_positive_metadata", -0.13)
        if _text_has_marker(meta_text, _BUILDING_HABITABILITY_DISTRACTOR_METADATA_MARKERS):
            apply_delta("building_habitability_distractor_metadata", 0.08)

    if _is_gi_bleed_emergency_query(question):
        if _text_has_marker(meta_text, _GI_BLEED_POSITIVE_METADATA_MARKERS):
            apply_delta("gi_bleed_positive_metadata", -0.12)
        if _text_has_marker(meta_text, _GI_BLEED_DISTRACTOR_METADATA_MARKERS):
            apply_delta("gi_bleed_distractor", 0.24)

    if _is_surgical_abdomen_emergency_query(question):
        if _text_has_marker(meta_text, _SURGICAL_ABDOMEN_POSITIVE_METADATA_MARKERS):
            apply_delta("surgical_abdomen_positive_metadata", -0.14)
        if _text_has_marker(meta_text, _SURGICAL_ABDOMEN_DISTRACTOR_METADATA_MARKERS):
            apply_delta("surgical_abdomen_distractor_metadata", 0.20)

    if _is_electrical_hazard_query(question):
        if _text_has_marker(meta_text, _ELECTRICAL_HAZARD_POSITIVE_METADATA_MARKERS):
            apply_delta("electrical_hazard_positive_metadata", -0.14)
        if _text_has_marker(meta_text, _ELECTRICAL_HAZARD_DISTRACTOR_METADATA_MARKERS):
            apply_delta("electrical_hazard_distractor_metadata", 0.13)

    if _is_drowning_cold_water_query(question):
        if _text_has_marker(meta_text, _DROWNING_COLD_WATER_POSITIVE_METADATA_MARKERS):
            apply_delta("drowning_cold_water_positive_metadata", -0.13)
        if _text_has_marker(meta_text, _DROWNING_COLD_WATER_DISTRACTOR_METADATA_MARKERS):
            apply_delta("drowning_cold_water_distractor_metadata", 0.10)

    if _is_urgent_nosebleed_query(question):
        if _text_has_marker(meta_text, _NOSEBLEED_URGENT_POSITIVE_METADATA_MARKERS):
            apply_delta("urgent_nosebleed_positive_metadata", -0.14)
        if _text_has_marker(meta_text, _NOSEBLEED_URGENT_DISTRACTOR_METADATA_MARKERS):
            apply_delta("urgent_nosebleed_distractor_metadata", 0.15)

    if _is_major_blood_loss_shock_query(question):
        if _text_has_marker(meta_text, _MAJOR_BLOOD_LOSS_SHOCK_POSITIVE_METADATA_MARKERS):
            apply_delta("major_blood_loss_shock_positive_metadata", -0.18)
        if _text_has_marker(meta_text, _MAJOR_BLOOD_LOSS_SHOCK_DISTRACTOR_METADATA_MARKERS):
            apply_delta("major_blood_loss_shock_distractor_metadata", 0.24)

    if _is_cardiac_first_query(question):
        if _text_has_marker(meta_text, _CARDIAC_FIRST_POSITIVE_METADATA_MARKERS):
            apply_delta("cardiac_first_positive_metadata", -0.14)
        if _text_has_marker(meta_text, _CARDIAC_FIRST_DISTRACTOR_METADATA_MARKERS):
            apply_delta("cardiac_first_distractor_metadata", 0.18)

    if _is_gyn_emergency_query(question):
        if _text_has_marker(meta_text, _GYN_EMERGENCY_POSITIVE_METADATA_MARKERS):
            apply_delta("gyn_emergency_positive_metadata", -0.12)
        if _text_has_marker(meta_text, _GYN_EMERGENCY_DISTRACTOR_METADATA_MARKERS):
            apply_delta("gyn_emergency_distractor_metadata", 0.28)

    if _is_crush_compartment_query(question):
        if _text_has_marker(meta_text, _CRUSH_COMPARTMENT_POSITIVE_METADATA_MARKERS):
            apply_delta("crush_compartment_positive_metadata", -0.10)
        if _text_has_marker(meta_text, _CRUSH_COMPARTMENT_DISTRACTOR_METADATA_MARKERS):
            apply_delta("crush_compartment_distractor_metadata", 0.18)

    if _is_serotonin_syndrome_query(question):
        if _text_has_marker(meta_text, _SEROTONIN_POSITIVE_METADATA_MARKERS):
            apply_delta("serotonin_syndrome_positive_metadata", -0.12)
        if _text_has_marker(meta_text, _SEROTONIN_DISTRACTOR_METADATA_MARKERS):
            apply_delta("serotonin_syndrome_distractor_metadata", 0.20)

    if _is_meningitis_rash_retrieval_query(question):
        if _text_has_marker(meta_text, _MENINGITIS_RASH_POSITIVE_METADATA_MARKERS):
            apply_delta("meningitis_rash_positive_metadata", -0.14)
        if _text_has_marker(meta_text, _MENINGITIS_RASH_DISTRACTOR_METADATA_MARKERS):
            apply_delta("meningitis_rash_distractor_metadata", 0.20)

    if _is_possible_spinal_injury_movement_query(question):
        if guide_id == "GD-049":
            apply_delta("spinal_injury_orthopedics_owner_id", -0.22)
        elif guide_id == "GD-232":
            apply_delta("spinal_injury_first_aid_owner_id", -0.18)
        has_spinal_owner_metadata = _text_has_marker(
            meta_text, _SPINAL_INJURY_POSITIVE_METADATA_MARKERS
        )
        if has_spinal_owner_metadata:
            apply_delta("spinal_injury_owner_metadata", -0.12)
        if (
            not has_spinal_owner_metadata
            and _text_has_marker(meta_text, _SPINAL_INJURY_DISTRACTOR_METADATA_MARKERS)
        ):
            apply_delta("spinal_injury_distractor_metadata", 0.14)

    if _is_meningitis_vs_viral_query(question):
        if guide_id == "GD-589":
            apply_delta("meningitis_vs_viral_primary_owner_id", -0.24)
        elif guide_id == "GD-284":
            apply_delta("meningitis_vs_viral_child_boundary_owner_id", -0.18)
        elif guide_id == "GD-298":
            apply_delta("meningitis_vs_viral_pediatric_owner_id", -0.06)
        elif guide_id == "GD-949":
            apply_delta("meningitis_vs_viral_headache_support_owner_id", -0.03)
        if _text_has_marker(meta_text, _MENINGITIS_RASH_POSITIVE_METADATA_MARKERS):
            apply_delta("meningitis_vs_viral_owner_metadata", -0.12)
        if _text_has_marker(
            meta_text,
            {
                "sepsis recognition",
                "sepsis-recognition-antibiotic-protocols",
                "infant & child care",
                "infant-child-care",
                "pediatric emergency medicine",
                "pediatric-emergency-medicine",
                "headaches: basic care",
                "headaches-basic-care",
                "meningitis or serious infection warning signs",
                "cns/meningitis",
            },
        ):
            apply_delta("meningitis_vs_viral_clinical_owner_metadata", -0.08)
        if (
            not _is_public_health_response_query(question)
            and _text_has_marker(
                meta_text,
                {
                    "public health",
                    "public-health-disease-surveillance",
                    "disease surveillance systems",
                    "health officer",
                    "contact tracing",
                    "quarantine",
                    "isolation",
                },
            )
        ):
            apply_delta("meningitis_vs_viral_public_health_drift_metadata", 0.14)
        if (
            not _text_has_marker(meta_text, _MENINGITIS_RASH_POSITIVE_METADATA_MARKERS)
            and _text_has_marker(meta_text, _MENINGITIS_RASH_DISTRACTOR_METADATA_MARKERS)
        ):
            apply_delta("meningitis_vs_viral_routine_distractor_metadata", 0.10)

    if _is_airway_obstruction_rag_query(question):
        has_airway_owner_metadata = _text_has_marker(
            meta_text, _AIRWAY_OBSTRUCTION_POSITIVE_METADATA_MARKERS
        )
        if has_airway_owner_metadata:
            apply_delta("airway_obstruction_owner_metadata", -0.16)
            if _is_food_bolus_airway_query(question):
                apply_delta("food_bolus_airway_owner_metadata", -0.10)
        if (
            not has_airway_owner_metadata
            and _text_has_marker(meta_text, _AIRWAY_OBSTRUCTION_DISTRACTOR_METADATA_MARKERS)
        ):
            apply_delta("airway_obstruction_distractor_metadata", 0.12)
        if _is_food_bolus_airway_query(question) and _text_has_marker(
            meta_text,
            {
                "unknown ingestion",
                "unknown-ingestion",
                "poison control",
                "poisoning",
                "toxicology",
                "swallowed substances",
            },
        ):
            apply_delta("food_bolus_poisoning_distractor_metadata", 0.28)

    if _is_newborn_sepsis_danger_retrieval_query(question):
        has_routine_newborn_metadata = _text_has_marker(
            meta_text,
            {
                "common ailments",
                "common-ailments",
                "routine baby care",
                "normal newborn",
                "digestive regularity",
                "routine feeding",
            },
        )
        if _text_has_marker(meta_text, _NEWBORN_SEPSIS_POSITIVE_METADATA_MARKERS):
            apply_delta("newborn_sepsis_owner_metadata", -0.16)
        if has_routine_newborn_metadata:
            apply_delta("newborn_sepsis_routine_distractor_metadata", 0.12)

    if _is_abdominal_trauma_danger_query(question):
        if _text_has_marker(meta_text, _ABDOMINAL_TRAUMA_POSITIVE_METADATA_MARKERS):
            apply_delta("abdominal_trauma_owner_metadata", -0.16)
            if _is_fall_belly_pain_query(question) or _is_handlebar_abdominal_trauma_query(question):
                apply_delta("abdominal_trauma_specific_owner_metadata", -0.08)
        if _is_fall_belly_pain_query(question) and _text_has_marker(
            meta_text,
            {
                "gynecological",
                "women's health",
                "ectopic pregnancy",
                "pregnancy",
                "vaginal bleeding",
            },
        ):
            apply_delta("child_fall_belly_pain_gyn_distractor_metadata", 0.18)
        if _text_has_marker(meta_text, _SURGICAL_ABDOMEN_DISTRACTOR_METADATA_MARKERS):
            apply_delta("abdominal_trauma_routine_gi_distractor_metadata", 0.12)
            if _is_fall_belly_pain_query(question) or _is_handlebar_abdominal_trauma_query(question):
                apply_delta("abdominal_trauma_specific_routine_distractor_metadata", 0.08)
    if _is_handlebar_abdominal_trauma_query(question) and _text_has_marker(
        meta_text,
        {"back pain", "musculoskeletal", "routine back pain", "self-care"},
    ):
        apply_delta("handlebar_back_pain_distractor_metadata", 0.18)
    if (
        (_is_fall_belly_pain_query(question) or _is_handlebar_abdominal_trauma_query(question))
        and not _text_has_marker(meta_text, _ABDOMINAL_TRAUMA_POSITIVE_METADATA_MARKERS)
        and _text_has_marker(meta_text, {"field surgery", "surgical procedures", "invasive"})
    ):
        apply_delta("abdominal_trauma_invasive_surgery_adjacent_metadata", 0.10)

    if _is_infected_wound_boundary_query(question):
        has_wound_owner_metadata = _text_has_marker(
            meta_text, _INFECTED_WOUND_POSITIVE_METADATA_MARKERS
        )
        if has_wound_owner_metadata:
            apply_delta("infected_wound_owner_metadata", -0.15)
        if (
            not has_wound_owner_metadata
            and _text_has_marker(meta_text, _INFECTED_WOUND_DISTRACTOR_METADATA_MARKERS)
        ):
            apply_delta("infected_wound_skin_distractor_metadata", 0.12)

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
            if _is_urinary_vaginal_overlap_query(question_lower) and _text_has_marker(
                meta_text, _VAGINAL_SYMPTOM_METADATA_MARKERS
            ):
                apply_delta("urinary_vaginal_overlap_positive_metadata", -0.08)
            if _is_hematuria_query(question_lower) and _text_has_marker(
                meta_text, _HEMATURIA_DISTRACTOR_METADATA_MARKERS
            ):
                apply_delta("hematuria_nonurinary_distractor", 0.12)
            if _text_has_marker(meta_text, _BOWEL_RECTAL_DISTRACTOR_MARKERS):
                apply_delta("urinary_bowel_distractor", 0.08)

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

        if _is_classic_stroke_fast_special_case(question):
            if guide_id == "GD-232" and _text_has_marker(
                meta_text,
                {
                    "first aid",
                    "first-aid",
                    "emergency response",
                    "stroke",
                    "fast",
                    "transient ischemic attack",
                    "tia",
                    "last known normal",
                    "last-known-normal",
                },
            ):
                apply_delta("classic_stroke_fast_first_aid_owner", -0.18)
            elif category == "medical" and not _text_has_marker(
                meta_text,
                {
                    "first aid",
                    "first-aid",
                    "emergency",
                    "stroke",
                    "transient ischemic attack",
                    "tia",
                    "neurologic",
                    "triage",
                },
            ):
                apply_delta("classic_stroke_fast_medical_distractor", 0.025)

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
        if _is_food_bolus_airway_query(question) and _text_has_marker(
            meta_text,
            {
                "unknown ingestion",
                "unknown-ingestion",
                "poison control",
                "poisoning",
                "toxicology",
                "swallowed substances",
            },
        ):
            apply_delta("food_bolus_poisoning_post_generic_distractor", 0.06)
        if _is_handlebar_abdominal_trauma_query(question) and _text_has_marker(
            meta_text,
            {"back pain", "musculoskeletal", "routine back pain", "self-care"},
        ):
            apply_delta("handlebar_back_pain_post_generic_distractor", 0.04)
        if _is_abdominal_trauma_danger_query(question) and _text_has_marker(
            meta_text,
            _ABDOMINAL_TRAUMA_POSITIVE_METADATA_MARKERS,
        ):
            apply_delta("abdominal_trauma_owner_post_generic_metadata", -0.10)

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
    return _scenario_frame_helpers._unique_ordered(items)


def _split_scenario_clauses(question):
    """Split a scenario prompt into meaningful user-written clauses."""
    return _scenario_frame_helpers._split_scenario_clauses(
        question, split_at_question_restart=_split_at_question_restart
    )


def _content_tokens(text):
    """Extract lightweight content tokens for overlap checks."""
    return _scenario_frame_helpers._content_tokens(text)


def _extract_deadline(question):
    """Return a compact deadline/time-pressure phrase when present."""
    return _scenario_frame_helpers._extract_deadline(question)


def _extract_assets(clauses):
    """Return user-stated assets/resources for the current turn."""
    return _scenario_frame_helpers._extract_assets(clauses)


def _extract_constraints(clauses, deadline):
    """Return user-stated constraints or limiting factors."""
    return _scenario_frame_helpers._extract_constraints(clauses, deadline)


def _extract_hazards(question):
    """Return salient hazards called out by the user."""
    return _scenario_frame_helpers._extract_hazards(question)


def _extract_people(question):
    """Return people/patient descriptors from the current turn."""
    return _scenario_frame_helpers._extract_people(question)


def _extract_environment(question):
    """Return environment/context signals from the question."""
    return _scenario_frame_helpers._extract_environment(question)


def _derive_objectives(question, clauses):
    """Return objective clauses for coverage tracking and review."""
    return _scenario_frame_helpers._derive_objectives(
        question, clauses, detect_domains=_detect_domains
    )


def build_scenario_frame(question):
    """Parse user-facing scenario structure without changing decomposition."""
    frame = _scenario_frame_helpers.build_scenario_frame(
        question,
        detect_domains=_detect_domains,
        split_at_question_restart=_split_at_question_restart,
        safety_critical_callback=_scenario_frame_is_safety_critical,
    )
    if _is_retinal_detachment_eye_emergency_query(question or ""):
        domains = set(frame.get("domains") or [])
        domains.add("medical")
        frame["domains"] = sorted(domains)
        for objective in frame.get("objectives") or []:
            objective_domains = set(objective.get("domains") or [])
            objective_domains.add("medical")
            objective["domains"] = sorted(objective_domains)
    return frame


def empty_session_state():
    """Return the default structured session state."""
    return _scenario_frame_helpers.empty_session_state()


def _copy_session_state(session_state):
    """Return a detached copy of the structured session state."""
    return _scenario_frame_helpers._copy_session_state(session_state)


def merge_frame_with_session(frame, session_state):
    """Merge current-turn structure onto prior session context for prompting/review."""
    return _scenario_frame_helpers.merge_frame_with_session(frame, session_state)


def update_session_state(session_state, frame):
    """Persist user-provided scenario facts into structured session state."""
    return _scenario_frame_helpers.update_session_state(session_state, frame)


def _session_state_is_empty(session_state):
    """Return True when the structured session state has no stored facts."""
    return _scenario_frame_helpers._session_state_is_empty(session_state)


def _render_session_state_text(session_state):
    """Render session state as compact text for prompts/debug."""
    return _scenario_frame_helpers._render_session_state_text(session_state)


def _should_use_session_context(question, frame, session_state):
    """Return True when retrieval should widen a vague follow-up using session state."""
    return _scenario_frame_helpers._should_use_session_context(
        question, frame, session_state
    )


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
    exclude_nosebleed_for_gi_bleed = _is_gi_bleed_emergency_query(question)
    rows = []
    for id_, doc, meta, dist in zip(
        results["ids"][0],
        results["documents"][0],
        results["metadatas"][0],
        results["distances"][0],
    ):
        if exclude_gd_918_for_mania_psychosis and meta.get("guide_id") == "GD-918":
            continue
        if exclude_nosebleed_for_gi_bleed:
            meta_text = " ".join(
                [
                    meta.get("guide_title", ""),
                    meta.get("section_heading", ""),
                    meta.get("slug", ""),
                    meta.get("description", ""),
                    meta.get("source_file", ""),
                ]
            ).lower()
            if _text_has_marker(meta_text, {"nosebleed", "nosebleeds"}):
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

    if _is_salt_jars_hot_humid_setup_query(question):
        for owner_id in ("GD-065", "GD-966"):
            owner_candidates = [
                row
                for row in rows
                if row["meta"].get("guide_id") == owner_id and row["id"] not in selected_ids
            ]
            if not owner_candidates:
                continue
            chosen = owner_candidates[0]
            selected.append(chosen)
            selected_ids.add(chosen["id"])
            family_counts[chosen["family"]] += 1
            section_counts[chosen["section_key"]] += 1

    if _is_classic_stroke_fast_special_case(question):
        owner_candidates = [
            row
            for row in rows
            if row["meta"].get("guide_id") == "GD-232"
            and row["id"] not in selected_ids
        ]
        if owner_candidates:
            chosen = owner_candidates[0]
            selected.append(chosen)
            selected_ids.add(chosen["id"])
            family_counts[chosen["family"]] += 1
            section_counts[chosen["section_key"]] += 1

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
    if _is_classic_stroke_fast_special_case(question):
        for index, row in enumerate(selected):
            if row["meta"].get("guide_id") == "GD-232":
                if index > 2:
                    selected.insert(2, selected.pop(index))
                break
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


def _append_retrieval_spec(specs, seen, *, text, category, limit, where=None):
    """Add a retrieval spec unless the text/category pair is already present."""
    where_key = json.dumps(where or {}, sort_keys=True)
    key = (text, category or "", where_key)
    if key in seen:
        return
    seen.add(key)
    specs.append(
        {
            "text": text,
            "category": category,
            "where": where,
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


def _is_airway_obstruction_rag_query(question):
    """Detect airway-obstruction prompts for retrieval-profile support."""
    lower = question.lower()
    return _text_has_marker(
        lower,
        {
            "choking",
            "food stuck",
            "stuck in the throat",
            "cannot speak",
            "can't speak",
            "cannot breathe",
            "can't breathe",
            "blue lips",
            "back blows",
            "heimlich",
            "abdominal thrust",
            "choking on",
            "drooling and cannot swallow",
            "cannot swallow after",
        },
    )


def _has_allergy_or_anaphylaxis_trigger(question):
    """Return True when an airway prompt explicitly includes allergy-trigger language."""
    lower = (question or "").lower()
    return _text_has_marker(
        lower,
        {
            "allergy",
            "allergic",
            "allergic reaction",
            "anaphylaxis",
            "epinephrine",
            "epi pen",
            "epipen",
            "hives",
            "whole body hives",
            "bee sting",
            "wasp sting",
            "hornet sting",
            "stung",
            "peanut",
            "shellfish",
            "new medicine",
            "medicine reaction",
            "throat tight",
            "throat feels tight",
            "throat closing",
            "tongue swelling",
            "lip swelling",
            "lips swelling",
            "face swelling",
            "facial swelling",
        },
    )


def _is_food_bolus_airway_query(question):
    """Detect food-bolus airway prompts that should outrank poisoning lanes."""
    lower = question.lower()
    food_context = _text_has_marker(
        lower,
        {"after a bite", "bite of food", "food stuck", "food bolus", "choking on"},
    )
    airway_context = _text_has_marker(
        lower,
        {"drooling", "cannot swallow", "can't swallow", "cannot breathe", "cannot speak"},
    )
    toxin_context = _text_has_marker(
        lower,
        {
            "poison",
            "toxin",
            "unknown substance",
            "cleaner",
            "chemical",
            "pills",
            "medication",
            "detergent",
            "fuel",
        },
    )
    return food_context and airway_context and not toxin_context


def _is_newborn_sepsis_danger_query(question):
    """Detect sick-newborn phrasing that needs sepsis-first retrieval."""
    lower = question.lower()
    newborn_context = _text_has_marker(lower, {"newborn", "baby", "infant"})
    danger = _text_has_marker(
        lower,
        {
            "limp",
            "will not feed",
            "won't feed",
            "poor feeding",
            "hard to wake",
            "sleepy",
            "fever",
            "low temperature",
            "acting weak",
            "very sick",
            "breathing trouble",
            "sepsis",
        },
    )
    return newborn_context and danger


def _has_young_infant_age_context(question):
    """Return True for explicit age-under-4-weeks phrasing."""
    lower = question.lower()
    return bool(
        re.search(
            r"\b(?:[0-3])[\s-]*(?:week|weeks|wk|wks)(?:[\s-]*old)?\b"
            r"|\b(?:[0-9]|1[0-9]|2[0-7])[\s-]*(?:day|days)(?:[\s-]*old)?\b"
            r"|\b(?:under|less than|younger than)\s*(?:4|four)\s*(?:week|weeks|wk|wks)\b"
            r"|\b(?:under|less than|younger than)\s*(?:28|twenty eight)\s*(?:day|days)\b",
            lower,
        )
    )


def _is_newborn_sepsis_danger_retrieval_query(question):
    """Normalize narrow newborn/young-infant danger variants for retrieval only."""
    if _is_newborn_sepsis_danger_query(question):
        return True

    lower = question.lower()
    newborn_context = _text_has_marker(lower, {"newborn", "baby", "infant"})
    young_infant_age_context = _has_young_infant_age_context(lower)
    if not (newborn_context or young_infant_age_context):
        return False

    poor_feeding = _text_has_marker(
        lower,
        {
            "not feeding well",
            "not eating well",
            "feeding poorly",
            "not taking feeds",
            "not nursing well",
            "has not fed much",
            "not fed much",
        },
    )
    abnormal_responsiveness = _text_has_marker(
        lower,
        {
            "harder to wake",
            "harder to wake up",
            "difficult to wake",
            "difficult to wake up",
            "hard to rouse",
            "harder to rouse",
        },
    )
    feels_cold = _text_has_marker(
        lower,
        {"feels cold", "cold to touch", "cold skin", "cold all over"},
    )
    return poor_feeding or abnormal_responsiveness or feels_cold


def _is_abdominal_trauma_danger_query(question):
    """Detect abdominal trauma prompts that should retrieve trauma/acute-abdomen owners."""
    lower = question.lower()
    abdomen = _text_has_marker(
        lower,
        {"belly", "abdomen", "abdominal", "stomach", "left side", "right side"},
    )
    trauma = _text_has_marker(
        lower,
        {"fell", "kicked", "hit", "handlebar", "injury", "trauma", "after a hit"},
    )
    danger = _text_has_marker(
        lower,
        {
            "hard belly",
            "vomiting",
            "pale",
            "dizzy",
            "watch at home",
            "urgent help",
            "get urgent help",
        },
    )
    return abdomen and (trauma or danger)


def _is_possible_spinal_injury_movement_query(question):
    """Detect trauma plus neuro symptoms where movement/carrying is the unsafe boundary."""
    lower = question.lower()
    has_trauma = _text_has_marker(
        lower,
        {
            "fall",
            "fell",
            "fallen",
            "slipped",
            "crash",
            "accident",
            "hit",
            "impact",
            "landed",
            "after a fall",
            "creek bank",
        },
    )
    has_neck_or_back = _text_has_marker(
        lower,
        {
            "neck hurts",
            "neck pain",
            "neck injury",
            "back hurts",
            "back pain",
            "spine",
            "spinal",
        },
    )
    has_neuro_symptom = _text_has_marker(
        lower,
        {
            "numb",
            "numbness",
            "feel numb",
            "feels numb",
            "tingling",
            "tingle",
            "pins and needles",
            "weakness",
            "clumsy",
        },
    )
    has_movement_pressure = _text_has_marker(
        lower,
        {
            "carry",
            "carrying",
            "drag",
            "dragging",
            "move them",
            "move him",
            "move her",
            "move inside",
            "transport",
            "walk them",
            "walk him",
            "walk her",
            "stand them",
            "truck",
            "cabin",
            "inside",
            "get them warm",
            "get him warm",
            "get her warm",
        },
    )
    return has_trauma and has_neck_or_back and has_neuro_symptom and has_movement_pressure


def _is_handlebar_abdominal_trauma_query(question):
    lower = question.lower()
    return "handlebar" in lower and _text_has_marker(
        lower,
        {"belly", "abdomen", "abdominal", "stomach", "left side", "right side", "pain"},
    )


def _is_fall_belly_pain_query(question):
    lower = question.lower()
    return _text_has_marker(lower, {"fell", "fall", "fallen"}) and _text_has_marker(
        lower,
        {"belly pain", "abdominal pain", "abdomen pain", "stomach pain"},
    )


def _is_infected_wound_boundary_query(question):
    """Detect wound infection boundary prompts that need infection/sepsis owners."""
    lower = question.lower()
    wound = _text_has_marker(lower, {"wound", "cut", "red streak", "red streaks"})
    danger = _text_has_marker(
        lower,
        {
            "spreading",
            "pus",
            "fever",
            "red streak",
            "red streaks",
            "feel weak",
            "treated urgently",
            "getting worse",
        },
    )
    return wound and danger


def _retrieval_profile_for_question(question, frame=None):
    """Classify the retrieval shape before fetching evidence."""
    lower = question.lower()
    has_urgent_boundary = any(
        marker in lower
        for marker in (
            "or just",
            "normal or",
            "watch at home",
            "can this wait",
            "treated urgently",
            "get urgent help first",
        )
    )
    if (
        _is_airway_obstruction_rag_query(question)
        or _is_generic_choking_help_special_case(question)
        or _is_meningitis_rash_retrieval_query(question)
        or _is_newborn_sepsis_danger_retrieval_query(question)
        or _is_abdominal_trauma_danger_query(question)
        or _is_possible_spinal_injury_movement_query(question)
        or _is_infected_wound_boundary_query(question)
        or _is_poisoning_unknown_ingestion_card_query(question)
        or _is_gi_bleed_emergency_query(question)
        or _is_electrical_hazard_query(question)
        or _is_cardiac_first_query(question)
        or _is_classic_stroke_fast_special_case(question)
        or _scenario_frame_is_safety_critical(frame or build_scenario_frame(question))
    ):
        if has_urgent_boundary:
            return "normal_vs_urgent"
        return "safety_triage"
    if has_urgent_boundary:
        return "normal_vs_urgent"
    if any(marker in lower for marker in (" or ", "versus", "vs ", "which")):
        return "compare_or_boundary"
    if len(_content_tokens(question)) <= 3:
        return "low_support"
    return "how_to_task"


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
    retrieval_profile = _retrieval_profile_for_question(question, frame)
    specs = []
    if retrieval_profile in {"safety_triage", "normal_vs_urgent"}:
        specs.append(
            {
                "text": (
                    f"{question} first aid emergency red flags urgent evaluation "
                    "airway breathing circulation shock sepsis poisoning trauma "
                    "do not reassure as routine home care before danger signs"
                ),
                "category": "medical",
                "limit": supplemental_limit,
            }
        )
        if _is_airway_obstruction_rag_query(question):
            specs.append(
                {
                    "text": (
                        f"{question} first-aid emergency-airway-management choking "
                        "foreign body airway obstruction cannot breathe cannot speak "
                        "ineffective cough back blows abdominal thrust chest thrust "
                        "infant choking no blind finger sweep nothing by mouth "
                        "drooling cannot swallow"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )
            if _is_food_bolus_airway_query(question):
                specs.append(
                    {
                        "text": (
                            f"{question} First Aid Emergency Response Choking and "
                            "Airway Management Emergency Airway Management Aspiration "
                            "Airway Foreign Bodies food bolus after bite of food "
                            "drooling cannot swallow nothing by mouth no poison ingestion"
                        ),
                        "category": "medical",
                        "limit": supplemental_limit,
                    }
                )
        if _is_newborn_sepsis_danger_retrieval_query(question):
            specs.append(
                {
                    "text": (
                        f"{question} sepsis-recognition-antibiotic-protocols newborn "
                        "sepsis neonatal fever low temperature limp poor feeding hard "
                        "to wake breathing trouble weak very sick urgent escalation "
                        "first aid"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )
        if _is_abdominal_trauma_danger_query(question):
            specs.append(
                {
                    "text": (
                        f"{question} acute-abdominal-emergencies trauma hemorrhage "
                        "control abdominal trauma hard belly vomiting pale dizzy "
                        "handlebar injury shock urgent evacuation nothing by mouth"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )
            if _is_fall_belly_pain_query(question):
                specs.append(
                    {
                        "text": (
                            f"{question} Acute Abdominal Emergencies blunt abdominal "
                            "trauma child fall belly pain internal bleeding shock "
                            "urgent medical evaluation not constipation"
                        ),
                        "category": "medical",
                        "limit": supplemental_limit,
                    }
                )
            if _is_handlebar_abdominal_trauma_query(question):
                specs.append(
                    {
                        "text": (
                            f"{question} Acute Abdominal Emergencies blunt abdominal "
                            "trauma handlebar injury left side pain internal bleeding "
                            "solid organ injury shock urgent medical evaluation"
                        ),
                        "category": "medical",
                        "limit": supplemental_limit,
                    }
                )
        if _is_possible_spinal_injury_movement_query(question):
            specs.extend(
                [
                    {
                        "text": (
                            f"{question} orthopedics-fractures First Aid Emergency "
                            "Response possible spinal injury neck back trauma after "
                            "fall numbness tingling weakness do not move carry drag "
                            "walk transport support head and neck keep spine neutral "
                            "keep warm without unsafe movement"
                        ),
                        "category": "medical",
                        "where": {
                            "$or": [
                                {"slug": "orthopedics-fractures"},
                                {"slug": "first-aid"},
                            ]
                        },
                        "limit": supplemental_limit,
                    },
                    {
                        "text": (
                            f"{question} spinal precautions suspected spine injury "
                            "neck pain with arm hand numbness tingling after fall "
                            "avoid casual movement carry only if immediate danger "
                            "immobilize support head neck emergency help"
                        ),
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                ]
            )
        if _is_infected_wound_boundary_query(question):
            specs.append(
                {
                    "text": (
                        f"{question} wound hygiene infection prevention sepsis "
                        "spreading redness red streaks pus fever weakness urgent "
                        "care not home cleaning only"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )
        if _is_poisoning_unknown_ingestion_card_query(question):
            specs.append(
                {
                    "text": (
                        f"{question} unknown-ingestion-child-poisoning-triage "
                        "toxicology poisoning response poison control possible "
                        "medicine ingestion sleepy drowsy altered mental status "
                        "mouth burns tasting liquid do not induce vomiting airway "
                        "breathing no oral remedies"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )
        if _is_classic_stroke_fast_special_case(question):
            specs.append(
                {
                    "text": (
                        f"{question} First Aid Emergency Response stroke "
                        "recognition act within minutes FAST face droop arm "
                        "weakness slurred speech transient neurologic deficit "
                        "possible TIA symptoms improved do not wait last known "
                        "normal time no food water pills urgent evacuation"
                    ),
                    "category": "medical",
                    "where": {"slug": "first-aid"},
                    "limit": supplemental_limit,
                }
            )
    if _is_meningitis_vs_viral_query(question):
        specs.append(
            {
                "text": (
                    f"{question} sepsis-recognition-antibiotic-protocols suspected "
                    "meningitis meningococcemia viral illness warning signs stiff neck "
                    "severe headache photophobia vomiting confusion hard to wake "
                    "non-blanching purple rash compare clarify not routine headache"
                ),
                "category": "medical",
                "limit": supplemental_limit,
            }
        )
    if _is_indoor_combustion_co_smoke_query(question):
        specs.extend(
            [
                {
                    "text": (
                        f"{question} smoke inhalation carbon monoxide fire-gas "
                        "exposure get fresh air evacuate symptoms headache dizziness "
                        "confusion coughing indoor stove blocked ventilation"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} cookstoves indoor heating safety woodstove "
                        "smoke-back chimney draft blocked flue stop stove ventilate "
                        "only if safe carbon monoxide alarm"
                    ),
                    "category": "survival",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_food_storage_container_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "food-storage-packaging Food Storage Packaging salt jars "
                        "hot humid room food storage packaging salted fish preserved "
                        "food sealed food-grade containers cool dry desiccants "
                        "preservation method first"
                    ),
                    "category": "agriculture",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} answer: this is primarily a food preservation "
                        "question, with storage-container choice second. "
                        "food-preservation food storage packaging container "
                        "choice does not fix unsafe unfinished preservation salt is "
                        "ingredient not only salt storage hot humid room jars are "
                        "not a complete preservation setup"
                    ),
                    "category": "agriculture",
                    "limit": supplemental_limit,
                },
            ]
        )
        if _is_salt_jars_hot_humid_setup_query(question):
            specs.extend(
                [
                    {
                        "text": (
                            "food-preservation Food Preservation salt jars hot humid "
                            "room preserve sound food first drying salting acidifying "
                            "fermenting before storage container choice"
                        ),
                        "category": "agriculture",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": (
                            "food-storage-packaging Food Storage Packaging hot humid "
                            "room jars sealed containers desiccants cool dry location "
                            "after preservation is complete"
                        ),
                        "category": "agriculture",
                        "limit": supplemental_limit,
                    },
                ]
            )

    if _is_dry_meat_fish_contamination_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "dry meat fish humidity animals dirt protected solar drying "
                        "raised rack screen cheesecloth cover insects flies dust "
                        "airflow low humidity avoid open hanging contamination"
                    ),
                    "category": "agriculture",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} simplest safe drying setup thin slices salted "
                        "if needed raised enclosed screened rack breathable cover "
                        "protect from animals flies dirt dust dry breezy low humidity"
                    ),
                    "category": "agriculture",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_canned_fruit_soft_spot_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "food spoilage assessment canned fruit soft spots bulging "
                        "leaking broken seal cloudiness off smell changed texture "
                        "discard first do not cut away affected portions"
                    ),
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} canned food safety discard if bulging leaking "
                        "seal failed pressure cloudy off smell suspicious texture "
                        "not salvageable by trimming"
                    ),
                    "category": "survival",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_cooked_rice_power_outage_spoilage_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "food spoilage assessment cooked rice power outage tastes off "
                        "discard stop eating do not taste test warm leftovers toxin risk"
                    ),
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} cooked rice after power loss discard first unsafe "
                        "warm holding not stale food sensory signs unreliable"
                    ),
                    "category": "agriculture",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_unknown_loose_chemical_powder_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "unknown loose chemical powder white powder residue fertilizer "
                        "detergent toxic substance isolate area keep people pets away "
                        "do not move sweep smell taste or handle poison control hazmat"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} unknown powder on counter possible household "
                        "chemical exposure toxicology poison control do not touch "
                        "do not move do not identify by handling"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_unknown_chemical_skin_burn_query(question):
        specs.extend(
            [
                {
                    "text": (
                        f"{question} unknown unlabeled under sink bottle hands "
                        "burning chemical skin burn toxicology dermal exposure "
                        "flush running water remove contaminated jewelry clothing "
                        "poison control not poison ivy not routine rash"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        "toxicology poisoning response dermal exposure unknown "
                        "household cleaner skin burning chemical burn water irrigation "
                        "poison control"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_industrial_chemical_smell_boundary_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "chemical industrial accident response strong chemical odor "
                        "wrong smell avoid inhalation do not sniff isolate area hand "
                        "off to chemical safety hazmat exposure response"
                    ),
                    "category": "defense",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} chemical smells wrong industrial process do not "
                        "assess by smell do not stay in chemistry fundamentals unless "
                        "pure feedstock design isolate ventilate from safe distance "
                        "chemical safety industrial accident response"
                    ),
                    "category": "chemistry",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_chemical_spill_sick_exposure_query(question):
        specs.extend(
            [
                {
                    "text": (
                        f"{question} chemical spill workshop someone feels sick "
                        "exposure triage first isolate area move to fresh air "
                        "identify exposure route skin eye inhalation ingestion "
                        "chemical safety toxicology first aid"
                    ),
                    "category": "medical",
                    "where": {
                        "$or": [
                            {"slug": "chemical-safety"},
                            {"slug": "toxicology-poisoning-response"},
                            {"slug": "chemical-industrial-accident-response"},
                            {"slug": "unknown-ingestion-child-poisoning-triage"},
                            {"slug": "first-aid"},
                        ]
                    },
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        "chemical spill sick person first response avoid inhalation "
                        "do not troubleshoot process isolate ventilate from safe "
                        "distance decontaminate by exposure route poison control "
                        "emergency medical help"
                    ),
                    "category": "chemistry",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_precursor_feedstock_exposure_boundary_query(question):
        specs.extend(
            [
                {
                    "text": (
                        f"{question} precursor feedstock raw material boundary known materials "
                        "product design chemistry fundamentals no exposure no spill no symptoms"
                    ),
                    "category": "chemistry",
                    "where": {"$or": [{"slug": "chemistry-fundamentals"}]},
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} poisoning exposure boundary unknown chemical spill leak odor "
                        "symptoms chemical safety toxicology industrial accident first response"
                    ),
                    "category": "medical",
                    "where": {
                        "$or": [
                            {"slug": "chemical-safety"},
                            {"slug": "toxicology-poisoning-response"},
                            {"slug": "chemical-industrial-accident-response"},
                            {"slug": "unknown-ingestion-child-poisoning-triage"},
                        ]
                    },
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_unlabeled_sealed_drum_safety_triage_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "unlabeled sealed drum unknown chemical safety triage isolate "
                        "do not open sniff test move use reuse disposal hazmat mark "
                        "unknown do not use"
                    ),
                    "category": "chemistry",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} sealed unlabeled drum nobody sick safety triage "
                        "first hazardous waste unknown container do not infer bitumen "
                        "do not use for waterproofing road repair feedstock"
                    ),
                    "category": "defense",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_market_space_layout_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "marketplace trade space physical footprint stalls walking "
                        "lanes foot traffic cart clearance loading edge blocked "
                        "corners notices inside market footprint"
                    ),
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} market layout stalls lanes notices inside market "
                        "space storage loading handoff without tax revenue drift"
                    ),
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_gi_bleed_emergency_query(question):
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

    if _is_surgical_abdomen_emergency_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "acute abdominal emergencies surgical abdomen guarding "
                        "rigid hard belly right lower quadrant appendicitis bowel "
                        "obstruction vomiting distention no stool no gas fever "
                        "localized tender spot urgent escalation npo"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} surgical-abdomen red flags first action "
                        "do not flatten into stomach flu constipation reflux "
                        "or dehydration home care"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_electrical_hazard_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "electrical safety hazard prevention shock cannot let go "
                        "collapsed live wire downed power line sparking outlet wet "
                        "breaker box de-energize do not touch cpr aed after safe"
                    ),
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} electrical hazard first keep people back shut "
                        "off power from dry safe place verify de-energized before repair"
                    ),
                    "category": "power-generation",
                    "limit": supplemental_limit,
                },
            ]
        )
        if _is_downed_power_line_query(question):
            specs.append(
                {
                    "text": (
                        f"{question} downed utility power line first action stay far "
                        "back keep everyone away call utility emergency response do "
                        "not approach do not move line no nonconductive separation "
                        "attempt avoid vehicles fences wet ground"
                    ),
                    "category": "power-generation",
                    "limit": supplemental_limit,
                }
            )

    if _is_drowning_cold_water_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "drowning rescue priorities reach throw row go post rescue "
                        "breathing cpr cold water gasping airway gentle warming ice rescue"
                    ),
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} active drowning cold water under ice first action "
                        "call help keep rescuer safe throw reach breathing cpr hypothermia"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )
        if _text_has_marker(question_lower, {"face down", "silent in the water", "motionless in the water"}):
            specs.append(
                {
                    "text": (
                        f"{question} face down silent in water active drowning now "
                        "call alert help immediately safe reach throw rescue remove "
                        "from water check breathing start cpr rescue breaths do not "
                        "lead with observation"
                    ),
                    "category": "survival",
                    "limit": supplemental_limit,
                }
            )
        if _is_post_rescue_drowning_breathing_query(question):
            specs.append(
                {
                    "text": (
                        f"{question} after water rescue coughing short of breath "
                        "requires urgent medical evaluation now monitor breathing "
                        "call emergency help aspiration delayed respiratory distress"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                }
            )

    if _is_urgent_nosebleed_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "nosebleed urgent red flags will not stop 20 30 minutes "
                        "blood down throat blood thinners dizzy pale weak repeated "
                        "heavy lean forward firm pressure urgent medical help"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} epistaxis urgent care lean forward pinch soft "
                        "nose blood thinner repeated heavy same day not headache dental"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_cardiac_first_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "acute coronary cardiac emergencies chest pressure chest "
                        "tightness shortness of breath jaw arm pain dread exertion "
                        "panic versus heart attack call emergency help aspirin if appropriate"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} cardiac first do not dismiss as panic urgent "
                        "medical evaluation heart attack warning signs"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_major_blood_loss_shock_query(question):
        specs.extend(
            [
                {
                    "text": (
                        f"{question} hemorrhagic shock blood loss pale dizzy "
                        "clammy weak control bleeding keep flat warm urgent "
                        "evacuation trauma hemorrhage control"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} trauma hemorrhage control major bleeding "
                        "direct pressure tourniquet wound packing impaled object "
                        "shock recognition resuscitation"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} shock bleeding trauma stabilization "
                        "hemorrhage control decision tree pale dizzy after blood "
                        "loss not nosebleed care"
                    ),
                    "category": "survival",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_gyn_emergency_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "gynecological emergency early pregnancy bleeding dizziness "
                        "missed period one-sided pelvic pain ectopic pregnancy "
                        "heavy vaginal bleeding shoulder pain urgent evacuation"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} gynecologic emergency first action possible "
                        "ectopic pregnancy hemorrhage do not use uterine massage "
                        "unless postpartum"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_crush_compartment_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "crush injury compartment syndrome pain out of proportion "
                        "tight swelling numbness tingling passive stretch urgent "
                        "immobilize at heart level no massage"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} possible compartment syndrome crush syndrome "
                        "neurovascular assessment splint keep limb at heart level "
                        "urgent evacuation"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_maintenance_record_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "basic record-keeping maintenance logs repair history failure logs "
                        "repeat failure prevention cause fix date person equipment lessons "
                        "learned from breakdowns"
                    ),
                    "category": "culture-knowledge",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} basic record keeping log template maintenance "
                        "repair failure cause fix date responsible person repeat mistake prevention"
                    ),
                    "category": "culture-knowledge",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_anxiety_crisis_explainer_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "anxiety stress crisis red flags thoughts of suicide self-harm "
                        "unable to stay safe hallucinations confusion chest pain fainting "
                        "blue lips severe trouble breathing urgent help 988"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} anxiety crisis boundary self-harm suicide cannot "
                        "function safely urgent escalation panic medically different symptoms"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_market_tax_revenue_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "taxation public revenue market fees taxes assessment collection "
                        "fairness transparency receipts posted rules appeals budget corruption prevention"
                    ),
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} taxation revenue systems tax collection market fees "
                        "public revenue fair assessment visible rules receipts appeals"
                    ),
                    "category": "resource-management",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_adhesive_binder_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "adhesives binders formulation adhesive selection wood leather "
                        "paper containers hide glue casein starch paste pine pitch "
                        "surface prep clamping curing safety"
                    ),
                    "category": "crafts",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} choose simple adhesive binder family by material "
                        "wood leather paper container concise complete matrix safety "
                        "limits ventilation heat caustic solvent hazards"
                    ),
                    "category": "crafts",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_message_auth_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "message authentication courier protocols prove note real "
                        "challenge response wax seal tamper evidence courier roster "
                        "chain of custody verify posted order before acting"
                    ),
                    "category": "communications",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} message trust verification authenticity integrity "
                        "sender confirmation posted evacuation order courier note "
                        "not wildfire evacuation planning"
                    ),
                    "category": "communications",
                    "limit": supplemental_limit,
                },
            ]
        )
        if _is_simple_courier_note_auth_query(question):
            specs.append(
                {
                    "text": (
                        f"{question} simplest robust courier note authenticity use "
                        "one shared sender mark seal or phrase plus courier roster "
                        "and callback path avoid fragile heavy one-time-pad checksum "
                        "witness bureaucracy"
                    ),
                    "category": "communications",
                    "limit": supplemental_limit,
                }
            )
        if _is_posted_order_verification_query(question):
            specs.append(
                {
                    "text": (
                        f"{question} posted evacuation order verify issuing authority "
                        "agreed authentication signal trusted courier runner radio "
                        "second channel before acting unless immediate visible danger"
                    ),
                    "category": "communications",
                    "limit": supplemental_limit,
                }
            )

    if _is_building_habitability_safety_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "building inspection habitability checklist do not enter soft floor "
                        "damaged building storage sleep occupancy outside first active collapse "
                        "sagging gas smell fire damage sewage backup short salvage"
                    ),
                    "category": "salvage",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} damaged building no entry triage structural safety "
                        "soft bouncy floor roof leak mold storage only usable after repair"
                    ),
                    "category": "salvage",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_stretcher_access_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "clinic stretcher access entrance patient transport door "
                        "direct outside route wide straight threshold turning space "
                        "waiting triage clinic layout"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} stretcher-capable access door should connect "
                        "outside approach directly to triage treatment route avoid "
                        "wheelchair-only minimums"
                    ),
                    "category": "building",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_serotonin_syndrome_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "serotonin syndrome antidepressant cough medicine "
                        "serotonergic medication reaction clonus fever confusion "
                        "rigid muscles shaking sweating poison control ems cooling"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} toxicology poisoning toxidrome emergency "
                        "serotonin syndrome agitation tremor diarrhea overheating "
                        "urgent escalation"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_meningitis_rash_retrieval_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "meningitis meningococcemia sepsis emergency fever "
                        "purple dark bruise-like non-blanching rash confusion "
                        "hard to wake stiff neck vomiting EMS emergency care now"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} suspected meningitis meningococcal sepsis "
                        "non-blanching petechial purplish rash altered mental "
                        "status immediate emergency evaluation not routine rash"
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

        if _is_common_ailments_gateway_query(question):
            specs.extend(
                [
                    {
                        "text": "common ailments recognition basic home care mild versus urgent symptoms red flags",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                    {
                        "text": "when to seek professional medical care common ailments red flags home care",
                        "category": "medical",
                        "limit": supplemental_limit,
                    },
                ]
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

        if (
            (
                _is_household_chemical_hazard_query(question)
                and not _is_unknown_chemical_skin_burn_query(question)
            )
            or _is_unknown_leaking_chemical_container_query(question)
            or _is_unknown_loose_chemical_powder_query(question)
        ):
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
            if _is_unknown_leaking_chemical_container_query(question):
                specs.append(
                    {
                        "text": (
                            f"{question} unknown leaking chemical container sharp smell "
                            "evacuate isolate do not touch do not open do not move "
                            "container ventilate only from safe distance poison control hazmat"
                        ),
                        "category": "medical",
                        "limit": supplemental_limit,
                    }
                )
            if _is_unknown_chemical_skin_burn_query(question):
                specs.append(
                    {
                        "text": (
                            f"{question} unknown unlabeled under sink bottle hands "
                            "burning chemical skin burn toxicology dermal exposure "
                            "flush running water remove contaminated jewelry clothing "
                            "poison control not poison ivy not routine rash"
                        ),
                        "category": "medical",
                        "limit": supplemental_limit,
                    }
                )
            if _is_unknown_loose_chemical_powder_query(question):
                specs.append(
                    {
                        "text": (
                            f"{question} unknown loose chemical powder white powder residue "
                            "isolate area keep people pets away do not touch do not move "
                            "do not sweep do not smell do not taste poison control hazmat"
                        ),
                        "category": "medical",
                        "limit": supplemental_limit,
                    }
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
            if _is_urinary_vaginal_overlap_query(question_lower):
                specs.extend(
                    [
                        {
                            "text": "vaginal itching discharge burning urination common vaginal infections vaginitis yeast bacterial vaginosis STI urinary overlap",
                            "category": "medical",
                            "limit": supplemental_limit,
                        },
                        {
                            "text": "burning when urinating plus vaginal itching and discharge differential diagnosis do not guess antibiotic choice",
                            "category": "medical",
                            "limit": supplemental_limit,
                        },
                    ]
                )
            if _is_hematuria_query(question_lower):
                specs.extend(
                    [
                        {
                            "text": "visible blood in urine hematuria urinary red flag medical evaluation urgent if clots heavy bleeding flank pain fever urinary retention",
                            "category": "medical",
                            "limit": supplemental_limit,
                        },
                        {
                            "text": "blood in urine urinary tract infection kidney stone bladder symptoms common ailments urinary when urgent",
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

    if _is_eye_globe_injury_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "eye injury embedded object penetrating globe high-speed "
                        "metal debris darker vision shield without pressure do not "
                        "flush remove rub or press urgent ophthalmology"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} emergency ophthalmology eye shield cover both "
                        "eyes no direct pressure no flushing embedded debris"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
            ]
        )

    if _is_retinal_detachment_eye_emergency_query(question):
        specs.extend(
            [
                {
                    "text": (
                        "sudden vision loss flashes floaters curtain shadow one eye "
                        "retinal detachment emergency ophthalmology urgent eye evaluation "
                        "do not treat as migraine glasses pink eye or night vision"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} retinal detachment warning sudden monocular vision "
                        "loss dark curtain floaters flashes urgent ophthalmology evacuate"
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
                        "mixed cleaners bleach ammonia vinegar acid chlorine "
                        "chloramine gas chemical "
                        "inhalation chest tightness coughing poison control "
                        "fresh air"
                    ),
                    "category": "medical",
                    "limit": supplemental_limit,
                },
                {
                    "text": (
                        f"{question} chemical safety incompatible cleaners mixed "
                        "bleach vinegar ammonia toxic fumes fresh air evacuate "
                        "poison control emergency exposure"
                    ),
                    "category": "chemistry",
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

    if _is_runoff_infant_formula_boundary_query(question_lower):
        specs.extend(
            [
                {
                    "text": "rainwater harvesting roof runoff first flush roof contamination drinking water limits",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "questionable water assessment flood contaminated well chemical contamination uncertain source",
                    "category": "survival",
                    "limit": supplemental_limit,
                },
                {
                    "text": "infant formula emergency water safe source public health dehydration danger signs",
                    "category": "medical",
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
    results,
    specs,
    sub_queries,
    scenario_frame,
    current_frame,
    session_state_used,
    retrieval_profile,
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
    safety_critical = _scenario_frame_is_safety_critical(
        scenario_frame
    ) or retrieval_profile in {"safety_triage", "normal_vs_urgent"}

    return {
        "sub_queries": sub_queries,
        "retrieval_profile": retrieval_profile,
        "safety_critical": safety_critical,
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


def _review_marks_safety_critical(results, frame=None) -> bool:
    """Return True when retrieval metadata or the scenario frame says safety-first."""
    review = results.get("_senku", {}) or results.get("_senku_review", {})
    if bool(review.get("safety_critical")):
        return True
    if review.get("retrieval_profile") in {"safety_triage", "normal_vs_urgent"}:
        return True
    return _scenario_frame_is_safety_critical(frame)


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
        _review_marks_safety_critical(reranked, frame)
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
    if _is_salt_jars_hot_humid_setup_query(question):
        candidate_limit = max(candidate_limit, 30)
    if _is_classic_stroke_fast_special_case(question):
        candidate_limit = max(candidate_limit, 80)
    retrieval_profile = _retrieval_profile_for_question(question, scenario_frame)
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
            where=spec.get("where"),
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
        if spec.get("where"):
            query_kwargs["where"] = (
                {"$and": [query_kwargs["where"], spec["where"]]}
                if query_kwargs.get("where")
                else spec["where"]
            )
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
        retrieval_profile,
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
    return _response_normalization._fix_mojibake(text)


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
    if _is_gi_bleed_emergency_query(question or ""):
        notes.append(
            "- Treat these symptoms as a possible GI bleed. Lead with airway, "
            "ongoing bleeding, and shock checks plus urgent escalation before "
            "hydration, food poisoning, nosebleed positioning, visible-wound "
            "direct pressure, or routine GI self-care advice."
        )
        notes.append(
            "- For bowel, rectal, black-tarry-stool, coffee-ground-vomit, or "
            "vomiting-blood presentations, do not write 'apply direct pressure' "
            "unless there is a separate visible external bleeding site."
        )
        notes.append(
            "- If the only bleeding described is stool, rectal bleeding, vomit, "
            "coffee-ground material, or vomiting blood, do not mention direct "
            "pressure at all; use shock checks, airway positioning, nothing by "
            "mouth for severe symptoms, and urgent transport."
        )
        notes.append(
            "- If immediate actions are needed, use a compact numbered list with "
            "bleed-first triage and emergency help up front."
        )
    if _is_surgical_abdomen_emergency_query(question or ""):
        notes.append(
            "- Surgical-abdomen red flags: lead with possible appendicitis, bowel "
            "obstruction, perforation, or another acute abdominal emergency. "
            "Escalate urgently before dehydration, stomach-flu, constipation, "
            "reflux, or routine home-care advice."
        )
        notes.append(
            "- For guarding, rigid/hard belly, right-lower-belly pain with movement "
            "or fever, localized very tender spot with fever, or vomiting plus "
            "distention and no stool/gas: keep them NPO/nothing by mouth if surgery "
            "or obstruction is possible, monitor shock/airway, and arrange urgent "
            "medical help."
        )
    if _is_gyn_emergency_query(question or ""):
        notes.append(
            "- Gynecologic/early-pregnancy red flags: lead with urgent evaluation "
            "or evacuation for possible ectopic pregnancy, ovarian torsion, "
            "miscarriage complication, or hemorrhage before routine cramps, STI, "
            "heartburn, or visible-wound bleeding care."
        )
        notes.append(
            "- For early pregnancy or undifferentiated pelvic bleeding, do not "
            "recommend vaginal direct pressure, internal inspection, or uterine "
            "massage unless the prompt clearly says this is postpartum bleeding."
        )
        notes.append(
            "- Unless the prompt clearly says postpartum hemorrhage after delivery, "
            "do not give uterotonic drug names/doses, uterine tamponade, embolization, "
            "or hysterectomy steps; say higher-level care may be needed instead."
        )
        notes.append(
            "- For early-pregnancy ectopic red flags, do not frame the action as "
            "postpartum hemorrhage care, emergency delivery, or uterine evacuation; "
            "use emergency evaluation/evacuation for possible ectopic pregnancy or "
            "internal bleeding."
        )
    if _is_crush_compartment_query(question or ""):
        notes.append(
            "- Crush/compartment warning signs: treat worsening out-of-proportion "
            "pain, tight swelling, numbness/tingling, or pain with passive toe/finger "
            "movement as urgent. Immobilize, keep the limb around heart level, "
            "remove constricting items, and write urgent surgical evaluation/evacuation "
            "as a required action now. Do not make urgent evaluation conditional when "
            "pain out of proportion plus tightening, pinned-under-weight with numbness "
            "or a hard calf, or fast swelling is present; do not write escalation as "
            "'if numbness persists' or 'if pain continues.' Treat the red flags named "
            "in the user's question as already present; do not tell them to wait for "
            "the same signs to appear or worsen before evacuation."
        )
        notes.append(
            "- Do not lead with elevation above heart level, massage, stretching, "
            "foot-care hygiene, rest-only care, back-pain saddle/bladder red flags, "
            "or NSAIDs as the primary response."
        )
    if _is_serotonin_syndrome_query(question or ""):
        notes.append(
            "- Medication-triggered serotonin-syndrome cues: name possible serotonin "
            "syndrome/toxidrome first, say not to take another dose of the suspected "
            "serotonergic medicine/source while contacting Poison Control, EMS, or a "
            "clinician, cool overheating, and escalate before panic, flu, dehydration, "
            "menopause, or dementia framing."
        )
        notes.append(
            "- Do not use broad stop phrases like 'stop all serotonergic medications,' "
            "'cease any potential source,' or 'stop taking the combo immediately'; do "
            "not tell the user to stop all medicines indefinitely or start "
            "cyproheptadine/benzodiazepines on their own. Frame medication holds, "
            "sedation, and antidotes as Poison Control, EMS, clinician, or trained-care decisions."
        )
    if _is_meningitis_rash_retrieval_query(question or ""):
        notes.append(
            "- Fever, confusion, hard-to-wake behavior, stiff neck, severe headache, "
            "vomiting, or a purple/dark/bruise-like/non-blanching rash in a concerning "
            "cluster: treat as possible "
            "meningitis or meningococcemia. The first numbered instruction "
            "must explicitly be to call EMS, go to emergency care, or seek emergency "
            "medical care immediately; do not lead with routine flu/rash care, antipyretic "
            "schedules, bug-bite infection checks, isolation, Health Officer notification, "
            "contact tracing, quarantine, or other public-health reporting."
        )
        if _text_has_marker(question_lower, {"sepsis", "septic", "shock", "very sick", "very ill", "very unwell"}):
            notes.append(
                "- Because the prompt explicitly frames sepsis, shock, or very-sick "
                "appearance, include sepsis screening and first-hour priorities after "
                "the emergency escalation."
            )
        notes.append(
            "- Isolation, health-officer notification, contact tracing, fever comfort "
            "measures, and hydration are secondary after urgent clinical escalation "
            "unless the prompt is only about community surveillance."
        )
    elif _is_meningitis_vs_viral_query(question or ""):
        notes.append(
            "- Meningitis-vs-viral comparison prompts should use an if/then compare "
            "shape: say you cannot diagnose from the label alone, check red flags "
            "first, and then distinguish dangerous meningitis signs from a possible "
            "uncomplicated viral illness. If stiff neck, severe headache, photophobia, "
            "repeated vomiting, confusion, hard-to-wake behavior, or a non-blanching "
            "purple/dark rash is present, treat it as possible meningitis or "
            "meningococcemia and seek emergency clinical evaluation. If those signs "
            "are absent and symptoms are mild, viral illness is possible; monitor for "
            "red flags. Do not recommend Health Officer notification, quarantine, "
            "contact tracing, isolation, or empiric treatment unless the question is "
            "about an outbreak, community response, surveillance, or public-health "
            "reporting."
        )
    if _is_airway_obstruction_rag_query(question or ""):
        notes.append(
            "- Choking/possible airway-obstruction prompts are emergency-first: if the "
            "person cannot speak, breathe, cough effectively, is blue, drooling, or "
            "cannot swallow, lead with urgent airway actions and emergency help. Do "
            "not answer as poison ingestion, panic, allergy, or routine observation "
            "before airway danger is handled. Do not mention anaphylaxis unless the "
            "prompt includes hives, swelling, allergic exposure, throat tightness, or "
            "another allergy trigger."
        )
        notes.append(
            "- Do not advise blind finger sweeps. Remove an object from the mouth only "
            "if it is clearly visible and reachable; otherwise continue age-appropriate "
            "choking rescue or CPR/escalation as indicated."
        )
        if not _has_allergy_or_anaphylaxis_trigger(question or ""):
            notes.append(
                "- This airway-obstruction prompt has no allergy trigger: do not "
                "mention allergy, allergic reaction, anaphylaxis, epinephrine, "
                "antihistamines, hives, swelling, or throat-tightness branches."
            )
        if "panic" in (question or "").lower():
            notes.append(
                "- For choking-versus-panic boundary prompts, give complete branch "
                "logic: first check whether the person can speak, cough forcefully, "
                "breathe, and maintain normal color; start choking rescue only if "
                "speech/cough/breathing is absent, weak, or worsening, or if blue "
                "color/collapse appears; discuss panic or observation only after "
                "those airway danger signs are absent. Do not leave conditional "
                "headings such as 'If unresponsive' without a complete action."
            )
    if _is_newborn_sepsis_danger_query(question or ""):
        notes.append(
            "- Newborn danger signs such as limpness, very weak or very sick appearance, "
            "poor feeding, hard-to-wake behavior, breathing trouble, fever, or low "
            "temperature require urgent medical evaluation immediately. The first "
            "numbered instruction must explicitly say to seek urgent medical evaluation "
            "immediately or emergency help now, keep the newborn warm, and perform ABC "
            "checks while arranging that help; do not substitute topical antiseptic, "
            "watchful waiting, routine feeding advice, or observation-only care."
        )
    if _is_abdominal_trauma_danger_query(question or ""):
        notes.append(
            "- Abdominal trauma with hard belly, vomiting, pale/dizzy appearance, "
            "handlebar impact, or watch-at-home uncertainty is trauma-first: arrange "
            "urgent medical evaluation/evacuation, monitor shock, keep them still, "
            "and avoid food or drink if internal injury or surgery is possible. Do "
            "not lead with routine stomach upset or constipation care."
        )
    if _is_infected_wound_boundary_query(question or ""):
        notes.append(
            "- Wounds with spreading redness, pus, red streaks, fever, worsening pain, "
            "or weakness need infection/sepsis escalation first. Lead with urgent care "
            "for red streaks/systemic symptoms plus wound protection/cleaning; do not "
            "answer as routine rash, bug bite, itch relief, or cosmetic skin irritation."
        )
    if _is_maintenance_record_query(question or ""):
        notes.append(
            "- Maintenance/repair/failure record prompts should start from basic "
            "record-keeping: log date, asset/equipment, symptom/failure, cause, fix, "
            "parts/materials, person responsible, follow-up check, and lessons to "
            "prevent repeat failures. Do not answer only as archives, accounting, "
            "tax, trade ledgers, or one specific equipment-maintenance guide."
        )
    if _is_anxiety_crisis_explainer_query(question or ""):
        notes.append(
            "- If asked when anxiety/stress becomes a crisis, explicitly list red "
            "flags: thoughts of suicide or self-harm, feeling unable to stay safe, "
            "confusion/hallucinations, inability to function safely, chest pain, "
            "fainting, blue lips, severe shortness of breath, or new/severe panic "
            "symptoms that feel medically different. Mention urgent help/988 or "
            "emergency care for those red flags before routine self-care."
        )
    if _is_active_rain_roof_repair_query(question or ""):
        notes.append(
            "- Active-rain roof repair: lead with fall/storm/electrical safety. Do "
            "not tell the user to climb onto a wet roof or work during lightning, "
            "high wind, ice, unstable footing, or active storm conditions. Prefer "
            "interior damage control and a tarp only from safe ground/ladder access; "
            "durable patching waits for dry, stable conditions."
        )
    if _is_runoff_infant_formula_boundary_query(question or ""):
        notes.append(
            "- Infant formula plus roof runoff or a flood-affected well is a "
            "high-risk contamination boundary. Lead with the infant/high-risk "
            "water-source decision: use the safest known or official/tested water "
            "first, treat the flood-affected well as not cleared, and treat roof "
            "runoff in a barrel as questionable. Do not say boiling makes roof "
            "runoff, flood-affected well water, or clean-barrel water safe for baby "
            "formula; boiling does not prove chemical, sewage, roof-material, or "
            "flood contamination safe."
        )
    if _is_market_tax_revenue_query(question or ""):
        notes.append(
            "- If a market prompt says taxes or public revenue, combine marketplace "
            "fee visibility with taxation/revenue-system guidance: say what the fee "
            "or tax funds, publish rates before collection, use receipts/records, "
            "separate assessor/collector/auditor roles when possible, provide an "
            "appeal path, and avoid arbitrary or favoritism-based collection."
        )
    if _is_building_habitability_safety_query(question or ""):
        notes.append(
            "- Damaged-building storage/sleep/use prompts: lead with no-entry triage "
            "before storage suitability or moving items. If there is active collapse, "
            "major sagging, strong gas smell, heavy fire damage, sewage backup, live "
            "electrical hazard, or a soft/bouncy floor, leave/reassess from outside "
            "and do not use it for storage, sleep, or normal occupancy yet."
        )
    if _is_food_storage_container_query(question or ""):
        notes.append(
            "- Food storage/container prompts: lead with whether the food is already "
            "safely preserved, dry, salted, acidified, or fermented before vessel "
            "choice. Salt purity/storage is secondary when the prompt is about salted "
            "fish, food jars, dried beans/herbs, finished fermented vegetables, or "
            "salt/jars/hot-humid preservation setups. If the user asks whether this "
            "is a preservation question or a storage-container question, answer that "
            "it is preservation-first and container/packaging second."
        )
        if _is_salt_jars_hot_humid_setup_query(question or ""):
            notes.append(
                "- For the salt/jars/hot-humid setup, prefer Food Preservation and "
                "Food Storage Packaging support when available. Do not cite or frame "
                "the answer as Salt Production unless the user is asking how to make, "
                "purify, or store salt itself."
            )
    if _is_dry_meat_fish_contamination_query(question or ""):
        notes.append(
            "- For drying meat or fish when humidity, animals, dirt, insects, or dust "
            "are explicit hazards, do not recommend open hanging in direct sun/wind "
            "by itself. The simplest answer must include a raised rack or enclosed "
            "drying frame, screen/cheesecloth or breathable cover, airflow, salt cure "
            "or brine for meat/fish in humidity, smoke/heat or controlled dehydrator "
            "backup if the air is humid, and protection from animals, flies, dust, "
            "and ground splash. Do not describe salt as optional for humid meat/fish."
        )
    if _is_canned_fruit_soft_spot_query(question or ""):
        notes.append(
            "- Canned fruit soft-spot safety prompts: if the can is bulging, leaking, "
            "seal-failed, pressure-releasing, cloudy, off-smelling, moldy, or texture "
            "has changed suspiciously, discard the contents. Do not say to discard "
            "only affected portions or that it may be salvageable after a discard-first "
            "trigger."
        )
    if _is_cooked_rice_power_outage_spoilage_query(question or ""):
        notes.append(
            "- Cooked rice after a power outage with off taste/smell is discard-first: "
            "tell the user to stop eating and discard it. Do not use smell or another "
            "taste as the primary test, and do not frame it as merely stale dry rice."
        )
    if _is_industrial_chemical_smell_boundary_query(question or ""):
        notes.append(
            "- For industrial/process chemical odor prompts, do not tell the user to "
            "trust their nose, use the phrase 'trust your nose', assess the odor, "
            "sniff, or stay in chemistry fundamentals "
            "by default. Treat wrong/strong chemical smell as an incidental warning: "
            "avoid inhalation, isolate, ventilate only from a safe position, and hand "
            "off to chemical safety or industrial accident response unless the prompt "
            "is explicitly only about feedstock/raw-material design with no exposure."
        )
    if _is_chemical_spill_sick_exposure_query(question or ""):
        notes.append(
            "- For chemical spill plus sick-person prompts, treat it as exposure triage "
            "first, not a chemistry-process question: isolate the area, move people to "
            "fresh air if safe, identify the exposure route, start water flushing for "
            "skin/eye contact, and escalate to Poison Control/EMS or chemical-safety "
            "response for symptoms. Do not lead with feedstock, precursor, or process "
            "troubleshooting."
        )
    if _is_precursor_feedstock_exposure_boundary_query(question or ""):
        notes.append(
            "- For precursor/feedstock versus poisoning/exposure boundary prompts, "
            "explain the split instead of using a blanket rule, and do not say every "
            "substance question is poisoning/exposure. Use feedstock/raw-material "
            "guides only when the user is designing a chemical product from known materials "
            "with no spill, illness, odor, contact, ingestion, or unknown container. Use "
            "chemical safety, toxicology, or industrial accident response first when there "
            "is exposure, symptoms, spill/leak, strong odor, or an unknown/unlabeled substance."
        )
    if _is_unlabeled_sealed_drum_safety_triage_query(question or ""):
        notes.append(
            "- For a sealed unlabeled drum with no symptoms, answer as unknown-chemical "
            "safety/disposal triage first: isolate, mark unknown/do not use, keep away "
            "from heat and people, check records/SDS/location only from a safe distance, "
            "and arrange trained disposal or hazmat review. Do not infer bitumen, "
            "petroleum residue, road repair, waterproofing, reuse, field-hospital triage, "
            "or feedstock handling."
        )
    if _is_market_space_layout_query(question or ""):
        notes.append(
            "- Physical market-space prompts should lead with marketplace trade-space "
            "layout: stalls, walking lanes, cart clearance, loading edge, blocked "
            "corners, and notices inside the market footprint. Use taxation only for "
            "explicit tax/levy/revenue prompts and bulletin systems only for broader "
            "public notices outside the market layout."
        )
    if _is_stretcher_access_query(question or ""):
        notes.append(
            "- Stretcher access prompts are not wheelchair-only door-width prompts. "
            "Lead with a direct outside-to-triage/treatment access door on the shortest "
            "firm route, wide/straight enough for stretcher handlers, with clear turning "
            "space and no step/threshold bottleneck."
        )
    if _is_electrical_hazard_query(question or ""):
        notes.append(
            "- Electrical hazard prompts: do not touch the person, wire, wet panel, "
            "or outlet first. Keep people back, de-energize from a dry safe place if "
            "possible, treat downed lines/live wires as energized, and only start "
            "CPR/AED or repair steps after the scene is safe."
        )
    if _is_downed_power_line_query(question or ""):
        notes.append(
            "- Downed power-line prompts: do not approach the line, do not try to "
            "move it, do not use a nonconductive object to separate it, and do not "
            "try to verify it personally. Do not say to verify power is off before "
            "approaching. Keep everyone far back, avoid vehicles, fences, puddles/wet "
            "ground, and anything touching the line, and call the utility/emergency "
            "responders to de-energize and ground it. Do not add breaker-box or "
            "sensitive-equipment troubleshooting to a downed-line answer."
        )
    if _is_drowning_cold_water_query(question or ""):
        notes.append(
            "- Active drowning/cold-water/ice rescue prompts: lead with rescuer safety "
            "and the reach/throw/row/go ladder, emergency help, breathing/CPR after "
            "rescue, and gentle warming. Do not open with routine swimming prevention "
            "or generic hypothermia background."
        )
    if _text_has_marker((question or "").lower(), {"face down", "silent in the water", "motionless in the water"}):
        notes.append(
            "- Face-down/silent/motionless-in-water prompts are active drowning "
            "emergencies: call/alert help and start safe reach/throw/rescue actions "
            "now. Do not lead with observe/assess/watchful waiting before rescue."
        )
    if _is_post_rescue_drowning_breathing_query(question or ""):
        notes.append(
            "- After water rescue, new coughing, shortness of breath, chest pain, "
            "confusion, or worsening breathing requires urgent medical evaluation "
            "now even if the person first seemed okay. The answer must explicitly "
            "say to get urgent medical evaluation/help now, then check breathing "
            "and start CPR/rescue breaths if breathing becomes abnormal."
        )
    if _is_urgent_nosebleed_query(question or ""):
        notes.append(
            "- Urgent nosebleed prompts: keep the person leaning forward and pinching "
            "the soft nose while arranging urgent medical help for bleeding beyond "
            "20-30 minutes, blood down the throat, blood thinners, dizziness/paleness/"
            "weakness, or repeated heavy same-day bleeding. Do not drift into dental, "
            "headache, or GI bleeding care unless those are explicitly present."
        )
    if _is_major_blood_loss_shock_query(question or ""):
        notes.append(
            "- Blood-loss shock prompts are trauma/hemorrhage-control first: control "
            "ongoing bleeding, keep the person flat and warm if breathing normally, "
            "monitor airway/breathing/mental status, and arrange urgent evacuation. "
            "Do not use nosebleed lean-forward/pinch-nose instructions, routine "
            "anatomy explanations, or vague drinking-fluids advice."
        )
    if _is_cardiac_first_query(question or ""):
        notes.append(
            "- Panic-versus-cardiac prompts with chest pressure/tightness, shortness "
            "of breath, dread, exertional symptoms, jaw/arm pain, or 'something feels "
            "wrong in my chest' must be cardiac-first: call emergency help or seek "
            "urgent medical evaluation before anxiety self-care, reassurance, or "
            "routine monitoring."
        )
    if _is_eye_globe_injury_query(question or ""):
        notes.append(
            "- Embedded, high-speed, penetrating, or vision-change eye trauma: do "
            "not flush, remove, rub, press, or apply direct pressure. Shield the "
            "eye without pressure, reduce eye movement, and seek urgent eye care."
        )
    if _is_retinal_detachment_eye_emergency_query(question or ""):
        notes.append(
            "- Sudden flashes/floaters, a curtain/shadow, or sudden one-eye vision "
            "loss is an urgent eye emergency: lead with possible retinal detachment "
            "or retinal/optic-nerve emergency and urgent ophthalmology/evacuation. "
            "Do not treat as migraine, eye strain, glasses, pink-eye, night vision, "
            "navigation, or signaling first."
        )
    if missing:
        notes.append(
            "- For objectives marked missing, give only the closest grounded guidance and say where support is thin."
        )
    return notes


@lru_cache(maxsize=1)
def _runtime_answer_cards():
    return _answer_card_runtime._runtime_answer_cards(_load_guide_answer_cards)


def _card_source_guide_ids(card):
    return _answer_card_runtime._card_source_guide_ids(card)


def _card_runtime_citation_guide_ids(card, allowed_guide_ids):
    return _answer_card_runtime._card_runtime_citation_guide_ids(
        card,
        allowed_guide_ids,
        citation_allowlist_from_card_sources=_citation_allowlist_from_card_sources,
    )


def _citation_allowlist_from_card_sources(card):
    return _answer_card_runtime._citation_allowlist_from_card_sources(card)


def _answer_cards_for_results(results, *, question=None, max_cards=2, max_source_ids=2):
    return _answer_card_runtime._answer_cards_for_results(
        results,
        question=question,
        max_cards=max_cards,
        max_source_ids=max_source_ids,
        runtime_answer_cards=_runtime_answer_cards,
        citation_allowlist_from_results=_citation_allowlist_from_results,
        prioritized_answer_card_ids_for_question=_prioritized_answer_card_ids_for_question,
        answer_card_matches_question=_answer_card_matches_question,
        card_source_guide_ids=_card_source_guide_ids,
    )


def _is_poisoning_unknown_ingestion_card_query(question):
    lower = (question or "").lower()
    if not lower or "food poisoning" in lower:
        return False

    route = _text_has_marker(
        lower,
        {
            "swallow",
            "swallowed",
            "swallowing",
            "ingest",
            "ingested",
            "ingestion",
            "ate",
            "eaten",
            "tasted",
            "tasting",
            "taste",
            "drank",
            "drink",
            "sipped",
            "licked",
            "took",
            "taken",
        },
    )
    source = _text_has_marker(
        lower,
        {
            "medicine",
            "medication",
            "pills",
            "pill",
            "tablet",
            "capsule",
            "drug",
            "cleaner",
            "chemical",
            "liquid",
            "bottle",
            "unlabeled",
            "unknown",
            "not sure",
            "something",
            "poison",
            "poisoning",
            "overdose",
        },
    )
    danger = _text_has_marker(
        lower,
        {
            "possible",
            "maybe",
            "might have",
            "not sure",
            "unknown",
            "unlabeled",
            "sleepy",
            "drowsy",
            "hard to wake",
            "acting off",
            "confused",
            "mouth burns",
            "burning mouth",
            "burned mouth",
            "throat burns",
            "drooling",
            "vomiting",
            "overdose",
            "poison",
            "poisoning",
            "poison control",
        },
    )
    explicit_poisoning = _text_has_marker(
        lower, {"poison", "poisoning", "poison control", "overdose", "toxidrome"}
    )
    if _has_allergy_or_anaphylaxis_trigger(question) and not explicit_poisoning:
        return False
    return explicit_poisoning or (route and source and danger)


def _prioritized_answer_card_ids_for_question(question):
    return _answer_card_runtime._prioritized_answer_card_ids_for_question(
        question,
        is_airway_obstruction_rag_query=_is_airway_obstruction_rag_query,
        has_allergy_or_anaphylaxis_trigger=_has_allergy_or_anaphylaxis_trigger,
        is_newborn_sepsis_danger_query=_is_newborn_sepsis_danger_query,
        is_meningitis_rash_emergency_query=_is_meningitis_rash_emergency_query,
        is_poisoning_unknown_ingestion_card_query=_is_poisoning_unknown_ingestion_card_query,
        is_infected_wound_card_query=_is_infected_wound_boundary_query,
    )


def _answer_card_matches_question(card, question):
    return _answer_card_runtime._answer_card_matches_question(
        card,
        question,
        is_airway_obstruction_rag_query=_is_airway_obstruction_rag_query,
        has_allergy_or_anaphylaxis_trigger=_has_allergy_or_anaphylaxis_trigger,
        is_newborn_sepsis_danger_query=_is_newborn_sepsis_danger_query,
        is_meningitis_rash_emergency_query=_is_meningitis_rash_emergency_query,
        is_poisoning_unknown_ingestion_card_query=_is_poisoning_unknown_ingestion_card_query,
        is_infected_wound_card_query=_is_infected_wound_boundary_query,
    )


def _format_card_items(items, *, limit=3):
    return _answer_card_runtime._format_card_items(items, limit=limit)


def _stringify_card_item(item):
    return _answer_card_runtime._stringify_card_item(item)


def _active_conditional_card_actions(card, question):
    return _answer_card_runtime._active_conditional_card_actions(card, question)


def _answer_card_contract_block(question, results, *, prompt_token_limit=None):
    return _answer_card_runtime._answer_card_contract_block(
        question,
        results,
        prompt_token_limit=prompt_token_limit,
        answer_cards_for_results=_answer_cards_for_results,
        build_guide_evidence_packet=_build_guide_evidence_packet,
        format_card_items=_format_card_items,
        estimate_tokens=estimate_tokens,
    )


def _card_backed_runtime_answer_plan(question, results):
    return _answer_card_runtime._card_backed_runtime_answer_plan(
        question,
        results,
        answer_cards_for_results=_answer_cards_for_results,
        citation_allowlist_from_results=_citation_allowlist_from_results,
        card_runtime_citation_guide_ids=_card_runtime_citation_guide_ids,
        compose_guide_card_backed_answer=_compose_guide_card_backed_answer,
    )


def _card_backed_runtime_answer(question, results):
    return _answer_card_runtime._card_backed_runtime_answer(
        question,
        results,
        card_backed_runtime_answer_plan=_card_backed_runtime_answer_plan,
    )


_AIRWAY_OBSTRUCTION_OWNER_GUIDE_IDS = {"GD-232", "GD-579", "GD-298", "GD-284", "GD-617"}
_AIRWAY_OBSTRUCTION_CONTEXT_ROW_MARKERS = {
    "choking",
    "choke",
    "foreign body",
    "food bolus",
    "food stuck",
    "stuck in the throat",
    "cannot speak",
    "can't speak",
    "cannot cough",
    "can't cough",
    "cannot breathe",
    "can't breathe",
    "cannot cry",
    "can't cry",
    "back blows",
    "abdominal thrust",
    "abdominal thrusts",
    "chest thrust",
    "chest thrusts",
    "heimlich",
    "complete obstruction",
    "partial obstruction",
    "clearly visible object",
    "visible object",
    "blind finger sweep",
    "blind finger sweeps",
}


def _airway_obstruction_context_row_allowed(meta, doc=""):
    meta = meta or {}
    guide_id = str(meta.get("guide_id") or "").strip().upper()
    if guide_id not in _AIRWAY_OBSTRUCTION_OWNER_GUIDE_IDS:
        return False
    row_text = " ".join(
        str(value or "")
        for value in (
            meta.get("guide_title"),
            meta.get("section_heading"),
            meta.get("source_file"),
            meta.get("slug"),
            doc,
        )
    ).lower()
    if _text_has_marker(row_text, _AIRWAY_OBSTRUCTION_CONTEXT_ROW_MARKERS):
        return True
    return "drooling" in row_text and (
        "cannot swallow" in row_text or "can't swallow" in row_text
    )


def _airway_obstruction_allowed_guide_ids_from_results(results):
    metadatas = (results or {}).get("metadatas") or []
    documents = (results or {}).get("documents") or []
    if not metadatas or not metadatas[0]:
        return []
    docs = documents[0] if documents else []
    allowed = []
    seen = set()
    for index, meta in enumerate(metadatas[0]):
        doc = docs[index] if index < len(docs) else ""
        if not _airway_obstruction_context_row_allowed(meta, doc):
            continue
        guide_id = str((meta or {}).get("guide_id") or "").strip().upper()
        if guide_id and guide_id not in seen:
            seen.add(guide_id)
            allowed.append(guide_id)
    return allowed


_CONTEXT_COMPACTION_DOC_CHAR_LIMITS = (1200, 900, 700, 520, 380, 260, 180)


def _compact_context_block_doc(block, max_doc_chars):
    """Shorten a guide excerpt body while preserving its citation header."""
    header, separator, body = (block or "").partition("\n")
    if not separator:
        return block
    body = body.strip()
    if len(body) <= max_doc_chars:
        return block
    compact_body = body[:max_doc_chars].rstrip()
    return f"{header}\n{compact_body} ..."


def _context_prompt_fit_limit(prompt_token_limit):
    """Return the target prompt estimate for context compaction."""
    if not prompt_token_limit:
        return None
    prompt_token_limit = int(prompt_token_limit)
    prompt_safety_margin = int(getattr(config, "PROMPT_TOKEN_SAFETY_MARGIN", 96))
    fit_limit = prompt_token_limit - prompt_safety_margin
    if prompt_token_limit <= 4096:
        fit_limit -= 256
    return max(fit_limit, 0)


def _fit_context_blocks_to_prompt_budget(
    context_blocks,
    render_prompt,
    *,
    prompt_token_limit=None,
    mode="default",
):
    """Progressively compact guide excerpts until the rendered prompt fits."""
    prompt = render_prompt(context_blocks)
    fit_limit = _context_prompt_fit_limit(prompt_token_limit)
    if not fit_limit:
        return context_blocks, prompt
    if (
        _estimate_chat_prompt_tokens(prompt, use_system_prompt=True, mode=mode)[
            "estimated_prompt_tokens"
        ]
        <= fit_limit
    ):
        return context_blocks, prompt

    best_blocks = context_blocks
    best_prompt = prompt
    for max_doc_chars in _CONTEXT_COMPACTION_DOC_CHAR_LIMITS:
        compacted_blocks = [
            _compact_context_block_doc(block, max_doc_chars)
            for block in context_blocks
        ]
        compacted_prompt = render_prompt(compacted_blocks)
        best_blocks = compacted_blocks
        best_prompt = compacted_prompt
        if (
            _estimate_chat_prompt_tokens(
                compacted_prompt,
                use_system_prompt=True,
                mode=mode,
            )["estimated_prompt_tokens"]
            <= fit_limit
        ):
            return compacted_blocks, compacted_prompt
    return best_blocks, best_prompt


def build_prompt(
    question, results, mode="default", session_state=None, prompt_token_limit=None
):
    """Build the context-augmented prompt from retrieved chunks."""
    review = results.get("_senku", {})
    annotations = review.get("result_annotations", [])
    context_blocks = []
    distances = results.get("distances", [[]])[0]
    docs = results["documents"][0]
    metas = results["metadatas"][0]
    strict_airway_owner_context = (
        _is_airway_obstruction_rag_query(question or "")
        and not _has_allergy_or_anaphylaxis_trigger(question or "")
        and any(
            _airway_obstruction_context_row_allowed(meta, doc)
            for doc, meta in zip(docs, metas)
        )
    )
    for i, (doc, meta) in enumerate(zip(docs, metas)):
        meta = meta or {}
        guide_id = str(meta.get("guide_id") or "").strip().upper()
        if strict_airway_owner_context and not _airway_obstruction_context_row_allowed(meta, doc):
            continue
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
        gen_url=getattr(config, "GEN_URL", config.LM_STUDIO_URL),
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
        or _is_gi_bleed_emergency_query(question)
        or _is_surgical_abdomen_emergency_query(question)
        or _is_gyn_emergency_query(question)
        or _is_crush_compartment_query(question)
        or _is_possible_spinal_injury_movement_query(question)
        or _is_serotonin_syndrome_query(question)
        or _is_meningitis_rash_retrieval_query(question)
        or _is_eye_globe_injury_query(question)
        or _is_electrical_hazard_query(question)
        or _is_drowning_cold_water_query(question)
        or _is_urgent_nosebleed_query(question)
        or _is_major_blood_loss_shock_query(question)
        or _is_cardiac_first_query(question)
        or _is_unknown_chemical_skin_burn_query(question)
        or _is_industrial_chemical_smell_boundary_query(question)
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
    if _is_urinary_vaginal_overlap_query(question):
        prompt_notes.append(
            "- Urinary burning plus vaginal itching/discharge is an overlap symptom question: explain that UTI, vaginal infection/irritation, or STI can overlap, recommend evaluation if new, persistent, recurrent, severe, or paired with pelvic pain/fever/flank pain, and do not name antibiotic choices or doses unless the user explicitly asks about a prescribed medication."
        )
    if _is_hematuria_query(question):
        prompt_notes.append(
            "- Blood in urine is itself a urinary red-flag question: say it warrants medical evaluation, with urgent escalation for clots/heavy bleeding, inability to urinate, flank/back pain, fever, worsening severe lower abdominal/bladder pain, pregnancy, fainting, or confusion. Do not frame it as only monitor for other signs first, and do not import microscopy workflow, STI-only red flags, nosebleed firm-pressure timing, cough/cold red flags, rectal bleeding, or stool/hemorrhoid guidance unless those symptoms are explicitly present."
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
    if (
        _is_household_chemical_hazard_query(question)
        or _is_unknown_leaking_chemical_container_query(question)
        or _is_unknown_loose_chemical_powder_query(question)
    ):
        if compact_litert_notes:
            prompt_notes.append(
                "- Chemical exposure: separate people from the source, ventilate if safe, flush eye/skin exposure with water, and escalate to Poison Control or EMS before home remedies."
            )
            if _is_unknown_chemical_skin_burn_query(question):
                prompt_notes.append(
                    "- Burning skin after touching an unknown or unlabeled bottle: treat it as possible chemical burn/toxic exposure, flush with running water, remove contaminated items, call Poison Control or EMS, and do not answer as poison ivy."
                )
            if _is_unknown_leaking_chemical_container_query(question):
                prompt_notes.append(
                    "- Unknown leaking chemical container with sharp smell: evacuate/isolate and do not touch, open, identify, or move the container yourself."
                )
            if _is_unknown_loose_chemical_powder_query(question):
                prompt_notes.append(
                    "- Unknown loose powder or residue: isolate the area. Do not move, sweep, brush, smell, taste, or identify it by handling."
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
            if _is_unknown_chemical_skin_burn_query(question):
                prompt_notes.append(
                    "- For burning skin after touching an unknown or unlabeled bottle, "
                    "treat it as possible chemical burn/toxic exposure: start running-"
                    "water flushing, remove contaminated clothing/jewelry, call Poison "
                    "Control or EMS, and do not answer as poison ivy, contact rash, or "
                    "routine skin irritation."
                )
            if _is_unknown_leaking_chemical_container_query(question):
                prompt_notes.append(
                    "- For an unknown leaking chemical container with sharp smell, "
                    "evacuate/isolate and do not touch, open, identify, or move the "
                    "container yourself. Mark or secure the area from a safe distance "
                    "only; wait for trained/hazmat help or Poison Control guidance."
                )
            if _is_unknown_loose_chemical_powder_query(question):
                prompt_notes.append(
                    "- For unknown loose powder, residue, granules, or crystals that "
                    "could be detergent, fertilizer, pesticide, cleaner, poison, or "
                    "another chemical, isolate the area and keep people/pets away. "
                    "Do not move, sweep, brush, smell, taste, identify by handling, "
                    "or package the powder yourself; wait for Poison Control, trained "
                    "cleanup, or hazmat guidance."
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
    if _is_adhesive_binder_query(question):
        prompt_notes.append(
            "- For simple adhesive/binder family prompts, give a concise complete "
            "material-fit answer: wood, leather, paper, and containers, with one "
            "safety/process-limit note. Do not stop mid-list or drift into soap, "
            "bleach, fuel, or dye chemistry."
        )
    if _is_message_auth_query(question):
        prompt_notes.append(
            "- For courier/message authenticity prompts, stay on message "
            "authentication and courier protocol: sender identity, tamper evidence, "
            "challenge-response, trusted courier roster, posted-order verification, "
            "and chain-of-custody log. Do not answer as wildfire evacuation planning, "
            "generic emergency management, medication triage, water treatment, or "
            "forensic evidence unless those are explicitly secondary examples."
        )
        if _is_simple_courier_note_auth_query(question):
            prompt_notes.append(
                "- For simple low-fragility courier note authentication, lead with "
                "one durable shared sender mark/seal plus a challenge-response or "
                "callback path and a trusted courier roster. Do not require witnesses, "
                "full chain-of-custody, one-time-pad checksums, or heavy bureaucracy "
                "as the simplest baseline."
            )
        if _is_posted_order_verification_query(question):
            prompt_notes.append(
                "- For a posted evacuation/order verification prompt, lead with "
                "checking the issuing authority or pre-agreed authentication signal "
                "through a trusted second channel before acting, unless there is "
                "immediate visible danger. If verification fails or is delayed, do not "
                "tell people simply to abort/ignore protective action; escalate to the "
                "duty authority or callback path, warn that the order is unverified, "
                "and move to a safer interim posture if there is visible danger. "
                "Physical condition/copy comparison is secondary."
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
    if _is_runoff_infant_formula_boundary_query(question):
        prompt_notes.append(
            "- For infant formula with roof runoff or a flood-affected well, do not use the ordinary water-purification template as a reassurance. Lead with the infant/high-risk contamination boundary, prefer the safest known or official/tested water source, and state that boiling does not prove chemical, sewage, roof-material, or flood contamination safe."
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
    frame_text = "\n".join(line for line in frame_lines if line)
    coverage_text = "\n".join(f"- {line}" for line in coverage_lines)
    extra_notes = "\n".join(prompt_notes)
    extra_notes_block = f"{extra_notes}\n" if extra_notes else ""
    answer_card_contract = _answer_card_contract_block(
        question,
        results,
        prompt_token_limit=prompt_token_limit,
    )
    answer_card_contract_block = (
        f"{answer_card_contract}\n\n" if answer_card_contract else ""
    )
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

    def render_prompt(active_context_blocks, active_answer_card_contract_block):
        reference = "\n---\n".join(active_context_blocks)
        return (
            f"Scenario Frame:\n{frame_text or '- none'}\n\n"
            f"Coverage Check:\n{coverage_text or '- none'}\n\n"
            f"Guide Excerpts:\n---\n{reference}\n---\n\n"
            f"{active_answer_card_contract_block}"
            f"Using the guides above, answer the following question. "
            f"Apply relevant principles even if no guide mentions the user's "
            f"exact scenario by name. If the question is vague, prioritize: "
            f"shelter, water, fire, food, signaling.\n\n"
            f"Response requirements:\n"
            f"- Lead with the most useful immediate action, check, or conclusion in the first sentence.\n"
            f"- If the user asks what to do right now, what to worry about first, or before anything else, or the scenario is clearly urgent/safety-critical, answer with a compact 3-4 step numbered immediate-action list. Put the first concrete action first, keep each step imperative and specific, and do not stop after a warning sentence or lead paragraph.\n"
            f"- If there is an immediate danger, use the same compact list pattern when it fits: Do first, Avoid, Escalate if, then brief supporting steps.\n"
            f"- Every numbered step must be a complete sentence with its action included; do not end on a bare conditional heading like 'If unresponsive:' or a dangling 'If'.\n"
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

    prompt = render_prompt(context_blocks, answer_card_contract_block)
    if answer_card_contract_block and prompt_token_limit:
        prompt_safety_margin = int(getattr(config, "PROMPT_TOKEN_SAFETY_MARGIN", 96))
        safe_limit = prompt_token_limit - prompt_safety_margin
        card_contract_margin = 512
        if safe_limit <= 0 or _estimate_chat_prompt_tokens(
            prompt,
            use_system_prompt=True,
            mode=mode,
        )["estimated_prompt_tokens"] > safe_limit - card_contract_margin:
            answer_card_contract_block = ""
            prompt = render_prompt(context_blocks, answer_card_contract_block)
    context_blocks, prompt = _fit_context_blocks_to_prompt_budget(
        context_blocks,
        lambda active_blocks: render_prompt(active_blocks, answer_card_contract_block),
        prompt_token_limit=prompt_token_limit,
        mode=mode,
    )
    return _add_citation_allowlist_contract(
        prompt,
        results,
        mode=mode,
        prompt_token_limit=prompt_token_limit,
        question=question,
    )


def _extract_gd_ids(text):
    """Extract recoverable GD citation IDs from noisy model output."""
    return _response_normalization._extract_gd_ids(text)


def _normalize_citation_group(match):
    """Deduplicate and normalize a single bracketed GD citation group."""
    return _response_normalization._normalize_citation_group(match)


MAX_INLINE_CITATIONS_PER_LINE = _response_normalization.MAX_INLINE_CITATIONS_PER_LINE
MAX_INLINE_CITATIONS_PER_STEP_LINE = _response_normalization.MAX_INLINE_CITATIONS_PER_STEP_LINE
_WARNING_RESIDUAL_BRACKET_PATTERN = _response_normalization._WARNING_RESIDUAL_BRACKET_PATTERN
_WARNING_RESIDUAL_CITATION_PATTERN = _response_normalization._WARNING_RESIDUAL_CITATION_PATTERN
_WARNING_RESIDUAL_PREFIXES = _response_normalization._WARNING_RESIDUAL_PREFIXES
_WARNING_RESIDUAL_EXACT_LABELS = _response_normalization._WARNING_RESIDUAL_EXACT_LABELS
_WARNING_RESIDUAL_TRAIL_MARKERS = _response_normalization._WARNING_RESIDUAL_TRAIL_MARKERS


def _compress_citations_on_line(line):
    """Collapse repeated citation groups on one output line into one small cluster."""
    return _response_normalization._compress_citations_on_line(line)


def _rewrite_line_citations(line, citations):
    """Rewrite one line with a normalized set of inline citations."""
    return _response_normalization._rewrite_line_citations(line, citations)


def _compress_citations_across_numbered_steps(text):
    """Remove repeated citations inside one numbered-step block while keeping new sources."""
    return _response_normalization._compress_citations_across_numbered_steps(text)


_FORBIDDEN_RESPONSE_PHRASE_REPLACEMENTS = (
    _response_normalization._FORBIDDEN_RESPONSE_PHRASE_REPLACEMENTS
)


def _match_replacement_case(source, replacement):
    return _response_normalization._match_replacement_case(source, replacement)


def _scrub_retrieval_mechanism_language(text):
    """Rewrite retrieval-mechanism phrasing into guide-facing language."""
    return _response_normalization._scrub_retrieval_mechanism_language(text)


def _is_warning_residual_bracket(label):
    """Return True for bracketed control/warning residue that is not a citation."""
    return _response_normalization._is_warning_residual_bracket(label)


def _strip_warning_residual_brackets(text):
    """Remove stale bracketed warning/instruction labels while keeping real citations."""
    return _response_normalization._strip_warning_residual_brackets(text)


def normalize_response_text(text):
    """Normalize common model-output citation/pathology issues."""
    return _response_normalization.normalize_response_text(
        text,
        valid_guide_ids_provider=all_guide_ids,
        warn_event=_log_warn_event,
    )


def _drop_unknown_guide_citations(text):
    """Remove citations that do not exist in the live guide catalog."""
    return _response_normalization._drop_unknown_guide_citations(
        text,
        valid_guide_ids_provider=all_guide_ids,
        warn_event=_log_warn_event,
    )


def _duplicate_citation_count(text):
    """Return the number of repeated guide citations in a response."""
    return _response_normalization._duplicate_citation_count(text)


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
    return _answer_card_runtime._citation_allowlist_from_results(results)


def _prioritized_citation_allowlist_for_question(question, guide_ids):
    return _citation_policy._prioritized_citation_allowlist_for_question(
        question,
        guide_ids,
        is_airway_obstruction_rag_query=_is_airway_obstruction_rag_query,
        is_meningitis_rash_emergency_query=_is_meningitis_rash_emergency_query,
        is_meningitis_vs_viral_query=_is_meningitis_vs_viral_query,
        is_newborn_sepsis_danger_query=_is_newborn_sepsis_danger_query,
        is_abdominal_trauma_danger_query=_is_abdominal_trauma_danger_query,
    )


def _citation_guide_ids_for_question(question, results, raw_allowed_guide_ids):
    return _citation_policy._citation_guide_ids_for_question(
        question,
        results,
        raw_allowed_guide_ids,
        prioritized_citation_allowlist_for_question=_prioritized_citation_allowlist_for_question,
        is_airway_obstruction_rag_query=_is_airway_obstruction_rag_query,
        has_allergy_or_anaphylaxis_trigger=_has_allergy_or_anaphylaxis_trigger,
        airway_obstruction_allowed_guide_ids_from_results=_airway_obstruction_allowed_guide_ids_from_results,
        is_meningitis_vs_viral_query=_is_meningitis_vs_viral_query,
        is_meningitis_rash_emergency_query=_is_meningitis_rash_emergency_query,
    )


def _add_citation_allowlist_contract(
    prompt, results, *, mode="default", prompt_token_limit=None, question=None
):
    """Restrict the model to citing only guide IDs retrieved for this prompt."""
    raw_allowed_guide_ids = _citation_allowlist_from_results(results)
    allowed_guide_ids = _citation_guide_ids_for_question(
        question, results, raw_allowed_guide_ids
    )
    if not allowed_guide_ids:
        return prompt

    if "Citation contract for this answer:" in (prompt or ""):
        return prompt

    allowed_tokens = ", ".join(f"[{guide_id}]" for guide_id in allowed_guide_ids)
    priority_note = ""
    if question:
        priority_note = (
            " Prefer the earliest listed retrieved guide that directly supports "
            "the lead action, main sequence, or main comparison before citing "
            "supporting guides."
        )
    contract = (
        "Citation contract for this answer: every guide citation must use one of "
        f"these exact retrieved guide IDs only: {allowed_tokens}. "
        "Do not invent, infer, or reuse any other guide ID, and do not output any "
        "other [GD-###] token. If support is missing from these retrieved guides, "
        "leave the claim uncited or say you do not have a retrieved guide source "
        f"for it.{priority_note}"
    )
    prompt_with_contract = f"{contract}\n\n{prompt}"
    runtime_prompt_limit = prompt_token_limit or _prompt_token_limit(
        gen_model=config.GEN_MODEL,
        gen_url=getattr(config, "GEN_URL", config.LM_STUDIO_URL),
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
    return _prompt_runtime.system_prompt_text(config, mode)


def _prompt_token_limit(gen_model=None, gen_url=None):
    """Return the configured prompt-window limit for the active runtime profile."""
    return _prompt_runtime.prompt_token_limit_from_config(config, gen_model, gen_url)


def _estimate_chat_prompt_tokens(
    prompt_text, *, use_system_prompt=True, mode="default", system_prompt_text=None
):
    """Estimate tokens for the live chat request before generation."""
    return _prompt_runtime.estimate_chat_prompt_tokens(
        prompt_text,
        estimate_tokens_fn=estimate_tokens,
        system_prompt_resolver=_system_prompt_text,
        use_system_prompt=use_system_prompt,
        mode=mode,
        system_prompt_text=system_prompt_text,
    )


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
    return _completion_hardening._numbered_step_numbers(text)


def _has_malformed_trailing_citation(text):
    return _completion_hardening._has_malformed_trailing_citation(text)


def _is_obviously_incomplete_crisis_response(text):
    return _completion_hardening._is_obviously_incomplete_crisis_response(text)


def _build_crisis_retry_messages(system_prompt, prompt):
    return _completion_hardening._build_crisis_retry_messages(system_prompt, prompt)


_ABSTAIN_ROW_LIMIT = _abstain_policy.ABSTAIN_ROW_LIMIT
_ABSTAIN_MAX_OVERLAP_TOKENS = _abstain_policy.ABSTAIN_MAX_OVERLAP_TOKENS
_ABSTAIN_MIN_VECTOR_SIMILARITY = _abstain_policy.ABSTAIN_MIN_VECTOR_SIMILARITY
_ABSTAIN_MIN_UNIQUE_LEXICAL_HITS = _abstain_policy.ABSTAIN_MIN_UNIQUE_LEXICAL_HITS


def _abstain_top_rows(results, *, limit=_ABSTAIN_ROW_LIMIT):
    return _abstain_policy._abstain_top_rows(results, limit=limit)


def _abstain_row_overlap_tokens(query_tokens, doc, meta):
    return _abstain_policy._abstain_row_overlap_tokens(
        query_tokens,
        doc,
        meta,
        content_tokens=_content_tokens,
    )


def _abstain_row_vector_similarity(meta, dist):
    return _abstain_policy._abstain_row_vector_similarity(meta, dist)


def _abstain_match_label(overlap_count, vector_similarity, lexical_hits):
    return _abstain_policy._abstain_match_label(
        overlap_count,
        vector_similarity,
        lexical_hits,
        min_vector_similarity=_ABSTAIN_MIN_VECTOR_SIMILARITY,
    )


def _should_abstain(results, query):
    return _abstain_policy._should_abstain(
        results,
        query,
        content_tokens=_content_tokens,
        row_limit=_ABSTAIN_ROW_LIMIT,
        max_overlap_tokens=_ABSTAIN_MAX_OVERLAP_TOKENS,
        min_vector_similarity=_ABSTAIN_MIN_VECTOR_SIMILARITY,
        min_unique_lexical_hits=_ABSTAIN_MIN_UNIQUE_LEXICAL_HITS,
    )


def _truncate_abstain_query(query, *, limit=60):
    return _abstain_policy._truncate_abstain_query(query, limit=limit)


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
        or _is_gi_bleed_emergency_query(question)
        or _is_surgical_abdomen_emergency_query(question)
        or _is_gyn_emergency_query(question)
        or _is_crush_compartment_query(question)
        or _is_serotonin_syndrome_query(question)
        or _is_meningitis_rash_retrieval_query(question)
        or _is_eye_globe_injury_query(question)
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
    if _review_marks_safety_critical(results, scenario_frame) and not _scenario_frame_is_safety_critical(
        scenario_frame
    ):
        scenario_frame = dict(scenario_frame)
        scenario_frame["safety_critical"] = True
        review["scenario_frame"] = scenario_frame
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

    card_backed_answer = _card_backed_runtime_answer(question, results)
    if card_backed_answer:
        console.print()
        console.print(card_backed_answer, highlight=False, markup=False)
        console.print()
        return card_backed_answer

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
        gen_url=getattr(config, "GEN_URL", config.LM_STUDIO_URL),
    )
    prompt = build_prompt(
        question,
        results,
        mode=mode,
        session_state=session_state,
        prompt_token_limit=prompt_token_limit,
    )
    prompt = _add_multi_objective_answer_shape(prompt, question)
    gen_url = getattr(config, "GEN_URL", config.LM_STUDIO_URL)
    url = f"{gen_url}/chat/completions"
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
        "--gen-url",
        type=str,
        default=getattr(config, "GEN_URL", config.LM_STUDIO_URL),
        help=(
            "Generation endpoint override "
            f"(default: {getattr(config, 'GEN_URL', config.LM_STUDIO_URL)})"
        ),
    )
    parser.add_argument(
        "--embed-url",
        type=str,
        default=getattr(config, "EMBED_URL", config.LM_STUDIO_URL),
        help=(
            "Embedding/retrieval endpoint override "
            f"(default: {getattr(config, 'EMBED_URL', config.LM_STUDIO_URL)})"
        ),
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
    config.GEN_URL = normalize_lm_studio_url(args.gen_url)
    config.EMBED_URL = normalize_lm_studio_url(args.embed_url)
    config.LM_STUDIO_URL = config.EMBED_URL
    if args.top_k is None:
        args.top_k = config.get_runtime_top_k(config.GEN_MODEL, config.GEN_URL)

    # Test local endpoints.
    try:
        requests.get(f"{config.GEN_URL}/models", timeout=5)
    except requests.ConnectionError:
        console.print(
            f"[red]Cannot connect to generation endpoint at {config.GEN_URL}. "
            "Make sure the LiteRT/LM Studio generation server is running.[/red]"
        )
        sys.exit(1)
    try:
        requests.get(f"{config.EMBED_URL}/models", timeout=5)
    except requests.ConnectionError:
        console.print(
            f"[red]Cannot connect to embedding endpoint at {config.EMBED_URL}. "
            "Make sure the embedding-capable LM Studio server is running.[/red]"
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
            + f" | gen={config.GEN_URL}"
            + f" | embed={config.EMBED_URL}"
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
                lm_studio_url=config.EMBED_URL,
            )
        except requests.ConnectionError:
            console.print(
                f"[red]Cannot connect to embedding endpoint at {config.EMBED_URL}. "
                "Make sure the embedding-capable LM Studio server is running.[/red]"
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
                f"\n[red]Cannot connect to generation endpoint at {config.GEN_URL}. "
                "Make sure the LiteRT/LM Studio generation server is running.[/red]"
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
