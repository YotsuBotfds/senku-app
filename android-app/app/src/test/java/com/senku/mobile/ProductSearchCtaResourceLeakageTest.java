package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

public final class ProductSearchCtaResourceLeakageTest {
    private static final String PRODUCT_SEARCH_LABEL = "@string/home_search_button";
    private static final String EXTERNAL_REVIEW_SEARCH_LABEL = "@string/external_review_home_search_button";

    @Test
    public void productActivityMainSearchButtonsDoNotReferenceExternalReviewCopy() throws Exception {
        List<Path> activityMainLayouts = activityMainLayoutVariants();
        assertFalse("Expected at least one activity_main.xml layout variant", activityMainLayouts.isEmpty());

        List<String> leakingLayouts = new ArrayList<>();
        int productSearchButtonReferences = 0;
        for (Path layout : activityMainLayouts) {
            String xml = new String(Files.readAllBytes(layout), StandardCharsets.UTF_8);
            if (xml.contains(EXTERNAL_REVIEW_SEARCH_LABEL)) {
                leakingLayouts.add(relativePath(layout));
            }
            if (xml.contains("android:id=\"@+id/search_button\"")) {
                productSearchButtonReferences++;
                assertTrue(
                    "Search CTA in " + relativePath(layout) + " should use product copy",
                    xml.contains(PRODUCT_SEARCH_LABEL)
                );
            }
        }

        assertEquals(
            "External-review search copy must stay out of product activity_main.xml variants",
            "",
            String.join(", ", leakingLayouts)
        );
        assertEquals(
            "Every normal product activity_main.xml variant should expose the product search CTA",
            activityMainLayouts.size(),
            productSearchButtonReferences
        );
    }

    private static List<Path> activityMainLayoutVariants() throws Exception {
        Path resRoot = locateRepoFile("android-app/app/src/main/res").toPath();
        List<Path> variants = new ArrayList<>();
        try (Stream<Path> entries = Files.list(resRoot)) {
            entries
                .filter(Files::isDirectory)
                .filter(path -> path.getFileName().toString().startsWith("layout"))
                .map(path -> path.resolve("activity_main.xml"))
                .filter(Files::exists)
                .sorted()
                .forEach(variants::add);
        }
        return variants;
    }

    private static File locateRepoFile(String relativePath) {
        File root = new File(System.getProperty("user.dir")).getAbsoluteFile();
        while (root != null) {
            File candidate = new File(root, relativePath);
            if (candidate.exists()) {
                return candidate;
            }
            root = root.getParentFile();
        }
        throw new AssertionError("Unable to locate " + relativePath);
    }

    private static String relativePath(Path path) {
        Path root = locateRepoFile("android-app").toPath().getParent();
        return root.relativize(path).toString();
    }
}
