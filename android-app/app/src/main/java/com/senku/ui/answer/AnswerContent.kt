@file:JvmName("AnswerContentFactory")

package com.senku.ui.answer

import com.senku.mobile.OfflineAnswerEngine
import com.senku.mobile.ReviewedCardMetadata
import java.util.Locale
import kotlin.math.round

internal const val AnswerEvidenceBulletSeparator = " \u2022 "
internal const val AnswerEvidenceDashSeparator = " - "

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

internal fun answerEvidenceBulletJoin(tokens: Iterable<String>): String =
    tokens.joinToString(AnswerEvidenceBulletSeparator)

internal fun answerEvidenceDashJoin(tokens: Iterable<String>): String =
    tokens.joinToString(AnswerEvidenceDashSeparator)

internal fun answerEvidenceSourceCountLabel(
    sourceCount: Int,
    zeroLabel: String = "No sources",
): String =
    when (sourceCount) {
        0 -> zeroLabel
        1 -> "1 source"
        else -> "$sourceCount sources"
    }

internal fun answerEvidenceSourceCountLabelUpper(
    sourceCount: Int,
    zeroLabel: String = "No sources",
): String =
    answerEvidenceSourceCountLabel(sourceCount, zeroLabel).uppercase(Locale.US)

internal fun answerEvidenceStatusToken(label: String): String =
    "\u2022 ${label.trim().uppercase(Locale.US)}"

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
    val parsed = AnswerBodyParser.parse(body, abstain)
    return AnswerContent(
        short = AnswerBodyParser.withStreamingCursor(
            parsed.short,
            parsed.limits,
            parsed.steps,
            showStreamingCursor,
        ),
        steps = parsed.steps,
        limits = parsed.limits?.let { limits ->
            if (showStreamingCursor && parsed.short.isBlank() && parsed.steps.isNullOrEmpty()) {
                AnswerBodyParser.appendCursor(limits)
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
            && ReviewedCardMetadata.isReviewedRuntimeCardWithCitedSources(
                ruleId,
                reviewedCardMetadata,
            ) -> AnswerSurfaceInference(
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

private fun roundSeconds(value: Double): Double {
    if (value <= 0.0) {
        return 0.0
    }
    return round(value * 10.0) / 10.0
}
