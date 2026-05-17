package com.moneyflowos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moneyflowos.core.database.entity.SessionEntity
import com.moneyflowos.core.domain.model.SessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: SessionEntity): Long

  @Update
  suspend fun update(entity: SessionEntity)

  @Query("SELECT * FROM sessions WHERE id = :id")
  suspend fun getById(id: Long): SessionEntity?

  @Query("SELECT * FROM sessions WHERE status = :status ORDER BY id DESC LIMIT 1")
  suspend fun getLatestByStatus(status: SessionStatus): SessionEntity?

  @Query("SELECT * FROM sessions ORDER BY id DESC LIMIT 1")
  suspend fun getLatest(): SessionEntity?

  @Query("SELECT * FROM sessions ORDER BY id DESC LIMIT 1")
  fun observeLatest(): Flow<SessionEntity?>

  @Query("UPDATE sessions SET status = :status WHERE id = :id")
  suspend fun setStatus(id: Long, status: SessionStatus)
}

