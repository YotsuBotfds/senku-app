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
    fun tabletGuideTopbarLocksAlignmentAndTitleTypeTokens() {
        val portraitPolicy = tabletGuideChromePolicy(isLandscape = false)
        val landscapePolicy = tabletGuideChromePolicy(isLandscape = true)

        assertEquals(18, tabletChromeHeaderHorizontalPaddingDp(isLandscape = false))
        assertEquals(32, tabletChromeHeaderHorizontalPaddingDp(isLandscape = true))
        assertEquals(54, tabletChromeHeaderHeightDp(isLandscape = false))
        assertEquals(48, tabletChromeHeaderHeightDp(isLandscape = true))
        assertEquals(18, portraitPolicy.topBarHorizontalPaddingDp)
        assertEquals(32, landscapePolicy.topBarHorizontalPaddingDp)
        assertEquals(14, portraitPolicy.topBarTitleFontSizeSp)
        assertEquals(18, portraitPolicy.topBarTitleLineHeightSp)
        assertEquals(14, landscapePolicy.topBarTitleFontSizeSp)
        assertEquals(18, landscapePolicy.topBarTitleLineHeightSp)
    }

    @Test
    fun tabletGuideTopbarBackAffordanceUsesSharedIconOnlyChrome() {
        val policy = tabletDetailBackActionPolicy()

        assertEquals(48, policy.widthDp)
        assertEquals(48, policy.heightDp)
        assertEquals(18, policy.iconSizeDp)
        assertEquals("Back to previous screen", policy.contentDescription)
        assertFalse(policy.showsTextLabel)
        assertEquals(policy.widthDp, tabletDetailBackActionWidthDp())
        assertEquals(policy.iconSizeDp, tabletDetailBackActionIconSizeDp())
    }

    @Test
    fun tabletTitlebarUsesOneSharedTitleScaleAcrossModes() {
        val portraitPolicy = tabletGuideChromePolicy(isLandscape = false)
        val landscapePolicy = tabletGuideChromePolicy(isLandscape = true)
        val portraitTitleType = tabletTitleBarTitleTypePolicy(isLandscape = false)
        val landscapeTitleType = tabletTitleBarTitleTypePolicy(isLandscape = true)

        assertEquals(portraitPolicy.topBarTitleFontSizeSp, portraitTitleType.fontSizeSp)
        assertEquals(portraitPolicy.topBarTitleLineHeightSp, portraitTitleType.lineHeightSp)
        assertEquals(landscapePolicy.topBarTitleFontSizeSp, landscapeTitleType.fontSizeSp)
        assertEquals(landscapePolicy.topBarTitleLineHeightSp, landscapeTitleType.lineHeightSp)
        assertEquals(portraitTitleType, landscapeTitleType)
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

    @Test
    fun tabletDetailAppRailLibraryActionUsesLibraryNavigationCallbackOnly() {
        val calls = mutableListOf<String>()

        tabletDetailAppRailDispatchAction(
            TabletDetailAppRailAction.Library,
            onLibraryClick = { calls += "library" },
            onAskClick = { calls += "ask" },
            onSavedClick = { calls += "saved" },
        )

        assertEquals(listOf("library"), calls)
    }

}
