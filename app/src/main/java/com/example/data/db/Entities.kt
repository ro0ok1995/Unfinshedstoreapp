package com.example.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.model.AppNotification
import com.example.core.model.CountryConstants
import com.example.core.model.Customer
import com.example.core.model.CustomerStatus
import com.example.core.model.Money
import com.example.core.model.Product
import com.example.core.model.ProductStatus
import com.example.core.model.Settings
import com.example.core.model.Transaction
import com.example.core.model.TransactionItem
import com.example.core.model.TransactionStatus
import com.example.core.model.TransactionType

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["status"]),
        Index(value = ["name"]),
        Index(value = ["phone"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone")
    val phone: String = "",

    @ColumnInfo(name = "country_code")
    val countryCode: String = CountryConstants.DEFAULT_COUNTRY_CODE,

    @ColumnInfo(name = "address")
    val address: String = "",

    @ColumnInfo(name = "status")
    val status: String = CustomerStatus.ACTIVE,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "archived_at")
    val archivedAt: Long? = null,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
)

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["status"]),
        Index(value = ["name"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "price")
    val price: Long = 0L, // Integer minor units (350 = 3.50 ₪)

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,

    @ColumnInfo(name = "status")
    val status: String = ProductStatus.ACTIVE,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "archived_at")
    val archivedAt: Long? = null,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["type"]),
        Index(value = ["status"]),
        Index(value = ["created_at"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "customer_id")
    val customerId: Long? = null,

    @ColumnInfo(name = "type")
    val type: String, // credit_purchase, cash_purchase, payment

    @ColumnInfo(name = "total_amount")
    val totalAmount: Long, // Integer minor units

    @ColumnInfo(name = "status")
    val status: String = TransactionStatus.COMPLETED, // completed, cancelled

    @ColumnInfo(name = "note")
    val note: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "cancelled_at")
    val cancelledAt: Long? = null,

    @ColumnInfo(name = "cancel_reason")
    val cancelReason: String? = null
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["transaction_id"]),
        Index(value = ["product_id"])
    ]
)
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,

    @ColumnInfo(name = "product_id")
    val productId: Long? = null,

    @ColumnInfo(name = "product_name_snapshot")
    val productNameSnapshot: String,

    @ColumnInfo(name = "unit_price")
    val unitPrice: Long, // Minor units

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "subtotal")
    val subtotal: Long // Minor units
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,

    @ColumnInfo(name = "store_name")
    val storeName: String = "حسابات المحل",

    @ColumnInfo(name = "owner_name")
    val ownerName: String = "",

    @ColumnInfo(name = "phone")
    val phone: String = "",

    @ColumnInfo(name = "address")
    val address: String = "",

    @ColumnInfo(name = "language")
    val language: String = "ar", // ar, en

    @ColumnInfo(name = "theme")
    val theme: String = "system", // light, dark, system

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// MAPPERS: Entity <-> Domain Model
// ==========================================

fun CustomerEntity.toDomain(): Customer = Customer(
    id = id,
    name = name,
    phone = phone,
    countryCode = countryCode,
    address = address,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    archivedAt = archivedAt,
    deletedAt = deletedAt
)

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    id = id,
    name = name,
    phone = phone,
    countryCode = countryCode,
    address = address,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    archivedAt = archivedAt,
    deletedAt = deletedAt
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    price = Money.fromMinorUnits(price),
    imagePath = imagePath,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    archivedAt = archivedAt,
    deletedAt = deletedAt
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    price = price.minorUnits,
    imagePath = imagePath,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    archivedAt = archivedAt,
    deletedAt = deletedAt
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    customerId = customerId,
    type = type,
    totalAmount = Money.fromMinorUnits(totalAmount),
    status = status,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
    cancelledAt = cancelledAt,
    cancelReason = cancelReason
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    customerId = customerId,
    type = type,
    totalAmount = totalAmount.minorUnits,
    status = status,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
    cancelledAt = cancelledAt,
    cancelReason = cancelReason
)

fun TransactionItemEntity.toDomain(): TransactionItem = TransactionItem(
    id = id,
    transactionId = transactionId,
    productId = productId,
    productNameSnapshot = productNameSnapshot,
    unitPrice = Money.fromMinorUnits(unitPrice),
    quantity = quantity,
    subtotal = Money.fromMinorUnits(subtotal)
)

fun TransactionItem.toEntity(): TransactionItemEntity = TransactionItemEntity(
    id = id,
    transactionId = transactionId,
    productId = productId,
    productNameSnapshot = productNameSnapshot,
    unitPrice = unitPrice.minorUnits,
    quantity = quantity,
    subtotal = subtotal.minorUnits
)

fun SettingsEntity.toDomain(): Settings = Settings(
    id = id,
    storeName = storeName,
    ownerName = ownerName,
    phone = phone,
    address = address,
    language = language,
    theme = theme,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Settings.toEntity(): SettingsEntity = SettingsEntity(
    id = id,
    storeName = storeName,
    ownerName = ownerName,
    phone = phone,
    address = address,
    language = language,
    theme = theme,
    createdAt = createdAt,
    updatedAt = updatedAt
)

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["is_read"]),
        Index(value = ["created_at"]),
        Index(value = ["customer_id"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "customer_id")
    val customerId: Long? = null,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

fun NotificationEntity.toDomain(): AppNotification = AppNotification(
    id = id,
    title = title,
    message = message,
    type = type,
    customerId = customerId,
    isRead = isRead,
    createdAt = createdAt
)

fun AppNotification.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    title = title,
    message = message,
    type = type,
    customerId = customerId,
    isRead = isRead,
    createdAt = createdAt
)
