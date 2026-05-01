package com.senku.ui

import com.senku.ui.tablet.tabletDetailBackActionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

internal data class MainChromeSpec(
    val qualifier: String,
    val height: String,
    val horizontalPadding: String,
    val expectedModeFontFamily: String? = null,
    val verticalPadding: String? = null,
    val backActionSize: String? = null,
    val backIconSize: String? = null,
    val searchActionSize: String = "32dp",
    val searchPadding: String = "8dp",
    val searchTint: String = "@color/senku_rev03_ink_2",
    val expectsOverflowPlaceholder: Boolean = true,
)

internal data class SearchChromeSpec(
    val qualifier: String,
    val queryMarginStart: String,
    val expectedIconTint: String? = null,
    val expectedCountColor: String? = null,
)

internal data class RailShellSpec(
    val railWidth: String,
    val firstItemTopMargin: String,
    val itemTopMargin: String,
    val labelTopMargin: String,
    val itemMinTouchHeight: String = "48dp",
)

internal data class RailItemSpec(
    val itemId: String,
    val labelId: String,
    val iconId: String,
    val expectedText: String,
    val expectedIcon: String,
    val expectedTint: String,
)

internal fun mainLayout(qualifier: String): Element =
    DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()
        .parse(mainLayoutFile(qualifier))
        .documentElement

internal fun mainLayoutText(qualifier: String): String =
    mainLayoutFile(qualifier).readText()

internal fun assertSharedMainChrome(layout: Element, spec: MainChromeSpec) {
    val row = layout.elementByAndroidId("home_chrome_row")
    val back = layout.elementByAndroidId("home_chrome_back_button")
    val backIcon = layout.elementByAndroidId("home_chrome_back_icon")
    val backLabel = layout.elementByAndroidId("home_chrome_back_label")
    val divider = layout.elementByAndroidId("home_chrome_divider")
    val mode = layout.elementByAndroidId("home_chrome_mode")
    val title = layout.elementByAndroidId("home_chrome_title")
    val searchIcon = layout.elementByAndroidId("home_chrome_search_icon")
    val overflowPlaceholders = row.directElementChildren("TextView").filter { it.android("text") == "..." }
    val bottomRule = layout.elementByAndroidId("home_chrome_bottom_rule")
    val verticalPadding = spec.verticalPadding ?: if (spec.height == "48dp") "9dp" else "12dp"
    val backPolicy = tabletDetailBackActionPolicy()
    val backActionSize = spec.backActionSize ?: "${backPolicy.widthDp}dp"
    val backIconSize = spec.backIconSize ?: "${backPolicy.iconSizeDp}dp"

    assertEquals("${spec.qualifier} topbar height", spec.height, row.android("layout_height"))
    assertEquals("${spec.qualifier} topbar start padding", spec.horizontalPadding, row.android("paddingStart"))
    assertEquals("${spec.qualifier} topbar end padding", spec.horizontalPadding, row.android("paddingEnd"))
    assertEquals("${spec.qualifier} topbar top padding", verticalPadding, row.android("paddingTop"))
    assertEquals("${spec.qualifier} topbar bottom padding", verticalPadding, row.android("paddingBottom"))
    assertDirectChildIdsContain(
        row = row,
        message = "${spec.qualifier} topbar semantic children",
        expectedIds = listOf(
            "home_chrome_back_button",
            "home_chrome_divider",
            "home_chrome_mode",
            "home_chrome_title",
            "home_chrome_search_icon",
        ),
    )

    assertEquals(backActionSize, back.android("layout_width"))
    assertEquals(spec.backActionSize ?: "${backPolicy.heightDp}dp", back.android("layout_height"))
    assertEquals("@string/detail_back_content_description", back.android("contentDescription"))
    assertEquals("horizontal", back.android("orientation"))
    assertEquals("center", back.android("gravity"))
    assertEquals("0dp", back.android("paddingStart"))
    assertEquals("0dp", back.android("paddingEnd"))
    assertEquals(backIconSize, backIcon.android("layout_width"))
    assertEquals(backIconSize, backIcon.android("layout_height"))
    assertEquals("@drawable/ic_home_back_chevron", backIcon.android("src"))
    assertEquals("@color/senku_rev03_ink_0", backIcon.android("tint"))
    assertEquals(backPolicy.showsTextLabel, backLabel.android("visibility") != "gone")
    assertEquals("0dp", backLabel.android("layout_marginStart"))
    assertEquals("@font/jetbrains_mono", backLabel.android("fontFamily"))
    assertEquals("10sp", backLabel.android("textSize"))
    assertEquals("500", backLabel.android("textFontWeight"))
    assertEquals("12sp", backLabel.android("lineHeight"))
    assertEquals("0.09", backLabel.android("letterSpacing"))

    assertEquals("1dp", divider.android("layout_width"))
    assertEquals("24dp", divider.android("layout_height"))
    assertEquals("6dp", divider.android("layout_marginStart"))
    assertEquals("10dp", divider.android("layout_marginEnd"))

    assertEquals("wrap_content", mode.android("layout_width"))
    assertEquals("@style/TextAppearance.Senku.Rev03.MonoCaps", mode.android("textAppearance"))
    spec.expectedModeFontFamily?.let { expected ->
        assertEquals(expected, mode.android("fontFamily"))
    }
    assertEquals("HOME SENKU", mode.android("text"))
    assertEquals("@color/senku_rev03_accent", mode.android("textColor"))
    assertEquals("500", mode.android("textFontWeight"))
    assertEquals("", mode.android("textStyle"))

    assertEquals("0dp", title.android("layout_width"))
    assertEquals("10dp", title.android("layout_marginStart"))
    assertEquals("1", title.android("layout_weight"))
    assertEquals("1", title.android("maxLines"))
    assertEquals("end", title.android("ellipsize"))
    assertEquals("@font/inter_tight", title.android("fontFamily"))
    assertEquals("14sp", title.android("textSize"))
    assertEquals("600", title.android("textFontWeight"))
    assertEquals("18sp", title.android("lineHeight"))
    assertEquals("@color/senku_rev03_ink_0", title.android("textColor"))
    assertEquals("Field manual \u2022 ed.2", title.android("text"))

    assertEquals(spec.searchActionSize, searchIcon.android("layout_width"))
    assertEquals(spec.searchActionSize, searchIcon.android("layout_height"))
    assertEquals(spec.searchPadding, searchIcon.android("padding"))
    assertEquals("@drawable/ic_search_magnifier", searchIcon.android("src"))
    assertEquals(spec.searchTint, searchIcon.android("tint"))
    assertEquals("@string/home_chrome_search_content_description", searchIcon.android("contentDescription"))
    assertEquals("true", searchIcon.android("clickable"))
    assertEquals("true", searchIcon.android("focusable"))
    assertEquals(spec.expectsOverflowPlaceholder, overflowPlaceholders.isNotEmpty())
    assertEquals("1dp", bottomRule.android("layout_height"))
    assertEquals("@color/senku_rev03_hairline_strong", bottomRule.android("background"))
}

internal fun assertSearchChrome(layout: Element, spec: SearchChromeSpec) {
    val row = layout.elementByAndroidId("tablet_search_topbar_row")
    val query = layout.elementByAndroidId("tablet_search_query_text")
    val count = layout.elementByAndroidId("tablet_search_count_text")
    val searchIcon = row.directElementChildren("ImageView")
        .single { it.android("src") == "@drawable/ic_search_magnifier" }
    val bottomRule = layout.elementByAndroidId("tablet_search_bottom_rule")

    assertEquals("${spec.qualifier} search topbar height", "wrap_content", row.android("layout_height"))
    assertEquals("${spec.qualifier} search topbar hidden by default", "gone", row.android("visibility"))
    assertDirectChildIdsContain(
        row = row,
        message = "${spec.qualifier} search topbar semantic text children",
        expectedIds = listOf("tablet_search_query_text", "tablet_search_count_text"),
    )
    assertEquals("20dp", searchIcon.android("layout_width"))
    assertEquals("20dp", searchIcon.android("layout_height"))
    assertEquals("@drawable/ic_search_magnifier", searchIcon.android("src"))
    spec.expectedIconTint?.let { expected ->
        assertEquals(expected, searchIcon.android("tint"))
    }
    assertEquals("0dp", query.android("layout_width"))
    assertEquals(spec.queryMarginStart, query.android("layout_marginStart"))
    assertEquals("1", query.android("layout_weight"))
    assertEquals("1", query.android("maxLines"))
    assertEquals("end", query.android("ellipsize"))
    assertEquals("@font/inter_tight", query.android("fontFamily"))
    assertEquals("14sp", query.android("textSize"))
    assertEquals("600", query.android("textFontWeight"))
    assertEquals("", query.android("textStyle"))
    assertEquals("18sp", query.android("lineHeight"))
    assertEquals("@font/jetbrains_mono", count.android("fontFamily"))
    assertEquals("12sp", count.android("textSize"))
    assertEquals("16sp", count.android("lineHeight"))
    spec.expectedCountColor?.let { expected ->
        assertEquals(expected, count.android("textColor"))
    }
    assertEquals("1dp", bottomRule.android("layout_height"))
    assertEquals("gone", bottomRule.android("visibility"))
    assertEquals("@color/senku_rev03_hairline_strong", bottomRule.android("background"))
}

internal fun assertRailShell(layout: Element, spec: RailShellSpec) {
    val home = layout.elementByAndroidId("phone_nav_home")
    val ask = layout.elementByAndroidId("phone_nav_ask")
    val pins = layout.elementByAndroidId("phone_nav_pins")
    val rail = home.parentNode as Element
    val badge = rail.firstElementChild("TextView")

    assertEquals(spec.railWidth, rail.android("layout_width"))
    assertEquals("36dp", badge.android("layout_width"))
    assertEquals("36dp", badge.android("layout_height"))
    assertEquals("@font/jetbrains_mono", badge.android("fontFamily"))
    assertEquals("14sp", badge.android("textSize"))
    assertEquals("18sp", badge.android("lineHeight"))
    assertEquals("0.09", badge.android("letterSpacing"))
    assertEquals("700", badge.android("textFontWeight"))
    assertEquals(spec.firstItemTopMargin, home.android("layout_marginTop"))
    assertEquals(spec.itemTopMargin, ask.android("layout_marginTop"))
    assertEquals(spec.itemTopMargin, pins.android("layout_marginTop"))
    listOf(home, ask, pins).forEach { item ->
        assertEquals(spec.itemMinTouchHeight, item.android("minHeight"))
    }

    listOf("phone_nav_home_icon", "phone_nav_ask_icon", "phone_nav_pins_icon").forEach { id ->
        val icon = layout.elementByAndroidId(id)
        assertEquals("@dimen/senku_rev03_nav_rail_icon", icon.android("layout_width"))
        assertEquals("@dimen/senku_rev03_nav_rail_icon", icon.android("layout_height"))
    }

    listOf("phone_nav_home_label", "phone_nav_ask_label", "phone_nav_pins_label").forEach { id ->
        val label = layout.elementByAndroidId(id)
        assertEquals(spec.labelTopMargin, label.android("layout_marginTop"))
        assertEquals("@font/inter_tight", label.android("fontFamily"))
    }
}

internal fun assertRailItemTokens(layout: Element, spec: RailItemSpec) {
    val item = layout.elementByAndroidId(spec.itemId)
    val label = layout.elementByAndroidId(spec.labelId)
    val icon = layout.elementByAndroidId(spec.iconId)

    assertEquals(spec.expectedText, item.android("contentDescription"))
    assertEquals(spec.expectedText, label.android("text"))
    assertEquals(spec.expectedText, icon.android("contentDescription"))
    assertEquals(spec.expectedIcon, icon.android("src"))
    assertEquals(spec.expectedTint, icon.android("tint"))
    assertEquals(spec.expectedTint, label.android("textColor"))
}

internal fun assertLayoutDoesNotReferenceResources(
    layout: Element,
    forbiddenResources: List<String>,
    message: String,
) {
    val resourceReferences = layout.allElements()
        .flatMap { element ->
            (0 until element.attributes.length)
                .map { index -> element.attributes.item(index).nodeValue }
        }
        .filter { value -> value.startsWith("@") }
        .toSet()

    forbiddenResources.forEach { resource ->
        assertTrue("$message should not reference $resource", resource !in resourceReferences)
    }
}

internal fun Element.elementByAndroidId(id: String): Element {
    val nodes = getElementsByTagName("*")
    for (index in 0 until nodes.length) {
        val element = nodes.item(index) as? Element ?: continue
        if (element.android("id") == "@+id/$id" || element.android("id") == "@id/$id") {
            return element
        }
    }
    error("Unable to locate @$id")
}

internal fun Element.elementByText(text: String): Element {
    val nodes = getElementsByTagName("*")
    for (index in 0 until nodes.length) {
        val element = nodes.item(index) as? Element ?: continue
        if (element.android("text") == text) {
            return element
        }
    }
    error("Unable to locate element with text $text")
}

internal fun Element.firstElementChild(tagName: String): Element {
    for (index in 0 until childNodes.length) {
        val node = childNodes.item(index)
        if (node is Element && node.tagName == tagName) {
            return node
        }
    }
    error("Unable to locate child <$tagName>")
}

internal fun Element.directElementChildren(tagName: String): List<Element> =
    directElementChildren().filter { it.tagName == tagName }

internal fun Element.directElementChildren(): List<Element> =
    (0 until childNodes.length)
        .map { childNodes.item(it) }
        .filterIsInstance<Element>()

private fun Element.allElements(): List<Element> =
    listOf(this) + (0 until getElementsByTagName("*").length)
        .mapNotNull { index -> getElementsByTagName("*").item(index) as? Element }

private fun assertDirectChildIdsContain(row: Element, message: String, expectedIds: List<String>) {
    val directChildIds = row.directElementChildren()
        .map { it.requiredAndroidId() }
        .filter { it.isNotBlank() }

    expectedIds.forEach { expectedId ->
        assertTrue("$message should include @$expectedId", directChildIds.contains(expectedId))
    }
}

internal fun Element.android(name: String): String =
    getAttributeNS("http://schemas.android.com/apk/res/android", name)
        .ifBlank { getAttribute("android:$name") }

internal fun Element.requiredAndroidId(): String =
    android("id").removePrefix("@+id/").removePrefix("@id/")

private fun mainLayoutFile(qualifier: String): File {
    val folder = if (qualifier.isBlank()) "layout" else qualifier
    return locateFile(
        "android-app/app/src/main/res/$folder/activity_main.xml",
        "app/src/main/res/$folder/activity_main.xml",
        "src/main/res/$folder/activity_main.xml",
    )
}

private fun locateFile(vararg candidates: String): File {
    val userDir = requireNotNull(System.getProperty("user.dir")) { "user.dir is not set" }
    val start = File(userDir).absoluteFile
    val roots = generateSequence(start) { root -> root.parentFile }
    for (root in roots) {
        for (candidate in candidates) {
            val file = File(root, candidate)
            if (file.exists()) {
                return file
            }
        }
    }
    error("Unable to locate XML layout from ${start.path}")
}
