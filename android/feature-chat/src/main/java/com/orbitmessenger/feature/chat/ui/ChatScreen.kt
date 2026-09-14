package com.orbitmessenger.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Simple UI state model
data class MessageUiModel(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: String,
    val status: String
)

@Composable
fun ChatScreen(
    conversationName: String,
    messages: List<MessageUiModel>,
    onSendMessage: (String, Int) -> Unit,
    onNavigateBack: () -> Unit,
    onDeleteMessage: (MessageUiModel) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            ChatComposer(onSendMessage = onSendMessage)
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 4.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(messages.reversed(), key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    onDelete = { onDeleteMessage(msg) }
                )
            }
        }
    }
}
