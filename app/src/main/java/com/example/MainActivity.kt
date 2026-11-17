package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.GoogleAuthHelper
import com.example.data.KrishiRepository
import com.example.data.StarterCatalog
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartCheckoutScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OffersScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SellerDashboardScreen
import com.example.ui.screens.SupportComplaintsScreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.KrishiBazarTheme
import com.example.ui.theme.MintBackground
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftMintCard
import com.example.viewmodel.KrishiViewModel
import com.example.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KrishiBazarTheme {
                AppNavigation()
            }
        }
    }
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    var pendingPhone by remember { mutableStateOf("") }

    if (user == null) {
        AuthScreen(
            initialPhone = pendingPhone,
            onPhoneEntered = { pendingPhone = it },
            onAuthSuccess = {}
        )
    } else {
        val verifiedPhone = user.phoneNumber?.takeIf { it.isNotBlank() }
        val prefillPhone = verifiedPhone ?: pendingPhone
        key(user.uid) {
            AuthenticatedKrishiApp(
                currentUserId = user.uid,
                userEmail = user.email,
                userDisplayName = user.displayName,
                userPhoneNumber = prefillPhone,
                isPhoneVerified = verifiedPhone != null
            )
        }
    }
}

private data class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private object Routes {
    const val HOME = "home"
    const val CATEGORIES = "categories"
    const val OFFERS = "offers"
    const val ORDERS = "orders"
    const val PROFILE = "profile"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val CART = "cart"
    const val NOTIFICATIONS = "notifications"
    const val FAVORITES = "favorites"
    const val SUPPORT = "support"
    const val SELLER_DASHBOARD = "seller_dashboard"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val EDIT_PROFILE = "edit_profile"
}

@Composable
fun AuthenticatedKrishiApp(
    currentUserId: String,
    userEmail: String?,
    userDisplayName: String?,
    userPhoneNumber: String = "",
    isPhoneVerified: Boolean = false
) {
    val viewModel: KrishiViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                KrishiViewModel(
                    repository = KrishiRepository(db),
                    currentUserId = currentUserId,
                    userEmail = userEmail,
                    userDisplayName = userDisplayName
                )
            }
        }
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.toastEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val catalogState by viewModel.catalogState.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val cartState by viewModel.cartState.collectAsStateWithLifecycle()
    val favoritesState by viewModel.favoritesState.collectAsStateWithLifecycle()
    val notificationsState by viewModel.notificationsState.collectAsStateWithLifecycle()
    val buyerOrdersState by viewModel.buyerOrdersState.collectAsStateWithLifecycle()
    val sellerProductsState by viewModel.sellerProductsState.collectAsStateWithLifecycle()
    val sellerOrdersState by viewModel.sellerOrdersState.collectAsStateWithLifecycle()
    val myComplaintsState by viewModel.myComplaintsState.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    val adminUsersState by viewModel.adminUsersState.collectAsStateWithLifecycle()
    val adminProductsState by viewModel.adminProductsState.collectAsStateWithLifecycle()
    val adminOrdersState by viewModel.adminOrdersState.collectAsStateWithLifecycle()
    val adminComplaintsState by viewModel.adminComplaintsState.collectAsStateWithLifecycle()

    val userProfile: UserProfile? = (profileState as? UiState.Success)?.data
    val allProducts: List<Product> = (catalogState as? UiState.Success)?.data ?: StarterCatalog.products
    val catalogError: String? = (catalogState as? UiState.Error)?.message
    val cartItems = (cartState as? UiState.Success)?.data ?: emptyList()
    val favorites = (favoritesState as? UiState.Success)?.data ?: emptyList()
    val favoriteIds = remember(favorites) { favorites.map { it.productId }.toSet() }
    val notifications = (notificationsState as? UiState.Success)?.data ?: emptyList()
    val unreadNotifications = notifications.count { !it.readStatus }
    val buyerOrders = (buyerOrdersState as? UiState.Success)?.data ?: emptyList()
    val sellerProducts = (sellerProductsState as? UiState.Success)?.data ?: emptyList()
    val sellerOrders = (sellerOrdersState as? UiState.Success)?.data ?: emptyList()
    val myComplaints = (myComplaintsState as? UiState.Success)?.data ?: emptyList()

    // Initial Registration Gate when profile doesn't exist in Firestore yet
    var skippedInitialReg by remember(currentUserId) { mutableStateOf(false) }
    if (profileState is UiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ForestGreen)
        }
        return
    }

    if (profileState is UiState.Success && userProfile == null && !skippedInitialReg) {
        RegistrationScreen(
            existingProfile = null,
            defaultName = viewModel.defaultUserName(),
            defaultPhone = userPhoneNumber,
            isPhoneVerified = isPhoneVerified,
            isBusy = isBusy,
            onSubmit = { name, phone, district, role, shopName, shopAddress, nid ->
                viewModel.saveUserProfile(
                    name = name,
                    phone = phone,
                    district = district,
                    role = role,
                    shopName = shopName,
                    shopAddress = shopAddress,
                    nidOrLicense = nid,
                    isNewProfile = true,
                    onComplete = { skippedInitialReg = true }
                )
            }
        )
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomTabs = remember {
        listOf(
            BottomNavTab(Routes.HOME, "হোম", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            BottomNavTab(Routes.CATEGORIES, "ক্যাটাগরি", Icons.Filled.GridView, Icons.Outlined.GridView, "nav_categories"),
            BottomNavTab(Routes.OFFERS, "অফার", Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer, "nav_offers"),
            BottomNavTab(Routes.ORDERS, "আমার অর্ডার", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_orders"),
            BottomNavTab(Routes.PROFILE, "প্রোফাইল", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
        )
    }

    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 840.dp && showBottomBar

        Scaffold(
            containerColor = MintBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showBottomBar && !useNavRail) {
                    NavigationBar(
                        containerColor = PureWhite,
                        tonalElevation = 6.dp
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ForestGreen,
                                    selectedTextColor = ForestGreen,
                                    indicatorColor = SoftMintCard,
                                    unselectedIconColor = MutedSage,
                                    unselectedTextColor = MutedSage
                                ),
                                modifier = Modifier.testTag(tab.testTag)
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
                    NavigationRail(containerColor = PureWhite) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, maxLines = 1) }
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            products = filteredProducts,
                            favoriteIds = favoriteIds,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            cartCount = cartItems.sumOf { it.quantity }.toInt(),
                            unreadNotifications = unreadNotifications,
                            catalogError = catalogError,
                            onSearchQueryChange = viewModel::onSearchQueryChanged,
                            onCategoryClick = viewModel::onCategorySelected,
                            onSeeAllCategories = { navigateToTab(Routes.CATEGORIES) },
                            onSeeOffers = { navigateToTab(Routes.OFFERS) },
                            onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                            onOpenCart = { navController.navigate(Routes.CART) },
                            onProductClick = { prod -> navController.navigate("product_detail/${prod.id}") },
                            onAddToCart = { prod -> viewModel.addToCart(prod) },
                            onToggleFavorite = { prod -> viewModel.toggleFavorite(prod, prod.id in favoriteIds) }
                        )
                    }

                    composable(Routes.CATEGORIES) {
                        CategoriesScreen(
                            allProducts = allProducts,
                            selectedCategory = selectedCategory,
                            favoriteIds = favoriteIds,
                            onSelectCategory = viewModel::onCategorySelected,
                            onProductClick = { prod -> navController.navigate("product_detail/${prod.id}") },
                            onAddToCart = { prod -> viewModel.addToCart(prod) },
                            onToggleFavorite = { prod -> viewModel.toggleFavorite(prod, prod.id in favoriteIds) }
                        )
                    }

                    composable(Routes.OFFERS) {
                        OffersScreen(
                            products = allProducts,
                            favoriteIds = favoriteIds,
                            onProductClick = { prod -> navController.navigate("product_detail/${prod.id}") },
                            onAddToCart = { prod -> viewModel.addToCart(prod) },
                            onToggleFavorite = { prod -> viewModel.toggleFavorite(prod, prod.id in favoriteIds) }
                        )
                    }

                    composable(Routes.ORDERS) {
                        OrdersScreen(
                            orders = buyerOrders,
                            onCancelOrder = { ord -> viewModel.updateOrderStatus(ord.id, OrderStatus.CANCELLED) }
                        )
                    }

                    composable(Routes.PROFILE) {
                        ProfileScreen(
                            profile = userProfile,
                            userEmail = userEmail,
                            isAdmin = isAdmin,
                            favoriteCount = favorites.size,
                            onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                            onOpenFavorites = { navController.navigate(Routes.FAVORITES) },
                            onOpenSupport = { navController.navigate(Routes.SUPPORT) },
                            onOpenSellerDashboard = { navController.navigate(Routes.SELLER_DASHBOARD) },
                            onOpenAdminDashboard = { navController.navigate(Routes.ADMIN_DASHBOARD) },
                            onSignOut = {
                                GoogleAuthHelper.signOut(credentialManager, scope)
                            }
                        )
                    }

                    composable(Routes.PRODUCT_DETAIL) { entry ->
                        val productId = entry.arguments?.getString("productId") ?: ""
                        val product = allProducts.firstOrNull { it.id == productId }
                            ?: sellerProducts.firstOrNull { it.id == productId }
                        if (product != null) {
                            ProductDetailScreen(
                                product = product,
                                isFavorite = product.id in favoriteIds,
                                onBack = { navController.popBackStack() },
                                onAddToCart = { qty -> viewModel.addToCart(product, qty) },
                                onBuyNow = { qty ->
                                    viewModel.addToCart(product, qty)
                                    navController.navigate(Routes.CART)
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(product, product.id in favoriteIds) },
                                onReportProduct = { subj, msg ->
                                    viewModel.submitComplaint(
                                        userName = userProfile?.name ?: viewModel.defaultUserName(),
                                        userPhone = userProfile?.phone ?: "",
                                        subject = subj,
                                        message = msg,
                                        targetType = "PRODUCT",
                                        targetId = product.id
                                    )
                                }
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "পণ্যটি পাওয়া যায়নি বা অনুমোদনের অপেক্ষায় রয়েছে",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { navController.popBackStack() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                                ) {
                                    Text("ফিরে যান")
                                }
                            }
                        }
                    }

                    composable(Routes.CART) {
                        CartCheckoutScreen(
                            cartItems = cartItems,
                            userProfile = userProfile,
                            isBusy = isBusy,
                            onBack = { navController.popBackStack() },
                            onUpdateQuantity = viewModel::updateCartQuantity,
                            onPlaceOrder = { name, phone, address, payMethod ->
                                viewModel.placeOrders(
                                    buyerName = name,
                                    buyerPhone = phone,
                                    deliveryAddress = address,
                                    paymentMethod = payMethod,
                                    cartItems = cartItems,
                                    onSuccess = {
                                        navController.popBackStack()
                                        navigateToTab(Routes.ORDERS)
                                    }
                                )
                            }
                        )
                    }

                    composable(Routes.NOTIFICATIONS) {
                        NotificationsScreen(
                            notifications = notifications,
                            onBack = { navController.popBackStack() },
                            onMarkRead = viewModel::markNotificationRead
                        )
                    }

                    composable(Routes.FAVORITES) {
                        FavoritesScreen(
                            favorites = favorites,
                            onBack = { navController.popBackStack() },
                            onOpenProductById = { id -> navController.navigate("product_detail/$id") },
                            onRemoveFavorite = { fav ->
                                val match = allProducts.firstOrNull { it.id == fav.productId }
                                    ?: Product(id = fav.productId, name = fav.productName)
                                viewModel.toggleFavorite(match, currentlyFavorite = true)
                            }
                        )
                    }

                    composable(Routes.SUPPORT) {
                        SupportComplaintsScreen(
                            complaints = myComplaints,
                            isBusy = isBusy,
                            onBack = { navController.popBackStack() },
                            onSubmitComplaint = { subj, msg ->
                                viewModel.submitComplaint(
                                    userName = userProfile?.name ?: viewModel.defaultUserName(),
                                    userPhone = userProfile?.phone ?: "",
                                    subject = subj,
                                    message = msg
                                )
                            }
                        )
                    }

                    composable(Routes.EDIT_PROFILE) {
                        RegistrationScreen(
                            existingProfile = userProfile,
                            defaultName = viewModel.defaultUserName(),
                            defaultPhone = userPhoneNumber,
                            isPhoneVerified = isPhoneVerified,
                            isBusy = isBusy,
                            onSubmit = { name, phone, district, role, shopName, shopAddress, nid ->
                                viewModel.saveUserProfile(
                                    name = name,
                                    phone = phone,
                                    district = district,
                                    role = role,
                                    shopName = shopName,
                                    shopAddress = shopAddress,
                                    nidOrLicense = nid,
                                    isNewProfile = userProfile == null,
                                    onComplete = { navController.popBackStack() }
                                )
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.SELLER_DASHBOARD) {
                        SellerDashboardScreen(
                            profile = userProfile,
                            sellerProducts = sellerProducts,
                            sellerOrders = sellerOrders,
                            isBusy = isBusy,
                            onBack = { navController.popBackStack() },
                            onSaveProduct = { prodId, name, cat, price, origPrice, unit, stock, desc, safety, code, imgKey, imgB64, badge, onDone ->
                                viewModel.upsertSellerProduct(
                                    productId = prodId,
                                    shopName = userProfile?.shopName?.ifBlank { userProfile.name } ?: "কৃষি দোকান",
                                    sellerVerified = userProfile?.sellerVerified == true,
                                    name = name,
                                    category = cat,
                                    price = price,
                                    originalPrice = origPrice,
                                    unit = unit,
                                    stock = stock,
                                    description = desc,
                                    safetyGuide = safety,
                                    approvalCode = code,
                                    imageKey = imgKey,
                                    imageBase64 = imgB64,
                                    badge = badge,
                                    onComplete = onDone
                                )
                            },
                            onQuickUpdateStockPrice = viewModel::updateSellerStockAndPrice,
                            onUpdateOrderStatus = viewModel::updateOrderStatus
                        )
                    }

                    composable(Routes.ADMIN_DASHBOARD) {
                        AdminDashboardScreen(
                            users = (adminUsersState as? UiState.Success)?.data ?: emptyList(),
                            products = (adminProductsState as? UiState.Success)?.data ?: emptyList(),
                            orders = (adminOrdersState as? UiState.Success)?.data ?: emptyList(),
                            complaints = (adminComplaintsState as? UiState.Success)?.data ?: emptyList(),
                            isBusy = isBusy,
                            onBack = { navController.popBackStack() },
                            onVerifySeller = viewModel::adminVerifySeller,
                            onToggleAccountStatus = viewModel::adminToggleAccountStatus,
                            onModerateProduct = viewModel::adminModerateProduct,
                            onResolveComplaint = viewModel::adminResolveComplaint,
                            onUpdateOrderStatus = viewModel::updateOrderStatus,
                            onSeedStarterCatalog = viewModel::adminSeedStarterCatalog
                        )
                    }
                }
            }
        }
    }
}
