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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.toBengaliPrice
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.FavoriteItem
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.components.ProductVisual
import com.example.ui.theme.BorderGreenTint
import com.example.ui.theme.CrimsonBadge
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
fun ProfileScreen(
    profile: UserProfile?,
    userEmail: String?,
    isAdmin: Boolean,
    favoriteCount: Int,
    onEditProfile: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenSellerDashboard: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onSignOut: () -> Unit
) {
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val role = UserRole.fromRaw(profile?.role)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MintBackground),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Surface(
                color = ForestGreen,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = PureWhite,
                            shape = CircleShape,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile?.name?.ifBlank { "কৃষি বাজার সদস্য" } ?: "কৃষি বাজার সদস্য",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PureWhite
                                )
                                if (profile?.sellerVerified == true) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "যাচাইকৃত",
                                        tint = LimeAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = profile?.phone?.ifBlank { userEmail ?: "" } ?: (userEmail ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFDFF6E5)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "${role.bengaliLabel} অ্যাকাউন্ট • ${profile?.district ?: "বাংলাদেশ"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LimeAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                        IconButton(onClick = onEditProfile, modifier = Modifier.testTag("profile_edit_button")) {
                            Icon(Icons.Filled.Edit, contentDescription = "প্রোফাইল সম্পাদনা", tint = PureWhite)
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileMenuTile(
                    icon = Icons.Filled.Storefront,
                    title = if (role == UserRole.SELLER) "দোকানদার ড্যাশবোর্ড" else "দোকানদার হিসেবে বিক্রি শুরু করুন",
                    subtitle = if (role == UserRole.SELLER) {
                        "${profile?.shopName?.ifBlank { "আমার দোকান" }} • পণ্য, স্টক ও বিক্রির হিসাব"
                    } else {
                        "প্রোফাইল আপডেট করে অনুমোদিত কৃষি পণ্য বিক্রি করুন"
                    },
                    accent = EmeraldGreen,
                    onClick = {
                        if (role == UserRole.SELLER) onOpenSellerDashboard()
                        else onEditProfile()
                    },
                    tag = "profile_seller_dashboard_btn"
                )

                if (isAdmin) {
                    ProfileMenuTile(
                        icon = Icons.Filled.AdminPanelSettings,
                        title = "অ্যাডমিন প্যানেল (Admin Dashboard)",
                        subtitle = "ব্যবহারকারী, বিক্রেতা যাচাই, পণ্য পর্যালোচনা, অভিযোগ ও রিপোর্ট",
                        accent = Color(0xFF0F766E),
                        onClick = onOpenAdminDashboard,
                        tag = "profile_admin_dashboard_btn"
                    )
                }

                ProfileMenuTile(
                    icon = Icons.Filled.Favorite,
                    title = "পছন্দের পণ্য তালিকা ($favoriteCount)",
                    subtitle = "সংরক্ষিত পছন্দের কৃষি পণ্যগুলো দেখুন",
                    accent = CrimsonBadge,
                    onClick = onOpenFavorites,
                    tag = "profile_favorites_btn"
                )

                ProfileMenuTile(
                    icon = Icons.Filled.SupportAgent,
                    title = "কাস্টমার সাপোর্ট ও অভিযোগ",
                    subtitle = "সাহায্য নিন অথবা পণ্যের মান নিয়ে অভিযোগ জানান",
                    accent = VerifiedSky,
                    onClick = onOpenSupport,
                    tag = "profile_support_btn"
                )

                ProfileMenuTile(
                    icon = Icons.Filled.Lock,
                    title = "গোপনীয়তা ও ডেটা সুরক্ষা",
                    subtitle = "আপনার ব্যক্তিগত তথ্য ও নিরাপত্তা নীতিমালা",
                    accent = ForestGreen,
                    onClick = { showPrivacyDialog = true },
                    tag = "profile_privacy_btn"
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.2.dp, CrimsonBadge),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("profile_sign_out_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = CrimsonBadge,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নিরাপদ লগআউট করুন", color = CrimsonBadge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("গোপনীয়তা ও ডেটা সুরক্ষা") },
            text = {
                Text(
                    "১. কৃষি বাজার কেবল আপনার নাম, মোবাইল নম্বর ও ডেলিভারি ঠিকানা অর্ডার প্রেরণের জন্য ব্যবহার করে।\n" +
                        "২. আপনার ব্যক্তিগত তথ্য অন্য কোনো ক্রেতা বা অননুমোদিত ব্যক্তির সাথে শেয়ার করা হয় না।\n" +
                        "৩. সকল ডেটা Google Firebase-এর এনক্রিপ্টেড নিরাপত্তা ব্যবস্থায় সংরক্ষিত থাকে।\n" +
                        "৪. কীটনাশক ও নিয়ন্ত্রিত পণ্যের ক্ষেত্রে প্রযোজ্য আইন ও কৃষি সম্প্রসারণ অধিদপ্তরের নির্দেশনা অনুসরণ করা হয়।",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("বুঝেছি")
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag),
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
                color = accent.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepInk)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MutedSage)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MutedSage
            )
        }
    }
}

@Composable
fun FavoritesScreen(
    favorites: List<FavoriteItem>,
    onBack: () -> Unit,
    onOpenProductById: (String) -> Unit,
    onRemoveFavorite: (FavoriteItem) -> Unit
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = PureWhite)
                    }
                    Text("পছন্দের পণ্য তালিকা", style = MaterialTheme.typography.titleLarge, color = PureWhite)
                }
            }
        }
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("এখনো কোনো পছন্দের পণ্য সংরক্ষণ করা হয়নি।", style = MaterialTheme.typography.bodyLarge, color = MutedSage)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favorites, key = { it.productId }) { fav ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenProductById(fav.productId) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, BorderGreenTint)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductVisual(
                                imageKey = fav.imageKey,
                                imageBase64 = fav.imageBase64,
                                productName = fav.productName,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(fav.productName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepInk)
                                Text(fav.shopName, style = MaterialTheme.typography.bodySmall, color = MutedSage)
                                Text("${fav.price.toBengaliPrice()} / ${fav.unit}", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                            }
                            IconButton(onClick = { onRemoveFavorite(fav) }) {
                                Icon(Icons.Filled.Favorite, contentDescription = "সরান", tint = CrimsonBadge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupportComplaintsScreen(
    complaints: List<Complaint>,
    isBusy: Boolean,
    onBack: () -> Unit,
    onSubmitComplaint: (subject: String, message: String) -> Unit
) {
    BackHandler(onBack = onBack)

    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = PureWhite)
                    }
                    Text("কাস্টমার সাপোর্ট ও অভিযোগ", style = MaterialTheme.typography.titleLarge, color = PureWhite)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, BorderGreenTint)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("নতুন সাহায্য বা অভিযোগ পাঠান", style = MaterialTheme.typography.titleMedium, color = DeepInk)
                        Text("জরুরি কৃষি সহায়তা হেল্পলাইন: ১৬১২৩ (কৃষি কল সেন্টার)", style = MaterialTheme.typography.labelLarge, color = ForestGreen)
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("বিষয় *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("বিস্তারিত বার্তা *") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (errorMsg != null) {
                            Text(errorMsg.orEmpty(), color = CrimsonBadge, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = {
                                when {
                                    subject.trim().length < 2 -> errorMsg = "বিষয় লিখুন।"
                                    message.trim().length < 4 -> errorMsg = "বিস্তারিত লিখুন।"
                                    else -> {
                                        errorMsg = null
                                        onSubmitComplaint(subject, message)
                                        subject = ""
                                        message = ""
                                    }
                                }
                            },
                            enabled = !isBusy,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isBusy) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PureWhite)
                            else Text("বার্তা পাঠান", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (complaints.isNotEmpty()) {
                item {
                    Text("আপনার পূর্ববর্তী অভিযোগসমূহ", style = MaterialTheme.typography.titleMedium, color = DeepInk)
                }
                items(complaints, key = { it.id }) { comp ->
                    val status = ComplaintStatus.fromRaw(comp.status)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, BorderGreenTint)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(comp.subject, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepInk)
                                Surface(color = SoftMintCard, shape = RoundedCornerShape(50)) {
                                    Text(
                                        status.bengaliLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ForestGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(comp.message, style = MaterialTheme.typography.bodySmall, color = MutedSage)
                            if (comp.adminNote.isNotBlank()) {
                                Surface(color = SoftMintCard, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "অ্যাডমিন উত্তর: ${comp.adminNote}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ForestGreen,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
