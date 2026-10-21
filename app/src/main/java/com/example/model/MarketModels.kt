package com.example.model

enum class Timeframe(val label: String, val interval: String, val range: String) {
  ONE_DAY("1D", "1m", "1d"),
  ONE_WEEK("1W", "15m", "5d"),
  ONE_MONTH("1M", "1d", "1mo"),
  ONE_YEAR("1Y", "1d", "1y"),
  ALL("ALL", "1wk", "5y")
}

enum class ChartType {
  LINE,
  CANDLESTICK
}

enum class Sector(val displayName: String) {
  ALL("All"),
  MEGA_TECH("Mega Tech"),
  SEMICONDUCTORS("Semiconductors"),
  AI_CLOUD("AI & Cloud"),
  CONSUMER("Consumer Tech"),
  INDEX("Indices & ETFs")
}

data class StockQuote(
  val symbol: String,
  val name: String,
  val sector: Sector,
  val price: Double,
  val previousClose: Double,
  val change: Double,
  val changePercent: Double,
  val dayHigh: Double,
  val dayLow: Double,
  val volume: Long,
  val peRatio: Double,
  val marketCap: String,
  val bid: Double,
  val ask: Double,
  val lastUpdated: Long = System.currentTimeMillis(),
  val isMarketOpen: Boolean = true,
  val sparkline: List<Double> = emptyList()
)

data class Candle(
  val timestamp: Long,
  val open: Double,
  val high: Double,
  val low: Double,
  val close: Double,
  val volume: Long
)

data class MarketDepthLevel(
  val bidPrice: Double,
  val bidSize: Int,
  val askPrice: Double,
  val askSize: Int
)

data class PortfolioPositionItem(
  val ticker: String,
  val companyName: String,
  val shares: Double,
  val averageCost: Double,
  val currentPrice: Double,
  val marketValue: Double,
  val totalInvested: Double,
  val unrealizedPnL: Double,
  val unrealizedPnLPercent: Double,
  val dayPnL: Double,
  val dayPnLPercent: Double
)

data class PortfolioSummary(
  val totalEquity: Double,
  val cashBalance: Double,
  val positionsValue: Double,
  val totalUnrealizedPnL: Double,
  val totalUnrealizedPnLPercent: Double,
  val todayPnL: Double,
  val todayPnLPercent: Double,
  val startingCapital: Double,
  val positions: List<PortfolioPositionItem>
)

data class EducationalLesson(
  val id: String,
  val title: String,
  val category: String,
  val readingTime: String,
  val iconName: String,
  val shortSummary: String,
  val detailedBody: String,
  val keyTakeaways: List<String>,
  val quizQuestion: String,
  val quizOptions: List<String>,
  val correctOptionIndex: Int,
  val explanation: String
)
