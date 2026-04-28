package com.senku.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.roundToInt

class ContrastAuditTest {
    private val xmlColors by lazy { loadColorsXml() }

    @Test
    fun rev03TokensStayMirroredInColorsXml() {
        assertMirror("senku_rev03_bg_0", DefaultSenkuColors.bg0)
        assertMirror("senku_rev03_bg_1", DefaultSenkuColors.bg1)
        assertMirror("senku_rev03_bg_2", DefaultSenkuColors.bg2)
        assertMirror("senku_rev03_bg_3", DefaultSenkuColors.bg3)
        assertMirror("senku_rev03_olive_10", DefaultSenkuColors.olive10)
        assertMirror("senku_rev03_olive_20", DefaultSenkuColors.olive20)
        assertMirror("senku_rev03_olive_40", DefaultSenkuColors.olive40)
        assertMirror("senku_rev03_olive_60", DefaultSenkuColors.olive60)
        assertMirror("senku_rev03_ink_0", DefaultSenkuColors.ink0)
        assertMirror("senku_rev03_ink_1", DefaultSenkuColors.ink1)
        assertMirror("senku_rev03_ink_2", DefaultSenkuColors.ink2)
        assertMirror("senku_rev03_ink_3", DefaultSenkuColors.ink3)
        assertMirror("senku_rev03_paper", DefaultSenkuColors.paper)
        assertMirror("senku_rev03_paper_ink", DefaultSenkuColors.paperInk)
        assertMirror("senku_rev03_paper_ink_muted", DefaultSenkuColors.paperInkMuted)
        assertMirror("senku_rev03_accent", DefaultSenkuColors.accent)
        assertMirror("senku_rev03_danger", DefaultSenkuColors.danger)
        assertMirror("senku_rev03_warn", DefaultSenkuColors.warn)
        assertMirror("senku_rev03_ok", DefaultSenkuColors.ok)
        assertMirror("senku_rev03_paper_danger", DefaultSenkuColors.paperDanger)
        assertMirror("senku_rev03_paper_warn", DefaultSenkuColors.paperWarn)
        assertMirror("senku_rev03_paper_ok", DefaultSenkuColors.paperOk)
        assertMirror("senku_rev03_hairline", DefaultSenkuColors.hairline)
        assertMirror("senku_rev03_hairline_strong", DefaultSenkuColors.hairlineStrong)
        assertMirror("senku_rev03_accent_copper", DefaultSenkuColors.copper)
        assertMirror("senku_rev03_accent_moss", DefaultSenkuColors.moss)
    }

    @Test
    fun importantBodyTextPairingsMeetAa() {
        assertContrastAtLeast(
            label = "search result subtitle / paper",
            foreground = DefaultSenkuColors.paperInkMuted.toSwatch(),
            background = DefaultSenkuColors.paper.toSwatch(),
            minimum = 4.5,
        )
        assertContrastAtLeast(
            label = "paper answer muted meta / paper",
            foreground = DefaultSenkuColors.paperInkMuted.toSwatch(),
            background = DefaultSenkuColors.paper.toSwatch(),
            minimum = 4.5,
        )
        assertContrastAtLeast(
            label = "ink3 / bg1",
            foreground = DefaultSenkuColors.ink3.toSwatch(),
            background = DefaultSenkuColors.bg1.toSwatch(),
            minimum = 4.5,
        )
        assertContrastAtLeast(
            label = "ink3 / bg2",
            foreground = DefaultSenkuColors.ink3.toSwatch(),
            background = DefaultSenkuColors.bg2.toSwatch(),
            minimum = 4.5,
        )
        assertContrastAtLeast(
            label = "paper ok / paper",
            foreground = DefaultSenkuColors.paperOk.toSwatch(),
            background = DefaultSenkuColors.paper.toSwatch(),
            minimum = 4.5,
        )
        assertContrastAtLeast(
            label = "paper warn / paper",
            foreground = DefaultSenkuColors.paperWarn.toSwatch(),
            background = DefaultSenkuColors.paper.toSwatch(),
            minimum = 4.5,
        )
        assertContrastAtLeast(
            label = "paper danger / paper",
            foreground = DefaultSenkuColors.paperDanger.toSwatch(),
            background = DefaultSenkuColors.paper.toSwatch(),
            minimum = 4.5,
        )
        assertContrastAtLeast(
            label = "guide reader body / paper shell",
            foreground = xmlColors.getValue("senku_rev03_paper_ink"),
            background = xmlColors.getValue("senku_rev03_paper"),
            minimum = 7.0,
        )
        assertContrastAtLeast(
            label = "guide reader anchor value / paper shell",
            foreground = xmlColors.getValue("senku_rev03_paper_ink_muted"),
            background = xmlColors.getValue("senku_rev03_paper"),
            minimum = 4.5,
        )
        assertContrastAtLeast(
            label = "guide reader warning accent / paper shell",
            foreground = xmlColors.getValue("senku_rev03_paper_warn"),
            background = xmlColors.getValue("senku_rev03_paper"),
            minimum = 4.5,
        )
        assertContrastAtLeast(
            label = "guide reader danger accent / paper shell",
            foreground = xmlColors.getValue("senku_rev03_paper_danger"),
            background = xmlColors.getValue("senku_rev03_paper"),
            minimum = 4.5,
        )
    }

    @Test
    fun retrievalLaneTagsMeetAaOnTintedPaperChip() {
        val paper = xmlColors.getValue("senku_rev03_paper")
        listOf(
            "senku_badge_hybrid",
            "senku_badge_lexical",
            "senku_badge_vector",
            "senku_badge_guide",
            "senku_badge_default",
        ).forEach { name ->
            val tone = xmlColors.getValue(name)
            val chipBackground = tone.withAlpha(0.13).compositeOver(paper)
            assertContrastAtLeast(
                label = "$name / chip background",
                foreground = tone,
                background = chipBackground,
                minimum = 4.5,
            )
            assertContrastAtLeast(
                label = "$name / white legacy badge text",
                foreground = Swatch(0xFF, 0xFF, 0xFF, 0xFF),
                background = tone,
                minimum = 4.5,
            )
        }
    }

    @Test
    fun emergencyBannerTextMeetsAaThroughDrawableHighlight() {
        val text = xmlColors.getValue("senku_emergency_banner_text")
        val baseBackground = xmlColors.getValue("senku_emergency_banner_bg")
        val highlightBackground = text.withAlpha(0x12 / 255.0).compositeOver(baseBackground)

        assertContrastAtLeast(
            label = "emergency banner text / base background",
            foreground = text,
            background = baseBackground,
            minimum = 7.0,
        )
        assertContrastAtLeast(
            label = "emergency banner text / highlighted background",
            foreground = text,
            background = highlightBackground,
            minimum = 7.0,
        )
    }

    private fun assertMirror(name: String, color: Color) {
        assertEquals(name, color.toSwatch().toCanonicalHex(), xmlColors.getValue(name).toCanonicalHex())
    }

    private fun assertContrastAtLeast(
        label: String,
        foreground: Swatch,
        background: Swatch,
        minimum: Double,
    ) {
        val ratio = foreground.contrastAgainst(background)
        assertTrue(
            "$label expected >= %.2f but was %.2f".format(minimum, ratio),
            ratio >= minimum,
        )
    }

    private fun loadColorsXml(): Map<String, Swatch> {
        val file = locateFile(
            "android-app/app/src/main/res/values/colors.xml",
            "app/src/main/res/values/colors.xml",
        )
        val pattern = Regex("""<color name="([^"]+)">\s*(#[0-9A-Fa-f]{6,8})\s*</color>""")
        return pattern.findAll(file.readText()).associate { match ->
            match.groupValues[1] to Swatch.fromHex(match.groupValues[2])
        }
    }

    private fun locateFile(vararg candidates: String): File {
        val start = File(System.getProperty("user.dir")).absoluteFile
        val roots = generateSequence(start) { it.parentFile }
        for (root in roots) {
            for (candidate in candidates) {
                val file = File(root, candidate)
                if (file.exists()) {
                    return file
                }
            }
        }
        error("Unable to locate colors.xml from ${start.path}")
    }
}

private data class Swatch(
    val alpha: Int,
    val red: Int,
    val green: Int,
    val blue: Int,
) {
    fun toCanonicalHex(): String = "#%02X%02X%02X%02X".format(alpha, red, green, blue)

    fun withAlpha(alphaFraction: Double): Swatch =
        copy(alpha = (alphaFraction.coerceIn(0.0, 1.0) * 255.0).roundToInt())

    fun compositeOver(background: Swatch): Swatch {
        val foregroundAlpha = alpha / 255.0
        val backgroundAlpha = background.alpha / 255.0
        val outAlpha = foregroundAlpha + (backgroundAlpha * (1.0 - foregroundAlpha))
        if (outAlpha <= 0.0) {
            return Swatch(0, 0, 0, 0)
        }
        val outRed = compositeChannel(red, foregroundAlpha, background.red, backgroundAlpha, outAlpha)
        val outGreen = compositeChannel(green, foregroundAlpha, background.green, backgroundAlpha, outAlpha)
        val outBlue = compositeChannel(blue, foregroundAlpha, background.blue, backgroundAlpha, outAlpha)
        return Swatch(
            alpha = (outAlpha * 255.0).roundToInt(),
            red = outRed,
            green = outGreen,
            blue = outBlue,
        )
    }

    fun contrastAgainst(other: Swatch): Double {
        val first = luminance()
        val second = other.luminance()
        val lighter = maxOf(first, second)
        val darker = minOf(first, second)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun luminance(): Double {
        val normalizedRed = red.channelLuminance()
        val normalizedGreen = green.channelLuminance()
        val normalizedBlue = blue.channelLuminance()
        return (0.2126 * normalizedRed) + (0.7152 * normalizedGreen) + (0.0722 * normalizedBlue)
    }

    companion object {
        fun fromHex(hex: String): Swatch {
            val normalized = hex.removePrefix("#")
            return when (normalized.length) {
                6 -> Swatch(
                    alpha = 0xFF,
                    red = normalized.substring(0, 2).toInt(16),
                    green = normalized.substring(2, 4).toInt(16),
                    blue = normalized.substring(4, 6).toInt(16),
                )

                8 -> Swatch(
                    alpha = normalized.substring(0, 2).toInt(16),
                    red = normalized.substring(2, 4).toInt(16),
                    green = normalized.substring(4, 6).toInt(16),
                    blue = normalized.substring(6, 8).toInt(16),
                )

                else -> error("Unsupported color hex: $hex")
            }
        }
    }
}

private fun Color.toSwatch(): Swatch = Swatch(
    alpha = (alpha * 255.0f).roundToInt(),
    red = (red * 255.0f).roundToInt(),
    green = (green * 255.0f).roundToInt(),
    blue = (blue * 255.0f).roundToInt(),
)

private fun compositeChannel(
    foreground: Int,
    foregroundAlpha: Double,
    background: Int,
    backgroundAlpha: Double,
    outAlpha: Double,
): Int {
    val numerator = (foreground * foregroundAlpha) + (background * backgroundAlpha * (1.0 - foregroundAlpha))
    return (numerator / outAlpha).roundToInt().coerceIn(0, 255)
}

private fun Int.channelLuminance(): Double {
    val normalized = this / 255.0
    return if (normalized <= 0.04045) {
        normalized / 12.92
    } else {
        ((normalized + 0.055) / 1.055).let { base -> Math.pow(base, 2.4) }
    }
}
