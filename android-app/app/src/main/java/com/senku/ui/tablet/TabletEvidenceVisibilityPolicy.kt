package com.senku.ui.tablet

internal data class TabletEvidenceVisibilityPolicy(
    val evidencePaneWidthDp: Int,
    val landscapeRailDensity: EvidenceRailDensity,
    val activeTitleMaxLines: Int,
    val activeSnippetMaxLines: Int,
    val portraitCollapsedByDefault: Boolean,
    val collapsedTitleMaxLines: Int,
    val collapsedSnippetMaxLines: Int,
)

internal enum class EvidenceRailDensity {
    Full,
    Collapsed,
    Hidden,
}

internal data class TabletEvidenceRailVisibilityInput(
    val detailMode: TabletDetailMode,
    val isLandscape: Boolean,
    val guideMode: Boolean,
    val evidenceExpanded: Boolean,
    val answerSourceCount: Int,
    val threadSourceCount: Int,
)

internal data class TabletEvidenceRailPresentation(
    val visible: Boolean,
    val widthDp: Int,
    val density: EvidenceRailDensity,
)

internal fun tabletEvidenceVisibilityPolicy(): TabletEvidenceVisibilityPolicy =
    TabletEvidenceVisibilityPolicy(
        evidencePaneWidthDp = tabletLandscapeReadingLayoutPolicy().evidenceRailWidthDp,
        landscapeRailDensity = EvidenceRailDensity.Full,
        activeTitleMaxLines = 2,
        activeSnippetMaxLines = 6,
        portraitCollapsedByDefault = true,
        collapsedTitleMaxLines = 2,
        collapsedSnippetMaxLines = 4,
    )

internal fun tabletEvidenceRailPresentation(
    input: TabletEvidenceRailVisibilityInput,
): TabletEvidenceRailPresentation {
    val presentation = when {
        input.detailMode == TabletDetailMode.Thread ->
            TabletEvidenceRailPresentation(
                visible = input.threadSourceCount > 0,
                widthDp = tabletThreadEvidenceRailWidthDp(input.isLandscape),
                density = if (input.isLandscape) EvidenceRailDensity.Full else EvidenceRailDensity.Collapsed,
            )
        input.guideMode ->
            TabletEvidenceRailPresentation(
                visible = input.isLandscape,
                widthDp = tabletGuideReferenceRailWidthDp(input.isLandscape),
                density = if (input.isLandscape) EvidenceRailDensity.Full else EvidenceRailDensity.Hidden,
            )
        input.detailMode == TabletDetailMode.Answer ->
            TabletEvidenceRailPresentation(
                visible = input.evidenceExpanded || input.answerSourceCount > 0,
                widthDp = tabletReadingLayoutPolicy(input.isLandscape).evidenceRailWidthDp,
                density = if (input.isLandscape) EvidenceRailDensity.Full else EvidenceRailDensity.Collapsed,
            )
        else ->
            TabletEvidenceRailPresentation(
                visible = false,
                widthDp = 0,
                density = EvidenceRailDensity.Hidden,
            )
    }
    return if (presentation.visible) {
        presentation
    } else {
        presentation.copy(widthDp = 0, density = EvidenceRailDensity.Hidden)
    }
}

internal fun tabletEvidenceRailPresentation(
    state: TabletDetailState,
    guideMode: Boolean,
): TabletEvidenceRailPresentation =
    tabletEvidenceRailPresentation(
        TabletEvidenceRailVisibilityInput(
            detailMode = state.detailMode,
            isLandscape = state.isLandscape,
            guideMode = guideMode,
            evidenceExpanded = state.evidenceExpanded,
            answerSourceCount = state.resolvedAnswerSourceCount(),
            threadSourceCount = state.resolvedVisibleThreadSourceCount(),
        )
    )

internal fun tabletShouldShowEvidencePane(
    state: TabletDetailState,
    guideMode: Boolean,
): Boolean = tabletEvidenceRailPresentation(state, guideMode).visible
