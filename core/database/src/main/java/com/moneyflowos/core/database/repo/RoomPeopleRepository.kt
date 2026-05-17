package com.moneyflowos.core.database.repo

import com.moneyflowos.core.common.Normalization
import com.moneyflowos.core.common.time.Clock
import com.moneyflowos.core.database.dao.PeopleDao
import com.moneyflowos.core.database.dao.TransactionDao
import com.moneyflowos.core.database.entity.PersonEntity
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.Person
import com.moneyflowos.core.domain.repo.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPeopleRepository(
  private val peopleDao: PeopleDao,
  private val transactionDao: TransactionDao,
  private val clock: Clock,
) : PeopleRepository {
  override fun observeTopPeople(sessionId: Long, limit: Int): Flow<List<Person>> {
    return peopleDao.observeTopPeople(limit).map { list -> list.map(PersonEntity::toDomain) }
  }

  override fun observePeople(sessionId: Long): Flow<List<Person>> {
    return peopleDao.observePeople().map { list -> list.map(PersonEntity::toDomain) }
  }

  override fun observeById(id: Long): Flow<Person?> {
    return peopleDao.observeById(id).map { it?.toDomain() }
  }

  override suspend fun rebuildPeopleLedger(sessionId: Long) {
    val txs = transactionDao.getAllForSession(sessionId)
    peopleDao.deleteAll()

    data class Agg(
      var displayName: String,
      var totalSent: Long,
      var totalReceived: Long,
      var count: Long,
      var firstSeen: Long,
      var lastSeen: Long,
    )

    val aggByKey = LinkedHashMap<String, Agg>()
    for (tx in txs) {
      val name = (tx.correctedPersonOrMerchant ?: tx.personOrMerchant)?.trim().orEmpty()
      if (name.isBlank()) continue

      val key = Normalization.normalizePersonKey(name)
      val ts = tx.timestampEpochMillis
      val effectiveAmount = tx.correctedAmount ?: tx.amount
      val effectiveDirection = tx.correctedDirection ?: tx.direction

      val agg = aggByKey.getOrPut(key) {
        Agg(
          displayName = name,
          totalSent = 0,
          totalReceived = 0,
          count = 0,
          firstSeen = ts,
          lastSeen = ts,
        )
      }
      agg.displayName = chooseBetterName(agg.displayName, name)
      agg.count += 1
      agg.firstSeen = minOf(agg.firstSeen, ts)
      agg.lastSeen = maxOf(agg.lastSeen, ts)
      when (effectiveDirection) {
        Direction.IN -> agg.totalReceived += effectiveAmount
        Direction.OUT -> agg.totalSent += effectiveAmount
      }
    }

    val now = clock.nowEpochMillis()
    for ((key, agg) in aggByKey) {
      peopleDao.insert(
        PersonEntity(
          name = agg.displayName,
          normalizedName = key,
          totalSent = agg.totalSent,
          totalReceived = agg.totalReceived,
          transactionCount = agg.count,
          firstSeenEpochMillis = agg.firstSeen.coerceAtMost(now),
          lastSeenEpochMillis = agg.lastSeen.coerceAtMost(now),
        ),
      )
    }
  }

  private fun chooseBetterName(existing: String, incoming: String): String {
    // Prefer the longer, more descriptive string deterministically.
    val e = existing.trim()
    val i = incoming.trim()
    return when {
      i.length > e.length -> i
      e.length > i.length -> e
      else -> minOf(e, i)
    }
  }
}

