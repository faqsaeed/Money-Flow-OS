package com.moneyflowos.core.parser

import com.moneyflowos.core.domain.model.SmsEnvelope
import com.moneyflowos.core.parser.rules.GenericRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserEngineTest {
  @Test
  fun `filters by sender or keywords`() {
    val filter = SmsIngestionFilter()
    assertTrue(filter.decide(SmsEnvelope(sender = "8558", messageBody = "Hello", receivedAtEpochMillis = 0)).shouldProcess)
    assertTrue(filter.decide(SmsEnvelope(sender = "X", messageBody = "You have sent Rs. 100", receivedAtEpochMillis = 0)).shouldProcess)
    assertEquals(false, filter.decide(SmsEnvelope(sender = "X", messageBody = "Sale at store", receivedAtEpochMillis = 0)).shouldProcess)
  }

  @Test
  fun `parses jazzcash sent`() {
    val engine = SmsParserEngine(GenericRules.all())
    val sms = SmsEnvelope(
      sender = "8558",
      messageBody = "You have sent Rs. 1,250 to Ali. Fee Rs 10. Bal: Rs 50,000",
      receivedAtEpochMillis = 0,
    )
    val result = engine.parse(sms)
    assertTrue(result is SmsParserEngine.Result.Parsed)
  }

  @Test
  fun `parses ibft transfer heuristic`() {
    val engine = SmsParserEngine(GenericRules.all())
    val sms = SmsEnvelope(
      sender = "HBL",
      messageBody = "IBFT Transfer of PKR 15,000 to HBL-1234 from YourAccount. Fee PKR 25. Bal PKR 100,000",
      receivedAtEpochMillis = 0,
    )
    val result = engine.parse(sms)
    assertTrue(result is SmsParserEngine.Result.Parsed)
  }
}
