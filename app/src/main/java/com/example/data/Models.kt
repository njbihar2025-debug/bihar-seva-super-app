package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

// --- Firestore Persisted Models (1:1 parity with firebase-blueprint.json & firestore.rules) ---

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val businessName: String = "",
    val district: String = "Patna",
    val address: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class KhataEntry(
    @DocumentId val id: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val entryType: String = "GAVE", // "GAVE" (लेना है) or "GOT" (देना है)
    val amount: Double = 0.0,
    val note: String = "",
    val settled: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ShopOrder(
    @DocumentId val id: String = "",
    val orderNumber: String = "",
    val itemsSummary: String = "",
    val itemCount: Long = 1L,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "COD", // "COD", "UPI", "CARD"
    val deliveryAddress: String = "",
    val status: String = "PLACED", // "PLACED", "SHIPPED", "DELIVERED", "CANCELLED"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ServiceTransaction(
    @DocumentId val id: String = "",
    val category: String = "AEPS", // "AEPS", "BANKING", "PAN", "RECHARGE", "CASH_TALLY", "ORDER"
    val title: String = "",
    val subtitle: String = "",
    val referenceId: String = "",
    val amount: Double = 0.0,
    val status: String = "SUCCESS", // "SUCCESS", "PENDING", "FAILED"
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SavedArticle(
    @DocumentId val id: String = "",
    val title: String = "",
    val source: String = "",
    val region: String = "BIHAR", // "LOCAL", "BIHAR", "NATIONAL"
    val link: String = "",
    val pubDate: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

// --- App Domain & UI Models ---

enum class NewsRegion(val code: String, val hiLabel: String, val enLabel: String) {
    LOCAL("LOCAL", "लोकल", "Local"),
    BIHAR("BIHAR", "बिहार", "Bihar"),
    NATIONAL("NATIONAL", "देश", "National")
}

data class NewsArticle(
    val id: String,
    val title: String,
    val description: String,
    val source: String,
    val pubDate: String,
    val link: String,
    val region: NewsRegion
)

enum class ProductCategory(val hiLabel: String, val enLabel: String) {
    ALL("सभी", "All"),
    MITHILA("मिथिला व शिल्प", "Bihar Craft"),
    ELECTRONICS("इलेक्ट्रॉनिक्स", "Electronics"),
    GROCERY("किराना व ऑर्गेनिक", "Grocery"),
    HOME("होम व किचन", "Home & Kitchen"),
    FASHION("फैशन", "Fashion")
}

data class Product(
    val id: String,
    val name: String,
    val hiName: String,
    val category: ProductCategory,
    val price: Double,
    val mrp: Double,
    val rating: Double,
    val badge: String,
    val description: String
)

data class CartItem(
    val product: Product,
    val quantity: Int
)

enum class ThemeMode(val labelHi: String, val labelEn: String) {
    SYSTEM("सिस्टम", "System"),
    LIGHT("लाइट", "Light"),
    DARK("डार्क", "Dark")
}

enum class AppLanguage(val label: String) {
    BILINGUAL("हिंदी + English"),
    HINDI("केवल हिंदी"),
    ENGLISH("English Only")
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.BILINGUAL,
    val defaultDistrict: String = "Patna",
    val defaultGstPercent: Int = 18,
    val compactMode: Boolean = false,
    val hapticFeedback: Boolean = true
)

val BiharDistricts = listOf(
    "Patna",
    "Muzaffarpur",
    "Gaya",
    "Bhagalpur",
    "Darbhanga",
    "Purnia",
    "Nalanda",
    "Begusarai",
    "Arrah",
    "Chapra",
    "Katihar",
    "Madhubani"
)

fun localizedText(language: AppLanguage, hi: String, en: String): String = when (language) {
    AppLanguage.HINDI -> hi
    AppLanguage.ENGLISH -> en
    AppLanguage.BILINGUAL -> "$hi • $en"
}
