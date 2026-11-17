package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.toBengaliDigits
import com.example.data.toBengaliPrice
import com.example.model.Product
import com.example.ui.components.ProductVisual
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.BorderGreenTint
import com.example.ui.theme.CrimsonBadge
import com.example.ui.theme.DeepInk
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintBackground
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGrayText
import com.example.ui.theme.SoftMintCard
import com.example.ui.theme.VerifiedSky

@Composable
fun ProductDetailScreen(
    product: Product,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onAddToCart: (Long) -> Unit,
    onBuyNow: (Long) -> Unit,
    onToggleFavorite: () -> Unit,
    onReportProduct: (subject: String, message: String) -> Unit
) {
    BackHandler(onBack = onBack)

    var quantity by remember { mutableLongStateOf(1L) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportMessage by remember { mutableStateOf("") }

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
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = PureWhite
                        )
                    }
                    Text(
                        text = "পণ্যের বিস্তারিত",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "পছন্দ করুন",
                            tint = if (isFavorite) CrimsonBadge else PureWhite
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = PureWhite,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onAddToCart(quantity) },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.5.dp, EmeraldGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_add_cart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "কার্টে যোগ করুন",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { onBuyNow(quantity) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("detail_buy_now_button")
                    ) {
                        Text(
                            text = "এখনই কিনুন",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, BorderGreenTint)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProductVisual(
                            imageKey = product.imageKey,
                            imageBase64 = product.imageBase64,
                            productName = product.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SoftMintCard,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = product.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ForestGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (product.badge.isNotBlank()) {
                                Surface(
                                    color = CrimsonBadge,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = product.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = DeepInk
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = product.price.toBengaliPrice(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 26.sp
                                ),
                                color = ForestGreen
                            )
                            if (product.originalPrice > product.price) {
                                Text(
                                    text = product.originalPrice.toBengaliPrice(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        textDecoration = TextDecoration.LineThrough
                                    ),
                                    color = SoftGrayText
                                )
                            }
                            Text(
                                text = "/ ${product.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSage
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "মজুত আছে: ${product.stock.toBengaliDigits()}টি",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (product.stock > 0) ForestGreen else CrimsonBadge
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SoftMintCard
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1L) quantity-- },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "পরিমাণ কমান", tint = ForestGreen)
                                    }
                                }
                                Text(
                                    text = quantity.toBengaliDigits(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DeepInk
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = SoftMintCard
                                ) {
                                    IconButton(
                                        onClick = { if (quantity < 99L) quantity++ },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "পরিমাণ বাড়ান", tint = ForestGreen)
                                    }
                                }
                            }
                        }
                    }
                }

                // Verified Seller Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, BorderGreenTint)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SoftMintCard,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Storefront,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = product.shopName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepInk
                                )
                                if (product.sellerVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "যাচাইকৃত বিক্রেতা",
                                        tint = VerifiedSky,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (product.sellerVerified) "কৃষি বাজার যাচাইকৃত বিশ্বস্ত দোকানদার"
                                else "নিবন্ধিত দোকানদার",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSage
                            )
                        }
                    }
                }

                // Description & Regulatory Safety Information
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, BorderGreenTint)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "পণ্যের বিবরণ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepInk
                        )
                        Text(
                            text = product.description.ifBlank { "কৃষি উপযোগী অনুমোদিত পণ্য।" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSage
                        )
                    }
                }

                // Approved Safety & Legal Compliance Box
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberContainer),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = Color(0xFFB45309)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "অনুমোদিত তথ্য ও নিরাপত্তা নির্দেশনা",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF78350F)
                            )
                        }
                        if (product.approvalCode.isNotBlank()) {
                            Text(
                                text = "অনুমোদন / নিবন্ধন কোড: ${product.approvalCode}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF92400E)
                            )
                        }
                        Text(
                            text = product.safetyGuide.ifBlank {
                                "প্রযোজ্য আইন ও কৃষি কর্মকর্তার পরামর্শ মেনে অনুমোদিত মাত্রায় ব্যবহার করুন।"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showReportDialog = true },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CrimsonBadge),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ReportProblem,
                        contentDescription = null,
                        tint = CrimsonBadge,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "পণ্য সম্পর্কে অভিযোগ / রিপোর্ট করুন",
                        color = CrimsonBadge
                    )
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("পণ্য সম্পর্কে অভিযোগ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${product.name} সম্পর্কে আপনার অভিযোগ বা পর্যবেক্ষণ লিখুন:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = reportMessage,
                        onValueChange = { reportMessage = it },
                        label = { Text("বিস্তারিত অভিযোগ") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportMessage.trim().length >= 4) {
                            onReportProduct("পণ্য বিষয়ক অভিযোগ: ${product.name}", reportMessage)
                            reportMessage = ""
                            showReportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("পাঠান")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
