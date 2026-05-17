package com.moneyflowos.core.parser.rules

import com.moneyflowos.core.domain.model.SmsEnvelope
import java.util.Locale

internal object RuleUtils {
  fun body(envelope: SmsEnvelope): String = envelope.messageBody.trim()
  fun bodyLower(envelope: SmsEnvelope): String = envelope.messageBody.lowercase(Locale.US)
  fun findFirstGroup(regex: Regex, text: String, groupIndex: Int = 1): String? =
    regex.find(text)?.groupValues?.getOrNull(groupIndex)
}

