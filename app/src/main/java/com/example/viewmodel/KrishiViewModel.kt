package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.KrishiRepository
import com.example.data.StarterCatalog
import com.example.model.CartItem
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.FavoriteItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductStatus
import com.example.model.UserNotification
import com.example.model.UserProfile
import com.example.model.UserRole
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class KrishiViewModel(
    private val repository: KrishiRepository,
    val currentUserId: String,
    private val userEmail: String?,
    private val userDisplayName: String?
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _isAdmin = MutableStateFlow(userEmail.equals(BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true))
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastEvents = _toastEvents.asSharedFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    init {
        viewModelScope.launch {
            val docAdmin = repository.checkAdminDocument(currentUserId)
            if (docAdmin || userEmail.equals(BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true)) {
                _isAdmin.value = true
            }
        }
    }

    private fun <T> Flow<T>.toSafeUiState(tag: String, fallbackError: String): StateFlow<UiState<T>> {
        return map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "$tag stream error", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: fallbackError))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )
    }

    val userProfileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId)
            .toSafeUiState("Profile", "প্রোফাইল লোড করতে সমস্যা হয়েছে।")

    private val liveCatalogFlow: Flow<List<Product>> =
        repository.observeApprovedProducts(currentUserId)
            .map { firestoreProducts ->
                val firestoreIds = firestoreProducts.map { it.id }.toSet()
                val starterMissing = StarterCatalog.products.filter { it.id !in firestoreIds }
                val combined = firestoreProducts + starterMissing
                combined.sortedByDescending { it.featured }
            }

    val catalogState: StateFlow<UiState<List<Product>>> =
        liveCatalogFlow.toSafeUiState("Catalog", "পণ্য তালিকা লোড করা যায়নি।")

    val filteredProducts: StateFlow<List<Product>> = combine(
        catalogState,
        _searchQuery,
        _selectedCategory
    ) { state, query, category ->
        val list = (state as? UiState.Success)?.data ?: StarterCatalog.products
        val q = query.trim().lowercase()
        list.filter { p ->
            val catOk = category == null || p.category == category
            val queryOk = q.isEmpty() ||
                p.name.lowercase().contains(q) ||
                p.category.lowercase().contains(q) ||
                p.shopName.lowercase().contains(q) ||
                p.description.lowercase().contains(q)
            catOk && queryOk
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), StarterCatalog.products)

    val cartState: StateFlow<UiState<List<CartItem>>> =
        repository.observeCart(currentUserId)
            .toSafeUiState("Cart", "কার্ট লোড করা যায়নি।")

    val favoritesState: StateFlow<UiState<List<FavoriteItem>>> =
        repository.observeFavorites(currentUserId)
            .toSafeUiState("Favorites", "পছন্দের তালিকা লোড করা যায়নি।")

    val notificationsState: StateFlow<UiState<List<UserNotification>>> =
        repository.observeNotifications(currentUserId)
            .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
            .toSafeUiState("Notifications", "নোটিফিকেশন লোড করা যায়নি।")

    val buyerOrdersState: StateFlow<UiState<List<Order>>> =
        repository.observeBuyerOrders(currentUserId)
            .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
            .toSafeUiState("BuyerOrders", "অর্ডার ইতিহাস লোড করা যায়নি।")

    val sellerProductsState: StateFlow<UiState<List<Product>>> =
        repository.observeSellerProducts(currentUserId)
            .map { list -> list.sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) } }
            .toSafeUiState("SellerProducts", "দোকানের পণ্য লোড করা যায়নি।")

    val sellerOrdersState: StateFlow<UiState<List<Order>>> =
        repository.observeSellerOrders(currentUserId)
            .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
            .toSafeUiState("SellerOrders", "দোকানের অর্ডার লোড করা যায়নি।")

    val myComplaintsState: StateFlow<UiState<List<Complaint>>> =
        repository.observeMyComplaints(currentUserId)
            .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
            .toSafeUiState("MyComplaints", "অভিযোগ লোড করা যায়নি।")

    // Admin streams — strictly gated behind isAdmin == true
    val adminUsersState: StateFlow<UiState<List<UserProfile>>> =
        _isAdmin.flatMapLatest { admin ->
            if (admin) repository.observeAllUsersAdmin(currentUserId)
            else flowOf(emptyList())
        }.toSafeUiState("AdminUsers", "ব্যবহারকারী তালিকা লোড করা যায়নি।")

    val adminProductsState: StateFlow<UiState<List<Product>>> =
        _isAdmin.flatMapLatest { admin ->
            if (admin) repository.observeAllProductsAdmin(currentUserId)
            else flowOf(emptyList())
        }.toSafeUiState("AdminProducts", "সকল পণ্য লোড করা যায়নি।")

    val adminOrdersState: StateFlow<UiState<List<Order>>> =
        _isAdmin.flatMapLatest { admin ->
            if (admin) repository.observeAllOrdersAdmin(currentUserId)
                .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
            else flowOf(emptyList())
        }.toSafeUiState("AdminOrders", "সকল অর্ডার লোড করা যায়নি।")

    val adminComplaintsState: StateFlow<UiState<List<Complaint>>> =
        _isAdmin.flatMapLatest { admin ->
            if (admin) repository.observeAllComplaintsAdmin(currentUserId)
                .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
            else flowOf(emptyList())
        }.toSafeUiState("AdminComplaints", "সকল অভিযোগ লোড করা যায়নি।")

    fun defaultUserName(): String = userDisplayName?.takeIf { it.isNotBlank() } ?: ""

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun emitMessage(msg: String) {
        _toastEvents.tryEmit(msg)
    }

    fun saveUserProfile(
        name: String,
        phone: String,
        district: String,
        role: UserRole,
        shopName: String,
        shopAddress: String,
        nidOrLicense: String,
        isNewProfile: Boolean,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.saveUserProfile(
                uid = currentUserId,
                name = name,
                phone = phone,
                district = district,
                role = role,
                shopName = shopName,
                shopAddress = shopAddress,
                nidOrLicense = nidOrLicense,
                isNewProfile = isNewProfile
            )
            _isBusy.value = false
            res.onSuccess {
                _toastEvents.emit("প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে।")
                onComplete()
            }.onFailure { err ->
                _toastEvents.emit(err.localizedMessage ?: "প্রোফাইল সংরক্ষণ করা যায়নি।")
            }
        }
    }

    fun addToCart(product: Product, qty: Long = 1L) {
        viewModelScope.launch {
            val res = repository.addToCart(currentUserId, product, qty)
            res.onSuccess {
                _toastEvents.emit("${product.name} কার্টে যোগ করা হয়েছে।")
            }.onFailure { err ->
                _toastEvents.emit(err.localizedMessage ?: "কার্টে যোগ করা যায়নি।")
            }
        }
    }

    fun updateCartQuantity(productId: String, newQty: Long) {
        viewModelScope.launch {
            repository.updateCartQuantity(currentUserId, productId, newQty)
        }
    }

    fun toggleFavorite(product: Product, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            val res = repository.toggleFavorite(currentUserId, product, currentlyFavorite)
            res.onSuccess {
                val msg = if (currentlyFavorite) "পছন্দের তালিকা থেকে সরানো হয়েছে।" else "পছন্দের তালিকায় সংরক্ষিত হয়েছে।"
                _toastEvents.emit(msg)
            }.onFailure {
                _toastEvents.emit("পছন্দের তালিকা আপডেট করা যায়নি।")
            }
        }
    }

    fun placeOrders(
        buyerName: String,
        buyerPhone: String,
        deliveryAddress: String,
        paymentMethod: String,
        cartItems: List<CartItem>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.placeOrdersFromCart(
                uid = currentUserId,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                deliveryAddress = deliveryAddress,
                paymentMethod = paymentMethod,
                cartItems = cartItems
            )
            _isBusy.value = false
            res.onSuccess {
                _toastEvents.emit("আপনার অর্ডার সফলভাবে সম্পন্ন হয়েছে!")
                onSuccess()
            }.onFailure { err ->
                _toastEvents.emit(err.localizedMessage ?: "অর্ডার সম্পন্ন করা যায়নি।")
            }
        }
    }

    fun updateOrderStatus(orderId: String, nextStatus: OrderStatus) {
        viewModelScope.launch {
            val res = repository.updateOrderStatus(orderId, nextStatus)
            res.onSuccess {
                _toastEvents.emit("অর্ডারের অবস্থা আপডেট হয়েছে: ${nextStatus.bengaliLabel}")
            }.onFailure { err ->
                _toastEvents.emit(err.localizedMessage ?: "অর্ডারের অবস্থা পরিবর্তন করা যায়নি।")
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(currentUserId, notificationId)
        }
    }

    fun upsertSellerProduct(
        productId: String?,
        shopName: String,
        sellerVerified: Boolean,
        name: String,
        category: String,
        price: Double,
        originalPrice: Double,
        unit: String,
        stock: Long,
        description: String,
        safetyGuide: String,
        approvalCode: String,
        imageKey: String,
        imageBase64: String,
        badge: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.upsertSellerProduct(
                productId = productId,
                sellerId = currentUserId,
                shopName = shopName,
                sellerVerified = sellerVerified,
                name = name,
                category = category,
                price = price,
                originalPrice = originalPrice,
                unit = unit,
                stock = stock,
                description = description,
                safetyGuide = safetyGuide,
                approvalCode = approvalCode,
                imageKey = imageKey,
                imageBase64 = imageBase64,
                badge = badge
            )
            _isBusy.value = false
            res.onSuccess {
                _toastEvents.emit(if (productId == null) "নতুন পণ্য যুক্ত করা হয়েছে।" else "পণ্য আপডেট হয়েছে।")
                onComplete()
            }.onFailure { err ->
                _toastEvents.emit(err.localizedMessage ?: "পণ্য সংরক্ষণ করা যায়নি।")
            }
        }
    }

    fun updateSellerStockAndPrice(productId: String, newPrice: Double, newStock: Long) {
        viewModelScope.launch {
            val res = repository.updateProductStockAndPrice(productId, newPrice, newStock)
            res.onSuccess {
                _toastEvents.emit("দাম ও স্টক আপডেট করা হয়েছে।")
            }.onFailure { err ->
                _toastEvents.emit(err.localizedMessage ?: "আপডেট ব্যর্থ হয়েছে।")
            }
        }
    }

    fun submitComplaint(
        userName: String,
        userPhone: String,
        subject: String,
        message: String,
        targetType: String = "GENERAL",
        targetId: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.submitComplaint(
                uid = currentUserId,
                userName = userName,
                userPhone = userPhone,
                subject = subject,
                message = message,
                targetType = targetType,
                targetId = targetId
            )
            _isBusy.value = false
            res.onSuccess {
                _toastEvents.emit("আপনার অভিযোগ / বার্তা পাঠানো হয়েছে।")
                onComplete()
            }.onFailure { err ->
                _toastEvents.emit(err.localizedMessage ?: "পাঠানো যায়নি।")
            }
        }
    }

    // Admin actions
    fun adminVerifySeller(targetUserId: String, verified: Boolean) {
        viewModelScope.launch {
            val res = repository.adminVerifySeller(targetUserId, verified)
            res.onSuccess {
                _toastEvents.emit(if (verified) "বিক্রেতা যাচাইকৃত হিসেবে চিহ্নিত হয়েছেন।" else "বিক্রেতা যাচাই বাতিল হয়েছে।")
            }.onFailure {
                _toastEvents.emit("যাচাই আপডেট করা যায়নি।")
            }
        }
    }

    fun adminToggleAccountStatus(targetUserId: String, currentStatus: String) {
        val next = if (currentStatus == "ACTIVE") "SUSPENDED" else "ACTIVE"
        viewModelScope.launch {
            val res = repository.adminSetAccountStatus(targetUserId, next)
            res.onSuccess {
                _toastEvents.emit(if (next == "ACTIVE") "অ্যাকাউন্ট সক্রিয় করা হয়েছে।" else "অ্যাকাউন্ট স্থগিত করা হয়েছে।")
            }
        }
    }

    fun adminModerateProduct(productId: String, status: ProductStatus) {
        viewModelScope.launch {
            val res = repository.adminModerateProduct(productId, status)
            res.onSuccess {
                _toastEvents.emit("পণ্যের অবস্থা: ${status.bengaliLabel}")
            }
        }
    }

    fun adminResolveComplaint(complaintId: String, status: ComplaintStatus, adminNote: String) {
        viewModelScope.launch {
            val res = repository.adminResolveComplaint(complaintId, status, adminNote)
            res.onSuccess {
                _toastEvents.emit("অভিযোগ আপডেট হয়েছে: ${status.bengaliLabel}")
            }
        }
    }

    fun adminSeedStarterCatalog() {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.adminSeedStarterCatalog()
            _isBusy.value = false
            res.onSuccess {
                _toastEvents.emit("প্রাথমিক অনুমোদিত পণ্যসমূহ ডেটাবেজে সংরক্ষিত হয়েছে।")
            }.onFailure {
                _toastEvents.emit("পণ্য লোড করা যায়নি।")
            }
        }
    }

    private companion object {
        const val TAG = "KrishiVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
        const val BOOTSTRAP_ADMIN_EMAIL = "mdparvazk321@gmail.com"
    }
}
