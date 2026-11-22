package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CartItem
import com.example.data.Product
import com.example.data.ProductCatalog
import com.example.data.ProductCategory
import com.example.data.UserProfile
import com.example.data.UserSettings
import com.example.data.localizedText
import com.example.ui.components.CategoryBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.formatInr
import com.example.ui.theme.CreditEmerald
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MarigoldSaffron
import com.example.ui.theme.MarigoldSoft
import com.example.ui.theme.PeacockTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    cartItems: List<CartItem>,
    userProfile: UserProfile?,
    settings: UserSettings,
    onAddToCart: (Product) -> Unit,
    onUpdateCartQty: (String, Int) -> Unit,
    onPlaceOrder: (paymentMethod: String, address: String, onDone: () -> Unit) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ProductCategory.ALL) }
    var query by remember { mutableStateOf("") }
    var showCartSheet by remember { mutableStateOf(false) }

    val filteredProducts = remember(selectedCategory, query) {
        val q = query.trim().lowercase()
        ProductCatalog.products.filter { product ->
            val catOk = selectedCategory == ProductCategory.ALL || product.category == selectedCategory
            val qOk = q.isEmpty() || product.name.lowercase().contains(q) ||
                product.hiName.lowercase().contains(q) || product.description.lowercase().contains(q)
            catOk && qOk
        }
    }

    val totalCartItems = cartItems.sumOf { it.quantity }
    val totalCartAmount = cartItems.sumOf { it.product.price * it.quantity }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Shop Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(142.dp)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_shop_banner_1795339173439),
                        contentDescription = "Bihar Seva Online Marketplace",
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
                                        PeacockTeal.copy(alpha = 0.55f),
                                        Color.Transparent
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
                        CategoryBadge(
                            label = "E-COMMERCE • बिहार बाज़ार",
                            containerColor = MarigoldSaffron,
                            contentColor = Color.White
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = localizedText(settings.language, "ऑनलाइन खरीदारी", "Online Shopping"),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "मिथिला शिल्प, इलेक्ट्रॉनिक्स व घरेलू उत्पाद",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("प्रोडक्ट खोजें: मखाना, सिल्क, प्रिंटर...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search products") },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_search_input")
                )
            }

            // Category Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ProductCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(localizedText(settings.language, cat.hiLabel, cat.enLabel)) }
                        )
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.LocalMall,
                        title = "कोई प्रोडक्ट नहीं मिला",
                        subtitle = "दूसरा शब्द या कैटेगरी चुनकर खोजें।"
                    )
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    val qtyInCart = cartItems.find { it.product.id == product.id }?.quantity ?: 0
                    ProductCard(
                        product = product,
                        quantityInCart = qtyInCart,
                        onAdd = { onAddToCart(product) },
                        onDelta = { delta -> onUpdateCartQty(product.id, delta) }
                    )
                }
            }
        }

        // Floating Cart Action Button
        ExtendedFloatingActionButton(
            onClick = { showCartSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("shop_open_cart_fab"),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White,
            icon = {
                BadgedBox(
                    badge = {
                        if (totalCartItems > 0) {
                            Badge(containerColor = PeacockTeal, contentColor = Color.White) {
                                Text("$totalCartItems")
                            }
                        }
                    }
                ) {
                    Icon(Icons.Rounded.ShoppingCart, contentDescription = "Open cart")
                }
            },
            text = {
                Text(
                    if (totalCartItems > 0) "कार्ट • ${formatInr(totalCartAmount)}"
                    else "कार्ट (0)"
                )
            }
        )
    }

    if (showCartSheet) {
        var paymentMethod by remember { mutableStateOf("COD") }
        var deliveryAddress by remember(userProfile) {
            mutableStateOf(
                userProfile?.address?.ifBlank { "${userProfile.district.ifBlank { settings.defaultDistrict }}, Bihar" }
                    ?: "${settings.defaultDistrict}, Bihar"
            )
        }
        ModalBottomSheet(onDismissRequest = { showCartSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "आपका शॉपिंग कार्ट • Your Cart ($totalCartItems)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (cartItems.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Rounded.ShoppingCart,
                        title = "कार्ट खाली है",
                        subtitle = "खरीदारी के लिए पहले प्रोडक्ट चुनें।"
                    )
                } else {
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.hiName,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${formatInr(item.product.price)} x ${item.quantity}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onUpdateCartQty(item.product.id, -1) }) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Decrease quantity")
                                }
                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { onUpdateCartQty(item.product.id, 1) }) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Increase quantity")
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("डिलीवरी पता • Delivery Address") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_address_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("COD" to "Cash on Delivery", "UPI" to "UPI", "CARD" to "Card").forEach { (code, label) ->
                            FilterChip(
                                selected = paymentMethod == code,
                                onClick = { paymentMethod = code },
                                label = { Text(label) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("कुल राशि • Total", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = formatInr(totalCartAmount),
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onPlaceOrder(paymentMethod, deliveryAddress) {
                                showCartSheet = false
                            }
                        },
                        enabled = deliveryAddress.trim().length >= 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("place_order_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ऑर्डर कन्फर्म करें • Place Order")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onDelta: (Int) -> Unit
) {
    val discountPercent = (((product.mrp - product.price) / product.mrp) * 100).toInt().coerceAtLeast(0)
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(
                    label = product.badge,
                    containerColor = MarigoldSoft,
                    contentColor = MarigoldSaffron
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Rating",
                        tint = HeritageGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "${product.rating}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = product.hiName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = formatInr(product.price),
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatInr(product.mrp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFamily,
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (discountPercent > 0) {
                        Text(
                            text = "$discountPercent% बचत • Free Delivery",
                            style = MaterialTheme.typography.labelSmall,
                            color = CreditEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (quantityInCart == 0) {
                    Button(
                        onClick = onAdd,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_to_cart_${product.id}")
                    ) {
                        Icon(Icons.Rounded.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("जोड़ें")
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onDelta(-1) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Remove one")
                            }
                            Text(
                                text = "$quantityInCart",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onDelta(1) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add one")
                            }
                        }
                    }
                }
            }
        }
    }
}
