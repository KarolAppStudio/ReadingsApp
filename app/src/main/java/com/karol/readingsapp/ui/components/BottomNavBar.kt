package com.karol.readingsapp.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karol.readingsapp.ui.LocalizedStrings
import com.karol.readingsapp.ui.theme.GlassBorder

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
    val isPleasant = MaterialTheme.colorScheme.outline == GlassBorder

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isPleasant) 0.dp else 8.dp,
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
            icon = Icons.AutoMirrored.Filled.MenuBook,
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
private fun RowScope.AppNavigationBarItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        icon = { Icon(icon, contentDescription = label) },
        label = {
            AutoResizingText(
                text = label,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                minFontSize = 8.sp,
            )
        },
        selected = selected,
        alwaysShowLabel = true,
        onClick = if (selected) ({}) else onClick,
        colors = if (selected) {
            NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = MaterialTheme.colorScheme.secondary,
            )
        } else {
            NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.primary,
            )
        },
    )
}
