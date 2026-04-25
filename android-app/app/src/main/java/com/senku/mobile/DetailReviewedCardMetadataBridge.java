package com.senku.mobile;

import android.content.Intent;

final class DetailReviewedCardMetadataBridge {
    private static final String EXTRA_REVIEWED_CARD_METADATA = "reviewed_card_metadata";

    private ReviewedCardMetadata current = ReviewedCardMetadata.empty();

    void reset() {
        current = ReviewedCardMetadata.empty();
    }

    void set(ReviewedCardMetadata metadata) {
        current = ReviewedCardMetadata.normalize(metadata);
    }

    ReviewedCardMetadata current() {
        return current;
    }

    void readFrom(Intent intent) {
        current = fromIntent(intent);
    }

    static void putInto(Intent intent, ReviewedCardMetadata metadata) {
        if (intent == null) {
            return;
        }
        intent.putExtra(EXTRA_REVIEWED_CARD_METADATA, ReviewedCardMetadata.normalize(metadata));
    }

    static ReviewedCardMetadata fromIntent(Intent intent) {
        if (intent == null) {
            return ReviewedCardMetadata.empty();
        }
        Object reviewedMetadataExtra = intent.getSerializableExtra(EXTRA_REVIEWED_CARD_METADATA);
        return reviewedMetadataExtra instanceof ReviewedCardMetadata
            ? ReviewedCardMetadata.normalize((ReviewedCardMetadata) reviewedMetadataExtra)
            : ReviewedCardMetadata.empty();
    }

    ReviewedCardMetadata forRuleId(String turnRuleId, String currentRuleId) {
        String turnRule = safe(turnRuleId).trim();
        String currentRule = safe(currentRuleId).trim();
        if (turnRule.isEmpty() || currentRule.isEmpty() || !turnRule.equals(currentRule)) {
            return ReviewedCardMetadata.empty();
        }
        return current;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
