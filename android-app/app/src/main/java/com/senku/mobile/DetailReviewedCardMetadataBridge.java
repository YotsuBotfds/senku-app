package com.senku.mobile;

import android.content.Intent;

final class DetailReviewedCardMetadataBridge {
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
        intent.putExtra(ReviewedCardMetadata.TRANSPORT_FIELD, ReviewedCardMetadata.normalize(metadata));
    }

    static ReviewedCardMetadata fromIntent(Intent intent) {
        if (intent == null) {
            return ReviewedCardMetadata.empty();
        }
        return ReviewedCardMetadata.fromSerializable(
            intent.getSerializableExtra(ReviewedCardMetadata.TRANSPORT_FIELD)
        );
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
