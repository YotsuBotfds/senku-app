@file:JvmName("AnswerContentFactory")

package com.senku.ui.answer

import com.senku.mobile.OfflineAnswerEngine
import com.senku.mobile.ReviewedCardMetadata
import java.util.Locale
import kotlin.math.round

enum class AnswerSurfaceLabel {
    ReviewedCardEvidence,
    GeneratedEvidence,
    DeterministicRule,
    LimitedFit,
    Abstain,
    Unknown,
}

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
    val answerSurfaceLabel: AnswerSurfaceLabel = AnswerSurfaceLabel.Unknown,
    val answerProvenance: String = "",
    val reviewedCardBacked: Boolean = false,
    val reviewedCardMetadata: ReviewedCardMetadata = ReviewedCardMetadata.empty(),
)

fun fromAnswerRun(run: OfflineAnswerEngine.AnswerRun): AnswerContent {
    val answerSurface = inferAnswerSurface(
        mode = run.mode,
        abstain = run.abstain,
        deterministic = run.deterministic,
        sourceCount = run.sources.size,
        ruleId = run.ruleId,
        reviewedCardMetadata = run.reviewedCardMetadata,
    )
    return fromRenderedAnswer(
        body = run.answerBody,
        sourceCount = run.sources.size,
        host = parseHost(run.subtitle),
        elapsedSeconds = roundSeconds(run.elapsedMs / 1000.0),
        evidence = evidenceForAnswerState(
            confidenceLabel = run.confidenceLabel,
            mode = run.mode,
            abstain = run.abstain,
            sourceCount = run.sources.size,
        ),
        abstain = run.abstain,
        uncertainFit = run.mode == OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT,
        showStreamingCursor = false,
        answerSurfaceLabel = answerSurface.answerSurfaceLabel,
        answerProvenance = answerSurface.answerProvenance,
        reviewedCardBacked = answerSurface.answerSurfaceLabel == AnswerSurfaceLabel.ReviewedCardEvidence,
        reviewedCardMetadata = run.reviewedCardMetadata,
    )
}

@JvmOverloads
fun fromRenderedAnswer(
    body: String,
    sourceCount: Int,
    host: String,
    elapsedSeconds: Double,
    evidence: Evidence,
    abstain: Boolean,
    uncertainFit: Boolean = false,
    showStreamingCursor: Boolean = false,
    answerSurfaceLabel: AnswerSurfaceLabel = AnswerSurfaceLabel.Unknown,
    answerProvenance: String = "",
    reviewedCardBacked: Boolean = false,
    reviewedCardMetadata: ReviewedCardMetadata = ReviewedCardMetadata.empty(),
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
            answerSurfaceLabel = answerSurfaceLabel,
            answerProvenance = answerProvenance,
            reviewedCardBacked = reviewedCardBacked,
            reviewedCardMetadata = reviewedCardMetadata,
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
        answerSurfaceLabel = answerSurfaceLabel,
        answerProvenance = answerProvenance,
        reviewedCardBacked = reviewedCardBacked,
        reviewedCardMetadata = reviewedCardMetadata,
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

fun evidenceForAnswerState(
    confidenceLabel: OfflineAnswerEngine.ConfidenceLabel?,
    mode: OfflineAnswerEngine.AnswerMode?,
    abstain: Boolean,
    sourceCount: Int,
): Evidence {
    if (abstain || mode == OfflineAnswerEngine.AnswerMode.ABSTAIN) {
        return Evidence.None
    }
    if (mode == OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT) {
        return Evidence.None
    }
    return when (confidenceLabel) {
        OfflineAnswerEngine.ConfidenceLabel.HIGH -> evidenceForSourceCount(sourceCount)
        OfflineAnswerEngine.ConfidenceLabel.MEDIUM -> if (sourceCount > 0) Evidence.Moderate else Evidence.None
        OfflineAnswerEngine.ConfidenceLabel.LOW -> Evidence.None
        null -> evidenceForSourceCount(sourceCount)
    }
}

data class AnswerSurfaceInference(
    val answerSurfaceLabel: AnswerSurfaceLabel,
    val answerProvenance: String,
)

@JvmOverloads
fun inferAnswerSurface(
    mode: OfflineAnswerEngine.AnswerMode?,
    abstain: Boolean,
    deterministic: Boolean,
    sourceCount: Int,
    ruleId: String = "",
    reviewedCardMetadata: ReviewedCardMetadata = ReviewedCardMetadata.empty(),
): AnswerSurfaceInference {
    return when {
        abstain || mode == OfflineAnswerEngine.AnswerMode.ABSTAIN -> AnswerSurfaceInference(
            answerSurfaceLabel = AnswerSurfaceLabel.Abstain,
            answerProvenance = "abstain_card",
        )
        mode == OfflineAnswerEngine.AnswerMode.UNCERTAIN_FIT -> AnswerSurfaceInference(
            answerSurfaceLabel = AnswerSurfaceLabel.LimitedFit,
            answerProvenance = "uncertain_fit_card",
        )
        deterministic
            && sourceCount > 0
            && ruleId.trim().startsWith("answer_card:")
            && reviewedCardMetadata.isPresent()
            && reviewedCardMetadata.provenance == ReviewedCardMetadata.PROVENANCE_REVIEWED_CARD_RUNTIME
            && reviewedCardMetadata.citedReviewedSourceGuideIds.isNotEmpty() -> AnswerSurfaceInference(
            answerSurfaceLabel = AnswerSurfaceLabel.ReviewedCardEvidence,
            answerProvenance = "reviewed_card_runtime",
        )
        deterministic -> AnswerSurfaceInference(
            answerSurfaceLabel = AnswerSurfaceLabel.DeterministicRule,
            answerProvenance = "deterministic_rule",
        )
        mode == OfflineAnswerEngine.AnswerMode.CONFIDENT && sourceCount > 0 -> AnswerSurfaceInference(
            answerSurfaceLabel = AnswerSurfaceLabel.GeneratedEvidence,
            answerProvenance = "generated_model",
        )
        else -> AnswerSurfaceInference(
            answerSurfaceLabel = AnswerSurfaceLabel.Unknown,
            answerProvenance = "",
        )
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
