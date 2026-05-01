package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class DetailOverflowPolicy {
    static final int SAVE_GUIDE_MENU_ID = 1;
    static final int HOME_MENU_ID = 2;
    static final int NO_MENU_ID = 0;

    enum Action {
        BACK(NO_MENU_ID, false),
        HOME(HOME_MENU_ID, true),
        SAVE_GUIDE(SAVE_GUIDE_MENU_ID, true),
        OVERFLOW(NO_MENU_ID, false);

        final int menuId;
        final boolean overflowMenuItem;

        Action(int menuId, boolean overflowMenuItem) {
            this.menuId = menuId;
            this.overflowMenuItem = overflowMenuItem;
        }
    }

    private DetailOverflowPolicy() {
    }

    static boolean shouldShow(
        boolean answerMode,
        boolean phoneXmlDetailLayoutActive,
        boolean compactPortraitPhone,
        String pinnableGuideId
    ) {
        return !actions(answerMode, phoneXmlDetailLayoutActive, compactPortraitPhone, pinnableGuideId).isEmpty();
    }

    static List<Action> actions(
        boolean answerMode,
        boolean phoneXmlDetailLayoutActive,
        boolean compactPortraitPhone,
        String pinnableGuideId
    ) {
        if (!answerMode || !phoneXmlDetailLayoutActive || safe(pinnableGuideId).trim().isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Action> actions = new ArrayList<>();
        actions.add(Action.SAVE_GUIDE);
        actions.add(Action.HOME);
        return actions;
    }

    static int menuId(Action action) {
        return action == null ? NO_MENU_ID : action.menuId;
    }

    static boolean isOverflowMenuItem(Action action) {
        return action != null && action.overflowMenuItem;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
