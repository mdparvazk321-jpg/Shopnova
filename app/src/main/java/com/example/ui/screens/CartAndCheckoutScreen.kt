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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AddressEntity
import com.example.data.model.BangladeshDivisions
import com.example.data.model.OrderEntity
import com.example.data.model.PaymentMethods
import com.example.ui.CartDisplayItem
import com.example.ui.ShopNovaViewModel
import com.example.ui.components.ProductArtwork
import com.example.ui.components.formatTaka
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaOutline
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessSoft
import com.example.ui.theme.NovaTurquoise
import com.example.ui.theme.NovaWarning
import com.example.ui.theme.NovaWarningSoft
import com.example.ui.theme.NovaWhite
import kotlin.math.roundToInt

@Composable
fun CartAndCheckoutScreen(
    viewModel: ShopNovaViewModel,
    onNavigateToProduct: (String) -> Unit,
    onExploreShop: () -> Unit,
    onOrderPlacedNavigateToTracking: (String) -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val addresses by viewModel.userAddresses.collectAsStateWithLifecycle()
    val coupons by viewModel.activeCoupons.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var couponInput by remember { mutableStateOf("") }
    var selectedAddressId by remember(addresses) {
        mutableStateOf(addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id)
    }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethods.COD) }
    var walletNumber by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }
    var trxId by remember { mutableStateOf("") }
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var confirmedOrder by remember { mutableStateOf<OrderEntity?>(null) }

    val selectedAddress = addresses.firstOrNull { it.id == selectedAddressId } ?: addresses.firstOrNull()

    val subtotal = cartItems.sumOf { it.product.discountedPrice * it.cartItem.quantity }
    val baseDelivery = if (selectedAddress?.isDhakaMetro != false) 60.0 else 120.0
    var deliveryFee = if (subtotal == 0.0 || subtotal >= 1500.0) 0.0 else baseDelivery
    var couponDiscount = 0.0
    appliedCoupon?.let { coupon ->
        if (subtotal >= coupon.minOrderAmount) {
            when (coupon.discountType) {
                "PERCENT" -> couponDiscount = (subtotal * (coupon.discountValue / 100.0)).roundToInt().toDouble()
                "FLAT" -> couponDiscount = coupon.discountValue.coerceAtMost(subtotal)
                "FREESHIP" -> deliveryFee = 0.0
            }
        }
    }
    val total = (subtotal - couponDiscount + deliveryFee).coerceAtLeast(0.0)

    if (cartItems.isEmpty() && confirmedOrder == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = NovaPinkSoft,
                    modifier = Modifier.size(88.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = "Empty cart",
                            tint = NovaPink,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
                Text("Your Cart is Empty", style = MaterialTheme.typography.headlineSmall, color = NovaNavy)
                Text(
                    "Discover flash deals, smartphones, Panjabi, watches and more across Bangladesh.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onExploreShop,
                    colors = ButtonDefaults.buttonColors(containerColor = NovaPink),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("empty_cart_shop_btn")
                ) {
                    Text("Start Shopping", color = NovaWhite)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Cart & Checkout", style = MaterialTheme.typography.headlineMedium, color = NovaNavy)
        }

        // Cart Items
        items(cartItems, key = { it.cartItem.id }) { displayItem ->
            CartItemCard(
                item = displayItem,
                onClickProduct = { onNavigateToProduct(displayItem.product.id) },
                onDecrease = { viewModel.updateCartQuantity(displayItem.cartItem, displayItem.cartItem.quantity - 1) },
                onIncrease = {
                    if (displayItem.cartItem.quantity < displayItem.product.stock) {
                        viewModel.updateCartQuantity(displayItem.cartItem, displayItem.cartItem.quantity + 1)
                    } else {
                        viewModel.emitMessage("Reached available stock (${displayItem.product.stock})")
                    }
                },
                onRemove = { viewModel.removeCartItem(displayItem.cartItem.id) }
            )
        }

        // Coupon Code Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NovaWhite)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = NovaPink, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Apply Coupon Code", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it.uppercase() },
                            placeholder = { Text("Enter e.g. NOVA15") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("coupon_code_input")
                        )
                        Button(
                            onClick = { viewModel.applyCouponCode(couponInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaNavy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("apply_coupon_btn")
                        ) {
                            Text("Apply", color = NovaWhite)
                        }
                    }
                    if (appliedCoupon != null) {
                        Surface(
                            color = NovaSuccessSoft,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Applied ${appliedCoupon!!.code} (${appliedCoupon!!.title})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NovaSuccess
                                )
                                TextButton(onClick = viewModel::removeCoupon) {
                                    Text("Remove", color = NovaError)
                                }
                            }
                        }
                    }
                    if (coupons.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(coupons) { c ->
                                FilterChip(
                                    selected = appliedCoupon?.code == c.code,
                                    onClick = {
                                        couponInput = c.code
                                        viewModel.applyCouponCode(c.code)
                                    },
                                    label = { Text("${c.code} • ${c.title}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NovaPinkSoft,
                                        selectedLabelColor = NovaPink
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Delivery Address Selector
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NovaWhite)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = NovaTurquoise)
                            Spacer(Modifier.width(6.dp))
                            Text("Delivery Address", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                        }
                        TextButton(
                            onClick = { showAddAddressDialog = true },
                            modifier = Modifier.testTag("checkout_add_address_btn")
                        ) {
                            Text("+ Add New", color = NovaPink)
                        }
                    }

                    if (addresses.isEmpty()) {
                        OutlinedButton(
                            onClick = { showAddAddressDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Add Delivery Address to Checkout", color = NovaPink)
                        }
                    } else {
                        addresses.forEach { addr ->
                            val isSelected = addr.id == selectedAddress?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.5.dp,
                                        if (isSelected) NovaPink else NovaOutline,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedAddressId = addr.id }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedAddressId = addr.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = NovaPink)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(addr.label, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                        Surface(color = NovaCyanSoft, shape = CircleShape) {
                                            Text(
                                                if (addr.isDhakaMetro) "Dhaka ৳60" else "Outside ৳120",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NovaNavy,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text("${addr.recipientName} • ${addr.phone}", style = MaterialTheme.typography.bodySmall, color = NovaNavy)
                                    Text(
                                        "${addr.streetAddress}, ${addr.area}, ${addr.district}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bangladesh Payment System
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NovaWhite)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = NovaPink)
                        Spacer(Modifier.width(6.dp))
                        Text("Bangladesh Payment Options", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                    }

                    PaymentMethods.ALL.forEach { method ->
                        val selected = selectedPaymentMethod == method
                        val badgeColor = when (method) {
                            PaymentMethods.BKASH -> Color(0xFFE2136E)
                            PaymentMethods.NAGAD -> Color(0xFFF6921E)
                            PaymentMethods.ROCKET -> Color(0xFF8C3494)
                            PaymentMethods.COD -> NovaSuccess
                            else -> NovaNavy
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.5.dp,
                                    if (selected) badgeColor else NovaOutline,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedPaymentMethod = method }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("payment_option_$method"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = { selectedPaymentMethod = method },
                                colors = RadioButtonDefaults.colors(selectedColor = badgeColor)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                Text(
                                    when (method) {
                                        PaymentMethods.COD -> "Pay cash when rider delivers to your door"
                                        PaymentMethods.BKASH, PaymentMethods.NAGAD, PaymentMethods.ROCKET ->
                                            "Send/Pay to Merchant Wallet & enter TrxID for verification"
                                        else -> "Visa / Mastercard / SSLCommerz Merchant Gateway"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // MFS TrxID input for bKash / Nagad / Rocket
                    if (selectedPaymentMethod in listOf(PaymentMethods.BKASH, PaymentMethods.NAGAD, PaymentMethods.ROCKET)) {
                        Surface(
                            color = NovaCyanSoft.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "$selectedPaymentMethod Merchant Payment Verification",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NovaNavy
                                )
                                Text(
                                    "1. Open $selectedPaymentMethod App -> Payment / Send Money to Merchant: 01700-000000\n2. Amount: ${formatTaka(total)}\n3. Enter your wallet number & TrxID below. Orders remain Pending Verification until Admin confirms.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NovaNavy
                                )
                                OutlinedTextField(
                                    value = walletNumber,
                                    onValueChange = { walletNumber = it },
                                    label = { Text("Your $selectedPaymentMethod Number (01XXXXXXXXX)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("mfs_wallet_input")
                                )
                                OutlinedTextField(
                                    value = trxId,
                                    onValueChange = { trxId = it.uppercase() },
                                    label = { Text("Transaction ID (TrxID)") },
                                    placeholder = { Text("e.g. BKS84920XY") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("mfs_trxid_input")
                                )
                            }
                        }
                    }

                    // Honest Card Payment Gateway Status (never simulate fake card approval!)
                    if (selectedPaymentMethod == PaymentMethods.CARD) {
                        Surface(
                            color = NovaWarningSoft,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = NovaWarning)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "Card Gateway Requires Merchant Credentials",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = NovaNavy
                                    )
                                    Text(
                                        "Live Visa/Mastercard/SSLCommerz processing is not connected in this local build. To avoid unverified payment simulation, please select Cash on Delivery or submit a bKash/Nagad/Rocket TrxID.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NovaNavy
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Price Summary & Place Order
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NovaWhite)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Order Summary", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                    SummaryRow("Subtotal (${cartItems.sumOf { it.cartItem.quantity }} items)", formatTaka(subtotal))
                    SummaryRow(
                        "Delivery Fee",
                        if (deliveryFee == 0.0) "FREE" else formatTaka(deliveryFee),
                        valueColor = if (deliveryFee == 0.0) NovaSuccess else NovaNavy
                    )
                    if (couponDiscount > 0.0) {
                        SummaryRow("Coupon (${appliedCoupon?.code})", "-${formatTaka(couponDiscount)}", valueColor = NovaPink)
                    }
                    HorizontalDivider(color = NovaOutline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Payable", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                        Text(
                            formatTaka(total),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = NovaPink
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = {
                            val addr = selectedAddress
                            if (addr == null) {
                                viewModel.emitMessage("Please add a delivery address first.")
                            } else {
                                viewModel.placeOrder(
                                    address = addr,
                                    paymentMethod = selectedPaymentMethod,
                                    walletNumber = walletNumber,
                                    trxId = trxId
                                ) { order ->
                                    trxId = ""
                                    confirmedOrder = order
                                }
                            }
                        },
                        enabled = selectedPaymentMethod != PaymentMethods.CARD,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaPink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("place_order_btn")
                    ) {
                        Text(
                            text = if (selectedPaymentMethod == PaymentMethods.CARD) {
                                "Select COD or MFS to Place Order"
                            } else {
                                "Confirm Order • ${formatTaka(total)}"
                            },
                            color = NovaWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showAddAddressDialog) {
        AddAddressDialog(
            defaultName = currentUser?.name ?: "",
            defaultPhone = currentUser?.phone ?: "",
            onDismiss = { showAddAddressDialog = false },
            onSave = { label, name, phone, div, dist, area, street, isDef ->
                viewModel.saveAddress(label, name, phone, div, dist, area, street, isDef)
                showAddAddressDialog = false
            }
        )
    }

    confirmedOrder?.let { order ->
        AlertDialog(
            onDismissRequest = { confirmedOrder = null },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NovaSuccess,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Order Placed! ${order.orderCode}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Total: ${formatTaka(order.totalAmount)}")
                    Text("Payment: ${order.paymentMethod} (${order.paymentStatus})")
                    Text("Delivering to: ${order.shippingAddress}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = order.id
                        confirmedOrder = null
                        onOrderPlacedNavigateToTracking(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaPink)
                ) {
                    Text("Track Order", color = NovaWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmedOrder = null }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartDisplayItem,
    onClickProduct: () -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NovaWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductArtwork(
                imageResKey = item.product.imageResKey,
                customImageUri = item.product.customImageUri,
                contentDescription = item.product.name,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onClickProduct)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.product.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = NovaNavy,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove item", tint = NovaError, modifier = Modifier.size(18.dp))
                    }
                }
                Text(
                    "${item.cartItem.selectedColor} • ${item.cartItem.selectedSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatTaka(item.product.discountedPrice * item.cartItem.quantity),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = NovaPink
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, NovaOutline, RoundedCornerShape(10.dp))
                    ) {
                        IconButton(onClick = onDecrease, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease quantity", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            "${item.cartItem.quantity}",
                            style = MaterialTheme.typography.titleSmall,
                            color = NovaNavy,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(onClick = onIncrease, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Increase quantity", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color = NovaNavy) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, color = valueColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressDialog(
    defaultName: String,
    defaultPhone: String,
    existing: AddressEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, Boolean) -> Unit
) {
    var label by remember { mutableStateOf(existing?.label ?: "Home") }
    var name by remember { mutableStateOf(existing?.recipientName ?: defaultName) }
    var phone by remember { mutableStateOf(existing?.phone ?: defaultPhone) }
    var division by remember { mutableStateOf(existing?.division ?: "Dhaka") }
    var district by remember { mutableStateOf(existing?.district ?: "Dhaka") }
    var area by remember { mutableStateOf(existing?.area ?: "Dhanmondi") }
    var street by remember { mutableStateOf(existing?.streetAddress ?: "") }
    var isDefault by remember { mutableStateOf(existing?.isDefault ?: true) }
    var divExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Delivery Address" else "Edit Address") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (Home/Office)") }, singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Recipient Name") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (01XXXXXXXXX)") }, singleLine = true)
                ExposedDropdownMenuBox(expanded = divExpanded, onExpandedChange = { divExpanded = it }) {
                    OutlinedTextField(
                        value = division,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Division") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = divExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = divExpanded, onDismissRequest = { divExpanded = false }) {
                        BangladeshDivisions.ALL.forEach { div ->
                            DropdownMenuItem(
                                text = { Text(div) },
                                onClick = {
                                    division = div
                                    if (div != "Dhaka" && district == "Dhaka") district = div
                                    divExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, singleLine = true)
                OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area / Thana") }, singleLine = true)
                OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("House, Road & Flat") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    Text("Set as default delivery address", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && street.isNotBlank()) {
                        onSave(label, name, phone, division, district, area, street, isDefault)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaPink)
            ) {
                Text("Save", color = NovaWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
