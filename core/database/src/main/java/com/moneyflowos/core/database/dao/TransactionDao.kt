package com.moneyflowos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moneyflowos.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: TransactionEntity): Long

  @Update
  suspend fun update(entity: TransactionEntity)

  @Query("SELECT * FROM transactions WHERE id = :id")
  fun observeById(id: Long): Flow<TransactionEntity?>

  @Query("SELECT * FROM transactions WHERE id = :id")
  suspend fun getById(id: Long): TransactionEntity?

  @Query(
    """
      SELECT * FROM transactions
      WHERE session_id = :sessionId
      ORDER BY timestamp_epoch_millis DESC, id DESC
      LIMIT :limit
    """,
  )
  fun observeRecent(sessionId: Long, limit: Int): Flow<List<TransactionEntity>>

  @Query(
    """
      SELECT
        SUM(CASE WHEN COALESCE(corrected_direction, direction) = 'IN'
          THEN COALESCE(corrected_amount, amount) ELSE 0 END) AS totalIn,
        SUM(CASE WHEN COALESCE(corrected_direction, direction) = 'OUT'
          THEN COALESCE(corrected_amount, amount) ELSE 0 END) AS totalOut,
        SUM(COALESCE(fee, 0)) AS feesPaid
      FROM transactions
      WHERE session_id = :sessionId
    """,
  )
  fun observeTotals(sessionId: Long): Flow<TotalsRow?>

  @Query(
    """
      SELECT
        COALESCE(corrected_channel, channel) AS channel,
        SUM(CASE WHEN COALESCE(corrected_direction, direction) = 'IN'
          THEN COALESCE(corrected_amount, amount) ELSE 0 END) AS totalIn,
        SUM(CASE WHEN COALESCE(corrected_direction, direction) = 'OUT'
          THEN COALESCE(corrected_amount, amount) ELSE 0 END) AS totalOut,
        COUNT(*) AS count
      FROM transactions
      WHERE session_id = :sessionId
      GROUP BY COALESCE(corrected_channel, channel)
      ORDER BY (totalIn + totalOut) DESC
    """,
  )
  fun observeChannelBreakdown(sessionId: Long): Flow<List<ChannelTotalRow>>

  @Query(
    """
      SELECT
        (timestamp_epoch_millis / 86400000) AS dayBucket,
        SUM(CASE WHEN COALESCE(corrected_direction, direction) = 'IN'
          THEN COALESCE(corrected_amount, amount) ELSE 0 END) AS totalIn,
        SUM(CASE WHEN COALESCE(corrected_direction, direction) = 'OUT'
          THEN COALESCE(corrected_amount, amount) ELSE 0 END) AS totalOut
      FROM transactions
      WHERE session_id = :sessionId
      GROUP BY (timestamp_epoch_millis / 86400000)
      ORDER BY dayBucket DESC
      LIMIT :days
    """,
  )
  fun observeDailyNetFlow(sessionId: Long, days: Int): Flow<List<DailyNetFlowRow>>

  @Query("SELECT * FROM transactions WHERE session_id = :sessionId")
  suspend fun getAllForSession(sessionId: Long): List<TransactionEntity>
}

data class TotalsRow(
  val totalIn: Long?,
  val totalOut: Long?,
  val feesPaid: Long?,
)

data class ChannelTotalRow(
  val channel: String,
  val totalIn: Long?,
  val totalOut: Long?,
  val count: Long,
)

data class DailyNetFlowRow(
  val dayBucket: Long,
  val totalIn: Long?,
  val totalOut: Long?,
)
