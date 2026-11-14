package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseSeeder
import com.example.data.local.SessionManager
import com.example.data.local.ShopNovaDatabase
import com.example.data.model.AddressEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.BannerEntity
import com.example.data.model.CartItemEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CouponEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ReturnRequestEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.SellerEntity
import com.example.data.model.UserEntity
import com.example.data.repository.ShopNovaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

data class CartDisplayItem(
    val cartItem: CartItemEntity,
    val product: ProductEntity
)

enum class ProductSortOption(val label: String) {
    POPULAR("Popular"),
    PRICE_LOW("Price: Low to High"),
    PRICE_HIGH("Price: High to Low"),
    TOP_RATED("Top Rated"),
    BIGGEST_DISCOUNT("Biggest Discount")
}

@OptIn(ExperimentalCoroutinesApi::class)
class ShopNovaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ShopNovaDatabase.getInstance(application)
    private val dao = database.shopNovaDao()
    private val sessionManager = SessionManager(application)
    val repository = ShopNovaRepository(dao, sessionManager)

    private val _isSeedReady = MutableStateFlow(false)
    val isSeedReady: StateFlow<Boolean> = _isSeedReady.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    // Search & filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _sortOption = MutableStateFlow(ProductSortOption.POPULAR)
    val sortOption: StateFlow<ProductSortOption> = _sortOption.asStateFlow()

    private val _maxPriceFilter = MutableStateFlow<Double?>(null)
    val maxPriceFilter: StateFlow<Double?> = _maxPriceFilter.asStateFlow()

    // Applied coupon in Cart
    private val _appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val appliedCoupon: StateFlow<CouponEntity?> = _appliedCoupon.asStateFlow()

    // Live countdown ticker (ticks every 1000ms)
    val currentTimeMillis: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), System.currentTimeMillis())

    val currentUserId: StateFlow<String?> = repository.currentUserIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SessionManager.DEFAULT_BUYER_ID)

    val currentUser: StateFlow<UserEntity?> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(null) else repository.observeUser(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val currentSeller: StateFlow<SellerEntity?> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(null) else repository.observeSellerByUser(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val activeCategories: StateFlow<List<CategoryEntity>> = repository.activeCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val approvedProducts: StateFlow<List<ProductEntity>> = repository.approvedProductsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProductsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeBanners: StateFlow<List<BannerEntity>> = repository.activeBannersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allBanners: StateFlow<List<BannerEntity>> = repository.allBannersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeCoupons: StateFlow<List<CouponEntity>> = repository.activeCouponsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allCoupons: StateFlow<List<CouponEntity>> = repository.allCouponsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cartItems: StateFlow<List<CartDisplayItem>> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList())
        else combine(repository.observeCart(uid), approvedProducts) { cart, products ->
            val productMap = products.associateBy { it.id }
            cart.mapNotNull { item -> productMap[item.productId]?.let { CartDisplayItem(item, it) } }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cartCount: StateFlow<Int> = cartItems
        .map { list -> list.sumOf { it.cartItem.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    val wishlistProductIds: StateFlow<Set<String>> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptySet())
        else repository.observeWishlist(uid).map { list -> list.map { it.productId }.toSet() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

    val userAddresses: StateFlow<List<AddressEntity>> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else repository.observeAddresses(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userOrders: StateFlow<List<OrderEntity>> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else repository.observeUserOrders(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userReturns: StateFlow<List<ReturnRequestEntity>> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else repository.observeUserReturns(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = currentUserId.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else repository.observeNotifications(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val unreadNotificationCount: StateFlow<Int> = userNotifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    // Admin & Seller Flows
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allSellers: StateFlow<List<SellerEntity>> = repository.allSellersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrdersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allOrderItems: StateFlow<List<OrderItemEntity>> = repository.allOrderItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allReturns: StateFlow<List<ReturnRequestEntity>> = repository.allReturnsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        approvedProducts,
        searchQuery,
        selectedCategoryId,
        sortOption,
        maxPriceFilter
    ) { products, query, catId, sort, maxPrice ->
        val q = query.trim().lowercase()
        products
            .filter { p ->
                (catId == null || p.categoryId == catId) &&
                    (maxPrice == null || p.discountedPrice <= maxPrice) &&
                    (q.isEmpty() || p.name.lowercase().contains(q) ||
                        p.categoryName.lowercase().contains(q) ||
                        p.sellerName.lowercase().contains(q) ||
                        p.description.lowercase().contains(q))
            }
            .let { list ->
                when (sort) {
                    ProductSortOption.POPULAR -> list.sortedByDescending { it.soldCount }
                    ProductSortOption.PRICE_LOW -> list.sortedBy { it.discountedPrice }
                    ProductSortOption.PRICE_HIGH -> list.sortedByDescending { it.discountedPrice }
                    ProductSortOption.TOP_RATED -> list.sortedByDescending { it.rating }
                    ProductSortOption.BIGGEST_DISCOUNT -> list.sortedByDescending { it.discountPercent }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    init {
        viewModelScope.launch {
            DatabaseSeeder.seedIfNeeded(dao)
            _isSeedReady.value = true
        }
    }

    fun emitMessage(msg: String) {
        _snackbarEvents.tryEmit(msg)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun updateSortOption(option: ProductSortOption) {
        _sortOption.value = option
    }

    fun updateMaxPriceFilter(maxPrice: Double?) {
        _maxPriceFilter.value = maxPrice
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategoryId.value = null
        _sortOption.value = ProductSortOption.POPULAR
        _maxPriceFilter.value = null
    }

    fun observeProductDetail(productId: String): Flow<ProductEntity?> = repository.observeProduct(productId)
    fun observeProductReviews(productId: String): Flow<List<ReviewEntity>> = repository.observeReviews(productId)
    fun observeOrderDetail(orderId: String): Flow<OrderEntity?> = repository.observeOrder(orderId)
    fun observeOrderItems(orderId: String): Flow<List<OrderItemEntity>> = repository.observeOrderItems(orderId)
    fun observeSellerProducts(sellerId: String): Flow<List<ProductEntity>> = repository.observeSellerProducts(sellerId)

    // --- Cart Actions ---
    fun addToCart(
        product: ProductEntity,
        selectedColor: String = product.colors.firstOrNull() ?: "Standard",
        selectedSize: String = product.sizes.firstOrNull() ?: "Standard",
        qty: Int = 1,
        onAdded: (() -> Unit)? = null
    ) {
        val uid = currentUserId.value
        if (uid == null) {
            emitMessage("Please sign in from Account to add items to your cart.")
            return
        }
        if (product.stock <= 0) {
            emitMessage("Sorry, ${product.name} is currently out of stock.")
            return
        }
        viewModelScope.launch {
            repository.addToCart(uid, product, selectedColor, selectedSize, qty)
            emitMessage("Added ${product.name} to cart")
            onAdded?.invoke()
        }
    }

    fun updateCartQuantity(item: CartItemEntity, newQty: Int) {
        viewModelScope.launch { repository.updateCartQuantity(item, newQty) }
    }

    fun removeCartItem(id: String) {
        viewModelScope.launch {
            repository.removeCartItem(id)
            emitMessage("Item removed from cart")
        }
    }

    fun toggleWishlist(productId: String) {
        val uid = currentUserId.value
        if (uid == null) {
            emitMessage("Sign in to save items to your Wishlist.")
            return
        }
        viewModelScope.launch {
            val added = repository.toggleWishlist(uid, productId)
            emitMessage(if (added) "Saved to Wishlist" else "Removed from Wishlist")
        }
    }

    fun applyCouponCode(code: String) {
        val subtotal = cartItems.value.sumOf { it.product.discountedPrice * it.cartItem.quantity }
        viewModelScope.launch {
            repository.validateCoupon(code, subtotal)
                .onSuccess { coupon ->
                    _appliedCoupon.value = coupon
                    emitMessage("Coupon ${coupon.code} applied!")
                }
                .onFailure { err ->
                    emitMessage(err.message ?: "Coupon could not be applied")
                }
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    // --- Checkout ---
    fun placeOrder(
        address: AddressEntity,
        paymentMethod: String,
        walletNumber: String,
        trxId: String,
        onOrderCreated: (OrderEntity) -> Unit
    ) {
        val uid = currentUserId.value
        if (uid == null) {
            emitMessage("Please sign in to place an order.")
            return
        }
        val items = cartItems.value.map { it.cartItem to it.product }
        viewModelScope.launch {
            repository.placeOrder(
                userId = uid,
                address = address,
                cartWithProducts = items,
                paymentMethod = paymentMethod,
                walletNumber = walletNumber,
                trxId = trxId,
                coupon = _appliedCoupon.value
            ).onSuccess { order ->
                _appliedCoupon.value = null
                onOrderCreated(order)
            }.onFailure { err ->
                emitMessage(err.message ?: "Checkout failed")
            }
        }
    }

    // --- Addresses ---
    fun saveAddress(
        label: String,
        recipientName: String,
        phone: String,
        division: String,
        district: String,
        area: String,
        streetAddress: String,
        isDefault: Boolean,
        existingId: String? = null
    ) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            val address = AddressEntity(
                id = existingId ?: "addr_${UUID.randomUUID().toString().take(8)}",
                userId = uid,
                label = label.ifBlank { "Home" },
                recipientName = recipientName.trim(),
                phone = phone.trim(),
                division = division,
                district = district.trim(),
                area = area.trim(),
                streetAddress = streetAddress.trim(),
                isDhakaMetro = district.trim().equals("Dhaka", ignoreCase = true) || division == "Dhaka",
                isDefault = isDefault
            )
            repository.saveAddress(address)
            emitMessage("Delivery address saved")
        }
    }

    fun setDefaultAddress(address: AddressEntity) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch { repository.setDefaultAddress(uid, address) }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch { repository.deleteAddress(addressId) }
    }

    // --- Reviews ---
    fun submitReview(productId: String, rating: Int, comment: String) {
        val user = currentUser.value
        if (user == null) {
            emitMessage("Please sign in to write a review.")
            return
        }
        if (comment.isBlank()) {
            emitMessage("Please write a brief review comment.")
            return
        }
        viewModelScope.launch {
            repository.submitReview(productId, user.id, user.name, rating, comment)
            emitMessage("Thank you for your review!")
        }
    }

    // --- Returns ---
    fun submitReturn(
        order: OrderEntity,
        reason: String,
        comments: String,
        refundMethod: String,
        refundAccount: String,
        onDone: (ReturnRequestEntity) -> Unit
    ) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            val ret = repository.submitReturnRequest(order, uid, reason, comments, refundMethod, refundAccount)
            emitMessage("Return request ${ret.returnCode} submitted")
            onDone(ret)
        }
    }

    // --- Auth ---
    fun login(phone: String, pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.loginWithPhone(phone, pin)
                .onSuccess { user ->
                    emitMessage("Welcome back, ${user.name}!")
                    onSuccess()
                }
                .onFailure { emitMessage(it.message ?: "Login failed") }
        }
    }

    fun signUp(
        name: String,
        phone: String,
        email: String,
        pin: String,
        otp: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.signUpWithPhone(name, phone, email, pin, otp)
                .onSuccess { user ->
                    emitMessage("Account verified! Welcome, ${user.name}")
                    onSuccess()
                }
                .onFailure { emitMessage(it.message ?: "Sign up failed") }
        }
    }

    fun switchDemoAccount(userId: String) {
        viewModelScope.launch {
            repository.switchToDemoUser(userId)
            emitMessage("Switched account")
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _appliedCoupon.value = null
            emitMessage("Logged out")
        }
    }

    fun updateProfile(name: String, email: String, phone: String) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            repository.updateProfile(uid, name, email, phone)
                .onSuccess { emitMessage("Profile updated") }
                .onFailure { emitMessage(it.message ?: "Update failed") }
        }
    }

    fun markAllNotificationsRead() {
        val uid = currentUserId.value ?: return
        viewModelScope.launch { repository.markAllNotificationsRead(uid) }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    // --- Seller ---
    fun registerAsSeller(
        shopName: String,
        ownerName: String,
        phone: String,
        pickupAddress: String,
        tradeLicense: String,
        payoutMethod: String,
        payoutAccount: String
    ) {
        val uid = currentUserId.value ?: return
        viewModelScope.launch {
            repository.registerSeller(
                uid, shopName, ownerName, phone, pickupAddress, tradeLicense, payoutMethod, payoutAccount
            )
            emitMessage("Seller registration submitted! Awaiting Admin approval.")
        }
    }

    fun saveSellerProduct(
        existingId: String?,
        seller: SellerEntity,
        category: CategoryEntity,
        name: String,
        description: String,
        originalPrice: Double,
        discountedPrice: Double,
        stock: Int,
        colorsCsv: String,
        sizesCsv: String,
        imageResKey: String,
        customImageUri: String?
    ) {
        viewModelScope.launch {
            val discountPct = if (originalPrice > 0 && discountedPrice < originalPrice) {
                (((originalPrice - discountedPrice) / originalPrice) * 100.0).roundToInt().coerceIn(0, 95)
            } else 0
            val existing = existingId?.let { dao.getProductById(it) }
            val product = (existing ?: ProductEntity(
                id = "prod_${UUID.randomUUID().toString().take(8)}",
                sellerId = seller.id,
                sellerName = seller.shopName,
                categoryId = category.id,
                categoryName = category.name,
                name = name.trim(),
                description = description.trim(),
                originalPrice = originalPrice,
                discountedPrice = discountedPrice,
                discountPercent = discountPct,
                approvalStatus = ApprovalStatus.PENDING
            )).copy(
                categoryId = category.id,
                categoryName = category.name,
                name = name.trim(),
                description = description.trim(),
                originalPrice = originalPrice,
                discountedPrice = discountedPrice,
                discountPercent = discountPct,
                stock = stock,
                colorsCsv = colorsCsv.ifBlank { "Standard" },
                sizesCsv = sizesCsv.ifBlank { "Standard" },
                imageResKey = imageResKey,
                customImageUri = customImageUri,
                isNewArrival = true
            )
            repository.saveSellerProduct(product)
            emitMessage(
                if (existing == null) "Product submitted for Admin approval!"
                else "Product updated"
            )
        }
    }

    fun adjustProductStock(product: ProductEntity, delta: Int) {
        viewModelScope.launch {
            repository.updateProductStock(product, product.stock + delta)
        }
    }

    // --- Admin & Fulfillment ---
    fun advanceOrderStatus(order: OrderEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(order, newStatus)
            emitMessage("Order ${order.orderCode} updated to $newStatus")
        }
    }

    fun verifyPayment(order: OrderEntity, approved: Boolean) {
        viewModelScope.launch {
            repository.verifyPayment(order, approved)
            emitMessage(if (approved) "Payment confirmed for ${order.orderCode}" else "Payment rejected")
        }
    }

    fun updateReturnStatus(ret: ReturnRequestEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateReturnStatus(ret, newStatus)
            emitMessage("Return ${ret.returnCode} -> $newStatus")
        }
    }

    fun updateProductApproval(product: ProductEntity, status: String) {
        viewModelScope.launch {
            repository.updateProductApproval(product, status)
            emitMessage("${product.name} marked $status")
        }
    }

    fun toggleProductFlashSale(product: ProductEntity, enable: Boolean, discountPercent: Int, hours: Int) {
        viewModelScope.launch {
            repository.toggleProductFlashSale(product, enable, discountPercent, hours)
            emitMessage(if (enable) "Added to Flash Sale (-$discountPercent%)" else "Removed from Flash Sale")
        }
    }

    fun updateSellerStatus(seller: SellerEntity, status: String) {
        viewModelScope.launch {
            repository.updateSellerStatus(seller, status)
            emitMessage("${seller.shopName} -> $status")
        }
    }

    fun toggleUserActive(user: UserEntity) {
        viewModelScope.launch {
            repository.toggleUserActive(user)
            emitMessage("Updated status for ${user.name}")
        }
    }

    fun updateUserRole(user: UserEntity, role: String) {
        viewModelScope.launch {
            repository.updateUserRole(user, role)
            emitMessage("${user.name} role set to $role")
        }
    }

    fun saveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.upsertCategory(category)
            emitMessage("Category ${category.name} saved")
        }
    }

    fun saveBanner(banner: BannerEntity) {
        viewModelScope.launch {
            repository.upsertBanner(banner)
            emitMessage("Banner updated")
        }
    }

    fun saveCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            repository.upsertCoupon(coupon)
            emitMessage("Coupon ${coupon.code} saved")
        }
    }
}
