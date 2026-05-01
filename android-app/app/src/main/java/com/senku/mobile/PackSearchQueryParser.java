package com.senku.mobile;

final class PackSearchQueryParser {
    private PackSearchQueryParser() {
    }

    static ParsedSearchQuery parse(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim();
        if (!query.startsWith(PackAnchorPriorPolicy.DIRECTIVE_PREFIX)) {
            return new ParsedSearchQuery(query, null);
        }
        int markerEnd = query.indexOf(' ');
        if (markerEnd <= 0) {
            return new ParsedSearchQuery(query, null);
        }
        String payload = query.substring(PackAnchorPriorPolicy.DIRECTIVE_PREFIX.length(), markerEnd);
        String[] parts = payload.split(":");
        if (parts.length != 3) {
            return new ParsedSearchQuery(query, null);
        }
        try {
            PackRepository.AnchorPriorDirective directive = new PackRepository.AnchorPriorDirective(
                parts[0].trim(),
                Integer.parseInt(parts[1].trim()),
                Integer.parseInt(parts[2].trim())
            );
            return new ParsedSearchQuery(query.substring(markerEnd + 1).trim(), directive);
        } catch (NumberFormatException ignored) {
            return new ParsedSearchQuery(query, null);
        }
    }

    static final class ParsedSearchQuery {
        final String query;
        final PackRepository.AnchorPriorDirective anchorPrior;

        ParsedSearchQuery(String query, PackRepository.AnchorPriorDirective anchorPrior) {
            this.query = query == null ? "" : query;
            this.anchorPrior = anchorPrior;
        }
    }
}
