package com.karol.readingsapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.karol.readingsapp.core.theme.*
import com.karol.readingsapp.core.ui.components.DownloadProgressOverlay
import com.karol.readingsapp.feature.about.ui.AboutScreen
import com.karol.readingsapp.feature.bible.data.BibleDatabase
import com.karol.readingsapp.feature.bible.data.LanguageService
import com.karol.readingsapp.feature.bible.data.ReadingRepository
import com.karol.readingsapp.feature.bible.ui.BibleReaderScreen
import com.karol.readingsapp.feature.bible.ui.BibleSelectionScreen
import com.karol.readingsapp.feature.bible.ui.ParallelReadingScreen
import com.karol.readingsapp.feature.contact.ui.ContactScreen
import com.karol.readingsapp.feature.home.ui.HomeScreen
import com.karol.readingsapp.feature.plan.data.ReadingPlanDatabase
import com.karol.readingsapp.feature.plan.ui.ReadingPlanScreen
import com.karol.readingsapp.feature.settings.ui.SettingsScreen
import com.karol.readingsapp.feature.shared.ui.ReadingViewModel
import com.karol.readingsapp.feature.voice.data.VoiceServiceProxy
import com.karol.readingsapp.feature.voice.ui.VoiceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            // Pre-initialize and open databases in background to speed up first run
            withContext(Dispatchers.IO) {
                try {
                    val bibleDb = BibleDatabase.getDatabase(applicationContext)
                    val planDb = ReadingPlanDatabase.getDatabase(applicationContext)
                    // Trigger database opening and any createFromAsset/onOpen logic
                    bibleDb.openHelper.writableDatabase
                    planDb.openHelper.writableDatabase
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error pre-initializing databases", e)
                }
            }

            // Ensure splash screen stays for at least 800ms for smooth transition
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < 800) {
                delay((800 - elapsed).milliseconds)
            }
            keepSplashScreen = false
        }

        super.onCreate(savedInstanceState)
        askNotificationPermission()
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
            val languageService = LanguageService(applicationContext, bibleDatabase)
            val voiceService: VoiceServiceProxy = remember { VoiceServiceProxy(applicationContext) }
            val viewModel: ReadingViewModel =
                viewModel(
                    factory =
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            ReadingViewModel(repository, languageService, voiceService, applicationContext) as T
                    },
                )
            val voiceViewModel: VoiceViewModel = viewModel()

            val currentTheme by viewModel.appTheme.collectAsState()

            LaunchedEffect(currentTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ),
                    navigationBarStyle = SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ),
                )
            }

            val batchProgress by viewModel.batchProgress.collectAsState()
            val showDownloadOverlay by viewModel.showDownloadOverlay.collectAsState()
            val strings by viewModel.strings.collectAsState()

            LaunchedEffect(strings.locale) {
                voiceViewModel.filterVoices(strings.locale, autoSelect = true)
            }

            val selectedLanguage by viewModel.selectedLanguage.collectAsState()

            ProvideWindowSizeClass(windowSizeClass) {
                ReadingsAppTheme(
                    appTheme = currentTheme,
                    language = selectedLanguage,
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    var settingsTabIndex by remember { mutableIntStateOf(0) }

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
                                            navController.navigate(
                                                "reader/${reading.bookId}/${reading.chapter}/${reading.verseId}/${reading.readingType}",
                                            )
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
                                        },
                                    ) {
                                        navController.navigate("contact")
                                    }
                                }
                                composable("about") {
                                    AboutScreen(strings = strings) {
                                        navController.popBackStack("home", inclusive = false)
                                    }
                                }
                                composable("contact") {
                                    ContactScreen(strings = strings) {
                                        navController.popBackStack("home", inclusive = false)
                                    }
                                }
                                composable("settings") {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        voiceViewModel = voiceViewModel,
                                        onHomeClick = {
                                            navController.popBackStack("home", inclusive = false)
                                        },
                                        onCalendarClick = {
                                            navController.navigate("reading_plan")
                                        },
                                        onBibleClick = {
                                            navController.navigate("bible")
                                        },
                                        initialTabIndex = settingsTabIndex,
                                    ) { settingsTabIndex = it }
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
                                            navController.navigate("reader/$bookId/$chapter/$verseId/null")
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
                                        navController.popBackStack("home", inclusive = false)
                                    }
                                }
                                composable(
                                    route = "reader/{bookId}/{chapter}/{verseId}/{readingType}",
                                    arguments =
                                    listOf(
                                        navArgument("bookId") { type = NavType.IntType },
                                        navArgument("chapter") { type = NavType.IntType },
                                        navArgument("verseId") { type = NavType.IntType },
                                        navArgument("readingType") {
                                            type = NavType.StringType
                                            nullable = true
                                        },
                                    ),
                                ) { backStackEntry ->
                                    val bookId = backStackEntry.arguments?.getInt("bookId") ?: 0
                                    val chapter = backStackEntry.arguments?.getInt("chapter") ?: 0
                                    val verseId = backStackEntry.arguments?.getInt("verseId") ?: 1
                                    val readingType = backStackEntry.arguments?.getString("readingType").let {
                                        if (it == "null") null else it
                                    }
                                    BibleReaderScreen(
                                        bookId = bookId,
                                        chapter = chapter,
                                        initialVerse = verseId,
                                        readingType = readingType,
                                        viewModel = viewModel,
                                        voiceViewModel = voiceViewModel,
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
                                    ) { bId, chap, type ->
                                        val typePath = type ?: "null"
                                        navController.navigate("reader/$bId/$chap/1/$typePath") {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }

                            val isDownloadTab = (currentRoute == "settings") && (settingsTabIndex == 1)
                            if (showDownloadOverlay && !isDownloadTab) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "E-Ink Light Mode")
@Composable
fun EInkReaderPreview() {
    ReadingsAppTheme(appTheme = AppTheme.E_INK, darkTheme = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Genesis 1", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.eInkBorder(),
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.eInkBorder(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        EInkButton(onClick = {}) {
                            Text("Previous")
                        }
                        EInkButton(onClick = {}) {
                            Text("Next")
                        }
                    }
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
            ) {
                EInkCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "In the beginning, God created the heavens and the earth. " +
                            "The earth was without form and void, and darkness was over the face of the deep.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                EInkTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Go to verse...",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
