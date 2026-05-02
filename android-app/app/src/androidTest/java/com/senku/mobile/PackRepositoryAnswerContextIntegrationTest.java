package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class PackRepositoryAnswerContextIntegrationTest {
    private File databaseFile;

    @After
    public void tearDown() {
        if (databaseFile != null) {
            databaseFile.delete();
            new File(databaseFile.getAbsolutePath() + "-journal").delete();
            new File(databaseFile.getAbsolutePath() + "-shm").delete();
            new File(databaseFile.getAbsolutePath() + "-wal").delete();
        }
    }

    @Test
    public void buildGuideAnswerContextLoadsDedupedAnchorSectionsFromPackDatabase() throws Exception {
        databaseFile = File.createTempFile(
            "senku-answer-context",
            ".sqlite",
            InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir()
        );
        createPackDatabase(databaseFile);

        SearchResult anchor = new SearchResult(
            "Cabin Roof Waterproofing",
            "GD-777 | building | Roof Deck | route-focus",
            "Anchor result for cabin roof waterproofing.",
            "Anchor result for cabin roof waterproofing.",
            "GD-777",
            "Roof Deck",
            "building",
            "route-focus",
            "procedure",
            "long_term",
            "cabin_house",
            "roofing,weatherproofing"
        );

        try (PackRepository repository = new PackRepository(databaseFile, null)) {
            List<SearchResult> context = repository.buildGuideAnswerContext(
                "how do i waterproof a cabin roof",
                Collections.singletonList(anchor),
                2
            );

            assertEquals(2, context.size());
            assertEquals("Roof Deck", context.get(0).sectionHeading);
            assertEquals("guide-focus", context.get(0).retrievalMode);
            assertTrue(context.get(0).body.contains("First roof deck row"));
            assertTrue(context.get(0).body.contains("Second roof deck row"));
            assertTrue(context.get(0).snippet.length() <= 220);
            assertEquals("Weatherproofing Layers", context.get(1).sectionHeading);
            assertEquals("guide-focus", context.get(1).retrievalMode);
        }
    }

    @Test
    public void buildGuideAnswerContextFallsBackToAnchorWhenChunkTableIsUnavailable() throws Exception {
        databaseFile = File.createTempFile(
            "senku-answer-context-missing-chunks",
            ".sqlite",
            InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir()
        );
        try (SQLiteDatabase ignored = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)) {
            // Intentionally leave the pack schema incomplete to simulate a stale or corrupt install.
        }
        SearchResult anchor = new SearchResult(
            "Cabin Roof Waterproofing",
            "GD-777 | building | Roof Deck | route-focus",
            "Anchor result for cabin roof waterproofing.",
            "Anchor result for cabin roof waterproofing.",
            "GD-777",
            "Roof Deck",
            "building",
            "route-focus",
            "procedure",
            "long_term",
            "cabin_house",
            "roofing,weatherproofing"
        );

        try (PackRepository repository = new PackRepository(databaseFile, null)) {
            List<SearchResult> context = repository.buildGuideAnswerContext(
                "how do i waterproof a cabin roof",
                Collections.singletonList(anchor),
                2
            );

            assertEquals(1, context.size());
            assertEquals("GD-777", context.get(0).guideId);
            assertEquals("Roof Deck", context.get(0).sectionHeading);
        }
    }

    private static void createPackDatabase(File file) {
        try (SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(file, null)) {
            database.execSQL(
                "CREATE TABLE chunks (" +
                    "chunk_id TEXT, " +
                    "vector_row_id INTEGER, " +
                    "guide_title TEXT, " +
                    "guide_id TEXT, " +
                    "section_heading TEXT, " +
                    "category TEXT, " +
                    "document TEXT, " +
                    "tags TEXT, " +
                    "description TEXT, " +
                    "content_role TEXT, " +
                    "time_horizon TEXT, " +
                    "structure_type TEXT, " +
                    "topic_tags TEXT" +
                    ")"
            );
            insertChunk(
                database,
                "chunk-1",
                "Roof Deck",
                "First roof deck row explains steep roof pitch, waterproofing, drainage, and bark overlap.",
                "roofing waterproofing cabin",
                "roof deck waterproofing"
            );
            insertChunk(
                database,
                "chunk-2",
                "Roof Deck",
                "Second roof deck row adds ridge cap detail, sealed seams, and rain-shedding drainage.",
                "roofing ridge waterproofing",
                "roof deck ridge cap"
            );
            insertChunk(
                database,
                "chunk-3",
                "Weatherproofing Layers",
                "Weatherproofing layers overlap bark courses and seal edges so water leaves the roof.",
                "weatherproofing bark roof",
                "weatherproofing layers"
            );
        }
    }

    private static void insertChunk(
        SQLiteDatabase database,
        String chunkId,
        String sectionHeading,
        String document,
        String tags,
        String description
    ) {
        database.execSQL(
            "INSERT INTO chunks (" +
                "chunk_id, vector_row_id, guide_title, guide_id, section_heading, category, document, " +
                "tags, description, content_role, time_horizon, structure_type, topic_tags" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            new Object[]{
                chunkId,
                0,
                "Cabin Roof Waterproofing",
                "GD-777",
                sectionHeading,
                "building",
                document,
                tags,
                description,
                "procedure",
                "long_term",
                "cabin_house",
                "roofing,weatherproofing"
            }
        );
    }
}
