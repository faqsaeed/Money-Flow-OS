package com.moneyflowos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "people",
  indices = [
    Index(value = ["normalized_name"], unique = true),
  ],
)
data class PersonEntity(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  @ColumnInfo(name = "name")
  val name: String,

  @ColumnInfo(name = "normalized_name")
  val normalizedName: String,

  @ColumnInfo(name = "total_sent")
  val totalSent: Long,

  @ColumnInfo(name = "total_received")
  val totalReceived: Long,

  @ColumnInfo(name = "transaction_count")
  val transactionCount: Long,

  @ColumnInfo(name = "first_seen_epoch_millis")
  val firstSeenEpochMillis: Long,

  @ColumnInfo(name = "last_seen_epoch_millis")
  val lastSeenEpochMillis: Long,
)

