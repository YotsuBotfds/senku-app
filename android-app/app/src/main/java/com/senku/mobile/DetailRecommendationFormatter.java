package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DetailRecommendationFormatter {
    static final class State {
        final String currentTitle;
        final String currentSubtitle;
        final String currentBody;
        final String currentRuleId;
        final String primaryCategory;
        final String primaryTopicTags;

        State(
            String currentTitle,
            String currentSubtitle,
            String currentBody,
            String currentRuleId,
            String primaryCategory,
            String primaryTopicTags
        ) {
            this.currentTitle = safe(currentTitle);
            this.currentSubtitle = safe(currentSubtitle);
            this.currentBody = safe(currentBody);
            this.currentRuleId = safe(currentRuleId);
            this.primaryCategory = safe(primaryCategory);
            this.primaryTopicTags = safe(primaryTopicTags);
        }
    }

    private static final Pattern GUIDE_ID_PATTERN = Pattern.compile("\\[GD-\\d{3}\\]");
    private static final Pattern EXPLICIT_MATERIALS_LINE = Pattern.compile(
        "(?i)(materials|ingredients|supplies|what you need|tools needed|equipment needed)\\s*:\\s*(.+)"
    );

    List<String> buildRelatedPaths(State state) {
        State safeState = state == null ? new State("", "", "", "", "", "") : state;
        String fingerprint = (
            safeState.currentTitle + " " +
            safeState.currentSubtitle + " " +
            safeState.currentBody + " " +
            safeState.primaryCategory + " " +
            safeState.primaryTopicTags
        ).toLowerCase(Locale.US);
        ArrayList<String> steps = new ArrayList<>();
        if (DeterministicAnswerRouter.isEmergencyRuleId(safeState.currentRuleId) ||
            fingerprint.contains("wound") ||
            fingerprint.contains("puncture") ||
            fingerprint.contains("bleed") ||
            fingerprint.contains("injur") ||
            fingerprint.contains("infection") ||
            fingerprint.contains("snake") ||
            fingerprint.contains("bite") ||
            fingerprint.contains("sepsis") ||
            fingerprint.contains("trauma") ||
            fingerprint.contains("medicine")) {
            steps.add("Stop bleeding with basic supplies");
            steps.add("Prevent infection in the field");
            steps.add("Decide whether to evacuate");
        } else if (fingerprint.contains("fire") || fingerprint.contains("tinder") || fingerprint.contains("rain")) {
            steps.add("Purify water using this fire");
            steps.add("Turn this into a signal fire");
            steps.add("Find dry tinder fast");
        } else if (fingerprint.contains("water") || fingerprint.contains("dehydrat") || fingerprint.contains("boil")) {
            steps.add("Store the clean water safely");
            steps.add("Find the fastest safe boil setup");
            steps.add("Check for dehydration signs");
        } else if (fingerprint.contains("security") || fingerprint.contains("threat") || fingerprint.contains("violence")) {
            steps.add("Secure shelter without drawing attention");
            steps.add("Choose the safest next communication step");
            steps.add("Move without leaving obvious signs");
        } else {
            steps.add("What should I do next if conditions worsen?");
            steps.add("What nearby risk should I prepare for now?");
            steps.add("What gear matters most here?");
        }
        return steps;
    }

    List<String> buildMaterialsChecklist(String formattedBody) {
        LinkedHashMap<String, String> materials = new LinkedHashMap<>();
        boolean explicitSignal = collectExplicitMaterials(formattedBody, materials);
        if (!explicitSignal) {
            collectKeywordMaterials(formattedBody, materials);
        }
        ArrayList<String> result = new ArrayList<>(materials.values());
        if (!explicitSignal && result.size() < 2) {
            return Collections.emptyList();
        }
        if (result.size() > 4) {
            return result.subList(0, 4);
        }
        return result;
    }

    private boolean collectExplicitMaterials(String body, LinkedHashMap<String, String> materials) {
        String[] lines = safe(body).split("\\r?\\n");
        boolean explicitHit = false;
        for (String rawLine : lines) {
            String line = safe(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }
            Matcher matcher = EXPLICIT_MATERIALS_LINE.matcher(line);
            if (matcher.find()) {
                explicitHit = true;
                addMaterialCandidates(matcher.group(2), materials);
            }
        }
        return explicitHit;
    }

    private void collectKeywordMaterials(String body, LinkedHashMap<String, String> materials) {
        String fingerprint = safe(body).toLowerCase(Locale.US);
        addMaterialIfPresent(fingerprint, materials, "bark", "Bark");
        addMaterialIfPresent(fingerprint, materials, "split wood", "Split wood");
        addMaterialIfPresent(fingerprint, materials, "flat stones", "Flat stones");
        addMaterialIfPresent(fingerprint, materials, "dry twigs", "Dry twigs");
        addMaterialIfPresent(fingerprint, materials, "feather sticks", "Feather sticks");
        addMaterialIfPresent(fingerprint, materials, "resin", "Resin");
        addMaterialIfPresent(fingerprint, materials, "inner bark", "Inner bark");
        addMaterialIfPresent(fingerprint, materials, "tinder", "Dry tinder");
        addMaterialIfPresent(fingerprint, materials, "cloth", "Cloth");
        addMaterialIfPresent(fingerprint, materials, "bandage", "Bandage");
        addMaterialIfPresent(fingerprint, materials, "clean water", "Clean water");
    }

    private void addMaterialIfPresent(String fingerprint, LinkedHashMap<String, String> materials, String token, String label) {
        if (fingerprint.contains(token)) {
            materials.putIfAbsent(label.toLowerCase(Locale.US), label);
        }
    }

    private void addMaterialCandidates(String raw, LinkedHashMap<String, String> materials) {
        for (String piece : safe(raw).split(",|;|\\band\\b")) {
            String cleaned = cleanMaterialToken(piece);
            if (cleaned.isEmpty()) {
                continue;
            }
            String key = cleaned.toLowerCase(Locale.US);
            if (!materials.containsKey(key)) {
                materials.put(key, cleaned);
            }
        }
    }

    private String cleanMaterialToken(String raw) {
        String cleaned = GUIDE_ID_PATTERN.matcher(safe(raw)).replaceAll("");
        cleaned = cleaned.replaceAll("^[\\-*\\u2022\\d\\.\\s]+", "").trim();
        cleaned = cleaned.replaceAll("[\\.:]+$", "").trim();
        if (cleaned.length() < 2 || cleaned.length() > 28) {
            return "";
        }
        String lower = cleaned.toLowerCase(Locale.US);
        if (lower.startsWith("if ") || lower.startsWith("when ") || lower.startsWith("unless ")) {
            return "";
        }
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
