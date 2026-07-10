package com.karol.readingsapp.ui.theme

import com.karol.readingsapp.ui.LocalizedStrings

enum class AppTheme {
    SKY_BLUE,
    PURPLE,
    SEPIA,
    DARK_FROSTED_GLASS,
    ;

    fun getDisplayName(strings: LocalizedStrings): String = when (this) {
        SKY_BLUE -> strings.themeSkyBlue
        PURPLE -> strings.themePurple
        SEPIA -> strings.themeSepia
        DARK_FROSTED_GLASS -> strings.themeDarkFrostedGlass
    }
}
