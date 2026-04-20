package com.senku.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.senku.mobile.R

private val InterTight = FontFamily(
    Font(R.font.inter_tight_variable, FontWeight.Normal),
    Font(R.font.inter_tight_variable, FontWeight.Medium),
    Font(R.font.inter_tight_variable, FontWeight.SemiBold),
    Font(R.font.inter_tight_variable, FontWeight.Bold),
)

private val SourceSerif4 = FontFamily(
    Font(R.font.source_serif_4_variable, FontWeight.Normal),
    Font(R.font.source_serif_4_variable, FontWeight.Medium),
    Font(R.font.source_serif_4_variable, FontWeight.SemiBold),
)

private val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_variable, FontWeight.Normal),
    Font(R.font.jetbrains_mono_variable, FontWeight.Medium),
)

@Immutable
data class SenkuTypographyTokens(
    val canvasTitle: TextStyle,
    val sectionTitle: TextStyle,
    val answerBody: TextStyle,
    val uiBody: TextStyle,
    val smallBody: TextStyle,
    val microBody: TextStyle,
    val monoCaps: TextStyle,
    val tag: TextStyle,
)

internal val DefaultSenkuTypography = SenkuTypographyTokens(
    canvasTitle = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 39.6.sp,
        letterSpacing = (-0.02).em,
    ),
    sectionTitle = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 26.4.sp,
        letterSpacing = (-0.01).em,
    ),
    answerBody = TextStyle(
        fontFamily = SourceSerif4,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.65.sp,
        letterSpacing = (-0.005).em,
    ),
    uiBody = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.005).em,
    ),
    smallBody = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 19.5.sp,
        letterSpacing = (-0.005).em,
    ),
    microBody = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.8.sp,
    ),
    monoCaps = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.4.sp,
        letterSpacing = 0.09.em,
    ),
    tag = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.4.sp,
    ),
)

internal val LocalSenkuTypography = staticCompositionLocalOf { DefaultSenkuTypography }
