package com.karol.readingsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.karol.readingsapp.ui.ParallelReadingScreen
import com.karol.readingsapp.ui.ReadingPlanScreen
import com.karol.readingsapp.ui.ReadingViewModel
import com.karol.readingsapp.ui.SettingsScreen
import com.karol.readingsapp.ui.theme.BackgroundBlue
import com.karol.readingsapp.ui.theme.ReadingsAppTheme

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
            ReadingsAppTheme {
                val bibleDatabase = BibleDatabase.getDatabase(applicationContext)
                val planDatabase = ReadingPlanDatabase.getDatabase(applicationContext)
                val repository = ReadingRepository(
                    bibleDatabase.bibleDao(),
                    planDatabase.readingPlanDao()
                )
                val languageService = LanguageService()
                val viewModel: ReadingViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ReadingViewModel(repository, languageService) as T
                        }
                    },
                )

                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundBlue,
                ) {
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
                                onAboutClick = {
                                    navController.navigate("about")
                                }
                            )
                        }
                        composable("about") {
                            AboutScreen {
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
                                onBibleClick = {
                                    navController.navigate("bible")
                                }
                            )
                        }
                        composable("reading_plan") {
                            ReadingPlanScreen(
                                viewModel = viewModel,
                                onDateClick = { date ->
                                    viewModel.loadReading(date)
                                    navController.popBackStack("home", inclusive = false)
                                },
                                onHomeClick = {
                                    navController.popBackStack("home", inclusive = false)
                                },
                                onBibleClick = {
                                    navController.navigate("bible")
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
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
                                onParallelClick = { bookId, chapter ->
                                    navController.navigate("parallel_reader/$bookId/$chapter")
                                }
                            )
                        }
                        composable(
                            route = "parallel_reader/{bookId}/{chapter}",
                            arguments = listOf(
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
                            arguments = listOf(
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
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
