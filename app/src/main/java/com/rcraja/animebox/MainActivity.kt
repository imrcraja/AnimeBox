package com.rcraja.animebox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect

private val Bg = Color(0xFF080D0B)
private val Panel = Color(0xFF151D19)
private val Green = Color(0xFF00E676)
private val Muted = Color(0xFFAAB9B1)
private val White = Color(0xFFF2F8F4)

data class Anime(val title: String, val genre: String, val symbol: String, val episodes: String)

private val demoAnime = listOf(
    Anime("Shadow Realm", "Action • Fantasy", "影", "12 episodes"),
    Anime("Neon Ronin", "Sci-Fi • Action", "刃", "24 episodes"),
    Anime("Moonlit Academy", "Mystery • School", "月", "12 episodes"),
    Anime("Spirit Bloom", "Supernatural • Drama", "花", "Completed"),
    Anime("Crimson Orbit", "Adventure • Sci-Fi", "星", "Coming soon"),
    Anime("Last Shrine", "Fantasy • Mystery", "鳥", "24 episodes")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AnimeBoxApp() }
    }
}

@Composable
private fun AnimeBoxApp() {
    var tab by remember { mutableStateOf("Home") }
    var query by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("Free") }
    var selectedAnime by remember { mutableStateOf<Anime?>(null) }
    var playerAnime by remember { mutableStateOf<Anime?>(null) }
    var queue by remember { mutableStateOf(listOf<String>()) }
    var supportText by remember { mutableStateOf("") }
    var ticketMessage by remember { mutableStateOf("") }
    val filtered = demoAnime.filter {
        (it.title + " " + it.genre).contains(query, ignoreCase = true)
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Green,
        onPrimary = Color(0xFF062014), onBackground = White,
        onSurface = White, secondary = Color(0xFF8BE8B0)
    )) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar(containerColor = Panel, contentColor = Green) {
                    listOf("Home", "Search", "Downloads", "Premium", "Profile").forEach { label ->
                        NavigationBarItem(
                            selected = tab == label,
                            onClick = { tab = label; selectedAnime = null },
                            icon = {
                                Icon(
                                    when (label) {
                                        "Home" -> Icons.Default.Home
                                        "Search" -> Icons.Default.Search
                                        "Downloads" -> Icons.Default.Download
                                        "Premium" -> Icons.Default.Star
                                        else -> Icons.Default.Person
                                    }, contentDescription = label
                                )
                            },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Green,
                                selectedTextColor = Green,
                                indicatorColor = Color(0xFF234332),
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ANIMEBOX", color = Green, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("YOUR ANIME UNIVERSE", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp)
                    }
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFF173725)) {
                        Text("BETA", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (playerAnime != null) {
                    VideoPlayerScreen(anime = playerAnime!!, onBack = { playerAnime = null })
                } else if (selectedAnime != null) {
                    AnimeDetails(selectedAnime!!, onBack = { selectedAnime = null }, onPlay = { playerAnime = selectedAnime }, onQueue = {
                        val limit = when (selectedPlan) { "Gold" -> 5; "Silver" -> 3; else -> 1 }
                        if (queue.size < limit && !queue.contains(selectedAnime!!.title)) {
                            queue = queue + selectedAnime!!.title
                            ticketMessage = "Added to demo queue. No file downloaded."
                        } else ticketMessage = "Queue limit reached or title already queued."
                    }, message = ticketMessage)
                } else when (tab) {
                    "Home" -> HomeScreen(
                        query = query,
                        onQuery = { query = it; tab = "Search" },
                        anime = demoAnime,
                        onAnime = { selectedAnime = it },
                        onPremium = { tab = "Premium" }
                    )
                    "Search" -> SearchScreen(query, { query = it }, filtered, { selectedAnime = it })
                    "Downloads" -> DownloadsScreen(queue, { queue = queue - it }, ticketMessage)
                    "Premium" -> PremiumScreen(selectedPlan, { selectedPlan = it })
                    else -> ProfileScreen(
                        selectedPlan = selectedPlan,
                        supportText = supportText,
                        onSupportText = { supportText = it },
                        ticketMessage = ticketMessage,
                        onTicket = {
                            ticketMessage = if (supportText.isBlank()) "Please enter a message first." else "Demo ticket created on this device. Backend not connected."
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(query: String, onQuery: (String) -> Unit, anime: List<Anime>, onAnime: (Anime) -> Unit, onPremium: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Surface(color = Color(0xFF14291F), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Text("DISCOVER YOUR NEXT WATCH", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Stories worth staying up for.", color = White, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Explore new worlds, one episode at a time.", color = Muted)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onQuery("") }, colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                        Icon(Icons.Default.Search, null); Spacer(Modifier.width(6.dp)); Text("Explore anime")
                    }
                    TextButton(onClick = onPremium) { Text("Explore Premium →", color = Green) }
                }
            }
        }
        item { SectionTitle("Trending now", "SAMPLE CATALOGUE") }
        items(anime.take(4)) { AnimeCard(it, onClick = { onAnime(it) }) }
        item {
            Surface(color = Panel, shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Headphones, null, tint = Green, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Dubbing community", fontWeight = FontWeight.Bold)
                        Text("Creator features are planned for a future release.", color = Muted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(query: String, onQuery: (String) -> Unit, anime: List<Anime>, onAnime: (Anime) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text("Find anime", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search titles or genres") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(12.dp))
        if (anime.isEmpty()) Text("No results found.", color = Muted)
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(anime) { AnimeCard(it, onClick = { onAnime(it) }) }
        }
    }
}

@Composable
private fun AnimeCard(anime: Anime, onClick: () -> Unit) {
    Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 94.dp, height = 112.dp).background(Color(0xFF203D2E), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(anime.symbol, color = Green, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(anime.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Text(anime.genre, color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(7.dp))
                Text(anime.episodes, color = Green, fontSize = 12.sp)
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayCircle, null, tint = Green, modifier = Modifier.size(18.dp))
                    Text("  View details", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AnimeDetails(anime: Anime, onBack: () -> Unit, onPlay: () -> Unit, onQueue: () -> Unit, message: String) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        TextButton(onClick = onBack) { Text("← Back to catalogue", color = Green) }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(190.dp).background(Color(0xFF1D3A2C), RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
            Text(anime.symbol, color = Green, fontSize = 76.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
        Text(anime.title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text(anime.genre, color = Muted)
        Spacer(Modifier.height(12.dp))
        Text(anime.episodes, color = Green)
        Spacer(Modifier.height(12.dp))
        Text("Sample anime details. Official episode sources, playback and subtitles will be connected in a later phase.", color = Muted)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onPlay, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Open video player") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onQueue, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Download, null); Spacer(Modifier.width(8.dp)); Text("Add to demo download queue") }
        if (message.isNotBlank()) Text(message, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun DownloadsScreen(queue: List<String>, onRemove: (String) -> Unit, message: String) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("My downloads", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Free: 1 · Silver: 3 · Gold: 5 concurrent items", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Surface(color = Color(0xFF182D21), shape = RoundedCornerShape(12.dp)) {
            Text("Prototype queue only. No video files are downloaded yet.", Modifier.padding(12.dp), color = Color(0xFFB9F6CF), fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        if (queue.isEmpty()) Text("Your queue is empty. Explore anime and add an episode.", color = Muted)
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(queue) { title ->
                Surface(color = Panel, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DownloadDone, null, tint = Green)
                        Spacer(Modifier.width(10.dp))
                        Text(title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { onRemove(title) }) { Text("Remove", color = Green) }
                    }
                }
            }
        }
        if (message.isNotBlank()) Text(message, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun PremiumScreen(plan: String, onPlan: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("AnimeBox Premium", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
            Text("Choose the plan you want to preview.", color = Muted)
        }
        item { PlanCard("Free", "₹0", "1 queued download", plan == "Free", { onPlan("Free") }, false) }
        item { PlanCard("Silver", "Price TBD", "Up to 3 concurrent downloads", plan == "Silver", { onPlan("Silver") }, false) }
        item { PlanCard("Gold", "Price TBD", "Up to 5 concurrent downloads", plan == "Gold", { onPlan("Gold") }, true) }
        item {
            Text("7, 30, 90-day durations and final prices will be configured with the backend. No payment is processed in this prototype.", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PlanCard(name: String, price: String, description: String, selected: Boolean, onClick: () -> Unit, gold: Boolean) {
    Surface(color = if (selected) Color(0xFF1B3527) else Panel, shape = RoundedCornerShape(18.dp), border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, Green) else null) {
        Column(Modifier.fillMaxWidth().padding(17.dp)) {
            Text(name.uppercase(), color = if (gold) Color(0xFFFFD166) else Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text(price, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Text(description, color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (gold) Color(0xFFFFD166) else Green)) {
                Text(if (selected) "Selected for preview" else "Preview $name", color = Color(0xFF07140C))
            }
        }
    }
}

@Composable
private fun ProfileScreen(selectedPlan: String, supportText: String, onSupportText: (String) -> Unit, ticketMessage: String, onTicket: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("My profile", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold) }
        item {
            Surface(color = Panel, shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, tint = Green, modifier = Modifier.size(52.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Guest user", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Sign-in and account sync will be added with backend", color = Muted, fontSize = 12.sp)
                        Text("Current preview plan: $selectedPlan", color = Green, fontSize = 12.sp)
                    }
                }
            }
        }
        item { Text("Support center", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        item {
            Surface(color = Panel, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Create a support ticket", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = supportText, onValueChange = onSupportText, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Describe your issue") }, minLines = 3)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onTicket, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.SupportAgent, null); Spacer(Modifier.width(6.dp)); Text("Submit demo ticket") }
                    if (ticketMessage.isNotBlank()) Text(ticketMessage, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                }
            }
        }
        item {
            Text("Support email: support@example.com (placeholder)", color = Muted, fontSize = 12.sp)
            Text("Discord invite will be added when the server is ready.", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 9.sp)
    }
}


@Composable
private fun VideoPlayerScreen(anime: Anime, onBack: () -> Unit) {
    val context = LocalContext.current
    var mediaUrl by remember(anime.title) { mutableStateOf("") }
    var playbackMessage by remember(anime.title) { mutableStateOf("Paste a direct video URL that you own or are authorized to stream.") }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
            player = null
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("← Back to details", color = Green) }
        Text(anime.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text("Video player · authorized sources only", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        Surface(color = Color.Black, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().height(220.dp)) {
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        useController = true
                        player = ExoPlayer.Builder(viewContext).build().also { this.player = it }
                    }
                },
                update = { view -> if (view.player !== player) view.player = player }
            )
        }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = mediaUrl,
            onValueChange = { mediaUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Direct video URL") },
            placeholder = { Text("https://your-authorized-host/video.mp4") },
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        Button(onClick = {
            val url = mediaUrl.trim()
            if (!(url.startsWith("https://") || url.startsWith("http://"))) {
                playbackMessage = "Enter a valid HTTP(S) video URL first."
            } else {
                try {
                    val activePlayer = player ?: ExoPlayer.Builder(context).build().also { player = it }
                    activePlayer.setMediaItem(MediaItem.fromUri(url))
                    activePlayer.prepare()
                    activePlayer.playWhenReady = true
                    playbackMessage = "Loading video… If it fails, check the URL and media format."
                } catch (e: Exception) {
                    playbackMessage = "Could not start playback. Check the video URL and try again."
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Load and play") }
        Text(playbackMessage, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
        Text("This screen does not include bundled anime streams. Use your own or licensed direct video URLs.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
    }
}
