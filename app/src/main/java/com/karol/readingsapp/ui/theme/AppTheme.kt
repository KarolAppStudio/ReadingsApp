package com.karol.readingsapp.ui.theme

import com.karol.readingsapp.ui.LocalizedStrings

enum class AppTheme {
    BLUE,
    PURPLE,
    SEPIA,
    PLEASANT,
    ;

    fun getDisplayName(strings: LocalizedStrings): String = when (this) {
        BLUE -> strings.themeSkyBlue
        PURPLE -> strings.themePurple
        SEPIA -> strings.themeSepia
        PLEASANT -> strings.themeModernGlass
    }
}
