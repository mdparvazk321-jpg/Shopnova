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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AddressEntity
import com.example.data.model.OrderStatuses
import com.example.data.model.PaymentStatuses
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(
    viewModel: ShopNovaViewModel,
    onBack: () -> Unit,
    onTrackOrder: (String) -> Unit
) {
    val orders by viewModel.userOrders.collectAsStateWithLifecycle()
    val fmt = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders (${orders.size})", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NovaWhite)
            )
        }
    ) { innerPadding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No orders placed yet.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackOrder(order.id) }
                            .testTag("order_card_${order.orderCode}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NovaWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(order.orderCode, style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                                Surface(
                                    color = if (order.orderStatus == OrderStatuses.DELIVERED) NovaSuccessSoft else NovaPinkSoft,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        order.orderStatus,
                                        color = if (order.orderStatus == OrderStatuses.DELIVERED) NovaSuccess else NovaPink,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(fmt.format(Date(order.createdAt)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (order.paymentStatus == PaymentStatuses.PAID) NovaSuccessSoft else NovaWarningSoft,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "${order.paymentMethod} • ${order.paymentStatus}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (order.paymentStatus == PaymentStatuses.PAID) NovaSuccess else NovaWarning,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(formatTaka(order.totalAmount), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = NovaPink)
                            }
                            OutlinedButton(
                                onClick = { onTrackOrder(order.id) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = NovaNavy, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Track Order & Details", color = NovaNavy)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    viewModel: ShopNovaViewModel,
    onBack: () -> Unit,
    onNavigateToProduct: (String) -> Unit
) {
    val wishlistIds by viewModel.wishlistProductIds.collectAsStateWithLifecycle()
    val products by viewModel.approvedProducts.collectAsStateWithLifecycle()
    val wishedProducts = products.filter { wishlistIds.contains(it.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wishlist (${wishedProducts.size})", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NovaWhite)
            )
        }
    ) { innerPadding ->
        if (wishedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = NovaPink, modifier = Modifier.size(48.dp))
                    Text("Your Wishlist is Empty", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                    Text("Tap the heart icon on any product to save it here.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wishedProducts, key = { it.id }) { prod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProduct(prod.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NovaWhite)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductArtwork(
                                imageResKey = prod.imageResKey,
                                customImageUri = prod.customImageUri,
                                contentDescription = prod.name,
                                modifier = Modifier
                                    .size(74.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(prod.name, style = MaterialTheme.typography.titleSmall, color = NovaNavy, maxLines = 2)
                                Text(formatTaka(prod.discountedPrice), style = MaterialTheme.typography.titleMedium, color = NovaPink)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                IconButton(onClick = { viewModel.toggleWishlist(prod.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remove from wishlist", tint = NovaError)
                                }
                                Button(
                                    onClick = { viewModel.addToCart(prod) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NovaPink),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Add", color = NovaWhite)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedAddressesScreen(
    viewModel: ShopNovaViewModel,
    onBack: () -> Unit
) {
    val addresses by viewModel.userAddresses.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var editingAddress by remember { mutableStateOf<AddressEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Addresses", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAdd = true }, modifier = Modifier.testTag("addresses_add_btn")) {
                        Icon(Icons.Default.Add, contentDescription = "Add Address", tint = NovaPink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NovaWhite)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(addresses, key = { it.id }) { addr ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaWhite)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(addr.label, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                if (addr.isDefault) {
                                    Surface(color = NovaPinkSoft, shape = CircleShape) {
                                        Text("Default", color = NovaPink, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Row {
                                IconButton(onClick = { editingAddress = addr }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit address", tint = NovaNavy, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { viewModel.deleteAddress(addr.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete address", tint = NovaError, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        Text("${addr.recipientName} • ${addr.phone}", style = MaterialTheme.typography.bodySmall, color = NovaNavy)
                        Text("${addr.streetAddress}, ${addr.area}, ${addr.district}, ${addr.division}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!addr.isDefault) {
                            TextButton(onClick = { viewModel.setDefaultAddress(addr) }) {
                                Text("Set as Default", color = NovaPink)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd || editingAddress != null) {
        AddAddressDialog(
            defaultName = user?.name ?: "",
            defaultPhone = user?.phone ?: "",
            existing = editingAddress,
            onDismiss = {
                showAdd = false
                editingAddress = null
            },
            onSave = { label, name, phone, div, dist, area, street, isDef ->
                viewModel.saveAddress(label, name, phone, div, dist, area, street, isDef, editingAddress?.id)
                showAdd = false
                editingAddress = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: ShopNovaViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.userNotifications.collectAsStateWithLifecycle()
    val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::markAllNotificationsRead) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = NovaPink, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Read All", color = NovaPink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NovaWhite)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notifications, key = { it.id }) { n ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.markNotificationRead(n.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (n.isRead) NovaWhite else NovaCyanSoft.copy(alpha = 0.55f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (n.type == "PROMO") NovaPinkSoft else NovaCyanSoft,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (n.type == "PROMO") NovaPink else NovaTurquoise,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(n.title, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                Text(fmt.format(Date(n.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(n.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneAuthDialog(
    viewModel: ShopNovaViewModel,
    onDismiss: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Sign Up
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var issuedOtp by remember { mutableStateOf<String?>(null) }
    var enteredOtp by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = NovaPink)
                Text("ShopNova BD Account")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TabRow(selectedTabIndex = tab, containerColor = NovaWhite) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Login") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Sign Up") })
                }

                if (tab == 0) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone (01XXXXXXXXX)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_login_phone")
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("PIN / Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_login_pin")
                    )
                    Surface(color = NovaCyanSoft, shape = RoundedCornerShape(10.dp)) {
                        Text(
                            "Demo Accounts (PIN: 1234):\n• Buyer: 01711223344\n• Seller: 01819556677\n• Admin: 01911000000",
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaNavy,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("BD Phone (01XXXXXXXXX)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("Create PIN (min 4 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = enteredOtp,
                            onValueChange = { enteredOtp = it },
                            label = { Text("6-Digit Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = {
                            issuedOtp = Random.nextInt(100000, 999999).toString()
                        }) {
                            Text("Get Code", color = NovaPink)
                        }
                    }
                    issuedOtp?.let { code ->
                        Surface(color = NovaSuccessSoft, shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NovaSuccess, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Local verification code: $code", style = MaterialTheme.typography.labelSmall, color = NovaSuccess)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tab == 0) {
                        viewModel.login(phone, pin) { onDismiss() }
                    } else {
                        if (issuedOtp == null || enteredOtp != issuedOtp) {
                            viewModel.emitMessage("Tap 'Get Code' and enter the 6-digit verification code.")
                        } else {
                            viewModel.signUp(name, phone, email, pin, enteredOtp) { onDismiss() }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaPink)
            ) {
                Text(if (tab == 0) "Login" else "Verify & Create", color = NovaWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
