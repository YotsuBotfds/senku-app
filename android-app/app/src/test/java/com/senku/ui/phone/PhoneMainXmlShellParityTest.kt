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
            ),
        )
    }
}
