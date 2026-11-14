package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatuses
import com.example.data.model.PaymentMethods
import com.example.data.model.ReturnStatuses
import com.example.ui.ShopNovaViewModel
import com.example.ui.components.formatTaka
import com.example.ui.theme.NovaCyanSoft
import com.example.ui.theme.NovaErrorSoft
import com.example.ui.theme.NovaNavy
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPinkSoft
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSuccessSoft
import com.example.ui.theme.NovaWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnRefundScreen(
    preselectedOrderId: String? = null,
    viewModel: ShopNovaViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.userOrders.collectAsStateWithLifecycle()
    val returns by viewModel.userReturns.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val deliveredOrders = orders.filter { it.orderStatus == OrderStatuses.DELIVERED }
    var dialogOrder by remember(preselectedOrderId, deliveredOrders) {
        mutableStateOf(deliveredOrders.firstOrNull { it.id == preselectedOrderId })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Easy Return & Refund", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("return_back_btn")) {
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
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("7-Day Easy Return Policy", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                        Text(
                            "Return damaged, defective, or mismatched items from any Delivered order. Refunds are returned to your original bKash, Nagad, Rocket, or Store Wallet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (deliveredOrders.isNotEmpty()) {
                item {
                    Text("Eligible Delivered Orders", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
                }
                items(deliveredOrders, key = { it.id }) { order ->
                    val alreadyRequested = returns.any { it.orderId == order.id }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NovaWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(order.orderCode, style = MaterialTheme.typography.titleSmall, color = NovaNavy)
                                Text(
                                    "${formatTaka(order.totalAmount)} • ${order.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (alreadyRequested) {
                                Surface(color = NovaCyanSoft, shape = CircleShape) {
                                    Text(
                                        "Return Filed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NovaNavy,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { dialogOrder = order },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("start_return_${order.orderCode}")
                                ) {
                                    Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = NovaPink)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Return", color = NovaPink)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Return & Refund History (${returns.size})", style = MaterialTheme.typography.titleMedium, color = NovaNavy)
            }

            if (returns.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NovaWhite,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "No return requests yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(returns, key = { it.id }) { ret ->
                    val statusBg = when (ret.status) {
                        ReturnStatuses.REFUNDED -> NovaSuccessSoft
                        ReturnStatuses.REJECTED -> NovaErrorSoft
                        else -> NovaPinkSoft
                    }
                    val statusText = when (ret.status) {
                        ReturnStatuses.REFUNDED -> NovaSuccess
                        ReturnStatuses.REJECTED -> MaterialTheme.colorScheme.error
                        else -> NovaPink
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NovaWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ret.returnCode, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = NovaNavy)
                                Surface(color = statusBg, shape = CircleShape) {
                                    Text(
                                        ret.status,
                                        color = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text("Order: ${ret.orderCode} • Refund: ${formatTaka(ret.refundAmount)}", style = MaterialTheme.typography.bodySmall, color = NovaNavy)
                            Text("Reason: ${ret.reason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Refund To: ${ret.refundMethod} (${ret.refundAccount})", style = MaterialTheme.typography.labelSmall, color = NovaPink)
                            if (ret.comments.isNotBlank()) {
                                Text("Note: ${ret.comments}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    dialogOrder?.let { order ->
        InitiateReturnDialog(
            order = order,
            defaultPhone = currentUser?.phone ?: order.customerPhone,
            onDismiss = { dialogOrder = null },
            onSubmit = { reason, comments, refundMethod, refundAcc ->
                viewModel.submitReturn(order, reason, comments, refundMethod, refundAcc) {
                    dialogOrder = null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InitiateReturnDialog(
    order: OrderEntity,
    defaultPhone: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    val reasons = listOf(
        "Damaged or Broken Product",
        "Wrong Item Received",
        "Size or Color Mismatch",
        "Product Not as Described",
        "Defective / Not Working"
    )
    val refundMethods = listOf(
        order.paymentMethod.takeIf { it in listOf(PaymentMethods.BKASH, PaymentMethods.NAGAD, PaymentMethods.ROCKET) }
            ?: PaymentMethods.BKASH,
        PaymentMethods.NAGAD,
        PaymentMethods.ROCKET,
        "Original Cash / Store Wallet"
    ).distinct()

    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var comments by remember { mutableStateOf("") }
    var refundMethod by remember { mutableStateOf(refundMethods.first()) }
    var refundAccount by remember { mutableStateOf(order.paymentWalletNumber.ifBlank { defaultPhone }) }
    var reasonExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Return ${order.orderCode}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Refund Amount: ${formatTaka(order.totalAmount)}", style = MaterialTheme.typography.titleSmall, color = NovaPink)
                ExposedDropdownMenuBox(expanded = reasonExpanded, onExpandedChange = { reasonExpanded = it }) {
                    OutlinedTextField(
                        value = selectedReason,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Return Reason") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = reasonExpanded, onDismissRequest = { reasonExpanded = false }) {
                        reasons.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = {
                                selectedReason = r
                                reasonExpanded = false
                            })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = methodExpanded, onExpandedChange = { methodExpanded = it }) {
                    OutlinedTextField(
                        value = refundMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Refund Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                        refundMethods.forEach { m ->
                            DropdownMenuItem(text = { Text(m) }, onClick = {
                                refundMethod = m
                                methodExpanded = false
                            })
                        }
                    }
                }
                OutlinedTextField(
                    value = refundAccount,
                    onValueChange = { refundAccount = it },
                    label = { Text("Wallet / Account Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text("Additional Details") },
                    placeholder = { Text("Describe the issue...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedReason, comments, refundMethod, refundAccount) },
                colors = ButtonDefaults.buttonColors(containerColor = NovaPink)
            ) {
                Text("Submit Return", color = NovaWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
