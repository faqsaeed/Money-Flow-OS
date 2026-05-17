package com.moneyflowos.sms.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.ParsedTransactionDraft
import com.moneyflowos.core.domain.model.SmsEnvelope
import com.moneyflowos.core.domain.model.TransactionCategory
import com.moneyflowos.core.domain.repo.SessionRepository
import com.moneyflowos.core.domain.repo.TransactionRepository
import com.moneyflowos.core.parser.SmsIngestionFilter
import com.moneyflowos.core.parser.SmsParserEngine
import com.moneyflowos.core.parser.rules.GenericRules
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SmsIngestWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted params: WorkerParameters,
  private val sessionRepository: SessionRepository,
  private val transactionRepository: TransactionRepository,
) : CoroutineWorker(appContext, params) {
  override suspend fun doWork(): Result {
    val sender = inputData.getString("sender")
    val body = inputData.getString("body").orEmpty()
    val receivedAt = inputData.getLong("received_at", System.currentTimeMillis())
    if (body.isBlank()) return Result.success()

    val envelope = SmsEnvelope(sender = sender, messageBody = body, receivedAtEpochMillis = receivedAt)
    val decision = SmsIngestionFilter().decide(envelope)
    if (!decision.shouldProcess) return Result.success()

    val session = sessionRepository.ensureActiveSession()
    if (session.status != com.moneyflowos.core.domain.model.SessionStatus.ACTIVE) return Result.success()

    val engine = SmsParserEngine(GenericRules.all())
    val parsed = engine.parse(envelope)

    val draft = when (parsed) {
      is SmsParserEngine.Result.Parsed -> parsed.draft
      is SmsParserEngine.Result.NotParsed -> ParsedTransactionDraft(
        amount = 0L,
        direction = Direction.OUT,
        channel = Channel.UNKNOWN,
        personOrMerchant = null,
        category = TransactionCategory.OTHER,
        fee = null,
        balanceAfter = null,
        timestampEpochMillis = null,
        parserRuleId = "unparsed",
      )
    }

    transactionRepository.insertParsed(
      sessionId = session.id,
      trustedSource = decision.trustedSource,
      sender = sender,
      receivedAtEpochMillis = receivedAt,
      rawSms = body,
      draft = draft,
    )
    return Result.success()
  }
}

