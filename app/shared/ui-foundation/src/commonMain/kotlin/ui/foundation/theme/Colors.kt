/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.ui.foundation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color


@Composable
fun Color.looming(): Color {
    return copy(alpha = 0.90f)
}

@Composable
fun Color.slightlyWeaken(): Color {
    return copy(alpha = 0.618f)
}

@Composable
fun Color.weaken(): Color {
    return copy(alpha = 0.5f)
}

@Composable
fun Color.stronglyWeaken(): Color {
    return copy(alpha = 1 - 0.618f)
}

@Composable
fun Color.disabledWeaken(): Color {
    return copy(alpha = 0.12f)
}

@Composable
fun aniColorScheme(
    dark: Boolean = isSystemInDarkTheme()
): ColorScheme {
    return if (dark) {
        aniDarkColorTheme()
    } else {
        aniLightColorTheme()
    }
}

@Stable
fun aniDarkColorTheme(): ColorScheme {
    PaletteTokens.run {
        return darkColorScheme()
    }
}


@Stable
fun aniLightColorTheme(): ColorScheme = lightColorScheme(
    background = Color(0xfff1f2f4),
    surface = Color.White,
    surfaceVariant = Color.White,
    surfaceContainer = Color.White,
    // TODO: We should use respect material design instead of overriding them as white
    surfaceContainerHigh = Color.White,
)
