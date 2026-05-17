package com.moneyflowos.core.parser

import com.moneyflowos.core.domain.model.SmsEnvelope
import java.util.Locale

class SmsIngestionFilter(
  private val trustedSenderExact: String = "8558",
  private val keywords: List<String> = listOf(
    "sent",
    "received",
    "ibft",
    "raast",
    "pos",
    "atm",
    "mobile load",
    "loan",
    "credit",
  ),
) {
  data class Decision(
    val shouldProcess: Boolean,
    val trustedSource: Boolean,
    val reason: String,
  )

  fun decide(envelope: SmsEnvelope): Decision {
    val sender = envelope.sender?.trim().orEmpty()
    val normalizedSender = sender.filter { it.isDigit() }
    if (normalizedSender == trustedSenderExact) {
      return Decision(shouldProcess = true, trustedSource = true, reason = "sender==$trustedSenderExact")
    }

    val body = envelope.messageBody.lowercase(Locale.US)
    val hasKeyword = keywords.any { body.contains(it) }
    return if (hasKeyword) {
      Decision(shouldProcess = true, trustedSource = false, reason = "keyword_match")
    } else {
      Decision(shouldProcess = false, trustedSource = false, reason = "filtered_out")
    }
  }
}

