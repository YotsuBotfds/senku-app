package com.senku.ui.tablet

import com.senku.ui.mainLayoutText
import org.junit.Assert.assertFalse
import org.junit.Test

class GenericTabletMainXmlColorTokenTest {
    @Test
    fun genericTabletShellDoesNotUseLegacyLightTextTokens() {
        val xml = mainLayoutText("layout-sw600dp")

        assertFalse(xml.contains("@color/senku_text_light"))
        assertFalse(xml.contains("@color/senku_text_muted_light"))
    }
}
