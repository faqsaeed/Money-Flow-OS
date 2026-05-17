package com.moneyflowos.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.moneyflowos.sms.worker.SmsIngestWork

class BootCompletedReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
    // Placeholder for future periodic health checks. Keep deterministic + offline.
    SmsIngestWork.prime(context)
  }
}

