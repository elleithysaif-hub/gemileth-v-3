package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppInfo
import com.example.data.AppRepository
import com.example.data.AuthRepository
import com.example.data.FirestoreRepository
import com.example.data.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

data class UserProfile(
    val displayName: String,
    val email: String,
    val photoUrl: String?
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = AuthRepository(application)
    private val geminiRepo = GeminiRepository()
    private val appRepo = AppRepository(application)
    private val firestoreRepo = FirestoreRepository()

    private val _isAuthenticated = MutableStateFlow(authRepo.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory.asStateFlow()

    private val _userProfileData = MutableStateFlow<UserProfile?>(null)
    val userProfileData: StateFlow<UserProfile?> = _userProfileData.asStateFlow()

    private val _suggestedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val suggestedApps: StateFlow<List<AppInfo>> = _suggestedApps.asStateFlow()

    init {
        loadApps()
        if (_isAuthenticated.value) {
            setupUserProfile()
            loadChatHistory()
            loadSuggestedApps()
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            _installedApps.value = appRepo.getInstalledApps()
        }
    }

    private fun loadSuggestedApps() {
        val user = authRepo.currentUser ?: return
        viewModelScope.launch {
            val suggestedPackageNames = firestoreRepo.getSuggestedApps(user.uid)
            val apps = _installedApps.value
            val suggested = apps.filter { it.packageName in suggestedPackageNames }
            _suggestedApps.value = suggested.take(4) // up to 4 apps
        }
    }

    private fun setupUserProfile() {
        val user = authRepo.currentUser ?: return
        _userProfileData.value = UserProfile(
            displayName = user.displayName ?: "Gemileith User",
            email = user.email ?: "",
            photoUrl = user.photoUrl?.toString()
        )
    }

    private fun loadChatHistory() {
        val user = authRepo.currentUser ?: return
        viewModelScope.launch {
            _isLoadingHistory.value = true
            val history = firestoreRepo.getMessages(user.uid)
            val appHistory = history.map { ChatMessage(it.text, it.isUser) }
            _chatMessages.value = appHistory
            _isLoadingHistory.value = false
        }
    }

    fun launchApp(packageName: String) {
        val user = authRepo.currentUser
        if (user != null) {
            viewModelScope.launch {
                firestoreRepo.saveAppLaunch(user.uid, packageName)
            }
        }
        appRepo.launchApp(packageName)
    }

    fun signIn() {
        viewModelScope.launch {
            val success = authRepo.signInWithGoogle()
            _isAuthenticated.value = success
            if (success) {
                setupUserProfile()
                loadChatHistory()
            }
        }
    }

    fun signOut() {
        authRepo.signOut()
        _isAuthenticated.value = false
        _userProfileData.value = null
        _chatMessages.value = emptyList() // clear history on sign out
    }

    fun clearChatHistory() {
        val user = authRepo.currentUser ?: return
        viewModelScope.launch {
            _isLoadingHistory.value = true
            firestoreRepo.clearMessages(user.uid)
            _chatMessages.value = emptyList()
            _isLoadingHistory.value = false
        }
    }

    fun sendChatMessage(message: String) {
        if (message.isBlank()) return
        
        // Add user message
        _chatMessages.update { it + ChatMessage(message, true) }
        _isThinking.value = true
        
        val user = authRepo.currentUser
        if (user != null) {
            viewModelScope.launch { firestoreRepo.saveMessage(user.uid, message, true) }
        }
        
        viewModelScope.launch {
            // Add an empty model message placeholder
            _chatMessages.update { it + ChatMessage("", false) }
            
            var fullResponse = ""
            geminiRepo.sendChatMessage(message).collect { chunk ->
                fullResponse += chunk
                _isThinking.value = false
                _chatMessages.update { current ->
                    val last = current.last()
                    val updatedLast = last.copy(text = last.text + chunk)
                    current.dropLast(1) + updatedLast
                }
            }
            if (user != null && fullResponse.isNotBlank()) {
                firestoreRepo.saveMessage(user.uid, fullResponse, false)
            }
        }
    }
}
