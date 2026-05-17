package com.moneyflowos.core.database.repo

import com.moneyflowos.core.database.dao.CorrectionLogDao
import com.moneyflowos.core.database.entity.CorrectionLogEntity
import com.moneyflowos.core.domain.model.CorrectionLogEntry
import com.moneyflowos.core.domain.repo.CorrectionLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCorrectionLogRepository(
  private val dao: CorrectionLogDao,
) : CorrectionLogRepository {
  override fun observeRecent(limit: Int): Flow<List<CorrectionLogEntry>> {
    return dao.observeRecent(limit).map { list -> list.map(CorrectionLogEntity::toDomain) }
  }

  override fun observeByTransactionId(transactionId: Long): Flow<List<CorrectionLogEntry>> {
    return dao.observeByTransactionId(transactionId).map { list -> list.map(CorrectionLogEntity::toDomain) }
  }
}

