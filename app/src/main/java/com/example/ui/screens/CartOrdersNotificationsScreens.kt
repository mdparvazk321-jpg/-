package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.toBengaliDigits
import com.example.data.toBengaliPrice
import com.example.model.CartItem
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.UserNotification
import com.example.model.UserProfile
import com.example.ui.components.ProductVisual
import com.example.ui.theme.BorderGreenTint
import com.example.ui.theme.CrimsonBadge
import com.example.ui.theme.DeepInk
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintBackground
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftMintCard

@Composable
fun CartCheckoutScreen(
    cartItems: List<CartItem>,
    userProfile: UserProfile?,
    isBusy: Boolean,
    onBack: () -> Unit,
    onUpdateQuantity: (productId: String, newQty: Long) -> Unit,
    onPlaceOrder: (name: String, phone: String, address: String, paymentMethod: String) -> Unit
) {
    BackHandler(onBack = onBack)

    var buyerName by remember(userProfile) { mutableStateOf(userProfile?.name ?: "") }
    var buyerPhone by remember(userProfile) { mutableStateOf(userProfile?.phone ?: "") }
    var deliveryAddress by remember(userProfile) {
        mutableStateOf(userProfile?.district?.takeIf { it.isNotBlank() } ?: "")
    }
    var paymentMethod by remember { mutableStateOf("ক্যাশ অন ডেলিভারি") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val subtotal = cartItems.sumOf { it.price * it.quantity }

    Scaffold(
        containerColor = MintBackground,
        topBar = {
            Surface(color = ForestGreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("cart_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = PureWhite)
                    }
                    Text(
                        text = "শপিং কার্ট ও অর্ডার (${cartItems.size.toBengaliDigits()})",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = MutedSage,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("আপনার কার্ট বর্তমানে খালি", style = MaterialTheme.typography.titleLarge, color = DeepInk)
                    Text("হোম বা ক্যাটাগরি থেকে প্রয়োজনীয় কৃষি পণ্য যোগ করুন", style = MaterialTheme.typography.bodyMedium, color = MutedSage)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cartItems, key = { it.productId }) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, BorderGreenTint)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductVisual(
                                imageKey = item.imageKey,
                                imageBase64 = item.imageBase64,
                                productName = item.productName,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, style = MaterialTheme.typography.titleSmall, color = DeepInk)
                                Text(item.shopName, style = MaterialTheme.typography.bodySmall, color = MutedSage)
                                Text(
                                    "${item.price.toBengaliPrice()} × ${item.quantity.toBengaliDigits()} (${item.unit})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ForestGreen
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onUpdateQuantity(item.productId, item.quantity - 1L) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.quantity <= 1L) Icons.Filled.Delete else Icons.Filled.Remove,
                                        contentDescription = "কমান",
                                        tint = if (item.quantity <= 1L) CrimsonBadge else ForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(item.quantity.toBengaliDigits(), fontWeight = FontWeight.Bold)
                                IconButton(
                                    onClick = { onUpdateQuantity(item.productId, item.quantity + 1L) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "বাড়ান", tint = ForestGreen, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, BorderGreenTint)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("ডেলিভারি ও পেমেন্ট তথ্য", style = MaterialTheme.typography.titleMedium, color = DeepInk)
                            OutlinedTextField(
                                value = buyerName,
                                onValueChange = { buyerName = it },
                                label = { Text("প্রাপকের নাম *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = buyerPhone,
                                onValueChange = { buyerPhone = it },
                                label = { Text("মোবাইল নম্বর *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = deliveryAddress,
                                onValueChange = { deliveryAddress = it },
                                label = { Text("গ্রাম / বাজার / উপজেলা / জেলা *") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("পেমেন্ট পদ্ধতি", style = MaterialTheme.typography.labelLarge, color = DeepInk)
                            listOf("ক্যাশ অন ডেলিভারি", "বিকাশ / নগদ পেমেন্ট").forEach { method ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { paymentMethod = method },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = paymentMethod == method,
                                        onClick = { paymentMethod = method }
                                    )
                                    Text(method, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            if (errorText != null) {
                                Text(errorText.orEmpty(), color = CrimsonBadge, style = MaterialTheme.typography.bodySmall)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoftMintCard, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("মোট পরিশোধযোগ্য:", style = MaterialTheme.typography.titleMedium, color = DeepInk)
                                Text(subtotal.toBengaliPrice(), style = MaterialTheme.typography.titleLarge, color = ForestGreen)
                            }

                            Button(
                                onClick = {
                                    when {
                                        buyerName.trim().length < 2 -> errorText = "নাম লিখুন।"
                                        buyerPhone.trim().length < 6 -> errorText = "সঠিক মোবাইল নম্বর দিন।"
                                        deliveryAddress.trim().length < 4 -> errorText = "সম্পূর্ণ ডেলিভারি ঠিকানা লিখুন।"
                                        else -> {
                                            errorText = null
                                            onPlaceOrder(buyerName, buyerPhone, deliveryAddress, paymentMethod)
                                        }
                                    }
                                },
                                enabled = !isBusy,
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("checkout_confirm_button")
                            ) {
                                if (isBusy) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = PureWhite)
                                } else {
                                    Text("অর্ডার নিশ্চিত করুন", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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
fun OrdersScreen(
    orders: List<Order>,
    onCancelOrder: (Order) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MintBackground)
    ) {
        Surface(
            color = ForestGreen,
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text("আমার অর্ডার সমূহ", style = MaterialTheme.typography.headlineSmall, color = PureWhite)
                Text("অর্ডারের ইতিহাস ও লাইভ ডেলিভারি অবস্থা", style = MaterialTheme.typography.bodySmall, color = Color(0xFFDFF6E5))
            }
        }

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = MutedSage, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("এখনো কোনো অর্ডার করা হয়নি", style = MaterialTheme.typography.titleMedium, color = DeepInk)
                    Text("পণ্য কার্টে যোগ করে সহজেই অর্ডার করুন", style = MaterialTheme.typography.bodySmall, color = MutedSage)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    val status = OrderStatus.fromRaw(order.status)
                    val statusColor = when (status) {
                        OrderStatus.DELIVERED -> ForestGreen
                        OrderStatus.SHIPPED -> Color(0xFF0284C7)
                        OrderStatus.CONFIRMED -> EmeraldGreen
                        OrderStatus.CANCELLED -> CrimsonBadge
                        OrderStatus.PENDING -> Color(0xFFD97706)
                    }
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, BorderGreenTint)
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
                                Text(order.shopName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepInk)
                                Surface(
                                    color = statusColor.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = status.bengaliLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            order.parsedItems().forEach { line ->
                                Text(
                                    text = "• ${line.name} (${line.quantity.toBengaliDigits()} × ${line.price.toBengaliPrice()})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSage
                                )
                            }
                            Text("ঠিকানা: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = MutedSage)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("মোট: ${order.totalAmount.toBengaliPrice()}", style = MaterialTheme.typography.titleMedium, color = ForestGreen)
                                Text(order.paymentMethod, style = MaterialTheme.typography.labelSmall, color = MutedSage)
                            }
                            if (status == OrderStatus.PENDING) {
                                OutlinedButton(
                                    onClick = { onCancelOrder(order) },
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, CrimsonBadge),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("অর্ডার বাতিল করুন", color = CrimsonBadge)
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
fun NotificationsScreen(
    notifications: List<UserNotification>,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = MintBackground,
        topBar = {
            Surface(color = ForestGreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notif_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = PureWhite)
                    }
                    Text("নোটিফিকেশন", style = MaterialTheme.typography.titleLarge, color = PureWhite)
                }
            }
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = MutedSage, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("নতুন কোনো নোটিফিকেশন নেই", style = MaterialTheme.typography.titleMedium, color = DeepInk)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (!notif.readStatus) onMarkRead(notif.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.readStatus) PureWhite else SoftMintCard
                        ),
                        border = BorderStroke(1.dp, BorderGreenTint)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = if (notif.readStatus) MutedSage else EmeraldGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepInk)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(notif.body, style = MaterialTheme.typography.bodySmall, color = MutedSage)
                            }
                            if (!notif.readStatus) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .background(CrimsonBadge, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
