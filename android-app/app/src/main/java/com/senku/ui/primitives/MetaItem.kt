package com.senku.ui.primitives

import androidx.compose.runtime.Immutable

@Immutable
data class MetaItem(
    val label: String,
    val tone: Tone = Tone.Default,
    val showDot: Boolean = false,
)

enum class Tone {
    Default,
    Warn,
    Ok,
    Danger,
    Accent,
}

enum class Orientation {
    Horizontal,
    Vertical,
}
