package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ApprovalStatus
import com.example.data.model.CategoryEntity
import com.example.data.model.OrderStatuses
import com.example.data.model.ProductEntity
import com.example.data.model.SellerEntity
import com.example.ui.ShopNovaViewModel
import com.example.ui.components.ProductArtwork
import com.example.ui.components.formatTaka
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessSoft
import com.example.ui.theme.NovaTurquoise
import com.example.ui.theme.NovaWarning
import com.example.ui.theme.NovaWarningSoft
import com.example.ui.theme.NovaWhite
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    viewModel: ShopNovaViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val seller by viewModel.currentSeller.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allOrderItems by viewModel.allOrderItems.collectAsStateWithLifecycle()

    val sellerProducts by remember(seller?.id) {
        seller?.id?.let { viewModel.observeSellerProducts(it) } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    var showProductEditor by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(seller?.shopName ?: "Seller Hub", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("seller_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NovaWhite)
            )
        },
        floatingActionButton = {
            if (seller != null && selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showProductEditor = true
                    },
                    containerColor = NovaPink,
                    contentColor = NovaWhite,
                    modifier = Modifier.testTag("seller_add_product_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { innerPadding ->
        val activeSeller = seller
        if (currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Please sign in from Account first to access the Seller Dashboard.")
            }
            return@Scaffold
        }

        if (activeSeller == null) {
            SellerRegistrationForm(
                defaultOwner = currentUser!!.name,
                defaultPhone = currentUser!!.phone,
                onSubmit = { shop, owner, phone, addr, nid, payMethod, payAcc ->
                    viewModel.registerAsSeller(shop, owner, phone, addr, nid, payMethod, payAcc)
                },
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        val sellerOrderIds = remember(allOrderItems, activeSeller.id) {
            allOrderItems.filter { it.sellerId == activeSeller.id }.map { it.orderId }.toSet()
        }
        val sellerOrders = remember(allOrders, sellerOrderIds) {
            allOrders.filter { sellerOrderIds.contains(it.id) }
        }
        val grossSales = remember(allOrderItems, activeSeller.id) {
            allOrderItems.filter { it.sellerId == activeSeller.id }.sumOf { it.unitPrice * it.quantity }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (activeSeller.status != ApprovalStatus.APPROVED) {
                Surface(color = NovaWarningSoft, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Store Status: ${activeSeller.status}. Admin approval is required before new products go live.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaNavy,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            TabRow(selectedTabIndex = selectedTab, containerColor = NovaWhite) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Reports & Earnings") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Products (${sellerProducts.size})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Orders (${sellerOrders.size})") })
            }

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                KpiStatCard("Total Earnings", formatTaka(activeSeller.totalEarnings + grossSales * 0.92), NovaPink, Modifier.weight(1f))
                                KpiStatCard("Pending Payout", formatTaka(activeSeller.pendingBalance), NovaTurquoise, Modifier.weight(1f))
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                KpiStatCard("Total Orders", "${sellerOrders.size}", NovaNavy, Modifier.weight(1f))
                                KpiStatCard("Units Sold", "${sellerProducts.sumOf { it.soldCount }}", NovaSuccess, Modifier.weight(1f))
                            }
                        }
                        item {
                            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Top Selling Products Report", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                                    val chartProducts = sellerProducts.sortedByDescending { it.soldCount }.take(5)
                                    val maxSold = (chartProducts.maxOfOrNull { it.soldCount } ?: 1).coerceAtLeast(1)
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                    ) {
                                        val count = chartProducts.size.coerceAtLeast(1)
                                        val barWidth = (size.width / count) * 0.52f
                                        val spacing = size.width / count
                                        chartProducts.forEachIndexed { idx, p ->
                                            val hRatio = p.soldCount.toFloat() / maxSold
                                            val barH = (size.height * 0.85f * hRatio).coerceAtLeast(12f)
                                            val left = idx * spacing + (spacing - barWidth) / 2f
                                            drawRoundRect(
                                                brush = Brush.verticalGradient(listOf(NovaPink, NovaCyan)),
                                                topLeft = Offset(left, size.height - barH),
                                                size = Size(barWidth, barH),
                                                cornerRadius = CornerRadius(12f, 12f)
                                            )
                                        }
                                    }
                                    chartProducts.forEach { p ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(p.name, style = MaterialTheme.typography.bodySmall, color = NovaNavy, modifier = Modifier.weight(1f))
                                            Text("${p.soldCount} sold", style = MaterialTheme.typography.labelSmall, color = NovaPink)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Button(
                                onClick = {
                                    editingProduct = null
                                    showProductEditor = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NovaPink),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = NovaWhite)
                                Spacer(Modifier.width(6.dp))
                                Text("Add New Product", color = NovaWhite)
                            }
                        }
                        items(sellerProducts, key = { it.id }) { prod ->
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProductArtwork(
                                        imageResKey = prod.imageResKey,
                                        customImageUri = prod.customImageUri,
                                        contentDescription = prod.name,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(prod.name, style = MaterialTheme.typography.titleSmall, color = NovaNavy, modifier = Modifier.weight(1f))
                                            Surface(
                                                color = if (prod.approvalStatus == ApprovalStatus.APPROVED) NovaSuccessSoft else NovaWarningSoft,
                                                shape = CircleShape
                                            ) {
                                                Text(
                                                    prod.approvalStatus,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (prod.approvalStatus == ApprovalStatus.APPROVED) NovaSuccess else NovaWarning,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text("${formatTaka(prod.discountedPrice)} • Sold: ${prod.soldCount}", style = MaterialTheme.typography.bodySmall, color = NovaPink)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Stock: ${prod.stock}", style = MaterialTheme.typography.labelMedium, color = NovaNavy)
                                            IconButton(onClick = { viewModel.adjustProductStock(prod, -1) }, modifier = Modifier.size(30.dp)) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease stock", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { viewModel.adjustProductStock(prod, +5) }, modifier = Modifier.size(30.dp)) {
                                                Icon(Icons.Default.Add, contentDescription = "Add 5 stock", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = {
                                                editingProduct = prod
                                                showProductEditor = true
                                            }, modifier = Modifier.size(30.dp)) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit product", tint = NovaTurquoise, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sellerOrders, key = { it.id }) { order ->
                            val nextIdx = (OrderStatuses.ALL.indexOf(order.orderStatus) + 1)
                                .coerceAtMost(OrderStatuses.ALL.lastIndex)
                            val nextStatus = OrderStatuses.ALL[nextIdx]
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(order.orderCode, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                        Text(order.orderStatus, style = MaterialTheme.typography.labelMedium, color = NovaPink)
                                    }
                                    Text("${order.customerName} • ${order.shippingAddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Total: ${formatTaka(order.totalAmount)} • ${order.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = NovaNavy)
                                    if (order.orderStatus != OrderStatuses.DELIVERED) {
                                        OutlinedButton(
                                            onClick = { viewModel.advanceOrderStatus(order, nextStatus) },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Mark as $nextStatus", color = NovaPink)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showProductEditor) {
            SellerProductEditorDialog(
                existing = editingProduct,
                categories = categories,
                onDismiss = { showProductEditor = false },
                onSave = { cat, name, desc, orig, disc, stock, colors, sizes, imgKey, uri ->
                    viewModel.saveSellerProduct(
                        editingProduct?.id, activeSeller, cat, name, desc, orig, disc, stock, colors, sizes, imgKey, uri
                    )
                    showProductEditor = false
                }
            )
        }
    }
}

@Composable
private fun SellerRegistrationForm(
    defaultOwner: String,
    defaultPhone: String,
    onSubmit: (String, String, String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf(defaultOwner) }
    var phone by remember { mutableStateOf(defaultPhone) }
    var pickupAddress by remember { mutableStateOf("") }
    var tradeLicense by remember { mutableStateOf("") }
    var payoutMethod by remember { mutableStateOf("bKash") }
    var payoutAccount by remember { mutableStateOf(defaultPhone) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = NovaPink)
                        Text("Register as a Seller on ShopNova BD", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                    }
                    Text("Reach customers across all 64 districts of Bangladesh.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text("Shop / Store Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Business Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pickupAddress, onValueChange = { pickupAddress = it }, label = { Text("Warehouse / Pickup Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tradeLicense, onValueChange = { tradeLicense = it }, label = { Text("Trade License or NID Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = payoutMethod, onValueChange = { payoutMethod = it }, label = { Text("Payout Method (bKash/Nagad/Bank)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = payoutAccount, onValueChange = { payoutAccount = it }, label = { Text("Payout Account Number") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (shopName.isNotBlank() && pickupAddress.isNotBlank()) {
                                onSubmit(shopName, ownerName, phone, pickupAddress, tradeLicense, payoutMethod, payoutAccount)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaPink),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Submit Seller Application", color = NovaWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerProductEditorDialog(
    existing: ProductEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity, String, String, Double, Double, Int, String, String, String, String?) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull { it.id == existing?.categoryId } ?: categories.first())
    }
    var catExpanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var desc by remember { mutableStateOf(existing?.description ?: "") }
    var origPrice by remember { mutableStateOf(existing?.originalPrice?.toInt()?.toString() ?: "") }
    var discPrice by remember { mutableStateOf(existing?.discountedPrice?.toInt()?.toString() ?: "") }
    var stock by remember { mutableStateOf(existing?.stock?.toString() ?: "20") }
    var colors by remember { mutableStateOf(existing?.colorsCsv ?: "Midnight Navy,Pink") }
    var sizes by remember { mutableStateOf(existing?.sizesCsv ?: "Standard") }
    var customUri by remember { mutableStateOf(existing?.customImageUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}
                customUri = uri.toString()
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add New Product" else "Edit Product") },
        text = {
            LazyColumn(
                modifier = Modifier.height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NovaPink)
                        Spacer(Modifier.width(6.dp))
                        Text(if (customUri != null) "Change Product Photo" else "Upload Product Image", color = NovaPink)
                    }
                }
                item {
                    ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                        OutlinedTextField(
                            value = selectedCategory.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat.name) }, onClick = {
                                    selectedCategory = cat
                                    catExpanded = false
                                })
                            }
                        }
                    }
                }
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Title") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = origPrice, onValueChange = { origPrice = it }, label = { Text("Original Price (৳)") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = discPrice, onValueChange = { discPrice = it }, label = { Text("Sale Price (৳)") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock Quantity") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = colors, onValueChange = { colors = it }, label = { Text("Colors (comma-separated)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = sizes, onValueChange = { sizes = it }, label = { Text("Sizes (comma-separated)") }, modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val o = origPrice.toDoubleOrNull() ?: 0.0
                    val d = discPrice.toDoubleOrNull() ?: o
                    val s = stock.toIntOrNull() ?: 10
                    if (name.isNotBlank() && d > 0) {
                        onSave(
                            selectedCategory, name, desc.ifBlank { name },
                            o.coerceAtLeast(d), d, s, colors, sizes,
                            selectedCategory.iconKey, customUri
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaPink)
            ) { Text("Save Product", color = NovaWhite) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun KpiStatCard(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NovaWhite)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = accent)
        }
    }
}
