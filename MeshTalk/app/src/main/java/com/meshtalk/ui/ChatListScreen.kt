package com.meshtalk.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ChatListScreen(navController: NavController) {
    // TODO: Replace with real chat data
    val chats = listOf(
        ChatPreview("1", "Alice", "Hey! How are you?", "12:00"),
        ChatPreview("2", "Bob", "Let's meet up.", "11:45")
    )
    LazyColumn {
        items(chats.size) { i ->
            val chat = chats[i]
            ListItem(
                headlineText = { Text(chat.name) },
                supportingText = { Text(chat.lastMessage) },
                trailingContent = { Text(chat.time) },
                modifier = Modifier
                    .clickable { navController.navigate("chat/${chat.id}") }
                    .fillMaxWidth()
            )
            Divider()
        }
    }
}

data class ChatPreview(val id: String, val name: String, val lastMessage: String, val time: String)