package com.senku.mobile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

final class PackFtsQueryBuilder {
    private static final Locale QUERY_LOCALE = Locale.US;

    private PackFtsQueryBuilder() {
    }

    static String build(PackRepository.QueryTerms queryTerms) {
        return build(queryTerms, 8, true);
    }

    static String build(
        PackRepository.QueryTerms queryTerms,
        int maxExpressions,
        boolean includeExpansionTokens
    ) {
        if (queryTerms == null || maxExpressions <= 0) {
            return "";
        }
        Set<String> tokens = new LinkedHashSet<>();
        for (String token : queryTerms.primaryFtsTokens()) {
            addFtsExpression(tokens, token);
            if (tokens.size() >= maxExpressions) {
                break;
            }
        }
        if (includeExpansionTokens && tokens.size() < maxExpressions) {
            for (String token : queryTerms.expansionTokens) {
                addFtsExpression(tokens, token);
                if (tokens.size() >= maxExpressions) {
                    break;
                }
            }
        }
        return String.join(" OR ", tokens);
    }

    static String buildForQuery(String query) {
        return build(PackRepository.QueryTerms.fromQuery(query));
    }

    private static void addFtsExpression(Set<String> expressions, String token) {
        String expression = buildExpression(token);
        if (!expression.isEmpty()) {
            expressions.add(expression);
        }
    }

    static String buildExpression(String token) {
        ArrayList<String> parts = new ArrayList<>();
        String[] split = emptySafe(token).toLowerCase(QUERY_LOCALE).split("[^a-z0-9]+");
        for (String part : split) {
            String normalized = emptySafe(part).trim().toLowerCase(QUERY_LOCALE);
            if (normalized.isEmpty()) {
                continue;
            }
            parts.add(normalized + "*");
        }
        if (parts.isEmpty()) {
            return "";
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return "(" + String.join(" AND ", parts) + ")";
    }

    private static String emptySafe(String value) {
        return value == null ? "" : value;
    }
}
