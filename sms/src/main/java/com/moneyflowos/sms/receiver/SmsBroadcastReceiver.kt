package com.moneyflowos.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.moneyflowos.core.domain.model.SmsEnvelope
import com.moneyflowos.core.parser.SmsIngestionFilter
import com.moneyflowos.sms.worker.SmsIngestWork

class SmsBroadcastReceiver : BroadcastReceiver() {
  // Keep receiver lean: delegate heavy work to WorkManager worker (offline, deterministic).
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent).orEmpty()
    if (messages.isEmpty()) return

    val sender = messages.firstOrNull()?.originatingAddress
    val body = buildString {
      for (msg in messages) append(msg.messageBody.orEmpty())
    }.trim()
    if (body.isBlank()) return

    val receivedAt = messages.minOfOrNull { it.timestampMillis } ?: System.currentTimeMillis()
    val envelope = SmsEnvelope(sender = sender, messageBody = body, receivedAtEpochMillis = receivedAt)

    val decision = SmsIngestionFilter().decide(envelope)
    if (!decision.shouldProcess) return

    SmsIngestWork.enqueue(context, envelope)
  }
}
