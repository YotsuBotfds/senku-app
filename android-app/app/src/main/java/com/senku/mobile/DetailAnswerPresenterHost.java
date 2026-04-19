package com.senku.mobile;

import android.content.Context;

import java.io.File;
import java.util.concurrent.Executor;

final class DetailAnswerPresenterHost implements AnswerPresenter.Host {
    private final DetailActivity activity;

    DetailAnswerPresenterHost(DetailActivity activity) {
        this.activity = activity;
    }

    @Override
    public Context applicationContext() {
        return activity.getApplicationContext();
    }

    @Override
    public File modelFile() {
        return ModelFileStore.getImportedModelFile(activity);
    }

    @Override
    public Executor executor() {
        return activity.answerPresenterExecutor();
    }

    @Override
    public SessionMemory sessionMemory() {
        return activity.answerPresenterSessionMemory();
    }

    @Override
    public int currentRequestToken() {
        return activity.currentAnswerRequestToken();
    }

    @Override
    public boolean isCurrentRequestToken(int token) {
        return activity.isCurrentAnswerRequestToken(token);
    }

    @Override
    public int beginHarnessTask(String tag) {
        return activity.beginHarnessTask(tag);
    }

    @Override
    public void runTrackedOnUiThread(int harnessToken, Runnable r) {
        activity.runTrackedOnUiThread(harnessToken, r);
    }

    @Override
    public OfflineAnswerEngine.AnswerProgressListener createAnswerProgressListener(int requestToken) {
        return activity.createAnswerProgressListener(requestToken);
    }

    @Override
    public void onPreparePreview(
        int requestToken,
        AnswerPresenter.Kind kind,
        OfflineAnswerEngine.PreparedAnswer preparedAnswer
    ) {
        activity.applyPreparedPreviewState(preparedAnswer);
        if (kind == AnswerPresenter.Kind.TABLET_FOLLOWUP) {
            activity.clearTabletFollowUpSelectionState();
        } else {
            activity.clearPhoneFollowUpInput();
        }
        activity.renderDetailState();
        if (kind == AnswerPresenter.Kind.PHONE_FOLLOWUP) {
            activity.refreshRelatedGuides();
            activity.renderSources();
            activity.renderInlineSources();
            activity.maybeShowPrimarySourceProvenancePanel();
        }
        activity.setBusy(activity.buildInFlightStatus(false), true);
        activity.beginGenerationStallMonitor(requestToken);
    }

    @Override
    public void onSuccess(
        int requestToken,
        AnswerPresenter.Kind kind,
        AnswerPresenter.AnswerRunResult result
    ) {
        activity.applyAnswerSuccessState(requestToken, result);
        if (kind == AnswerPresenter.Kind.TABLET_FOLLOWUP) {
            activity.clearTabletFollowUpSelectionState();
        } else if (kind == AnswerPresenter.Kind.PHONE_FOLLOWUP) {
            activity.clearPhoneFollowUpInput();
        }
        activity.renderDetailState();
        if (kind != AnswerPresenter.Kind.TABLET_FOLLOWUP) {
            activity.refreshRelatedGuides();
        }
        activity.setBusy(OfflineAnswerEngine.buildCompletionStatus(activity, result.answerRun), false);
        activity.scrollToLatestTurn();
        if (kind == AnswerPresenter.Kind.INITIAL_PENDING) {
            activity.maybeStartAutoFollowUp();
        }
    }

    @Override
    public void onFailure(int requestToken, AnswerPresenter.Kind kind, Throwable exc, String fallbackQuery) {
        activity.applyAnswerFailureState(requestToken, exc, fallbackQuery);
        if (kind == AnswerPresenter.Kind.TABLET_FOLLOWUP) {
            activity.setTabletComposerDraft(activity.lastFailedQuery());
        }
        activity.renderDetailState();
        activity.setBusy(activity.buildGenerationFailureStatus(exc), false);
        if (kind != AnswerPresenter.Kind.TABLET_FOLLOWUP) {
            activity.restoreFollowUpInputFromLastFailedQuery();
        }
    }
}
