package com.example.ui.screens.chat

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SecondaryNeon
import com.example.ui.theme.SurfaceDarkVariant
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val isLoadingHistory by viewModel.isLoadingHistory.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }

    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) { isListening = false }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        inputText = matches[0]
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }
            speechRecognizer.startListening(intent)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Gemileith AI", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val historyText = messages.joinToString(separator = "\n\n") {
                            val role = if (it.isUser) "User" else "Gemileith"
                            "$role: ${it.text}"
                        }
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, historyText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Chat History"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isThinking) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (isLoadingHistory) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(message.text, message.isUser)
                    }
                    if (isThinking) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemileith is thinking deeply...",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Input section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask anything...") },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (inputText.isBlank()) {
                    IconButton(
                        onClick = {
                            if (!isListening) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                speechRecognizer.stopListening()
                                isListening = false
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary, RoundedCornerShape(24.dp))
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.onSecondary)
                    }
                } else {
                    IconButton(
                        onClick = {
                            viewModel.sendChatMessage(inputText)
                            inputText = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    val backgroundColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(backgroundColor, shape)
                .padding(16.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp
            )
        }
    }
}
