package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrderEntity
import com.example.model.PortfolioSummary
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
import kotlin.math.floor
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderExecutionSheet(
  stock: StockQuote,
  initialIsBuy: Boolean = true,
  portfolioSummary: PortfolioSummary?,
  onDismiss: () -> Unit,
  onExecuteTrade: (ticker: String, companyName: String, isBuy: Boolean, orderType: String, shares: Double, limitPrice: Double?) -> Unit
) {
  var isBuy by remember { mutableStateOf(initialIsBuy) }
  var orderType by remember { mutableStateOf("MARKET") } // MARKET or LIMIT
  var sharesText by remember { mutableStateOf("10") }
  var limitPriceText by remember(stock.price) {
    mutableStateOf(String.format(Locale.US, "%.2f", stock.price))
  }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val cashAvailable = portfolioSummary?.cashBalance ?: 100000.0
  val currentPosition = portfolioSummary?.positions?.find { it.ticker == stock.symbol }
  val sharesOwned = currentPosition?.shares ?: 0.0

  val sharesNum = sharesText.toDoubleOrNull() ?: 0.0
  val activePrice = if (isBuy) stock.ask else stock.bid
  val targetPrice = if (orderType == "LIMIT") {
    limitPriceText.toDoubleOrNull() ?: activePrice
  } else {
    activePrice
  }

  val estimatedTotal = sharesNum * targetPrice

  val hasSufficientBalance = if (isBuy) {
    cashAvailable >= estimatedTotal && estimatedTotal > 0
  } else {
    sharesOwned >= sharesNum && sharesNum > 0
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = NavyDark,
    contentColor = TextPrimary,
    dragHandle = {
      Box(
        modifier = Modifier
          .padding(vertical = 10.dp)
          .width(40.dp)
          .height(4.dp)
          .clip(CircleShape)
          .background(NavyBorder)
      )
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      // Stock header & close button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = stock.symbol,
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              color = NavySurfaceVariant,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "NASDAQ",
                color = CyanNeon,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
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

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Buy vs Sell Toggle
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(NavySurface)
          .padding(4.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isBuy) ProfitGreen else Color.Transparent)
            .clickable { isBuy = true }
            .padding(vertical = 10.dp)
            .testTag("trade_toggle_buy"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "BUY",
            fontWeight = FontWeight.Bold,
            color = if (isBuy) NavyDarkest else TextSecondary
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (!isBuy) LossRed else Color.Transparent)
            .clickable { isBuy = false }
            .padding(vertical = 10.dp)
            .testTag("trade_toggle_sell"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "SELL",
            fontWeight = FontWeight.Bold,
            color = if (!isBuy) TextPrimary else TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Order Type Selector: Market vs Limit
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("MARKET" to "Market Order", "LIMIT" to "Limit Order").forEach { (typeKey, label) ->
          val isSelected = orderType == typeKey
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .border(
                width = 1.dp,
                color = if (isSelected) CyanNeon else NavyBorder,
                shape = RoundedCornerShape(8.dp)
              )
              .background(if (isSelected) CyanNeon.copy(alpha = 0.12f) else NavySurfaceVariant)
              .clickable { orderType = typeKey }
              .padding(vertical = 8.dp)
              .testTag("order_type_$typeKey"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
              color = if (isSelected) CyanNeon else TextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Price Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (orderType == "MARKET") "Current ${if (isBuy) "Ask" else "Bid"} Price" else "Limit Target Price",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )
        if (orderType == "MARKET") {
          Text(
            text = "$${String.format(Locale.US, "%.2f", targetPrice)}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
          )
        } else {
          OutlinedTextField(
            value = limitPriceText,
            onValueChange = { limitPriceText = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .width(130.dp)
              .testTag("limit_price_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanNeon,
              unfocusedBorderColor = NavyBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedContainerColor = NavySurfaceVariant,
              unfocusedContainerColor = NavySurfaceVariant
            ),
            prefix = { Text("$", color = TextSecondary) }
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Share quantity input with stepper
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Number of Shares",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = {
              val current = sharesText.toDoubleOrNull() ?: 0.0
              if (current > 1) {
                sharesText = (current - 1).toInt().toString()
              }
            },
            modifier = Modifier
              .size(36.dp)
              .background(NavySurfaceVariant, CircleShape)
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary, modifier = Modifier.size(16.dp))
          }

          OutlinedTextField(
            value = sharesText,
            onValueChange = { sharesText = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .width(90.dp)
              .padding(horizontal = 6.dp)
              .testTag("shares_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanNeon,
              unfocusedBorderColor = NavyBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary,
              focusedContainerColor = NavySurfaceVariant,
              unfocusedContainerColor = NavySurfaceVariant
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
          )

          IconButton(
            onClick = {
              val current = sharesText.toDoubleOrNull() ?: 0.0
              sharesText = (current + 1).toInt().toString()
            },
            modifier = Modifier
              .size(36.dp)
              .background(NavySurfaceVariant, CircleShape)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextPrimary, modifier = Modifier.size(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Quick percentage sizing chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(25, 50, 75, 100).forEach { pct ->
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(NavySurfaceVariant)
              .clickable {
                if (isBuy) {
                  val allocatableCash = cashAvailable * (pct / 100.0)
                  val calcShares = floor(allocatableCash / targetPrice).coerceAtLeast(1.0)
                  sharesText = calcShares.toInt().toString()
                } else {
                  val calcShares = floor(sharesOwned * (pct / 100.0)).coerceAtLeast(0.0)
                  sharesText = calcShares.toInt().toString()
                }
              }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (pct == 100) "MAX" else "$pct%",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = TextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Balance & Summary Row
      Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = if (isBuy) "Available Simulated Cash" else "Shares Currently Owned",
              fontSize = 12.sp,
              color = TextSecondary
            )
            Text(
              text = if (isBuy) "$${String.format(Locale.US, "%,.2f", cashAvailable)}" else "$sharesOwned shares",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = if (isBuy) "Estimated Total Cost" else "Estimated Proceeds",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )
            Text(
              text = "$${String.format(Locale.US, "%,.2f", estimatedTotal)}",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = if (isBuy) ProfitGreen else CyanNeon
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Educational Mechanics Explainer Box
      Card(
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Educational tip",
            tint = GoldAccent,
            modifier = Modifier
              .size(18.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (orderType == "MARKET") {
              if (isBuy) {
                "Educational Tip: Market Buy crosses the spread to buy at the current Ask ($${String.format(Locale.US, "%.2f", stock.ask)}). Real-world markets can experience slippage."
              } else {
                "Educational Tip: Market Sell executes at current Bid ($${String.format(Locale.US, "%.2f", stock.bid)}). Zero brokerage fees simulated."
              }
            } else {
              if (isBuy) {
                "Educational Tip: Buy Limit ($$limitPriceText) guarantees you pay no higher than this price. Funds are reserved until filled or cancelled."
              } else {
                "Educational Tip: Sell Limit ($$limitPriceText) guarantees you sell at or above this price. Order will trigger automatically."
              }
            },
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Confirm Order CTA Button
      Button(
        onClick = {
          if (sharesNum > 0 && hasSufficientBalance) {
            onExecuteTrade(
              stock.symbol,
              stock.name,
              isBuy,
              orderType,
              sharesNum,
              if (orderType == "LIMIT") targetPrice else null
            )
            onDismiss()
          }
        },
        enabled = hasSufficientBalance && sharesNum > 0,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_trade_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isBuy) ProfitGreen else LossRed,
          disabledContainerColor = NavySurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(
          text = if (!hasSufficientBalance) {
            if (isBuy) "Insufficient Cash ($${String.format(Locale.US, "%,.2f", estimatedTotal)} required)" else "Not Enough Shares"
          } else {
            "Confirm ${if (isBuy) "Buy" else "Sell"} ($${String.format(Locale.US, "%,.2f", estimatedTotal)})"
          },
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = if (hasSufficientBalance) NavyDarkest else TextMuted
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun TradeConfirmationDialog(
  order: OrderEntity,
  onDismiss: () -> Unit
) {
  val isBuy = order.orderType.contains("BUY")
  val isLimit = order.orderType.contains("LIMIT")

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = NavyDark,
    titleContentColor = TextPrimary,
    textContentColor = TextSecondary,
    icon = {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(if (isBuy) ProfitGreenBg else CyanNeon.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Success",
          tint = if (isBuy) ProfitGreen else CyanNeon,
          modifier = Modifier.size(36.dp)
        )
      }
    },
    title = {
      Text(
        text = if (isLimit) "Limit Order Placed!" else "Order Filled!",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "${if (isBuy) "Bought" else "Sold"} ${order.shares} shares of ${order.ticker}",
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
          colors = CardDefaults.cardColors(containerColor = NavySurface),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Order Type", fontSize = 12.sp, color = TextSecondary)
              Text(order.orderType, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CyanNeon)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(if (isLimit) "Target Limit Price" else "Executed Fill Price", fontSize = 12.sp, color = TextSecondary)
              Text("$${String.format(Locale.US, "%.2f", order.executionPrice ?: order.limitPrice ?: 0.0)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Total Value", fontSize = 12.sp, color = TextSecondary)
              Text("$${String.format(Locale.US, "%,.2f", order.totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isBuy) ProfitGreen else GoldAccent)
            }
            if (order.realizedPnL != 0.0) {
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Realized Gain/Loss", fontSize = 12.sp, color = TextSecondary)
                val sign = if (order.realizedPnL >= 0) "+" else ""
                Text(
                  "$sign$${String.format(Locale.US, "%,.2f", order.realizedPnL)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (order.realizedPnL >= 0) ProfitGreen else LossRed
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = if (isLimit) {
            "Educational note: Limit orders remain in your Active Orders tab until market conditions reach your price target."
          } else {
            "Educational note: Market orders take immediate liquidity from the book. Your portfolio metrics have been updated."
          },
          fontSize = 11.sp,
          color = TextMuted,
          textAlign = TextAlign.Center
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dismiss_confirmation_button"),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Done", color = NavyDarkest, fontWeight = FontWeight.Bold)
      }
    }
  )
}
