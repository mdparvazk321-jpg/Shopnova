package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderStatuses
import com.example.ui.ShopNovaViewModel
import com.example.ui.components.ProductArtwork
import com.example.ui.components.formatTaka
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaOutline
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessSoft
import com.example.ui.theme.NovaTurquoise
import com.example.ui.theme.NovaWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    viewModel: ShopNovaViewModel,
    onBack: () -> Unit,
    onRequestReturn: (String) -> Unit
) {
    val order by viewModel.observeOrderDetail(orderId).collectAsStateWithLifecycle(initialValue = null)
    val items by viewModel.observeOrderItems(orderId).collectAsStateWithLifecycle(initialValue = emptyList())

    val currentOrder = order
    if (currentOrder == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading order details...")
        }
        return
    }

    val currentStepIdx = OrderStatuses.ALL.indexOf(currentOrder.orderStatus).coerceAtLeast(0)
    val dateFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track ${currentOrder.orderCode}", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tracking_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Order Header Card
            item {
                Card(
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
                            Text(currentOrder.orderCode, style = MaterialTheme.typography.titleLarge, color = NovaNavy)
                            Surface(color = NovaPinkSoft, shape = CircleShape) {
                                Text(
                                    currentOrder.orderStatus,
                                    color = NovaPink,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                        Text(
                            "Placed on ${dateFmt.format(Date(currentOrder.createdAt))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(color = NovaOutline)
                        Text(
                            "Payment: ${currentOrder.paymentMethod} • ${currentOrder.paymentStatus}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaNavy
                        )
                        if (currentOrder.paymentTrxId.isNotBlank()) {
                            Text(
                                "TrxID: ${currentOrder.paymentTrxId} (${currentOrder.paymentWalletNumber})",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaTurquoise
                            )
                        }
                        Text(
                            "Deliver to: ${currentOrder.customerName} (${currentOrder.customerPhone})\n${currentOrder.shippingAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 6-Stage Order Tracking Stepper
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = NovaTurquoise)
                            Spacer(Modifier.width(8.dp))
                            Text("Live Delivery Timeline", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                        }
                        Spacer(Modifier.height(10.dp))
                        OrderStatuses.ALL.forEachIndexed { idx, status ->
                            val isCompleted = idx <= currentStepIdx
                            val isCurrent = idx == currentStepIdx
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCurrent -> NovaPink
                                                    isCompleted -> NovaSuccess
                                                    else -> NovaOutline
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = NovaWhite,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    if (idx < OrderStatuses.ALL.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.5.dp)
                                                .height(28.dp)
                                                .background(if (idx < currentStepIdx) NovaSuccess else NovaOutline)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.padding(top = 2.dp)) {
                                    Text(
                                        text = status,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold
                                        ),
                                        color = if (isCompleted) NovaNavy else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = when (status) {
                                            OrderStatuses.PLACED -> "Order received by ShopNova BD"
                                            OrderStatuses.CONFIRMED -> "Seller & payment verified"
                                            OrderStatuses.PROCESSING -> "Packed at seller hub"
                                            OrderStatuses.SHIPPED -> "Handed over to Express Courier"
                                            OrderStatuses.OUT_FOR_DELIVERY -> "Rider is on the way to your address"
                                            OrderStatuses.DELIVERED -> "Package delivered to customer"
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Order Items
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Items (${items.size})", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                        items.forEach { oi ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProductArtwork(
                                    imageResKey = oi.imageResKey,
                                    customImageUri = oi.customImageUri,
                                    contentDescription = oi.productName,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(oi.productName, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                    Text(
                                        "${oi.selectedColor} • ${oi.selectedSize} × ${oi.quantity}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(formatTaka(oi.unitPrice * oi.quantity), style = MaterialTheme.typography.titleSmall, color = NovaPink)
                            }
                        }
                        HorizontalDivider(color = NovaOutline)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid / Due", style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                            Text(formatTaka(currentOrder.totalAmount), style = MaterialTheme.typography.titleMedium, color = NovaPink)
                        }
                    }
                }
            }

            // Easy Return Action when Delivered
            if (currentOrder.orderStatus == OrderStatuses.DELIVERED) {
                item {
                    OutlinedButton(
                        onClick = { onRequestReturn(currentOrder.id) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("initiate_return_btn")
                    ) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = NovaPink)
                        Spacer(Modifier.width(8.dp))
                        Text("Request Easy Return & Refund", color = NovaPink, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
