package com.senku.ui.phone

import com.senku.ui.MainChromeSpec
import com.senku.ui.android
import com.senku.ui.assertSharedMainChrome
import com.senku.ui.elementByAndroidId
import com.senku.ui.firstElementChild
import com.senku.ui.mainLayout
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneMainXmlShellParityTest {
    @Test
    fun phoneHomeChromeUsesSharedBackAndTitleTokens() {
        assertSharedMainChrome(
            layout = mainLayout(""),
            spec = MainChromeSpec(
                qualifier = "layout",
                height = "54dp",
                horizontalPadding = "18dp",
                expectedModeFontFamily = "@font/jetbrains_mono",
                verticalPadding = "3dp",
                backActionSize = "48dp",
                searchActionSize = "48dp",
                searchPadding = "15dp",
                searchTint = "@color/senku_rev03_ink_0",
                expectsOverflowPlaceholder = false,
            ),
        )
    }

    @Test
    fun landscapePhoneHomeChromeUsesSharedBackAndTitleTokens() {
        assertSharedMainChrome(
            layout = mainLayout("layout-land"),
            spec = MainChromeSpec(
                qualifier = "layout-land",
                height = "48dp",
                horizontalPadding = "12dp",
                expectedModeFontFamily = "@font/jetbrains_mono",
                verticalPadding = "0dp",
                backActionSize = "48dp",
                searchActionSize = "48dp",
                searchPadding = "15dp",
                searchTint = "@color/senku_rev03_ink_2",
                expectsOverflowPlaceholder = false,
            ),
        )
    }

    @Test
    fun phoneHomeHeadersUseRev03TypographyTokens() {
        val layout = mainLayout("")

        assertEquals(
            "@style/TextAppearance.Senku.Rev03.ChromeMono",
            layout.elementByAndroidId("status_text").android("textAppearance"),
        )
        assertEquals(
            "@style/TextAppearance.Senku.Rev03.ChromeMono",
            layout.elementByAndroidId("category_section_header").android("textAppearance"),
        )
    }

    @Test
    fun phoneSearchHeadersUseRev03TypographyTokens() {
        val layout = mainLayout("")

        assertEquals(
            "@style/TextAppearance.Senku.Rev03.ChromeMono",
            layout.elementByAndroidId("results_header").android("textAppearance"),
        )
        assertEquals(
            "@style/TextAppearance.Senku.Rev03.SmallBody",
            layout.elementByAndroidId("phone_search_query_text").android("textAppearance"),
        )
        assertEquals(
            "@style/TextAppearance.Senku.Rev03.ChromeMono",
            layout.elementByAndroidId("phone_search_count_text").android("textAppearance"),
        )
    }

    @Test
    fun landscapePhoneHomeAndSearchHeadersUseRev03TypographyTokens() {
        val layout = mainLayout("layout-land")

        assertEquals(
            "@style/TextAppearance.Senku.Rev03.MonoCaps",
            (layout.elementByAndroidId("phone_nav_home").parentNode as org.w3c.dom.Element)
                .firstElementChild("TextView")
                .android("textAppearance"),
        )
        assertEquals(
            "@style/TextAppearance.Senku.Rev03.ChromeMono",
            layout.elementByAndroidId("status_text").android("textAppearance"),
        )
        assertEquals(
            "@style/TextAppearance.Senku.Rev03.ChromeMono",
            layout.elementByAndroidId("category_section_header").android("textAppearance"),
        )
        assertEquals(
            "@style/TextAppearance.Senku.Rev03.ChromeMono",
            layout.elementByAndroidId("results_header").android("textAppearance"),
        )
        assertEquals(
            "@style/TextAppearance.Senku.Rev03.SmallBody",
            layout.elementByAndroidId("phone_search_query_text").android("textAppearance"),
        )
        assertEquals(
            "@style/TextAppearance.Senku.Rev03.ChromeMono",
            layout.elementByAndroidId("phone_search_count_text").android("textAppearance"),
        )
    }
}
