@file:JvmName("SearchResultCardKt")
@file:JvmMultifileClass

package com.senku.ui.search

import java.util.ArrayList
import java.util.LinkedHashSet
import java.util.Locale

private const val WARM_THREAD_WINDOW_MS = 30L * 60L * 1000L
private const val SCORE_TICK_TRACK_WIDTH_DP = 22

internal fun compactSearchResultMetadataLabel(metadataLine: String): String {
    val cleaned = metadataLine.trim()
    if (cleaned.isEmpty()) {
        return ""
    }
    val valuesByKey = LinkedHashMap<String, String>()
    for (token in cleaned.split("//")) {
        val parts = token.trim().split(":", limit = 2)
        if (parts.size == 2) {
            val key = parts[0].trim().lowercase(Locale.US)
            val value = parts[1].trim()
            if (value.isNotEmpty()) {
                valuesByKey[key] = value
            }
        }
    }
    if (valuesByKey.isEmpty()) {
        return cleaned.uppercase(Locale.US)
    }
    val tokens = ArrayList<String>(3)
    valuesByKey["category"]?.let { tokens.add(it.uppercase(Locale.US)) }
    valuesByKey["role"]?.let { tokens.add(it.uppercase(Locale.US)) }
    valuesByKey["window"]?.let { tokens.add("WINDOW ${compactWindowMetadataToken(it).uppercase(Locale.US)}") }
    return tokens.joinToString(" \u00B7 ")
}

private fun compactWindowMetadataToken(value: String): String {
    return when (value.trim().lowercase(Locale.US).replace("_", "-").replace(" ", "-")) {
        "long-term", "longterm" -> "Long"
        "short-term", "shortterm" -> "Short"
        else -> value.trim()
    }
}

internal fun compactResultPreviewText(subtitle: String, snippet: String): String {
    val cleanedSubtitle = subtitle.trim()
    val cleanedSnippet = stripPreviewMarkdown(snippet).trim()
    if (isGuideCategorySubtitle(cleanedSubtitle) && cleanedSnippet.isNotEmpty()) {
        return collapseRepeatedPreviewLead(cleanedSnippet, "")
    }
    if (cleanedSubtitle.isEmpty() || cleanedSnippet.startsWith(cleanedSubtitle)) {
        return collapseRepeatedPreviewLead(cleanedSnippet, cleanedSubtitle)
    }
    if (cleanedSnippet.isEmpty()) {
        return cleanedSubtitle
    }
    return "$cleanedSubtitle: ${collapseRepeatedPreviewLead(cleanedSnippet, cleanedSubtitle)}"
}

private fun isGuideCategorySubtitle(value: String): Boolean {
    return Regex("(?i)^GD-\\d{3}\\s*[\\u00B7-]\\s*[A-Z][A-Za-z ]+$").matches(value.trim())
}

private fun stripPreviewMarkdown(value: String): String {
    return value
        .replace("\r", "\n")
        .replace(Regex("!\\[([^\\]]*)\\]\\([^)]*\\)"), "$1")
        .replace(Regex("\\[([^\\]]+)]\\([^)]*\\)"), "$1")
        .replace(Regex("(?m)^\\s*#{1,6}\\s*"), "")
        .replace(Regex("(?m)^\\s*>+\\s*"), "")
        .replace(Regex("(?m)^\\s*[-*+]\\s+\\[[ xX]\\]\\s*"), "")
        .replace(Regex("(?m)^\\s*[-*+]\\s+"), "")
        .replace(Regex("`|\\*\\*|__|~~"), "")
        .replace(Regex("\\s+"), " ")
        .replace(Regex("(?i)^Guide\\s*:\\s*[A-Z]{2}-\\d{3}\\s+"), "")
        .replace(Regex("(?i)^Guide\\s*:\\s+"), "")
}

internal fun scoreTickFillFraction(rankLabel: String): Float {
    val score = rankLabel.trim().toIntOrNull() ?: return 0.72f
    return when {
        score >= 90 -> 0.94f
        score >= 75 -> 0.82f
        score >= 70 -> 0.74f
        score >= 60 -> 0.62f
        else -> 0.52f
    }
}

internal fun scoreTickTrackWidthDp(rankLabel: String): Int {
    return SCORE_TICK_TRACK_WIDTH_DP
}

private fun collapseRepeatedPreviewLead(value: String, lead: String): String {
    val cleaned = value.trim()
    val cleanedLead = lead.trim()
    if (cleanedLead.isEmpty() || !cleaned.startsWith(cleanedLead, ignoreCase = true)) {
        return cleaned
    }
    val remainder = stripLeadingPreviewJoiners(cleaned.drop(cleanedLead.length))
    if (!remainder.startsWith(cleanedLead, ignoreCase = true)) {
        return cleaned
    }
    val secondRemainder = stripLeadingPreviewJoiners(remainder.drop(cleanedLead.length))
    return if (secondRemainder.isEmpty()) {
        cleanedLead
    } else {
        "$cleanedLead: $secondRemainder"
    }
}

private fun stripLeadingPreviewJoiners(value: String): String {
    var cleaned = value.trim()
    while (cleaned.isNotEmpty() && cleaned.first() in listOf(':', '-', '\u2013', '\u2014')) {
        cleaned = cleaned.drop(1).trim()
    }
    return cleaned
}

fun laneLabelForRetrievalMode(retrievalMode: String): String {
    val normalized = retrievalMode.trim().lowercase(Locale.US)
    return when (normalized) {
        "vector" -> "Concept match"
        "lexical" -> "Keyword match"
        "guide-focus", "guide" -> "Related guide"
        "route-focus", "hybrid" -> "Best match"
        else -> "Best match"
    }
}

fun honestRankLabel(rankLabel: String): String {
    val cleaned = rankLabel.trim()
    return if (cleaned.isEmpty()) {
        "#1"
    } else {
        cleaned
    }
}

fun metadataLineForSearchResultCard(
    role: String,
    window: String,
    category: String,
): String {
    val tokens = ArrayList<String>(3)
    normalizedMetadataValue(role)?.let { tokens.add("Role: $it") }
    normalizedMetadataValue(window)?.let { tokens.add("Window: $it") }
    normalizedMetadataValue(category)?.let { tokens.add("Category: $it") }
    return tokens.joinToString(" // ")
}

private fun normalizedMetadataValue(value: String): String? {
    val cleaned = value.trim()
    if (cleaned.isEmpty()) {
        return null
    }
    val normalized = cleaned.lowercase(Locale.US)
    if (normalized == "general" || normalized == "unknown" || normalized == "none") {
        return null
    }
    return cleaned
}

fun continueConversationContentDescription(guideId: String = ""): String {
    val cleanedGuideId = guideId.trim()
    return if (cleanedGuideId.isEmpty()) {
        "Continue conversation about this result"
    } else {
        "Continue conversation about $cleanedGuideId"
    }
}

fun relatedGuideContentDescription(guideLabel: String = ""): String {
    val cleanedGuideLabel = guideLabel.trim()
    return if (cleanedGuideLabel.isEmpty()) {
        "Open cross-reference guide"
    } else {
        "Open cross-reference guide: $cleanedGuideLabel"
    }
}

internal fun isWarmConversation(
    lastActivityEpoch: Long,
    nowEpochMs: Long = System.currentTimeMillis(),
): Boolean {
    if (lastActivityEpoch <= 0L) {
        return false
    }
    val elapsed = nowEpochMs - lastActivityEpoch
    return elapsed in 0L..WARM_THREAD_WINDOW_MS
}

internal fun warmThreadGuideIdsForPreview(
    lastActivityEpoch: Long,
    sourceGuideIds: List<String>,
    nowEpochMs: Long = System.currentTimeMillis(),
): Set<String> {
    if (!isWarmConversation(lastActivityEpoch, nowEpochMs) || sourceGuideIds.isEmpty()) {
        return emptySet()
    }
    val guideIds = LinkedHashSet<String>(sourceGuideIds.size)
    for (guideId in sourceGuideIds) {
        val normalized = guideId.trim()
        if (normalized.isNotEmpty()) {
            guideIds.add(normalized.lowercase(Locale.US))
        }
    }
    return guideIds
}
