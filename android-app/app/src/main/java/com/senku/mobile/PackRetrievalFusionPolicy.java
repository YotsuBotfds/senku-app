package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class PackRetrievalFusionPolicy {
    private static final int HYBRID_RRF_K = 60;

    interface DebugSink {
        void log(String message);
    }

    private PackRetrievalFusionPolicy() {
    }

    static List<PackRepository.RankedChunk> mergeLexicalHits(
        List<PackRepository.RankedChunk> ftsHits,
        List<PackRepository.RankedChunk> keywordHits
    ) {
        if (ftsHits.isEmpty()) {
            return reindexLexicalHits(keywordHits);
        }
        if (keywordHits.isEmpty()) {
            return reindexLexicalHits(ftsHits);
        }

        LinkedHashMap<String, PackRepository.LexicalCombinedHit> merged = new LinkedHashMap<>();

        for (int index = 0; index < ftsHits.size(); index++) {
            PackRepository.RankedChunk hit = ftsHits.get(index);
            PackRepository.LexicalCombinedHit combined = merged.get(hit.chunkId);
            if (combined == null) {
                combined = new PackRepository.LexicalCombinedHit(hit);
                merged.put(hit.chunkId, combined);
            }
            combined.ftsRank = Math.min(combined.ftsRank, index);
            combined.score += reciprocalRank(index) * 1.25;
        }

        for (int index = 0; index < keywordHits.size(); index++) {
            PackRepository.RankedChunk hit = keywordHits.get(index);
            PackRepository.LexicalCombinedHit combined = merged.get(hit.chunkId);
            if (combined == null) {
                combined = new PackRepository.LexicalCombinedHit(hit);
                merged.put(hit.chunkId, combined);
            }
            combined.keywordRank = Math.min(combined.keywordRank, index);
            combined.score += reciprocalRank(index);
        }

        ArrayList<PackRepository.LexicalCombinedHit> ordered = new ArrayList<>(merged.values());
        ordered.sort((left, right) -> {
            int scoreOrder = Double.compare(right.score, left.score);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            int bestRankOrder = Integer.compare(bestLexicalRank(left), bestLexicalRank(right));
            if (bestRankOrder != 0) {
                return bestRankOrder;
            }
            return Integer.compare(left.chunk.document.length(), right.chunk.document.length());
        });

        ArrayList<PackRepository.RankedChunk> results = new ArrayList<>();
        for (int index = 0; index < ordered.size(); index++) {
            results.add(ordered.get(index).chunk.withLexicalRank(index, ordered.get(index).score));
        }
        return results;
    }

    static List<PackRepository.CombinedHit> mergeHybrid(
        List<PackRepository.RankedChunk> lexicalHits,
        List<PackRepository.RankedChunk> vectorHits,
        PackRepository.AnchorPriorDirective anchorPrior,
        Map<String, Map<String, Double>> relatedWeightsByGuide,
        DebugSink debugSink
    ) {
        LinkedHashMap<String, PackRepository.CombinedHit> merged = new LinkedHashMap<>();

        for (int index = 0; index < lexicalHits.size(); index++) {
            PackRepository.RankedChunk hit = lexicalHits.get(index);
            PackRepository.CombinedHit combined = merged.get(hit.chunkId);
            if (combined == null) {
                combined = new PackRepository.CombinedHit(hit);
                merged.put(hit.chunkId, combined);
            }
            combined.lexicalRank = Math.min(combined.lexicalRank, index);
            combined.rrfScore += reciprocalRank(index);
        }

        for (int index = 0; index < vectorHits.size(); index++) {
            PackRepository.RankedChunk hit = vectorHits.get(index);
            PackRepository.CombinedHit combined = merged.get(hit.chunkId);
            if (combined == null) {
                combined = new PackRepository.CombinedHit(hit);
                merged.put(hit.chunkId, combined);
            }
            combined.vectorRank = Math.min(combined.vectorRank, index);
            combined.vectorScore = Math.max(combined.vectorScore, hit.vectorScore);
            combined.rrfScore += reciprocalRank(index);
        }

        applyAnchorPrior(merged, anchorPrior, relatedWeightsByGuide, debugSink);

        ArrayList<PackRepository.CombinedHit> ordered = new ArrayList<>(merged.values());
        ordered.sort((left, right) -> {
            int scoreOrder = Double.compare(right.rrfScore, left.rrfScore);
            if (scoreOrder != 0) {
                return scoreOrder;
            }
            int modeOrder = Integer.compare(modePriority(right), modePriority(left));
            if (modeOrder != 0) {
                return modeOrder;
            }
            int vectorOrder = Float.compare(right.vectorScore, left.vectorScore);
            if (vectorOrder != 0) {
                return vectorOrder;
            }
            return Integer.compare(left.lexicalRank, right.lexicalRank);
        });
        return ordered;
    }

    static List<SearchResult> toSearchResults(List<PackRepository.CombinedHit> combinedHits, int limit) {
        ArrayList<SearchResult> results = new ArrayList<>();
        Set<String> seenGuideSections = new LinkedHashSet<>();
        int capped = limit <= 0 ? combinedHits.size() : limit;
        for (int index = 0; index < combinedHits.size() && results.size() < capped; index++) {
            PackRepository.CombinedHit combined = combinedHits.get(index);
            PackRepository.RankedChunk hit = combined.chunk;
            String guideSectionKey = PackQueryPipelineHelper.guideSectionKey(
                hit.guideId,
                hit.guideTitle,
                hit.sectionHeading
            );
            if (seenGuideSections.contains(guideSectionKey)) {
                continue;
            }
            seenGuideSections.add(guideSectionKey);
            String retrievalMode = PackQueryPipelineHelper.retrievalModeForRanks(
                combined.lexicalRank,
                combined.vectorRank
            );
            results.add(PackQueryPipelineHelper.mapChunkResult(
                hit.guideTitle,
                hit.guideId,
                hit.sectionHeading,
                hit.category,
                hit.document,
                retrievalMode,
                hit.contentRole,
                hit.timeHorizon,
                hit.structureType,
                hit.topicTags
            ));
        }
        return results;
    }

    static double reciprocalRankForTest(int rank) {
        return reciprocalRank(rank);
    }

    private static void applyAnchorPrior(
        Map<String, PackRepository.CombinedHit> merged,
        PackRepository.AnchorPriorDirective anchorPrior,
        Map<String, Map<String, Double>> relatedWeightsByGuide,
        DebugSink debugSink
    ) {
        if (merged == null || merged.isEmpty() || anchorPrior == null) {
            return;
        }
        Map<String, Double> relatedWeights = relatedWeightsByGuide == null
            ? Collections.emptyMap()
            : relatedWeightsByGuide.get(anchorPrior.anchorGuideId);
        if (relatedWeights == null) {
            relatedWeights = Collections.emptyMap();
        }
        for (PackRepository.CombinedHit combined : merged.values()) {
            String guideId = emptySafe(combined.chunk.guideId).trim();
            PackAnchorPriorPolicy.AnchorPriorAdjustment adjustment = PackAnchorPriorPolicy.adjustment(
                anchorPrior.anchorGuideId,
                anchorPrior.turnsSinceAnchor,
                guideId,
                relatedWeights
            );
            if (!adjustment.hasBonus()) {
                continue;
            }
            combined.rrfScore += adjustment.bonus;
            if (debugSink != null) {
                debugSink.log(
                    "anchor_prior turn=" + anchorPrior.turnCount +
                        " anchor_gid=" + anchorPrior.anchorGuideId +
                        " chunk_gid=" + guideId +
                        " base=" + PackAnchorPriorPolicy.BASE_BONUS +
                        " decay=" + adjustment.decay +
                        " weight=" + adjustment.weight +
                        " bonus=" + adjustment.bonus
                );
            }
        }
    }

    private static List<PackRepository.RankedChunk> reindexLexicalHits(List<PackRepository.RankedChunk> hits) {
        ArrayList<PackRepository.RankedChunk> reindexed = new ArrayList<>();
        for (int index = 0; index < hits.size(); index++) {
            reindexed.add(hits.get(index).withLexicalRank(index));
        }
        return reindexed;
    }

    private static int modePriority(PackRepository.CombinedHit hit) {
        if (hit.lexicalRank != Integer.MAX_VALUE && hit.vectorRank != Integer.MAX_VALUE) {
            return 3;
        }
        if (hit.vectorRank != Integer.MAX_VALUE) {
            return 2;
        }
        return 1;
    }

    private static int bestLexicalRank(PackRepository.LexicalCombinedHit hit) {
        return Math.min(hit.ftsRank, hit.keywordRank);
    }

    private static double reciprocalRank(int rank) {
        return 1.0 / (HYBRID_RRF_K + rank + 1.0);
    }

    private static String emptySafe(String value) {
        return value == null ? "" : value;
    }
}
