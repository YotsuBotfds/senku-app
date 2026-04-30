package com.senku.mobile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PackRouteSignalPolicyTest {
    @Test
    public void waterDistributionSignalsKeepTitleDetailAndStrongGuideChecks() {
        SearchResult titleSignal = result(
            "Community Water Distribution Systems",
            "Gravity-Fed Distribution Systems",
            "",
            "",
            "building",
            "route-focus",
            "planning",
            "water_distribution",
            "water_distribution"
        );
        SearchResult detailOnlySignal = result(
            "Settlement Utility Planning",
            "Planning",
            "Place the storage tank above household taps and protect the spring box.",
            "Place the storage tank above household taps and protect the spring box.",
            "building",
            "route-focus",
            "planning",
            "water_distribution",
            "water_distribution"
        );
        SearchResult weakGuideTitle = result(
            "Water System Notes",
            "",
            "",
            "",
            "utility",
            "guide-focus",
            "reference",
            "general",
            "water_distribution"
        );

        assertTrue(PackRouteSignalPolicy.hasWaterDistributionTitleSignal(titleSignal));
        assertTrue(PackRouteSignalPolicy.hasStrongWaterDistributionGuideSignal(titleSignal));
        assertFalse(PackRouteSignalPolicy.hasWaterDistributionTitleSignal(detailOnlySignal));
        assertTrue(PackRouteSignalPolicy.hasWaterDistributionDetailSignal(detailOnlySignal));
        assertFalse(PackRouteSignalPolicy.hasStrongWaterDistributionGuideSignal(weakGuideTitle));
    }

    @Test
    public void roofWeatherproofSignalsUseSharedAnchorAndDistractorMarkers() {
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "how do i waterproof a roof with no tar or shingles"
        );
        SearchResult anchor = result(
            "Roofing Systems",
            "Rainproofing and Water Shedding",
            "",
            "",
            "building",
            "route-focus",
            "subsystem",
            "cabin_house",
            "roofing,weatherproofing"
        );
        SearchResult distractor = result(
            "Construction & Carpentry",
            "Structural Engineering Basics",
            "",
            "",
            "building",
            "route-focus",
            "starter",
            "cabin_house",
            "foundation"
        );

        assertTrue(PackRouteSignalPolicy.prefersRoofWeatherproofContext(metadataProfile));
        assertTrue(PackRouteSignalPolicy.hasRoofWeatherproofAnchorSignal(anchor));
        assertFalse(PackRouteSignalPolicy.hasRoofWeatherproofDistractorSignal(anchor));
        assertTrue(PackRouteSignalPolicy.hasRoofWeatherproofDistractorSignal(distractor));
        assertFalse(PackRouteSignalPolicy.hasRoofWeatherproofAnchorSignal(distractor));
    }

    @Test
    public void governanceSignalsKeepTrustRepairAndSupportMixChecks() {
        QueryMetadataProfile metadataProfile = QueryMetadataProfile.fromQuery(
            "How do we merge with another group if we don't trust each other yet?"
        );
        SearchResult trustRepair = result(
            "Community Governance",
            "Trust Repair and Mediation",
            "",
            "",
            "community",
            "route-focus",
            "planning",
            "community_governance",
            "governance"
        );
        SearchResult supportMixDistractor = result(
            "Community Governance",
            "Membership Monitoring and Graduated Sanctions",
            "",
            "",
            "community",
            "route-focus",
            "planning",
            "community_governance",
            "governance"
        );

        assertTrue(PackRouteSignalPolicy.prefersGovernanceTrustRepairContext(metadataProfile));
        assertTrue(PackRouteSignalPolicy.hasGovernanceTrustRepairSignal(trustRepair));
        assertFalse(PackRouteSignalPolicy.hasGovernanceSupportMixDistractor(trustRepair));
        assertTrue(PackRouteSignalPolicy.hasGovernanceSupportMixDistractor(supportMixDistractor));
        assertFalse(PackRouteSignalPolicy.hasGovernanceTrustRepairSignal(supportMixDistractor));
    }

    @Test
    public void soapmakingStrongGuideRequiresProcessHeadingAndRejectsGenericChemistry() {
        SearchResult dedicatedSoap = result(
            "Everyday Compounds and Production",
            "Soap Making - Cold Process",
            "Render fat, prepare lye water, mix to trace, and cure soap bars.",
            "Render fat, prepare lye water, mix to trace, and cure soap bars.",
            "crafts",
            "route-focus",
            "subsystem",
            "soapmaking",
            "soapmaking,lye_safety"
        );
        SearchResult genericChemistry = result(
            "Chemistry Fundamentals",
            "Acids and Bases",
            "Render fat, prepare lye water, mix to trace, and cure soap bars.",
            "Render fat, prepare lye water, mix to trace, and cure soap bars.",
            "chemistry",
            "route-focus",
            "safety",
            "soapmaking",
            "soapmaking,lye_safety"
        );

        assertTrue(PackRouteSignalPolicy.hasStrongSoapmakingGuideSignal(dedicatedSoap));
        assertFalse(PackRouteSignalPolicy.hasStrongSoapmakingGuideSignal(genericChemistry));
    }

    private static SearchResult result(
        String title,
        String sectionHeading,
        String snippet,
        String body,
        String category,
        String retrievalMode,
        String contentRole,
        String structureType,
        String topicTags
    ) {
        return new SearchResult(
            title,
            "",
            snippet,
            body,
            "GD-TEST",
            sectionHeading,
            category,
            retrievalMode,
            contentRole,
            "long_term",
            structureType,
            topicTags
        );
    }
}
