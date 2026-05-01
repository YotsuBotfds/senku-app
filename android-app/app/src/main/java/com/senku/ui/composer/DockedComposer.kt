package com.senku.ui.composer

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme
import java.util.function.Consumer

object DockedComposerTouchTargetTokens {
    const val ADD_ACTION_TOUCH_TARGET_DP = 48
    const val ADD_ACTION_VISUAL_SIZE_DP = 32
    const val ADD_ACTION_PADDING_DP = 8
    const val ADD_ACTION_DISABLED_CONTENT_DESCRIPTION = "Add action unavailable"
    const val ADD_ACTION_ENABLED = false
}

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
    val fieldHeight = 34.dp
    val fieldVerticalPadding = 6.dp
    val rowVerticalPadding = if (landscapePhoneBudgeted) 5.dp else 4.dp
    val actionTouchTargetSize = DockedComposerTouchTargetTokens.ADD_ACTION_TOUCH_TARGET_DP.dp
    val actionVisualSize = DockedComposerTouchTargetTokens.ADD_ACTION_VISUAL_SIZE_DP.dp
    val actionPadding = DockedComposerTouchTargetTokens.ADD_ACTION_PADDING_DP.dp
    val sendVerticalPadding = if (landscapePhoneBudgeted) 7.dp else 6.dp
    val hasSendText = model.enabled && model.text.trim().isNotEmpty()
    val contextHint = model.contextHint.trim()
    val fieldHint = model.hint

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
        if (contextHint.isNotEmpty()) {
            Text(
                text = contextHint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (model.compact) 16.dp else 24.dp,
                        top = 6.dp,
                        end = if (model.compact) 16.dp else 24.dp,
                    ),
                style = typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
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
                .padding(horizontal = if (model.compact) 16.dp else 24.dp, vertical = rowVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (model.showRetry && onRetryClick != null) {
                Surface(
                    color = colors.bg2,
                    contentColor = colors.ink1,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, colors.hairline),
                    onClick = { if (model.enabled) onRetryClick() },
                ) {
                    Text(
                        text = model.retryLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        style = typography.tag.copy(
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = colors.ink1,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(actionTouchTargetSize)
                    .semantics {
                        contentDescription =
                            DockedComposerTouchTargetTokens.ADD_ACTION_DISABLED_CONTENT_DESCRIPTION
                        disabled()
                    }
                    .clip(CircleShape)
                    .clickable(
                        enabled = DockedComposerTouchTargetTokens.ADD_ACTION_ENABLED,
                        role = Role.Button,
                        onClick = { },
                    )
                    .padding(actionPadding),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(actionVisualSize),
                    color = colors.bg0,
                    contentColor = colors.ink2,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, colors.hairline),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "+",
                            style = typography.tag.copy(
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = colors.ink2,
                        )
                    }
                }
            }

            BasicTextField(
                value = model.text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .height(fieldHeight)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.bg2.copy(alpha = 0.72f))
                    .focusRequester(focusRequester)
                    .padding(horizontal = 11.dp, vertical = fieldVerticalPadding)
                    .onFocusChanged {
                        onFocusChange?.invoke(it.isFocused)
                    },
                enabled = model.enabled,
                singleLine = model.compact,
                textStyle = typography.uiBody.copy(
                    fontSize = 13.sp,
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
                                text = fieldHint,
                                style = typography.uiBody.copy(
                                    fontSize = 13.sp,
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
                color = colors.bg0,
                contentColor = if (hasSendText) colors.ink0 else colors.ink2,
                shape = RoundedCornerShape(9.dp),
                border = BorderStroke(
                    1.dp,
                    if (hasSendText) colors.accent.copy(alpha = 0.72f) else colors.hairline,
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
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = sendVerticalPadding),
                        style = typography.tag.copy(
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = if (hasSendText) colors.ink0 else colors.ink2,
                    )
                }
            }
        }
    }
}
