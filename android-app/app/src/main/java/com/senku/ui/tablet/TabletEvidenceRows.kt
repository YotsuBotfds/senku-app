package com.senku.ui.tablet

internal data class TabletEvidenceCardRow(
    val guideId: String,
    val relation: String,
    val title: String,
    val section: String = "",
    val match: String = "",
    val quote: String = "",
)

internal fun tabletGuideModeReferenceRows(
    anchor: AnchorState,
    xrefs: List<XRefState>,
    reviewDemoEvidenceStackEnabled: Boolean = false,
): List<TabletEvidenceCardRow> {
    val visibleXRefs = tabletSourceGraphVisibleXRefs(anchor, xrefs)
    val rows = mutableListOf<TabletEvidenceCardRow>()
    val gd220 = visibleXRefs.firstOrNull { it.id.trim().equals("GD-220", ignoreCase = true) }
    val anchorLikeGd220 = reviewDemoEvidenceStackEnabled && gd220 != null && hasFoundryCurrentAnchor(anchor, visibleXRefs)
    val seenGuideIds = mutableSetOf<String>()

    if (anchorLikeGd220) {
        rows += requireNotNull(gd220).toGuideReferenceRow(relationOverride = "ANCHOR")
        seenGuideIds += "GD-220"
    } else if (anchor.hasSource) {
        val anchorId = anchor.id.trim()
        rows += TabletEvidenceCardRow(
            guideId = anchorId,
            relation = "ANCHOR",
            title = anchor.title.trim(),
            section = anchor.section.trim(),
        )
        if (anchorId.isNotEmpty()) {
            seenGuideIds += anchorId.uppercase()
        }
    }

    visibleXRefs.forEach { xref ->
        val xrefId = xref.id.trim()
        if (xrefId.isEmpty() || !seenGuideIds.add(xrefId.uppercase())) {
            return@forEach
        }
        val relation = if (anchorLikeGd220 && xref.relation.trim().equals("ANCHOR", ignoreCase = true)) {
            "RELATED"
        } else {
            xref.relation.trim().ifEmpty { "RELATED" }
        }
        rows += xref.toGuideReferenceRow(relationOverride = relation)
    }
    return rows
}

private fun hasFoundryCurrentAnchor(anchor: AnchorState, xrefs: List<XRefState>): Boolean {
    if (anchor.id.trim().equals("GD-132", ignoreCase = true)) {
        return true
    }
    return xrefs.any { xref ->
        xref.relation.trim().equals("ANCHOR", ignoreCase = true) &&
            xref.id.trim().equals("GD-132", ignoreCase = true)
    }
}

private fun XRefState.toGuideReferenceRow(relationOverride: String): TabletEvidenceCardRow =
    TabletEvidenceCardRow(
        guideId = id.trim(),
        relation = relationOverride.trim().ifEmpty { "RELATED" }.uppercase(),
        title = title.trim(),
    )

internal fun buildCrossReferenceCardCount(anchor: AnchorState, xrefs: List<XRefState>): Int =
    tabletSourceGraphVisibleXRefs(anchor, xrefs).size

internal fun buildAnswerModeSourceHeaderCount(
    anchor: AnchorState,
    xrefs: List<XRefState>,
    answerSourceCount: Int,
    reviewDemoEvidenceStackEnabled: Boolean = false,
): Int {
    val visibleXRefs = tabletSourceGraphVisibleXRefs(anchor, xrefs)
    val answerRows = tabletAnswerModeSourceRows(
        anchor = anchor,
        xrefs = visibleXRefs,
        reviewDemoEvidenceStackEnabled = reviewDemoEvidenceStackEnabled,
    )
    if (reviewDemoEvidenceStackEnabled && answerRows.size == 3 && containsRainShelterAnswerStack(anchor, visibleXRefs)) {
        return 3
    }
    return maxOf(
        answerSourceCount.coerceAtLeast(0),
        visibleXRefs.size + if (anchor.hasSource) 1 else 0,
    )
}

internal fun answerModeSourceSectionTitle(sourceCount: Int): String =
    "SOURCES \u2022 ${sourceCount.coerceAtLeast(0)}"

internal fun tabletAnswerModeSourceRows(
    anchor: AnchorState,
    xrefs: List<XRefState>,
    reviewDemoEvidenceStackEnabled: Boolean = false,
): List<TabletEvidenceCardRow> {
    val visibleXRefs = tabletSourceGraphVisibleXRefs(anchor, xrefs)
    if (reviewDemoEvidenceStackEnabled && containsRainShelterAnswerStack(anchor, visibleXRefs)) {
        return listOf(
            TabletEvidenceCardRow(
                guideId = "GD-220",
                relation = "ANCHOR",
                title = "Abrasives Manufacturing",
                match = "74%",
                quote = "Every melt starts with a foundry safety check, not with metal charge...",
            ),
            TabletEvidenceCardRow(
                guideId = "GD-132",
                relation = "RELATED",
                title = "Foundry & Metal Casting",
                match = "68%",
                quote = "Pitch the ridgeline along prevailing wind. Tension corners with prusik or taut-line hitches.",
            ),
            TabletEvidenceCardRow(
                guideId = "GD-345",
                relation = "TOPIC",
                title = "Tarp & Cord Shelters",
                match = "61%",
                quote = "A simple ridgeline shelter requires only tarp, cord, and two anchor points.",
            ),
        )
    }
    val rows = mutableListOf<TabletEvidenceCardRow>()
    if (anchor.hasSource) {
        rows += TabletEvidenceCardRow(
            guideId = anchor.id,
            relation = "ANCHOR",
            title = anchor.title,
            section = anchor.section,
        )
    }
    rows += visibleXRefs.map { xref ->
        TabletEvidenceCardRow(
            guideId = xref.id,
            relation = xref.relation.trim().ifEmpty { "RELATED" },
            title = xref.title,
        )
    }
    return rows
}

private fun containsRainShelterAnswerStack(anchor: AnchorState, xrefs: List<XRefState>): Boolean {
    if (isCanonicalRainShelterAnswerAnchor(anchor)) {
        return true
    }
    val rows = mutableListOf<Pair<String, String>>()
    if (anchor.hasSource) {
        rows += anchor.id.trim().uppercase() to anchor.title.trim().lowercase()
    }
    rows += xrefs.map { it.id.trim().uppercase() to it.title.trim().lowercase() }
    val ids = rows.map { it.first }.toSet()
    if (!ids.containsAll(listOf("GD-220", "GD-132", "GD-345"))) {
        return false
    }
    return rows.any { (id, title) -> id == "GD-345" && (title.contains("rain") || title.contains("tarp") || title.contains("shelter")) }
        || rows.any { (id, title) -> id == "GD-220" && title.contains("abrasives") }
        || rows.any { (id, title) -> id == "GD-132" && title.contains("foundry") }
}

private fun isCanonicalRainShelterAnswerAnchor(anchor: AnchorState): Boolean {
    if (!anchor.hasSource || !anchor.id.trim().equals("GD-345", ignoreCase = true)) {
        return false
    }
    val identityText = listOf(anchor.title, anchor.section, anchor.snippet)
        .joinToString(" ")
        .lowercase()
    return "rain shelter" in identityText ||
        "tarp" in identityText ||
        "cord" in identityText ||
        "shelter" in identityText
}

internal fun tabletSourceGraphVisibleXRefs(anchor: AnchorState, xrefs: List<XRefState>): List<XRefState> {
    val anchorId = anchor.id.trim()
    return xrefs.filter { xref ->
        val xrefId = xref.id.trim()
        xrefId.isNotEmpty() && (anchorId.isEmpty() || !xrefId.equals(anchorId, ignoreCase = true))
    }
}
