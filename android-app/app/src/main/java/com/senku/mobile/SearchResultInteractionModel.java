package com.senku.mobile;

final class SearchResultInteractionModel {
    final boolean showContinueThreadChip;
    final boolean bindContinueThreadAction;
    final boolean bindLinkedGuideAction;
    final String linkedGuideLabel;
    final String linkedGuideContentDescription;

    SearchResultInteractionModel(
        boolean showContinueThreadChip,
        boolean bindContinueThreadAction,
        boolean bindLinkedGuideAction,
        String linkedGuideLabel,
        String linkedGuideContentDescription
    ) {
        this.showContinueThreadChip = showContinueThreadChip;
        this.bindContinueThreadAction = bindContinueThreadAction;
        this.bindLinkedGuideAction = bindLinkedGuideAction;
        this.linkedGuideLabel = linkedGuideLabel;
        this.linkedGuideContentDescription = linkedGuideContentDescription;
    }

    static SearchResultInteractionModel hidden() {
        return new SearchResultInteractionModel(false, false, false, null, null);
    }

    static SearchResultInteractionModel decide(
        boolean showContinueThreadChip,
        boolean hasLinkedGuideAction,
        String linkedGuideDisplayLabel,
        String linkedGuideId,
        String linkedGuideTitle
    ) {
        String linkedLabel = hasLinkedGuideAction
            ? SearchResultCardModelMapper.buildLinkedGuideChipLabel()
            : null;
        String linkedDescription = hasLinkedGuideAction
            ? SearchResultCardModelMapper.buildLinkedGuideOpenDescription(
                SearchResultCardModelMapper.buildLinkedGuidePreviewLabel(
                    linkedGuideDisplayLabel,
                    linkedGuideId,
                    linkedGuideTitle
                )
            )
            : null;
        return new SearchResultInteractionModel(
            showContinueThreadChip,
            showContinueThreadChip,
            hasLinkedGuideAction,
            linkedLabel,
            linkedDescription
        );
    }
}
