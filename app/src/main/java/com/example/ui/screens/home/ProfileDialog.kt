package com.example.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.viewmodel.UserProfile

@Composable
fun ProfileDialog(
    userProfile: UserProfile,
    onDismissRequest: () -> Unit,
    onSignOut: () -> Unit,
    onClearHistory: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (userProfile.photoUrl != null) {
                    AsyncImage(
                        model = userProfile.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = userProfile.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = userProfile.email,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onClearHistory,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Chat History")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            }
        }
    }
}
