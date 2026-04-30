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
    fun portraitAndLandscapeTabletShellsDoNotUsePlatformMonospaceAlias() {
        listOf("layout-sw600dp-port", "layout-sw600dp-land").forEach { qualifier ->
            val xml = layoutFile(qualifier).readText()
            assertFalse("$qualifier should use explicit font resources", xml.contains("android:fontFamily=\"monospace\""))
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
            assertEquals("22dp", icon.android("layout_width"))
            assertEquals("22dp", icon.android("layout_height"))
        }

        listOf("phone_nav_home_label", "phone_nav_ask_label", "phone_nav_pins_label").forEach { id ->
            val label = layout.elementByAndroidId(id)
            assertEquals(labelTopMargin, label.android("layout_marginTop"))
            assertEquals("@font/inter_tight", label.android("fontFamily"))
        }
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
