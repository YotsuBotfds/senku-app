package com.senku.mobile;

import android.content.Context;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class MainPresentationFormatter {
    private static final long MILLIS_PER_MINUTE = 60_000L;
    private static final long MINUTES_PER_HOUR = 60L;
    private static final long HOURS_PER_DAY = 24L;

    private final Context context;

    MainPresentationFormatter(Context context) {
        this.context = context;
    }

    String buildGuideButtonLabel(SearchResult result) {
        String guideId = safe(result == null ? null : result.guideId).trim();
        String title = safe(result == null ? null : result.title).trim();
        if (!guideId.isEmpty() && !title.isEmpty()) {
            return guideId + " | " + clipLabel(title, 24);
        }
        if (!title.isEmpty()) {
            return clipLabel(title, 28);
        }
        return guideId;
    }

    String clipLabel(String text, int maxLength) {
        String value = safe(text).trim();
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 1)).trim() + "\u2026";
    }

    String buildRecentThreadLabel(ChatSessionStore.ConversationPreview preview) {
        if (preview == null || preview.latestTurn == null) {
            return "";
        }
        String question = clipLabel(preview.latestTurn.question, 54);
        StringBuilder builder = new StringBuilder(question);
        String metaLine = clipLabel(buildRecentThreadMetaLine(preview), 72);
        if (!metaLine.isEmpty()) {
            builder.append("\n").append(metaLine);
        }
        builder.append("\n").append(context.getString(R.string.recent_thread_continue));
        return builder.toString();
    }

    String buildCompactRecentThreadLabel(ChatSessionStore.ConversationPreview preview) {
        if (preview == null || preview.latestTurn == null) {
            return "";
        }
        String question = clipLabel(preview.latestTurn.question, 38);
        String metaLine = clipLabel(buildRecentThreadMetaLine(preview), 44);
        if (metaLine.isEmpty()) {
            return question;
        }
        return question + "\n" + metaLine;
    }

    String buildRecentThreadContentDescription(ChatSessionStore.ConversationPreview preview, int index) {
        if (preview == null || preview.latestTurn == null) {
            return "Recent thread";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Recent thread ").append(index + 1).append(": ").append(safe(preview.latestTurn.question));
        String metaLine = buildRecentThreadMetaLine(preview);
        if (!metaLine.isEmpty()) {
            builder.append(". ").append(metaLine);
        }
        builder.append(". Tap to continue thread. Long press to remove.");
        return builder.toString();
    }

    String buildRecentThreadSubtitle(ChatSessionStore.ConversationPreview preview) {
        SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
        if (turn == null) {
            return "";
        }
        ArrayList<String> parts = new ArrayList<>();
        parts.add(isDeterministicThread(turn)
            ? context.getString(R.string.detail_route_deterministic)
            : context.getString(R.string.recent_thread_resume_title));
        int sourceCount = turn.sourceResults == null ? 0 : turn.sourceResults.size();
        if (sourceCount > 0) {
            parts.add(sourceCount + (sourceCount == 1 ? " source" : " sources"));
        }
        int turnCount = preview == null ? 0 : preview.turnCount;
        if (turnCount > 0) {
            parts.add(turnCount + (turnCount == 1 ? " turn" : " turns"));
        }
        return String.join(" | ", parts);
    }

    String buildRecentThreadMetaLine(ChatSessionStore.ConversationPreview preview) {
        SessionMemory.TurnSnapshot turn = preview == null ? null : preview.latestTurn;
        if (turn == null) {
            return "";
        }
        ArrayList<String> parts = new ArrayList<>();
        String primaryGuideId = resolvePreviewPrimaryGuideId(preview);
        if (!primaryGuideId.isEmpty()) {
            parts.add(primaryGuideId);
        }
        parts.add(isDeterministicThread(turn)
            ? context.getString(R.string.detail_route_deterministic_short)
            : context.getString(R.string.detail_route_generated_short));
        String relativeTime = buildRelativeTimeLabel(preview == null ? 0L : preview.lastActivityEpoch);
        if (!relativeTime.isEmpty()) {
            parts.add(relativeTime);
        }
        return String.join(" | ", parts);
    }

    String resolvePreviewPrimaryGuideId(ChatSessionStore.ConversationPreview preview) {
        if (preview == null || preview.latestTurn == null || preview.latestTurn.sourceResults == null) {
            return "";
        }
        for (SearchResult source : preview.latestTurn.sourceResults) {
            String guideId = safe(source == null ? null : source.guideId).trim();
            if (!guideId.isEmpty()) {
                return guideId;
            }
        }
        return "";
    }

    String resolvePreviewPrimaryGuideTitle(ChatSessionStore.ConversationPreview preview, String primaryGuideId) {
        if (preview == null || preview.latestTurn == null || preview.latestTurn.sourceResults == null) {
            return "";
        }
        String normalizedGuideId = safe(primaryGuideId).trim();
        for (SearchResult source : preview.latestTurn.sourceResults) {
            String guideId = safe(source == null ? null : source.guideId).trim();
            if (!guideId.equalsIgnoreCase(normalizedGuideId)) {
                continue;
            }
            return safe(source.title).trim();
        }
        return "";
    }

    String buildGuideReference(SearchResult result, String fallbackGuideId) {
        if (result == null) {
            return safe(fallbackGuideId).trim();
        }
        return buildGuideReference(result.guideId, result.title);
    }

    String buildGuideReference(String guideId, String title) {
        String normalizedGuideId = safe(guideId).trim();
        String normalizedTitle = safe(title).trim();
        if (!normalizedGuideId.isEmpty() && !normalizedTitle.isEmpty()) {
            return normalizedGuideId + " - " + normalizedTitle;
        }
        if (!normalizedTitle.isEmpty()) {
            return normalizedTitle;
        }
        return normalizedGuideId;
    }

    String buildLinkedGuideFallbackLabel(String displayLabel, String guideId, String title) {
        String label = safe(displayLabel).trim();
        if (!label.isEmpty()) {
            return label;
        }
        return buildGuideReference(guideId, title);
    }

    String buildGuideAnchorToken(String guideId, String label) {
        String tokenValue = safe(guideId).trim();
        if (tokenValue.isEmpty()) {
            tokenValue = safe(label).trim();
            int separatorIndex = tokenValue.indexOf(" - ");
            if (separatorIndex > 0) {
                tokenValue = tokenValue.substring(0, separatorIndex).trim();
            }
            tokenValue = clipLabel(tokenValue, 18);
        }
        if (tokenValue.isEmpty()) {
            return "";
        }
        return "[" + tokenValue + "]";
    }

    String cleanHomeRelatedGuideSubtitle(SearchResult result) {
        if (result == null) {
            return "";
        }
        String subtitle = safe(result.subtitle).trim();
        if (subtitle.isEmpty()) {
            return "";
        }
        String retrievalMode = safe(result.retrievalMode).trim();
        int lastPipe = subtitle.lastIndexOf('|');
        if (lastPipe > 0 && (!retrievalMode.isEmpty() || !"text".equals(extractRetrievalMode(result)))) {
            subtitle = subtitle.substring(0, lastPipe).trim();
        }
        String guideId = safe(result.guideId).trim();
        if (!guideId.isEmpty()) {
            String prefixedGuideId = guideId + " |";
            if (subtitle.regionMatches(true, 0, prefixedGuideId, 0, prefixedGuideId.length())) {
                subtitle = subtitle.substring(prefixedGuideId.length()).trim();
            } else if (subtitle.equalsIgnoreCase(guideId)) {
                return "";
            }
        }
        return subtitle;
    }

    String buildGuideAnchorContextLabel(SearchResult result) {
        String section = safe(result == null ? null : result.sectionHeading).trim();
        String category = humanizeMetadataLabel(result == null ? null : result.category);
        if (!section.isEmpty() && !category.isEmpty()) {
            if (normalizeBucketText(section).equals(normalizeBucketText(category))) {
                return section;
            }
            return section + " - " + category;
        }
        if (!section.isEmpty()) {
            return section;
        }
        if (!category.isEmpty()) {
            return category;
        }
        return cleanHomeRelatedGuideSubtitle(result);
    }

    String buildGuideHandoffAnchorLabel(SearchResult result, String fallbackGuideId) {
        String guideReference = buildGuideReference(result, fallbackGuideId);
        String anchorContext = buildGuideAnchorContextLabel(result);
        if (guideReference.isEmpty()) {
            return anchorContext;
        }
        if (anchorContext.isEmpty()) {
            return guideReference;
        }
        if (normalizeBucketText(guideReference).contains(normalizeBucketText(anchorContext))) {
            return guideReference;
        }
        return guideReference + " [" + anchorContext + "]";
    }

    String buildRelativeTimeLabel(long recordedAtEpoch) {
        if (recordedAtEpoch <= 0L) {
            return "";
        }
        long elapsedMillis = Math.max(0L, System.currentTimeMillis() - recordedAtEpoch);
        long totalMinutes = elapsedMillis / MILLIS_PER_MINUTE;
        long totalHours = totalMinutes / MINUTES_PER_HOUR;
        long minutes = totalMinutes % MINUTES_PER_HOUR;
        long days = totalHours / HOURS_PER_DAY;
        if (days > 0L) {
            long hours = totalHours % HOURS_PER_DAY;
            return context.getString(R.string.external_review_1151_home_recent_thread_t_minus_days_hours, days, hours);
        }
        if (totalHours > 0L) {
            return context.getString(R.string.external_review_1151_home_recent_thread_t_minus_hours_minutes, totalHours, minutes);
        }
        return context.getString(
            R.string.external_review_1151_home_recent_thread_t_minus_hours_minutes,
            totalHours,
            minutes
        );
    }

    String buildPackSummary(PackInstaller.InstalledPack pack, int loadedGuideCount) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        return context.getString(R.string.home_manual_summary, format.format(loadedGuideCount));
    }

    String categoryLabelForBucket(String bucketKey) {
        switch (safe(bucketKey).trim()) {
            case "water":
                return "Water & sanitation";
            case "shelter":
                return "Shelter & build";
            case "fire":
                return "Fire & energy";
            case "medicine":
                return "Medicine";
            case "food":
                return "Food & agriculture";
            case "tools":
                return "Tools & craft";
            case "communications":
                return "Communications";
            case "community":
                return "Community";
            default:
                return "";
        }
    }

    String formatCategoryCount(int count) {
        return context.getResources().getQuantityString(R.plurals.category_count, count, count);
    }

    String buildCategoryCardContentDescription(String bucketKey, int count) {
        String label = categoryLabelForBucket(bucketKey);
        if (label.isEmpty()) {
            label = humanizeMetadataLabel(bucketKey);
        }
        if (label.isEmpty()) {
            label = "Category";
        }
        return label + ", " + formatCategoryCount(count) + ". Tap to filter.";
    }

    String buildHomeReadyStatus(int guideCount) {
        if (guideCount <= 0) {
            return "Manual ready";
        }
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        return context.getString(R.string.external_review_home_ready_status, format.format(guideCount));
    }

    String buildPackUnavailableStatus() {
        return "Manual is still preparing. Try again when the pack is ready.";
    }

    String buildPackInstallFailedHeader() {
        return "Manual pack unavailable";
    }

    String buildNoResultsHeader(String query) {
        String trimmedQuery = safe(query).trim();
        if (trimmedQuery.isEmpty()) {
            return "No guide matches";
        }
        return "No guide matches for \"" + trimmedQuery + "\"";
    }

    String buildModelUnavailableStatus() {
        return "Answer model unavailable. Import a model or enable Host GPU.";
    }

    String buildDeveloperDiagnostics(PackInstaller.InstalledPack pack, PackRepository repo, int loadedGuideCount) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        HostInferenceConfig.Settings inferenceSettings = HostInferenceConfig.resolve(context);
        PackManifest manifest = pack.manifest;
        String retrievalMode = repo.hasVectorStore() ? "hybrid vector+lexical" : "lexical fallback";
        return "Pack v" + manifest.packVersion +
            " | generated " + formatPackGeneratedAt(manifest.generatedAt) +
            "\nGuides " + format.format(loadedGuideCount) + " loaded / " + format.format(manifest.guideCount) + " manifest" +
            " | chunks " + format.format(manifest.chunkCount) +
            " | rules " + format.format(manifest.deterministicRuleCount) +
            "\nRetrieval " + retrievalMode +
            " | top_k " + manifest.mobileTopK +
            " | links " + format.format(manifest.relatedLinkCount) +
            "\nDB " + shortHash(manifest.sqliteSha256) +
            " | vectors " + shortHash(manifest.vectorSha256) +
            " | " + manifest.vectorDtype +
            "\nInference " + inferenceSettings.modeSummary() +
            " | " + (inferenceSettings.enabled ? inferenceSettings.serverLabel() : ModelFileStore.getModelSummary(context)) +
            "\nModel " + (inferenceSettings.enabled ? inferenceSettings.modelId : ModelFileStore.getModelSummary(context));
    }

    String formatPackGeneratedAt(String generatedAt) {
        String value = safe(generatedAt).trim();
        if (value.length() >= 16) {
            return value.substring(0, 16).replace('T', ' ') + "Z";
        }
        return value.isEmpty() ? "unknown" : value;
    }

    String buildHomeManualSerial(PackManifest manifest) {
        if (manifest == null) {
            return "";
        }
        String revisionDate = formatHomeManualRevision(manifest.generatedAt);
        String hash = shortHash(manifest.sqliteSha256);
        if ("unknown".equalsIgnoreCase(hash)) {
            hash = "";
        }
        if (revisionDate.isEmpty()) {
            return hash.isEmpty() ? "" : "#" + hash;
        }
        if (hash.isEmpty()) {
            return revisionDate;
        }
        return revisionDate + " #" + hash;
    }

    String shortHash(String hash) {
        String value = safe(hash).trim();
        if (value.length() <= 8) {
            return value.isEmpty() ? "unknown" : value;
        }
        return value.substring(0, 8);
    }

    String buildResultsHeader(
        String query,
        List<SearchResult> results,
        boolean hybridEnabled,
        int cap,
        boolean useCompactHeader,
        boolean landscapePhoneLayout,
        boolean largeFontScale
    ) {
        String label = hybridEnabled ? "Hybrid results" : "Lexical results";
        if (useCompactHeader) {
            return buildCompactResultsHeader(query, results.size(), landscapePhoneLayout, largeFontScale);
        }
        if (results.isEmpty()) {
            return buildNoResultsHeader(query);
        }

        int hybridCount = 0;
        int routeCount = 0;
        int guideCount = 0;
        int vectorCount = 0;
        int lexicalCount = 0;
        int textCount = 0;
        for (SearchResult result : results) {
            String mode = extractRetrievalMode(result);
            switch (mode) {
                case "route-focus":
                    routeCount += 1;
                    break;
                case "guide-focus":
                    guideCount += 1;
                    break;
                case "hybrid":
                    hybridCount += 1;
                    break;
                case "vector":
                    vectorCount += 1;
                    break;
                case "lexical":
                    lexicalCount += 1;
                    break;
                default:
                    textCount += 1;
                    break;
            }
        }

        String countLabel = results.size() >= cap ? "showing top " + results.size() : String.valueOf(results.size());
        StringBuilder builder = new StringBuilder();
        builder.append(label)
            .append(" for \"")
            .append(query)
            .append("\" (")
            .append(countLabel)
            .append(": ")
            .append(routeCount)
            .append(" route, ")
            .append(hybridCount)
            .append(" hybrid, ")
            .append(guideCount)
            .append(" guide, ")
            .append(vectorCount)
            .append(" vector, ")
            .append(lexicalCount)
            .append(" lexical");
        if (textCount > 0) {
            builder.append(", ").append(textCount).append(" fallback");
        }
        builder.append(")");
        return builder.toString();
    }

    String buildResultsHeader(
        String query,
        List<SearchResult> results,
        boolean hybridEnabled,
        int cap,
        boolean useCompactHeader,
        boolean landscapePhoneLayout,
        boolean largeFontScale,
        boolean sessionUsed
    ) {
        String header = buildResultsHeader(
            query,
            results,
            hybridEnabled,
            cap,
            useCompactHeader,
            landscapePhoneLayout,
            largeFontScale
        );
        return sessionUsed ? header + " + session" : header;
    }

    String buildAnswerContextHeader(String query, int contextCount, boolean sessionUsed) {
        String guideLabel = contextCount == 1 ? "1 guide" : contextCount + " guides";
        String header = "Guides Senku is using for \"" + query + "\" (" + guideLabel + ")";
        return sessionUsed ? header + " + session" : header;
    }

    String extractRetrievalMode(SearchResult result) {
        String subtitle = result == null || result.subtitle == null ? "" : result.subtitle;
        int marker = subtitle.lastIndexOf('|');
        if (marker < 0 || marker + 1 >= subtitle.length()) {
            return "text";
        }
        return subtitle.substring(marker + 1).trim().toLowerCase(Locale.US);
    }

    private String buildCompactResultsHeader(
        String query,
        int resultCount,
        boolean landscapePhoneLayout,
        boolean largeFontScale
    ) {
        String trimmedQuery = query == null ? "" : query.trim();
        if (trimmedQuery.isEmpty()) {
            return "Results (" + resultCount + ")";
        }
        String shortQuery = trimmedQuery;
        int maxQueryLength = (landscapePhoneLayout || largeFontScale) ? 18 : 24;
        if (shortQuery.length() > maxQueryLength) {
            shortQuery = shortQuery.substring(0, Math.max(0, maxQueryLength - 3)).trim() + "...";
        }
        return "Results for \"" + shortQuery + "\" (" + resultCount + ")";
    }

    private String formatHomeManualRevision(String generatedAt) {
        String value = formatPackGeneratedAt(generatedAt);
        if (value.length() >= 10) {
            return value.substring(0, 10);
        }
        return value;
    }

    private boolean isDeterministicThread(SessionMemory.TurnSnapshot turn) {
        return turn != null && !safe(turn.ruleId).trim().isEmpty();
    }

    String humanizeMetadataLabel(String value) {
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

    String normalizeBucketText(String value) {
        return safe(value).trim().toLowerCase(Locale.US);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
