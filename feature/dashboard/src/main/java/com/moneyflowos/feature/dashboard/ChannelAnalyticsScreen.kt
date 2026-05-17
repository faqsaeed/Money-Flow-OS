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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyflowos.core.common.MoneyFormat
import com.moneyflowos.core.domain.repo.ChannelTotal
import kotlin.math.min

@Composable
fun ChannelAnalyticsRoute(
  vm: ChannelAnalyticsViewModel = hiltViewModel(),
) {
  val channels by vm.channels.collectAsState()
  ChannelAnalyticsScreen(channels)
}

@Composable
private fun ChannelAnalyticsScreen(channels: List<ChannelTotal>) {
  val totals = channels.map { (it.totalIn + it.totalOut).coerceAtLeast(0) }
  val grand = totals.sum().coerceAtLeast(1)

  LazyColumn(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text(text = "Channel Analytics", style = MaterialTheme.typography.headlineSmall)
      Text(text = "Deterministic breakdown by rail.", style = MaterialTheme.typography.bodyMedium)
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = "Share of Volume", style = MaterialTheme.typography.titleMedium)
          Pie(channels = channels, modifier = Modifier.fillMaxWidth().height(200.dp))
        }
      }
    }

    items(channels) { row ->
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = row.channel.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "${(100 * (row.totalIn + row.totalOut) / grand)}%", style = MaterialTheme.typography.labelLarge)
          }
          Text(text = "In: ${MoneyFormat.pkr(row.totalIn)}", style = MaterialTheme.typography.bodyMedium)
          Text(text = "Out: ${MoneyFormat.pkr(row.totalOut)}", style = MaterialTheme.typography.bodyMedium)
          Text(text = "Count: ${row.count}", style = MaterialTheme.typography.bodySmall)
        }
      }
    }
  }
}

@Composable
private fun Pie(channels: List<ChannelTotal>, modifier: Modifier = Modifier) {
  val total = channels.sumOf { (it.totalIn + it.totalOut).coerceAtLeast(0) }.coerceAtLeast(1)
  val colors = listOf(
    MaterialTheme.colorScheme.primary,
    MaterialTheme.colorScheme.secondary,
    MaterialTheme.colorScheme.tertiary,
    MaterialTheme.colorScheme.error,
    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
    MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f),
    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f),
  )

  Canvas(modifier = modifier) {
    val d = min(size.width, size.height)
    val topLeft = androidx.compose.ui.geometry.Offset((size.width - d) / 2f, (size.height - d) / 2f)
    val rectSize = Size(d, d)
    var start = -90f
    for ((index, row) in channels.withIndex()) {
      val v = (row.totalIn + row.totalOut).coerceAtLeast(0)
      if (v == 0L) continue
      val sweep = (v.toFloat() / total.toFloat()) * 360f
      drawArc(
        color = colors[index % colors.size],
        startAngle = start,
        sweepAngle = sweep,
        useCenter = true,
        topLeft = topLeft,
        size = rectSize,
      )
      start += sweep
    }
    drawArc(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
      startAngle = 0f,
      sweepAngle = 360f,
      useCenter = false,
      style = Stroke(width = 2f),
      topLeft = topLeft,
      size = rectSize,
    )
  }
}

