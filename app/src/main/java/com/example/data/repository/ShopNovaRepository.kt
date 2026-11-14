package com.example.data.repository

import com.example.data.local.PasswordHasher
import com.example.data.local.SessionManager
import com.example.data.local.ShopNovaDao
import com.example.data.model.AddressEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.BannerEntity
import com.example.data.model.CartItemEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CouponEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.OrderStatuses
import com.example.data.model.PaymentMethods
import com.example.data.model.PaymentStatuses
import com.example.data.model.ProductEntity
import com.example.data.model.ReturnRequestEntity
import com.example.data.model.ReturnStatuses
import com.example.data.model.ReviewEntity
import com.example.data.model.SellerEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRoles
import com.example.data.model.WishlistEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

class ShopNovaRepository(
    private val dao: ShopNovaDao,
    private val sessionManager: SessionManager
) {
    val currentUserIdFlow: Flow<String?> = sessionManager.currentUserIdFlow
    val allUsersFlow: Flow<List<UserEntity>> = dao.observeAllUsers()
    val allSellersFlow: Flow<List<SellerEntity>> = dao.observeAllSellers()
    val allCategoriesFlow: Flow<List<CategoryEntity>> = dao.observeAllCategories()
    val activeCategoriesFlow: Flow<List<CategoryEntity>> = dao.observeActiveCategories()
    val allProductsFlow: Flow<List<ProductEntity>> = dao.observeAllProducts()
    val approvedProductsFlow: Flow<List<ProductEntity>> = dao.observeApprovedProducts()
    val allBannersFlow: Flow<List<BannerEntity>> = dao.observeAllBanners()
    val activeBannersFlow: Flow<List<BannerEntity>> = dao.observeActiveBanners()
    val allCouponsFlow: Flow<List<CouponEntity>> = dao.observeAllCoupons()
    val activeCouponsFlow: Flow<List<CouponEntity>> = dao.observeActiveCoupons()
    val allOrdersFlow: Flow<List<OrderEntity>> = dao.observeAllOrders()
    val allOrderItemsFlow: Flow<List<OrderItemEntity>> = dao.observeAllOrderItems()
    val allReturnsFlow: Flow<List<ReturnRequestEntity>> = dao.observeAllReturns()

    fun observeUser(userId: String): Flow<UserEntity?> = dao.observeUserById(userId)
    fun observeSellerByUser(userId: String): Flow<SellerEntity?> = dao.observeSellerByUserId(userId)
    fun observeSellerProducts(sellerId: String): Flow<List<ProductEntity>> = dao.observeSellerProducts(sellerId)
    fun observeProduct(productId: String): Flow<ProductEntity?> = dao.observeProductById(productId)
    fun observeReviews(productId: String): Flow<List<ReviewEntity>> = dao.observeReviewsForProduct(productId)
    fun observeCart(userId: String): Flow<List<CartItemEntity>> = dao.observeCart(userId)
    fun observeWishlist(userId: String): Flow<List<WishlistEntity>> = dao.observeWishlist(userId)
    fun observeAddresses(userId: String): Flow<List<AddressEntity>> = dao.observeAddresses(userId)
    fun observeUserOrders(userId: String): Flow<List<OrderEntity>> = dao.observeOrdersForUser(userId)
    fun observeOrder(orderId: String): Flow<OrderEntity?> = dao.observeOrderById(orderId)
    fun observeOrderItems(orderId: String): Flow<List<OrderItemEntity>> = dao.observeOrderItems(orderId)
    fun observeUserReturns(userId: String): Flow<List<ReturnRequestEntity>> = dao.observeReturnsForUser(userId)
    fun observeNotifications(userId: String): Flow<List<NotificationEntity>> = dao.observeNotificationsForUser(userId)

    // --- Authentication ---
    suspend fun loginWithPhone(phone: String, pin: String): Result<UserEntity> {
        val cleanPhone = normalizeBdPhone(phone)
        val user = dao.getUserByPhone(cleanPhone)
            ?: return Result.failure(IllegalArgumentException("No account found for $cleanPhone"))
        if (!user.isActive) {
            return Result.failure(IllegalStateException("This account has been suspended by Admin."))
        }
        val expected = PasswordHasher.hash(pin, user.salt)
        if (expected != user.passwordHash) {
            return Result.failure(IllegalArgumentException("Incorrect PIN/password. Please try again."))
        }
        sessionManager.setCurrentUserId(user.id)
        return Result.success(user)
    }

    suspend fun signUpWithPhone(
        name: String,
        phone: String,
        email: String,
        pin: String,
        verificationCodeEntered: String
    ): Result<UserEntity> {
        val cleanPhone = normalizeBdPhone(phone)
        if (!isValidBdPhone(cleanPhone)) {
            return Result.failure(IllegalArgumentException("Enter a valid 11-digit BD mobile number (01XXXXXXXXX)."))
        }
        if (pin.length < 4) {
            return Result.failure(IllegalArgumentException("PIN/password must be at least 4 characters."))
        }
        if (verificationCodeEntered.length != 6) {
            return Result.failure(IllegalArgumentException("Enter the 6-digit phone verification code."))
        }
        if (dao.getUserByPhone(cleanPhone) != null) {
            return Result.failure(IllegalArgumentException("An account already exists with $cleanPhone."))
        }
        val salt = UUID.randomUUID().toString().take(12)
        val user = UserEntity(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            phone = cleanPhone,
            email = email.trim(),
            passwordHash = PasswordHasher.hash(pin, salt),
            salt = salt,
            role = UserRoles.BUYER,
            isPhoneVerified = true,
            isActive = true
        )
        dao.upsertUser(user)
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                title = "Welcome to ShopNova BD!",
                message = "Enjoy 15% off your first order with coupon code NOVA15.",
                type = "PROMO"
            )
        )
        sessionManager.setCurrentUserId(user.id)
        return Result.success(user)
    }

    suspend fun switchToDemoUser(userId: String) {
        sessionManager.setCurrentUserId(userId)
    }

    suspend fun logout() {
        sessionManager.setCurrentUserId(null)
    }

    suspend fun updateProfile(userId: String, name: String, email: String, phone: String): Result<Unit> {
        val user = dao.getUserById(userId) ?: return Result.failure(IllegalStateException("User not found"))
        val cleanPhone = normalizeBdPhone(phone)
        if (!isValidBdPhone(cleanPhone)) {
            return Result.failure(IllegalArgumentException("Invalid BD phone number (01XXXXXXXXX)."))
        }
        dao.upsertUser(user.copy(name = name.trim(), email = email.trim(), phone = cleanPhone))
        return Result.success(Unit)
    }

    // --- Cart & Wishlist ---
    suspend fun addToCart(
        userId: String,
        product: ProductEntity,
        selectedColor: String,
        selectedSize: String,
        quantityDelta: Int = 1
    ) {
        val existing = dao.findCartItem(userId, product.id, selectedColor, selectedSize)
        if (existing != null) {
            val updatedQty = (existing.quantity + quantityDelta).coerceIn(1, product.stock.coerceAtLeast(1))
            dao.upsertCartItem(existing.copy(quantity = updatedQty))
        } else {
            dao.upsertCartItem(
                CartItemEntity(
                    id = "cart_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    productId = product.id,
                    selectedColor = selectedColor,
                    selectedSize = selectedSize,
                    quantity = quantityDelta.coerceIn(1, product.stock.coerceAtLeast(1))
                )
            )
        }
    }

    suspend fun updateCartQuantity(item: CartItemEntity, newQty: Int) {
        if (newQty <= 0) {
            dao.deleteCartItemById(item.id)
        } else {
            dao.upsertCartItem(item.copy(quantity = newQty))
        }
    }

    suspend fun removeCartItem(id: String) {
        dao.deleteCartItemById(id)
    }

    suspend fun toggleWishlist(userId: String, productId: String): Boolean {
        val existing = dao.findWishlistItem(userId, productId)
        return if (existing != null) {
            dao.deleteWishlistItem(userId, productId)
            false
        } else {
            dao.insertWishlistItem(
                WishlistEntity(
                    id = "wish_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    productId = productId
                )
            )
            true
        }
    }

    // --- Addresses ---
    suspend fun saveAddress(address: AddressEntity) {
        if (address.isDefault) {
            dao.clearDefaultAddress(address.userId)
        }
        dao.upsertAddress(address)
    }

    suspend fun setDefaultAddress(userId: String, address: AddressEntity) {
        dao.clearDefaultAddress(userId)
        dao.upsertAddress(address.copy(isDefault = true))
    }

    suspend fun deleteAddress(addressId: String) {
        dao.deleteAddressById(addressId)
    }

    // --- Reviews ---
    suspend fun submitReview(productId: String, userId: String, userName: String, rating: Int, comment: String) {
        val review = ReviewEntity(
            id = "rev_${UUID.randomUUID().toString().take(8)}",
            productId = productId,
            userId = userId,
            userName = userName,
            rating = rating.coerceIn(1, 5),
            comment = comment.trim()
        )
        dao.upsertReview(review)
        val product = dao.getProductById(productId)
        if (product != null) {
            val newCount = product.reviewCount + 1
            val newAvg = ((product.rating * product.reviewCount) + rating) / newCount
            dao.upsertProduct(
                product.copy(
                    reviewCount = newCount,
                    rating = ((newAvg * 10.0).roundToInt() / 10.0)
                )
            )
        }
    }

    // --- Coupons & Checkout ---
    suspend fun validateCoupon(code: String, subtotal: Double): Result<CouponEntity> {
        val coupon = dao.getCouponByCode(code.trim())
            ?: return Result.failure(IllegalArgumentException("Invalid or expired coupon code."))
        if (subtotal < coupon.minOrderAmount) {
            return Result.failure(
                IllegalArgumentException("Minimum order ৳${coupon.minOrderAmount.toInt()} required for ${coupon.code}.")
            )
        }
        return Result.success(coupon)
    }

    suspend fun placeOrder(
        userId: String,
        address: AddressEntity,
        cartWithProducts: List<Pair<CartItemEntity, ProductEntity>>,
        paymentMethod: String,
        walletNumber: String,
        trxId: String,
        coupon: CouponEntity?
    ): Result<OrderEntity> {
        if (cartWithProducts.isEmpty()) {
            return Result.failure(IllegalStateException("Your cart is empty."))
        }
        if (paymentMethod == PaymentMethods.CARD) {
            return Result.failure(
                IllegalStateException("Live Card/SSLCommerz gateway requires merchant credentials. Choose Cash on Delivery or bKash/Nagad/Rocket TrxID verification.")
            )
        }
        if (paymentMethod in listOf(PaymentMethods.BKASH, PaymentMethods.NAGAD, PaymentMethods.ROCKET)) {
            if (!isValidBdPhone(normalizeBdPhone(walletNumber)) || trxId.trim().length < 6) {
                return Result.failure(
                    IllegalArgumentException("Enter your $paymentMethod mobile number and valid Transaction ID (min 6 chars).")
                )
            }
        }

        val subtotal = cartWithProducts.sumOf { (cart, prod) -> prod.discountedPrice * cart.quantity }
        val baseDelivery = if (address.isDhakaMetro) 60.0 else 120.0
        var deliveryFee = if (subtotal >= 1500.0) 0.0 else baseDelivery
        var couponDiscount = 0.0
        if (coupon != null && subtotal >= coupon.minOrderAmount) {
            when (coupon.discountType) {
                "PERCENT" -> couponDiscount = (subtotal * (coupon.discountValue / 100.0)).roundToInt().toDouble()
                "FLAT" -> couponDiscount = coupon.discountValue.coerceAtMost(subtotal)
                "FREESHIP" -> deliveryFee = 0.0
            }
        }
        val total = (subtotal - couponDiscount + deliveryFee).coerceAtLeast(0.0)

        val paymentStatus = when (paymentMethod) {
            PaymentMethods.COD -> PaymentStatuses.COD_DUE
            else -> PaymentStatuses.PENDING_VERIFICATION
        }

        val orderId = "ord_${UUID.randomUUID().toString().take(8)}"
        val orderCode = "SN-BD-${Random.nextInt(10000, 99999)}"
        val order = OrderEntity(
            id = orderId,
            orderCode = orderCode,
            userId = userId,
            customerName = address.recipientName,
            customerPhone = address.phone,
            shippingAddress = "${address.streetAddress}, ${address.area}, ${address.district}",
            isDhakaMetro = address.isDhakaMetro,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            couponDiscount = couponDiscount,
            couponCode = coupon?.code ?: "",
            totalAmount = total,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            paymentWalletNumber = normalizeBdPhone(walletNumber),
            paymentTrxId = trxId.trim().uppercase(),
            orderStatus = OrderStatuses.PLACED
        )

        val orderItems = cartWithProducts.map { (cart, prod) ->
            // Decrement stock and increment soldCount
            dao.upsertProduct(
                prod.copy(
                    stock = (prod.stock - cart.quantity).coerceAtLeast(0),
                    soldCount = prod.soldCount + cart.quantity
                )
            )
            // Credit seller pending balance
            val seller = dao.getSellerById(prod.sellerId)
            if (seller != null) {
                val net = prod.discountedPrice * cart.quantity * 0.92 // 8% platform fee
                dao.upsertSeller(seller.copy(pendingBalance = seller.pendingBalance + net))
            }
            OrderItemEntity(
                id = "oi_${UUID.randomUUID().toString().take(8)}",
                orderId = orderId,
                productId = prod.id,
                sellerId = prod.sellerId,
                productName = prod.name,
                imageResKey = prod.imageResKey,
                customImageUri = prod.customImageUri,
                selectedColor = cart.selectedColor,
                selectedSize = cart.selectedSize,
                unitPrice = prod.discountedPrice,
                quantity = cart.quantity
            )
        }

        dao.upsertOrder(order)
        dao.insertOrderItems(orderItems)
        dao.clearCart(userId)

        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                title = "Order $orderCode Placed",
                message = if (paymentStatus == PaymentStatuses.PENDING_VERIFICATION) {
                    "Your $paymentMethod TrxID (${order.paymentTrxId}) is awaiting payment verification."
                } else {
                    "Your Cash on Delivery order of ৳${total.toInt()} has been received."
                },
                type = "ORDER"
            )
        )
        return Result.success(order)
    }

    // --- Returns & Refunds ---
    suspend fun submitReturnRequest(
        order: OrderEntity,
        userId: String,
        reason: String,
        comments: String,
        refundMethod: String,
        refundAccount: String
    ): ReturnRequestEntity {
        val ret = ReturnRequestEntity(
            id = "ret_${UUID.randomUUID().toString().take(8)}",
            returnCode = "RET-2026-${Random.nextInt(1000, 9999)}",
            orderId = order.id,
            orderCode = order.orderCode,
            userId = userId,
            reason = reason,
            comments = comments.trim(),
            refundMethod = refundMethod,
            refundAccount = refundAccount.trim(),
            refundAmount = order.totalAmount,
            status = ReturnStatuses.REQUESTED
        )
        dao.upsertReturn(ret)
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                title = "Return Request ${ret.returnCode}",
                message = "Return requested for order ${order.orderCode}. Our support team will review shortly.",
                type = "RETURN"
            )
        )
        return ret
    }

    // --- Notifications ---
    suspend fun markAllNotificationsRead(userId: String) = dao.markAllNotificationsRead(userId)
    suspend fun markNotificationRead(id: String) = dao.markNotificationRead(id)

    // --- Seller Operations ---
    suspend fun registerSeller(
        userId: String,
        shopName: String,
        ownerName: String,
        phone: String,
        pickupAddress: String,
        tradeLicenseOrNid: String,
        payoutMethod: String,
        payoutAccount: String
    ): SellerEntity {
        val seller = SellerEntity(
            id = "seller_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            shopName = shopName.trim(),
            ownerName = ownerName.trim(),
            phone = normalizeBdPhone(phone),
            pickupAddress = pickupAddress.trim(),
            tradeLicenseOrNid = tradeLicenseOrNid.trim(),
            payoutMethod = payoutMethod,
            payoutAccount = payoutAccount.trim(),
            status = ApprovalStatus.PENDING
        )
        dao.upsertSeller(seller)
        val user = dao.getUserById(userId)
        if (user != null && user.role == UserRoles.BUYER) {
            dao.upsertUser(user.copy(role = UserRoles.SELLER))
        }
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                title = "Seller Application Submitted",
                message = "$shopName is pending Admin approval. You can manage it once approved.",
                type = "SELLER"
            )
        )
        return seller
    }

    suspend fun saveSellerProduct(product: ProductEntity) {
        dao.upsertProduct(product)
    }

    suspend fun updateProductStock(product: ProductEntity, newStock: Int) {
        dao.upsertProduct(product.copy(stock = newStock.coerceAtLeast(0)))
    }

    // --- Admin & Order Progression ---
    suspend fun updateOrderStatus(order: OrderEntity, newStatus: String) {
        dao.upsertOrder(order.copy(orderStatus = newStatus, updatedAt = System.currentTimeMillis()))
        if (newStatus == OrderStatuses.DELIVERED && order.paymentMethod == PaymentMethods.COD) {
            dao.upsertOrder(
                order.copy(
                    orderStatus = newStatus,
                    paymentStatus = PaymentStatuses.PAID,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = order.userId,
                title = "Order ${order.orderCode}: $newStatus",
                message = "Your order status has been updated to $newStatus.",
                type = "ORDER"
            )
        )
    }

    suspend fun verifyPayment(order: OrderEntity, approved: Boolean) {
        val newPayStatus = if (approved) PaymentStatuses.PAID else PaymentStatuses.REJECTED
        val newOrderStatus = if (approved && order.orderStatus == OrderStatuses.PLACED) {
            OrderStatuses.CONFIRMED
        } else {
            order.orderStatus
        }
        dao.upsertOrder(
            order.copy(
                paymentStatus = newPayStatus,
                orderStatus = newOrderStatus,
                updatedAt = System.currentTimeMillis()
            )
        )
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = order.userId,
                title = if (approved) "Payment Verified" else "Payment Verification Issue",
                message = if (approved) {
                    "Payment for ${order.orderCode} via ${order.paymentMethod} has been confirmed."
                } else {
                    "TrxID ${order.paymentTrxId} for ${order.orderCode} could not be verified."
                },
                type = "ORDER"
            )
        )
    }

    suspend fun updateReturnStatus(ret: ReturnRequestEntity, newStatus: String) {
        dao.upsertReturn(ret.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = ret.userId,
                title = "Return ${ret.returnCode}: $newStatus",
                message = "Your return & refund status is now: $newStatus.",
                type = "RETURN"
            )
        )
    }

    suspend fun updateProductApproval(product: ProductEntity, status: String) {
        dao.upsertProduct(product.copy(approvalStatus = status))
    }

    suspend fun toggleProductFlashSale(
        product: ProductEntity,
        enable: Boolean,
        flashDiscountPercent: Int,
        durationHours: Int
    ) {
        if (enable) {
            val discounted = (product.originalPrice * (100 - flashDiscountPercent) / 100.0).roundToInt().toDouble()
            dao.upsertProduct(
                product.copy(
                    isFlashSale = true,
                    discountPercent = flashDiscountPercent,
                    discountedPrice = discounted,
                    flashSaleEndMillis = System.currentTimeMillis() + durationHours * 3600L * 1000L
                )
            )
        } else {
            dao.upsertProduct(product.copy(isFlashSale = false))
        }
    }

    suspend fun updateSellerStatus(seller: SellerEntity, newStatus: String) {
        dao.upsertSeller(seller.copy(status = newStatus))
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = seller.userId,
                title = "Seller Account $newStatus",
                message = "Your store ${seller.shopName} status is now $newStatus.",
                type = "SELLER"
            )
        )
    }

    suspend fun toggleUserActive(user: UserEntity) {
        dao.upsertUser(user.copy(isActive = !user.isActive))
    }

    suspend fun updateUserRole(user: UserEntity, newRole: String) {
        dao.upsertUser(user.copy(role = newRole))
    }

    suspend fun upsertCategory(category: CategoryEntity) = dao.upsertCategory(category)
    suspend fun upsertBanner(banner: BannerEntity) = dao.upsertBanner(banner)
    suspend fun upsertCoupon(coupon: CouponEntity) = dao.upsertCoupon(coupon)

    private fun normalizeBdPhone(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.startsWith("8801") && digits.length == 13 -> "0" + digits.substring(3)
            digits.startsWith("01") && digits.length == 11 -> digits
            else -> digits
        }
    }

    private fun isValidBdPhone(phone: String): Boolean {
        return phone.length == 11 && phone.startsWith("01") && phone[2] in '3'..'9'
    }
}
