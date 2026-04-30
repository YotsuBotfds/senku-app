package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import android.view.inputmethod.EditorInfo;

import com.senku.mobile.AskSearchCoordinator.SubmitTarget;

import org.junit.Test;

public final class SharedInputChromePolicyTest {
    @Test
    public void askTargetUsesQuestionChrome() {
        assertEquals(R.string.ask_hint, SharedInputChromePolicy.inputHintResource(SubmitTarget.ASK));
        assertEquals(
            R.string.ask_input_description,
            SharedInputChromePolicy.inputDescriptionResource(SubmitTarget.ASK)
        );
        assertEquals(EditorInfo.IME_ACTION_DONE, SharedInputChromePolicy.inputImeAction(SubmitTarget.ASK));
        assertEquals(
            R.string.ask_button,
            SharedInputChromePolicy.submitButtonLabelResource(SubmitTarget.ASK, false)
        );
        assertEquals(
            R.string.ask_button_ready,
            SharedInputChromePolicy.submitButtonLabelResource(SubmitTarget.ASK, true)
        );
        assertEquals(
            R.string.ask_button_description,
            SharedInputChromePolicy.submitButtonDescriptionResource(SubmitTarget.ASK)
        );
    }

    @Test
    public void searchTargetUsesSearchChrome() {
        assertEquals(R.string.search_hint, SharedInputChromePolicy.inputHintResource(SubmitTarget.SEARCH));
        assertEquals(
            R.string.search_input_description,
            SharedInputChromePolicy.inputDescriptionResource(SubmitTarget.SEARCH)
        );
        assertEquals(EditorInfo.IME_ACTION_SEARCH, SharedInputChromePolicy.inputImeAction(SubmitTarget.SEARCH));
        assertEquals(
            R.string.external_review_home_search_button,
            SharedInputChromePolicy.submitButtonLabelResource(SubmitTarget.SEARCH, false)
        );
        assertEquals(
            R.string.external_review_home_search_button,
            SharedInputChromePolicy.submitButtonLabelResource(SubmitTarget.SEARCH, true)
        );
        assertEquals(
            R.string.search_button_description,
            SharedInputChromePolicy.submitButtonDescriptionResource(SubmitTarget.SEARCH)
        );
    }

    @Test
    public void nullTargetFallsBackToSearchChrome() {
        assertEquals(R.string.search_hint, SharedInputChromePolicy.inputHintResource(null));
        assertEquals(
            R.string.search_input_description,
            SharedInputChromePolicy.inputDescriptionResource(null)
        );
        assertEquals(EditorInfo.IME_ACTION_SEARCH, SharedInputChromePolicy.inputImeAction(null));
        assertEquals(
            R.string.external_review_home_search_button,
            SharedInputChromePolicy.submitButtonLabelResource(null, true)
        );
        assertEquals(
            R.string.search_button_description,
            SharedInputChromePolicy.submitButtonDescriptionResource(null)
        );
    }
}
