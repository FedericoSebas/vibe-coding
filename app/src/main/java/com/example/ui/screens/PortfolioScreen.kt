package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PortfolioPositionItem
import com.example.model.PortfolioSummary
import com.example.ui.theme.BlueElectric
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
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun PortfolioScreen(
  portfolioSummary: PortfolioSummary?,
  onStockClick: (String) -> Unit,
  onResetCapital: (Double) -> Unit,
  onExploreMarket: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showResetModal by remember { mutableStateOf(false) }

  val summary = portfolioSummary ?: PortfolioSummary(
    totalEquity = 100000.0,
    cashBalance = 100000.0,
    positionsValue = 0.0,
    totalUnrealizedPnL = 0.0,
    totalUnrealizedPnLPercent = 0.0,
    todayPnL = 0.0,
    todayPnLPercent = 0.0,
    startingCapital = 100000.0,
    positions = emptyList()
  )

  val isTotalGain = summary.totalUnrealizedPnL >= 0
  val totalReturnColor = if (isTotalGain) ProfitGreen else LossRed

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(NavyDarkest)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(NavyDark)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Simulated Portfolio",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )
        Text(
          text = "Live Net Worth & Risk Allocation",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }

      OutlinedButton(
        onClick = { showResetModal = true },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.testTag("reset_portfolio_button")
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Reset Capital", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // Main Net Worth Card
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = NavyDark),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "TOTAL SIMULATED EQUITY",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$${String.format(Locale.US, "%,.2f", summary.totalEquity)}",
              style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Total Return Row
            Row(verticalAlignment = Alignment.CenterVertically) {
              val sign = if (isTotalGain) "+" else ""
              Icon(
                imageVector = if (isTotalGain) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = totalReturnColor,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$sign$${String.format(Locale.US, "%,.2f", summary.totalUnrealizedPnL)} ($sign${String.format(Locale.US, "%.2f", summary.totalUnrealizedPnLPercent)}%) All-Time",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = totalReturnColor
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Breakdown (Cash vs Positions)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface, RoundedCornerShape(10.dp))
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Buying Power (Cash)", fontSize = 11.sp, color = TextSecondary)
                Text(
                  "$${String.format(Locale.US, "%,.2f", summary.cashBalance)}",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = CyanNeon
                )
              }
              Column {
                Text("Invested in Stocks", fontSize = 11.sp, color = TextSecondary)
                Text(
                  "$${String.format(Locale.US, "%,.2f", summary.positionsValue)}",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Today's P&L", fontSize = 11.sp, color = TextSecondary)
                val daySign = if (summary.todayPnL >= 0) "+" else ""
                val dayColor = if (summary.todayPnL >= 0) ProfitGreen else LossRed
                Text(
                  "$daySign$${String.format(Locale.US, "%,.2f", summary.todayPnL)}",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = dayColor
                )
              }
            }
          }
        }
      }

      // Asset Allocation Segmented Bar
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = NavyDark),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Portfolio Allocation",
              style = MaterialTheme.typography.titleSmall,
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val cashRatio = (summary.cashBalance / summary.totalEquity).toFloat().coerceIn(0f, 1f)
            val stockRatio = (summary.positionsValue / summary.totalEquity).toFloat().coerceIn(0f, 1f)

            // Segmented Bar
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(NavySurfaceVariant)
            ) {
              if (cashRatio > 0.01f) {
                Box(
                  modifier = Modifier
                    .weight(cashRatio.coerceAtLeast(0.001f))
                    .fillMaxSize()
                    .background(CyanNeon)
                )
              }
              if (stockRatio > 0.01f) {
                Box(
                  modifier = Modifier
                    .weight(stockRatio.coerceAtLeast(0.001f))
                    .fillMaxSize()
                    .background(GoldAccent)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyanNeon))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cash: ${(cashRatio * 100).toInt()}%", fontSize = 11.sp, color = TextSecondary)
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GoldAccent))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Equities: ${(stockRatio * 100).toInt()}%", fontSize = 11.sp, color = TextSecondary)
              }
            }
          }
        }
      }

      // Open Positions Title
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Active Positions (${summary.positions.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }
      }

      // Open Positions or Empty State
      if (summary.positions.isEmpty()) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = NavyDark),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(CircleShape)
                  .background(NavySurfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AccountBalanceWallet,
                  contentDescription = null,
                  tint = CyanNeon,
                  modifier = Modifier.size(28.dp)
                )
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No Open Positions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "You have $${String.format(Locale.US, "%,.2f", summary.cashBalance)} in simulated capital ready to trade.",
                fontSize = 12.sp,
                color = TextSecondary
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = onExploreMarket,
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("explore_nasdaq_stocks_button")
              ) {
                Text("Explore NASDAQ Stocks", color = NavyDarkest, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      } else {
        items(summary.positions, key = { it.ticker }) { position ->
          PositionListItemCard(
            position = position,
            onClick = { onStockClick(position.ticker) }
          )
        }
      }
    }
  }

  // Reset Capital Modal
  if (showResetModal) {
    ResetCapitalDialog(
      onDismiss = { showResetModal = false },
      onConfirm = { amount ->
        showResetModal = false
        onResetCapital(amount)
      }
    )
  }
}

@Composable
fun PositionListItemCard(
  position: PortfolioPositionItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isGain = position.unrealizedPnL >= 0
  val pnlColor = if (isGain) ProfitGreen else LossRed

  Card(
    colors = CardDefaults.cardColors(containerColor = NavyDark),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("position_item_${position.ticker}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = position.ticker,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${position.shares} shares",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }
          Text(
            text = position.companyName,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            maxLines = 1
          )
        }

        // Market Value & Unrealized P&L
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "$${String.format(Locale.US, "%,.2f", position.marketValue)}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
          val sign = if (isGain) "+" else ""
          Text(
            text = "$sign$${String.format(Locale.US, "%,.2f", position.unrealizedPnL)} ($sign${String.format(Locale.US, "%.2f", position.unrealizedPnLPercent)}%)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = pnlColor
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(NavySurface, RoundedCornerShape(8.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Avg Cost: $${String.format(Locale.US, "%.2f", position.averageCost)}", fontSize = 11.sp, color = TextSecondary)
        Text("Current: $${String.format(Locale.US, "%.2f", position.currentPrice)}", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Text("Invested: $${String.format(Locale.US, "%,.2f", position.totalInvested)}", fontSize = 11.sp, color = TextSecondary)
      }
    }
  }
}

@Composable
fun ResetCapitalDialog(
  onDismiss: () -> Unit,
  onConfirm: (Double) -> Unit
) {
  var selectedAmount by remember { mutableStateOf(100000.0) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = NavyDark,
    title = {
      Text(
        text = "Reset Paper Trading Capital",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary
      )
    },
    text = {
      Column {
        Text(
          text = "Select a simulated starting balance to test different educational strategies (e.g. small account vs institutional size):",
          fontSize = 13.sp,
          color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        listOf(10000.0, 50000.0, 100000.0, 250000.0).forEach { amount ->
          val isSelected = selectedAmount == amount
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) CyanNeon.copy(alpha = 0.15f) else NavySurface)
              .clickable { selectedAmount = amount }
              .padding(horizontal = 12.dp, vertical = 10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "$${String.format(Locale.US, "%,.0f", amount)}",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) CyanNeon else TextPrimary
              )
              if (isSelected) {
                Text("Selected", fontSize = 11.sp, color = CyanNeon, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Warning: This will clear existing simulated positions and trade records.",
          fontSize = 11.sp,
          color = LossRed
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(selectedAmount) },
        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("confirm_reset_capital_button")
      ) {
        Text("Reset to $${String.format(Locale.US, "%,.0f", selectedAmount)}", color = NavyDarkest, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    }
  )
}
