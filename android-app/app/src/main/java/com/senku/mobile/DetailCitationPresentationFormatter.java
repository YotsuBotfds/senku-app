package com.senku.mobile;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DetailCitationPresentationFormatter {
    private static final Pattern INLINE_CITATION_PATTERN = Pattern.compile("\\s*\\[(?:GD-\\d{3})(?:,\\s*GD-\\d{3})*\\]");
    private static final Pattern DISPLAY_INLINE_CITATION_PATTERN =
        Pattern.compile("\\[(?:GD-\\d{3})(?:,\\s*GD-\\d{3})*\\]");

    private final Context context;

    DetailCitationPresentationFormatter(Context context) {
        this.context = context;
    }

    void applyInlineCitationSpans(SpannableStringBuilder styled) {
        if (styled == null || styled.length() == 0) {
            return;
        }
        Matcher matcher = DISPLAY_INLINE_CITATION_PATTERN.matcher(styled.toString());
        while (matcher.find()) {
            styled.setSpan(new StyleSpan(Typeface.BOLD), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styled.setSpan(
                new TypefaceSpan("monospace"),
                matcher.start(),
                matcher.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            styled.setSpan(
                new RelativeSizeSpan(0.94f),
                matcher.start(),
                matcher.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            styled.setSpan(
                new ForegroundColorSpan(context.getColor(R.color.senku_card_warm_light)),
                matcher.start(),
                matcher.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            styled.setSpan(
                new BackgroundColorSpan(context.getColor(R.color.senku_bg_olive_moss)),
                matcher.start(),
                matcher.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    String stripInlineCitationText(String text) {
        return INLINE_CITATION_PATTERN.matcher(safe(text)).replaceAll("");
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
