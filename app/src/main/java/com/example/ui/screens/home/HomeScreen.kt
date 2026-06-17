package com.example.ui.screens.home

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppInfo
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SurfaceDarkVariant
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    val apps by viewModel.installedApps.collectAsState()
    val userProfile by viewModel.userProfileData.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showProfileDialog by remember { mutableStateOf(false) }

    val filteredApps = apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    val suggestedApps by viewModel.suggestedApps.collectAsState()

    if (showProfileDialog && userProfile != null) {
        ProfileDialog(
            userProfile = userProfile!!,
            onDismissRequest = { showProfileDialog = false },
            onSignOut = { 
                showProfileDialog = false
                viewModel.signOut()
            },
            onClearHistory = {
                showProfileDialog = false
                viewModel.clearChatHistory()
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent, // Let wallpaper show through
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToChat,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Gemini Chat")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -30f) {
                            onNavigateToChat()
                        }
                    }
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                    placeholder = { Text("Search apps or ask AI...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Profile Avatar / Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .clickable { showProfileDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (userProfile?.photoUrl != null) {
                        AsyncImage(
                            model = userProfile!!.photoUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (suggestedApps.isNotEmpty() && searchQuery.isEmpty()) {
                Text(
                    text = "Suggested",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    suggestedApps.forEach { app ->
                        AppIcon(app = app) { viewModel.launchApp(app.packageName) }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(items = filteredApps, key = { it.packageName }) { app: AppInfo ->
                    AppIcon(app = app) {
                        viewModel.launchApp(app.packageName)
                    }
                }
            }
        }
    }
}

@Composable
fun AppIcon(app: AppInfo, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val bitmap = remember(app.icon) { drawableToBitmap(app.icon) }
        
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = app.label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            maxLines = 1,
            fontWeight = FontWeight.Medium,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        if (drawable.bitmap != null) {
            return drawable.bitmap
        }
    }
    val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    } else {
        Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    }
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
