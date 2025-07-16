package com.ruchitech.carlanuchertab.ui.screens.apps

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ruchitech.carlanuchertab.AppsActivity
import com.ruchitech.carlanuchertab.MyApp
import com.ruchitech.carlanuchertab.MyApp.Companion.loadInstalledApps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.filter

@Composable
fun AppUi(onBack:()-> Unit){
    DisposableEffect(Unit) {
        onDispose {
            CoroutineScope(Dispatchers.IO).launch {
                loadInstalledApps()
            }
        }
    }
    MaterialTheme {
        Scaffold { _padding ->
            InstalledAppsScreen(modifier = Modifier.padding(_padding),onBack)
        }
    }
}

@Composable
fun InstalledAppsScreen(modifier: Modifier,onBack:()-> Unit) {
    val context = LocalContext.current
    val allApps = remember { mutableStateOf(MyApp.allApps) }
    val searchQuery = remember { mutableStateOf("") }
    rememberScrollState()
    val gradientColors = listOf(
        Color(0xFF1A2A3A),
        Color(0xFF0D1A26),
        Color(0xFF02070D)
    )

    val filteredApps by remember(searchQuery.value, allApps.value) {
        derivedStateOf {
            if (searchQuery.value.isBlank()) allApps.value
            else allApps.value.filter {
                it.name.contains(searchQuery.value, ignoreCase = true)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Premium Car Dashboard Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )

            // Dashboard lines effect
            repeat(15) { i ->
                drawLine(
                    color = Color(0xFF3A4D5E).copy(alpha = 0.3f),
                    start = Offset(0f, size.height / 15 * i),
                    end = Offset(size.width, size.height / 15 * i),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Dashboard Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Premium Search Bar with Neumorphic Effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = true,
                        ambientColor = Color(0xFF3A5A78),
                        spotColor = Color(0xFF102A3F)
                    )
                    .background(
                        color = Color(0xFF1A2E42),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF3A5A78).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 8.dp),  // Reduced padding to accommodate back button
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Back Button with subtle hover effect
                    IconButton(
                        onClick = { /* Handle back action */ },
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .background(
                                color = if (isSystemInDarkTheme()) Color(0xFF2A3D52) else Color(
                                    0xFF1A2E42
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, // Use your back icon
                            contentDescription = "Back",
                            tint = Color(0xFF5D8BF4),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(onClick = {
                                    onBack()
                                })
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Search Icon
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF5D8BF4),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Search Field
                    BasicTextField(
                        value = searchQuery.value,
                        onValueChange = { searchQuery.value = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(Color(0xFF5D8BF4)),
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        decorationBox = { innerTextField ->
                            if (searchQuery.value.isEmpty()) {
                                Text(
                                    "Search Apps...",
                                    color = Color(0xFF7D8FA1),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Clear Button (appears when there's text)
                    if (searchQuery.value.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery.value = "" },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF7D8FA1).copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (allApps.value.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF5D8BF4),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // Premium App Grid with 3D Effect
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(100.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(filteredApps) { app ->
                        AppGridItemPremium(app = app)
                    }
                }
            }
        }

        // Dashboard Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(top = 20.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        //end = Offset(size.width, size.height)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "APPS",
                    color = Color(0xFF5D8BF4),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    "${filteredApps.size} installed",
                    color = Color(0xFF7D8FA1),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun AppGridItemPremium(app: AppInfo) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation = animateDpAsState(if (isPressed) 4.dp else 8.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .height(120.dp)
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    rotationY = if (isPressed) 5f else 0f
                    rotationX = if (isPressed) -2f else 0f
                    scaleX = if (isPressed) 0.95f else 1f
                    scaleY = if (isPressed) 0.95f else 1f
                }
                .shadow(
                    elevation = elevation.value,
                    shape = RoundedCornerShape(20.dp),
                    clip = true,
                    ambientColor = Color(0xFF3A5A78),
                    spotColor = Color(0xFF102A3F)
                )
                .background(
                    color = if (isPressed) Color(0xFF2A3D52) else Color(0xFF1A2E42),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF3A5A78).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                    onClick = {
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(app.packageName)
                        launchIntent?.let { context.startActivity(it) }
                    }
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(14.dp),
                            clip = true,
                            ambientColor = Color(0xFF3A5A78),
                            spotColor = Color(0xFF102A3F)
                        )
                        .background(
                            color = Color(0xFF2A3D52),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Icon(
                        painter = rememberAsyncImagePainter(app.icon),
                        contentDescription = app.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        tint = Color.Unspecified
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(80.dp)
                )
            }
        }
    }
}

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
)