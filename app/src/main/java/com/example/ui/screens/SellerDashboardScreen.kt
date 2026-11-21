package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.ImageUtils
import com.example.data.toBengaliDigits
import com.example.data.toBengaliPrice
import com.example.model.AgroCategories
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.UserProfile
import com.example.ui.components.ProductVisual
import com.example.ui.theme.BorderGreenTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepInk
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.ui.theme.MintBackground
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.VibrantGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    profile: UserProfile?,
    sellerProducts: List<Product>,
    sellerOrders: List<Order>,
    isBusy: Boolean,
    onBack: () -> Unit,
    onSaveProduct: (
        productId: String?,
        name: String,
        category: String,
        price: Double,
        originalPrice: Double,
        unit: String,
        stock: Long,
        description: String,
        safetyGuide: String,
        approvalCode: String,
        imageKey: String,
        imageBase64: String,
        badge: String,
        onDone: () -> Unit
    ) -> Unit,
    onQuickUpdateStockPrice: (productId: String, price: Double, stock: Long) -> Unit,
    onUpdateOrderStatus: (orderId: String, nextStatus: OrderStatus) -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var quickEditProduct by remember { mutableStateOf<Product?>(null) }

    val totalRevenue = sellerOrders
        .filter { OrderStatus.fromRaw(it.status) == OrderStatus.DELIVERED }
        .sumOf { it.totalAmount }
    val activeOrders = sellerOrders.count {
        val st = OrderStatus.fromRaw(it.status)
        st == OrderStatus.PENDING || st == OrderStatus.CONFIRMED || st == OrderStatus.SHIPPED
    }

    Scaffold(
        containerColor = MintBackground,
        topBar = {
            Surface(color = ForestGreen) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("seller_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = PureWhite)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile?.shopName?.ifBlank { "দোকানদার ড্যাশবোর্ড" } ?: "দোকানদার ড্যাশবোর্ড",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PureWhite
                                )
                                if (profile?.sellerVerified == true) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Filled.Verified, contentDescription = "যাচাইকৃত", tint = LimeAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(
                                text = if (profile?.sellerVerified == true) "পরিচয় যাচাইকৃত বিক্রেতা"
                                else "পরিচয় যাচাইয়ের অপেক্ষায় (অ্যাডমিন অনুমোদন)",
                                style = MaterialTheme.typography.labelSmall,
                                color = LimeAccent
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showAddEditSheet = true
                    },
                    containerColor = EmeraldGreen,
                    contentColor = PureWhite,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("seller_add_product_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "নতুন পণ্য যোগ করুন")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sales Summary Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SellerMetricCard("মোট বিক্রি", totalRevenue.toBengaliPrice(), Modifier.weight(1f))
                SellerMetricCard("চলমান অর্ডার", activeOrders.toBengaliDigits(), Modifier.weight(1f))
                SellerMetricCard("মোট পণ্য", sellerProducts.size.toBengaliDigits(), Modifier.weight(1f))
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = ForestGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("পণ্য ও স্টক (${sellerProducts.size.toBengaliDigits()})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("অর্ডার সমূহ (${sellerOrders.size.toBengaliDigits()})") }
                )
            }

            if (selectedTab == 0) {
                if (sellerProducts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("আপনার দোকানে এখনো কোনো পণ্য যুক্ত হয়নি", style = MaterialTheme.typography.titleMedium, color = DeepInk)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                editingProduct = null
                                showAddEditSheet = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("প্রথম পণ্য যোগ করুন")
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sellerProducts, key = { it.id }) { prod ->
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
                                        imageKey = prod.imageKey,
                                        imageBase64 = prod.imageBase64,
                                        productName = prod.name,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepInk)
                                        Text("${prod.category} • ${prod.price.toBengaliPrice()}", style = MaterialTheme.typography.bodySmall, color = ForestGreen)
                                        Text("স্টক: ${prod.stock.toBengaliDigits()}টি • অবস্থা: ${prod.status}", style = MaterialTheme.typography.labelSmall, color = MutedSage)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        IconButton(onClick = {
                                            editingProduct = prod
                                            showAddEditSheet = true
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "সম্পাদনা", tint = ForestGreen)
                                        }
                                        Text(
                                            text = "দাম/স্টক",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldGreen,
                                            modifier = Modifier
                                                .clickable { quickEditProduct = prod }
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (sellerOrders.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("এখনো কোনো ক্রেতার অর্ডার আসেনি", style = MaterialTheme.typography.titleMedium, color = MutedSage)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sellerOrders, key = { it.id }) { order ->
                            val st = OrderStatus.fromRaw(order.status)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, BorderGreenTint)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("ক্রেতা: ${order.buyerName}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(st.bengaliLabel, style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                                    }
                                    Text("মোবাইল: ${order.buyerPhone} • ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = MutedSage)
                                    order.parsedItems().forEach { line ->
                                        Text("• ${line.name} × ${line.quantity.toBengaliDigits()}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text("মোট মূল্য: ${order.totalAmount.toBengaliPrice()}", style = MaterialTheme.typography.titleSmall, color = ForestGreen)

                                    if (st != OrderStatus.DELIVERED && st != OrderStatus.CANCELLED) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (st == OrderStatus.PENDING) {
                                                Button(
                                                    onClick = { onUpdateOrderStatus(order.id, OrderStatus.CONFIRMED) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                    modifier = Modifier.weight(1f)
                                                ) { Text("নিশ্চিত করুন") }
                                            }
                                            if (st == OrderStatus.CONFIRMED) {
                                                Button(
                                                    onClick = { onUpdateOrderStatus(order.id, OrderStatus.SHIPPED) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen),
                                                    modifier = Modifier.weight(1f)
                                                ) { Text("পাঠিয়ে দিন") }
                                            }
                                            if (st == OrderStatus.SHIPPED) {
                                                Button(
                                                    onClick = { onUpdateOrderStatus(order.id, OrderStatus.DELIVERED) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                                    modifier = Modifier.weight(1f)
                                                ) { Text("ডেলিভারি সম্পন্ন") }
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
    }

    if (showAddEditSheet) {
        ModalBottomSheet(onDismissRequest = { showAddEditSheet = false }) {
            AddEditProductForm(
                initial = editingProduct,
                isBusy = isBusy,
                onSubmit = { name, cat, price, origPrice, unit, stock, desc, safety, code, imgKey, imgB64, badge ->
                    onSaveProduct(
                        editingProduct?.id,
                        name, cat, price, origPrice, unit, stock, desc, safety, code, imgKey, imgB64, badge
                    ) {
                        showAddEditSheet = false
                    }
                },
                onDismiss = { showAddEditSheet = false }
            )
        }
    }

    quickEditProduct?.let { target ->
        var priceText by remember(target) { mutableStateOf(target.price.toInt().toString()) }
        var stockText by remember(target) { mutableStateOf(target.stock.toString()) }
        AlertDialog(
            onDismissRequest = { quickEditProduct = null },
            title = { Text("দাম ও স্টক আপডেট: ${target.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("নতুন দাম (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("স্টক সংখ্যা") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = priceText.toDoubleOrNull() ?: target.price
                        val s = stockText.toLongOrNull() ?: target.stock
                        if (p > 0 && s >= 0) {
                            onQuickUpdateStockPrice(target.id, p, s)
                            quickEditProduct = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) { Text("সংরক্ষণ") }
            },
            dismissButton = {
                TextButton(onClick = { quickEditProduct = null }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
private fun SellerMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, BorderGreenTint)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = ForestGreen)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MutedSage)
        }
    }
}

@Composable
private fun AddEditProductForm(
    initial: Product?,
    isBusy: Boolean,
    onSubmit: (
        name: String, category: String, price: Double, origPrice: Double,
        unit: String, stock: Long, desc: String, safety: String,
        code: String, imgKey: String, imgB64: String, badge: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: AgroCategories.all.first().name) }
    var priceStr by remember { mutableStateOf(initial?.price?.toInt()?.toString() ?: "") }
    var origPriceStr by remember { mutableStateOf(initial?.originalPrice?.toInt()?.toString() ?: "") }
    var unit by remember { mutableStateOf(initial?.unit ?: "১ লিটার") }
    var stockStr by remember { mutableStateOf(initial?.stock?.toString() ?: "20") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var safetyGuide by remember {
        mutableStateOf(
            initial?.safetyGuide ?: "অনুমোদিত মাত্রায় এবং প্রযোজ্য আইন ও কৃষি নির্দেশনা মেনে ব্যবহার করুন।"
        )
    }
    var approvalCode by remember { mutableStateOf(initial?.approvalCode ?: "") }
    var imageBase64 by remember { mutableStateOf(initial?.imageBase64 ?: "") }
    var badge by remember { mutableStateOf(initial?.badge ?: "") }
    var formError by remember { mutableStateOf<String?>(null) }

    val imageKey = AgroCategories.all.firstOrNull { it.name == category }?.imageKey ?: "urea"

    // Compliant Zero-Permission Android Photo Picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val encoded = ImageUtils.uriToCompressedBase64(context, uri)
            if (encoded != null) {
                imageBase64 = encoded
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (initial == null) "নতুন অনুমোদিত পণ্য যোগ করুন" else "পণ্য সম্পাদনা করুন",
            style = MaterialTheme.typography.titleLarge,
            color = ForestGreen
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProductVisual(
                imageKey = imageKey,
                imageBase64 = imageBase64,
                productName = name.ifBlank { "পণ্য" },
                modifier = Modifier.size(72.dp)
            )
            OutlinedButton(
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("seller_pick_photo_btn")
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (imageBase64.isBlank()) "পণ্যের ছবি আপলোড" else "ছবি পরিবর্তন করুন")
            }
        }

        Text("ক্যাটাগরি নির্বাচন করুন", style = MaterialTheme.typography.labelLarge, color = DeepInk)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(AgroCategories.all) { cat ->
                FilterChip(
                    selected = category == cat.name,
                    onClick = { category = cat.name },
                    label = { Text(cat.name) }
                )
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("পণ্যের নাম *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = { Text("বিক্রয় মূল্য (৳) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = origPriceStr,
                onValueChange = { origPriceStr = it },
                label = { Text("পূর্বমূল্য (ঐচ্ছিক)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("পরিমাণ / একক *") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = stockStr,
                onValueChange = { stockStr = it },
                label = { Text("স্টক সংখ্যা *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = approvalCode,
            onValueChange = { approvalCode = it },
            label = { Text("অনুমোদন / নিবন্ধন কোড (যেমন: AP-BD-102)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("পণ্যের বিবরণ") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = safetyGuide,
            onValueChange = { safetyGuide = it },
            label = { Text("নিরাপত্তা ও ব্যবহার নির্দেশনা *") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        if (formError != null) {
            Text(formError.orEmpty(), color = DeepForestGreen, style = MaterialTheme.typography.bodySmall)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("বাতিল")
            }
            Button(
                onClick = {
                    val p = priceStr.toDoubleOrNull() ?: 0.0
                    val op = origPriceStr.toDoubleOrNull() ?: p
                    val st = stockStr.toLongOrNull() ?: -1L
                    when {
                        name.trim().length < 2 -> formError = "পণ্যের সঠিক নাম দিন।"
                        p <= 0.0 -> formError = "সঠিক বিক্রয় মূল্য দিন।"
                        st < 0L -> formError = "সঠিক স্টক সংখ্যা দিন।"
                        else -> {
                            formError = null
                            onSubmit(name, category, p, op, unit, st, description, safetyGuide, approvalCode, imageKey, imageBase64, badge)
                        }
                    }
                },
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.weight(1f)
            ) {
                Text("সংরক্ষণ করুন")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
