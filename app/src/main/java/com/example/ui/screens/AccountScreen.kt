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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRoles
import com.example.ui.ShopNovaViewModel
import com.example.ui.components.NovaPinkCyanGradient
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaOutline
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaTurquoise
import com.example.ui.theme.NovaWhite

@Composable
fun AccountScreen(
    viewModel: ShopNovaViewModel,
    onOpenOrders: () -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenAddresses: () -> Unit,
    onOpenReturns: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSellerDashboard: () -> Unit,
    onOpenAdminDashboard: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentSeller by viewModel.currentSeller.collectAsStateWithLifecycle()
    val orders by viewModel.userOrders.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.wishlistProductIds.collectAsStateWithLifecycle()
    val addresses by viewModel.userAddresses.collectAsStateWithLifecycle()
    val returns by viewModel.userReturns.collectAsStateWithLifecycle()
    val unreadNotifs by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()

    var showAuthDialog by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showDeployGuide by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = NovaWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NovaPinkCyanGradient)
                        .padding(18.dp)
                ) {
                    val user = currentUser
                    if (user == null) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Welcome to ShopNova BD", style = MaterialTheme.typography.headlineSmall, color = NovaWhite)
                            Text(
                                "Sign in with your BD mobile number to manage orders, wishlist, returns, or sell products.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaWhite.copy(alpha = 0.9f)
                            )
                            Button(
                                onClick = { showAuthDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NovaWhite),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("account_signin_btn")
                            ) {
                                Text("Login / Sign Up", color = NovaPink, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = NovaWhite,
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, contentDescription = "Avatar", tint = NovaPink, modifier = Modifier.size(32.dp))
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(user.name, style = MaterialTheme.typography.titleLarge, color = NovaWhite)
                                        if (user.isPhoneVerified) {
                                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = NovaCyanSoft, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Text(user.phone, style = MaterialTheme.typography.bodySmall, color = NovaWhite.copy(alpha = 0.92f))
                                    Surface(color = NovaNavy.copy(alpha = 0.7f), shape = CircleShape) {
                                        Text(
                                            user.role,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NovaWhite,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = { showEditProfile = true },
                                modifier = Modifier.testTag("edit_profile_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = NovaWhite)
                            }
                        }
                    }
                }
            }
        }

        // Quick Role Switcher for Testing Buyer / Seller / Admin Workflows
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NovaWhite)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = NovaTurquoise, modifier = Modifier.size(18.dp))
                        Text("Account & Role Switcher", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = currentUser?.id == "user_buyer_demo",
                            onClick = { viewModel.switchDemoAccount("user_buyer_demo") },
                            label = { Text("Buyer") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NovaPinkSoft, selectedLabelColor = NovaPink)
                        )
                        FilterChip(
                            selected = currentUser?.id == "user_seller_demo",
                            onClick = { viewModel.switchDemoAccount("user_seller_demo") },
                            label = { Text("Seller") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NovaCyanSoft, selectedLabelColor = NovaNavy)
                        )
                        FilterChip(
                            selected = currentUser?.id == "user_admin_demo",
                            onClick = { viewModel.switchDemoAccount("user_admin_demo") },
                            label = { Text("Admin") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NovaPinkSoft, selectedLabelColor = NovaPink)
                        )
                    }
                    TextButton(onClick = { showAuthDialog = true }) {
                        Text("Sign In / Register New Phone Number ->", color = NovaPink)
                    }
                }
            }
        }

        // Customer Menu
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NovaWhite)
            ) {
                Column {
                    AccountMenuRow(Icons.Default.ReceiptLong, "My Orders", "${orders.size} orders", NovaPink, onOpenOrders, "menu_my_orders")
                    AccountMenuRow(Icons.Default.Favorite, "My Wishlist", "${wishlistIds.size} saved", NovaPink, onOpenWishlist, "menu_wishlist")
                    AccountMenuRow(Icons.Default.LocationOn, "Saved Addresses", "${addresses.size} addresses", NovaTurquoise, onOpenAddresses, "menu_addresses")
                    AccountMenuRow(Icons.Default.AssignmentReturn, "Return & Refund History", "${returns.size} requests", NovaTurquoise, onOpenReturns, "menu_returns")
                    AccountMenuRow(Icons.Default.Notifications, "Notifications", if (unreadNotifs > 0) "$unreadNotifs unread" else "All caught up", NovaNavy, onOpenNotifications, "menu_notifications")
                }
            }
        }

        // Seller & Admin Portals
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NovaWhite)
            ) {
                Column {
                    AccountMenuRow(
                        icon = Icons.Default.Storefront,
                        title = if (currentSeller != null) "Seller Dashboard (${currentSeller!!.shopName})" else "Become a Seller / Seller Hub",
                        subtitle = currentSeller?.status ?: "Register shop & sell across Bangladesh",
                        tint = NovaTurquoise,
                        onClick = onOpenSellerDashboard,
                        tag = "menu_seller_dashboard"
                    )
                    if (currentUser?.role == UserRoles.ADMIN) {
                        AccountMenuRow(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Admin Control Panel",
                            subtitle = "Users, Sellers, Approvals, Payments, Flash Sales",
                            tint = NovaPink,
                            onClick = onOpenAdminDashboard,
                            tag = "menu_admin_dashboard"
                        )
                    }
                    AccountMenuRow(
                        icon = Icons.Default.Info,
                        title = "Setup & Deployment Guide",
                        subtitle = "Database, payment gateway & APK build notes",
                        tint = NovaNavy,
                        onClick = { showDeployGuide = true },
                        tag = "menu_setup_guide"
                    )
                }
            }
        }

        if (currentUser != null) {
            item {
                OutlinedButton(
                    onClick = viewModel::logout,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logout_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = NovaError)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", color = NovaError, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAuthDialog) {
        PhoneAuthDialog(viewModel = viewModel, onDismiss = { showAuthDialog = false })
    }

    if (showEditProfile && currentUser != null) {
        var name by remember { mutableStateOf(currentUser!!.name) }
        var email by remember { mutableStateOf(currentUser!!.email) }
        var phone by remember { mutableStateOf(currentUser!!.phone) }
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (01XXXXXXXXX)") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(name, email, phone)
                        showEditProfile = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaPink)
                ) { Text("Save", color = NovaWhite) }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeployGuide) {
        AlertDialog(
            onDismissRequest = { showDeployGuide = false },
            title = { Text("ShopNova BD Architecture & Setup") },
            text = {
                Text(
                    "1. Local Database: Powered by Room SQLite (shopnova_bd.db) with salted SHA-256 phone authentication.\n\n" +
                        "2. Payments: Cash on Delivery works out of the box. bKash, Nagad & Rocket use TrxID verification confirmed in Admin -> Payments. Card payments require configuring live SSLCommerz merchant API keys before enabling capture.\n\n" +
                        "3. Cloud Sync: Re-enable Firebase Firestore when ready to sync multi-vendor catalogs across devices.\n\n" +
                        "4. Release Build: Generate signed APK/AAB via AI Studio Build menu or `gradle assembleRelease`."
                )
            },
            confirmButton = {
                Button(onClick = { showDeployGuide = false }, colors = ButtonDefaults.buttonColors(containerColor = NovaPink)) {
                    Text("Got It", color = NovaWhite)
                }
            }
        )
    }
}

@Composable
private fun AccountMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = RoundedCornerShape(12.dp), color = tint.copy(alpha = 0.12f), modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
