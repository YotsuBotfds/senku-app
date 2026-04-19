@file:JvmName("AnswerContentFactory")

package com.senku.ui.answer

import com.senku.mobile.OfflineAnswerEngine
import java.util.Locale
import kotlin.math.round

data class AnswerContent(
    val short: String,
    val steps: List<String>? = null,
    val limits: String? = null,
    val evidence: Evidence = Evidence.Moderate,
    val sourceCount: Int,
    val host: String,
    val elapsedSeconds: Double,
    val abstain: Boolean = false,
    val uncertainFit: Boolean = false,
    val showStreamingCursor: Boolean = false,
)

fun fromAnswerRun(run: OfflineAnswerEngine.AnswerRun): AnswerContent {
    return fromRenderedAnswer(
        body = run.answerBody,
        sourceCount = run.sources.size,
        host = parseHost(run.subtitle),
        elapsedSeconds = roundSeconds(run.elapsedMs / 1000.0),
        evidence = if (run.abstain) Evidence.None else evidenceForSourceCount(run.sources.size),
        abstain = run.abstain,
        uncertainFit = run.mode == OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
        showStreamingCursor = false,
    )
}

fun fromRenderedAnswer(
    body: String,
    sourceCount: Int,
    host: String,
    elapsedSeconds: Double,
    evidence: Evidence,
    abstain: Boolean,
    uncertainFit: Boolean = false,
    showStreamingCursor: Boolean = false,
): AnswerContent {
    val cleaned = stripEnvelope(body)
    if (cleaned.isBlank()) {
        return AnswerContent(
            short = if (showStreamingCursor) " |" else "",
            sourceCount = sourceCount,
            host = host.trim(),
            elapsedSeconds = roundSeconds(elapsedSeconds),
            evidence = evidence,
            abstain = abstain,
            uncertainFit = uncertainFit,
            showStreamingCursor = showStreamingCursor,
        )
    }

    val parsed = if (abstain) parseAbstain(cleaned) else parseStructured(cleaned)
    return AnswerContent(
        short = withStreamingCursor(parsed.short, parsed.limits, parsed.steps, showStreamingCursor),
        steps = parsed.steps,
        limits = parsed.limits?.let { limits ->
            if (showStreamingCursor && parsed.short.isBlank() && parsed.steps.isNullOrEmpty()) {
                appendCursor(limits)
            } else {
                limits
            }
        },
        sourceCount = sourceCount,
        host = host.trim(),
        elapsedSeconds = roundSeconds(elapsedSeconds),
        evidence = evidence,
        abstain = abstain,
        uncertainFit = uncertainFit,
        showStreamingCursor = showStreamingCursor,
    )
}

fun parseElapsedSeconds(subtitle: String): Double {
    val cleaned = subtitle.trim().lowercase(Locale.US)
    val secondsMatch = Regex("""(\d+(?:\.\d+)?)\s*s\b""").find(cleaned)
    if (secondsMatch != null) {
        return roundSeconds(secondsMatch.groupValues[1].toDoubleOrNull() ?: 0.0)
    }
    val millisMatch = Regex("""(\d+(?:\.\d+)?)\s*ms\b""").find(cleaned)
    if (millisMatch != null) {
        val millis = millisMatch.groupValues[1].toDoubleOrNull() ?: 0.0
        return roundSeconds(millis / 1000.0)
    }
    return 0.0
}

fun parseHost(subtitle: String): String {
    val cleaned = subtitle.trim().lowercase(Locale.US)
    if (cleaned.contains("on-device fallback")) {
        return "On-device fallback"
    }
    if (cleaned.startsWith("host answer |") || cleaned.contains(" @ ")) {
        return "Host"
    }
    if (cleaned.startsWith("offline answer |")) {
        return "On-device"
    }
    if (cleaned.startsWith("low coverage |")) {
        return if (cleaned.contains(" @ ")) "Host" else "On-device"
    }
    if (cleaned.startsWith("abstain |")) {
        return "Instant"
    }
    if (cleaned.contains("deterministic")) {
        return "Deterministic"
    }
    return ""
}

fun evidenceForSourceCount(sourceCount: Int): Evidence {
    return when {
        sourceCount >= 3 -> Evidence.Strong
        sourceCount >= 1 -> Evidence.Moderate
        else -> Evidence.None
    }
}

private data class ParsedAnswer(
    val short: String,
    val steps: List<String>?,
    val limits: String?,
)

private fun stripEnvelope(body: String): String {
    var cleaned = body.replace("\r\n", "\n").trim()
    if (cleaned.startsWith("Answer\n", ignoreCase = true)) {
        cleaned = cleaned.substringAfter('\n').trim()
    }
    val sourcesIndex = cleaned.indexOf("\n\nSources used", ignoreCase = true)
    if (sourcesIndex >= 0) {
        cleaned = cleaned.substring(0, sourcesIndex).trim()
    }
    return cleaned
}

private fun parseStructured(body: String): ParsedAnswer {
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
            trimmed.equals("Steps:", ignoreCase = true) -> "steps"
            trimmed.equals("Limits or safety:", ignoreCase = true) -> "limits"
            else -> section
        }
        if (trimmed.equals("Short answer:", ignoreCase = true)
            || trimmed.equals("Steps:", ignoreCase = true)
            || trimmed.equals("Limits or safety:", ignoreCase = true)
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
        .filterNot {
            val normalized = it.lowercase(Locale.US).replace(Regex("""[.!:]+$"""), "").trim()
            normalized == "no steps available"
                || normalized == "no steps are available"
                || normalized == "no steps were found"
        }
}

private fun roundSeconds(value: Double): Double {
    if (value <= 0.0) {
        return 0.0
    }
    return round(value * 10.0) / 10.0
}

private fun withStreamingCursor(
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

private fun appendCursor(text: String): String {
    val trimmed = text.trimEnd()
    return if (trimmed.isEmpty()) " |" else "$trimmed |"
}
