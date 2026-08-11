package com.karol.readingsapp.core.theme

import com.karol.readingsapp.core.i18n.LocalizedStrings

enum class AppTheme {
    SKY_BLUE,
    PURPLE,
    SEPIA,
    E_INK,
    ;

    fun getDisplayName(strings: LocalizedStrings): String = when (this) {
        SKY_BLUE -> strings.themeSkyBlue
        PURPLE -> strings.themePurple
        SEPIA -> strings.themeSepia
        E_INK -> "E-Ink" // Fallback if not in strings
    }
}
