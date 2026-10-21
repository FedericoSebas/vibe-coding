package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Candle
import com.example.model.ChartType
import com.example.ui.theme.ChartCyanEnd
import com.example.ui.theme.ChartCyanStart
import com.example.ui.theme.ChartGreenEnd
import com.example.ui.theme.ChartGreenStart
import com.example.ui.theme.ChartRedEnd
import com.example.ui.theme.ChartRedStart
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun InteractiveChart(
  candles: List<Candle>,
  chartType: ChartType,
  showSMA: Boolean,
  showVolume: Boolean,
  modifier: Modifier = Modifier
) {
  if (candles.isEmpty()) {
    Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "Loading real-time chart data...",
        color = TextSecondary,
        style = MaterialTheme.typography.bodyMedium
      )
    }
    return
  }

  var selectedCandleIndex by remember(candles) { mutableStateOf<Int?>(null) }
  val activeCandle = selectedCandleIndex?.let { candles.getOrNull(it) } ?: candles.lastOrNull()

  val firstClose = candles.first().close
  val currentClose = activeCandle?.close ?: candles.last().close
  val isPositive = currentClose >= firstClose

  val lineColor = if (chartType == ChartType.LINE) {
    if (isPositive) ProfitGreen else LossRed
  } else CyanNeon

  val gradientBrush = Brush.verticalGradient(
    colors = if (isPositive) {
      listOf(ChartGreenStart, ChartGreenEnd)
    } else {
      listOf(ChartRedStart, ChartRedEnd)
    }
  )

  Column(modifier = modifier) {
    // Top active scrubber badge
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (activeCandle != null) {
        val dateStr = remember(activeCandle.timestamp) {
          SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(activeCandle.timestamp))
        }
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "$${String.format(Locale.US, "%.2f", activeCandle.close)}",
              color = TextPrimary,
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val delta = activeCandle.close - firstClose
            val pct = if (firstClose > 0) (delta / firstClose) * 100 else 0.0
            val deltaSign = if (delta >= 0) "+" else ""
            Text(
              text = "$deltaSign${String.format(Locale.US, "%.2f", delta)} ($deltaSign${String.format(Locale.US, "%.2f", pct)}%)",
              color = if (delta >= 0) ProfitGreen else LossRed,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }

          if (chartType == ChartType.CANDLESTICK) {
            Row(modifier = Modifier.padding(top = 2.dp)) {
              Text(
                text = "O: ${String.format(Locale.US, "%.2f", activeCandle.open)}  H: ${String.format(Locale.US, "%.2f", activeCandle.high)}  L: ${String.format(Locale.US, "%.2f", activeCandle.low)}",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }

          Text(
            text = dateStr,
            color = TextMuted,
            fontSize = 11.sp
          )
        }
      }
    }

    // Canvas chart area
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("interactive_chart_canvas")
        .pointerInput(candles) {
          detectTapGestures(
            onTap = { offset ->
              val total = candles.size
              if (total > 0) {
                val index = (offset.x / size.width * total).toInt().coerceIn(0, total - 1)
                selectedCandleIndex = index
              }
            }
          )
        }
        .pointerInput(candles) {
          detectDragGestures(
            onDragStart = { offset ->
              val total = candles.size
              if (total > 0) {
                val index = (offset.x / size.width * total).toInt().coerceIn(0, total - 1)
                selectedCandleIndex = index
              }
            },
            onDrag = { change, _ ->
              change.consume()
              val total = candles.size
              if (total > 0) {
                val index = (change.position.x / size.width * total).toInt().coerceIn(0, total - 1)
                selectedCandleIndex = index
              }
            },
            onDragEnd = {
              selectedCandleIndex = null
            },
            onDragCancel = {
              selectedCandleIndex = null
            }
          )
        }
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val volumeHeight = if (showVolume) height * 0.22f else 0f
        val priceHeight = height - volumeHeight - 20f

        val minPrice = candles.minOf { it.low } * 0.999
        val maxPrice = candles.maxOf { it.high } * 1.001
        val priceRange = max(0.01, maxPrice - minPrice)

        val maxVolume = candles.maxOfOrNull { it.volume } ?: 1L

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
          val y = priceHeight * (i.toFloat() / gridLines)
          drawLine(
            color = NavyBorder.copy(alpha = 0.5f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
          )
        }

        // Draw Volume Histogram if enabled
        if (showVolume && maxVolume > 0) {
          val barWidth = (width / candles.size) * 0.7f
          candles.forEachIndexed { i, candle ->
            val x = (i.toFloat() / (candles.size - 1).coerceAtLeast(1)) * width
            val volRatio = (candle.volume.toFloat() / maxVolume.toFloat()).coerceIn(0.05f, 1f)
            val barH = volumeHeight * volRatio
            val volColor = if (candle.close >= candle.open) ProfitGreen.copy(alpha = 0.35f) else LossRed.copy(alpha = 0.35f)
            drawRect(
              color = volColor,
              topLeft = Offset(x - barWidth / 2, height - barH),
              size = Size(barWidth, barH)
            )
          }
        }

        // Draw Candlestick or Line Chart
        if (chartType == ChartType.CANDLESTICK) {
          drawCandlesticks(candles, minPrice, priceRange, priceHeight, width)
        } else {
          drawLineChart(candles, minPrice, priceRange, priceHeight, width, lineColor, gradientBrush)
        }

        // Draw SMA 20 Overlay
        if (showSMA && candles.size >= 5) {
          drawMovingAverage(candles, minPrice, priceRange, priceHeight, width, period = 10, color = GoldAccent)
        }

        // Draw Crosshair Scrubber
        selectedCandleIndex?.let { idx ->
          if (idx in candles.indices) {
            val x = (idx.toFloat() / (candles.size - 1).coerceAtLeast(1)) * width
            val candle = candles[idx]
            val y = priceHeight - ((candle.close - minPrice) / priceRange * priceHeight).toFloat()

            // Vertical crosshair
            drawLine(
              color = TextPrimary.copy(alpha = 0.7f),
              start = Offset(x, 0f),
              end = Offset(x, height),
              strokeWidth = 1.5.dp.toPx()
            )

            // Horizontal crosshair
            drawLine(
              color = TextPrimary.copy(alpha = 0.4f),
              start = Offset(0f, y),
              end = Offset(width, y),
              strokeWidth = 1.dp.toPx()
            )

            // Highlight point
            drawCircle(
              color = CyanNeon,
              radius = 5.dp.toPx(),
              center = Offset(x, y)
            )
            drawCircle(
              color = Color.White,
              radius = 2.5.dp.toPx(),
              center = Offset(x, y)
            )
          }
        }
      }
    }
  }
}

private fun DrawScope.drawLineChart(
  candles: List<Candle>,
  minPrice: Double,
  priceRange: Double,
  height: Float,
  width: Float,
  lineColor: Color,
  fillBrush: Brush
) {
  if (candles.size < 2) return

  val linePath = Path()
  val fillPath = Path()

  val firstX = 0f
  val firstY = height - ((candles.first().close - minPrice) / priceRange * height).toFloat()

  linePath.moveTo(firstX, firstY)
  fillPath.moveTo(firstX, height)
  fillPath.lineTo(firstX, firstY)

  for (i in 1 until candles.size) {
    val prevX = ((i - 1).toFloat() / (candles.size - 1)) * width
    val prevY = height - ((candles[i - 1].close - minPrice) / priceRange * height).toFloat()
    val curX = (i.toFloat() / (candles.size - 1)) * width
    val curY = height - ((candles[i].close - minPrice) / priceRange * height).toFloat()

    val controlX = (prevX + curX) / 2f
    linePath.cubicTo(controlX, prevY, controlX, curY, curX, curY)
    fillPath.cubicTo(controlX, prevY, controlX, curY, curX, curY)
  }

  fillPath.lineTo(width, height)
  fillPath.close()

  // Draw gradient fill
  drawPath(fillPath, brush = fillBrush)

  // Draw main line
  drawPath(
    path = linePath,
    color = lineColor,
    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
  )
}

private fun DrawScope.drawCandlesticks(
  candles: List<Candle>,
  minPrice: Double,
  priceRange: Double,
  height: Float,
  width: Float
) {
  val candleWidth = max(2f, (width / candles.size) * 0.7f)

  candles.forEachIndexed { i, candle ->
    val x = (i.toFloat() / (candles.size - 1).coerceAtLeast(1)) * width
    val openY = height - ((candle.open - minPrice) / priceRange * height).toFloat()
    val closeY = height - ((candle.close - minPrice) / priceRange * height).toFloat()
    val highY = height - ((candle.high - minPrice) / priceRange * height).toFloat()
    val lowY = height - ((candle.low - minPrice) / priceRange * height).toFloat()

    val isBullish = candle.close >= candle.open
    val candleColor = if (isBullish) ProfitGreen else LossRed

    // Wick
    drawLine(
      color = candleColor,
      start = Offset(x, highY),
      end = Offset(x, lowY),
      strokeWidth = 1.5.dp.toPx()
    )

    // Body
    val bodyTop = min(openY, closeY)
    val bodyBottom = max(openY, closeY)
    val bodyHeight = max(2f, bodyBottom - bodyTop)

    drawRect(
      color = candleColor,
      topLeft = Offset(x - candleWidth / 2, bodyTop),
      size = Size(candleWidth, bodyHeight)
    )
  }
}

private fun DrawScope.drawMovingAverage(
  candles: List<Candle>,
  minPrice: Double,
  priceRange: Double,
  height: Float,
  width: Float,
  period: Int,
  color: Color
) {
  val path = Path()
  var started = false

  for (i in period - 1 until candles.size) {
    var sum = 0.0
    for (k in 0 until period) {
      sum += candles[i - k].close
    }
    val sma = sum / period
    val x = (i.toFloat() / (candles.size - 1)) * width
    val y = height - ((sma - minPrice) / priceRange * height).toFloat()

    if (!started) {
      path.moveTo(x, y)
      started = true
    } else {
      path.lineTo(x, y)
    }
  }

  if (started) {
    drawPath(
      path = path,
      color = color,
      style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
    )
  }
}
