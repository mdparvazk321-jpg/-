package com.example.data

import android.content.Context
import com.example.R
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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class KrishiRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("ব্যবহারকারী লগইন করা নেই।")
    }

    private fun safeId(prefix: String): String =
        "${prefix}_${UUID.randomUUID().toString().replace("-", "").take(16)}"

    private fun handleStreamError(error: Throwable, op: OperationType, path: String, expectedUid: String): Nothing {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
        throw error
    }

    // --- USER PROFILE ---

    fun observeUserProfile(uid: String): Flow<UserProfile?> = flow {
        require(uid.isNotBlank()) { "User ID required" }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error -> handleStreamError(error, OperationType.GET, path, uid) }
        )
    }

    suspend fun checkAdminDocument(uid: String): Boolean {
        return try {
            val snap = db.collection("admins").document(uid).get().await()
            snap.exists()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    suspend fun saveUserProfile(
        uid: String,
        name: String,
        phone: String,
        district: String,
        role: UserRole,
        shopName: String,
        shopAddress: String,
        nidOrLicense: String,
        isNewProfile: Boolean
    ): Result<Unit> {
        val path = "users/$uid"
        return try {
            val currentUid = requireUserId()
            check(currentUid == uid) { "অন্য ব্যবহারকারীর প্রোফাইল পরিবর্তন করা যাবে না।" }
            val cleanName = name.trim()
            val cleanPhone = phone.trim()
            val cleanDistrict = district.trim()
            require(cleanName.length in 2..80) { "নাম ২ থেকে ৮০ অক্ষরের মধ্যে হতে হবে।" }
            require(cleanPhone.length in 6..20) { "সঠিক মোবাইল নম্বর দিন।" }
            require(cleanDistrict.length in 2..60) { "জেলার নাম লিখুন।" }
            val docRef = db.collection("users").document(uid)
            if (isNewProfile) {
                val payload = mapOf(
                    "userId" to uid,
                    "name" to cleanName,
                    "phone" to cleanPhone,
                    "district" to cleanDistrict,
                    "role" to role.name,
                    "shopName" to shopName.trim().take(100),
                    "shopAddress" to shopAddress.trim().take(200),
                    "nidOrLicense" to nidOrLicense.trim().take(60),
                    "sellerVerified" to false,
                    "accountStatus" to "ACTIVE",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                createWelcomeNotification(uid, cleanName)
            } else {
                docRef.update(
                    mapOf(
                        "name" to cleanName,
                        "phone" to cleanPhone,
                        "district" to cleanDistrict,
                        "role" to role.name,
                        "shopName" to shopName.trim().take(100),
                        "shopAddress" to shopAddress.trim().take(200),
                        "nidOrLicense" to nidOrLicense.trim().take(60),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNewProfile) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- PRODUCTS ---

    fun observeApprovedProducts(uid: String): Flow<List<Product>> = flow {
        val path = "products"
        emitAll(
            db.collection("products")
                .whereEqualTo("status", ProductStatus.APPROVED.name)
                .snapshots()
                .map { snap -> snap.toObjects(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    fun observeSellerProducts(uid: String): Flow<List<Product>> = flow {
        val path = "products"
        emitAll(
            db.collection("products")
                .whereEqualTo("sellerId", uid)
                .snapshots()
                .map { snap -> snap.toObjects(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    suspend fun upsertSellerProduct(
        productId: String?,
        sellerId: String,
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
        badge: String
    ): Result<Unit> {
        val targetId = productId ?: safeId("prod")
        val path = "products/$targetId"
        return try {
            val uid = requireUserId()
            check(uid == sellerId) { "অন্য বিক্রেতার পণ্য পরিবর্তন করা যাবে না।" }
            val cleanName = name.trim()
            val cleanShop = shopName.trim().ifBlank { "কৃষি দোকান" }
            require(cleanName.length in 2..120) { "পণ্যের নাম ২-১২০ অক্ষরের মধ্যে দিন।" }
            require(price > 0.0 && price <= 10_000_000.0) { "সঠিক মূল্য দিন।" }
            val cleanOriginal = maxOf(price, originalPrice)
            val discount = if (cleanOriginal > price) {
                (((cleanOriginal - price) / cleanOriginal) * 100.0).toLong().coerceIn(0L, 90L)
            } else 0L
            require(stock in 0L..1_000_000L) { "স্টক সংখ্যা সঠিক নয়।" }
            require(imageBase64.length <= 150_000) { "ছবির আকার অনেক বড়, ছোট ছবি দিন।" }

            val docRef = db.collection("products").document(targetId)
            if (productId == null) {
                val payload = mapOf(
                    "sellerId" to uid,
                    "shopName" to cleanShop.take(100),
                    "sellerVerified" to sellerVerified,
                    "name" to cleanName,
                    "category" to category,
                    "price" to price,
                    "originalPrice" to cleanOriginal,
                    "discountPercent" to discount,
                    "unit" to unit.trim().ifBlank { "১টি" }.take(40),
                    "stock" to stock,
                    "description" to description.trim().take(1000),
                    "safetyGuide" to safetyGuide.trim().ifBlank {
                        "অনুমোদিত মাত্রায় এবং প্রযোজ্য নিরাপত্তা নির্দেশনা মেনে ব্যবহার করুন।"
                    }.take(1000),
                    "approvalCode" to approvalCode.trim().take(60),
                    "imageKey" to imageKey.ifBlank { "urea" }.take(40),
                    "imageBase64" to imageBase64,
                    "badge" to badge.trim().take(40),
                    "status" to ProductStatus.APPROVED.name,
                    "featured" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                docRef.update(
                    mapOf(
                        "name" to cleanName,
                        "category" to category,
                        "price" to price,
                        "originalPrice" to cleanOriginal,
                        "discountPercent" to discount,
                        "unit" to unit.trim().ifBlank { "১টি" }.take(40),
                        "stock" to stock,
                        "description" to description.trim().take(1000),
                        "safetyGuide" to safetyGuide.trim().take(1000),
                        "approvalCode" to approvalCode.trim().take(60),
                        "imageKey" to imageKey.ifBlank { "urea" }.take(40),
                        "imageBase64" to imageBase64,
                        "badge" to badge.trim().take(40),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (productId == null) OperationType.CREATE else OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProductStockAndPrice(productId: String, newPrice: Double, newStock: Long): Result<Unit> {
        val path = "products/$productId"
        return try {
            requireUserId()
            require(newPrice > 0.0 && newPrice <= 10_000_000.0) { "সঠিক দাম দিন।" }
            require(newStock in 0L..1_000_000L) { "সঠিক স্টক সংখ্যা দিন।" }
            db.collection("products").document(productId).update(
                mapOf(
                    "price" to newPrice,
                    "originalPrice" to newPrice,
                    "discountPercent" to 0L,
                    "stock" to newStock,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- CART & FAVORITES ---

    fun observeCart(uid: String): Flow<List<CartItem>> = flow {
        val path = "users/$uid/cart"
        emitAll(
            db.collection("users").document(uid).collection("cart")
                .snapshots()
                .map { snap -> snap.toObjects(CartItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    suspend fun addToCart(uid: String, product: Product, quantityDelta: Long = 1L): Result<Unit> {
        val docId = product.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { safeId("item") }
        val path = "users/$uid/cart/$docId"
        return try {
            val currentUid = requireUserId()
            check(currentUid == uid)
            val docRef = db.collection("users").document(uid).collection("cart").document(docId)
            val existing = docRef.get().await()
            if (existing.exists()) {
                val currentQty = existing.getLong("quantity") ?: 1L
                val nextQty = (currentQty + quantityDelta).coerceIn(1L, 999L)
                docRef.update(
                    mapOf(
                        "quantity" to nextQty,
                        "price" to product.price,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "productId" to docId,
                        "productName" to product.name.take(120),
                        "shopName" to product.shopName.take(100),
                        "sellerId" to product.sellerId.take(128),
                        "unit" to product.unit.take(40),
                        "price" to product.price,
                        "quantity" to quantityDelta.coerceIn(1L, 999L),
                        "imageKey" to product.imageKey.take(40),
                        "imageBase64" to product.imageBase64.take(150_000),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateCartQuantity(uid: String, productId: String, newQuantity: Long): Result<Unit> {
        val path = "users/$uid/cart/$productId"
        return try {
            val currentUid = requireUserId()
            check(currentUid == uid)
            val docRef = db.collection("users").document(uid).collection("cart").document(productId)
            if (newQuantity <= 0L) {
                docRef.delete().await()
            } else {
                docRef.update(
                    mapOf(
                        "quantity" to newQuantity.coerceIn(1L, 999L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeFavorites(uid: String): Flow<List<FavoriteItem>> = flow {
        val path = "users/$uid/favorites"
        emitAll(
            db.collection("users").document(uid).collection("favorites")
                .snapshots()
                .map { snap -> snap.toObjects(FavoriteItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    suspend fun toggleFavorite(uid: String, product: Product, currentlyFavorite: Boolean): Result<Unit> {
        val docId = product.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { safeId("fav") }
        val path = "users/$uid/favorites/$docId"
        return try {
            val currentUid = requireUserId()
            check(currentUid == uid)
            val docRef = db.collection("users").document(uid).collection("favorites").document(docId)
            if (currentlyFavorite) {
                docRef.delete().await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "productId" to docId,
                        "productName" to product.name.take(120),
                        "category" to product.category.take(40),
                        "price" to product.price,
                        "unit" to product.unit.take(40),
                        "shopName" to product.shopName.take(100),
                        "sellerVerified" to product.sellerVerified,
                        "imageKey" to product.imageKey.take(40),
                        "imageBase64" to product.imageBase64.take(150_000),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- ORDERS ---

    fun observeBuyerOrders(uid: String): Flow<List<Order>> = flow {
        val path = "orders"
        emitAll(
            db.collection("orders")
                .whereEqualTo("buyerId", uid)
                .snapshots()
                .map { snap -> snap.toObjects(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    fun observeSellerOrders(uid: String): Flow<List<Order>> = flow {
        val path = "orders"
        emitAll(
            db.collection("orders")
                .whereEqualTo("sellerId", uid)
                .snapshots()
                .map { snap -> snap.toObjects(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    suspend fun placeOrdersFromCart(
        uid: String,
        buyerName: String,
        buyerPhone: String,
        deliveryAddress: String,
        paymentMethod: String,
        cartItems: List<CartItem>
    ): Result<Int> {
        val path = "orders"
        return try {
            val currentUid = requireUserId()
            check(currentUid == uid)
            require(cartItems.isNotEmpty()) { "কার্টে কোনো পণ্য নেই।" }
            val cleanName = buyerName.trim().ifBlank { "কৃষক ক্রেতা" }.take(80)
            val cleanPhone = buyerPhone.trim()
            val cleanAddress = deliveryAddress.trim()
            require(cleanPhone.length in 6..20) { "সঠিক মোবাইল নম্বর দিন।" }
            require(cleanAddress.length in 4..250) { "ডেলিভারির ঠিকানা লিখুন।" }

            val groupedBySeller = cartItems.groupBy { it.sellerId.ifBlank { "official_verified_dealer" } }
            val batch = db.batch()
            var createdOrders = 0
            for ((sellerId, items) in groupedBySeller) {
                val orderId = safeId("ord")
                val orderRef = db.collection("orders").document(orderId)
                val lineMaps = items.take(20).map { item ->
                    mapOf(
                        "productId" to item.productId,
                        "name" to item.productName,
                        "price" to item.price,
                        "quantity" to item.quantity,
                        "unit" to item.unit
                    )
                }
                val total = items.sumOf { it.price * it.quantity }
                val shopName = items.firstOrNull()?.shopName?.ifBlank { "কৃষি বাজার ডিলার" } ?: "কৃষি বাজার ডিলার"
                batch.set(
                    orderRef,
                    mapOf(
                        "buyerId" to uid,
                        "buyerName" to cleanName,
                        "buyerPhone" to cleanPhone,
                        "deliveryAddress" to cleanAddress,
                        "sellerId" to sellerId,
                        "shopName" to shopName.take(100),
                        "items" to lineMaps,
                        "totalAmount" to total,
                        "paymentMethod" to paymentMethod.take(60),
                        "status" to OrderStatus.PENDING.name,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                createdOrders++
            }
            for (item in cartItems) {
                val cartId = item.id.ifBlank { item.productId }
                if (cartId.isNotBlank()) {
                    batch.delete(db.collection("users").document(uid).collection("cart").document(cartId))
                }
            }
            val notifId = safeId("notif")
            batch.set(
                db.collection("users").document(uid).collection("notifications").document(notifId),
                mapOf(
                    "userId" to uid,
                    "title" to "নতুন অর্ডার সম্পন্ন হয়েছে",
                    "body" to "আপনার ${createdOrders.toBengaliDigits()}টি অর্ডার সফলভাবে গৃহীত হয়েছে।",
                    "type" to "ORDER",
                    "readStatus" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(createdOrders)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, nextStatus: OrderStatus): Result<Unit> {
        val path = "orders/$orderId"
        return try {
            requireUserId()
            require(orderId.isNotBlank())
            db.collection("orders").document(orderId).update(
                mapOf(
                    "status" to nextStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- NOTIFICATIONS ---

    fun observeNotifications(uid: String): Flow<List<UserNotification>> = flow {
        val path = "users/$uid/notifications"
        emitAll(
            db.collection("users").document(uid).collection("notifications")
                .snapshots()
                .map { snap -> snap.toObjects(UserNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    suspend fun markNotificationRead(uid: String, notificationId: String): Result<Unit> {
        val path = "users/$uid/notifications/$notificationId"
        return try {
            val currentUid = requireUserId()
            check(currentUid == uid)
            db.collection("users").document(uid).collection("notifications").document(notificationId)
                .update(mapOf("readStatus" to true))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    private suspend fun createWelcomeNotification(uid: String, name: String) {
        val id = safeId("welcome")
        db.collection("users").document(uid).collection("notifications").document(id)
            .set(
                mapOf(
                    "userId" to uid,
                    "title" to "কৃষি বাজারে স্বাগতম, $name!",
                    "body" to "যাচাইকৃত কৃষি ডিলারের কাছ থেকে ন্যায্য দামে অনুমোদিত বীজ, সার ও সরঞ্জাম অর্ডার করুন।",
                    "type" to "SYSTEM",
                    "readStatus" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // --- COMPLAINTS / SUPPORT ---

    fun observeMyComplaints(uid: String): Flow<List<Complaint>> = flow {
        val path = "complaints"
        emitAll(
            db.collection("complaints")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snap -> snap.toObjects(Complaint::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    suspend fun submitComplaint(
        uid: String,
        userName: String,
        userPhone: String,
        subject: String,
        message: String,
        targetType: String = "GENERAL",
        targetId: String = ""
    ): Result<Unit> {
        val id = safeId("comp")
        val path = "complaints/$id"
        return try {
            val currentUid = requireUserId()
            check(currentUid == uid)
            val cleanSub = subject.trim()
            val cleanMsg = message.trim()
            require(cleanSub.length in 2..120) { "বিষয় ২-১২০ অক্ষরের মধ্যে লিখুন।" }
            require(cleanMsg.length in 4..1000) { "বিস্তারিত বার্তা লিখুন।" }
            db.collection("complaints").document(id).set(
                mapOf(
                    "userId" to uid,
                    "userName" to userName.trim().ifBlank { "ব্যবহারকারী" }.take(80),
                    "userPhone" to userPhone.trim().take(20),
                    "subject" to cleanSub,
                    "message" to cleanMsg,
                    "targetType" to targetType,
                    "targetId" to targetId.take(128),
                    "status" to ComplaintStatus.OPEN.name,
                    "adminNote" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- ADMIN STREAMS & ACTIONS (Only attached when isAdmin == true) ---

    fun observeAllUsersAdmin(uid: String): Flow<List<UserProfile>> = flow {
        val path = "users"
        emitAll(
            db.collection("users")
                .snapshots()
                .map { snap -> snap.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    fun observeAllProductsAdmin(uid: String): Flow<List<Product>> = flow {
        val path = "products"
        emitAll(
            db.collection("products")
                .snapshots()
                .map { snap -> snap.toObjects(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    fun observeAllOrdersAdmin(uid: String): Flow<List<Order>> = flow {
        val path = "orders"
        emitAll(
            db.collection("orders")
                .snapshots()
                .map { snap -> snap.toObjects(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    fun observeAllComplaintsAdmin(uid: String): Flow<List<Complaint>> = flow {
        val path = "complaints"
        emitAll(
            db.collection("complaints")
                .snapshots()
                .map { snap -> snap.toObjects(Complaint::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error -> handleStreamError(error, OperationType.LIST, path, uid) }
        )
    }

    suspend fun adminVerifySeller(targetUserId: String, verified: Boolean): Result<Unit> {
        val path = "users/$targetUserId"
        return try {
            requireUserId()
            db.collection("users").document(targetUserId).update(
                mapOf(
                    "sellerVerified" to verified,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminSetAccountStatus(targetUserId: String, status: String): Result<Unit> {
        val path = "users/$targetUserId"
        return try {
            requireUserId()
            db.collection("users").document(targetUserId).update(
                mapOf(
                    "accountStatus" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminModerateProduct(productId: String, status: ProductStatus): Result<Unit> {
        val path = "products/$productId"
        return try {
            requireUserId()
            db.collection("products").document(productId).update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminResolveComplaint(complaintId: String, status: ComplaintStatus, adminNote: String): Result<Unit> {
        val path = "complaints/$complaintId"
        return try {
            requireUserId()
            db.collection("complaints").document(complaintId).update(
                mapOf(
                    "status" to status.name,
                    "adminNote" to adminNote.trim().take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun adminSeedStarterCatalog(): Result<Unit> {
        val path = "products"
        return try {
            requireUserId()
            val batch = db.batch()
            for (p in StarterCatalog.products) {
                val docRef = db.collection("products").document(p.id)
                batch.set(
                    docRef,
                    mapOf(
                        "sellerId" to p.sellerId,
                        "shopName" to p.shopName,
                        "sellerVerified" to p.sellerVerified,
                        "name" to p.name,
                        "category" to p.category,
                        "price" to p.price,
                        "originalPrice" to p.originalPrice,
                        "discountPercent" to p.discountPercent,
                        "unit" to p.unit,
                        "stock" to p.stock,
                        "description" to p.description,
                        "safetyGuide" to p.safetyGuide,
                        "approvalCode" to p.approvalCode,
                        "imageKey" to p.imageKey,
                        "imageBase64" to p.imageBase64,
                        "badge" to p.badge,
                        "status" to ProductStatus.APPROVED.name,
                        "featured" to p.featured,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }
}
