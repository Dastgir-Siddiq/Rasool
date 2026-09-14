package com.orbitmessenger.feature.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var readReceiptsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<-")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ListItem(
                headlineContent = { Text("Push Notifications") },
                supportingContent = { Text("Receive alerts for new messages") },
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            )
            Divider()
            
            ListItem(
                headlineContent = { Text("Read Receipts") },
                supportingContent = { Text("Let others know when you've read their messages") },
                trailingContent = {
                    Switch(
                        checked = readReceiptsEnabled,
                        onCheckedChange = { readReceiptsEnabled = it }
                    )
                }
            )
            Divider()

            ListItem(
                headlineContent = { Text("Log Out") },
                supportingContent = { Text("End your session on this device") },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Log Out")
            }
        }
    }
}
