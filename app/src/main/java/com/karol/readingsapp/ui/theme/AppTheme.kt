package com.karol.readingsapp.ui.theme

import com.karol.readingsapp.ui.LocalizedStrings

enum class AppTheme {
    SKY_BLUE,
    PURPLE,
    SEPIA,
    ;

    fun getDisplayName(strings: LocalizedStrings): String = when (this) {
        SKY_BLUE -> strings.themeSkyBlue
        PURPLE -> strings.themePurple
        SEPIA -> strings.themeSepia
    }
}
