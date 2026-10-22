# Nasdaq Sim - Real-Time Paper Trading & Academy

An educational paper trading simulator for the NASDAQ market built with React 19, TypeScript, Vite, and Tailwind CSS.

## Features

- **Live NASDAQ Markets**:
  - Browse over 3,400+ NASDAQ tickers with deterministic market pricing and volume.
  - Live market quote ticker simulating real-world price drift, spreads, and volume every 2.5 seconds.
  - QQQ Benchmark card with real-time price, day change, and interactive sparkline.
  - Sector filtering (All, Mega Tech, Semiconductors, AI & Cloud, Consumer Tech, Indices & ETFs) and Watchlist starring.
  - Fast search across all 3,400+ equities by ticker or company name.

- **Interactive Financial Charts**:
  - Seamless toggle between Line chart (with smooth cubic bezier curves and vertical gradient fills) and Japanese Candlestick charts (Open, High, Low, Close with wicks).
  - Timeframe selection: 1D, 1W, 1M, 1Y, and ALL.
  - Technical analysis indicators: SMA 20 (Moving Average) overlay and Volume histogram bars.
  - Interactive scrubbing and crosshair inspection on mouse hover and touch drag.

- **Trading & Execution Simulator**:
  - $100,000 initial simulated buying power with customizable reset options ($10k, $50k, $100k, $250k).
  - Real-world Market Orders: Buy at Ask, Sell at Bid.
  - Limit Orders: Queued limit buy and limit sell orders with continuous live monitoring against incoming quotes. Automatic execution with cash refund if filled better than limit!
  - Position tracking: Average cost basis, total invested, market value, unrealized gain/loss ($ and %).
  - Quick percentage sizing (25%, 50%, 75%, MAX) and share quantity stepper.
  - Educational mechanics explainer for each order type.

- **Portfolio Analytics**:
  - Net worth card tracking Total Simulated Equity, All-Time P&L, and Today's P&L.
  - Asset Allocation segmented progress bar (Cash vs Equities ratio).
  - Active positions table with real-time P&L calculations.

- **Orders & Execution Journal**:
  - Pending Limit Orders tab with condition status, reserved capital, and cancel/refund functionality.
  - Complete Execution History journal with timestamps, fill prices, and realized gains/losses.

- **Trading Academy**:
  - 6 interactive educational lessons covering NASDAQ fundamentals, order mechanics, bid-ask spread, candlestick interpretation, the 1% risk rule, and valuation metrics.
  - Interactive Knowledge Check Quizzes with immediate feedback, detailed explanations, and progress tracking.

## Getting Started

```bash
npm install
npm run dev
```

The application runs at `http://localhost:3000`.
