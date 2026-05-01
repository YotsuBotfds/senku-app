package com.senku.mobile;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class DetailSourcePreviewDensityTokenTest {
    @Test
    public void phoneSourceEntryPreviewUsesCompactTextTokens() throws Exception {
        Document document = parsePhoneDetailLayout();

        Element panel = elementByAndroidId(document, "detail_provenance_panel");
        assertEquals("10dp", android(panel, "padding"));

        Element stamp = elementByAndroidId(document, "detail_provenance_stamp");
        assertEquals("9sp", android(stamp, "textSize"));
        assertEquals("12sp", android(stamp, "lineHeight"));

        Element title = elementByAndroidId(document, "detail_provenance_title_text");
        assertEquals("12sp", android(title, "textSize"));
        assertEquals("15sp", android(title, "lineHeight"));
        assertEquals("4dp", android(title, "paddingTop"));
        assertEquals("4dp", android(title, "paddingBottom"));

        Element meta = elementByAndroidId(document, "detail_provenance_meta");
        assertEquals("12sp", android(meta, "textSize"));
        assertEquals("14sp", android(meta, "lineHeight"));
        assertEquals("1dp", android(meta, "lineSpacingExtra"));
        assertEquals("3", android(meta, "maxLines"));

        Element body = elementByAndroidId(document, "detail_provenance_body");
        assertEquals("12sp", android(body, "textSize"));
        assertEquals("16sp", android(body, "lineHeight"));
        assertEquals("1dp", android(body, "lineSpacingExtra"));
        assertEquals("3", android(body, "maxLines"));

        Element open = elementByAndroidId(document, "detail_provenance_open");
        assertEquals("6dp", android(open, "layout_marginTop"));
        assertEquals("0dp", android(open, "minHeight"));
        assertEquals("6dp", android(open, "paddingTop"));
        assertEquals("6dp", android(open, "paddingBottom"));
    }

    @Test
    public void phoneRelatedGuidePreviewKeepsOpenCtaClearOfComposer() throws Exception {
        Document document = parsePhoneDetailLayout();

        Element panel = elementByAndroidId(document, "detail_related_guide_preview_panel");
        assertEquals("8dp", android(panel, "layout_marginTop"));
        assertEquals("8dp", android(panel, "padding"));

        Element title = elementByAndroidId(document, "detail_related_guide_preview_title");
        assertEquals("12sp", android(title, "textSize"));
        assertEquals("15sp", android(title, "lineHeight"));
        assertEquals("4dp", android(title, "paddingTop"));
        assertEquals("4dp", android(title, "paddingBottom"));

        Element meta = elementByAndroidId(document, "detail_related_guide_preview_meta");
        assertEquals("6dp", android(meta, "layout_marginTop"));
        assertEquals("12sp", android(meta, "textSize"));
        assertEquals("15sp", android(meta, "lineHeight"));
        assertEquals("2", android(meta, "maxLines"));

        Element body = elementByAndroidId(document, "detail_related_guide_preview_body");
        assertEquals("4dp", android(body, "layout_marginTop"));
        assertEquals("12sp", android(body, "textSize"));
        assertEquals("16sp", android(body, "lineHeight"));
        assertEquals("1dp", android(body, "lineSpacingExtra"));
        assertEquals("4", android(body, "maxLines"));

        Element toggle = elementByAndroidId(document, "detail_related_guide_preview_toggle");
        assertEquals("0dp", android(toggle, "minHeight"));
        assertEquals("6dp", android(toggle, "paddingTop"));
        assertEquals("6dp", android(toggle, "paddingBottom"));

        Element open = elementByAndroidId(document, "detail_related_guide_preview_open");
        assertEquals("4dp", android(open, "layout_marginTop"));
        assertEquals("0dp", android(open, "minHeight"));
        assertEquals("6dp", android(open, "paddingTop"));
        assertEquals("6dp", android(open, "paddingBottom"));
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
}
