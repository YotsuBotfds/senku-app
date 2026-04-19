package com.senku.mobile;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

final class AnswerPresenter {
    static final String HARNESS_PENDING_GENERATION = "detail.pendingGeneration";
    static final String HARNESS_FOLLOWUP_PREPARE = "detail.followup.prepare";
    static final String HARNESS_FOLLOWUP_DETERMINISTIC = "detail.followup.deterministic";
    static final String HARNESS_FOLLOWUP_ABSTAIN = "detail.followup.abstain";
    static final String HARNESS_FOLLOWUP_GENERATE = "detail.followup.generate";
    static final String HARNESS_FOLLOWUP_FAILURE = "detail.followup.failure";
    static final String HARNESS_TABLET_PREPARE = "detail.followup.prepare.tablet";
    static final String HARNESS_TABLET_DETERMINISTIC = "detail.followup.deterministic.tablet";
    static final String HARNESS_TABLET_ABSTAIN = "detail.followup.abstain.tablet";
    static final String HARNESS_TABLET_GENERATE = "detail.followup.generate.tablet";
    static final String HARNESS_TABLET_FAILURE = "detail.followup.failure.tablet";

    enum Kind {
        INITIAL_PENDING,
        PHONE_FOLLOWUP,
        TABLET_FOLLOWUP,
    }

    interface Host {
        Context applicationContext();
        File modelFile();
        Executor executor();
        SessionMemory sessionMemory();
        int currentRequestToken();
        boolean isCurrentRequestToken(int token);
        int beginHarnessTask(String tag);
        void runTrackedOnUiThread(int harnessToken, Runnable r);
        OfflineAnswerEngine.AnswerProgressListener createAnswerProgressListener(int requestToken);
        void onPreparePreview(int requestToken, Kind kind, OfflineAnswerEngine.PreparedAnswer preparedAnswer);
        void onSuccess(int requestToken, Kind kind, AnswerRunResult result);
        void onFailure(int requestToken, Kind kind, Throwable exc, String fallbackQuery);
    }

    interface FinalBodyResolver {
        String resolve(String finalAnswerBody, int requestToken);
    }

    interface RepositoryResolver {
        PackRepository resolve(PackRepository repo) throws Exception;
    }

    interface Engine {
        OfflineAnswerEngine.PreparedAnswer prepare(
            Context context,
            PackRepository repo,
            SessionMemory sessionMemory,
            File modelFile,
            String query
        ) throws Exception;

        OfflineAnswerEngine.AnswerRun generate(
            Context context,
            File modelFile,
            OfflineAnswerEngine.PreparedAnswer prepared,
            OfflineAnswerEngine.AnswerProgressListener progressListener
        ) throws Exception;
    }

    static final class AnswerRunResult {
        final OfflineAnswerEngine.AnswerRun answerRun;
        final List<SearchResult> answerSources;
        final String resolvedAnswerBody;
        final OfflineAnswerEngine.ConfidenceLabel confidenceLabel;

        AnswerRunResult(
            OfflineAnswerEngine.AnswerRun run,
            List<SearchResult> sources,
            String resolvedBody,
            OfflineAnswerEngine.ConfidenceLabel confidenceLabel
        ) {
            this.answerRun = run;
            this.answerSources = sources == null ? Collections.emptyList() : new ArrayList<>(sources);
            this.resolvedAnswerBody = safe(resolvedBody);
            this.confidenceLabel = confidenceLabel;
        }
    }

    private static final Engine DEFAULT_ENGINE = new Engine() {
        @Override
        public OfflineAnswerEngine.PreparedAnswer prepare(
            Context context,
            PackRepository repo,
            SessionMemory sessionMemory,
            File modelFile,
            String query
        ) throws Exception {
            return OfflineAnswerEngine.prepare(context, repo, sessionMemory, modelFile, query);
        }

        @Override
        public OfflineAnswerEngine.AnswerRun generate(
            Context context,
            File modelFile,
            OfflineAnswerEngine.PreparedAnswer prepared,
            OfflineAnswerEngine.AnswerProgressListener progressListener
        ) throws Exception {
            return OfflineAnswerEngine.generate(context, modelFile, prepared, progressListener);
        }
    };

    private final Host host;
    private final Engine engine;
    private final FinalBodyResolver finalBodyResolver;
    private final RepositoryResolver repositoryResolver;

    AnswerPresenter(Host host) {
        this(host, DEFAULT_ENGINE, (finalAnswerBody, requestToken) -> safe(finalAnswerBody), repo -> repo);
    }

    AnswerPresenter(Host host, FinalBodyResolver finalBodyResolver) {
        this(host, DEFAULT_ENGINE, finalBodyResolver, repo -> repo);
    }

    AnswerPresenter(
        Host host,
        FinalBodyResolver finalBodyResolver,
        RepositoryResolver repositoryResolver
    ) {
        this(host, DEFAULT_ENGINE, finalBodyResolver, repositoryResolver);
    }

    AnswerPresenter(
        Host host,
        Engine engine,
        FinalBodyResolver finalBodyResolver,
        RepositoryResolver repositoryResolver
    ) {
        this.host = host;
        this.engine = engine == null ? DEFAULT_ENGINE : engine;
        this.finalBodyResolver = finalBodyResolver == null
            ? (finalAnswerBody, requestToken) -> safe(finalAnswerBody)
            : finalBodyResolver;
        this.repositoryResolver = repositoryResolver == null
            ? repo -> repo
            : repositoryResolver;
    }

    void generateRestored(int requestToken, OfflineAnswerEngine.PreparedAnswer preparedAnswer) {
        Context context = host.applicationContext();
        File modelFile = host.modelFile();
        int harnessToken = host.beginHarnessTask(HARNESS_PENDING_GENERATION);
        host.executor().execute(() -> {
            try {
                OfflineAnswerEngine.AnswerRun answerRun = engine.generate(
                    context,
                    modelFile,
                    preparedAnswer,
                    host.createAnswerProgressListener(requestToken)
                );
                AnswerRunResult result = buildResult(answerRun, requestToken);
                host.runTrackedOnUiThread(harnessToken, () -> {
                    if (!host.isCurrentRequestToken(requestToken)) {
                        return;
                    }
                    host.onSuccess(requestToken, Kind.INITIAL_PENDING, result);
                });
            } catch (Exception exc) {
                host.runTrackedOnUiThread(harnessToken, () -> {
                    if (!host.isCurrentRequestToken(requestToken)) {
                        return;
                    }
                    host.onFailure(
                        requestToken,
                        Kind.INITIAL_PENDING,
                        exc,
                        safe(preparedAnswer == null ? null : preparedAnswer.query).trim()
                    );
                });
            }
        });
    }

    void prepareThenGenerate(int requestToken, Kind kind, PackRepository repo, String query) {
        if (kind != Kind.PHONE_FOLLOWUP && kind != Kind.TABLET_FOLLOWUP) {
            throw new IllegalArgumentException("prepareThenGenerate only supports follow-up kinds");
        }
        Context context = host.applicationContext();
        File modelFile = host.modelFile();
        SessionMemory sessionMemory = host.sessionMemory();
        String fallbackQuery = safe(query).trim();
        int prepareHarnessToken = host.beginHarnessTask(prepareTagFor(kind));
        host.executor().execute(() -> {
            try {
                PackRepository resolvedRepo = repositoryResolver.resolve(repo);
                OfflineAnswerEngine.PreparedAnswer preparedAnswer = engine.prepare(
                    context,
                    resolvedRepo,
                    sessionMemory,
                    modelFile,
                    fallbackQuery
                );
                if (!preparedAnswer.deterministic && !preparedAnswer.abstain) {
                    host.runTrackedOnUiThread(prepareHarnessToken, () -> {
                        if (!host.isCurrentRequestToken(requestToken)) {
                            return;
                        }
                        host.onPreparePreview(requestToken, kind, preparedAnswer);
                    });
                } else {
                    HarnessTestSignals.end(prepareHarnessToken);
                }
                int generationHarnessToken = host.beginHarnessTask(generationTagFor(kind, preparedAnswer));
                OfflineAnswerEngine.AnswerRun answerRun = engine.generate(
                    context,
                    modelFile,
                    preparedAnswer,
                    host.createAnswerProgressListener(requestToken)
                );
                AnswerRunResult result = buildResult(answerRun, requestToken);
                host.runTrackedOnUiThread(generationHarnessToken, () -> {
                    if (!host.isCurrentRequestToken(requestToken)) {
                        return;
                    }
                    host.onSuccess(requestToken, kind, result);
                });
            } catch (Exception exc) {
                HarnessTestSignals.end(prepareHarnessToken);
                int failureHarnessToken = host.beginHarnessTask(failureTagFor(kind));
                host.runTrackedOnUiThread(failureHarnessToken, () -> {
                    if (!host.isCurrentRequestToken(requestToken)) {
                        return;
                    }
                    host.onFailure(requestToken, kind, exc, fallbackQuery);
                });
            }
        });
    }

    private AnswerRunResult buildResult(OfflineAnswerEngine.AnswerRun answerRun, int requestToken) {
        List<SearchResult> answerSources = answerRun == null || answerRun.sources == null
            ? Collections.emptyList()
            : answerRun.sources;
        return new AnswerRunResult(
            answerRun,
            answerSources,
            finalBodyResolver.resolve(answerRun == null ? null : answerRun.answerBody, requestToken),
            answerRun == null ? null : answerRun.confidenceLabel
        );
    }

    private static String prepareTagFor(Kind kind) {
        return kind == Kind.TABLET_FOLLOWUP ? HARNESS_TABLET_PREPARE : HARNESS_FOLLOWUP_PREPARE;
    }

    private static String generationTagFor(Kind kind, OfflineAnswerEngine.PreparedAnswer preparedAnswer) {
        boolean deterministic = preparedAnswer != null && preparedAnswer.deterministic;
        boolean abstain = preparedAnswer != null && preparedAnswer.abstain;
        if (kind == Kind.TABLET_FOLLOWUP) {
            return deterministic
                ? HARNESS_TABLET_DETERMINISTIC
                : (abstain ? HARNESS_TABLET_ABSTAIN : HARNESS_TABLET_GENERATE);
        }
        return deterministic
            ? HARNESS_FOLLOWUP_DETERMINISTIC
            : (abstain ? HARNESS_FOLLOWUP_ABSTAIN : HARNESS_FOLLOWUP_GENERATE);
    }

    private static String failureTagFor(Kind kind) {
        return kind == Kind.TABLET_FOLLOWUP ? HARNESS_TABLET_FAILURE : HARNESS_FOLLOWUP_FAILURE;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
