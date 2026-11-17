package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.toBengaliDigits
import com.example.model.AgroCategories
import com.example.model.Product
import com.example.ui.components.CategoryIconTile
import com.example.ui.components.HeroBannerCarousel
import com.example.ui.components.KrishiLogoEmblem
import com.example.ui.components.ProductCard
import com.example.ui.theme.BorderGreenTint
import com.example.ui.theme.CrimsonBadge
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepInk
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.ui.theme.LimePill
import com.example.ui.theme.MintBackground
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGrayText
import com.example.ui.theme.SoftMintCard
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    products: List<Product>,
    favoriteIds: Set<String>,
    searchQuery: String,
    selectedCategory: String?,
    cartCount: Int,
    unreadNotifications: Int,
    catalogError: String? = null,
    onSearchQueryChange: (String) -> Unit,
    onCategoryClick: (String?) -> Unit,
    onSeeAllCategories: () -> Unit,
    onSeeOffers: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenCart: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val searchFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val isRegulatedCategory = selectedCategory != null &&
        AgroCategories.all.any { it.name == selectedCategory && it.isRegulated }
    val hasActiveFilter = searchQuery.isNotBlank() || selectedCategory != null

    fun scrollToProductsSection() {
        focusManager.clearFocus()
        keyboardController?.hide()
        scope.launch {
            listState.animateScrollToItem(3)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MintBackground)
    ) {
        val columns = when {
            maxWidth >= 900.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }
        val rows = products.chunked(columns)

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // Item 0: Top Gradient Header + Search Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(DeepForestGreen, ForestGreen, EmeraldGreen)
                            ),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KrishiLogoEmblem(size = 50.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "কৃষি বাজার",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = PureWhite,
                                maxLines = 1
                            )
                            Text(
                                text = "Krishi Bazar",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = LimeAccent
                            )
                            Text(
                                text = "স্মার্ট কৃষির জন্য, স্মার্ট সমাধান",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE6F4EA),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Notification icon with badge
                        HeaderBadgeIcon(
                            icon = Icons.Filled.Notifications,
                            contentDescription = "নোটিফিকেশন",
                            count = unreadNotifications,
                            onClick = onOpenNotifications,
                            tag = "home_notifications_button"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Cart icon with badge
                        HeaderBadgeIcon(
                            icon = Icons.Filled.ShoppingCart,
                            contentDescription = "শপিং কার্ট",
                            count = cartCount,
                            onClick = onOpenCart,
                            tag = "home_cart_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Bar
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PureWhite,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 14.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = MutedSage,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "কীটনাশক, সার, বীজ ইত্যাদি খুঁজুন...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SoftGrayText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = { scrollToProductsSection() }
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = DeepInk),
                                    cursorBrush = SolidColor(ForestGreen),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(searchFocusRequester)
                                        .testTag("home_search_input")
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("home_search_clear")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "খোঁজা মুছুন",
                                        tint = MutedSage,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = EmeraldGreen,
                                modifier = Modifier
                                    .size(width = 52.dp, height = 42.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        if (searchQuery.isBlank()) {
                                            searchFocusRequester.requestFocus()
                                            keyboardController?.show()
                                        } else {
                                            scrollToProductsSection()
                                        }
                                    }
                                    .testTag("home_search_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "খুঁজুন",
                                        tint = PureWhite,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Item 1: Hero Banner Carousel
            item {
                Spacer(modifier = Modifier.height(12.dp))
                HeroBannerCarousel(
                    onCtaClick = { page ->
                        when (page) {
                            1 -> onSeeAllCategories()
                            2 -> onSeeOffers()
                            else -> {
                                if (hasActiveFilter && products.isEmpty()) {
                                    onSearchQueryChange("")
                                    onCategoryClick(null)
                                }
                                scrollToProductsSection()
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }

            // Item 2: 8 Category Strip Card + Active Filter / Compliance Info
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, BorderGreenTint),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(AgroCategories.all, key = { it.name }) { cat ->
                                val isSelected = selectedCategory == cat.name
                                CategoryIconTile(
                                    category = cat,
                                    selected = isSelected,
                                    onClick = {
                                        onCategoryClick(if (isSelected) null else cat.name)
                                    }
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isRegulatedCategory || hasActiveFilter || catalogError != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isRegulatedCategory) {
                            Surface(
                                color = SoftMintCard,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderGreenTint),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VerifiedUser,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "নিয়ন্ত্রিত পণ্য: অনুমোদিত মাত্রা ও নিরাপত্তা নির্দেশনা মেনে ব্যবহার করুন",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = DeepForestGreen
                                    )
                                }
                            }
                        }

                        if (hasActiveFilter) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${products.size.toBengaliDigits()}টি পণ্য পাওয়া গেছে",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MutedSage
                                )
                                Surface(
                                    color = PureWhite,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, BorderGreenTint),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            onSearchQueryChange("")
                                            onCategoryClick(null)
                                        }
                                        .testTag("home_clear_filters_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = null,
                                            tint = ForestGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ফিল্টার মুছুন",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ForestGreen
                                        )
                                    }
                                }
                            }
                        }

                        if (!catalogError.isNullOrBlank()) {
                            Surface(
                                color = Color(0xFFFFF7ED),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "লাইভ সার্ভার সিঙ্ক সতর্কতা: প্রাথমিক অনুমোদিত তালিকা দেখানো হচ্ছে।",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF9A3412),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Item 3: Popular Products Section Green Banner Bar
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = ForestGreen
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Eco,
                                contentDescription = null,
                                tint = LimeAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedCategory ?: "জনপ্রিয় পণ্যসমূহ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = LimePill,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    if (selectedCategory != null) onCategoryClick(null)
                                    else onSeeAllCategories()
                                }
                                .testTag("home_see_all_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedCategory != null) "সব পণ্য" else "সব দেখুন",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DeepForestGreen
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = DeepForestGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (products.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BorderGreenTint)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = MutedSage,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "কোনো পণ্য পাওয়া যায়নি",
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepInk
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "অন্য নাম বা ক্যাটাগরি দিয়ে খুঁজে দেখুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSage
                            )
                            if (hasActiveFilter) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        onSearchQueryChange("")
                                        onCategoryClick(null)
                                    },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldGreen,
                                        contentColor = PureWhite
                                    ),
                                    modifier = Modifier.testTag("home_reset_empty_button")
                                ) {
                                    Text("সব পণ্য দেখুন")
                                }
                            }
                        }
                    }
                }
            } else {
                items(rows) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (item in rowItems) {
                            ProductCard(
                                product = item,
                                isFavorite = item.id in favoriteIds,
                                onProductClick = { onProductClick(item) },
                                onAddToCart = { onAddToCart(item) },
                                onToggleFavorite = { onToggleFavorite(item) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderBadgeIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    count: Int,
    onClick: () -> Unit,
    tag: String
) {
    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .testTag(tag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = PureWhite,
                modifier = Modifier.size(26.dp)
            )
        }
        if (count > 0) {
            Surface(
                color = CrimsonBadge,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .border(1.5.dp, PureWhite, CircleShape)
            ) {
                Text(
                    text = count.toBengaliDigits(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = PureWhite,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}
