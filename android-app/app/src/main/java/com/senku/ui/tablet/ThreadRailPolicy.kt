package com.senku.ui.tablet

internal fun threadRailSectionTitle(label: String, count: Int): String =
    "${label.trim().ifEmpty { "THREAD" }} \u2022 $count"

internal fun threadRailTurnRowMinHeightDp(active: Boolean): Int =
    if (active) 54 else 48

internal fun threadRailSourceRowMinHeightDp(): Int = 38

internal fun threadRailShouldShowToolbar(guideMode: Boolean): Boolean = guideMode

internal fun threadRailShouldShowSourceSectionHeader(guideMode: Boolean): Boolean = guideMode

internal fun threadRailTurnLabel(index: Int, guideMode: Boolean): String =
    if (guideMode) "SEC $index" else "Q$index"

internal fun threadRailTurnStatusLabel(status: Status, active: Boolean): String =
    when {
        active -> "ACTIVE"
        status == Status.Done -> "DONE"
        status == Status.Pending -> "PENDING"
        else -> "READY"
    }

internal fun threadRailTurnMetaLabel(index: Int, guideMode: Boolean, status: Status, active: Boolean): String =
    "${threadRailTurnLabel(index, guideMode)} \u00B7 ${threadRailTurnStatusLabel(status, active)}"

internal fun threadRailTurnSourceLabel(sourceCount: Int): String =
    "SOURCES \u2022 ${sourceCount.coerceAtLeast(0)}"

internal fun threadRailAnswerLabel(index: Int, guideMode: Boolean): String =
    if (guideMode) "REF $index" else "A$index"

internal fun threadRailAnswerMetaLabel(index: Int, guideMode: Boolean, sourceCount: Int): String =
    if (guideMode) {
        threadRailAnswerLabel(index, guideMode)
    } else {
        threadRailAnswerLabel(index, guideMode)
    }

internal fun threadRailAnswerConfidenceLabel(status: Status, active: Boolean): String =
    when {
        active || status == Status.Active -> "ACTIVE"
        status == Status.Done -> "CONFIDENT"
        status == Status.Pending -> "UNSURE"
        else -> "READY"
    }

internal fun threadRailAnswerPreviewLabel(index: Int, guideMode: Boolean, answer: String): String =
    "${threadRailAnswerLabel(index, guideMode)} \u00B7 ${threadRailAnswerPreviewText(answer)}"

internal fun threadRailAnswerPreviewLabel(
    index: Int,
    guideMode: Boolean,
    answer: String,
    sourceCount: Int,
    status: Status,
    active: Boolean,
): String {
    if (guideMode) {
        return threadRailAnswerPreviewLabel(index, guideMode, answer)
    }
    return "${threadRailAnswerLabel(index, guideMode)} \u00B7 ${threadRailAnswerPreviewText(answer)}"
}

internal fun threadRailAnswerPreviewText(answer: String): String =
    threadRailAnswerLeadText(answer)
        .take(96)
        .trimEnd()
        .ifEmpty { "No answer recorded." }

private fun threadRailAnswerLeadText(answer: String): String {
    var firstCandidate = ""
    var preferNextBodyLine = false
    var skipNextBodyLine = false
    answer.trim().lineSequence().forEach { rawLine ->
        val candidate = rawLine.trim()
        if (candidate.isEmpty()) {
            return@forEach
        }
        if (candidate.isThreadRailAnswerHeading()) {
            if (candidate.isThreadRailQuestionHeading()) {
                skipNextBodyLine = true
                preferNextBodyLine = false
            }
            if (candidate.isThreadRailAnswerLeadHeading()) {
                preferNextBodyLine = true
            }
            if (firstCandidate.isNotEmpty() && candidate.isThreadRailAnswerTrailingHeading()) {
                return firstCandidate
            }
            return@forEach
        }
        if (skipNextBodyLine) {
            skipNextBodyLine = false
            return@forEach
        }
        if (preferNextBodyLine) {
            return candidate
        }
        if (firstCandidate.isEmpty()) {
            firstCandidate = candidate
        }
    }
    return firstCandidate
}

private fun String.isThreadRailAnswerHeading(): Boolean {
    val normalized = normalizeThreadRailAnswerHeading()
    return normalized == "answer" ||
        normalized == "short answer" ||
        normalized == "source match" ||
        normalized == "steps" ||
        normalized == "field steps" ||
        normalized == "limits safety" ||
        normalized == "limits and safety" ||
        normalized == "watch" ||
        normalized == "question" ||
        normalized == "user" ||
        normalized == "field question" ||
        normalized.startsWith("field entry") ||
        normalized.startsWith("uncertain fit") ||
        normalized.startsWith("boundary") ||
        normalized.matches(Regex("\\d+\\..*"))
}

private fun String.isThreadRailAnswerLeadHeading(): Boolean {
    val normalized = normalizeThreadRailAnswerHeading()
    return normalized == "answer" ||
        normalized == "short answer" ||
        normalized == "source match"
}

private fun String.isThreadRailQuestionHeading(): Boolean =
    normalizeThreadRailAnswerHeading() == "field question"

private fun String.isThreadRailAnswerTrailingHeading(): Boolean {
    val normalized = normalizeThreadRailAnswerHeading()
    return normalized == "steps" ||
        normalized == "field steps" ||
        normalized == "limits safety" ||
        normalized == "limits and safety" ||
        normalized == "watch" ||
        normalized.startsWith("uncertain fit") ||
        normalized.startsWith("boundary")
}

private fun String.normalizeThreadRailAnswerHeading(): String =
    trim()
        .replace('&', ' ')
        .replace(':', ' ')
        .replace('-', ' ')
        .replace(Regex("\\s+"), " ")
        .lowercase()

internal fun threadRailSourceContextPriority(source: SourceState): Int {
    val sourceText = "${source.id} ${source.title}".lowercase()
    return when {
        "rain" in sourceText && "shelter" in sourceText -> 3
        "shelter" in sourceText -> 2
        "rain" in sourceText -> 1
        else -> 0
    }
}

internal fun threadRailSourceTitleLabel(source: SourceState, guideMode: Boolean): String {
    val title = source.title.trim()
    return when {
        source.id.isBlank() && title.isConfusingSourceFallbackTitle() -> ""
        title.isNotEmpty() -> title
        guideMode -> "Related guide"
        else -> "Source guide"
    }
}

internal fun threadRailSourceDisplayLabel(
    source: SourceState,
    guideMode: Boolean,
    reviewDemoSourcePolicy: Boolean = false,
): String {
    val sourceId = source.id.trim()
    if (sourceId.isEmpty()) {
        return threadRailSourceTitleLabel(source, guideMode)
    }
    if (!guideMode) {
        return "$sourceId \u2022 ${threadRailSourceRelationLabel(source, reviewDemoSourcePolicy)}"
    }
    return when {
        reviewDemoSourcePolicy && threadRailSourceContextPriority(source) >= 3 -> "$sourceId - RAIN SHELTER"
        source.isAnchor -> "$sourceId - ANCHOR"
        source.isSelected -> "$sourceId - OPEN"
        else -> "$sourceId - RELATED"
    }
}

internal fun threadRailSourceRelationLabel(
    source: SourceState,
    reviewDemoSourcePolicy: Boolean = false,
): String =
    if (reviewDemoSourcePolicy) {
        when (source.id.trim().uppercase()) {
            "GD-220" -> "ANCHOR"
            "GD-345" -> "TOPIC"
            "GD-132" -> "RELATED"
            else -> threadRailGenericSourceRelationLabel(source)
        }
    } else {
        threadRailGenericSourceRelationLabel(source)
    }

internal fun threadRailGenericSourceRelationLabel(source: SourceState): String =
    when {
        source.isAnchor -> "ANCHOR"
        source.isSelected -> "CURRENT"
        else -> "SOURCE"
    }

internal fun threadRailShouldShowSource(
    source: SourceState,
    guideMode: Boolean,
    reviewDemoSourcePolicy: Boolean = false,
): Boolean =
    threadRailSourceDisplayLabel(source, guideMode, reviewDemoSourcePolicy).isNotEmpty() ||
        threadRailSourceTitleLabel(source, guideMode).isNotEmpty()

internal fun threadRailVisibleSources(
    sources: List<SourceState>,
    guideMode: Boolean,
    reviewDemoSourcePolicy: Boolean = false,
): List<SourceState> {
    val visibleSources = sources.filter { threadRailShouldShowSource(it, guideMode, reviewDemoSourcePolicy) }
    if (guideMode && reviewDemoSourcePolicy) {
        return visibleSources.sortedWith(compareBy<SourceState> {
            threadRailGuideSourceOrder(it, reviewDemoSourcePolicy = true)
        })
    }
    return visibleSources
}

internal fun threadRailGuideSourceOrder(
    source: SourceState,
    reviewDemoSourcePolicy: Boolean = false,
): Int =
    if (reviewDemoSourcePolicy) {
        when (source.id.trim().uppercase()) {
            "GD-220" -> 0
            "GD-132" -> 1
            "GD-345" -> 2
            else -> 10 - threadRailSourceContextPriority(source)
        }
    } else {
        0
    }

private fun String.isConfusingSourceFallbackTitle(): Boolean =
    trim().equals("Field note summary", ignoreCase = true)

internal fun threadRailTurnMetaLabel(
    index: Int,
    guideMode: Boolean,
    status: Status,
    active: Boolean,
    sourceCount: Int,
): String = if (guideMode) {
    threadRailTurnLabel(index, guideMode)
} else {
    "${threadRailTurnLabel(index, guideMode)} \u00B7 FIELD QUESTION"
}
