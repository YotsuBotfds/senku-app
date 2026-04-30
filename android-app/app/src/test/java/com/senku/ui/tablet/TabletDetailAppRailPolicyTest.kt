package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Test

class TabletDetailAppRailPolicyTest {
    @Test
    fun tabletDetailAppRailDimensionsMatchXmlShells() {
        assertEquals(72, tabletGuideAppRailWidthDp(isLandscape = false))
        assertEquals(96, tabletGuideAppRailWidthDp(isLandscape = true))
        assertEquals(36, tabletGuideAppRailBadgeHeightDp(isLandscape = false))
        assertEquals(36, tabletGuideAppRailBadgeHeightDp(isLandscape = true))
        assertEquals(24, tabletGuideAppRailFirstItemTopMarginDp(isLandscape = false))
        assertEquals(18, tabletGuideAppRailFirstItemTopMarginDp(isLandscape = true))
        assertEquals(18, tabletGuideAppRailItemTopMarginDp(isLandscape = false))
        assertEquals(12, tabletGuideAppRailItemTopMarginDp(isLandscape = true))
        assertEquals(3, tabletGuideAppRailLabelTopMarginDp(isLandscape = false))
        assertEquals(4, tabletGuideAppRailLabelTopMarginDp(isLandscape = true))
    }

    @Test
    fun tabletDetailAppRailActiveDestinationFollowsDetailMode() {
        assertEquals(
            TabletDetailAppRailDestination.Library,
            tabletDetailAppRailActiveDestination(TabletDetailMode.Guide),
        )
        assertEquals(
            TabletDetailAppRailDestination.Ask,
            tabletDetailAppRailActiveDestination(TabletDetailMode.Answer),
        )
        assertEquals(
            TabletDetailAppRailDestination.Ask,
            tabletDetailAppRailActiveDestination(TabletDetailMode.Thread),
        )
    }

    @Test
    fun tabletDetailAppRailSavedActionUsesSavedNavigationCallbackOnly() {
        val calls = mutableListOf<String>()

        tabletDetailAppRailDispatchAction(
            TabletDetailAppRailAction.Saved,
            onLibraryClick = { calls += "library" },
            onAskClick = { calls += "ask" },
            onSavedClick = { calls += "saved" },
        )

        assertEquals(listOf("saved"), calls)
    }

    @Test
    fun tabletDetailAppRailAskActionUsesAskNavigationCallbackOnly() {
        val calls = mutableListOf<String>()

        tabletDetailAppRailDispatchAction(
            TabletDetailAppRailAction.Ask,
            onLibraryClick = { calls += "library" },
            onAskClick = { calls += "ask" },
            onSavedClick = { calls += "saved" },
        )

        assertEquals(listOf("ask"), calls)
    }
}
