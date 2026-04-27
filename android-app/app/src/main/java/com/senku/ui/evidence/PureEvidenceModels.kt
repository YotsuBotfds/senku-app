package com.senku.ui.evidence

import java.util.Locale

private const val FallbackGuideId = "GD-?"
private const val FallbackSourceTitle = "Source guide"
private const val FallbackSurfaceLabel = "Evidence"
private const val EmptySnippetLabel = "No snippet yet."
private const val Separator = " | "

private val WhitespaceRegex = Regex("\\s+")
private val LabelDelimiterRegex = Regex("[-_]+")

data class EvidenceSourceModel(
    val guideId: String,
    val title: String,
    val section: String = "",
    val snippet: String = "",
    val label: String = "",
    val isAnchor: Boolean = false,
)

data class EvidenceSurfaceModel(
    val label: String,
    val sources: List<EvidenceSourceModel> = emptyList(),
    val activeSourceIndex: Int = 0,
)

internal data class EvidenceSourceRowData(
    val guideId: String,
    val title: String,
    val metaLabel: String,
    val snippet: String,
    val quotedSnippet: String,
    val isAnchor: Boolean,
    val hasSnippet: Boolean,
)

internal data class EvidenceSurfaceViewData(
    val label: String,
    val sourceCountLabel: String,
    val activeSource: EvidenceSourceRowData?,
    val rows: List<EvidenceSourceRowData>,
    val isEmpty: Boolean,
)

internal fun EvidenceSurfaceModel.toEvidenceSurfaceViewData(): EvidenceSurfaceViewData {
    val rows = sources.map { it.toEvidenceSourceRowData() }
    val activeSource = if (rows.isEmpty()) {
        null
    } else {
        rows[activeSourceIndex.coerceIn(rows.indices)]
    }
    return EvidenceSurfaceViewData(
        label = normalizeEvidenceLabel(label).ifEmpty { FallbackSurfaceLabel },
        sourceCountLabel = sourceCountLabel(rows.size),
        activeSource = activeSource,
        rows = rows,
        isEmpty = rows.isEmpty(),
    )
}

internal fun EvidenceSourceModel.toEvidenceSourceRowData(): EvidenceSourceRowData {
    val normalizedSnippet = normalizeEvidenceSnippet(snippet)
    return EvidenceSourceRowData(
        guideId = guideId.trim().ifEmpty { FallbackGuideId },
        title = normalizeEvidenceTitle(title).ifEmpty { FallbackSourceTitle },
        metaLabel = buildEvidenceSourceMetaLabel(
            label = label,
            section = section,
            isAnchor = isAnchor,
        ),
        snippet = normalizedSnippet,
        quotedSnippet = quoteEvidenceSnippet(normalizedSnippet),
        isAnchor = isAnchor,
        hasSnippet = normalizedSnippet.isNotEmpty(),
    )
}

internal fun normalizeEvidenceLabel(label: String): String =
    label
        .trim()
        .replace(LabelDelimiterRegex, " ")
        .replace(WhitespaceRegex, " ")
        .lowercase(Locale.US)

internal fun normalizeEvidenceTitle(title: String): String =
    title.trim().replace(WhitespaceRegex, " ")

internal fun normalizeEvidenceSnippet(snippet: String): String =
    snippet.trim().replace(WhitespaceRegex, " ")

internal fun quoteEvidenceSnippet(snippet: String): String =
    if (snippet.isEmpty()) {
        EmptySnippetLabel
    } else {
        "\"" + snippet + "\""
    }

internal fun buildEvidenceSourceMetaLabel(
    label: String,
    section: String,
    isAnchor: Boolean,
): String {
    val parts = buildList {
        normalizeEvidenceLabel(label)
            .takeIf { it.isNotEmpty() }
            ?.let(::add)
        normalizeEvidenceTitle(section)
            .takeIf { it.isNotEmpty() }
            ?.let(::add)
        if (isAnchor) {
            add("anchor")
        }
    }
    return parts.joinToString(separator = Separator)
}

private fun sourceCountLabel(sourceCount: Int): String =
    when (sourceCount) {
        0 -> "No sources"
        1 -> "1 source"
        else -> "$sourceCount sources"
    }
