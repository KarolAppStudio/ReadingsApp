package com.karol.readingsapp.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.karol.readingsapp.core.i18n.LocalizedStrings
import com.karol.readingsapp.core.theme.AdaptiveDimens

enum class NavItem {
    Home,
    Calendar,
    Bible,
    Settings,
}

@Composable
fun AppBottomNavBar(
    selectedItem: NavItem,
    strings: LocalizedStrings,
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onBibleClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
    ) {
        AppNavigationBarItem(
            selected = selectedItem == NavItem.Home,
            icon = Icons.Default.Home,
            label = strings.home,
            onClick = onHomeClick,
        )
        AppNavigationBarItem(
            selected = selectedItem == NavItem.Calendar,
            icon = Icons.Default.DateRange,
            label = strings.calendar,
            onClick = onCalendarClick,
        )
        AppNavigationBarItem(
            selected = selectedItem == NavItem.Bible,
            icon = Icons.AutoMirrored.Filled.List,
            label = strings.bible,
            onClick = onBibleClick,
        )
        AppNavigationBarItem(
            selected = selectedItem == NavItem.Settings,
            icon = Icons.Default.Settings,
            label = strings.settings,
            onClick = onSettingsClick,
        )
    }
}

@Composable
private fun RowScope.AppNavigationBarItem(selected: Boolean, icon: ImageVector, label: String, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(if (AdaptiveDimens.fontScale > 1.0f) 30.dp else 24.dp),
            )
        },
        label = {
            AutoResizingText(
                text = label,
                fontSize = AdaptiveDimens.smallFontSize,
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    )
}
