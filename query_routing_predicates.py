"""Marker-only query routing predicates shared by query.py."""

from query_routing_text import text_has_marker as _text_has_marker


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
    "mixed bleach and vinegar",
    "bleach and vinegar",
    "bleach and acid",
    "chlorine gas",
    "chloramine gas",
}

_CORROSIVE_HOUSEHOLD_CHEMICAL_SOURCE_MARKERS = {
    "bleach",
    "ammonia",
    "vinegar",
    "acid",
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
    "mixed bleach",
    "mixed bleach and vinegar",
    "bleach and vinegar",
    "bleach and acid",
    "coughing",
    "chest tightness",
    "chest is tight",
    "chest feels tight",
    "trouble breathing",
    "shortness of breath",
    "wheezing",
}

_CHEMICAL_EYE_ROUTE_MARKERS = {
    "in eye",
    "in my eye",
    "in one eye",
    "in the eye",
    "in eyes",
    "in my eyes",
    "eye exposure",
    "eye burn",
    "burning eye",
    "got in my eye",
    "got in the eye",
    "splash in one eye",
    "splashed in my eye",
    "splashed in one eye",
    "splashed in the eye",
    "sprayed in my eye",
    "bleach splash in one eye",
    "pain worse after rinsing",
    "pain is getting worse after rinsing",
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

_INDOOR_COMBUSTION_CO_SOURCE_MARKERS = {
    "stove",
    "wood stove",
    "woodstove",
    "heater",
    "heat source",
    "charcoal",
    "coal",
    "propane",
    "kerosene",
    "generator",
    "fireplace",
    "chimney",
    "flue",
    "combustion",
}

_INDOOR_COMBUSTION_CO_HAZARD_MARKERS = {
    "carbon monoxide",
    "co poisoning",
    "blocked ventilation",
    "blocked vent",
    "blocked flue",
    "poor ventilation",
    "smoke",
    "smoky",
    "smoking",
    "smoke back",
    "smoke-back",
    "smoke backing",
    "backing into",
    "headache",
    "dizzy",
    "dizziness",
    "nausea",
    "confusion",
    "cough",
    "fresh air",
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
    "mixed bleach",
    "mixed bleach and vinegar",
    "bleach and vinegar",
    "bleach and acid",
    "coughing",
    "chest tightness",
    "chest is tight",
    "chest feels tight",
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

_UNKNOWN_LOOSE_CHEMICAL_POWDER_QUERY_UNKNOWN_MARKERS = {
    "unknown",
    "cannot tell",
    "can't tell",
    "cannot identify",
    "can't identify",
    "not sure",
    "unlabeled",
    "not labeled",
    "not labelled",
}

_UNKNOWN_LOOSE_CHEMICAL_POWDER_QUERY_FORM_MARKERS = {
    "white powder",
    "powder",
    "powdery",
    "powder or liquid",
    "liquid",
    "residue",
    "granules",
    "crystals",
}

_UNKNOWN_LOOSE_CHEMICAL_POWDER_QUERY_HAZARD_MARKERS = {
    "toxic",
    "poison",
    "poisonous",
    "chemical",
    "detergent",
    "fertilizer",
    "cleaner",
    "pesticide",
    "unknown substance",
}

_MAINTENANCE_RECORD_QUERY_MARKERS = {
    "maintenance",
    "maintain",
    "repair",
    "repairs",
    "failure",
    "failures",
    "breakdown",
    "breakdowns",
    "same mistake",
    "mistake stops repeating",
    "repeat failure",
    "repeating",
}

_MAINTENANCE_RECORD_RECORD_MARKERS = {
    "record",
    "records",
    "log",
    "logs",
    "history",
    "track",
    "tracking",
    "write down",
}

_ANXIETY_CRISIS_EXPLAIN_QUERY_MARKERS = {
    "anxiety",
    "stress",
    "panic",
}

_ANXIETY_CRISIS_EXPLAIN_CRISIS_MARKERS = {
    "becoming a crisis",
    "become a crisis",
    "a crisis instead",
    "crisis instead of",
    "when is anxiety",
    "when is stress",
    "when should i get urgent help",
    "when should i worry",
    "something more dangerous",
    "more dangerous",
}

_ROOF_ACTIVE_RAIN_REPAIR_QUERY_MARKERS = {
    "roof",
    "leak",
    "leaking",
}

_ROOF_ACTIVE_RAIN_REPAIR_RISK_MARKERS = {
    "in the rain",
    "active rain",
    "raining",
    "wet roof",
    "during storm",
    "storm",
    "patch a roof",
}

_MARKET_TAX_REVENUE_QUERY_MARKERS = {
    "tax",
    "taxes",
    "taxation",
    "revenue",
    "public revenue",
    "market fees or taxes",
    "fees or taxes",
    "fee was over-collected",
}

_MARKET_SPACE_LAYOUT_QUERY_MARKERS = {
    "market day",
    "trade space",
    "market space",
    "stalls",
    "stall",
    "walking lanes",
    "foot traffic",
    "cart",
    "carts",
    "loading edge",
    "blocked corner",
    "blocked corners",
    "market flow",
    "storage to market",
    "connect storage",
    "notice board",
    "notices",
}

_BUILDING_HABITABILITY_QUERY_MARKERS = {
    "damaged building",
    "old building",
    "building",
    "house",
    "structure",
}

_BUILDING_HABITABILITY_RISK_MARKERS = {
    "safe enough",
    "safe to sleep",
    "sleep in",
    "storage only",
    "too risky",
    "usable after repair",
    "soft floor",
    "floor feels soft",
    "roof leaked",
    "leaks and mold",
    "leaked",
    "mold",
    "not safe to enter",
    "safe to enter",
    "short salvage",
}

_FOOD_STORAGE_CONTAINER_QUERY_MARKERS = {
    "salted fish",
    "salt fish",
    "salt, jars",
    "salt and jars",
    "salt jars",
    "preservation setup",
    "hot humid room",
    "hot and humid",
    "humid room",
    "dried beans",
    "dry beans",
    "dried herbs",
    "dry herbs",
    "fermented vegetables",
    "ferment vegetables",
    "clay pot",
    "plastic bucket",
    "food storage",
    "store food",
}

_DRY_MEAT_FISH_CONTAMINATION_QUERY_MARKERS = {
    "dry meat",
    "dry fish",
    "dry meat or fish",
    "drying meat",
    "drying fish",
    "meat or fish",
}

_DRY_MEAT_FISH_CONTAMINATION_HAZARD_MARKERS = {
    "humidity",
    "humid",
    "animals",
    "dirt",
    "flies",
    "insects",
    "dust",
    "contamination",
    "do not ruin it",
}

_ADHESIVE_BINDER_QUERY_MARKERS = {
    "simple adhesives",
    "simple adhesive",
    "simple binders",
    "simple binder",
    "adhesives or binders",
    "adhesive or binder",
    "wood, leather, paper, or containers",
    "wood leather paper or containers",
}

_MESSAGE_AUTH_QUERY_MARKERS = {
    "couriers between camps",
    "courier note",
    "prove a note is real",
    "note is real",
    "message is real",
    "verify it before acting",
    "verify a posted order",
    "verify an evacuation order",
    "evacuation order on the board",
    "posted order",
    "chain-of-custody",
    "chain of custody",
    "urgent notes",
    "patrol changes",
    "water contamination",
}


def _is_unknown_chemical_skin_burn_query(question):
    """Detect unknown/unlabeled chemical skin burns that should not route to rash care."""
    lower = question.lower()
    return (
        _text_has_marker(lower, {"unknown", "unlabeled", "under the sink", "under-sink"})
        and _text_has_marker(lower, {"bottle", "cleaner", "chemical", "product", "liquid"})
        and _text_has_marker(lower, {"hand", "hands", "skin", "touched", "touching", "contact"})
        and _text_has_marker(lower, {"burning", "burned", "burns", "stinging", "pain"})
    )


def _is_corrosive_household_chemical_exposure_query(question):
    """Detect actual corrosive/household-chemical exposures that need emergency-first structure."""
    lower = question.lower()
    return _text_has_marker(
        lower, _CORROSIVE_HOUSEHOLD_CHEMICAL_SOURCE_MARKERS
    ) and _text_has_marker(lower, _CHEMICAL_EXPOSURE_ROUTE_MARKERS)


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


def _is_chemical_spill_sick_exposure_query(question):
    """Detect chemical spill plus symptoms prompts as exposure triage, not process design."""
    lower = question.lower()
    return (
        _text_has_marker(lower, {"chemical spill", "spill", "spilled chemical", "chemical leaked", "chemical leak"})
        and _text_has_marker(lower, {"workshop", "shop", "garage", "facility", "lab", "room", "area"})
        and _text_has_marker(lower, {"feels sick", "feel sick", "sick", "nausea", "nauseous", "dizzy", "headache", "coughing", "trouble breathing"})
    )


def _is_indoor_combustion_co_smoke_query(question):
    """Detect indoor combustion smoke/CO prompts that should prefer smoke/CO owners."""
    lower = question.lower()
    if _is_household_chemical_inhalation_query(lower):
        return False
    explicit_co = "carbon monoxide" in lower or "co poisoning" in lower
    blocked_ventilation = _text_has_marker(
        lower, {"blocked ventilation", "blocked vent", "blocked flue", "poor ventilation"}
    )
    has_source = _text_has_marker(lower, _INDOOR_COMBUSTION_CO_SOURCE_MARKERS)
    has_hazard = _text_has_marker(lower, _INDOOR_COMBUSTION_CO_HAZARD_MARKERS)
    enclosed = _text_has_marker(lower, {"indoors", "inside", "room", "house", "cabin", "tent", "enclosed"})
    return (explicit_co and (blocked_ventilation or has_source or enclosed)) or (
        has_source and has_hazard and enclosed
    )


def _is_unknown_leaking_chemical_container_query(question):
    """Detect unknown leaking chemical containers that should not be moved by users."""
    lower = question.lower()
    return (
        _text_has_marker(lower, {"unknown", "cannot identify", "can't identify", "unlabeled"})
        and _text_has_marker(lower, {"chemical", "product", "container", "bottle"})
        and _text_has_marker(lower, {"leaked", "leaking", "leak", "sharp chemical smell", "chemical smell"})
    )


def _is_unknown_loose_chemical_powder_query(question):
    """Detect unknown loose powders/residues that should be isolated, not handled or moved."""
    lower = question.lower()
    return (
        _text_has_marker(lower, _UNKNOWN_LOOSE_CHEMICAL_POWDER_QUERY_UNKNOWN_MARKERS)
        and _text_has_marker(lower, _UNKNOWN_LOOSE_CHEMICAL_POWDER_QUERY_FORM_MARKERS)
        and _text_has_marker(lower, _UNKNOWN_LOOSE_CHEMICAL_POWDER_QUERY_HAZARD_MARKERS)
    )


def _is_industrial_chemical_smell_boundary_query(question):
    """Detect industrial/process odor prompts that should not invite sniff-testing."""
    lower = question.lower()
    return (
        _text_has_marker(
            lower,
            {"smells wrong", "chemical smell", "strong chemical odor", "wrong odor", "off odor"},
        )
        and _text_has_marker(
            lower,
            {"chemical", "industrial", "process", "workshop", "shop", "facility", "drum"},
        )
        and not (
            _text_has_marker(
                lower,
                {"raw-material path", "raw material path", "feedstock question"},
            )
            and not _text_has_marker(lower, {"spill", "leak", "sick", "exposure", "smells wrong"})
        )
    )


def _is_precursor_feedstock_exposure_boundary_query(question):
    """Detect prompts asking how to split feedstock work from exposure triage."""
    lower = question.lower()
    return (
        _text_has_marker(lower, {"precursor", "feedstock", "raw-material", "raw material"})
        and _text_has_marker(lower, {"poisoning", "exposure", "sick", "symptoms", "spill"})
        and _text_has_marker(lower, {"question", "route", "really", "first", "start", "hand off"})
    )


def _is_unlabeled_sealed_drum_safety_triage_query(question):
    """Detect sealed unlabeled drum prompts that need unknown-chemical safety triage."""
    lower = question.lower()
    return (
        _text_has_marker(lower, {"unlabeled drum", "unlabelled drum", "unknown drum"})
        and _text_has_marker(lower, {"workshop", "shop", "sealed", "nobody feels sick", "no symptoms"})
        and _text_has_marker(lower, {"disposal", "feedstock", "safety triage", "first"})
    )


def _is_maintenance_record_query(question):
    """Detect maintenance/repair/failure log prompts that belong in basic records."""
    lower = question.lower()
    return _text_has_marker(lower, _MAINTENANCE_RECORD_QUERY_MARKERS) and (
        _text_has_marker(lower, _MAINTENANCE_RECORD_RECORD_MARKERS)
        or "same mistake" in lower
        or "stops repeating" in lower
    )


def _is_anxiety_crisis_explainer_query(question):
    """Detect anxiety/stress prompts asking for the crisis boundary."""
    lower = question.lower()
    return _text_has_marker(lower, _ANXIETY_CRISIS_EXPLAIN_QUERY_MARKERS) and (
        "crisis" in lower
        or _text_has_marker(lower, _ANXIETY_CRISIS_EXPLAIN_CRISIS_MARKERS)
    )


def _is_active_rain_roof_repair_query(question):
    """Detect roof repair questions where wet-roof fall/storm safety must lead."""
    lower = question.lower()
    return _text_has_marker(lower, _ROOF_ACTIVE_RAIN_REPAIR_QUERY_MARKERS) and (
        _text_has_marker(lower, _ROOF_ACTIVE_RAIN_REPAIR_RISK_MARKERS)
    )


def _is_market_tax_revenue_query(question):
    """Detect market-fee/tax prompts that need public-revenue guidance."""
    lower = question.lower()
    return _text_has_marker(lower, _MARKET_TAX_REVENUE_QUERY_MARKERS) and (
        "market" in lower or "fee" in lower or "tax" in lower or "revenue" in lower
    )


def _is_market_space_layout_query(question):
    """Detect physical market-space layout prompts that should lead with GD-963."""
    lower = question.lower()
    if _is_market_tax_revenue_query(question) and not _text_has_marker(
        lower, {"stall", "stalls", "lane", "lanes", "foot traffic", "loading edge"}
    ):
        return False
    return _text_has_marker(lower, _MARKET_SPACE_LAYOUT_QUERY_MARKERS) and (
        "market" in lower or "trade" in lower or "stall" in lower or "stalls" in lower
    )


def _is_building_habitability_safety_query(question):
    """Detect damaged-building/storage/sleep/use prompts that need no-entry triage."""
    lower = question.lower()
    return _text_has_marker(lower, _BUILDING_HABITABILITY_QUERY_MARKERS) and (
        _text_has_marker(lower, _BUILDING_HABITABILITY_RISK_MARKERS)
    )


def _is_food_storage_container_query(question):
    """Detect food preservation/storage/container prompts that should not be salt-only."""
    lower = question.lower()
    return _text_has_marker(lower, _FOOD_STORAGE_CONTAINER_QUERY_MARKERS) and (
        "food" in lower
        or "fish" in lower
        or "beans" in lower
        or "herbs" in lower
        or "fermented" in lower
        or "salt" in lower
        or "store" in lower
        or "preservation" in lower
    )


def _is_salt_jars_hot_humid_setup_query(question):
    """Detect salt/jars/humidity prompts that need preservation plus packaging, not salt storage."""
    lower = question.lower()
    return _is_food_storage_container_query(question) and (
        _text_has_marker(lower, {"salt, jars", "salt jars", "salt and jars"})
        and _text_has_marker(lower, {"hot humid room", "hot and humid", "humid room"})
    )


def _is_dry_meat_fish_contamination_query(question):
    """Detect meat/fish drying prompts where pests, dirt, and humidity are explicit constraints."""
    lower = question.lower()
    return _text_has_marker(
        lower, _DRY_MEAT_FISH_CONTAMINATION_QUERY_MARKERS
    ) and _text_has_marker(lower, _DRY_MEAT_FISH_CONTAMINATION_HAZARD_MARKERS)


def _is_adhesive_binder_query(question):
    """Detect simple adhesive/binder family-selection prompts."""
    lower = question.lower()
    return _text_has_marker(lower, _ADHESIVE_BINDER_QUERY_MARKERS) and (
        "adhesive" in lower or "binder" in lower or "glue" in lower
    )


def _is_message_auth_query(question):
    """Detect courier/message authenticity and chain-of-custody prompts."""
    lower = question.lower()
    return _text_has_marker(lower, _MESSAGE_AUTH_QUERY_MARKERS) and (
        "note" in lower
        or "message" in lower
        or "order" in lower
        or "courier" in lower
        or "chain" in lower
    )


def _is_simple_courier_note_auth_query(question):
    """Detect simple, low-fragility courier note authentication prompts."""
    lower = question.lower()
    return _is_message_auth_query(lower) and _text_has_marker(
        lower, {"simplest", "simple", "not too fragile", "without making the system too fragile"}
    )


def _is_posted_order_verification_query(question):
    """Detect posted-order verification prompts where source confirmation must lead."""
    lower = question.lower()
    return _is_message_auth_query(lower) and _text_has_marker(
        lower, {"posted", "board", "evacuation order", "verify it before acting"}
    )


def _is_canned_fruit_soft_spot_query(question):
    """Detect opened/canned fruit soft-spot prompts that need discard-first consistency."""
    lower = question.lower()
    return "canned fruit" in lower and _text_has_marker(
        lower, {"soft spot", "soft spots", "still safe", "safe or not"}
    )


def _is_cooked_rice_power_outage_spoilage_query(question):
    """Detect cooked rice/power-loss prompts that need discard-first guidance."""
    lower = question.lower()
    return (
        "rice" in lower
        and _text_has_marker(lower, {"power went out", "power outage", "lost power", "no power"})
        and _text_has_marker(lower, {"tastes off", "taste off", "smells off", "spoilage", "stale", "safe"})
    )


def _is_stretcher_access_query(question):
    """Detect stretcher-width entrance/layout prompts distinct from wheelchair-only access."""
    lower = question.lower()
    return "stretcher" in lower and _text_has_marker(
        lower, {"entrance", "access door", "door", "too narrow", "clinic"}
    )
