package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductStatus
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.BorderGreenTint
import com.example.ui.theme.CrimsonBadge
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepInk
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.ui.theme.MintBackground
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftMintCard
import com.example.ui.theme.VerifiedSky

@Composable
fun AdminDashboardScreen(
    users: List<UserProfile>,
    products: List<Product>,
    orders: List<Order>,
    complaints: List<Complaint>,
    isBusy: Boolean,
    onBack: () -> Unit,
    onVerifySeller: (userId: String, verified: Boolean) -> Unit,
    onToggleAccountStatus: (userId: String, currentStatus: String) -> Unit,
    onModerateProduct: (productId: String, status: ProductStatus) -> Unit,
    onResolveComplaint: (complaintId: String, status: ComplaintStatus, adminNote: String) -> Unit,
    onUpdateOrderStatus: (orderId: String, nextStatus: OrderStatus) -> Unit,
    onSeedStarterCatalog: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    var resolvingComplaint by remember { mutableStateOf<Complaint?>(null) }

    val tabs = listOf("বিক্রির রিপোর্ট", "দোকানদার ও কৃষক", "পণ্য পর্যালোচনা", "অভিযোগ", "সকল অর্ডার")

    val farmersCount = users.count { UserRole.fromRaw(it.role) == UserRole.FARMER }
    val sellersCount = users.count { UserRole.fromRaw(it.role) == UserRole.SELLER }
    val verifiedSellersCount = users.count { UserRole.fromRaw(it.role) == UserRole.SELLER && it.sellerVerified }
    val totalSales = orders.filter { OrderStatus.fromRaw(it.status) != OrderStatus.CANCELLED }.sumOf { it.totalAmount }
    val deliveredSales = orders.filter { OrderStatus.fromRaw(it.status) == OrderStatus.DELIVERED }.sumOf { it.totalAmount }
    val openComplaints = complaints.count { ComplaintStatus.fromRaw(it.status) != ComplaintStatus.RESOLVED }

    Scaffold(
        containerColor = MintBackground,
        topBar = {
            Surface(color = DeepForestGreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = PureWhite)
                    }
                    Column {
                        Text("অ্যাডমিন ড্যাশবোর্ড", style = MaterialTheme.typography.titleLarge, color = PureWhite)
                        Text("মার্কেটপ্লেস নিয়ন্ত্রণ, যাচাই ও রিপোর্ট", style = MaterialTheme.typography.labelSmall, color = LimeAccent)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = ForestGreen,
                edgePadding = 8.dp
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AdminStatCard("মোট অর্ডার মূল্য", totalSales.toBengaliPrice(), ForestGreen, Modifier.weight(1f))
                                AdminStatCard("সম্পন্ন বিক্রি", deliveredSales.toBengaliPrice(), EmeraldGreen, Modifier.weight(1f))
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AdminStatCard("মোট কৃষক", farmersCount.toBengaliDigits(), VerifiedSky, Modifier.weight(1f))
                                AdminStatCard("মোট দোকানদার", "$sellersCount (যাচাইকৃত $verifiedSellersCount)".toBengaliDigits(), Color(0xFF0F766E), Modifier.weight(1f))
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AdminStatCard("তালিকাভুক্ত পণ্য", products.size.toBengaliDigits(), ForestGreen, Modifier.weight(1f))
                                AdminStatCard("চলমান অভিযোগ", openComplaints.toBengaliDigits(), CrimsonBadge, Modifier.weight(1f))
                            }
                        }
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, BorderGreenTint)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("ক্যাটালগ ও সিস্টেম ম্যানেজমেন্ট", style = MaterialTheme.typography.titleMedium, color = DeepInk)
                                    Text(
                                        "প্রাথমিক অনুমোদিত ৮টি কৃষি পণ্য সরাসরি ক্লাউড ডেটাবেজে সংরক্ষণ ও পর্যালোচনা করতে নিচের বাটনে চাপ দিন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSage
                                    )
                                    Button(
                                        onClick = onSeedStarterCatalog,
                                        enabled = !isBusy,
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("প্রাথমিক পণ্য ডেটাবেজে সিঙ্ক করুন")
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(users, key = { it.userId }) { user ->
                            val role = UserRole.fromRaw(user.role)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, BorderGreenTint)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(user.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepInk)
                                            if (user.sellerVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Filled.Verified, contentDescription = null, tint = VerifiedSky, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Surface(color = SoftMintCard, shape = RoundedCornerShape(50)) {
                                            Text(role.bengaliLabel, style = MaterialTheme.typography.labelSmall, color = ForestGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }
                                    }
                                    Text("মোবাইল: ${user.phone} • জেলা: ${user.district}", style = MaterialTheme.typography.bodySmall, color = MutedSage)
                                    if (role == UserRole.SELLER) {
                                        Text("দোকান: ${user.shopName} • লাইসেন্স/এনআইডি: ${user.nidOrLicense}", style = MaterialTheme.typography.bodySmall, color = DeepInk)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (role == UserRole.SELLER) {
                                            Button(
                                                onClick = { onVerifySeller(user.userId, !user.sellerVerified) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (user.sellerVerified) MutedSage else EmeraldGreen
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(if (user.sellerVerified) "যাচাই বাতিল" else "বিক্রেতা যাচাই করুন")
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { onToggleAccountStatus(user.userId, user.accountStatus) },
                                            border = BorderStroke(1.dp, if (user.accountStatus == "ACTIVE") CrimsonBadge else ForestGreen),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (user.accountStatus == "ACTIVE") "স্থগিত করুন" else "সক্রিয় করুন",
                                                color = if (user.accountStatus == "ACTIVE") CrimsonBadge else ForestGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(products, key = { it.id }) { prod ->
                            val st = ProductStatus.fromRaw(prod.status)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, BorderGreenTint)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(prod.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepInk)
                                        Text(st.bengaliLabel, style = MaterialTheme.typography.labelLarge, color = if (st == ProductStatus.REMOVED) CrimsonBadge else ForestGreen)
                                    }
                                    Text("${prod.category} • ${prod.shopName} • ${prod.price.toBengaliPrice()}", style = MaterialTheme.typography.bodySmall, color = MutedSage)
                                    if (prod.approvalCode.isNotBlank()) {
                                        Text("অনুমোদন কোড: ${prod.approvalCode}", style = MaterialTheme.typography.labelSmall, color = DeepInk)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { onModerateProduct(prod.id, ProductStatus.APPROVED) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("অনুমোদন")
                                        }
                                        OutlinedButton(
                                            onClick = { onModerateProduct(prod.id, ProductStatus.REMOVED) },
                                            border = BorderStroke(1.dp, CrimsonBadge),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.Block, contentDescription = null, tint = CrimsonBadge, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("নিষিদ্ধ/অপসারণ", color = CrimsonBadge)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(complaints, key = { it.id }) { comp ->
                            val st = ComplaintStatus.fromRaw(comp.status)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, BorderGreenTint)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(comp.subject, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepInk)
                                        Text(st.bengaliLabel, style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                                    }
                                    Text("${comp.userName} (${comp.userPhone})", style = MaterialTheme.typography.labelSmall, color = MutedSage)
                                    Text(comp.message, style = MaterialTheme.typography.bodySmall, color = DeepInk)
                                    if (comp.adminNote.isNotBlank()) {
                                        Text("উত্তর: ${comp.adminNote}", style = MaterialTheme.typography.bodySmall, color = ForestGreen)
                                    }
                                    Button(
                                        onClick = { resolvingComplaint = comp },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("অভিযোগ পর্যালোচনা ও সমাধান")
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(orders, key = { it.id }) { order ->
                            val st = OrderStatus.fromRaw(order.status)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, BorderGreenTint)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${order.buyerName} -> ${order.shopName}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(st.bengaliLabel, style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                                    }
                                    Text("${order.totalAmount.toBengaliPrice()} • ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = MutedSage)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (st != OrderStatus.DELIVERED) {
                                            OutlinedButton(
                                                onClick = { onUpdateOrderStatus(order.id, OrderStatus.DELIVERED) },
                                                modifier = Modifier.weight(1f)
                                            ) { Text("ডেলিভারি") }
                                        }
                                        if (st != OrderStatus.CANCELLED) {
                                            OutlinedButton(
                                                onClick = { onUpdateOrderStatus(order.id, OrderStatus.CANCELLED) },
                                                border = BorderStroke(1.dp, CrimsonBadge),
                                                modifier = Modifier.weight(1f)
                                            ) { Text("বাতিল", color = CrimsonBadge) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    resolvingComplaint?.let { comp ->
        var note by remember(comp) { mutableStateOf(comp.adminNote) }
        AlertDialog(
            onDismissRequest = { resolvingComplaint = null },
            title = { Text("অভিযোগ সমাধান: ${comp.subject}") },
            text = {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("অ্যাডমিনের মন্তব্য / সমাধান") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResolveComplaint(comp.id, ComplaintStatus.RESOLVED, note.ifBlank { "সমাধান করা হয়েছে।" })
                        resolvingComplaint = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) { Text("সমাধান সম্পন্ন") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onResolveComplaint(comp.id, ComplaintStatus.IN_REVIEW, note)
                    resolvingComplaint = null
                }) { Text("পর্যালোচনাধীন রাখুন") }
            }
        )
    }
}

@Composable
private fun AdminStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, BorderGreenTint)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = MutedSage)
        }
    }
}
