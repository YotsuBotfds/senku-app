#!/usr/bin/env python3
"""Minimal headless Android-parity answer runner for exported mobile packs."""

from __future__ import annotations

import argparse
import json
import re
import sqlite3
import struct
import sys
import time
from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path
from typing import Any, Iterable, Sequence

import numpy as np
import requests


REPO_ROOT = Path(__file__).resolve().parent.parent
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))


VECTOR_HEADER_STRUCT = struct.Struct("<8s6I")
VECTOR_MAGIC = b"SNKUVEC1"
VECTOR_DTYPE_FLOAT16 = 1
VECTOR_DTYPE_INT8 = 2

DEFAULT_PACK_MANIFEST = (
    REPO_ROOT
    / "android-app"
    / "app"
    / "src"
    / "main"
    / "assets"
    / "mobile_pack"
    / "senku_manifest.json"
)
DEFAULT_OUTPUT_DIR = REPO_ROOT / "artifacts" / "bench" / "mobile_headless_answers_20260412"

HYBRID_RRF_K = 60
DEFAULT_SEARCH_LIMIT = 16
DEFAULT_CONTEXT_LIMIT = 4
DEFAULT_CONTEXT_ITEMS = 2
DEFAULT_SOURCE_ITEMS = 3
LEXICAL_CANDIDATE_LIMIT = 72
VECTOR_NEIGHBOR_LIMIT = 28
VECTOR_SEED_COUNT = 6
DEFAULT_HOST_TIMEOUT_SECONDS = 120
DEFAULT_HOST_MAX_TOKENS = 2048
HOST_TEMPERATURE = 0.11


def safe_text(value: Any) -> str:
    return "" if value is None else str(value)


def normalize_match_text(text: str) -> str:
    return re.sub(r"[^a-z0-9]+", " ", safe_text(text).lower()).strip()


def normalize_whitespace(text: str) -> str:
    return re.sub(r"\s+", " ", safe_text(text)).strip()


def contains_term(haystack: str, needle: str) -> bool:
    normalized_haystack = normalize_match_text(haystack)
    normalized_needle = normalize_match_text(needle)
    if not normalized_haystack or not normalized_needle:
        return False
    return f" {normalized_needle} " in f" {normalized_haystack} "


def contains_any(text: str, markers: Iterable[str]) -> bool:
    return any(contains_term(text, marker) for marker in markers)


def count_matches(text: str, markers: Iterable[str]) -> int:
    return sum(1 for marker in markers if contains_term(text, marker))


def split_csv(value: str) -> list[str]:
    return [part.strip().lower() for part in safe_text(value).split(",") if part.strip()]


def clip(text: str, limit: int) -> str:
    compact = normalize_whitespace(text)
    if len(compact) <= limit:
        return compact
    return compact[: max(0, limit - 3)].rstrip() + "..."


def reciprocal_rank(rank: int) -> float:
    return 1.0 / (HYBRID_RRF_K + rank + 1.0)


def utc_now_iso() -> str:
    return datetime.now().astimezone().isoformat()


def timestamp_slug() -> str:
    return datetime.now().strftime("%Y%m%d_%H%M%S")


def resolve_path(value: str | None, default: Path | None = None, *, base_dir: Path | None = None) -> Path:
    if value:
        candidate = Path(value)
    elif default is not None:
        candidate = default
    else:
        raise ValueError("No path value or default provided")
    if candidate.is_absolute():
        return candidate.resolve()
    anchor = base_dir if base_dir is not None else REPO_ROOT
    return (anchor / candidate).resolve()


def normalize_answer_excerpt(text: str) -> str:
    normalized = safe_text(text)
    normalized = normalized.replace("\\.", ".")
    normalized = normalized.replace("$", "")
    normalized = re.sub(r"\\text\{([^}]*)\}", r"\1", normalized)
    normalized = normalized.replace("\\", "")
    normalized = normalized.replace("{", "")
    normalized = normalized.replace("}", "")
    normalized = re.sub(r"`+", "", normalized)
    normalized = re.sub(r"\*\*|__|\*", "", normalized)
    normalized = re.sub(r"(?m)(^|\s)#+\s*", " ", normalized)
    return normalize_whitespace(normalized)


SESSION_CONTEXT_HINTS = frozenset(
    {
        "what now",
        "what next",
        "and then",
        "what about",
        "how long",
        "should we",
        "do we",
        "next step",
        "next steps",
        "that",
        "it",
        "this",
        "those",
    }
)
STOP_TOKENS = frozenset(
    {
        "a",
        "about",
        "an",
        "and",
        "are",
        "at",
        "be",
        "but",
        "by",
        "can",
        "do",
        "does",
        "for",
        "from",
        "how",
        "i",
        "if",
        "in",
        "into",
        "is",
        "it",
        "me",
        "my",
        "of",
        "on",
        "or",
        "our",
        "should",
        "so",
        "that",
        "the",
        "their",
        "them",
        "there",
        "these",
        "they",
        "this",
        "to",
        "use",
        "using",
        "we",
        "what",
        "when",
        "where",
        "which",
        "who",
        "why",
        "with",
        "you",
        "your",
    }
)
LOW_SIGNAL_TOKENS = frozenset(
    {
        "build",
        "create",
        "do",
        "find",
        "fix",
        "get",
        "help",
        "improve",
        "keep",
        "make",
        "need",
        "replace",
        "start",
        "stop",
        "use",
    }
)
RETRIEVAL_CONTEXT_STOP_TOKENS = frozenset(
    {
        "about",
        "available",
        "build",
        "construct",
        "fallback",
        "it",
        "light",
        "lighting",
        "make",
        "next",
        "not",
        "start",
        "that",
        "then",
        "this",
        "those",
        "using",
        "when",
    }
)

BUILD_MARKERS = frozenset(
    {
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
)
HOUSE_MARKERS = frozenset({"house", "home", "cabin", "hut", "homestead", "dwelling"})
HOUSE_SUBSYSTEM_QUERY_MARKERS = frozenset(
    {
        "roof",
        "roofing",
        "insulation",
        "weatherproof",
        "weatherproofing",
        "rainproof",
        "rainproofing",
        "wall",
        "wall framing",
        "foundation",
        "ventilation",
        "weatherstripping",
        "window",
        "door",
        "threshold",
    }
)
HOUSE_SITE_QUERY_MARKERS = frozenset({"building site", "site selection", "choose a site", "choose a building site", "site and foundation"})
HOUSE_SITE_FACTOR_MARKERS = frozenset({"drainage", "wind", "sun", "access", "hazard", "foundation"})
HOUSE_SITE_SEASONAL_PROMPT_MARKERS = frozenset(
    {"winter", "summer", "shade", "shading", "microclimate", "seasonal", "solar", "orientation"}
)
HOUSE_SITE_BREADTH_MARKERS = frozenset(
    {"wind", "sun", "access", "hazard", "hazards", "terrain", "microclimate", "exposure", "seasonal", "resource proximity", "winter", "summer", "shade", "solar", "orientation"}
)
HOUSE_PROJECT_MARKERS = frozenset(
    {
        "site",
        "site selection",
        "foundation",
        "drainage",
        "framing",
        "roof",
        "roofing",
        "weatherproof",
        "weatherproofing",
        "wall",
        "walls",
        "ventilation",
        "insulation",
        "window",
        "door",
        "threshold",
    }
)
HOUSE_INTENT_MARKERS = frozenset(
    {
        "foundation",
        "wall construction",
        "roofing",
        "weatherproofing",
        "framing",
        "site selection",
        "floor system",
        "wall framing",
        "roof framing",
        "drainage",
        "ventilation",
        "window and door",
        "weatherstripping",
        "site drainage",
        "roof waterproofing",
    }
)
OVEN_BUILD_QUERY_MARKERS = frozenset(
    {
        "clay oven",
        "cob oven",
        "bread oven",
        "brick oven",
        "earth oven",
        "masonry oven",
        "rocket stove",
        "mass heater",
        "pottery kiln",
        "kiln",
        "kilns",
        "firebrick",
        "refractory",
    }
)
WATER_MARKERS = frozenset({"water", "drink", "drinking"})
WATER_DISTRIBUTION_MARKERS = frozenset(
    {
        "water distribution",
        "distribution system",
        "gravity-fed",
        "gravity fed",
        "community water",
        "household taps",
        "water tower",
        "spring box",
        "storage tank",
        "storage tanks",
        "cistern",
        "piping",
        "plumbing",
        "catchment",
    }
)
WATER_DISTRIBUTION_PROJECT_MARKERS = frozenset({"design", "set up", "setup", "build", "plan", "install", "system"})
WATER_STORAGE_MARKERS = frozenset(
    {
        "store water",
        "water storage",
        "stored water",
        "treated water",
        "container",
        "containers",
        "drum",
        "barrel",
        "bucket",
        "bottle",
        "bottles",
        "soda bottle",
        "soda bottles",
        "jug",
        "food-grade",
        "food safe",
        "food-safe",
        "ration",
        "rotate",
        "rotation",
        "sanitize",
        "sanitise",
    }
)
WATER_PURITY_MARKERS = frozenset(
    {
        "purify",
        "purification",
        "filter",
        "filtration",
        "safe to drink",
        "clean water",
        "safe drinking water",
        "disinfect water",
        "biosand",
        "boil",
        "chlorine",
        "sodis",
        "distill",
    }
)
WATERCRAFT_MARKERS = frozenset({"canoe", "boat", "watercraft", "dugout", "coracle", "kayak", "rowboat", "skiff"})
SANITATION_MARKERS = frozenset(
    {
        "latrine",
        "latrines",
        "toilet",
        "toilets",
        "wash station",
        "handwashing station",
        "hand washing station",
        "hygiene station",
        "waste management",
        "greywater",
    }
)
MESSAGE_AUTH_SUBJECT_MARKERS = frozenset({"courier", "message", "note", "order"})
MESSAGE_AUTH_VERIFY_MARKERS = frozenset(
    {
        "verify",
        "verification",
        "authentic",
        "real",
        "forged",
        "forgery",
        "seal",
        "chain of custody",
        "chain-of-custody",
        "challenge-response",
        "challenge response",
        "tamper",
    }
)
FAIR_TRIAL_MARKERS = frozenset({"fair trial", "trial with no lawyers", "no lawyers or judges", "community trial", "lay tribunal"})
SOAP_MARKERS = frozenset(
    {
        "make soap",
        "making soap",
        "soap making",
        "soap from animal fat",
        "tallow soap",
        "wood ash lye",
        "lye water",
        "saponification",
    }
)
GLASS_MARKERS = frozenset({"make glass", "making glass", "glass from scratch", "glassmaking", "annealing"})
COMMUNITY_SECURITY_ACTION_MARKERS = frozenset(
    {
        "protect",
        "guard",
        "watch",
        "patrol",
        "secure",
        "defend",
        "fortify",
        "checkpoint",
        "access control",
        "early warning",
        "perimeter",
    }
)
COMMUNITY_SECURITY_TARGET_MARKERS = frozenset(
    {
        "work site",
        "field",
        "water point",
        "food storage",
        "storehouse",
        "granary",
        "infrastructure",
        "entry point",
        "vulnerable",
        "spread people too thin",
    }
)
COMMUNITY_GOVERNANCE_MARKERS = frozenset(
    {
        "steal",
        "stealing",
        "theft",
        "stolen",
        "trust",
        "dont trust",
        "don't trust",
        "merge",
        "merge groups",
        "resource dispute",
        "sanction",
        "vouch",
        "mediation",
        "mutual aid",
        "restorative",
        "reputation",
        "commons",
    }
)
PUNCTURE_MARKERS = frozenset({"puncture wound", "puncture", "deep puncture", "stepped on a nail", "nail wound"})
FIRE_MARKERS = frozenset({"fire", "flame", "ignite", "burn"})
WET_WEATHER_MARKERS = frozenset({"rain", "wet", "soaked", "damp", "storm"})
METAL_MARKERS = frozenset({"metal", "steel", "iron", "copper", "brass"})
JOIN_MARKERS = frozenset({"join", "joining", "weld", "welding", "braze", "solder"})
WELDER_ABSENCE_MARKERS = frozenset(
    {"without a welder", "without welder", "no welder", "without electricity", "without electric welder"}
)
SAFETY_MARKERS = frozenset({"safe", "safest", "safely", "risk", "hazard", "warning", "emergency"})
LONG_TERM_MARKERS = frozenset({"long term", "long-term", "permanent", "semi-permanent", "seasonal", "store", "storage"})
IMMEDIATE_MARKERS = frozenset({"emergency", "right now", "immediately", "temporary", "tonight", "first aid"})

TOPIC_MARKERS: dict[str, frozenset[str]] = {
    "site_selection": frozenset({"site", "location", "hazard", "terrain"}),
    "drainage": frozenset({"drainage", "runoff", "french drain", "slope"}),
    "foundation": frozenset({"foundation", "footing", "footings"}),
    "wall_construction": frozenset({"wall", "walls", "framing", "stud"}),
    "roofing": frozenset({"roof", "roofing", "rafter", "gable"}),
    "weatherproofing": frozenset({"weatherproof", "rainproof", "waterproof", "seal", "sealing"}),
    "ventilation": frozenset({"ventilation", "airflow", "condensation"}),
    "water_storage": frozenset({"water storage", "store water", "stored water", "treated water"}),
    "container_sanitation": frozenset({"container", "sanitize", "sanitise", "food-grade", "food safe", "clean container"}),
    "water_rotation": frozenset({"rotate", "rotation", "inspect", "seal"}),
    "water_distribution": frozenset(
        {
            "distribution",
            "gravity-fed",
            "gravity fed",
            "water tower",
            "cistern",
            "household taps",
            "pipe",
            "piping",
            "plumbing",
            "community water",
            "catchment",
            "spring box",
            "tank",
            "storage tank",
        }
    ),
    "water_purification": frozenset({"purify", "purification", "filter", "filtration", "treat water"}),
    "prefilter": frozenset({"settle", "sediment", "prefilter", "pre-filter", "biosand"}),
    "disinfection": frozenset({"boil", "chlorine", "chlorinate", "disinfect", "sodis"}),
    "small_watercraft": frozenset({"canoe", "boat", "watercraft", "dugout"}),
    "hull": frozenset({"hull", "planking", "ribs", "buoyancy"}),
    "sealing": frozenset({"caulk", "caulking", "pitch", "resin", "seal"}),
    "first_aid": frozenset({"first aid", "wound", "puncture", "splinter"}),
    "wound_cleaning": frozenset({"clean", "wash", "rinse", "irrigate"}),
    "infection_monitoring": frozenset({"infection", "pus", "redness", "swelling", "tetanus"}),
    "latrine_design": frozenset({"latrine", "toilet", "pit latrine", "vip latrine", "waste trench"}),
    "wash_station": frozenset({"wash station", "handwashing", "hand washing", "greywater", "hygiene station"}),
    "message_authentication": frozenset({"verify", "authentication", "authentic", "real note", "tamper", "seal"}),
    "chain_of_custody": frozenset({"chain of custody", "courier", "challenge-response", "challenge response", "dead drop"}),
    "trial_procedure": frozenset({"trial", "lay tribunal", "hearing", "appeal", "panel selection"}),
    "evidence_rules": frozenset({"evidence", "record", "records", "witness", "record rules"}),
    "soapmaking": frozenset({"soap", "saponification", "lye water", "ash soap", "tallow soap"}),
    "lye_safety": frozenset({"lye", "caustic", "burn", "flush with water", "chemical burn"}),
    "glassmaking": frozenset({"glass", "silica", "soda ash", "furnace", "crucible"}),
    "annealing": frozenset({"anneal", "annealing", "controlled cooling", "thermal stress"}),
    "clay_oven": frozenset({"clay oven", "bread oven", "cob oven", "brick oven", "earth oven", "masonry oven"}),
    "masonry_hearth": frozenset({"hearth", "chimney", "draft", "thermal mass", "firebrick", "cob"}),
    "community_security": frozenset(
        {"security", "guard", "patrol", "checkpoint", "perimeter", "access control", "fortification", "early warning"}
    ),
    "resource_security": frozenset({"water point", "field", "food storage", "storehouse", "granary", "critical infrastructure"}),
    "community_governance": frozenset(
        {"governance", "resource rules", "graduated sanctions", "restorative justice", "mutual aid", "commons"}
    ),
    "conflict_resolution": frozenset({"mediation", "de-escalation", "dispute", "restorative", "sanction", "theft", "stealing", "stolen"}),
    "trust_systems": frozenset({"trust", "reputation", "vouch", "merge", "merge groups", "dont trust", "don't trust", "resource pooling", "mutual aid"}),
}

STRUCTURE_TO_TOPICS: dict[str, tuple[str, ...]] = {
    "cabin_house": (
        "site_selection",
        "drainage",
        "foundation",
        "wall_construction",
        "roofing",
        "weatherproofing",
        "ventilation",
    ),
    "water_storage": ("water_storage", "container_sanitation", "water_rotation"),
    "water_distribution": ("water_distribution", "water_storage"),
    "water_purification": ("water_purification", "prefilter", "disinfection"),
    "small_watercraft": ("small_watercraft", "hull", "sealing"),
    "wound_care": ("first_aid", "wound_cleaning", "infection_monitoring"),
    "sanitation_system": ("latrine_design", "wash_station"),
    "message_auth": ("message_authentication", "chain_of_custody"),
    "fair_trial": ("trial_procedure", "evidence_rules"),
    "soapmaking": ("soapmaking", "lye_safety"),
    "glassmaking": ("glassmaking", "annealing"),
    "clay_oven": ("clay_oven", "masonry_hearth"),
    "community_security": ("community_security", "resource_security"),
    "community_governance": ("community_governance", "conflict_resolution", "trust_systems"),
}

PREFERRED_CATEGORIES: dict[str, tuple[str, ...]] = {
    "cabin_house": ("building", "survival", "resource-management"),
    "earth_shelter": ("building", "survival"),
    "emergency_shelter": ("building", "survival"),
    "water_storage": ("resource-management", "survival", "utility", "building"),
    "water_distribution": ("building", "utility", "resource-management"),
    "water_purification": ("utility", "survival", "medical", "building"),
    "small_watercraft": ("transportation", "building", "survival"),
    "wound_care": ("medical", "survival"),
    "sanitation_system": ("building", "medical", "utility"),
    "message_auth": ("communications", "society"),
    "fair_trial": ("society", "communications"),
    "soapmaking": ("crafts", "chemistry"),
    "glassmaking": ("crafts", "chemistry", "building"),
    "clay_oven": ("agriculture", "building", "crafts", "chemistry"),
    "community_security": ("defense", "resource-management", "building", "society"),
    "community_governance": ("society", "resource-management", "communications"),
}
DISFAVORED_CATEGORIES: dict[str, tuple[str, ...]] = {
    "cabin_house": ("medical", "society", "agriculture"),
    "water_storage": ("agriculture", "chemistry"),
    "water_distribution": ("agriculture", "medical"),
    "water_purification": ("agriculture", "society"),
    "message_auth": ("building", "medical", "agriculture"),
    "fair_trial": ("building", "medical", "agriculture"),
    "soapmaking": ("medical", "society"),
    "glassmaking": ("society", "medical"),
    "clay_oven": ("society", "medical"),
    "community_security": ("medical", "chemistry"),
    "community_governance": ("medical", "chemistry", "building"),
}
QUERY_EXPANSIONS: dict[str, tuple[str, ...]] = {
    "boat": ("watercraft", "vessel", "hull"),
    "canoe": ("watercraft", "boat", "dugout", "paddle"),
    "charcoal": ("filtration", "activated charcoal", "water purification"),
    "coracle": ("watercraft", "boat", "hide"),
    "dugout": ("canoe", "watercraft", "boat"),
    "drum": ("container", "water storage", "sanitation"),
    "filter": ("filtration", "water purification", "safe water"),
    "fire": ("tinder", "kindling", "ignite"),
    "kayak": ("watercraft", "boat", "paddle"),
    "metal": ("forge", "brazing", "soldering"),
    "oar": ("paddle", "rowing"),
    "paddle": ("oar", "rowing"),
    "puncture": ("wound management", "first aid", "irrigation", "tetanus"),
    "purify": ("water purification", "disinfect", "filtration"),
    "raft": ("watercraft", "boat", "flotation"),
    "rowboat": ("boat", "watercraft", "oar"),
    "sand": ("filtration", "settling", "prefilter"),
    "sailboat": ("boat", "watercraft", "sail"),
    "store": ("storage", "container sanitation", "rationing"),
    "welder": ("forge welding", "brazing", "soldering"),
    "wet": ("rain", "damp", "dry tinder"),
    "wound": ("first aid", "infection prevention", "dressings"),
}


def content_tokens(text: str, *, stop_tokens: frozenset[str] = STOP_TOKENS) -> list[str]:
    seen: set[str] = set()
    ordered: list[str] = []
    for raw in re.split(r"[^a-z0-9-]+", safe_text(text).lower()):
        token = raw.strip()
        if len(token) < 2 or token in stop_tokens:
            continue
        if token in seen:
            continue
        seen.add(token)
        ordered.append(token)
    return ordered


def retrieval_tokens(text: str) -> list[str]:
    seen: set[str] = set()
    ordered: list[str] = []
    for token in content_tokens(text):
        if token in RETRIEVAL_CONTEXT_STOP_TOKENS:
            continue
        if token in seen:
            continue
        seen.add(token)
        ordered.append(token)
    return ordered


def has_build_verb(normalized_query: str) -> bool:
    if contains_any(normalized_query, BUILD_MARKERS):
        return True
    bounded = f" {normalize_match_text(normalized_query)} "
    return any(marker in bounded for marker in (" build ", " construct ", " make "))


def has_fire_verb(normalized_query: str) -> bool:
    bounded = f" {normalize_match_text(normalized_query)} "
    return any(marker in bounded for marker in (" start ", " make ", " build ", " light ", " ignite "))


def looks_like_house_site_selection(normalized_query: str) -> bool:
    return contains_any(normalized_query, HOUSE_SITE_QUERY_MARKERS) and (
        count_matches(normalized_query, HOUSE_SITE_FACTOR_MARKERS) >= 2
        or "foundation" in normalized_query
        or "footing" in normalized_query
    )


def detect_route_kind(normalized_query: str) -> str:
    if contains_any(normalized_query, WATER_MARKERS) and contains_any(normalized_query, WATER_DISTRIBUTION_MARKERS):
        if contains_any(normalized_query, WATER_DISTRIBUTION_PROJECT_MARKERS) or contains_term(normalized_query, "system"):
            return "water_distribution"
    if contains_any(normalized_query, WATER_MARKERS) and contains_any(normalized_query, WATER_PURITY_MARKERS):
        return "water_purification"
    if contains_any(normalized_query, WATER_MARKERS) and contains_any(normalized_query, WATER_STORAGE_MARKERS):
        return "water_storage"
    if contains_any(normalized_query, MESSAGE_AUTH_SUBJECT_MARKERS) and contains_any(normalized_query, MESSAGE_AUTH_VERIFY_MARKERS):
        return "message_auth"
    if contains_any(normalized_query, FAIR_TRIAL_MARKERS):
        return "fair_trial"
    if contains_any(normalized_query, SOAP_MARKERS):
        return "soapmaking"
    if contains_any(normalized_query, GLASS_MARKERS):
        return "glassmaking"
    if contains_any(normalized_query, SANITATION_MARKERS):
        return "sanitation_system"
    if contains_any(normalized_query, COMMUNITY_SECURITY_ACTION_MARKERS) and contains_any(normalized_query, COMMUNITY_SECURITY_TARGET_MARKERS):
        return "community_security"
    if contains_any(normalized_query, COMMUNITY_GOVERNANCE_MARKERS):
        return "community_governance"
    if has_build_verb(normalized_query) and contains_any(normalized_query, OVEN_BUILD_QUERY_MARKERS):
        return "clay_oven"
    if has_build_verb(normalized_query) and contains_any(normalized_query, WATERCRAFT_MARKERS):
        return "small_watercraft"
    if (
        contains_any(normalized_query, HOUSE_MARKERS)
        and (has_build_verb(normalized_query) or contains_any(normalized_query, HOUSE_INTENT_MARKERS) or contains_any(normalized_query, HOUSE_PROJECT_MARKERS))
    ):
        return "house_build"
    if looks_like_house_site_selection(normalized_query):
        return "house_build"
    if contains_any(normalized_query, HOUSE_SUBSYSTEM_QUERY_MARKERS) and not contains_any(normalized_query, WATERCRAFT_MARKERS):
        return "house_build"
    if contains_any(normalized_query, PUNCTURE_MARKERS):
        return "generic_puncture"
    if has_fire_verb(normalized_query) and contains_any(normalized_query, FIRE_MARKERS) and contains_any(normalized_query, WET_WEATHER_MARKERS):
        return "fire_in_rain"
    if contains_any(normalized_query, WELDER_ABSENCE_MARKERS) and contains_any(normalized_query, METAL_MARKERS) and contains_any(normalized_query, JOIN_MARKERS):
        return "metal_join_no_welder"
    return "default"


def route_structure_type(kind: str) -> str:
    return {
        "house_build": "cabin_house",
        "small_watercraft": "small_watercraft",
        "water_purification": "water_purification",
        "water_storage": "water_storage",
        "water_distribution": "water_distribution",
        "sanitation_system": "sanitation_system",
        "message_auth": "message_auth",
        "fair_trial": "fair_trial",
        "soapmaking": "soapmaking",
        "glassmaking": "glassmaking",
        "clay_oven": "clay_oven",
        "community_security": "community_security",
        "community_governance": "community_governance",
        "generic_puncture": "wound_care",
        "fire_in_rain": "general",
        "metal_join_no_welder": "general",
    }.get(kind, "")


def route_preferred_categories(kind: str) -> tuple[str, ...]:
    return {
        "house_build": ("building", "survival", "resource-management"),
        "small_watercraft": ("transportation", "building", "survival"),
        "water_purification": ("utility", "survival", "medical"),
        "water_storage": ("resource-management", "survival", "utility", "building"),
        "water_distribution": ("building", "utility", "resource-management"),
        "sanitation_system": ("building", "medical", "utility"),
        "message_auth": ("communications", "society"),
        "fair_trial": ("society", "communications"),
        "soapmaking": ("crafts", "chemistry"),
        "glassmaking": ("crafts", "chemistry", "building"),
        "clay_oven": ("agriculture", "building", "crafts", "chemistry"),
        "community_security": ("defense", "resource-management", "building", "society"),
        "community_governance": ("society", "resource-management", "communications"),
        "generic_puncture": ("medical", "survival"),
        "fire_in_rain": ("survival",),
        "metal_join_no_welder": ("metalworking", "transportation", "building"),
    }.get(kind, ())


def route_expansion_tokens(kind: str) -> tuple[str, ...]:
    return {
        "house_build": ("foundation", "drainage", "roofing", "weatherproofing"),
        "small_watercraft": ("watercraft", "hull", "sealing", "caulking"),
        "water_purification": ("water purification", "disinfection", "filtration"),
        "water_storage": ("water storage", "container sanitation", "rationing"),
        "water_distribution": ("water distribution", "storage tank", "gravity-fed", "plumbing"),
        "sanitation_system": ("latrine", "wash station", "greywater"),
        "message_auth": ("message authentication", "chain of custody", "challenge-response"),
        "fair_trial": ("trial procedure", "evidence", "appeal"),
        "soapmaking": ("soapmaking", "lye safety", "saponification"),
        "glassmaking": ("glassmaking", "annealing", "furnace"),
        "clay_oven": (
            "clay oven",
            "cob oven",
            "bread oven",
            "brick oven",
            "earth oven",
            "site selection",
            "foundation",
            "chimney draft",
            "curing schedule",
            "thermal mass",
            "refractory",
            "firebrick",
            "kiln",
        ),
        "community_security": (
            "community defense",
            "physical security",
            "guard rotation",
            "patrol routes",
            "checkpoint design",
            "water point security",
            "food storage security",
            "early warning",
        ),
        "community_governance": (
            "community trust",
            "resource governance",
            "mutual aid",
            "reputation system",
            "restorative justice",
            "dispute mediation",
            "graduated sanctions",
            "resource pooling",
        ),
        "generic_puncture": ("wound care", "irrigation", "infection monitoring"),
        "fire_in_rain": ("tinder", "kindling", "dry inner wood"),
        "metal_join_no_welder": ("forge welding", "brazing", "soldering"),
    }.get(kind, ())


def detect_topic_tags(normalized_query: str) -> list[str]:
    tags: list[str] = []
    for topic_tag, markers in TOPIC_MARKERS.items():
        if contains_any(normalized_query, markers):
            tags.append(topic_tag)
    return tags


def explicit_single_topic_bonus(explicit_topic_tags: tuple[str, ...], section_heading: str, topic_tag: str, bonus: int) -> int:
    if topic_tag not in explicit_topic_tags:
        return 0
    if not contains_any(normalize_match_text(section_heading), TOPIC_MARKERS.get(topic_tag, ())):
        return 0
    return bonus


def matches_topic(section_heading: str, topic_tag: str) -> bool:
    return contains_any(normalize_match_text(section_heading), TOPIC_MARKERS.get(topic_tag, ()))


@dataclass(frozen=True)
class QueryRouteProfileLite:
    kind: str
    route_focused: bool
    preferred_categories: tuple[str, ...]
    expansion_tokens: tuple[str, ...]
    preferred_context_items: int
    prompt_excerpt_chars: int
    preferred_structure_type: str

    @classmethod
    def from_query(cls, query: str) -> "QueryRouteProfileLite":
        normalized = normalize_match_text(query)
        kind = detect_route_kind(normalized)
        route_focused = kind != "default"
        expansion_tokens = list(route_expansion_tokens(kind))
        if kind == "house_build":
            seasonal_site_focus = contains_any(normalized, HOUSE_SITE_QUERY_MARKERS) and (
                any(marker in normalized for marker in HOUSE_SITE_SEASONAL_PROMPT_MARKERS)
                or ("sun" in normalized and "shade" in normalized)
            )
            house_site_focus = (
                looks_like_house_site_selection(normalized)
                or seasonal_site_focus
                or count_matches(normalized, HOUSE_SITE_FACTOR_MARKERS) >= 2
            )
            if house_site_focus:
                prioritized_site_tokens = [
                    "site selection",
                    "hazard assessment",
                    "terrain analysis",
                    "wind exposure",
                    "natural hazards",
                    "water proximity",
                    "access routes",
                ]
                if any(marker in normalized for marker in HOUSE_SITE_SEASONAL_PROMPT_MARKERS) or (
                    "sun" in normalized and "shade" in normalized
                ):
                    prioritized_site_tokens[:0] = [
                        "microclimate",
                        "sun exposure",
                        "seasonal considerations",
                    ]
                seen_tokens: set[str] = set()
                merged_tokens: list[str] = []
                for token in [*prioritized_site_tokens, *expansion_tokens]:
                    if token not in seen_tokens:
                        seen_tokens.add(token)
                        merged_tokens.append(token)
                expansion_tokens = merged_tokens
        preferred_context_items = (
            4
            if kind in {"water_distribution", "community_security", "community_governance"}
            else 3 if kind in {"house_build", "water_storage", "clay_oven"} else DEFAULT_CONTEXT_ITEMS
        )
        prompt_excerpt_chars = 520 if kind in {"house_build", "water_distribution", "clay_oven", "community_security", "community_governance"} else 420
        return cls(
            kind=kind,
            route_focused=route_focused,
            preferred_categories=route_preferred_categories(kind),
            expansion_tokens=tuple(expansion_tokens),
            preferred_context_items=preferred_context_items,
            prompt_excerpt_chars=prompt_excerpt_chars,
            preferred_structure_type=route_structure_type(kind),
        )

    def is_starter_build_project(self) -> bool:
        return self.kind == "house_build"

    def prefers_summary_key_points_format(self) -> bool:
        return self.kind in {"message_auth", "fair_trial", "community_security", "community_governance"}

    def prompt_guidance_lines(self) -> list[str]:
        guidance: dict[str, tuple[str, ...]] = {
            "house_build": (
                "Favor build order, siting, drainage, and weatherproofing details when the notes support them.",
                "Prefer durable dwelling guidance over off-topic shelter or lifestyle material.",
            ),
            "small_watercraft": (
                "Keep the answer on hull shape, materials, sealing, and safety tradeoffs from the notes.",
            ),
            "water_purification": (
                "Keep the answer on treatment choice, filtration, and disinfection details from the notes.",
            ),
            "water_storage": (
                "Keep the answer on container safety, sanitation, rotation, and contamination control.",
            ),
            "water_distribution": (
                "Keep the answer on layout, elevation, storage, gravity flow, and distribution components.",
                "Avoid drifting into generic water-storage advice unless the retrieved notes explicitly connect it.",
            ),
            "clay_oven": (
                "Keep the answer on foundation, thermal mass, curing, draft, and heat-safe material choices from the notes.",
                "Avoid drifting into unrelated chemistry or generic non-construction material.",
            ),
            "community_security": (
                "Keep the answer on minimum staffing, chokepoints, watch rotation, access control, and early warning.",
            ),
            "community_governance": (
                "Keep the answer on trust repair, sanctions, mediation, monitoring, and resource rules.",
            ),
            "sanitation_system": (
                "Keep the answer on latrine siting, wash-station layout, and contamination control.",
            ),
            "message_auth": (
                "Treat the question as an operational verification problem, not generic leadership advice.",
            ),
            "fair_trial": (
                "Keep the answer on process, evidence, legitimacy, and role clarity from the notes.",
            ),
            "soapmaking": (
                "Keep the answer on soapmaking process, lye handling, and realistic safety limits.",
            ),
            "glassmaking": (
                "Keep the answer on materials, furnace demands, and process bottlenecks from the notes.",
            ),
            "generic_puncture": (
                "Keep the answer on wound cleaning, dressing, infection watch, and safety escalation signs.",
            ),
            "fire_in_rain": (
                "Keep the answer on fire-starting in wet conditions, dry material selection, and sequencing.",
            ),
            "metal_join_no_welder": (
                "Keep the answer on realistic low-tech joining methods and their limits.",
            ),
        }
        return list(guidance.get(self.kind, ()))

    def is_seasonal_house_site_selection_prompt(self, original_query: str) -> bool:
        normalized = normalize_match_text(original_query)
        if self.kind != "house_build":
            return False
        if not any(marker in normalized for marker in HOUSE_SITE_QUERY_MARKERS) and count_matches(normalized, HOUSE_SITE_FACTOR_MARKERS) < 2:
            return False
        return any(marker in normalized for marker in HOUSE_SITE_SEASONAL_PROMPT_MARKERS) or (
            "sun" in normalized and "shade" in normalized
        )

    def route_bonus(self, combined_text: str, category: str) -> int:
        if not self.route_focused:
            return 0
        score = 0
        if self.preferred_structure_type and contains_term(combined_text, self.preferred_structure_type.replace("_", " ")):
            score += 8
        if category.lower() in self.preferred_categories:
            score += 4
        for token in self.expansion_tokens:
            if contains_term(combined_text, token):
                score += 2
        return min(score, 18)


@dataclass(frozen=True)
class QueryMetadataProfileLite:
    preferred_content_roles: tuple[str, ...]
    preferred_categories: tuple[str, ...]
    disfavored_categories: tuple[str, ...]
    preferred_time_horizon: str
    preferred_structure_type: str
    preferred_topic_tags: tuple[str, ...]
    explicit_topic_tags: tuple[str, ...]
    prefers_diversified_context: bool
    site_breadth_intent: bool

    @classmethod
    def from_query(cls, query: str, route_profile: QueryRouteProfileLite | None = None) -> "QueryMetadataProfileLite":
        route_profile = route_profile or QueryRouteProfileLite.from_query(query)
        normalized = normalize_match_text(query)
        structure_type = route_profile.preferred_structure_type
        if not structure_type:
            if contains_any(normalized, HOUSE_MARKERS) and contains_any(normalized, HOUSE_PROJECT_MARKERS):
                structure_type = "cabin_house"
            elif contains_any(normalized, WATER_DISTRIBUTION_MARKERS):
                structure_type = "water_distribution"
            elif contains_any(normalized, WATER_STORAGE_MARKERS):
                structure_type = "water_storage"
            elif contains_any(normalized, WATER_PURITY_MARKERS):
                structure_type = "water_purification"
            elif contains_any(normalized, WATERCRAFT_MARKERS):
                structure_type = "small_watercraft"
            elif contains_any(normalized, SANITATION_MARKERS):
                structure_type = "sanitation_system"
            elif contains_any(normalized, COMMUNITY_SECURITY_ACTION_MARKERS) and contains_any(normalized, COMMUNITY_SECURITY_TARGET_MARKERS):
                structure_type = "community_security"
            elif contains_any(normalized, COMMUNITY_GOVERNANCE_MARKERS):
                structure_type = "community_governance"
            elif contains_any(normalized, MESSAGE_AUTH_VERIFY_MARKERS):
                structure_type = "message_auth"
            elif contains_any(normalized, FAIR_TRIAL_MARKERS):
                structure_type = "fair_trial"
            elif contains_any(normalized, SOAP_MARKERS):
                structure_type = "soapmaking"
            elif contains_any(normalized, GLASS_MARKERS):
                structure_type = "glassmaking"
            elif contains_any(normalized, PUNCTURE_MARKERS):
                structure_type = "wound_care"
            else:
                structure_type = ""

        explicit_topic_tags = detect_topic_tags(normalized)
        preferred_topic_tags = list(explicit_topic_tags)
        for topic_tag in STRUCTURE_TO_TOPICS.get(structure_type, ()):
            if topic_tag not in preferred_topic_tags:
                preferred_topic_tags.append(topic_tag)

        build_intent = has_build_verb(normalized) or structure_type in {
            "cabin_house",
            "water_distribution",
            "water_storage",
            "small_watercraft",
            "clay_oven",
        }
        roles: list[str] = ["starter", "subsystem"]
        if build_intent:
            roles = ["planning", "starter", "subsystem"]
        if contains_any(normalized, SAFETY_MARKERS) or structure_type in {"water_storage", "water_purification", "wound_care"}:
            roles.append("safety")

        preferred_time_horizon = ""
        if structure_type == "emergency_shelter" or contains_any(normalized, IMMEDIATE_MARKERS):
            preferred_time_horizon = "immediate"
        elif structure_type in {"cabin_house", "water_storage", "water_distribution", "community_security", "community_governance"} or contains_any(normalized, LONG_TERM_MARKERS):
            preferred_time_horizon = "long_term"

        prefers_diversified_context = build_intent or structure_type in {
            "cabin_house",
            "small_watercraft",
            "water_storage",
            "water_distribution",
            "water_purification",
            "community_security",
            "community_governance",
            "clay_oven",
        }
        site_breadth_intent = (
            structure_type == "cabin_house"
            and ("site_selection" in explicit_topic_tags or contains_any(normalized, HOUSE_SITE_QUERY_MARKERS))
            and count_matches(normalized, HOUSE_SITE_BREADTH_MARKERS) >= 2
        )

        return cls(
            preferred_content_roles=tuple(dict.fromkeys(roles)),
            preferred_categories=PREFERRED_CATEGORIES.get(structure_type, ()),
            disfavored_categories=DISFAVORED_CATEGORIES.get(structure_type, ()),
            preferred_time_horizon=preferred_time_horizon,
            preferred_structure_type=structure_type,
            preferred_topic_tags=tuple(dict.fromkeys(preferred_topic_tags)),
            explicit_topic_tags=tuple(dict.fromkeys(explicit_topic_tags)),
            prefers_diversified_context=prefers_diversified_context,
            site_breadth_intent=site_breadth_intent,
        )

    def has_explicit_topic_focus(self) -> bool:
        return bool(self.explicit_topic_tags)

    def has_explicit_topic(self, topic_tag: str) -> bool:
        return topic_tag.lower() in self.explicit_topic_tags

    def explicit_topic_overlap_count(self, topic_tags: str) -> int:
        if not self.explicit_topic_tags:
            return 0
        return sum(1 for topic_tag in split_csv(topic_tags) if topic_tag in self.explicit_topic_tags)

    def preferred_topic_overlap_count(self, topic_tags: str) -> int:
        if not self.preferred_topic_tags:
            return 0
        return sum(1 for topic_tag in split_csv(topic_tags) if topic_tag in self.preferred_topic_tags)

    def section_heading_bonus(self, section_heading: str) -> int:
        normalized = normalize_match_text(section_heading)
        if not normalized:
            return 0
        score = 0
        for topic_tag in self.explicit_topic_tags:
            if contains_any(normalized, TOPIC_MARKERS.get(topic_tag, ())):
                score += 12
        if not self.explicit_topic_tags:
            for topic_tag in self.preferred_topic_tags:
                if contains_any(normalized, TOPIC_MARKERS.get(topic_tag, ())):
                    score += 6
        else:
            for topic_tag in self.preferred_topic_tags:
                if topic_tag in self.explicit_topic_tags:
                    continue
                if contains_any(normalized, TOPIC_MARKERS.get(topic_tag, ())):
                    score += 3
        if self.preferred_structure_type == "water_distribution":
            if contains_any(
                normalized,
                {"distribution", "gravity fed", "gravity-fed", "storage tank", "system components", "household taps", "overflow", "spring box"},
            ):
                score += 10 if self.explicit_topic_tags else 12
            if contains_any(
                normalized,
                {"tower", "water point", "standpipe", "layout", "network", "main line", "branch line"},
            ):
                score += 8 if self.explicit_topic_tags else 10
            if contains_any(
                normalized,
                {"common mistakes", "operational mistakes", "construction mistakes", "troubleshooting", "emergency repairs", "system recovery"},
            ):
                score -= 10 if self.explicit_topic_tags else 14
            if contains_any(normalized, {"food storage", "grain storage", "pantry storage", "chemical storage"}):
                score -= 26 if self.explicit_topic_tags else 34
            if contains_any(normalized, {"hydration assurance", "container sanitation", "rotation schedules"}):
                score -= 10 if self.explicit_topic_tags else 16
        if self.preferred_structure_type == "water_storage":
            explicit_broad_water_storage_focus = bool(self.explicit_topic_tags) and "water_distribution" not in self.explicit_topic_tags
            if "water_distribution" not in self.explicit_topic_tags and contains_any(
                normalized,
                {"distribution", "system design", "community water"},
            ):
                score -= 10 if self.explicit_topic_tags else 14
            if "water_distribution" in self.explicit_topic_tags:
                if contains_any(
                    normalized,
                    {"distribution", "gravity", "storage tank", "cistern", "community water"},
                ):
                    score += 10
                if contains_any(
                    normalized,
                    {"piping", "plumbing", "service line", "spring box", "household taps", "overflow"},
                ):
                    score += 7
                if contains_any(normalized, {"container sanitation", "rotation", "inspection", "food grade"}):
                    score -= 8
            if contains_any(normalized, TOPIC_MARKERS.get("water_storage", ())):
                score += 6 if self.explicit_topic_tags else 8
            if contains_any(normalized, {"hydration", "maintenance", "storage location", "protection"}):
                generic_storage_bonus = 4 if self.explicit_topic_tags else 6
                if explicit_broad_water_storage_focus and "hydration" in normalized:
                    generic_storage_bonus -= 8
                score += generic_storage_bonus
            if contains_any(
                normalized,
                {"chemical storage", "hazard management", "hazard segregation", "incompatible materials"},
            ):
                score -= 24 if self.explicit_topic_tags else 32
            if contains_any(normalized, {"historical context", "science of storage"}):
                score -= 6 if self.explicit_topic_tags else 10
            if contains_any(
                normalized,
                {"food storage", "grain storage", "pantry storage", "root cellar", "root cellars", "canning", "preservation"},
            ):
                score -= 16 if self.explicit_topic_tags else 22
            if "container" in normalized:
                score += 4 if self.explicit_topic_tags else 7
            if contains_any(normalized, {"food grade", "food-safe", "sanitation", "seal integrity"}):
                score += 18 if explicit_broad_water_storage_focus else (4 if self.explicit_topic_tags else 7)
            if contains_any(normalized, {"rotation", "inspection", "seal integrity"}):
                score += 14 if explicit_broad_water_storage_focus else (4 if self.explicit_topic_tags else 7)
            if contains_any(normalized, {"pest prevention", "rodent", "insect control"}):
                score -= 10 if self.explicit_topic_tags else 16
        if self.preferred_structure_type == "cabin_house":
            if self.explicit_topic_tags:
                score += explicit_single_topic_bonus(self.explicit_topic_tags, section_heading, "roofing", 18)
                score += explicit_single_topic_bonus(self.explicit_topic_tags, section_heading, "weatherproofing", 18)
                score += explicit_single_topic_bonus(self.explicit_topic_tags, section_heading, "foundation", 16)
                score += explicit_single_topic_bonus(self.explicit_topic_tags, section_heading, "drainage", 14)
                score += explicit_single_topic_bonus(self.explicit_topic_tags, section_heading, "site_selection", 16)
                score += explicit_single_topic_bonus(self.explicit_topic_tags, section_heading, "wall_construction", 12)
                score += explicit_single_topic_bonus(self.explicit_topic_tags, section_heading, "ventilation", 10)
                if "site_selection" in self.explicit_topic_tags and "foundation" in self.explicit_topic_tags:
                    if matches_topic(section_heading, "foundation"):
                        score += 8
                    if matches_topic(section_heading, "drainage") and not matches_topic(section_heading, "foundation"):
                        if contains_any(normalized, {"french drain", "swale", "culvert", "ditch"}):
                            score -= 18
                        else:
                            score += 2
                if self.site_breadth_intent:
                    if contains_any(
                        normalized,
                        {
                            "terrain",
                            "wind exposure",
                            "microclimate",
                            "seasonal",
                            "sun exposure",
                            "shade",
                            "natural hazard",
                            "water proximity",
                            "access",
                        },
                    ):
                        score += 12
                    if not matches_topic(section_heading, "site_selection") and contains_any(
                        normalized, {"french drain", "swale", "culvert", "road drainage", "ditch"}
                    ):
                        score -= 18
                    if not matches_topic(section_heading, "site_selection") and contains_any(
                        normalized, {"drainage and waterproofing", "waterproofing", "water shedding"}
                    ):
                        score -= 16
                if (
                    "site_selection" in self.explicit_topic_tags
                    and "foundation" in self.explicit_topic_tags
                    and "roofing" not in self.explicit_topic_tags
                    and "weatherproofing" not in self.explicit_topic_tags
                    and "insulation" in normalized
                ):
                    score -= 24
                if "roofing" not in self.explicit_topic_tags and "weatherproofing" not in self.explicit_topic_tags:
                    if contains_any(normalized, {"insulation", "weatherstripping", "air sealing", "roof insulation"}):
                        score -= 18
                if "roofing" in self.explicit_topic_tags or "weatherproofing" in self.explicit_topic_tags:
                    if contains_any(normalized, TOPIC_MARKERS.get("site_selection", ())):
                        score -= 16
                    if contains_any(normalized, TOPIC_MARKERS.get("drainage", ())):
                        score -= 14
                    if contains_any(normalized, TOPIC_MARKERS.get("foundation", ())):
                        score -= 12
                    if contains_any(normalized, TOPIC_MARKERS.get("wall_construction", ())):
                        score -= 6
                    if contains_any(normalized, {"calculator", "calculation"}):
                        score -= 18
            if contains_any(
                normalized,
                {"thermal efficiency", "calculation", "sizing", "off grid", "outbuildings"},
            ):
                score -= 10
            if contains_any(normalized, {"structural engineering basics", "design loads", "load paths"}):
                score -= 12
            if not self.explicit_topic_tags:
                if contains_any(normalized, TOPIC_MARKERS.get("foundation", ())):
                    score += 12
                if contains_any(normalized, {"frost line", "frost heave", "footing", "footings"}):
                    score += 8
                if contains_any(normalized, TOPIC_MARKERS.get("drainage", ())):
                    score += 10
                if contains_any(normalized, TOPIC_MARKERS.get("site_selection", ())):
                    score += 8
                if contains_any(normalized, TOPIC_MARKERS.get("wall_construction", ())):
                    score += 1
                if contains_any(normalized, TOPIC_MARKERS.get("roofing", ())):
                    score -= 2
                if contains_any(normalized, TOPIC_MARKERS.get("weatherproofing", ())):
                    score += 3
                if "alternative building materials" in normalized:
                    score += 4
                if contains_any(normalized, {"door", "window", "doors & window"}):
                    score -= 8
                if "calculator" in normalized:
                    score -= 10
        if self.preferred_structure_type == "clay_oven":
            if contains_any(
                normalized,
                {
                    "clay oven",
                    "cob oven",
                    "bread oven",
                    "brick bread oven",
                    "earth oven",
                    "oven types",
                    "site selection",
                    "foundation",
                    "chimney",
                    "draft",
                    "curing schedule",
                    "thermal mass",
                    "maintenance",
                    "repair",
                    "safety",
                },
            ):
                score += 10
            if contains_any(normalized, {"kiln construction", "refractory", "firebrick"}):
                score += 7
            if contains_any(normalized, {"currency", "ledger", "token", "tally", "astronomy", "aquaculture"}):
                score -= 18
        if self.preferred_structure_type == "community_security":
            if contains_any(normalized, {"security", "guard", "patrol", "checkpoint", "perimeter", "access control", "fortification", "early warning"}):
                score += 10 if self.explicit_topic_tags else 12
            if contains_any(normalized, {"water point", "field", "food storage", "storehouse", "granary", "critical infrastructure"}):
                score += 8 if self.explicit_topic_tags else 10
            if contains_any(normalized, {"authentication", "chain of custody", "message", "trial", "appeal"}):
                score -= 10 if self.explicit_topic_tags else 14
        if self.preferred_structure_type == "community_governance":
            if contains_any(normalized, {"resource governance", "commons", "governance", "shared rules", "membership rules"}):
                score += 10 if self.explicit_topic_tags else 12
            if contains_any(normalized, {"sanctions", "graduated sanctions", "mediation", "restorative", "dispute", "boundaries", "membership", "monitoring", "restitution", "conflict"}):
                score += 8 if self.explicit_topic_tags else 10
            if contains_any(normalized, {"trust", "reputation", "vouch", "hosting", "integration"}):
                score += 7 if self.explicit_topic_tags else 9
            if contains_any(normalized, {"theft", "inventory", "stock management", "auditing", "verification"}):
                score += 6 if self.explicit_topic_tags else 8
            if "conflict_resolution" in self.explicit_topic_tags and contains_any(normalized, {"sanctions", "graduated sanctions", "mediation", "restorative", "dispute", "boundaries", "membership", "monitoring"}):
                score += 10
            if "trust_systems" in self.explicit_topic_tags and contains_any(normalized, {"trust", "reputation", "vouch", "hosting", "integration"}):
                score += 8
            if contains_any(normalized, {"insurance", "pooling mathematics", "accounting", "record keeping", "record-keeping", "mutual aid", "resource pooling"}):
                score -= 6 if self.explicit_topic_tags else 10
            if contains_any(normalized, {"checkpoint", "perimeter", "patrol", "authentication"}):
                score -= 8 if self.explicit_topic_tags else 12
        return score

    def metadata_bonus(
        self,
        category: str,
        content_role: str,
        time_horizon: str,
        structure_type: str,
        topic_tags: str,
    ) -> int:
        score = 0
        normalized_category = safe_text(category).strip().lower()
        normalized_role = safe_text(content_role).strip().lower()
        normalized_time = safe_text(time_horizon).strip().lower()
        normalized_structure = safe_text(structure_type).strip().lower()

        if normalized_category and normalized_category in self.preferred_categories:
            score += 8
        elif normalized_category and normalized_category in self.disfavored_categories:
            score -= 6
        if normalized_role and normalized_role in self.preferred_content_roles:
            score += 4
        if self.preferred_time_horizon and normalized_time == self.preferred_time_horizon:
            score += 3
        if self.preferred_structure_type and normalized_structure == self.preferred_structure_type:
            score += 10
        if self.preferred_structure_type == "soapmaking":
            if normalized_category == "medical":
                score -= 8
            if normalized_structure in {"wound_care", "sanitation_system"}:
                score -= 8

        explicit_overlap = self.explicit_topic_overlap_count(topic_tags)
        preferred_overlap = self.preferred_topic_overlap_count(topic_tags)
        score += explicit_overlap * 7
        if explicit_overlap == 0:
            score += preferred_overlap * 3

        if self.prefers_diversified_context and not safe_text(structure_type).strip() and not safe_text(topic_tags).strip():
            score -= 4
        return score


@dataclass(frozen=True)
class QueryTerms:
    query_lower: str
    raw_tokens: tuple[str, ...]
    primary_tokens: tuple[str, ...]
    strong_tokens: tuple[str, ...]
    expansion_tokens: tuple[str, ...]
    route_profile: QueryRouteProfileLite
    metadata_profile: QueryMetadataProfileLite

    @classmethod
    def from_query(cls, query: str) -> "QueryTerms":
        route_profile = QueryRouteProfileLite.from_query(query)
        metadata_profile = QueryMetadataProfileLite.from_query(query, route_profile)
        raw_tokens = tuple(content_tokens(query, stop_tokens=frozenset()))
        primary_tokens = tuple(token for token in raw_tokens if token not in STOP_TOKENS)
        strong_tokens = tuple(token for token in primary_tokens if token not in LOW_SIGNAL_TOKENS)
        expansion_tokens: list[str] = []
        seen_expansions: set[str] = set()
        for token in raw_tokens:
            for expanded in QUERY_EXPANSIONS.get(token, ()):
                if expanded not in seen_expansions:
                    seen_expansions.add(expanded)
                    expansion_tokens.append(expanded)
        for expanded in route_profile.expansion_tokens:
            if expanded not in seen_expansions:
                seen_expansions.add(expanded)
                expansion_tokens.append(expanded)
        expansion_tokens = [
            token
            for token in expansion_tokens
            if token not in primary_tokens and token not in strong_tokens
        ]
        return cls(
            query_lower=normalize_match_text(query),
            raw_tokens=raw_tokens,
            primary_tokens=primary_tokens or raw_tokens,
            strong_tokens=strong_tokens or primary_tokens or raw_tokens,
            expansion_tokens=tuple(expansion_tokens),
            route_profile=route_profile,
            metadata_profile=metadata_profile,
        )

    def is_empty(self) -> bool:
        return not self.raw_tokens

    def primary_fts_tokens(self) -> tuple[str, ...]:
        return self.strong_tokens or self.primary_tokens or self.raw_tokens

    def primary_keyword_tokens(self) -> tuple[str, ...]:
        return self.strong_tokens or self.primary_tokens or self.raw_tokens

    def keyword_tokens(self) -> list[str]:
        ordered: list[str] = []
        seen: set[str] = set()
        for token in (*self.primary_keyword_tokens(), *self.expansion_tokens):
            if token in seen:
                continue
            seen.add(token)
            ordered.append(token)
        return ordered


@dataclass
class SearchResultRow:
    title: str
    subtitle: str
    snippet: str
    body: str
    guide_id: str
    section_heading: str
    category: str
    retrieval_mode: str
    content_role: str
    time_horizon: str
    structure_type: str
    topic_tags: str
    chunk_id: str = ""
    vector_row_id: int = -1
    lexical_rank: int | None = None
    vector_rank: int | None = None
    vector_score: float | None = None
    hybrid_rrf_score: float | None = None
    support_score: int | None = None
    keyword_score: int | None = None

    def source_key(self) -> str:
        base = self.guide_id.strip() or self.title.strip()
        section = self.section_heading.strip()
        if section:
            return f"{base.lower()}::{section.lower()}"
        return base.lower()

    def guide_key(self) -> str:
        return self.guide_id.strip().lower() or self.title.strip().lower()

    def source_label(self) -> str:
        if self.guide_id.strip() and self.title.strip():
            return f"[{self.guide_id.strip()}] {self.title.strip()}"
        if self.guide_id.strip():
            return f"[{self.guide_id.strip()}]"
        return self.title.strip() or "Unknown source"

    def report_dict(self, *, include_body_preview: bool = True) -> dict[str, Any]:
        payload = {
            "chunk_id": self.chunk_id,
            "guide_id": self.guide_id,
            "title": self.title,
            "section_heading": self.section_heading,
            "category": self.category,
            "retrieval_mode": self.retrieval_mode,
            "content_role": self.content_role,
            "time_horizon": self.time_horizon,
            "structure_type": self.structure_type,
            "topic_tags": split_csv(self.topic_tags),
            "subtitle": self.subtitle,
            "snippet": self.snippet,
            "source_label": self.source_label(),
            "lexical_rank": self.lexical_rank,
            "vector_rank": self.vector_rank,
            "vector_score": self.vector_score,
            "hybrid_rrf_score": self.hybrid_rrf_score,
            "support_score": self.support_score,
            "keyword_score": self.keyword_score,
        }
        if include_body_preview:
            payload["body_preview"] = clip(self.body, 1600)
            payload["body_chars"] = len(self.body or "")
        return payload


@dataclass(frozen=True)
class PackAssets:
    pack_manifest_path: Path
    pack_dir: Path
    sqlite_path: Path
    vector_path: Path
    manifest: dict[str, Any]

    @classmethod
    def load(cls, *, pack_manifest: str | None = None, pack_dir: str | None = None) -> "PackAssets":
        resolved_pack_dir = resolve_path(pack_dir, DEFAULT_PACK_MANIFEST.parent) if pack_dir else None
        manifest_path = resolve_path(
            pack_manifest,
            (resolved_pack_dir / "senku_manifest.json") if resolved_pack_dir else DEFAULT_PACK_MANIFEST,
            base_dir=resolved_pack_dir or REPO_ROOT,
        )
        if not manifest_path.exists():
            raise SystemExit(f"Pack manifest not found: {manifest_path}")
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        actual_pack_dir = resolved_pack_dir or manifest_path.parent
        sqlite_name = safe_text(manifest.get("files", {}).get("sqlite", {}).get("path")) or "senku_mobile.sqlite3"
        vector_name = safe_text(manifest.get("files", {}).get("vectors", {}).get("path")) or "senku_vectors.f16"
        sqlite_path = resolve_path(sqlite_name, base_dir=actual_pack_dir)
        vector_path = resolve_path(vector_name, base_dir=actual_pack_dir)
        if not sqlite_path.exists():
            raise SystemExit(f"Pack SQLite not found: {sqlite_path}")
        if not vector_path.exists():
            raise SystemExit(f"Pack vector file not found: {vector_path}")
        return cls(
            pack_manifest_path=manifest_path,
            pack_dir=actual_pack_dir.resolve(),
            sqlite_path=sqlite_path.resolve(),
            vector_path=vector_path.resolve(),
            manifest=manifest,
        )

    def summary(self) -> dict[str, Any]:
        return {
            "pack_manifest_path": str(self.pack_manifest_path),
            "pack_dir": str(self.pack_dir),
            "sqlite_path": str(self.sqlite_path),
            "vector_path": str(self.vector_path),
            "pack_format": self.manifest.get("pack_format"),
            "pack_version": self.manifest.get("pack_version"),
            "generated_at": self.manifest.get("generated_at"),
            "counts": self.manifest.get("counts", {}),
            "embedding": self.manifest.get("embedding", {}),
            "runtime_defaults": self.manifest.get("runtime_defaults", {}),
            "files": self.manifest.get("files", {}),
        }


class VectorStore:
    def __init__(self, vector_path: Path) -> None:
        with vector_path.open("rb") as handle:
            header = handle.read(VECTOR_HEADER_STRUCT.size)
        if len(header) != VECTOR_HEADER_STRUCT.size:
            raise ValueError(f"Vector header too short: {vector_path}")
        magic, version, header_bytes, row_count, dimension, dtype_code, _flags = VECTOR_HEADER_STRUCT.unpack(header)
        if magic != VECTOR_MAGIC:
            raise ValueError(f"Unsupported vector magic in {vector_path}")
        if version != 1:
            raise ValueError(f"Unsupported vector file version {version} in {vector_path}")
        if dtype_code not in {VECTOR_DTYPE_FLOAT16, VECTOR_DTYPE_INT8}:
            raise ValueError(f"Unsupported vector dtype code {dtype_code} in {vector_path}")
        self.path = vector_path
        self.header_bytes = header_bytes
        self.row_count = row_count
        self.dimension = dimension
        self.dtype_code = dtype_code
        dtype = np.float16 if dtype_code == VECTOR_DTYPE_FLOAT16 else np.int8
        self.matrix = np.memmap(vector_path, dtype=dtype, mode="r", offset=header_bytes, shape=(row_count, dimension))

    def build_centroid(self, row_ids: Sequence[int], seed_count: int) -> np.ndarray | None:
        valid = [row_id for row_id in row_ids[:seed_count] if 0 <= row_id < self.row_count]
        if not valid:
            return None
        rows = self.matrix[np.array(valid)]
        values = rows.astype(np.float32)
        if self.dtype_code == VECTOR_DTYPE_INT8:
            values = values / 127.0
        centroid = values.mean(axis=0)
        norm = float(np.linalg.norm(centroid))
        if norm <= 0:
            return None
        return centroid / norm

    def find_nearest(self, centroid: np.ndarray, limit: int) -> list[tuple[int, float]]:
        if limit <= 0 or centroid is None:
            return []
        matrix = self.matrix.astype(np.float32)
        if self.dtype_code == VECTOR_DTYPE_INT8:
            matrix = matrix / 127.0
        scores = matrix @ centroid
        if limit >= scores.shape[0]:
            ordered_indices = np.argsort(-scores)
        else:
            partition = np.argpartition(scores, -limit)[-limit:]
            ordered_indices = partition[np.argsort(-scores[partition])]
        return [(int(index), float(scores[index])) for index in ordered_indices]


def build_fts_expression(token: str) -> str:
    parts = [part.strip().lower() for part in re.split(r"[^a-z0-9]+", safe_text(token).lower()) if part.strip()]
    if not parts:
        return ""
    if len(parts) == 1:
        return parts[0] + "*"
    return "(" + " AND ".join(part + "*" for part in parts) + ")"


def build_fts_query(query_terms: QueryTerms) -> str:
    expressions: list[str] = []
    seen: set[str] = set()
    for token in query_terms.primary_fts_tokens():
        expression = build_fts_expression(token)
        if expression and expression not in seen:
            seen.add(expression)
            expressions.append(expression)
        if len(expressions) >= 8:
            break
    if len(expressions) < 8:
        for token in query_terms.expansion_tokens:
            expression = build_fts_expression(token)
            if expression and expression not in seen:
                seen.add(expression)
                expressions.append(expression)
            if len(expressions) >= 8:
                break
    return " OR ".join(expressions)


def lexical_keyword_score(
    query_terms: QueryTerms,
    *,
    title: str,
    section: str,
    category: str,
    tags: str,
    description: str,
    document: str,
) -> int:
    title_lower = safe_text(title).lower()
    section_lower = safe_text(section).lower()
    category_lower = safe_text(category).lower()
    tags_lower = safe_text(tags).lower()
    description_lower = safe_text(description).lower()
    document_lower = safe_text(document).lower()
    query_lower = query_terms.query_lower

    score = 0
    if contains_term(title_lower, query_lower):
        score += 18
    if contains_term(section_lower, query_lower):
        score += 14
    if contains_term(description_lower, query_lower):
        score += 8

    strong_matches = 0
    for token in query_terms.primary_keyword_tokens():
        if contains_term(title_lower, token):
            score += 12
            strong_matches += 1
        if contains_term(section_lower, token):
            score += 10
            strong_matches += 1
        if contains_term(tags_lower, token):
            score += 8
            strong_matches += 1
        if contains_term(category_lower, token):
            score += 5
        if contains_term(description_lower, token):
            score += 6
            strong_matches += 1
        if contains_term(document_lower, token):
            score += 3
            strong_matches += 1

    expansion_matches = 0
    for token in query_terms.expansion_tokens:
        if contains_term(title_lower, token):
            score += 6
            expansion_matches += 1
        if contains_term(section_lower, token):
            score += 5
            expansion_matches += 1
        if contains_term(tags_lower, token):
            score += 4
            expansion_matches += 1
        if contains_term(category_lower, token):
            score += 3
        if contains_term(description_lower, token):
            score += 3
            expansion_matches += 1
        if contains_term(document_lower, token):
            score += 2
            expansion_matches += 1

    combined_text = " ".join(
        part
        for part in (title_lower, section_lower, category_lower, tags_lower, description_lower, document_lower)
        if part
    )
    score += query_terms.route_profile.route_bonus(combined_text, category_lower)

    if strong_matches >= 2:
        score += 10
    if query_terms.primary_keyword_tokens() and strong_matches >= len(query_terms.primary_keyword_tokens()):
        score += 8
    if expansion_matches >= 2:
        score += 4
    return score


def keyword_sql_limit(query_terms: QueryTerms, limit: int) -> int:
    if query_terms.route_profile.is_starter_build_project():
        return max(limit * 2, 96)
    return max(limit * 4, 160)


def lexical_candidate_limit(query_terms: QueryTerms, limit: int) -> int:
    base = max(limit, LEXICAL_CANDIDATE_LIMIT)
    if not query_terms.route_profile.route_focused:
        return base
    if query_terms.metadata_profile.preferred_structure_type == "water_storage" and not query_terms.metadata_profile.has_explicit_topic(
        "water_distribution"
    ):
        return min(base, max(limit * 3, 48))
    if query_terms.route_profile.is_starter_build_project():
        return min(base, max(limit * 4, 64))
    return min(base, max(limit * 4, 60))


def build_guide_section_key(guide_id: str, guide_title: str, section_heading: str) -> str:
    base = guide_id.strip() or guide_title.strip()
    section = section_heading.strip()
    if section:
        return f"{base.lower()}::{section.lower()}"
    return base.lower()


def support_score(query_terms: QueryTerms, result: SearchResultRow) -> int:
    if result.retrieval_mode == "vector":
        return 0
    score = lexical_keyword_score(
        query_terms,
        title=result.title,
        section=result.section_heading,
        category=result.category,
        tags=result.topic_tags,
        description=result.snippet,
        document=result.body,
    )
    score += query_terms.metadata_profile.metadata_bonus(
        result.category,
        result.content_role,
        result.time_horizon,
        result.structure_type,
        result.topic_tags,
    )
    score += query_terms.metadata_profile.section_heading_bonus(result.section_heading)
    score += specialized_structured_anchor_bias(query_terms, result)
    if query_terms.metadata_profile.prefers_diversified_context and not result.section_heading.strip():
        score -= 8
    if result.retrieval_mode == "hybrid":
        score += 4
    elif result.retrieval_mode == "lexical":
        score += 2
    return score


def specialized_structured_anchor_bias(query_terms: QueryTerms, result: SearchResultRow) -> int:
    preferred = safe_text(query_terms.metadata_profile.preferred_structure_type).strip().lower()
    if preferred == "community_governance":
        return community_governance_structured_anchor_bias(query_terms, result)
    return 0


def community_governance_structured_anchor_bias(query_terms: QueryTerms, result: SearchResultRow) -> int:
    query_lower = safe_text(query_terms.query_lower)
    normalized_title = safe_text(result.title).strip().lower()
    normalized_section = safe_text(result.section_heading).strip().lower()
    normalized_mode = safe_text(result.retrieval_mode).strip().lower()
    financial_intent = contains_any(
        query_lower,
        {
            "insurance",
            "insured",
            "fund",
            "funds",
            "premium",
            "premiums",
            "pool",
            "pooling",
            "risk transfer",
            "reinsurance",
            "claim",
            "claims",
            "contribution",
            "contributions",
            "accounting",
            "ledger",
            "record",
            "records",
            "compensation",
        },
    )
    trust_repair_intent = contains_any(
        query_lower,
        {
            "merge",
            "trust",
            "vouch",
            "reputation",
            "mediation",
            "restitution",
            "sanction",
            "sanctions",
            "membership",
            "rules",
        },
    )

    score = 0
    if normalized_mode == "route-focus" and query_terms.metadata_profile.section_heading_bonus(result.section_heading) > 0:
        score += 12
    elif normalized_mode == "guide-focus" and normalized_section:
        score -= 6

    if not financial_intent and (
        "insurance" in normalized_title
        or "risk pooling" in normalized_title
        or "historical mutual aid" in normalized_section
        or "reinsurance" in normalized_section
        or "risk transfer" in normalized_section
        or "accounting" in normalized_section
        or "fund governance" in normalized_section
        or "actuarial" in normalized_section
        or "record-keeping" in normalized_section
    ):
        score -= 28

    if trust_repair_intent and (
        "monitoring" in normalized_section
        or "sanctions" in normalized_section
        or "mediation" in normalized_section
        or "membership" in normalized_section
        or "boundaries" in normalized_section
        or "conflict" in normalized_section
        or "restitution" in normalized_section
    ):
        score += 10

    if financial_intent and (
        "insurance" in normalized_title
        or "risk pooling" in normalized_title
        or "fund governance" in normalized_section
        or "accounting" in normalized_section
        or "record-keeping" in normalized_section
        or "reinsurance" in normalized_section
        or "risk transfer" in normalized_section
    ):
        score += 18

    return score


def search_result_from_hit(hit: dict[str, Any]) -> SearchResultRow:
    lexical_rank = hit.get("lexical_rank")
    vector_rank = hit.get("vector_rank")
    if lexical_rank is not None and vector_rank is not None:
        retrieval_mode = "hybrid"
    elif vector_rank is not None:
        retrieval_mode = "vector"
    else:
        retrieval_mode = "lexical"
    section_heading = safe_text(hit.get("section_heading"))
    subtitle = f"{safe_text(hit.get('guide_id'))} | {safe_text(hit.get('category'))} | {section_heading} | {retrieval_mode}"
    return SearchResultRow(
        title=safe_text(hit.get("guide_title")),
        subtitle=subtitle,
        snippet=clip(safe_text(hit.get("document")), 220),
        body=safe_text(hit.get("document")),
        guide_id=safe_text(hit.get("guide_id")),
        section_heading=section_heading,
        category=safe_text(hit.get("category")),
        retrieval_mode=retrieval_mode,
        content_role=safe_text(hit.get("content_role")),
        time_horizon=safe_text(hit.get("time_horizon")),
        structure_type=safe_text(hit.get("structure_type")),
        topic_tags=safe_text(hit.get("topic_tags")),
        chunk_id=safe_text(hit.get("chunk_id")),
        vector_row_id=int(hit.get("vector_row_id", -1)),
        lexical_rank=lexical_rank,
        vector_rank=vector_rank,
        vector_score=hit.get("vector_score"),
        hybrid_rrf_score=hit.get("hybrid_rrf_score"),
        keyword_score=hit.get("keyword_score"),
    )
