package com.example.aurum.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.aurum.domain.model.Transaction
import com.example.aurum.domain.model.TransactionStatus

/**
 * Room database entity representing a persisted UPI transaction record.
 *
 * Mapped to and from the domain model [Transaction].
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["status"]),
        Index(value = ["created_at"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "vpa")
    val vpa: String,

    @ColumnInfo(name = "payee_name")
    val payeeName: String? = null,

    @ColumnInfo(name = "amount_paise")
    val amountPaise: Long,

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "qr_txn_ref")
    val qrTxnRef: String? = null,

    @ColumnInfo(name = "upi_txn_id")
    val upiTxnId: String? = null,

    @ColumnInfo(name = "app_package")
    val appPackage: String? = null,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

/**
 * Maps [TransactionEntity] to the domain [Transaction] model.
 */
fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    vpa = vpa,
    payeeName = payeeName,
    amountPaise = amountPaise,
    note = note,
    qrTxnRef = qrTxnRef,
    upiTxnId = upiTxnId,
    appPackage = appPackage,
    status = runCatching { TransactionStatus.valueOf(status) }.getOrDefault(TransactionStatus.UNCONFIRMED),
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Maps domain [Transaction] model to [TransactionEntity].
 */
fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    vpa = vpa,
    payeeName = payeeName,
    amountPaise = amountPaise,
    note = note,
    qrTxnRef = qrTxnRef,
    upiTxnId = upiTxnId,
    appPackage = appPackage,
    status = status.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)
