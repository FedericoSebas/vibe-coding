import { Candle, IntradayInterval, MarketDepthLevel, StockQuote, Timeframe } from '../types/market';
import { BaseNasdaqStock, getBaseStock } from './NasdaqStockCatalog';

function roundToCents(val: number): number {
  return Math.round(val * 100) / 100;
}

/**
 * Returns breakdown of timestamp in Eastern Time (America/New_York).
 */
export function getEasternTimeParts(ms: number = Date.now()) {
  const formatter = new Intl.DateTimeFormat('en-US', {
    timeZone: 'America/New_York',
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    weekday: 'short',
    hour: 'numeric',
    minute: 'numeric',
    second: 'numeric',
    hour12: false
  });
  const parts = formatter.formatToParts(new Date(ms));
  const partMap: Record<string, string> = {};
  for (const p of parts) {
    partMap[p.type] = p.value;
  }
  return {
    year: parseInt(partMap.year, 10),
    month: parseInt(partMap.month, 10),
    day: parseInt(partMap.day, 10),
    hour: parseInt(partMap.hour, 10) % 24,
    minute: parseInt(partMap.minute, 10),
    weekday: partMap.weekday
  };
}

/**
 * Returns the exact UTC epoch timestamp for a given clock hour/min in America/New_York on a given date.
 */
export function getETTimestamp(year: number, month: number, day: number, hour: number, minute: number): number {
  const utcGuess = Date.UTC(year, month - 1, day, hour, minute, 0, 0);
  const et = getEasternTimeParts(utcGuess);
  const etAsUtc = Date.UTC(et.year, et.month - 1, et.day, et.hour, et.minute, 0, 0);
  const offset = etAsUtc - utcGuess;
  return utcGuess - offset;
}

/**
 * Determines NASDAQ market session for a given timestamp in Eastern Time:
 * - PRE: 4:00 AM (240m) <= time < 9:30 AM (570m)
 * - REGULAR: 9:30 AM (570m) <= time < 4:00 PM (960m)
 * - POST: 4:00 PM (960m) <= time < 8:00 PM (1200m)
 */
export function getMarketSessionForET(timestampMs: number): 'PRE' | 'REGULAR' | 'POST' {
  const { hour, minute } = getEasternTimeParts(timestampMs);
  const timeInMins = hour * 60 + minute;
  if (timeInMins >= 240 && timeInMins < 570) {
    return 'PRE';
  }
  if (timeInMins >= 570 && timeInMins < 960) {
    return 'REGULAR';
  }
  if (timeInMins >= 960 && timeInMins < 1200) {
    return 'POST';
  }
  return 'REGULAR';
}

export class NasdaqMarketDataSource {
  private simulatedQuotes: Map<string, StockQuote> = new Map();

  constructor() {}

  public getOrCreateQuote(symbol: string): StockQuote {
    const sym = symbol.toUpperCase();
    const existing = this.simulatedQuotes.get(sym);
    if (existing) return existing;
    return this.generateSimulatedTick(sym);
  }

  public generateSimulatedTick(symbol: string): StockQuote {
    const sym = symbol.toUpperCase();
    const existing = this.simulatedQuotes.get(sym);
    const base: BaseNasdaqStock = getBaseStock(sym);
    const currentPrice = existing ? existing.price : base.basePrice;
    const prevClose = existing ? existing.previousClose : roundToCents(base.basePrice * 0.99);

    // Micro-fluctuation drift: -0.2% to +0.22%
    const drift = (Math.random() * (0.0022 - -0.002)) + -0.002;
    const newPrice = roundToCents(Math.max(0.10, currentPrice * (1.0 + drift)));
    const change = roundToCents(newPrice - prevClose);
    const changePercent = prevClose > 0 ? roundToCents((change / prevClose) * 100) : 0;
    const newDayHigh = Math.max(existing?.dayHigh ?? newPrice, newPrice);
    const newDayLow = Math.min(existing?.dayLow ?? newPrice, newPrice);
    const spread = Math.max(0.01, roundToCents(newPrice * 0.0003));

    let spark = existing?.sparkline ? [...existing.sparkline] : [];
    if (spark.length === 0) {
      spark = this.generateSparkline(newPrice, prevClose);
    } else {
      spark.push(newPrice);
      if (spark.length > 24) spark.shift();
    }

    const updated: StockQuote = {
      symbol: sym,
      name: existing?.name ?? base.name,
      sector: existing?.sector ?? base.sector,
      price: newPrice,
      previousClose: prevClose,
      change,
      changePercent,
      dayHigh: newDayHigh,
      dayLow: newDayLow,
      volume: (existing?.volume ?? base.avgVolume) + Math.floor(Math.random() * 1400) + 100,
      peRatio: existing?.peRatio ?? base.peRatio,
      marketCap: existing?.marketCap ?? base.marketCap,
      bid: roundToCents(newPrice - spread / 2),
      ask: roundToCents(newPrice + spread / 2),
      isMarketOpen: true,
      lastUpdated: Date.now(),
      sparkline: spark
    };

    this.simulatedQuotes.set(sym, updated);
    return updated;
  }

  public generateDepth(quote: StockQuote): MarketDepthLevel[] {
    const levels: MarketDepthLevel[] = [];
    const baseSpread = Math.max(0.01, roundToCents(quote.price * 0.0002));
    for (let i = 1; i <= 5; i++) {
      const bidPrice = roundToCents(quote.bid - (i - 1) * baseSpread);
      const askPrice = roundToCents(quote.ask + (i - 1) * baseSpread);
      const bidSize = 100 * (6 - i) * (Math.floor(Math.random() * 4) + 2);
      const askSize = 100 * (6 - i) * (Math.floor(Math.random() * 4) + 2);
      levels.push({ bidPrice, bidSize, askPrice, askSize });
    }
    return levels;
  }

  public generateSparkline(currentPrice: number, prevClose: number): number[] {
    const points: number[] = [];
    const steps = 20;
    const totalDelta = currentPrice - prevClose;
    for (let i = 0; i < steps; i++) {
      const progress = i / steps;
      const noise = currentPrice * ((Math.random() * 0.008) - 0.004);
      const p = prevClose + totalDelta * progress + noise;
      points.push(roundToCents(p));
    }
    points.push(currentPrice);
    return points;
  }

  public generateFallbackCandles(
    symbol: string,
    timeframe: Timeframe,
    intradayInterval: IntradayInterval = '5m'
  ): Candle[] {
    const quote = this.getOrCreateQuote(symbol);
    const currentPrice = quote.price;

    if (timeframe === '1D') {
      const nowMs = Date.now();
      const currentET = getEasternTimeParts(nowMs);
      let targetYear = currentET.year;
      let targetMonth = currentET.month;
      let targetDay = currentET.day;

      // If weekend, roll back to Friday
      if (currentET.weekday === 'Sun') {
        const friET = getEasternTimeParts(nowMs - 2 * 24 * 3600 * 1000);
        targetYear = friET.year;
        targetMonth = friET.month;
        targetDay = friET.day;
      } else if (currentET.weekday === 'Sat') {
        const friET = getEasternTimeParts(nowMs - 1 * 24 * 3600 * 1000);
        targetYear = friET.year;
        targetMonth = friET.month;
        targetDay = friET.day;
      }

      // Pre-market starts at 4:00 AM ET (04:00)
      const dayStartETMs = getETTimestamp(targetYear, targetMonth, targetDay, 4, 0);
      // After-hours ends at 8:00 PM ET (20:00)
      const dayEndETMs = getETTimestamp(targetYear, targetMonth, targetDay, 20, 0);

      let stepMinutes = 5;
      let deltaFactor = 0.007;

      if (intradayInterval === '1m') {
        stepMinutes = 1;
        deltaFactor = 0.003;
      } else if (intradayInterval === '10m') {
        stepMinutes = 10;
        deltaFactor = 0.012;
      } else {
        stepMinutes = 5;
        deltaFactor = 0.007;
      }

      const stepMs = stepMinutes * 60 * 1000;
      const totalCandles = Math.floor((dayEndETMs - dayStartETMs) / stepMs); // 96 for 10m, 192 for 5m, 960 for 1m

      const candles: Candle[] = [];
      let runningPrice = currentPrice * 0.94;

      for (let i = 0; i < totalCandles; i++) {
        const t = dayStartETMs + i * stepMs;
        const session = getMarketSessionForET(t);

        const open = roundToCents(runningPrice);
        const delta = runningPrice * ((Math.random() * (deltaFactor * 2)) - deltaFactor);
        const close = roundToCents(Math.max(1.0, open + delta));
        const high = roundToCents(Math.max(open, close) + runningPrice * ((Math.random() * (deltaFactor * 0.6)) + 0.0005));
        const low = roundToCents(Math.min(open, close) - runningPrice * ((Math.random() * (deltaFactor * 0.6)) + 0.0005));
        const volume = Math.floor(Math.random() * 1800000) + 200000;

        candles.push({ timestamp: t, open, high, low, close, volume, session });
        runningPrice = close;
      }

      // Ensure last candle close is at current price
      if (candles.length > 0) {
        const last = candles[candles.length - 1];
        candles[candles.length - 1] = {
          ...last,
          close: currentPrice,
          high: Math.max(last.high, currentPrice),
          low: Math.min(last.low, currentPrice)
        };
      }

      return candles;
    }

    let count = 35;
    let intervalMillis = 10 * 60 * 1000;
    let deltaFactor = 0.015;

    switch (timeframe) {
      case '1W':
        count = 40;
        intervalMillis = 2 * 3600 * 1000;
        deltaFactor = 0.02;
        break;
      case '1M':
        count = 30;
        intervalMillis = 24 * 3600 * 1000;
        deltaFactor = 0.03;
        break;
      case '1Y':
        count = 52;
        intervalMillis = 7 * 24 * 3600 * 1000;
        deltaFactor = 0.04;
        break;
      case 'ALL':
        count = 60;
        intervalMillis = 30 * 24 * 3600 * 1000;
        deltaFactor = 0.06;
        break;
    }

    const now = Date.now();
    const candles: Candle[] = [];
    let runningPrice = currentPrice * 0.94;
    const dayStartTime = now - count * intervalMillis;

    for (let i = 0; i < count; i++) {
      const t = dayStartTime + i * intervalMillis;
      const open = roundToCents(runningPrice);
      const delta = runningPrice * ((Math.random() * (deltaFactor * 2)) - deltaFactor);
      const close = roundToCents(Math.max(1.0, open + delta));
      const high = roundToCents(Math.max(open, close) + runningPrice * ((Math.random() * (deltaFactor * 0.6)) + 0.0005));
      const low = roundToCents(Math.min(open, close) - runningPrice * ((Math.random() * (deltaFactor * 0.6)) + 0.0005));
      const volume = Math.floor(Math.random() * 1800000) + 200000;

      candles.push({ timestamp: t, open, high, low, close, volume });
      runningPrice = close;
    }

    if (candles.length > 0) {
      const last = candles[candles.length - 1];
      candles[candles.length - 1] = {
        ...last,
        close: currentPrice,
        high: Math.max(last.high, currentPrice),
        low: Math.min(last.low, currentPrice)
      };
    }

    return candles;
  }

  public async fetchRealtimeQuote(symbol: string): Promise<StockQuote | null> {
    const sym = symbol.toUpperCase();
    try {
      const url = `https://query1.finance.yahoo.com/v8/finance/chart/${sym}?interval=1m&range=1d`;
      const res = await fetch(url, { headers: { 'User-Agent': 'Mozilla/5.0' } });
      if (res.ok) {
        const json = await res.json();
        const results = json?.chart?.result;
        if (results && results.length > 0) {
          const item = results[0];
          const meta = item.meta;
          const price = meta.regularMarketPrice ?? 0;
          const prevClose = meta.previousClose ?? price;
          const dayHigh = meta.regularMarketDayHigh ?? price * 1.01;
          const dayLow = meta.regularMarketDayLow ?? price * 0.99;
          const volume = meta.regularMarketVolume ?? 1000000;
          const base = getBaseStock(sym);

          const change = price - prevClose;
          const changePercent = prevClose > 0 ? (change / prevClose) * 100 : 0;
          const spread = Math.max(0.01, price * 0.0003);

          const sparkline: number[] = [];
          const closes = item?.indicators?.quote?.[0]?.close;
          if (Array.isArray(closes)) {
            for (const v of closes) {
              if (typeof v === 'number' && !isNaN(v) && v > 0) {
                sparkline.push(roundToCents(v));
              }
            }
          }

          const finalSparkline = sparkline.length >= 5 ? sparkline.slice(-20) : this.generateSparkline(price, prevClose);

          const quote: StockQuote = {
            symbol: sym,
            name: meta.shortName ?? base.name,
            sector: base.sector,
            price: roundToCents(price),
            previousClose: roundToCents(prevClose),
            change: roundToCents(change),
            changePercent: roundToCents(changePercent),
            dayHigh: roundToCents(dayHigh),
            dayLow: roundToCents(dayLow),
            volume,
            peRatio: base.peRatio,
            marketCap: base.marketCap,
            bid: roundToCents(price - spread / 2),
            ask: roundToCents(price + spread / 2),
            isMarketOpen: true,
            lastUpdated: Date.now(),
            sparkline: finalSparkline
          };

          this.simulatedQuotes.set(sym, quote);
          return quote;
        }
      }
    } catch (_e) {
      // Return simulated quote on network restriction or error
    }
    return this.generateSimulatedTick(sym);
  }

  public async fetchCandles(
    symbol: string,
    timeframe: Timeframe,
    intradayInterval: IntradayInterval = '5m'
  ): Promise<Candle[]> {
    const sym = symbol.toUpperCase();
    let interval = '1m';
    let range = '1d';
    switch (timeframe) {
      case '1D':
        interval = intradayInterval === '10m' ? '5m' : intradayInterval;
        range = '1d';
        break;
      case '1W':
        interval = '15m';
        range = '5d';
        break;
      case '1M':
        interval = '1d';
        range = '1mo';
        break;
      case '1Y':
        interval = '1d';
        range = '1y';
        break;
      case 'ALL':
        interval = '1wk';
        range = '5y';
        break;
    }

    try {
      const url = `https://query1.finance.yahoo.com/v8/finance/chart/${sym}?interval=${interval}&range=${range}`;
      const res = await fetch(url, { headers: { 'User-Agent': 'Mozilla/5.0' } });
      if (res.ok) {
        const json = await res.json();
        const results = json?.chart?.result;
        if (results && results.length > 0) {
          const item = results[0];
          const timestamps: number[] = item.timestamp ?? [];
          const quoteData = item.indicators?.quote?.[0];
          if (timestamps.length > 0 && quoteData) {
            const { open, high, low, close, volume } = quoteData;
            const candles: Candle[] = [];
            for (let i = 0; i < timestamps.length; i++) {
              const t = timestamps[i] * 1000;
              const o = open?.[i];
              const h = high?.[i];
              const l = low?.[i];
              const c = close?.[i];
              const v = volume?.[i] ?? 50000;
              if (
                typeof o === 'number' && !isNaN(o) &&
                typeof h === 'number' && !isNaN(h) &&
                typeof l === 'number' && !isNaN(l) &&
                typeof c === 'number' && !isNaN(c) && c > 0
              ) {
                let session: 'PRE' | 'REGULAR' | 'POST' | undefined;
                if (timeframe === '1D') {
                  session = getMarketSessionForET(t);
                }

                candles.push({
                  timestamp: t,
                  open: roundToCents(o),
                  high: roundToCents(h),
                  low: roundToCents(l),
                  close: roundToCents(c),
                  volume: v,
                  session
                });
              }
            }
            if (candles.length >= 8) {
              return candles;
            }
          }
        }
      }
    } catch (_e) {
      // Fall through to fallback generator
    }

    return this.generateFallbackCandles(sym, timeframe, intradayInterval);
  }
}

export const marketDataSource = new NasdaqMarketDataSource();
