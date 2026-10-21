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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.OrderEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
  orders: List<OrderEntity>,
  pendingOrders: List<OrderEntity>,
  quotes: Map<String, StockQuote>,
  onCancelOrder: (Long) -> Unit,
  onStockClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val filledOrders = orders.filter { it.status == "FILLED" }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(NavyDarkest)
  ) {
    // Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(NavyDark)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Text(
        text = "Trade Orders & Journal",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary
      )
      Text(
        text = "Track active pending limit orders and execution records",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )
    }

    // Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = NavyDark,
      contentColor = CyanNeon,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = CyanNeon
        )
      }
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Active Limit Orders",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedTab == 0) CyanNeon else TextSecondary
            )
            if (pendingOrders.isNotEmpty()) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                color = GoldAccent,
                shape = CircleShape
              ) {
                Text(
                  text = "${pendingOrders.size}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavyDarkest,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
              }
            }
          }
        },
        modifier = Modifier.testTag("tab_pending_orders")
      )

      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text(
            text = "Execution History (${filledOrders.size})",
            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
            color = if (selectedTab == 1) CyanNeon else TextSecondary
          )
        },
        modifier = Modifier.testTag("tab_execution_history")
      )
    }

    // Tab Content
    if (selectedTab == 0) {
      // Pending Limit Orders List
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        if (pendingOrders.isEmpty()) {
          item {
            EmptyOrdersCard(
              title = "No Pending Limit Orders",
              description = "When you place a Limit Order, it stays queued here until the market price reaches your specified target."
            )
          }
        } else {
          items(pendingOrders, key = { it.id }) { order ->
            PendingOrderItemCard(
              order = order,
              currentQuote = quotes[order.ticker],
              onCancel = { onCancelOrder(order.id) },
              onStockClick = { onStockClick(order.ticker) }
            )
          }
        }
      }
    } else {
      // Execution History List
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        if (filledOrders.isEmpty()) {
          item {
            EmptyOrdersCard(
              title = "No Trade History Yet",
              description = "Executed trades and real-time paper order fills will appear here with timestamps, prices, and realized gains/losses."
            )
          }
        } else {
          items(filledOrders, key = { it.id }) { order ->
            FilledOrderItemCard(
              order = order,
              onStockClick = { onStockClick(order.ticker) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun PendingOrderItemCard(
  order: OrderEntity,
  currentQuote: StockQuote?,
  onCancel: () -> Unit,
  onStockClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isBuy = order.orderType.contains("BUY")
  val currentPrice = currentQuote?.price ?: 0.0

  Card(
    colors = CardDefaults.cardColors(containerColor = NavyDark),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onStockClick)
      .testTag("pending_order_${order.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = if (isBuy) ProfitGreenBg else LossRedBg,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = order.orderType.replace("_", " "),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isBuy) ProfitGreen else LossRed,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = order.ticker,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }

        OutlinedButton(
          onClick = onCancel,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = LossRed),
          shape = RoundedCornerShape(6.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.testTag("cancel_order_${order.id}")
        ) {
          Text("Cancel", fontSize = 11.sp)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("Shares", fontSize = 11.sp, color = TextSecondary)
          Text("${order.shares}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        Column {
          Text("Limit Price", fontSize = 11.sp, color = TextSecondary)
          Text("$${String.format(Locale.US, "%.2f", order.limitPrice ?: 0.0)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CyanNeon)
        }
        Column {
          Text("Current Market", fontSize = 11.sp, color = TextSecondary)
          Text("$${String.format(Locale.US, "%.2f", currentPrice)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        Column(horizontalAlignment = Alignment.End) {
          Text("Reserved", fontSize = 11.sp, color = TextSecondary)
          Text("$${String.format(Locale.US, "%,.2f", order.totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GoldAccent)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = if (isBuy) {
          "Condition: Executes when market Ask drops to $${String.format(Locale.US, "%.2f", order.limitPrice ?: 0.0)} or lower"
        } else {
          "Condition: Executes when market Bid reaches $${String.format(Locale.US, "%.2f", order.limitPrice ?: 0.0)} or higher"
        },
        fontSize = 11.sp,
        color = TextMuted
      )
    }
  }
}

@Composable
fun FilledOrderItemCard(
  order: OrderEntity,
  onStockClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isBuy = order.orderType.contains("BUY")
  val timeFormatted = remember(order.timestamp) {
    SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.US).format(Date(order.timestamp))
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = NavyDark),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onStockClick)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = if (isBuy) ProfitGreenBg else LossRedBg,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = order.orderType.replace("_", " "),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isBuy) ProfitGreen else LossRed,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = order.ticker,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        }

        Text(
          text = "$${String.format(Locale.US, "%,.2f", order.totalAmount)}",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "${order.shares} shares @ $${String.format(Locale.US, "%.2f", order.executionPrice ?: 0.0)}",
          fontSize = 12.sp,
          color = TextSecondary
        )

        if (order.realizedPnL != 0.0) {
          val sign = if (order.realizedPnL >= 0) "+" else ""
          val pnlColor = if (order.realizedPnL >= 0) ProfitGreen else LossRed
          Text(
            text = "Realized: $sign$${String.format(Locale.US, "%,.2f", order.realizedPnL)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = pnlColor
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = timeFormatted,
        fontSize = 11.sp,
        color = TextMuted
      )
    }
  }
}

@Composable
fun EmptyOrdersCard(
  title: String,
  description: String
) {
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
          .size(48.dp)
          .clip(CircleShape)
          .background(NavySurfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ReceiptLong,
          contentDescription = null,
          tint = CyanNeon,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = description,
        fontSize = 12.sp,
        color = TextSecondary,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
    }
  }
}
