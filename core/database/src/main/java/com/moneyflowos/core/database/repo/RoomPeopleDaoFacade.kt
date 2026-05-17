package com.moneyflowos.core.database.repo

import com.moneyflowos.core.common.Normalization
import com.moneyflowos.core.database.dao.PeopleDao
import com.moneyflowos.core.database.entity.PersonEntity
import com.moneyflowos.core.domain.model.Direction
class RoomPeopleDaoFacade(
  private val peopleDao: PeopleDao,
) : PeopleDaoFacade {
  override suspend fun findOrCreatePersonId(name: String?, timestampEpochMillis: Long): Long? {
    val raw = name?.trim().orEmpty()
    if (raw.isBlank()) return null

    val normalized = Normalization.normalizePersonKey(raw)
    val existing = peopleDao.getByNormalizedName(normalized)
    if (existing != null) return existing.id

    val id = peopleDao.insert(
      PersonEntity(
        name = raw,
        normalizedName = normalized,
        totalSent = 0,
        totalReceived = 0,
        transactionCount = 0,
        firstSeenEpochMillis = timestampEpochMillis,
        lastSeenEpochMillis = timestampEpochMillis,
      ),
    )
    return id
  }

  override suspend fun applyTransactionToPersonLedger(
    personId: Long?,
    amount: Long,
    direction: Direction,
    timestampEpochMillis: Long,
  ) {
    if (personId == null) return
    val current = peopleDao.getById(personId) ?: return
    val newTotalSent = current.totalSent + (if (direction == Direction.OUT) amount else 0L)
    val newTotalReceived = current.totalReceived + (if (direction == Direction.IN) amount else 0L)
    peopleDao.update(
      current.copy(
        totalSent = newTotalSent,
        totalReceived = newTotalReceived,
        transactionCount = current.transactionCount + 1,
        lastSeenEpochMillis = maxOf(current.lastSeenEpochMillis, timestampEpochMillis),
        firstSeenEpochMillis = minOf(current.firstSeenEpochMillis, timestampEpochMillis),
      ),
    )
  }
}
