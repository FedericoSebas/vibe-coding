import React, { useState } from 'react';
import { useTrading } from '../context/TradingContext';
import { IntradayInterval, Timeframe } from '../types/market';
import { InteractiveChart } from '../components/InteractiveChart';
import { OrderExecutionSheet } from '../components/OrderExecutionSheet';
import { ArrowLeft, Star, TrendingUp, TrendingDown, Info, Clock } from 'lucide-react';

export const StockDetailScreen: React.FC = () => {
  const {
    selectedStock,
    candles,
    marketDepth,
    selectedTimeframe,
    setTimeframe,
    intradayInterval,
    setIntradayInterval,
    chartType,
    setChartType,
    showSMA,
    toggleSMA,
    showVolume,
    toggleVolume,
    watchlist,
    toggleWatchlist,
    portfolioSummary,
    executeTrade,
    setActiveDetailTicker
  } = useTrading();

  const [tradeSheetOpen, setTradeSheetOpen] = useState(false);
  const [tradeIsBuy, setTradeIsBuy] = useState(true);
  const [showExtHours, setShowExtHours] = useState(true);

  const displayCandles = React.useMemo(() => {
    if (selectedTimeframe !== '1D' || showExtHours) {
      return candles;
    }
    const reg = candles.filter((c) => c.session === 'REGULAR');
    return reg.length > 0 ? reg : candles;
  }, [candles, selectedTimeframe, showExtHours]);

  if (!selectedStock) {
    return (
      <div className="flex items-center justify-center h-full bg-[#080D1A] text-slate-400">
        No stock selected
      </div>
    );
  }

  const isWatchlisted = watchlist.has(selectedStock.symbol);
  const isGain = selectedStock.change >= 0;
  const userPosition = portfolioSummary.positions.find((p) => p.ticker === selectedStock.symbol);

  const timeframes: Timeframe[] = ['1D', '1W', '1M', '1Y', 'ALL'];

  const openTrade = (buy: boolean) => {
    setTradeIsBuy(buy);
    setTradeSheetOpen(true);
  };

  const spread = Math.max(0.01, selectedStock.ask - selectedStock.bid);

  return (
    <div className="flex flex-col h-full bg-[#080D1A] overflow-hidden">
      {/* Top Bar */}
      <div className="bg-[#0F172A] border-b border-[#2B3A58]/50 px-4 py-3 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3">
          <button
            type="button"
            data-testid="detail_back_button"
            onClick={() => setActiveDetailTicker(null)}
            className="p-1.5 rounded-lg hover:bg-[#1E2942] text-slate-300 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-[#F8FAFC] text-base">{selectedStock.symbol}</span>
              <span className="bg-[#1E2942] text-[#00E5FF] text-[10px] font-semibold px-2 py-0.5 rounded">
                {selectedStock.sector.replace('_', ' ')}
              </span>
            </div>
            <div className="text-xs text-slate-400 truncate max-w-xs">{selectedStock.name}</div>
          </div>
        </div>

        <button
          type="button"
          data-testid="detail_star_watchlist"
          onClick={() => toggleWatchlist(selectedStock.symbol)}
          className="p-2 rounded-lg hover:bg-[#1E2942] transition-colors"
        >
          <Star
            className={`w-5 h-5 ${
              isWatchlisted ? 'text-[#FFD166] fill-[#FFD166]' : 'text-slate-500 hover:text-slate-300'
            }`}
          />
        </button>
      </div>

      {/* Main Scrollable Content */}
      <div className="flex-1 overflow-y-auto pb-24">
        {/* Live Price Header */}
        <div className="bg-[#0F172A] px-4 py-3 border-b border-[#2B3A58]/40">
          <div className="flex flex-wrap items-end justify-between gap-2">
            <div>
              <div className="text-2xl sm:text-3xl font-extrabold text-[#F8FAFC] font-mono">
                ${selectedStock.price.toFixed(2)}
              </div>
              <div
                className={`flex items-center gap-1 text-xs font-bold mt-0.5 ${
                  isGain ? 'text-[#00E676]' : 'text-[#FF3D57]'
                }`}
              >
                {isGain ? <TrendingUp className="w-4 h-4" /> : <TrendingDown className="w-4 h-4" />}
                <span>
                  {isGain ? '+' : ''}${selectedStock.change.toFixed(2)} ({isGain ? '+' : ''}
                  {selectedStock.changePercent.toFixed(2)}%) Today
                </span>
              </div>
            </div>

            {/* Bid / Ask Pill */}
            <div className="flex items-center gap-2.5 px-3 py-1.5 bg-[#172036] rounded-lg border border-[#2B3A58]/50 text-xs font-mono">
              <span className="text-[#00E676]">Bid: ${selectedStock.bid.toFixed(2)}</span>
              <span className="text-slate-500">•</span>
              <span className="text-[#FF3D57]">Ask: ${selectedStock.ask.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Timeframe & Controls Toolbar */}
        <div className="bg-[#0F172A] px-4 py-2 border-b border-[#2B3A58]/30 flex flex-col gap-2">
          <div className="flex flex-wrap items-center justify-between gap-2">
            {/* Timeframes */}
            <div className="flex items-center gap-1">
              {timeframes.map((tf) => {
                const isSelected = selectedTimeframe === tf;
                return (
                  <button
                    key={tf}
                    type="button"
                    data-testid={`timeframe_${tf}`}
                    onClick={() => setTimeframe(tf)}
                    className={`px-2.5 py-1 rounded text-xs font-bold transition-all ${
                      isSelected
                        ? 'bg-[#00E5FF] text-[#080D1A]'
                        : 'text-slate-400 hover:text-white hover:bg-[#1E2942]'
                    }`}
                  >
                    {tf}
                  </button>
                );
              })}
            </div>

            {/* Chart Controls */}
            <div className="flex items-center gap-1.5 text-xs">
              <button
                type="button"
                data-testid="chart_type_toggle"
                onClick={() => setChartType(chartType === 'LINE' ? 'CANDLESTICK' : 'LINE')}
                className="px-2 py-1 rounded bg-[#1E2942] hover:bg-[#2B3A58] text-[#00E5FF] font-semibold transition-colors"
              >
                {chartType === 'LINE' ? 'Candles' : 'Line'}
              </button>
              <button
                type="button"
                onClick={toggleSMA}
                className={`px-2 py-1 rounded font-semibold transition-colors ${
                  showSMA ? 'bg-[#FFD166]/20 text-[#FFD166]' : 'bg-[#1E2942] text-slate-400'
                }`}
              >
                SMA 20
              </button>
              <button
                type="button"
                onClick={toggleVolume}
                className={`px-2 py-1 rounded font-semibold transition-colors ${
                  showVolume ? 'bg-[#00E5FF]/20 text-[#00E5FF]' : 'bg-[#1E2942] text-slate-400'
                }`}
              >
                Vol
              </button>
              {selectedTimeframe === '1D' && (
                <button
                  type="button"
                  data-testid="ext_hours_toggle"
                  onClick={() => setShowExtHours(!showExtHours)}
                  className={`px-2 py-1 rounded font-semibold transition-colors border ${
                    showExtHours
                      ? 'bg-[#7C4DFF]/25 text-[#C4B5FD] border-[#7C4DFF]/50'
                      : 'bg-[#1E2942] text-slate-400 border-transparent'
                  }`}
                  title="Toggle Pre-Market & After-Hours extended trading sessions"
                >
                  Ext Hours
                </button>
              )}
            </div>
          </div>

          {/* 1D Intraday Intervals (1 minute, 5 minute, 10 minute) */}
          {selectedTimeframe === '1D' && (
            <div className="flex flex-wrap items-center justify-between gap-2 pt-1 border-t border-[#2B3A58]/30 text-xs">
              <div className="flex items-center gap-2">
                <div className="flex items-center gap-1 text-[11px] text-[#00E5FF] font-semibold">
                  <Clock className="w-3.5 h-3.5" />
                  <span>1D Interval:</span>
                </div>
                <div className="flex items-center gap-1 bg-[#172036] p-0.5 rounded-lg border border-[#2B3A58]/50">
                  {(['1m', '5m', '10m'] as IntradayInterval[]).map((intv) => {
                    const isCur = intradayInterval === intv;
                    const labels: Record<IntradayInterval, string> = {
                      '1m': '1 Min',
                      '5m': '5 Min',
                      '10m': '10 Min'
                    };
                    return (
                      <button
                        key={intv}
                        type="button"
                        data-testid={`timeframe_${intv}`}
                        onClick={() => setIntradayInterval(intv)}
                        className={`px-2.5 py-0.5 rounded-md text-[11px] font-bold transition-all ${
                          isCur
                            ? 'bg-[#00E5FF] text-[#080D1A] shadow-xs'
                            : 'text-slate-300 hover:text-white hover:bg-[#1E2942]'
                        }`}
                      >
                        {labels[intv]}
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Pre / Reg / Post Session Indicators */}
              <div className="flex items-center gap-2.5 text-[10px] text-slate-400 font-mono">
                <span className="flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#A78BFA]" />
                  <span>Pre</span>
                </span>
                <span className="flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#00E676]" />
                  <span>Regular</span>
                </span>
                <span className="flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#FFD166]" />
                  <span>After</span>
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Chart Canvas */}
        <InteractiveChart
          candles={displayCandles}
          chartType={chartType}
          showSMA={showSMA}
          showVolume={showVolume}
          className="border-b border-[#2B3A58]/40"
        />

        <div className="p-4 space-y-4">
          {/* User's Position Card (if owned) */}
          {userPosition && (
            <div
              data-testid="user_position_card"
              className="p-4 bg-[#0F172A] border border-[#2B3A58] rounded-xl"
            >
              <div className="flex items-center justify-between mb-3">
                <span className="text-xs font-bold text-slate-300">Your Position</span>
                <span className="text-xs font-bold text-[#00E5FF]">
                  {userPosition.shares} Shares Owned
                </span>
              </div>
              <div className="grid grid-cols-3 gap-2 text-xs">
                <div>
                  <div className="text-slate-400 text-[11px]">Avg Cost</div>
                  <div className="font-semibold text-white font-mono">
                    ${userPosition.averageCost.toFixed(2)}
                  </div>
                </div>
                <div>
                  <div className="text-slate-400 text-[11px]">Market Value</div>
                  <div className="font-semibold text-white font-mono">
                    ${userPosition.marketValue.toFixed(2)}
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-slate-400 text-[11px]">Unrealized Return</div>
                  <div
                    className={`font-bold font-mono ${
                      userPosition.unrealizedPnL >= 0 ? 'text-[#00E676]' : 'text-[#FF3D57]'
                    }`}
                  >
                    {userPosition.unrealizedPnL >= 0 ? '+' : ''}${userPosition.unrealizedPnL.toFixed(2)} (
                    {userPosition.unrealizedPnL >= 0 ? '+' : ''}
                    {userPosition.unrealizedPnLPercent.toFixed(2)}%)
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Level 2 Order Book Depth Ladder */}
          <div className="p-4 bg-[#0F172A] border border-[#2B3A58] rounded-xl">
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-slate-200">Level 2 Order Book (Depth)</span>
              <span className="text-xs font-semibold text-[#FFD166] font-mono">
                Spread: ${spread.toFixed(2)}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-4 text-xs font-mono">
              {/* Bid (Buyers) */}
              <div>
                <div className="text-[10px] font-bold text-[#00E676] mb-1.5 flex justify-between">
                  <span>SIZE</span>
                  <span>BID (BUYERS)</span>
                </div>
                <div className="space-y-1">
                  {marketDepth.map((level, i) => (
                    <div key={i} className="flex justify-between text-slate-300">
                      <span className="text-slate-400">{level.bidSize}</span>
                      <span className="text-[#00E676] font-semibold">${level.bidPrice.toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Ask (Sellers) */}
              <div>
                <div className="text-[10px] font-bold text-[#FF3D57] mb-1.5 flex justify-between">
                  <span>ASK (SELLERS)</span>
                  <span>SIZE</span>
                </div>
                <div className="space-y-1">
                  {marketDepth.map((level, i) => (
                    <div key={i} className="flex justify-between text-slate-300">
                      <span className="text-[#FF3D57] font-semibold">${level.askPrice.toFixed(2)}</span>
                      <span className="text-slate-400">{level.askSize}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Key Financial Stats Grid */}
          <div className="p-4 bg-[#0F172A] border border-[#2B3A58] rounded-xl">
            <h3 className="text-xs font-bold text-slate-200 mb-3">Market Statistics</h3>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
              <div>
                <div className="text-slate-400 text-[11px]">Market Cap</div>
                <div className="font-semibold text-white font-mono">{selectedStock.marketCap}</div>
              </div>
              <div>
                <div className="text-slate-400 text-[11px]">P/E Ratio</div>
                <div className="font-semibold text-white font-mono">{selectedStock.peRatio}x</div>
              </div>
              <div>
                <div className="text-slate-400 text-[11px]">Day Range</div>
                <div className="font-semibold text-white font-mono">
                  ${selectedStock.dayLow.toFixed(2)} - ${selectedStock.dayHigh.toFixed(2)}
                </div>
              </div>
              <div>
                <div className="text-slate-400 text-[11px]">Day Volume</div>
                <div className="font-semibold text-white font-mono">
                  {selectedStock.volume.toLocaleString()}
                </div>
              </div>
              <div>
                <div className="text-slate-400 text-[11px]">Prev Close</div>
                <div className="font-semibold text-white font-mono">
                  ${selectedStock.previousClose.toFixed(2)}
                </div>
              </div>
              <div>
                <div className="text-slate-400 text-[11px]">Spread</div>
                <div className="font-semibold text-white font-mono">${spread.toFixed(2)}</div>
              </div>
            </div>
          </div>

          {/* Educational Insight Card */}
          <div className="p-4 bg-[#172036] border border-[#2B3A58]/70 rounded-xl">
            <div className="flex items-center gap-2 mb-2 text-[#00E5FF]">
              <Info className="w-4 h-4 shrink-0" />
              <span className="text-xs font-bold">Educational Trading Context</span>
            </div>
            <p className="text-xs text-slate-300 leading-relaxed">
              {selectedStock.symbol} is a constituent in the NASDAQ-100 {selectedStock.sector.replace('_', ' ')} sector with a valuation of {selectedStock.marketCap}. With a P/E ratio of {selectedStock.peRatio}x, investors are pricing in future technological moat and cash flow expansion. In paper trading, test setting Limit Orders near recent support to avoid paying market ask premiums.
            </p>
          </div>
        </div>
      </div>

      {/* Sticky Bottom Action Buttons */}
      <div className="fixed bottom-0 left-0 right-0 p-3 bg-[#0F172A] border-t border-[#2B3A58] flex gap-3 z-30">
        <button
          type="button"
          data-testid="detail_sell_button"
          onClick={() => openTrade(false)}
          className="flex-1 py-3 rounded-xl bg-[#FF3D57] hover:bg-[#FF3D57]/90 text-white font-bold text-sm transition-all shadow-md"
        >
          Sell {selectedStock.symbol}
        </button>
        <button
          type="button"
          data-testid="detail_buy_button"
          onClick={() => openTrade(true)}
          className="flex-1 py-3 rounded-xl bg-[#00E676] hover:bg-[#00E676]/90 text-[#080D1A] font-bold text-sm transition-all shadow-md"
        >
          Buy {selectedStock.symbol}
        </button>
      </div>

      {/* Order Execution Sheet */}
      {tradeSheetOpen && (
        <OrderExecutionSheet
          stock={selectedStock}
          initialIsBuy={tradeIsBuy}
          portfolioSummary={portfolioSummary}
          onDismiss={() => setTradeSheetOpen(false)}
          onExecuteTrade={executeTrade}
        />
      )}
    </div>
  );
};
