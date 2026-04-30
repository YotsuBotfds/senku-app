package com.senku.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class TypographyTokenAuditTest {
    private val styles by lazy { loadTextAppearances() }

    @Test
    fun rev03XmlTextAppearancesMirrorComposeTypographyMetrics() {
        assertTextAppearanceMirrors(
            name = "TextAppearance.Senku.Rev03.CanvasTitle",
            style = DefaultSenkuTypography.canvasTitle,
            textSize = "36sp",
            lineHeight = "39.6sp",
            letterSpacing = "-0.02",
        )
        assertTextAppearanceMirrors(
            name = "TextAppearance.Senku.Rev03.SectionTitle",
            style = DefaultSenkuTypography.sectionTitle,
            textSize = "22sp",
            lineHeight = "26.4sp",
            letterSpacing = "-0.01",
        )
        assertTextAppearanceMirrors(
            name = "TextAppearance.Senku.Rev03.UiBody",
            style = DefaultSenkuTypography.uiBody,
            textSize = "14sp",
            lineHeight = "21sp",
            letterSpacing = "-0.005",
        )
        assertTextAppearanceMirrors(
            name = "TextAppearance.Senku.Rev03.SmallBody",
            style = DefaultSenkuTypography.smallBody,
            textSize = "13sp",
            lineHeight = "19.5sp",
            letterSpacing = "-0.005",
        )
        assertTextAppearanceMirrors(
            name = "TextAppearance.Senku.Rev03.AnswerBody",
            style = DefaultSenkuTypography.answerBody,
            textSize = "17sp",
            lineHeight = "24.65sp",
            letterSpacing = "-0.005",
        )
        assertTextAppearanceMirrors(
            name = "TextAppearance.Senku.Rev03.MonoCaps",
            style = DefaultSenkuTypography.monoCaps,
            textSize = "11sp",
            lineHeight = "15.4sp",
            letterSpacing = "0.09",
        )
        assertTextAppearanceMirrors(
            name = "TextAppearance.Senku.Rev03.Tag",
            style = DefaultSenkuTypography.tag,
            textSize = "12sp",
            lineHeight = "14.4sp",
        )
    }

    private fun assertTextAppearanceMirrors(
        name: String,
        style: TextStyle,
        textSize: String,
        lineHeight: String,
        letterSpacing: String? = null,
    ) {
        val xmlStyle = styles.getValue(name)
        assertEquals("$name textSize", textSize, xmlStyle.getValue("android:textSize"))
        assertEquals("$name lineHeight", lineHeight, xmlStyle.getValue("android:lineHeight"))
        assertEquals("$name textSize compose mirror", textSize, style.fontSize.asSpString())
        assertEquals("$name lineHeight compose mirror", lineHeight, style.lineHeight.asSpString())

        letterSpacing?.let { expected ->
            assertEquals("$name letterSpacing", expected, xmlStyle.getValue("android:letterSpacing"))
            assertTrue("$name compose letterSpacing should use em", style.letterSpacing.type == TextUnitType.Em)
            assertEquals("$name letterSpacing compose mirror", expected.toFloat(), style.letterSpacing.value, 0.0001f)
        }
    }

    private fun loadTextAppearances(): Map<String, Map<String, String>> {
        val file = locateFile(
            "android-app/app/src/main/res/values/styles.xml",
            "app/src/main/res/values/styles.xml",
        )
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(file)
        val styleNodes = document.getElementsByTagName("style")
        val appearances = mutableMapOf<String, Map<String, String>>()
        for (index in 0 until styleNodes.length) {
            val style = styleNodes.item(index) as Element
            val name = style.getAttribute("name")
            if (!name.startsWith("TextAppearance.Senku.Rev03.")) {
                continue
            }
            val items = mutableMapOf<String, String>()
            val itemNodes = style.getElementsByTagName("item")
            for (itemIndex in 0 until itemNodes.length) {
                val item = itemNodes.item(itemIndex) as Element
                items[item.getAttribute("name")] = item.textContent.trim()
            }
            appearances[name] = items
        }
        return appearances
    }

    private fun locateFile(vararg candidates: String): File {
        val userDir = requireNotNull(System.getProperty("user.dir")) { "user.dir is not set" }
        val start = File(userDir).absoluteFile
        val roots = generateSequence(start) { it.parentFile }
        for (root in roots) {
            for (candidate in candidates) {
                val file = File(root, candidate)
                if (file.exists()) {
                    return file
                }
            }
        }
        error("Unable to locate styles.xml from ${start.path}")
    }
}

private fun androidx.compose.ui.unit.TextUnit.asSpString(): String {
    assertTrue("Expected sp text unit but was $type", type == TextUnitType.Sp)
    return value.toPlainToken() + "sp"
}

private fun Float.toPlainToken(): String {
    val asInt = toInt()
    return if (this == asInt.toFloat()) {
        asInt.toString()
    } else {
        toString()
    }
}
