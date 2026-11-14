package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ShopNovaViewModel
import com.example.ui.components.ProductArtwork
import com.example.ui.components.formatTaka
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaGold
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaOutline
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessSoft
import com.example.ui.theme.NovaTurquoise
import com.example.ui.theme.NovaWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ShopNovaViewModel,
    onBack: () -> Unit,
    onBuyNowNavigateToCart: () -> Unit
) {
    val product by viewModel.observeProductDetail(productId).collectAsStateWithLifecycle(initialValue = null)
    val reviews by viewModel.observeProductReviews(productId).collectAsStateWithLifecycle(initialValue = emptyList())
    val wishlistIds by viewModel.wishlistProductIds.collectAsStateWithLifecycle()

    val item = product
    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading product details...")
        }
        return
    }

    var selectedColor by remember(item.id) { mutableStateOf(item.colors.firstOrNull() ?: "Standard") }
    var selectedSize by remember(item.id) { mutableStateOf(item.sizes.firstOrNull() ?: "Standard") }
    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    LaunchedEffect(item.id) {
        if (selectedColor.isBlank()) selectedColor = item.colors.firstOrNull() ?: "Standard"
        if (selectedSize.isBlank()) selectedSize = item.sizes.firstOrNull() ?: "Standard"
    }

    val isWishlisted = wishlistIds.contains(item.id)
    val galleryKeys = remember(item.id, item.imageResKey) {
        listOf(item.imageResKey, "showcase")
    }
    val pagerState = rememberPagerState(pageCount = { galleryKeys.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("product_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleWishlist(item.id) },
                        modifier = Modifier.testTag("product_detail_wishlist_btn")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Wishlist",
                            tint = if (isWishlisted) NovaPink else NovaNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NovaWhite)
            )
        },
        bottomBar = {
            Surface(
                color = NovaWhite,
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.addToCart(item, selectedColor, selectedSize, 1) },
                        enabled = item.stock > 0,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_add_to_cart_btn")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = NovaPink)
                        Spacer(Modifier.width(6.dp))
                        Text("Add to Cart", color = NovaPink, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            viewModel.addToCart(item, selectedColor, selectedSize, 1) {
                                onBuyNowNavigateToCart()
                            }
                        },
                        enabled = item.stock > 0,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaPink),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_buy_now_btn")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = NovaWhite)
                        Spacer(Modifier.width(4.dp))
                        Text("Buy Now", color = NovaWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Product Image Gallery
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .background(NovaWhite)
                ) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        ProductArtwork(
                            imageResKey = galleryKeys[page],
                            customImageUri = if (page == 0) item.customImageUri else null,
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (item.discountPercent > 0) {
                        Surface(
                            color = NovaPink,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                "-${item.discountPercent}% OFF",
                                color = NovaWhite,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(galleryKeys.size) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == idx) 10.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(if (pagerState.currentPage == idx) NovaPink else NovaOutline)
                            )
                        }
                    }
                }
            }

            // 2. Price, Title, Rating, Stock & Seller
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NovaWhite)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = NovaCyanSoft, shape = CircleShape) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = NovaTurquoise, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(item.sellerName, style = MaterialTheme.typography.labelSmall, color = NovaNavy)
                            }
                        }
                        Surface(
                            color = if (item.stock > 0) NovaSuccessSoft else MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (item.stock > 0) "In Stock (${item.stock} left)" else "Out of Stock",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (item.stock > 0) NovaSuccess else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = NovaNavy
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = formatTaka(item.discountedPrice),
                            style = MaterialTheme.typography.displaySmall,
                            color = NovaPink
                        )
                        if (item.originalPrice > item.discountedPrice) {
                            Text(
                                text = formatTaka(item.originalPrice),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(5) { idx ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (idx < item.rating.toInt()) NovaGold else NovaOutline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            "${item.rating} (${item.reviewCount} reviews) • ${item.soldCount} sold",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaNavy
                        )
                    }
                }
            }

            // 3. Color & Size Selection
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NovaWhite)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select Color: $selectedColor", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(item.colors) { color ->
                            FilterChip(
                                selected = selectedColor == color,
                                onClick = { selectedColor = color },
                                label = { Text(color) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NovaPinkSoft,
                                    selectedLabelColor = NovaPink
                                )
                            )
                        }
                    }
                    Text("Select Size / Variant: $selectedSize", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(item.sizes) { size ->
                            FilterChip(
                                selected = selectedSize == size,
                                onClick = { selectedSize = size },
                                label = { Text(size) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NovaCyanSoft,
                                    selectedLabelColor = NovaNavy
                                )
                            )
                        }
                    }
                }
            }

            // 4. Delivery Information Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.horizontalGradient(listOf(NovaCyanSoft.copy(alpha = 0.5f), NovaWhite)))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Delivery & Warranty", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = NovaTurquoise, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Dhaka Metro: 24–48 hrs (${formatTaka(item.deliveryDhakaCharge)}) • Outside Dhaka: 2–4 days (${formatTaka(item.deliveryOutsideCharge)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaNavy
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NovaSuccess, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Free Delivery on orders ৳1,500+ • Cash on Delivery available",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaNavy
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NovaPink, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "7-Day Easy Return & 100% Authentic Product Guarantee",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaNavy
                            )
                        }
                    }
                }
            }

            // 5. Product Description
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NovaWhite)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Product Description", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 6. Customer Ratings & Reviews + Write Review
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NovaWhite)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Customer Reviews (${reviews.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = NovaNavy
                    )
                    // Write a review
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, NovaOutline, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Write a Review", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..5).forEach { star ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Rate $star stars",
                                    tint = if (star <= reviewRating) NovaGold else NovaOutline,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { reviewRating = star }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            placeholder = { Text("Share your experience with this product...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.submitReview(item.id, reviewRating, reviewComment)
                                reviewComment = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaNavy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Submit Review", color = NovaWhite)
                        }
                    }

                    reviews.forEach { rev ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(rev.userName, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                    Row {
                                        repeat(rev.rating) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = NovaGold, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                                Text(rev.comment, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
