package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ImageUtils
import com.example.model.AgroCategory
import com.example.ui.theme.BorderGreenTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepInk
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftMintCard
import com.example.ui.theme.VibrantGreen

@Composable
fun KrishiLogoEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = PureWhite,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(5.dp)) {
                val w = this.size.width
                val h = this.size.height
                // Terraced green hills
                drawArc(
                    color = ForestGreen,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = true,
                    topLeft = Offset(0f, h * 0.35f),
                    size = Size(w, h * 0.65f)
                )
                drawArc(
                    color = VibrantGreen,
                    startAngle = 25f,
                    sweepAngle = 130f,
                    useCenter = true,
                    topLeft = Offset(w * 0.08f, h * 0.45f),
                    size = Size(w * 0.84f, h * 0.55f)
                )
                // Twin leaves crest at top
                val leafPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.42f)
                    quadraticTo(w * 0.25f, h * 0.14f, w * 0.48f, h * 0.04f)
                    quadraticTo(w * 0.62f, h * 0.20f, w * 0.5f, h * 0.42f)
                }
                drawPath(leafPath, color = EmeraldGreen)
                val rightLeaf = Path().apply {
                    moveTo(w * 0.52f, h * 0.42f)
                    quadraticTo(w * 0.78f, h * 0.12f, w * 0.85f, h * 0.28f)
                    quadraticTo(w * 0.72f, h * 0.45f, w * 0.52f, h * 0.42f)
                }
                drawPath(rightLeaf, color = Color(0xFF8BC34A))
            }
            Icon(
                imageVector = Icons.Filled.Agriculture,
                contentDescription = "কৃষি বাজার লোগো",
                tint = DeepForestGreen,
                modifier = Modifier
                    .size(size * 0.42f)
                    .offset(y = 3.dp)
            )
        }
    }
}

data class BannerSlide(
    val headlineTop: String,
    val headlineBottom: String,
    val subQuote: String,
    val ctaText: String,
    val accentColor: Color
)

@Composable
fun HeroBannerCarousel(
    onCtaClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = remember {
        listOf(
            BannerSlide(
                headlineTop = "সুস্থ ফসল,",
                headlineBottom = "সমৃদ্ধ কৃষি",
                subQuote = "\"বিশ্বস্ত পণ্য, ন্যায্য দাম\"",
                ctaText = "এখনই অর্ডার করুন",
                accentColor = ForestGreen
            ),
            BannerSlide(
                headlineTop = "যাচাইকৃত দোকানদার,",
                headlineBottom = "নিরাপদ উপকরণ",
                subQuote = "অনুমোদিত মাত্রা ও সঠিক ব্যবহার নির্দেশনা",
                ctaText = "ক্যাটাগরি দেখুন",
                accentColor = EmeraldGreen
            ),
            BannerSlide(
                headlineTop = "মৌসুমী ছাড়,",
                headlineBottom = "সাশ্রয়ী বাজার",
                subQuote = "বীজ, সার ও সেচ সরঞ্জামে বিশেষ অফার",
                ctaText = "অফার দেখুন",
                accentColor = ForestGreen
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { slides.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(PureWhite)
            .border(1.dp, Color(0xFFD5EAD9), RoundedCornerShape(22.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(186.dp)
        ) { page ->
            val slide = slides[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFF6FBF6),
                                Color(0xFFE3F6E5),
                                Color(0xFFB7E9B8)
                            )
                        )
                    )
            ) {
                // Lush green field landscape artwork on right half
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Sky glow
                    drawCircle(
                        color = Color(0x44FFFFFF),
                        radius = h * 0.65f,
                        center = Offset(w * 0.78f, h * 0.25f)
                    )
                    // Rolling rice field waves
                    val hillBack = Path().apply {
                        moveTo(w * 0.35f, h)
                        quadraticTo(w * 0.62f, h * 0.42f, w, h * 0.56f)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(hillBack, color = Color(0xFF66BB6A).copy(alpha = 0.45f))

                    val hillFront = Path().apply {
                        moveTo(w * 0.44f, h)
                        quadraticTo(w * 0.74f, h * 0.58f, w, h * 0.68f)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(hillFront, color = Color(0xFF2E7D32).copy(alpha = 0.32f))

                    // Stylized fertilizer bag & bottle silhouette on right
                    val bagLeft = w * 0.62f
                    val bagTop = h * 0.36f
                    drawRoundRect(
                        color = PureWhite,
                        topLeft = Offset(bagLeft, bagTop),
                        size = Size(w * 0.16f, h * 0.46f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = VibrantGreen,
                        topLeft = Offset(bagLeft + 6f, bagTop + h * 0.24f),
                        size = Size(w * 0.16f - 12f, h * 0.16f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    // Pesticide bottle next to bag
                    val bLeft = w * 0.52f
                    val bTop = h * 0.42f
                    drawRoundRect(
                        color = Color(0xFFFDFEFE),
                        topLeft = Offset(bLeft, bTop),
                        size = Size(w * 0.09f, h * 0.40f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = ForestGreen,
                        topLeft = Offset(bLeft + w * 0.02f, bTop - h * 0.08f),
                        size = Size(w * 0.05f, h * 0.09f),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    drawRoundRect(
                        color = VibrantGreen,
                        topLeft = Offset(bLeft + 4f, bTop + h * 0.16f),
                        size = Size(w * 0.09f - 8f, h * 0.14f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    // Farmer hat + joyful silhouette on far right (white & green attire)
                    val fx = w * 0.86f
                    val fy = h * 0.30f
                    // Woven straw-green farmer hat
                    drawArc(
                        color = Color(0xFF81C784),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(fx - 26f, fy - 24f),
                        size = Size(52f, 34f)
                    )
                    drawOval(
                        color = Color(0xFFA5D6A7),
                        topLeft = Offset(fx - 38f, fy - 10f),
                        size = Size(76f, 14f)
                    )
                    // Face
                    drawCircle(
                        color = Color(0xFFD7EBD9),
                        radius = 16f,
                        center = Offset(fx, fy + 14f)
                    )
                    // Crisp White shirt with Emerald Green gamcha accent
                    drawRoundRect(
                        color = PureWhite,
                        topLeft = Offset(fx - 26f, fy + 30f),
                        size = Size(52f, h * 0.48f),
                        cornerRadius = CornerRadius(18f, 18f)
                    )
                    drawLine(
                        color = EmeraldGreen,
                        start = Offset(fx - 12f, fy + 32f),
                        end = Offset(fx + 6f, fy + 70f),
                        strokeWidth = 7f
                    )
                    // Harvest bowl at bottom right in white & green
                    drawArc(
                        color = DeepForestGreen,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.68f, h * 0.64f),
                        size = Size(w * 0.17f, h * 0.28f)
                    )
                    drawOval(
                        color = LimeAccent,
                        topLeft = Offset(w * 0.69f, h * 0.66f),
                        size = Size(w * 0.15f, h * 0.10f)
                    )
                    // Corner fresh green leaves framing the banner
                    val leftLeaf = Path().apply {
                        moveTo(0f, h)
                        quadraticTo(w * 0.04f, h * 0.62f, w * 0.10f, h * 0.80f)
                        quadraticTo(w * 0.07f, h * 0.94f, 0f, h)
                    }
                    drawPath(leftLeaf, color = EmeraldGreen.copy(alpha = 0.55f))

                    val rightLeafAccent = Path().apply {
                        moveTo(w, h)
                        quadraticTo(w * 0.88f, h * 0.72f, w * 0.82f, h * 0.88f)
                        quadraticTo(w * 0.90f, h * 0.98f, w, h)
                    }
                    drawPath(rightLeafAccent, color = ForestGreen.copy(alpha = 0.65f))
                }

                // Foreground copy + CTA on left
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.62f)
                        .padding(start = 16.dp, top = 14.dp, bottom = 24.dp, end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = slide.headlineTop,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 21.sp,
                                lineHeight = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = slide.accentColor
                        )
                        Text(
                            text = slide.headlineBottom,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 21.sp,
                                lineHeight = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = DeepForestGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = slide.subQuote,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = DeepInk,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { onCtaClick(page) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepForestGreen,
                            contentColor = PureWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("hero_cta_button_$page")
                    ) {
                        Text(
                            text = slide.ctaText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // Pager dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(slides.size) { index ->
                val active = pagerState.currentPage == index
                val dotWidth by animateDpAsState(
                    targetValue = if (active) 20.dp else 7.dp,
                    animationSpec = tween(durationMillis = 220),
                    label = "banner_dot_width"
                )
                Box(
                    modifier = Modifier
                        .size(width = dotWidth, height = 7.dp)
                        .clip(CircleShape)
                        .background(if (active) ForestGreen else Color(0xAAFFFFFF))
                )
            }
        }
    }
}

@Composable
fun CategoryIconTile(
    category: AgroCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgTint = when (category.imageKey) {
        "insecticide", "herbicide", "seeds", "irrigation" -> SoftMintCard
        else -> Color(0xFFF1F9F3)
    }
    val iconTint = when (category.imageKey) {
        "insecticide", "seeds" -> ForestGreen
        "fungicide", "irrigation" -> EmeraldGreen
        "herbicide", "safety" -> VibrantGreen
        else -> DeepForestGreen
    }
    val iconVec = when (category.imageKey) {
        "insecticide" -> Icons.Filled.Sanitizer
        "fungicide" -> Icons.Filled.Security
        "herbicide" -> Icons.Filled.Eco
        "urea" -> Icons.Filled.Spa
        "seeds" -> Icons.Filled.LocalFlorist
        "tools" -> Icons.Filled.Build
        "irrigation" -> Icons.Filled.WaterDrop
        else -> Icons.Filled.HealthAndSafety
    }

    val tileBg by animateColorAsState(
        targetValue = if (selected) EmeraldGreen else bgTint,
        animationSpec = tween(durationMillis = 180),
        label = "category_tile_bg"
    )
    val tileBorder by animateColorAsState(
        targetValue = if (selected) DeepForestGreen else Color(0xFFE0ECE2),
        animationSpec = tween(durationMillis = 180),
        label = "category_tile_border"
    )
    val tileIconTint by animateColorAsState(
        targetValue = if (selected) PureWhite else iconTint,
        animationSpec = tween(durationMillis = 180),
        label = "category_tile_icon"
    )

    Column(
        modifier = modifier
            .width(78.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(vertical = 4.dp)
            .testTag("category_tile_${category.imageKey}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(tileBg)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = tileBorder,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVec,
                contentDescription = category.name,
                tint = tileIconTint,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 11.5.sp
            ),
            color = if (selected) ForestGreen else DeepInk,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProductVisual(
    imageKey: String,
    imageBase64: String,
    productName: String,
    modifier: Modifier = Modifier
) {
    val decodedBitmap = remember(imageBase64) {
        ImageUtils.decodeBase64Bitmap(imageBase64)
    }

    if (decodedBitmap != null) {
        Image(
            bitmap = decodedBitmap.asImageBitmap(),
            contentDescription = productName,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(14.dp))
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF9FCF9), Color(0xFFEEF6EF))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val w = size.width
                val h = size.height
                // Subtle soft ground shadow
                drawOval(
                    color = Color(0x1814532D),
                    topLeft = Offset(w * 0.22f, h * 0.85f),
                    size = Size(w * 0.56f, h * 0.10f)
                )
                when (imageKey) {
                    "urea" -> {
                        // Crisp white & green fertilizer sack
                        drawRoundRect(
                            color = PureWhite,
                            topLeft = Offset(w * 0.18f, h * 0.14f),
                            size = Size(w * 0.64f, h * 0.76f),
                            cornerRadius = CornerRadius(18f, 18f)
                        )
                        drawRoundRect(
                            color = Color(0xFFD1D5DB),
                            topLeft = Offset(w * 0.18f, h * 0.14f),
                            size = Size(w * 0.64f, h * 0.76f),
                            cornerRadius = CornerRadius(18f, 18f),
                            style = Stroke(width = 3f)
                        )
                        drawRoundRect(
                            color = VibrantGreen,
                            topLeft = Offset(w * 0.20f, h * 0.54f),
                            size = Size(w * 0.60f, h * 0.32f),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        drawCircle(
                            color = LimeAccent,
                            radius = w * 0.08f,
                            center = Offset(w * 0.50f, h * 0.36f)
                        )
                    }
                    "fungicide", "herbicide", "insecticide" -> {
                        val capColor = when (imageKey) {
                            "fungicide" -> EmeraldGreen
                            "herbicide" -> ForestGreen
                            else -> DeepForestGreen
                        }
                        val labelColor = when (imageKey) {
                            "fungicide" -> VibrantGreen
                            "herbicide" -> EmeraldGreen
                            else -> ForestGreen
                        }
                        // Bottle cap
                        drawRoundRect(
                            color = capColor,
                            topLeft = Offset(w * 0.37f, h * 0.08f),
                            size = Size(w * 0.26f, h * 0.13f),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        // Bottle body
                        drawRoundRect(
                            color = PureWhite,
                            topLeft = Offset(w * 0.24f, h * 0.22f),
                            size = Size(w * 0.52f, h * 0.68f),
                            cornerRadius = CornerRadius(22f, 22f)
                        )
                        drawRoundRect(
                            color = BorderGreenTint,
                            topLeft = Offset(w * 0.24f, h * 0.22f),
                            size = Size(w * 0.52f, h * 0.68f),
                            cornerRadius = CornerRadius(22f, 22f),
                            style = Stroke(width = 3f)
                        )
                        // Label band
                        drawRoundRect(
                            color = labelColor.copy(alpha = 0.16f),
                            topLeft = Offset(w * 0.27f, h * 0.36f),
                            size = Size(w * 0.46f, h * 0.40f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawRoundRect(
                            color = labelColor,
                            topLeft = Offset(w * 0.27f, h * 0.64f),
                            size = Size(w * 0.46f, h * 0.12f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                    "seeds" -> {
                        // Crisp white & green seed pouch
                        drawRoundRect(
                            color = PureWhite,
                            topLeft = Offset(w * 0.20f, h * 0.14f),
                            size = Size(w * 0.60f, h * 0.74f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = BorderGreenTint,
                            topLeft = Offset(w * 0.20f, h * 0.14f),
                            size = Size(w * 0.60f, h * 0.74f),
                            cornerRadius = CornerRadius(16f, 16f),
                            style = Stroke(width = 3f)
                        )
                        drawRoundRect(
                            color = ForestGreen,
                            topLeft = Offset(w * 0.28f, h * 0.34f),
                            size = Size(w * 0.44f, h * 0.34f),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        drawCircle(
                            color = LimeAccent,
                            radius = w * 0.10f,
                            center = Offset(w * 0.50f, h * 0.51f)
                        )
                    }
                    "tools" -> {
                        // White & emerald sprayer tank
                        drawRoundRect(
                            color = PureWhite,
                            topLeft = Offset(w * 0.26f, h * 0.20f),
                            size = Size(w * 0.48f, h * 0.68f),
                            cornerRadius = CornerRadius(18f, 18f)
                        )
                        drawRoundRect(
                            color = EmeraldGreen,
                            topLeft = Offset(w * 0.26f, h * 0.20f),
                            size = Size(w * 0.48f, h * 0.68f),
                            cornerRadius = CornerRadius(18f, 18f),
                            style = Stroke(width = 4f)
                        )
                        drawRoundRect(
                            color = ForestGreen,
                            topLeft = Offset(w * 0.36f, h * 0.10f),
                            size = Size(w * 0.28f, h * 0.12f),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                        drawRoundRect(
                            color = VibrantGreen.copy(alpha = 0.22f),
                            topLeft = Offset(w * 0.32f, h * 0.38f),
                            size = Size(w * 0.36f, h * 0.32f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                    "irrigation" -> {
                        drawCircle(
                            color = ForestGreen,
                            radius = w * 0.30f,
                            center = Offset(w * 0.50f, h * 0.52f),
                            style = Stroke(width = 16f)
                        )
                        drawCircle(
                            color = VibrantGreen,
                            radius = w * 0.18f,
                            center = Offset(w * 0.50f, h * 0.52f),
                            style = Stroke(width = 10f)
                        )
                    }
                    else -> {
                        drawCircle(
                            color = ForestGreen.copy(alpha = 0.14f),
                            radius = w * 0.34f,
                            center = Offset(w * 0.50f, h * 0.50f)
                        )
                    }
                }
            }
            val overlayLabel = when (imageKey) {
                "urea" -> "ইউরিয়া"
                "herbicide" -> "আগাছা"
                "fungicide" -> "টপসিন"
                "insecticide" -> "সুরক্ষা"
                "seeds" -> "বীজ"
                "tools" -> "স্প্রেয়ার"
                "irrigation" -> "ড্রিপ"
                "safety" -> "সুরক্ষা"
                else -> "কৃষি"
            }
            Text(
                text = overlayLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DeepForestGreen,
                modifier = Modifier
                    .background(Color(0xCCFFFFFF), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
