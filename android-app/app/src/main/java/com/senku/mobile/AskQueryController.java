package com.senku.mobile;

import android.content.Context;

import java.io.File;
import java.util.concurrent.Executor;

final class AskQueryController {
    static final String HARNESS_PREPARE = "main.ask.prepare";

    interface Host {
        Context applicationContext();
        Executor executor();
        SessionMemory sessionMemory();
        boolean isRepositoryAvailable();
        PackRepository repository();
        File modelFile();
        HostInferenceConfig.Settings hostInferenceSettings();
        boolean reviewedCardRuntimeEnabled();
        boolean hasAutoQuery();
        int beginHarnessTask(String label);
        void runTrackedOnUiThread(int harnessToken, Runnable action);
        void onPackUnavailable();
        void onBlankQuery();
        void onDeterministicAnswer(
            String query,
            DeterministicAnswerRouter.DeterministicAnswer deterministic,
            String answerBody
        );
        void onModelUnavailable(boolean hasAutoQuery);
        void onPrepareStarted(String query);
        void onPrepareSuccess(OfflineAnswerEngine.PreparedAnswer preparedAnswer);
        void onPrepareFailure(
            String query,
            OfflineAnswerEngine.PreparedAnswer failedPrepared,
            Exception exception,
            boolean hasAutoQuery
        );
    }

    interface Engine {
        OfflineAnswerEngine.PreparedAnswer prepare(
            Context context,
            PackRepository repo,
            SessionMemory sessionMemory,
            File modelFile,
            String query
        ) throws Exception;
    }

    interface DeterministicMatcher {
        DeterministicAnswerRouter.DeterministicAnswer match(String query);
    }

    private static final Engine DEFAULT_ENGINE = OfflineAnswerEngine::prepare;
    private static final DeterministicMatcher DEFAULT_MATCHER = DeterministicAnswerRouter::match;

    private final Host host;
    private final Engine engine;
    private final DeterministicMatcher deterministicMatcher;

    AskQueryController(Host host) {
        this(host, DEFAULT_ENGINE, DEFAULT_MATCHER);
    }

    AskQueryController(Host host, Engine engine, DeterministicMatcher deterministicMatcher) {
        this.host = host;
        this.engine = engine == null ? DEFAULT_ENGINE : engine;
        this.deterministicMatcher = deterministicMatcher == null ? DEFAULT_MATCHER : deterministicMatcher;
    }

    void runAsk(String rawQuery) {
        String query = safe(rawQuery).trim();
        if (!host.isRepositoryAvailable()) {
            host.onPackUnavailable();
            return;
        }
        if (query.isEmpty()) {
            host.onBlankQuery();
            return;
        }

        DeterministicAnswerRouter.DeterministicAnswer deterministic = deterministicMatcher.match(query);
        if (deterministic != null) {
            String answerBody = PromptBuilder.buildAnswerBody(deterministic.answerText, deterministic.sources, 0);
            host.onDeterministicAnswer(query, deterministic, answerBody);
            return;
        }

        File modelFile = host.modelFile();
        HostInferenceConfig.Settings inferenceSettings = host.hostInferenceSettings();
        if (!host.reviewedCardRuntimeEnabled()
            && (inferenceSettings == null || !inferenceSettings.enabled)
            && modelFile == null) {
            host.onModelUnavailable(host.hasAutoQuery());
            return;
        }

        host.onPrepareStarted(query);
        PackRepository repo = host.repository();
        Context context = host.applicationContext();
        SessionMemory sessionMemory = host.sessionMemory();
        boolean hasAutoQuery = host.hasAutoQuery();
        int harnessToken = host.beginHarnessTask(HARNESS_PREPARE);
        host.executor().execute(() -> {
            OfflineAnswerEngine.PreparedAnswer prepared = null;
            try {
                prepared = engine.prepare(context, repo, sessionMemory, modelFile, query);
                OfflineAnswerEngine.PreparedAnswer preparedAnswer = prepared;
                host.runTrackedOnUiThread(harnessToken, () -> host.onPrepareSuccess(preparedAnswer));
            } catch (Exception exc) {
                OfflineAnswerEngine.PreparedAnswer failedPrepared = prepared;
                host.runTrackedOnUiThread(
                    harnessToken,
                    () -> host.onPrepareFailure(query, failedPrepared, exc, hasAutoQuery)
                );
            }
        });
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
