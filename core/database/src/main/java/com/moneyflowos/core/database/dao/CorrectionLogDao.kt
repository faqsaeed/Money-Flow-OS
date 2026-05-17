package com.moneyflowos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moneyflowos.core.database.entity.CorrectionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CorrectionLogDao {
  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: CorrectionLogEntity): Long

  @Query(
    """
      SELECT * FROM correction_log
      ORDER BY timestamp_epoch_millis DESC, id DESC
      LIMIT :limit
    """,
  )
  fun observeRecent(limit: Int): Flow<List<CorrectionLogEntity>>

  @Query(
    """
      SELECT * FROM correction_log
      WHERE transaction_id = :transactionId
      ORDER BY timestamp_epoch_millis DESC, id DESC
    """,
  )
  fun observeByTransactionId(transactionId: Long): Flow<List<CorrectionLogEntity>>
}

