package com.moneyflowos.sms.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.moneyflowos.core.domain.model.SmsEnvelope

object SmsIngestWork {
  private const val UNIQUE_WORK_PREFIX = "sms_ingest:"

  private const val KEY_SENDER = "sender"
  private const val KEY_BODY = "body"
  private const val KEY_RECEIVED_AT = "received_at"

  fun enqueue(context: Context, envelope: SmsEnvelope) {
    val input = Data.Builder()
      .putString(KEY_SENDER, envelope.sender)
      .putString(KEY_BODY, envelope.messageBody)
      .putLong(KEY_RECEIVED_AT, envelope.receivedAtEpochMillis)
      .build()

    val workName = UNIQUE_WORK_PREFIX + stableKey(envelope)
    val request = OneTimeWorkRequestBuilder<SmsIngestWorker>()
      .setInputData(input)
      .setConstraints(Constraints.NONE)
      .build()

    WorkManager.getInstance(context).enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, request)
  }

  fun prime(context: Context) {
    // no-op; kept for future deterministic, offline scheduled tasks.
    WorkManager.getInstance(context)
  }

  private fun stableKey(envelope: SmsEnvelope): String {
    // Deterministic, no crypto: just a stable hash to reduce duplicates.
    val raw = "${envelope.sender}|${envelope.receivedAtEpochMillis}|${envelope.messageBody}"
    return raw.hashCode().toString()
  }
}

