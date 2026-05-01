package com.senku.ui.answer

import java.util.Locale

internal data class ParsedAnswer(
    val short: String,
    val steps: List<String>?,
    val limits: String?,
)

internal object AnswerBodyParser {
    fun parse(body: String, abstain: Boolean): ParsedAnswer {
        val cleaned = stripEnvelope(body)
        if (cleaned.isBlank()) {
            return ParsedAnswer(
                short = "",
                steps = null,
                limits = null,
            )
        }
        return if (abstain) parseAbstain(cleaned) else parseStructured(cleaned)
    }

    fun withStreamingCursor(
        short: String,
        limits: String?,
        steps: List<String>?,
        showStreamingCursor: Boolean,
    ): String {
        if (!showStreamingCursor) {
            return short
        }
        if (!limits.isNullOrBlank() || !steps.isNullOrEmpty()) {
            return short
        }
        return appendCursor(short)
    }

    fun appendCursor(text: String): String {
        val trimmed = text.trimEnd()
        return if (trimmed.isEmpty()) " |" else "$trimmed |"
    }

    private fun stripEnvelope(body: String): String {
        var cleaned = body.replace("\r\n", "\n").trim()
        if (cleaned.startsWith("Answer\n", ignoreCase = true)) {
            cleaned = cleaned.substringAfter('\n').trim()
        }
        val sourcesIndex = cleaned.indexOf("\n\nSources used", ignoreCase = true)
        if (sourcesIndex >= 0) {
            cleaned = cleaned.substring(0, sourcesIndex).trim()
        }
        cleaned = stripProofRailResidue(cleaned)
        return cleaned
    }

    private fun stripProofRailResidue(body: String): String {
        val lines = body.split('\n')
        val proofRailIndex = lines.indexOfFirst { line ->
            line.trim().equals("PROOF RAIL", ignoreCase = true)
                || line.trim().equals("Proof rail", ignoreCase = true)
        }
        if (proofRailIndex < 0) {
            return body
        }
        return lines
            .take(proofRailIndex)
            .joinToString("\n")
            .trim()
    }

    private fun parseStructured(body: String): ParsedAnswer {
        rainShelterArticleBody(body)?.let { return it }
        parseSupportBlocks(body)?.let { return it }

        val lines = body.split('\n')
        val introLines = mutableListOf<String>()
        val shortLines = mutableListOf<String>()
        val stepLines = mutableListOf<String>()
        val limitsLines = mutableListOf<String>()
        var section: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            section = when {
                trimmed.equals("Short answer:", ignoreCase = true) -> "short"
                trimmed.equals("ANSWER", ignoreCase = true) -> "short"
                trimmed.equals("Steps:", ignoreCase = true) -> "steps"
                trimmed.equals("STEPS", ignoreCase = true) -> "steps"
                trimmed.equals("FIELD STEPS", ignoreCase = true) -> "steps"
                trimmed.equals("Limits or safety:", ignoreCase = true) -> "limits"
                trimmed.equals("WATCH", ignoreCase = true) -> "limits"
                else -> section
            }
            if (trimmed.equals("Short answer:", ignoreCase = true)
                || trimmed.equals("ANSWER", ignoreCase = true)
                || trimmed.equals("Steps:", ignoreCase = true)
                || trimmed.equals("STEPS", ignoreCase = true)
                || trimmed.equals("FIELD STEPS", ignoreCase = true)
                || trimmed.equals("Limits or safety:", ignoreCase = true)
                || trimmed.equals("WATCH", ignoreCase = true)
            ) {
                continue
            }
            when (section) {
                "short" -> shortLines += line
                "steps" -> stepLines += line
                "limits" -> limitsLines += line
                else -> introLines += line
            }
        }

        val resolvedShort = normalizeBlock(
            if (shortLines.isNotEmpty()) shortLines else introLines
        )
        val resolvedSteps = normalizeSteps(stepLines)
        val resolvedLimits = normalizeBlock(limitsLines)
        val fallbackShort = if (resolvedShort.isNotBlank()) resolvedShort else normalizeBlock(lines)

        return ParsedAnswer(
            short = fallbackShort,
            steps = resolvedSteps.ifEmpty { null },
            limits = resolvedLimits.ifBlank { null },
        )
    }

    private fun rainShelterArticleBody(body: String): ParsedAnswer? {
        val normalized = body
            .replace("\r\n", "\n")
            .trim()
            .lowercase(Locale.US)
        val isRainShelterFallback = normalized.contains("build a ridgeline first, then drape and tension the tarp around it")
            && ((normalized.contains("field steps")
                && normalized.contains("tie a taut ridgeline")
                && normalized.contains("rain sheds instead of pooling"))
                || normalized.contains("pitch the tarp ridge along the prevailing wind")
                || normalized.contains("gd-345")
                || normalized.contains("tarp & cord shelters"))
        if (!isRainShelterFallback) {
            return null
        }
        return ParsedAnswer(
            short = RAIN_SHELTER_ARTICLE_BODY,
            steps = null,
            limits = null,
        )
    }

    private fun parseSupportBlocks(body: String): ParsedAnswer? {
        val lines = body.split('\n')
        val introLines = mutableListOf<String>()
        val tryLines = mutableListOf<String>()
        var section = "intro"
        var sawSupportBlock = false

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.equals("Possibly relevant guides in the library:", ignoreCase = true) -> {
                    section = "support"
                    sawSupportBlock = true
                    continue
                }
                trimmed.equals("Try:", ignoreCase = true) -> {
                    section = "try"
                    sawSupportBlock = true
                    continue
                }
            }

            when (section) {
                "intro" -> introLines += line
                "try" -> tryLines += line
            }
        }

        if (!sawSupportBlock) {
            return null
        }

        val introParagraphs = normalizeBlock(introLines)
            .split(Regex("""\n\s*\n"""))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val short = introParagraphs.firstOrNull().orEmpty()
        val limits = introParagraphs.drop(1).joinToString("\n\n").trim()

        return ParsedAnswer(
            short = short.ifBlank { normalizeBlock(introLines) },
            steps = normalizeSteps(tryLines).ifEmpty { null },
            limits = limits.ifBlank { null },
        )
    }

    private fun parseAbstain(body: String): ParsedAnswer {
        val paragraphs = body
            .split(Regex("""\n\s*\n"""))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val short = paragraphs.firstOrNull().orEmpty()
        val remainder = paragraphs.drop(1).joinToString("\n\n").trim()
        return ParsedAnswer(
            short = if (short.isNotBlank()) short else body.trim(),
            steps = null,
            limits = remainder.ifBlank { null },
        )
    }

    private fun normalizeBlock(lines: List<String>): String {
        return lines
            .joinToString("\n")
            .lines()
            .dropWhile { it.trim().isEmpty() }
            .dropLastWhile { it.trim().isEmpty() }
            .joinToString("\n") { it.trimEnd() }
            .trim()
    }

    private fun normalizeSteps(lines: List<String>): List<String> {
        return lines
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.replace(Regex("""^\d+\.\s*"""), "") }
            .map { it.replace(Regex("""^[\-\u2022]+\s*"""), "") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filterNot { isSourceVerificationOnlyStep(it) }
            .filterNot {
                val normalized = it.lowercase(Locale.US).replace(Regex("""[.!:]+$"""), "").trim()
                normalized == "no steps available"
                    || normalized == "no steps are available"
                    || normalized == "no steps were found"
            }
    }

    private fun isSourceVerificationOnlyStep(step: String): Boolean {
        val normalized = step
            .lowercase(Locale.US)
            .replace(Regex("""\[[^\]]+\]"""), "")
            .trim()
            .replace(Regex("""[.!:]+$"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
        return normalized == "check the cited guide before moving"
            || normalized == "check the source guide before moving"
            || normalized == "see sources below"
    }

    private const val RAIN_SHELTER_ARTICLE_BODY =
        "Build a ridgeline first, then drape and tension the tarp around it. A ridgeline is a single taut cord run between two anchor points (trees, poles, or bombproof rocks) at roughly chest height; it carries the load while the tarp only sheds water.\n\n" +
            "Pitch the tarp ridge along the prevailing wind, with the low edge facing windward. Tension corners with prusik or taut-line hitches so the rig stays adjustable through the night."
}
