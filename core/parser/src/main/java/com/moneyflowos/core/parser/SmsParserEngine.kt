package com.moneyflowos.core.parser

import com.moneyflowos.core.domain.model.ParsedTransactionDraft
import com.moneyflowos.core.domain.model.SmsEnvelope

class SmsParserEngine(
  private val rules: List<SmsParserRule>,
) {
  sealed class Result {
    data class Parsed(
      val draft: ParsedTransactionDraft,
      val matchedRuleId: String,
    ) : Result()

    data class NotParsed(
      val reason: String,
      val attemptedRuleIds: List<String>,
    ) : Result()
  }

  fun parse(envelope: SmsEnvelope): Result {
    val attempted = ArrayList<String>(rules.size)
    for (rule in rules) {
      attempted.add(rule.id)
      if (!rule.applies(envelope)) continue
      val draft = rule.parse(envelope) ?: continue
      return Result.Parsed(draft = draft, matchedRuleId = rule.id)
    }
    return Result.NotParsed(reason = "no_rule_matched", attemptedRuleIds = attempted)
  }
}

interface SmsParserRule {
  val id: String
  fun applies(envelope: SmsEnvelope): Boolean
  fun parse(envelope: SmsEnvelope): ParsedTransactionDraft?
}

