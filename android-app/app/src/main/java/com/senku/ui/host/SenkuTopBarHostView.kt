package com.senku.ui.host

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import com.senku.ui.primitives.SenkuTopBar
import com.senku.ui.primitives.TopBarActionKind
import com.senku.ui.primitives.TopBarActionSpec
import com.senku.ui.theme.SenkuAppTheme

fun interface TopBarActionHandler {
    fun onAction(action: TopBarActionKind)
}

internal fun normalizeTopBarHeaderText(text: String): String =
    text
        .replace("\u00C3\u00A2\u00E2\u201A\u00AC\u00C2\u00A2", "\u2022")
        .replace("\u00E2\u20AC\u00A2", "\u2022")
        .replace("\u00C2\u00B7", "\u00B7")

class SenkuTopBarHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbstractComposeView(context, attrs) {
    private var title: String by mutableStateOf("")
    private var subtitle: String? by mutableStateOf(null)
    private var dangerPillLabel: String? by mutableStateOf(null)
    private var showHome: Boolean by mutableStateOf(false)
    private var showPin: Boolean by mutableStateOf(false)
    private var pinActive: Boolean by mutableStateOf(false)
    private var showShare: Boolean by mutableStateOf(false)
    private var showOverflow: Boolean by mutableStateOf(false)
    private var titleMaxLines: Int by mutableStateOf(1)
    private var backDescription: String by mutableStateOf("")
    private var homeDescription: String by mutableStateOf("")
    private var pinDescription: String by mutableStateOf("")
    private var shareDescription: String by mutableStateOf("")
    private var overflowDescription: String by mutableStateOf("")
    private var actionHandler: TopBarActionHandler? = null

    fun setTopBarState(
        title: String,
        subtitle: String?,
        dangerPillLabel: String?,
        showHome: Boolean,
        showPin: Boolean,
        pinActive: Boolean,
        showShare: Boolean,
        showOverflow: Boolean,
        titleMaxLines: Int,
        backDescription: String,
        homeDescription: String,
        pinDescription: String,
        shareDescription: String,
        overflowDescription: String,
        actionHandler: TopBarActionHandler?,
    ) {
        this.title = normalizeTopBarHeaderText(title)
        this.subtitle = subtitle?.takeIf { it.isNotBlank() }?.let(::normalizeTopBarHeaderText)
        this.dangerPillLabel = dangerPillLabel?.takeIf { it.isNotBlank() }?.let(::normalizeTopBarHeaderText)
        this.showHome = showHome
        this.showPin = showPin
        this.pinActive = pinActive
        this.showShare = showShare
        this.showOverflow = showOverflow
        this.titleMaxLines = titleMaxLines.coerceAtLeast(1)
        this.backDescription = backDescription
        this.homeDescription = homeDescription
        this.pinDescription = pinDescription
        this.shareDescription = shareDescription
        this.overflowDescription = overflowDescription
        this.actionHandler = actionHandler
    }

    @Composable
    override fun Content() {
        SenkuAppTheme {
            SenkuTopBar(
                title = title,
                subtitle = subtitle,
                dangerPillLabel = dangerPillLabel,
                actions = buildActions(),
                onActionClick = { action -> actionHandler?.onAction(action) },
                modifier = Modifier,
                titleMaxLines = titleMaxLines,
            )
        }
    }

    private fun buildActions(): List<TopBarActionSpec> {
        val actions = ArrayList<TopBarActionSpec>(4)
        actions += TopBarActionSpec.back(
            contentDescription = backDescription,
        )
        actions += TopBarActionSpec.home(
            contentDescription = homeDescription,
            isVisible = showHome,
        )
        actions += TopBarActionSpec.pin(
            contentDescription = pinDescription,
            isVisible = showPin,
            isActive = pinActive,
        )
        actions += TopBarActionSpec.share(
            contentDescription = shareDescription,
            isVisible = showShare,
        )
        actions += TopBarActionSpec.overflow(
            contentDescription = overflowDescription,
            isVisible = showOverflow,
        )
        return actions
    }

}
