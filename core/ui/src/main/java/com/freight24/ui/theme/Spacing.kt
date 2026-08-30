package com.freight24.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Freight24 spacing scale — the ONLY spacing values screens and shared
 * components should use. Every step is a multiple of 4dp.
 *
 * Rhythm guide:
 *  - [xs]      icon↔label, inside badges
 *  - [sm]      related elements (label↔input, card internal rows)
 *  - [md]      list-item internal padding
 *  - [base]    screen-edge padding, unrelated sibling gap
 *  - [lg]      card↔card in a list
 *  - [xl]      between sections on a screen
 *  - [xxl]      major section breaks
 *  - [xxxl]    hero/empty-state breathing room
 */
@Immutable
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val base: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
}
