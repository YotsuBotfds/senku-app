package com.senku.mobile;

final class SearchScoreMarkerPolicy {
    private static final int TRACK_WIDTH_DP = 22;

    private SearchScoreMarkerPolicy() {
    }

    static Marker markerForPosition(int position) {
        int score = SearchResultCardModelMapper.tabletScoreForPosition(position);
        return new Marker(
            score,
            Integer.toString(score),
            TRACK_WIDTH_DP,
            fillFractionForScore(score),
            score >= 70,
            "Rank " + SearchResultCardModelMapper.buildOrdinalRankLabel(position) + ", score marker " + score
        );
    }

    private static float fillFractionForScore(int score) {
        if (score >= 90) {
            return 0.94f;
        }
        if (score >= 75) {
            return 0.82f;
        }
        if (score >= 70) {
            return 0.74f;
        }
        if (score >= 60) {
            return 0.62f;
        }
        return 0.52f;
    }

    static final class Marker {
        final int score;
        final String label;
        final int trackWidthDp;
        final float fillFraction;
        final boolean highEmphasisTone;
        final String contentDescription;

        Marker(
            int score,
            String label,
            int trackWidthDp,
            float fillFraction,
            boolean highEmphasisTone,
            String contentDescription
        ) {
            this.score = score;
            this.label = label;
            this.trackWidthDp = trackWidthDp;
            this.fillFraction = fillFraction;
            this.highEmphasisTone = highEmphasisTone;
            this.contentDescription = contentDescription;
        }
    }
}
