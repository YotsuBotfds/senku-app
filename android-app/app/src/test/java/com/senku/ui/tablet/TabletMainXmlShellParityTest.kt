package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class TabletMainXmlShellParityTest {
    @Test
    fun portraitAndLandscapeAppRailKeepExpectedShellMetrics() {
        val portrait = layout("layout-sw600dp-port")
        val landscape = layout("layout-sw600dp-land")

        assertRailShell(
            layout = portrait,
            railWidth = "72dp",
            firstItemTopMargin = "24dp",
            itemTopMargin = "18dp",
            labelTopMargin = "3dp",
        )
        assertRailShell(
            layout = landscape,
            railWidth = "96dp",
            firstItemTopMargin = "18dp",
            itemTopMargin = "12dp",
            labelTopMargin = "4dp",
        )
    }

    @Test
    fun portraitAndLandscapeAppRailKeepLibraryAskSavedOrderAndTokens() {
        listOf("layout-sw600dp-port", "layout-sw600dp-land").forEach { qualifier ->
            val layout = layout(qualifier)
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
                itemId = "phone_nav_home",
                labelId = "phone_nav_home_label",
                iconId = "phone_nav_home_icon",
                expectedText = "@string/bottom_tab_home",
                expectedIcon = "@drawable/ic_home_library",
                expectedTint = "@color/senku_rev03_accent",
            )
            assertRailItemTokens(
                layout = layout,
                itemId = "phone_nav_ask",
                labelId = "phone_nav_ask_label",
                iconId = "phone_nav_ask_icon",
                expectedText = "@string/bottom_tab_ask",
                expectedIcon = "@drawable/ic_home_ask",
                expectedTint = "@color/senku_rev03_ink_2",
            )
            assertRailItemTokens(
                layout = layout,
                itemId = "phone_nav_pins",
                labelId = "phone_nav_pins_label",
                iconId = "phone_nav_pins_icon",
                expectedText = "@string/bottom_tab_pins",
                expectedIcon = "@drawable/ic_home_saved",
                expectedTint = "@color/senku_rev03_ink_2",
            )
        }
    }

    @Test
    fun portraitAndLandscapeMainChromeRowsKeepTopbarAlignmentTokens() {
        assertMainChromeRow(
            qualifier = "layout-sw600dp-port",
            height = "54dp",
            horizontalPadding = "18dp",
        )
        assertMainChromeRow(
            qualifier = "layout-sw600dp-land",
            height = "48dp",
            horizontalPadding = "32dp",
        )
        assertSearchChromeRow(
            qualifier = "layout-sw600dp-port",
            height = "54dp",
            horizontalPadding = "18dp",
        )
        assertSearchChromeRow(
            qualifier = "layout-sw600dp-land",
            height = "48dp",
            horizontalPadding = "32dp",
        )
    }

    @Test
    fun portraitAndLandscapeTabletShellsDoNotUsePlatformMonospaceAlias() {
        listOf("layout-sw600dp-port", "layout-sw600dp-land").forEach { qualifier ->
            val xml = layoutFile(qualifier).readText()
            assertFalse("$qualifier should use explicit font resources", xml.contains("android:fontFamily=\"monospace\""))
        }
    }

    @Test
    fun portraitAndLandscapeSearchFilterTypographyStaysDenseAndExplicit() {
        listOf("layout-sw600dp-port", "layout-sw600dp-land").forEach { qualifier ->
            val layout = layout(qualifier)
            val xml = layoutFile(qualifier).readText()

            listOf("15sp", "28sp", "9sp").forEach { size ->
                assertFalse("$qualifier should not use $size text", xml.contains("android:textSize=\"$size\""))
            }

            listOf("FILTER \u2022 CATEGORY", "WINDOW").forEach { text ->
                val header = layout.elementByText(text)
                assertEquals("@font/jetbrains_mono", header.android("fontFamily"))
                assertEquals("10sp", header.android("textSize"))
            }

            assertEquals("13sp", layout.elementByAndroidId("tablet_search_query_text").android("textSize"))
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
                assertEquals("13sp", label.android("textSize"))
                assertEquals("16sp", label.android("lineHeight"))
            }
        }
    }

    private fun assertMainChromeRow(
        qualifier: String,
        height: String,
        horizontalPadding: String,
    ) {
        val layout = layout(qualifier)
        val row = layout.elementByAndroidId("home_chrome_row")
        val back = layout.elementByAndroidId("home_chrome_back_button")
        val backIcon = layout.elementByAndroidId("home_chrome_back_icon")
        val backLabel = layout.elementByAndroidId("home_chrome_back_label")
        val divider = layout.elementByAndroidId("home_chrome_divider")
        val mode = layout.elementByAndroidId("home_chrome_mode")
        val title = layout.elementByAndroidId("home_chrome_title")
        val children = row.directElementChildren()
        val searchIcon = layout.elementByAndroidId("home_chrome_search_icon")
        val overflow = children[5]
        val bottomRule = layout.elementByAndroidId("home_chrome_bottom_rule")
        val verticalPadding = if (height == "48dp") "9dp" else "12dp"

        assertEquals("$qualifier topbar height", height, row.android("layout_height"))
        assertEquals("$qualifier topbar start padding", horizontalPadding, row.android("paddingStart"))
        assertEquals("$qualifier topbar end padding", horizontalPadding, row.android("paddingEnd"))
        assertEquals("$qualifier topbar top padding", verticalPadding, row.android("paddingTop"))
        assertEquals("$qualifier topbar bottom padding", verticalPadding, row.android("paddingBottom"))
        assertEquals(
            "$qualifier topbar child token order",
            listOf("LinearLayout", "View", "TextView", "TextView", "ImageView", "TextView"),
            children.map { it.tagName },
        )

        assertEquals("60dp", back.android("layout_width"))
        assertEquals("28dp", back.android("layout_height"))
        assertEquals("@string/detail_back_content_description", back.android("contentDescription"))
        assertEquals("horizontal", back.android("orientation"))
        assertEquals("center", back.android("gravity"))
        assertEquals("6dp", back.android("paddingStart"))
        assertEquals("6dp", back.android("paddingEnd"))
        assertEquals("14dp", backIcon.android("layout_width"))
        assertEquals("14dp", backIcon.android("layout_height"))
        assertEquals("@drawable/ic_home_back_chevron", backIcon.android("src"))
        assertEquals("@color/senku_rev03_ink_0", backIcon.android("tint"))
        assertEquals("@string/detail_back", backLabel.android("text"))
        assertEquals("2dp", backLabel.android("layout_marginStart"))
        assertEquals("@font/jetbrains_mono", backLabel.android("fontFamily"))
        assertEquals("10sp", backLabel.android("textSize"))
        assertEquals("500", backLabel.android("textFontWeight"))
        assertEquals("12sp", backLabel.android("lineHeight"))
        assertEquals("0.09", backLabel.android("letterSpacing"))

        assertEquals("1dp", divider.android("layout_width"))
        assertEquals("24dp", divider.android("layout_height"))
        assertEquals("10dp", divider.android("layout_marginStart"))
        assertEquals("10dp", divider.android("layout_marginEnd"))

        assertEquals("wrap_content", mode.android("layout_width"))
        assertEquals("@style/TextAppearance.Senku.Rev03.MonoCaps", mode.android("textAppearance"))
        assertEquals("HOME SENKU", mode.android("text"))
        assertEquals("@color/senku_rev03_accent", mode.android("textColor"))
        assertEquals("500", mode.android("textFontWeight"))
        assertEquals("", mode.android("textStyle"))

        assertEquals("0dp", title.android("layout_width"))
        assertEquals("10dp", title.android("layout_marginStart"))
        assertEquals("1", title.android("layout_weight"))
        assertEquals("1", title.android("maxLines"))
        assertEquals("end", title.android("ellipsize"))
        assertEquals("@font/inter_tight", title.android("fontFamily"))
        assertEquals("17sp", title.android("textSize"))
        assertEquals("600", title.android("textFontWeight"))
        assertEquals("20sp", title.android("lineHeight"))
        assertEquals("@color/senku_rev03_ink_0", title.android("textColor"))
        assertEquals("Field manual \u2022 ed.2", title.android("text"))

        assertEquals("16dp", searchIcon.android("layout_width"))
        assertEquals("16dp", searchIcon.android("layout_height"))
        assertEquals("@drawable/ic_search_magnifier", searchIcon.android("src"))
        assertEquals("...", overflow.android("text"))
        assertEquals("1dp", bottomRule.android("layout_height"))
        assertEquals("@color/senku_rev03_hairline_strong", bottomRule.android("background"))
    }

    private fun assertSearchChromeRow(
        qualifier: String,
        height: String,
        horizontalPadding: String,
    ) {
        val layout = layout(qualifier)
        val row = layout.elementByAndroidId("tablet_search_topbar_row")
        val query = layout.elementByAndroidId("tablet_search_query_text")
        val children = row.directElementChildren()
        val searchIcon = children[0]
        val count = children[2]
        val bottomRule = layout.elementByAndroidId("tablet_search_bottom_rule")
        val verticalPadding = if (height == "48dp") "9dp" else "12dp"

        assertEquals("$qualifier search topbar height", height, row.android("layout_height"))
        assertEquals("$qualifier search topbar start padding", horizontalPadding, row.android("paddingStart"))
        assertEquals("$qualifier search topbar end padding", horizontalPadding, row.android("paddingEnd"))
        assertEquals("$qualifier search topbar top padding", verticalPadding, row.android("paddingTop"))
        assertEquals("$qualifier search topbar bottom padding", verticalPadding, row.android("paddingBottom"))
        assertEquals(
            "$qualifier search topbar child token order",
            listOf("ImageView", "TextView", "TextView"),
            children.map { it.tagName },
        )
        assertEquals("20dp", searchIcon.android("layout_width"))
        assertEquals("20dp", searchIcon.android("layout_height"))
        assertEquals("@drawable/ic_search_magnifier", searchIcon.android("src"))
        assertEquals("0dp", query.android("layout_width"))
        assertEquals("14dp", query.android("layout_marginStart"))
        assertEquals("1", query.android("layout_weight"))
        assertEquals("1", query.android("maxLines"))
        assertEquals("end", query.android("ellipsize"))
        assertEquals("@font/jetbrains_mono", query.android("fontFamily"))
        assertEquals("13sp", query.android("textSize"))
        assertEquals("18sp", query.android("lineHeight"))
        assertEquals("@font/jetbrains_mono", count.android("fontFamily"))
        assertEquals("12sp", count.android("textSize"))
        assertEquals("16sp", count.android("lineHeight"))
        assertEquals("1dp", bottomRule.android("layout_height"))
        assertEquals("@color/senku_rev03_hairline_strong", bottomRule.android("background"))
    }

    private fun assertRailShell(
        layout: Element,
        railWidth: String,
        firstItemTopMargin: String,
        itemTopMargin: String,
        labelTopMargin: String,
    ) {
        val home = layout.elementByAndroidId("phone_nav_home")
        val ask = layout.elementByAndroidId("phone_nav_ask")
        val pins = layout.elementByAndroidId("phone_nav_pins")
        val rail = home.parentNode as Element
        val badge = rail.firstElementChild("TextView")

        assertEquals(railWidth, rail.android("layout_width"))
        assertEquals("36dp", badge.android("layout_width"))
        assertEquals("36dp", badge.android("layout_height"))
        assertEquals("@font/inter_tight", badge.android("fontFamily"))
        assertEquals(firstItemTopMargin, home.android("layout_marginTop"))
        assertEquals(itemTopMargin, ask.android("layout_marginTop"))
        assertEquals(itemTopMargin, pins.android("layout_marginTop"))

        listOf("phone_nav_home_icon", "phone_nav_ask_icon", "phone_nav_pins_icon").forEach { id ->
            val icon = layout.elementByAndroidId(id)
            assertEquals("@dimen/senku_rev03_nav_rail_icon", icon.android("layout_width"))
            assertEquals("@dimen/senku_rev03_nav_rail_icon", icon.android("layout_height"))
        }

        listOf("phone_nav_home_label", "phone_nav_ask_label", "phone_nav_pins_label").forEach { id ->
            val label = layout.elementByAndroidId(id)
            assertEquals(labelTopMargin, label.android("layout_marginTop"))
            assertEquals("@font/inter_tight", label.android("fontFamily"))
        }
    }

    private fun assertRailItemTokens(
        layout: Element,
        itemId: String,
        labelId: String,
        iconId: String,
        expectedText: String,
        expectedIcon: String,
        expectedTint: String,
    ) {
        val item = layout.elementByAndroidId(itemId)
        val label = layout.elementByAndroidId(labelId)
        val icon = layout.elementByAndroidId(iconId)

        assertEquals(expectedText, item.android("contentDescription"))
        assertEquals(expectedText, label.android("text"))
        assertEquals(expectedText, icon.android("contentDescription"))
        assertEquals(expectedIcon, icon.android("src"))
        assertEquals(expectedTint, icon.android("tint"))
        assertEquals(expectedTint, label.android("textColor"))
    }

    private fun layout(qualifier: String): Element =
        DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(layoutFile(qualifier))
            .documentElement

    private fun layoutFile(qualifier: String): File =
        locateFile(
            "android-app/app/src/main/res/$qualifier/activity_main.xml",
            "app/src/main/res/$qualifier/activity_main.xml",
        )

    private fun Element.elementByAndroidId(id: String): Element {
        val nodes = getElementsByTagName("*")
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as Element
            if (element.android("id") == "@+id/$id" || element.android("id") == "@id/$id") {
                return element
            }
        }
        error("Unable to locate @$id")
    }

    private fun Element.firstElementChild(tagName: String): Element {
        for (index in 0 until childNodes.length) {
            val node = childNodes.item(index)
            if (node is Element && node.tagName == tagName) {
                return node
            }
        }
        error("Unable to locate child <$tagName>")
    }

    private fun Element.directElementChildren(tagName: String): List<Element> =
        (0 until childNodes.length)
            .map { childNodes.item(it) }
            .filterIsInstance<Element>()
            .filter { it.tagName == tagName }

    private fun Element.directElementChildren(): List<Element> =
        (0 until childNodes.length)
            .map { childNodes.item(it) }
            .filterIsInstance<Element>()

    private fun Element.elementByText(text: String): Element {
        val nodes = getElementsByTagName("*")
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as Element
            if (element.android("text") == text) {
                return element
            }
        }
        error("Unable to locate element with text $text")
    }

    private fun Element.android(name: String): String =
        getAttributeNS("http://schemas.android.com/apk/res/android", name)

    private fun Element.requiredAndroidId(): String =
        android("id").removePrefix("@+id/").removePrefix("@id/")

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
        error("Unable to locate tablet XML layout from ${start.path}")
    }
}
