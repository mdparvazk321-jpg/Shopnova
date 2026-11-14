package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryEntity
import com.example.ui.ProductSortOption
import com.example.ui.ShopNovaViewModel
import com.example.ui.components.categoryIconForKey
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaOutline
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaWhite

@Composable
fun CategoriesScreen(
    viewModel: ShopNovaViewModel,
    onNavigateToProduct: (String) -> Unit
) {
    val categories by viewModel.activeCategories.collectAsStateWithLifecycle()
    val allApproved by viewModel.approvedProducts.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.wishlistProductIds.collectAsStateWithLifecycle()

    val countByCat = allApproved.groupingBy { it.categoryId }.eachCount()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val catCols = if (maxWidth >= 600.dp) 4 else 3
        val prodCols = when {
            maxWidth >= 840.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "All Categories",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NovaNavy
                    )
                    Text(
                        text = "Browse 12 departments across Bangladesh",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    placeholder = { Text("Search within categories...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaPink) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
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
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("categories_search_input")
                )
            }

            // 12-Category Grid
            val catRows = categories.chunked(catCols)
            items(catRows) { rowCats ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowCats.forEach { cat ->
                        CategoryTileCard(
                            category = cat,
                            productCount = countByCat[cat.id] ?: 0,
                            isSelected = selectedCategoryId == cat.id,
                            onClick = {
                                val next = if (selectedCategoryId == cat.id) null else cat.id
                                viewModel.selectCategory(next)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(catCols - rowCats.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            // Sort Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val catTitle = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "All Products"
                        Text(
                            text = "$catTitle (${filteredProducts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = NovaNavy
                        )
                        if (selectedCategoryId != null) {
                            Surface(
                                onClick = { viewModel.selectCategory(null) },
                                shape = RoundedCornerShape(20.dp),
                                color = NovaPinkSoft
                            ) {
                                Text(
                                    "Show All",
                                    color = NovaPink,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ProductSortOption.entries) { opt ->
                            FilterChip(
                                selected = sortOption == opt,
                                onClick = { viewModel.updateSortOption(opt) },
                                label = { Text(opt.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NovaPinkSoft,
                                    selectedLabelColor = NovaPink
                                )
                            )
                        }
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
                            "No products found in this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val rows = filteredProducts.chunked(prodCols)
                items(rows) { rowItems ->
                    ProductGridRow(
                        rowItems = rowItems,
                        gridCols = prodCols,
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

@Composable
private fun CategoryTileCard(
    category: CategoryEntity,
    productCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(category.accentHex)
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) NovaPink else NovaOutline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .testTag("category_tile_${category.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NovaPinkSoft.copy(alpha = 0.45f) else NovaWhite
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(accent.copy(alpha = 0.16f), NovaCyanSoft)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIconForKey(category.iconKey),
                    contentDescription = category.name,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = NovaNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )
            Text(
                text = "$productCount items",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
