package com.ruchitech.carlanuchertab.ui.screens.apps

import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.ruchitech.carlanuchertab.MyApp
import com.ruchitech.carlanuchertab.MyApp.Companion.loadInstalledApps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AppUi(onBack: () -> Unit) {
    DisposableEffect(Unit) {
        onDispose {
            CoroutineScope(Dispatchers.IO).launch {
                loadInstalledApps()
            }
        }
    }
    MaterialTheme {
        Scaffold { _padding ->
            InstalledAppsScreen(modifier = Modifier.padding(_padding), onBack)
        }
    }
}

enum class AppCategory(val displayName: String) {
    ALL("All Apps"),
    NAVIGATION("Navigation"),
    MEDIA("Media & Audio"),
    SYSTEM("System & Tools")
}

@Composable
fun InstalledAppsScreen(modifier: Modifier, onBack: () -> Unit) {
    val allApps = remember { mutableStateOf(MyApp.allApps) }
    val searchQuery = remember { mutableStateOf("") }
    val selectedCategory = remember { mutableStateOf(AppCategory.ALL) }

    // Categorization keywords
    val navigationKeywords = listOf("map", "navigation", "gps", "waze", "sygic", "here", "route")
    val mediaKeywords = listOf("music", "spotify", "audio", "player", "sound", "radio", "youtube", "podcast", "vlc", "mxplayer", "tv", "video")

    val filteredApps by remember(searchQuery.value, allApps.value, selectedCategory.value) {
        derivedStateOf {
            val baseList = allApps.value.filter { app ->
                when (selectedCategory.value) {
                    AppCategory.ALL -> true
                    AppCategory.NAVIGATION -> {
                        navigationKeywords.any { kw -> 
                            app.packageName.contains(kw, ignoreCase = true) || 
                            app.name.contains(kw, ignoreCase = true)
                        }
                    }
                    AppCategory.MEDIA -> {
                        mediaKeywords.any { kw -> 
                            app.packageName.contains(kw, ignoreCase = true) || 
                            app.name.contains(kw, ignoreCase = true)
                        }
                    }
                    AppCategory.SYSTEM -> {
                        val isNav = navigationKeywords.any { kw -> 
                            app.packageName.contains(kw, ignoreCase = true) || 
                            app.name.contains(kw, ignoreCase = true)
                        }
                        val isMedia = mediaKeywords.any { kw -> 
                            app.packageName.contains(kw, ignoreCase = true) || 
                            app.name.contains(kw, ignoreCase = true)
                        }
                        !isNav && !isMedia
                    }
                }
            }

            if (searchQuery.value.isBlank()) {
                baseList
            } else {
                baseList.filter {
                    it.name.contains(searchQuery.value, ignoreCase = true)
                }
            }
        }
    }

    // Modern Cybernetic Dark Background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Deep rich blue-grey
            Color(0xFF020617)  // Deepest slate black
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .drawBehind {
                // Dynamically paint a fine glowing tech grid overlay
                val gridSpacing = 60.dp.toPx()
                val gridColor = Color(0xFF38BDF8).copy(alpha = 0.03f)
                val strokeWidth = 1.dp.toPx()

                var x = 0f
                while (x < size.width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth)
                    x += gridSpacing
                }

                var y = 0f
                while (y < size.height) {
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth)
                    y += gridSpacing
                }
                
                // Top accent light glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.05f), Color.Transparent),
                        startY = 0f,
                        endY = 150.dp.toPx()
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Spacer to avoid overlapping with system UI / top status
            Spacer(modifier = Modifier.height(20.dp))

            // Premium Top Bar Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glassmorphic Back Button Card
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(
                            width = 1.2.dp,
                            color = Color(0xFF38BDF8).copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "APPLICATIONS",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF38BDF8).copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 10f
                            )
                        )
                    )
                    Text(
                        text = "${filteredApps.size} apps found",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Premium Glassmorphic Search Bar capsule
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(44.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(22.dp),
                            clip = true,
                            ambientColor = Color(0xFF0F172A),
                            spotColor = Color(0xFF020617)
                        )
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(
                            width = 1.dp,
                            color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery.value,
                            onValueChange = { searchQuery.value = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(Color(0xFF38BDF8)),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.value.isEmpty()) {
                                    Text(
                                        text = "Search apps...",
                                        color = Color(0xFF64748B),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.value.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery.value = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Category Slider Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCategory.values().forEach { category ->
                    val isSelected = selectedCategory.value == category
                    val tabBgColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.04f),
                        animationSpec = tween(durationMillis = 200)
                    )
                    val tabTextColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF020617) else Color(0xFF94A3B8),
                        animationSpec = tween(durationMillis = 200)
                    )
                    val strokeColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.08f),
                        animationSpec = tween(durationMillis = 200)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(tabBgColor)
                            .border(
                                width = 1.dp,
                                color = strokeColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedCategory.value = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.displayName,
                            color = tabTextColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Grid Content
            if (allApps.value.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF38BDF8),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // Highly Responsive Grid layout with beautiful spacing
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(135.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredApps, key = { "${it.packageName}_${it.name}" }) { app ->
                        AppGridItemPremium(app = app)
                    }
                }
            }
        }
    }
}

@Composable
fun AppGridItemPremium(app: AppInfo) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Tactile 3D Scale and Glow Animations
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = tween(durationMillis = 100)
    )
    val strokeColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF38BDF8) else Color(0xFF38BDF8).copy(alpha = 0.15f),
        animationSpec = tween(durationMillis = 150)
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = tween(durationMillis = 150)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = cardAlpha
            }
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(20.dp),
                clip = true,
                ambientColor = Color(0xFF0F172A),
                spotColor = Color(0xFF020617)
            )
            .background(
                color = Color(0xFF1E293B).copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = strokeColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    launchIntent?.let { context.startActivity(it) }
                }
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Icon Frame
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(18.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = rememberAsyncImagePainter(app.icon),
                contentDescription = app.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // High contrast highly readable app name
        Text(
            text = app.name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
)