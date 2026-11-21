package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.toBengaliDigits
import com.example.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.ui.theme.MintBackground
import com.example.ui.theme.PureWhite
import com.example.ui.theme.VibrantGreen

@Composable
fun OffersScreen(
    products: List<Product>,
    favoriteIds: Set<String>,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit
) {
    val discounted = products.filter { it.discountPercent > 0L || it.originalPrice > it.price }
        .ifEmpty { products }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MintBackground)
    ) {
        val columns = if (maxWidth >= 600.dp) 3 else 2
        val rows = discounted.chunked(columns)

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocalOffer,
                                contentDescription = null,
                                tint = LimeAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "মৌসুমী অফার ও বিশেষ ছাড়",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PureWhite
                            )
                        }
                        Text(
                            text = "কৃষকের সাশ্রয়ের জন্য যাচাইকৃত পণ্যে ন্যায্য মূল্যে ছাড়",
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
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(listOf(DeepForestGreen, ForestGreen, VibrantGreen))
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "বোরো মৌসুম সাশ্রয় উৎসব",
                                style = MaterialTheme.typography.titleLarge,
                                color = LimeAccent
                            )
                            Text(
                                text = "বর্তমানে ${discounted.size.toBengaliDigits()}টি অনুমোদিত পণ্যে বিশেষ ছাড় চলছে!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PureWhite
                            )
                        }
                        Surface(
                            color = PureWhite,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "১৭% পর্যন্ত\nছাড়",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = ForestGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

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
