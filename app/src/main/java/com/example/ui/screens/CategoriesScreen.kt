package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.toBengaliDigits
import com.example.model.AgroCategories
import com.example.model.Product
import com.example.ui.components.CategoryIconTile
import com.example.ui.components.ProductCard
import com.example.ui.theme.BorderGreenTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepInk
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintBackground
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftMintCard

@Composable
fun CategoriesScreen(
    allProducts: List<Product>,
    selectedCategory: String?,
    favoriteIds: Set<String>,
    onSelectCategory: (String?) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit
) {
    val displayed = if (selectedCategory == null) allProducts
    else allProducts.filter { it.category == selectedCategory }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MintBackground)
    ) {
        val columns = if (maxWidth >= 600.dp) 3 else 2
        val categoryRows = AgroCategories.all.chunked(4)
        val productRows = displayed.chunked(columns)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
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
                        Text(
                            text = "সকল পণ্যের ক্যাটাগরি",
                            style = MaterialTheme.typography.headlineSmall,
                            color = PureWhite
                        )
                        Text(
                            text = "প্রয়োজন অনুযায়ী অনুমোদিত কৃষি উপকরণ বেছে নিন",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFDFF6E5)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, BorderGreenTint)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (rowCats in categoryRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (cat in rowCats) {
                                    val isSel = selectedCategory == cat.name
                                    CategoryIconTile(
                                        category = cat,
                                        selected = isSel,
                                        onClick = { onSelectCategory(if (isSel) null else cat.name) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Regulatory Compliance Card for Pesticides / Crop Protection
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = SoftMintCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderGreenTint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "কীটনাশক, ছত্রাকনাশক ও আগাছানাশকের ক্ষেত্রে কেবল অনুমোদিত নিবন্ধন তথ্য, নিরাপত্তা নির্দেশনা এবং প্রযোজ্য আইন মেনে যাচাইকৃত পণ্য প্রদর্শিত হয়।",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepForestGreen
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (selectedCategory ?: "সব ক্যাটাগরির পণ্য") + " (${displayed.size.toBengaliDigits()}টি)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepInk
                    )
                    if (selectedCategory != null) {
                        Text(
                            text = "সব দেখুন",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldGreen,
                            modifier = Modifier
                                .clickable { onSelectCategory(null) }
                                .padding(4.dp)
                                .testTag("cat_clear_filter")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(productRows) { rowItems ->
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
