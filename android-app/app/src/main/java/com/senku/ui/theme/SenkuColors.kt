package com.senku.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SenkuColorTokens(
    val bg0: Color = Color(0xFF1A1D16),
    val bg1: Color = Color(0xFF22271D),
    val bg2: Color = Color(0xFF2C3224),
    val bg3: Color = Color(0xFF3A4130),
    val olive10: Color = Color(0xFF4A5139),
    val olive20: Color = Color(0xFF5A6147),
    val olive40: Color = Color(0xFF7A8263),
    val olive60: Color = Color(0xFF9AA084),
    val ink0: Color = Color(0xFFF1EEE2),
    val ink1: Color = Color(0xFFD7D3C2),
    val ink2: Color = Color(0xFFA09C8A),
    val ink3: Color = Color(0xFF9A9789),
    val paper: Color = Color(0xFFE9E1CF),
    val paperInk: Color = Color(0xFF1F2318),
    val paperInkMuted: Color = Color(0xFF646456),
    val accent: Color = Color(0xFFC9B682),
    val danger: Color = Color(0xFFC4704B),
    val warn: Color = Color(0xFFC49A4B),
    val ok: Color = Color(0xFF7A9A5A),
    val paperDanger: Color = Color(0xFF935438),
    val paperWarn: Color = Color(0xFF7A5F2E),
    val paperOk: Color = Color(0xFF546A3E),
    val hairline: Color = Color(0x14F1EEE2),
    val hairlineStrong: Color = Color(0x24F1EEE2),
    val copper: Color = Color(0xFFC48A5A),
    val moss: Color = Color(0xFF9BAB6A),
)

internal val DefaultSenkuColors = SenkuColorTokens()
internal val LocalSenkuColors = staticCompositionLocalOf { DefaultSenkuColors }
