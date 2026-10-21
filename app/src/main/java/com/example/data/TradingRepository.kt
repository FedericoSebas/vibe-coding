package com.example.data

import com.example.data.local.AppDatabase
import com.example.data.local.OrderEntity
import com.example.data.local.PositionEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.WatchlistEntity
import com.example.model.PortfolioPositionItem
import com.example.model.PortfolioSummary
import com.example.model.StockQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

sealed class TradeResult {
  data class Success(val message: String, val order: OrderEntity) : TradeResult()
  data class Error(val reason: String) : TradeResult()
}

class TradingRepository(private val database: AppDatabase) {

  private val portfolioDao = database.portfolioDao()
  private val orderDao = database.orderDao()
  private val watchlistDao = database.watchlistDao()

  val userAccount: Flow<UserAccountEntity?> = portfolioDao.getUserAccount()
  val positions: Flow<List<PositionEntity>> = portfolioDao.getAllPositions()
  val orders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
  val pendingOrders: Flow<List<OrderEntity>> = orderDao.getPendingOrders()
  val watchlist: Flow<List<WatchlistEntity>> = watchlistDao.getWatchlist()

  suspend fun ensureAccountInitialized() = withContext(Dispatchers.IO) {
    val existing = portfolioDao.getUserAccountDirect()
    if (existing == null) {
      portfolioDao.insertOrUpdateAccount(
        UserAccountEntity(
          id = 1,
          cashBalance = 100000.0,
          startingCapital = 100000.0,
          resetCount = 0
        )
      )
      // Pre-populate some popular Nasdaq tickers in Watchlist
      listOf("QQQ", "AAPL", "NVDA", "TSLA", "MSFT", "AMZN").forEach {
        watchlistDao.addToWatchlist(WatchlistEntity(ticker = it))
      }
    }
  }

  suspend fun executeMarketBuy(
    ticker: String,
    companyName: String,
    shares: Double,
    price: Double
  ): TradeResult = withContext(Dispatchers.IO) {
    if (shares <= 0.0 || price <= 0.0) {
      return@withContext TradeResult.Error("Invalid order parameters")
    }

    val account = portfolioDao.getUserAccountDirect()
      ?: return@withContext TradeResult.Error("Account not found")

    val totalCost = roundToCents(shares * price)
    if (account.cashBalance < totalCost) {
      return@withContext TradeResult.Error(
        "Insufficient cash balance ($${formatMoney(account.cashBalance)} available, $${formatMoney(totalCost)} needed)"
      )
    }

    // Deduct cash
    portfolioDao.insertOrUpdateAccount(
      account.copy(cashBalance = roundToCents(account.cashBalance - totalCost))
    )

    // Update or create Position
    val existingPosition = portfolioDao.getPositionByTicker(ticker)
    if (existingPosition != null) {
      val newTotalShares = existingPosition.shares + shares
      val newTotalInvested = existingPosition.totalInvested + totalCost
      val newAvgCost = roundToCents(newTotalInvested / newTotalShares)
      portfolioDao.insertOrUpdatePosition(
        existingPosition.copy(
          shares = newTotalShares,
          averageCost = newAvgCost,
          totalInvested = newTotalInvested,
          lastUpdated = System.currentTimeMillis()
        )
      )
    } else {
      portfolioDao.insertOrUpdatePosition(
        PositionEntity(
          ticker = ticker,
          companyName = companyName,
          shares = shares,
          averageCost = roundToCents(price),
          totalInvested = totalCost
        )
      )
    }

    // Record filled order
    val order = OrderEntity(
      ticker = ticker,
      companyName = companyName,
      orderType = "MARKET_BUY",
      shares = shares,
      limitPrice = null,
      executionPrice = price,
      totalAmount = totalCost,
      status = "FILLED",
      timestamp = System.currentTimeMillis(),
      realizedPnL = 0.0
    )
    val orderId = orderDao.insertOrder(order)
    return@withContext TradeResult.Success("Market Buy executed for $shares shares of $ticker @ $$price", order.copy(id = orderId))
  }

  suspend fun executeMarketSell(
    ticker: String,
    companyName: String,
    shares: Double,
    price: Double
  ): TradeResult = withContext(Dispatchers.IO) {
    if (shares <= 0.0 || price <= 0.0) {
      return@withContext TradeResult.Error("Invalid order parameters")
    }

    val position = portfolioDao.getPositionByTicker(ticker)
      ?: return@withContext TradeResult.Error("You do not own any shares of $ticker")

    if (position.shares < shares - 0.0001) {
      return@withContext TradeResult.Error("You only own ${position.shares} shares of $ticker")
    }

    val account = portfolioDao.getUserAccountDirect()
      ?: return@withContext TradeResult.Error("Account not found")

    val totalProceeds = roundToCents(shares * price)
    val costBasisPortion = roundToCents(shares * position.averageCost)
    val realizedPnL = roundToCents(totalProceeds - costBasisPortion)

    // Add cash proceeds
    portfolioDao.insertOrUpdateAccount(
      account.copy(cashBalance = roundToCents(account.cashBalance + totalProceeds))
    )

    // Update or remove position
    val remainingShares = position.shares - shares
    if (remainingShares <= 0.0001) {
      portfolioDao.deletePosition(ticker)
    } else {
      val remainingInvested = roundToCents(remainingShares * position.averageCost)
      portfolioDao.insertOrUpdatePosition(
        position.copy(
          shares = remainingShares,
          totalInvested = remainingInvested,
          lastUpdated = System.currentTimeMillis()
        )
      )
    }

    // Record order
    val order = OrderEntity(
      ticker = ticker,
      companyName = companyName,
      orderType = "MARKET_SELL",
      shares = shares,
      limitPrice = null,
      executionPrice = price,
      totalAmount = totalProceeds,
      status = "FILLED",
      timestamp = System.currentTimeMillis(),
      realizedPnL = realizedPnL
    )
    val orderId = orderDao.insertOrder(order)
    return@withContext TradeResult.Success("Market Sell executed for $shares shares of $ticker @ $$price", order.copy(id = orderId))
  }

  suspend fun placeLimitOrder(
    ticker: String,
    companyName: String,
    isBuy: Boolean,
    shares: Double,
    limitPrice: Double
  ): TradeResult = withContext(Dispatchers.IO) {
    if (shares <= 0.0 || limitPrice <= 0.0) {
      return@withContext TradeResult.Error("Invalid limit price or share quantity")
    }

    val account = portfolioDao.getUserAccountDirect()
      ?: return@withContext TradeResult.Error("Account not found")

    val totalAmount = roundToCents(shares * limitPrice)

    if (isBuy) {
      if (account.cashBalance < totalAmount) {
        return@withContext TradeResult.Error("Insufficient cash to hold for limit order ($${formatMoney(totalAmount)} required)")
      }
      // Reserve cash for limit order
      portfolioDao.insertOrUpdateAccount(
        account.copy(cashBalance = roundToCents(account.cashBalance - totalAmount))
      )
      val order = OrderEntity(
        ticker = ticker,
        companyName = companyName,
        orderType = "LIMIT_BUY",
        shares = shares,
        limitPrice = limitPrice,
        executionPrice = null,
        totalAmount = totalAmount,
        status = "PENDING",
        timestamp = System.currentTimeMillis()
      )
      val orderId = orderDao.insertOrder(order)
      return@withContext TradeResult.Success("Limit Buy queued @ $$limitPrice. Cash is reserved until execution.", order.copy(id = orderId))
    } else {
      val position = portfolioDao.getPositionByTicker(ticker)
        ?: return@withContext TradeResult.Error("You do not own shares of $ticker to sell")
      if (position.shares < shares) {
        return@withContext TradeResult.Error("You only own ${position.shares} shares of $ticker")
      }
      val order = OrderEntity(
        ticker = ticker,
        companyName = companyName,
        orderType = "LIMIT_SELL",
        shares = shares,
        limitPrice = limitPrice,
        executionPrice = null,
        totalAmount = totalAmount,
        status = "PENDING",
        timestamp = System.currentTimeMillis()
      )
      val orderId = orderDao.insertOrder(order)
      return@withContext TradeResult.Success("Limit Sell queued @ $$limitPrice. Will trigger automatically at or above limit.", order.copy(id = orderId))
    }
  }

  suspend fun cancelPendingOrder(orderId: Long): Boolean = withContext(Dispatchers.IO) {
    val pendingList = orderDao.getPendingOrdersList()
    val order = pendingList.find { it.id == orderId } ?: return@withContext false

    // Refund cash if it was a LIMIT_BUY
    if (order.orderType == "LIMIT_BUY") {
      val account = portfolioDao.getUserAccountDirect()
      if (account != null) {
        portfolioDao.insertOrUpdateAccount(
          account.copy(cashBalance = roundToCents(account.cashBalance + order.totalAmount))
        )
      }
    }
    orderDao.cancelOrder(orderId)
    return@withContext true
  }

  suspend fun checkPendingLimitOrders(quotes: Map<String, StockQuote>) = withContext(Dispatchers.IO) {
    val pending = orderDao.getPendingOrdersList()
    for (order in pending) {
      val quote = quotes[order.ticker] ?: continue
      val limitP = order.limitPrice ?: continue

      if (order.orderType == "LIMIT_BUY" && quote.ask <= limitP) {
        // Fill Limit Buy
        val fillPrice = quote.ask
        val actualTotalCost = roundToCents(order.shares * fillPrice)
        val refundCash = roundToCents(order.totalAmount - actualTotalCost)

        val account = portfolioDao.getUserAccountDirect()
        if (account != null && refundCash > 0) {
          portfolioDao.insertOrUpdateAccount(
            account.copy(cashBalance = roundToCents(account.cashBalance + refundCash))
          )
        }

        val existing = portfolioDao.getPositionByTicker(order.ticker)
        if (existing != null) {
          val newShares = existing.shares + order.shares
          val newInvested = existing.totalInvested + actualTotalCost
          val newAvg = roundToCents(newInvested / newShares)
          portfolioDao.insertOrUpdatePosition(
            existing.copy(
              shares = newShares,
              averageCost = newAvg,
              totalInvested = newInvested,
              lastUpdated = System.currentTimeMillis()
            )
          )
        } else {
          portfolioDao.insertOrUpdatePosition(
            PositionEntity(
              ticker = order.ticker,
              companyName = order.companyName,
              shares = order.shares,
              averageCost = fillPrice,
              totalInvested = actualTotalCost
            )
          )
        }

        orderDao.updateOrder(
          order.copy(
            status = "FILLED",
            executionPrice = fillPrice,
            totalAmount = actualTotalCost,
            timestamp = System.currentTimeMillis()
          )
        )
      } else if (order.orderType == "LIMIT_SELL" && quote.bid >= limitP) {
        // Fill Limit Sell
        val fillPrice = quote.bid
        val totalProceeds = roundToCents(order.shares * fillPrice)
        val existing = portfolioDao.getPositionByTicker(order.ticker)
        val avgCost = existing?.averageCost ?: fillPrice
        val realized = roundToCents(totalProceeds - (order.shares * avgCost))

        val account = portfolioDao.getUserAccountDirect()
        if (account != null) {
          portfolioDao.insertOrUpdateAccount(
            account.copy(cashBalance = roundToCents(account.cashBalance + totalProceeds))
          )
        }

        if (existing != null) {
          val remainingShares = existing.shares - order.shares
          if (remainingShares <= 0.0001) {
            portfolioDao.deletePosition(order.ticker)
          } else {
            val remainingInvested = roundToCents(remainingShares * existing.averageCost)
            portfolioDao.insertOrUpdatePosition(
              existing.copy(
                shares = remainingShares,
                totalInvested = remainingInvested,
                lastUpdated = System.currentTimeMillis()
              )
            )
          }
        }

        orderDao.updateOrder(
          order.copy(
            status = "FILLED",
            executionPrice = fillPrice,
            totalAmount = totalProceeds,
            realizedPnL = realized,
            timestamp = System.currentTimeMillis()
          )
        )
      }
    }
  }

  suspend fun resetAccount(startingCapital: Double = 100000.0) = withContext(Dispatchers.IO) {
    portfolioDao.clearAllPositions()
    orderDao.clearAllOrders()
    val currentAccount = portfolioDao.getUserAccountDirect()
    val resetCount = (currentAccount?.resetCount ?: 0) + 1
    portfolioDao.insertOrUpdateAccount(
      UserAccountEntity(
        id = 1,
        cashBalance = startingCapital,
        startingCapital = startingCapital,
        resetCount = resetCount
      )
    )
  }

  suspend fun toggleWatchlist(ticker: String) = withContext(Dispatchers.IO) {
    val isPresent = watchlistDao.getWatchlist().firstOrNull()?.any { it.ticker == ticker } == true
    if (isPresent) {
      watchlistDao.removeFromWatchlist(ticker)
    } else {
      watchlistDao.addToWatchlist(WatchlistEntity(ticker = ticker))
    }
  }

  fun computePortfolioSummary(
    account: UserAccountEntity?,
    positionsList: List<PositionEntity>,
    quotes: Map<String, StockQuote>
  ): PortfolioSummary {
    val cash = account?.cashBalance ?: 100000.0
    val startCapital = account?.startingCapital ?: 100000.0

    var positionsValue = 0.0
    var totalInvestedCost = 0.0
    var todayPnLSum = 0.0

    val positionItems = positionsList.map { pos ->
      val quote = quotes[pos.ticker]
      val currentPrice = quote?.price ?: pos.averageCost
      val marketVal = roundToCents(pos.shares * currentPrice)
      val unrealized = roundToCents(marketVal - pos.totalInvested)
      val unrealizedPct = if (pos.totalInvested > 0) (unrealized / pos.totalInvested) * 100.0 else 0.0

      val dayChangePerShare = quote?.change ?: 0.0
      val dayPnL = roundToCents(pos.shares * dayChangePerShare)
      val dayPnLPct = quote?.changePercent ?: 0.0

      positionsValue += marketVal
      totalInvestedCost += pos.totalInvested
      todayPnLSum += dayPnL

      PortfolioPositionItem(
        ticker = pos.ticker,
        companyName = pos.companyName,
        shares = pos.shares,
        averageCost = pos.averageCost,
        currentPrice = currentPrice,
        marketValue = marketVal,
        totalInvested = pos.totalInvested,
        unrealizedPnL = unrealized,
        unrealizedPnLPercent = unrealizedPct,
        dayPnL = dayPnL,
        dayPnLPercent = dayPnLPct
      )
    }

    val totalEquity = roundToCents(cash + positionsValue)
    val totalPnL = roundToCents(totalEquity - startCapital)
    val totalPnLPct = if (startCapital > 0) (totalPnL / startCapital) * 100.0 else 0.0
    val todayPnLPct = if (totalEquity > 0) (todayPnLSum / totalEquity) * 100.0 else 0.0

    return PortfolioSummary(
      totalEquity = totalEquity,
      cashBalance = cash,
      positionsValue = roundToCents(positionsValue),
      totalUnrealizedPnL = totalPnL,
      totalUnrealizedPnLPercent = totalPnLPct,
      todayPnL = roundToCents(todayPnLSum),
      todayPnLPercent = todayPnLPct,
      startingCapital = startCapital,
      positions = positionItems
    )
  }

  private fun roundToCents(v: Double): Double = Math.round(v * 100.0) / 100.0

  private fun formatMoney(v: Double): String = String.format("%,.2f", v)
}
