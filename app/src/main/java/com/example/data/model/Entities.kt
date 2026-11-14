package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

object UserRoles {
    const val BUYER = "BUYER"
    const val SELLER = "SELLER"
    const val ADMIN = "ADMIN"
}

object ApprovalStatus {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
    const val SUSPENDED = "SUSPENDED"
}

object OrderStatuses {
    val ALL = listOf(
        "Order Placed",
        "Confirmed",
        "Processing",
        "Shipped",
        "Out for Delivery",
        "Delivered"
    )
    const val PLACED = "Order Placed"
    const val CONFIRMED = "Confirmed"
    const val PROCESSING = "Processing"
    const val SHIPPED = "Shipped"
    const val OUT_FOR_DELIVERY = "Out for Delivery"
    const val DELIVERED = "Delivered"
}

object PaymentMethods {
    const val BKASH = "bKash"
    const val NAGAD = "Nagad"
    const val ROCKET = "Rocket"
    const val COD = "Cash on Delivery"
    const val CARD = "Card Payment"

    val ALL = listOf(BKASH, NAGAD, ROCKET, COD, CARD)
}

object PaymentStatuses {
    const val COD_DUE = "Pay on Delivery (COD)"
    const val PENDING_VERIFICATION = "Pending Verification"
    const val PAID = "Paid & Verified"
    const val REJECTED = "Verification Rejected"
}

object ReturnStatuses {
    val ALL = listOf(
        "Requested",
        "Approved",
        "Pickup Scheduled",
        "Refund Processing",
        "Refunded",
        "Rejected"
    )
    const val REQUESTED = "Requested"
    const val APPROVED = "Approved"
    const val PICKUP_SCHEDULED = "Pickup Scheduled"
    const val REFUND_PROCESSING = "Refund Processing"
    const val REFUNDED = "Refunded"
    const val REJECTED = "Rejected"
}

object BangladeshDivisions {
    val ALL = listOf(
        "Dhaka", "Chattogram", "Sylhet", "Rajshahi",
        "Khulna", "Barishal", "Rangpur", "Mymensingh"
    )
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String = "",
    val passwordHash: String,
    val salt: String,
    val role: String = UserRoles.BUYER,
    val isPhoneVerified: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sellers")
data class SellerEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val shopName: String,
    val ownerName: String,
    val phone: String,
    val pickupAddress: String,
    val tradeLicenseOrNid: String,
    val payoutMethod: String,
    val payoutAccount: String,
    val status: String = ApprovalStatus.PENDING,
    val totalEarnings: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val rating: Double = 4.8,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val accentHex: Long,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val sellerId: String,
    val sellerName: String,
    val categoryId: String,
    val categoryName: String,
    val name: String,
    val description: String,
    val originalPrice: Double,
    val discountedPrice: Double,
    val discountPercent: Int,
    val rating: Double = 4.6,
    val reviewCount: Int = 0,
    val colorsCsv: String = "Midnight Navy,Pearl White",
    val sizesCsv: String = "Standard",
    val stock: Int = 25,
    val soldCount: Int = 0,
    val flashSaleTargetStock: Int = 100,
    val isFlashSale: Boolean = false,
    val flashSaleEndMillis: Long = 0L,
    val isTrending: Boolean = false,
    val isBestSeller: Boolean = false,
    val isNewArrival: Boolean = false,
    val isRecommended: Boolean = false,
    val approvalStatus: String = ApprovalStatus.APPROVED,
    val imageResKey: String = "electronics",
    val customImageUri: String? = null,
    val deliveryDhakaCharge: Double = 60.0,
    val deliveryOutsideCharge: Double = 120.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val colors: List<String>
        get() = colorsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val sizes: List<String>
        get() = sizesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val selectedColor: String,
    val selectedSize: String,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val label: String,
    val recipientName: String,
    val phone: String,
    val division: String,
    val district: String,
    val area: String,
    val streetAddress: String,
    val isDhakaMetro: Boolean = true,
    val isDefault: Boolean = false
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val id: String,
    val code: String,
    val title: String,
    val discountType: String, // "PERCENT", "FLAT", "FREESHIP"
    val discountValue: Double,
    val minOrderAmount: Double = 0.0,
    val isActive: Boolean = true,
    val expiryMillis: Long = System.currentTimeMillis() + 30L * 24 * 3600 * 1000
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val imageKey: String,
    val ctaCategory: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderCode: String,
    val userId: String,
    val customerName: String,
    val customerPhone: String,
    val shippingAddress: String,
    val isDhakaMetro: Boolean,
    val subtotal: Double,
    val deliveryFee: Double,
    val couponDiscount: Double,
    val couponCode: String = "",
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val paymentWalletNumber: String = "",
    val paymentTrxId: String = "",
    val orderStatus: String = OrderStatuses.PLACED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val productId: String,
    val sellerId: String,
    val productName: String,
    val imageResKey: String,
    val customImageUri: String? = null,
    val selectedColor: String,
    val selectedSize: String,
    val unitPrice: Double,
    val quantity: Int
)

@Entity(tableName = "return_requests")
data class ReturnRequestEntity(
    @PrimaryKey val id: String,
    val returnCode: String,
    val orderId: String,
    val orderCode: String,
    val userId: String,
    val reason: String,
    val comments: String,
    val refundMethod: String,
    val refundAccount: String,
    val refundAmount: Double,
    val status: String = ReturnStatuses.REQUESTED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String = "ORDER",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
