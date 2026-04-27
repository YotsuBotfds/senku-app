package com.senku.ui.composer

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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

data class DockedComposerModel(
    val text: String,
    val hint: String,
    val enabled: Boolean,
    val showRetry: Boolean,
    val retryLabel: String,
    val compact: Boolean,
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
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequestTick, model.enabled) {
        if (focusRequestTick > 0 && model.enabled) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg1),
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = colors.hairlineStrong,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (model.compact) 12.dp else 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (model.showRetry && onRetryClick != null) {
                Surface(
                    color = colors.bg2,
                    contentColor = colors.ink1,
                    shape = RoundedCornerShape(999.dp),
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

            BasicTextField(
                value = model.text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .focusRequester(focusRequester)
                    .border(
                        width = 1.dp,
                        color = if (focused) colors.accent.copy(alpha = 0.55f) else colors.hairlineStrong,
                        shape = RoundedCornerShape(999.dp),
                    )
                    .background(colors.bg2, RoundedCornerShape(999.dp))
                    .padding(horizontal = 16.dp, vertical = if (model.compact) 12.dp else 14.dp)
                    .onFocusChanged {
                        focused = it.isFocused
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
                        if (model.enabled && model.text.trim().isNotEmpty()) {
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
                modifier = Modifier.size(40.dp),
                color = if (model.enabled && model.text.trim().isNotEmpty()) colors.accent else colors.olive20,
                contentColor = colors.paperInk,
                shape = CircleShape,
                onClick = {
                    if (model.enabled && model.text.trim().isNotEmpty()) {
                        onSendClick()
                    }
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = ">",
                        style = typography.tag.copy(
                            fontSize = 18.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.paperInk,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}
