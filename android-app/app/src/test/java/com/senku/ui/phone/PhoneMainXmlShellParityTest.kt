package com.senku.ui.phone

import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class PhoneMainXmlShellParityTest {
    @Test
    fun phoneHomeChromeUsesSharedBackAndTitleTokens() {
        val layout = layout()
        val row = layout.elementByAndroidId("home_chrome_row")
        val back = layout.elementByAndroidId("home_chrome_back_button")
        val backIcon = layout.elementByAndroidId("home_chrome_back_icon")
        val backLabel = layout.elementByAndroidId("home_chrome_back_label")
        val divider = layout.elementByAndroidId("home_chrome_divider")
        val mode = layout.elementByAndroidId("home_chrome_mode")
        val title = layout.elementByAndroidId("home_chrome_title")
        val searchIcon = layout.elementByAndroidId("home_chrome_search_icon")
        val bottomRule = layout.elementByAndroidId("home_chrome_bottom_rule")
        val children = row.directElementChildren()
        val overflow = children[5]

        assertEquals("54dp", row.android("layout_height"))
        assertEquals("18dp", row.android("paddingStart"))
        assertEquals("18dp", row.android("paddingEnd"))
        assertEquals("12dp", row.android("paddingTop"))
        assertEquals("12dp", row.android("paddingBottom"))
        assertEquals(
            listOf("LinearLayout", "View", "TextView", "TextView", "ImageView", "TextView"),
            children.map { it.tagName },
        )

        assertEquals("28dp", back.android("layout_width"))
        assertEquals("28dp", back.android("layout_height"))
        assertEquals("@string/detail_back_content_description", back.android("contentDescription"))
        assertEquals("horizontal", back.android("orientation"))
        assertEquals("center", back.android("gravity"))
        assertEquals("0dp", back.android("paddingStart"))
        assertEquals("0dp", back.android("paddingEnd"))
        assertEquals("18dp", backIcon.android("layout_width"))
        assertEquals("18dp", backIcon.android("layout_height"))
        assertEquals("@drawable/ic_home_back_chevron", backIcon.android("src"))
        assertEquals("@color/senku_rev03_ink_0", backIcon.android("tint"))
        assertEquals("@string/detail_back", backLabel.android("text"))
        assertEquals("gone", backLabel.android("visibility"))

        assertEquals("1dp", divider.android("layout_width"))
        assertEquals("24dp", divider.android("layout_height"))
        assertEquals("6dp", divider.android("layout_marginStart"))
        assertEquals("10dp", divider.android("layout_marginEnd"))

        assertEquals("HOME SENKU", mode.android("text"))
        assertEquals("@style/TextAppearance.Senku.Rev03.MonoCaps", mode.android("textAppearance"))
        assertEquals("@font/jetbrains_mono", mode.android("fontFamily"))
        assertEquals("@color/senku_rev03_accent", mode.android("textColor"))
        assertEquals("500", mode.android("textFontWeight"))

        assertEquals("Field manual \u2022 ed.2", title.android("text"))
        assertEquals("10dp", title.android("layout_marginStart"))
        assertEquals("@font/inter_tight", title.android("fontFamily"))
        assertEquals("14sp", title.android("textSize"))
        assertEquals("600", title.android("textFontWeight"))
        assertEquals("18sp", title.android("lineHeight"))
        assertEquals("@color/senku_rev03_ink_0", title.android("textColor"))

        assertEquals("16dp", searchIcon.android("layout_width"))
        assertEquals("16dp", searchIcon.android("layout_height"))
        assertEquals("@drawable/ic_search_magnifier", searchIcon.android("src"))
        assertEquals("...", overflow.android("text"))
        assertEquals("1dp", bottomRule.android("layout_height"))
        assertEquals("@color/senku_rev03_hairline_strong", bottomRule.android("background"))
    }

    private fun layout(): Element =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(layoutFile())
            .documentElement

    private fun layoutFile(): File =
        File("src/main/res/layout/activity_main.xml")

    private fun Element.elementByAndroidId(id: String): Element {
        val nodes = getElementsByTagName("*")
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as? Element ?: continue
            if (element.android("id") == "@+id/$id" || element.android("id") == "@id/$id") {
                return element
            }
        }
        throw AssertionError("Missing android id $id")
    }

    private fun Element.directElementChildren(): List<Element> =
        (0 until childNodes.length)
            .mapNotNull { childNodes.item(it) as? Element }

    private fun Element.android(name: String): String =
        getAttribute("android:$name")
}
