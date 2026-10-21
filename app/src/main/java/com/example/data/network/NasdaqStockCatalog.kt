package com.example.data.network

import android.content.Context
import com.example.model.Sector
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs

object NasdaqStockCatalog {

  // Prominent well-known NASDAQ stocks metadata
  private val knownMetadata = mapOf(
    "QQQ" to Pair("Invesco QQQ Trust", Sector.INDEX),
    "AAPL" to Pair("Apple Inc.", Sector.MEGA_TECH),
    "NVDA" to Pair("NVIDIA Corporation", Sector.SEMICONDUCTORS),
    "MSFT" to Pair("Microsoft Corporation", Sector.MEGA_TECH),
    "AMZN" to Pair("Amazon.com Inc.", Sector.AI_CLOUD),
    "GOOGL" to Pair("Alphabet Inc. Class A", Sector.AI_CLOUD),
    "GOOG" to Pair("Alphabet Inc. Class C", Sector.AI_CLOUD),
    "META" to Pair("Meta Platforms Inc.", Sector.MEGA_TECH),
    "TSLA" to Pair("Tesla Inc.", Sector.CONSUMER),
    "PLTR" to Pair("Palantir Technologies Inc.", Sector.AI_CLOUD),
    "COIN" to Pair("Coinbase Global Inc.", Sector.AI_CLOUD),
    "HOOD" to Pair("Robinhood Markets Inc.", Sector.CONSUMER),
    "SOFI" to Pair("SoFi Technologies Inc.", Sector.CONSUMER),
    "MSTR" to Pair("MicroStrategy Incorporated", Sector.MEGA_TECH),
    "RIVN" to Pair("Rivian Automotive Inc.", Sector.CONSUMER),
    "SMCI" to Pair("Super Micro Computer Inc.", Sector.SEMICONDUCTORS),
    "AVGO" to Pair("Broadcom Inc.", Sector.SEMICONDUCTORS),
    "AMD" to Pair("Advanced Micro Devices Inc.", Sector.SEMICONDUCTORS),
    "NFLX" to Pair("Netflix Inc.", Sector.CONSUMER),
    "COST" to Pair("Costco Wholesale Corp.", Sector.CONSUMER),
    "QCOM" to Pair("Qualcomm Inc.", Sector.SEMICONDUCTORS),
    "ADBE" to Pair("Adobe Inc.", Sector.AI_CLOUD),
    "ASML" to Pair("ASML Holding N.V.", Sector.SEMICONDUCTORS),
    "INTC" to Pair("Intel Corporation", Sector.SEMICONDUCTORS),
    "ARM" to Pair("Arm Holdings plc", Sector.SEMICONDUCTORS),
    "PANW" to Pair("Palo Alto Networks", Sector.AI_CLOUD),
    "CRWD" to Pair("CrowdStrike Holdings Inc.", Sector.AI_CLOUD),
    "RKLB" to Brother("Rocket Lab USA Inc.", Sector.AI_CLOUD),
    "MARA" to Pair("MARA Holdings Inc.", Sector.AI_CLOUD),
    "CLSK" to Pair("CleanSpark Inc.", Sector.AI_CLOUD),
    "HUT" to Pair("Hut 8 Corp.", Sector.AI_CLOUD),
    "IREN" to Pair("Iris Energy Limited", Sector.AI_CLOUD),
    "WULF" to Pair("TeraWulf Inc.", Sector.AI_CLOUD),
    "BTBT" to Pair("Bit Digital Inc.", Sector.AI_CLOUD),
    "LCID" to Pair("Lucid Group Inc.", Sector.CONSUMER),
    "DASH" to Pair("DoorDash Inc.", Sector.CONSUMER),
    "DKNG" to Pair("DraftKings Inc.", Sector.CONSUMER),
    "PYPL" to Pair("PayPal Holdings Inc.", Sector.CONSUMER),
    "SHOP" to Pair("Shopify Inc.", Sector.AI_CLOUD),
    "ROKU" to Pair("Roku Inc.", Sector.CONSUMER),
    "DOCU" to Pair("DocuSign Inc.", Sector.AI_CLOUD),
    "ZM" to Pair("Zoom Video Communications", Sector.AI_CLOUD),
    "BIDU" to Pair("Baidu Inc.", Sector.AI_CLOUD),
    "MRVL" to Pair("Marvell Technology Inc.", Sector.SEMICONDUCTORS),
    "MU" to Pair("Micron Technology Inc.", Sector.SEMICONDUCTORS),
    "FTNT" to Pair("Fortinet Inc.", Sector.AI_CLOUD),
    "ZS" to Pair("Zscaler Inc.", Sector.AI_CLOUD),
    "MDB" to Pair("MongoDB Inc.", Sector.AI_CLOUD),
    "DDOG" to Pair("Datadog Inc.", Sector.AI_CLOUD),
    "OKTA" to Pair("Okta Inc.", Sector.AI_CLOUD),
    "TEAM" to Pair("Atlassian Corporation", Sector.AI_CLOUD),
    "CSCO" to Pair("Cisco Systems Inc.", Sector.MEGA_TECH),
    "PEP" to Pair("PepsiCo Inc.", Sector.CONSUMER),
    "ISRG" to Pair("Intuitive Surgical Inc.", Sector.CONSUMER),
    "AMAT" to Pair("Applied Materials Inc.", Sector.SEMICONDUCTORS),
    "LRCX" to Pair("Lam Research Corp.", Sector.SEMICONDUCTORS),
    "KLAC" to Pair("KLA Corporation", Sector.SEMICONDUCTORS),
    "SNPS" to Pair("Synopsys Inc.", Sector.AI_CLOUD),
    "CDNS" to Pair("Cadence Design Systems", Sector.AI_CLOUD),
    "SHFS" to Pair("SHF Holdings Inc.", Sector.CONSUMER),
    "XHLD" to Pair("Xcel Brands Inc.", Sector.CONSUMER),
    "WHLR" to Pair("Wheeler Real Estate Trust", Sector.CONSUMER),
    "BETR" to Pair("Better Home & Finance", Sector.CONSUMER),
    "PDSB" to Pair("PDS Biotechnology Corp.", Sector.CONSUMER),
    "BRLS" to Pair("Borealis Foods Inc.", Sector.CONSUMER),
    "INVE" to Pair("Identiv Inc.", Sector.SEMICONDUCTORS),
    "KPTI" to Pair("Karyopharm Therapeutics", Sector.CONSUMER),
    "ENVX" to Pair("Enovix Corporation", Sector.SEMICONDUCTORS),
    "APLD" to Pair("Applied Digital Corp.", Sector.AI_CLOUD),
    "MNDR" to Pair("Mobile Infrastructure", Sector.CONSUMER),
    "DJT" to Pair("Trump Media & Technology", Sector.CONSUMER),
    "IBKR" to Pair("Interactive Brokers Group", Sector.CONSUMER),
    "ASTS" to Pair("AST SpaceMobile Inc.", Sector.AI_CLOUD),
    "LUNR" to Pair("Intuitive Machines Inc.", Sector.AI_CLOUD)
  )

  private fun Brother(name: String, sector: Sector) = Pair(name, sector)

  private var cachedSymbols: List<String>? = null

  fun loadAllSymbols(context: Context?): List<String> {
    cachedSymbols?.let { return it }

    val loaded = mutableListOf<String>()
    if (context != null) {
      try {
        context.assets.open("nasdaq_symbols.txt").use { inputStream ->
          BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
            lines.forEach { line ->
              val trimmed = line.trim().uppercase()
              if (trimmed.isNotEmpty()) {
                loaded.add(trimmed)
              }
            }
          }
        }
      } catch (_: Exception) {
        // Fall through to known keys
      }
    }

    if (loaded.isEmpty()) {
      loaded.addAll(knownMetadata.keys)
    }

    // Ensure QQQ is always first
    if (loaded.contains("QQQ")) {
      loaded.remove("QQQ")
      loaded.add(0, "QQQ")
    }

    cachedSymbols = loaded
    return loaded
  }

  fun getBaseStock(symbol: String): BaseNasdaqStock {
    val sym = symbol.uppercase()
    val known = knownMetadata[sym]
    if (known != null) {
      val (name, sector) = known
      val basePrice = when (sym) {
        "QQQ" -> 510.45
        "AAPL" -> 235.80
        "NVDA" -> 142.60
        "MSFT" -> 428.15
        "AMZN" -> 194.50
        "GOOGL", "GOOG" -> 172.90
        "META" -> 592.40
        "TSLA" -> 228.30
        "PLTR" -> 44.50
        "COIN" -> 215.20
        "HOOD" -> 27.80
        "SOFI" -> 11.20
        "MSTR" -> 220.00
        "RIVN" -> 10.50
        "SMCI" -> 48.00
        "AMD" -> 156.80
        "NFLX" -> 755.60
        "COST" -> 915.20
        "QCOM" -> 168.90
        "ADBE" -> 498.30
        "ASML" -> 735.40
        "INTC" -> 22.75
        "ARM" -> 148.10
        "PANW" -> 372.50
        "CRWD" -> 320.00
        "RKLB" -> 14.80
        "DJT" -> 32.50
        "ASTS" -> 26.40
        else -> calculateDeterministicPrice(sym)
      }
      return BaseNasdaqStock(
        symbol = sym,
        name = name,
        sector = sector,
        basePrice = basePrice,
        marketCap = calculateMarketCap(sym, basePrice),
        peRatio = calculatePeRatio(sym),
        avgVolume = calculateVolume(sym)
      )
    }

    // Dynamic generation for any of the 3,429 symbols
    val hash = abs(sym.hashCode())
    val name = "$sym Corporation"
    val sector = when (hash % 4) {
      0 -> Sector.MEGA_TECH
      1 -> Sector.SEMICONDUCTORS
      2 -> Sector.AI_CLOUD
      else -> Sector.CONSUMER
    }
    val basePrice = calculateDeterministicPrice(sym)

    return BaseNasdaqStock(
      symbol = sym,
      name = name,
      sector = sector,
      basePrice = basePrice,
      marketCap = calculateMarketCap(sym, basePrice),
      peRatio = calculatePeRatio(sym),
      avgVolume = calculateVolume(sym)
    )
  }

  private fun calculateDeterministicPrice(sym: String): Double {
    val hash = abs(sym.hashCode())
    val raw = when {
      sym.length >= 5 && sym.endsWith("U") -> 10.0 + (hash % 100) / 100.0 // SPAC Unit
      sym.length >= 5 && sym.endsWith("W") -> 1.50 + (hash % 200) / 100.0 // Warrant
      hash % 10 < 3 -> 2.50 + (hash % 1500) / 100.0 // Micro-cap ($2.50 - $17.50)
      hash % 10 < 7 -> 20.0 + (hash % 8000) / 100.0 // Mid-cap ($20 - $100)
      else -> 100.0 + (hash % 25000) / 100.0 // Large-cap ($100 - $350)
    }
    return Math.round(raw * 100.0) / 100.0
  }

  private fun calculateMarketCap(sym: String, price: Double): String {
    val hash = abs(sym.hashCode())
    return when {
      price > 200 -> "$${(hash % 900 + 100)}B"
      price > 50 -> "$${(hash % 90 + 10)}B"
      price > 10 -> "$${(hash % 800 + 100)}M"
      else -> "$${(hash % 90 + 10)}M"
    }
  }

  private fun calculatePeRatio(sym: String): Double {
    val hash = abs(sym.hashCode())
    return Math.round((14.0 + (hash % 500) / 10.0) * 10.0) / 10.0
  }

  private fun calculateVolume(sym: String): Long {
    val hash = abs(sym.hashCode())
    return 200000L + (hash % 15000000L)
  }
}
