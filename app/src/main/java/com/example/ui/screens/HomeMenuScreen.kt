package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.KhataEntry
import com.example.data.NewsRegion
import com.example.data.UiState
import com.example.data.UserProfile
import com.example.data.UserSettings
import com.example.data.localizedText
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatInr
import com.example.ui.theme.CreditEmerald
import com.example.ui.theme.CreditEmeraldContainer
import com.example.ui.theme.DebitCoral
import com.example.ui.theme.DebitCoralContainer
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.HeritageGoldSoft
import com.example.ui.theme.InfoIndigo
import com.example.ui.theme.InfoIndigoContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MarigoldSaffron
import com.example.ui.theme.MarigoldSoft
import com.example.ui.theme.PeacockMist
import com.example.ui.theme.PeacockTeal
import com.example.ui.theme.VioletService
import com.example.ui.theme.VioletServiceContainer

data class SuperMenuTile(
    val id: String,
    val titleHi: String,
    val titleEn: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentBg: Color,
    val accentFg: Color,
    val onClick: () -> Unit
)

data class SuperMenuSection(
    val headerHi: String,
    val headerEn: String,
    val subtitle: String,
    val tiles: List<SuperMenuTile>
)

@Composable
fun HomeMenuScreen(
    userProfileState: UiState<UserProfile?>,
    khataState: UiState<List<KhataEntry>>,
    settings: UserSettings,
    cartCount: Int,
    onOpenNews: (NewsRegion) -> Unit,
    onOpenShop: () -> Unit,
    onOpenBanking: (Int) -> Unit,
    onOpenTools: (Int) -> Unit,
    onOpenHisabKitab: () -> Unit,
    onOpenAccountTab: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val profile = (userProfileState as? UiState.Success)?.data
    val displayName = profile?.displayName?.ifBlank { "नमस्ते" } ?: "नमस्ते"
    val khataList = (khataState as? UiState.Success)?.data.orEmpty()
    val totalReceivable = khataList.filter { !it.settled && it.entryType == "GAVE" }.sumOf { it.amount }
    val totalPayable = khataList.filter { !it.settled && it.entryType == "GOT" }.sumOf { it.amount }

    val allSections = remember(settings.defaultDistrict, cartCount) {
        listOf(
            SuperMenuSection(
                headerHi = "समाचार",
                headerEn = "News",
                subtitle = "लोकल (${settings.defaultDistrict}), बिहार व देश की ताज़ा खबरें",
                tiles = listOf(
                    SuperMenuTile("news_local", "लोकल न्यूज़", "Local News", settings.defaultDistrict, Icons.Rounded.LocationCity, MarigoldSoft, MarigoldSaffron) { onOpenNews(NewsRegion.LOCAL) },
                    SuperMenuTile("news_bihar", "बिहार समाचार", "Bihar News", "पटना व सभी जिले", Icons.Rounded.Newspaper, PeacockMist, PeacockTeal) { onOpenNews(NewsRegion.BIHAR) },
                    SuperMenuTile("news_national", "देश समाचार", "National News", "भारत प्रमुख खबरें", Icons.Rounded.Public, InfoIndigoContainer, InfoIndigo) { onOpenNews(NewsRegion.NATIONAL) }
                )
            ),
            SuperMenuSection(
                headerHi = "ऑनलाइन खरीदारी",
                headerEn = "E-Commerce",
                subtitle = "बिहार शिल्प, इलेक्ट्रॉनिक्स, किराना व घरेलू सामान",
                tiles = listOf(
                    SuperMenuTile(
                        "shop_main",
                        "ऑनलाइन बाज़ार",
                        "Online Store",
                        if (cartCount > 0) "कार्ट में $cartCount आइटम" else "बेस्ट डील्स व ऑफर्स",
                        Icons.Rounded.ShoppingBag,
                        MarigoldSoft,
                        MarigoldSaffron,
                        onOpenShop
                    ),
                    SuperMenuTile(
                        "shop_orders",
                        "मेरे ऑर्डर",
                        "My Orders",
                        "ट्रैक व स्टेटस देखें",
                        Icons.Rounded.ReceiptLong,
                        HeritageGoldSoft,
                        HeritageGold
                    ) { onOpenAccountTab(1) }
                )
            ),
            SuperMenuSection(
                headerHi = "बैंकिंग सेवाएं",
                headerEn = "Banking",
                subtitle = "AEPS आधार बैंकिंग, मिनी स्टेटमेंट व ट्रांसफर",
                tiles = listOf(
                    SuperMenuTile("bank_aeps", "AEPS सेवाएं", "AEPS Banking", "निकासी, बैलेंस, मिनी स्टेटमेंट", Icons.Rounded.Fingerprint, PeacockMist, PeacockTeal) { onOpenBanking(0) },
                    SuperMenuTile("bank_transfer", "बैंकिंग व EMI", "Banking & DMT", "ट्रांसफर लॉग व EMI कैलकुलेटर", Icons.Rounded.AccountBalance, InfoIndigoContainer, InfoIndigo) { onOpenBanking(1) }
                )
            ),
            SuperMenuSection(
                headerHi = "डिजिटल सेवाएं",
                headerEn = "Digital Services",
                subtitle = "PAN कार्ड सेवाएं, मोबाइल व DTH रिचार्ज",
                tiles = listOf(
                    SuperMenuTile("dig_pan", "PAN सेवाएं", "PAN Services", "नया PAN, सुधार, स्टेटस", Icons.Rounded.Badge, VioletServiceContainer, VioletService) { onOpenBanking(2) },
                    SuperMenuTile("dig_recharge", "रिचार्ज", "Recharge", "मोबाइल व DTH रिचार्ज", Icons.Rounded.PhoneAndroid, CreditEmeraldContainer, CreditEmerald) { onOpenBanking(3) }
                )
            ),
            SuperMenuSection(
                headerHi = "स्मार्ट टूल्स",
                headerEn = "Tools & Personal Tools",
                subtitle = "GST कैलकुलेटर, नोट कैश काउंटर व आयु कैलकुलेटर",
                tiles = listOf(
                    SuperMenuTile("tool_calc", "कैलकुलेटर", "Calculator", "GST + स्मार्ट हिसाब", Icons.Rounded.Calculate, InfoIndigoContainer, InfoIndigo) { onOpenTools(0) },
                    SuperMenuTile("tool_cash", "कैश काउंटर", "Cash Counter", "₹500 से ₹1 नोट गिनती", Icons.Rounded.Payments, CreditEmeraldContainer, CreditEmerald) { onOpenTools(1) },
                    SuperMenuTile("tool_age", "आयु कैलकुलेटर", "Age Calculator", "Personal Tool • जन्मदिन", Icons.Rounded.Cake, MarigoldSoft, MarigoldSaffron) { onOpenTools(2) }
                )
            ),
            SuperMenuSection(
                headerHi = "बिज़नेस टूल्स",
                headerEn = "Business Tools",
                subtitle = "दुकान व ग्राहक उधार खाता — क्लाउड सिंक",
                tiles = listOf(
                    SuperMenuTile("biz_hisab", "हिसाब किताब", "Hisab Kitab", "ग्राहक लेना-देना खाता बही", Icons.Rounded.MenuBook, CreditEmeraldContainer, CreditEmerald, onOpenHisabKitab)
                )
            ),
            SuperMenuSection(
                headerHi = "मेरा खाता व सेटिंग्स",
                headerEn = "My Account & Settings",
                subtitle = "प्रोफाइल, ऑर्डर, हिस्ट्री व कस्टम पसंद",
                tiles = listOf(
                    SuperMenuTile("acc_profile", "प्रोफाइल", "Profile", "नाम, जिला, दुकान पता", Icons.Rounded.AccountCircle, PeacockMist, PeacockTeal) { onOpenAccountTab(0) },
                    SuperMenuTile("acc_history", "हिस्ट्री", "History", "सभी रसीद व लेन-देन", Icons.Rounded.History, VioletServiceContainer, VioletService) { onOpenAccountTab(2) },
                    SuperMenuTile("acc_settings", "सेटिंग्स", "Settings", "थीम, भाषा, जिला, GST", Icons.Rounded.Settings, HeritageGoldSoft, HeritageGold) { onOpenAccountTab(3) }
                )
            )
        )
    }

    val filteredSections = remember(searchQuery, allSections) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) allSections
        else allSections.mapNotNull { section ->
            val matching = section.tiles.filter {
                it.titleHi.lowercase().contains(q) ||
                    it.titleEn.lowercase().contains(q) ||
                    it.subtitle.lowercase().contains(q) ||
                    section.headerEn.lowercase().contains(q) ||
                    section.headerHi.lowercase().contains(q)
            }
            if (matching.isNotEmpty()) section.copy(tiles = matching) else null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_menu_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Editorial Hero Greeting Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_biharseva_1795339173836),
                    contentDescription = "Bihar Seva dashboard banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    PeacockTeal.copy(alpha = 0.94f),
                                    PeacockTeal.copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "📍 ${settings.defaultDistrict}, Bihar",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        IconButton(
                            onClick = { onOpenAccountTab(3) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .testTag("home_quick_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "नमस्ते, $displayName",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = localizedText(settings.language, "Super App का पूरा मेन्यू", "Complete Super App Menu"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // 2. Quick Business Hisab Kitab Snapshot Strip
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenHisabKitab)
                    .testTag("home_hisab_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = localizedText(settings.language, "लेना है", "To Receive"),
                            style = MaterialTheme.typography.labelSmall,
                            color = CreditEmerald
                        )
                        Text(
                            text = formatInr(totalReceivable),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                            color = CreditEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            text = localizedText(settings.language, "देना है", "To Give"),
                            style = MaterialTheme.typography.labelSmall,
                            color = DebitCoral
                        )
                        Text(
                            text = formatInr(totalPayable),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                            color = DebitCoral,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hisab",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Search Super App Services
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("सेवा खोजें: AEPS, PAN, Recharge, Hisab, News...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_menu_search_input")
            )
        }

        // 4. All Categorized Super App Sections
        filteredSections.forEach { section ->
            item(key = section.headerEn) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = localizedText(settings.language, section.headerHi, section.headerEn),
                        subtitle = section.subtitle
                    )
                    val chunked = section.tiles.chunked(if (section.tiles.size == 1) 1 else 2)
                    chunked.forEach { rowTiles ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowTiles.forEach { tile ->
                                MenuFeatureTile(
                                    tile = tile,
                                    language = settings.language,
                                    compact = settings.compactMode,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowTiles.size == 1 && section.tiles.size > 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun MenuFeatureTile(
    tile: SuperMenuTile,
    language: com.example.data.AppLanguage,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = tile.onClick)
            .testTag("menu_tile_${tile.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 11.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 40.dp else 46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tile.accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = tile.titleEn,
                    tint = tile.accentFg,
                    modifier = Modifier.size(if (compact) 22.dp else 25.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localizedText(language, tile.titleHi, tile.titleEn),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tile.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
