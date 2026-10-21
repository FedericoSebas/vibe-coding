package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Candle
import com.example.model.ChartType
import com.example.model.MarketDepthLevel
import com.example.model.PortfolioPositionItem
import com.example.model.StockQuote
import com.example.model.Timeframe
import com.example.ui.components.InteractiveChart
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedBg
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyDarkest
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.max

@Composable
fun StockDetailScreen(
  stock: StockQuote,
  candles: List<Candle>,
  marketDepth: List<MarketDepthLevel>,
  selectedTimeframe: Timeframe,
  chartType: ChartType,
  showSMA: Boolean,
  showVolume: Boolean,
  isWatchlisted: Boolean,
  userPosition: PortfolioPositionItem?,
  onBack: () -> Unit,
  onTimeframeSelect: (Timeframe) -> Unit,
  onChartTypeToggle: (ChartType) -> Unit,
  onToggleSMA: () -> Unit,
  onToggleVolume: () -> Unit,
  onToggleWatchlist: () -> Unit,
  onOpenTrade: (isBuy: Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  val isGain = stock.change >= 0
  val changeColor = if (isGain) ProfitGreen else LossRed

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(NavyDarkest)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(NavyDark)
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("detail_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = stock.symbol,
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = NavySurfaceVariant,
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = stock.sector.displayName,
                fontSize = 10.sp,
                color = CyanNeon,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = stock.name,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }
      }

      IconButton(
        onClick = onToggleWatchlist,
        modifier = Modifier.testTag("detail_star_watchlist")
      ) {
        Icon(
          imageVector = if (isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarOutline,
          contentDescription = "Watchlist",
          tint = if (isWatchlisted) GoldAccent else TextMuted
        )
      }
    }

    // Scrollable content
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
    ) {
      // Live Price & Stats Header
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(NavyDark)
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          Column {
            Text(
              text = "$${String.format(Locale.US, "%.2f", stock.price)}",
              style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            val sign = if (isGain) "+" else ""
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (isGain) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = changeColor,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$sign${String.format(Locale.US, "%.2f", stock.change)} ($sign${String.format(Locale.US, "%.2f", stock.changePercent)}%) Today",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = changeColor
              )
            }
          }

          // Bid / Ask Pill
          Surface(
            color = NavySurface,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(bottom = 4.dp)
          ) {
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
              Text(
                text = "Bid: $${String.format(Locale.US, "%.2f", stock.bid)}",
                fontSize = 11.sp,
                color = ProfitGreen
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Ask: $${String.format(Locale.US, "%.2f", stock.ask)}",
                fontSize = 11.sp,
                color = LossRed
              )
            }
          }
        }
      }

      // Timeframe Selector & Chart Controls
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(NavyDark)
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Timeframe buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Timeframe.values().forEach { tf ->
            val isSelected = selectedTimeframe == tf
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) CyanNeon else Color.Transparent)
                .clickable { onTimeframeSelect(tf) }
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .testTag("timeframe_${tf.label}"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = tf.label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) NavyDarkest else TextSecondary
              )
            }
          }
        }

        // Chart Type & Indicator Controls
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(NavySurfaceVariant)
              .clickable {
                onChartTypeToggle(if (chartType == ChartType.LINE) ChartType.CANDLESTICK else ChartType.LINE)
              }
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("chart_type_toggle"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (chartType == ChartType.LINE) "Candles" else "Line",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = CyanNeon
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (showSMA) GoldAccent.copy(alpha = 0.2f) else NavySurfaceVariant)
              .clickable { onToggleSMA() }
              .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "SMA 20",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (showSMA) GoldAccent else TextMuted
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (showVolume) CyanNeon.copy(alpha = 0.2f) else NavySurfaceVariant)
              .clickable { onToggleVolume() }
              .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Vol",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (showVolume) CyanNeon else TextMuted
            )
          }
        }
      }

      // Interactive Chart Canvas Component
      InteractiveChart(
        candles = candles,
        chartType = chartType,
        showSMA = showSMA,
        showVolume = showVolume,
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp)
          .background(NavyDark)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // User's Position Card (if owned)
      if (userPosition != null) {
        Card(
          colors = CardDefaults.cardColors(containerColor = NavyDark),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("user_position_card")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Your Position", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
              Text("${userPosition.shares} Shares Owned", fontSize = 12.sp, color = CyanNeon, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Avg Cost", fontSize = 11.sp, color = TextSecondary)
                Text("$${String.format(Locale.US, "%.2f", userPosition.averageCost)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
              }
              Column {
                Text("Market Value", fontSize = 11.sp, color = TextSecondary)
                Text("$${String.format(Locale.US, "%,.2f", userPosition.marketValue)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Unrealized Return", fontSize = 11.sp, color = TextSecondary)
                val retSign = if (userPosition.unrealizedPnL >= 0) "+" else ""
                val retColor = if (userPosition.unrealizedPnL >= 0) ProfitGreen else LossRed
                Text(
                  "$retSign$${String.format(Locale.US, "%,.2f", userPosition.unrealizedPnL)} ($retSign${String.format(Locale.US, "%.2f", userPosition.unrealizedPnLPercent)}%)",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = retColor
                )
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }

      // Level 2 / Order Book Depth Ladder
      Card(
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Level 2 Order Book (Depth)",
              style = MaterialTheme.typography.titleSmall,
              color = TextPrimary
            )
            Text(
              text = "Spread: $${String.format(Locale.US, "%.2f", stock.ask - stock.bid)}",
              fontSize = 11.sp,
              color = GoldAccent
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(modifier = Modifier.fillMaxWidth()) {
            // Bid side (Buyers)
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "BID (BUYERS)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ProfitGreen
              )
              Spacer(modifier = Modifier.height(4.dp))
              marketDepth.forEach { level ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("${level.bidSize}", fontSize = 11.sp, color = TextSecondary)
                  Text("$${String.format(Locale.US, "%.2f", level.bidPrice)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ProfitGreen)
                }
              }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Ask side (Sellers)
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "ASK (SELLERS)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LossRed
              )
              Spacer(modifier = Modifier.height(4.dp))
              marketDepth.forEach { level ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("$${String.format(Locale.US, "%.2f", level.askPrice)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LossRed)
                  Text("${level.askSize}", fontSize = 11.sp, color = TextSecondary)
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Key Financial Stats Grid
      Card(
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Market Statistics",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(10.dp))

          val stats = listOf(
            "Market Cap" to stock.marketCap,
            "P/E Ratio" to "${stock.peRatio}x",
            "Day Range" to "$${String.format(Locale.US, "%.2f", stock.dayLow)} - $${String.format(Locale.US, "%.2f", stock.dayHigh)}",
            "Day Volume" to String.format(Locale.US, "%,d", stock.volume),
            "Prev Close" to "$${String.format(Locale.US, "%.2f", stock.previousClose)}",
            "Spread" to "$${String.format(Locale.US, "%.2f", stock.ask - stock.bid)}"
          )

          stats.chunked(2).forEach { rowStats ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              rowStats.forEach { (label, value) ->
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = label, fontSize = 11.sp, color = TextSecondary)
                  Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Educational Insight Card for this Stock
      Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Educational Trading Context",
              style = MaterialTheme.typography.titleSmall,
              color = CyanNeon
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "${stock.symbol} is a leading constituent in the NASDAQ-100 ${stock.sector.displayName} sector with a valuation of ${stock.marketCap}. With a P/E ratio of ${stock.peRatio}x, investors are pricing in future technological moat and cash flow expansion. In paper trading, test setting Limit Orders near recent support to avoid paying market ask premiums.",
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }

    // Bottom Action Bar: BUY and SELL
    Surface(
      color = NavyDark,
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Button(
          onClick = { onOpenTrade(false) },
          colors = ButtonDefaults.buttonColors(containerColor = LossRed),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("detail_sell_button")
        ) {
          Text("Sell ${stock.symbol}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
        }

        Button(
          onClick = { onOpenTrade(true) },
          colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1.3f)
            .height(48.dp)
            .testTag("detail_buy_button")
        ) {
          Text("Buy ${stock.symbol}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyDarkest)
        }
      }
    }
  }
}
