package com.senku.ui.evidence

import com.senku.ui.answer.answerEvidenceSourceCountLabel
import java.util.Locale

private const val FallbackSourceTitle = "Source guide"
private const val FallbackSurfaceLabel = "Evidence"
private const val EmptySnippetLabel = "No snippet yet."
private const val Separator = " | "
private const val AnchorLabel = "anchor"
private const val AnchorGuideLabel = "anchor guide"

private val WhitespaceRegex = Regex("\\s+")
private val LabelDelimiterRegex = Regex("[-_]+")

internal enum class EvidenceAnchorLabelStyle {
    Short,
    Guide,
}

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
    val section: String,
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
        sourceCountLabel = answerEvidenceSourceCountLabel(rows.size),
        activeSource = activeSource,
        rows = rows,
        isEmpty = rows.isEmpty(),
    )
}

internal fun EvidenceSourceModel.toEvidenceSourceRowData(): EvidenceSourceRowData {
    val normalizedSnippet = normalizeEvidenceSnippet(snippet)
    return EvidenceSourceRowData(
        guideId = normalizeEvidenceGuideId(guideId),
        title = normalizeEvidenceSourceTitle(title),
        section = normalizeEvidenceTitle(section),
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

internal fun normalizeEvidenceGuideId(guideId: String): String =
    guideId.trim()

internal fun normalizeEvidenceSourceTitle(title: String): String =
    normalizeEvidenceTitle(title).ifEmpty { FallbackSourceTitle }

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
            add(evidenceAnchorLabel(EvidenceAnchorLabelStyle.Short))
        }
    }
    return parts.joinToString(separator = Separator)
}

internal fun evidenceAnchorLabel(style: EvidenceAnchorLabelStyle): String =
    when (style) {
        EvidenceAnchorLabelStyle.Short -> AnchorLabel
        EvidenceAnchorLabelStyle.Guide -> AnchorGuideLabel
    }
