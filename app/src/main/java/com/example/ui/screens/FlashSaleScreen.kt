package com.example.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.ShopNovaViewModel
import com.example.ui.components.CountdownPillBox
import com.example.ui.components.formatCountdown
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaTurquoise
import com.example.ui.theme.NovaWhite

@Composable
fun FlashSaleScreen(
    viewModel: ShopNovaViewModel,
    onNavigateToProduct: (String) -> Unit
) {
    val products by viewModel.approvedProducts.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.wishlistProductIds.collectAsStateWithLifecycle()
    val nowMillis by viewModel.currentTimeMillis.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("All") }
    val flashProducts = remember(products, selectedTab) {
        val base = products.filter { it.isFlashSale }
        when (selectedTab) {
            "Under ৳3,000" -> base.filter { it.discountedPrice <= 3000.0 }
            "Tech & Audio" -> base.filter { it.categoryId in listOf("cat_electronics", "cat_mobiles", "cat_watches") }
            "Fashion & Beauty" -> base.filter { it.categoryId in listOf("cat_mens", "cat_womens", "cat_beauty", "cat_sports") }
            else -> base
        }
    }
    val flashEnd = flashProducts.maxOfOrNull { it.flashSaleEndMillis } ?: (nowMillis + 3600_000L)
    val (hrs, mins, secs) = formatCountdown(flashEnd, nowMillis)

    val infiniteTransition = rememberInfiniteTransition(label = "flash_banner")
    val animatedPink by infiniteTransition.animateColor(
        initialValue = NovaPink,
        targetValue = Color(0xFFFF389C),
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "pink_pulse"
    )
    val animatedCyan by infiniteTransition.animateColor(
        initialValue = NovaTurquoise,
        targetValue = NovaCyan,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "cyan_pulse"
    )

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated Promotional Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(190.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_banner_flash_deals),
                            contentDescription = "Flash Sale Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            animatedPink.copy(alpha = 0.88f),
                                            NovaNavy.copy(alpha = 0.55f),
                                            animatedCyan.copy(alpha = 0.35f)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = NovaWhite) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = NovaPink,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            "LIVE FLASH DEALS",
                                            color = NovaPink,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold)
                                        )
                                    }
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Lightning Drops Up to 45% Off",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = NovaWhite
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        "Ending in",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = NovaCyanSoft
                                    )
                                    CountdownPillBox(hrs, mins, secs)
                                }
                            }
                        }
                    }
                }
            }

            // Filter tabs
            item {
                val tabs = listOf("All", "Under ৳3,000", "Tech & Audio", "Fashion & Beauty")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tabs) { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaPinkSoft,
                                selectedLabelColor = NovaPink
                            )
                        )
                    }
                }
            }

            if (flashProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No flash deals in this filter right now.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val rows = flashProducts.chunked(gridCols)
                items(rows) { rowItems ->
                    ProductGridRow(
                        rowItems = rowItems,
                        gridCols = gridCols,
                        wishlistIds = wishlistIds,
                        showFlashProgress = true,
                        onProductClick = onNavigateToProduct,
                        onToggleWishlist = viewModel::toggleWishlist,
                        onAddToCart = { viewModel.addToCart(it) }
                    )
                }
            }
        }
    }
}
