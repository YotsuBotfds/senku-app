package com.senku.ui.tablet

import com.senku.ui.assertLayoutDoesNotReferenceResources
import com.senku.ui.mainLayout
import org.junit.Test

class GenericTabletMainXmlColorTokenTest {
    @Test
    fun genericTabletShellDoesNotUseLegacyLightTextTokens() {
        assertLayoutDoesNotReferenceResources(
            layout = mainLayout("layout-sw600dp"),
            forbiddenResources = listOf(
                "@color/senku_text_light",
                "@color/senku_text_muted_light",
            ),
            message = "generic tablet shell",
        )
    }
}
