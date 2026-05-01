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

        Element title = elementByAndroidId(document, "detail_provenance_title_text");
        assertEquals("13sp", android(title, "textSize"));

        Element meta = elementByAndroidId(document, "detail_provenance_meta");
        assertEquals("12sp", android(meta, "textSize"));
        assertEquals("15sp", android(meta, "lineHeight"));
        assertEquals("1dp", android(meta, "lineSpacingExtra"));
        assertEquals("4", android(meta, "maxLines"));

        Element body = elementByAndroidId(document, "detail_provenance_body");
        assertEquals("13sp", android(body, "textSize"));
        assertEquals("17sp", android(body, "lineHeight"));
        assertEquals("1dp", android(body, "lineSpacingExtra"));
        assertEquals("3", android(body, "maxLines"));
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
