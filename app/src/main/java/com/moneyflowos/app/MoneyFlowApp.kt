package com.moneyflowos.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moneyflowos.core.security.AdminSession
import com.moneyflowos.feature.admin.AdminLoginRoute
import com.moneyflowos.feature.admin.AdminPanelRoute
import com.moneyflowos.feature.admin.BulkEditRoute
import com.moneyflowos.feature.admin.CorrectionLogsRoute
import com.moneyflowos.feature.admin.TxEditorRoute
import com.moneyflowos.feature.dashboard.ChannelAnalyticsRoute
import com.moneyflowos.feature.dashboard.DashboardRoute
import com.moneyflowos.feature.people.PeopleRoute
import com.moneyflowos.feature.session.SessionRoute
import com.moneyflowos.feature.transactions.TransactionDetailsRoute
import com.moneyflowos.feature.transactions.TransactionsRoute
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MoneyFlowApp(
  navController: NavHostController = rememberNavController(),
  adminSession: AdminSession = hiltViewModel<AdminSessionViewModel>().session,
) {
  val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
  val showBottomBar = currentRoute?.startsWith("admin") != true && currentRoute?.startsWith("tx") != true

  Scaffold(
    bottomBar = {
      if (showBottomBar) BottomNav(navController)
    },
    containerColor = MaterialTheme.colorScheme.background,
  ) { padding ->
    NavHost(
      navController = navController,
      startDestination = Routes.Dashboard,
      modifier = Modifier.padding(padding),
    ) {
      composable(Routes.Dashboard) {
        DashboardRoute(onOpenChannels = { navController.navigate(Routes.Channels) })
      }
      composable(Routes.Transactions) {
        TransactionsRoute(onOpenTransaction = { id -> navController.navigate("${Routes.TransactionDetails}/$id") })
      }
      composable("${Routes.TransactionDetails}/{id}") {
        val unlocked by adminSession.isUnlocked.collectAsState()
        TransactionDetailsRoute(
          canEdit = unlocked,
          onEdit = { id -> navController.navigate("${Routes.AdminTxEditor}/$id") },
        )
      }
      composable(Routes.People) { PeopleRoute() }
      composable(Routes.Session) {
        SessionRoute(onOpenAdmin = { navController.navigate(Routes.AdminLogin) })
      }
      composable(Routes.Channels) { ChannelAnalyticsRoute() }

      composable(Routes.AdminLogin) {
        AdminLoginRoute(onUnlocked = { navController.navigate(Routes.AdminPanel) })
      }
      composable(Routes.AdminPanel) {
        AdminPanelRoute(
          onOpenTxEditor = { id -> navController.navigate("${Routes.AdminTxEditor}/$id") },
          onOpenLogs = { navController.navigate(Routes.AdminLogs) },
          onOpenBulk = { navController.navigate(Routes.AdminBulk) },
          onExit = { navController.popBackStack(Routes.Dashboard, inclusive = false) },
        )
      }
      composable(Routes.AdminLogs) { CorrectionLogsRoute() }
      composable(Routes.AdminBulk) { BulkEditRoute(onDone = { navController.popBackStack() }) }
      composable("${Routes.AdminTxEditor}/{id}") { TxEditorRoute(onDone = { navController.popBackStack() }) }
    }
  }
}

private object Routes {
  const val Dashboard = "dashboard"
  const val Transactions = "transactions"
  const val People = "people"
  const val Session = "session"
  const val Channels = "channels"
  const val TransactionDetails = "tx"
  const val AdminLogin = "admin/login"
  const val AdminPanel = "admin/panel"
  const val AdminLogs = "admin/logs"
  const val AdminBulk = "admin/bulk"
  const val AdminTxEditor = "admin/tx"
}

@Composable
private fun BottomNav(navController: NavHostController) {
  val backStack by navController.currentBackStackEntryAsState()
  val route = backStack?.destination?.route

  val items = listOf(
    NavItem("Home", Routes.Dashboard),
    NavItem("Tx", Routes.Transactions),
    NavItem("People", Routes.People),
    NavItem("Session", Routes.Session),
  )

  NavigationBar {
    for (item in items) {
      val selected = route == item.route
      NavigationBarItem(
        selected = selected,
        onClick = {
          navController.navigate(item.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
          }
        },
        icon = { },
        label = { Text(item.label) },
      )
    }
  }
}

private data class NavItem(val label: String, val route: String)
