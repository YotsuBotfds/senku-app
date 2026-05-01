package com.senku.mobile;

final class SearchRowAccentPolicy {
    private SearchRowAccentPolicy() {
    }

    static float alphaForPosition(int position) {
        if (position <= 0) {
            return 1.0f;
        }
        if (position == 1) {
            return 0.82f;
        }
        if (position == 2) {
            return 0.65f;
        }
        return 0.50f;
    }
}
