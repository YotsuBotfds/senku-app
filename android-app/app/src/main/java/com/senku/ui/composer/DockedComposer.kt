package com.senku.ui.composer

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme
import java.util.function.Consumer

data class DockedComposerModel @JvmOverloads constructor(
    val text: String,
    val hint: String,
    val enabled: Boolean,
    val showRetry: Boolean,
    val retryLabel: String,
    val compact: Boolean,
    val contextHint: String = "",
)

class DockedComposerHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbstractComposeView(context, attrs) {
    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    private var model: DockedComposerModel by mutableStateOf(
        DockedComposerModel(
            text = "",
            hint = "",
            enabled = true,
            showRetry = false,
            retryLabel = "Retry",
            compact = true,
        )
    )
    private var onTextChange: Consumer<String>? by mutableStateOf(null)
    private var onSendClick: Runnable? by mutableStateOf(null)
    private var onRetryClick: Runnable? by mutableStateOf(null)
    private var onFocusChange: Consumer<Boolean>? by mutableStateOf(null)
    private var focusRequestTick by mutableStateOf(0)
    private var composerFocused by mutableStateOf(false)
    private var phoneLandscapeBudgetActive by mutableStateOf(false)

    fun updateModel(
        value: DockedComposerModel,
        onTextChange: Consumer<String>? = null,
        onSendClick: Runnable? = null,
        onRetryClick: Runnable? = null,
        onFocusChange: Consumer<Boolean>? = null,
    ) {
        model = value
        this.onTextChange = onTextChange
        this.onSendClick = onSendClick
        this.onRetryClick = onRetryClick
        this.onFocusChange = onFocusChange
    }

    fun requestComposerFocus() {
        requestFocus()
        post { focusRequestTick += 1 }
    }

    fun isComposerFocused(): Boolean = composerFocused

    fun getFocusRequestCount(): Int = focusRequestTick

    fun setLandscapePhoneBudgeted(value: Boolean) {
        phoneLandscapeBudgetActive = value
    }

    private fun handleComposerFocusChanged(focused: Boolean) {
        composerFocused = focused
        onFocusChange?.accept(focused)
    }

    @Composable
    override fun Content() {
        SenkuAppTheme {
            DockedComposer(
                model = model,
                focusRequestTick = focusRequestTick,
                onTextChange = { onTextChange?.accept(it) },
                onSendClick = { onSendClick?.run() },
                onRetryClick = if (model.showRetry) ({ onRetryClick?.run() }) else null,
                onFocusChange = ::handleComposerFocusChanged,
                landscapePhoneBudgeted = phoneLandscapeBudgetActive,
            )
        }
    }
}

@Composable
fun DockedComposer(
    model: DockedComposerModel,
    modifier: Modifier = Modifier,
    focusRequestTick: Int = 0,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onRetryClick: (() -> Unit)? = null,
    onFocusChange: ((Boolean) -> Unit)? = null,
    landscapePhoneBudgeted: Boolean = false,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    val focusRequester = remember { FocusRequester() }
    val fieldHeight = if (landscapePhoneBudgeted) 36.dp else 40.dp
    val fieldVerticalPadding = if (landscapePhoneBudgeted) 7.dp else 9.dp
    val rowVerticalPadding = if (landscapePhoneBudgeted) 6.dp else 9.dp
    val actionSize = if (landscapePhoneBudgeted) 34.dp else 36.dp
    val sendVerticalPadding = if (landscapePhoneBudgeted) 8.dp else 9.dp
    val hasSendText = model.enabled && model.text.trim().isNotEmpty()
    val contextHint = model.contextHint.trim()

    LaunchedEffect(focusRequestTick, model.enabled) {
        if (focusRequestTick > 0 && model.enabled) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg0),
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = colors.hairlineStrong,
        )
        if (contextHint.isNotEmpty()) {
            Text(
                text = contextHint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (model.compact) 18.dp else 24.dp,
                        top = 8.dp,
                        end = if (model.compact) 18.dp else 24.dp,
                    ),
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.ink3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (model.compact) 18.dp else 24.dp, vertical = rowVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (model.showRetry && onRetryClick != null) {
                Surface(
                    color = colors.bg2,
                    contentColor = colors.ink1,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.hairlineStrong),
                    onClick = { if (model.enabled) onRetryClick() },
                ) {
                    Text(
                        text = model.retryLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = typography.tag.copy(
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = colors.ink1,
                    )
                }
            }

            Surface(
                modifier = Modifier.size(actionSize),
                color = colors.bg1,
                contentColor = colors.ink2,
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.hairlineStrong),
                onClick = { },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+",
                        style = typography.tag.copy(
                            fontSize = 20.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = colors.ink2,
                    )
                }
            }

            BasicTextField(
                value = model.text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .height(fieldHeight)
                    .focusRequester(focusRequester)
                    .padding(horizontal = 12.dp, vertical = fieldVerticalPadding)
                    .onFocusChanged {
                        onFocusChange?.invoke(it.isFocused)
                    },
                enabled = model.enabled,
                singleLine = model.compact,
                textStyle = typography.uiBody.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.ink0,
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (hasSendText) {
                            onSendClick()
                        }
                    }
                ),
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (model.text.isBlank()) {
                            Text(
                                text = model.hint,
                                style = typography.uiBody.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = colors.ink3,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            Surface(
                color = colors.bg1,
                contentColor = if (hasSendText) colors.ink0 else colors.ink2,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (hasSendText) colors.accent.copy(alpha = 0.72f) else colors.hairlineStrong,
                ),
                onClick = {
                    if (hasSendText) {
                        onSendClick()
                    }
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Send",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = sendVerticalPadding),
                        style = typography.tag.copy(
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = if (hasSendText) colors.ink0 else colors.ink2,
                    )
                }
            }
        }
    }
}
