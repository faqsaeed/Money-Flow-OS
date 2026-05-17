package com.moneyflowos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moneyflowos.core.database.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleDao {
  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(entity: PersonEntity): Long

  @Update
  suspend fun update(entity: PersonEntity)

  @Query("SELECT * FROM people WHERE id = :id")
  fun observeById(id: Long): Flow<PersonEntity?>

  @Query("SELECT * FROM people WHERE id = :id")
  suspend fun getById(id: Long): PersonEntity?

  @Query(
    """
      SELECT * FROM people
      ORDER BY (total_sent + total_received) DESC, transaction_count DESC
      LIMIT :limit
    """,
  )
  fun observeTopPeople(limit: Int): Flow<List<PersonEntity>>

  @Query(
    """
      SELECT * FROM people
      ORDER BY (total_sent + total_received) DESC, transaction_count DESC
    """,
  )
  fun observePeople(): Flow<List<PersonEntity>>

  @Query("SELECT * FROM people WHERE normalized_name = :normalizedName LIMIT 1")
  suspend fun getByNormalizedName(normalizedName: String): PersonEntity?

  @Query("DELETE FROM people")
  suspend fun deleteAll()
}
