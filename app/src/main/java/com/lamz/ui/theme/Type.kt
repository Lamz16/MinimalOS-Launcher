package com.lamz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun createMinimalTypography(
    fontScale: Float = 1.0f,
    fontFamily: FontFamily = FontFamily.SansSerif
): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = (56 * fontScale).sp,
            lineHeight = (64 * fontScale).sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (44 * fontScale).sp,
            lineHeight = (52 * fontScale).sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (32 * fontScale).sp,
            lineHeight = (40 * fontScale).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (26 * fontScale).sp,
            lineHeight = (32 * fontScale).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * fontScale).sp,
            lineHeight = (26 * fontScale).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * fontScale).sp,
            lineHeight = (22 * fontScale).sp,
            letterSpacing = 0.15.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * fontScale).sp,
            lineHeight = (24 * fontScale).sp,
            letterSpacing = 0.25.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp,
            letterSpacing = 0.25.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * fontScale).sp,
            lineHeight = (16 * fontScale).sp,
            letterSpacing = 0.5.sp
        )
    )
}
