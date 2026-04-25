package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AnswerCardSource {
    public final String cardId;
    public final String sourceGuideId;
    public final String slug;
    public final String title;
    public final List<String> sections;
    public final boolean primary;

    public AnswerCardSource(
        String cardId,
        String sourceGuideId,
        String slug,
        String title,
        List<String> sections,
        boolean primary
    ) {
        this.cardId = emptySafe(cardId);
        this.sourceGuideId = emptySafe(sourceGuideId);
        this.slug = emptySafe(slug);
        this.title = emptySafe(title);
        this.sections = immutableCopy(sections);
        this.primary = primary;
    }

    private static List<String> immutableCopy(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static String emptySafe(String text) {
        return text == null ? "" : text;
    }
}
