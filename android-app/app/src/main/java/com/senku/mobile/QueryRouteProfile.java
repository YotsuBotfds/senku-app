package com.senku.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class QueryRouteProfile {
    private enum Kind {
        DEFAULT,
        HOUSE_BUILD,
        SMALL_WATERCRAFT,
        WATER_PURIFICATION,
        WATER_STORAGE,
        SANITATION_SYSTEM,
        MESSAGE_AUTH,
        FAIR_TRIAL,
        CLAY_OVEN,
        COMMUNITY_SECURITY,
        COMMUNITY_GOVERNANCE,
        SOAPMAKING,
        GLASSMAKING,
        GENERIC_PUNCTURE,
        FIRE_IN_RAIN,
        METAL_JOIN_NO_WELDER
    }

    private static final Set<String> STARTER_BUILD_MARKERS = buildSet(
        "how do i build", "how do we build", "how can i build", "how can we build",
        "how to build", "build a", "build an", "construct a", "construct an", "make a", "make an"
    );
    private static final Set<String> HOUSE_MARKERS = buildSet("house", "home", "cabin", "hut", "homestead");
    private static final Set<String> HOUSE_SUBSYSTEM_QUERY_MARKERS = buildSet(
        "roof", "roofing", "insulation", "weatherproof", "weatherproofing", "rainproof",
        "rainproofing", "wall", "wall framing", "foundation", "ventilation",
        "weatherstripping", "window", "door", "threshold"
    );
    private static final Set<String> HOUSE_ROOF_QUERY_MARKERS = buildSet(
        "roof", "roofing", "rafter", "rafters", "gable", "water shedding", "thatch", "bark", "shakes", "flashing"
    );
    private static final Set<String> HOUSE_INSULATION_QUERY_MARKERS = buildSet(
        "insulation", "thermal efficiency", "air sealing", "drafts", "heat loss", "condensation"
    );
    private static final Set<String> HOUSE_OPENING_QUERY_MARKERS = buildSet(
        "window", "windows", "door", "doors", "weatherstripping", "threshold", "sill"
    );
    private static final Set<String> HOUSE_WALL_QUERY_MARKERS = buildSet(
        "wall", "walls", "wall framing", "framing", "stud", "studs", "sheathing", "siding", "brace", "bracing"
    );
    private static final Set<String> HOUSE_SITE_QUERY_MARKERS = buildSet(
        "building site", "site selection", "choose a site", "choose a building site", "site and foundation"
    );
    private static final Set<String> HOUSE_SITE_FACTOR_MARKERS = buildSet(
        "drainage", "wind", "sun", "access", "hazard", "foundation"
    );
    private static final Set<String> HOUSE_SITE_SEASONAL_MARKERS = buildSet(
        "winter", "summer", "shade", "shading", "microclimate", "seasonal", "solar", "orientation"
    );
    private static final Set<String> HOUSE_EARTH_SHELTER_QUERY_MARKERS = buildSet(
        "earth shelter", "earth-shelter", "earth sheltered", "earth-sheltering",
        "underground shelter", "underground", "bunker", "berm"
    );
    private static final Set<String> WATERCRAFT_MARKERS = buildSet(
        "canoe", "boat", "watercraft", "dugout", "coracle", "kayak", "rowboat", "skiff"
    );
    private static final Set<String> WATER_MARKERS = buildSet("water", "drink", "drinking");
    private static final Set<String> WATER_PURITY_ACTION_MARKERS = buildSet(
        "purify", "purification", "filter", "filtration", "safe to drink", "clean water", "charcoal sand"
    );
    private static final Set<String> WATER_STORAGE_ACTION_MARKERS = buildSet(
        "store", "storage", "container", "containers", "drum", "barrel", "bucket", "bottle",
        "jug", "reused", "reuse", "hold water", "ration"
    );
    private static final Set<String> WATER_STORAGE_CONTAINER_QUERY_MARKERS = buildSet(
        "container", "containers", "bottle", "bottles", "jug", "jugs", "bucket", "buckets",
        "drum", "drums", "barrel", "barrels", "food-grade", "food safe", "food-safe",
        "glass", "plastic", "reused", "reuse", "soda bottle", "soda bottles", "milk jug"
    );
    private static final Set<String> WATER_STORAGE_SANITATION_QUERY_MARKERS = buildSet(
        "clean", "cleaning", "sanitize", "sanitise", "sanitation", "disinfect", "contamination",
        "contaminated", "bleach", "rinse", "sealed", "seal", "rotate", "rotation", "freshly"
    );
    private static final Set<String> WATER_STORAGE_PLANNING_QUERY_MARKERS = buildSet(
        "long term", "long-term", "gallon", "gallons", "liter", "liters", "litre", "litres",
        "per person", "day", "days", "week", "weeks", "month", "months", "ration", "rationing",
        "reserve", "emergency supply", "duration", "hydration assurance"
    );
    private static final Set<String> WATER_STORAGE_SYSTEM_QUERY_MARKERS = buildSet(
        "rainwater", "harvest", "harvesting", "cistern", "distribution", "gravity-fed",
        "gravity fed", "tank", "tanks", "plumbing", "piping", "spigot", "community",
        "household system", "water system"
    );
    private static final Set<String> SANITATION_QUERY_MARKERS = buildSet(
        "latrine", "latrines", "toilet", "toilets", "wash station", "handwashing station",
        "hand washing station", "hygiene station", "waste management", "greywater"
    );
    private static final Set<String> MESSAGE_AUTH_QUERY_MARKERS = buildSet(
        "courier", "couriers", "message", "messages", "note", "notes", "order", "orders",
        "verify", "verification", "authentic", "real", "forged", "forgery", "seal",
        "chain of custody", "challenge-response", "challenge response", "tamper"
    );
    private static final Set<String> FAIR_TRIAL_QUERY_MARKERS = buildSet(
        "fair trial", "trial with no lawyers", "no lawyers or judges", "community trial", "lay tribunal"
    );
    private static final Set<String> OVEN_BUILD_QUERY_MARKERS = buildSet(
        "clay oven", "bread oven", "cob oven", "brick oven", "earth oven", "masonry oven"
    );
    private static final Set<String> COMMUNITY_SECURITY_ACTION_MARKERS = buildSet(
        "protect", "guard", "secure", "security", "defend", "watch", "patrol",
        "checkpoint", "perimeter", "fortify", "fortification", "early warning"
    );
    private static final Set<String> COMMUNITY_SECURITY_TARGET_MARKERS = buildSet(
        "work site", "worksite", "field", "water point", "food storage", "storehouse",
        "granary", "critical infrastructure", "vulnerable site"
    );
    private static final Set<String> COMMUNITY_GOVERNANCE_MARKERS = buildSet(
        "steal", "stealing", "theft", "stolen", "trust", "trusts", "dont trust", "don't trust",
        "nobody trusts", "formal punishment", "punishment", "restitution",
        "merge", "merge groups", "mutual aid", "reputation", "vouch", "mediation",
        "restorative", "sanction", "sanctions", "resource dispute", "commons",
        "resource governance", "resource rules"
    );
    private static final Set<String> SOAP_QUERY_MARKERS = buildSet(
        "make soap", "making soap", "soap making", "soap from animal fat", "ash and animal fat",
        "ash & animal fat", "saponification", "wood ash lye", "lye water", "tallow soap", "lard soap"
    );
    private static final Set<String> GLASS_QUERY_MARKERS = buildSet(
        "make glass", "making glass", "glass from scratch", "glassmaking"
    );
    private static final Set<String> PUNCTURE_QUERY_MARKERS = buildSet(
        "puncture wound", "puncture", "deep puncture", "stepped on a nail", "nail wound"
    );
    private static final Set<String> ANIMAL_BITE_MARKERS = buildSet(
        "animal bite", "dog bite", "cat bite", "rabies", "bitten by"
    );
    private static final Set<String> FIRE_MARKERS = buildSet("fire", "flame", "ignite", "burn");
    private static final Set<String> WET_WEATHER_MARKERS = buildSet("rain", "wet", "soaked", "damp", "storm");
    private static final Set<String> METAL_MARKERS = buildSet("metal", "steel", "iron", "copper", "brass");
    private static final Set<String> JOIN_MARKERS = buildSet("join", "joining", "weld", "welding", "braze", "solder");
    private static final Set<String> WELDER_ABSENCE_MARKERS = buildSet(
        "without a welder", "without welder", "no welder", "without electricity", "without electric welder"
    );
    private static final Set<String> HOUSE_POSITIVE_MARKERS = buildSet(
        "waterproofing and sealants",
        "window and door assembly", "construction & carpentry", "passive solar design",
        "cabin", "site selection",
        "drainage", "foundation", "floor system", "wall framing", "roof framing",
        "roofing", "weatherproofing", "rainproofing", "insulation", "ventilation", "earth-sheltering",
        "thermal efficiency", "air sealing", "moisture management", "water shedding",
        "terrain analysis", "water proximity", "stable ground", "soil type and drainage",
        "wind exposure", "sun exposure", "access routes",
        "window frame", "weatherstripping", "threshold", "site drainage", "roof waterproofing",
        "doors & window construction", "wall construction", "foundations", "rainproofing and water shedding",
        "shelter site selection & hazard assessment"
    );
    private static final Set<String> HOUSE_DISTRACTOR_MARKERS = buildSet(
        "population census", "household surveys", "record organization", "record storage",
        "demographics", "vital records", "urban sociology", "housing policy",
        "household", "cabinet", "greenhouse", "greenhouses", "cold frame", "cold frames",
        "medicine cabinet", "kitchen cabinet",
        "accessible shelter", "universal design", "salvage", "demolition",
        "building materials salvage", "salvage & recovery", "roofing materials",
        "shared shelter", "shared shelters", "group dynamics",
        "seasonal shelter adaptation", "long-term camp evolution", "camp evolution",
        "camp layout", "communal shelter"
    );
    private static final Set<String> HOUSE_UNDERGROUND_MARKERS = buildSet(
        "earth shelter", "earth-shelter", "earth sheltered", "earth-sheltering",
        "underground shelter", "bunker", "cut-and-cover", "overburden", "shoring",
        "fallout protection", "nbc filtration", "blast effects", "confined spaces"
    );
    private static final Set<String> WATERCRAFT_POSITIVE_MARKERS = buildSet(
        "small watercraft construction", "transportation & infrastructure", "shipbuilding & boats",
        "waterproofing and sealants", "boat building", "watercraft", "dugout canoe", "plank canoe", "hull",
        "ribs", "planking", "caulking", "buoyancy", "paddle", "oar", "sealing",
        "hollowed log", "boat caulking", "shoreline test", "boat building & watercraft"
    );
    private static final Set<String> WATERCRAFT_DISTRACTOR_MARKERS = buildSet(
        "water rescue", "ferry operations", "shipping", "harbor", "portage trails",
        "bridge approaches", "road building", "rescue doctrine", "commercial shipping"
    );
    private static final Set<String> WATER_PURIFICATION_POSITIVE_MARKERS = buildSet(
        "water purification", "survival water purification", "filtration systems", "chemical treatment",
        "water safety basics", "combining purification methods", "testing water quality",
        "solar disinfection", "distillation", "boiling", "water still contaminated troubleshooter",
        "sanitation & public health", "chlorine & bleach production", "desalination systems",
        "simple water treatment methods", "water finding & emergency purification",
        "water supply protection", "disinfection & sterilization", "water chemistry & treatment",
        "biosand filter"
    );
    private static final Set<String> WATER_PURIFICATION_DISTRACTOR_MARKERS = buildSet(
        "alkali & soda production", "aquatic biology", "fisheries", "irrigation", "waterway",
        "greywater recycling", "canal", "lock", "harbor", "boat building",
        "camp outbreak", "dysentery operations", "case tracking", "latrine inspection",
        "handwashing enforcement", "food-service restrictions", "charcoal & fuels",
        "activated charcoal", "water source checks"
    );
    private static final Set<String> WATER_STORAGE_POSITIVE_MARKERS = buildSet(
        "water storage", "water storage systems", "rationing protocol", "sanitation & public health",
        "water system design and distribution", "container construction", "gravity-fed distribution",
        "rainwater harvesting", "water harvesting systems", "water storage sanitation",
        "water supply protection", "distribution system components", "clean container",
        "contamination prevention"
    );
    private static final Set<String> WATER_STORAGE_DISTRACTOR_MARKERS = buildSet(
        "alkali & soda production", "aquatic biology", "fisheries", "irrigation", "waterway",
        "boat building", "harbor", "shipping", "desalination", "fuel storage",
        "camp outbreak", "dysentery operations", "case tracking", "latrine inspection"
    );
    private static final Set<String> SANITATION_POSITIVE_MARKERS = buildSet(
        "sanitation & public health", "sanitation and waste management systems",
        "latrine construction", "latrine and toilet system design", "wash station",
        "handwashing station", "greywater", "waste management", "disease prevention"
    );
    private static final Set<String> SANITATION_DISTRACTOR_MARKERS = buildSet(
        "animal husbandry", "veterinary", "livestock", "water storage", "hydration assurance",
        "garden", "crop", "evacuation", "wildfire"
    );
    private static final Set<String> MESSAGE_AUTH_POSITIVE_MARKERS = buildSet(
        "message authentication & courier protocols", "message authentication",
        "courier protocols", "chain of custody", "challenge-response verbal protocols",
        "emergency authentication", "tamper-evident", "courier selection and vetting"
    );
    private static final Set<String> MESSAGE_AUTH_DISTRACTOR_MARKERS = buildSet(
        "animal husbandry", "veterinary", "wildfire defense and evacuation",
        "property law", "market organization", "weather & geology"
    );
    private static final Set<String> FAIR_TRIAL_POSITIVE_MARKERS = buildSet(
        "trial procedure", "criminal justice procedures for small communities",
        "evidence standards", "judicial independence", "community courts",
        "restorative justice", "appeal", "record rules"
    );
    private static final Set<String> FAIR_TRIAL_DISTRACTOR_MARKERS = buildSet(
        "property law", "market organization", "federation", "democratic governance"
    );
    private static final Set<String> CLAY_OVEN_POSITIVE_MARKERS = buildSet(
        "clay bread oven construction", "clay oven", "cob oven", "bread oven", "brick bread oven",
        "earth oven", "masonry oven", "site selection", "foundation", "chimney", "draft",
        "curing schedule", "thermal mass", "refractory", "firebrick", "masonry hearth"
    );
    private static final Set<String> CLAY_OVEN_DISTRACTOR_MARKERS = buildSet(
        "currency", "ledger", "token", "tally", "astronomy", "aquaculture"
    );
    private static final Set<String> COMMUNITY_SECURITY_POSITIVE_MARKERS = buildSet(
        "community defense", "critical infrastructure physical security", "physical security",
        "guard rotation", "checkpoint", "perimeter", "access control", "early warning",
        "watch rotation", "food storage security", "water system security",
        "water point security", "field security"
    );
    private static final Set<String> COMMUNITY_SECURITY_DISTRACTOR_MARKERS = buildSet(
        "message authentication", "chain of custody", "trial procedure", "appeal",
        "insurance", "accounting", "accessible shelter", "abrasives manufacturing"
    );
    private static final Set<String> COMMUNITY_GOVERNANCE_POSITIVE_MARKERS = buildSet(
        "commons management", "sustainable resource governance", "resource governance",
        "resource rules", "graduated sanctions", "restorative justice", "mutual aid",
        "community mediation", "trust recovery", "reputation", "vouching",
        "dispute resolution", "membership rules"
    );
    private static final Set<String> COMMUNITY_GOVERNANCE_DISTRACTOR_MARKERS = buildSet(
        "critical infrastructure physical security", "checkpoint", "perimeter", "access control",
        "insurance", "actuarial", "pooling mathematics", "abrasives manufacturing"
    );
    private static final Set<String> SOAP_POSITIVE_MARKERS = buildSet(
        "soap making", "soap & candle making", "making soap", "saponification",
        "ash & animal fat", "ash soap", "wood ash lye", "rendered fat soap", "tallow soap",
        "lard soap", "hot process soap", "cold process soap", "homestead chemistry", "trace"
    );
    private static final Set<String> SOAP_DISTRACTOR_MARKERS = buildSet(
        "lighting production", "animal bite", "rabies",
        "chemical & fuel salvage safety", "chemical safety", "alkali & soda production",
        "p h acids bases water chemistry essentials"
    );
    private static final Set<String> GLASS_POSITIVE_MARKERS = buildSet(
        "glassmaking", "glass making", "raw materials", "furnace construction",
        "silica", "soda ash", "annealing", "forming techniques",
        "glass, optics & ceramics", "glass-making-raw-materials"
    );
    private static final Set<String> GLASS_DISTRACTOR_MARKERS = buildSet(
        "hourglass", "flat glass cutting", "flat glass reuse", "window glass",
        "plastics, rubber & glass identification", "industrial applications",
        "waterglass", "sodium silicate"
    );
    private static final Set<String> PUNCTURE_POSITIVE_MARKERS = buildSet(
        "first aid & emergency response", "first aid essentials", "wound management",
        "wound assessment and cleaning", "infection prevention", "wound hygiene",
        "puncture wound", "deep puncture wounds", "tetanus", "foreign objects",
        "irrigation", "emergency first aid priorities"
    );
    private static final Set<String> PUNCTURE_DISTRACTOR_MARKERS = buildSet(
        "animal bite", "rabies", "bite wound", "acute abdominal emergencies",
        "acute coronary syndrome", "radiation syndrome", "eye injuries",
        "ophthalmology", "defensive encounters", "perioperative", "surgical decision-making",
        "diabetic foot", "diabetes management", "resource-poor settings"
    );
    private static final Set<String> FIRE_IN_RAIN_POSITIVE_MARKERS = buildSet(
        "fire by friction methods", "fire in wet conditions", "fire in winter conditions",
        "fire starting", "tinder preparation", "fire bundle", "wood species selection",
        "kindling", "char cloth", "feather stick", "wet wood", "survival basics",
        "winter survival systems", "swamp & wetland survival systems"
    );
    private static final Set<String> FIRE_IN_RAIN_DISTRACTOR_MARKERS = buildSet(
        "fire suppression", "wildland fire", "fire safety and compartmentalization",
        "firearms", "heat illness", "smoke inhalation", "brick making", "anti-counterfeit"
    );
    private static final Set<String> METAL_JOIN_POSITIVE_MARKERS = buildSet(
        "welding metallurgy & joint integrity", "forge welding", "fire welding", "brazing and soldering",
        "oxy-fuel welding", "basic forge operation", "forging & metalwork", "metalworking & blacksmithing",
        "basic smithing techniques", "joint integrity", "forge construction", "basic forge"
    );
    private static final Set<String> METAL_JOIN_DISTRACTOR_MARKERS = buildSet(
        "anti-counterfeit", "minting", "coin", "quality control", "testing weld integrity",
        "inspection", "foundry", "casting", "alternator repurposing"
    );
    private static final Set<String> HOUSE_LEAD_MARKERS = buildSet(
        "construction & carpentry",
        "one room cabin",
        "site selection",
        "shelter site selection & hazard assessment",
        "terrain analysis",
        "water proximity",
        "wind exposure",
        "natural hazards",
        "resource proximity",
        "site assessment checklist"
    );
    private static final Set<String> WATERCRAFT_LEAD_MARKERS = buildSet(
        "small watercraft construction",
        "boat building & watercraft",
        "dugout canoe",
        "plank canoe",
        "hollowed log"
    );
    private static final Set<String> WATER_PURIFICATION_LEAD_MARKERS = buildSet(
        "water purification",
        "survival water purification selection guide",
        "filtration systems",
        "chemical treatment",
        "simple water treatment methods",
        "water finding & emergency purification"
    );
    private static final Set<String> WATER_STORAGE_LEAD_MARKERS = buildSet(
        "water storage systems",
        "water storage",
        "rainwater harvesting",
        "water system design and distribution",
        "water supply protection"
    );
    private static final Set<String> PUNCTURE_LEAD_MARKERS = buildSet(
        "first aid & emergency response",
        "wound management",
        "first aid essentials",
        "emergency first aid priorities",
        "wound hygiene, infection prevention & field sanitation",
        "chronic wound care"
    );
    private static final Set<String> FIRE_IN_RAIN_LEAD_MARKERS = buildSet(
        "fire by friction methods",
        "fire in wet conditions",
        "fire in winter conditions",
        "tinder preparation and fire bundle construction"
    );
    private static final Set<String> METAL_JOIN_LEAD_MARKERS = buildSet(
        "welding metallurgy & joint integrity",
        "forge welding",
        "brazing and soldering",
        "basic forge operation",
        "forging & metalwork"
    );
    private static final Set<String> SANITATION_LEAD_MARKERS = buildSet(
        "sanitation & public health",
        "sanitation and waste management systems",
        "latrine construction",
        "latrine and toilet system design"
    );
    private static final Set<String> MESSAGE_AUTH_LEAD_MARKERS = buildSet(
        "message authentication & courier protocols",
        "chain of custody and documentation",
        "challenge-response verbal protocols",
        "emergency authentication when systems are compromised"
    );
    private static final Set<String> FAIR_TRIAL_LEAD_MARKERS = buildSet(
        "criminal justice procedures for small communities",
        "trial procedure",
        "community courts"
    );
    private static final Set<String> CLAY_OVEN_LEAD_MARKERS = buildSet(
        "clay bread oven construction",
        "site selection & foundation",
        "cob oven construction",
        "brick bread oven"
    );
    private static final Set<String> COMMUNITY_SECURITY_LEAD_MARKERS = buildSet(
        "critical infrastructure physical security",
        "community defense planning",
        "access control without electronics",
        "perimeter detection systems",
        "food storage security",
        "water system security"
    );
    private static final Set<String> COMMUNITY_GOVERNANCE_LEAD_MARKERS = buildSet(
        "commons management & sustainable resource governance",
        "commons management",
        "resource governance",
        "community mediation",
        "restorative justice",
        "mutual aid"
    );
    private static final Set<String> SOAP_LEAD_MARKERS = buildSet(
        "homestead chemistry",
        "soap & candle making",
        "everyday compounds and production",
        "using fat for soap making",
        "lye soap from ash leaching",
        "soap making",
        "making soap"
    );
    private static final Set<String> GLASS_LEAD_MARKERS = buildSet(
        "glass, optics & ceramics",
        "glassmaking",
        "forming techniques"
    );
    private static final Set<String> HOUSE_INTENT_MARKERS = buildSet(
        "foundation", "wall construction", "roofing", "weatherproofing", "framing", "site selection",
        "floor system", "wall framing", "roof framing", "drainage", "ventilation",
        "window and door", "weatherstripping", "site drainage", "roof waterproofing"
    );
    private static final Set<String> WATERCRAFT_INTENT_MARKERS = buildSet(
        "dugout canoe", "plank canoe", "hull", "planking", "caulking", "boat building"
    );
    private static final Set<String> WATER_PURIFICATION_INTENT_MARKERS = buildSet(
        "purify", "purification", "filter", "filtration", "biosand", "boiling",
        "disinfection", "safe drinking water", "water safety", "distillation", "sodis"
    );
    private static final Set<String> WATER_STORAGE_INTENT_MARKERS = buildSet(
        "water storage", "container selection", "container sanitation", "food-grade",
        "food safe", "clean container", "fill", "seal", "rotate", "reused container"
    );
    private static final Set<String> PUNCTURE_INTENT_MARKERS = buildSet(
        "puncture", "wound", "irrigation", "tetanus", "do not probe",
        "infection prevention", "wound cleaning", "first aid"
    );
    private static final Set<String> FIRE_IN_RAIN_INTENT_MARKERS = buildSet(
        "tinder", "kindling", "dry inner wood", "fire bundle", "feather stick", "char cloth"
    );
    private static final Set<String> METAL_JOIN_INTENT_MARKERS = buildSet(
        "weld", "welding", "forge welding", "fire welding", "braze", "brazing",
        "solder", "soldering", "joint integrity", "flux", "lap joint", "basic forge"
    );
    private static final Set<String> SANITATION_INTENT_MARKERS = buildSet(
        "latrine", "toilet", "wash station", "handwashing", "waste", "greywater", "contaminate", "disease"
    );
    private static final Set<String> MESSAGE_AUTH_INTENT_MARKERS = buildSet(
        "courier", "note", "message", "verify", "authentic", "seal", "chain of custody", "tamper"
    );
    private static final Set<String> FAIR_TRIAL_INTENT_MARKERS = buildSet(
        "trial", "panel", "evidence", "hearing", "appeal", "records"
    );
    private static final Set<String> CLAY_OVEN_INTENT_MARKERS = buildSet(
        "clay oven", "cob oven", "bread oven", "brick oven", "site selection", "foundation",
        "chimney", "draft", "curing", "thermal mass", "refractory", "firebrick"
    );
    private static final Set<String> COMMUNITY_SECURITY_INTENT_MARKERS = buildSet(
        "security", "guard", "patrol", "checkpoint", "perimeter", "access control",
        "early warning", "watch rotation", "field", "water point", "food storage",
        "storehouse", "granary", "critical infrastructure", "work site"
    );
    private static final Set<String> COMMUNITY_GOVERNANCE_INTENT_MARKERS = buildSet(
        "theft", "stealing", "stolen", "trust", "trusts", "reputation", "vouch", "mediation",
        "restorative", "sanction", "sanctions", "resource governance", "resource rules",
        "commons", "mutual aid", "merge groups", "merge", "formal punishment", "punishment", "restitution"
    );
    private static final Set<String> SOAP_INTENT_MARKERS = buildSet(
        "soap", "saponification", "lye water", "wood ash lye", "fat", "tallow", "trace", "cure"
    );
    private static final Set<String> SOAP_FOCUS_MARKERS = buildSet(
        "soap", "soap making", "making soap", "soapmaking", "saponification",
        "animal fat", "render fat", "tallow", "trace", "bar soap", "cold process", "cure"
    );
    private static final Set<String> GLASS_INTENT_MARKERS = buildSet(
        "glass", "silica", "soda ash", "furnace", "anneal", "annealing", "crucible"
    );
    private static final Set<String> WATER_PURIFICATION_HARD_REJECT_MARKERS = buildSet(
        "camp outbreak", "dysentery operations", "case tracking", "ors workflow",
        "handwashing enforcement", "food-service restrictions"
    );
    private static final Set<String> HOUSE_HARD_REJECT_MARKERS = buildSet(
        "debris hut", "lean-to shelter", "lean to shelter", "a-frame shelter",
        "snow shelter", "quinzhee", "tipi", "wickiup",
        "cave shelter", "cave shelter systems", "cave", "cavern",
        "greenhouse", "greenhouses", "cold frame", "cold frames",
        "primitive shelter construction", "primitive shelter construction techniques",
        "outbuildings for off-grid living", "simple shed construction",
        "chicken coop design", "root cellar construction",
        "group dynamics in shared shelters", "shared shelters",
        "seasonal shelter adaptation", "long-term camp evolution"
    );
    private static final Set<String> WATER_STORAGE_HARD_REJECT_MARKERS = buildSet(
        "camp outbreak", "dysentery operations", "case tracking", "ors workflow"
    );
    private static final Set<String> PUNCTURE_HARD_REJECT_MARKERS = buildSet(
        "animal bite", "bite wound", "rabies", "ophthalmology", "eye injuries",
        "emergency dental procedures", "oral anatomy", "diabetic foot", "diabetes management"
    );
    private static final Set<String> METAL_JOIN_HARD_REJECT_MARKERS = buildSet(
        "charcoal & fuels", "anti-counterfeit", "alternator repurposing"
    );

    private final Kind kind;
    private final List<String> expansionTokens;
    private final Set<String> preferredCategories;
    private final Set<String> positiveMarkers;
    private final Set<String> distractorMarkers;
    private final List<String> promptGuidanceLines;
    private final int preferredContextItems;
    private final int promptExcerptChars;
    private final boolean allowEarthShelterFallback;

    private QueryRouteProfile(
        Kind kind,
        List<String> expansionTokens,
        Set<String> preferredCategories,
        Set<String> positiveMarkers,
        Set<String> distractorMarkers,
        List<String> promptGuidanceLines,
        int preferredContextItems,
        int promptExcerptChars,
        boolean allowEarthShelterFallback
    ) {
        this.kind = kind;
        this.expansionTokens = expansionTokens;
        this.preferredCategories = preferredCategories;
        this.positiveMarkers = positiveMarkers;
        this.distractorMarkers = distractorMarkers;
        this.promptGuidanceLines = promptGuidanceLines;
        this.preferredContextItems = preferredContextItems;
        this.promptExcerptChars = promptExcerptChars;
        this.allowEarthShelterFallback = allowEarthShelterFallback;
    }

    public static QueryRouteProfile fromQuery(String query) {
        String lower = normalize(query);
        if (isWaterPurification(lower)) {
            return new QueryRouteProfile(
                Kind.WATER_PURIFICATION,
                immutableList(
                    "water purification", "filtration", "charcoal sand filter", "boiling", "chemical treatment",
                    "distillation", "solar disinfection", "safe drinking water", "water safety", "contamination"
                ),
                buildSet("survival", "medical", "utility"),
                WATER_PURIFICATION_POSITIVE_MARKERS,
                WATER_PURIFICATION_DISTRACTOR_MARKERS,
                immutableList(
                    "For water purification prompts, pick one safe treatment path from the notes and keep filtration, disinfection, and storage clearly separated.",
                    "If a simple filter is mentioned, explain that filtering alone is not the same as making water microbiologically safe unless the notes clearly support it.",
                    "Do not drift into alkali production, fisheries, irrigation, or broad waterway engineering. Use exactly 4 short numbered lines: source triage, prefilter, disinfect, safe storage."
                ),
                3,
                320,
                false
            );
        }
        if (isWaterStorage(lower)) {
            boolean explicitDistributionFocus = hasExplicitWaterDistributionFocus(lower);
            return new QueryRouteProfile(
                Kind.WATER_STORAGE,
                explicitDistributionFocus
                    ? immutableList(
                        "water distribution", "gravity-fed system", "source elevation", "storage tank",
                        "cistern", "overflow", "service line", "piping", "household taps", "spring box"
                    )
                    : immutableList(
                        "water storage", "safe containers", "container sanitation", "clean container",
                        "rationing", "rainwater harvesting", "sanitary storage", "reused bottle", "drum"
                    ),
                buildSet("survival", "resource-management", "utility", "building"),
                WATER_STORAGE_POSITIVE_MARKERS,
                WATER_STORAGE_DISTRACTOR_MARKERS,
                explicitDistributionFocus
                    ? immutableList(
                        "For gravity-fed water-distribution prompts, stay on source elevation, head pressure, main-line layout, and outlet/tank placement rather than bottle or drum sanitation advice.",
                        "If the notes split storage and distribution, synthesize one practical system path: source/head, main line and branch layout, elevated storage or pressure control, and distribution points.",
                        "Do not answer like a container-rotation prompt or a failure-analysis checklist. Use exactly 4 short numbered lines: source/head, main line layout, storage/pressure, outlets/limits."
                    )
                    : immutableList(
                        "For water storage prompts, stay focused on container vetting, cleaning, filling, sealing, and rotation rather than broad water systems.",
                        "If the container history is unknown or hazardous, say so plainly and prefer known food-safe or freshly cleaned containers.",
                        "Do not drift into alkali production, aquatic biology, or irrigation. Use exactly 4 short numbered lines: pick container, clean/sanitize, fill/seal, rotate/check."
                    ),
                explicitDistributionFocus ? 4 : 3,
                explicitDistributionFocus ? 420 : 320,
                false
            );
        }
        if (isSanitationSystem(lower)) {
            return new QueryRouteProfile(
                Kind.SANITATION_SYSTEM,
                immutableList(
                    "latrine construction", "toilet system", "wash station", "handwashing station",
                    "sanitation", "waste management", "greywater", "disease prevention", "contamination control"
                ),
                buildSet("building", "survival", "medical"),
                SANITATION_POSITIVE_MARKERS,
                SANITATION_DISTRACTOR_MARKERS,
                immutableList(
                    "For sanitation-system prompts, stay concrete on siting, contamination control, user flow, and maintenance.",
                    "Keep latrines, wash stations, and dirty-water handling clearly separated instead of answering with broad camp hygiene slogans.",
                    "Use exactly 4 short numbered lines: site/layout, build details, contamination control, maintenance."
                ),
                3,
                320,
                false
            );
        }
        if (isMessageAuth(lower)) {
            return new QueryRouteProfile(
                Kind.MESSAGE_AUTH,
                immutableList(
                    "message authentication", "courier protocols", "chain of custody", "challenge response",
                    "tamper evident", "verification", "seal", "urgent notes"
                ),
                buildSet("communications", "society"),
                MESSAGE_AUTH_POSITIVE_MARKERS,
                MESSAGE_AUTH_DISTRACTOR_MARKERS,
                immutableList(
                    "For message-authentication prompts, keep the answer on tamper evidence, challenge-response, chain of custody, and fallback verification.",
                    "Do not drift into evacuation planning, weather, or general governance theory unless the user explicitly asks for that.",
                    "Use exactly 4 short numbered lines: issue/authenticate, courier handoff, urgent-message verification, compromised-system fallback."
                ),
                3,
                320,
                false
            );
        }
        if (isFairTrial(lower)) {
            return new QueryRouteProfile(
                Kind.FAIR_TRIAL,
                immutableList(
                    "community trial procedure", "lay panel", "evidence standards", "hearing order",
                    "records", "appeal", "restorative justice", "small communities"
                ),
                buildSet("society"),
                FAIR_TRIAL_POSITIVE_MARKERS,
                FAIR_TRIAL_DISTRACTOR_MARKERS,
                immutableList(
                    "For a fair trial without lawyers or judges, go straight to lay-panel procedure.",
                    "Do not lead with mediation, property law, federation models, or ideological theory unless the question explicitly asks for them.",
                    "Use exactly 4 short numbered lines: panel selection, hearing order, evidence/record rules, review/appeal."
                ),
                3,
                320,
                false
            );
        }
        if (isClayOven(lower)) {
            return new QueryRouteProfile(
                Kind.CLAY_OVEN,
                immutableList(
                    "clay oven", "cob oven", "bread oven", "brick oven", "earth oven",
                    "site selection", "foundation", "chimney draft", "curing schedule",
                    "thermal mass", "refractory", "firebrick", "masonry hearth"
                ),
                buildSet("agriculture", "building", "crafts", "chemistry"),
                CLAY_OVEN_POSITIVE_MARKERS,
                CLAY_OVEN_DISTRACTOR_MARKERS,
                immutableList(
                    "For a clay or cob oven, keep the answer on site and foundation, oven body construction, drying or curing, and firing limits.",
                    "Prefer bread-oven and masonry-hearth guidance over unrelated kiln, accounting, or generic clay-processing tangents.",
                    "Use exactly 4 short numbered lines: choose site/foundation, build shell and hearth, dry or cure, fire and limits."
                ),
                3,
                420,
                false
            );
        }
        if (isCommunitySecurity(lower)) {
            return new QueryRouteProfile(
                Kind.COMMUNITY_SECURITY,
                immutableList(
                    "community defense", "physical security", "guard rotation", "checkpoint",
                    "perimeter", "access control", "early warning", "water point security",
                    "food storage security", "critical infrastructure"
                ),
                buildSet("defense", "resource-management", "building", "society"),
                COMMUNITY_SECURITY_POSITIVE_MARKERS,
                COMMUNITY_SECURITY_DISTRACTOR_MARKERS,
                immutableList(
                    "For community-security prompts, stay practical on guard coverage, chokepoints, early warning, and protecting vulnerable resources without spreading people too thin.",
                    "Favor low-tech layouts, observation, barriers, and watch rotation over electronics, abstract doctrine, or unrelated communications protocol advice.",
                    "Use exactly 4 short numbered lines: prioritize targets, set observation and warning, organize coverage, limits and escalation."
                ),
                4,
                420,
                false
            );
        }
        if (isCommunityGovernance(lower)) {
            return new QueryRouteProfile(
                Kind.COMMUNITY_GOVERNANCE,
                immutableList(
                    "resource governance", "commons management", "mutual aid", "reputation",
                    "vouching", "mediation", "restorative justice", "graduated sanctions",
                    "membership rules", "trust recovery"
                ),
                buildSet("society", "resource-management", "communications"),
                COMMUNITY_GOVERNANCE_POSITIVE_MARKERS,
                COMMUNITY_GOVERNANCE_DISTRACTOR_MARKERS,
                immutableList(
                    "For community-governance prompts, keep the answer on fair process, trust repair, monitoring, restitution, and shared rules rather than abstract ideology or finance.",
                    "If theft or sabotage is involved, combine immediate fact-finding with proportional consequences and a path back into the group when the notes support it.",
                    "Use exactly 4 short numbered lines: verify facts, set immediate containment, decide process and consequences, rebuild trust and records."
                ),
                4,
                420,
                false
            );
        }
        if (isSoapmaking(lower)) {
            return new QueryRouteProfile(
                Kind.SOAPMAKING,
                immutableList(
                    "soap making", "animal fat", "ash lye", "saponification", "caustic safety",
                    "curing", "tallow soap", "lye water"
                ),
                buildSet("crafts", "chemistry"),
                SOAP_POSITIVE_MARKERS,
                SOAP_DISTRACTOR_MARKERS,
                immutableList(
                    "For soapmaking, keep it practical: one safe ash-lye-and-fat starter path plus immediate lye-safety steps when relevant.",
                    "Do not drift into candle making or a full chemistry lecture.",
                    "Use exactly 4 short numbered lines: lye prep, fat prep, mix to trace, cure and safety."
                ),
                3,
                320,
                false
            );
        }
        if (isGlassmaking(lower)) {
            return new QueryRouteProfile(
                Kind.GLASSMAKING,
                immutableList(
                    "glassmaking", "silica", "soda ash", "lime", "glass furnace", "crucible",
                    "forming", "annealing", "glass from scratch"
                ),
                buildSet("crafts", "chemistry", "building"),
                GLASS_POSITIVE_MARKERS,
                GLASS_DISTRACTOR_MARKERS,
                immutableList(
                    "For making glass from scratch, keep the answer centered on raw materials, furnace heat, forming, and annealing.",
                    "Do not drift into optics, hourglasses, flat-glass salvage, or sodium silicate side uses.",
                    "Use exactly 4 short numbered lines: raw materials, furnace/crucible setup, melt/refine, forming/annealing."
                ),
                3,
                320,
                false
            );
        }
        if (isHouseBuild(lower)) {
            boolean allowEarthShelterFallback = containsAny(lower, HOUSE_EARTH_SHELTER_QUERY_MARKERS);
            return new QueryRouteProfile(
                Kind.HOUSE_BUILD,
                immutableList(
                    "shelter", "dwelling", "cabin", "hut", "roof", "wall",
                    "foundation", "framing", "weatherproofing", "drainage", "ventilation",
                    "site selection", "hazard assessment", "site prep", "terrain analysis", "stable ground",
                    "primitive shelter", "one room cabin", "rainproofing",
                    "window and door", "weatherstripping", "foundation waterproofing"
                ),
                buildSet("building", "survival", "resource-management", "utility"),
                buildHousePositiveMarkers(allowEarthShelterFallback),
                buildHouseDistractorMarkers(allowEarthShelterFallback),
                immutableList(
                    "For a house or cabin prompt, choose one simple low-tech starter dwelling path from the notes and keep the answer on site/drainage, floor/foundation, walls/frame, and roof/weatherproofing.",
                    "If the notes split across shelter and subsystem guides, synthesize one coherent starter cabin path instead of listing unrelated building topics.",
                    "For broad house prompts, prefer material-agnostic starter steps over dimensioned-lumber details, code-style calculations, or modern framing specs unless the retrieved notes make them central.",
                    "If the notes include both engineering detail and a general build sequence, prefer the general sequence first: choose site, prepare drainage and foundation, raise walls and roof, then weatherproof and seal.",
                    "Do not answer with a greenhouse, shed, cave, bunker, or other specialty structure unless the question explicitly asks for one.",
                    "Do not drift into census, settlement planning, abstract architecture theory, or broad housing policy. Use exactly 4 short numbered sections with no sub-bullets."
                ),
                3,
                340,
                allowEarthShelterFallback
            );
        }
        if (isSmallWatercraft(lower)) {
            return new QueryRouteProfile(
                Kind.SMALL_WATERCRAFT,
                immutableList(
                    "watercraft", "dugout", "paddle", "hull", "planking", "caulking", "buoyancy", "sealing",
                    "small watercraft", "dugout canoe", "plank canoe", "hollowed log",
                    "boat building", "boat caulking", "shoreline test"
                ),
                buildSet("transportation", "building", "survival"),
                WATERCRAFT_POSITIVE_MARKERS,
                WATERCRAFT_DISTRACTOR_MARKERS,
                immutableList(
                    "For a canoe or small boat, choose the simplest grounded build path from the notes, preferably dugout or basic plank construction when supported. Keep it on hull, shaping, sealing, and test-launch limits.",
                    "Choose one primary build path rather than mixing multiple boat families together. Mention an alternative only briefly if the material requirements are different.",
                    "Do not drift into rescue doctrine, shipping, or broad transportation theory. Use exactly 4 short numbered sections: choose hull type/material, shape the hull, seal/fit out, shoreline test and limits."
                ),
                3,
                340,
                false
            );
        }
        if (isGenericPuncture(lower)) {
            return new QueryRouteProfile(
                Kind.GENERIC_PUNCTURE,
                immutableList(
                    "first aid", "wound management", "irrigation", "infection prevention",
                    "tetanus", "dressings", "keep open", "do not probe", "monitor infection"
                ),
                buildSet("medical", "survival"),
                PUNCTURE_POSITIVE_MARKERS,
                PUNCTURE_DISTRACTOR_MARKERS,
                immutableList(
                    "For a generic puncture wound, keep the answer conservative and practical: bleeding check, irrigation, loose dressing, tetanus risk, and infection red flags.",
                    "Do not drift into rabies, animal-bite protocols, abdominal surgery, or invasive procedures unless the question explicitly asks for them.",
                    "Use exactly 4 short numbered lines: assess bleeding, irrigate/clean, dress and leave open if supported, watch for red flags."
                ),
                2,
                300,
                false
            );
        }
        if (isFireInRain(lower)) {
            return new QueryRouteProfile(
                Kind.FIRE_IN_RAIN,
                immutableList(
                    "fire in wet conditions", "tinder", "kindling", "fire bundle", "feather stick",
                    "dry inner wood", "shelter the flame", "char cloth", "fire lay", "windbreak"
                ),
                buildSet("survival"),
                FIRE_IN_RAIN_POSITIVE_MARKERS,
                FIRE_IN_RAIN_DISTRACTOR_MARKERS,
                immutableList(
                    "For wet-weather fire prompts, make it a dry-core ignition plan: shelter the work area, harvest dry inner material, stage tinder and kindling, then grow the flame carefully.",
                    "Prefer practical ignition advice from the notes over theory. If the notes show multiple methods, keep one as the primary path and mention backups briefly.",
                    "Do not drift into fire suppression, wildfire management, or fire safety systems. Use exactly 4 short numbered lines: shelter, gather dry core, ignite tinder, build the fire."
                ),
                3,
                300,
                false
            );
        }
        if (isMetalJoinWithoutWelder(lower)) {
            return new QueryRouteProfile(
                Kind.METAL_JOIN_NO_WELDER,
                immutableList(
                    "forge welding", "brazing", "soldering", "oxy-fuel", "forge", "metal join",
                    "joint integrity", "basic forge", "flux", "lap joint"
                ),
                buildSet("metalworking"),
                METAL_JOIN_POSITIVE_MARKERS,
                METAL_JOIN_DISTRACTOR_MARKERS,
                immutableList(
                    "For joining metal without an electric welder, choose one grounded low-tech joining family from the notes, such as forge welding, brazing, or soldering, based on heat and material limits.",
                    "Keep the answer on surface prep, fit-up, heat source, and joint limits instead of drifting into foundry, minting, or abstract metallurgy.",
                    "Use exactly 4 short numbered lines: choose method, prep joint, heat/join, inspect limits."
                ),
                3,
                300,
                false
            );
        }
        return new QueryRouteProfile(
            Kind.DEFAULT,
            Collections.emptyList(),
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptyList(),
            2,
            480,
            false
        );
    }

    public List<String> expansionTokens() {
        return expansionTokens;
    }

    public List<String> promptGuidanceLines() {
        return promptGuidanceLines;
    }

    public Set<String> preferredCategories() {
        return preferredCategories;
    }

    public int preferredContextItems() {
        return preferredContextItems;
    }

    public int promptExcerptChars() {
        return promptExcerptChars;
    }

    public boolean prefersSummaryKeyPointsFormat() {
        return switch (kind) {
            case MESSAGE_AUTH, FAIR_TRIAL, COMMUNITY_SECURITY, COMMUNITY_GOVERNANCE -> true;
            default -> false;
        };
    }

    public boolean isStarterBuildProject() {
        return kind == Kind.HOUSE_BUILD || kind == Kind.SMALL_WATERCRAFT;
    }

    public boolean usesCompactGuideSweep() {
        return kind == Kind.WATER_PURIFICATION;
    }

    public boolean usesCompactGuideSweep(String originalQuery) {
        if (kind == Kind.WATER_PURIFICATION) {
            return true;
        }
        if (kind == Kind.HOUSE_BUILD) {
            return looksLikeHouseSiteSelection(normalize(originalQuery));
        }
        if (kind != Kind.WATER_STORAGE) {
            return false;
        }
        String query = normalize(originalQuery);
        if (hasWaterStorageSystemFocus(query)) {
            return false;
        }
        return hasWaterStorageContainerFocus(query)
            || hasWaterStorageSanitationFocus(query)
            || hasWaterStoragePlanningFocus(query);
    }

    public boolean prefersDiversifiedAnswerContext() {
        return kind == Kind.HOUSE_BUILD
            || kind == Kind.CLAY_OVEN
            || kind == Kind.COMMUNITY_SECURITY
            || kind == Kind.COMMUNITY_GOVERNANCE;
    }

    public boolean isRouteFocused() {
        return kind != Kind.DEFAULT;
    }

    public boolean isSeasonalHouseSiteSelectionPrompt(String originalQuery) {
        if (kind != Kind.HOUSE_BUILD) {
            return false;
        }
        String query = normalize(originalQuery);
        if (!looksLikeHouseSiteSelection(query)) {
            return false;
        }
        return containsAny(query, HOUSE_SITE_SEASONAL_MARKERS)
            || (query.contains("sun") && query.contains("shade"));
    }

    public List<RouteSearchSpec> routeSearchSpecs(String originalQuery) {
        String query = normalize(originalQuery);
        ArrayList<RouteSearchSpec> specs = new ArrayList<>();
        boolean houseSiteFocus = kind == Kind.HOUSE_BUILD && (
            looksLikeHouseSiteSelection(query) || countMatches(query, HOUSE_SITE_FACTOR_MARKERS) >= 2
        );
        if (!query.isEmpty() && !houseSiteFocus) {
            specs.add(new RouteSearchSpec(query, preferredCategories, 12));
        }
        switch (kind) {
            case HOUSE_BUILD -> {
                boolean siteFocus = houseSiteFocus;
                boolean foundationFocus = query.contains("foundation") || query.contains("footing");
                boolean roofFocus = containsAny(query, HOUSE_ROOF_QUERY_MARKERS);
                boolean insulationFocus = containsAny(query, HOUSE_INSULATION_QUERY_MARKERS);
                boolean openingFocus = containsAny(query, HOUSE_OPENING_QUERY_MARKERS);
                boolean wallFocus = containsAny(query, HOUSE_WALL_QUERY_MARKERS);
                boolean subsystemFocus = foundationFocus || roofFocus || insulationFocus || openingFocus || wallFocus;
                boolean siteSeasonalFocus = siteFocus && (
                    containsAny(query, HOUSE_SITE_SEASONAL_MARKERS)
                        || (query.contains("sun") && query.contains("shade"))
                );
                boolean broadStarterBuild = !siteFocus && !subsystemFocus;

                if (siteSeasonalFocus) {
                    specs.add(spec(
                        "wind exposure microclimate sun exposure seasonal considerations winter sun summer shade shelter site selection access routes orientation",
                        24,
                        "survival"
                    ));
                }
                specs.add(spec(
                    "terrain analysis wind exposure sun exposure access routes shelter site selection hazard assessment natural hazards resource proximity seasonal considerations",
                    22,
                    "survival"
                ));
                specs.add(spec(
                    "stable ground drainage terrain analysis natural hazards water proximity shelter site selection hazard assessment seasonal considerations",
                    18,
                    allowEarthShelterFallback ? new String[]{"survival", "building"} : new String[]{"survival"}
                ));
                if (foundationFocus && !siteFocus) {
                    specs.add(spec(
                        "foundation footing load bearing sill plate pier rubble trench drainage stable ground frost line",
                        14,
                        "building"
                    ));
                } else if (foundationFocus || broadStarterBuild) {
                    specs.add(spec(
                        "simple one room house site drainage foundation frame roof weatherproofing",
                        siteFocus ? 12 : 16,
                        "building"
                    ));
                }
                if (broadStarterBuild) {
                    specs.add(spec("cabin hut construction floor wall roof insulation ventilation", 14, "building"));
                }
                if (broadStarterBuild) {
                    specs.add(spec("roof waterproofing sealants water shedding low resource structure", 10, "building"));
                }
                if (roofFocus) {
                    specs.add(spec("roof waterproofing rainproofing water shedding thatch bark shakes sealant flashing", 14, "building"));
                }
                if (insulationFocus) {
                    specs.add(spec("insulation thermal efficiency air sealing drafts heat loss condensation weatherproofing", 14, "building"));
                }
                if (openingFocus) {
                    specs.add(spec("window door assembly weatherstripping timber wall sill threshold", 10, "building"));
                }
                if (wallFocus) {
                    specs.add(spec("wall framing bracing sheathing siding wall assembly moisture barrier", 12, "building"));
                }
            }
            case SMALL_WATERCRAFT -> {
                specs.add(spec("dugout canoe construction tree selection hollowing sealing paddle", 16, "transportation"));
                specs.add(spec("small watercraft construction plank canoe ribs planking caulking", 14, "transportation"));
                specs.add(spec("boat building buoyancy hull stability waterproofing shoreline test", 12, "building"));
                specs.add(spec("transportation hollowed log dugout canoe bark canoe paddle", 10, "transportation"));
                specs.add(spec("boat caulking pitch resin seams waterproof hull", 10, "building"));
                specs.add(spec("tree selection straight trunk cedar pine cottonwood canoe", 8, "survival"));
            }
            case WATER_PURIFICATION -> {
                specs.add(spec("water purification filtration systems biosand filter construction safe drinking water", 16, "survival"));
                specs.add(spec("combining purification methods prefilter activated charcoal coarse sand gravel disinfection", 12, "survival"));
                specs.add(spec("sanitation public health contaminated water disease prevention disinfection", 8, "medical"));
                specs.add(spec("simple water treatment methods water system design distribution source quality", 10, "building"));
                specs.add(spec("chlorine bleach production water disinfection sanitation", 8, "chemistry"));
            }
            case WATER_STORAGE -> {
                boolean containerFocus = hasWaterStorageContainerFocus(query);
                boolean sanitationFocus = hasWaterStorageSanitationFocus(query);
                boolean planningFocus = hasWaterStoragePlanningFocus(query);
                boolean systemFocus = hasWaterStorageSystemFocus(query);
                boolean explicitDistributionFocus = hasExplicitWaterDistributionFocus(query);
                boolean focusedContainerLane = containerFocus || sanitationFocus;

                if (systemFocus) {
                    if (explicitDistributionFocus) {
                        specs.add(spec(
                            "community water distribution systems gravity fed storage tank household taps spring box service line",
                            18,
                            "building",
                            "utility"
                        ));
                    }
                    specs.add(spec(
                        "water system design distribution rainwater harvesting cistern storage tank elevated tank gravity fed piping",
                        explicitDistributionFocus ? 17 : 15,
                        "building",
                        "utility"
                    ));
                    specs.add(spec(
                        "storage tank cistern reserve tank overflow vent inlet outlet gravity fed distribution",
                        explicitDistributionFocus ? 13 : 11,
                        "building",
                        "utility",
                        "resource-management"
                    ));
                }
                specs.add(spec(
                    "safe water storage container sanitation reused bottles drums buckets jugs",
                    explicitDistributionFocus ? 6 : (systemFocus ? 10 : 14),
                    "survival",
                    "utility",
                    "resource-management"
                ));
                if (planningFocus || (!focusedContainerLane && !systemFocus)) {
                    specs.add(spec("long term water storage gallons per person rationing reserve rotation", 12, "survival", "resource-management", "utility"));
                }
                if (focusedContainerLane) {
                    specs.add(spec("water storage container selection preparation food grade clean container seal rotate reused bottle", 10, "resource-management", "utility", "survival"));
                } else if (!systemFocus || planningFocus) {
                    specs.add(spec("water storage systems clean container seal rotate food grade", 10, "survival", "utility", "resource-management"));
                }
                if (sanitationFocus || (!systemFocus && !planningFocus)) {
                    specs.add(spec("sanitation public health water container cleaning contamination prevention", 8, "medical"));
                }
            }
            case SANITATION_SYSTEM -> {
                specs.add(spec("latrine construction siting drainage water separation disease prevention", 16, "building", "survival"));
                specs.add(spec("sanitation and waste management systems latrine toilet wash station greywater", 14, "building", "medical"));
                specs.add(spec("handwashing station food prep wound cleaning hygiene station contamination control", 10, "medical", "survival"));
            }
            case MESSAGE_AUTH -> {
                specs.add(spec("message authentication courier protocols chain of custody tamper evidence", 16, "communications", "society"));
                specs.add(spec("challenge response verbal protocols emergency authentication compromised systems", 14, "communications", "society"));
                specs.add(spec("verify urgent notes posted orders seals check words courier handoff logbook", 10, "communications", "society"));
            }
            case FAIR_TRIAL -> {
                specs.add(spec("community trial procedure lay panel evidence records appeal", 16, "society"));
                specs.add(spec("criminal justice procedures for small communities evidence standard lay tribunal", 14, "society"));
                specs.add(spec("restorative justice versus adjudication small community due process", 10, "society"));
            }
            case CLAY_OVEN -> {
                specs.add(spec("clay bread oven construction site selection foundation cob oven", 18, "agriculture", "building"));
                specs.add(spec("cob oven construction thermal mass hearth chimney draft curing", 16, "building", "crafts"));
                specs.add(spec("brick bread oven refractory firebrick masonry hearth", 14, "building", "crafts", "chemistry"));
                specs.add(spec("earth oven clay oven firing schedule drying curing maintenance", 10, "agriculture", "building"));
            }
            case COMMUNITY_SECURITY -> {
                boolean resourceFocus = containsAny(query, COMMUNITY_SECURITY_TARGET_MARKERS);
                specs.add(spec("community defense physical security guard rotation checkpoint access control early warning", 18, "defense", "society"));
                specs.add(spec("protect vulnerable work site field water point food storage without spreading people too thin", 16, "resource-management", "defense"));
                specs.add(spec("critical infrastructure physical security no electronics barriers observation posts patrol routes", 14, "building", "defense"));
                if (resourceFocus) {
                    specs.add(spec("water point security food storage security perimeter detection watch rotation", 12, "resource-management", "defense"));
                }
            }
            case COMMUNITY_GOVERNANCE -> {
                boolean theftFocus = containsAny(query, buildSet("steal", "stealing", "theft", "stolen"));
                boolean trustFocus = containsAny(query, buildSet("trust", "dont trust", "don't trust", "merge", "merge groups", "vouch", "reputation"));
                specs.add(spec("commons management sustainable resource governance monitoring graduated sanctions", 18, "society", "resource-management"));
                specs.add(spec("community mediation restorative justice dispute resolution resource rules restitution", 16, "society", "communications"));
                if (theftFocus) {
                    specs.add(spec("theft response small community fact finding restitution proportional sanctions", 14, "society", "resource-management"));
                }
                if (trustFocus) {
                    specs.add(spec("merge groups trust recovery mutual aid reputation vouching membership rules", 14, "society", "communications"));
                } else {
                    specs.add(spec("trust reputation vouching mutual aid membership rules for small communities", 12, "society", "communications"));
                }
            }
            case SOAPMAKING -> {
                specs.add(spec("soap making from animal fat ash lye saponification curing safety cold process", 16, "crafts", "chemistry"));
                specs.add(spec("soap and candle making animal fat soap wood ash lye trace cure bar soap", 15, "crafts", "chemistry"));
                specs.add(spec("cold process soap rendered fat lye water mix to trace cure bar soap", 14, "crafts", "chemistry"));
                specs.add(spec("homestead chemistry soap making rendered fat tallow lard wood ash lye", 12, "crafts", "chemistry"));
                specs.add(spec("using fat for soap making rendered tallow lard wood ash lye", 10, "crafts", "agriculture", "chemistry"));
            }
            case GLASSMAKING -> {
                specs.add(spec("glassmaking from scratch silica sand soda ash furnace annealing", 16, "crafts", "chemistry"));
                specs.add(spec("glassmaking from scratch glass furnace forming annealing", 12, "crafts", "building", "chemistry"));
            }
            case GENERIC_PUNCTURE -> {
                specs.add(spec("puncture wound first aid do not probe irrigation dressing tetanus infection monitoring", 14, "medical"));
                specs.add(spec("wound hygiene infection prevention field sanitation puncture wound", 12, "medical"));
                specs.add(spec("first aid wound management contaminated wound cleaning red flags", 8, "medical", "survival"));
                specs.add(spec("first aid emergency response wound management wound assessment and cleaning", 10, "medical"));
            }
            case FIRE_IN_RAIN -> {
                specs.add(spec("fire in wet conditions tinder kindling dry inner wood windbreak", 14, "survival"));
                specs.add(spec("fire by friction methods tinder preparation fire bundle char cloth", 10, "survival"));
            }
            case METAL_JOIN_NO_WELDER -> {
                specs.add(spec("forge welding fire welding joint integrity metalworking", 14, "metalworking"));
                specs.add(spec("brazing soldering flux lap joint oxy fuel", 12, "metalworking", "transportation"));
                specs.add(spec("basic forge operation simple forge designs no special equipment", 8, "metalworking"));
            }
            default -> {
            }
        }
        return Collections.unmodifiableList(specs);
    }

    public int metadataBonus(
        String titleLower,
        String sectionLower,
        String categoryLower,
        String tagsLower,
        String descriptionLower,
        String documentLower
    ) {
        if (kind == Kind.DEFAULT) {
            return 0;
        }
        String combined = String.join(
            " ",
            titleLower,
            sectionLower,
            categoryLower,
            tagsLower,
            descriptionLower,
            documentLower
        );
        boolean positiveHit = containsAny(combined, positiveMarkers);
        boolean expansionHit = containsAny(combined, new LinkedHashSet<>(expansionTokens));
        boolean leadGuideHit = switch (kind) {
            case HOUSE_BUILD -> containsAny(combined, HOUSE_LEAD_MARKERS);
            case SMALL_WATERCRAFT -> containsAny(combined, WATERCRAFT_LEAD_MARKERS);
            case WATER_PURIFICATION -> containsAny(combined, WATER_PURIFICATION_LEAD_MARKERS);
            case WATER_STORAGE -> containsAny(combined, WATER_STORAGE_LEAD_MARKERS);
            case SANITATION_SYSTEM -> containsAny(combined, SANITATION_LEAD_MARKERS);
            case MESSAGE_AUTH -> containsAny(combined, MESSAGE_AUTH_LEAD_MARKERS);
            case FAIR_TRIAL -> containsAny(combined, FAIR_TRIAL_LEAD_MARKERS);
            case CLAY_OVEN -> containsAny(combined, CLAY_OVEN_LEAD_MARKERS);
            case COMMUNITY_SECURITY -> containsAny(combined, COMMUNITY_SECURITY_LEAD_MARKERS);
            case COMMUNITY_GOVERNANCE -> containsAny(combined, COMMUNITY_GOVERNANCE_LEAD_MARKERS);
            case SOAPMAKING -> containsAny(combined, SOAP_LEAD_MARKERS);
            case GLASSMAKING -> containsAny(combined, GLASS_LEAD_MARKERS);
            case GENERIC_PUNCTURE -> containsAny(combined, PUNCTURE_LEAD_MARKERS);
            case FIRE_IN_RAIN -> containsAny(combined, FIRE_IN_RAIN_LEAD_MARKERS);
            case METAL_JOIN_NO_WELDER -> containsAny(combined, METAL_JOIN_LEAD_MARKERS);
            default -> false;
        };
        int score = 0;
        if (kind == Kind.HOUSE_BUILD && !allowEarthShelterFallback && containsAny(combined, HOUSE_UNDERGROUND_MARKERS)) {
            score -= 28;
        }
        if (preferredCategories.contains(categoryLower) && (positiveHit || expansionHit || leadGuideHit)) {
            score += 12;
        } else if (isOffTargetCategory(categoryLower)) {
            score -= 10;
        }
        if (leadGuideHit) {
            score += 18;
        }
        if (positiveHit) {
            score += 18;
        }
        if (expansionHit) {
            score += 6;
        }
        if (containsAny(combined, distractorMarkers)) {
            score -= 20;
        }
        return score;
    }

    private static boolean isHouseBuild(String lower) {
        if (containsAny(lower, HOUSE_MARKERS)
            && (hasBuildVerb(lower) || containsAny(lower, HOUSE_INTENT_MARKERS))) {
            return true;
        }
        if (looksLikeHouseSiteSelection(lower)) {
            return true;
        }
        return containsAny(lower, HOUSE_SUBSYSTEM_QUERY_MARKERS) && !containsAny(lower, WATERCRAFT_MARKERS);
    }

    private static boolean isSmallWatercraft(String lower) {
        if (lower.contains("raft to cross a lake") || lower.contains("raft across a lake")) {
            return false;
        }
        return hasBuildVerb(lower) && containsAny(lower, WATERCRAFT_MARKERS);
    }

    private static boolean isClayOven(String lower) {
        return hasBuildVerb(lower) && containsAny(lower, OVEN_BUILD_QUERY_MARKERS);
    }

    private boolean isOffTargetCategory(String categoryLower) {
        return switch (kind) {
            case HOUSE_BUILD -> "medical".equals(categoryLower) || "society".equals(categoryLower)
                || "agriculture".equals(categoryLower) || "culture-knowledge".equals(categoryLower);
            case SMALL_WATERCRAFT -> "medical".equals(categoryLower) || "society".equals(categoryLower);
            case WATER_PURIFICATION -> "biology".equals(categoryLower) || "agriculture".equals(categoryLower);
            case WATER_STORAGE -> "biology".equals(categoryLower) || "agriculture".equals(categoryLower) || "chemistry".equals(categoryLower);
            case SANITATION_SYSTEM -> "agriculture".equals(categoryLower) || "society".equals(categoryLower);
            case MESSAGE_AUTH, FAIR_TRIAL -> "agriculture".equals(categoryLower) || "medical".equals(categoryLower)
                || "building".equals(categoryLower) || "transportation".equals(categoryLower);
            case CLAY_OVEN -> "society".equals(categoryLower) || "medical".equals(categoryLower);
            case COMMUNITY_SECURITY -> "medical".equals(categoryLower) || "chemistry".equals(categoryLower)
                || "crafts".equals(categoryLower) || "culture-knowledge".equals(categoryLower);
            case COMMUNITY_GOVERNANCE -> "medical".equals(categoryLower) || "chemistry".equals(categoryLower)
                || "building".equals(categoryLower) || "crafts".equals(categoryLower);
            case SOAPMAKING -> "society".equals(categoryLower);
            case GLASSMAKING -> "agriculture".equals(categoryLower) || "medical".equals(categoryLower) || "society".equals(categoryLower);
            case GENERIC_PUNCTURE -> "agriculture".equals(categoryLower) || "metalworking".equals(categoryLower)
                || "building".equals(categoryLower) || "chemistry".equals(categoryLower);
            case FIRE_IN_RAIN -> "medical".equals(categoryLower) || "defense".equals(categoryLower) || "building".equals(categoryLower);
            case METAL_JOIN_NO_WELDER -> "resource-management".equals(categoryLower) || "salvage".equals(categoryLower) || "society".equals(categoryLower);
            default -> false;
        };
    }

    private static boolean isWaterPurification(String lower) {
        return containsAny(lower, WATER_MARKERS) && containsAny(lower, WATER_PURITY_ACTION_MARKERS);
    }

    private static boolean isWaterStorage(String lower) {
        return containsAny(lower, WATER_MARKERS)
            && (containsAny(lower, WATER_STORAGE_ACTION_MARKERS) || hasWaterStorageSystemFocus(lower));
    }

    private static boolean hasWaterStorageContainerFocus(String lower) {
        return containsAny(lower, WATER_STORAGE_CONTAINER_QUERY_MARKERS);
    }

    private static boolean hasWaterStorageSanitationFocus(String lower) {
        return containsAny(lower, WATER_STORAGE_SANITATION_QUERY_MARKERS);
    }

    private static boolean hasWaterStoragePlanningFocus(String lower) {
        return containsAny(lower, WATER_STORAGE_PLANNING_QUERY_MARKERS);
    }

    private static boolean hasWaterStorageSystemFocus(String lower) {
        return containsAny(lower, WATER_STORAGE_SYSTEM_QUERY_MARKERS);
    }

    private static boolean hasExplicitWaterDistributionFocus(String lower) {
        if (!hasWaterStorageSystemFocus(lower)) {
            return false;
        }
        return containsAny(
            lower,
            buildSet(
                "distribution", "gravity-fed", "gravity fed", "piping", "plumbing",
                "household taps", "community water", "water tower", "spring box"
            )
        );
    }

    private static boolean isGenericPuncture(String lower) {
        return containsAny(lower, PUNCTURE_QUERY_MARKERS) && !containsAny(lower, ANIMAL_BITE_MARKERS);
    }

    private static boolean isSanitationSystem(String lower) {
        return containsAny(lower, SANITATION_QUERY_MARKERS);
    }

    private static boolean isMessageAuth(String lower) {
        boolean subject = containsAny(lower, buildSet("courier", "message", "note", "order"));
        boolean verification = containsAny(
            lower,
            buildSet("verify", "verification", "authentic", "real", "seal", "chain of custody", "challenge response", "tamper")
        );
        return subject && verification;
    }

    private static boolean isFairTrial(String lower) {
        return lower.contains("trial") && containsAny(lower, FAIR_TRIAL_QUERY_MARKERS);
    }

    private static boolean isCommunitySecurity(String lower) {
        return containsAny(lower, COMMUNITY_SECURITY_ACTION_MARKERS)
            && containsAny(lower, COMMUNITY_SECURITY_TARGET_MARKERS);
    }

    private static boolean isCommunityGovernance(String lower) {
        boolean theftConflict = containsAny(lower, buildSet("steal", "stealing", "theft", "stolen"))
            && containsAny(lower, buildSet("food", "group", "community", "commons", "stores", "storehouse"));
        boolean trustMerge = containsAny(
                lower,
                buildSet("trust", "trusts", "dont trust", "don't trust", "nobody trusts", "merge", "merge groups", "vouch", "reputation")
            )
            && containsAny(lower, buildSet("group", "groups", "community", "camp", "camps"));
        return theftConflict || trustMerge || countMatches(lower, COMMUNITY_GOVERNANCE_MARKERS) >= 2;
    }

    private static boolean isSoapmaking(String lower) {
        return lower.contains("soap") && containsAny(lower, SOAP_QUERY_MARKERS);
    }

    private static boolean isGlassmaking(String lower) {
        return lower.contains("glass") && containsAny(lower, GLASS_QUERY_MARKERS);
    }

    private static boolean isFireInRain(String lower) {
        return hasFireVerb(lower) && containsAny(lower, FIRE_MARKERS) && containsAny(lower, WET_WEATHER_MARKERS);
    }

    private static boolean isMetalJoinWithoutWelder(String lower) {
        if (containsAny(lower, WELDER_ABSENCE_MARKERS) && containsAny(lower, METAL_MARKERS) && containsAny(lower, JOIN_MARKERS)) {
            return true;
        }
        return lower.contains("weld without a welder");
    }

    private static boolean hasBuildVerb(String lower) {
        if (containsAny(lower, STARTER_BUILD_MARKERS)) {
            return true;
        }
        return lower.contains(" build ") || lower.contains(" construct ") || lower.contains(" make ")
            || lower.startsWith("build ") || lower.startsWith("construct ") || lower.startsWith("make ");
    }

    private static boolean looksLikeHouseSiteSelection(String lower) {
        return containsAny(lower, HOUSE_SITE_QUERY_MARKERS)
            && (countMatches(lower, HOUSE_SITE_FACTOR_MARKERS) >= 2
                || lower.contains("foundation")
                || lower.contains("footing"));
    }

    private static boolean hasFireVerb(String lower) {
        return lower.contains("start ") || lower.contains("make ") || lower.contains("build ")
            || lower.contains("light ") || lower.contains("ignite ");
    }

    public boolean supportsRouteResult(
        String titleLower,
        String sectionLower,
        String categoryLower,
        String tagsLower,
        String descriptionLower,
        String documentLower
    ) {
        if (kind == Kind.DEFAULT) {
            return false;
        }
        String combined = String.join(
            " ",
            titleLower,
            sectionLower,
            categoryLower,
            tagsLower,
            descriptionLower,
            documentLower
        );
        boolean positiveHit = containsAny(combined, positiveMarkers);
        boolean leadGuideHit = switch (kind) {
            case HOUSE_BUILD -> containsAny(combined, HOUSE_LEAD_MARKERS);
            case SMALL_WATERCRAFT -> containsAny(combined, WATERCRAFT_LEAD_MARKERS);
            case WATER_PURIFICATION -> containsAny(combined, WATER_PURIFICATION_LEAD_MARKERS);
            case WATER_STORAGE -> containsAny(combined, WATER_STORAGE_LEAD_MARKERS);
            case SANITATION_SYSTEM -> containsAny(combined, SANITATION_LEAD_MARKERS);
            case MESSAGE_AUTH -> containsAny(combined, MESSAGE_AUTH_LEAD_MARKERS);
            case FAIR_TRIAL -> containsAny(combined, FAIR_TRIAL_LEAD_MARKERS);
            case CLAY_OVEN -> containsAny(combined, CLAY_OVEN_LEAD_MARKERS);
            case COMMUNITY_SECURITY -> containsAny(combined, COMMUNITY_SECURITY_LEAD_MARKERS);
            case COMMUNITY_GOVERNANCE -> containsAny(combined, COMMUNITY_GOVERNANCE_LEAD_MARKERS);
            case SOAPMAKING -> containsAny(combined, SOAP_LEAD_MARKERS);
            case GLASSMAKING -> containsAny(combined, GLASS_LEAD_MARKERS);
            case GENERIC_PUNCTURE -> containsAny(combined, PUNCTURE_LEAD_MARKERS);
            case FIRE_IN_RAIN -> containsAny(combined, FIRE_IN_RAIN_LEAD_MARKERS);
            case METAL_JOIN_NO_WELDER -> containsAny(combined, METAL_JOIN_LEAD_MARKERS);
            default -> false;
        };
        boolean intentHit = switch (kind) {
            case HOUSE_BUILD -> containsAny(combined, HOUSE_INTENT_MARKERS);
            case SMALL_WATERCRAFT -> containsAny(combined, WATERCRAFT_INTENT_MARKERS);
            case WATER_PURIFICATION -> containsAny(combined, WATER_PURIFICATION_INTENT_MARKERS);
            case WATER_STORAGE -> containsAny(combined, WATER_STORAGE_INTENT_MARKERS);
            case SANITATION_SYSTEM -> containsAny(combined, SANITATION_INTENT_MARKERS);
            case MESSAGE_AUTH -> containsAny(combined, MESSAGE_AUTH_INTENT_MARKERS);
            case FAIR_TRIAL -> containsAny(combined, FAIR_TRIAL_INTENT_MARKERS);
            case CLAY_OVEN -> containsAny(combined, CLAY_OVEN_INTENT_MARKERS);
            case COMMUNITY_SECURITY -> containsAny(combined, COMMUNITY_SECURITY_INTENT_MARKERS);
            case COMMUNITY_GOVERNANCE -> containsAny(combined, COMMUNITY_GOVERNANCE_INTENT_MARKERS);
            case SOAPMAKING -> containsAny(combined, SOAP_INTENT_MARKERS);
            case GLASSMAKING -> containsAny(combined, GLASS_INTENT_MARKERS);
            case GENERIC_PUNCTURE -> containsAny(combined, PUNCTURE_INTENT_MARKERS);
            case FIRE_IN_RAIN -> containsAny(combined, FIRE_IN_RAIN_INTENT_MARKERS);
            case METAL_JOIN_NO_WELDER -> containsAny(combined, METAL_JOIN_INTENT_MARKERS);
            default -> false;
        };
        boolean hardReject = switch (kind) {
            case HOUSE_BUILD -> containsAny(combined, HOUSE_HARD_REJECT_MARKERS);
            case WATER_PURIFICATION -> containsAny(combined, WATER_PURIFICATION_HARD_REJECT_MARKERS);
            case WATER_STORAGE -> containsAny(combined, WATER_STORAGE_HARD_REJECT_MARKERS);
            case GENERIC_PUNCTURE -> containsAny(combined, PUNCTURE_HARD_REJECT_MARKERS);
            case METAL_JOIN_NO_WELDER -> containsAny(combined, METAL_JOIN_HARD_REJECT_MARKERS);
            default -> false;
        };
        int intentCount = switch (kind) {
            case HOUSE_BUILD -> countMatches(combined, HOUSE_INTENT_MARKERS);
            case SMALL_WATERCRAFT -> countMatches(combined, WATERCRAFT_INTENT_MARKERS);
            case WATER_PURIFICATION -> countMatches(combined, WATER_PURIFICATION_INTENT_MARKERS);
            case WATER_STORAGE -> countMatches(combined, WATER_STORAGE_INTENT_MARKERS);
            case SANITATION_SYSTEM -> countMatches(combined, SANITATION_INTENT_MARKERS);
            case MESSAGE_AUTH -> countMatches(combined, MESSAGE_AUTH_INTENT_MARKERS);
            case FAIR_TRIAL -> countMatches(combined, FAIR_TRIAL_INTENT_MARKERS);
            case CLAY_OVEN -> countMatches(combined, CLAY_OVEN_INTENT_MARKERS);
            case COMMUNITY_SECURITY -> countMatches(combined, COMMUNITY_SECURITY_INTENT_MARKERS);
            case COMMUNITY_GOVERNANCE -> countMatches(combined, COMMUNITY_GOVERNANCE_INTENT_MARKERS);
            case SOAPMAKING -> countMatches(combined, SOAP_INTENT_MARKERS);
            case GLASSMAKING -> countMatches(combined, GLASS_INTENT_MARKERS);
            case GENERIC_PUNCTURE -> countMatches(combined, PUNCTURE_INTENT_MARKERS);
            case FIRE_IN_RAIN -> countMatches(combined, FIRE_IN_RAIN_INTENT_MARKERS);
            case METAL_JOIN_NO_WELDER -> countMatches(combined, METAL_JOIN_INTENT_MARKERS);
            default -> 0;
        };

        if (kind == Kind.HOUSE_BUILD && hardReject) {
            return false;
        }
        if (kind == Kind.HOUSE_BUILD && !allowEarthShelterFallback && containsAny(combined, HOUSE_UNDERGROUND_MARKERS)) {
            return false;
        }
        if (kind == Kind.HOUSE_BUILD && containsAny(combined, distractorMarkers) && !leadGuideHit && intentCount < 2) {
            return false;
        }
        if (kind == Kind.GENERIC_PUNCTURE && hardReject && !leadGuideHit) {
            return false;
        }
        if (kind == Kind.SOAPMAKING && !leadGuideHit && !containsAny(combined, SOAP_FOCUS_MARKERS)) {
            return false;
        }
        if (hardReject && !(positiveHit || leadGuideHit)) {
            return false;
        }
        if (containsAny(combined, distractorMarkers) && !(positiveHit || leadGuideHit || intentHit)) {
            return false;
        }
        if (isOffTargetCategory(categoryLower) && !(positiveHit || leadGuideHit || intentCount >= requiredIntentMatches())) {
            return false;
        }
        return positiveHit || leadGuideHit || (intentCount >= requiredIntentMatches() && preferredCategories.contains(categoryLower));
    }

    private static boolean containsAny(String text, Set<String> markers) {
        String normalized = normalize(text);
        for (String marker : markers) {
            if (matchesMarker(normalized, marker)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesMarker(String normalizedText, String marker) {
        if (normalizedText.isEmpty() || marker == null || marker.isEmpty()) {
            return false;
        }
        String boundedText = " " + normalizedText.replaceAll("[^a-z0-9]+", " ").trim() + " ";
        String normalizedMarker = normalize(marker).replaceAll("[^a-z0-9]+", " ").trim();
        if (normalizedMarker.isEmpty()) {
            return false;
        }
        return boundedText.contains(" " + normalizedMarker + " ");
    }

    private static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.US);
    }

    private static Set<String> buildSet(String... values) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        Collections.addAll(set, values);
        return Collections.unmodifiableSet(set);
    }

    private static Set<String> buildHousePositiveMarkers(boolean allowEarthShelterFallback) {
        if (allowEarthShelterFallback) {
            LinkedHashSet<String> markers = new LinkedHashSet<>(HOUSE_POSITIVE_MARKERS);
            markers.addAll(HOUSE_UNDERGROUND_MARKERS);
            return Collections.unmodifiableSet(markers);
        }
        LinkedHashSet<String> markers = new LinkedHashSet<>(HOUSE_POSITIVE_MARKERS);
        markers.remove("earth-sheltering");
        return Collections.unmodifiableSet(markers);
    }

    private static Set<String> buildHouseDistractorMarkers(boolean allowEarthShelterFallback) {
        if (allowEarthShelterFallback) {
            return HOUSE_DISTRACTOR_MARKERS;
        }
        LinkedHashSet<String> markers = new LinkedHashSet<>(HOUSE_DISTRACTOR_MARKERS);
        markers.addAll(HOUSE_UNDERGROUND_MARKERS);
        return Collections.unmodifiableSet(markers);
    }

    private static List<String> immutableList(String... values) {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(values)));
    }

    private int requiredIntentMatches() {
        return switch (kind) {
            case WATER_PURIFICATION, WATER_STORAGE, SANITATION_SYSTEM, MESSAGE_AUTH, FAIR_TRIAL,
                CLAY_OVEN, COMMUNITY_SECURITY, COMMUNITY_GOVERNANCE,
                SOAPMAKING, GLASSMAKING, GENERIC_PUNCTURE -> 2;
            default -> 1;
        };
    }

    private static int countMatches(String text, Set<String> markers) {
        String normalized = normalize(text);
        int count = 0;
        for (String marker : markers) {
            if (matchesMarker(normalized, marker)) {
                count += 1;
            }
        }
        return count;
    }

    private static RouteSearchSpec spec(String text, int bonus, String... categories) {
        return new RouteSearchSpec(text, buildSet(categories), bonus);
    }

    public static final class RouteSearchSpec {
        private final String text;
        private final Set<String> categories;
        private final int bonus;

        RouteSearchSpec(String text, Set<String> categories, int bonus) {
            this.text = text;
            this.categories = categories;
            this.bonus = bonus;
        }

        public String text() {
            return text;
        }

        public Set<String> categories() {
            return categories;
        }

        public int bonus() {
            return bonus;
        }
    }
}
