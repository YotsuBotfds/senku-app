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
            railWidth = "88dp",
            firstItemTopMargin = "24dp",
            itemTopMargin = "18dp",
            labelTopMargin = "3dp",
        )
        assertRailShell(
            layout = landscape,
            railWidth = "112dp",
            firstItemTopMargin = "18dp",
            itemTopMargin = "12dp",
            labelTopMargin = "4dp",
        )
    }

    @Test
    fun portraitAndLandscapeHomeShellMetricsMatchMockProportions() {
        val portrait = layout("layout-sw600dp-port")
        val landscape = layout("layout-sw600dp-land")

        assertHomeWorkspace(
            layout = portrait,
            chromeGutter = "22dp",
            scrollGutterStart = "22dp",
            scrollGutterEnd = "22dp",
            searchHeight = "46dp",
            searchTopMargin = "14dp",
            searchHorizontalPadding = "20dp",
            searchIconSize = "32dp",
            searchIconPadding = "8dp",
            categoryRecentGap = "18dp",
            recentWeight = "0.92",
        )
        assertHomeWorkspace(
            layout = landscape,
            chromeGutter = "34dp",
            scrollGutterStart = "48dp",
            scrollGutterEnd = "30dp",
            searchHeight = "56dp",
            searchTopMargin = "26dp",
            searchHorizontalPadding = "20dp",
            searchIconSize = "38dp",
            searchIconPadding = "10dp",
            categoryRecentGap = "28dp",
            recentWeight = "1",
        )
    }

    @Test
    fun portraitAndLandscapeSearchColumnMetricsMatchMockProportions() {
        val portrait = layout("layout-sw600dp-port")
        val landscape = layout("layout-sw600dp-land")

        assertSearchWorkspace(
            layout = portrait,
            queryBarHeight = "34dp",
            queryBarGutter = "22dp",
            filterWidth = "192dp",
            filterPaddingStart = "18dp",
            filterPaddingTop = "12dp",
            filterPaddingEnd = "14dp",
            resultListMargin = "16dp",
            previewWidth = "236dp",
            previewVisibility = "gone",
        )
        assertSearchWorkspace(
            layout = landscape,
            queryBarHeight = "48dp",
            queryBarGutter = "28dp",
            filterWidth = "254dp",
            filterPaddingStart = "18dp",
            filterPaddingTop = "16dp",
            filterPaddingEnd = "16dp",
            resultListMargin = "20dp",
            previewWidth = "404dp",
            previewVisibility = "",
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

    private fun assertHomeWorkspace(
        layout: Element,
        chromeGutter: String,
        scrollGutterStart: String,
        scrollGutterEnd: String,
        searchHeight: String,
        searchTopMargin: String,
        searchHorizontalPadding: String,
        searchIconSize: String,
        searchIconPadding: String,
        categoryRecentGap: String,
        recentWeight: String,
    ) {
        val chrome = layout.elementByAndroidId("home_chrome_title").parentNode as Element
        val scroll = layout.elementByAndroidId("browse_scroll_view")
        val searchInput = layout.elementByAndroidId("search_input")
        val searchShell = searchInput.parentNode as Element
        val searchIcon = searchShell.firstElementChild("ImageView")
        val categoriesColumn = layout.elementByAndroidId("category_section_header").parentNode as Element
        val recent = layout.elementByAndroidId("recent_threads_section")

        assertEquals(chromeGutter, chrome.android("paddingStart"))
        assertEquals(chromeGutter, chrome.android("paddingEnd"))
        assertEquals(scrollGutterStart, scroll.android("paddingStart"))
        assertEquals(scrollGutterEnd, scroll.android("paddingEnd"))
        assertEquals(searchHeight, searchShell.android("layout_height"))
        assertEquals(searchTopMargin, searchShell.android("layout_marginTop"))
        assertEquals(searchHorizontalPadding, searchShell.android("paddingStart"))
        assertEquals(searchHorizontalPadding, searchShell.android("paddingEnd"))
        assertEquals(searchIconSize, searchIcon.android("layout_width"))
        assertEquals(searchIconSize, searchIcon.android("layout_height"))
        assertEquals(searchIconPadding, searchIcon.android("padding"))
        assertEquals(categoryRecentGap, categoriesColumn.android("layout_marginEnd"))
        assertEquals(recentWeight, recent.android("layout_weight"))
    }

    private fun assertSearchWorkspace(
        layout: Element,
        queryBarHeight: String,
        queryBarGutter: String,
        filterWidth: String,
        filterPaddingStart: String,
        filterPaddingTop: String,
        filterPaddingEnd: String,
        resultListMargin: String,
        previewWidth: String,
        previewVisibility: String,
    ) {
        val queryBar = layout.elementByAndroidId("tablet_search_query_text").parentNode as Element
        val filterColumn = layout.elementByText("FILTER \u2022 CATEGORY").parentNode as Element
        val results = layout.elementByAndroidId("results_list")
        val preview = layout.elementByAndroidId("tablet_search_preview_rail")

        assertEquals(queryBarHeight, queryBar.android("layout_height"))
        assertEquals(queryBarGutter, queryBar.android("paddingStart"))
        assertEquals(queryBarGutter, queryBar.android("paddingEnd"))
        assertEquals(filterWidth, filterColumn.android("layout_width"))
        assertEquals(filterPaddingStart, filterColumn.android("paddingStart"))
        assertEquals(filterPaddingTop, filterColumn.android("paddingTop"))
        assertEquals(filterPaddingEnd, filterColumn.android("paddingEnd"))
        assertEquals(resultListMargin, results.android("layout_marginStart"))
        assertEquals(resultListMargin, results.android("layout_marginEnd"))
        assertEquals(previewWidth, preview.android("layout_width"))
        assertEquals(previewVisibility, preview.android("visibility"))
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
