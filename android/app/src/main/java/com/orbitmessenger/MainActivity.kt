package com.orbitmessenger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.orbitmessenger.feature.chat.ui.ChatScreen
import com.orbitmessenger.feature.chat.ui.MessageUiModel
import java.util.Locale

// ─── Palette ────────────────────────────────────────────────────────────────
val RasoolBlue    = Color(0xFF0A84FF)
val RasoolGreen   = Color(0xFF30D158)
val RasoolPurple  = Color(0xFF5E5CE6)
val RasoolPink    = Color(0xFFFF375F)
val RasoolOrange  = Color(0xFFFF9F0A)
val RasoolTeal    = Color(0xFF32ADE6)
val RasoolBg      = Color(0xFFF2F2F7)
val RasoolSurface = Color.White

// Avatar colors by initial bucket
val avatarPalette = listOf(
    RasoolBlue, RasoolGreen, RasoolPurple, RasoolPink, RasoolOrange, RasoolTeal,
    Color(0xFFAC8E68), Color(0xFF30D158), Color(0xFFFF6961), Color(0xFF5AC8FA)
)
fun avatarColor(name: String): Color =
    avatarPalette[(name.firstOrNull()?.code ?: 0) % avatarPalette.size]

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionState.value = permissions.entries.all { it.value }
    }
    private val permissionState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        checkPermissions()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary   = RasoolBlue,
                    secondary = RasoolPurple,
                    background = RasoolBg,
                    surface    = RasoolSurface
                )
            ) {
                val hasPerms by permissionState
                if (hasPerms) OrbitAppSmsNavigation()
                else PermissionRequestScreen { checkPermissions() }
            }
        }
    }

    private fun checkPermissions() {
        val needed = mutableListOf(
            Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (needed.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            permissionState.value = true
        } else {
            requestPermissionLauncher.launch(needed.toTypedArray())
        }
    }
}

@Composable
fun PermissionRequestScreen(onRequest: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(RasoolBlue, RasoolPurple))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("📱", fontSize = 64.sp)
            Spacer(Modifier.height(24.dp))
            Text("Rasool", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Needs SMS & Contacts access to show your messages.", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = RasoolBlue),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permissions", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun OrbitAppSmsNavigation() {
    var activeThreadAddress by remember { mutableStateOf<String?>(null) }
    BackHandler(enabled = activeThreadAddress != null) { activeThreadAddress = null }

    AnimatedContent(
        targetState = activeThreadAddress,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            } else {
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            }
        }
    ) { address ->
        if (address == null) {
            SmsHomeScreen(onThreadClick = { activeThreadAddress = it })
        } else {
            SmsChatScreen(address = address, onNavigateBack = { activeThreadAddress = null })
        }
    }
}

// ─── Home Screen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsHomeScreen(onThreadClick: (String) -> Unit) {
    val context = LocalContext.current
    var allThreads by remember { mutableStateOf<List<SmsThread>>(emptyList()) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showComposeDialog by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { allThreads = SmsFetcher.fetchRecentConversations(context) }

    val personalThreads = allThreads.filter { it.address.replace("+", "").all(Char::isDigit) }
    val promoThreads    = allThreads.filterNot { it.address.replace("+", "").all(Char::isDigit) }
    val baseThreads     = if (selectedTabIndex == 0) personalThreads else promoThreads
    val displayedThreads = if (searchQuery.isBlank()) baseThreads else
        baseThreads.filter {
            val n = ContactResolver.getContactName(context, it.address)
            it.address.contains(searchQuery, true) || it.snippet.contains(searchQuery, true) || n.contains(searchQuery, true)
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RasoolSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Rasool",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = RasoolBlue
                    )
                    Row {
                        IconButton(onClick = { isSearching = !isSearching; searchQuery = "" }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = RasoolBlue)
                        }
                    }
                }

                // Inline search bar
                AnimatedVisibility(visible = isSearching) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RasoolBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .padding(bottom = 4.dp),
                        singleLine = true,
                        decorationBox = { inner ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                if (searchQuery.isEmpty()) Text("Search conversations…", color = Color.Gray, fontSize = 15.sp)
                                else inner()
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = RasoolSurface,
                modifier = Modifier.height(64.dp)
            ) {
                listOf("Messages", "Notices").forEachIndexed { idx, label ->
                    NavigationBarItem(
                        selected = selectedTabIndex == idx,
                        onClick  = { selectedTabIndex = idx },
                        icon = {},
                        label = {
                            Text(
                                label,
                                fontWeight = if (selectedTabIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == idx) RasoolBlue else Color.Gray,
                                fontSize = 15.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = RasoolBlue.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showComposeDialog = true },
                shape = CircleShape,
                containerColor = RasoolBlue,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 16.dp) // Little bit above bottom
            ) {
                Icon(Icons.Default.Create, contentDescription = "Compose")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.bg_home),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Crossfade(
                targetState = displayedThreads,
                animationSpec = tween(300),
                modifier = Modifier.fillMaxSize()
            ) { threads ->
                if (threads.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💬", fontSize = 56.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("No messages yet", fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = paddingValues
                    ) {
                        items(threads, key = { it.address }) { thread ->
                            val resolvedName = remember(thread.address) {
                                ContactResolver.getContactName(context, thread.address)
                            }
                            ConversationRow(thread, resolvedName, onThreadClick)
                        }
                    }
                }
            }
        }

        // Compose Dialog
        if (showComposeDialog) {
            var newAddress by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showComposeDialog = false },
                title = { Text("New Message", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("To: (number or name)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showComposeDialog = false; if (newAddress.isNotBlank()) onThreadClick(newAddress) },
                        shape = RoundedCornerShape(20.dp)
                    ) { Text("Start Chat") }
                },
                dismissButton = {
                    TextButton(onClick = { showComposeDialog = false }) { Text("Cancel") }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun ConversationRow(thread: SmsThread, resolvedName: String, onThreadClick: (String) -> Unit) {
    val accentColor = avatarColor(resolvedName)
    val initial = resolvedName.take(1).uppercase(Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.93f))
            .clickable { onThreadClick(thread.address) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colorful Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(accentColor.copy(alpha = 0.7f), accentColor))),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        resolvedName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        thread.timestamp.substringAfter(", ").ifBlank { thread.timestamp },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    thread.snippet,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E93)
                )
            }
        }
        Divider(
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 78.dp),
            color = Color(0xFFE5E5EA),
            thickness = 0.7.dp
        )
    }
}

// ─── Chat Screen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsChatScreen(address: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val smsMessages = remember { mutableStateListOf<MessageUiModel>() }
    val resolvedName = remember(address) { ContactResolver.getContactName(context, address) }
    val accentColor = avatarColor(resolvedName)
    val initial = resolvedName.take(1).uppercase(Locale.getDefault())

    LaunchedEffect(address) {
        smsMessages.clear()
        smsMessages.addAll(SmsFetcher.fetchMessagesForAddress(context, address))
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RasoolSurface)
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RasoolBlue)
                    }
                    // Mini avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(accentColor.copy(alpha = 0.7f), accentColor))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initial, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(resolvedName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(address, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    // Call buttons
                    IconButton(onClick = {
                        context.startActivity(
                            android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:$address")
                            }
                        )
                    }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = RasoolBlue)
                    }
                    IconButton(onClick = {
                        context.startActivity(
                            android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:$address")
                            }
                        )
                    }) {
                        Text("📹", fontSize = 20.sp)
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_chat),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            ChatScreen(
                conversationName = resolvedName,
                messages = smsMessages,
                onSendMessage = { text, simIndex ->
                    SmsFetcher.sendSms(context, address, text, simIndex)
                    smsMessages.add(
                        MessageUiModel(
                            id = System.currentTimeMillis().toString(),
                            text = text,
                            isFromMe = true,
                            timestamp = java.text.SimpleDateFormat("h:mm a", Locale.getDefault()).format(java.util.Date()),
                            status = "Delivered"
                        )
                    )
                },
                onNavigateBack = onNavigateBack,
                onDeleteMessage = { msg -> smsMessages.remove(msg) }
            )
        }
    }
}
