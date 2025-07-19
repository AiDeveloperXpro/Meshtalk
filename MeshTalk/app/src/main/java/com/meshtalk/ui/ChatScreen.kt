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

@Composable
fun ChatScreen(navController: NavController, chatId: String) {
    // TODO: Replace with real messages
    val messages = listOf(
        Message("1", "Alice", "Hello!", true),
        Message("2", "Me", "Hi Alice!", false)
    )
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
        var input by remember { mutableStateOf("") }
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
            IconButton(onClick = { /* TODO: Send message */ }) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

data class Message(val id: String, val sender: String, val text: String, val isReceived: Boolean)

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send