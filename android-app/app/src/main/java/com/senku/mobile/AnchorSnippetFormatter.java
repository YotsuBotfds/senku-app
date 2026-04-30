package com.senku.mobile;

final class AnchorSnippetFormatter {
    private static final int ANCHOR_SNIPPET_LIMIT = 280;

    private AnchorSnippetFormatter() {
    }

    static String resolve(String sessionChunkText, String guideChunkText, String guideBody) {
        String sessionSnippet = fromChunkText(sessionChunkText);
        if (!sessionSnippet.isEmpty()) {
            return sessionSnippet;
        }
        String guideChunkSnippet = fromChunkText(guideChunkText);
        if (!guideChunkSnippet.isEmpty()) {
            return guideChunkSnippet;
        }
        return firstParagraph(guideBody);
    }

    static String firstParagraph(String guideBody) {
        String normalized = emptySafe(guideBody).replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.trim().isEmpty()) {
            return "";
        }

        StringBuilder paragraph = new StringBuilder();
        for (String rawLine : normalized.split("\n", -1)) {
            String cleaned = sanitizeLine(rawLine);
            if (cleaned.isEmpty()) {
                if (paragraph.length() > 0) {
                    break;
                }
                continue;
            }
            if (paragraph.length() > 0) {
                paragraph.append(' ');
            }
            paragraph.append(cleaned);
        }
        return clip(paragraph.toString(), ANCHOR_SNIPPET_LIMIT);
    }

    private static String fromChunkText(String chunkText) {
        return clip(normalizeText(chunkText), ANCHOR_SNIPPET_LIMIT);
    }

    private static String normalizeText(String text) {
        String normalized = emptySafe(text).replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.trim().isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String rawLine : normalized.split("\n", -1)) {
            String cleaned = sanitizeLine(rawLine);
            if (cleaned.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(cleaned);
        }
        return builder.toString().replaceAll("\\s+", " ").trim();
    }

    private static String sanitizeLine(String rawLine) {
        String trimmed = emptySafe(rawLine).trim();
        if (trimmed.isEmpty()
            || trimmed.startsWith("#")
            || trimmed.startsWith("```")
            || trimmed.matches("^[-*_]{3,}$")) {
            return "";
        }

        String cleaned = trimmed;
        cleaned = cleaned.replaceFirst("^>+\\s*", "");
        cleaned = cleaned.replaceFirst("^[-*+]\\s+", "");
        cleaned = cleaned.replaceFirst("^\\d+[.)]\\s+", "");
        cleaned = cleaned.replaceFirst("^\\[![^\\]]+\\]\\s*", "");
        cleaned = cleaned.replaceAll("\\[(.+?)\\]\\((.+?)\\)", "$1");
        cleaned = cleaned.replace("**", "");
        cleaned = cleaned.replace("__", "");
        cleaned = cleaned.replace("`", "");
        return cleaned.replaceAll("\\s+", " ").trim();
    }

    private static String clip(String text, int limit) {
        String safe = emptySafe(text).replaceAll("\\s+", " ").trim();
        if (safe.length() <= limit) {
            return safe;
        }
        return safe.substring(0, Math.max(0, limit - 3)).trim() + "...";
    }

    private static String emptySafe(String text) {
        return text == null ? "" : text;
    }
}
