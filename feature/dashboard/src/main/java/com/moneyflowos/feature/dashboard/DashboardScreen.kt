package com.moneyflowos.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyflowos.core.common.MoneyFormat
import com.moneyflowos.core.domain.repo.DailyNetFlowPoint

@Composable
fun DashboardRoute(
  onOpenChannels: () -> Unit,
  vm: DashboardViewModel = hiltViewModel(),
) {
  LaunchedEffect(Unit) { vm.ensureSession() }
  val totals by vm.totals.collectAsState()
  val points by vm.daily.collectAsState()

  DashboardScreen(
    totalIn = totals.totalIn,
    totalOut = totals.totalOut,
    netFlow = totals.netFlow,
    feesPaid = totals.feesPaid,
    daily = points,
    onOpenChannels = onOpenChannels,
  )
}

@Composable
private fun DashboardScreen(
  totalIn: Long,
  totalOut: Long,
  netFlow: Long,
  feesPaid: Long,
  daily: List<DailyNetFlowPoint>,
  onOpenChannels: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text(
        text = "MONEY FLOW OS",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Text(text = "Offline-first finance intelligence from SMS (rule-based).", style = MaterialTheme.typography.bodyMedium)
    }

    item {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        KpiCard(title = "Money In", value = MoneyFormat.pkr(totalIn), modifier = Modifier.weight(1f))
        KpiCard(title = "Money Out", value = MoneyFormat.pkr(totalOut), modifier = Modifier.weight(1f))
      }
    }

    item {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        KpiCard(title = "Net Flow", value = MoneyFormat.pkr(netFlow), modifier = Modifier.weight(1f))
        KpiCard(title = "Fees", value = MoneyFormat.pkr(feesPaid), modifier = Modifier.weight(1f))
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = "Trend (last 14 days)", style = MaterialTheme.typography.titleMedium)
          TrendChart(points = daily, modifier = Modifier.fillMaxWidth().height(160.dp))
          Spacer(Modifier.height(2.dp))
          Text(
            text = "Tap “Channel Analytics” in the menu for rail breakdown (IBFT/RAAST/POS/ATM/etc).",
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth(), onClick = onOpenChannels) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(text = "Channel Analytics", style = MaterialTheme.typography.titleMedium)
          Text(text = "IBFT • RAAST • POS • ATM • Mobile Load", style = MaterialTheme.typography.bodyMedium)
        }
      }
    }
  }
}

@Composable
private fun KpiCard(title: String, value: String, modifier: Modifier = Modifier) {
  Card(modifier = modifier) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(text = title, style = MaterialTheme.typography.labelLarge)
      Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
private fun TrendChart(points: List<DailyNetFlowPoint>, modifier: Modifier = Modifier) {
  val inSeries = points.map { it.totalIn.toFloat() }
  val outSeries = points.map { it.totalOut.toFloat() }
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val errorColor = MaterialTheme.colorScheme.error
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface

  Canvas(modifier = modifier) {
    if (points.size < 2) return@Canvas

    val maxY = (inSeries.maxOrNull() ?: 0f).coerceAtLeast(outSeries.maxOrNull() ?: 0f).coerceAtLeast(1f)
    val w = size.width
    val h = size.height
    val step = w / (points.size - 1).toFloat()

    fun y(v: Float): Float = h - (v / maxY) * h

    fun path(series: List<Float>): Path {
      val p = Path()
      for (i in series.indices) {
        val x = i * step
        val yy = y(series[i])
        if (i == 0) p.moveTo(x, yy) else p.lineTo(x, yy)
      }
      return p
    }

    val stroke = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawPath(path(inSeries), color = tertiaryColor, style = stroke)
    drawPath(path(outSeries), color = errorColor, style = stroke)

    // baseline dots
    for (i in points.indices) {
      drawCircle(color = onSurfaceColor.copy(alpha = 0.18f), radius = 3f, center = Offset(i * step, h - 2f))
    }
  }
}

