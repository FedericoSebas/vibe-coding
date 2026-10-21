package com.example.data.network

import android.content.Context
import com.example.model.Candle
import com.example.model.MarketDepthLevel
import com.example.model.Sector
import com.example.model.StockQuote
import com.example.model.Timeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class BaseNasdaqStock(
  val symbol: String,
  val name: String,
  val sector: Sector,
  val basePrice: Double,
  val marketCap: String,
  val peRatio: Double,
  val avgVolume: Long
)

class NasdaqMarketDataSource(context: Context? = null) {

  private val client = OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .build()

  val allSymbols: List<String> = NasdaqStockCatalog.loadAllSymbols(context)

  // Map of all base stocks for O(1) lookup
  private val baseStockMap = mutableMapOf<String, BaseNasdaqStock>()

  val nasdaqDirectory: List<BaseNasdaqStock>

  // Cache for dynamic quotes to keep consecutive ticks consistent
  private val simulatedQuotes = mutableMapOf<String, StockQuote>()

  init {
    nasdaqDirectory = allSymbols.take(100).map { sym ->
      val base = NasdaqStockCatalog.getBaseStock(sym)
      baseStockMap[sym] = base
      base
    }

    // Pre-populate initial active quotes
    nasdaqDirectory.forEach { base ->
      generateSimulatedTick(base.symbol)
    }
  }

  fun getBaseStock(symbol: String): BaseNasdaqStock {
    val sym = symbol.uppercase()
    return baseStockMap.getOrPut(sym) {
      NasdaqStockCatalog.getBaseStock(sym)
    }
  }

  fun getOrCreateQuote(symbol: String): StockQuote {
    val sym = symbol.uppercase()
    return simulatedQuotes[sym] ?: generateSimulatedTick(sym)
  }

  suspend fun fetchRealtimeQuote(symbol: String): StockQuote? = withContext(Dispatchers.IO) {
    val sym = symbol.uppercase()
    try {
      val url = "https://query1.finance.yahoo.com/v8/finance/chart/$sym?interval=1m&range=1d"
      val request = Request.Builder()
        .url(url)
        .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
        .build()

      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        val body = response.body?.string()
        if (!body.isNullOrBlank()) {
          val json = JSONObject(body)
          val chart = json.getJSONObject("chart")
          val results = chart.getJSONArray("result")
          if (results.length() > 0) {
            val res = results.getJSONObject(0)
            val meta = res.getJSONObject("meta")
            val price = meta.optDouble("regularMarketPrice", 0.0)
            val prevClose = meta.optDouble("previousClose", price)
            val dayHigh = meta.optDouble("regularMarketDayHigh", price * 1.01)
            val dayLow = meta.optDouble("regularMarketDayLow", price * 0.99)
            val volume = meta.optLong("regularMarketVolume", 1000000L)

            val base = getBaseStock(sym)
            val name = meta.optString("shortName", base.name)
            val sector = base.sector
            val peRatio = base.peRatio
            val marketCap = base.marketCap

            val change = price - prevClose
            val changePercent = if (prevClose > 0) (change / prevClose) * 100.0 else 0.0
            val spread = max(0.01, price * 0.0003)

            // Extract sparkline points from indicators
            val sparkline = mutableListOf<Double>()
            if (res.has("indicators")) {
              val indicators = res.getJSONObject("indicators")
              if (indicators.has("quote")) {
                val quoteArr = indicators.getJSONArray("quote")
                if (quoteArr.length() > 0) {
                  val closes = quoteArr.getJSONObject(0).optJSONArray("close")
                  if (closes != null) {
                    for (i in 0 until closes.length()) {
                      val v = closes.optDouble(i)
                      if (!v.isNaN() && v > 0) {
                        sparkline.add(roundToCents(v))
                      }
                    }
                  }
                }
              }
            }

            val finalSparkline = if (sparkline.size >= 5) sparkline.takeLast(20) else generateSparkline(price, prevClose)

            val quote = StockQuote(
              symbol = sym,
              name = name,
              sector = sector,
              price = roundToCents(price),
              previousClose = roundToCents(prevClose),
              change = roundToCents(change),
              changePercent = roundToCents(changePercent),
              dayHigh = roundToCents(dayHigh),
              dayLow = roundToCents(dayLow),
              volume = volume,
              peRatio = peRatio,
              marketCap = marketCap,
              bid = roundToCents(price - spread / 2),
              ask = roundToCents(price + spread / 2),
              isMarketOpen = true,
              sparkline = finalSparkline
            )
            simulatedQuotes[sym] = quote
            return@withContext quote
          }
        }
      }
    } catch (_: Exception) {
      // Fallback to simulated live quote on network failure or rate limit
    }
    return@withContext generateSimulatedTick(sym)
  }

  suspend fun fetchCandles(symbol: String, timeframe: Timeframe): List<Candle> = withContext(Dispatchers.IO) {
    val sym = symbol.uppercase()
    try {
      val url = "https://query1.finance.yahoo.com/v8/finance/chart/$sym?interval=${timeframe.interval}&range=${timeframe.range}"
      val request = Request.Builder()
        .url(url)
        .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
        .build()

      val response = client.newCall(request).execute()
      if (response.isSuccessful) {
        val body = response.body?.string()
        if (!body.isNullOrBlank()) {
          val json = JSONObject(body)
          val chart = json.getJSONObject("chart")
          val results = chart.getJSONArray("result")
          if (results.length() > 0) {
            val res = results.getJSONObject(0)
            val timestamps = res.optJSONArray("timestamp")
            val quoteArr = res.optJSONObject("indicators")?.optJSONArray("quote")
            if (timestamps != null && quoteArr != null && quoteArr.length() > 0) {
              val q = quoteArr.getJSONObject(0)
              val opens = q.optJSONArray("open")
              val highs = q.optJSONArray("high")
              val lows = q.optJSONArray("low")
              val closes = q.optJSONArray("close")
              val volumes = q.optJSONArray("volume")

              val candles = mutableListOf<Candle>()
              for (i in 0 until timestamps.length()) {
                val t = timestamps.optLong(i) * 1000
                val o = opens?.optDouble(i) ?: Double.NaN
                val h = highs?.optDouble(i) ?: Double.NaN
                val l = lows?.optDouble(i) ?: Double.NaN
                val c = closes?.optDouble(i) ?: Double.NaN
                val v = volumes?.optLong(i, 50000L) ?: 50000L

                if (!o.isNaN() && !h.isNaN() && !l.isNaN() && !c.isNaN() && c > 0) {
                  candles.add(Candle(t, roundToCents(o), roundToCents(h), roundToCents(l), roundToCents(c), v))
                }
              }
              if (candles.size >= 8) {
                return@withContext candles
              }
            }
          }
        }
      }
    } catch (_: Exception) {
      // Fall through to fallback generator
    }
    return@withContext generateFallbackCandles(sym, timeframe)
  }

  fun generateSimulatedTick(symbol: String): StockQuote {
    val sym = symbol.uppercase()
    val existing = simulatedQuotes[sym]
    val base = getBaseStock(sym)
    val currentPrice = existing?.price ?: base.basePrice
    val prevClose = existing?.previousClose ?: (base.basePrice * 0.99)

    // Micro-fluctuation: +/- 0.05% to 0.2%
    val driftPercent = Random.nextDouble(-0.002, 0.0022)
    val newPrice = roundToCents(max(0.10, currentPrice * (1.0 + driftPercent)))
    val change = roundToCents(newPrice - prevClose)
    val changePercent = roundToCents((change / prevClose) * 100.0)
    val newDayHigh = max(existing?.dayHigh ?: newPrice, newPrice)
    val newDayLow = min(existing?.dayLow ?: newPrice, newPrice)
    val spread = max(0.01, roundToCents(newPrice * 0.0003))

    val currentSpark = existing?.sparkline?.toMutableList() ?: mutableListOf()
    if (currentSpark.isEmpty()) {
      currentSpark.addAll(generateSparkline(newPrice, prevClose))
    } else {
      currentSpark.add(newPrice)
      if (currentSpark.size > 24) currentSpark.removeAt(0)
    }

    val updated = StockQuote(
      symbol = sym,
      name = existing?.name ?: base.name,
      sector = existing?.sector ?: base.sector,
      price = newPrice,
      previousClose = prevClose,
      change = change,
      changePercent = changePercent,
      dayHigh = newDayHigh,
      dayLow = newDayLow,
      volume = (existing?.volume ?: base.avgVolume) + Random.nextLong(100, 1500),
      peRatio = existing?.peRatio ?: base.peRatio,
      marketCap = existing?.marketCap ?: base.marketCap,
      bid = roundToCents(newPrice - spread / 2),
      ask = roundToCents(newPrice + spread / 2),
      isMarketOpen = true,
      sparkline = currentSpark
    )
    simulatedQuotes[sym] = updated
    return updated
  }

  fun generateDepth(quote: StockQuote): List<MarketDepthLevel> {
    val levels = mutableListOf<MarketDepthLevel>()
    val baseSpread = max(0.01, roundToCents(quote.price * 0.0002))
    for (i in 1..5) {
      val bidP = roundToCents(quote.bid - (i - 1) * baseSpread)
      val askP = roundToCents(quote.ask + (i - 1) * baseSpread)
      val bidS = (100 * (6 - i) * Random.nextInt(2, 6))
      val askS = (100 * (6 - i) * Random.nextInt(2, 6))
      levels.add(MarketDepthLevel(bidP, bidS, askP, askS))
    }
    return levels
  }

  private fun generateSparkline(currentPrice: Double, prevClose: Double): List<Double> {
    val points = mutableListOf<Double>()
    val steps = 20
    val totalDelta = currentPrice - prevClose
    for (i in 0 until steps) {
      val progress = i.toDouble() / steps
      val randomNoise = currentPrice * Random.nextDouble(-0.004, 0.004)
      val p = prevClose + totalDelta * progress + randomNoise
      points.add(roundToCents(p))
    }
    points.add(currentPrice)
    return points
  }

  private fun generateFallbackCandles(symbol: String, timeframe: Timeframe): List<Candle> {
    val quote = getOrCreateQuote(symbol)
    val currentPrice = quote.price
    val count = when (timeframe) {
      Timeframe.ONE_DAY -> 35
      Timeframe.ONE_WEEK -> 40
      Timeframe.ONE_MONTH -> 30
      Timeframe.ONE_YEAR -> 52
      Timeframe.ALL -> 60
    }
    val intervalMillis = when (timeframe) {
      Timeframe.ONE_DAY -> 10 * 60 * 1000L
      Timeframe.ONE_WEEK -> 2 * 3600 * 1000L
      Timeframe.ONE_MONTH -> 24 * 3600 * 1000L
      Timeframe.ONE_YEAR -> 7 * 24 * 3600 * 1000L
      Timeframe.ALL -> 30 * 24 * 3600 * 1000L
    }

    val now = System.currentTimeMillis()
    val candles = mutableListOf<Candle>()
    var runningPrice = currentPrice * 0.92

    for (i in 0 until count) {
      val t = now - (count - i) * intervalMillis
      val open = runningPrice
      val delta = runningPrice * Random.nextDouble(-0.015, 0.018)
      val close = roundToCents(max(1.0, open + delta))
      val high = roundToCents(max(open, close) + runningPrice * Random.nextDouble(0.001, 0.008))
      val low = roundToCents(min(open, close) - runningPrice * Random.nextDouble(0.001, 0.008))
      val vol = Random.nextLong(200000, 2000000)
      candles.add(Candle(t, open, high, low, close, vol))
      runningPrice = close
    }
    return candles
  }

  private fun roundToCents(value: Double): Double {
    return Math.round(value * 100.0) / 100.0
  }
}
