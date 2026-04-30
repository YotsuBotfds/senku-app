package com.senku.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class DetailOverflowPolicy {
    static final int SAVE_GUIDE_MENU_ID = 1;
    static final int HOME_MENU_ID = 2;

    enum Action {
        SAVE_GUIDE,
        HOME
    }

    private DetailOverflowPolicy() {
    }

    static boolean shouldShow(
        boolean answerMode,
        boolean phoneXmlDetailLayoutActive,
        boolean compactPortraitPhone,
        String pinnableGuideId
    ) {
        return answerMode
            && phoneXmlDetailLayoutActive
            && !safe(pinnableGuideId).trim().isEmpty();
    }

    static List<Action> actions(
        boolean answerMode,
        boolean phoneXmlDetailLayoutActive,
        boolean compactPortraitPhone,
        String pinnableGuideId
    ) {
        if (!shouldShow(answerMode, phoneXmlDetailLayoutActive, compactPortraitPhone, pinnableGuideId)) {
            return Collections.emptyList();
        }
        ArrayList<Action> actions = new ArrayList<>();
        actions.add(Action.SAVE_GUIDE);
        actions.add(Action.HOME);
        return actions;
    }

    static int menuId(Action action) {
        if (action == Action.SAVE_GUIDE) {
            return SAVE_GUIDE_MENU_ID;
        }
        if (action == Action.HOME) {
            return HOME_MENU_ID;
        }
        return 0;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
