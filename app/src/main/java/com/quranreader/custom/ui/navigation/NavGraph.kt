package com.quranreader.custom.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quranreader.custom.ui.screens.bookmarks.BookmarksScreen
import com.quranreader.custom.ui.screens.download.DownloadScreen
import com.quranreader.custom.ui.screens.home.DashboardScreen
import com.quranreader.custom.ui.screens.juz.JuzScreen
import com.quranreader.custom.ui.screens.onboarding.MandatoryDownloadScreen
import com.quranreader.custom.ui.screens.reading.ReadingScreen
import com.quranreader.custom.ui.screens.reading.MushafReaderScreen
import com.quranreader.custom.ui.screens.search.SearchScreen
import com.quranreader.custom.ui.screens.session.SessionManagementScreen
import com.quranreader.custom.ui.screens.settings.SettingsScreen
import com.quranreader.custom.ui.viewmodel.SettingsViewModel

// ── Route definitions ────────────────────────────────────────────────────────

sealed class Screen(val route: String) {
    object MandatoryDownload : Screen("mandatory_download")
    object Reading : Screen("reading")
    object ReadingPage : Screen("reading/{page}") {
        fun createRoute(page: Int) = "reading/$page"
    }
    object MushafReader : Screen("mushaf/{page}") {
        fun createRoute(page: Int) = "mushaf/$page"
    }
    object MushafReaderWithSession : Screen("mushaf_session/{page}") {
        fun createRoute(page: Int) = "mushaf_session/$page"
    }
    object Juz : Screen("juz")
    object Sessions : Screen("sessions")
    object Bookmarks : Screen("bookmarks")
    object Settings : Screen("settings")
    object Search : Screen("search")
    object Download : Screen("download")
    // Legacy routes kept for compatibility
    object Home : Screen("home")
}

// ── Bottom navigation tab definitions ────────────────────────────────────────

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Reading.route, "Reading", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    BottomNavItem(Screen.Juz.route, "Juz", Icons.Filled.GridView, Icons.Outlined.GridView),
    BottomNavItem(Screen.Sessions.route, "Session", Icons.Filled.Timer, Icons.Outlined.Timer),
    BottomNavItem(Screen.Bookmarks.route, "Bookmark", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
    BottomNavItem(Screen.Settings.route, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

// Routes where the bottom bar should be visible
private val bottomBarRoutes = bottomNavItems.map { it.route }.toSet()

// ── Main Navigation Graph ────────────────────────────────────────────────────

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuranNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val isFirstLaunchComplete by settingsViewModel.isFirstLaunchComplete.collectAsState(initial = false)

    val startDestination = if (isFirstLaunchComplete) {
        Screen.Reading.route
    } else {
        Screen.MandatoryDownload.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on the 5 main tabs
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = navBackStackEntry?.destination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(150, easing = LinearOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(100, easing = FastOutLinearInEasing))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(150, easing = LinearOutSlowInEasing))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(100, easing = FastOutLinearInEasing))
            }
        ) {
            // ── Mandatory Download (first launch) ─────────────────────────────
            composable(Screen.MandatoryDownload.route) {
                MandatoryDownloadScreen(
                    onDownloadComplete = {
                        navController.navigate(Screen.Reading.route) {
                            popUpTo(Screen.MandatoryDownload.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Tab 1: Reading (Dashboard - With Session Controls) ───────────
            composable(Screen.Reading.route) {
                DashboardScreen(
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToMushafWithSession = { page ->
                        navController.navigate(Screen.MushafReaderWithSession.createRoute(page)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Tab 2: Juz ────────────────────────────────────────────────────
            composable(Screen.Juz.route) {
                JuzScreen(
                    onNavigateToReading = { page ->
                        // Navigate directly to Mushaf Reader
                        navController.navigate(Screen.MushafReader.createRoute(page)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Tab 3: Session ────────────────────────────────────────────────
            composable(Screen.Sessions.route) {
                SessionManagementScreen(
                    onStartReading = { page ->
                        // Navigate to Mushaf Reader WITH session auto-start
                        navController.navigate(Screen.MushafReaderWithSession.createRoute(page)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Tab 4: Bookmark ───────────────────────────────────────────────
            composable(Screen.Bookmarks.route) {
                BookmarksScreen(
                    onNavigateToReading = { page ->
                        // Navigate directly to Mushaf Reader
                        navController.navigate(Screen.MushafReader.createRoute(page)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Tab 5: Settings ───────────────────────────────────────────────
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToDownload = {
                        navController.navigate(Screen.Download.route)
                    }
                )
            }

            // ── Secondary: Mushaf Reader (Layer 2) ───────────────────────────
            composable(
                route = Screen.MushafReader.route,
                arguments = listOf(
                    navArgument("page") {
                        type = NavType.IntType
                        defaultValue = 1
                    }
                )
            ) { backStackEntry ->
                val page = backStackEntry.arguments?.getInt("page") ?: 1
                MushafReaderScreen(
                    initialPage = page,
                    onBack = { navController.popBackStack() },
                    startSessionAutomatically = false
                )
            }

            // ── Secondary: Mushaf Reader WITH Session (from Session tab) ─────
            composable(
                route = Screen.MushafReaderWithSession.route,
                arguments = listOf(
                    navArgument("page") {
                        type = NavType.IntType
                        defaultValue = 1
                    }
                )
            ) { backStackEntry ->
                val page = backStackEntry.arguments?.getInt("page") ?: 1
                MushafReaderScreen(
                    initialPage = page,
                    onBack = { navController.popBackStack() },
                    startSessionAutomatically = true  // Auto-start session
                )
            }

            // ── Secondary: Reading with specific page ─────────────────────────
            composable(
                route = Screen.ReadingPage.route,
                arguments = listOf(
                    navArgument("page") {
                        type = NavType.IntType
                        defaultValue = 1
                    }
                )
            ) { backStackEntry ->
                val page = backStackEntry.arguments?.getInt("page") ?: 1
                ReadingScreen(
                    initialPage = page,
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToMushaf = { mushafPage ->
                        navController.navigate(Screen.MushafReader.createRoute(mushafPage)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Secondary: Search ─────────────────────────────────────────────
            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToReading = { page ->
                        // Navigate directly to Mushaf Reader, not dashboard
                        navController.navigate(Screen.MushafReader.createRoute(page)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Secondary: Download ───────────────────────────────────────────
            composable(Screen.Download.route) {
                DownloadScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
