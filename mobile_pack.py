"""Helpers for exporting Senku's offline mobile knowledge pack."""

from __future__ import annotations

import hashlib
import json
import math
import re
import sqlite3
import struct
from array import array
from contextlib import closing
from dataclasses import asdict, dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable, Iterator, Sequence

import chromadb

import config
from deterministic_special_case_registry import DETERMINISTIC_SPECIAL_CASE_SPECS
from ingest import parse_frontmatter
from metadata_validation import (
    REPORT_FILENAME as METADATA_VALIDATION_REPORT_FILENAME,
    format_validation_errors,
    report_has_errors,
    validate_guide_records,
    write_validation_report,
)
from token_estimation import estimate_tokens


PACK_FORMAT_VERSION = 2
VECTOR_FILE_VERSION = 1
VECTOR_FILE_MAGIC = b"SNKUVEC1"
VECTOR_FILE_FORMAT = "senku-vectors-v1"
VECTOR_HEADER_STRUCT = struct.Struct("<8s6I")

VECTOR_DTYPE_CODES = {
    "float16": 1,
    "int8": 2,
}
VECTOR_DTYPE_SUFFIXES = {
    "float16": "f16",
    "int8": "i8",
}

VECTOR_FLAG_NORMALIZED = 1 << 0
VECTOR_FLAG_LITTLE_ENDIAN = 1 << 1

DEFAULT_COLLECTION_NAME = "senku_guides"
DEFAULT_SQLITE_FILENAME = "senku_mobile.sqlite3"
DEFAULT_MANIFEST_FILENAME = "senku_manifest.json"
DEFAULT_BATCH_SIZE = 256
DEFAULT_MOBILE_TOP_K = 10
DEFAULT_ANDROID_DETERMINISTIC_PREDICATE_MANIFEST = Path("android-app") / "deterministic_predicate_manifest.txt"
DEFAULT_ANDROID_DETERMINISTIC_ROUTER_PATH = (
    Path("android-app")
    / "app"
    / "src"
    / "main"
    / "java"
    / "com"
    / "senku"
    / "mobile"
    / "DeterministicAnswerRouter.java"
)

ANDROID_DETERMINISTIC_METHOD_PATTERN = re.compile(
    r"private static boolean (is[A-Z][A-Za-z0-9_]*)\s*\("
)
ANDROID_DETERMINISTIC_ACTIVE_RULE_COUNT_PATTERN = re.compile(
    r"ACTIVE_RULE_COUNT\s*=\s*(\d+)\s*;"
)

CONTENT_ROLE_STARTER = "starter"
CONTENT_ROLE_PLANNING = "planning"
CONTENT_ROLE_SUBSYSTEM = "subsystem"
CONTENT_ROLE_SAFETY = "safety"
CONTENT_ROLE_REFERENCE = "reference"

TIME_HORIZON_IMMEDIATE = "immediate"
TIME_HORIZON_LONG_TERM = "long_term"
TIME_HORIZON_MIXED = "mixed"

STRUCTURE_TYPE_CABIN_HOUSE = "cabin_house"
STRUCTURE_TYPE_EMERGENCY_SHELTER = "emergency_shelter"
STRUCTURE_TYPE_EARTH_SHELTER = "earth_shelter"
STRUCTURE_TYPE_WATER_STORAGE = "water_storage"
STRUCTURE_TYPE_WATER_DISTRIBUTION = "water_distribution"
STRUCTURE_TYPE_WATER_PURIFICATION = "water_purification"
STRUCTURE_TYPE_SMALL_WATERCRAFT = "small_watercraft"
STRUCTURE_TYPE_WOUND_CARE = "wound_care"
STRUCTURE_TYPE_SANITATION_SYSTEM = "sanitation_system"
STRUCTURE_TYPE_MESSAGE_AUTH = "message_auth"
STRUCTURE_TYPE_FAIR_TRIAL = "fair_trial"
STRUCTURE_TYPE_COMMUNITY_SECURITY = "community_security"
STRUCTURE_TYPE_COMMUNITY_GOVERNANCE = "community_governance"
STRUCTURE_TYPE_SOAPMAKING = "soapmaking"
STRUCTURE_TYPE_GLASSMAKING = "glassmaking"
STRUCTURE_TYPE_SAFETY_POISONING = "safety_poisoning"
STRUCTURE_TYPE_GENERAL = "general"

TOPIC_TAG_MARKERS = {
    "site_selection": (
        "site selection",
        "hazard assessment",
        "breaking ground",
        "grade stakes",
    ),
    "drainage": (
        "drainage",
        "french drain",
        "water shedding",
        "slope away",
        "runoff",
    ),
    "foundation": (
        "foundation work",
        "foundation waterproofing",
        "stone foundation",
        "slab foundation",
        "pier foundation",
        "footing",
        "footings",
        "frost line",
        "bearing capacity",
        "grade beam",
    ),
    "wall_construction": (
        "wall construction",
        "wall framing",
        "studs",
        "stud spacing",
        "studs on center",
        "timber frame wall",
    ),
    "roofing": (
        "roofing",
        "roof framing",
        "gable roof",
        "rafters",
        "ridge",
        "roof slab",
    ),
    "weatherproofing": (
        "weatherproof",
        "weatherproofing",
        "rainproof",
        "rainproofing",
        "waterproof",
        "sealant",
        "weatherstripping",
    ),
    "ventilation": (
        "ventilation",
        "condensation",
        "cross-ventilation",
        "gable vent",
        "earth tube",
    ),
    "water_storage": (
        "water storage",
        "stored water",
    ),
    "container_sanitation": (
        "clean container",
        "sanitize the container",
        "sanitise the container",
        "sanitized container",
        "sanitised container",
        "container sanitation",
        "food-grade",
        "food safe",
        "food-safe",
        "bleach rinse",
    ),
    "water_rotation": (
        "rotate stored water",
        "rotation schedule",
        "inspect stored water",
        "label containers",
        "date filled",
        "sealed container",
    ),
    "water_distribution": (
        "distribution",
        "gravity-fed distribution",
        "gravity fed distribution",
        "distribution system",
        "water distribution",
        "distribution network",
        "water tower",
        "cistern",
        "storage tank",
        "catchment",
        "spring box",
        "household taps",
        "community water",
        "pipe sizing",
        "pipe",
        "piping",
        "plumbing",
        "elevated storage and pressure systems",
    ),
    "water_purification": (
        "water purification",
        "safe drinking water",
        "water treatment",
        "treat water",
    ),
    "prefilter": (
        "prefilter",
        "pre-filter",
        "settle",
        "sediment",
        "clarification",
        "biosand",
    ),
    "disinfection": (
        "disinfection",
        "chlorination",
        "boiling",
        "sodis",
        "solar disinfection",
        "chemical treatment",
    ),
    "soapmaking": (
        "soap",
        "soap making",
        "soapmaking",
        "bar soap",
        "liquid soap",
        "saponification",
        "cold process soap",
        "hot process soap",
    ),
    "lye_safety": (
        "lye",
        "caustic soda",
        "sodium hydroxide",
        "potassium hydroxide",
        "caustic burns",
        "alkali burns",
        "chemical burns",
    ),
    "message_authentication": (
        "authenticate",
        "authentication",
        "challenge-response",
        "challenge response",
        "verification",
        "codeword",
        "code phrase",
        "authentic message",
        "evacuation order",
    ),
    "chain_of_custody": (
        "courier",
        "messenger",
        "chain-of-custody",
        "chain of custody",
        "messenger and courier security",
    ),
    "community_security": (
        "physical security",
        "guard rotation",
        "watch rotation",
        "patrol route",
        "checkpoint",
        "perimeter",
        "access control",
        "fortification",
        "early warning",
        "threat assessment",
    ),
    "resource_security": (
        "water point",
        "food storage",
        "storehouse",
        "granary",
        "critical infrastructure",
        "water system security",
        "field security",
    ),
    "community_governance": (
        "commons management",
        "resource governance",
        "resource rules",
        "mutual aid",
        "graduated sanctions",
        "restorative justice",
        "membership rules",
    ),
    "conflict_resolution": (
        "mediation",
        "dispute resolution",
        "restitution",
        "theft",
        "stealing",
        "stolen",
        "sanction",
        "sanctions",
    ),
    "trust_systems": (
        "trust",
        "reputation",
        "vouching",
        "vouch",
        "merge groups",
        "mutual aid",
        "trust recovery",
    ),
    "latrine_design": (
        "latrine",
        "latrine inspection",
        "pit latrine",
        "outhouse",
        "toilet trench",
    ),
    "wash_station": (
        "sanitation",
        "hygiene station",
        "wash station",
        "handwashing",
        "food-service restrictions",
        "outbreak",
        "dysentery",
    ),
    "glassmaking": (
        "glass making",
        "glassmaking",
        "glass furnace",
        "silica",
        "melt sand",
    ),
    "small_watercraft": (
        "watercraft",
        "canoe",
        "boat",
        "dugout",
        "kayak",
        "coracle",
        "rowboat",
    ),
    "hull": (
        "hull",
        "planking",
        "ribs",
        "buoyancy",
        "shoreline test",
        "stability",
    ),
    "sealing": (
        "boat caulking",
        "caulking",
        "pitch",
        "resin",
        "waterproof hull",
    ),
    "first_aid": (
        "first aid",
        "emergency response",
        "wound care",
        "wound management",
    ),
    "wound_cleaning": (
        "irrigation",
        "flush the wound",
        "wash the area",
        "clean dressing",
        "foreign body removal",
    ),
    "infection_monitoring": (
        "infection",
        "pus",
        "redness",
        "swelling",
        "red streak",
        "tetanus",
    ),
    "latrine_design": (
        "latrine",
        "latrine construction",
        "latrine and toilet system design",
        "pit latrine",
        "vip latrine",
        "toilet system",
        "latrine inspection",
        "inspect the latrine",
        "toilet trench",
        "outhouse",
    ),
    "wash_station": (
        "sanitation",
        "wash station",
        "handwashing station",
        "hand washing station",
        "handwashing",
        "hygiene station",
        "greywater",
        "food-service restrictions",
        "outbreak",
        "dysentery",
    ),
    "message_authentication": (
        "message authentication",
        "authenticate",
        "verify a note",
        "prove a note is real",
        "evacuation order",
        "order verification",
        "message verification",
        "verification",
        "tamper evident",
        "tamper-evident",
        "authentication",
        "codeword",
    ),
    "chain_of_custody": (
        "chain of custody",
        "chain-of-custody",
        "courier",
        "messenger",
        "courier protocol",
        "challenge-response",
        "challenge response",
        "messenger and courier security",
        "dead drops",
        "cut-outs",
        "cut outs",
    ),
    "trial_procedure": (
        "trial procedure",
        "lay tribunal",
        "community trial",
        "hearing order",
        "appeal",
        "restorative justice",
    ),
    "evidence_rules": (
        "evidence standards",
        "evidence rule",
        "record rules",
        "recordkeeping",
        "judicial independence",
    ),
    "soapmaking": (
        "soap",
        "soap making",
        "making soap",
        "saponification",
        "ash soap",
        "bar soap",
        "liquid soap",
        "cold process",
        "hot process",
        "tallow soap",
        "lye water",
    ),
    "lye_safety": (
        "caustic",
        "lye",
        "caustic soda",
        "sodium hydroxide",
        "potassium hydroxide",
        "bleach",
        "drain cleaner",
        "detergent pod",
        "poison control",
        "unknown ingestion",
        "corrosive exposure",
        "alkali burns",
        "chemical burns",
        "flush with water",
    ),
    "poisoning_triage": (
        "poisoning",
        "poison control",
        "unknown ingestion",
        "accidental poisoning",
        "do not induce vomiting",
        "activated charcoal",
        "decontamination",
        "chemical exposure",
        "corrosive exposure",
    ),
    "toxidrome": (
        "toxidrome",
        "toxidromes",
        "anticholinergic",
        "cholinergic",
        "sympathomimetic",
        "opioid overdose",
    ),
    "antidotes": (
        "antidote",
        "antidotes",
        "naloxone",
        "atropine",
        "pralidoxime",
        "n-acetylcysteine",
        "vitamin k",
    ),
    "glassmaking": (
        "glassmaking",
        "glass making",
        "make glass",
        "glass from scratch",
        "glass furnace",
        "silica",
        "soda-lime glass",
        "forming techniques",
    ),
    "annealing": (
        "annealing",
        "anneal",
        "controlled cooling",
        "thermal stress",
    ),
}

WATER_STORAGE_BODY_SUPPORT_MARKERS = (
    "cistern",
    "tank",
    "barrel",
    "drum",
    "jerry can",
    "food-grade",
    "food grade",
    "food-safe",
    "food safe",
    "clean container",
    "container sanitation",
    "sanitize the container",
    "sanitise the container",
    "sealed container",
    "date filled",
    "storage container",
)

WATER_DISTRIBUTION_CORE_MARKERS = (
    "water distribution",
    "distribution system",
    "distribution network",
    "gravity-fed distribution",
    "gravity fed distribution",
    "gravity-fed water distribution",
    "gravity fed water distribution",
    "water tower",
    "household taps",
    "community water",
    "pipe sizing",
    "distribution piping",
    "piping",
    "plumbing",
)

WATER_DISTRIBUTION_BODY_STRONG_MARKERS = WATER_DISTRIBUTION_CORE_MARKERS
WATER_DISTRIBUTION_BODY_SUPPORT_MARKERS = (
    "storage tank",
    "elevated storage",
    "cistern",
    "catchment",
    "spring box",
)

STRUCTURE_TYPE_MARKERS = (
    (STRUCTURE_TYPE_EMERGENCY_SHELTER, (
        "debris hut",
        "lean-to shelter",
        "a-frame shelter",
        "quinzhee",
        "wickiup",
        "tipi",
        "emergency shelter",
        "rain shelter",
        "rain fly",
        "tarp shelter",
        "tarp ridgeline",
        "ridgeline shelter",
    )),
    (STRUCTURE_TYPE_CABIN_HOUSE, (
        "construction & carpentry",
        "cabin",
        "dwelling",
        "house construction",
        "one room cabin",
        "timber framing",
        "wall construction",
        "roofing systems",
        "insulation & weatherproofing",
        "window and door assembly",
        "doors & window construction",
        "shelter site selection & hazard assessment",
    )),
    (STRUCTURE_TYPE_EARTH_SHELTER, (
        "earth-sheltering",
        "earth shelter",
        "earth sheltered",
        "underground shelter",
        "bunker",
        "cut-and-cover",
        "berm",
        "overburden",
        "earth tube",
    )),
    (STRUCTURE_TYPE_WATER_STORAGE, (
        "water storage",
        "stored water",
        "cistern",
    )),
    (STRUCTURE_TYPE_WATER_DISTRIBUTION, (
        "water distribution",
        "distribution system",
        "distribution network",
        "gravity-fed distribution",
        "gravity fed distribution",
        "gravity-fed water distribution",
        "gravity fed water distribution",
        "water system design and distribution",
        "community water distribution systems",
        "water tower",
        "household taps",
        "plumbing & water systems",
    )),
    (STRUCTURE_TYPE_WATER_PURIFICATION, (
        "water purification",
        "water chemistry & treatment",
        "safe drinking water",
        "water treatment",
        "biosand",
    )),
    (STRUCTURE_TYPE_SMALL_WATERCRAFT, (
        "small watercraft",
        "watercraft",
        "canoe",
        "boat",
        "dugout",
        "kayak",
        "coracle",
    )),
    (STRUCTURE_TYPE_WOUND_CARE, (
        "splinter",
        "foreign body removal",
        "puncture wound",
        "wound cleaning",
        "wound hygiene",
        "wound management",
        "infection prevention",
    )),
    (STRUCTURE_TYPE_SANITATION_SYSTEM, (
        "sanitation & public health",
        "sanitation and waste management systems",
        "latrine construction",
        "latrine and toilet system design",
        "wash station",
        "handwashing station",
    )),
    (STRUCTURE_TYPE_MESSAGE_AUTH, (
        "message authentication & courier protocols",
        "message authentication",
        "courier protocols",
        "chain of custody",
        "challenge-response",
        "tamper-evident",
    )),
    (STRUCTURE_TYPE_FAIR_TRIAL, (
        "criminal justice procedures for small communities",
        "trial procedure",
        "community courts",
        "lay tribunal",
        "evidence standards",
    )),
    (STRUCTURE_TYPE_COMMUNITY_SECURITY, (
        "critical infrastructure physical security",
        "community defense planning & fortification",
        "physical security",
        "guard rotation",
        "watch rotation",
        "checkpoint",
        "perimeter",
        "access control",
        "fortification",
        "early warning",
    )),
    (STRUCTURE_TYPE_COMMUNITY_GOVERNANCE, (
        "commons management & sustainable resource governance",
        "resource governance",
        "commons management",
        "mutual aid",
        "graduated sanctions",
        "restorative justice",
        "trust systems",
        "membership rules",
    )),
    (STRUCTURE_TYPE_SOAPMAKING, (
        "soap making",
        "making soap",
        "saponification",
        "ash & animal fat",
        "ash soap",
        "tallow soap",
    )),
    (STRUCTURE_TYPE_GLASSMAKING, (
        "glassmaking",
        "glass making",
        "glass-making-raw-materials",
        "forming techniques",
        "annealing",
        "soda-lime glass",
    )),
)

SOAPMAKING_BODY_STRONG_MARKERS = (
    "soap making",
    "making soap",
    "saponification",
    "tallow soap",
    "wood ash lye",
    "cold process soap",
    "hot process soap",
)

SOAPMAKING_BODY_SUPPORT_MARKERS = (
    "soap",
    "bar soap",
    "liquid soap",
    "animal fat",
    "tallow",
    "lye water",
    "trace",
    "cure",
)

GLASSMAKING_BODY_STRONG_MARKERS = (
    "glassmaking",
    "glass making",
    "make glass",
    "glass furnace",
    "soda-lime glass",
)

MESSAGE_AUTH_BODY_STRONG_MARKERS = (
    "message authentication",
    "challenge-response",
    "challenge response",
    "chain of custody",
    "tamper-evident",
    "tamper evident",
)

FAIR_TRIAL_BODY_STRONG_MARKERS = (
    "trial procedure",
    "lay tribunal",
    "community trial",
    "evidence standards",
    "hearing order",
)

COMMUNITY_SECURITY_BODY_STRONG_MARKERS = (
    "critical infrastructure physical security",
    "physical security",
    "guard rotation",
    "watch rotation",
    "patrol route",
    "checkpoint",
    "perimeter",
    "access control",
    "fortification",
    "early warning",
    "water system security",
)

COMMUNITY_SECURITY_BODY_SUPPORT_MARKERS = (
    "water point",
    "food storage",
    "storehouse",
    "granary",
    "critical infrastructure",
    "threat assessment",
)

COMMUNITY_GOVERNANCE_BODY_STRONG_MARKERS = (
    "commons management",
    "resource governance",
    "mutual aid",
    "graduated sanctions",
    "restorative justice",
    "membership rules",
    "trust recovery",
)

COMMUNITY_GOVERNANCE_BODY_SUPPORT_MARKERS = (
    "mediation",
    "dispute resolution",
    "restitution",
    "theft",
    "stealing",
    "stolen",
    "trust",
    "reputation",
    "vouching",
    "vouch",
    "merge groups",
)

PLANNING_ROLE_MARKERS = (
    "site selection",
    "hazard assessment",
    "construction sequence",
    "system design",
    "hydration assurance",
    "overview",
    "introduction",
)
STARTER_ROLE_MARKERS = (
    "basics",
    "essentials",
    "starter",
    "getting started",
    "wall construction",
    "roofing systems",
    "water purification",
    "water storage",
)
SAFETY_ROLE_MARKERS = (
    "safety",
    "hazard",
    "warning",
    "risk",
    "red flags",
    "protocol",
)
REFERENCE_ROLE_MARKERS = (
    "maintenance",
    "troubleshooting",
    "repair",
    "calculation",
    "design principle",
    "properties",
    "materials",
)
IMMEDIATE_TIME_MARKERS = (
    "emergency",
    "immediate",
    "first 24",
    "first 72",
    "rapid",
    "temporary",
)
LONG_TERM_TIME_MARKERS = (
    "long-term",
    "long term",
    "semi-permanent",
    "permanent",
    "seasonal",
    "rotation",
    "camp evolution",
)

BUILDING_CONTEXT_MARKERS = (
    "construction & carpentry",
    "carpentry",
    "cabin",
    "dwelling",
    "house",
    "shelter",
    "roof",
    "roofing",
    "wall construction",
    "wall framing",
    "timber framing",
    "weatherproofing",
    "window and door",
    "site selection",
    "foundation work",
)
WATER_CONTEXT_MARKERS = (
    "water",
    "hydration",
    "container",
    "cistern",
    "rainwater",
    "purification",
    "filtration",
    "disinfection",
    "stored water",
    "water treatment",
)
MEDICAL_CONTEXT_MARKERS = (
    "medical",
    "first aid",
    "wound",
    "puncture",
    "splinter",
    "foreign body",
    "infection",
    "dressing",
    "tetanus",
)
SANITATION_CONTEXT_MARKERS = (
    "latrine",
    "toilet",
    "sanitation",
    "wash station",
    "handwashing",
    "waste management",
    "greywater",
)
SOCIETY_CONTEXT_MARKERS = (
    "courier",
    "message",
    "note",
    "order verification",
    "authentication",
    "chain of custody",
    "trial",
    "evidence",
    "appeal",
)
COMMUNITY_SECURITY_CONTEXT_MARKERS = (
    "security",
    "guard",
    "patrol",
    "checkpoint",
    "perimeter",
    "access control",
    "fortification",
    "early warning",
    "critical infrastructure",
)
COMMUNITY_GOVERNANCE_CONTEXT_MARKERS = (
    "commons",
    "governance",
    "trust",
    "reputation",
    "mutual aid",
    "mediation",
    "restitution",
    "theft",
    "stealing",
    "stolen",
    "sanction",
    "sanctions",
)
CHEMISTRY_CONTEXT_MARKERS = (
    "soap",
    "lye",
    "saponification",
    "glass",
    "annealing",
    "silica",
    "soda ash",
    "furnace",
)
SAFETY_POISONING_CORE_MARKERS = (
    "toxicology",
    "poisoning",
    "poison management",
    "poison control",
    "toxidrome",
    "toxidromes",
    "antidote",
    "antidotes",
    "unknown ingestion",
    "accidental poisoning",
)
SAFETY_POISONING_SUPPORT_MARKERS = (
    "child accidental poisoning",
    "swallowed cleaner",
    "do not induce vomiting",
    "activated charcoal",
    "decontamination",
    "chemical exposure",
    "corrosive exposure",
    "rodenticide",
    "smoke and carbon monoxide exposure",
    "field poisoning",
)
BUILDING_TOPIC_TAGS = frozenset(
    {
        "site_selection",
        "drainage",
        "foundation",
        "wall_construction",
        "roofing",
        "weatherproofing",
        "ventilation",
    }
)
WATER_TOPIC_TAGS = frozenset(
    {
        "water_storage",
        "container_sanitation",
        "water_rotation",
        "water_distribution",
        "water_purification",
        "prefilter",
        "disinfection",
    }
)
WATERCRAFT_TOPIC_TAGS = frozenset(
    {
        "small_watercraft",
        "hull",
        "sealing",
    }
)
MEDICAL_TOPIC_TAGS = frozenset(
    {
        "first_aid",
        "wound_cleaning",
        "infection_monitoring",
    }
)
SANITATION_TOPIC_TAGS = frozenset({"latrine_design", "wash_station"})
SOCIETY_TOPIC_TAGS = frozenset(
    {"message_authentication", "chain_of_custody", "trial_procedure", "evidence_rules"}
)
COMMUNITY_SECURITY_TOPIC_TAGS = frozenset({"community_security", "resource_security"})
COMMUNITY_GOVERNANCE_TOPIC_TAGS = frozenset(
    {"community_governance", "conflict_resolution", "trust_systems"}
)
CHEMISTRY_TOPIC_TAGS = frozenset({"soapmaking", "lye_safety", "glassmaking", "annealing"})
POISONING_TOPIC_TAGS = frozenset({"poisoning_triage", "toxidrome", "antidotes"})


@dataclass(frozen=True)
class GuideRecord:
    guide_id: str
    slug: str
    title: str
    source_file: str
    description: str = ""
    category: str = ""
    difficulty: str = ""
    last_updated: str = ""
    version: str = ""
    liability_level: str = ""
    tags: str = ""
    related: str = ""
    body_markdown: str = ""


@dataclass(frozen=True)
class ChunkRecord:
    chunk_id: str
    source_file: str
    guide_id: str
    guide_title: str
    slug: str
    description: str
    category: str
    difficulty: str
    last_updated: str
    version: str
    liability_level: str
    tags: str
    related: str
    section_id: str
    section_heading: str
    document: str
    embedding: Sequence[float]


@dataclass(frozen=True)
class DeterministicRuleRecord:
    rule_id: str
    predicate_name: str
    builder_name: str
    sample_prompt: str


@dataclass(frozen=True)
class AndroidDeterministicPredicateManifestEntry:
    rule_id: str
    predicate_name: str
    android_predicate_name: str


@dataclass(frozen=True)
class ExportSummary:
    output_dir: str
    manifest_path: str
    sqlite_path: str
    vector_path: str
    vector_dtype: str
    vector_dimension: int
    counts: dict
    files: dict


@dataclass(frozen=True)
class RetrievalMetadata:
    content_role: str
    time_horizon: str
    structure_type: str
    topic_tags: str


class VectorFileWriter:
    """Write a Senku mobile vector matrix in a compact row-major format."""

    def __init__(self, path: Path, row_count: int, dimension: int, vector_dtype: str):
        if vector_dtype not in VECTOR_DTYPE_CODES:
            raise ValueError(f"Unsupported vector dtype: {vector_dtype}")
        self.path = Path(path)
        self.row_count = row_count
        self.dimension = dimension
        self.vector_dtype = vector_dtype
        self.handle = self.path.open("wb")
        self._write_header()
        self._rows_written = 0
        self._float16_struct = None
        if vector_dtype == "float16":
            self._float16_struct = struct.Struct("<" + ("e" * dimension))

    def _write_header(self):
        flags = VECTOR_FLAG_NORMALIZED | VECTOR_FLAG_LITTLE_ENDIAN
        self.handle.write(
            VECTOR_HEADER_STRUCT.pack(
                VECTOR_FILE_MAGIC,
                VECTOR_FILE_VERSION,
                VECTOR_HEADER_STRUCT.size,
                self.row_count,
                self.dimension,
                VECTOR_DTYPE_CODES[self.vector_dtype],
                flags,
            )
        )

    def write_row(self, vector: Sequence[float]):
        if len(vector) != self.dimension:
            raise ValueError(
                f"Vector dimension mismatch for row {self._rows_written}: "
                f"expected {self.dimension}, got {len(vector)}"
            )

        normalized = _normalize_vector(vector)
        if self.vector_dtype == "float16":
            self.handle.write(self._float16_struct.pack(*normalized))
        else:
            quantized = array("b", (_quantize_int8(value) for value in normalized))
            self.handle.write(quantized.tobytes())

        self._rows_written += 1

    def close(self):
        if self.handle.closed:
            return
        self.handle.close()
        if self._rows_written != self.row_count:
            raise ValueError(
                f"Vector row count mismatch: expected {self.row_count}, wrote {self._rows_written}"
            )


def load_guide_records(guides_dir=None):
    """Load guide frontmatter/body into mobile-pack guide records."""
    guides_path = Path(guides_dir or config.COMPENDIUM_DIR)
    records = []
    for path in sorted(guides_path.glob("*.md")):
        text = path.read_text(encoding="utf-8")
        meta, body = parse_frontmatter(text)
        if meta is None:
            raise ValueError(f"Guide is missing valid YAML frontmatter: {path}")

        guide_id = _safe_text(meta.get("id"))
        slug = _safe_text(meta.get("slug"))
        title = _safe_text(meta.get("title"))
        if not guide_id or not slug or not title:
            raise ValueError(f"Guide is missing required id/slug/title fields: {path}")

        records.append(
            GuideRecord(
                guide_id=guide_id,
                slug=slug,
                title=title,
                source_file=path.name,
                description=_safe_text(meta.get("description")),
                category=_safe_text(meta.get("category")),
                difficulty=_safe_text(meta.get("difficulty")),
                last_updated=_safe_text(meta.get("last_updated")),
                version=_safe_text(meta.get("version")),
                liability_level=_safe_text(meta.get("liability_level")),
                tags=_join_meta_list(meta.get("tags")),
                related=_join_meta_list(meta.get("related")),
                body_markdown=(body or "").strip(),
            )
        )
    return records


def load_deterministic_rule_records(
    manifest_path: Path | str = DEFAULT_ANDROID_DETERMINISTIC_PREDICATE_MANIFEST,
):
    """Expose the Android-backed deterministic registry rows for mobile-pack metadata."""
    manifest_entries = load_android_deterministic_predicate_manifest(manifest_path)
    specs_by_rule_id = {
        spec.rule_id: spec
        for spec in DETERMINISTIC_SPECIAL_CASE_SPECS
    }
    records = []
    for entry in manifest_entries:
        spec = specs_by_rule_id.get(entry.rule_id)
        if spec is None:
            raise ValueError(
                "Android deterministic manifest references unknown desktop rule_id "
                f"{entry.rule_id!r}"
            )
        if spec.predicate_name != entry.predicate_name:
            raise ValueError(
                "Android deterministic manifest predicate mismatch for rule_id "
                f"{entry.rule_id!r}: expected {spec.predicate_name!r}, "
                f"got {entry.predicate_name!r}"
            )
        records.append(
            DeterministicRuleRecord(
                rule_id=spec.rule_id,
                predicate_name=spec.predicate_name,
                builder_name=spec.builder_name,
                sample_prompt=spec.sample_prompt,
            )
        )
    return records


def load_android_deterministic_predicate_manifest(
    manifest_path: Path | str = DEFAULT_ANDROID_DETERMINISTIC_PREDICATE_MANIFEST,
):
    """Load the checked-in Android deterministic predicate manifest."""
    resolved_path = _resolve_repo_relative_path(manifest_path)
    if not resolved_path.is_file():
        raise ValueError(
            "Android deterministic predicate manifest not found: "
            f"{resolved_path}"
        )

    entries = []
    seen_rule_ids = {}
    seen_predicates = {}
    seen_android_predicates = {}
    for line_number, raw_line in enumerate(
        resolved_path.read_text(encoding="utf-8").splitlines(),
        start=1,
    ):
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        parts = [part.strip() for part in line.split("|")]
        if len(parts) != 3 or any(not part for part in parts):
            raise ValueError(
                "Invalid Android deterministic predicate manifest row at "
                f"{resolved_path}:{line_number}: {raw_line!r}"
            )
        entry = AndroidDeterministicPredicateManifestEntry(
            rule_id=parts[0],
            predicate_name=parts[1],
            android_predicate_name=parts[2],
        )
        duplicate_rule_line = seen_rule_ids.get(entry.rule_id)
        if duplicate_rule_line is not None:
            raise ValueError(
                "Duplicate Android deterministic manifest rule_id "
                f"{entry.rule_id!r} at {resolved_path}:{line_number}; "
                f"first seen on line {duplicate_rule_line}"
            )
        duplicate_predicate_line = seen_predicates.get(entry.predicate_name)
        if duplicate_predicate_line is not None:
            raise ValueError(
                "Duplicate Android deterministic manifest predicate "
                f"{entry.predicate_name!r} at {resolved_path}:{line_number}; "
                f"first seen on line {duplicate_predicate_line}"
            )
        duplicate_android_line = seen_android_predicates.get(entry.android_predicate_name)
        if duplicate_android_line is not None:
            raise ValueError(
                "Duplicate Android deterministic manifest Android predicate "
                f"{entry.android_predicate_name!r} at {resolved_path}:{line_number}; "
                f"first seen on line {duplicate_android_line}"
            )
        seen_rule_ids[entry.rule_id] = line_number
        seen_predicates[entry.predicate_name] = line_number
        seen_android_predicates[entry.android_predicate_name] = line_number
        entries.append(entry)

    if not entries:
        raise ValueError(
            "Android deterministic predicate manifest contained no entries: "
            f"{resolved_path}"
        )
    return entries


def parse_android_deterministic_predicate_names(
    router_path: Path | str = DEFAULT_ANDROID_DETERMINISTIC_ROUTER_PATH,
):
    """Parse Android predicate method names from the handwritten router."""
    resolved_path = _resolve_repo_relative_path(router_path)
    if not resolved_path.is_file():
        raise ValueError(f"Android deterministic router not found: {resolved_path}")

    source = resolved_path.read_text(encoding="utf-8")
    predicate_names = ANDROID_DETERMINISTIC_METHOD_PATTERN.findall(source)
    if not predicate_names:
        raise ValueError(
            "No Android deterministic predicate methods found in "
            f"{resolved_path}"
        )
    if len(predicate_names) != len(set(predicate_names)):
        duplicates = sorted(
            {
                name
                for name in predicate_names
                if predicate_names.count(name) > 1
            }
        )
        raise ValueError(
            "Duplicate Android deterministic predicate methods found in "
            f"{resolved_path}: {', '.join(duplicates)}"
        )

    active_rule_count_match = ANDROID_DETERMINISTIC_ACTIVE_RULE_COUNT_PATTERN.search(source)
    if active_rule_count_match is not None:
        active_rule_count = int(active_rule_count_match.group(1))
        if active_rule_count != len(predicate_names):
            raise ValueError(
                "DeterministicAnswerRouter ACTIVE_RULE_COUNT mismatch: "
                f"declares {active_rule_count}, parsed {len(predicate_names)} "
                f"predicate methods in {resolved_path}"
            )
    return predicate_names


def validate_mobile_pack_deterministic_parity(
    *,
    manifest_path: Path | str = DEFAULT_ANDROID_DETERMINISTIC_PREDICATE_MANIFEST,
    router_path: Path | str = DEFAULT_ANDROID_DETERMINISTIC_ROUTER_PATH,
):
    """Validate mobile-pack deterministic metadata parity with Android coverage."""
    manifest_entries = load_android_deterministic_predicate_manifest(manifest_path)
    exported_rule_records = load_deterministic_rule_records(manifest_path)
    java_predicate_names = parse_android_deterministic_predicate_names(router_path)

    manifest_android_predicates = [entry.android_predicate_name for entry in manifest_entries]
    manifest_rule_ids = [entry.rule_id for entry in manifest_entries]
    manifest_predicates = [entry.predicate_name for entry in manifest_entries]

    failures = []

    missing_in_manifest = sorted(set(java_predicate_names) - set(manifest_android_predicates))
    if missing_in_manifest:
        failures.append(
            "Android router predicates missing from manifest: "
            + ", ".join(missing_in_manifest)
        )

    missing_in_router = sorted(set(manifest_android_predicates) - set(java_predicate_names))
    if missing_in_router:
        failures.append(
            "Manifest expects Android predicates missing from router: "
            + ", ".join(missing_in_router)
        )

    exported_rule_ids = [record.rule_id for record in exported_rule_records]
    exported_predicates = [record.predicate_name for record in exported_rule_records]
    if exported_rule_ids != manifest_rule_ids:
        failures.append(
            "Exported deterministic rule ids do not match the Android manifest order"
        )
    if exported_predicates != manifest_predicates:
        failures.append(
            "Exported deterministic predicate names do not match the Android manifest"
        )

    if failures:
        raise ValueError("\n".join(failures))

    return {
        "manifest_path": str(_resolve_repo_relative_path(manifest_path)),
        "router_path": str(_resolve_repo_relative_path(router_path)),
        "manifest_entry_count": len(manifest_entries),
        "java_predicate_count": len(java_predicate_names),
        "exported_rule_count": len(exported_rule_records),
        "rule_ids": manifest_rule_ids,
        "android_predicate_names": manifest_android_predicates,
    }


def _resolve_repo_relative_path(path: Path | str):
    candidate = Path(path)
    if candidate.is_absolute():
        return candidate
    return Path(__file__).resolve().parent / candidate


def _load_all_deterministic_rule_records():
    return [
        DeterministicRuleRecord(
            rule_id=spec.rule_id,
            predicate_name=spec.predicate_name,
            builder_name=spec.builder_name,
            sample_prompt=spec.sample_prompt,
        )
        for spec in DETERMINISTIC_SPECIAL_CASE_SPECS
    ]


def export_mobile_pack(
    output_dir,
    guide_records: Sequence[GuideRecord],
    chunk_batches: Iterable[Sequence[ChunkRecord]],
    *,
    chunk_count: int,
    embedding_model_id: str,
    vector_dtype: str = "float16",
    mobile_top_k: int = DEFAULT_MOBILE_TOP_K,
    source=None,
    sqlite_filename: str = DEFAULT_SQLITE_FILENAME,
    manifest_filename: str = DEFAULT_MANIFEST_FILENAME,
):
    """Write a self-contained mobile knowledge pack from prepared guide/chunk rows."""
    if vector_dtype not in VECTOR_DTYPE_CODES:
        raise ValueError(f"Unsupported vector dtype: {vector_dtype}")
    if chunk_count <= 0:
        raise ValueError("chunk_count must be positive")

    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)

    sqlite_path = output_path / sqlite_filename
    manifest_path = output_path / manifest_filename
    vector_path = output_path / _vector_filename_for_dtype(vector_dtype)
    metadata_report_path = output_path / METADATA_VALIDATION_REPORT_FILENAME

    for path in (sqlite_path, manifest_path, vector_path):
        if path.exists():
            path.unlink()

    guide_records = list(guide_records)
    metadata_report = validate_guide_records(
        guide_records,
        scope="mobile_pack_export",
    )
    write_validation_report(metadata_report, metadata_report_path)
    if report_has_errors(metadata_report):
        raise ValueError(format_validation_errors(metadata_report))
    rule_records = load_deterministic_rule_records()
    guide_metadata_by_id = {
        guide.guide_id: _derive_guide_metadata(guide)
        for guide in guide_records
    }
    guide_by_id = {guide.guide_id: guide for guide in guide_records}
    guide_by_slug = {guide.slug: guide for guide in guide_records}
    related_link_count = sum(
        1 for guide in guide_records for item in _split_csv_field(guide.related)
    )

    vector_dimension = None
    vector_writer = None
    written_chunk_count = 0

    try:
        with closing(sqlite3.connect(sqlite_path)) as conn:
            conn.execute("PRAGMA journal_mode=DELETE")
            conn.execute("PRAGMA synchronous=NORMAL")
            _create_mobile_schema(conn)
            _insert_guides(conn, guide_records, guide_by_slug, guide_by_id, guide_metadata_by_id)
            _insert_deterministic_rules(conn, rule_records)

            for batch in chunk_batches:
                rows = list(batch)
                if not rows:
                    continue

                if vector_dimension is None:
                    vector_dimension = len(rows[0].embedding)
                    if vector_dimension <= 0:
                        raise ValueError("Chunk embeddings must have a positive dimension")
                    vector_writer = VectorFileWriter(
                        vector_path,
                        row_count=chunk_count,
                        dimension=vector_dimension,
                        vector_dtype=vector_dtype,
                    )

                for row in rows:
                    if row.guide_id not in guide_by_id:
                        raise ValueError(
                            f"Chunk {row.chunk_id} references unknown guide id {row.guide_id!r}"
                        )
                    if len(row.embedding) != vector_dimension:
                        raise ValueError(
                            f"Chunk {row.chunk_id} has embedding dimension {len(row.embedding)} "
                            f"but expected {vector_dimension}"
                        )

                _insert_chunk_batch(
                    conn,
                    rows,
                    vector_row_start=written_chunk_count,
                    guide_metadata_by_id=guide_metadata_by_id,
                )
                for row in rows:
                    vector_writer.write_row(row.embedding)
                written_chunk_count += len(rows)

            if vector_writer is None or vector_dimension is None:
                raise ValueError("No chunk rows were provided for export")
    finally:
        if vector_writer is not None:
            vector_writer.close()

    if written_chunk_count != chunk_count:
        raise ValueError(
            f"Chunk export count mismatch: expected {chunk_count}, wrote {written_chunk_count}"
        )

    counts = {
        "guides": len(guide_records),
        "chunks": chunk_count,
        "deterministic_rules": len(rule_records),
        "guide_related_links": related_link_count,
        "retrieval_metadata_guides": sum(
            1
            for metadata in guide_metadata_by_id.values()
            if metadata.structure_type != STRUCTURE_TYPE_GENERAL or metadata.topic_tags
        ),
    }
    generated_at = datetime.now(timezone.utc).isoformat()
    vector_file = _file_info(vector_path)
    with closing(sqlite3.connect(sqlite_path)) as conn:
        # pack_meta hashes are export telemetry only; PackInstaller validates
        # against senku_manifest.json as the single source of truth.
        _upsert_pack_meta(
            conn,
            {
                "pack_version": str(PACK_FORMAT_VERSION),
                "generated_at": generated_at,
                "embedding_model_id": embedding_model_id,
                "vector_dtype": vector_dtype,
                "vector_dimension": str(vector_dimension),
                "mobile_top_k": str(mobile_top_k),
                "guide_count": str(counts["guides"]),
                "chunk_count": str(counts["chunks"]),
                "deterministic_rule_count": str(counts["deterministic_rules"]),
                "retrieval_metadata_guide_count": str(counts["retrieval_metadata_guides"]),
                "sqlite_filename": sqlite_path.name,
                "vector_filename": vector_path.name,
                "manifest_filename": manifest_path.name,
                "sqlite_sha256": "see_senku_manifest.json",
                "vector_sha256": vector_file["sha256"],
            },
        )

    files = {
        "sqlite": _file_info(sqlite_path),
        "vectors": vector_file,
    }
    manifest = _build_manifest(
        counts=counts,
        files=files,
        embedding_model_id=embedding_model_id,
        vector_dtype=vector_dtype,
        vector_dimension=vector_dimension,
        mobile_top_k=mobile_top_k,
        source=source or {},
    )
    manifest["generated_at"] = generated_at
    manifest_path.write_text(
        json.dumps(manifest, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )

    return ExportSummary(
        output_dir=str(output_path),
        manifest_path=str(manifest_path),
        sqlite_path=str(sqlite_path),
        vector_path=str(vector_path),
        vector_dtype=vector_dtype,
        vector_dimension=vector_dimension,
        counts=counts,
        files=files,
    )


def export_mobile_pack_from_chroma(
    output_dir,
    *,
    chroma_dir=None,
    guides_dir=None,
    collection_name: str = DEFAULT_COLLECTION_NAME,
    embedding_model_id: str = None,
    vector_dtype: str = "float16",
    mobile_top_k: int = DEFAULT_MOBILE_TOP_K,
    batch_size: int = DEFAULT_BATCH_SIZE,
):
    """Build a mobile pack from the current guides directory and Chroma collection."""
    chroma_path = Path(chroma_dir or config.CHROMA_DB_DIR)
    guides_path = Path(guides_dir or config.COMPENDIUM_DIR)
    embedding_model_id = embedding_model_id or config.EMBED_MODEL

    client = chromadb.PersistentClient(path=str(chroma_path))
    collection = client.get_collection(collection_name)
    sorted_ids = _sorted_chunk_ids(collection, batch_size=batch_size)
    if not sorted_ids:
        raise ValueError(f"Collection {collection_name!r} contained no chunks")

    guide_records = load_guide_records(guides_path)
    source = {
        "collection_name": collection_name,
        "chroma_dir": str(chroma_path),
        "guides_dir": str(guides_path),
        "desktop_top_k": getattr(config, "TOP_K", None),
        "prompt_modes": list(getattr(config, "PROMPT_MODES", ())),
    }
    chunk_batches = _iter_chunk_batches(collection, sorted_ids, batch_size=batch_size)
    return export_mobile_pack(
        output_dir,
        guide_records,
        chunk_batches,
        chunk_count=len(sorted_ids),
        embedding_model_id=embedding_model_id,
        vector_dtype=vector_dtype,
        mobile_top_k=mobile_top_k,
        source=source,
    )


def _create_mobile_schema(conn):
    conn.executescript(
        """
        CREATE TABLE guides (
            guide_id TEXT PRIMARY KEY,
            slug TEXT NOT NULL UNIQUE,
            title TEXT NOT NULL,
            source_file TEXT NOT NULL,
            description TEXT,
            category TEXT,
            difficulty TEXT,
            last_updated TEXT,
            version TEXT,
            liability_level TEXT,
            tags TEXT,
            related TEXT,
            body_markdown TEXT,
            content_role TEXT,
            time_horizon TEXT,
            structure_type TEXT,
            topic_tags TEXT
        );

        CREATE INDEX guides_category_idx ON guides(category);
        CREATE INDEX guides_slug_idx ON guides(slug);
        CREATE INDEX guides_structure_type_idx ON guides(structure_type);
        CREATE INDEX guides_content_role_idx ON guides(content_role);

        CREATE TABLE guide_related (
            guide_id TEXT NOT NULL,
            related_ref TEXT NOT NULL,
            related_guide_id TEXT,
            related_slug TEXT,
            PRIMARY KEY (guide_id, related_ref)
        );

        CREATE INDEX guide_related_guide_idx ON guide_related(guide_id);

        CREATE TABLE deterministic_rules (
            rule_id TEXT PRIMARY KEY,
            predicate_name TEXT NOT NULL,
            builder_name TEXT NOT NULL,
            sample_prompt TEXT NOT NULL
        );

        CREATE TABLE chunks (
            row_id INTEGER PRIMARY KEY,
            vector_row_id INTEGER NOT NULL UNIQUE,
            chunk_id TEXT NOT NULL UNIQUE,
            document TEXT NOT NULL,
            source_file TEXT,
            guide_id TEXT NOT NULL,
            guide_title TEXT,
            slug TEXT,
            description TEXT,
            category TEXT,
            difficulty TEXT,
            last_updated TEXT,
            version TEXT,
            liability_level TEXT,
            tags TEXT,
            related TEXT,
            section_id TEXT,
            section_heading TEXT,
            token_estimate INTEGER NOT NULL,
            content_role TEXT,
            time_horizon TEXT,
            structure_type TEXT,
            topic_tags TEXT
        );

        CREATE INDEX chunks_guide_id_idx ON chunks(guide_id);
        CREATE INDEX chunks_category_idx ON chunks(category);
        CREATE INDEX chunks_slug_idx ON chunks(slug);
        CREATE INDEX chunks_vector_row_idx ON chunks(vector_row_id);
        CREATE INDEX chunks_structure_type_idx ON chunks(structure_type);
        CREATE INDEX chunks_content_role_idx ON chunks(content_role);

        CREATE VIRTUAL TABLE lexical_chunks_fts USING fts5(
            chunk_id UNINDEXED,
            search_text,
            guide_title,
            guide_id,
            section_heading,
            slug,
            tags,
            description,
            category,
            liability_level,
            content_role,
            time_horizon,
            structure_type,
            topic_tags,
            tokenize='porter unicode61'
        );

        CREATE VIRTUAL TABLE lexical_chunks_fts4 USING fts4(
            chunk_id,
            search_text,
            guide_title,
            guide_id,
            section_heading,
            slug,
            tags,
            description,
            category,
            liability_level,
            content_role,
            time_horizon,
            structure_type,
            topic_tags,
            tokenize=porter
        );

        CREATE TABLE pack_meta (
            key TEXT PRIMARY KEY,
            value TEXT NOT NULL
        );
        """
    )


def _insert_guides(conn, guide_records, guide_by_slug, guide_by_id, guide_metadata_by_id):
    guide_rows = [
        (
            guide.guide_id,
            guide.slug,
            guide.title,
            guide.source_file,
            guide.description,
            guide.category,
            guide.difficulty,
            guide.last_updated,
            guide.version,
            guide.liability_level,
            guide.tags,
            guide.related,
            guide.body_markdown,
            guide_metadata_by_id[guide.guide_id].content_role,
            guide_metadata_by_id[guide.guide_id].time_horizon,
            guide_metadata_by_id[guide.guide_id].structure_type,
            guide_metadata_by_id[guide.guide_id].topic_tags,
        )
        for guide in guide_records
    ]
    conn.executemany(
        """
        INSERT INTO guides (
            guide_id, slug, title, source_file, description, category, difficulty,
            last_updated, version, liability_level, tags, related, body_markdown,
            content_role, time_horizon, structure_type, topic_tags
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        guide_rows,
    )

    relation_rows = []
    for guide in guide_records:
        for related_ref in _split_csv_field(guide.related):
            resolved = guide_by_slug.get(related_ref) or guide_by_id.get(related_ref)
            relation_rows.append(
                (
                    guide.guide_id,
                    related_ref,
                    resolved.guide_id if resolved else None,
                    resolved.slug if resolved else None,
                )
            )
    if relation_rows:
        conn.executemany(
            """
            INSERT INTO guide_related (
                guide_id, related_ref, related_guide_id, related_slug
            ) VALUES (?, ?, ?, ?)
            """,
            relation_rows,
        )
    conn.commit()


def _insert_deterministic_rules(conn, rule_records):
    conn.executemany(
        """
        INSERT INTO deterministic_rules (rule_id, predicate_name, builder_name, sample_prompt)
        VALUES (?, ?, ?, ?)
        """,
        [
            (
                rule.rule_id,
                rule.predicate_name,
                rule.builder_name,
                rule.sample_prompt,
            )
            for rule in rule_records
        ],
    )
    conn.commit()


def _insert_chunk_batch(conn, rows, *, vector_row_start, guide_metadata_by_id):
    chunk_rows = []
    fts_rows = []
    for offset, row in enumerate(rows):
        metadata = _derive_chunk_metadata(row, guide_metadata_by_id.get(row.guide_id))
        chunk_rows.append(
            (
                vector_row_start + offset,
                row.chunk_id,
                row.document,
                row.source_file,
                row.guide_id,
                row.guide_title,
                row.slug,
                row.description,
                row.category,
                row.difficulty,
                row.last_updated,
                row.version,
                row.liability_level,
                row.tags,
                row.related,
                row.section_id,
                row.section_heading,
                estimate_tokens(row.document),
                metadata.content_role,
                metadata.time_horizon,
                metadata.structure_type,
                metadata.topic_tags,
            )
        )
        fts_rows.append(
            (
                row.chunk_id,
                row.document,
                row.guide_title,
                row.guide_id,
                row.section_heading,
                row.slug,
                row.tags,
                row.description,
                row.category,
                row.liability_level,
                metadata.content_role,
                metadata.time_horizon,
                metadata.structure_type,
                metadata.topic_tags,
            )
        )

    conn.executemany(
        """
        INSERT INTO chunks (
            vector_row_id, chunk_id, document, source_file, guide_id, guide_title,
            slug, description, category, difficulty, last_updated, version,
            liability_level, tags, related, section_id, section_heading, token_estimate,
            content_role, time_horizon, structure_type, topic_tags
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        chunk_rows,
    )
    conn.executemany(
        """
        INSERT INTO lexical_chunks_fts (
            chunk_id, search_text, guide_title, guide_id, section_heading,
            slug, tags, description, category, liability_level,
            content_role, time_horizon, structure_type, topic_tags
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        fts_rows,
    )
    conn.executemany(
        """
        INSERT INTO lexical_chunks_fts4 (
            chunk_id, search_text, guide_title, guide_id, section_heading,
            slug, tags, description, category, liability_level,
            content_role, time_horizon, structure_type, topic_tags
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        fts_rows,
    )
    conn.commit()


def _upsert_pack_meta(conn, entries):
    conn.executemany(
        """
        INSERT INTO pack_meta (key, value)
        VALUES (?, ?)
        ON CONFLICT(key) DO UPDATE SET value = excluded.value
        """,
        sorted(entries.items()),
    )
    conn.commit()


def _build_manifest(
    *,
    counts,
    files,
    embedding_model_id,
    vector_dtype,
    vector_dimension,
    mobile_top_k,
    source,
):
    return {
        "pack_format": "senku-mobile-pack-v2",
        "pack_version": PACK_FORMAT_VERSION,
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "counts": counts,
        "embedding": {
            "model_id": embedding_model_id,
            "dimension": vector_dimension,
            "distance": "cosine",
            "vector_dtype": vector_dtype,
            "vectors_normalized": True,
            "query_normalization_required": True,
            "int8_divisor": 127 if vector_dtype == "int8" else None,
        },
        "runtime_defaults": {
            "desktop_top_k": getattr(config, "TOP_K", None),
            "mobile_top_k": mobile_top_k,
            "prompt_modes": list(getattr(config, "PROMPT_MODES", ())),
        },
        "schema": {
            "sqlite_tables": [
                "guides",
                "guide_related",
                "deterministic_rules",
                "chunks",
                "lexical_chunks_fts",
                "lexical_chunks_fts4",
                "pack_meta",
            ],
            "retrieval_metadata": {
                "columns": [
                    "content_role",
                    "time_horizon",
                    "structure_type",
                    "topic_tags",
                ],
            },
            "deterministic_rules": {
                "columns": [
                    "rule_id",
                    "predicate_name",
                    "builder_name",
                    "sample_prompt",
                ],
                "metadata_only": True,
            },
            "vector_file": {
                "format": VECTOR_FILE_FORMAT,
                "magic": VECTOR_FILE_MAGIC.decode("ascii"),
                "version": VECTOR_FILE_VERSION,
                "header_bytes": VECTOR_HEADER_STRUCT.size,
                "dtype_code": VECTOR_DTYPE_CODES[vector_dtype],
                "row_major": True,
                "little_endian": True,
            },
        },
        "source": source,
        "files": files,
    }


def _derive_guide_metadata(guide: GuideRecord) -> RetrievalMetadata:
    core_text = _normalized_match_text(
        guide.title,
        guide.slug.replace("-", " "),
        guide.description,
        guide.category,
        guide.tags,
    )
    body_text = _normalized_match_text(guide.body_markdown[:1200])
    return _derive_metadata_from_text(core_text, body_text)


def _derive_chunk_metadata(row: ChunkRecord, guide_metadata: RetrievalMetadata | None) -> RetrievalMetadata:
    core_text = _normalized_match_text(
        row.guide_title,
        row.section_heading,
        row.slug.replace("-", " "),
        row.category,
        row.tags,
    )
    body_text = _normalized_match_text(row.document[:1200])
    local = _derive_metadata_from_text(core_text, body_text)
    if guide_metadata is None:
        return local

    inherit_guide_domain = _should_inherit_guide_domain(core_text, body_text, guide_metadata)

    content_role = local.content_role
    time_horizon = (
        local.time_horizon
        if local.time_horizon != TIME_HORIZON_MIXED
        else guide_metadata.time_horizon
    )
    structure_type = (
        local.structure_type
        if local.structure_type != STRUCTURE_TYPE_GENERAL
        else guide_metadata.structure_type if inherit_guide_domain else STRUCTURE_TYPE_GENERAL
    )
    topic_tags = local.topic_tags
    if not topic_tags:
        topic_tags = guide_metadata.topic_tags if inherit_guide_domain else ""
    elif inherit_guide_domain and guide_metadata.topic_tags and (
        structure_type == guide_metadata.structure_type
        or local.structure_type == STRUCTURE_TYPE_GENERAL
        or guide_metadata.structure_type == STRUCTURE_TYPE_GENERAL
    ):
        topic_tags = _join_csv_values(local.topic_tags, guide_metadata.topic_tags)
    return RetrievalMetadata(
        content_role=content_role,
        time_horizon=time_horizon,
        structure_type=structure_type,
        topic_tags=topic_tags,
    )


def _should_inherit_guide_domain(
    core_text: str,
    body_text: str,
    guide_metadata: RetrievalMetadata,
) -> bool:
    if guide_metadata.structure_type == STRUCTURE_TYPE_WATER_STORAGE:
        return _has_water_storage_inheritance_focus(core_text, body_text)
    if guide_metadata.structure_type == STRUCTURE_TYPE_WATER_DISTRIBUTION:
        return _has_water_distribution_focus(core_text, body_text)
    if guide_metadata.structure_type == STRUCTURE_TYPE_SOAPMAKING:
        return _has_soapmaking_focus(core_text, body_text)
    if guide_metadata.structure_type == STRUCTURE_TYPE_GLASSMAKING:
        return _has_glassmaking_focus(core_text, body_text)
    return True


def _derive_metadata_from_text(core_text: str, body_text: str = "") -> RetrievalMetadata:
    combined_text = _normalized_match_text(core_text, body_text)
    structure_type = _detect_structure_type(core_text, body_text)
    content_role = _detect_content_role(core_text, body_text)
    time_horizon = _detect_time_horizon(combined_text, structure_type)
    topic_tags = ",".join(_detect_topic_tags(core_text, body_text, structure_type))
    return RetrievalMetadata(
        content_role=content_role,
        time_horizon=time_horizon,
        structure_type=structure_type,
        topic_tags=topic_tags,
    )


def _detect_structure_type(core_text: str, body_text: str = "") -> str:
    normalized_core = _normalized_match_text(core_text)
    if _looks_like_water_context(core_text) and any(
        _has_marker(normalized_core, marker) for marker in WATER_DISTRIBUTION_CORE_MARKERS
    ):
        return STRUCTURE_TYPE_WATER_DISTRIBUTION
    combined = _normalized_match_text(core_text, body_text)
    if _has_safety_poisoning_focus(core_text, body_text):
        return STRUCTURE_TYPE_SAFETY_POISONING
    if _looks_like_chemistry_context(combined):
        soapmaking_strong = sum(1 for marker in SOAPMAKING_BODY_STRONG_MARKERS if _has_marker(combined, marker))
        soapmaking_support = sum(1 for marker in SOAPMAKING_BODY_SUPPORT_MARKERS if _has_marker(combined, marker))
        if soapmaking_strong >= 2 or (soapmaking_strong >= 1 and soapmaking_support >= 2):
            return STRUCTURE_TYPE_SOAPMAKING
        if any(_has_marker(combined, marker) for marker in GLASSMAKING_BODY_STRONG_MARKERS):
            return STRUCTURE_TYPE_GLASSMAKING
    if (
        _looks_like_society_context(combined)
        or _looks_like_community_security_context(combined)
        or _looks_like_community_governance_context(combined)
    ):
        message_auth_hits = sum(
            1 for marker in MESSAGE_AUTH_BODY_STRONG_MARKERS if _has_marker(combined, marker)
        )
        fair_trial_hits = sum(
            1 for marker in FAIR_TRIAL_BODY_STRONG_MARKERS if _has_marker(combined, marker)
        )
        community_security_strong = sum(
            1 for marker in COMMUNITY_SECURITY_BODY_STRONG_MARKERS if _has_marker(combined, marker)
        )
        community_security_support = sum(
            1 for marker in COMMUNITY_SECURITY_BODY_SUPPORT_MARKERS if _has_marker(combined, marker)
        )
        if message_auth_hits < 2 and (
            community_security_strong >= 2 or (
                community_security_strong >= 1 and community_security_support >= 1
            )
        ):
            return STRUCTURE_TYPE_COMMUNITY_SECURITY
        community_governance_strong = sum(
            1 for marker in COMMUNITY_GOVERNANCE_BODY_STRONG_MARKERS if _has_marker(combined, marker)
        )
        community_governance_support = sum(
            1 for marker in COMMUNITY_GOVERNANCE_BODY_SUPPORT_MARKERS if _has_marker(combined, marker)
        )
        if fair_trial_hits == 0 and message_auth_hits < 2 and (
            community_governance_strong >= 2 or (
                community_governance_strong >= 1 and community_governance_support >= 1
            )
        ):
            return STRUCTURE_TYPE_COMMUNITY_GOVERNANCE
        if message_auth_hits >= 2:
            return STRUCTURE_TYPE_MESSAGE_AUTH
        if fair_trial_hits >= 1:
            return STRUCTURE_TYPE_FAIR_TRIAL
    for structure_type, markers in STRUCTURE_TYPE_MARKERS:
        if any(marker in core_text for marker in markers):
            return structure_type
    if _looks_like_building_context(core_text) and any(marker in combined for marker in STRUCTURE_TYPE_MARKERS[1][1]):
        return STRUCTURE_TYPE_CABIN_HOUSE
    if _looks_like_water_context(core_text):
        has_distribution_core = any(marker in core_text for marker in STRUCTURE_TYPE_MARKERS[4][1])
        has_distribution_focus = _has_water_distribution_focus(core_text, body_text)
        has_storage_core = any(marker in core_text for marker in STRUCTURE_TYPE_MARKERS[3][1])
        has_storage_focus = _has_water_storage_focus(core_text, body_text)
        has_purification_core = any(marker in core_text for marker in STRUCTURE_TYPE_MARKERS[5][1])
        has_purification_focus = any(marker in combined for marker in STRUCTURE_TYPE_MARKERS[5][1])
        if has_purification_core and not has_storage_core:
            return STRUCTURE_TYPE_WATER_PURIFICATION
        if has_distribution_core or has_distribution_focus:
            return STRUCTURE_TYPE_WATER_DISTRIBUTION
        if has_storage_focus and any(marker in combined for marker in STRUCTURE_TYPE_MARKERS[3][1]):
            return STRUCTURE_TYPE_WATER_STORAGE
        if has_purification_focus:
            return STRUCTURE_TYPE_WATER_PURIFICATION
    if _looks_like_medical_context(core_text) and any(marker in combined for marker in STRUCTURE_TYPE_MARKERS[7][1]):
        return STRUCTURE_TYPE_WOUND_CARE
    return STRUCTURE_TYPE_GENERAL


def _detect_content_role(core_text: str, body_text: str = "") -> str:
    combined = _normalized_match_text(core_text, body_text[:400])
    if any(marker in combined for marker in SAFETY_ROLE_MARKERS):
        return CONTENT_ROLE_SAFETY
    if any(marker in core_text for marker in PLANNING_ROLE_MARKERS):
        return CONTENT_ROLE_PLANNING
    if any(marker in core_text for marker in STARTER_ROLE_MARKERS):
        return CONTENT_ROLE_STARTER
    if any(marker in core_text for marker in REFERENCE_ROLE_MARKERS):
        return CONTENT_ROLE_REFERENCE
    return CONTENT_ROLE_SUBSYSTEM


def _detect_time_horizon(text: str, structure_type: str) -> str:
    if structure_type in {STRUCTURE_TYPE_EMERGENCY_SHELTER, STRUCTURE_TYPE_SAFETY_POISONING}:
        return TIME_HORIZON_IMMEDIATE
    if structure_type in (
        STRUCTURE_TYPE_CABIN_HOUSE,
        STRUCTURE_TYPE_WATER_STORAGE,
        STRUCTURE_TYPE_WATER_DISTRIBUTION,
        STRUCTURE_TYPE_SANITATION_SYSTEM,
        STRUCTURE_TYPE_COMMUNITY_SECURITY,
        STRUCTURE_TYPE_COMMUNITY_GOVERNANCE,
    ):
        return TIME_HORIZON_LONG_TERM
    if any(marker in text for marker in IMMEDIATE_TIME_MARKERS):
        return TIME_HORIZON_IMMEDIATE
    if any(marker in text for marker in LONG_TERM_TIME_MARKERS):
        return TIME_HORIZON_LONG_TERM
    return TIME_HORIZON_MIXED


def _detect_topic_tags(core_text: str, body_text: str, structure_type: str) -> list[str]:
    combined = _normalized_match_text(core_text, body_text)
    extended_context_text = _normalized_match_text(core_text, body_text[:600])
    building_context = (
        structure_type in {
            STRUCTURE_TYPE_CABIN_HOUSE,
            STRUCTURE_TYPE_EMERGENCY_SHELTER,
            STRUCTURE_TYPE_EARTH_SHELTER,
        }
        or _looks_like_building_context(core_text)
    )
    water_context = (
        structure_type in {
            STRUCTURE_TYPE_WATER_STORAGE,
            STRUCTURE_TYPE_WATER_DISTRIBUTION,
            STRUCTURE_TYPE_WATER_PURIFICATION,
        }
        or _looks_like_water_context(core_text)
    )
    watercraft_context = structure_type == STRUCTURE_TYPE_SMALL_WATERCRAFT
    poisoning_context = (
        structure_type == STRUCTURE_TYPE_SAFETY_POISONING
        or _looks_like_safety_poisoning_context(extended_context_text)
    )
    medical_context = (
        structure_type in {STRUCTURE_TYPE_WOUND_CARE, STRUCTURE_TYPE_SAFETY_POISONING}
        or poisoning_context
        or _looks_like_medical_context(core_text)
    )
    sanitation_context = (
        structure_type == STRUCTURE_TYPE_SANITATION_SYSTEM
        or _looks_like_sanitation_context(extended_context_text)
    )
    society_context = (
        structure_type in {STRUCTURE_TYPE_MESSAGE_AUTH, STRUCTURE_TYPE_FAIR_TRIAL}
        or _looks_like_society_context(extended_context_text)
    )
    community_security_context = (
        structure_type == STRUCTURE_TYPE_COMMUNITY_SECURITY
        or _looks_like_community_security_context(extended_context_text)
    )
    community_governance_context = (
        structure_type == STRUCTURE_TYPE_COMMUNITY_GOVERNANCE
        or _looks_like_community_governance_context(extended_context_text)
    )
    chemistry_context = (
        structure_type in {STRUCTURE_TYPE_SOAPMAKING, STRUCTURE_TYPE_GLASSMAKING}
        or _looks_like_chemistry_context(extended_context_text)
    )
    tags = []
    for topic_tag, markers in TOPIC_TAG_MARKERS.items():
        if topic_tag in BUILDING_TOPIC_TAGS and not building_context:
            continue
        if topic_tag in WATER_TOPIC_TAGS and not water_context:
            continue
        if topic_tag in WATERCRAFT_TOPIC_TAGS and not watercraft_context:
            continue
        if topic_tag in MEDICAL_TOPIC_TAGS and not medical_context:
            continue
        if topic_tag in SANITATION_TOPIC_TAGS and not sanitation_context:
            continue
        if topic_tag in SOCIETY_TOPIC_TAGS and not society_context:
            continue
        if topic_tag in COMMUNITY_SECURITY_TOPIC_TAGS and not community_security_context:
            continue
        if topic_tag in COMMUNITY_GOVERNANCE_TOPIC_TAGS and not community_governance_context:
            continue
        if topic_tag in CHEMISTRY_TOPIC_TAGS and not chemistry_context:
            if not (topic_tag == "lye_safety" and poisoning_context):
                continue
        if topic_tag in POISONING_TOPIC_TAGS and not poisoning_context:
            continue
        if topic_tag == "water_storage" and not _has_water_storage_focus(core_text, body_text):
            continue
        if topic_tag == "water_distribution" and not _has_water_distribution_focus(core_text, body_text):
            continue
        if any(_has_marker(combined, marker) for marker in markers):
            tags.append(topic_tag)
    return tags


def _looks_like_building_context(text: str) -> bool:
    return any(marker in text for marker in BUILDING_CONTEXT_MARKERS)


def _looks_like_water_context(text: str) -> bool:
    return any(marker in text for marker in WATER_CONTEXT_MARKERS)


def _looks_like_medical_context(text: str) -> bool:
    return any(marker in text for marker in MEDICAL_CONTEXT_MARKERS)


def _looks_like_sanitation_context(text: str) -> bool:
    return any(marker in text for marker in SANITATION_CONTEXT_MARKERS)


def _looks_like_society_context(text: str) -> bool:
    return any(marker in text for marker in SOCIETY_CONTEXT_MARKERS)


def _looks_like_community_security_context(text: str) -> bool:
    return any(marker in text for marker in COMMUNITY_SECURITY_CONTEXT_MARKERS)


def _looks_like_community_governance_context(text: str) -> bool:
    return any(marker in text for marker in COMMUNITY_GOVERNANCE_CONTEXT_MARKERS)


def _looks_like_chemistry_context(text: str) -> bool:
    return any(marker in text for marker in CHEMISTRY_CONTEXT_MARKERS)


def _looks_like_safety_poisoning_context(text: str) -> bool:
    return _has_safety_poisoning_focus(text)


def _has_marker(text: str, marker: str) -> bool:
    if not marker:
        return False
    if " " in marker or "-" in marker or "/" in marker or "&" in marker:
        return marker in text
    pattern = rf"(?<![a-z0-9]){re.escape(marker)}(?![a-z0-9])"
    return re.search(pattern, text) is not None


def _has_safety_poisoning_focus(core_text: str, body_text: str = "") -> bool:
    combined = _normalized_match_text(core_text, body_text)
    strong_matches = sum(
        1 for marker in SAFETY_POISONING_CORE_MARKERS if _has_marker(combined, marker)
    )
    if strong_matches >= 2:
        return True
    if strong_matches == 0:
        return False
    support_matches = sum(
        1 for marker in SAFETY_POISONING_SUPPORT_MARKERS if _has_marker(combined, marker)
    )
    return support_matches >= 1 or _has_marker(combined, "medical")


def _has_water_storage_focus(core_text: str, body_text: str) -> bool:
    core = _normalized_match_text(core_text)
    body = _normalized_match_text(body_text)
    if any(_has_marker(core, marker) for marker in STRUCTURE_TYPE_MARKERS[3][1]):
        return True
    return any(_has_marker(body, marker) for marker in WATER_STORAGE_BODY_SUPPORT_MARKERS)


def _has_water_storage_inheritance_focus(core_text: str, body_text: str) -> bool:
    core = _normalized_match_text(core_text)
    body = _normalized_match_text(body_text)
    explicit_markers = STRUCTURE_TYPE_MARKERS[3][1]
    if any(_has_marker(core, marker) for marker in explicit_markers):
        return True
    water_context_markers = (
        "water",
        "treated water",
        "drinking water",
        "potable",
        "hydration",
        "purification",
    )
    support_matches = sum(1 for marker in WATER_STORAGE_BODY_SUPPORT_MARKERS if _has_marker(body, marker))
    has_water_context = any(_has_marker(body, marker) for marker in water_context_markers)
    return has_water_context and support_matches >= 2


def _has_water_distribution_focus(core_text: str, body_text: str) -> bool:
    core = _normalized_match_text(core_text)
    body = _normalized_match_text(body_text)
    if any(_has_marker(core, marker) for marker in WATER_DISTRIBUTION_CORE_MARKERS):
        return True
    strong_matches = sum(1 for marker in WATER_DISTRIBUTION_BODY_STRONG_MARKERS if _has_marker(body, marker))
    if strong_matches >= 2:
        return True
    support_matches = sum(1 for marker in WATER_DISTRIBUTION_BODY_SUPPORT_MARKERS if _has_marker(body, marker))
    return strong_matches >= 1 and support_matches >= 2


def _has_soapmaking_focus(core_text: str, body_text: str) -> bool:
    combined = _normalized_match_text(core_text, body_text)
    strong_matches = sum(1 for marker in SOAPMAKING_BODY_STRONG_MARKERS if _has_marker(combined, marker))
    if strong_matches >= 1:
        return True
    support_matches = sum(1 for marker in SOAPMAKING_BODY_SUPPORT_MARKERS if _has_marker(combined, marker))
    return support_matches >= 2


def _has_glassmaking_focus(core_text: str, body_text: str) -> bool:
    combined = _normalized_match_text(core_text, body_text)
    return any(_has_marker(combined, marker) for marker in GLASSMAKING_BODY_STRONG_MARKERS)


def _normalized_match_text(*parts) -> str:
    joined = " ".join(_safe_text(part).lower() for part in parts if part)
    return " ".join(joined.split())


def _join_csv_values(*values: str) -> str:
    items = []
    seen = set()
    for value in values:
        for item in _split_csv_field(value):
            if item not in seen:
                seen.add(item)
                items.append(item)
    return ",".join(items)


def _iter_chunk_batches(collection, sorted_ids, *, batch_size):
    for start in range(0, len(sorted_ids), batch_size):
        batch_ids = sorted_ids[start : start + batch_size]
        raw = collection.get(
            ids=batch_ids,
            include=["documents", "metadatas", "embeddings"],
        )
        ids = raw.get("ids", [])
        id_to_index = {chunk_id: index for index, chunk_id in enumerate(ids)}
        rows = []
        for chunk_id in batch_ids:
            index = id_to_index.get(chunk_id)
            if index is None:
                raise ValueError(f"Collection get() did not return requested chunk id {chunk_id!r}")
            rows.append(
                ChunkRecord(
                    chunk_id=chunk_id,
                    source_file=_safe_text(raw["metadatas"][index].get("source_file")),
                    guide_id=_safe_text(raw["metadatas"][index].get("guide_id")),
                    guide_title=_safe_text(raw["metadatas"][index].get("guide_title")),
                    slug=_safe_text(raw["metadatas"][index].get("slug")),
                    description=_safe_text(raw["metadatas"][index].get("description")),
                    category=_safe_text(raw["metadatas"][index].get("category")),
                    difficulty=_safe_text(raw["metadatas"][index].get("difficulty")),
                    last_updated=_safe_text(raw["metadatas"][index].get("last_updated")),
                    version=_safe_text(raw["metadatas"][index].get("version")),
                    liability_level=_safe_text(raw["metadatas"][index].get("liability_level")),
                    tags=_safe_text(raw["metadatas"][index].get("tags")),
                    related=_safe_text(raw["metadatas"][index].get("related")),
                    section_id=_safe_text(raw["metadatas"][index].get("section_id")),
                    section_heading=_safe_text(raw["metadatas"][index].get("section_heading")),
                    document=_safe_text(raw["documents"][index]),
                    embedding=tuple(float(value) for value in raw["embeddings"][index]),
                )
            )
        yield rows


def _sorted_chunk_ids(collection, *, batch_size=DEFAULT_BATCH_SIZE):
    page_size = max(1, int(batch_size or DEFAULT_BATCH_SIZE))
    total = collection.count()
    chunk_ids: list[str] = []
    for offset in range(0, total, page_size):
        raw = collection.get(limit=page_size, offset=offset, include=[])
        chunk_ids.extend(raw.get("ids", []))
    return sorted(chunk_ids, key=_chunk_sort_key)


def _vector_filename_for_dtype(vector_dtype):
    return f"senku_vectors.{VECTOR_DTYPE_SUFFIXES[vector_dtype]}"


def _chunk_sort_key(chunk_id):
    numeric = _chunk_numeric_suffix(chunk_id)
    if numeric is None:
        return (1, chunk_id)
    return (0, numeric)


def _chunk_numeric_suffix(chunk_id):
    if not chunk_id.startswith("chunk_"):
        return None
    suffix = chunk_id.split("_", 1)[1]
    return int(suffix) if suffix.isdigit() else None


def _safe_text(value):
    return str(value or "")


def _join_meta_list(value):
    if isinstance(value, list):
        return ",".join(str(item) for item in value if str(item).strip())
    return _safe_text(value)


def _split_csv_field(value):
    return [item.strip() for item in _safe_text(value).split(",") if item.strip()]


def _normalize_vector(vector):
    norm = math.sqrt(sum(float(value) * float(value) for value in vector))
    if norm <= 0:
        return [0.0 for _ in vector]
    inv = 1.0 / norm
    return [float(value) * inv for value in vector]


def _quantize_int8(value):
    clamped = max(-1.0, min(1.0, float(value)))
    return max(-127, min(127, int(round(clamped * 127.0))))


def _file_info(path: Path):
    digest = hashlib.sha256()
    size = 0
    with path.open("rb") as handle:
        while True:
            chunk = handle.read(1024 * 1024)
            if not chunk:
                break
            size += len(chunk)
            digest.update(chunk)
    return {
        "path": path.name,
        "bytes": size,
        "sha256": digest.hexdigest(),
    }
