package com.senku.mobile;

import com.senku.mobile.AskSearchCoordinator.SubmitTarget;
import com.senku.ui.primitives.BottomTabDestination;

final class MainSharedInputSubmitPolicy {
    private MainSharedInputSubmitPolicy() {
    }

    static SharedSubmitAction resolveSearchButtonSubmitAction(
        BottomTabDestination activePhoneTab,
        boolean askLaneActive,
        boolean answerReady
    ) {
        SubmitTarget target = AskSearchCoordinator.resolveSubmitTarget(activePhoneTab, askLaneActive);
        return new SharedSubmitAction(
            target,
            SharedInputChromePolicy.submitButtonLabelResource(target, answerReady),
            SharedInputChromePolicy.submitButtonDescriptionResource(target)
        );
    }

    static final class SharedSubmitAction {
        final SubmitTarget target;
        final int buttonTextResource;
        final int buttonDescriptionResource;

        SharedSubmitAction(
            SubmitTarget target,
            int buttonTextResource,
            int buttonDescriptionResource
        ) {
            this.target = target == null ? SubmitTarget.SEARCH : target;
            this.buttonTextResource = buttonTextResource;
            this.buttonDescriptionResource = buttonDescriptionResource;
        }
    }
}
