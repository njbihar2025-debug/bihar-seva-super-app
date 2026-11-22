package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppLanguage
import com.example.data.CartItem
import com.example.data.KhataEntry
import com.example.data.NewsArticle
import com.example.data.NewsRegion
import com.example.data.NewsRepository
import com.example.data.Product
import com.example.data.SavedArticle
import com.example.data.ServiceTransaction
import com.example.data.SettingsDataStore
import com.example.data.ShopOrder
import com.example.data.SuperAppRepository
import com.example.data.ThemeMode
import com.example.data.UiState
import com.example.data.UserProfile
import com.example.data.UserSettings
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SuperAppViewModel(
    private val repository: SuperAppRepository,
    private val newsRepository: NewsRepository,
    private val settingsStore: SettingsDataStore,
    private val currentUserId: String
) : ViewModel() {

    private companion object {
        const val TAG = "SuperAppVM"
        const val STOP_TIMEOUT_MS = 5000L
    }

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    val settings: StateFlow<UserSettings> = settingsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UserSettings())

    private fun <T> Flow<T>.toSafeUiState(label: String): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing $label", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load $label"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = UiState.Loading
            )

    val userProfileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId).toSafeUiState("profile")

    val khataState: StateFlow<UiState<List<KhataEntry>>> =
        repository.observeKhataEntries(currentUserId).toSafeUiState("Hisab Kitab")

    val ordersState: StateFlow<UiState<List<ShopOrder>>> =
        repository.observeOrders(currentUserId).toSafeUiState("orders")

    val transactionsState: StateFlow<UiState<List<ServiceTransaction>>> =
        repository.observeTransactions(currentUserId).toSafeUiState("history")

    val savedArticlesState: StateFlow<UiState<List<SavedArticle>>> =
        repository.observeSavedArticles(currentUserId).toSafeUiState("saved news")

    // --- Live News State ---
    private val _selectedNewsRegion = MutableStateFlow(NewsRegion.BIHAR)
    val selectedNewsRegion: StateFlow<NewsRegion> = _selectedNewsRegion.asStateFlow()

    private val _newsState = MutableStateFlow<UiState<List<NewsArticle>>>(UiState.Loading)
    val newsState: StateFlow<UiState<List<NewsArticle>>> = _newsState.asStateFlow()

    // --- E-Commerce Cart State ---
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        bootstrapProfileIfNeeded()
        refreshNews(NewsRegion.BIHAR)
    }

    private fun bootstrapProfileIfNeeded() {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            repository.ensureAndSaveUserProfile(
                displayName = user.displayName?.ifBlank { "Bihar Seva User" } ?: "Bihar Seva User",
                email = user.email,
                phone = user.phoneNumber,
                businessName = null,
                district = settings.value.defaultDistrict,
                address = null
            )
        }
    }

    // --- News Actions ---
    fun selectNewsRegion(region: NewsRegion) {
        _selectedNewsRegion.value = region
        refreshNews(region)
    }

    fun refreshNews(region: NewsRegion = _selectedNewsRegion.value, districtOverride: String? = null) {
        val district = districtOverride ?: settings.value.defaultDistrict
        val preferEnglish = settings.value.language == AppLanguage.ENGLISH
        viewModelScope.launch {
            _newsState.value = UiState.Loading
            val result = newsRepository.fetchNews(region, district, preferEnglish)
            result.fold(
                onSuccess = { list -> _newsState.value = UiState.Success(list) },
                onFailure = { err ->
                    _newsState.value = UiState.Error(err.localizedMessage ?: "Unable to fetch live news")
                }
            )
        }
    }

    fun bookmarkArticle(article: NewsArticle) {
        viewModelScope.launch {
            repository.saveArticle(article).fold(
                onSuccess = { _toastMessage.tryEmit("समाचार सेव किया गया • Article saved") },
                onFailure = { _toastMessage.tryEmit(it.localizedMessage ?: "Could not save article") }
            )
        }
    }

    fun removeSavedArticle(articleId: String) {
        viewModelScope.launch {
            repository.removeSavedArticle(articleId).fold(
                onSuccess = { _toastMessage.tryEmit("बुकमार्क हटाया गया • Bookmark removed") },
                onFailure = { _toastMessage.tryEmit(it.localizedMessage ?: "Failed to remove") }
            )
        }
    }

    // --- Cart & E-Commerce Actions ---
    fun addToCart(product: Product) {
        _cartItems.update { list ->
            val existing = list.find { it.product.id == product.id }
            if (existing != null) {
                list.map {
                    if (it.product.id == product.id) it.copy(quantity = (it.quantity + 1).coerceAtMost(20))
                    else it
                }
            } else {
                list + CartItem(product, 1)
            }
        }
        _toastMessage.tryEmit("${product.name} कार्ट में जोड़ा गया")
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        _cartItems.update { list ->
            list.mapNotNull { item ->
                if (item.product.id == productId) {
                    val nextQty = item.quantity + delta
                    if (nextQty <= 0) null else item.copy(quantity = nextQty.coerceAtMost(20))
                } else item
            }
        }
    }

    fun placeOrder(paymentMethod: String, deliveryAddress: String, onPlaced: () -> Unit) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            _toastMessage.tryEmit("कार्ट खाली है • Cart is empty")
            return
        }
        val summary = items.joinToString(", ") { "${it.product.name} x${it.quantity}" }
        val totalCount = items.sumOf { it.quantity }
        val totalAmount = items.sumOf { it.product.price * it.quantity }

        viewModelScope.launch {
            repository.placeOrder(
                itemsSummary = summary,
                itemCount = totalCount,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                deliveryAddress = deliveryAddress
            ).fold(
                onSuccess = { orderNo ->
                    _cartItems.value = emptyList()
                    _toastMessage.tryEmit("ऑर्डर कन्फर्म हुआ: $orderNo")
                    onPlaced()
                },
                onFailure = { err ->
                    _toastMessage.tryEmit(err.localizedMessage ?: "Order failed")
                }
            )
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus).fold(
                onSuccess = { _toastMessage.tryEmit("ऑर्डर स्टेटस अपडेट: $newStatus") },
                onFailure = { _toastMessage.tryEmit(it.localizedMessage ?: "Unable to update order") }
            )
        }
    }

    // --- Banking (AEPS/Banking), Digital Services (PAN/Recharge) & Cash Tally ---
    fun recordServiceTransaction(
        category: String,
        title: String,
        subtitle: String,
        referenceId: String,
        amount: Double,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.recordTransaction(
                category = category,
                title = title,
                subtitle = subtitle,
                referenceId = referenceId,
                amount = amount
            ).fold(
                onSuccess = { ref ->
                    _toastMessage.tryEmit("रसीद सेव हुई • Ref: $ref")
                    onSuccess(ref)
                },
                onFailure = { err ->
                    _toastMessage.tryEmit(err.localizedMessage ?: "Transaction save failed")
                }
            )
        }
    }

    fun deleteTransaction(txId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(txId).fold(
                onSuccess = { _toastMessage.tryEmit("रिकॉर्ड हटाया गया • Deleted") },
                onFailure = { _toastMessage.tryEmit(it.localizedMessage ?: "Delete failed") }
            )
        }
    }

    // --- Business Tools: Hisab Kitab ---
    fun addKhataEntry(
        customerName: String,
        customerPhone: String,
        entryType: String,
        amount: Double,
        note: String,
        onAdded: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addKhataEntry(
                customerName = customerName,
                customerPhone = customerPhone.ifBlank { null },
                entryType = entryType,
                amount = amount,
                note = note.ifBlank { null }
            ).fold(
                onSuccess = {
                    _toastMessage.tryEmit("हिसाब किताब में एंट्री सेव हुई")
                    onAdded()
                },
                onFailure = { err ->
                    _toastMessage.tryEmit(err.localizedMessage ?: "Failed to save entry")
                }
            )
        }
    }

    fun toggleKhataSettled(entryId: String, settled: Boolean) {
        viewModelScope.launch {
            repository.toggleKhataSettled(entryId, settled).fold(
                onSuccess = {
                    _toastMessage.tryEmit(if (settled) "हिसाब चुकता किया गया • Settled" else "फिर से खोला गया • Reopened")
                },
                onFailure = { _toastMessage.tryEmit(it.localizedMessage ?: "Update failed") }
            )
        }
    }

    fun deleteKhataEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteKhataEntry(entryId).fold(
                onSuccess = { _toastMessage.tryEmit("एंट्री हटाई गई • Entry deleted") },
                onFailure = { _toastMessage.tryEmit(it.localizedMessage ?: "Delete failed") }
            )
        }
    }

    // --- Profile & Settings ---
    fun saveUserProfile(
        displayName: String,
        email: String,
        phone: String,
        businessName: String,
        district: String,
        address: String
    ) {
        viewModelScope.launch {
            repository.ensureAndSaveUserProfile(
                displayName = displayName,
                email = email.ifBlank { null },
                phone = phone.ifBlank { null },
                businessName = businessName.ifBlank { null },
                district = district.ifBlank { "Patna" },
                address = address.ifBlank { null }
            ).fold(
                onSuccess = {
                    settingsStore.setDefaultDistrict(district.ifBlank { "Patna" })
                    _toastMessage.tryEmit("प्रोफाइल सेव हुई • Profile saved")
                },
                onFailure = { _toastMessage.tryEmit(it.localizedMessage ?: "Profile update failed") }
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsStore.setThemeMode(mode) }
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            settingsStore.setLanguage(lang)
            refreshNews(_selectedNewsRegion.value)
        }
    }

    fun setDefaultDistrict(district: String) {
        viewModelScope.launch {
            settingsStore.setDefaultDistrict(district)
            if (_selectedNewsRegion.value == NewsRegion.LOCAL) {
                refreshNews(NewsRegion.LOCAL, district)
            }
        }
    }

    fun setDefaultGst(gst: Int) {
        viewModelScope.launch { settingsStore.setDefaultGstPercent(gst) }
    }

    fun setCompactMode(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setCompactMode(enabled) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setHapticFeedback(enabled) }
    }
}
