package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.EducationalContent
import com.example.data.TradeResult
import com.example.data.TradingRepository
import com.example.data.local.AppDatabase
import com.example.data.local.OrderEntity
import com.example.data.local.PositionEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.WatchlistEntity
import com.example.data.network.NasdaqMarketDataSource
import com.example.model.Candle
import com.example.model.ChartType
import com.example.model.EducationalLesson
import com.example.model.MarketDepthLevel
import com.example.model.PortfolioSummary
import com.example.model.Sector
import com.example.model.StockQuote
import com.example.model.Timeframe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
  MARKET("Market"),
  PORTFOLIO("Portfolio"),
  ORDERS("Orders"),
  ACADEMY("Learn")
}

data class TradeUiState(
  val currentTab: AppTab = AppTab.MARKET,
  val quotes: Map<String, StockQuote> = emptyMap(),
  val selectedStock: StockQuote? = null,
  val candles: List<Candle> = emptyList(),
  val marketDepth: List<MarketDepthLevel> = emptyList(),
  val selectedTimeframe: Timeframe = Timeframe.ONE_DAY,
  val chartType: ChartType = ChartType.LINE,
  val showSMA: Boolean = true,
  val showVolume: Boolean = true,
  val selectedSector: Sector = Sector.ALL,
  val searchQuery: String = "",
  val watchlistTickers: Set<String> = emptySet(),
  val portfolioSummary: PortfolioSummary? = null,
  val orderHistory: List<OrderEntity> = emptyList(),
  val pendingOrders: List<OrderEntity> = emptyList(),
  val educationalLessons: List<EducationalLesson> = EducationalContent.lessons,
  val activeLesson: EducationalLesson? = null,
  val quizAnswerState: Map<String, Int> = emptyMap(),
  val isRefreshing: Boolean = false,
  val userFeedbackMessage: String? = null,
  val tradeConfirmationOrder: OrderEntity? = null,
  val showResetDialog: Boolean = false
)

class TradeViewModel(
  application: Application,
  private val tradingRepo: TradingRepository,
  private val marketDataSource: NasdaqMarketDataSource
) : AndroidViewModel(application) {

  private val _uiState = MutableStateFlow(TradeUiState())
  val uiState: StateFlow<TradeUiState> = _uiState.asStateFlow()

  private var tickJob: Job? = null
  private var candleJob: Job? = null

  init {
    viewModelScope.launch {
      tradingRepo.ensureAccountInitialized()
      initQuotes()
      startLiveMarketTicker()
      observeDatabase()
    }
  }

  private fun initQuotes() {
    val initialQuotes = mutableMapOf<String, StockQuote>()
    marketDataSource.nasdaqDirectory.forEach { base ->
      val quote = marketDataSource.getOrCreateQuote(base.symbol)
      initialQuotes[base.symbol] = quote
    }
    _uiState.value = _uiState.value.copy(
      quotes = initialQuotes,
      selectedStock = initialQuotes["QQQ"] ?: initialQuotes.values.firstOrNull()
    )
    loadCandlesForSelected()
  }

  private fun startLiveMarketTicker() {
    tickJob?.cancel()
    tickJob = viewModelScope.launch {
      while (isActive) {
        delay(2500)
        val updatedQuotes = _uiState.value.quotes.toMutableMap()

        // Prioritize updating active items
        val curSelected = _uiState.value.selectedStock?.symbol
        val positionsTickers = _uiState.value.portfolioSummary?.positions?.map { it.ticker } ?: emptyList()
        val watchTickers = _uiState.value.watchlistTickers.toList()
        val activeTop = marketDataSource.nasdaqDirectory.take(25).map { it.symbol }
        val searchMatches = if (_uiState.value.searchQuery.isNotBlank()) {
          _uiState.value.quotes.keys.take(25)
        } else emptyList()

        val symbolsToUpdate = (activeTop + positionsTickers + watchTickers + listOfNotNull(curSelected) + searchMatches).distinct()

        symbolsToUpdate.forEach { sym ->
          val updated = marketDataSource.generateSimulatedTick(sym)
          updatedQuotes[sym] = updated
        }

        // Check if selected stock needs depth update
        val curSelectedStock = _uiState.value.selectedStock
        val updatedSelected = if (curSelectedStock != null) updatedQuotes[curSelectedStock.symbol] ?: curSelectedStock else null
        val updatedDepth = if (updatedSelected != null) marketDataSource.generateDepth(updatedSelected) else emptyList()

        _uiState.value = _uiState.value.copy(
          quotes = updatedQuotes,
          selectedStock = updatedSelected,
          marketDepth = updatedDepth
        )

        // Evaluate pending limit orders against updated market quotes
        tradingRepo.checkPendingLimitOrders(updatedQuotes)
      }
    }
  }

  private fun observeDatabase() {
    viewModelScope.launch {
      combine(
        tradingRepo.userAccount,
        tradingRepo.positions,
        tradingRepo.orders,
        tradingRepo.watchlist
      ) { account: UserAccountEntity?, positions: List<PositionEntity>, orders: List<OrderEntity>, watchlist: List<WatchlistEntity> ->
        val watchSet = watchlist.map { it.ticker }.toSet()
        val pending = orders.filter { it.status == "PENDING" }
        val summary = tradingRepo.computePortfolioSummary(account, positions, _uiState.value.quotes)

        _uiState.value = _uiState.value.copy(
          watchlistTickers = watchSet,
          portfolioSummary = summary,
          orderHistory = orders,
          pendingOrders = pending
        )
      }.collect {}
    }
  }

  fun selectTab(tab: AppTab) {
    _uiState.value = _uiState.value.copy(currentTab = tab)
  }

  fun selectStock(symbol: String) {
    val stock = marketDataSource.getOrCreateQuote(symbol)
    val depth = marketDataSource.generateDepth(stock)
    val updatedQuotes = _uiState.value.quotes.toMutableMap()
    updatedQuotes[stock.symbol] = stock
    _uiState.value = _uiState.value.copy(
      selectedStock = stock,
      quotes = updatedQuotes,
      marketDepth = depth
    )
    loadCandlesForSelected()

    // Fetch live quote from Yahoo Finance API in background
    viewModelScope.launch {
      val realQuote = marketDataSource.fetchRealtimeQuote(symbol)
      if (realQuote != null) {
        val qMap = _uiState.value.quotes.toMutableMap()
        qMap[symbol] = realQuote
        val newDepth = marketDataSource.generateDepth(realQuote)
        _uiState.value = _uiState.value.copy(
          quotes = qMap,
          selectedStock = if (_uiState.value.selectedStock?.symbol == symbol) realQuote else _uiState.value.selectedStock,
          marketDepth = if (_uiState.value.selectedStock?.symbol == symbol) newDepth else _uiState.value.marketDepth
        )
      }
    }
  }

  fun setTimeframe(tf: Timeframe) {
    _uiState.value = _uiState.value.copy(selectedTimeframe = tf)
    loadCandlesForSelected()
  }

  fun setChartType(chartType: ChartType) {
    _uiState.value = _uiState.value.copy(chartType = chartType)
  }

  fun toggleIndicatorSMA() {
    _uiState.value = _uiState.value.copy(showSMA = !_uiState.value.showSMA)
  }

  fun toggleIndicatorVolume() {
    _uiState.value = _uiState.value.copy(showVolume = !_uiState.value.showVolume)
  }

  fun setSectorFilter(sector: Sector) {
    _uiState.value = _uiState.value.copy(selectedSector = sector)
  }

  fun setSearchQuery(query: String) {
    val trimmed = query.trim().uppercase()
    val updatedQuotes = _uiState.value.quotes.toMutableMap()
    if (trimmed.isNotEmpty()) {
      // Find matching symbols from the full 3,429 stock universe
      val matched = marketDataSource.allSymbols.filter {
        it.contains(trimmed, ignoreCase = true)
      }.take(40)

      matched.forEach { sym ->
        if (!updatedQuotes.containsKey(sym)) {
          updatedQuotes[sym] = marketDataSource.getOrCreateQuote(sym)
        }
      }
    }
    _uiState.value = _uiState.value.copy(
      searchQuery = query,
      quotes = updatedQuotes
    )
  }

  fun toggleWatchlist(symbol: String) {
    viewModelScope.launch {
      tradingRepo.toggleWatchlist(symbol)
    }
  }

  fun executeTrade(
    ticker: String,
    companyName: String,
    isBuy: Boolean,
    orderType: String,
    shares: Double,
    limitPrice: Double?
  ) {
    val quote = marketDataSource.getOrCreateQuote(ticker)
    val executionPrice = if (isBuy) quote.ask else quote.bid

    viewModelScope.launch {
      val result = if (orderType == "LIMIT" && limitPrice != null && limitPrice > 0) {
        tradingRepo.placeLimitOrder(ticker, companyName, isBuy, shares, limitPrice)
      } else {
        if (isBuy) {
          tradingRepo.executeMarketBuy(ticker, companyName, shares, executionPrice)
        } else {
          tradingRepo.executeMarketSell(ticker, companyName, shares, executionPrice)
        }
      }

      when (result) {
        is TradeResult.Success -> {
          _uiState.value = _uiState.value.copy(
            tradeConfirmationOrder = result.order,
            userFeedbackMessage = result.message
          )
        }
        is TradeResult.Error -> {
          _uiState.value = _uiState.value.copy(
            userFeedbackMessage = result.reason
          )
        }
      }
    }
  }

  fun cancelPendingOrder(orderId: Long) {
    viewModelScope.launch {
      val success = tradingRepo.cancelPendingOrder(orderId)
      if (success) {
        _uiState.value = _uiState.value.copy(
          userFeedbackMessage = "Pending order cancelled. Funds returned."
        )
      }
    }
  }

  fun openLesson(lesson: EducationalLesson?) {
    _uiState.value = _uiState.value.copy(activeLesson = lesson)
  }

  fun submitQuizAnswer(lessonId: String, selectedIndex: Int) {
    val currentAnswers = _uiState.value.quizAnswerState.toMutableMap()
    currentAnswers[lessonId] = selectedIndex
    _uiState.value = _uiState.value.copy(quizAnswerState = currentAnswers)
  }

  fun dismissFeedbackMessage() {
    _uiState.value = _uiState.value.copy(userFeedbackMessage = null)
  }

  fun dismissTradeConfirmation() {
    _uiState.value = _uiState.value.copy(tradeConfirmationOrder = null)
  }

  fun showResetDialog(show: Boolean) {
    _uiState.value = _uiState.value.copy(showResetDialog = show)
  }

  fun resetPortfolio(capitalAmount: Double = 100000.0) {
    viewModelScope.launch {
      tradingRepo.resetAccount(capitalAmount)
      _uiState.value = _uiState.value.copy(
        showResetDialog = false,
        userFeedbackMessage = "Simulated portfolio reset to $$capitalAmount capital."
      )
    }
  }

  private fun loadCandlesForSelected() {
    val stock = _uiState.value.selectedStock ?: return
    val tf = _uiState.value.selectedTimeframe
    candleJob?.cancel()
    candleJob = viewModelScope.launch {
      val candles = marketDataSource.fetchCandles(stock.symbol, tf)
      _uiState.value = _uiState.value.copy(candles = candles)
    }
  }

  class Factory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      val db = AppDatabase.getDatabase(app)
      val repo = TradingRepository(db)
      val source = NasdaqMarketDataSource(app.applicationContext)
      return TradeViewModel(app, repo, source) as T
    }
  }
}
