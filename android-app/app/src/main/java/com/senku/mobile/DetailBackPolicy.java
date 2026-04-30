package com.senku.mobile;

public final class DetailBackPolicy {
    enum SourceRoute {
        UNKNOWN,
        ANSWER,
        GUIDE,
        HOME_GUIDE,
        CROSS_REFERENCE_GUIDE
    }

    enum BackTrigger {
        VISIBLE_BACK_BUTTON,
        SYSTEM_BACK,
        SUPPORT_NAVIGATE_UP,
        GUIDE_RETURN
    }

    enum Effect {
        FINISH_ACTIVITY,
        NAVIGATE_HOME
    }

    enum FinishBehavior {
        FINISH,
        FINISH_AFTER_TRANSITION
    }

    private DetailBackPolicy() {
    }

    static Decision decide(boolean taskRoot) {
        return decide(new Inputs(taskRoot, SourceRoute.UNKNOWN, BackTrigger.SYSTEM_BACK));
    }

    static Decision decide(Inputs inputs) {
        Inputs normalized = inputs == null ? Inputs.defaultState() : inputs.normalized();
        // SourceRoute and BackTrigger are kept as explicit inputs so call sites can
        // classify how detail was reached without implying different navigation yet.
        return normalized.taskRoot
            ? new Decision(Effect.NAVIGATE_HOME, FinishBehavior.FINISH)
            : new Decision(Effect.FINISH_ACTIVITY, FinishBehavior.FINISH);
    }

    static VisibleBackAffordance visibleBackAffordance(boolean taskRoot) {
        return new VisibleBackAffordance(
            R.string.detail_back,
            R.string.detail_back_content_description,
            false
        );
    }

    static VisibleBackAffordance visibleBackAffordance(Inputs inputs) {
        Inputs normalized = inputs == null ? Inputs.defaultState() : inputs.normalized();
        Decision decision = decide(normalized);
        if (decision.effect == Effect.NAVIGATE_HOME) {
            return new VisibleBackAffordance(
                R.string.home_button,
                R.string.detail_home_content_description,
                false
            );
        }
        return new VisibleBackAffordance(
            R.string.detail_back,
            R.string.detail_back_content_description,
            false
        );
    }

    static boolean shouldFallbackToHome(boolean taskRoot) {
        return decide(taskRoot).effect == Effect.NAVIGATE_HOME;
    }

    static final class Inputs {
        final boolean taskRoot;
        final SourceRoute sourceRoute;
        final BackTrigger trigger;

        Inputs(boolean taskRoot, SourceRoute sourceRoute, BackTrigger trigger) {
            this.taskRoot = taskRoot;
            this.sourceRoute = sourceRoute == null ? SourceRoute.UNKNOWN : sourceRoute;
            this.trigger = trigger == null ? BackTrigger.SYSTEM_BACK : trigger;
        }

        private static Inputs defaultState() {
            return new Inputs(false, SourceRoute.UNKNOWN, BackTrigger.SYSTEM_BACK);
        }

        private Inputs normalized() {
            return new Inputs(taskRoot, sourceRoute, trigger);
        }
    }

    static final class Decision {
        final Effect effect;
        final FinishBehavior finishBehavior;

        Decision(Effect effect, FinishBehavior finishBehavior) {
            this.effect = effect == null ? Effect.FINISH_ACTIVITY : effect;
            this.finishBehavior = finishBehavior == null ? FinishBehavior.FINISH : finishBehavior;
        }
    }

    static final class VisibleBackAffordance {
        final int labelResource;
        final int contentDescriptionResource;
        final boolean longPressHomeShortcutEnabled;

        VisibleBackAffordance(
            int labelResource,
            int contentDescriptionResource,
            boolean longPressHomeShortcutEnabled
        ) {
            this.labelResource = labelResource;
            this.contentDescriptionResource = contentDescriptionResource;
            this.longPressHomeShortcutEnabled = longPressHomeShortcutEnabled;
        }
    }
}
