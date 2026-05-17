package com.moneyflowos.feature.people

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyflowos.core.common.MoneyFormat
import com.moneyflowos.core.domain.model.Person
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun PeopleRoute(
  vm: PeopleViewModel = hiltViewModel(),
) {
  val people by vm.people.collectAsState()
  PeopleScreen(people = people)
}

@Composable
private fun PeopleScreen(people: List<Person>) {
  val top = people.take(12)
  LazyColumn(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text(text = "People Ledger", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
      Text(text = "Nodes=people/entities, edges=money flow (You ↔ them).", style = MaterialTheme.typography.bodyMedium)
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = "Relationship Graph (Top 12)", style = MaterialTheme.typography.titleMedium)
          PeopleGraph(top, modifier = Modifier.fillMaxWidth().height(240.dp))
        }
      }
    }

    items(people, key = { it.id }) { p ->
      PersonRow(p)
    }
  }
}

@Composable
private fun PersonRow(p: Person) {
  val net = p.totalReceived - p.totalSent
  val netColor = when {
    net > 0 -> MaterialTheme.colorScheme.tertiary
    net < 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
  }
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = p.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = "Net: ${MoneyFormat.pkr(net)}", color = netColor, fontWeight = FontWeight.Bold)
      }
      Text(text = "Sent: ${MoneyFormat.pkr(p.totalSent)} • Received: ${MoneyFormat.pkr(p.totalReceived)}", style = MaterialTheme.typography.bodyMedium)
      Text(text = "Count: ${p.transactionCount}", style = MaterialTheme.typography.bodySmall)
    }
  }
}

@Composable
private fun PeopleGraph(people: List<Person>, modifier: Modifier = Modifier) {
  val maxWeight = people.maxOfOrNull { (it.totalSent + it.totalReceived).coerceAtLeast(1) } ?: 1L
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)
    val r = min(w, h) * 0.38f
    val youRadius = 18f

    // Center node (You)
    drawCircle(color = MaterialTheme.colorScheme.primary, radius = youRadius, center = center)

    val n = people.size.coerceAtLeast(1)
    for (i in people.indices) {
      val p = people[i]
      val weight = (p.totalSent + p.totalReceived).coerceAtLeast(1)
      val thickness = 2f + 10f * (weight.toFloat() / maxWeight.toFloat())
      val angle = (i.toFloat() / n.toFloat()) * (2f * PI).toFloat() - (PI / 2f).toFloat()
      val node = Offset(
        x = center.x + r * cos(angle),
        y = center.y + r * sin(angle),
      )
      val nodeRadius = 10f + 10f * (weight.toFloat() / maxWeight.toFloat())

      val edgeColor = when {
        p.totalReceived > p.totalSent -> MaterialTheme.colorScheme.tertiary
        p.totalSent > p.totalReceived -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
      }
      drawLine(
        color = edgeColor.copy(alpha = 0.65f),
        start = center,
        end = node,
        strokeWidth = thickness,
        cap = StrokeCap.Round,
      )
      drawCircle(color = MaterialTheme.colorScheme.surface, radius = nodeRadius + 2f, center = node)
      drawCircle(color = edgeColor, radius = nodeRadius, center = node)
    }

    // subtle ring
    drawCircle(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
      radius = r,
      center = center,
      style = Stroke(width = 2f),
    )
  }
}

