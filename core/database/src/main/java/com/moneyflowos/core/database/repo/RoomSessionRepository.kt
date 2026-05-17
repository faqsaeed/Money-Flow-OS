package com.moneyflowos.core.database.repo

import com.moneyflowos.core.common.time.Clock
import com.moneyflowos.core.database.dao.SessionDao
import com.moneyflowos.core.database.entity.SessionEntity
import com.moneyflowos.core.domain.model.Session
import com.moneyflowos.core.domain.model.SessionStatus
import com.moneyflowos.core.domain.repo.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class RoomSessionRepository(
  private val sessionDao: SessionDao,
  private val clock: Clock,
) : SessionRepository {
  override fun observeCurrentSession(): Flow<Session> {
    return sessionDao.observeLatest()
      .filterNotNull()
      .map(SessionEntity::toDomain)
  }

  override suspend fun ensureActiveSession(): Session {
    val now = clock.nowEpochMillis()
    val latest = sessionDao.getLatest()
    if (latest == null) {
      val id = sessionDao.insert(SessionEntity(status = SessionStatus.ACTIVE, startTimeEpochMillis = now, endTimeEpochMillis = null))
      return Session(id = id, status = SessionStatus.ACTIVE, startTimeEpochMillis = now, endTimeEpochMillis = null)
    }
    return when (latest.status) {
      SessionStatus.ACTIVE, SessionStatus.PAUSED -> latest.toDomain()
      SessionStatus.ARCHIVED -> {
        val id = sessionDao.insert(SessionEntity(status = SessionStatus.ACTIVE, startTimeEpochMillis = now, endTimeEpochMillis = null))
        Session(id = id, status = SessionStatus.ACTIVE, startTimeEpochMillis = now, endTimeEpochMillis = null)
      }
    }
  }

  override suspend fun setStatus(status: SessionStatus) {
    val latest = sessionDao.getLatest() ?: return
    sessionDao.setStatus(latest.id, status)
  }

  override suspend fun resetSession(): Session {
    val now = clock.nowEpochMillis()
    val latest = sessionDao.getLatest()
    if (latest != null) {
      sessionDao.update(latest.copy(status = SessionStatus.ARCHIVED, endTimeEpochMillis = now))
    }
    val id = sessionDao.insert(SessionEntity(status = SessionStatus.ACTIVE, startTimeEpochMillis = now, endTimeEpochMillis = null))
    return Session(id = id, status = SessionStatus.ACTIVE, startTimeEpochMillis = now, endTimeEpochMillis = null)
  }
}

