package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Sector
import com.example.model.StockQuote
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
fun MarketScreen(
  quotes: Map<String, StockQuote>,
  watchlist: Set<String>,
  selectedSector: Sector,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  onSectorSelect: (Sector) -> Unit,
  onStockClick: (String) -> Unit,
  onToggleWatchlist: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showOnlyWatchlist by remember { mutableStateOf(false) }

  val qqqQuote = quotes["QQQ"]

  // Filter list
  val filteredQuotes = quotes.values.filter { stock ->
    val matchesSearch = stock.symbol.contains(searchQuery, ignoreCase = true) ||
      stock.name.contains(searchQuery, ignoreCase = true)
    val matchesSector = selectedSector == Sector.ALL || stock.sector == selectedSector
    val matchesWatchlist = !showOnlyWatchlist || watchlist.contains(stock.symbol)
    matchesSearch && matchesSector && matchesWatchlist
  }.sortedByDescending { it.volume }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(NavyDarkest)
  ) {
    // Header & Benchmark Banner
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(NavyDark)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "NASDAQ Live Markets",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
          Text(
            text = "3,400+ stocks • Real-Time Paper Trading",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        Surface(
          color = ProfitGreenBg,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, ProfitGreen.copy(alpha = 0.4f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(ProfitGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "LIVE FEED",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = ProfitGreen
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // QQQ Benchmark Card
      if (qqqQuote != null) {
        Card(
          colors = CardDefaults.cardColors(containerColor = NavySurface),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onStockClick("QQQ") }
            .testTag("qqq_banner_card")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "QQQ",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "NASDAQ-100 Benchmark",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Vol: ${String.format(Locale.US, "%,d", qqqQuote.volume)}",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }

            // Sparkline
            MiniSparkline(
              points = qqqQuote.sparkline,
              isPositive = qqqQuote.change >= 0,
              modifier = Modifier
                .width(68.dp)
                .height(28.dp)
            )

            // Price & Change
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "$${String.format(Locale.US, "%.2f", qqqQuote.price)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
              )
              val sign = if (qqqQuote.change >= 0) "+" else ""
              val changeColor = if (qqqQuote.change >= 0) ProfitGreen else LossRed
              Text(
                text = "$sign${String.format(Locale.US, "%.2f", qqqQuote.change)} ($sign${String.format(Locale.US, "%.2f", qqqQuote.changePercent)}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = changeColor
              )
            }
          }
        }
      }
    }

    // Search input
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Search 3,400+ tickers (e.g. SHFS, PLTR, WHLR, NVDA)...", color = TextMuted, fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("stock_search_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CyanNeon,
          unfocusedBorderColor = NavyBorder,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedContainerColor = NavyDark,
          unfocusedContainerColor = NavyDark
        ),
        shape = RoundedCornerShape(10.dp)
      )
    }

    // Sector Filter Chips & Watchlist Toggle
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(bottom = 8.dp)
    ) {
      item {
        FilterChip(
          selected = showOnlyWatchlist,
          onClick = { showOnlyWatchlist = !showOnlyWatchlist },
          label = { Text("★ Watchlist (${watchlist.size})", fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GoldAccent.copy(alpha = 0.2f),
            selectedLabelColor = GoldAccent,
            containerColor = NavyDark,
            labelColor = TextSecondary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = showOnlyWatchlist,
            borderColor = if (showOnlyWatchlist) GoldAccent else NavyBorder
          ),
          modifier = Modifier.testTag("filter_chip_watchlist")
        )
      }

      items(Sector.values()) { sector ->
        val isSelected = !showOnlyWatchlist && selectedSector == sector
        FilterChip(
          selected = isSelected,
          onClick = {
            showOnlyWatchlist = false
            onSectorSelect(sector)
          },
          label = { Text(sector.displayName, fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyanNeon.copy(alpha = 0.15f),
            selectedLabelColor = CyanNeon,
            containerColor = NavyDark,
            labelColor = TextSecondary
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) CyanNeon else NavyBorder
          ),
          modifier = Modifier.testTag("filter_sector_${sector.name}")
        )
      }
    }

    // Stock List
    LazyColumn(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      if (filteredQuotes.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = if (showOnlyWatchlist) "Your Watchlist is empty" else "No matching NASDAQ stocks found",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (showOnlyWatchlist) "Tap the star icon on any stock to add it here." else "Try searching by symbol or select another sector.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
              )
            }
          }
        }
      } else {
        items(filteredQuotes, key = { it.symbol }) { stock ->
          StockListItemCard(
            stock = stock,
            isWatchlisted = watchlist.contains(stock.symbol),
            onStockClick = { onStockClick(stock.symbol) },
            onToggleWatchlist = { onToggleWatchlist(stock.symbol) }
          )
        }
      }
    }
  }
}

@Composable
fun StockListItemCard(
  stock: StockQuote,
  isWatchlisted: Boolean,
  onStockClick: () -> Unit,
  onToggleWatchlist: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isGain = stock.change >= 0
  val changeColor = if (isGain) ProfitGreen else LossRed

  Card(
    colors = CardDefaults.cardColors(containerColor = NavyDark),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onStockClick)
      .testTag("stock_item_${stock.symbol}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Star toggle
      IconButton(
        onClick = onToggleWatchlist,
        modifier = Modifier
          .size(32.dp)
          .testTag("star_watchlist_${stock.symbol}")
      ) {
        Icon(
          imageVector = if (isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarOutline,
          contentDescription = "Watchlist",
          tint = if (isWatchlisted) GoldAccent else TextMuted,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Ticker & Company Name
      Column(modifier = Modifier.weight(1.1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = stock.symbol,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = stock.marketCap,
            fontSize = 10.sp,
            color = TextMuted
          )
        }
        Text(
          text = stock.name,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          maxLines = 1
        )
      }

      // Sparkline
      MiniSparkline(
        points = stock.sparkline,
        isPositive = isGain,
        modifier = Modifier
          .width(56.dp)
          .height(24.dp)
          .padding(horizontal = 4.dp)
      )

      Spacer(modifier = Modifier.width(8.dp))

      // Price & % Change Pill
      Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.width(96.dp)
      ) {
        Text(
          text = "$${String.format(Locale.US, "%.2f", stock.price)}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
          color = if (isGain) ProfitGreenBg else LossRedBg,
          shape = RoundedCornerShape(6.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isGain) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
              contentDescription = null,
              tint = changeColor,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            val sign = if (isGain) "+" else ""
            Text(
              text = "$sign${String.format(Locale.US, "%.2f", stock.changePercent)}%",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = changeColor
            )
          }
        }
      }
    }
  }
}

@Composable
fun MiniSparkline(
  points: List<Double>,
  isPositive: Boolean,
  modifier: Modifier = Modifier
) {
  val lineColor = if (isPositive) ProfitGreen else LossRed

  Canvas(modifier = modifier) {
    if (points.size < 2) return@Canvas

    val width = size.width
    val height = size.height

    val minP = points.minOrNull() ?: 0.0
    val maxP = points.maxOrNull() ?: 1.0
    val range = max(0.01, maxP - minP)

    val path = Path()
    val firstY = height - ((points.first() - minP) / range * height).toFloat()
    path.moveTo(0f, firstY)

    for (i in 1 until points.size) {
      val x = (i.toFloat() / (points.size - 1)) * width
      val y = height - ((points[i] - minP) / range * height).toFloat()
      path.lineTo(x, y)
    }

    drawPath(
      path = path,
      color = lineColor,
      style = Stroke(width = 1.5.dp.toPx())
    )
  }
}
