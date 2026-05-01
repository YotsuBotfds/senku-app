package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QueryMetadataProfile {
    static final String CONTENT_ROLE_STARTER = "starter";
    static final String CONTENT_ROLE_PLANNING = "planning";
    static final String CONTENT_ROLE_SUBSYSTEM = "subsystem";
    private static final Pattern ROOFING_TOPIC_PATTERN = Pattern.compile(".*\\broof(?:ing)?\\b.*");
    private static final Pattern RAFTER_TOPIC_PATTERN = Pattern.compile(".*\\brafters?\\b.*");
    private static final Pattern GABLE_TOPIC_PATTERN = Pattern.compile(".*\\bgable\\b.*");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{Alnum}]+");
    static final String CONTENT_ROLE_SAFETY = "safety";

    static final String TIME_HORIZON_IMMEDIATE = "immediate";
    static final String TIME_HORIZON_LONG_TERM = "long_term";

    static final String STRUCTURE_TYPE_CABIN_HOUSE = "cabin_house";
    static final String STRUCTURE_TYPE_EMERGENCY_SHELTER = "emergency_shelter";
    static final String STRUCTURE_TYPE_EARTH_SHELTER = "earth_shelter";
    static final String STRUCTURE_TYPE_WATER_STORAGE = "water_storage";
    static final String STRUCTURE_TYPE_WATER_DISTRIBUTION = "water_distribution";
    static final String STRUCTURE_TYPE_WATER_PURIFICATION = "water_purification";
    static final String STRUCTURE_TYPE_SMALL_WATERCRAFT = "small_watercraft";
    static final String STRUCTURE_TYPE_WOUND_CARE = "wound_care";
    static final String STRUCTURE_TYPE_SANITATION_SYSTEM = "sanitation_system";
    static final String STRUCTURE_TYPE_MESSAGE_AUTH = "message_auth";
    static final String STRUCTURE_TYPE_FAIR_TRIAL = "fair_trial";
    static final String STRUCTURE_TYPE_CLAY_OVEN = "clay_oven";
    static final String STRUCTURE_TYPE_COMMUNITY_SECURITY = "community_security";
    static final String STRUCTURE_TYPE_COMMUNITY_GOVERNANCE = "community_governance";
    static final String STRUCTURE_TYPE_SOAPMAKING = "soapmaking";
    static final String STRUCTURE_TYPE_GLASSMAKING = "glassmaking";
    static final String STRUCTURE_TYPE_SAFETY_POISONING = "safety_poisoning";
    static final String STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH = "acute_mental_health";

    private static final Set<String> BUILD_MARKERS = buildSet(
        "how do i build", "how do we build", "how can i build", "how to build",
        "build a", "build an", "construct a", "construct an", "make a", "make an"
    );
    private static final Set<String> HOUSE_PROJECT_MARKERS = buildSet(
        "site", "site selection", "foundation", "drainage", "framing", "roof", "roofing",
        "weatherproof", "weatherproofing", "wall", "walls", "ventilation", "insulation",
        "window", "door", "threshold"
    );
    private static final Set<String> HOUSE_TOPIC_TAGS = buildSet(
        "site_selection", "drainage", "foundation", "wall_construction",
        "roofing", "weatherproofing", "ventilation"
    );
    private static final Set<String> HOUSE_SITE_QUERY_MARKERS = buildSet(
        "building site", "choose a site", "choose a building site", "safe site", "site and foundation"
    );
    private static final Set<String> SAFETY_MARKERS = buildSet(
        "safe", "safest", "safely", "risk", "hazard", "warning", "emergency"
    );
    private static final Set<String> POISONING_DIRECT_MARKERS = buildSet(
        "poisoning", "poisoned", "poison control"
    );
    private static final Set<String> POISONING_AMBIGUOUS_MARKERS = buildSet(
        "poison", "toxic", "toxicity"
    );
    private static final Set<String> POISONING_CHILD_MARKERS = buildSet(
        "child", "kid", "toddler", "baby", "infant"
    );
    private static final Set<String> POISONING_INGESTION_ROUTE_MARKERS = buildSet(
        "swallow", "swallowed", "swallowing",
        "ingest", "ingested", "ingestion",
        "drink", "drank", "drunk",
        "ate", "eaten",
        "lick", "licked",
        "taste", "tasted",
        "in mouth", "mouth burn", "mouth exposure", "mouth pain"
    );
    private static final Set<String> POISONING_EXPOSURE_ROUTE_MARKERS = buildSet(
        "in eye", "in my eye", "in the eye", "in eyes", "in my eyes",
        "eye exposure", "eye burn", "burning eye",
        "on skin", "on my skin", "on the skin",
        "skin exposure", "skin burn", "burning skin",
        "chemical burn", "acid burn",
        "splashed", "splash", "sprayed on", "got on",
        "inhaled", "breathed in",
        "fumes", "vapors", "vapours",
        "mixed cleaners", "mix bleach", "mixing bleach",
        "coughing", "chest tightness", "trouble breathing", "shortness of breath", "wheezing"
    );
    private static final Set<String> POISONING_CHEMICAL_SOURCE_MARKERS = buildSet(
        "cleaner", "cleaning product",
        "drain cleaner", "toilet bowl cleaner", "oven cleaner",
        "bleach", "ammonia",
        "lye", "caustic", "corrosive",
        "acid", "battery acid", "muriatic acid", "pool acid",
        "solvent", "detergent", "detergent pod"
    );
    private static final Set<String> POISONING_UNKNOWN_INGESTION_MARKERS = buildSet(
        "unknown ingestion", "unknown substance",
        "under the sink", "under sink",
        "got into cleaner", "got into the cleaner", "got into some cleaner"
    );
    private static final Set<String> ACUTE_MENTAL_HEALTH_QUERY_MARKERS = buildSet(
        "barely slept",
        "hardly slept",
        "keeps pacing",
        "normal rules do not apply",
        "special mission",
        "acting invincible",
        "nothing can hurt",
        "just stress",
        "calm down"
    );
    private static final Set<String> ACCESSIBILITY_MARKERS = buildSet(
        "accessible", "accessibility", "universal design", "wheelchair",
        "elderly", "disabled", "one-handed", "mobility", "grab bar", "grab bars"
    );
    private static final Set<String> CLIMATE_CONTEXT_MARKERS = buildSet(
        "desert", "arid", "tropical", "jungle", "swamp", "wetland",
        "alpine", "mountain", "snow", "winter", "cold climate", "hot climate"
    );
    static final Set<String> HOUSE_ACCESSIBILITY_SECTION_MARKERS = buildSet(
        "accessible", "accessibility", "universal design", "wheelchair", "elderly",
        "disabled", "one-handed", "mobility", "grab bar", "grab bars",
        "low vision", "blind"
    );
    static final Set<String> HOUSE_CLIMATE_SECTION_MARKERS = buildSet(
        "desert", "arid", "tropical", "jungle", "swamp", "wetland", "alpine",
        "mountain", "snow", "winter", "cold climate", "hot climate", "avalanche"
    );
    static final Set<String> HOUSE_SOCIAL_SHELTER_SECTION_MARKERS = buildSet(
        "shared shelter", "shared shelters", "group dynamics", "camp evolution",
        "camp layout", "seasonal shelter", "seasonal shelter adaptation",
        "long-term camp evolution", "communal shelter"
    );
    private static final Set<String> HOUSE_SITE_BREADTH_MARKERS = buildSet(
        "wind", "sun", "access", "hazard", "hazards", "terrain",
        "microclimate", "exposure", "seasonal", "resource proximity",
        "winter", "summer", "shade", "solar", "orientation"
    );
    private static final Set<String> LONG_TERM_MARKERS = buildSet(
        "long term", "long-term", "permanent", "semi-permanent", "seasonal", "store", "storage"
    );
    private static final Set<String> IMMEDIATE_MARKERS = buildSet(
        "emergency", "right now", "immediately", "temporary", "tonight", "first aid"
    );
    private static final Set<String> WATER_MARKERS = buildSet("water", "drink", "drinking");
    private static final Set<String> WATER_STORAGE_SYSTEM_MARKERS = buildSet(
        "water distribution", "distribution system", "gravity-fed", "gravity fed",
        "water system", "storage tank", "storage tanks", "cistern", "rainwater",
        "rainwater cistern", "community water", "water tower", "piping", "plumbing",
        "household taps", "household system", "spring box", "catchment"
    );
    private static final Set<String> WATER_STORAGE_SYSTEM_PROJECT_MARKERS = buildSet(
        "design", "set up", "setup", "build", "plan", "install"
    );
    private static final Set<String> WATER_STORAGE_CONTAINER_MARKERS = buildSet(
        "container", "containers", "vessel", "vessels", "jug", "jugs", "bucket", "buckets", "barrel", "barrels"
    );
    private static final Set<String> WATER_STORAGE_CONTAINER_BUILD_MARKERS = buildSet(
        "make", "build", "craft", "construct"
    );
    static final Set<String> WATER_DISTRIBUTION_META_SECTION_MARKERS = buildSet(
        "see also", "final checklist", "checklist",
        "preventive maintenance", "maintenance schedule", "maintenance schedules",
        "system care", "lifecycle"
    );
    private static final Set<String> MESSAGE_AUTH_QUERY_MARKERS = buildSet(
        "courier", "couriers", "message", "messages", "note", "notes", "order", "orders",
        "verify", "verification", "authentic", "real", "forgery", "forged", "seal",
        "tamper", "tampered", "chain-of-custody", "chain of custody", "challenge-response",
        "challenge response"
    );
    private static final Set<String> FAIR_TRIAL_QUERY_MARKERS = buildSet(
        "fair trial", "trial with no lawyers", "no lawyers or judges", "community trial", "lay tribunal"
    );
    private static final Set<String> CLAY_OVEN_QUERY_MARKERS = buildSet(
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
    private static final Set<String> COMMUNITY_GOVERNANCE_MERGE_MARKERS = buildSet(
        "merge", "merge groups", "merge with another group", "merge with another village", "integration"
    );
    private static final Set<String> COMMUNITY_GOVERNANCE_TRUST_REPAIR_MARKERS = buildSet(
        "trust", "trusts", "dont trust", "don't trust", "vouch", "reputation", "mediation", "restorative"
    );
    private static final Set<String> SOAP_QUERY_MARKERS = buildSet(
        "make soap", "making soap", "soap making", "soap from animal fat", "tallow soap",
        "wood ash lye", "lye water", "saponification"
    );
    private static final Set<String> GLASS_QUERY_MARKERS = buildSet(
        "make glass", "making glass", "glass from scratch", "glassmaking"
    );
    private static final Set<String> SANITATION_QUERY_MARKERS = buildSet(
        "latrine", "latrines", "toilet", "toilets", "wash station", "handwashing station",
        "hand washing station", "hygiene station", "waste management", "greywater"
    );
    private static final Map<String, Set<String>> STRUCTURE_MARKERS = buildStructureMarkers();
    private static final Map<String, Set<String>> TOPIC_MARKERS = buildTopicMarkers();

    private final Set<String> preferredContentRoles;
    private final Set<String> preferredCategories;
    private final Set<String> disfavoredCategories;
    private final String preferredTimeHorizon;
    private final String preferredStructureType;
    private final Set<String> preferredTopicTags;
    private final Set<String> explicitTopicTags;
    private final boolean prefersDiversifiedContext;
    private final boolean accessibilityIntent;
    private final boolean climateContextIntent;
    private final boolean siteSelectionLeadIntent;
    private final boolean siteBreadthIntent;
    private final boolean trustRepairMergeIntent;
    private final boolean waterStorageContainerMakingIntent;
    private final QueryMetadataSectionScoringPolicy sectionScoringPolicy;

    private QueryMetadataProfile(
        Set<String> preferredContentRoles,
        Set<String> preferredCategories,
        Set<String> disfavoredCategories,
        String preferredTimeHorizon,
        String preferredStructureType,
        Set<String> preferredTopicTags,
        Set<String> explicitTopicTags,
        boolean prefersDiversifiedContext,
        boolean accessibilityIntent,
        boolean climateContextIntent,
        boolean siteSelectionLeadIntent,
        boolean siteBreadthIntent,
        boolean trustRepairMergeIntent,
        boolean waterStorageContainerMakingIntent
    ) {
        this.preferredContentRoles = preferredContentRoles;
        this.preferredCategories = preferredCategories;
        this.disfavoredCategories = disfavoredCategories;
        this.preferredTimeHorizon = preferredTimeHorizon;
        this.preferredStructureType = preferredStructureType;
        this.preferredTopicTags = preferredTopicTags;
        this.explicitTopicTags = explicitTopicTags;
        this.prefersDiversifiedContext = prefersDiversifiedContext;
        this.accessibilityIntent = accessibilityIntent;
        this.climateContextIntent = climateContextIntent;
        this.siteSelectionLeadIntent = siteSelectionLeadIntent;
        this.siteBreadthIntent = siteBreadthIntent;
        this.trustRepairMergeIntent = trustRepairMergeIntent;
        this.waterStorageContainerMakingIntent = waterStorageContainerMakingIntent;
        this.sectionScoringPolicy = new QueryMetadataSectionScoringPolicy(this);
    }

    public static QueryMetadataProfile fromQuery(String query) {
        String normalized = normalize(query);
        String structureType = detectStructureType(normalized);
        boolean buildIntent = containsAny(normalized, BUILD_MARKERS)
            || STRUCTURE_TYPE_WATER_DISTRIBUTION.equals(structureType)
            || STRUCTURE_TYPE_CLAY_OVEN.equals(structureType)
            || (STRUCTURE_TYPE_CABIN_HOUSE.equals(structureType) && containsAny(normalized, HOUSE_PROJECT_MARKERS));
        String timeHorizon = detectTimeHorizon(normalized, structureType);

        LinkedHashSet<String> roles = new LinkedHashSet<>();
        if (buildIntent) {
            roles.add(CONTENT_ROLE_PLANNING);
            roles.add(CONTENT_ROLE_STARTER);
            roles.add(CONTENT_ROLE_SUBSYSTEM);
        } else {
            roles.add(CONTENT_ROLE_STARTER);
            roles.add(CONTENT_ROLE_SUBSYSTEM);
        }
        if (STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(structureType)
            || STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(structureType)) {
            roles.add(CONTENT_ROLE_PLANNING);
        }
        if (containsAny(normalized, SAFETY_MARKERS)
            || STRUCTURE_TYPE_WATER_STORAGE.equals(structureType)
            || STRUCTURE_TYPE_WATER_PURIFICATION.equals(structureType)
            || STRUCTURE_TYPE_WOUND_CARE.equals(structureType)
            || STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(structureType)
            || STRUCTURE_TYPE_SAFETY_POISONING.equals(structureType)) {
            roles.add(CONTENT_ROLE_SAFETY);
        }

        LinkedHashSet<String> explicitTopicTags = new LinkedHashSet<>(detectTopicTags(normalized));
        pruneCrossStructureExplicitTopics(explicitTopicTags, structureType);
        LinkedHashSet<String> topicTags = new LinkedHashSet<>(explicitTopicTags);
        boolean siteSelectionIntent = STRUCTURE_TYPE_CABIN_HOUSE.equals(structureType)
            && (explicitTopicTags.contains("site_selection") || containsAny(normalized, HOUSE_SITE_QUERY_MARKERS));
        seedTopicTags(topicTags, explicitTopicTags, buildIntent, structureType, siteSelectionIntent);
        LinkedHashSet<String> preferredCategories = new LinkedHashSet<>(buildPreferredCategories(structureType));
        LinkedHashSet<String> disfavoredCategories = new LinkedHashSet<>(buildDisfavoredCategories(structureType));
        if (STRUCTURE_TYPE_WATER_STORAGE.equals(structureType)
            && !explicitTopicTags.contains("water_distribution")) {
            preferredCategories.remove("building");
            preferredCategories.add("utility");
        }

        boolean diversified = (buildIntent && (
            STRUCTURE_TYPE_CABIN_HOUSE.equals(structureType)
                || STRUCTURE_TYPE_SMALL_WATERCRAFT.equals(structureType)
        )) || STRUCTURE_TYPE_WATER_STORAGE.equals(structureType)
            || STRUCTURE_TYPE_WATER_DISTRIBUTION.equals(structureType)
            || STRUCTURE_TYPE_WATER_PURIFICATION.equals(structureType)
            || STRUCTURE_TYPE_CLAY_OVEN.equals(structureType)
            || STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(structureType)
            || STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(structureType);
        boolean accessibilityIntent = containsAny(normalized, ACCESSIBILITY_MARKERS);
        boolean climateContextIntent = containsAny(normalized, CLIMATE_CONTEXT_MARKERS);
        boolean siteSelectionLeadIntent = STRUCTURE_TYPE_CABIN_HOUSE.equals(structureType)
            && containsAny(normalized, HOUSE_SITE_QUERY_MARKERS);
        boolean siteBreadthIntent = STRUCTURE_TYPE_CABIN_HOUSE.equals(structureType)
            && siteSelectionIntent
            && matchCount(normalized, HOUSE_SITE_BREADTH_MARKERS) >= 2;
        boolean trustRepairMergeIntent = STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(structureType)
            && containsAny(normalized, COMMUNITY_GOVERNANCE_MERGE_MARKERS)
            && containsAny(normalized, COMMUNITY_GOVERNANCE_TRUST_REPAIR_MARKERS);
        boolean waterStorageContainerMakingIntent = STRUCTURE_TYPE_WATER_STORAGE.equals(structureType)
            && containsAny(normalized, WATER_STORAGE_CONTAINER_MARKERS)
            && containsAny(normalized, WATER_STORAGE_CONTAINER_BUILD_MARKERS);

        return new QueryMetadataProfile(
            Collections.unmodifiableSet(roles),
            Collections.unmodifiableSet(preferredCategories),
            Collections.unmodifiableSet(disfavoredCategories),
            timeHorizon,
            structureType,
            Collections.unmodifiableSet(topicTags),
            Collections.unmodifiableSet(explicitTopicTags),
            diversified,
            accessibilityIntent,
            climateContextIntent,
            siteSelectionLeadIntent,
            siteBreadthIntent,
            trustRepairMergeIntent,
            waterStorageContainerMakingIntent
        );
    }

    public boolean prefersDiversifiedContext() {
        return prefersDiversifiedContext;
    }

    public String preferredStructureType() {
        return preferredStructureType;
    }

    public boolean hasExplicitTopicFocus() {
        return !explicitTopicTags.isEmpty();
    }

    public boolean hasExplicitTopicOverlap(String topicTags) {
        if (explicitTopicTags.isEmpty()) {
            return false;
        }
        for (String topicTag : splitCsv(topicTags)) {
            if (explicitTopicTags.contains(topicTag)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasExplicitTopic(String topicTag) {
        return explicitTopicTags.contains(normalize(topicTag));
    }

    public boolean hasPreferredTopicOverlap(String topicTags) {
        return preferredTopicOverlapCount(topicTags) > 0;
    }

    public int preferredTopicOverlapCount(String topicTags) {
        if (preferredTopicTags.isEmpty()) {
            return 0;
        }
        int overlaps = 0;
        for (String topicTag : splitCsv(topicTags)) {
            if (preferredTopicTags.contains(topicTag)) {
                overlaps += 1;
            }
        }
        return overlaps;
    }

    public Set<String> preferredTopicTags() {
        return preferredTopicTags;
    }

    Set<String> preferredContentRoles() {
        return preferredContentRoles;
    }

    Set<String> preferredCategories() {
        return preferredCategories;
    }

    Set<String> disfavoredCategories() {
        return disfavoredCategories;
    }

    String preferredTimeHorizon() {
        return preferredTimeHorizon;
    }

    Set<String> explicitTopicTags() {
        return explicitTopicTags;
    }

    boolean siteBreadthIntent() {
        return siteBreadthIntent;
    }

    public boolean accessibilityIntent() {
        return accessibilityIntent;
    }

    public boolean climateContextIntent() {
        return climateContextIntent;
    }

    public boolean siteSelectionLeadIntent() {
        return siteSelectionLeadIntent;
    }

    public boolean trustRepairMergeIntent() {
        return trustRepairMergeIntent;
    }

    public boolean waterStorageContainerMakingIntent() {
        return waterStorageContainerMakingIntent;
    }

    int sectionHeadingBonus(String sectionHeading) {
        return sectionScoringPolicy.sectionHeadingBonus(sectionHeading);
    }

    public int metadataBonus(String category, String contentRole, String timeHorizon, String structureType, String topicTags) {
        return sectionScoringPolicy.metadataBonus(
            category,
            contentRole,
            timeHorizon,
            structureType,
            topicTags
        );
    }

    private static Set<String> buildPreferredCategories(String structureType) {
        LinkedHashSet<String> categories = new LinkedHashSet<>();
        if (STRUCTURE_TYPE_CABIN_HOUSE.equals(structureType)
            || STRUCTURE_TYPE_EMERGENCY_SHELTER.equals(structureType)
            || STRUCTURE_TYPE_EARTH_SHELTER.equals(structureType)) {
            Collections.addAll(categories, "building", "survival");
        } else if (STRUCTURE_TYPE_WATER_STORAGE.equals(structureType)
            || STRUCTURE_TYPE_WATER_PURIFICATION.equals(structureType)) {
            Collections.addAll(categories, "resource-management", "survival", "building");
        } else if (STRUCTURE_TYPE_WATER_DISTRIBUTION.equals(structureType)) {
            Collections.addAll(categories, "building", "utility", "resource-management");
        } else if (STRUCTURE_TYPE_SMALL_WATERCRAFT.equals(structureType)) {
            Collections.addAll(categories, "transportation", "building", "survival");
        } else if (STRUCTURE_TYPE_WOUND_CARE.equals(structureType)) {
            Collections.addAll(categories, "medical");
        } else if (STRUCTURE_TYPE_SAFETY_POISONING.equals(structureType)) {
            Collections.addAll(categories, "medical", "chemistry");
        } else if (STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH.equals(structureType)) {
            Collections.addAll(categories, "medical");
        } else if (STRUCTURE_TYPE_SANITATION_SYSTEM.equals(structureType)) {
            Collections.addAll(categories, "building", "survival", "medical");
        } else if (STRUCTURE_TYPE_MESSAGE_AUTH.equals(structureType)
            || STRUCTURE_TYPE_FAIR_TRIAL.equals(structureType)) {
            Collections.addAll(categories, "communications", "society");
        } else if (STRUCTURE_TYPE_CLAY_OVEN.equals(structureType)) {
            Collections.addAll(categories, "agriculture", "building", "crafts", "chemistry");
        } else if (STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(structureType)) {
            Collections.addAll(categories, "defense", "resource-management", "building", "society");
        } else if (STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(structureType)) {
            Collections.addAll(categories, "society", "resource-management", "communications");
        } else if (STRUCTURE_TYPE_SOAPMAKING.equals(structureType)) {
            Collections.addAll(categories, "crafts", "chemistry");
        } else if (STRUCTURE_TYPE_GLASSMAKING.equals(structureType)) {
            Collections.addAll(categories, "crafts", "chemistry", "building");
        }
        return Collections.unmodifiableSet(categories);
    }

    private static Set<String> buildDisfavoredCategories(String structureType) {
        LinkedHashSet<String> categories = new LinkedHashSet<>();
        if (STRUCTURE_TYPE_CABIN_HOUSE.equals(structureType)) {
            Collections.addAll(categories, "agriculture", "medical", "society");
        } else if (STRUCTURE_TYPE_WATER_STORAGE.equals(structureType)
            || STRUCTURE_TYPE_WATER_PURIFICATION.equals(structureType)) {
            Collections.addAll(categories, "agriculture", "medical", "society");
        } else if (STRUCTURE_TYPE_WATER_DISTRIBUTION.equals(structureType)) {
            Collections.addAll(categories, "agriculture", "medical", "society");
        } else if (STRUCTURE_TYPE_SMALL_WATERCRAFT.equals(structureType)) {
            Collections.addAll(categories, "medical", "society");
        } else if (STRUCTURE_TYPE_WOUND_CARE.equals(structureType)) {
            Collections.addAll(categories, "agriculture", "building", "transportation", "society");
        } else if (STRUCTURE_TYPE_SAFETY_POISONING.equals(structureType)) {
            Collections.addAll(categories, "agriculture", "building", "crafts", "society", "transportation");
        } else if (STRUCTURE_TYPE_SANITATION_SYSTEM.equals(structureType)) {
            Collections.addAll(categories, "agriculture", "society");
        } else if (STRUCTURE_TYPE_MESSAGE_AUTH.equals(structureType)
            || STRUCTURE_TYPE_FAIR_TRIAL.equals(structureType)) {
            Collections.addAll(categories, "agriculture", "medical", "building", "transportation");
        } else if (STRUCTURE_TYPE_CLAY_OVEN.equals(structureType)) {
            Collections.addAll(categories, "society", "medical");
        } else if (STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(structureType)) {
            Collections.addAll(categories, "medical", "chemistry", "crafts", "culture-knowledge");
        } else if (STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(structureType)) {
            Collections.addAll(categories, "medical", "chemistry", "building", "crafts");
        } else if (STRUCTURE_TYPE_SOAPMAKING.equals(structureType)) {
            Collections.addAll(categories, "agriculture", "medical", "society", "transportation");
        } else if (STRUCTURE_TYPE_GLASSMAKING.equals(structureType)) {
            Collections.addAll(categories, "agriculture", "medical", "society");
        }
        return Collections.unmodifiableSet(categories);
    }

    private static void seedTopicTags(
        Set<String> topicTags,
        Set<String> explicitTopicTags,
        boolean buildIntent,
        String structureType,
        boolean siteSelectionIntent
    ) {
        if (buildIntent && STRUCTURE_TYPE_CABIN_HOUSE.equals(structureType)) {
            boolean explicitSiteSelection = siteSelectionIntent
                || (explicitTopicTags != null && explicitTopicTags.contains("site_selection"));
            boolean explicitFoundation = explicitTopicTags != null && explicitTopicTags.contains("foundation");
            boolean explicitWallConstruction = explicitTopicTags != null && explicitTopicTags.contains("wall_construction");
            boolean explicitRoofing = explicitTopicTags != null && explicitTopicTags.contains("roofing");
            boolean explicitWeatherproofing = explicitTopicTags != null && explicitTopicTags.contains("weatherproofing");
            boolean explicitVentilation = explicitTopicTags != null && explicitTopicTags.contains("ventilation");
            boolean explicitHouseSubsystem = explicitFoundation
                || explicitWallConstruction
                || explicitRoofing
                || explicitWeatherproofing
                || explicitVentilation;
            if (explicitSiteSelection) {
                topicTags.add("site_selection");
                topicTags.add("drainage");
                if (explicitFoundation) {
                    topicTags.add("foundation");
                }
                if (explicitRoofing) {
                    topicTags.add("roofing");
                }
                if (explicitWeatherproofing) {
                    topicTags.add("weatherproofing");
                }
                if (explicitVentilation) {
                    topicTags.add("ventilation");
                }
            } else if (explicitHouseSubsystem) {
                if (explicitFoundation) {
                    topicTags.add("foundation");
                    topicTags.add("drainage");
                }
                if (explicitWallConstruction) {
                    topicTags.add("wall_construction");
                }
                if (explicitRoofing) {
                    topicTags.add("roofing");
                    topicTags.add("weatherproofing");
                } else if (explicitWeatherproofing) {
                    topicTags.add("weatherproofing");
                }
                if (explicitVentilation) {
                    topicTags.add("ventilation");
                }
            } else {
                Collections.addAll(
                    topicTags,
                    "site_selection",
                    "drainage",
                    "foundation",
                    "wall_construction",
                    "roofing",
                    "weatherproofing",
                    "ventilation"
                );
            }
        }
        if (STRUCTURE_TYPE_WATER_STORAGE.equals(structureType)) {
            topicTags.add("water_storage");
            if (explicitTopicTags == null || !explicitTopicTags.contains("water_distribution")) {
                Collections.addAll(topicTags, "container_sanitation", "water_rotation");
            }
        }
        if (STRUCTURE_TYPE_WATER_DISTRIBUTION.equals(structureType)) {
            Collections.addAll(topicTags, "water_distribution", "water_storage");
        }
        if (STRUCTURE_TYPE_WATER_PURIFICATION.equals(structureType)) {
            Collections.addAll(topicTags, "water_purification", "prefilter", "disinfection");
        }
        if (STRUCTURE_TYPE_SMALL_WATERCRAFT.equals(structureType)) {
            Collections.addAll(topicTags, "small_watercraft", "hull", "sealing");
        }
        if (STRUCTURE_TYPE_WOUND_CARE.equals(structureType)) {
            Collections.addAll(topicTags, "first_aid", "wound_cleaning", "infection_monitoring");
        }
        if (STRUCTURE_TYPE_SAFETY_POISONING.equals(structureType)
            && explicitTopicTags != null
            && explicitTopicTags.contains("lye_safety")) {
            topicTags.add("lye_safety");
        }
        if (STRUCTURE_TYPE_SANITATION_SYSTEM.equals(structureType)) {
            Collections.addAll(topicTags, "latrine_design", "wash_station");
        }
        if (STRUCTURE_TYPE_MESSAGE_AUTH.equals(structureType)) {
            Collections.addAll(topicTags, "message_authentication", "chain_of_custody");
        }
        if (STRUCTURE_TYPE_FAIR_TRIAL.equals(structureType)) {
            Collections.addAll(topicTags, "trial_procedure", "evidence_rules");
        }
        if (STRUCTURE_TYPE_CLAY_OVEN.equals(structureType)) {
            Collections.addAll(topicTags, "clay_oven", "masonry_hearth");
        }
        if (STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(structureType)) {
            Collections.addAll(topicTags, "community_security", "resource_security");
        }
        if (STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(structureType)) {
            Collections.addAll(topicTags, "community_governance", "conflict_resolution", "trust_systems");
        }
        if (STRUCTURE_TYPE_SOAPMAKING.equals(structureType)) {
            Collections.addAll(topicTags, "soapmaking", "lye_safety");
        }
        if (STRUCTURE_TYPE_GLASSMAKING.equals(structureType)) {
            Collections.addAll(topicTags, "glassmaking", "annealing");
        }
    }

    private static int matchCount(String normalized, Set<String> markers) {
        if (normalized == null || normalized.isEmpty() || markers == null || markers.isEmpty()) {
            return 0;
        }
        List<String> textTokens = tokenize(normalized);
        if (textTokens.isEmpty()) {
            return 0;
        }
        LinkedHashSet<String> tokenSet = new LinkedHashSet<>(textTokens);
        int count = 0;
        for (String marker : markers) {
            if (matchesMarker(textTokens, tokenSet, marker)) {
                count += 1;
            }
        }
        return count;
    }

    private static void pruneCrossStructureExplicitTopics(Set<String> explicitTopicTags, String structureType) {
        if (explicitTopicTags == null || explicitTopicTags.isEmpty()) {
            return;
        }
        if (STRUCTURE_TYPE_CABIN_HOUSE.equals(structureType)) {
            LinkedHashSet<String> houseTags = new LinkedHashSet<>(explicitTopicTags);
            houseTags.retainAll(HOUSE_TOPIC_TAGS);
            if (!houseTags.isEmpty()) {
                explicitTopicTags.retainAll(HOUSE_TOPIC_TAGS);
            }
            return;
        }
        if (STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(structureType)) {
            explicitTopicTags.remove("community_governance");
            explicitTopicTags.remove("conflict_resolution");
            explicitTopicTags.remove("trust_systems");
            return;
        }
        if (STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(structureType)) {
            explicitTopicTags.remove("community_security");
            explicitTopicTags.remove("resource_security");
        }
    }

    private static String detectStructureType(String normalized) {
        for (Map.Entry<String, Set<String>> entry : STRUCTURE_MARKERS.entrySet()) {
            if (containsAny(normalized, entry.getValue())) {
                return entry.getKey();
            }
        }
        if (looksLikeMessageAuth(normalized)) {
            return STRUCTURE_TYPE_MESSAGE_AUTH;
        }
        if (looksLikeFairTrial(normalized)) {
            return STRUCTURE_TYPE_FAIR_TRIAL;
        }
        if (looksLikeClayOven(normalized)) {
            return STRUCTURE_TYPE_CLAY_OVEN;
        }
        if (looksLikeCommunitySecurity(normalized)) {
            return STRUCTURE_TYPE_COMMUNITY_SECURITY;
        }
        if (looksLikeCommunityGovernance(normalized)) {
            return STRUCTURE_TYPE_COMMUNITY_GOVERNANCE;
        }
        if (looksLikeSoapmaking(normalized)) {
            return STRUCTURE_TYPE_SOAPMAKING;
        }
        if (looksLikeGlassmaking(normalized)) {
            return STRUCTURE_TYPE_GLASSMAKING;
        }
        if (looksLikeSanitationSystem(normalized)) {
            return STRUCTURE_TYPE_SANITATION_SYSTEM;
        }
        if (looksLikeWaterDistributionSystem(normalized)) {
            return STRUCTURE_TYPE_WATER_DISTRIBUTION;
        }
        if (looksLikeWaterStorageSystem(normalized)) {
            return STRUCTURE_TYPE_WATER_STORAGE;
        }
        if (looksLikeSafetyPoisoning(normalized)) {
            return STRUCTURE_TYPE_SAFETY_POISONING;
        }
        if (looksLikeAcuteMentalHealthProfile(normalized)) {
            return STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH;
        }
        if (containsAny(normalized, HOUSE_PROJECT_MARKERS)) {
            return STRUCTURE_TYPE_CABIN_HOUSE;
        }
        return "";
    }

    private static boolean looksLikeWaterDistributionSystem(String normalized) {
        if (!containsAny(normalized, WATER_MARKERS)
            || containsAny(normalized, TOPIC_MARKERS.get("water_purification"))) {
            return false;
        }
        return containsAny(normalized, TOPIC_MARKERS.get("water_distribution"))
            && (containsAny(normalized, WATER_STORAGE_SYSTEM_PROJECT_MARKERS)
                || normalized.contains("system")
                || normalized.contains("gravity-fed")
                || normalized.contains("gravity fed"));
    }

    private static boolean looksLikeWaterStorageSystem(String normalized) {
        if (!containsAny(normalized, WATER_MARKERS)
            || containsAny(normalized, TOPIC_MARKERS.get("water_purification"))) {
            return false;
        }
        if (normalized.contains("water distribution system")
            || normalized.contains("gravity-fed water distribution")
            || normalized.contains("gravity fed water distribution")) {
            return true;
        }
        return containsAny(normalized, WATER_STORAGE_SYSTEM_MARKERS)
            && (containsAny(normalized, WATER_STORAGE_SYSTEM_PROJECT_MARKERS)
                || normalized.contains("system"));
    }

    private static String detectTimeHorizon(String normalized, String structureType) {
        if (STRUCTURE_TYPE_EMERGENCY_SHELTER.equals(structureType)
            || STRUCTURE_TYPE_SAFETY_POISONING.equals(structureType)
            || STRUCTURE_TYPE_ACUTE_MENTAL_HEALTH.equals(structureType)
            || containsAny(normalized, IMMEDIATE_MARKERS)) {
            return TIME_HORIZON_IMMEDIATE;
        }
        if (STRUCTURE_TYPE_CABIN_HOUSE.equals(structureType)
            || STRUCTURE_TYPE_WATER_STORAGE.equals(structureType)
            || STRUCTURE_TYPE_WATER_DISTRIBUTION.equals(structureType)
            || STRUCTURE_TYPE_CLAY_OVEN.equals(structureType)
            || STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(structureType)
            || STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(structureType)
            || containsAny(normalized, LONG_TERM_MARKERS)) {
            return TIME_HORIZON_LONG_TERM;
        }
        return "";
    }

    private static Set<String> detectTopicTags(String normalized) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        for (Map.Entry<String, Set<String>> entry : TOPIC_MARKERS.entrySet()) {
            if ("roofing".equals(entry.getKey())) {
                if (containsRoofingTopicMarker(normalized)) {
                    tags.add(entry.getKey());
                }
                continue;
            }
            if (containsAny(normalized, entry.getValue())) {
                tags.add(entry.getKey());
            }
        }
        return tags;
    }

    private static Map<String, Set<String>> buildStructureMarkers() {
        LinkedHashMap<String, Set<String>> markers = new LinkedHashMap<>();
        markers.put(
            STRUCTURE_TYPE_EMERGENCY_SHELTER,
            buildSet(
                "debris hut", "lean-to", "a-frame shelter", "emergency shelter",
                "rain shelter", "rain fly", "tarp shelter", "tarp ridgeline",
                "ridgeline shelter", "tarp and cord", "tarp and rope"
            )
        );
        markers.put(STRUCTURE_TYPE_CABIN_HOUSE, buildSet("house", "home", "cabin", "homestead", "dwelling"));
        markers.put(
            STRUCTURE_TYPE_EARTH_SHELTER,
            buildSet(
                "earth shelter", "earth-shelter", "earth sheltered", "earth-sheltering",
                "berm", "bunker", "underground shelter", "cut-and-cover"
            )
        );
        markers.put(
            STRUCTURE_TYPE_WATER_DISTRIBUTION,
            buildSet(
                "water distribution", "distribution system", "gravity-fed", "gravity fed",
                "community water", "household taps", "water tower", "spring box"
            )
        );
        markers.put(STRUCTURE_TYPE_WATER_STORAGE, buildSet("store water", "water storage", "stored water", "treated water"));
        markers.put(STRUCTURE_TYPE_WATER_PURIFICATION, buildSet("purify water", "water purification", "safe drinking water", "disinfect water"));
        markers.put(STRUCTURE_TYPE_SMALL_WATERCRAFT, buildSet("canoe", "boat", "watercraft", "dugout", "kayak"));
        markers.put(STRUCTURE_TYPE_WOUND_CARE, buildSet("splinter", "puncture", "wound", "first aid"));
        markers.put(STRUCTURE_TYPE_SANITATION_SYSTEM, buildSet("latrine", "toilet", "wash station", "handwashing station", "waste management"));
        markers.put(STRUCTURE_TYPE_MESSAGE_AUTH, buildSet("message authentication", "courier", "chain of custody", "challenge-response", "tamper-evident"));
        markers.put(STRUCTURE_TYPE_FAIR_TRIAL, buildSet("fair trial", "community trial", "lay tribunal", "trial procedure"));
        markers.put(STRUCTURE_TYPE_CLAY_OVEN, buildSet("clay oven", "bread oven", "cob oven", "brick oven", "earth oven", "masonry oven"));
        markers.put(
            STRUCTURE_TYPE_COMMUNITY_SECURITY,
            buildSet("community defense", "physical security", "access control", "checkpoint", "perimeter", "early warning")
        );
        markers.put(
            STRUCTURE_TYPE_COMMUNITY_GOVERNANCE,
            buildSet("resource governance", "commons management", "graduated sanctions", "mutual aid", "restorative justice", "community mediation")
        );
        markers.put(STRUCTURE_TYPE_SOAPMAKING, buildSet("soap making", "making soap", "soap from animal fat", "saponification"));
        markers.put(STRUCTURE_TYPE_GLASSMAKING, buildSet("glassmaking", "make glass", "glass from scratch", "annealing"));
        return Collections.unmodifiableMap(markers);
    }

    private static Map<String, Set<String>> buildTopicMarkers() {
        LinkedHashMap<String, Set<String>> markers = new LinkedHashMap<>();
        markers.put(
            "site_selection",
            buildSet(
                "site selection",
                "building site",
                "choose a site",
                "choose a building site",
                "choose a safe site",
                "safe site",
                "site and foundation",
                "terrain",
                "stable ground",
                "hazard assessment"
            )
        );
        markers.put("drainage", buildSet("drainage", "runoff", "french drain", "slope"));
        markers.put("foundation", buildSet("foundation", "foundations", "footing", "footings"));
        markers.put("wall_construction", buildSet("wall", "walls", "framing", "stud"));
        markers.put("roofing", buildSet("roof", "roofing", "rafter", "gable"));
        markers.put("weatherproofing", buildSet("weatherproof", "rainproof", "waterproof", "seal", "sealing"));
        markers.put("ventilation", buildSet("ventilation", "airflow", "condensation"));
        markers.put("water_storage", buildSet("water storage", "store water", "stored water", "treated water"));
        markers.put("container_sanitation", buildSet("container", "sanitize", "food-grade", "food safe", "clean container"));
        markers.put("water_rotation", buildSet("rotate", "rotation", "inspect", "seal"));
        markers.put(
            "water_distribution",
            buildSet(
                "distribution", "gravity-fed", "gravity fed", "water tower", "cistern", "household taps",
                "pipe", "piping", "plumbing", "community water", "catchment", "spring box"
            )
        );
        markers.put("water_purification", buildSet("purify", "purification", "filter", "filtration", "treat water"));
        markers.put("prefilter", buildSet("settle", "sediment", "prefilter", "pre-filter", "biosand"));
        markers.put("disinfection", buildSet("boil", "chlorine", "chlorinate", "disinfect", "sodis"));
        markers.put("small_watercraft", buildSet("canoe", "boat", "watercraft", "dugout"));
        markers.put("hull", buildSet("hull", "planking", "ribs", "buoyancy"));
        markers.put("sealing", buildSet("caulk", "caulking", "pitch", "resin", "seal"));
        markers.put("first_aid", buildSet("first aid", "wound", "puncture", "splinter"));
        markers.put("wound_cleaning", buildSet("clean", "wash", "rinse", "irrigate"));
        markers.put("infection_monitoring", buildSet("infection", "pus", "redness", "swelling", "tetanus"));
        markers.put("latrine_design", buildSet("latrine", "toilet", "pit latrine", "vip latrine", "waste trench"));
        markers.put("wash_station", buildSet("wash station", "handwashing", "hand washing", "greywater", "hygiene station"));
        markers.put("message_authentication", buildSet("verify", "authentication", "authentic", "real note", "tamper", "seal"));
        markers.put("chain_of_custody", buildSet("chain of custody", "courier", "challenge-response", "challenge response", "dead drop"));
        markers.put("trial_procedure", buildSet("trial", "lay tribunal", "hearing", "appeal", "panel selection"));
        markers.put("evidence_rules", buildSet("evidence", "record", "records", "witness", "record rules"));
        markers.put("clay_oven", buildSet("clay oven", "bread oven", "cob oven", "brick oven", "earth oven", "masonry oven"));
        markers.put("masonry_hearth", buildSet("hearth", "chimney", "draft", "thermal mass", "firebrick", "cob"));
        markers.put(
            "community_security",
            buildSet(
                "protect", "defend", "security", "guard", "watch", "patrol", "checkpoint",
                "perimeter", "access control", "early warning", "watch rotation"
            )
        );
        markers.put(
            "resource_security",
            buildSet("water point", "work site", "food storage", "storehouse", "granary", "critical infrastructure")
        );
        markers.put(
            "community_governance",
            buildSet(
                "commons", "resource governance", "resource rules", "graduated sanctions",
                "mutual aid", "restorative justice", "theft", "stealing", "trust", "trusts",
                "merge groups", "merge", "reputation", "vouch", "formal punishment", "punishment", "restitution"
            )
        );
        markers.put(
            "conflict_resolution",
            buildSet("theft", "stealing", "stolen", "mediation", "restorative", "sanction", "sanctions", "dispute", "restitution")
        );
        markers.put(
            "trust_systems",
            buildSet(
                "trust", "trusts", "reputation", "vouch", "merge", "merge groups",
                "mutual aid", "resource pooling", "integration", "nobody trusts"
            )
        );
        markers.put("soapmaking", buildSet("soap", "saponification", "lye water", "ash soap", "tallow soap"));
        markers.put(
            "lye_safety",
            buildSet(
                "lye", "caustic", "corrosive",
                "bleach", "drain cleaner", "toilet bowl cleaner", "oven cleaner", "detergent pod",
                "lye burn", "caustic burn", "flush with water", "chemical burn", "poison control"
            )
        );
        markers.put("glassmaking", buildSet("glass", "silica", "soda ash", "furnace", "crucible"));
        markers.put("annealing", buildSet("anneal", "annealing", "controlled cooling", "thermal stress"));
        return Collections.unmodifiableMap(markers);
    }

    private static boolean looksLikeMessageAuth(String normalized) {
        boolean subject = containsAny(normalized, buildSet("courier", "message", "note", "order"));
        boolean verification = containsAny(normalized, buildSet("verify", "verification", "authentic", "real", "forgery", "chain of custody", "challenge response", "seal"));
        return subject && verification;
    }

    private static boolean looksLikeFairTrial(String normalized) {
        return containsAny(normalized, FAIR_TRIAL_QUERY_MARKERS);
    }

    private static boolean looksLikeClayOven(String normalized) {
        return containsAny(normalized, CLAY_OVEN_QUERY_MARKERS) && containsAny(normalized, BUILD_MARKERS);
    }

    private static boolean looksLikeCommunitySecurity(String normalized) {
        return containsAny(normalized, COMMUNITY_SECURITY_ACTION_MARKERS)
            && containsAny(normalized, COMMUNITY_SECURITY_TARGET_MARKERS);
    }

    private static boolean looksLikeCommunityGovernance(String normalized) {
        boolean theftConflict = containsAny(normalized, buildSet("steal", "stealing", "theft", "stolen"))
            && containsAny(normalized, buildSet("food", "group", "community", "commons", "stores", "storehouse"));
        boolean trustMerge = containsAny(normalized, buildSet("trust", "dont trust", "don't trust", "merge", "merge groups", "vouch", "reputation"))
            && containsAny(normalized, buildSet("group", "groups", "community", "camp", "camps"));
        return theftConflict || trustMerge || containsAny(normalized, COMMUNITY_GOVERNANCE_MARKERS);
    }

    private static boolean looksLikeSoapmaking(String normalized) {
        return containsAny(normalized, buildSet("soap", "soapmaking")) && containsAny(normalized, SOAP_QUERY_MARKERS);
    }

    private static boolean looksLikeGlassmaking(String normalized) {
        return containsAny(normalized, buildSet("glass", "glassmaking")) && containsAny(normalized, GLASS_QUERY_MARKERS);
    }

    private static boolean looksLikeSanitationSystem(String normalized) {
        return containsAny(normalized, SANITATION_QUERY_MARKERS);
    }

    private static boolean looksLikeSafetyPoisoning(String normalized) {
        boolean childContext = containsAny(normalized, POISONING_CHILD_MARKERS);
        boolean ingestionRoute = containsAny(normalized, POISONING_INGESTION_ROUTE_MARKERS);
        boolean exposureRoute = ingestionRoute || containsAny(normalized, POISONING_EXPOSURE_ROUTE_MARKERS);
        boolean chemicalSource = containsAny(normalized, POISONING_CHEMICAL_SOURCE_MARKERS);
        boolean directPoisoning = containsAny(normalized, POISONING_DIRECT_MARKERS);
        boolean ambiguousPoisoning = containsAny(normalized, POISONING_AMBIGUOUS_MARKERS)
            && (childContext || exposureRoute || chemicalSource);
        boolean childUnknownIngestion = childContext && containsAny(normalized, POISONING_UNKNOWN_INGESTION_MARKERS);
        boolean childChemicalIngestion = childContext && ingestionRoute && chemicalSource;
        boolean chemicalExposure = chemicalSource && exposureRoute;
        return directPoisoning
            || ambiguousPoisoning
            || childUnknownIngestion
            || childChemicalIngestion
            || chemicalExposure;
    }

    private static boolean looksLikeAcuteMentalHealthProfile(String normalized) {
        return containsAny(normalized, ACUTE_MENTAL_HEALTH_QUERY_MARKERS);
    }

    static boolean matchesTopic(String text, String topicTag) {
        Set<String> markers = TOPIC_MARKERS.get(topicTag);
        if (markers == null || markers.isEmpty()) {
            return false;
        }
        return containsAny(normalize(text), markers);
    }

    private static boolean containsMarker(String normalized, String marker) {
        if (normalized == null || normalized.isEmpty()) {
            return false;
        }
        List<String> textTokens = tokenize(normalized);
        if (textTokens.isEmpty()) {
            return false;
        }
        return matchesMarker(textTokens, new LinkedHashSet<>(textTokens), marker);
    }

    static boolean containsAny(String normalized, Set<String> markers) {
        if (normalized == null || normalized.isEmpty() || markers == null || markers.isEmpty()) {
            return false;
        }
        List<String> textTokens = tokenize(normalized);
        if (textTokens.isEmpty()) {
            return false;
        }
        LinkedHashSet<String> tokenSet = new LinkedHashSet<>(textTokens);
        for (String marker : markers) {
            if (matchesMarker(textTokens, tokenSet, marker)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesMarker(List<String> textTokens, Set<String> tokenSet, String marker) {
        List<String> markerTokens = tokenize(normalize(marker));
        if (markerTokens.isEmpty()) {
            return false;
        }
        if (markerTokens.size() == 1) {
            return tokenSet.contains(markerTokens.get(0));
        }
        if (markerTokens.size() > textTokens.size()) {
            return false;
        }
        for (int i = 0; i <= textTokens.size() - markerTokens.size(); i++) {
            boolean matched = true;
            for (int j = 0; j < markerTokens.size(); j++) {
                if (!markerTokens.get(j).equals(textTokens.get(i + j))) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return true;
            }
        }
        return false;
    }

    static Set<String> splitCsv(String value) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String part : normalize(value).split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.US).trim();
    }

    private static List<String> tokenize(String normalized) {
        ArrayList<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(normalized);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private static boolean containsRoofingTopicMarker(String normalized) {
        return ROOFING_TOPIC_PATTERN.matcher(normalized).matches()
            || RAFTER_TOPIC_PATTERN.matcher(normalized).matches()
            || GABLE_TOPIC_PATTERN.matcher(normalized).matches();
    }

    private static Set<String> buildSet(String... values) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        Collections.addAll(set, values);
        return Collections.unmodifiableSet(set);
    }
}
