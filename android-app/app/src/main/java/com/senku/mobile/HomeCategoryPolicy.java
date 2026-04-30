package com.senku.mobile;

import com.senku.ui.home.CategoryShelfItemModel;
import com.senku.ui.home.CategoryShelfLayoutMode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class HomeCategoryPolicy {
    private static final int MAX_COMPACT_HOME_CATEGORY_TILES = 6;
    private static final int MANUAL_HOME_CATEGORY_COLUMNS = 3;
    private static final int SHELF_ACCENT_DEFAULT = 0xFFC9B682;
    private static final int SHELF_ACCENT_WATER = 0xFF7A9AB4;
    private static final int SHELF_ACCENT_SHELTER = 0xFF7A9A5A;
    private static final int SHELF_ACCENT_FIRE = 0xFFC48A5A;
    private static final int SHELF_ACCENT_MEDICINE = 0xFFB67A7A;
    private static final int SHELF_ACCENT_FOOD = 0xFF9AA064;
    private static final int SHELF_ACCENT_TOOLS = 0xFFC9B682;
    private static final int SHELF_ACCENT_COMMUNICATIONS = 0xFF7A9A9A;
    private static final int SHELF_ACCENT_COMMUNITY = 0xFF9AA084;
    private static final CategoryDefinition[] HOME_CHROME_CATEGORIES = {
        new CategoryDefinition("water", new CategoryStyle("Water & sanitation", SHELF_ACCENT_WATER)),
        new CategoryDefinition("shelter", new CategoryStyle("Shelter & build", SHELF_ACCENT_SHELTER)),
        new CategoryDefinition("fire", new CategoryStyle("Fire & energy", SHELF_ACCENT_FIRE)),
        new CategoryDefinition("medicine", new CategoryStyle("Medicine", SHELF_ACCENT_MEDICINE)),
        new CategoryDefinition("food", new CategoryStyle("Food & agriculture", SHELF_ACCENT_FOOD)),
        new CategoryDefinition("tools", new CategoryStyle("Tools & craft", SHELF_ACCENT_TOOLS)),
        new CategoryDefinition("communications", new CategoryStyle("Communications", SHELF_ACCENT_COMMUNICATIONS)),
        new CategoryDefinition("community", new CategoryStyle("Community", SHELF_ACCENT_COMMUNITY))
    };

    private HomeCategoryPolicy() {
    }

    static ArrayList<HomeCategoryModel> buildCategoryModels(List<SearchResult> guides) {
        ArrayList<HomeCategoryModel> categoryModels = new ArrayList<>();
        for (int index = 0; index < HOME_CHROME_CATEGORIES.length; index++) {
            CategoryDefinition definition = HOME_CHROME_CATEGORIES[index];
            categoryModels.add(new HomeCategoryModel(
                definition.bucketKey,
                index,
                countForBucket(guides, definition.bucketKey)
            ));
        }
        return categoryModels;
    }

    static ArrayList<HomeCategoryModel> visibleCategoryModels(
        List<HomeCategoryModel> categoryModels,
        boolean condense
    ) {
        ArrayList<HomeCategoryModel> visible = new ArrayList<>();
        if (categoryModels != null) {
            for (HomeCategoryModel model : categoryModels) {
                if (model != null && model.count > 0) {
                    visible.add(model);
                }
            }
        }
        visible.sort(Comparator
            .comparingInt((HomeCategoryModel model) -> model.count)
            .reversed()
            .thenComparingInt(model -> model.defaultOrder));
        if (condense && visible.size() > MAX_COMPACT_HOME_CATEGORY_TILES) {
            int cutoffCount = visible.get(MAX_COMPACT_HOME_CATEGORY_TILES - 1).count;
            int nextCount = visible.get(MAX_COMPACT_HOME_CATEGORY_TILES).count;
            if (cutoffCount >= Math.max(2, nextCount * 2)) {
                return new ArrayList<>(visible.subList(0, MAX_COMPACT_HOME_CATEGORY_TILES));
            }
        }
        return visible;
    }

    static ArrayList<HomeCategoryModel> manualHomeCategoryModels(List<HomeCategoryModel> categoryModels) {
        ArrayList<HomeCategoryModel> ordered = new ArrayList<>();
        addCategoryModelByBucket(ordered, categoryModels, "shelter");
        addCategoryModelByBucket(ordered, categoryModels, "water");
        addCategoryModelByBucket(ordered, categoryModels, "fire");
        addCategoryModelByBucket(ordered, categoryModels, "food");
        addCategoryModelByBucket(ordered, categoryModels, "medicine");
        addCategoryModelByBucket(ordered, categoryModels, "tools");
        return ordered;
    }

    static List<CategoryShelfItemModel> buildShelfItems(List<HomeCategoryModel> visibleCategoryModels) {
        ArrayList<CategoryShelfItemModel> items = new ArrayList<>();
        if (visibleCategoryModels == null) {
            return items;
        }
        for (HomeCategoryModel model : visibleCategoryModels) {
            String label = labelForBucket(model == null ? null : model.bucketKey);
            if (label.isEmpty()) {
                label = humanizeMetadataLabel(model == null ? null : model.bucketKey);
            }
            int count = model == null ? 0 : model.count;
            items.add(new CategoryShelfItemModel(
                model == null ? "" : model.bucketKey,
                label,
                formatCount(count),
                accentForBucket(model == null ? null : model.bucketKey),
                model != null && count > 0,
                contentDescription(model == null ? null : model.bucketKey, count)
            ));
        }
        return items;
    }

    static List<CategoryShelfItemModel> buildManualShelfItems(List<HomeCategoryModel> visibleCategoryModels) {
        ArrayList<CategoryShelfItemModel> items = new ArrayList<>();
        if (visibleCategoryModels == null) {
            return items;
        }
        for (HomeCategoryModel model : visibleCategoryModels) {
            int count = model == null ? 0 : model.count;
            items.add(new CategoryShelfItemModel(
                model == null ? "" : model.bucketKey,
                manualLabel(model == null ? null : model.bucketKey),
                formatCount(count),
                accentForBucket(model == null ? null : model.bucketKey),
                model != null && count > 0,
                contentDescription(model == null ? null : model.bucketKey, count)
            ));
        }
        return items;
    }

    static List<CategoryShelfItemModel> buildShelfItemsFromGuides(List<SearchResult> guides, boolean condense) {
        return buildShelfItems(visibleCategoryModels(buildCategoryModels(guides), condense));
    }

    static String manualFilterLabel(String bucketKey, int count) {
        return filterLabel(manualLabel(bucketKey), count);
    }

    static String manualLabel(String bucketKey) {
        switch (safe(bucketKey).trim()) {
            case "shelter":
                return "Shelter";
            case "water":
                return "Water";
            case "fire":
                return "Fire";
            case "food":
                return "Food";
            case "medicine":
                return "Medicine";
            case "tools":
                return "Tools";
            default:
                return labelForBucket(bucketKey);
        }
    }

    static String filterLabel(String label, int count) {
        String cleanLabel = safe(label).trim();
        if (cleanLabel.isEmpty()) {
            cleanLabel = "Guides";
        }
        return cleanLabel + " (" + Math.max(0, count) + ")";
    }

    static CategoryShelfLayoutMode layoutMode(
        boolean phoneFormFactor,
        boolean tabletPortraitLayout,
        boolean landscapeTabletLayout
    ) {
        if (landscapeTabletLayout || tabletPortraitLayout) {
            return CategoryShelfLayoutMode.TABLET_GRID;
        }
        return CategoryShelfLayoutMode.PHONE_GRID;
    }

    static boolean interactionsEnabled(boolean hasRepository, boolean busy) {
        return hasRepository && !busy;
    }

    static int accentForBucket(String bucketKey) {
        CategoryDefinition definition = categoryDefinitionForBucket(bucketKey);
        return definition == null ? SHELF_ACCENT_DEFAULT : definition.style.accentColor;
    }

    static int defaultOrderForBucket(String bucketKey) {
        String normalizedBucket = safe(bucketKey).trim();
        for (int index = 0; index < HOME_CHROME_CATEGORIES.length; index++) {
            if (HOME_CHROME_CATEGORIES[index].bucketKey.equals(normalizedBucket)) {
                return index;
            }
        }
        return HOME_CHROME_CATEGORIES.length;
    }

    static int countForBucket(List<SearchResult> guides, String bucket) {
        int count = 0;
        if (guides == null) {
            return count;
        }
        for (SearchResult result : guides) {
            if (matchesBucket(result, bucket)) {
                count += 1;
            }
        }
        return count;
    }

    static boolean matchesBucket(SearchResult result, String bucket) {
        if (result == null) {
            return false;
        }
        String category = normalizeBucketText(result.category);
        String searchable = normalizeBucketText(
            result.category + " " +
                result.topicTags + " " +
                result.title
        );
        switch (bucket) {
            case "water":
                return "water".equals(category)
                    || "sanitation".equals(category)
                    || containsAnyBucketToken(searchable,
                        "water", "rainwater", "rain barrel", "sanitation", "hygiene",
                        "latrine", "filtration", "filter", "purify", "purification",
                        "wastewater", "aquaculture"
                    );
            case "shelter":
                return "shelter".equals(category)
                    || "building".equals(category)
                    || "utility".equals(category)
                    || containsAnyBucketToken(searchable,
                        "shelter", "housing", "cabin", "roof", "foundation",
                        "insulation", "weatherproof", "bathhouse"
                    );
            case "fire":
                return "fire".equals(category)
                    || "energy".equals(category)
                    || "power-generation".equals(category)
                    || containsAnyBucketToken(searchable,
                        "fire", "stove", "fuel", "charcoal", "candle", "lamp",
                        "heat", "thermal", "combustion", "boiler", "steam"
                    );
            case "medicine":
                return "medical".equals(category)
                    || "medicine".equals(category)
                    || "health".equals(category)
                    || containsAnyBucketToken(searchable,
                        "medicine", "medical", "health", "first aid", "wound", "clinic"
                    );
            case "food":
                return "food".equals(category)
                    || "agriculture".equals(category)
                    || "biology".equals(category)
                    || containsAnyBucketToken(searchable,
                        "food", "garden", "crop", "seed", "soil", "farming",
                        "preserve", "ferment", "fish", "animal husbandry"
                    );
            case "tools":
                return "tools".equals(category)
                    || "craft".equals(category)
                    || "crafts".equals(category)
                    || "metalworking".equals(category)
                    || "textiles-fiber-arts".equals(category)
                    || "salvage".equals(category)
                    || containsAnyBucketToken(searchable,
                        "tool", "tools", "craft", "joinery", "forge", "metal",
                        "textile", "fabric", "rope", "repair", "salvage"
                    );
            case "communications":
                return "communications".equals(category)
                    || "communication".equals(category)
                    || containsAnyBucketToken(searchable,
                        "communications", "communication", "radio", "signal",
                        "telegraph", "antenna", "message"
                    );
            case "community":
                return "community".equals(category)
                    || "society".equals(category)
                    || "resource-management".equals(category)
                    || "defense".equals(category)
                    || "culture-knowledge".equals(category)
                    || containsAnyBucketToken(searchable,
                        "community", "governance", "security", "defense",
                        "education", "apprenticeship", "records", "currency"
                    );
            default:
                return false;
        }
    }

    static String labelForBucket(String bucketKey) {
        CategoryDefinition definition = categoryDefinitionForBucket(bucketKey);
        return definition == null ? "" : definition.style.label;
    }

    static String formatCount(int count) {
        return count + (count == 1 ? " guide" : " guides");
    }

    static String contentDescription(String bucketKey, int count) {
        String label = labelForBucket(bucketKey);
        if (label.isEmpty()) {
            label = humanizeMetadataLabel(bucketKey);
        }
        if (label.isEmpty()) {
            label = "Category";
        }
        return label + ", " + formatCount(count) + ". Tap to filter.";
    }

    static int shelfMinimumHeightDp(int itemCount, int cardHeightDp, int rowGapDp) {
        int safeItemCount = Math.max(0, itemCount);
        if (safeItemCount == 0) {
            return 0;
        }
        int rows = (safeItemCount + MANUAL_HOME_CATEGORY_COLUMNS - 1) / MANUAL_HOME_CATEGORY_COLUMNS;
        return rows * cardHeightDp + Math.max(0, rows - 1) * rowGapDp;
    }

    private static void addCategoryModelByBucket(
        ArrayList<HomeCategoryModel> destination,
        List<HomeCategoryModel> categoryModels,
        String bucketKey
    ) {
        if (categoryModels == null) {
            return;
        }
        for (HomeCategoryModel model : categoryModels) {
            if (model != null && model.count > 0 && bucketKey.equals(model.bucketKey)) {
                destination.add(model);
                return;
            }
        }
    }

    private static boolean containsAnyBucketToken(String searchable, String... tokens) {
        for (String token : tokens) {
            if (containsBucketToken(searchable, token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsBucketToken(String searchable, String token) {
        String normalizedSearchable = safe(searchable);
        String normalizedToken = safe(token).trim();
        if (normalizedSearchable.isEmpty() || normalizedToken.isEmpty()) {
            return false;
        }
        int searchStart = 0;
        while (searchStart < normalizedSearchable.length()) {
            int matchStart = normalizedSearchable.indexOf(normalizedToken, searchStart);
            if (matchStart < 0) {
                return false;
            }
            int matchEnd = matchStart + normalizedToken.length();
            if (isBucketTokenBoundary(normalizedSearchable, matchStart - 1)
                && isBucketTokenBoundary(normalizedSearchable, matchEnd)) {
                return true;
            }
            searchStart = matchStart + 1;
        }
        return false;
    }

    private static boolean isBucketTokenBoundary(String text, int index) {
        return index < 0
            || index >= text.length()
            || !Character.isLetterOrDigit(text.charAt(index));
    }

    private static CategoryDefinition categoryDefinitionForBucket(String bucketKey) {
        String normalizedBucket = safe(bucketKey).trim();
        for (CategoryDefinition definition : HOME_CHROME_CATEGORIES) {
            if (definition.bucketKey.equals(normalizedBucket)) {
                return definition;
            }
        }
        return null;
    }

    private static String humanizeMetadataLabel(String value) {
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

    private static String normalizeBucketText(String value) {
        return safe(value).trim().toLowerCase(Locale.US);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    static final class HomeCategoryModel {
        final String bucketKey;
        final int defaultOrder;
        final int count;

        HomeCategoryModel(String bucketKey, int defaultOrder, int count) {
            this.bucketKey = safe(bucketKey).trim();
            this.defaultOrder = defaultOrder;
            this.count = count;
        }
    }

    private static final class CategoryDefinition {
        final String bucketKey;
        final CategoryStyle style;

        CategoryDefinition(String bucketKey, CategoryStyle style) {
            this.bucketKey = bucketKey;
            this.style = style;
        }
    }

    private static final class CategoryStyle {
        final String label;
        final int accentColor;

        CategoryStyle(String label, int accentColor) {
            this.label = label;
            this.accentColor = accentColor;
        }
    }
}
