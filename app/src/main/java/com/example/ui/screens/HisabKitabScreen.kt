package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.KhataEntry
import com.example.data.UiState
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
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PeacockTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisabKitabScreen(
    khataState: UiState<List<KhataEntry>>,
    settings: UserSettings,
    onAddEntry: (name: String, phone: String, type: String, amount: Double, note: String, onDone: () -> Unit) -> Unit,
    onToggleSettled: (entryId: String, settled: Boolean) -> Unit,
    onDeleteEntry: (entryId: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("ALL") } // ALL, GAVE, GOT, SETTLED
    var showAddSheet by remember { mutableStateOf(false) }

    val entries = (khataState as? UiState.Success)?.data.orEmpty()
    val totalGave = entries.filter { !it.settled && it.entryType == "GAVE" }.sumOf { it.amount }
    val totalGot = entries.filter { !it.settled && it.entryType == "GOT" }.sumOf { it.amount }
    val netBalance = totalGave - totalGot

    val filtered = remember(entries, searchQuery, filterMode) {
        val q = searchQuery.trim().lowercase()
        entries.filter { e ->
            val modeOk = when (filterMode) {
                "GAVE" -> !e.settled && e.entryType == "GAVE"
                "GOT" -> !e.settled && e.entryType == "GOT"
                "SETTLED" -> e.settled
                else -> true
            }
            val qOk = q.isEmpty() || e.customerName.lowercase().contains(q) ||
                e.note.lowercase().contains(q) || e.customerPhone.contains(q)
            modeOk && qOk
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Dashboard Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PeacockTeal)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                        Text(
                            text = "बिज़नेस टूल: हिसाब किताब • Khata Ledger",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("लेना है (Gave)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA7F3D0))
                                    Text(
                                        text = formatInr(totalGave),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("देना है (Got)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFECACA))
                                    Text(
                                        text = formatInr(totalGot),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("कुल शेष • Net Balance", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                            Text(
                                text = (if (netBalance >= 0) "+" else "") + formatInr(netBalance),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = Color(0xFFFFE082),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Search & Filters
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ग्राहक नाम या नोट खोजें...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search khata") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val chips = listOf(
                        "ALL" to "सभी (${entries.size})",
                        "GAVE" to "लेना है (Credit)",
                        "GOT" to "देना है (Debit)",
                        "SETTLED" to "चुकता (Settled)"
                    )
                    items(chips) { (code, label) ->
                        FilterChip(
                            selected = filterMode == code,
                            onClick = { filterMode = code },
                            label = { Text(label) }
                        )
                    }
                }
            }

            when (khataState) {
                is UiState.Loading -> item { LoadingStateView("हिसाब किताब लोड हो रहा है...") }
                is UiState.Error -> item { ErrorStateCard(khataState.message) }
                is UiState.Success -> {
                    if (filtered.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.MenuBook,
                                title = "अभी कोई हिसाब एंट्री नहीं है",
                                subtitle = "नीचे 'नई एंट्री' बटन दबाकर ग्राहक का उधार/जमा खाता लिखें।"
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { entry ->
                            KhataEntryCard(
                                entry = entry,
                                onToggleSettled = { onToggleSettled(entry.id, !entry.settled) },
                                onDelete = { onDeleteEntry(entry.id) }
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("hisab_add_entry_fab"),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White,
            icon = { Icon(Icons.Rounded.Add, contentDescription = "Add khata entry") },
            text = { Text("नई एंट्री • Add") }
        )
    }

    if (showAddSheet) {
        var customerName by remember { mutableStateOf("") }
        var customerPhone by remember { mutableStateOf("") }
        var entryType by remember { mutableStateOf("GAVE") }
        var amountText by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        val parsedAmt = amountText.toDoubleOrNull() ?: 0.0
        val canSave = customerName.trim().isNotEmpty() && parsedAmt > 0.0

        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("नई हिसाब एंट्री • New Khata Entry", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = entryType == "GAVE",
                        onClick = { entryType = "GAVE" },
                        label = { Text("दिया / लेना है (You Gave)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = entryType == "GOT",
                        onClick = { entryType = "GOT" },
                        label = { Text("लिया / देना है (You Got)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("ग्राहक का नाम • Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("hisab_name_input")
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("राशि (₹) • Amount *") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("hisab_amount_input")
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { if (it.length <= 15) customerPhone = it.filter(Char::isDigit) },
                    label = { Text("मोबाइल नंबर (वैकल्पिक)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("विवरण / सामान • Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        onAddEntry(customerName, customerPhone, entryType, parsedAmt, note) {
                            showAddSheet = false
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("hisab_save_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("हिसाब सेव करें • Save Entry")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun KhataEntryCard(
    entry: KhataEntry,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit
) {
    val isGave = entry.entryType == "GAVE"
    val accentBg = if (isGave) CreditEmeraldContainer else DebitCoralContainer
    val accentFg = if (isGave) CreditEmerald else DebitCoral

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = entry.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (entry.settled) {
                            CategoryBadge("चुकता • Settled", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (entry.note.isNotBlank()) {
                        Text(
                            text = entry.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    CategoryBadge(
                        label = if (isGave) "लेना है" else "देना है",
                        containerColor = accentBg,
                        contentColor = accentFg
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatInr(entry.amount),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = accentFg,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(entry.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleSettled, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = if (entry.settled) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = "Toggle settled",
                            tint = if (entry.settled) CreditEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete entry",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
