package com.senku.mobile;

import static com.senku.mobile.QueryMetadataProfile.*;

import java.util.Set;

final class QueryMetadataSectionScoringPolicy {
    private final Set<String> preferredContentRoles;
    private final Set<String> preferredCategories;
    private final Set<String> disfavoredCategories;
    private final String preferredTimeHorizon;
    private final String preferredStructureType;
    private final Set<String> preferredTopicTags;
    private final Set<String> explicitTopicTags;
    private final boolean accessibilityIntent;
    private final boolean climateContextIntent;
    private final boolean siteSelectionLeadIntent;
    private final boolean siteBreadthIntent;
    private final boolean trustRepairMergeIntent;

    QueryMetadataSectionScoringPolicy(QueryMetadataProfile profile) {
        this.preferredContentRoles = profile.preferredContentRoles();
        this.preferredCategories = profile.preferredCategories();
        this.disfavoredCategories = profile.disfavoredCategories();
        this.preferredTimeHorizon = profile.preferredTimeHorizon();
        this.preferredStructureType = profile.preferredStructureType();
        this.preferredTopicTags = profile.preferredTopicTags();
        this.explicitTopicTags = profile.explicitTopicTags();
        this.accessibilityIntent = profile.accessibilityIntent();
        this.climateContextIntent = profile.climateContextIntent();
        this.siteSelectionLeadIntent = profile.siteSelectionLeadIntent();
        this.siteBreadthIntent = profile.siteBreadthIntent();
        this.trustRepairMergeIntent = profile.trustRepairMergeIntent();
    }

    int sectionHeadingBonus(String sectionHeading) {
        String normalizedSection = normalize(sectionHeading);
        if (normalizedSection.isEmpty()) {
            return 0;
        }

        int score = 0;
        boolean explicitTopicFocus = !explicitTopicTags.isEmpty();
        if (explicitTopicFocus) {
            if (STRUCTURE_TYPE_CABIN_HOUSE.equals(preferredStructureType)) {
                score += explicitHouseSectionBonus(normalizedSection);
            } else if (STRUCTURE_TYPE_SMALL_WATERCRAFT.equals(preferredStructureType)) {
                score += explicitSingleTopicBonus(normalizedSection, "sealing", 16);
            } else if (STRUCTURE_TYPE_WOUND_CARE.equals(preferredStructureType)) {
                score += explicitSingleTopicBonus(normalizedSection, "infection_monitoring", 14);
                score += explicitSingleTopicBonus(normalizedSection, "wound_cleaning", 16);
            } else if (STRUCTURE_TYPE_SAFETY_POISONING.equals(preferredStructureType)) {
                score += explicitSingleTopicBonus(normalizedSection, "lye_safety", 14);
            } else if (STRUCTURE_TYPE_MESSAGE_AUTH.equals(preferredStructureType)) {
                score += explicitSingleTopicBonus(normalizedSection, "message_authentication", 18);
                score += explicitSingleTopicBonus(normalizedSection, "chain_of_custody", 16);
            } else if (STRUCTURE_TYPE_CLAY_OVEN.equals(preferredStructureType)) {
                score += explicitSingleTopicBonus(normalizedSection, "clay_oven", 18);
                score += explicitSingleTopicBonus(normalizedSection, "masonry_hearth", 14);
            } else if (STRUCTURE_TYPE_SOAPMAKING.equals(preferredStructureType)) {
                score += explicitSingleTopicBonus(normalizedSection, "soapmaking", 18);
                score += explicitSingleTopicBonus(normalizedSection, "lye_safety", 14);
            } else if (STRUCTURE_TYPE_GLASSMAKING.equals(preferredStructureType)) {
                score += explicitSingleTopicBonus(normalizedSection, "glassmaking", 18);
                score += explicitSingleTopicBonus(normalizedSection, "annealing", 14);
            } else if (STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(preferredStructureType)) {
                score += explicitSingleTopicBonus(normalizedSection, "community_security", 18);
                score += explicitSingleTopicBonus(normalizedSection, "resource_security", 14);
            } else if (STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(preferredStructureType)) {
                score += explicitSingleTopicBonus(normalizedSection, "community_governance", 18);
                score += explicitSingleTopicBonus(normalizedSection, "conflict_resolution", 16);
                score += explicitSingleTopicBonus(normalizedSection, "trust_systems", 14);
            }
        }
        if (STRUCTURE_TYPE_CABIN_HOUSE.equals(preferredStructureType)) {
            if (normalizedSection.contains("outbuildings") || normalizedSection.contains("off-grid")) {
                score -= explicitTopicFocus ? 8 : 20;
            }
            if (containsAny(normalizedSection, HOUSE_SOCIAL_SHELTER_SECTION_MARKERS)) {
                score -= explicitTopicFocus ? 24 : 26;
            }
            if (!explicitTopicFocus) {
                if (!accessibilityIntent && containsAny(normalizedSection, HOUSE_ACCESSIBILITY_SECTION_MARKERS)) {
                    score -= 18;
                }
                if (!climateContextIntent && containsAny(normalizedSection, HOUSE_CLIMATE_SECTION_MARKERS)) {
                    score -= 18;
                }
                if (normalizedSection.contains("thermal efficiency")
                    || normalizedSection.contains("air sealing")
                    || normalizedSection.contains("condensation")
                    || normalizedSection.contains("calculation")
                    || normalizedSection.contains("sizing")
                    || normalizedSection.contains("seismic")
                    || normalizedSection.contains("structural engineering basics")
                    || normalizedSection.contains("design loads")
                    || normalizedSection.contains("load paths")) {
                    score -= 18;
                }
                if (matchesTopic(sectionHeading, "foundation")) {
                    score += 8;
                }
                if (normalizedSection.contains("frost line")
                    || normalizedSection.contains("frost heave")
                    || normalizedSection.contains("footing")
                    || normalizedSection.contains("footings")) {
                    score += 5;
                }
                if (matchesTopic(sectionHeading, "drainage")) {
                    score += 7;
                }
                if (matchesTopic(sectionHeading, "site_selection")) {
                    score += 6;
                }
                if (matchesTopic(sectionHeading, "wall_construction")) {
                    score += 3;
                }
                if (matchesTopic(sectionHeading, "roofing")) {
                    score += 3;
                }
                if (matchesTopic(sectionHeading, "weatherproofing")) {
                    score += 3;
                }
                if (normalizedSection.contains("alternative building materials")) {
                    score += 4;
                }
            }
        } else if (STRUCTURE_TYPE_WATER_STORAGE.equals(preferredStructureType)) {
            boolean explicitBroadWaterStorageFocus = explicitTopicFocus && !explicitTopicTags.contains("water_distribution");
            if (!explicitTopicTags.contains("water_distribution")
                && (normalizedSection.contains("distribution")
                    || normalizedSection.contains("system design")
                    || normalizedSection.contains("community water")
                    || normalizedSection.contains("water tower")
                    || normalizedSection.contains("distribution network")
                    || normalizedSection.contains("main line")
                    || normalizedSection.contains("branch line")
                    || normalizedSection.contains("household taps")
                    || normalizedSection.contains("spring box")
                    || normalizedSection.contains("standpipe")
                    || normalizedSection.contains("service line")
                    || normalizedSection.contains("pressure"))) {
                score -= explicitTopicFocus ? 10 : 14;
            }
            if (explicitTopicTags.contains("water_distribution")) {
                if (normalizedSection.contains("distribution")
                    || normalizedSection.contains("gravity")
                    || normalizedSection.contains("storage tank")
                    || normalizedSection.contains("cistern")
                    || normalizedSection.contains("community water")) {
                    score += 10;
                }
                if (normalizedSection.contains("piping")
                    || normalizedSection.contains("plumbing")
                    || normalizedSection.contains("service line")
                    || normalizedSection.contains("spring box")
                    || normalizedSection.contains("household taps")
                    || normalizedSection.contains("overflow")) {
                    score += 7;
                }
                if (normalizedSection.contains("container sanitation")
                    || normalizedSection.contains("rotation")
                    || normalizedSection.contains("inspection")
                    || normalizedSection.contains("food grade")) {
                    score -= 8;
                }
            }
            if (matchesTopic(sectionHeading, "water_storage")) {
                score += explicitTopicFocus ? 6 : 8;
            }
            if (normalizedSection.contains("hydration")
                || normalizedSection.contains("maintenance")
                || normalizedSection.contains("storage location")
                || normalizedSection.contains("protection")) {
                int genericStorageBonus = explicitTopicFocus ? 4 : 6;
                if (explicitBroadWaterStorageFocus && normalizedSection.contains("hydration")) {
                    genericStorageBonus -= 8;
                }
                score += genericStorageBonus;
            }
            if (normalizedSection.contains("chemical storage")
                || normalizedSection.contains("hazard management")
                || normalizedSection.contains("hazard segregation")
                || normalizedSection.contains("incompatible materials")
                || normalizedSection.contains("ammunition")
                || normalizedSection.contains("explosive")
                || normalizedSection.contains("powder storage")) {
                score -= explicitTopicFocus ? 24 : 32;
            }
            if (normalizedSection.contains("historical context")
                || normalizedSection.contains("science of storage")) {
                score -= explicitTopicFocus ? 6 : 10;
            }
            if (normalizedSection.contains("food storage")
                || normalizedSection.contains("grain storage")
                || normalizedSection.contains("pantry storage")
                || normalizedSection.contains("root cellar")
                || normalizedSection.contains("root cellars")
                || normalizedSection.contains("canning")
                || normalizedSection.contains("preservation")) {
                score -= explicitTopicFocus ? 16 : 22;
            }
            if (normalizedSection.contains("critical documents")
                || normalizedSection.contains("assembly checklist")
                || normalizedSection.contains("two-week scenario")
                || normalizedSection.contains("shelf-stable")
                || normalizedSection.contains("cooking without power")) {
                score -= explicitTopicFocus ? 18 : 24;
            }
            if (normalizedSection.contains("container")) {
                score += explicitTopicFocus ? 4 : 7;
            }
            if (normalizedSection.contains("food grade")
                || normalizedSection.contains("food-safe")
                || normalizedSection.contains("sanitation")
                || normalizedSection.contains("seal integrity")) {
                score += explicitBroadWaterStorageFocus ? 18 : (explicitTopicFocus ? 4 : 7);
            }
            if (normalizedSection.contains("rotation")
                || normalizedSection.contains("inspection")
                || normalizedSection.contains("seal integrity")) {
                score += explicitBroadWaterStorageFocus ? 14 : (explicitTopicFocus ? 4 : 7);
            }
            if (normalizedSection.contains("pest prevention")
                || normalizedSection.contains("rodent")
                || normalizedSection.contains("insect control")) {
                score -= explicitTopicFocus ? 10 : 16;
            }
        } else if (STRUCTURE_TYPE_WATER_DISTRIBUTION.equals(preferredStructureType)) {
            if (matchesTopic(sectionHeading, "water_distribution")) {
                score += explicitTopicFocus ? 10 : 12;
            }
            if (normalizedSection.contains("gravity-fed")
                || normalizedSection.contains("distribution")
                || normalizedSection.contains("storage tank")
                || normalizedSection.contains("system components")
                || normalizedSection.contains("household taps")
                || normalizedSection.contains("overflow")
                || normalizedSection.contains("spring box")) {
                score += explicitTopicFocus ? 8 : 10;
            }
            if (normalizedSection.contains("tower")
                || normalizedSection.contains("layout")
                || normalizedSection.contains("network")
                || normalizedSection.contains("main line")
                || normalizedSection.contains("branch line")
                || normalizedSection.contains("water point")
                || normalizedSection.contains("standpipe")) {
                score += explicitTopicFocus ? 6 : 8;
            }
            if (normalizedSection.contains("common mistakes")
                || normalizedSection.contains("operational mistakes")
                || normalizedSection.contains("construction mistakes")
                || normalizedSection.contains("troubleshooting")
                || normalizedSection.contains("emergency repairs")
                || normalizedSection.contains("system recovery")) {
                score -= explicitTopicFocus ? 10 : 14;
            }
            if (containsAny(normalizedSection, WATER_DISTRIBUTION_META_SECTION_MARKERS)) {
                score -= explicitTopicFocus ? 26 : 30;
            }
            if (normalizedSection.startsWith("phase ")) {
                score -= explicitTopicFocus ? 12 : 16;
            }
            if (normalizedSection.contains("chemical storage")
                || normalizedSection.contains("hazard management")
                || normalizedSection.contains("food storage")
                || normalizedSection.contains("grain storage")
                || normalizedSection.contains("pantry storage")) {
                score -= explicitTopicFocus ? 26 : 34;
            }
            if (normalizedSection.contains("hydration assurance")
                || normalizedSection.contains("container sanitation")
                || normalizedSection.contains("rotation schedules")) {
                score -= explicitTopicFocus ? 10 : 16;
            }
        } else if (STRUCTURE_TYPE_SAFETY_POISONING.equals(preferredStructureType)) {
            if (matchesTopic(sectionHeading, "lye_safety")) {
                score += explicitTopicFocus ? 8 : 10;
            }
            if (normalizedSection.contains("airway")
                || normalizedSection.contains("first five minutes")
                || normalizedSection.contains("field priorities")
                || normalizedSection.contains("stabilization")
                || normalizedSection.contains("decontamination")
                || normalizedSection.contains("exposure treatment")
                || normalizedSection.contains("emergency procedures")
                || normalizedSection.contains("escalation")
                || normalizedSection.contains("activated charcoal")
                || normalizedSection.contains("induce vomiting")
                || normalizedSection.contains("antidote")
                || normalizedSection.contains("toxidrome")
                || normalizedSection.contains("poisoning")
                || normalizedSection.contains("poison")) {
                score += explicitTopicFocus ? 8 : 10;
            }
            if (normalizedSection.contains("worked example")
                || normalizedSection.contains("chemical theory")
                || normalizedSection.contains("scale-up")
                || normalizedSection.contains("production")
                || normalizedSection.contains("equipment setup")
                || normalizedSection.contains("storage compatibility")
                || normalizedSection.contains("soap making")
                || normalizedSection.contains("saponification")) {
                score -= explicitTopicFocus ? 10 : 14;
            }
        } else if (STRUCTURE_TYPE_MESSAGE_AUTH.equals(preferredStructureType)) {
            if (matchesTopic(sectionHeading, "message_authentication")) {
                score += explicitTopicFocus ? 8 : 10;
            }
            if (matchesTopic(sectionHeading, "chain_of_custody")) {
                score += explicitTopicFocus ? 7 : 9;
            }
            if (normalizedSection.contains("governance")
                || normalizedSection.contains("leadership")
                || normalizedSection.contains("trust")) {
                score -= explicitTopicFocus ? 10 : 14;
            }
        } else if (STRUCTURE_TYPE_CLAY_OVEN.equals(preferredStructureType)) {
            if (matchesTopic(sectionHeading, "clay_oven")) {
                score += explicitTopicFocus ? 10 : 12;
            }
            if (matchesTopic(sectionHeading, "masonry_hearth")) {
                score += explicitTopicFocus ? 7 : 9;
            }
            if (normalizedSection.contains("site selection")
                || normalizedSection.contains("foundation")
                || normalizedSection.contains("chimney")
                || normalizedSection.contains("draft")
                || normalizedSection.contains("curing")
                || normalizedSection.contains("thermal mass")) {
                score += explicitTopicFocus ? 5 : 7;
            }
            if (normalizedSection.contains("lab site")
                || normalizedSection.contains("laboratory")
                || normalizedSection.contains("chemical setup")) {
                score -= explicitTopicFocus ? 18 : 22;
            }
            if (normalizedSection.contains("currency")
                || normalizedSection.contains("ledger")
                || normalizedSection.contains("token")
                || normalizedSection.contains("insurance")
                || normalizedSection.contains("accounting")
                || normalizedSection.contains("aquaculture")) {
                score -= explicitTopicFocus ? 10 : 14;
            }
        } else if (STRUCTURE_TYPE_SOAPMAKING.equals(preferredStructureType)) {
            if (matchesTopic(sectionHeading, "soapmaking")) {
                score += explicitTopicFocus ? 8 : 10;
            }
            if (matchesTopic(sectionHeading, "lye_safety")) {
                score += explicitTopicFocus ? 7 : 9;
            }
            if (normalizedSection.contains("overview")
                || normalizedSection.contains("industrial applications")
                || normalizedSection.contains("storage compatibility")) {
                score -= explicitTopicFocus ? 10 : 14;
            }
        } else if (STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(preferredStructureType)) {
            if (matchesTopic(sectionHeading, "community_security")) {
                score += explicitTopicFocus ? 10 : 12;
            }
            if (matchesTopic(sectionHeading, "resource_security")) {
                score += explicitTopicFocus ? 7 : 9;
            }
            if (normalizedSection.contains("access control")
                || normalizedSection.contains("checkpoint")
                || normalizedSection.contains("perimeter")
                || normalizedSection.contains("early warning")
                || normalizedSection.contains("watch rotation")) {
                score += explicitTopicFocus ? 6 : 8;
            }
            if (normalizedSection.contains("message authentication")
                || normalizedSection.contains("chain of custody")
                || normalizedSection.contains("trial")
                || normalizedSection.contains("appeal")) {
                score -= explicitTopicFocus ? 10 : 14;
            }
        } else if (STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(preferredStructureType)) {
            if (matchesTopic(sectionHeading, "community_governance")) {
                score += explicitTopicFocus ? 10 : 12;
            }
            if (matchesTopic(sectionHeading, "conflict_resolution")) {
                score += explicitTopicFocus ? 8 : 10;
            }
            if (matchesTopic(sectionHeading, "trust_systems")) {
                score += explicitTopicFocus ? 7 : 9;
            }
            if (normalizedSection.contains("graduated sanctions")
                || normalizedSection.contains("mediation")
                || normalizedSection.contains("restitution")
                || normalizedSection.contains("membership")
                || normalizedSection.contains("monitoring")) {
                score += explicitTopicFocus ? 5 : 7;
            }
            if (normalizedSection.contains("insurance")
                || normalizedSection.contains("accounting")
                || normalizedSection.contains("checkpoint")
                || normalizedSection.contains("perimeter")) {
                score -= explicitTopicFocus ? 8 : 10;
            }
            if (trustRepairMergeIntent) {
                if (normalizedSection.contains("trust")
                    || normalizedSection.contains("reputation")
                    || normalizedSection.contains("vouch")
                    || normalizedSection.contains("restorative")
                    || normalizedSection.contains("mediation")) {
                    score += explicitTopicFocus ? 8 : 10;
                }
                if (normalizedSection.contains("monitoring")
                    || normalizedSection.contains("membership")
                    || normalizedSection.contains("boundaries")
                    || normalizedSection.contains("graduated sanctions")
                    || normalizedSection.contains("sanctions")
                    || normalizedSection.contains("quota")
                    || normalizedSection.contains("allocation")) {
                    score -= explicitTopicFocus ? 6 : 8;
                }
                if (normalizedSection.contains("insurance")
                    || normalizedSection.contains("accounting")
                    || normalizedSection.contains("fund governance")
                    || normalizedSection.contains("risk pooling")
                    || normalizedSection.contains("record-keeping")) {
                    score -= explicitTopicFocus ? 12 : 14;
                }
            }
        }

        if ("conclusion".equals(normalizedSection) || "introduction".equals(normalizedSection)) {
            score -= explicitTopicFocus ? 10 : 4;
        } else if ("quick reference".equals(normalizedSection)) {
            score -= explicitTopicFocus ? 6 : 2;
        }
        return score;
    }

    public int metadataBonus(String category, String contentRole, String timeHorizon, String structureType, String topicTags) {
        String normalizedCategory = normalize(category);
        String normalizedRole = normalize(contentRole);
        String normalizedTime = normalize(timeHorizon);
        String normalizedStructure = normalize(structureType);
        Set<String> rowTopics = splitCsv(topicTags);

        int score = 0;
        boolean explicitWaterDistribution = STRUCTURE_TYPE_WATER_DISTRIBUTION.equals(preferredStructureType)
            || (STRUCTURE_TYPE_WATER_STORAGE.equals(preferredStructureType)
                && explicitTopicTags.contains("water_distribution"));
        if (!normalizedCategory.isEmpty()) {
            if (preferredCategories.contains(normalizedCategory)) {
                score += 6;
            } else if (disfavoredCategories.contains(normalizedCategory)) {
                score -= 12;
            }
        }
        if (!preferredStructureType.isEmpty()) {
            if (preferredStructureType.equals(normalizedStructure)
                || (explicitWaterDistribution && STRUCTURE_TYPE_WATER_DISTRIBUTION.equals(normalizedStructure))) {
                score += 18;
            } else if (normalizedStructure.isEmpty() || "general".equals(normalizedStructure)) {
                score -= 4;
            } else if (STRUCTURE_TYPE_CABIN_HOUSE.equals(preferredStructureType)
                && STRUCTURE_TYPE_SMALL_WATERCRAFT.equals(normalizedStructure)) {
                score -= 18;
            } else if (STRUCTURE_TYPE_CABIN_HOUSE.equals(preferredStructureType)
                && STRUCTURE_TYPE_EARTH_SHELTER.equals(normalizedStructure)) {
                score -= 12;
            } else if (STRUCTURE_TYPE_CABIN_HOUSE.equals(preferredStructureType)
                && STRUCTURE_TYPE_EMERGENCY_SHELTER.equals(normalizedStructure)) {
                score -= 20;
            } else if (STRUCTURE_TYPE_WATER_STORAGE.equals(preferredStructureType)
                && STRUCTURE_TYPE_WATER_PURIFICATION.equals(normalizedStructure)) {
                score -= 6;
            } else if (STRUCTURE_TYPE_WATER_PURIFICATION.equals(preferredStructureType)
                && STRUCTURE_TYPE_WATER_STORAGE.equals(normalizedStructure)) {
                score -= 4;
            } else {
                score -= 10;
            }
        }
        if (!preferredTimeHorizon.isEmpty()) {
            if (preferredTimeHorizon.equals(normalizedTime)) {
                score += 8;
            } else if (TIME_HORIZON_LONG_TERM.equals(preferredTimeHorizon)
                && TIME_HORIZON_IMMEDIATE.equals(normalizedTime)) {
                score -= 10;
            } else if (TIME_HORIZON_IMMEDIATE.equals(preferredTimeHorizon)
                && TIME_HORIZON_LONG_TERM.equals(normalizedTime)) {
                score -= 6;
            }
        }
        if (preferredContentRoles.contains(normalizedRole)) {
            if (CONTENT_ROLE_PLANNING.equals(normalizedRole) || CONTENT_ROLE_STARTER.equals(normalizedRole)) {
                score += 10;
            } else if (CONTENT_ROLE_SAFETY.equals(normalizedRole)) {
                score += 7;
            } else {
                score += 5;
            }
        }
        if (STRUCTURE_TYPE_WATER_STORAGE.equals(preferredStructureType)
            && rowTopics.contains("water_distribution")
            && !rowTopics.contains("container_sanitation")
            && !rowTopics.contains("water_rotation")
            && !explicitTopicTags.contains("water_distribution")) {
            score -= 14;
            if ("building".equals(normalizedCategory)) {
                score -= 4;
            }
            if (CONTENT_ROLE_PLANNING.equals(normalizedRole)) {
                score -= 4;
            }
        }
        if (STRUCTURE_TYPE_WATER_STORAGE.equals(preferredStructureType)
            && !explicitTopicTags.contains("water_distribution")) {
            if (STRUCTURE_TYPE_WATER_STORAGE.equals(normalizedStructure)) {
                if (CONTENT_ROLE_PLANNING.equals(normalizedRole) || CONTENT_ROLE_SUBSYSTEM.equals(normalizedRole)) {
                    score += 6;
                } else if (CONTENT_ROLE_SAFETY.equals(normalizedRole)) {
                    score += 4;
                } else if (CONTENT_ROLE_STARTER.equals(normalizedRole)) {
                    score -= 6;
                }
            }
            if (rowTopics.contains("container_sanitation")) {
                score += 8;
            }
            if (rowTopics.contains("water_rotation")) {
                score += 6;
            }
            if (rowTopics.contains("disinfection")) {
                score += 3;
            }
            if (rowTopics.contains("water_storage")
                && rowTopics.size() == 1
                && CONTENT_ROLE_STARTER.equals(normalizedRole)) {
                score -= 4;
            }
        }
        if (explicitWaterDistribution) {
            if (rowTopics.contains("water_distribution")) {
                score += 12;
                if ("building".equals(normalizedCategory)) {
                    score += 4;
                }
                if ("utility".equals(normalizedCategory)) {
                    score += 4;
                }
                if (CONTENT_ROLE_PLANNING.equals(normalizedRole) || CONTENT_ROLE_SUBSYSTEM.equals(normalizedRole)) {
                    score += 4;
                }
            } else if (rowTopics.contains("container_sanitation")
                || rowTopics.contains("water_rotation")
                || rowTopics.contains("water_storage")) {
                score -= 8;
            }
        }
        if (STRUCTURE_TYPE_MESSAGE_AUTH.equals(preferredStructureType)) {
            if ("communications".equals(normalizedCategory)) {
                score += 6;
            }
            if ("defense".equals(normalizedCategory)) {
                score += rowTopics.contains("message_authentication") || rowTopics.contains("chain_of_custody") ? 3 : -4;
            }
        }
        if (STRUCTURE_TYPE_SAFETY_POISONING.equals(preferredStructureType)) {
            boolean lyeSafetyTagged = rowTopics.contains("lye_safety");
            boolean soapmakingTagged = rowTopics.contains("soapmaking");
            if ("medical".equals(normalizedCategory)) {
                score += 6;
            } else if ("chemistry".equals(normalizedCategory)) {
                score += lyeSafetyTagged ? 4 : 1;
            }
            if (lyeSafetyTagged) {
                score += 8;
            }
            if (soapmakingTagged) {
                score -= 12;
            }
            if (STRUCTURE_TYPE_WOUND_CARE.equals(normalizedStructure)
                || STRUCTURE_TYPE_SANITATION_SYSTEM.equals(normalizedStructure)
                || STRUCTURE_TYPE_SOAPMAKING.equals(normalizedStructure)) {
                score -= 8;
            }
        }
        if (STRUCTURE_TYPE_SOAPMAKING.equals(preferredStructureType)
            && ("crafts".equals(normalizedCategory) || "chemistry".equals(normalizedCategory))
            && CONTENT_ROLE_SUBSYSTEM.equals(normalizedRole)) {
            score += 4;
        }
        if (STRUCTURE_TYPE_SOAPMAKING.equals(preferredStructureType)) {
            boolean soapTagged = rowTopics.contains("soapmaking");
            boolean lyeTagged = rowTopics.contains("lye_safety");
            if ("medical".equals(normalizedCategory)) {
                score -= 8;
            }
            if (STRUCTURE_TYPE_WOUND_CARE.equals(normalizedStructure)
                || STRUCTURE_TYPE_SANITATION_SYSTEM.equals(normalizedStructure)) {
                score -= 8;
            }
            if (soapTagged) {
                score += 10;
            } else {
                score -= 6;
                if ("chemistry".equals(normalizedCategory) && CONTENT_ROLE_SAFETY.equals(normalizedRole)) {
                    score -= 4;
                }
            }
            if (lyeTagged && !soapTagged) {
                score += 2;
            }
        }

        int topicOverlap = 0;
        int topicScore = 0;
        for (String topicTag : preferredTopicTags) {
            if (rowTopics.contains(topicTag)) {
                topicOverlap += 1;
                topicScore += topicWeight(topicTag);
            }
        }
        score += Math.min(20, topicScore);
        if (!preferredStructureType.isEmpty() && topicOverlap == 0 && !"general".equals(normalizedStructure)) {
            score -= 4;
        }

        return score;
    }

    private int topicWeight(String topicTag) {
        boolean hasExplicitTopicFocus = !explicitTopicTags.isEmpty();
        boolean explicitlyRequested = explicitTopicTags.contains(topicTag);
        if (STRUCTURE_TYPE_CABIN_HOUSE.equals(preferredStructureType)) {
            if (hasExplicitTopicFocus) {
                if (explicitlyRequested) {
                    return switch (topicTag) {
                        case "site_selection" -> 10;
                        case "drainage", "foundation" -> 9;
                        case "wall_construction" -> 7;
                        case "roofing", "weatherproofing" -> 9;
                        case "ventilation" -> 5;
                        default -> 4;
                    };
                }
                return switch (topicTag) {
                    case "site_selection", "drainage", "foundation" -> 2;
                    case "wall_construction", "roofing", "weatherproofing" -> 1;
                    case "ventilation" -> 0;
                    default -> 1;
                };
            }
            return switch (topicTag) {
                case "site_selection" -> 8;
                case "drainage", "foundation" -> 7;
                case "wall_construction" -> 5;
                case "roofing" -> 3;
                case "weatherproofing" -> 2;
                case "ventilation" -> 1;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_WATER_STORAGE.equals(preferredStructureType)) {
            if (hasExplicitTopicFocus) {
                if (explicitlyRequested) {
                    return switch (topicTag) {
                        case "container_sanitation" -> 10;
                        case "water_rotation" -> 8;
                        case "water_distribution" -> 9;
                        case "water_storage" -> 7;
                        case "disinfection" -> 6;
                        default -> 4;
                    };
                }
                return 1;
            }
            return switch (topicTag) {
                case "container_sanitation" -> 8;
                case "water_rotation" -> 7;
                case "water_storage" -> 6;
                case "water_distribution" -> 0;
                case "disinfection" -> 4;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_WATER_DISTRIBUTION.equals(preferredStructureType)) {
            if (hasExplicitTopicFocus) {
                if (explicitlyRequested) {
                    return switch (topicTag) {
                        case "water_distribution" -> 10;
                        case "water_storage" -> 6;
                        case "container_sanitation", "water_rotation" -> 2;
                        default -> 4;
                    };
                }
                return switch (topicTag) {
                    case "water_distribution" -> 4;
                    case "water_storage" -> 2;
                    default -> 1;
                };
            }
            return switch (topicTag) {
                case "water_distribution" -> 9;
                case "water_storage" -> 4;
                case "container_sanitation", "water_rotation" -> 1;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_WATER_PURIFICATION.equals(preferredStructureType)) {
            if (hasExplicitTopicFocus) {
                if (explicitlyRequested) {
                    return switch (topicTag) {
                        case "prefilter", "disinfection" -> 9;
                        case "water_purification" -> 8;
                        default -> 4;
                    };
                }
                return 1;
            }
            return switch (topicTag) {
                case "prefilter", "disinfection" -> 7;
                case "water_purification" -> 6;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_SMALL_WATERCRAFT.equals(preferredStructureType)) {
            if (hasExplicitTopicFocus) {
                if (explicitlyRequested) {
                    return switch (topicTag) {
                        case "sealing" -> 10;
                        case "hull" -> 8;
                        case "small_watercraft" -> 7;
                        default -> 4;
                    };
                }
                return 1;
            }
            return 4;
        }
        if (STRUCTURE_TYPE_WOUND_CARE.equals(preferredStructureType)) {
            if (hasExplicitTopicFocus) {
                if (explicitlyRequested) {
                    return switch (topicTag) {
                        case "wound_cleaning" -> 9;
                        case "infection_monitoring" -> 8;
                        case "first_aid" -> 7;
                        default -> 4;
                    };
                }
                return 1;
            }
            return switch (topicTag) {
                case "wound_cleaning" -> 7;
                case "infection_monitoring" -> 6;
                case "first_aid" -> 5;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_SAFETY_POISONING.equals(preferredStructureType)) {
            if (hasExplicitTopicFocus) {
                if (explicitlyRequested) {
                    return switch (topicTag) {
                        case "lye_safety" -> 10;
                        default -> 4;
                    };
                }
                return 1;
            }
            return switch (topicTag) {
                case "lye_safety" -> 8;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_SANITATION_SYSTEM.equals(preferredStructureType)) {
            return switch (topicTag) {
                case "latrine_design" -> 8;
                case "wash_station" -> 6;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_MESSAGE_AUTH.equals(preferredStructureType)) {
            return switch (topicTag) {
                case "message_authentication" -> 9;
                case "chain_of_custody" -> 8;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_FAIR_TRIAL.equals(preferredStructureType)) {
            return switch (topicTag) {
                case "trial_procedure" -> 9;
                case "evidence_rules" -> 8;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_CLAY_OVEN.equals(preferredStructureType)) {
            return switch (topicTag) {
                case "clay_oven" -> 9;
                case "masonry_hearth" -> 8;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_COMMUNITY_SECURITY.equals(preferredStructureType)) {
            return switch (topicTag) {
                case "community_security" -> 9;
                case "resource_security" -> 8;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_COMMUNITY_GOVERNANCE.equals(preferredStructureType)) {
            return switch (topicTag) {
                case "community_governance" -> 9;
                case "conflict_resolution" -> 8;
                case "trust_systems" -> 7;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_SOAPMAKING.equals(preferredStructureType)) {
            return switch (topicTag) {
                case "soapmaking" -> 9;
                case "lye_safety" -> 8;
                default -> 2;
            };
        }
        if (STRUCTURE_TYPE_GLASSMAKING.equals(preferredStructureType)) {
            return switch (topicTag) {
                case "glassmaking" -> 9;
                case "annealing" -> 8;
                default -> 2;
            };
        }
        return 4;
    }

    private int explicitHouseSectionBonus(String sectionHeading) {
        int score = 0;
        String normalizedSection = normalize(sectionHeading);
        boolean explicitRoofWeatherproofFocus = explicitTopicTags.contains("roofing")
            || explicitTopicTags.contains("weatherproofing");
        score += explicitSingleTopicBonus(sectionHeading, "roofing", 18);
        score += explicitSingleTopicBonus(sectionHeading, "weatherproofing", 18);
        score += explicitSingleTopicBonus(sectionHeading, "foundation", 16);
        score += explicitSingleTopicBonus(sectionHeading, "drainage", 14);
        score += explicitSingleTopicBonus(sectionHeading, "site_selection", 16);
        score += explicitSingleTopicBonus(sectionHeading, "wall_construction", 12);
        score += explicitSingleTopicBonus(sectionHeading, "ventilation", 10);

        if (explicitTopicTags.contains("site_selection") && explicitTopicTags.contains("foundation")) {
            if (matchesTopic(sectionHeading, "site_selection")) {
                score += siteSelectionLeadIntent ? 12 : 4;
            }
            if (matchesTopic(sectionHeading, "foundation")) {
                score += siteSelectionLeadIntent ? 4 : 8;
            }
            if (matchesTopic(sectionHeading, "drainage") && !matchesTopic(sectionHeading, "foundation")) {
                if (normalizedSection.contains("french drain")
                    || normalizedSection.contains("swale")
                    || normalizedSection.contains("culvert")
                    || normalizedSection.contains("ditch")) {
                    score -= 18;
                } else {
                    score += 2;
                }
            }
        }
        if (siteBreadthIntent) {
            if (normalizedSection.contains("terrain")
                || normalizedSection.contains("wind exposure")
                || normalizedSection.contains("microclimate")
                || normalizedSection.contains("seasonal")
                || normalizedSection.contains("sun exposure")
                || normalizedSection.contains("shade")
                || normalizedSection.contains("natural hazard")
                || normalizedSection.contains("water proximity")
                || normalizedSection.contains("access")) {
                score += 12;
            }
            if (!matchesTopic(sectionHeading, "site_selection")
                && (normalizedSection.contains("french drain")
                    || normalizedSection.contains("swale")
                    || normalizedSection.contains("culvert")
                    || normalizedSection.contains("road drainage")
                    || normalizedSection.contains("ditch"))) {
                score -= 18;
            }
            if (!matchesTopic(sectionHeading, "site_selection")
                && (normalizedSection.contains("drainage and waterproofing")
                    || normalizedSection.contains("waterproofing")
                    || normalizedSection.contains("water shedding"))) {
                score -= 16;
            }
        }
        if (explicitTopicTags.contains("site_selection")
            && explicitTopicTags.contains("foundation")
            && !explicitTopicTags.contains("roofing")
            && !explicitTopicTags.contains("weatherproofing")
            && normalizedSection.contains("insulation")) {
            score -= 24;
        }

        if (!explicitTopicTags.contains("roofing") && !explicitTopicTags.contains("weatherproofing")) {
            if (explicitTopicTags.contains("wall_construction")) {
                if (matchesTopic(sectionHeading, "site_selection")) {
                    score -= 12;
                }
                if (matchesTopic(sectionHeading, "drainage")) {
                    score -= 10;
                }
                if (matchesTopic(sectionHeading, "foundation")) {
                    score -= 10;
                }
                if (matchesTopic(sectionHeading, "roofing") || matchesTopic(sectionHeading, "weatherproofing")) {
                    score -= 12;
                }
            }
            return score;
        }
        if (matchesTopic(sectionHeading, "site_selection")) {
            score -= 18;
        }
        if (matchesTopic(sectionHeading, "drainage")) {
            score -= 16;
        }
        if (matchesTopic(sectionHeading, "foundation")) {
            score -= 18;
        }
        if (matchesTopic(sectionHeading, "wall_construction")) {
            score -= 10;
        }
        if (normalizedSection.contains("alternative building materials")) {
            score -= 12;
        }
        if (normalizedSection.contains("calculator") || normalizedSection.contains("calculation")) {
            score -= 18;
        }
        if (explicitRoofWeatherproofFocus
            && !matchesTopic(sectionHeading, "roofing")
            && !matchesTopic(sectionHeading, "weatherproofing")
            && (normalizedSection.contains("structural engineering basics")
                || normalizedSection.contains("structural overview")
                || normalizedSection.contains("general engineering")
                || normalizedSection.contains("design loads")
                || normalizedSection.contains("load paths")
                || normalizedSection.contains("mixing ratio")
                || normalizedSection.contains("seismic")
                || normalizedSection.contains("sizing"))) {
            score -= 18;
        }
        return score;
    }

    private int explicitSingleTopicBonus(String sectionHeading, String topicTag, int bonus) {
        if (!explicitTopicTags.contains(topicTag) || !matchesTopic(sectionHeading, topicTag)) {
            return 0;
        }
        return bonus;
    }

}
