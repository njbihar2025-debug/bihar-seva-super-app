package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.UserSettings
import com.example.ui.components.CategoryBadge
import com.example.ui.components.formatInr
import com.example.ui.theme.CreditEmerald
import com.example.ui.theme.CreditEmeraldContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MarigoldSaffron
import com.example.ui.theme.PeacockTeal
import java.util.UUID
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankingServicesScreen(
    initialTab: Int,
    settings: UserSettings,
    onRecordTransaction: (category: String, title: String, subtitle: String, refId: String, amount: Double, onDone: (String) -> Unit) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    LaunchedEffect(initialTab) {
        selectedTab = initialTab.coerceIn(0, 3)
    }
    var lastReceipt by remember { mutableStateOf<String?>(null) }

    val tabs = listOf(
        "AEPS" to Icons.Rounded.Fingerprint,
        "बैंकिंग / EMI" to Icons.Rounded.AccountBalance,
        "PAN सेवाएं" to Icons.Rounded.Badge,
        "रिचार्ज" to Icons.Rounded.PhoneAndroid
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(118.dp)
                .clip(RoundedCornerShape(22.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_banking_seva_1795339172628),
                contentDescription = "Banking AEPS and Digital Services banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PeacockTeal.copy(alpha = 0.92f),
                                PeacockTeal.copy(alpha = 0.55f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                CategoryBadge("BANKING & DIGITAL SEVA", MarigoldSaffron, Color.White)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "AEPS • बैंकिंग • PAN • रिचार्ज",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "हर सेवा की डिजिटल रसीद सीधे 'हिस्ट्री' में सुरक्षित होती है",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        PrimaryScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("banking_tab_$index"),
                    text = { Text(title) },
                    icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (lastReceipt != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CreditEmeraldContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = CreditEmerald)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "रसीद हिस्ट्री में सेव हो गई • Receipt Logged",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CreditEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = lastReceipt ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = CreditEmerald
                                )
                            }
                        }
                    }
                }
            }

            item {
                when (selectedTab) {
                    0 -> AepsDeskCard { title, sub, ref, amt ->
                        onRecordTransaction("AEPS", title, sub, ref, amt) { savedRef ->
                            lastReceipt = "$title • Ref: $savedRef"
                        }
                    }
                    1 -> BankingAndEmiCard { title, sub, ref, amt ->
                        onRecordTransaction("BANKING", title, sub, ref, amt) { savedRef ->
                            lastReceipt = "$title • Ref: $savedRef"
                        }
                    }
                    2 -> PanServicesCard { title, sub, ref, amt ->
                        onRecordTransaction("PAN", title, sub, ref, amt) { savedRef ->
                            lastReceipt = "$title • Ref: $savedRef"
                        }
                    }
                    else -> RechargeDeskCard { title, sub, ref, amt ->
                        onRecordTransaction("RECHARGE", title, sub, ref, amt) { savedRef ->
                            lastReceipt = "$title • Ref: $savedRef"
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "नोट: यह सेवा डेस्क आपके CSP/व्यक्तिगत लेन-देन का डिजिटल लॉग व रसीद रिकॉर्ड सुरक्षित रखता है। पूरा आधार नंबर कभी दर्ज न करें—केवल अंतिम 4 अंक दर्ज करें।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AepsDeskCard(
    onSubmit: (title: String, subtitle: String, refId: String, amount: Double) -> Unit
) {
    val banks = listOf("SBI", "PNB", "Dakshin Bihar Gramin", "Uttar Bihar Gramin", "Bank of Baroda", "HDFC", "ICICI")
    val ops = listOf("Cash Withdrawal", "Balance Enquiry", "Mini Statement", "Aadhaar Pay")
    var selectedBank by remember { mutableStateOf(banks.first()) }
    var selectedOp by remember { mutableStateOf(ops.first()) }
    var last4Aadhaar by remember { mutableStateOf("") }
    var customerMobile by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("1000") }

    val needsAmount = selectedOp == "Cash Withdrawal" || selectedOp == "Aadhaar Pay"
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = last4Aadhaar.length == 4 && customerMobile.length == 10 && (!needsAmount || parsedAmount > 0.0)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("AEPS सेवा डेस्क • Aadhaar Banking Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ops.forEach { op ->
                    FilterChip(selected = selectedOp == op, onClick = { selectedOp = op }, label = { Text(op) })
                }
            }
            Text("बैंक चुनें • Select Bank", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                banks.forEach { bank ->
                    FilterChip(selected = selectedBank == bank, onClick = { selectedBank = bank }, label = { Text(bank) })
                }
            }
            OutlinedTextField(
                value = last4Aadhaar,
                onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) last4Aadhaar = it },
                label = { Text("आधार के अंतिम 4 अंक • Last 4 Aadhaar Digits") },
                prefix = { Text("XXXX-XXXX-") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("aeps_aadhaar_input")
            )
            OutlinedTextField(
                value = customerMobile,
                onValueChange = { if (it.length <= 10 && it.all(Char::isDigit)) customerMobile = it },
                label = { Text("ग्राहक मोबाइल (10 अंक) • Mobile Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("aeps_mobile_input")
            )
            if (needsAmount) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("राशि (₹) • Amount") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("aeps_amount_input")
                )
            }
            Button(
                onClick = {
                    val ref = "AEPS-${UUID.randomUUID().toString().take(8).uppercase()}"
                    val amt = if (needsAmount) parsedAmount else 0.0
                    onSubmit(
                        "AEPS $selectedOp",
                        "$selectedBank • Aadhaar XXXX-$last4Aadhaar • Mob $customerMobile",
                        ref,
                        amt
                    )
                    last4Aadhaar = ""
                    customerMobile = ""
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("aeps_submit_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("रसीद जनरेट व सेव करें • Log AEPS Receipt")
            }
        }
    }
}

@Composable
private fun BankingAndEmiCard(
    onSubmit: (title: String, subtitle: String, refId: String, amount: Double) -> Unit
) {
    var beneficiaryName by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }

    // EMI Calculator state
    var loanAmountText by remember { mutableStateOf("100000") }
    var interestRateText by remember { mutableStateOf("10.5") }
    var tenureMonthsText by remember { mutableStateOf("24") }

    val principal = loanAmountText.toDoubleOrNull() ?: 0.0
    val annualRate = interestRateText.toDoubleOrNull() ?: 0.0
    val months = tenureMonthsText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val monthlyRate = annualRate / 12.0 / 100.0
    val emi = if (principal <= 0.0) 0.0
    else if (monthlyRate == 0.0) principal / months
    else (principal * monthlyRate * (1 + monthlyRate).pow(months)) / ((1 + monthlyRate).pow(months) - 1)
    val totalPayable = emi * months

    val parsedTransfer = transferAmount.toDoubleOrNull() ?: 0.0
    val validTransfer = beneficiaryName.trim().length >= 2 &&
        ifscCode.matches(Regex("^[A-Z]{4}0[A-Z0-9]{6}$")) &&
        parsedTransfer > 0.0

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("बैंकिंग ट्रांसफर लॉग • Bank Transfer (DMT)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = beneficiaryName,
                    onValueChange = { beneficiaryName = it },
                    label = { Text("खाताधारक का नाम • Beneficiary Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ifscCode,
                    onValueChange = { if (it.length <= 11) ifscCode = it.uppercase() },
                    label = { Text("IFSC कोड (जैसे SBIN0001234)") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = transferAmount,
                    onValueChange = { transferAmount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("ट्रांसफर राशि (₹) • Amount") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val ref = "DMT-${UUID.randomUUID().toString().take(8).uppercase()}"
                        onSubmit(
                            "Bank Transfer to ${beneficiaryName.trim()}",
                            "IFSC: $ifscCode",
                            ref,
                            parsedTransfer
                        )
                        beneficiaryName = ""
                        ifscCode = ""
                        transferAmount = ""
                    },
                    enabled = validTransfer,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("banking_transfer_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("बैंकिंग रिकॉर्ड सेव करें • Save Transfer Receipt")
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("लोन EMI कैलकुलेटर • Loan EMI Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = loanAmountText,
                        onValueChange = { loanAmountText = it.filter(Char::isDigit) },
                        label = { Text("राशि (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                    OutlinedTextField(
                        value = interestRateText,
                        onValueChange = { interestRateText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("ब्याज %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                    OutlinedTextField(
                        value = tenureMonthsText,
                        onValueChange = { tenureMonthsText = it.filter(Char::isDigit) },
                        label = { Text("माह") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("मासिक EMI", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatInr(emi),
                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("कुल भुगतान", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = formatInr(totalPayable),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PanServicesCard(
    onSubmit: (title: String, subtitle: String, refId: String, amount: Double) -> Unit
) {
    val context = LocalContext.current
    val services = listOf("New PAN (Form 49A)", "PAN Correction", "Reprint e-PAN", "PAN-Aadhaar Link Check")
    var selectedService by remember { mutableStateOf(services.first()) }
    var applicantName by remember { mutableStateOf("") }
    var panNumber by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }

    val needsExistingPan = selectedService != "New PAN (Form 49A)"
    val panRegex = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    val isValid = applicantName.trim().length >= 2 &&
        mobile.length == 10 &&
        (!needsExistingPan || panRegex.matches(panNumber))

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("PAN कार्ड डिजिटल सेवाएं • PAN Desk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                services.forEach { svc ->
                    FilterChip(selected = selectedService == svc, onClick = { selectedService = svc }, label = { Text(svc) })
                }
            }
            OutlinedTextField(
                value = applicantName,
                onValueChange = { applicantName = it },
                label = { Text("आवेदक का नाम • Applicant Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("pan_name_input")
            )
            if (needsExistingPan) {
                OutlinedTextField(
                    value = panNumber,
                    onValueChange = { if (it.length <= 10) panNumber = it.uppercase() },
                    label = { Text("PAN नंबर (जैसे ABCDE1234F)") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("pan_number_input")
                )
            }
            OutlinedTextField(
                value = mobile,
                onValueChange = { if (it.length <= 10 && it.all(Char::isDigit)) mobile = it },
                label = { Text("मोबाइल नंबर (10 अंक)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("pan_mobile_input")
            )
            Button(
                onClick = {
                    val ref = "PAN-${UUID.randomUUID().toString().take(8).uppercase()}"
                    val fee = if (selectedService == "PAN-Aadhaar Link Check") 0.0 else 107.0
                    val detail = buildString {
                        append(applicantName.trim())
                        if (needsExistingPan) append(" • $panNumber")
                        append(" • Mob $mobile")
                    }
                    onSubmit(selectedService, detail, ref, fee)
                    applicantName = ""
                    panNumber = ""
                    mobile = ""
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("pan_submit_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("PAN आवेदन रसीद सेव करें • Log PAN Request")
            }
            OutlinedButton(
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.onlineservices.nsdl.com/paam/endUserRegisterContact.html"))
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("आधिकारिक NSDL PAN पोर्टल खोलें")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RechargeDeskCard(
    onSubmit: (title: String, subtitle: String, refId: String, amount: Double) -> Unit
) {
    val operators = listOf("Jio", "Airtel", "Vi", "BSNL", "Tata Play DTH", "Airtel DTH", "Dish TV")
    val quickPlans = listOf(199.0, 299.0, 349.0, 666.0, 899.0, 2999.0)
    var selectedOperator by remember { mutableStateOf(operators.first()) }
    var subscriberNumber by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("299") }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = subscriberNumber.length in 10..12 && parsedAmount > 0.0

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("मोबाइल व DTH रिचार्ज • Quick Recharge", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                operators.forEach { op ->
                    FilterChip(selected = selectedOperator == op, onClick = { selectedOperator = op }, label = { Text(op) })
                }
            }
            OutlinedTextField(
                value = subscriberNumber,
                onValueChange = { if (it.length <= 12 && it.all(Char::isDigit)) subscriberNumber = it },
                label = { Text("मोबाइल / सब्सक्राइबर नंबर") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("recharge_number_input")
            )
            Text("पॉपुलर प्लान चुनें • Popular Plans", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                quickPlans.forEach { plan ->
                    FilterChip(
                        selected = amountText == plan.toInt().toString(),
                        onClick = { amountText = plan.toInt().toString() },
                        label = { Text("₹${plan.toInt()}") }
                    )
                }
            }
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("रिचार्ज राशि (₹)") },
                prefix = { Text("₹ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("recharge_amount_input")
            )
            Button(
                onClick = {
                    val ref = "RCH-${UUID.randomUUID().toString().take(8).uppercase()}"
                    onSubmit(
                        "$selectedOperator Recharge",
                        "No: $subscriberNumber • Bihar Circle",
                        ref,
                        parsedAmount
                    )
                    subscriberNumber = ""
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("recharge_submit_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("रिचार्ज रसीद सेव करें • Complete Recharge (${formatInr(parsedAmount)})")
            }
        }
    }
}
