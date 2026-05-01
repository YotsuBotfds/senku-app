package com.senku.mobile;

final class DetailBackPolicy {
    enum SourceRoute {
        UNKNOWN,
        ANSWER,
        EMERGENCY_ANSWER,
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

    enum RouteBackIntent {
        FINISH_OPENED_DETAIL,
        NAVIGATE_HOME,
        NAVIGATE_EMERGENCY_MANUAL_HOME
    }

    enum FinishBehavior {
        FINISH,
        FINISH_AFTER_TRANSITION
    }

    private static final RouteBackContract UNKNOWN_ROUTE_BACK_CONTRACT = new RouteBackContract(
        RouteBackIntent.FINISH_OPENED_DETAIL,
        RouteBackIntent.NAVIGATE_HOME
    );
    private static final RouteBackContract ANSWER_ROUTE_BACK_CONTRACT = new RouteBackContract(
        RouteBackIntent.FINISH_OPENED_DETAIL,
        RouteBackIntent.NAVIGATE_HOME
    );
    private static final RouteBackContract EMERGENCY_ANSWER_ROUTE_BACK_CONTRACT = new RouteBackContract(
        RouteBackIntent.FINISH_OPENED_DETAIL,
        RouteBackIntent.NAVIGATE_EMERGENCY_MANUAL_HOME
    );
    private static final RouteBackContract GUIDE_ROUTE_BACK_CONTRACT = new RouteBackContract(
        RouteBackIntent.FINISH_OPENED_DETAIL,
        RouteBackIntent.NAVIGATE_HOME
    );
    private static final RouteBackContract HOME_GUIDE_ROUTE_BACK_CONTRACT = new RouteBackContract(
        RouteBackIntent.FINISH_OPENED_DETAIL,
        RouteBackIntent.NAVIGATE_HOME
    );
    private static final RouteBackContract CROSS_REFERENCE_GUIDE_ROUTE_BACK_CONTRACT = new RouteBackContract(
        RouteBackIntent.FINISH_OPENED_DETAIL,
        RouteBackIntent.NAVIGATE_HOME
    );

    private DetailBackPolicy() {
    }

    static Decision decide(boolean taskRoot) {
        return decide(new Inputs(taskRoot, SourceRoute.UNKNOWN, BackTrigger.SYSTEM_BACK));
    }

    static Decision decide(Inputs inputs) {
        Inputs normalized = inputs == null ? Inputs.defaultState() : inputs.normalized();
        RouteBackIntent routeIntent = routeBackIntent(normalized);
        return decisionFor(routeIntent);
    }

    static VisibleBackAffordance visibleBackAffordance(boolean taskRoot) {
        return visibleBackAffordance(new Inputs(taskRoot, SourceRoute.UNKNOWN, BackTrigger.VISIBLE_BACK_BUTTON));
    }

    static VisibleBackAffordance visibleBackAffordance(Inputs inputs) {
        Inputs normalized = inputs == null ? Inputs.defaultState() : inputs.normalized();
        Decision decision = decide(normalized);
        if (decision.effect == Effect.NAVIGATE_HOME) {
            if (decision.routeIntent == RouteBackIntent.NAVIGATE_EMERGENCY_MANUAL_HOME) {
                return new VisibleBackAffordance(
                    R.string.detail_emergency_app_rail_manual_label,
                    R.string.detail_emergency_app_rail_manual_content_description,
                    false
                );
            }
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

    private static RouteBackIntent routeBackIntent(Inputs inputs) {
        RouteBackContract contract = routeBackContract(inputs.sourceRoute);
        return inputs.taskRoot ? contract.taskRootIntent : contract.stackedIntent;
    }

    private static RouteBackContract routeBackContract(SourceRoute sourceRoute) {
        SourceRoute normalized = sourceRoute == null ? SourceRoute.UNKNOWN : sourceRoute;
        return switch (normalized) {
            case UNKNOWN -> UNKNOWN_ROUTE_BACK_CONTRACT;
            case ANSWER -> ANSWER_ROUTE_BACK_CONTRACT;
            case EMERGENCY_ANSWER -> EMERGENCY_ANSWER_ROUTE_BACK_CONTRACT;
            case GUIDE -> GUIDE_ROUTE_BACK_CONTRACT;
            case HOME_GUIDE -> HOME_GUIDE_ROUTE_BACK_CONTRACT;
            case CROSS_REFERENCE_GUIDE -> CROSS_REFERENCE_GUIDE_ROUTE_BACK_CONTRACT;
        };
    }

    private static Decision decisionFor(RouteBackIntent routeIntent) {
        RouteBackIntent normalized = routeIntent == null ? RouteBackIntent.FINISH_OPENED_DETAIL : routeIntent;
        return switch (normalized) {
            case FINISH_OPENED_DETAIL -> new Decision(
                Effect.FINISH_ACTIVITY,
                FinishBehavior.FINISH,
                RouteBackIntent.FINISH_OPENED_DETAIL
            );
            case NAVIGATE_HOME -> new Decision(
                Effect.NAVIGATE_HOME,
                FinishBehavior.FINISH,
                RouteBackIntent.NAVIGATE_HOME
            );
            case NAVIGATE_EMERGENCY_MANUAL_HOME -> new Decision(
                Effect.NAVIGATE_HOME,
                FinishBehavior.FINISH,
                RouteBackIntent.NAVIGATE_EMERGENCY_MANUAL_HOME
            );
        };
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
        final RouteBackIntent routeIntent;

        Decision(Effect effect, FinishBehavior finishBehavior) {
            this(effect, finishBehavior, routeIntentForEffect(effect));
        }

        Decision(Effect effect, FinishBehavior finishBehavior, RouteBackIntent routeIntent) {
            this.effect = effect == null ? Effect.FINISH_ACTIVITY : effect;
            this.finishBehavior = finishBehavior == null ? FinishBehavior.FINISH : finishBehavior;
            this.routeIntent = routeIntent == null ? routeIntentForEffect(this.effect) : routeIntent;
        }

        private static RouteBackIntent routeIntentForEffect(Effect effect) {
            return effect == Effect.NAVIGATE_HOME
                ? RouteBackIntent.NAVIGATE_HOME
                : RouteBackIntent.FINISH_OPENED_DETAIL;
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

    private static final class RouteBackContract {
        final RouteBackIntent stackedIntent;
        final RouteBackIntent taskRootIntent;

        RouteBackContract(RouteBackIntent stackedIntent, RouteBackIntent taskRootIntent) {
            this.stackedIntent = stackedIntent == null
                ? RouteBackIntent.FINISH_OPENED_DETAIL
                : stackedIntent;
            this.taskRootIntent = taskRootIntent == null
                ? RouteBackIntent.NAVIGATE_HOME
                : taskRootIntent;
        }
    }
}
