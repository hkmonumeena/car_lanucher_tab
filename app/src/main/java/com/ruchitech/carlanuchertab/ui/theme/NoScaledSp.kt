package com.ruchitech.carlanuchertab.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

// Extension to get non-scaled sp (ignores fontScale)
val TextUnit.nonScaledSp: TextUnit
    @Composable
    get() {
        val fontScale = LocalDensity.current.fontScale
        return (this.value / fontScale).sp
    }

// Extension to get non-scaled dp (ignores screen density)
val TextUnit.nonScaledDp: Dp
    @Composable
    get() {
        val density = LocalDensity.current.density
        return (this.value / density).dp
    }

// Usage: Convert 100.dp to px manually
@Composable
fun dpToPx(dp: Dp): Float {
    return with(LocalDensity.current) { dp.toPx() }
}
