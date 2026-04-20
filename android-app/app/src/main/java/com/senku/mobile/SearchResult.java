package com.senku.mobile;

import java.io.Serializable;
import java.util.Objects;

public final class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String title;
    public final String subtitle;
    public final String snippet;
    public final String body;
    public final String guideId;
    public final String sectionHeading;
    public final String category;
    public final String retrievalMode;
    public final String contentRole;
    public final String timeHorizon;
    public final String structureType;
    public final String topicTags;

    public SearchResult(String title, String subtitle, String snippet, String body) {
        this(title, subtitle, snippet, body, "", "", "", "", "", "", "", "");
    }

    public SearchResult(
        String title,
        String subtitle,
        String snippet,
        String body,
        String guideId,
        String sectionHeading,
        String category,
        String retrievalMode
    ) {
        this(title, subtitle, snippet, body, guideId, sectionHeading, category, retrievalMode, "", "", "", "");
    }

    public SearchResult(
        String title,
        String subtitle,
        String snippet,
        String body,
        String guideId,
        String sectionHeading,
        String category,
        String retrievalMode,
        String contentRole,
        String timeHorizon,
        String structureType,
        String topicTags
    ) {
        this.title = title;
        this.subtitle = subtitle;
        this.snippet = snippet;
        this.body = body;
        this.guideId = guideId == null ? "" : guideId;
        this.sectionHeading = sectionHeading == null ? "" : sectionHeading;
        this.category = category == null ? "" : category;
        this.retrievalMode = retrievalMode == null ? "" : retrievalMode;
        this.contentRole = contentRole == null ? "" : contentRole;
        this.timeHorizon = timeHorizon == null ? "" : timeHorizon;
        this.structureType = structureType == null ? "" : structureType;
        this.topicTags = topicTags == null ? "" : topicTags;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SearchResult)) {
            return false;
        }
        SearchResult that = (SearchResult) other;
        return Objects.equals(title, that.title)
            && Objects.equals(subtitle, that.subtitle)
            && Objects.equals(snippet, that.snippet)
            && Objects.equals(body, that.body)
            && Objects.equals(guideId, that.guideId)
            && Objects.equals(sectionHeading, that.sectionHeading)
            && Objects.equals(category, that.category)
            && Objects.equals(retrievalMode, that.retrievalMode)
            && Objects.equals(contentRole, that.contentRole)
            && Objects.equals(timeHorizon, that.timeHorizon)
            && Objects.equals(structureType, that.structureType)
            && Objects.equals(topicTags, that.topicTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            title,
            subtitle,
            snippet,
            body,
            guideId,
            sectionHeading,
            category,
            retrievalMode,
            contentRole,
            timeHorizon,
            structureType,
            topicTags
        );
    }

    @Override
    public String toString() {
        return "SearchResult{" +
            "title='" + title + '\'' +
            ", guideId='" + guideId + '\'' +
            ", sectionHeading='" + sectionHeading + '\'' +
            ", category='" + category + '\'' +
            ", retrievalMode='" + retrievalMode + '\'' +
            '}';
    }
}
