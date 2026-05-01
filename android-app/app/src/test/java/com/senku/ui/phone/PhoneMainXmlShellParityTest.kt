package com.senku.ui.phone

import com.senku.ui.MainChromeSpec
import com.senku.ui.assertSharedMainChrome
import com.senku.ui.mainLayout
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
}
