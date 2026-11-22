package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.AppLanguage
import com.example.data.BiharDistricts
import com.example.data.ServiceTransaction
import com.example.data.ShopOrder
import com.example.data.ThemeMode
import com.example.data.UiState
import com.example.data.UserProfile
import com.example.data.UserSettings
import com.example.ui.components.CategoryBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.LoadingStateView
import com.example.ui.components.formatInr
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.CreditEmerald
import com.example.ui.theme.CreditEmeraldContainer
import com.example.ui.theme.DebitCoral
import com.example.ui.theme.DebitCoralContainer
import com.example.ui.theme.InfoIndigo
import com.example.ui.theme.InfoIndigoContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MarigoldSaffron
import com.example.ui.theme.MarigoldSoft
import com.example.ui.theme.PeacockTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAndSettingsScreen(
    initialTab: Int,
    userProfileState: UiState<UserProfile?>,
    ordersState: UiState<List<ShopOrder>>,
    transactionsState: UiState<List<ServiceTransaction>>,
    settings: UserSettings,
    onSaveProfile: (name: String, email: String, phone: String, biz: String, district: String, address: String) -> Unit,
    onUpdateOrderStatus: (orderId: String, status: String) -> Unit,
    onDeleteTransaction: (txId: String) -> Unit,
    onSetTheme: (ThemeMode) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetDistrict: (String) -> Unit,
    onSetDefaultGst: (Int) -> Unit,
    onSetCompactMode: (Boolean) -> Unit,
    onSetHaptic: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    LaunchedEffect(initialTab) {
        selectedTab = initialTab.coerceIn(0, 3)
    }

    val tabs = listOf(
        "प्रोफाइल" to Icons.Rounded.AccountCircle,
        "ऑर्डर" to Icons.Rounded.LocalShipping,
        "हिस्ट्री" to Icons.Rounded.History,
        "सेटिंग्स" to Icons.Rounded.Settings
    )

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
            tabs.forEachIndexed { idx, (label, icon) ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    modifier = Modifier.testTag("account_tab_$idx"),
                    text = { Text(label) },
                    icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        when (selectedTab) {
            0 -> ProfileTab(
                userProfileState = userProfileState,
                defaultDistrict = settings.defaultDistrict,
                onSaveProfile = onSaveProfile,
                onSignOut = onSignOut
            )
            1 -> OrdersTab(
                ordersState = ordersState,
                onUpdateStatus = onUpdateOrderStatus
            )
            2 -> HistoryTab(
                transactionsState = transactionsState,
                onDeleteTransaction = onDeleteTransaction
            )
            else -> SettingsTab(
                settings = settings,
                onSetTheme = onSetTheme,
                onSetLanguage = onSetLanguage,
                onSetDistrict = onSetDistrict,
                onSetDefaultGst = onSetDefaultGst,
                onSetCompactMode = onSetCompactMode,
                onSetHaptic = onSetHaptic
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileTab(
    userProfileState: UiState<UserProfile?>,
    defaultDistrict: String,
    onSaveProfile: (String, String, String, String, String, String) -> Unit,
    onSignOut: () -> Unit
) {
    val profile = (userProfileState as? UiState.Success)?.data
    var displayName by remember(profile?.displayName) { mutableStateOf(profile?.displayName.orEmpty()) }
    var email by remember(profile?.email) { mutableStateOf(profile?.email.orEmpty()) }
    var phone by remember(profile?.phone) { mutableStateOf(profile?.phone.orEmpty()) }
    var businessName by remember(profile?.businessName) { mutableStateOf(profile?.businessName.orEmpty()) }
    var district by remember(profile?.district, defaultDistrict) {
        mutableStateOf(profile?.district?.ifBlank { defaultDistrict } ?: defaultDistrict)
    }
    var address by remember(profile?.address) { mutableStateOf(profile?.address.orEmpty()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PeacockTeal)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(MarigoldSaffron),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.firstOrNull()?.uppercase() ?: "B",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName.ifBlank { "Bihar Seva Member" },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = email.ifBlank { "Google Account Connected" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        if (businessName.isNotBlank()) {
                            Text(
                                text = "🏪 $businessName • $district",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFE082)
                            )
                        }
                    }
                }
            }
        }

        if (userProfileState is UiState.Error) {
            item { ErrorStateCard(userProfileState.message) }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("व्यक्तिगत व बिज़नेस प्रोफाइल • Edit Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("पूरा नाम • Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 15) phone = it.filter(Char::isDigit) },
                        label = { Text("मोबाइल नंबर • Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("दुकान / बिज़नेस नाम • Shop Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("जिला चुनें • Bihar District", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BiharDistricts.forEach { d ->
                            FilterChip(
                                selected = district == d,
                                onClick = { district = d },
                                label = { Text(d) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("डिलीवरी पता • Address") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onSaveProfile(displayName, email, phone, businessName, district, address) },
                        enabled = displayName.trim().isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("profile_save_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("प्रोफाइल सेव करें • Save Profile")
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sign_out_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Sign out", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("लॉग आउट करें • Sign Out")
            }
        }
    }
}

@Composable
private fun OrdersTab(
    ordersState: UiState<List<ShopOrder>>,
    onUpdateStatus: (String, String) -> Unit
) {
    when (ordersState) {
        is UiState.Loading -> LoadingStateView("ऑर्डर लोड हो रहे हैं...")
        is UiState.Error -> Box(Modifier.padding(16.dp)) { ErrorStateCard(ordersState.message) }
        is UiState.Success -> {
            if (ordersState.data.isEmpty()) {
                Box(Modifier.padding(16.dp)) {
                    EmptyStateCard(
                        icon = Icons.Rounded.LocalShipping,
                        title = "कोई ऑर्डर नहीं है • No Orders Yet",
                        subtitle = "E-Commerce सेक्शन से खरीदारी करें और अपने ऑर्डर यहां ट्रैक करें।"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ordersState.data, key = { it.id }) { order ->
                        val isTerminal = order.status == "DELIVERED" || order.status == "CANCELLED"
                        val (badgeBg, badgeFg) = when (order.status) {
                            "DELIVERED" -> CreditEmeraldContainer to CreditEmerald
                            "CANCELLED" -> DebitCoralContainer to DebitCoral
                            "SHIPPED" -> InfoIndigoContainer to InfoIndigo
                            else -> MarigoldSoft to MarigoldSaffron
                        }
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = order.orderNumber,
                                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                        fontWeight = FontWeight.Bold
                                    )
                                    CategoryBadge(order.status, badgeBg, badgeFg)
                                }
                                Text(order.itemsSummary, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "पता: ${order.deliveryAddress} • ${order.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatTimestamp(order.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatInr(order.totalAmount),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (!isTerminal) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                                        OutlinedButton(
                                            onClick = { onUpdateStatus(order.id, "DELIVERED") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("डिलीवर हुआ")
                                        }
                                        OutlinedButton(
                                            onClick = { onUpdateStatus(order.id, "CANCELLED") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("रद्द करें")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTab(
    transactionsState: UiState<List<ServiceTransaction>>,
    onDeleteTransaction: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    val categories = listOf("ALL", "AEPS", "BANKING", "PAN", "RECHARGE", "CASH_TALLY", "ORDER")

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.replace("_", " ")) }
                )
            }
        }

        when (transactionsState) {
            is UiState.Loading -> LoadingStateView("हिस्ट्री लोड हो रही है...")
            is UiState.Error -> Box(Modifier.padding(16.dp)) { ErrorStateCard(transactionsState.message) }
            is UiState.Success -> {
                val filtered = transactionsState.data.filter {
                    selectedCategory == "ALL" || it.category == selectedCategory
                }
                if (filtered.isEmpty()) {
                    Box(Modifier.padding(16.dp)) {
                        EmptyStateCard(
                            icon = Icons.Rounded.ReceiptLong,
                            title = "कोई हिस्ट्री रिकॉर्ड नहीं • No History",
                            subtitle = "AEPS, बैंकिंग, PAN, रिचार्ज, कैश टैली व ऑर्डर की रसीदें यहां दिखेंगी।"
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered, key = { it.id }) { tx ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CategoryBadge(
                                                tx.category.replace("_", " "),
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = tx.referenceId,
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(tx.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        if (tx.subtitle.isNotBlank()) {
                                            Text(
                                                tx.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = formatTimestamp(tx.createdAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (tx.amount > 0) formatInr(tx.amount) else "INFO",
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                            color = CreditEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = { onDeleteTransaction(tx.id) }) {
                                            Icon(
                                                Icons.Rounded.DeleteOutline,
                                                contentDescription = "Delete receipt",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsTab(
    settings: UserSettings,
    onSetTheme: (ThemeMode) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetDistrict: (String) -> Unit,
    onSetDefaultGst: (Int) -> Unit,
    onSetCompactMode: (Boolean) -> Unit,
    onSetHaptic: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("settings_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Theme preference
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("थीम मोड • App Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { onSetTheme(mode) },
                                label = { Text("${mode.labelHi} (${mode.labelEn})") },
                                modifier = Modifier.testTag("settings_theme_${mode.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // 2. Language preference
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("भाषा पसंद • Language & News Feed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = settings.language == lang,
                                onClick = { onSetLanguage(lang) },
                                label = { Text(lang.label) },
                                modifier = Modifier.testTag("settings_lang_${lang.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // 3. Default Bihar District
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("लोकल न्यूज़ जिला • Default Bihar District", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BiharDistricts.forEach { district ->
                            FilterChip(
                                selected = settings.defaultDistrict == district,
                                onClick = { onSetDistrict(district) },
                                label = { Text(district) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Default GST Rate & Layout toggles
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("कैलकुलेटर व UI सेटिंग्स • Tools & Display", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("डिफ़ॉल्ट GST दर • Default GST Rate", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 12, 18, 28).forEach { gst ->
                            FilterChip(
                                selected = settings.defaultGstPercent == gst,
                                onClick = { onSetDefaultGst(gst) },
                                label = { Text("$gst%") }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("कॉम्पैक्ट मेन्यू कार्ड • Compact Menu Cards", style = MaterialTheme.typography.titleSmall)
                            Text("होम स्क्रीन पर अधिक टाइल्स एक साथ देखें", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.compactMode,
                            onCheckedChange = onSetCompactMode,
                            modifier = Modifier.testTag("settings_compact_switch")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("हैप्टिक फ़ीडबैक • Tactile Feedback", style = MaterialTheme.typography.titleSmall)
                            Text("कैलकुलेटर और कैश काउंटर टैप पर हल्का स्पर्श", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.hapticFeedback,
                            onCheckedChange = onSetHaptic,
                            modifier = Modifier.testTag("settings_haptic_switch")
                        )
                    }
                }
            }
        }
    }
}
