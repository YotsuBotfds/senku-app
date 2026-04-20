package com.senku.ui.suggest

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.AbstractComposeView
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme
import java.util.function.Consumer

data class SuggestChipModel(
    val label: String,
    val query: String,
    val contentDescription: String = label,
)

class SuggestChipRailHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbstractComposeView(context, attrs) {
    private var suggestions: List<SuggestChipModel> by mutableStateOf(emptyList())
    private var onSuggestionSelected: Consumer<String>? by mutableStateOf(null)

    fun updateSuggestions(
        value: List<SuggestChipModel>,
        onSuggestionSelected: Consumer<String>? = null,
    ) {
        suggestions = value.toList()
        this.onSuggestionSelected = onSuggestionSelected
    }

    @Composable
    override fun Content() {
        SenkuAppTheme {
            SuggestChipRail(
                suggestions = suggestions,
                onSuggestionSelected = { onSuggestionSelected?.accept(it) },
            )
        }
    }
}

@Composable
fun SuggestChipRail(
    suggestions: List<SuggestChipModel>,
    modifier: Modifier = Modifier,
    onSuggestionSelected: (String) -> Unit,
) {
    if (suggestions.isEmpty()) {
        return
    }
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        suggestions.forEach { suggestion ->
            SuggestChip(
                model = suggestion,
                onSuggestionSelected = onSuggestionSelected,
            )
        }
    }
}

@Composable
private fun SuggestChip(
    model: SuggestChipModel,
    onSuggestionSelected: (String) -> Unit,
) {
    val colors = SenkuTheme.colors
    val typography = SenkuTheme.typography
    Surface(
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = model.contentDescription
        },
        color = colors.bg1,
        contentColor = colors.ink0,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, colors.hairlineStrong),
        onClick = { onSuggestionSelected(model.query) },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = model.label,
                style = typography.tag.copy(
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.ink1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "›",
                style = typography.tag.copy(
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.accent,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 390)
@Composable
private fun SuggestChipRailPreview() {
    SenkuAppTheme {
        SuggestChipRail(
            suggestions = listOf(
                SuggestChipModel(
                    label = "Boiling water",
                    query = "What should I know next about boiling water?",
                    contentDescription = "Try next 1 of 2: Boiling water. Tap to send as follow-up.",
                ),
                SuggestChipModel(
                    label = "Signal fire",
                    query = "What should I know next about signal fire?",
                    contentDescription = "Try next 2 of 2: Signal fire. Tap to send as follow-up.",
                ),
            ),
            onSuggestionSelected = {},
        )
    }
}
