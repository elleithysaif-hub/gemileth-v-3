package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    GemileithApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun GemileithApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(
                viewModel = viewModel,
                onNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToChat = { navController.navigate("chat") }
            )
        }
        composable("chat") {
            ChatScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
