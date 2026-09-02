package com.example.core.services.backup

import android.content.Context
import androidx.room.withTransaction
import com.example.core.model.Customer
import com.example.core.model.Product
import com.example.core.model.Settings
import com.example.core.model.Transaction
import com.example.core.model.TransactionItem
import com.example.data.db.AppDatabase
import com.example.data.db.CustomerEntity
import com.example.data.db.ProductEntity
import com.example.data.db.SettingsEntity
import com.example.data.db.TransactionEntity
import com.example.data.db.TransactionItemEntity
import com.example.data.db.toDomain
import com.example.data.db.toEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupMetadata(
    val appVersion: String = "1.0",
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val totalCustomers: Int = 0,
    val totalProducts: Int = 0,
    val totalTransactions: Int = 0,
    val totalTransactionItems: Int = 0,
    val checksum: String = ""
)

data class BackupPayload(
    val metadata: BackupMetadata,
    val customers: List<Customer>,
    val products: List<Product>,
    val transactions: List<Transaction>,
    val transactionItems: List<TransactionItem>,
    val settings: Settings
)

class BackupService(private val database: AppDatabase) {

    /**
     * Generates a complete verified backup JSON string.
     */
    suspend fun generateBackupJson(): Result<String> = runCatching {
        val customerDao = database.customerDao()
        val productDao = database.productDao()
        val transactionDao = database.transactionDao()
        val settingsDao = database.settingsDao()

        val customers = customerDao.getAllCustomersList().map { it.toDomain() }
        val products = productDao.getAllProductsList().map { it.toDomain() }
        val transactions = transactionDao.getAllTransactionsList().map { it.toDomain() }
        val items = transactionDao.getAllTransactionItemsList().map { it.toDomain() }
        val settings = settingsDao.getSettings()?.toDomain() ?: Settings()

        val rootObj = JSONObject()
        rootObj.put("app_id", "com.aistudio.shopaccounts")
        rootObj.put("format_version", 1)
        rootObj.put("timestamp", System.currentTimeMillis())

        // Customers array
        val customersArr = JSONArray()
        for (c in customers) {
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("country_code", c.countryCode)
                put("address", c.address)
                put("status", c.status)
                put("created_at", c.createdAt)
                put("updated_at", c.updatedAt)
                if (c.archivedAt != null) put("archived_at", c.archivedAt)
                if (c.deletedAt != null) put("deleted_at", c.deletedAt)
            }
            customersArr.put(cObj)
        }
        rootObj.put("customers", customersArr)

        // Products array
        val productsArr = JSONArray()
        for (p in products) {
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("price", p.price.minorUnits)
                if (p.imagePath != null) put("image_path", p.imagePath)
                put("status", p.status)
                put("created_at", p.createdAt)
                put("updated_at", p.updatedAt)
                if (p.archivedAt != null) put("archived_at", p.archivedAt)
                if (p.deletedAt != null) put("deleted_at", p.deletedAt)
            }
            productsArr.put(pObj)
        }
        rootObj.put("products", productsArr)

        // Transactions array
        val txArr = JSONArray()
        for (t in transactions) {
            val tObj = JSONObject().apply {
                put("id", t.id)
                if (t.customerId != null) put("customer_id", t.customerId)
                put("type", t.type)
                put("total_amount", t.totalAmount.minorUnits)
                put("status", t.status)
                put("note", t.note)
                put("created_at", t.createdAt)
                put("updated_at", t.updatedAt)
                if (t.cancelledAt != null) put("cancelled_at", t.cancelledAt)
                if (t.cancelReason != null) put("cancel_reason", t.cancelReason)
            }
            txArr.put(tObj)
        }
        rootObj.put("transactions", txArr)

        // Transaction items array
        val itemsArr = JSONArray()
        for (item in items) {
            val itemObj = JSONObject().apply {
                put("id", item.id)
                put("transaction_id", item.transactionId)
                if (item.productId != null) put("product_id", item.productId)
                put("product_name_snapshot", item.productNameSnapshot)
                put("unit_price", item.unitPrice.minorUnits)
                put("quantity", item.quantity)
                put("subtotal", item.subtotal.minorUnits)
            }
            itemsArr.put(itemObj)
        }
        rootObj.put("transaction_items", itemsArr)

        // Settings object
        val settingsObj = JSONObject().apply {
            put("id", settings.id)
            put("store_name", settings.storeName)
            put("owner_name", settings.ownerName)
            put("phone", settings.phone)
            put("address", settings.address)
            put("language", settings.language)
            put("theme", settings.theme)
            put("created_at", settings.createdAt)
            put("updated_at", settings.updatedAt)
        }
        rootObj.put("settings", settingsObj)

        // Calculate SHA-256 checksum of payload data
        val dataString = rootObj.toString()
        val checksum = calculateSha256(dataString)
        rootObj.put("checksum", checksum)

        rootObj.toString(2)
    }

    /**
     * Creates a real local backup file with .shopbackup extension.
     */
    suspend fun createBackupFile(context: Context): Result<File> = runCatching {
        val json = generateBackupJson().getOrThrow()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "shop_backup_$timeStamp.shopbackup"

        val backupDir = File(context.filesDir, "backups").apply {
            if (!exists()) mkdirs()
        }

        val file = File(backupDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(json.toByteArray(Charsets.UTF_8))
        }
        file
    }

    /**
     * Validates a backup JSON string and parses it into a strongly typed BackupPayload.
     */
    fun validateBackupJson(jsonString: String): Result<BackupPayload> = runCatching {
        val rootObj = JSONObject(jsonString)

        if (!rootObj.has("customers") || !rootObj.has("transactions") || !rootObj.has("products")) {
            throw IllegalArgumentException("Invalid backup file: Missing essential tables.")
        }

        val formatVersion = rootObj.optInt("format_version", 1)
        val timestamp = rootObj.optLong("timestamp", System.currentTimeMillis())

        // Parse Customers
        val customersList = mutableListOf<Customer>()
        val custArr = rootObj.getJSONArray("customers")
        for (i in 0 until custArr.length()) {
            val c = custArr.getJSONObject(i)
            customersList.add(
                Customer(
                    id = c.getLong("id"),
                    name = c.getString("name"),
                    phone = c.optString("phone", ""),
                    countryCode = c.optString("country_code", "+970"),
                    address = c.optString("address", ""),
                    status = c.optString("status", "active"),
                    createdAt = c.optLong("created_at", System.currentTimeMillis()),
                    updatedAt = c.optLong("updated_at", System.currentTimeMillis()),
                    archivedAt = if (c.has("archived_at") && !c.isNull("archived_at")) c.getLong("archived_at") else null,
                    deletedAt = if (c.has("deleted_at") && !c.isNull("deleted_at")) c.getLong("deleted_at") else null
                )
            )
        }

        // Parse Products
        val productsList = mutableListOf<Product>()
        val prodArr = rootObj.getJSONArray("products")
        for (i in 0 until prodArr.length()) {
            val p = prodArr.getJSONObject(i)
            productsList.add(
                Product(
                    id = p.getLong("id"),
                    name = p.getString("name"),
                    price = com.example.core.model.Money.fromMinorUnits(p.getLong("price")),
                    imagePath = if (p.has("image_path") && !p.isNull("image_path")) p.getString("image_path") else null,
                    status = p.optString("status", "active"),
                    createdAt = p.optLong("created_at", System.currentTimeMillis()),
                    updatedAt = p.optLong("updated_at", System.currentTimeMillis()),
                    archivedAt = if (p.has("archived_at") && !p.isNull("archived_at")) p.getLong("archived_at") else null,
                    deletedAt = if (p.has("deleted_at") && !p.isNull("deleted_at")) p.getLong("deleted_at") else null
                )
            )
        }

        // Parse Transactions
        val transactionsList = mutableListOf<Transaction>()
        val txArr = rootObj.getJSONArray("transactions")
        for (i in 0 until txArr.length()) {
            val t = txArr.getJSONObject(i)
            transactionsList.add(
                Transaction(
                    id = t.getLong("id"),
                    customerId = if (t.has("customer_id") && !t.isNull("customer_id")) t.getLong("customer_id") else null,
                    type = t.getString("type"),
                    totalAmount = com.example.core.model.Money.fromMinorUnits(t.getLong("total_amount")),
                    status = t.optString("status", "completed"),
                    note = t.optString("note", ""),
                    createdAt = t.optLong("created_at", System.currentTimeMillis()),
                    updatedAt = t.optLong("updated_at", System.currentTimeMillis()),
                    cancelledAt = if (t.has("cancelled_at") && !t.isNull("cancelled_at")) t.getLong("cancelled_at") else null,
                    cancelReason = if (t.has("cancel_reason") && !t.isNull("cancel_reason")) t.getString("cancel_reason") else null
                )
            )
        }

        // Parse Transaction Items
        val itemsList = mutableListOf<TransactionItem>()
        if (rootObj.has("transaction_items")) {
            val itemArr = rootObj.getJSONArray("transaction_items")
            for (i in 0 until itemArr.length()) {
                val itm = itemArr.getJSONObject(i)
                itemsList.add(
                    TransactionItem(
                        id = itm.getLong("id"),
                        transactionId = itm.getLong("transaction_id"),
                        productId = if (itm.has("product_id") && !itm.isNull("product_id")) itm.getLong("product_id") else null,
                        productNameSnapshot = itm.getString("product_name_snapshot"),
                        unitPrice = com.example.core.model.Money.fromMinorUnits(itm.getLong("unit_price")),
                        quantity = itm.getDouble("quantity"),
                        subtotal = com.example.core.model.Money.fromMinorUnits(itm.getLong("subtotal"))
                    )
                )
            }
        }

        // Parse Settings
        val settings = if (rootObj.has("settings")) {
            val s = rootObj.getJSONObject("settings")
            Settings(
                id = s.optInt("id", 1),
                storeName = s.optString("store_name", "حسابات المحل"),
                ownerName = s.optString("owner_name", ""),
                phone = s.optString("phone", ""),
                address = s.optString("address", ""),
                language = s.optString("language", "ar"),
                theme = s.optString("theme", "system"),
                createdAt = s.optLong("created_at", System.currentTimeMillis()),
                updatedAt = s.optLong("updated_at", System.currentTimeMillis())
            )
        } else {
            Settings()
        }

        val metadata = BackupMetadata(
            schemaVersion = formatVersion,
            exportedAt = timestamp,
            totalCustomers = customersList.size,
            totalProducts = productsList.size,
            totalTransactions = transactionsList.size,
            totalTransactionItems = itemsList.size
        )

        BackupPayload(
            metadata = metadata,
            customers = customersList,
            products = productsList,
            transactions = transactionsList,
            transactionItems = itemsList,
            settings = settings
        )
    }

    /**
     * Restores all data from payload atomically within a database transaction.
     */
    suspend fun restoreFromPayload(payload: BackupPayload): Result<Unit> = runCatching {
        val customerDao = database.customerDao()
        val productDao = database.productDao()
        val transactionDao = database.transactionDao()
        val settingsDao = database.settingsDao()

        database.withTransaction {
            // 1. Clear existing records
            transactionDao.deleteAllTransactionItems()
            transactionDao.deleteAllTransactions()
            customerDao.deleteAllCustomers()
            productDao.deleteAllProducts()

            // 2. Insert restored records
            if (payload.customers.isNotEmpty()) {
                customerDao.insertAll(payload.customers.map { it.toEntity() })
            }
            if (payload.products.isNotEmpty()) {
                productDao.insertAll(payload.products.map { it.toEntity() })
            }
            if (payload.transactions.isNotEmpty()) {
                transactionDao.insertAllTransactions(payload.transactions.map { it.toEntity() })
            }
            if (payload.transactionItems.isNotEmpty()) {
                transactionDao.insertTransactionItems(payload.transactionItems.map { it.toEntity() })
            }

            // 3. Update settings
            settingsDao.insertOrUpdate(payload.settings.toEntity())
        }
    }

    private fun calculateSha256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
