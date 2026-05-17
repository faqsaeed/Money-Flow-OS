package com.moneyflowos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "correction_log",
  indices = [
    Index(value = ["transaction_id"]),
    Index(value = ["timestamp_epoch_millis"]),
  ],
)
data class CorrectionLogEntity(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  @ColumnInfo(name = "transaction_id")
  val transactionId: Long,

  @ColumnInfo(name = "old_values_json")
  val oldValuesJson: String,

  @ColumnInfo(name = "new_values_json")
  val newValuesJson: String,

  @ColumnInfo(name = "reason")
  val reason: String?,

  @ColumnInfo(name = "timestamp_epoch_millis")
  val timestampEpochMillis: Long,

  @ColumnInfo(name = "admin_id")
  val adminId: String,
)

