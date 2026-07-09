package com.karol.readingsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.karol.readingsapp.data.LanguageService
import com.karol.readingsapp.data.ReadingRepository
import com.karol.readingsapp.data.bible.BibleDatabase
import com.karol.readingsapp.data.plan.ReadingPlanDatabase
import com.karol.readingsapp.ui.AboutScreen
import com.karol.readingsapp.ui.BibleReaderScreen
import com.karol.readingsapp.ui.BibleSelectionScreen
import com.karol.readingsapp.ui.HomeScreen
import com.karol.readingsapp.ui.Localization
import com.karol.readingsapp.ui.ParallelReadingScreen
import com.karol.readingsapp.ui.ReadingPlanScreen
import com.karol.readingsapp.ui.ReadingViewModel
import com.karol.readingsapp.ui.SettingsScreen
import com.karol.readingsapp.ui.components.DownloadProgressOverlay
import com.karol.readingsapp.ui.theme.ProvideWindowSizeClass
import com.karol.readingsapp.ui.theme.ReadingsAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        lifecycleScope.launch {
            delay(2.seconds)
            keepSplashScreen = false
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass =
                @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
                calculateWindowSizeClass(this)
            val bibleDatabase = BibleDatabase.getDatabase(applicationContext)
            val planDatabase = ReadingPlanDatabase.getDatabase(applicationContext)
            val repository =
                ReadingRepository(
                    bibleDatabase.bibleDao(),
                    planDatabase.readingPlanDao(),
                )
            val languageService = LanguageService(applicationContext, bibleDatabase.bibleDao())
            val viewModel: ReadingViewModel =
                viewModel(
                    factory =
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T = ReadingViewModel(repository, languageService, applicationContext) as T
                    },
                )

            val currentTheme by viewModel.appTheme.collectAsState()
            val batchProgress by viewModel.batchProgress.collectAsState()
            val translations by viewModel.availableTranslations.collectAsState()
            val selectedCode by viewModel.selectedTranslationCode.collectAsState()

            val selectedLanguage = remember(selectedCode, translations) {
                translations.find { it.code == selectedCode }?.language ?: "English"
            }
            val strings = remember(selectedLanguage) { Localization.getStrings(selectedLanguage) }

            ProvideWindowSizeClass(windowSizeClass) {
                ReadingsAppTheme(appTheme = currentTheme) {
                    val navController = rememberNavController()

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            NavHost(navController = navController, startDestination = "home") {
                                composable("home") {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onReadingClick = { reading ->
                                            navController.navigate("reader/${reading.bookId}/${reading.chapter}/1")
                                        },
                                        onCalendarClick = {
                                            navController.navigate("reading_plan")
                                        },
                                        onBibleClick = {
                                            navController.navigate("bible")
                                        },
                                        onSettingsClick = {
                                            navController.navigate("settings")
                                        },
                                    ) {
                                        navController.navigate("about")
                                    }
                                }
                                composable("about") {
                                    AboutScreen(strings = strings) {
                                        navController.popBackStack("home", inclusive = false)
                                    }
                                }
                                composable("settings") {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onHomeClick = {
                                            navController.popBackStack("home", inclusive = false)
                                        },
                                        onCalendarClick = {
                                            navController.navigate("reading_plan")
                                        },
                                    ) {
                                        navController.navigate("bible")
                                    }
                                }
                                composable("reading_plan") {
                                    ReadingPlanScreen(
                                        viewModel = viewModel,
                                        onHomeClick = {
                                            navController.popBackStack("home", inclusive = false)
                                        },
                                        onBibleClick = {
                                            navController.navigate("bible")
                                        },
                                        onSettingsClick = {
                                            navController.navigate("settings")
                                        },
                                        onDateClick = { date ->
                                            viewModel.loadReading(date)
                                            navController.popBackStack("home", inclusive = false)
                                        },
                                    )
                                }
                                composable("bible") {
                                    BibleSelectionScreen(
                                        viewModel = viewModel,
                                        onHomeClick = {
                                            navController.popBackStack("home", inclusive = false)
                                        },
                                        onCalendarClick = {
                                            navController.navigate("reading_plan")
                                        },
                                        onSettingsClick = {
                                            navController.navigate("settings")
                                        },
                                        onChapterClick = { bookId, chapter, verseId ->
                                            navController.navigate("reader/$bookId/$chapter/$verseId")
                                        },
                                    ) { bookId, chapter ->
                                        navController.navigate("parallel_reader/$bookId/$chapter")
                                    }
                                }
                                composable(
                                    route = "parallel_reader/{bookId}/{chapter}",
                                    arguments =
                                    listOf(
                                        navArgument("bookId") { type = NavType.IntType },
                                        navArgument("chapter") { type = NavType.IntType },
                                    ),
                                ) { backStackEntry ->
                                    val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
                                    val chapter = backStackEntry.arguments?.getInt("chapter") ?: 0
                                    ParallelReadingScreen(
                                        bookId = bookId,
                                        chapter = chapter,
                                        viewModel = viewModel,
                                    ) {
                                        navController.popBackStack()
                                    }
                                }
                                composable(
                                    route = "reader/{bookId}/{chapter}/{verseId}",
                                    arguments =
                                    listOf(
                                        navArgument("bookId") { type = NavType.IntType },
                                        navArgument("chapter") { type = NavType.IntType },
                                        navArgument("verseId") { type = NavType.IntType },
                                    ),
                                ) { backStackEntry ->
                                    val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
                                    val chapter = backStackEntry.arguments?.getInt("chapter") ?: 0
                                    val verseId = backStackEntry.arguments?.getInt("verseId") ?: 1
                                    BibleReaderScreen(
                                        bookId = bookId,
                                        chapter = chapter,
                                        initialVerse = verseId,
                                        viewModel = viewModel,
                                        onHomeClick = {
                                            navController.popBackStack("home", inclusive = false)
                                        },
                                        onBackClick = {
                                            navController.popBackStack()
                                        },
                                        onParallelClick = { bId, chap ->
                                            viewModel.loadSecondChapterVerses(bId, chap, "ENG")
                                            navController.navigate("parallel_reader/$bId/$chap")
                                        },
                                    ) { bId, chap ->
                                        navController.navigate("reader/$bId/$chap/1") {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }

                            DownloadProgressOverlay(
                                progress = batchProgress,
                                strings = strings,
                            )
                        }
                    }
                }
            }
        }
    }
}
