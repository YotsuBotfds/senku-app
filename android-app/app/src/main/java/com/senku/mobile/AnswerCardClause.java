package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AnswerCardClause {
    public final String cardId;
    public final String clauseKind;
    public final int ordinal;
    public final String text;
    public final List<String> triggerTerms;

    public AnswerCardClause(
        String cardId,
        String clauseKind,
        int ordinal,
        String text,
        List<String> triggerTerms
    ) {
        this.cardId = emptySafe(cardId);
        this.clauseKind = emptySafe(clauseKind);
        this.ordinal = ordinal;
        this.text = emptySafe(text);
        this.triggerTerms = immutableCopy(triggerTerms);
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
