package com.moneyflowos.core.database.repo

import com.moneyflowos.core.common.StableJson
import com.moneyflowos.core.common.time.Clock
import com.moneyflowos.core.database.dao.CorrectionLogDao
import com.moneyflowos.core.database.dao.TransactionDao
import com.moneyflowos.core.database.entity.CorrectionLogEntity
import com.moneyflowos.core.database.entity.TransactionEntity
import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.ParsedTransactionDraft
import com.moneyflowos.core.domain.model.TransactionCategory
import com.moneyflowos.core.domain.repo.ChannelTotal
import com.moneyflowos.core.domain.repo.DailyNetFlowPoint
import com.moneyflowos.core.domain.repo.PeopleRepository
import com.moneyflowos.core.domain.repo.TransactionRepository
import com.moneyflowos.core.domain.repo.TransactionTotals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTransactionRepository(
  private val transactionDao: TransactionDao,
  private val correctionLogDao: CorrectionLogDao,
  private val peopleRepository: PeopleRepository,
  private val peopleDaoFacade: PeopleDaoFacade,
  private val clock: Clock,
) : TransactionRepository {
  override fun observeRecent(sessionId: Long, limit: Int): Flow<List<com.moneyflowos.core.domain.model.Transaction>> {
    return transactionDao.observeRecent(sessionId = sessionId, limit = limit)
      .map { list -> list.map(TransactionEntity::toDomain) }
  }

  override fun observeById(id: Long): Flow<com.moneyflowos.core.domain.model.Transaction?> {
    return transactionDao.observeById(id).map { it?.toDomain() }
  }

  override fun observeTotals(sessionId: Long): Flow<TransactionTotals> {
    return transactionDao.observeTotals(sessionId).map { row ->
      val totalIn = row?.totalIn ?: 0L
      val totalOut = row?.totalOut ?: 0L
      val feesPaid = row?.feesPaid ?: 0L
      TransactionTotals(
        totalIn = totalIn,
        totalOut = totalOut,
        feesPaid = feesPaid,
        netFlow = totalIn - totalOut,
      )
    }
  }

  override fun observeChannelsBreakdown(sessionId: Long): Flow<List<ChannelTotal>> {
    return transactionDao.observeChannelBreakdown(sessionId).map { rows ->
      rows.mapNotNull { row ->
        val channel = runCatching { Channel.valueOf(row.channel) }.getOrNull() ?: Channel.UNKNOWN
        ChannelTotal(
          channel = channel,
          totalIn = row.totalIn ?: 0L,
          totalOut = row.totalOut ?: 0L,
          count = row.count,
        )
      }
    }
  }

  override fun observeDailyNetFlow(sessionId: Long, days: Int): Flow<List<DailyNetFlowPoint>> {
    return transactionDao.observeDailyNetFlow(sessionId, days).map { rows ->
      rows.map { row ->
        DailyNetFlowPoint(
          dayStartEpochMillis = row.dayBucket * 86_400_000L,
          totalIn = row.totalIn ?: 0L,
          totalOut = row.totalOut ?: 0L,
        )
      }.sortedBy { it.dayStartEpochMillis }
    }
  }

  override suspend fun insertParsed(
    sessionId: Long,
    trustedSource: Boolean,
    sender: String?,
    receivedAtEpochMillis: Long,
    rawSms: String,
    draft: ParsedTransactionDraft,
  ): Long {
    val personId = peopleDaoFacade.findOrCreatePersonId(
      name = draft.personOrMerchant,
      timestampEpochMillis = draft.timestampEpochMillis ?: receivedAtEpochMillis,
    )

    val tx = TransactionEntity(
      amount = draft.amount,
      direction = draft.direction,
      channel = draft.channel,
      personOrMerchant = draft.personOrMerchant?.takeIf { it.isNotBlank() },
      personId = personId,
      category = draft.category,
      fee = draft.fee,
      balanceAfter = draft.balanceAfter,
      timestampEpochMillis = draft.timestampEpochMillis ?: receivedAtEpochMillis,
      rawSms = rawSms,
      sessionId = sessionId,
      trustedSource = trustedSource,
      sourceSender = sender,
      sourceReceivedAtEpochMillis = receivedAtEpochMillis,
      parserRuleId = draft.parserRuleId,
    )

    val id = transactionDao.insert(tx)
    peopleDaoFacade.applyTransactionToPersonLedger(
      personId = personId,
      amount = draft.amount,
      direction = draft.direction,
      timestampEpochMillis = tx.timestampEpochMillis,
    )
    return id
  }

  override suspend fun applyCorrection(
    transactionId: Long,
    adminId: String,
    reason: String?,
    correctedAmount: Long?,
    correctedDirection: Direction?,
    correctedCategory: TransactionCategory?,
    correctedChannel: Channel?,
    correctedPersonOrMerchant: String?,
  ) {
    val existing = transactionDao.getById(transactionId) ?: return

    val oldJson = StableJson.obj(
      mapOf(
        "amount" to existing.amount,
        "direction" to existing.direction.name,
        "category" to existing.category.name,
        "channel" to existing.channel.name,
        "person_or_merchant" to (existing.personOrMerchant ?: ""),
        "corrected_amount" to existing.correctedAmount,
        "corrected_direction" to existing.correctedDirection?.name,
        "corrected_category" to existing.correctedCategory?.name,
        "corrected_channel" to existing.correctedChannel?.name,
        "corrected_person_or_merchant" to existing.correctedPersonOrMerchant,
      ),
    )

    val normalizedNewPerson = correctedPersonOrMerchant?.takeIf { it.isNotBlank() }
    val correctedPersonId = peopleDaoFacade.findOrCreatePersonId(
      name = normalizedNewPerson,
      timestampEpochMillis = existing.timestampEpochMillis,
    )

    val updated = existing.copy(
      isCorrected = true,
      correctedAmount = correctedAmount,
      correctedDirection = correctedDirection,
      correctedCategory = correctedCategory,
      correctedChannel = correctedChannel,
      correctedPersonOrMerchant = normalizedNewPerson,
      correctedPersonId = correctedPersonId,
    )
    transactionDao.update(updated)

    val newJson = StableJson.obj(
      mapOf(
        "corrected_amount" to correctedAmount,
        "corrected_direction" to correctedDirection?.name,
        "corrected_category" to correctedCategory?.name,
        "corrected_channel" to correctedChannel?.name,
        "corrected_person_or_merchant" to normalizedNewPerson,
      ),
    )

    correctionLogDao.insert(
      CorrectionLogEntity(
        transactionId = transactionId,
        oldValuesJson = oldJson,
        newValuesJson = newJson,
        reason = reason,
        timestampEpochMillis = clock.nowEpochMillis(),
        adminId = adminId,
      ),
    )

    // Keep ledger consistent with overlays (deterministic + correct), even if O(n).
    peopleRepository.rebuildPeopleLedger(updated.sessionId)
  }
}

/**
 * Small facade to keep Room repositories free of Android DI frameworks.
 * Provided from app module via Hilt.
 */
interface PeopleDaoFacade {
  suspend fun findOrCreatePersonId(name: String?, timestampEpochMillis: Long): Long?
  suspend fun applyTransactionToPersonLedger(
    personId: Long?,
    amount: Long,
    direction: Direction,
    timestampEpochMillis: Long,
  )
}
