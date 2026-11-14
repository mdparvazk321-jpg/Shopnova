package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.BannerEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CouponEntity
import com.example.data.model.ProductEntity
import com.example.ui.ProductSortOption
import com.example.ui.ShopNovaViewModel
import com.example.ui.components.CountdownPillBox
import com.example.ui.components.NovaPinkCyanGradient
import com.example.ui.components.NovaSoftBannerGradient
import com.example.ui.components.ProductArtwork
import com.example.ui.components.ProductCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.ShimmerLoadingPlaceholder
import com.example.ui.components.ShopNovaLogo
import com.example.ui.components.categoryIconForKey
import com.example.ui.components.formatCountdown
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaOutline
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaTurquoise
import com.example.ui.theme.NovaWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ShopNovaViewModel,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToFlashSale: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onSelectCategoryAndBrowse: (String) -> Unit
) {
    val isReady by viewModel.isSeedReady.collectAsStateWithLifecycle()
    val banners by viewModel.activeBanners.collectAsStateWithLifecycle()
    val categories by viewModel.activeCategories.collectAsStateWithLifecycle()
    val products by viewModel.approvedProducts.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val coupons by viewModel.activeCoupons.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.wishlistProductIds.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
    val unreadNotifs by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val maxPriceFilter by viewModel.maxPriceFilter.collectAsStateWithLifecycle()
    val nowMillis by viewModel.currentTimeMillis.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    if (!isReady) {
        ShimmerLoadingPlaceholder(modifier = Modifier.fillMaxSize())
        return
    }

    val isSearchingOrFiltering = searchQuery.isNotBlank() || selectedCategoryId != null || maxPriceFilter != null

    val flashSaleProducts = remember(products) { products.filter { it.isFlashSale } }
    val trendingProducts = remember(products) { products.filter { it.isTrending } }
    val bestSellingProducts = remember(products) { products.filter { it.isBestSeller } }
    val newArrivals = remember(products) { products.filter { it.isNewArrival } }
    val recommendedProducts = remember(products) { products.filter { it.isRecommended } }
    val flashEndMillis = flashSaleProducts.maxOfOrNull { it.flashSaleEndMillis } ?: (nowMillis + 3600_000L)
    val (hrs, mins, secs) = formatCountdown(flashEndMillis, nowMillis)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val gridCols = when {
            maxWidth >= 840.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Top Header with Logo, Notifications, Cart, Profile
            item {
                HomeTopHeader(
                    cartCount = cartCount,
                    unreadNotifs = unreadNotifs,
                    onNotificationsClick = onNavigateToNotifications,
                    onCartClick = onNavigateToCart,
                    onProfileClick = onNavigateToAccount
                )
            }

            // 2. Search Bar + Filter Button
            item {
                HomeSearchBar(
                    query = searchQuery,
                    hasActiveFilters = selectedCategoryId != null || maxPriceFilter != null,
                    onQueryChange = viewModel::updateSearchQuery,
                    onClearQuery = { viewModel.updateSearchQuery("") },
                    onFilterClick = { showFilterSheet = true }
                )
            }

            if (isSearchingOrFiltering) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredProducts.size} results found",
                            style = MaterialTheme.typography.titleSmall,
                            color = NovaNavy
                        )
                        TextButton(onClick = viewModel::clearFilters) {
                            Text("Reset Filters", color = NovaPink)
                        }
                    }
                }
                if (filteredProducts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No matching products found. Try adjusting search or filters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val rows = filteredProducts.chunked(gridCols)
                    items(rows) { rowItems ->
                        ProductGridRow(
                            rowItems = rowItems,
                            gridCols = gridCols,
                            wishlistIds = wishlistIds,
                            onProductClick = onNavigateToProduct,
                            onToggleWishlist = viewModel::toggleWishlist,
                            onAddToCart = { viewModel.addToCart(it) }
                        )
                    }
                }
            } else {
                // 3. Promotional Banner Carousel
                if (banners.isNotEmpty()) {
                    item {
                        PromoBannerCarousel(
                            banners = banners,
                            onBannerClick = { banner ->
                                if (banner.ctaCategory.isNotBlank()) {
                                    onSelectCategoryAndBrowse(banner.ctaCategory)
                                } else {
                                    onNavigateToFlashSale()
                                }
                            }
                        )
                    }
                }

                // 4. Categories Strip
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "Shop by Category",
                            subtitle = "12 curated departments",
                            actionText = "See All",
                            onActionClick = onNavigateToCategories,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(categories, key = { it.id }) { category ->
                                CategoryQuickChip(
                                    category = category,
                                    onClick = { onSelectCategoryAndBrowse(category.id) }
                                )
                            }
                        }
                    }
                }

                // 5. Flash Sale Section with Live Countdown
                if (flashSaleProducts.isNotEmpty()) {
                    item {
                        FlashSaleHomeStrip(
                            products = flashSaleProducts,
                            hours = hrs,
                            minutes = mins,
                            seconds = secs,
                            wishlistIds = wishlistIds,
                            onSeeAllClick = onNavigateToFlashSale,
                            onProductClick = onNavigateToProduct,
                            onToggleWishlist = viewModel::toggleWishlist,
                            onAddToCart = { viewModel.addToCart(it) }
                        )
                    }
                }

                // 6. Special Discount Offers (Coupons)
                if (coupons.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(
                                title = "Special Discount Offers",
                                subtitle = "Tap any voucher to copy code",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(coupons, key = { it.id }) { coupon ->
                                    VoucherOfferCard(
                                        coupon = coupon,
                                        onCopy = {
                                            clipboard.setText(AnnotatedString(coupon.code))
                                            viewModel.emitMessage("Copied coupon ${coupon.code}!")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Free Delivery Promotional Banner
                item {
                    FreeDeliveryPromoBanner(
                        onShopNowClick = onNavigateToCategories,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // 8. Trending Products
                if (trendingProducts.isNotEmpty()) {
                    item {
                        HorizontalProductSection(
                            title = "Trending Now",
                            subtitle = "Most loved across Dhaka & Chattogram",
                            products = trendingProducts,
                            wishlistIds = wishlistIds,
                            onSeeAllClick = onNavigateToCategories,
                            onProductClick = onNavigateToProduct,
                            onToggleWishlist = viewModel::toggleWishlist,
                            onAddToCart = { viewModel.addToCart(it) }
                        )
                    }
                }

                // 9. Best-Selling Products Grid
                if (bestSellingProducts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Best-Selling Products",
                            subtitle = "Verified top-rated picks",
                            actionText = "Explore",
                            onActionClick = onNavigateToCategories,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    val rows = bestSellingProducts.take(4).chunked(gridCols)
                    items(rows) { rowItems ->
                        ProductGridRow(
                            rowItems = rowItems,
                            gridCols = gridCols,
                            wishlistIds = wishlistIds,
                            onProductClick = onNavigateToProduct,
                            onToggleWishlist = viewModel::toggleWishlist,
                            onAddToCart = { viewModel.addToCart(it) }
                        )
                    }
                }

                // 10. New Arrivals
                if (newArrivals.isNotEmpty()) {
                    item {
                        HorizontalProductSection(
                            title = "New Arrivals",
                            subtitle = "Fresh drops from verified sellers",
                            products = newArrivals,
                            wishlistIds = wishlistIds,
                            onSeeAllClick = onNavigateToCategories,
                            onProductClick = onNavigateToProduct,
                            onToggleWishlist = viewModel::toggleWishlist,
                            onAddToCart = { viewModel.addToCart(it) }
                        )
                    }
                }

                // 11. Recommended Products Grid
                if (recommendedProducts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Recommended For You",
                            subtitle = "Handpicked daily essentials & tech",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    val recRows = recommendedProducts.chunked(gridCols)
                    items(recRows) { rowItems ->
                        ProductGridRow(
                            rowItems = rowItems,
                            gridCols = gridCols,
                            wishlistIds = wishlistIds,
                            onProductClick = onNavigateToProduct,
                            onToggleWishlist = viewModel::toggleWishlist,
                            onAddToCart = { viewModel.addToCart(it) }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = NovaWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Sort & Filter", style = MaterialTheme.typography.titleLarge, color = NovaNavy)
                Text("Sort By", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ProductSortOption.entries) { option ->
                        FilterChip(
                            selected = sortOption == option,
                            onClick = { viewModel.updateSortOption(option) },
                            label = { Text(option.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaPinkSoft,
                                selectedLabelColor = NovaPink
                            )
                        )
                    }
                }
                Text("Max Budget", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                val priceOptions = listOf<Pair<String, Double?>>(
                    "Any" to null,
                    "Under ৳2,000" to 2000.0,
                    "Under ৳5,000" to 5000.0,
                    "Under ৳15,000" to 15000.0,
                    "Under ৳50,000" to 50000.0
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(priceOptions) { (label, cap) ->
                        FilterChip(
                            selected = maxPriceFilter == cap,
                            onClick = { viewModel.updateMaxPriceFilter(cap) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaCyanSoft,
                                selectedLabelColor = NovaNavy
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        viewModel.clearFilters()
                        showFilterSheet = false
                    }) {
                        Text("Reset All", color = NovaPink)
                    }
                    Surface(
                        onClick = { showFilterSheet = false },
                        shape = RoundedCornerShape(12.dp),
                        color = NovaPink
                    ) {
                        Text(
                            "Apply",
                            color = NovaWhite,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopHeader(
    cartCount: Int,
    unreadNotifs: Int,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        color = NovaWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShopNovaLogo()
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("home_notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifs > 0) {
                                Badge(containerColor = NovaPink) {
                                    Text("$unreadNotifs", color = NovaWhite)
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = NovaNavy
                        )
                    }
                }
                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier.testTag("home_cart_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(containerColor = NovaPink) {
                                    Text("$cartCount", color = NovaWhite)
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Shopping Cart",
                            tint = NovaNavy
                        )
                    }
                }
                Surface(
                    onClick = onProfileClick,
                    shape = CircleShape,
                    color = NovaCyanSoft,
                    modifier = Modifier
                        .size(38.dp)
                        .border(1.5.dp, NovaCyan, CircleShape)
                        .testTag("home_profile_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "User Profile",
                            tint = NovaNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSearchBar(
    query: String,
    hasActiveFilters: Boolean,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    "Search phones, panjabi, watches...",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = NovaPink)
            },
            trailingIcon = {
                AnimatedVisibility(visible = query.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NovaWhite,
                unfocusedContainerColor = NovaWhite,
                focusedBorderColor = NovaPink,
                unfocusedBorderColor = NovaOutline
            ),
            modifier = Modifier
                .weight(1f)
                .testTag("home_search_input")
        )
        Surface(
            onClick = onFilterClick,
            shape = RoundedCornerShape(16.dp),
            color = if (hasActiveFilters) NovaPink else NovaNavy,
            modifier = Modifier
                .size(54.dp)
                .testTag("home_filter_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = "Filter products",
                    tint = NovaWhite
                )
            }
        }
    }
}

@Composable
private fun PromoBannerCarousel(
    banners: List<BannerEntity>,
    onBannerClick: (BannerEntity) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val banner = banners[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clickable { onBannerClick(banner) }
                    .testTag("promo_banner_${banner.id}"),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ProductArtwork(
                        imageResKey = banner.imageKey,
                        contentDescription = banner.title,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        NovaNavy.copy(alpha = 0.86f),
                                        NovaNavy.copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = NovaPink,
                            shape = CircleShape
                        ) {
                            Text(
                                text = banner.badgeText,
                                color = NovaWhite,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Column(modifier = Modifier.fillMaxWidth(0.68f)) {
                            Text(
                                text = banner.title,
                                color = NovaWhite,
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = banner.subtitle,
                                color = NovaCyanSoft,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { idx ->
                val selected = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (selected) 20.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) NovaPink else NovaOutline)
                )
            }
        }
    }
}

@Composable
private fun CategoryQuickChip(
    category: CategoryEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(78.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag("home_category_${category.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(category.accentHex).copy(alpha = 0.14f),
                            NovaCyanSoft
                        )
                    )
                )
                .border(1.dp, Color(category.accentHex).copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIconForKey(category.iconKey),
                contentDescription = category.name,
                tint = Color(category.accentHex),
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = NovaNavy,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 13.sp
        )
    }
}

@Composable
private fun FlashSaleHomeStrip(
    products: List<ProductEntity>,
    hours: String,
    minutes: String,
    seconds: String,
    wishlistIds: Set<String>,
    onSeeAllClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(NovaPinkCyanGradient)
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = NovaWhite,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
                Column {
                    Text(
                        text = "FLASH SALE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = NovaWhite
                    )
                    Text(
                        text = "Ends soon • Limited stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = NovaWhite.copy(alpha = 0.9f)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountdownPillBox(hours, minutes, seconds)
                Surface(
                    onClick = onSeeAllClick,
                    shape = CircleShape,
                    color = NovaWhite
                ) {
                    Text(
                        "View",
                        color = NovaPink,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    isWishlisted = wishlistIds.contains(product.id),
                    showFlashProgress = true,
                    onClick = { onProductClick(product.id) },
                    onToggleWishlist = { onToggleWishlist(product.id) },
                    onAddToCart = { onAddToCart(product) },
                    modifier = Modifier.width(172.dp)
                )
            }
        }
    }
}

@Composable
private fun VoucherOfferCard(
    coupon: CouponEntity,
    onCopy: () -> Unit
) {
    Surface(
        onClick = onCopy,
        shape = RoundedCornerShape(16.dp),
        color = NovaWhite,
        shadowElevation = 2.dp,
        modifier = Modifier
            .width(210.dp)
            .border(1.dp, NovaPink.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .testTag("coupon_card_${coupon.code}")
    ) {
        Row(
            modifier = Modifier
                .background(NovaSoftBannerGradient)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = coupon.code,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = NovaPink
                )
                Text(
                    text = coupon.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Min order ৳${coupon.minOrderAmount.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(shape = CircleShape, color = NovaWhite) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy coupon code",
                    tint = NovaPink,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FreeDeliveryPromoBanner(
    onShopNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onShopNowClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NovaWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(NovaCyanSoft, NovaWhite, NovaPinkSoft.copy(alpha = 0.55f))
                    )
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_banner_free_delivery),
                contentDescription = "Free Express Delivery",
                modifier = Modifier
                    .size(width = 112.dp, height = 74.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = NovaTurquoise,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "FREE EXPRESS DELIVERY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = NovaTurquoise
                    )
                }
                Text(
                    "Free Nationwide Shipping on orders ৳1,500+",
                    style = MaterialTheme.typography.titleSmall,
                    color = NovaNavy
                )
                Text(
                    "Dhaka Metro ৳60 • Outside Dhaka ৳120",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HorizontalProductSection(
    title: String,
    subtitle: String,
    products: List<ProductEntity>,
    wishlistIds: Set<String>,
    onSeeAllClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = title,
            subtitle = subtitle,
            actionText = "See All",
            onActionClick = onSeeAllClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    isWishlisted = wishlistIds.contains(product.id),
                    onClick = { onProductClick(product.id) },
                    onToggleWishlist = { onToggleWishlist(product.id) },
                    onAddToCart = { onAddToCart(product) },
                    modifier = Modifier.width(174.dp)
                )
            }
        }
    }
}

@Composable
fun ProductGridRow(
    rowItems: List<ProductEntity>,
    gridCols: Int,
    wishlistIds: Set<String>,
    showFlashProgress: Boolean = false,
    onProductClick: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rowItems.forEach { product ->
            ProductCard(
                product = product,
                isWishlisted = wishlistIds.contains(product.id),
                showFlashProgress = showFlashProgress,
                onClick = { onProductClick(product.id) },
                onToggleWishlist = { onToggleWishlist(product.id) },
                onAddToCart = { onAddToCart(product) },
                modifier = Modifier.weight(1f)
            )
        }
        repeat(gridCols - rowItems.size) {
            Spacer(Modifier.weight(1f))
        }
    }
}
