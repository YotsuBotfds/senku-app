package com.senku.ui.tablet

import com.senku.ui.primitives.Rev03ComposeNavRailIconSizeDp
import com.senku.ui.primitives.Rev03ComposeNavRailLabelFontSizeSp
import com.senku.ui.primitives.Rev03ComposeNavRailLabelLineHeightSp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals(22, tabletGuideAppRailIconSizeDp(isLandscape = false))
        assertEquals(22, tabletGuideAppRailIconSizeDp(isLandscape = true))
        assertEquals(10, tabletGuideAppRailLabelFontSizeSp(isLandscape = false))
        assertEquals(10, tabletGuideAppRailLabelFontSizeSp(isLandscape = true))
        assertEquals(13, tabletGuideAppRailLabelLineHeightSp(isLandscape = false))
        assertEquals(13, tabletGuideAppRailLabelLineHeightSp(isLandscape = true))
        assertEquals(0, tabletGuideAppRailLabelLetterSpacingSp())
    }

    @Test
    fun tabletDetailAppRailReusesComposeNavRailVisualTokens() {
        listOf(false, true).forEach { isLandscape ->
            assertEquals(Rev03ComposeNavRailIconSizeDp, tabletGuideAppRailIconSizeDp(isLandscape))
            assertEquals(Rev03ComposeNavRailLabelFontSizeSp, tabletGuideAppRailLabelFontSizeSp(isLandscape))
            assertEquals(Rev03ComposeNavRailLabelLineHeightSp, tabletGuideAppRailLabelLineHeightSp(isLandscape))
        }
    }

    @Test
    fun tabletDetailAppRailDestinationsKeepLibraryAskSavedOrder() {
        assertEquals(
            listOf(
                TabletDetailAppRailDestination.Library,
                TabletDetailAppRailDestination.Ask,
                TabletDetailAppRailDestination.Saved,
            ),
            TabletDetailAppRailDestination.entries,
        )
        assertEquals(
            listOf(
                TabletDetailAppRailAction.Library,
                TabletDetailAppRailAction.Ask,
                TabletDetailAppRailAction.Saved,
            ),
            TabletDetailAppRailAction.entries,
        )
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
    fun tabletPortraitDoesNotExposeDuplicateGuideRailModes() {
        assertEquals(72, tabletGuideAppRailWidthDp(isLandscape = false))
        assertEquals(196, tabletThreadRailWidthDp(isLandscape = false, guideMode = true))
        assertEquals(0, tabletGuideReferenceRailWidthDp(isLandscape = false))
        assertFalse(tabletGuideSectionRailShowsToolbar())
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
