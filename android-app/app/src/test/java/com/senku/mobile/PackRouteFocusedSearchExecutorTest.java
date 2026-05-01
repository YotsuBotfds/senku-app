package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import android.database.sqlite.SQLiteException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public final class PackRouteFocusedSearchExecutorTest {
    @Test
    public void likeFallbackReturnsZeroWhenSqliteExecutionFails() {
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan chunkPlan = PackRouteSearchSqlPolicy.chunkLikePlan(
            List.of("shelter"),
            List.of("building"),
            12
        );
        PackRouteSearchSqlPolicy.RouteLikeSqlPlan guidePlan = PackRouteSearchSqlPolicy.guideLikePlan(
            List.of("shelter"),
            List.of("building"),
            12
        );
        AtomicInteger attempts = new AtomicInteger();

        int chunkAdded = PackRouteFocusedSearchExecutor.collectLikeCursorSafelyForTest(
            chunkPlan,
            (sql, args) -> {
                attempts.incrementAndGet();
                throw new SQLiteException("missing chunk fallback column");
            }
        );
        int guideAdded = PackRouteFocusedSearchExecutor.collectLikeCursorSafelyForTest(
            guidePlan,
            (sql, args) -> {
                attempts.incrementAndGet();
                throw new SQLiteException("missing guide fallback column");
            }
        );

        assertEquals(0, chunkAdded);
        assertEquals(0, guideAdded);
        assertEquals(2, attempts.get());
    }
}
