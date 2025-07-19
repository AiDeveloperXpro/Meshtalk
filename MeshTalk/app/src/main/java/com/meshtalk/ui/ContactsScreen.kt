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
fun ContactsScreen(navController: NavController) {
    // TODO: Replace with real contacts
    val contacts = listOf("Alice", "Bob", "Charlie")
    LazyColumn {
        items(contacts.size) { i ->
            val contact = contacts[i]
            ListItem(
                headlineText = { Text(contact) },
                modifier = Modifier
                    .clickable { /* TODO: Start chat */ }
                    .fillMaxWidth()
            )
            Divider()
        }
    }
}