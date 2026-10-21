package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.StockQuote
import com.example.ui.AppTab
import com.example.ui.TradeViewModel
import com.example.ui.components.OrderExecutionSheet
import com.example.ui.components.TradeConfirmationDialog
import com.example.ui.screens.AcademyScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.PortfolioScreen
import com.example.ui.screens.StockDetailScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDarkest
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        val viewModel: TradeViewModel = viewModel(
          factory = TradeViewModel.Factory(application)
        )
        NasdaqPaperTradingApp(viewModel)
      }
    }
  }
}

@Composable
fun NasdaqPaperTradingApp(viewModel: TradeViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  var activeDetailTicker by remember { mutableStateOf<String?>(null) }
  var tradeSheetStock by remember { mutableStateOf<StockQuote?>(null) }
  var tradeSheetIsBuy by remember { mutableStateOf(true) }

  // Show user feedback messages in snackbar
  LaunchedEffect(uiState.userFeedbackMessage) {
    uiState.userFeedbackMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.dismissFeedbackMessage()
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      // Show bottom navigation bar when not in detail view
      if (activeDetailTicker == null && uiState.activeLesson == null) {
        NavigationBar(
          containerColor = NavyDark,
          contentColor = TextPrimary,
          tonalElevation = 8.dp,
          modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar")
        ) {
          NavigationBarItem(
            selected = uiState.currentTab == AppTab.MARKET,
            onClick = { viewModel.selectTab(AppTab.MARKET) },
            icon = {
              Icon(
                imageVector = if (uiState.currentTab == AppTab.MARKET) Icons.Filled.ShowChart else Icons.Outlined.ShowChart,
                contentDescription = "Market"
              )
            },
            label = { Text("Market", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyDarkest,
              selectedTextColor = CyanNeon,
              indicatorColor = CyanNeon,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_market")
          )

          NavigationBarItem(
            selected = uiState.currentTab == AppTab.PORTFOLIO,
            onClick = { viewModel.selectTab(AppTab.PORTFOLIO) },
            icon = {
              Icon(
                imageVector = if (uiState.currentTab == AppTab.PORTFOLIO) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                contentDescription = "Portfolio"
              )
            },
            label = { Text("Portfolio", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyDarkest,
              selectedTextColor = CyanNeon,
              indicatorColor = CyanNeon,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_portfolio")
          )

          NavigationBarItem(
            selected = uiState.currentTab == AppTab.ORDERS,
            onClick = { viewModel.selectTab(AppTab.ORDERS) },
            icon = {
              BadgedBox(
                badge = {
                  if (uiState.pendingOrders.isNotEmpty()) {
                    Badge(
                      containerColor = GoldAccent,
                      contentColor = NavyDarkest
                    ) {
                      Text("${uiState.pendingOrders.size}")
                    }
                  }
                }
              ) {
                Icon(
                  imageVector = if (uiState.currentTab == AppTab.ORDERS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                  contentDescription = "Orders"
                )
              }
            },
            label = { Text("Orders", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyDarkest,
              selectedTextColor = CyanNeon,
              indicatorColor = CyanNeon,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_orders")
          )

          NavigationBarItem(
            selected = uiState.currentTab == AppTab.ACADEMY,
            onClick = { viewModel.selectTab(AppTab.ACADEMY) },
            icon = {
              Icon(
                imageVector = if (uiState.currentTab == AppTab.ACADEMY) Icons.Filled.School else Icons.Outlined.School,
                contentDescription = "Learn"
              )
            },
            label = { Text("Learn", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyDarkest,
              selectedTextColor = CyanNeon,
              indicatorColor = CyanNeon,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_academy")
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(NavyDarkest)
    ) {
      if (activeDetailTicker != null) {
        val selectedStock = uiState.quotes[activeDetailTicker]
        if (selectedStock != null) {
          StockDetailScreen(
            stock = selectedStock,
            candles = uiState.candles,
            marketDepth = uiState.marketDepth,
            selectedTimeframe = uiState.selectedTimeframe,
            chartType = uiState.chartType,
            showSMA = uiState.showSMA,
            showVolume = uiState.showVolume,
            isWatchlisted = uiState.watchlistTickers.contains(selectedStock.symbol),
            userPosition = uiState.portfolioSummary?.positions?.find { it.ticker == selectedStock.symbol },
            onBack = { activeDetailTicker = null },
            onTimeframeSelect = { tf -> viewModel.setTimeframe(tf) },
            onChartTypeToggle = { ct -> viewModel.setChartType(ct) },
            onToggleSMA = { viewModel.toggleIndicatorSMA() },
            onToggleVolume = { viewModel.toggleIndicatorVolume() },
            onToggleWatchlist = { viewModel.toggleWatchlist(selectedStock.symbol) },
            onOpenTrade = { isBuy ->
              tradeSheetStock = selectedStock
              tradeSheetIsBuy = isBuy
            }
          )
        }
      } else {
        when (uiState.currentTab) {
          AppTab.MARKET -> {
            MarketScreen(
              quotes = uiState.quotes,
              watchlist = uiState.watchlistTickers,
              selectedSector = uiState.selectedSector,
              searchQuery = uiState.searchQuery,
              onSearchChange = { viewModel.setSearchQuery(it) },
              onSectorSelect = { viewModel.setSectorFilter(it) },
              onStockClick = { sym ->
                viewModel.selectStock(sym)
                activeDetailTicker = sym
              },
              onToggleWatchlist = { sym -> viewModel.toggleWatchlist(sym) }
            )
          }

          AppTab.PORTFOLIO -> {
            PortfolioScreen(
              portfolioSummary = uiState.portfolioSummary,
              onStockClick = { sym ->
                viewModel.selectStock(sym)
                activeDetailTicker = sym
              },
              onResetCapital = { amount -> viewModel.resetPortfolio(amount) },
              onExploreMarket = { viewModel.selectTab(AppTab.MARKET) }
            )
          }

          AppTab.ORDERS -> {
            OrdersScreen(
              orders = uiState.orderHistory,
              pendingOrders = uiState.pendingOrders,
              quotes = uiState.quotes,
              onCancelOrder = { id -> viewModel.cancelPendingOrder(id) },
              onStockClick = { sym ->
                viewModel.selectStock(sym)
                activeDetailTicker = sym
              }
            )
          }

          AppTab.ACADEMY -> {
            AcademyScreen(
              lessons = uiState.educationalLessons,
              activeLesson = uiState.activeLesson,
              quizAnswers = uiState.quizAnswerState,
              onSelectLesson = { lesson -> viewModel.openLesson(lesson) },
              onAnswerQuiz = { lessonId, idx -> viewModel.submitQuizAnswer(lessonId, idx) }
            )
          }
        }
      }

      // Trade Execution Bottom Sheet
      tradeSheetStock?.let { stock ->
        OrderExecutionSheet(
          stock = stock,
          initialIsBuy = tradeSheetIsBuy,
          portfolioSummary = uiState.portfolioSummary,
          onDismiss = { tradeSheetStock = null },
          onExecuteTrade = { ticker, companyName, isBuy, orderType, shares, limitPrice ->
            viewModel.executeTrade(ticker, companyName, isBuy, orderType, shares, limitPrice)
          }
        )
      }

      // Trade Confirmation Dialog
      uiState.tradeConfirmationOrder?.let { order ->
        TradeConfirmationDialog(
          order = order,
          onDismiss = { viewModel.dismissTradeConfirmation() }
        )
      }
    }
  }
}
