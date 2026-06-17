package com.example.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SurfaceDarkVariant
import com.example.viewmodel.MainViewModel

@Composable
fun AuthScreen(viewModel: MainViewModel, onNavigateHome: () -> Unit) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    if (isAuthenticated) {
        onNavigateHome()
        return // Avoid rendering the below layout while navigating
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_gemileith),
            contentDescription = "Cosmic Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                .padding(40.dp)
        ) {

            Text(
                text = "Gemileith OS",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "The Magical AI Launcher\nPowered by Gemini 3.1 Pro",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.signIn() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Authenticate to Enter",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
