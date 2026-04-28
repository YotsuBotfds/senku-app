package com.senku.ui.home

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senku.mobile.R
import com.senku.ui.theme.SenkuAppTheme
import com.senku.ui.theme.SenkuTheme
import java.util.Locale

enum class CategoryShelfLayoutMode {
    PHONE_GRID,
    TABLET_GRID,
    TABLET_RAIL,
}

@Immutable
data class CategoryShelfItemModel(
    val bucketKey: String,
    val label: String,
    val countLabel: String,
    val accentColor: Int,
    val enabled: Boolean,
    val contentDescription: String,
)

fun interface CategoryShelfSelectionHandler {
    fun onCategorySelected(item: CategoryShelfItemModel)
}

class CategoryShelfHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbstractComposeView(context, attrs) {
    private var items: List<CategoryShelfItemModel> by mutableStateOf(emptyList())
    private var layoutMode: CategoryShelfLayoutMode by mutableStateOf(CategoryShelfLayoutMode.PHONE_GRID)
    private var selectionEnabledState: Boolean by mutableStateOf(true)
    private var selectionHandler: CategoryShelfSelectionHandler? = null

    fun setShelf(
        value: List<CategoryShelfItemModel>,
        layoutMode: CategoryShelfLayoutMode,
        selectionHandler: CategoryShelfSelectionHandler?,
    ) {
        items = value.toList()
        this.layoutMode = layoutMode
        this.selectionHandler = selectionHandler
    }

    fun setSelectionEnabled(enabled: Boolean) {
        selectionEnabledState = enabled
    }

    @Composable
    override fun Content() {
        SenkuAppTheme {
            CategoryShelf(
                items = items,
                layoutMode = layoutMode,
                selectionEnabled = selectionEnabledState,
                onCategorySelected = { item -> selectionHandler?.onCategorySelected(item) },
            )
        }
    }
}

@Composable
fun CategoryShelf(
    items: List<CategoryShelfItemModel>,
    layoutMode: CategoryShelfLayoutMode,
    selectionEnabled: Boolean,
    onCategorySelected: (CategoryShelfItemModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val verticalGap = dimensionResource(R.dimen.senku_rev03_space_8)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalGap),
    ) {
        when (layoutMode) {
            CategoryShelfLayoutMode.PHONE_GRID -> {
                CategoryGrid(
                    items = items,
                    columns = 3,
                    cardHeight = 50.dp,
                    selectionEnabled = selectionEnabled,
                    onCategorySelected = onCategorySelected,
                )
            }

            CategoryShelfLayoutMode.TABLET_GRID -> {
                CategoryGrid(
                    items = items,
                    columns = 3,
                    cardHeight = 48.dp,
                    selectionEnabled = selectionEnabled,
                    onCategorySelected = onCategorySelected,
                )
            }

            CategoryShelfLayoutMode.TABLET_RAIL -> {
                items.forEach { item ->
                    TabletCategoryRow(
                        item = item,
                        selectionEnabled = selectionEnabled,
                        onClick = { onCategorySelected(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    items: List<CategoryShelfItemModel>,
    columns: Int,
    cardHeight: Dp,
    selectionEnabled: Boolean,
    onCategorySelected: (CategoryShelfItemModel) -> Unit,
) {
    val gap = dimensionResource(R.dimen.senku_rev03_space_8)
    items.chunked(columns).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            rowItems.forEach { item ->
                PhoneCategoryCard(
                    item = item,
                    selectionEnabled = selectionEnabled,
                    cardHeight = cardHeight,
                    modifier = Modifier.weight(1f),
                    onClick = { onCategorySelected(item) },
                )
            }
            repeat(columns - rowItems.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PhoneCategoryCard(
    item: CategoryShelfItemModel,
    selectionEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 74.dp,
) {
    val colors = SenkuTheme.colors
    val corner = 3.dp
    val accentWidth = 2.dp
    val horizontalPadding = dimensionResource(R.dimen.senku_rev03_space_8)
    val verticalPadding = dimensionResource(R.dimen.senku_rev03_space_6)
    val enabled = selectionEnabled && item.enabled
    val accent = Color(item.accentColor)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                if (item.contentDescription.isNotBlank()) {
                    contentDescription = item.contentDescription
                }
            },
        color = colors.bg0,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(corner),
        enabled = enabled,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight),
        ) {
            Box(
                modifier = Modifier
                    .width(accentWidth)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = item.label,
                    style = SenkuTheme.typography.uiBody.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.ink0,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.countLabel.uppercase(Locale.US),
                    style = SenkuTheme.typography.monoCaps.copy(
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.ink2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.hairline),
                )
            }
        }
    }
}

@Composable
private fun TabletCategoryRow(
    item: CategoryShelfItemModel,
    selectionEnabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = SenkuTheme.colors
    val enabled = selectionEnabled && item.enabled
    val accent = Color(item.accentColor)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                if (item.contentDescription.isNotBlank()) {
                    contentDescription = item.contentDescription
                }
            },
        color = colors.bg0,
        contentColor = colors.ink0,
        shape = RoundedCornerShape(2.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .padding(
                    horizontal = dimensionResource(R.dimen.senku_rev03_space_8),
                    vertical = dimensionResource(R.dimen.senku_rev03_space_6),
                ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.senku_rev03_space_8)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.senku_rev03_space_6))
                    .clip(CircleShape)
                    .background(accent),
            )
            Text(
                text = item.label,
                modifier = Modifier.weight(1f),
                style = SenkuTheme.typography.uiBody.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.ink0,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.countLabel.uppercase(Locale.US),
                style = SenkuTheme.typography.monoCaps.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.ink2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 390)
@Composable
private fun CategoryShelfPhonePreview() {
    SenkuAppTheme {
        Surface(color = SenkuTheme.colors.bg0) {
            CategoryShelf(
                items = previewItems(),
                layoutMode = CategoryShelfLayoutMode.PHONE_GRID,
                selectionEnabled = true,
                onCategorySelected = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1D16, widthDp = 280)
@Composable
private fun CategoryShelfTabletPreview() {
    SenkuAppTheme {
        Surface(color = SenkuTheme.colors.bg0) {
            CategoryShelf(
                items = previewItems().take(6),
                layoutMode = CategoryShelfLayoutMode.TABLET_RAIL,
                selectionEnabled = true,
                onCategorySelected = {},
                modifier = Modifier
                    .width(240.dp)
                    .padding(16.dp),
            )
        }
    }
}

private fun previewItems(): List<CategoryShelfItemModel> {
    return listOf(
        CategoryShelfItemModel("water", "Water & sanitation", "142 guides", 0xFF7A9AB4.toInt(), true, ""),
        CategoryShelfItemModel("shelter", "Shelter & build", "96 guides", 0xFF7A9A5A.toInt(), true, ""),
        CategoryShelfItemModel("fire", "Fire & energy", "88 guides", 0xFFC48A5A.toInt(), true, ""),
        CategoryShelfItemModel("medicine", "Medicine", "73 guides", 0xFFB67A7A.toInt(), true, ""),
        CategoryShelfItemModel("food", "Food & agriculture", "61 guides", 0xFF9AA064.toInt(), true, ""),
        CategoryShelfItemModel("communications", "Communications", "24 guides", 0xFF7A9A9A.toInt(), true, ""),
        CategoryShelfItemModel("tools", "Tools & craft", "49 guides", 0xFFC9B682.toInt(), true, ""),
        CategoryShelfItemModel("community", "Community", "38 guides", 0xFF9AA084.toInt(), true, ""),
    )
}
