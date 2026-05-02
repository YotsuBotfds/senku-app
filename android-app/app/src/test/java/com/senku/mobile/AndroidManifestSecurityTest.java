package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class AndroidManifestSecurityTest {
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    @Test
    public void onlyLauncherMainActivityIsExported() throws Exception {
        Document manifest = parse(Paths.get("src/main/AndroidManifest.xml"));
        Element application = firstElement(manifest, "application");

        assertEquals("true", application.getAttributeNS(ANDROID_NS, "allowBackup"));
        assertEquals("@xml/network_security_config", application.getAttributeNS(ANDROID_NS, "networkSecurityConfig"));
        assertEquals("false", application.getAttributeNS(ANDROID_NS, "usesCleartextTraffic"));

        List<Element> exportedActivities = exportedActivities(manifest);
        assertEquals(1, exportedActivities.size());
        Element mainActivity = exportedActivities.get(0);
        assertEquals(".MainActivity", mainActivity.getAttributeNS(ANDROID_NS, "name"));
        assertHasLauncherIntent(mainActivity);

        Element detailActivity = activityNamed(manifest, ".DetailActivity");
        assertNotNull(detailActivity);
        assertEquals("false", detailActivity.getAttributeNS(ANDROID_NS, "exported"));
    }

    @Test
    public void cleartextTrafficIsLimitedToLocalInferenceHosts() throws Exception {
        Document config = parse(Paths.get("src/main/res/xml/network_security_config.xml"));

        Element baseConfig = firstElement(config, "base-config");
        assertEquals("false", baseConfig.getAttribute("cleartextTrafficPermitted"));

        Element domainConfig = firstElement(config, "domain-config");
        assertEquals("true", domainConfig.getAttribute("cleartextTrafficPermitted"));
        assertEquals(
            List.of("10.0.2.2", "127.0.0.1", "localhost"),
            domainTexts(domainConfig)
        );
    }

    private static Document parse(Path path) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(path.toFile());
    }

    private static Element firstElement(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        assertTrue("Missing <" + tagName + "> element", nodes.getLength() > 0);
        return (Element) nodes.item(0);
    }

    private static List<Element> exportedActivities(Document manifest) {
        ArrayList<Element> exported = new ArrayList<>();
        NodeList activities = manifest.getElementsByTagName("activity");
        for (int i = 0; i < activities.getLength(); i++) {
            Element activity = (Element) activities.item(i);
            if ("true".equals(activity.getAttributeNS(ANDROID_NS, "exported"))) {
                exported.add(activity);
            }
        }
        return exported;
    }

    private static Element activityNamed(Document manifest, String name) {
        NodeList activities = manifest.getElementsByTagName("activity");
        for (int i = 0; i < activities.getLength(); i++) {
            Element activity = (Element) activities.item(i);
            if (name.equals(activity.getAttributeNS(ANDROID_NS, "name"))) {
                return activity;
            }
        }
        return null;
    }

    private static void assertHasLauncherIntent(Element activity) {
        NodeList filters = activity.getElementsByTagName("intent-filter");
        assertEquals(1, filters.getLength());
        Element filter = (Element) filters.item(0);
        assertTrue(hasChildWithAndroidName(filter, "action", "android.intent.action.MAIN"));
        assertTrue(hasChildWithAndroidName(filter, "category", "android.intent.category.LAUNCHER"));
    }

    private static boolean hasChildWithAndroidName(Element parent, String tagName, String androidName) {
        NodeList children = parent.getElementsByTagName(tagName);
        for (int i = 0; i < children.getLength(); i++) {
            Element child = (Element) children.item(i);
            if (androidName.equals(child.getAttributeNS(ANDROID_NS, "name"))) {
                return true;
            }
        }
        return false;
    }

    private static List<String> domainTexts(Element domainConfig) {
        ArrayList<String> domains = new ArrayList<>();
        NodeList nodes = domainConfig.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element && "domain".equals(((Element) node).getTagName())) {
                domains.add(node.getTextContent().trim());
            }
        }
        return domains;
    }
}
