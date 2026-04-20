package com.senku.mobile;

import android.content.Context;
import android.text.Layout;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

final class DetailExpandableTextHelper {
    boolean needsToggle(Layout layout, int collapsedMaxLines) {
        if (layout == null) {
            return false;
        }
        int lineCount = layout.getLineCount();
        if (lineCount > collapsedMaxLines) {
            return true;
        }
        return lineCount > 0 && layout.getEllipsisCount(lineCount - 1) > 0;
    }

    void applyExpansionState(
        Context context,
        TextView body,
        Button toggleButton,
        boolean expanded,
        int collapsedMaxLines
    ) {
        if (body == null) {
            return;
        }
        if (expanded) {
            body.setMaxLines(Integer.MAX_VALUE);
            body.setEllipsize(null);
        } else {
            body.setMaxLines(collapsedMaxLines);
            body.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (toggleButton != null) {
            int labelRes = expanded ? R.string.detail_provenance_show_less : R.string.detail_provenance_show_more;
            toggleButton.setText(labelRes);
            toggleButton.setContentDescription(context.getString(labelRes));
        }
    }
}
