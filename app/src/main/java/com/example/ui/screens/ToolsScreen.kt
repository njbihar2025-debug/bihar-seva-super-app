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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettings
import com.example.ui.components.formatInr
import com.example.ui.components.numberToIndianWords
import com.example.ui.theme.CreditEmerald
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MarigoldSaffron
import com.example.ui.theme.PeacockTeal
import java.util.Calendar
import java.util.UUID
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    initialTab: Int,
    settings: UserSettings,
    onSaveCashTally: (title: String, subtitle: String, refId: String, amount: Double) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    LaunchedEffect(initialTab) {
        selectedTab = initialTab.coerceIn(0, 2)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.testTag("tools_tab_calculator"),
                text = { Text("कैलकुलेटर", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                icon = { Icon(Icons.Rounded.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.testTag("tools_tab_cash_counter"),
                text = { Text("कैश काउंटर", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                icon = { Icon(Icons.Rounded.Payments, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier.testTag("tools_tab_age"),
                text = { Text("आयु कैलकुलेटर", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                icon = { Icon(Icons.Rounded.Cake, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> SmartCalculatorPane(defaultGst = settings.defaultGstPercent)
            1 -> CashCounterPane(onSaveTally = onSaveCashTally)
            else -> AgeCalculatorPane()
        }
    }
}

@Composable
private fun SmartCalculatorPane(defaultGst: Int) {
    var display by remember { mutableStateOf("0") }
    var operand by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var resetOnNextDigit by remember { mutableStateOf(false) }
    var expressionTape by remember { mutableStateOf("") }

    fun formatCalcResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (value % 1.0 == 0.0 && abs(value) < 1e14) {
            value.toLong().toString()
        } else {
            String.format("%.4f", value).trimEnd('0').trimEnd('.')
        }
    }

    fun applyPending(currentVal: Double): Double {
        val left = operand ?: return currentVal
        return when (operator) {
            "+" -> left + currentVal
            "-" -> left - currentVal
            "×" -> left * currentVal
            "÷" -> if (currentVal == 0.0) Double.NaN else left / currentVal
            else -> currentVal
        }
    }

    fun onDigit(d: String) {
        if (display == "Error") {
            display = if (d == ".") "0." else d
            resetOnNextDigit = false
            return
        }
        if (resetOnNextDigit) {
            display = if (d == ".") "0." else d
            resetOnNextDigit = false
        } else {
            if (d == "." && display.contains(".")) return
            display = if (display == "0" && d != ".") d else (display + d).take(16)
        }
    }

    fun onOp(op: String) {
        val current = display.toDoubleOrNull() ?: 0.0
        val computed = if (operand != null && operator != null && !resetOnNextDigit) {
            applyPending(current)
        } else current
        operand = computed
        operator = op
        display = formatCalcResult(computed)
        expressionTape = "$display $op"
        resetOnNextDigit = true
    }

    fun onEquals() {
        val current = display.toDoubleOrNull() ?: 0.0
        if (operand != null && operator != null) {
            val result = applyPending(current)
            expressionTape = "${formatCalcResult(operand!!)} $operator ${formatCalcResult(current)} ="
            display = formatCalcResult(result)
            operand = null
            operator = null
            resetOnNextDigit = true
        }
    }

    fun applyGst(rate: Int, add: Boolean) {
        val base = display.toDoubleOrNull() ?: return
        val result = if (add) base * (1.0 + rate / 100.0) else base / (1.0 + rate / 100.0)
        val gstDiff = abs(result - base)
        expressionTape = if (add) "+$rate% GST (₹${formatCalcResult(gstDiff)})" else "-$rate% GST (₹${formatCalcResult(gstDiff)})"
        display = formatCalcResult(result)
        resetOnNextDigit = true
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PeacockTeal)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = expressionTape.ifEmpty { "Smart GST Calculator (Default $defaultGst%)" },
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = display,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 38.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("calculator_display")
                    )
                }
            }
        }

        // GST Quick Strip
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(5, 12, defaultGst, 28).distinct().forEach { gst ->
                    OutlinedButton(
                        onClick = { applyGst(gst, add = true) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+$gst% GST", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }

        item {
            val rows = listOf(
                listOf("C", "⌫", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("00", "0", ".", "=")
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { key ->
                            val isAccent = key in listOf("÷", "×", "-", "+", "=")
                            val isClear = key == "C" || key == "⌫"
                            Button(
                                onClick = {
                                    when (key) {
                                        "C" -> {
                                            display = "0"
                                            operand = null
                                            operator = null
                                            expressionTape = ""
                                            resetOnNextDigit = false
                                        }
                                        "⌫" -> {
                                            display = if (display.length <= 1 || display == "Error") "0"
                                            else display.dropLast(1)
                                        }
                                        "%" -> {
                                            val v = display.toDoubleOrNull() ?: 0.0
                                            display = formatCalcResult(v / 100.0)
                                        }
                                        "÷", "×", "-", "+" -> onOp(key)
                                        "=" -> onEquals()
                                        "00" -> {
                                            onDigit("0")
                                            onDigit("0")
                                        }
                                        else -> onDigit(key)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .testTag("calc_btn_$key"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        key == "=" -> MarigoldSaffron
                                        isAccent -> MaterialTheme.colorScheme.primary
                                        isClear -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    },
                                    contentColor = when {
                                        key == "=" -> Color.White
                                        isAccent -> MaterialTheme.colorScheme.onPrimary
                                        isClear -> MaterialTheme.colorScheme.onSecondaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            ) {
                                if (key == "⌫") {
                                    Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Backspace")
                                } else {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
                                        fontWeight = FontWeight.Bold
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

@Composable
private fun CashCounterPane(
    onSaveTally: (title: String, subtitle: String, refId: String, amount: Double) -> Unit
) {
    val denominations = listOf(500, 200, 100, 50, 20, 10, 5, 2, 1)
    val counts = remember {
        mutableStateMapOf<Int, Int>().apply {
            denominations.forEach { put(it, 0) }
        }
    }
    var tallyLabel by remember { mutableStateOf("") }

    val totalAmount = denominations.sumOf { denom -> denom.toLong() * (counts[denom] ?: 0).toLong() }
    val totalNotes = denominations.sumOf { counts[it] ?: 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PeacockTeal)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "कुल नकद राशि • Total Cash",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.18f)) {
                            Text(
                                text = "$totalNotes नोट/सिक्के",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = formatInr(totalAmount.toDouble()),
                        style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("cash_counter_total")
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = numberToIndianWords(totalAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        denominations.forEach { denom ->
            item(key = denom) {
                val count = counts[denom] ?: 0
                val subtotal = denom.toLong() * count.toLong()
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.width(68.dp)
                        ) {
                            Text(
                                text = "₹$denom",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (count > 0) counts[denom] = count - 1 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease $denom count")
                            }
                            OutlinedTextField(
                                value = if (count == 0) "" else count.toString(),
                                onValueChange = { raw ->
                                    val digits = raw.filter(Char::isDigit).take(5)
                                    counts[denom] = digits.toIntOrNull() ?: 0
                                },
                                placeholder = { Text("0") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(74.dp)
                                    .testTag("cash_input_$denom")
                            )
                            IconButton(
                                onClick = { counts[denom] = (count + 1).coerceAtMost(99999) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Increase $denom count")
                            }
                        }
                        Text(
                            text = formatInr(subtotal.toDouble()),
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                            fontWeight = FontWeight.SemiBold,
                            color = if (subtotal > 0) CreditEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(86.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = tallyLabel,
                onValueChange = { tallyLabel = it },
                label = { Text("नोट / काउंटर नाम (वैकल्पिक) • Label") },
                placeholder = { Text("जैसे: शाम का गल्ला क्लोजिंग") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        denominations.forEach { counts[it] = 0 }
                        tallyLabel = ""
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("रीसेट • Reset")
                }
                Button(
                    onClick = {
                        val breakdown = denominations
                            .filter { (counts[it] ?: 0) > 0 }
                            .joinToString(", ") { "₹$it×${counts[it]}" }
                        val ref = "CASH-${UUID.randomUUID().toString().take(8).uppercase()}"
                        onSaveTally(
                            tallyLabel.trim().ifEmpty { "Cash Tally ($totalNotes notes)" },
                            breakdown.take(190),
                            ref,
                            totalAmount.toDouble()
                        )
                    },
                    enabled = totalAmount > 0L,
                    modifier = Modifier.weight(1.4f).height(50.dp).testTag("cash_save_tally_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("हिस्ट्री में सेव करें")
                }
            }
        }
    }
}

@Composable
private fun AgeCalculatorPane() {
    val todayCal = remember { Calendar.getInstance() }
    var birthDay by remember { mutableStateOf("15") }
    var birthMonth by remember { mutableStateOf("8") }
    var birthYear by remember { mutableStateOf("1998") }

    val curYear = todayCal.get(Calendar.YEAR)
    val curMonth = todayCal.get(Calendar.MONTH) + 1
    val curDay = todayCal.get(Calendar.DAY_OF_MONTH)

    val bY = birthYear.toIntOrNull()
    val bM = birthMonth.toIntOrNull()
    val bD = birthDay.toIntOrNull()

    val validDate = bY != null && bM != null && bD != null &&
        bY in 1900..curYear && bM in 1..12 && bD in 1..31

    val ageData = remember(bY, bM, bD) {
        if (!validDate) null
        else {
            val dob = Calendar.getInstance().apply {
                isLenient = false
                try {
                    set(bY!!, bM!! - 1, bD!!, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                    time
                } catch (_: Exception) {
                    return@remember null
                }
            }
            val now = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (dob.after(now)) return@remember null

            var years = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            var months = now.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
            var days = now.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH)
            if (days < 0) {
                months -= 1
                val prevMonth = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            if (months < 0) {
                years -= 1
                months += 12
            }

            val totalDays = ((now.timeInMillis - dob.timeInMillis) / (1000L * 60 * 60 * 24)).coerceAtLeast(0L)
            val totalWeeks = totalDays / 7L
            val totalMonths = years * 12L + months
            val totalHours = totalDays * 24L

            val nextBday = Calendar.getInstance().apply {
                set(now.get(Calendar.YEAR), bM!! - 1, bD!!.coerceAtMost(28), 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
                if (!after(now)) add(Calendar.YEAR, 1)
            }
            val daysToNextBday = ((nextBday.timeInMillis - now.timeInMillis) / (1000L * 60 * 60 * 24)).coerceAtLeast(0L)
            val dayNames = listOf("रविवार (Sun)", "सोमवार (Mon)", "मंगलवार (Tue)", "बुधवार (Wed)", "गुरुवार (Thu)", "शुक्रवार (Fri)", "शनिवार (Sat)")
            val bornDayName = dayNames[(dob.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)]

            AgeBreakdown(years, months, days, totalMonths, totalWeeks, totalDays, totalHours, daysToNextBday, bornDayName)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "पर्सनल टूल: आयु कैलकुलेटर • Age Calculator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "आज की तारीख: $curDay/$curMonth/$curYear • जन्मतिथि दर्ज करें:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = birthDay,
                            onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) birthDay = it },
                            label = { Text("दिन (DD)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("age_day_input")
                        )
                        OutlinedTextField(
                            value = birthMonth,
                            onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) birthMonth = it },
                            label = { Text("माह (MM)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("age_month_input")
                        )
                        OutlinedTextField(
                            value = birthYear,
                            onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) birthYear = it },
                            label = { Text("वर्ष (YYYY)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1.3f).testTag("age_year_input")
                        )
                    }
                }
            }
        }

        if (ageData != null) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PeacockTeal)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(
                            text = "आपकी सटीक आयु • Exact Age",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AgeStatBlock("${ageData.years}", "वर्ष • Years")
                            AgeStatBlock("${ageData.months}", "माह • Months")
                            AgeStatBlock("${ageData.days}", "दिन • Days")
                        }
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("अगला जन्मदिन • Next Birthday", style = MaterialTheme.typography.labelMedium, color = Color.White)
                                Text(
                                    text = "${ageData.daysToNextBirthday} दिन शेष",
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = Color(0xFFFFE082),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("जीवन सारांश • Life Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        SummaryRow("जन्म का दिन • Born On", ageData.bornDayName)
                        SummaryRow("कुल महीने • Total Months", "${ageData.totalMonths}")
                        SummaryRow("कुल सप्ताह • Total Weeks", "${ageData.totalWeeks}")
                        SummaryRow("कुल दिन • Total Days", "${ageData.totalDays}")
                        SummaryRow("कुल घंटे • Total Hours", "${ageData.totalHours}")
                    }
                }
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "कृपया सही जन्मतिथि दर्ज करें (आज से पहले की तारीख)।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

private data class AgeBreakdown(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalMonths: Long,
    val totalWeeks: Long,
    val totalDays: Long,
    val totalHours: Long,
    val daysToNextBirthday: Long,
    val bornDayName: String
)

@Composable
private fun AgeStatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMonoFamily),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
            fontWeight = FontWeight.SemiBold
        )
    }
}
