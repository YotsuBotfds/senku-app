package com.senku.ui.host

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import com.senku.ui.primitives.MetaItem
import com.senku.ui.primitives.MetaStrip
import com.senku.ui.primitives.Orientation
import com.senku.ui.theme.SenkuAppTheme

class SenkuMetaStripHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AbstractComposeView(context, attrs) {
    private var items: List<MetaItem> by mutableStateOf(emptyList())
    private var orientation: Orientation by mutableStateOf(Orientation.Horizontal)

    @JvmOverloads
    fun updateItems(value: List<MetaItem>, orientation: Orientation = Orientation.Horizontal) {
        items = value.toList()
        this.orientation = orientation
    }

    @Composable
    override fun Content() {
        SenkuAppTheme {
            MetaStrip(
                items = items,
                orientation = orientation,
            )
        }
    }
}
