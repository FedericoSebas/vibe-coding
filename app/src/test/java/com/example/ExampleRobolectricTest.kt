package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.network.NasdaqMarketDataSource
import com.example.data.network.NasdaqStockCatalog
import com.example.model.Sector
import com.example.model.StockQuote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nasdaq Sim", appName)
  }

  @Test
  fun `verify simulated stock quote calculations`() {
    val quote = StockQuote(
      symbol = "NVDA",
      name = "NVIDIA Corporation",
      sector = Sector.SEMICONDUCTORS,
      price = 140.0,
      previousClose = 136.0,
      change = 4.0,
      changePercent = 2.94,
      dayHigh = 142.0,
      dayLow = 135.5,
      volume = 45000000L,
      peRatio = 58.2,
      marketCap = "$3.4T",
      bid = 139.98,
      ask = 140.02
    )

    assertEquals("NVDA", quote.symbol)
    assertTrue(quote.ask > quote.bid)
    assertTrue(quote.price > quote.previousClose)
  }

  @Test
  fun `verify 3400+ nasdaq symbols catalog and lookup`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val symbols = NasdaqStockCatalog.loadAllSymbols(context)
    assertTrue("Should load more than 3000 symbols", symbols.size > 3000)

    // Check specific symbols requested by user
    assertTrue(symbols.contains("SHFS"))
    assertTrue(symbols.contains("XHLD"))
    assertTrue(symbols.contains("WHLR"))
    assertTrue(symbols.contains("PLTR"))
    assertTrue(symbols.contains("AFNXU"))

    val shfsStock = NasdaqStockCatalog.getBaseStock("SHFS")
    assertEquals("SHFS", shfsStock.symbol)
    assertTrue(shfsStock.basePrice > 0)

    val dataSource = NasdaqMarketDataSource(context)
    val quote = dataSource.getOrCreateQuote("SHFS")
    assertEquals("SHFS", quote.symbol)
    assertTrue(quote.price > 0)
    assertTrue(quote.ask >= quote.bid)
  }
}
