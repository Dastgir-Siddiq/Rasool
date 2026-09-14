package com.orbitmessenger.feature.chat.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Sent = Blue gradient, Received = vibrant green ──────────────────────────
private val SentBrush    = Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF005FCC)))
private val ReceivedBrush= Brush.linearGradient(listOf(Color(0xFF30D158), Color(0xFF1A9E40)))

@Composable
fun MessageBubble(
    message: MessageUiModel,
    onDelete: () -> Unit
) {
    val isFromMe = message.isFromMe
    val context  = LocalContext.current

    val brush = if (isFromMe) SentBrush else ReceivedBrush
    val bubbleShape = if (isFromMe)
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    else
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)

    var showMenu by remember { mutableStateOf(false) }
    var reaction by remember { mutableStateOf<String?>(null) }

    // Entry animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = if (isFromMe)
            slideInHorizontally { it / 2 } + fadeIn()
        else
            slideInHorizontally { -it / 2 } + fadeIn(),
        exit  = shrinkHorizontally() + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start  = if (isFromMe) 60.dp else 8.dp,
                    end    = if (isFromMe) 8.dp else 60.dp,
                    top    = 3.dp,
                    bottom = 2.dp
                ),
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .shadow(2.dp, bubbleShape)
                    .clip(bubbleShape)
                    .background(brush)
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = { showMenu = true })
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text  = message.text,
                        color = Color.White,
                        fontSize  = 15.sp,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = message.timestamp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // Emoji reactions row
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        listOf("❤️", "👍", "😂", "😮", "😢", "🙏").forEach { emoji ->
                            TextButton(
                                onClick = { reaction = emoji; showMenu = false },
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(emoji, fontSize = 22.sp)
                            }
                        }
                    }
                    Divider()
                    DropdownMenuItem(text = { Text("Copy") }, onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("msg", message.text))
                        showMenu = false
                    })
                    DropdownMenuItem(text = { Text("Reply") }, onClick = { showMenu = false })
                    DropdownMenuItem(text = { Text("Forward") }, onClick = { showMenu = false })
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            showMenu  = false
                            visible   = false
                            onDelete()
                        }
                    )
                }
            }

            // Floating reaction badge
            if (reaction != null) {
                Surface(
                    modifier  = Modifier
                        .offset(y = (-6).dp)
                        .padding(horizontal = 8.dp),
                    shape     = RoundedCornerShape(20.dp),
                    color     = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Text(reaction!!, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            // Delivered status
            if (isFromMe && message.status == "Delivered") {
                Text(
                    "✓ Delivered",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(end = 4.dp, top = 2.dp)
                )
            }
        }
    }
}
