package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ProductEntity
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaGold
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaOutline
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaTurquoise
import com.example.ui.theme.NovaWhite

fun categoryIconForKey(key: String): ImageVector = when (key) {
    "electronics", "headphones" -> Icons.Default.Headphones
    "mobile", "smartphone" -> Icons.Default.Smartphone
    "computer", "laptop" -> Icons.Default.LaptopMac
    "mens_fashion" -> Icons.Default.Checkroom
    "womens_fashion" -> Icons.Default.Diamond
    "watch" -> Icons.Default.Watch
    "beauty" -> Icons.Default.Spa
    "home_kitchen" -> Icons.Default.Kitchen
    "lifestyle" -> Icons.Default.Backpack
    "sports" -> Icons.AutoMirrored.Filled.DirectionsRun
    "essentials" -> Icons.Default.LocalGroceryStore
    "tv_appliances" -> Icons.Default.Tv
    else -> Icons.Default.Headphones
}

@Composable
fun ProductArtwork(
    imageResKey: String,
    customImageUri: String? = null,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    if (!customImageUri.isNullOrBlank()) {
        AsyncImage(
            model = customImageUri,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }

    when (imageResKey) {
        "showcase" -> {
            Image(
                painter = painterResource(id = R.drawable.img_showcase_featured),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
            return
        }
        "mega_sale" -> {
            Image(
                painter = painterResource(id = R.drawable.img_banner_mega_sale),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
            return
        }
        "flash_deals" -> {
            Image(
                painter = painterResource(id = R.drawable.img_banner_flash_deals),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
            return
        }
        "free_delivery" -> {
            Image(
                painter = painterResource(id = R.drawable.img_banner_free_delivery),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
            return
        }
    }

    val (bgColors, accentColor, icon) = when (imageResKey) {
        "headphones", "electronics" -> Triple(
            listOf(Color(0xFFFFE3F3), Color(0xFFD9FBFF)),
            NovaPink,
            Icons.Default.Headphones
        )
        "smartphone", "mobile" -> Triple(
            listOf(Color(0xFFD8F7FF), Color(0xFFEBF2FF)),
            NovaTurquoise,
            Icons.Default.Smartphone
        )
        "watch" -> Triple(
            listOf(Color(0xFFEDE9FE), Color(0xFFD9FBFF)),
            Color(0xFF7C3AED),
            Icons.Default.Watch
        )
        "laptop", "computer" -> Triple(
            listOf(Color(0xFFE2E8F0), Color(0xFFD8F7FF)),
            NovaNavy,
            Icons.Default.LaptopMac
        )
        "mens_fashion" -> Triple(
            listOf(Color(0xFFDBEAFE), Color(0xFFE0FAFC)),
            Color(0xFF2563EB),
            Icons.Default.Checkroom
        )
        "womens_fashion" -> Triple(
            listOf(Color(0xFFFFD8EC), Color(0xFFFCE7F3)),
            NovaPink,
            Icons.Default.Diamond
        )
        "beauty" -> Triple(
            listOf(Color(0xFFFFE4E6), Color(0xFFFFF1F2)),
            Color(0xFFF43F5E),
            Icons.Default.Spa
        )
        "home_kitchen" -> Triple(
            listOf(Color(0xFFD1FAE5), Color(0xFFE0FAFC)),
            Color(0xFF059669),
            Icons.Default.Kitchen
        )
        "lifestyle" -> Triple(
            listOf(Color(0xFFE0FAFC), Color(0xFFFFE5F4)),
            NovaTurquoise,
            Icons.Default.Backpack
        )
        "sports" -> Triple(
            listOf(Color(0xFFFEF3C7), Color(0xFFD8F7FF)),
            Color(0xFFD97706),
            Icons.AutoMirrored.Filled.DirectionsRun
        )
        "essentials" -> Triple(
            listOf(Color(0xFFDCFCE7), Color(0xFFE0FAFC)),
            Color(0xFF10B981),
            Icons.Default.LocalGroceryStore
        )
        "tv_appliances" -> Triple(
            listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9)),
            NovaNavy,
            Icons.Default.Tv
        )
        else -> Triple(
            listOf(NovaPinkSoft, NovaCyanSoft),
            NovaPink,
            Icons.Default.Headphones
        )
    }

    Box(
        modifier = modifier.background(Brush.linearGradient(bgColors)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = accentColor.copy(alpha = 0.12f),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = accentColor,
            modifier = Modifier.size(46.dp)
        )
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    isWishlisted: Boolean,
    showFlashProgress: Boolean = false,
    onClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = NovaPink.copy(alpha = 0.10f),
                spotColor = NovaNavy.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NovaWhite)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                ProductArtwork(
                    imageResKey = product.imageResKey,
                    customImageUri = product.customImageUri,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize()
                )

                if (product.discountPercent > 0) {
                    Surface(
                        color = NovaPink,
                        shape = RoundedCornerShape(bottomEnd = 12.dp, topStart = 18.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            color = NovaWhite,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    onClick = onToggleWishlist,
                    shape = CircleShape,
                    color = NovaWhite.copy(alpha = 0.92f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .testTag("wishlist_btn_${product.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) NovaPink else NovaNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = product.categoryName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = NovaTurquoise,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = NovaNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = NovaGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${product.rating}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NovaNavy,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(${product.reviewCount})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showFlashProgress) {
                    val progress = (product.soldCount.toFloat() / product.flashSaleTargetStock.coerceAtLeast(1))
                        .coerceIn(0.08f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = NovaPink,
                            trackColor = NovaPinkSoft
                        )
                        Text(
                            text = "${product.soldCount} sold • ${(progress * 100).toInt()}% claimed",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = NovaPink
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatTaka(product.discountedPrice),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = NovaPink
                        )
                        if (product.originalPrice > product.discountedPrice) {
                            Text(
                                text = formatTaka(product.originalPrice),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(12.dp),
                        color = if (product.stock > 0) NovaCyanSoft else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(38.dp)
                            .border(
                                1.dp,
                                if (product.stock > 0) NovaCyan.copy(alpha = 0.5f) else NovaOutline,
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("add_cart_btn_${product.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AddShoppingCart,
                                contentDescription = "Add to Cart",
                                tint = if (product.stock > 0) NovaNavy else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
