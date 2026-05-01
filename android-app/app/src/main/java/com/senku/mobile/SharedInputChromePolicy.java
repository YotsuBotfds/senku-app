package com.senku.mobile;

import android.view.inputmethod.EditorInfo;

import com.senku.mobile.AskSearchCoordinator.SubmitTarget;

final class SharedInputChromePolicy {
    private SharedInputChromePolicy() {
    }

    static int inputHintResource(SubmitTarget target) {
        return target == SubmitTarget.ASK ? R.string.ask_hint : R.string.search_hint;
    }

    static int inputDescriptionResource(SubmitTarget target) {
        return target == SubmitTarget.ASK
            ? R.string.ask_input_description
            : R.string.search_input_description;
    }

    static int inputImeAction(SubmitTarget target) {
        return target == SubmitTarget.ASK ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_SEARCH;
    }

    static int submitButtonLabelResource(SubmitTarget target, boolean answerReady) {
        if (target == SubmitTarget.ASK) {
            return answerReady ? R.string.ask_button_ready : R.string.ask_button;
        }
        return R.string.home_search_button;
    }

    static int submitButtonDescriptionResource(SubmitTarget target) {
        return target == SubmitTarget.ASK
            ? R.string.ask_button_description
            : R.string.search_button_description;
    }
}
