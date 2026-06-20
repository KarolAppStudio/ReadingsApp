package com.karol.readingsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.karol.readingsapp.data.AppDatabase
import com.karol.readingsapp.data.ReadingRepository
import com.karol.readingsapp.ui.AboutScreen
import com.karol.readingsapp.ui.BibleReaderScreen
import com.karol.readingsapp.ui.HomeScreen
import com.karol.readingsapp.ui.ReadingPlanScreen
import com.karol.readingsapp.ui.ReadingViewModel
import com.karol.readingsapp.ui.SettingsScreen
import com.karol.readingsapp.ui.theme.ReadingsAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadingsAppTheme {
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = ReadingRepository(database.combinedDao())
                val viewModel: ReadingViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ReadingViewModel(repository) as T
                        }
                    },
                )

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onReadingClick = { reading ->
                                navController.navigate("reader/${reading.bookName}/${reading.chapter}")
                            },
                            onCalendarClick = {
                                navController.navigate("reading_plan")
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
                            },
                            onAboutClick = {
                                navController.navigate("about")
                            },
                        )
                    }
                    composable("about") {
                        AboutScreen {
                            navController.popBackStack()
                        }
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                        ) {
                            navController.popBackStack()
                        }
                    }
                    composable("reading_plan") {
                        ReadingPlanScreen(
                            viewModel = viewModel,
                            onDateClick = { date ->
                                viewModel.loadReading(date)
                                navController.popBackStack("home", inclusive = false)
                            },
                        ) {
                            navController.popBackStack("home", inclusive = false)
                        }
                    }
                    composable(
                        route = "reader/{bookName}/{chapter}",
                        arguments = listOf(
                            navArgument("bookName") { type = NavType.StringType },
                            navArgument("chapter") { type = NavType.IntType },
                        ),
                    ) { backStackEntry ->
                        val bookName = backStackEntry.arguments?.getString("bookName") ?: ""
                        val chapter = backStackEntry.arguments?.getInt("chapter") ?: 0
                        BibleReaderScreen(
                            bookName = bookName,
                            chapter = chapter,
                            viewModel = viewModel
                        ) {
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}
