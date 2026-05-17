package com.moneyflowos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.TransactionCategory

@Entity(
  tableName = "transactions",
  indices = [
    Index(value = ["timestamp_epoch_millis"]),
    Index(value = ["person_id"]),
    Index(value = ["session_id"]),
  ],
)
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  @ColumnInfo(name = "amount")
  val amount: Long,

  @ColumnInfo(name = "direction")
  val direction: Direction,

  @ColumnInfo(name = "channel")
  val channel: Channel,

  @ColumnInfo(name = "person_or_merchant")
  val personOrMerchant: String?,

  @ColumnInfo(name = "person_id")
  val personId: Long?,

  @ColumnInfo(name = "category")
  val category: TransactionCategory,

  @ColumnInfo(name = "fee")
  val fee: Long?,

  @ColumnInfo(name = "balance_after")
  val balanceAfter: Long?,

  @ColumnInfo(name = "timestamp_epoch_millis")
  val timestampEpochMillis: Long,

  @ColumnInfo(name = "raw_sms")
  val rawSms: String,

  @ColumnInfo(name = "session_id")
  val sessionId: Long,

  @ColumnInfo(name = "trusted_source")
  val trustedSource: Boolean,

  @ColumnInfo(name = "source_sender")
  val sourceSender: String?,

  @ColumnInfo(name = "source_received_at_epoch_millis")
  val sourceReceivedAtEpochMillis: Long,

  @ColumnInfo(name = "parser_rule_id")
  val parserRuleId: String,

  @ColumnInfo(name = "is_corrected")
  val isCorrected: Boolean = false,

  @ColumnInfo(name = "corrected_amount")
  val correctedAmount: Long? = null,

  @ColumnInfo(name = "corrected_direction")
  val correctedDirection: Direction? = null,

  @ColumnInfo(name = "corrected_category")
  val correctedCategory: TransactionCategory? = null,

  @ColumnInfo(name = "corrected_channel")
  val correctedChannel: Channel? = null,

  @ColumnInfo(name = "corrected_person_or_merchant")
  val correctedPersonOrMerchant: String? = null,

  @ColumnInfo(name = "corrected_person_id")
  val correctedPersonId: Long? = null,
)

