package com.moneyflowos.core.parser.rules

import com.moneyflowos.core.domain.model.Channel
import com.moneyflowos.core.domain.model.Direction
import com.moneyflowos.core.domain.model.ParsedTransactionDraft
import com.moneyflowos.core.domain.model.SmsEnvelope
import com.moneyflowos.core.domain.model.TransactionCategory
import com.moneyflowos.core.parser.SmsParserRule

/**
 * These are conservative, rule-based parsers that aim for deterministic extraction.
 * If a rule can't confidently extract amount + direction, it returns null.
 */
object GenericRules {
  fun all(): List<SmsParserRule> = listOf(
    JazzCashSentRule,
    JazzCashReceivedRule,
    RaastRule,
    IbftRule,
    PosRule,
    AtmRule,
    MobileLoadRule,
    LoanCreditRule,
    FallbackSentRule,
    FallbackReceivedRule,
  )

  private val amountRegex = Regex("""(?i)\b(?:rs\.?|pkr)\s*([0-9][0-9,]*\.?[0-9]*)\b""")
  private val feeRegex = Regex("""(?i)\bfee\s*(?:rs\.?|pkr)?\s*([0-9][0-9,]*\.?[0-9]*)\b""")
  private val balanceRegex = Regex("""(?i)\b(?:bal|balance)\s*[:=]?\s*(?:rs\.?|pkr)?\s*([0-9][0-9,]*\.?[0-9]*)\b""")

  private fun extractAmount(body: String): Long? =
    RuleUtils.findFirstGroup(amountRegex, body)?.let(MoneyParsing::parseAmountRupees)

  private fun extractFee(body: String): Long? =
    RuleUtils.findFirstGroup(feeRegex, body)?.let(MoneyParsing::parseAmountRupees)

  private fun extractBalance(body: String): Long? =
    RuleUtils.findFirstGroup(balanceRegex, body)?.let(MoneyParsing::parseAmountRupees)

  private object JazzCashSentRule : SmsParserRule {
    override val id: String = "jazzcash_sent_v1"
    private val appliesRegex = Regex("""(?i)\bsent\b""")
    private val personRegex = Regex("""(?i)\bto\s+([a-z0-9][a-z0-9 ._-]{1,40})\b""")

    override fun applies(envelope: SmsEnvelope): Boolean {
      val sender = envelope.sender?.filter { it.isDigit() } ?: ""
      return sender == "8558" && appliesRegex.containsMatchIn(envelope.messageBody)
    }

    override fun parse(envelope: SmsEnvelope): ParsedTransactionDraft? {
      val body = RuleUtils.body(envelope)
      val amount = extractAmount(body) ?: return null
      val person = RuleUtils.findFirstGroup(personRegex, body)?.trim()
      return ParsedTransactionDraft(
        amount = amount,
        direction = Direction.OUT,
        channel = Channel.WALLET,
        personOrMerchant = person,
        category = TransactionCategory.MONEY_OUT,
        fee = extractFee(body),
        balanceAfter = extractBalance(body),
        timestampEpochMillis = null,
        parserRuleId = id,
      )
    }
  }

  private object JazzCashReceivedRule : SmsParserRule {
    override val id: String = "jazzcash_received_v1"
    private val appliesRegex = Regex("""(?i)\breceived\b""")
    private val personRegex = Regex("""(?i)\bfrom\s+([a-z0-9][a-z0-9 ._-]{1,40})\b""")

    override fun applies(envelope: SmsEnvelope): Boolean {
      val sender = envelope.sender?.filter { it.isDigit() } ?: ""
      return sender == "8558" && appliesRegex.containsMatchIn(envelope.messageBody)
    }

    override fun parse(envelope: SmsEnvelope): ParsedTransactionDraft? {
      val body = RuleUtils.body(envelope)
      val amount = extractAmount(body) ?: return null
      val person = RuleUtils.findFirstGroup(personRegex, body)?.trim()
      return ParsedTransactionDraft(
        amount = amount,
        direction = Direction.IN,
        channel = Channel.WALLET,
        personOrMerchant = person,
        category = TransactionCategory.MONEY_IN,
        fee = extractFee(body),
        balanceAfter = extractBalance(body),
        timestampEpochMillis = null,
        parserRuleId = id,
      )
    }
  }

  private object RaastRule : SmsParserRule {
    override val id: String = "raast_v1"
    override fun applies(envelope: SmsEnvelope): Boolean = RuleUtils.bodyLower(envelope).contains("raast")
    override fun parse(envelope: SmsEnvelope): ParsedTransactionDraft? {
      val body = RuleUtils.body(envelope)
      val amount = extractAmount(body) ?: return null
      val dir = when {
        RuleUtils.bodyLower(envelope).contains("received") -> Direction.IN
        RuleUtils.bodyLower(envelope).contains("sent") -> Direction.OUT
        else -> return null
      }
      val person = RuleUtils.findFirstGroup(Regex("""(?i)\b(?:to|from)\s+([a-z0-9][a-z0-9 ._-]{1,60})\b"""), body)?.trim()
      return ParsedTransactionDraft(
        amount = amount,
        direction = dir,
        channel = Channel.RAAST,
        personOrMerchant = person,
        category = TransactionCategory.RAAST,
        fee = extractFee(body),
        balanceAfter = extractBalance(body),
        timestampEpochMillis = null,
        parserRuleId = id,
      )
    }
  }

  private object IbftRule : SmsParserRule {
    override val id: String = "ibft_v1"
    override fun applies(envelope: SmsEnvelope): Boolean = RuleUtils.bodyLower(envelope).contains("ibft")
    override fun parse(envelope: SmsEnvelope): ParsedTransactionDraft? {
      val body = RuleUtils.body(envelope)
      val amount = extractAmount(body) ?: return null
      val lower = RuleUtils.bodyLower(envelope)
      val dir = when {
        lower.contains("received") -> Direction.IN
        lower.contains("sent") -> Direction.OUT
        lower.contains("debited") -> Direction.OUT
        lower.contains("credited") -> Direction.IN
        // Heuristic fallback: most IBFT alerts are outbound when they explicitly mention a beneficiary ("to X").
        lower.contains(" to ") -> Direction.OUT
        else -> return null
      }
      val person = RuleUtils.findFirstGroup(Regex("""(?i)\b(?:to|from)\s+([a-z0-9][a-z0-9 ._-]{1,60})\b"""), body)?.trim()
      return ParsedTransactionDraft(
        amount = amount,
        direction = dir,
        channel = Channel.IBFT,
        personOrMerchant = person,
        category = TransactionCategory.IBFT,
        fee = extractFee(body),
        balanceAfter = extractBalance(body),
        timestampEpochMillis = null,
        parserRuleId = id,
      )
    }
  }

  private object PosRule : SmsParserRule {
    override val id: String = "pos_v1"
    override fun applies(envelope: SmsEnvelope): Boolean = RuleUtils.bodyLower(envelope).contains("pos")
    override fun parse(envelope: SmsEnvelope): ParsedTransactionDraft? {
      val body = RuleUtils.body(envelope)
      val amount = extractAmount(body) ?: return null
      val merchant = RuleUtils.findFirstGroup(Regex("""(?i)\b(?:at|merchant)\s+([a-z0-9][a-z0-9 ._-]{1,60})\b"""), body)?.trim()
      return ParsedTransactionDraft(
        amount = amount,
        direction = Direction.OUT,
        channel = Channel.POS,
        personOrMerchant = merchant,
        category = TransactionCategory.POS,
        fee = extractFee(body),
        balanceAfter = extractBalance(body),
        timestampEpochMillis = null,
        parserRuleId = id,
      )
    }
  }

  private object AtmRule : SmsParserRule {
    override val id: String = "atm_withdrawal_v1"
    override fun applies(envelope: SmsEnvelope): Boolean = RuleUtils.bodyLower(envelope).contains("atm")
    override fun parse(envelope: SmsEnvelope): ParsedTransactionDraft? {
      val body = RuleUtils.body(envelope)
      val amount = extractAmount(body) ?: return null
      return ParsedTransactionDraft(
        amount = amount,
        direction = Direction.OUT,
        channel = Channel.ATM,
        personOrMerchant = "ATM",
        category = TransactionCategory.ATM_WITHDRAWAL,
        fee = extractFee(body),
        balanceAfter = extractBalance(body),
        timestampEpochMillis = null,
        parserRuleId = id,
      )
    }
  }

  private object MobileLoadRule : SmsParserRule {
    override val id: String = "mobile_load_v1"
    override fun applies(envelope: SmsEnvelope): Boolean = RuleUtils.bodyLower(envelope).contains("mobile load")
    override fun parse(envelope: SmsEnvelope): ParsedTransactionDraft? {
      val body = RuleUtils.body(envelope)
      val amount = extractAmount(body) ?: return null
      val number = RuleUtils.findFirstGroup(Regex("""(?i)\b(?:to|for)\s+(\+?92[0-9]{10}|0[0-9]{10})\b"""), body)?.trim()
      return ParsedTransactionDraft(
        amount = amount,
        direction = Direction.OUT,
        channel = Channel.MOBILE_LOAD,
        personOrMerchant = number ?: "Mobile Load",
        category = TransactionCategory.MOBILE_LOAD,
        fee = extractFee(body),
        balanceAfter = extractBalance(body),
        timestampEpochMillis = null,
        parserRuleId = id,
      )
    }
  }

  private object LoanCreditRule : SmsParserRule {
    override val id: String = "loan_credit_v1"
    override fun applies(envelope: SmsEnvelope): Boolean {
      val lower = RuleUtils.bodyLower(envelope)
      return lower.contains("loan") || lower.contains("credit")
    }

    override fun parse(envelope: SmsEnvelope): ParsedTransactionDraft? {
      val body = RuleUtils.body(envelope)
      val amount = extractAmount(body) ?: return null
      val lower = RuleUtils.bodyLower(envelope)
      val dir = when {
        lower.contains("disbursed") || lower.contains("credited") -> Direction.IN
        lower.contains("repay") || lower.contains("paid") || lower.contains("debited") -> Direction.OUT
        else -> Direction.OUT
      }
      return ParsedTransactionDraft(
        amount = amount,
        direction = dir,
        channel = Channel.LOAN,
        personOrMerchant = "Loan/Credit",
        category = TransactionCategory.LOAN_CREDIT,
        fee = extractFee(body),
        balanceAfter = extractBalance(body),
        timestampEpochMillis = null,
        parserRuleId = id,
      )
    }
  }

  private object FallbackSentRule : SmsParserRule {
    override val id: String = "fallback_sent_v1"
    override fun applies(envelope: SmsEnvelope): Boolean = RuleUtils.bodyLower(envelope).contains("sent")
    override fun parse(envelope: SmsEnvelope): ParsedTransactionDraft? {
      val body = RuleUtils.body(envelope)
      val amount = extractAmount(body) ?: return null
      val person = RuleUtils.findFirstGroup(Regex("""(?i)\bto\s+([a-z0-9][a-z0-9 ._-]{1,60})\b"""), body)?.trim()
      return ParsedTransactionDraft(
        amount = amount,
        direction = Direction.OUT,
        channel = Channel.UNKNOWN,
        personOrMerchant = person,
        category = TransactionCategory.MONEY_OUT,
        fee = extractFee(body),
        balanceAfter = extractBalance(body),
        timestampEpochMillis = null,
        parserRuleId = id,
      )
    }
  }

  private object FallbackReceivedRule : SmsParserRule {
    override val id: String = "fallback_received_v1"
    override fun applies(envelope: SmsEnvelope): Boolean = RuleUtils.bodyLower(envelope).contains("received")
    override fun parse(envelope: SmsEnvelope): ParsedTransactionDraft? {
      val body = RuleUtils.body(envelope)
      val amount = extractAmount(body) ?: return null
      val person = RuleUtils.findFirstGroup(Regex("""(?i)\bfrom\s+([a-z0-9][a-z0-9 ._-]{1,60})\b"""), body)?.trim()
      return ParsedTransactionDraft(
        amount = amount,
        direction = Direction.IN,
        channel = Channel.UNKNOWN,
        personOrMerchant = person,
        category = TransactionCategory.MONEY_IN,
        fee = extractFee(body),
        balanceAfter = extractBalance(body),
        timestampEpochMillis = null,
        parserRuleId = id,
      )
    }
  }
}
