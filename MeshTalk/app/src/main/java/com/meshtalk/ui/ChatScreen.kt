package com.meshtalk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meshtalk.meshcore.MeshManager
import com.meshtalk.meshcore.MeshMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context

// Singleton or DI for MeshManager (for demo)
object MeshManagerProvider {
    var meshManager: MeshManager? = null
}

class ChatViewModel(private val chatId: String, context: Context) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    private val myId = "Me" // TODO: Use real device/user ID
    private val meshManager = MeshManagerProvider.meshManager ?: MeshManager(context, myId)

    init {
        MeshManagerProvider.meshManager = meshManager
        meshManager.setOnMessageReceived { from, meshMsg ->
            if (meshMsg.type == "text" && (meshMsg.to == null || meshMsg.to == myId)) {
                viewModelScope.launch {
                    _messages.value = _messages.value + Message(
                        id = System.currentTimeMillis().toString(),
                        sender = from,
                        text = meshMsg.payload,
                        isReceived = true
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val msg = MeshMessage(
            from = myId,
            to = chatId, // For demo, direct to chatId
            timestamp = System.currentTimeMillis(),
            type = "text",
            payload = text
        )
        meshManager.broadcastMessage(msg)
        viewModelScope.launch {
            _messages.value = _messages.value + Message(
                id = System.currentTimeMillis().toString(),
                sender = myId,
                text = text,
                isReceived = false
            )
        }
    }
}

@Composable
fun ChatScreen(navController: NavController, chatId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: ChatViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatId, context.applicationContext) as T
        }
    })
    val messages by viewModel.messages.collectAsState()
    var input by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages.size) { i ->
                val msg = messages[i]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = if (msg.isReceived) Arrangement.Start else Arrangement.End
                ) {
                    Surface(
                        color = if (msg.isReceived) Color(0xFFFFFFFF) else Color(0xFFDCF8C6),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = msg.text,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            IconButton(onClick = {
                if (input.isNotBlank()) {
                    viewModel.sendMessage(input)
                    input = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

data class Message(val id: String, val sender: String, val text: String, val isReceived: Boolean)

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send