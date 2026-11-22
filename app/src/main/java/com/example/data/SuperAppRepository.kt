package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SuperAppRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        }
        return uid
    }

    private fun <T> Flow<T>.logAndRethrowFirestoreErrors(
        uid: String,
        op: OperationType,
        path: String
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
        throw error
    }

    // --- 1. User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Session mismatch" }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else {
                        null
                    }
                }
                .logAndRethrowFirestoreErrors(uid, OperationType.GET, path)
        )
    }

    suspend fun ensureAndSaveUserProfile(
        displayName: String,
        email: String?,
        phone: String?,
        businessName: String?,
        district: String?,
        address: String?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid"
        return try {
            val cleanName = displayName.trim().take(80)
            require(cleanName.isNotEmpty()) { "Name cannot be empty" }
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (existing.exists()) {
                val updatePayload = mapOf<String, Any?>(
                    "displayName" to cleanName,
                    "email" to email?.trim()?.take(120)?.ifEmpty { null },
                    "phone" to phone?.trim()?.take(20)?.ifEmpty { null },
                    "businessName" to businessName?.trim()?.take(100)?.ifEmpty { null },
                    "district" to district?.trim()?.take(60)?.ifEmpty { null },
                    "address" to address?.trim()?.take(250)?.ifEmpty { null },
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf<String, Any?>(
                    "userId" to uid,
                    "displayName" to cleanName,
                    "email" to email?.trim()?.take(120)?.ifEmpty { null },
                    "phone" to phone?.trim()?.take(20)?.ifEmpty { null },
                    "businessName" to businessName?.trim()?.take(100)?.ifEmpty { null },
                    "district" to (district?.trim()?.take(60)?.ifEmpty { null } ?: "Patna"),
                    "address" to address?.trim()?.take(250)?.ifEmpty { null },
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null } as Map<String, Any>
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- 2. Business Tools: Hisab Kitab ---

    fun observeKhataEntries(userId: String): Flow<List<KhataEntry>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Session mismatch" }
        val path = "users/$uid/khataEntries"
        emitAll(
            db.collection("users").document(uid).collection("khataEntries")
                .snapshots()
                .map { snap ->
                    snap.toObjects(KhataEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logAndRethrowFirestoreErrors(uid, OperationType.LIST, path)
        )
    }

    suspend fun addKhataEntry(
        customerName: String,
        customerPhone: String?,
        entryType: String,
        amount: Double,
        note: String?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        var path = "users/$uid/khataEntries"
        return try {
            val cleanName = customerName.trim().take(80)
            require(cleanName.isNotEmpty()) { "Customer name is required" }
            require(entryType == "GAVE" || entryType == "GOT") { "Invalid entry type" }
            require(amount > 0.0 && amount <= 100000000.0) { "Amount must be greater than zero" }
            val docRef = db.collection("users").document(uid).collection("khataEntries").document()
            path = docRef.path
            val payload = mapOf<String, Any?>(
                "customerName" to cleanName,
                "customerPhone" to customerPhone?.trim()?.take(20)?.ifEmpty { null },
                "entryType" to entryType,
                "amount" to amount,
                "note" to note?.trim()?.take(200)?.ifEmpty { null },
                "settled" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleKhataSettled(entryId: String, settled: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid/khataEntries/$entryId"
        return try {
            require(entryId.isNotBlank()) { "Entry ID is required" }
            db.collection("users").document(uid).collection("khataEntries").document(entryId)
                .update(
                    mapOf(
                        "settled" to settled,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteKhataEntry(entryId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid/khataEntries/$entryId"
        return try {
            require(entryId.isNotBlank()) { "Entry ID is required" }
            db.collection("users").document(uid).collection("khataEntries").document(entryId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- 3. E-Commerce Orders ---

    fun observeOrders(userId: String): Flow<List<ShopOrder>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Session mismatch" }
        val path = "users/$uid/orders"
        emitAll(
            db.collection("users").document(uid).collection("orders")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ShopOrder::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logAndRethrowFirestoreErrors(uid, OperationType.LIST, path)
        )
    }

    suspend fun placeOrder(
        itemsSummary: String,
        itemCount: Int,
        totalAmount: Double,
        paymentMethod: String,
        deliveryAddress: String
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        var path = "users/$uid/orders"
        return try {
            val cleanSummary = itemsSummary.trim().take(300)
            val cleanAddress = deliveryAddress.trim().take(250)
            require(cleanSummary.isNotEmpty()) { "Order items missing" }
            require(itemCount in 1..100) { "Item count out of bounds" }
            require(totalAmount > 0.0 && totalAmount <= 10000000.0) { "Invalid order total" }
            require(paymentMethod in listOf("COD", "UPI", "CARD")) { "Invalid payment method" }
            require(cleanAddress.length >= 3) { "Please enter a delivery address" }

            val orderNumber = "BS-${UUID.randomUUID().toString().take(8).uppercase()}"
            val orderRef = db.collection("users").document(uid).collection("orders").document()
            path = orderRef.path
            val orderPayload = mapOf(
                "orderNumber" to orderNumber,
                "itemsSummary" to cleanSummary,
                "itemCount" to itemCount,
                "totalAmount" to totalAmount,
                "paymentMethod" to paymentMethod,
                "deliveryAddress" to cleanAddress,
                "status" to "PLACED",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            orderRef.set(orderPayload).await()

            // Also record in unified transaction history
            recordTransaction(
                category = "ORDER",
                title = "Order $orderNumber",
                subtitle = "$cleanSummary ($paymentMethod)",
                referenceId = orderNumber,
                amount = totalAmount,
                status = "SUCCESS"
            )
            Result.success(orderNumber)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid/orders/$orderId"
        return try {
            require(orderId.isNotBlank()) { "Order ID required" }
            require(newStatus in listOf("SHIPPED", "DELIVERED", "CANCELLED")) { "Invalid status" }
            db.collection("users").document(uid).collection("orders").document(orderId)
                .update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- 4. Unified Transactions (AEPS, Banking, PAN, Recharge, Cash Tally, Orders) ---

    fun observeTransactions(userId: String): Flow<List<ServiceTransaction>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Session mismatch" }
        val path = "users/$uid/transactions"
        emitAll(
            db.collection("users").document(uid).collection("transactions")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ServiceTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logAndRethrowFirestoreErrors(uid, OperationType.LIST, path)
        )
    }

    suspend fun recordTransaction(
        category: String,
        title: String,
        subtitle: String?,
        referenceId: String,
        amount: Double,
        status: String = "SUCCESS"
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        var path = "users/$uid/transactions"
        return try {
            require(category in listOf("AEPS", "BANKING", "PAN", "RECHARGE", "CASH_TALLY", "ORDER"))
            val cleanTitle = title.trim().take(100)
            val cleanRef = referenceId.trim().take(60)
            require(cleanTitle.isNotEmpty()) { "Title required" }
            require(cleanRef.length >= 3) { "Reference ID required" }
            require(amount >= 0.0 && amount <= 100000000.0) { "Amount out of bounds" }
            val docRef = db.collection("users").document(uid).collection("transactions").document()
            path = docRef.path
            val payload = mapOf<String, Any?>(
                "category" to category,
                "title" to cleanTitle,
                "subtitle" to subtitle?.trim()?.take(200)?.ifEmpty { null },
                "referenceId" to cleanRef,
                "amount" to amount,
                "status" to status,
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            docRef.set(payload).await()
            Result.success(cleanRef)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid/transactions/$transactionId"
        return try {
            require(transactionId.isNotBlank())
            db.collection("users").document(uid).collection("transactions").document(transactionId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- 5. Saved News Articles ---

    fun observeSavedArticles(userId: String): Flow<List<SavedArticle>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Session mismatch" }
        val path = "users/$uid/savedArticles"
        emitAll(
            db.collection("users").document(uid).collection("savedArticles")
                .snapshots()
                .map { snap ->
                    snap.toObjects(SavedArticle::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logAndRethrowFirestoreErrors(uid, OperationType.LIST, path)
        )
    }

    suspend fun saveArticle(article: NewsArticle): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        var path = "users/$uid/savedArticles"
        return try {
            val docRef = db.collection("users").document(uid).collection("savedArticles").document()
            path = docRef.path
            val payload = mapOf<String, Any?>(
                "title" to article.title.trim().take(250),
                "source" to article.source.trim().take(100).ifEmpty { "News Desk" },
                "region" to article.region.code,
                "link" to article.link.trim().take(500).ifEmpty { null },
                "pubDate" to article.pubDate.trim().take(80).ifEmpty { null },
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun removeSavedArticle(articleId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid/savedArticles/$articleId"
        return try {
            require(articleId.isNotBlank())
            db.collection("users").document(uid).collection("savedArticles").document(articleId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
