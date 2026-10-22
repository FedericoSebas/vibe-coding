import React, { useState } from 'react';
import { useTrading } from '../context/TradingContext';
import { ResetCapitalModal } from '../components/ResetCapitalModal';
import { RefreshCw, TrendingUp, TrendingDown, Wallet } from 'lucide-react';

export const PortfolioScreen: React.FC = () => {
  const {
    portfolioSummary,
    resetPortfolio,
    selectStock,
    setActiveDetailTicker,
    setCurrentTab
  } = useTrading();

  const [resetModalOpen, setResetModalOpen] = useState(false);

  const isTotalGain = portfolioSummary.totalUnrealizedPnL >= 0;
  const isDayGain = portfolioSummary.todayPnL >= 0;

  const cashRatio = portfolioSummary.totalEquity > 0
    ? Math.min(1, Math.max(0, portfolioSummary.cashBalance / portfolioSummary.totalEquity))
    : 1;
  const stockRatio = portfolioSummary.totalEquity > 0
    ? Math.min(1, Math.max(0, portfolioSummary.positionsValue / portfolioSummary.totalEquity))
    : 0;

  const handleStockClick = (symbol: string) => {
    selectStock(symbol);
    setActiveDetailTicker(symbol);
  };

  return (
    <div className="flex flex-col h-full bg-[#080D1A] overflow-hidden">
      {/* Header */}
      <div className="bg-[#0F172A] border-b border-[#2B3A58]/50 px-4 py-3 flex items-center justify-between shrink-0">
        <div>
          <h1 className="text-lg font-bold text-[#F8FAFC]">Simulated Portfolio</h1>
          <p className="text-xs text-slate-400">Live Net Worth & Risk Allocation</p>
        </div>

        <button
          type="button"
          data-testid="reset_portfolio_button"
          onClick={() => setResetModalOpen(true)}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-[#00E5FF]/40 text-[#00E5FF] hover:bg-[#00E5FF]/10 text-xs font-semibold transition-colors"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Reset Capital</span>
        </button>
      </div>

      {/* Main Content */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 pb-24 sm:pb-6">
        {/* Total Equity Card */}
        <div className="p-5 bg-[#0F172A] border border-[#2B3A58] rounded-2xl shadow-lg">
          <div className="text-[11px] font-bold text-slate-400 tracking-wider uppercase mb-1">
            Total Simulated Equity
          </div>
          <div className="text-3xl font-extrabold text-[#F8FAFC] font-mono">
            ${portfolioSummary.totalEquity.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>

          <div className="flex items-center gap-1 mt-1.5">
            {isTotalGain ? (
              <TrendingUp className="w-4 h-4 text-[#00E676]" />
            ) : (
              <TrendingDown className="w-4 h-4 text-[#FF3D57]" />
            )}
            <span
              className={`text-xs font-bold ${
                isTotalGain ? 'text-[#00E676]' : 'text-[#FF3D57]'
              }`}
            >
              {isTotalGain ? '+' : ''}${portfolioSummary.totalUnrealizedPnL.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} (
              {isTotalGain ? '+' : ''}{portfolioSummary.totalUnrealizedPnLPercent.toFixed(2)}%) All-Time
            </span>
          </div>

          {/* Breakdown Row */}
          <div className="grid grid-cols-3 gap-2 mt-4 p-3 bg-[#172036] rounded-xl border border-[#2B3A58]/50 text-xs">
            <div>
              <div className="text-slate-400 text-[11px]">Buying Power (Cash)</div>
              <div className="font-bold text-[#00E5FF] font-mono mt-0.5">
                ${portfolioSummary.cashBalance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </div>
            </div>
            <div>
              <div className="text-slate-400 text-[11px]">Invested in Stocks</div>
              <div className="font-bold text-white font-mono mt-0.5">
                ${portfolioSummary.positionsValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </div>
            </div>
            <div className="text-right">
              <div className="text-slate-400 text-[11px]">Today's P&L</div>
              <div
                className={`font-bold font-mono mt-0.5 ${
                  isDayGain ? 'text-[#00E676]' : 'text-[#FF3D57]'
                }`}
              >
                {isDayGain ? '+' : ''}${portfolioSummary.todayPnL.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </div>
            </div>
          </div>
        </div>

        {/* Portfolio Allocation Segmented Bar */}
        <div className="p-4 bg-[#0F172A] border border-[#2B3A58] rounded-xl">
          <h2 className="text-xs font-bold text-[#F8FAFC] mb-2">Portfolio Allocation</h2>
          <div className="w-full h-2.5 bg-[#1E2942] rounded-full overflow-hidden flex">
            <div
              style={{ width: `${cashRatio * 100}%` }}
              className="bg-[#00E5FF] h-full transition-all duration-300"
            />
            <div
              style={{ width: `${stockRatio * 100}%` }}
              className="bg-[#FFD166] h-full transition-all duration-300"
            />
          </div>
          <div className="flex justify-between items-center text-xs mt-2 text-slate-400">
            <div className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-[#00E5FF]" />
              <span>Cash: {Math.round(cashRatio * 100)}%</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-[#FFD166]" />
              <span>Equities: {Math.round(stockRatio * 100)}%</span>
            </div>
          </div>
        </div>

        {/* Active Positions */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-sm font-bold text-white">
              Active Positions ({portfolioSummary.positions.length})
            </h2>
          </div>

          {portfolioSummary.positions.length === 0 ? (
            <div className="p-8 bg-[#0F172A] border border-[#2B3A58] rounded-2xl text-center">
              <div className="w-12 h-12 rounded-full bg-[#1E2942] flex items-center justify-center text-[#00E5FF] mx-auto mb-3">
                <Wallet className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-white text-base">No Open Positions</h3>
              <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
                You have ${portfolioSummary.cashBalance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} in simulated capital ready to trade.
              </p>
              <button
                type="button"
                data-testid="explore_nasdaq_stocks_button"
                onClick={() => setCurrentTab('MARKET')}
                className="mt-4 px-4 py-2 bg-[#00E5FF] hover:bg-[#00E5FF]/90 text-[#080D1A] font-bold text-xs rounded-xl shadow-md transition-all"
              >
                Explore NASDAQ Stocks
              </button>
            </div>
          ) : (
            <div className="space-y-2">
              {portfolioSummary.positions.map((pos) => {
                const isPosGain = pos.unrealizedPnL >= 0;
                return (
                  <div
                    key={pos.ticker}
                    data-testid={`position_item_${pos.ticker}`}
                    onClick={() => handleStockClick(pos.ticker)}
                    className="p-3.5 bg-[#0F172A] hover:bg-[#172036] border border-[#2B3A58]/70 rounded-xl cursor-pointer transition-colors"
                  >
                    <div className="flex items-center justify-between">
                      <div>
                        <div className="flex items-center gap-1.5">
                          <span className="font-bold text-white text-sm">{pos.ticker}</span>
                          <span className="text-xs text-slate-400">• {pos.shares} shares</span>
                        </div>
                        <div className="text-xs text-slate-400 truncate max-w-xs">
                          {pos.companyName}
                        </div>
                      </div>

                      <div className="text-right">
                        <div className="font-bold text-white text-sm font-mono">
                          ${pos.marketValue.toFixed(2)}
                        </div>
                        <div
                          className={`text-xs font-semibold ${
                            isPosGain ? 'text-[#00E676]' : 'text-[#FF3D57]'
                          }`}
                        >
                          {isPosGain ? '+' : ''}${pos.unrealizedPnL.toFixed(2)} ({isPosGain ? '+' : ''}
                          {pos.unrealizedPnLPercent.toFixed(2)}%)
                        </div>
                      </div>
                    </div>

                    <div className="grid grid-cols-3 gap-2 mt-2 pt-2 border-t border-[#2B3A58]/40 text-[11px] text-slate-400 font-mono">
                      <div>Avg Cost: ${pos.averageCost.toFixed(2)}</div>
                      <div className="text-center font-semibold text-white">
                        Cur: ${pos.currentPrice.toFixed(2)}
                      </div>
                      <div className="text-right">Invested: ${pos.totalInvested.toFixed(2)}</div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {resetModalOpen && (
        <ResetCapitalModal
          onDismiss={() => setResetModalOpen(false)}
          onConfirm={(amount) => {
            resetPortfolio(amount);
            setResetModalOpen(false);
          }}
        />
      )}
    </div>
  );
};
