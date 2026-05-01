package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class DetailSourcePreviewDensityTokenTest {
    @Test
    public void phoneSourceEntryPreviewUsesCompactReadableXmlDensity() throws Exception {
        Document document = parsePhoneDetailLayout();

        int panelPadding = dimensionInt(elementByAndroidId(document, "detail_provenance_panel"), "padding");
        TextToken stamp = textToken(elementByAndroidId(document, "detail_provenance_stamp"));
        TextToken title = textToken(elementByAndroidId(document, "detail_provenance_title_text"));
        TextToken meta = textToken(elementByAndroidId(document, "detail_provenance_meta"));
        TextToken body = textToken(elementByAndroidId(document, "detail_provenance_body"));
        ButtonToken open = buttonToken(elementByAndroidId(document, "detail_provenance_open"));

        assertTrue(panelPadding > 0);
        assertTrue(stamp.textSizeSp < title.textSizeSp);
        assertTrue(title.hasReadableLineHeight());
        assertTrue(meta.hasReadableLineHeight());
        assertTrue(body.hasReadableLineHeight());
        assertTrue(body.lineHeightSp > meta.lineHeightSp);
        assertTrue(meta.maxLines <= DetailActivity.resolvePhonePortraitSourceCardMaxLines(false, false));
        assertTrue(body.maxLines <= DetailActivity.resolvePhonePortraitSourceCardMaxLines(false, false));
        assertTrue(open.isCompactAction());
        assertTrue(open.topMarginDp <= panelPadding);
    }

    @Test
    public void phoneRelatedGuidePreviewKeepsOpenCtaClearOfComposer() throws Exception {
        Document document = parsePhoneDetailLayout();

        PanelToken panel = panelToken(elementByAndroidId(document, "detail_related_guide_preview_panel"));
        TextToken title = textToken(elementByAndroidId(document, "detail_related_guide_preview_title"));
        TextToken meta = textToken(elementByAndroidId(document, "detail_related_guide_preview_meta"));
        TextToken body = textToken(elementByAndroidId(document, "detail_related_guide_preview_body"));
        ButtonToken toggle = buttonToken(elementByAndroidId(document, "detail_related_guide_preview_toggle"));
        ButtonToken open = buttonToken(elementByAndroidId(document, "detail_related_guide_preview_open"));

        assertTrue(panel.marginTopDp <= panel.paddingDp);
        assertTrue(title.hasReadableLineHeight());
        assertTrue(meta.hasReadableLineHeight());
        assertTrue(body.hasReadableLineHeight());
        assertTrue(meta.maxLines < body.maxLines);
        assertTrue(body.maxLines <= 4);
        assertTrue(toggle.isCompactAction());
        assertTrue(open.isCompactAction());
        assertTrue(open.topMarginDp <= panel.paddingDp);

        assertTrue(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(
            true,
            true,
            true,
            true,
            true
        ));
        assertFalse(DetailActivity.shouldApplyRelatedGuidePreviewCtaClearance(
            true,
            true,
            true,
            false,
            true
        ));
        assertEquals(
            28,
            DetailActivity.resolveRelatedGuidePreviewCtaBottomClearancePx(8, 12, 16)
        );
    }

    private static Document parsePhoneDetailLayout() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(locatePhoneDetailLayout());
    }

    private static Element elementByAndroidId(Document document, String id) {
        NodeList nodes = document.getElementsByTagName("*");
        for (int index = 0; index < nodes.getLength(); index++) {
            Element element = (Element) nodes.item(index);
            String value = android(element, "id");
            if (value.equals("@+id/" + id) || value.equals("@id/" + id)) {
                return element;
            }
        }
        throw new AssertionError("Unable to locate @" + id);
    }

    private static TextToken textToken(Element element) {
        return new TextToken(
            dimensionFloat(element, "textSize"),
            dimensionFloat(element, "lineHeight"),
            dimensionInt(element, "lineSpacingExtra"),
            integer(element, "maxLines")
        );
    }

    private static PanelToken panelToken(Element element) {
        return new PanelToken(
            dimensionInt(element, "padding"),
            dimensionInt(element, "layout_marginTop")
        );
    }

    private static ButtonToken buttonToken(Element element) {
        return new ButtonToken(
            dimensionInt(element, "minHeight"),
            dimensionInt(element, "paddingTop"),
            dimensionInt(element, "paddingBottom"),
            dimensionInt(element, "layout_marginTop")
        );
    }

    private static int dimensionInt(Element element, String name) {
        return Math.round(dimensionFloat(element, name));
    }

    private static float dimensionFloat(Element element, String name) {
        String value = android(element, name).trim();
        if (value.isEmpty()) {
            return 0f;
        }
        return Float.parseFloat(value.replace("dp", "").replace("sp", ""));
    }

    private static int integer(Element element, String name) {
        String value = android(element, name).trim();
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    private static String android(Element element, String name) {
        String namespaced = element.getAttributeNS("http://schemas.android.com/apk/res/android", name);
        return namespaced == null || namespaced.isEmpty()
            ? element.getAttribute("android:" + name)
            : namespaced;
    }

    private static File locatePhoneDetailLayout() {
        File root = new File(System.getProperty("user.dir")).getAbsoluteFile();
        while (root != null) {
            File candidate = new File(root, "android-app/app/src/main/res/layout/activity_detail.xml");
            if (candidate.exists()) {
                return candidate;
            }
            candidate = new File(root, "app/src/main/res/layout/activity_detail.xml");
            if (candidate.exists()) {
                return candidate;
            }
            root = root.getParentFile();
        }
        throw new AssertionError("Unable to locate phone activity_detail.xml");
    }

    private static final class TextToken {
        final float textSizeSp;
        final float lineHeightSp;
        final int lineSpacingExtraDp;
        final int maxLines;

        TextToken(float textSizeSp, float lineHeightSp, int lineSpacingExtraDp, int maxLines) {
            this.textSizeSp = textSizeSp;
            this.lineHeightSp = lineHeightSp;
            this.lineSpacingExtraDp = lineSpacingExtraDp;
            this.maxLines = maxLines;
        }

        boolean hasReadableLineHeight() {
            return lineHeightSp >= textSizeSp && lineSpacingExtraDp >= 0;
        }
    }

    private static final class PanelToken {
        final int paddingDp;
        final int marginTopDp;

        PanelToken(int paddingDp, int marginTopDp) {
            this.paddingDp = paddingDp;
            this.marginTopDp = marginTopDp;
        }
    }

    private static final class ButtonToken {
        final int minHeightDp;
        final int paddingTopDp;
        final int paddingBottomDp;
        final int topMarginDp;

        ButtonToken(int minHeightDp, int paddingTopDp, int paddingBottomDp, int topMarginDp) {
            this.minHeightDp = minHeightDp;
            this.paddingTopDp = paddingTopDp;
            this.paddingBottomDp = paddingBottomDp;
            this.topMarginDp = topMarginDp;
        }

        boolean isCompactAction() {
            return minHeightDp == 0 && paddingTopDp > 0 && paddingBottomDp > 0;
        }
    }
}
