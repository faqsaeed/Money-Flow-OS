package com.moneyflowos.core.domain.repo

import com.moneyflowos.core.domain.model.CorrectionLogEntry
import com.moneyflowos.core.domain.model.Person
import com.moneyflowos.core.domain.model.Session
import com.moneyflowos.core.domain.model.SessionStatus
import com.moneyflowos.core.domain.model.Transaction
import com.moneyflowos.core.domain.model.TransactionCategory
import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.model.Direction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
  fun observeRecent(sessionId: Long, limit: Int): Flow<List<Transaction>>
  fun observeById(id: Long): Flow<Transaction?>
  fun observeTotals(sessionId: Long): Flow<TransactionTotals>
  fun observeChannelsBreakdown(sessionId: Long): Flow<List<ChannelTotal>>
  fun observeDailyNetFlow(sessionId: Long, days: Int): Flow<List<DailyNetFlowPoint>>
  suspend fun insertParsed(
    sessionId: Long,
    trustedSource: Boolean,
    sender: String?,
    receivedAtEpochMillis: Long,
    rawSms: String,
    draft: com.moneyflowos.core.domain.model.ParsedTransactionDraft,
  ): Long

  suspend fun applyCorrection(
    transactionId: Long,
    adminId: String,
    reason: String?,
    correctedAmount: Long?,
    correctedDirection: Direction?,
    correctedCategory: TransactionCategory?,
    correctedChannel: Channel?,
    correctedPersonOrMerchant: String?,
  )
}

data class TransactionTotals(
  val totalIn: Long,
  val totalOut: Long,
  val feesPaid: Long,
  val netFlow: Long,
)

data class ChannelTotal(
  val channel: Channel,
  val totalIn: Long,
  val totalOut: Long,
  val count: Long,
)

data class DailyNetFlowPoint(
  val dayStartEpochMillis: Long,
  val totalIn: Long,
  val totalOut: Long,
)

interface PeopleRepository {
  fun observeTopPeople(sessionId: Long, limit: Int): Flow<List<Person>>
  fun observePeople(sessionId: Long): Flow<List<Person>>
  fun observeById(id: Long): Flow<Person?>
  suspend fun rebuildPeopleLedger(sessionId: Long)
}

interface SessionRepository {
  fun observeCurrentSession(): Flow<Session>
  suspend fun ensureActiveSession(): Session
  suspend fun setStatus(status: SessionStatus)
  suspend fun resetSession(): Session
}

interface CorrectionLogRepository {
  fun observeRecent(limit: Int): Flow<List<CorrectionLogEntry>>
  fun observeByTransactionId(transactionId: Long): Flow<List<CorrectionLogEntry>>
}
