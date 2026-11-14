package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ApprovalStatus
import com.example.data.model.BannerEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CouponEntity
import com.example.data.model.OrderStatuses
import com.example.data.model.PaymentStatuses
import com.example.data.model.ReturnStatuses
import com.example.data.model.UserRoles
import com.example.ui.ShopNovaViewModel
import com.example.ui.components.formatTaka
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessSoft
import com.example.ui.theme.NovaTurquoise
import com.example.ui.theme.NovaWarning
import com.example.ui.theme.NovaWarningSoft
import com.example.ui.theme.NovaWhite
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: ShopNovaViewModel,
    onBack: () -> Unit
) {
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val returns by viewModel.allReturns.collectAsStateWithLifecycle()
    val banners by viewModel.allBanners.collectAsStateWithLifecycle()
    val coupons by viewModel.allCoupons.collectAsStateWithLifecycle()

    val tabs = listOf(
        "Sales", "Products", "Sellers", "Users",
        "Orders", "Payments", "Refunds", "Flash Sale", "Banners & Cats", "Coupons"
    )
    var selectedTab by remember { mutableIntStateOf(0) }

    val totalRevenue = orders.sumOf { it.totalAmount }
    val pendingPayments = orders.filter { it.paymentStatus == PaymentStatuses.PENDING_VERIFICATION }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Control Panel", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NovaWhite)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = NovaWhite, edgePadding = 12.dp) {
                tabs.forEachIndexed { idx, label ->
                    Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(label) })
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    // 0. Total Sales & Revenue
                    0 -> {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                KpiStatCard("Total Revenue", formatTaka(totalRevenue), NovaPink, Modifier.weight(1f))
                                KpiStatCard("Total Orders", "${orders.size}", NovaTurquoise, Modifier.weight(1f))
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                KpiStatCard("Active Users", "${users.count { it.isActive }}", NovaNavy, Modifier.weight(1f))
                                KpiStatCard("Sellers", "${sellers.size}", NovaSuccess, Modifier.weight(1f))
                            }
                        }
                        item {
                            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Revenue by Order", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                                    val chartOrders = orders.take(6)
                                    val maxAmt = (chartOrders.maxOfOrNull { it.totalAmount } ?: 1.0).coerceAtLeast(1.0)
                                    Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                                        val count = chartOrders.size.coerceAtLeast(1)
                                        val spacing = size.width / count
                                        val barW = spacing * 0.5f
                                        chartOrders.forEachIndexed { idx, ord ->
                                            val h = ((ord.totalAmount / maxAmt) * size.height * 0.85f).toFloat().coerceAtLeast(14f)
                                            drawRoundRect(
                                                brush = Brush.verticalGradient(listOf(NovaPink, NovaCyan)),
                                                topLeft = Offset(idx * spacing + (spacing - barW) / 2f, size.height - h),
                                                size = Size(barW, h),
                                                cornerRadius = CornerRadius(10f, 10f)
                                            )
                                        }
                                    }
                                    chartOrders.forEach { o ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${o.orderCode} (${o.orderStatus})", style = MaterialTheme.typography.bodySmall, color = NovaNavy)
                                            Text(formatTaka(o.totalAmount), style = MaterialTheme.typography.labelMedium, color = NovaPink)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 1. Approve or Reject Products
                    1 -> {
                        items(products, key = { it.id }) { prod ->
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(prod.name, style = MaterialTheme.typography.titleSmall, color = NovaNavy, modifier = Modifier.weight(1f))
                                        StatusBadge(prod.approvalStatus)
                                    }
                                    Text("${prod.sellerName} • ${prod.categoryName} • ${formatTaka(prod.discountedPrice)}", style = MaterialTheme.typography.bodySmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.updateProductApproval(prod, ApprovalStatus.APPROVED) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NovaSuccess),
                                            shape = RoundedCornerShape(10.dp)
                                        ) { Text("Approve", color = NovaWhite) }
                                        OutlinedButton(
                                            onClick = { viewModel.updateProductApproval(prod, ApprovalStatus.REJECTED) },
                                            shape = RoundedCornerShape(10.dp)
                                        ) { Text("Reject", color = NovaError) }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Manage Sellers
                    2 -> {
                        items(sellers, key = { it.id }) { seller ->
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(seller.shopName, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                        StatusBadge(seller.status)
                                    }
                                    Text("Owner: ${seller.ownerName} • ${seller.phone}", style = MaterialTheme.typography.bodySmall)
                                    Text("License/NID: ${seller.tradeLicenseOrNid} • Payout: ${seller.payoutMethod}", style = MaterialTheme.typography.labelSmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.updateSellerStatus(seller, ApprovalStatus.APPROVED) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NovaSuccess)
                                        ) { Text("Approve", color = NovaWhite) }
                                        OutlinedButton(onClick = { viewModel.updateSellerStatus(seller, ApprovalStatus.SUSPENDED) }) {
                                            Text("Suspend", color = NovaError)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Manage Users
                    3 -> {
                        items(users, key = { it.id }) { u ->
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${u.name} (${u.role})", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                        Text(if (u.isActive) "ACTIVE" else "SUSPENDED", color = if (u.isActive) NovaSuccess else NovaError, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Text("${u.phone} • ${u.email}", style = MaterialTheme.typography.bodySmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { viewModel.toggleUserActive(u) }) {
                                            Text(if (u.isActive) "Suspend" else "Activate", color = NovaPink)
                                        }
                                        val nextRole = when (u.role) {
                                            UserRoles.BUYER -> UserRoles.SELLER
                                            UserRoles.SELLER -> UserRoles.ADMIN
                                            else -> UserRoles.BUYER
                                        }
                                        OutlinedButton(onClick = { viewModel.updateUserRole(u, nextRole) }) {
                                            Text("Set $nextRole", color = NovaNavy)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Manage Orders
                    4 -> {
                        items(orders, key = { it.id }) { order ->
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(order.orderCode, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                        Text(formatTaka(order.totalAmount), style = MaterialTheme.typography.titleSmall, color = NovaPink)
                                    }
                                    Text("${order.customerName} • ${order.orderStatus}", style = MaterialTheme.typography.bodySmall)
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(OrderStatuses.ALL) { st ->
                                            FilterChip(
                                                selected = order.orderStatus == st,
                                                onClick = { viewModel.advanceOrderStatus(order, st) },
                                                label = { Text(st) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NovaPinkSoft, selectedLabelColor = NovaPink)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Manage Payments
                    5 -> {
                        item {
                            Text("Pending MFS Verifications: ${pendingPayments.size}", style = MaterialTheme.typography.titleSmall, color = NovaPink)
                        }
                        items(orders, key = { it.id }) { order ->
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${order.orderCode} • ${order.paymentMethod}", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                        Text(formatTaka(order.totalAmount), style = MaterialTheme.typography.titleSmall, color = NovaPink)
                                    }
                                    Text("Status: ${order.paymentStatus}", style = MaterialTheme.typography.bodySmall)
                                    if (order.paymentTrxId.isNotBlank()) {
                                        Text("Wallet: ${order.paymentWalletNumber} • TrxID: ${order.paymentTrxId}", style = MaterialTheme.typography.labelSmall, color = NovaTurquoise)
                                    }
                                    if (order.paymentStatus == PaymentStatuses.PENDING_VERIFICATION) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.verifyPayment(order, true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = NovaSuccess)
                                            ) { Text("Confirm Payment", color = NovaWhite) }
                                            OutlinedButton(onClick = { viewModel.verifyPayment(order, false) }) {
                                                Text("Reject", color = NovaError)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Manage Refunds
                    6 -> {
                        items(returns, key = { it.id }) { ret ->
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${ret.returnCode} • Order ${ret.orderCode}", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                    Text("Refund: ${formatTaka(ret.refundAmount)} -> ${ret.refundMethod} (${ret.refundAccount})", style = MaterialTheme.typography.bodySmall)
                                    Text("Reason: ${ret.reason} • Status: ${ret.status}", style = MaterialTheme.typography.bodySmall, color = NovaPink)
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(ReturnStatuses.ALL) { st ->
                                            FilterChip(
                                                selected = ret.status == st,
                                                onClick = { viewModel.updateReturnStatus(ret, st) },
                                                label = { Text(st) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 7. Create Flash Sales
                    7 -> {
                        items(products, key = { it.id }) { prod ->
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                        Text(
                                            "${formatTaka(prod.discountedPrice)} (-${prod.discountPercent}%)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NovaPink
                                        )
                                    }
                                    Switch(
                                        checked = prod.isFlashSale,
                                        onCheckedChange = { enable ->
                                            viewModel.toggleProductFlashSale(prod, enable, discountPercent = 35, hours = 8)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 8. Banners & Categories
                    8 -> {
                        item {
                            var newBannerTitle by remember { mutableStateOf("") }
                            var newBannerSub by remember { mutableStateOf("") }
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Add Promotional Banner", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                    OutlinedTextField(value = newBannerTitle, onValueChange = { newBannerTitle = it }, label = { Text("Banner Title") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = newBannerSub, onValueChange = { newBannerSub = it }, label = { Text("Subtitle") }, modifier = Modifier.fillMaxWidth())
                                    Button(
                                        onClick = {
                                            if (newBannerTitle.isNotBlank()) {
                                                viewModel.saveBanner(
                                                    BannerEntity(
                                                        id = "banner_${UUID.randomUUID().toString().take(6)}",
                                                        title = newBannerTitle,
                                                        subtitle = newBannerSub.ifBlank { "Special ShopNova BD Offer" },
                                                        badgeText = "NEW PROMO",
                                                        imageKey = "mega_sale",
                                                        sortOrder = banners.size + 1
                                                    )
                                                )
                                                newBannerTitle = ""
                                                newBannerSub = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NovaPink)
                                    ) { Text("Publish Banner", color = NovaWhite) }
                                }
                            }
                        }
                        items(banners, key = { it.id }) { b ->
                            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(b.title, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                        Text(b.subtitle, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Switch(checked = b.isActive, onCheckedChange = { viewModel.saveBanner(b.copy(isActive = it)) })
                                }
                            }
                        }
                        item { Text("Categories (${categories.size})", style = MaterialTheme.typography.titleSmall, color = NovaNavy) }
                        items(categories, key = { it.id }) { c ->
                            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(c.name, style = MaterialTheme.typography.titleSmall, color = NovaNavy, modifier = Modifier.weight(1f))
                                    Switch(checked = c.isActive, onCheckedChange = { viewModel.saveCategory(c.copy(isActive = it)) })
                                }
                            }
                        }
                    }

                    // 9. Coupons
                    9 -> {
                        item {
                            var code by remember { mutableStateOf("") }
                            var title by remember { mutableStateOf("") }
                            var pct by remember { mutableStateOf("10") }
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Create Discount Coupon", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                    OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Coupon Code (e.g. DHAKA20)") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Offer Title") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = pct, onValueChange = { pct = it }, label = { Text("Discount %") }, modifier = Modifier.fillMaxWidth())
                                    Button(
                                        onClick = {
                                            if (code.isNotBlank()) {
                                                viewModel.saveCoupon(
                                                    CouponEntity(
                                                        id = "cpn_${UUID.randomUUID().toString().take(6)}",
                                                        code = code,
                                                        title = title.ifBlank { "$pct% Off" },
                                                        discountType = "PERCENT",
                                                        discountValue = pct.toDoubleOrNull() ?: 10.0,
                                                        minOrderAmount = 500.0,
                                                        isActive = true
                                                    )
                                                )
                                                code = ""
                                                title = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NovaPink)
                                    ) { Text("Add Coupon", color = NovaWhite) }
                                }
                            }
                        }
                        items(coupons, key = { it.id }) { c ->
                            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = NovaWhite)) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${c.code} • ${c.title}", style = MaterialTheme.typography.titleSmall, color = NovaPink)
                                        Text("Min Order: ${formatTaka(c.minOrderAmount)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Switch(checked = c.isActive, onCheckedChange = { viewModel.saveCoupon(c.copy(isActive = it)) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val bg = when (status) {
        ApprovalStatus.APPROVED -> NovaSuccessSoft
        ApprovalStatus.PENDING -> NovaWarningSoft
        else -> NovaPinkSoft
    }
    val fg = when (status) {
        ApprovalStatus.APPROVED -> NovaSuccess
        ApprovalStatus.PENDING -> NovaWarning
        else -> NovaError
    }
    Surface(color = bg, shape = CircleShape) {
        Text(status, color = fg, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}
