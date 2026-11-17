package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class UserRole(val bengaliLabel: String) {
    FARMER("কৃষক"),
    SELLER("দোকানদার");

    companion object {
        fun fromRaw(raw: String?): UserRole =
            entries.firstOrNull { it.name == raw } ?: FARMER
    }
}

enum class OrderStatus(val bengaliLabel: String) {
    PENDING("অপেক্ষমাণ"),
    CONFIRMED("নিশ্চিত"),
    SHIPPED("পথে আছে"),
    DELIVERED("ডেলিভারি সম্পন্ন"),
    CANCELLED("বাতিল");

    companion object {
        fun fromRaw(raw: String?): OrderStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class ProductStatus(val bengaliLabel: String) {
    APPROVED("অনুমোদিত"),
    PENDING("পর্যালোচনাধীন"),
    REMOVED("অপসারিত");

    companion object {
        fun fromRaw(raw: String?): ProductStatus =
            entries.firstOrNull { it.name == raw } ?: APPROVED
    }
}

enum class ComplaintStatus(val bengaliLabel: String) {
    OPEN("খোলা"),
    IN_REVIEW("পর্যালোচনাধীন"),
    RESOLVED("সমাধান হয়েছে");

    companion object {
        fun fromRaw(raw: String?): ComplaintStatus =
            entries.firstOrNull { it.name == raw } ?: OPEN
    }
}

data class AgroCategory(
    val name: String,
    val subtitle: String,
    val imageKey: String,
    val isRegulated: Boolean = false
)

object AgroCategories {
    val all = listOf(
        AgroCategory("কীটনাশক", "পোকামাকড় দমন", "insecticide", isRegulated = true),
        AgroCategory("ছত্রাকনাশক", "রোগ ও ছত্রাক দমন", "fungicide", isRegulated = true),
        AgroCategory("আগাছানাশক", "আগাছা নিয়ন্ত্রণ", "herbicide", isRegulated = true),
        AgroCategory("সার", "ফসলের পুষ্টি", "urea", isRegulated = false),
        AgroCategory("বীজ", "উন্নত ও হাইব্রিড বীজ", "seeds", isRegulated = false),
        AgroCategory("কৃষি সরঞ্জাম", "স্প্রেয়ার ও যন্ত্রপাতি", "tools", isRegulated = false),
        AgroCategory("সেচ সরঞ্জাম", "পাইপ ও ড্রিপ সেচ", "irrigation", isRegulated = false),
        AgroCategory("কৃষি সুরক্ষা সামগ্রী", "মাস্ক, গ্লাভস ও নিরাপত্তা", "safety", isRegulated = false)
    )

    val names = all.map { it.name }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val district: String = "",
    val role: String = UserRole.FARMER.name,
    val shopName: String = "",
    val shopAddress: String = "",
    val nidOrLicense: String = "",
    val sellerVerified: Boolean = false,
    val accountStatus: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val sellerId: String = "",
    val shopName: String = "",
    val sellerVerified: Boolean = false,
    val name: String = "",
    val category: String = "সার",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discountPercent: Long = 0L,
    val unit: String = "১টি",
    val stock: Long = 0L,
    val description: String = "",
    val safetyGuide: String = "",
    val approvalCode: String = "",
    val imageKey: String = "urea",
    val imageBase64: String = "",
    val badge: String = "",
    val status: String = ProductStatus.APPROVED.name,
    val featured: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CartItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val productName: String = "",
    val shopName: String = "",
    val sellerId: String = "",
    val unit: String = "",
    val price: Double = 0.0,
    val quantity: Long = 1L,
    val imageKey: String = "urea",
    val imageBase64: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FavoriteItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val productName: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val unit: String = "",
    val shopName: String = "",
    val sellerVerified: Boolean = false,
    val imageKey: String = "urea",
    val imageBase64: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class OrderLineItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Long = 1L,
    val unit: String = ""
)

@Keep
@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerPhone: String = "",
    val deliveryAddress: String = "",
    val sellerId: String = "",
    val shopName: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "ক্যাশ অন ডেলিভারি",
    val status: String = OrderStatus.PENDING.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun parsedItems(): List<OrderLineItem> = items.map { map ->
        OrderLineItem(
            productId = map["productId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            price = (map["price"] as? Number)?.toDouble() ?: 0.0,
            quantity = (map["quantity"] as? Number)?.toLong() ?: 1L,
            unit = map["unit"] as? String ?: ""
        )
    }
}

@Keep
@IgnoreExtraProperties
data class Complaint(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val subject: String = "",
    val message: String = "",
    val targetType: String = "GENERAL",
    val targetId: String = "",
    val status: String = ComplaintStatus.OPEN.name,
    val adminNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "SYSTEM",
    val readStatus: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)
