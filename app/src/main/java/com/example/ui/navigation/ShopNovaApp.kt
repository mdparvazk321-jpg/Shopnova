package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.ShopNovaViewModel
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartAndCheckoutScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.FlashSaleScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.OrdersListScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ReturnRefundScreen
import com.example.ui.screens.SavedAddressesScreen
import com.example.ui.screens.SellerDashboardScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaWhite

object Routes {
    const val HOME = "home"
    const val CATEGORIES = "categories"
    const val FLASH_SALE = "flash_sale"
    const val CART = "cart"
    const val ACCOUNT = "account"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val ORDER_TRACKING = "tracking/{orderId}"
    const val ORDERS = "orders"
    const val WISHLIST = "wishlist"
    const val ADDRESSES = "addresses"
    const val RETURNS = "returns?orderId={orderId}"
    const val NOTIFICATIONS = "notifications"
    const val SELLER_DASHBOARD = "seller_dashboard"
    const val ADMIN_DASHBOARD = "admin_dashboard"

    fun product(id: String) = "product/$id"
    fun tracking(id: String) = "tracking/$id"
    fun returns(orderId: String? = null) = if (orderId == null) "returns" else "returns?orderId=$orderId"
}

private data class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomTabs = listOf(
    BottomNavTab(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavTab(Routes.CATEGORIES, "Categories", Icons.Filled.Category, Icons.Outlined.Category),
    BottomNavTab(Routes.FLASH_SALE, "Flash Sale", Icons.Filled.Bolt, Icons.Outlined.Bolt),
    BottomNavTab(Routes.CART, "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    BottomNavTab(Routes.ACCOUNT, "Account", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun ShopNovaApp(viewModel: ShopNovaViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val topLevelRoutes = remember { bottomTabs.map { it.route }.toSet() }
    val showNavigation = currentRoute in topLevelRoutes

    fun navigateTopLevel(route: String) {
        if (route == Routes.HOME) {
            viewModel.clearFilters()
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showNavigation && !useRail) {
                    NavigationBar(
                        containerColor = NovaWhite,
                        tonalElevation = 8.dp
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(tab.route) },
                                icon = {
                                    val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                                    if (tab.route == Routes.CART && cartCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = NovaPink) {
                                                    Text("$cartCount", color = NovaWhite)
                                                }
                                            }
                                        ) {
                                            Icon(icon, contentDescription = tab.label)
                                        }
                                    } else {
                                        Icon(icon, contentDescription = tab.label)
                                    }
                                },
                                label = {
                                    Text(
                                        tab.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NovaPink,
                                    selectedTextColor = NovaPink,
                                    indicatorColor = NovaPinkSoft,
                                    unselectedIconColor = NovaNavy.copy(alpha = 0.65f),
                                    unselectedTextColor = NovaNavy.copy(alpha = 0.65f)
                                ),
                                modifier = Modifier.testTag("nav_${tab.route}")
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
                if (showNavigation && useRail) {
                    NavigationRail(containerColor = NovaWhite) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(tab.route) },
                                icon = {
                                    val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                                    if (tab.route == Routes.CART && cartCount > 0) {
                                        BadgedBox(badge = { Badge(containerColor = NovaPink) { Text("$cartCount", color = NovaWhite) } }) {
                                            Icon(icon, contentDescription = tab.label)
                                        }
                                    } else {
                                        Icon(icon, contentDescription = tab.label)
                                    }
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = NovaPink,
                                    selectedTextColor = NovaPink,
                                    indicatorColor = NovaCyanSoft
                                )
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME
                    ) {
                        composable(Routes.HOME) {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToProduct = { navController.navigate(Routes.product(it)) },
                                onNavigateToCategories = { navigateTopLevel(Routes.CATEGORIES) },
                                onNavigateToFlashSale = { navigateTopLevel(Routes.FLASH_SALE) },
                                onNavigateToCart = { navigateTopLevel(Routes.CART) },
                                onNavigateToAccount = { navigateTopLevel(Routes.ACCOUNT) },
                                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                                onSelectCategoryAndBrowse = { catId ->
                                    viewModel.selectCategory(catId)
                                    navigateTopLevel(Routes.CATEGORIES)
                                }
                            )
                        }
                        composable(Routes.CATEGORIES) {
                            CategoriesScreen(
                                viewModel = viewModel,
                                onNavigateToProduct = { navController.navigate(Routes.product(it)) }
                            )
                        }
                        composable(Routes.FLASH_SALE) {
                            FlashSaleScreen(
                                viewModel = viewModel,
                                onNavigateToProduct = { navController.navigate(Routes.product(it)) }
                            )
                        }
                        composable(Routes.CART) {
                            CartAndCheckoutScreen(
                                viewModel = viewModel,
                                onNavigateToProduct = { navController.navigate(Routes.product(it)) },
                                onExploreShop = { navigateTopLevel(Routes.HOME) },
                                onOrderPlacedNavigateToTracking = { orderId ->
                                    navController.navigate(Routes.tracking(orderId))
                                }
                            )
                        }
                        composable(Routes.ACCOUNT) {
                            AccountScreen(
                                viewModel = viewModel,
                                onOpenOrders = { navController.navigate(Routes.ORDERS) },
                                onOpenWishlist = { navController.navigate(Routes.WISHLIST) },
                                onOpenAddresses = { navController.navigate(Routes.ADDRESSES) },
                                onOpenReturns = { navController.navigate(Routes.returns()) },
                                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                                onOpenSellerDashboard = { navController.navigate(Routes.SELLER_DASHBOARD) },
                                onOpenAdminDashboard = { navController.navigate(Routes.ADMIN_DASHBOARD) }
                            )
                        }
                        composable(
                            route = Routes.PRODUCT_DETAIL,
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { entry ->
                            val productId = entry.arguments?.getString("productId") ?: ""
                            ProductDetailScreen(
                                productId = productId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onBuyNowNavigateToCart = { navigateTopLevel(Routes.CART) }
                            )
                        }
                        composable(
                            route = Routes.ORDER_TRACKING,
                            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                        ) { entry ->
                            val orderId = entry.arguments?.getString("orderId") ?: ""
                            OrderTrackingScreen(
                                orderId = orderId,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onRequestReturn = { oid -> navController.navigate(Routes.returns(oid)) }
                            )
                        }
                        composable(Routes.ORDERS) {
                            OrdersListScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onTrackOrder = { oid -> navController.navigate(Routes.tracking(oid)) }
                            )
                        }
                        composable(Routes.WISHLIST) {
                            WishlistScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onNavigateToProduct = { pid -> navController.navigate(Routes.product(pid)) }
                            )
                        }
                        composable(Routes.ADDRESSES) {
                            SavedAddressesScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = Routes.RETURNS,
                            arguments = listOf(
                                navArgument("orderId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { entry ->
                            ReturnRefundScreen(
                                preselectedOrderId = entry.arguments?.getString("orderId"),
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.NOTIFICATIONS) {
                            NotificationsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.SELLER_DASHBOARD) {
                            SellerDashboardScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_DASHBOARD) {
                            AdminDashboardScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
