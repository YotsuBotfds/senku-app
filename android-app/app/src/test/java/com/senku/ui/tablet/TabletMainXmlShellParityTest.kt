package com.senku.ui.tablet

import com.senku.ui.MainChromeSpec
import com.senku.ui.RailItemSpec
import com.senku.ui.RailShellSpec
import com.senku.ui.SearchChromeSpec
import com.senku.ui.android
import com.senku.ui.assertRailItemTokens
import com.senku.ui.assertRailShell
import com.senku.ui.assertSearchChrome
import com.senku.ui.assertSharedMainChrome
import com.senku.ui.directElementChildren
import com.senku.ui.elementByAndroidId
import com.senku.ui.elementByText
import com.senku.ui.firstElementChild
import com.senku.ui.mainLayout
import com.senku.ui.mainLayoutText
import com.senku.ui.requiredAndroidId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.w3c.dom.Element

class TabletMainXmlShellParityTest {
    @Test
    fun portraitAndLandscapeAppRailKeepExpectedShellMetrics() {
        val portrait = mainLayout("layout-sw600dp-port")
        val landscape = mainLayout("layout-sw600dp-land")

        assertRailShell(
            layout = portrait,
            spec = RailShellSpec(
                railWidth = "72dp",
                firstItemTopMargin = "24dp",
                itemTopMargin = "18dp",
                labelTopMargin = "3dp",
            ),
        )
        assertRailShell(
            layout = landscape,
            spec = RailShellSpec(
                railWidth = "96dp",
                firstItemTopMargin = "18dp",
                itemTopMargin = "12dp",
                labelTopMargin = "4dp",
            ),
        )
    }

    @Test
    fun portraitAndLandscapeAppRailKeepLibraryAskSavedOrderAndTokens() {
        listOf("layout-sw600dp-port", "layout-sw600dp-land").forEach { qualifier ->
            val layout = mainLayout(qualifier)
            val home = layout.elementByAndroidId("phone_nav_home")
            val ask = layout.elementByAndroidId("phone_nav_ask")
            val pins = layout.elementByAndroidId("phone_nav_pins")
            val rail = home.parentNode as Element
            val navItems = rail.directElementChildren("LinearLayout")

            assertEquals(
                "$qualifier should keep the tablet rail in Library, Ask, Saved order",
                listOf("phone_nav_home", "phone_nav_ask", "phone_nav_pins"),
                navItems.map { it.requiredAndroidId() },
            )

            assertRailItemTokens(
                layout = layout,
                spec = RailItemSpec(
                    itemId = "phone_nav_home",
                    labelId = "phone_nav_home_label",
                    iconId = "phone_nav_home_icon",
                    expectedText = "@string/bottom_tab_home",
                    expectedIcon = "@drawable/ic_home_library",
                    expectedTint = "@color/senku_rev03_accent",
                ),
            )
            assertRailItemTokens(
                layout = layout,
                spec = RailItemSpec(
                    itemId = "phone_nav_ask",
                    labelId = "phone_nav_ask_label",
                    iconId = "phone_nav_ask_icon",
                    expectedText = "@string/bottom_tab_ask",
                    expectedIcon = "@drawable/ic_home_ask",
                    expectedTint = "@color/senku_rev03_ink_2",
                ),
            )
            assertRailItemTokens(
                layout = layout,
                spec = RailItemSpec(
                    itemId = "phone_nav_pins",
                    labelId = "phone_nav_pins_label",
                    iconId = "phone_nav_pins_icon",
                    expectedText = "@string/bottom_tab_pins",
                    expectedIcon = "@drawable/ic_home_saved",
                    expectedTint = "@color/senku_rev03_ink_2",
                ),
            )
        }
    }

    @Test
    fun portraitAndLandscapeMainChromeRowsKeepTopbarAlignmentTokens() {
        assertSharedMainChrome(
            layout = mainLayout("layout-sw600dp-port"),
            spec = MainChromeSpec(
                qualifier = "layout-sw600dp-port",
                height = "54dp",
                horizontalPadding = "18dp",
                verticalPadding = "3dp",
                searchActionSize = "48dp",
                searchPadding = "15dp",
            ),
        )
        assertSharedMainChrome(
            layout = mainLayout("layout-sw600dp-land"),
            spec = MainChromeSpec(
                qualifier = "layout-sw600dp-land",
                height = "48dp",
                horizontalPadding = "32dp",
                verticalPadding = "0dp",
                searchActionSize = "48dp",
                searchPadding = "15dp",
            ),
        )
        assertSearchChrome(
            layout = mainLayout("layout-sw600dp-port"),
            spec = SearchChromeSpec(
                qualifier = "layout-sw600dp-port",
                queryMarginStart = "10dp",
                expectedIconTint = "@color/senku_rev03_ink_2",
                expectedCountColor = "@color/senku_rev03_ink_2",
            ),
        )
        assertSearchChrome(
            layout = mainLayout("layout-sw600dp-land"),
            spec = SearchChromeSpec(
                qualifier = "layout-sw600dp-land",
                queryMarginStart = "14dp",
                expectedIconTint = "@color/senku_rev03_accent",
                expectedCountColor = "@color/senku_rev03_accent",
            ),
        )
    }

    @Test
    fun genericTabletSearchChromeMockRowHasSharedTypography() {
        assertSearchChrome(
            layout = mainLayout("layout-sw600dp"),
            spec = SearchChromeSpec(
                qualifier = "layout-sw600dp",
                queryMarginStart = "14dp",
            ),
        )
    }

    @Test
    fun genericTabletSavedShortcutKeepsSavedLabelIconAndActionSemantics() {
        val layout = mainLayout("layout-sw600dp")
        val saved = layout.elementByAndroidId("saved_button")

        assertEquals("@string/bottom_tab_pins", saved.android("text"))
        assertEquals("@drawable/ic_home_saved", saved.android("drawableStart"))
        assertEquals("@string/bottom_tab_pins", saved.android("contentDescription"))
        assertEquals("true", saved.android("clickable"))
        assertEquals("true", saved.android("focusable"))
    }

    @Test
    fun portraitAndLandscapeTabletShellsDoNotUsePlatformMonospaceAlias() {
        listOf("layout-sw600dp-port", "layout-sw600dp-land").forEach { qualifier ->
            val xml = mainLayoutText(qualifier)
            assertFalse("$qualifier should use explicit font resources", xml.contains("android:fontFamily=\"monospace\""))
        }
    }

    @Test
    fun portraitAndLandscapeSearchFilterTypographyStaysDenseAndExplicit() {
        listOf("layout-sw600dp-port", "layout-sw600dp-land").forEach { qualifier ->
            val layout = mainLayout(qualifier)
            val xml = mainLayoutText(qualifier)

            listOf("28sp", "9sp").forEach { size ->
                assertFalse("$qualifier should not use $size text", xml.contains("android:textSize=\"$size\""))
            }

            val expectedFilterColor = if (qualifier == "layout-sw600dp-port") {
                "@color/senku_rev03_ink_3"
            } else {
                "@color/senku_rev03_ink_2"
            }
            val expectedLabelColor = if (qualifier == "layout-sw600dp-port") {
                "@color/senku_rev03_ink_1"
            } else {
                "@color/senku_rev03_ink_0"
            }
            val expectedLabelSize = if (qualifier == "layout-sw600dp-port") "12sp" else "13sp"
            val expectedLabelLineHeight = if (qualifier == "layout-sw600dp-port") "15sp" else "16sp"
            val expectedFilterHeaderSize = if (qualifier == "layout-sw600dp-port") "11sp" else "10sp"

            listOf("FILTER \u2022 CATEGORY", "WINDOW").forEach { text ->
                val header = layout.elementByText(text)
                assertEquals("@font/jetbrains_mono", header.android("fontFamily"))
                assertEquals(expectedFilterHeaderSize, header.android("textSize"))
                assertEquals(expectedFilterColor, header.android("textColor"))
            }

            assertEquals(
                "14sp",
                layout.elementByAndroidId("tablet_search_query_text").android("textSize"),
            )
            assertFalse(
                "$qualifier search count text should use an explicit text size",
                layout.elementByAndroidId("tablet_search_count_text").android("textSize").isBlank(),
            )

            listOf(
                "Shelter (12)",
                "Water (4)",
                "Fire (3)",
                "Survival (8)",
                "Immediate",
                "Short",
                "Long",
                "Mixed",
            ).forEach { text ->
                val label = layout.elementByText(text)
                assertEquals("@font/inter_tight", label.android("fontFamily"))
                assertEquals(expectedLabelSize, label.android("textSize"))
                assertEquals(expectedLabelLineHeight, label.android("lineHeight"))
                assertEquals(expectedLabelColor, label.android("textColor"))
                if (qualifier == "layout-sw600dp-port") {
                    assertEquals("500", label.android("textFontWeight"))
                    assertEquals("10dp", label.android("drawablePadding"))
                }
            }
        }
    }

    @Test
    fun portraitTabletHomeSearchInputUsesLighterUnboxedDensity() {
        val layout = mainLayout("layout-sw600dp-port")
        val input = layout.elementByAndroidId("search_input")
        val shell = input.parentNode as Element
        val icon = shell.firstElementChild("ImageView")

        assertEquals("48dp", shell.android("layout_height"))
        assertEquals("14dp", shell.android("layout_marginTop"))
        assertEquals("@color/senku_rev03_bg_0", shell.android("background"))
        assertEquals("12dp", shell.android("paddingStart"))
        assertEquals("8dp", shell.android("paddingTop"))
        assertEquals("12dp", shell.android("paddingEnd"))
        assertEquals("8dp", shell.android("paddingBottom"))
        assertEquals("18dp", icon.android("layout_width"))
        assertEquals("18dp", icon.android("layout_height"))
        assertEquals("3dp", icon.android("padding"))
        assertEquals("@color/senku_rev03_ink_2", icon.android("tint"))
        assertEquals("8dp", input.android("layout_marginStart"))
        assertEquals("@font/inter_tight", input.android("fontFamily"))
        assertEquals("500", input.android("textFontWeight"))
        assertEquals("18sp", input.android("lineHeight"))
        assertEquals("14sp", input.android("textSize"))
    }

    @Test
    fun portraitTabletHomeColumnsKeepBalancedCategoryAndRecentRhythm() {
        val layout = mainLayout("layout-sw600dp-port")
        val categoryHeader = layout.elementByAndroidId("category_section_header")
        val categoryColumn = categoryHeader.parentNode as Element
        val categoryContainer = layout.elementByAndroidId("category_section_container")
        val recentSection = layout.elementByAndroidId("recent_threads_section")
        val recentContainer = layout.elementByAndroidId("recent_threads_container")

        assertEquals("0dp", categoryColumn.android("layout_width"))
        assertEquals("12dp", categoryColumn.android("layout_marginEnd"))
        assertEquals("1.02", categoryColumn.android("layout_weight"))
        assertEquals("4dp", categoryContainer.android("layout_marginTop"))
        assertEquals("0dp", recentSection.android("layout_width"))
        assertEquals("0.9", recentSection.android("layout_weight"))
        assertEquals("4dp", recentContainer.android("layout_marginTop"))
    }

}
