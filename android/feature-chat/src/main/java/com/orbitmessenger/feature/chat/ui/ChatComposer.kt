package com.orbitmessenger.feature.chat.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SendBrush = Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6)))

@Composable
fun ChatComposer(
    onSendMessage: (String, Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedSim by remember { mutableStateOf(0) } // 0 = SIM1, 1 = SIM2

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Text input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text("Type a message…", color = Color(0xFFAEAEB2), fontSize = 15.sp)
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 6,
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = Color.Black)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Bottom row: SIM chips + Send button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // SIM selector chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("SIM 1", "SIM 2").forEachIndexed { idx, label ->
                        val active = selectedSim == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (active) Color(0xFF0A84FF).copy(alpha = 0.12f)
                                    else Color(0xFFF2F2F7)
                                )
                                .clickable { selectedSim = idx }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                label,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) Color(0xFF0A84FF) else Color.Gray
                            )
                        }
                    }
                }

                // Send button — gradient circle, animates in when text not empty
                AnimatedVisibility(
                    visible = text.isNotBlank(),
                    enter = scaleIn() + fadeIn(),
                    exit  = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SendBrush)
                            .clickable {
                                onSendMessage(text.trim(), selectedSim)
                                text = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                // Idle state: greyed-out send indicator
                AnimatedVisibility(
                    visible = text.isBlank(),
                    enter = scaleIn() + fadeIn(),
                    exit  = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E5EA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
