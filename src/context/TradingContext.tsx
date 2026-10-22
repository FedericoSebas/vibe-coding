import React, { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import {
  AppTab,
  Candle,
  ChartType,
  IntradayInterval,
  MarketDepthLevel,
  OrderEntity,
  PortfolioPositionItem,
  PortfolioSummary,
  Sector,
  StockQuote,
  Timeframe,
  UserAccount,
  PositionEntity
} from '../types/market';
import { loadAllSymbols } from '../data/NasdaqStockCatalog';
import { marketDataSource } from '../data/NasdaqMarketDataSource';
import { EDUCATIONAL_LESSONS } from '../data/EducationalContent';

function roundToCents(v: number): number {
  return Math.round(v * 100) / 100;
}

function formatMoney(v: number): string {
  return v.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

interface TradeResult {
  success: boolean;
  message: string;
  order?: OrderEntity;
}

interface TradingContextValue {
  currentTab: AppTab;
  setCurrentTab: (tab: AppTab) => void;
  quotes: Record<string, StockQuote>;
  selectedStock: StockQuote | null;
  selectStock: (symbol: string) => void;
  candles: Candle[];
  marketDepth: MarketDepthLevel[];
  selectedTimeframe: Timeframe;
  setTimeframe: (tf: Timeframe) => void;
  intradayInterval: IntradayInterval;
  setIntradayInterval: (interval: IntradayInterval) => void;
  chartType: ChartType;
  setChartType: (ct: ChartType) => void;
  showSMA: boolean;
  toggleSMA: () => void;
  showVolume: boolean;
  toggleVolume: () => void;
  selectedSector: Sector;
  setSectorFilter: (s: Sector) => void;
  searchQuery: string;
  setSearchQuery: (q: string) => void;
  allSymbols: string[];
  watchlist: Set<string>;
  toggleWatchlist: (ticker: string) => void;
  portfolioSummary: PortfolioSummary;
  orderHistory: OrderEntity[];
  pendingOrders: OrderEntity[];
  userAccount: UserAccount;
  executeTrade: (
    ticker: string,
    companyName: string,
    isBuy: boolean,
    orderType: string,
    shares: number,
    limitPrice: number | null
  ) => TradeResult;
  cancelPendingOrder: (orderId: number) => boolean;
  resetPortfolio: (startingCapital?: number) => void;
  activeLessonId: string | null;
  setActiveLessonId: (id: string | null) => void;
  quizAnswers: Record<string, number>;
  submitQuizAnswer: (lessonId: string, answerIndex: number) => void;
  feedbackMessage: string | null;
  dismissFeedbackMessage: () => void;
  tradeConfirmationOrder: OrderEntity | null;
  dismissTradeConfirmation: () => void;
  activeDetailTicker: string | null;
  setActiveDetailTicker: (ticker: string | null) => void;
}

const TradingContext = createContext<TradingContextValue | null>(null);

const STORAGE_KEYS = {
  ACCOUNT: 'nasdaq_sim_account',
  POSITIONS: 'nasdaq_sim_positions',
  ORDERS: 'nasdaq_sim_orders',
  WATCHLIST: 'nasdaq_sim_watchlist',
  QUIZ: 'nasdaq_sim_quiz',
};

const DEFAULT_WATCHLIST = ['QQQ', 'AAPL', 'NVDA', 'TSLA', 'MSFT', 'AMZN'];

export const TradingProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentTab, setCurrentTab] = useState<AppTab>('MARKET');
  const [quotes, setQuotes] = useState<Record<string, StockQuote>>({});
  const [selectedStock, setSelectedStock] = useState<StockQuote | null>(null);
  const [activeDetailTicker, setActiveDetailTicker] = useState<string | null>(null);
  const [candles, setCandles] = useState<Candle[]>([]);
  const [marketDepth, setMarketDepth] = useState<MarketDepthLevel[]>([]);
  const [selectedTimeframe, setSelectedTimeframe] = useState<Timeframe>('1D');
  const [intradayInterval, setIntradayInterval] = useState<IntradayInterval>('5m');
  const [chartType, setChartType] = useState<ChartType>('LINE');
  const [showSMA, setShowSMA] = useState<boolean>(true);
  const [showVolume, setShowVolume] = useState<boolean>(true);
  const [selectedSector, setSectorFilter] = useState<Sector>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [allSymbols, setAllSymbols] = useState<string[]>([]);
  const [activeLessonId, setActiveLessonId] = useState<string | null>(null);
  const [feedbackMessage, setFeedbackMessage] = useState<string | null>(null);
  const [tradeConfirmationOrder, setTradeConfirmationOrder] = useState<OrderEntity | null>(null);

  // Persistence States
  const [userAccount, setUserAccount] = useState<UserAccount>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.ACCOUNT);
      if (saved) return JSON.parse(saved);
    } catch (_e) {}
    return { id: 1, cashBalance: 100000.0, startingCapital: 100000.0, resetCount: 0 };
  });

  const [positions, setPositions] = useState<PositionEntity[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.POSITIONS);
      if (saved) return JSON.parse(saved);
    } catch (_e) {}
    return [];
  });

  const [orderHistory, setOrderHistory] = useState<OrderEntity[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.ORDERS);
      if (saved) return JSON.parse(saved);
    } catch (_e) {}
    return [];
  });

  const [watchlist, setWatchlist] = useState<Set<string>>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.WATCHLIST);
      if (saved) return new Set(JSON.parse(saved));
    } catch (_e) {}
    return new Set(DEFAULT_WATCHLIST);
  });

  const [quizAnswers, setQuizAnswers] = useState<Record<string, number>>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEYS.QUIZ);
      if (saved) return JSON.parse(saved);
    } catch (_e) {}
    return {};
  });

  // Save to localStorage
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.ACCOUNT, JSON.stringify(userAccount));
    } catch (_e) {}
  }, [userAccount]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.POSITIONS, JSON.stringify(positions));
    } catch (_e) {}
  }, [positions]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.ORDERS, JSON.stringify(orderHistory));
    } catch (_e) {}
  }, [orderHistory]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.WATCHLIST, JSON.stringify(Array.from(watchlist)));
    } catch (_e) {}
  }, [watchlist]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.QUIZ, JSON.stringify(quizAnswers));
    } catch (_e) {}
  }, [quizAnswers]);

  // Load symbols on mount
  useEffect(() => {
    let isMounted = true;
    loadAllSymbols().then((syms) => {
      if (!isMounted) return;
      setAllSymbols(syms);

      // Pre-populate initial active quotes
      const initialMap: Record<string, StockQuote> = {};
      const initialToLoad = syms.slice(0, 100);
      initialToLoad.forEach((sym) => {
        initialMap[sym] = marketDataSource.getOrCreateQuote(sym);
      });
      setQuotes(initialMap);

      const qqq = initialMap['QQQ'] || Object.values(initialMap)[0];
      setSelectedStock(qqq);
      if (qqq) {
        setMarketDepth(marketDataSource.generateDepth(qqq));
      }
    });
    return () => { isMounted = false; };
  }, []);

  // Load candles when selectedStock, timeframe, or intradayInterval changes
  useEffect(() => {
    if (!selectedStock) return;
    let isCancelled = false;
    marketDataSource
      .fetchCandles(selectedStock.symbol, selectedTimeframe, intradayInterval)
      .then((c) => {
        if (!isCancelled) {
          setCandles(c);
        }
      });
    return () => { isCancelled = true; };
  }, [selectedStock?.symbol, selectedTimeframe, intradayInterval]);

  // Mutable refs for current quotes and pending orders to use inside tick loop
  const quotesRef = useRef<Record<string, StockQuote>>({});
  quotesRef.current = quotes;

  const positionsRef = useRef<PositionEntity[]>([]);
  positionsRef.current = positions;

  const userAccountRef = useRef<UserAccount>(userAccount);
  userAccountRef.current = userAccount;

  const orderHistoryRef = useRef<OrderEntity[]>(orderHistory);
  orderHistoryRef.current = orderHistory;

  const selectedStockRef = useRef<StockQuote | null>(selectedStock);
  selectedStockRef.current = selectedStock;

  const activeDetailTickerRef = useRef<string | null>(activeDetailTicker);
  activeDetailTickerRef.current = activeDetailTicker;

  // Check pending limit orders against latest quotes
  const checkPendingLimitOrders = useCallback((updatedQuotes: Record<string, StockQuote>) => {
    const orders = [...orderHistoryRef.current];
    let hasOrderChanges = false;
    let currentAccount = { ...userAccountRef.current };
    let currentPositions = [...positionsRef.current];

    for (let i = 0; i < orders.length; i++) {
      const order = orders[i];
      if (order.status !== 'PENDING') continue;

      const quote = updatedQuotes[order.ticker];
      if (!quote || order.limitPrice === null) continue;

      if (order.orderType === 'LIMIT_BUY' && quote.ask <= order.limitPrice) {
        const fillPrice = quote.ask;
        const actualTotalCost = roundToCents(order.shares * fillPrice);
        const refundCash = roundToCents(order.totalAmount - actualTotalCost);

        if (refundCash > 0) {
          currentAccount = {
            ...currentAccount,
            cashBalance: roundToCents(currentAccount.cashBalance + refundCash)
          };
        }

        const existingPosIdx = currentPositions.findIndex((p) => p.ticker === order.ticker);
        if (existingPosIdx > -1) {
          const ex = currentPositions[existingPosIdx];
          const newShares = ex.shares + order.shares;
          const newInvested = ex.totalInvested + actualTotalCost;
          const newAvg = roundToCents(newInvested / newShares);
          currentPositions[existingPosIdx] = {
            ...ex,
            shares: newShares,
            averageCost: newAvg,
            totalInvested: newInvested,
            lastUpdated: Date.now()
          };
        } else {
          currentPositions.push({
            ticker: order.ticker,
            companyName: order.companyName,
            shares: order.shares,
            averageCost: fillPrice,
            totalInvested: actualTotalCost,
            lastUpdated: Date.now()
          });
        }

        orders[i] = {
          ...order,
          status: 'FILLED',
          executionPrice: fillPrice,
          totalAmount: actualTotalCost,
          timestamp: Date.now()
        };
        hasOrderChanges = true;
      } else if (order.orderType === 'LIMIT_SELL' && quote.bid >= order.limitPrice) {
        const fillPrice = quote.bid;
        const totalProceeds = roundToCents(order.shares * fillPrice);
        const existingPosIdx = currentPositions.findIndex((p) => p.ticker === order.ticker);
        const avgCost = existingPosIdx > -1 ? currentPositions[existingPosIdx].averageCost : fillPrice;
        const realized = roundToCents(totalProceeds - (order.shares * avgCost));

        currentAccount = {
          ...currentAccount,
          cashBalance: roundToCents(currentAccount.cashBalance + totalProceeds)
        };

        if (existingPosIdx > -1) {
          const ex = currentPositions[existingPosIdx];
          const remainingShares = ex.shares - order.shares;
          if (remainingShares <= 0.0001) {
            currentPositions.splice(existingPosIdx, 1);
          } else {
            const remainingInvested = roundToCents(remainingShares * ex.averageCost);
            currentPositions[existingPosIdx] = {
              ...ex,
              shares: remainingShares,
              totalInvested: remainingInvested,
              lastUpdated: Date.now()
            };
          }
        }

        orders[i] = {
          ...order,
          status: 'FILLED',
          executionPrice: fillPrice,
          totalAmount: totalProceeds,
          realizedPnL: realized,
          timestamp: Date.now()
        };
        hasOrderChanges = true;
      }
    }

    if (hasOrderChanges) {
      setUserAccount(currentAccount);
      setPositions(currentPositions);
      setOrderHistory(orders);
    }
  }, []);

  // Live Market Ticker Interval (every 2.5 seconds)
  useEffect(() => {
    const timer = setInterval(() => {
      const curQuotes = { ...quotesRef.current };
      const curSelected = selectedStockRef.current?.symbol;
      const curDetail = activeDetailTickerRef.current;
      const positionsTickers = positionsRef.current.map((p) => p.ticker);
      const watchTickers = Array.from(watchlist);
      const topSymbols = allSymbols.slice(0, 25);

      const symbolsToTick = Array.from(
        new Set([
          ...topSymbols,
          ...positionsTickers,
          ...watchTickers,
          ...(curSelected ? [curSelected] : []),
          ...(curDetail ? [curDetail] : [])
        ])
      );

      symbolsToTick.forEach((sym) => {
        const tick = marketDataSource.generateSimulatedTick(sym);
        curQuotes[sym] = tick;
      });

      setQuotes(curQuotes);

      // Check depth update for selected
      if (curSelected && curQuotes[curSelected]) {
        setSelectedStock(curQuotes[curSelected]);
        setMarketDepth(marketDataSource.generateDepth(curQuotes[curSelected]));
      }

      checkPendingLimitOrders(curQuotes);
    }, 2500);

    return () => clearInterval(timer);
  }, [allSymbols, watchlist, checkPendingLimitOrders]);

  // Select Stock
  const selectStock = useCallback((symbol: string) => {
    const sym = symbol.toUpperCase();
    const stock = marketDataSource.getOrCreateQuote(sym);
    const depth = marketDataSource.generateDepth(stock);
    setQuotes((prev) => ({ ...prev, [sym]: stock }));
    setSelectedStock(stock);
    setMarketDepth(depth);

    // Also attempt real-time quote in background
    marketDataSource.fetchRealtimeQuote(sym).then((realQuote) => {
      if (realQuote) {
        setQuotes((prev) => ({ ...prev, [sym]: realQuote }));
        if (selectedStockRef.current?.symbol === sym) {
          setSelectedStock(realQuote);
          setMarketDepth(marketDataSource.generateDepth(realQuote));
        }
      }
    });
  }, []);

  // Toggle Watchlist
  const toggleWatchlist = useCallback((ticker: string) => {
    setWatchlist((prev) => {
      const next = new Set(prev);
      if (next.has(ticker)) {
        next.delete(ticker);
      } else {
        next.add(ticker);
      }
      return next;
    });
  }, []);

  // Compute Portfolio Summary
  const portfolioSummary: PortfolioSummary = React.useMemo(() => {
    const cash = userAccount.cashBalance;
    const startCapital = userAccount.startingCapital;

    let positionsValue = 0;
    let todayPnLSum = 0;

    const items: PortfolioPositionItem[] = positions.map((pos) => {
      const quote = quotes[pos.ticker];
      const currentPrice = quote ? quote.price : pos.averageCost;
      const marketVal = roundToCents(pos.shares * currentPrice);
      const unrealized = roundToCents(marketVal - pos.totalInvested);
      const unrealizedPct = pos.totalInvested > 0 ? (unrealized / pos.totalInvested) * 100 : 0;

      const dayChangePerShare = quote ? quote.change : 0;
      const dayPnL = roundToCents(pos.shares * dayChangePerShare);
      const dayPnLPct = quote ? quote.changePercent : 0;

      positionsValue += marketVal;
      todayPnLSum += dayPnL;

      return {
        ticker: pos.ticker,
        companyName: pos.companyName,
        shares: pos.shares,
        averageCost: pos.averageCost,
        currentPrice,
        marketValue: marketVal,
        totalInvested: pos.totalInvested,
        unrealizedPnL: unrealized,
        unrealizedPnLPercent: unrealizedPct,
        dayPnL,
        dayPnLPercent: dayPnLPct
      };
    });

    const totalEquity = roundToCents(cash + positionsValue);
    const totalPnL = roundToCents(totalEquity - startCapital);
    const totalPnLPct = startCapital > 0 ? (totalPnL / startCapital) * 100 : 0;
    const todayPnLPct = totalEquity > 0 ? (todayPnLSum / totalEquity) * 100 : 0;

    return {
      totalEquity,
      cashBalance: cash,
      positionsValue: roundToCents(positionsValue),
      totalUnrealizedPnL: totalPnL,
      totalUnrealizedPnLPercent: totalPnLPct,
      todayPnL: roundToCents(todayPnLSum),
      todayPnLPercent: todayPnLPct,
      startingCapital: startCapital,
      positions: items
    };
  }, [userAccount, positions, quotes]);

  // Execute Trade
  const executeTrade = useCallback(
    (
      ticker: string,
      companyName: string,
      isBuy: boolean,
      orderTypeStr: string,
      shares: number,
      limitPrice: number | null
    ): TradeResult => {
      if (shares <= 0) {
        setFeedbackMessage('Invalid share quantity');
        return { success: false, message: 'Invalid share quantity' };
      }

      const quote = marketDataSource.getOrCreateQuote(ticker);
      const execPrice = isBuy ? quote.ask : quote.bid;

      if (orderTypeStr === 'LIMIT') {
        if (!limitPrice || limitPrice <= 0) {
          setFeedbackMessage('Invalid limit price');
          return { success: false, message: 'Invalid limit price' };
        }

        const totalAmount = roundToCents(shares * limitPrice);

        if (isBuy) {
          if (userAccount.cashBalance < totalAmount) {
            const err = `Insufficient cash to hold for limit order ($${formatMoney(totalAmount)} required)`;
            setFeedbackMessage(err);
            return { success: false, message: err };
          }

          // Reserve cash
          setUserAccount((prev) => ({
            ...prev,
            cashBalance: roundToCents(prev.cashBalance - totalAmount)
          }));

          const newOrder: OrderEntity = {
            id: Date.now() + Math.floor(Math.random() * 1000),
            ticker,
            companyName,
            orderType: 'LIMIT_BUY',
            shares,
            limitPrice,
            executionPrice: null,
            totalAmount,
            status: 'PENDING',
            timestamp: Date.now(),
            realizedPnL: 0
          };

          setOrderHistory((prev) => [newOrder, ...prev]);
          const msg = `Limit Buy queued @ $${limitPrice}. Cash is reserved until execution.`;
          setFeedbackMessage(msg);
          setTradeConfirmationOrder(newOrder);
          return { success: true, message: msg, order: newOrder };
        } else {
          const pos = positions.find((p) => p.ticker === ticker);
          if (!pos || pos.shares < shares) {
            const err = `You only own ${pos ? pos.shares : 0} shares of ${ticker}`;
            setFeedbackMessage(err);
            return { success: false, message: err };
          }

          const newOrder: OrderEntity = {
            id: Date.now() + Math.floor(Math.random() * 1000),
            ticker,
            companyName,
            orderType: 'LIMIT_SELL',
            shares,
            limitPrice,
            executionPrice: null,
            totalAmount,
            status: 'PENDING',
            timestamp: Date.now(),
            realizedPnL: 0
          };

          setOrderHistory((prev) => [newOrder, ...prev]);
          const msg = `Limit Sell queued @ $${limitPrice}. Will trigger automatically at or above limit.`;
          setFeedbackMessage(msg);
          setTradeConfirmationOrder(newOrder);
          return { success: true, message: msg, order: newOrder };
        }
      }

      // Market Order Execution
      if (isBuy) {
        const totalCost = roundToCents(shares * execPrice);
        if (userAccount.cashBalance < totalCost) {
          const err = `Insufficient cash balance ($${formatMoney(userAccount.cashBalance)} available, $${formatMoney(totalCost)} needed)`;
          setFeedbackMessage(err);
          return { success: false, message: err };
        }

        setUserAccount((prev) => ({
          ...prev,
          cashBalance: roundToCents(prev.cashBalance - totalCost)
        }));

        setPositions((prev) => {
          const idx = prev.findIndex((p) => p.ticker === ticker);
          if (idx > -1) {
            const ex = prev[idx];
            const newTotalShares = ex.shares + shares;
            const newTotalInvested = ex.totalInvested + totalCost;
            const newAvgCost = roundToCents(newTotalInvested / newTotalShares);
            const copy = [...prev];
            copy[idx] = {
              ...ex,
              shares: newTotalShares,
              averageCost: newAvgCost,
              totalInvested: newTotalInvested,
              lastUpdated: Date.now()
            };
            return copy;
          } else {
            return [
              ...prev,
              {
                ticker,
                companyName,
                shares,
                averageCost: roundToCents(execPrice),
                totalInvested: totalCost,
                lastUpdated: Date.now()
              }
            ];
          }
        });

        const newOrder: OrderEntity = {
          id: Date.now() + Math.floor(Math.random() * 1000),
          ticker,
          companyName,
          orderType: 'MARKET_BUY',
          shares,
          limitPrice: null,
          executionPrice: execPrice,
          totalAmount: totalCost,
          status: 'FILLED',
          timestamp: Date.now(),
          realizedPnL: 0
        };

        setOrderHistory((prev) => [newOrder, ...prev]);
        const msg = `Market Buy executed for ${shares} shares of ${ticker} @ $${execPrice}`;
        setFeedbackMessage(msg);
        setTradeConfirmationOrder(newOrder);
        return { success: true, message: msg, order: newOrder };
      } else {
        const pos = positions.find((p) => p.ticker === ticker);
        if (!pos) {
          const err = `You do not own any shares of ${ticker}`;
          setFeedbackMessage(err);
          return { success: false, message: err };
        }
        if (pos.shares < shares - 0.0001) {
          const err = `You only own ${pos.shares} shares of ${ticker}`;
          setFeedbackMessage(err);
          return { success: false, message: err };
        }

        const totalProceeds = roundToCents(shares * execPrice);
        const costBasisPortion = roundToCents(shares * pos.averageCost);
        const realizedPnL = roundToCents(totalProceeds - costBasisPortion);

        setUserAccount((prev) => ({
          ...prev,
          cashBalance: roundToCents(prev.cashBalance + totalProceeds)
        }));

        setPositions((prev) => {
          const idx = prev.findIndex((p) => p.ticker === ticker);
          if (idx === -1) return prev;
          const ex = prev[idx];
          const remainingShares = ex.shares - shares;
          if (remainingShares <= 0.0001) {
            return prev.filter((p) => p.ticker !== ticker);
          } else {
            const remainingInvested = roundToCents(remainingShares * ex.averageCost);
            const copy = [...prev];
            copy[idx] = {
              ...ex,
              shares: remainingShares,
              totalInvested: remainingInvested,
              lastUpdated: Date.now()
            };
            return copy;
          }
        });

        const newOrder: OrderEntity = {
          id: Date.now() + Math.floor(Math.random() * 1000),
          ticker,
          companyName,
          orderType: 'MARKET_SELL',
          shares,
          limitPrice: null,
          executionPrice: execPrice,
          totalAmount: totalProceeds,
          status: 'FILLED',
          timestamp: Date.now(),
          realizedPnL
        };

        setOrderHistory((prev) => [newOrder, ...prev]);
        const msg = `Market Sell executed for ${shares} shares of ${ticker} @ $${execPrice}`;
        setFeedbackMessage(msg);
        setTradeConfirmationOrder(newOrder);
        return { success: true, message: msg, order: newOrder };
      }
    },
    [userAccount, positions]
  );

  // Cancel Pending Order
  const cancelPendingOrder = useCallback((orderId: number): boolean => {
    let refunded = false;
    setOrderHistory((prev) => {
      return prev.map((ord) => {
        if (ord.id === orderId && ord.status === 'PENDING') {
          if (ord.orderType === 'LIMIT_BUY') {
            setUserAccount((acc) => ({
              ...acc,
              cashBalance: roundToCents(acc.cashBalance + ord.totalAmount)
            }));
            refunded = true;
          }
          return { ...ord, status: 'CANCELLED' };
        }
        return ord;
      });
    });
    setFeedbackMessage(refunded ? 'Pending order cancelled. Funds returned.' : 'Pending order cancelled.');
    return true;
  }, []);

  // Reset Portfolio
  const resetPortfolio = useCallback((startingCapital: number = 100000.0) => {
    setPositions([]);
    setOrderHistory([]);
    setUserAccount((prev) => ({
      id: 1,
      cashBalance: startingCapital,
      startingCapital,
      resetCount: prev.resetCount + 1
    }));
    setFeedbackMessage(`Simulated portfolio reset to $${startingCapital.toLocaleString()} capital.`);
  }, []);

  // Quiz submission
  const submitQuizAnswer = useCallback((lessonId: string, answerIndex: number) => {
    setQuizAnswers((prev) => ({
      ...prev,
      [lessonId]: answerIndex
    }));
  }, []);

  const pendingOrders = React.useMemo(() => {
    return orderHistory.filter((o) => o.status === 'PENDING');
  }, [orderHistory]);

  const value: TradingContextValue = {
    currentTab,
    setCurrentTab,
    quotes,
    selectedStock,
    selectStock,
    candles,
    marketDepth,
    selectedTimeframe,
    setTimeframe: setSelectedTimeframe,
    intradayInterval,
    setIntradayInterval,
    chartType,
    setChartType,
    showSMA,
    toggleSMA: () => setShowSMA((s) => !s),
    showVolume,
    toggleVolume: () => setShowVolume((v) => !v),
    selectedSector,
    setSectorFilter,
    searchQuery,
    setSearchQuery,
    allSymbols,
    watchlist,
    toggleWatchlist,
    portfolioSummary,
    orderHistory,
    pendingOrders,
    userAccount,
    executeTrade,
    cancelPendingOrder,
    resetPortfolio,
    activeLessonId,
    setActiveLessonId,
    quizAnswers,
    submitQuizAnswer,
    feedbackMessage,
    dismissFeedbackMessage: () => setFeedbackMessage(null),
    tradeConfirmationOrder,
    dismissTradeConfirmation: () => setTradeConfirmationOrder(null),
    activeDetailTicker,
    setActiveDetailTicker
  };

  return <TradingContext.Provider value={value}>{children}</TradingContext.Provider>;
};

export const useTrading = (): TradingContextValue => {
  const context = useContext(TradingContext);
  if (!context) {
    throw new Error('useTrading must be used within a TradingProvider');
  }
  return context;
};
