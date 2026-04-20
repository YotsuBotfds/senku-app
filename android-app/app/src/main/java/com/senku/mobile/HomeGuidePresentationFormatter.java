package com.senku.mobile;

import android.content.Context;

final class HomeGuidePresentationFormatter {
    private final Context context;
    private final MainPresentationFormatter baseFormatter;

    HomeGuidePresentationFormatter(Context context, MainPresentationFormatter baseFormatter) {
        this.context = context;
        this.baseFormatter = baseFormatter;
    }

    String buildHomeRelatedGuideButtonLabel(SearchResult result, MainActivity.HomeGuideAnchor anchor) {
        String primaryLine = baseFormatter.buildGuideButtonLabel(result);
        String contextLine = baseFormatter.clipLabel(buildHomeRelatedGuideContextLine(anchor, result), 52);
        if (contextLine.isEmpty()) {
            return primaryLine;
        }
        return primaryLine + "\n" + contextLine;
    }

    String buildHomeRelatedGuideContentDescription(
        SearchResult result,
        MainActivity.HomeGuideAnchor anchor,
        int index,
        int total
    ) {
        String title = safe(result == null ? null : result.title).trim();
        String guideId = safe(result == null ? null : result.guideId).trim();
        String label = !title.isEmpty() ? title : guideId;
        StringBuilder builder = new StringBuilder();
        builder.append(context.getString(
            R.string.external_review_home_related_button_content_description,
            index + 1,
            total,
            label
        ));
        if (!guideId.isEmpty() && !guideId.equalsIgnoreCase(label)) {
            builder.append(". Guide ").append(guideId);
        }
        String reason = buildHomeRelatedGuideReason(anchor);
        if (!reason.isEmpty()) {
            builder.append(". ").append(reason);
        }
        String section = safe(result == null ? null : result.sectionHeading).trim();
        if (!section.isEmpty()) {
            builder.append(". Section ").append(section);
        }
        String category = humanizeMetadataLabel(result == null ? null : result.category);
        if (!category.isEmpty()) {
            builder.append(". Category ").append(category);
        }
        String subtitle = baseFormatter.cleanHomeRelatedGuideSubtitle(result);
        if (!subtitle.isEmpty() && !normalizeBucketText(subtitle).equals(normalizeBucketText(section))) {
            builder.append(". Context ").append(subtitle);
        }
        builder.append(". Tap to open.");
        return builder.toString();
    }

    String buildHomeRelatedSubtitle(MainActivity.HomeGuideAnchor anchor, int guideCount) {
        if (anchor == null || safe(anchor.guideId).trim().isEmpty()) {
            return "";
        }
        String pivotToken = baseFormatter.buildGuideAnchorToken(anchor.guideId, anchor.label);
        if (pivotToken.isEmpty()) {
            pivotToken = baseFormatter.clipLabel(
                safe(anchor.label).trim().isEmpty() ? anchor.guideId : anchor.label,
                28
            );
        }
        if (guideCount > 0) {
            int subtitleRes = anchor.fromRecentThread
                ? R.plurals.external_review_home_related_subtitle_recent_count
                : R.plurals.external_review_home_related_subtitle_pinned_count;
            return context.getResources().getQuantityString(subtitleRes, guideCount, guideCount, pivotToken);
        }
        return context.getString(
            anchor.fromRecentThread
                ? R.string.external_review_home_related_empty_recent
                : R.string.external_review_home_related_empty_pinned,
            pivotToken
        );
    }

    String buildHomeRelatedAnchorButtonLabel(MainActivity.HomeGuideAnchor anchor, boolean compactPhoneHome) {
        if (anchor == null || safe(anchor.guideId).trim().isEmpty()) {
            return "";
        }
        if (!compactPhoneHome) {
            return buildHomeRelatedAnchorLine(anchor);
        }
        String label = baseFormatter.buildGuideAnchorToken(anchor.guideId, anchor.label);
        if (label.isEmpty()) {
            label = baseFormatter.clipLabel(
                safe(anchor.label).trim().isEmpty() ? anchor.guideId : anchor.label,
                18
            );
        }
        return context.getString(R.string.external_review_home_related_anchor_compact_label, label);
    }

    String buildHomeRelatedAnchorContentDescription(MainActivity.HomeGuideAnchor anchor) {
        if (anchor == null || safe(anchor.guideId).trim().isEmpty()) {
            return "";
        }
        int labelRes = anchor.fromRecentThread
            ? R.string.external_review_home_related_anchor_recent_pivot_content_description
            : R.string.external_review_home_related_anchor_pinned_pivot_content_description;
        String label = baseFormatter.clipLabel(
            safe(anchor.label).trim().isEmpty() ? anchor.guideId : anchor.label,
            72
        );
        return context.getString(labelRes, label);
    }

    String buildHomeRelatedEmptyState(MainActivity.HomeGuideAnchor anchor) {
        if (anchor == null || safe(anchor.guideId).trim().isEmpty()) {
            return "";
        }
        String pivotToken = baseFormatter.buildGuideAnchorToken(anchor.guideId, anchor.label);
        if (pivotToken.isEmpty()) {
            pivotToken = baseFormatter.clipLabel(
                safe(anchor.label).trim().isEmpty() ? anchor.guideId : anchor.label,
                28
            );
        }
        return context.getString(
            anchor.fromRecentThread
                ? R.string.external_review_home_related_empty_recent
                : R.string.external_review_home_related_empty_pinned,
            pivotToken
        );
    }

    private String buildHomeRelatedGuideContextLine(MainActivity.HomeGuideAnchor anchor, SearchResult result) {
        String pivotToken = baseFormatter.buildGuideAnchorToken(
            anchor == null ? null : anchor.guideId,
            anchor == null ? null : anchor.label
        );
        String guideAnchorContext = baseFormatter.buildGuideAnchorContextLabel(result);
        if (!pivotToken.isEmpty() && !guideAnchorContext.isEmpty()) {
            return context.getString(
                R.string.external_review_home_related_context_from_pivot_with_anchor,
                pivotToken,
                guideAnchorContext
            );
        }
        if (!pivotToken.isEmpty()) {
            return context.getString(R.string.external_review_home_related_context_from_pivot, pivotToken);
        }
        if (!guideAnchorContext.isEmpty()) {
            return guideAnchorContext;
        }
        return baseFormatter.cleanHomeRelatedGuideSubtitle(result);
    }

    private String buildHomeRelatedGuideReason(MainActivity.HomeGuideAnchor anchor) {
        if (anchor == null || safe(anchor.guideId).trim().isEmpty()) {
            return "";
        }
        String pivotLabel = safe(anchor.label).trim();
        if (pivotLabel.isEmpty()) {
            pivotLabel = anchor.guideId;
        }
        String pivotToken = baseFormatter.buildGuideAnchorToken(anchor.guideId, pivotLabel);
        if (!pivotToken.isEmpty()) {
            pivotLabel = pivotToken;
        }
        if (anchor.fromRecentThread) {
            return context.getString(R.string.external_review_home_related_reason_recent, pivotLabel);
        }
        return context.getString(R.string.external_review_home_related_reason_pinned, pivotLabel);
    }

    private String buildHomeRelatedAnchorLine(MainActivity.HomeGuideAnchor anchor) {
        if (anchor == null || safe(anchor.guideId).trim().isEmpty()) {
            return "";
        }
        int labelRes = anchor.fromRecentThread
            ? R.string.external_review_home_related_anchor_recent_pivot_label
            : R.string.external_review_home_related_anchor_pinned_pivot_label;
        String label = baseFormatter.buildGuideAnchorToken(anchor.guideId, anchor.label);
        if (label.isEmpty()) {
            label = baseFormatter.clipLabel(
                safe(anchor.label).trim().isEmpty() ? anchor.guideId : anchor.label,
                28
            );
        }
        return context.getString(labelRes, label);
    }

    private String humanizeMetadataLabel(String value) {
        String text = safe(value).trim();
        if (text.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(text.length());
        boolean capitalizeNext = true;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '-' || character == '_' || Character.isWhitespace(character)) {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != ' ') {
                    builder.append(' ');
                }
                capitalizeNext = true;
                continue;
            }
            builder.append(capitalizeNext ? Character.toUpperCase(character) : character);
            capitalizeNext = false;
        }
        return builder.toString().trim();
    }

    private String normalizeBucketText(String value) {
        return safe(value).trim().toLowerCase(java.util.Locale.US);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
