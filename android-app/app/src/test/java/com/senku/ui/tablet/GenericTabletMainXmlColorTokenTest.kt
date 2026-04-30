package com.senku.ui.tablet

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class GenericTabletMainXmlColorTokenTest {
    @Test
    fun genericTabletShellDoesNotUseLegacyLightTextTokens() {
        val xml = locateFile(
            "android-app/app/src/main/res/layout-sw600dp/activity_main.xml",
            "app/src/main/res/layout-sw600dp/activity_main.xml",
        ).readText()

        assertFalse(xml.contains("@color/senku_text_light"))
        assertFalse(xml.contains("@color/senku_text_muted_light"))
    }

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
        error("Unable to locate generic tablet XML layout from ${start.path}")
    }
}
