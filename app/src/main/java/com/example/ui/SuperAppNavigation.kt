package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.signOutFromGoogle
import com.example.data.UiState
import com.example.ui.screens.AccountAndSettingsScreen
import com.example.ui.screens.BankingServicesScreen
import com.example.ui.screens.HisabKitabScreen
import com.example.ui.screens.HomeMenuScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.ToolsScreen
import com.example.viewmodel.SuperAppViewModel

object SuperAppRoutes {
    const val HOME = "home_menu"
    const val NEWS = "news_feed"
    const val SHOP = "ecommerce_shop"
    const val BANKING = "banking_services"
    const val ACCOUNT = "account_settings"
    const val TOOLS = "smart_tools"
    const val HISAB = "business_hisab_kitab"
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAppShell(
    viewModel: SuperAppViewModel
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val khataState by viewModel.khataState.collectAsStateWithLifecycle()
    val ordersState by viewModel.ordersState.collectAsStateWithLifecycle()
    val transactionsState by viewModel.transactionsState.collectAsStateWithLifecycle()
    val savedArticlesState by viewModel.savedArticlesState.collectAsStateWithLifecycle()
    val selectedNewsRegion by viewModel.selectedNewsRegion.collectAsStateWithLifecycle()
    val newsState by viewModel.newsState.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()

    var bankingInitialTab by remember { mutableIntStateOf(0) }
    var toolsInitialTab by remember { mutableIntStateOf(0) }
    var accountInitialTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val topLevelDestinations = remember {
        listOf(
            TopLevelDestination(SuperAppRoutes.HOME, "मेन्यू", Icons.Filled.Apps, Icons.Outlined.Apps),
            TopLevelDestination(SuperAppRoutes.NEWS, "समाचार", Icons.Filled.Newspaper, Icons.Outlined.Newspaper),
            TopLevelDestination(SuperAppRoutes.SHOP, "खरीदारी", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
            TopLevelDestination(SuperAppRoutes.BANKING, "सेवाएं", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
            TopLevelDestination(SuperAppRoutes.ACCOUNT, "खाता", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: SuperAppRoutes.HOME
    val isSecondaryScreen = currentRoute == SuperAppRoutes.TOOLS || currentRoute == SuperAppRoutes.HISAB
    val totalCartItems = cartItems.sumOf { it.quantity }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val topBarTitle = when (currentRoute) {
        SuperAppRoutes.HOME -> "Bihar Seva • सुपर ऐप"
        SuperAppRoutes.NEWS -> "समाचार • News"
        SuperAppRoutes.SHOP -> "ऑनलाइन खरीदारी • Shop"
        SuperAppRoutes.BANKING -> "बैंकिंग व डिजिटल सेवाएं"
        SuperAppRoutes.ACCOUNT -> "मेरा खाता व सेटिंग्स"
        SuperAppRoutes.TOOLS -> "स्मार्ट व पर्सनल टूल्स"
        SuperAppRoutes.HISAB -> "बिज़नेस • हिसाब किताब"
        else -> "Bihar Seva"
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        if (isSecondaryScreen) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.testTag("top_bar_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentRoute != SuperAppRoutes.TOOLS) {
                            IconButton(
                                onClick = {
                                    toolsInitialTab = 0
                                    navController.navigate(SuperAppRoutes.TOOLS) { launchSingleTop = true }
                                },
                                modifier = Modifier.testTag("top_bar_tools_button")
                            ) {
                                Icon(Icons.Rounded.Calculate, contentDescription = "Tools & Calculators")
                            }
                        }
                        if (currentRoute != SuperAppRoutes.HISAB) {
                            IconButton(
                                onClick = {
                                    navController.navigate(SuperAppRoutes.HISAB) { launchSingleTop = true }
                                },
                                modifier = Modifier.testTag("top_bar_hisab_button")
                            ) {
                                Icon(Icons.Rounded.MenuBook, contentDescription = "Hisab Kitab")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                if (!useNavRail && !isSecondaryScreen) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        topLevelDestinations.forEach { dest ->
                            val selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                modifier = Modifier.testTag("nav_bar_${dest.route}"),
                                icon = {
                                    if (dest.route == SuperAppRoutes.SHOP && totalCartItems > 0) {
                                        BadgedBox(badge = { Badge { Text("$totalCartItems") } }) {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = dest.label
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    }
                                },
                                label = { Text(dest.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        topLevelDestinations.forEach { dest ->
                            val selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = SuperAppRoutes.HOME
                    ) {
                        composable(SuperAppRoutes.HOME) {
                            HomeMenuScreen(
                                userProfileState = userProfileState,
                                khataState = khataState,
                                settings = settings,
                                cartCount = totalCartItems,
                                onOpenNews = { region ->
                                    viewModel.selectNewsRegion(region)
                                    navigateTopLevel(SuperAppRoutes.NEWS)
                                },
                                onOpenShop = { navigateTopLevel(SuperAppRoutes.SHOP) },
                                onOpenBanking = { tab ->
                                    bankingInitialTab = tab
                                    navigateTopLevel(SuperAppRoutes.BANKING)
                                },
                                onOpenTools = { tab ->
                                    toolsInitialTab = tab
                                    navController.navigate(SuperAppRoutes.TOOLS) { launchSingleTop = true }
                                },
                                onOpenHisabKitab = {
                                    navController.navigate(SuperAppRoutes.HISAB) { launchSingleTop = true }
                                },
                                onOpenAccountTab = { tab ->
                                    accountInitialTab = tab
                                    navigateTopLevel(SuperAppRoutes.ACCOUNT)
                                }
                            )
                        }

                        composable(SuperAppRoutes.NEWS) {
                            NewsScreen(
                                selectedRegion = selectedNewsRegion,
                                newsState = newsState,
                                savedArticlesState = savedArticlesState,
                                settings = settings,
                                onSelectRegion = viewModel::selectNewsRegion,
                                onRefresh = { viewModel.refreshNews() },
                                onChangeDistrict = viewModel::setDefaultDistrict,
                                onBookmarkArticle = viewModel::bookmarkArticle,
                                onRemoveBookmark = viewModel::removeSavedArticle
                            )
                        }

                        composable(SuperAppRoutes.SHOP) {
                            ShopScreen(
                                cartItems = cartItems,
                                userProfile = (userProfileState as? UiState.Success)?.data,
                                settings = settings,
                                onAddToCart = viewModel::addToCart,
                                onUpdateCartQty = viewModel::updateCartQuantity,
                                onPlaceOrder = viewModel::placeOrder
                            )
                        }

                        composable(SuperAppRoutes.BANKING) {
                            BankingServicesScreen(
                                initialTab = bankingInitialTab,
                                settings = settings,
                                onRecordTransaction = viewModel::recordServiceTransaction
                            )
                        }

                        composable(SuperAppRoutes.TOOLS) {
                            BackHandler { navController.popBackStack() }
                            ToolsScreen(
                                initialTab = toolsInitialTab,
                                settings = settings,
                                onSaveCashTally = { title, sub, ref, amt ->
                                    viewModel.recordServiceTransaction("CASH_TALLY", title, sub, ref, amt)
                                }
                            )
                        }

                        composable(SuperAppRoutes.HISAB) {
                            BackHandler { navController.popBackStack() }
                            HisabKitabScreen(
                                khataState = khataState,
                                settings = settings,
                                onAddEntry = viewModel::addKhataEntry,
                                onToggleSettled = viewModel::toggleKhataSettled,
                                onDeleteEntry = viewModel::deleteKhataEntry
                            )
                        }

                        composable(SuperAppRoutes.ACCOUNT) {
                            AccountAndSettingsScreen(
                                initialTab = accountInitialTab,
                                userProfileState = userProfileState,
                                ordersState = ordersState,
                                transactionsState = transactionsState,
                                settings = settings,
                                onSaveProfile = viewModel::saveUserProfile,
                                onUpdateOrderStatus = viewModel::updateOrderStatus,
                                onDeleteTransaction = viewModel::deleteTransaction,
                                onSetTheme = viewModel::setThemeMode,
                                onSetLanguage = viewModel::setLanguage,
                                onSetDistrict = viewModel::setDefaultDistrict,
                                onSetDefaultGst = viewModel::setDefaultGst,
                                onSetCompactMode = viewModel::setCompactMode,
                                onSetHaptic = viewModel::setHapticFeedback,
                                onSignOut = {
                                    signOutFromGoogle(credentialManager, scope)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
