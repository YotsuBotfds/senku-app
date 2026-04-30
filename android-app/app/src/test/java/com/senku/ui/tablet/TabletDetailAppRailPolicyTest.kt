package com.senku.ui.tablet

import com.senku.ui.primitives.Rev03ComposeNavRailIconSizeDp
import com.senku.ui.primitives.Rev03ComposeNavRailLabelFontSizeSp
import com.senku.ui.primitives.Rev03ComposeNavRailLabelLineHeightSp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

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
    fun tabletGuideTopbarBackAffordanceKeepsCompactWidthAndVisibleLabel() {
        val source = locateFile(
            "android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt",
            "app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt",
        ).readText()

        assertSourceContains(source, ".height(28.dp)")
        assertSourceContains(source, ".widthIn(min = 54.dp, max = 60.dp)")
        assertSourceContains(source, "contentDescription = \"Back to previous screen\"")
        assertSourceContains(source, "Modifier.padding(horizontal = 6.dp)")
        assertSourceContains(source, "horizontalArrangement = Arrangement.spacedBy(2.dp)")
        assertSourceContains(source, "text = \"Back\"")
        assertSourceContains(source, "fontSize = 10.sp")
        assertSourceContains(source, "lineHeight = 12.sp")
        assertSourceContains(source, "overflow = TextOverflow.Clip")
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

    private fun assertSourceContains(source: String, token: String) {
        assertEquals("Expected TabletDetailScreen.kt to contain `$token`", true, source.contains(token))
    }

    private fun locateFile(vararg candidates: String): File {
        val userDir = requireNotNull(System.getProperty("user.dir")) { "user.dir is not set" }
        val start = File(userDir).absoluteFile
        val roots = generateSequence(start) { root -> root.parentFile }
        for (root in roots) {
            for (candidate in candidates) {
                val file = File(root, candidate)
                if (file.exists()) {
                    return file
                }
            }
        }
        error("Unable to locate tablet detail source from ${start.path}")
    }
}
