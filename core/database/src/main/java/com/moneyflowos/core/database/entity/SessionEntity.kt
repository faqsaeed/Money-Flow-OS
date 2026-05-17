package com.moneyflowos.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneyflowos.core.domain.model.SessionStatus

@Entity(
  tableName = "sessions",
  indices = [
    Index(value = ["status"]),
  ],
)
data class SessionEntity(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  @ColumnInfo(name = "status")
  val status: SessionStatus,

  @ColumnInfo(name = "start_time_epoch_millis")
  val startTimeEpochMillis: Long,

  @ColumnInfo(name = "end_time_epoch_millis")
  val endTimeEpochMillis: Long?,
)

