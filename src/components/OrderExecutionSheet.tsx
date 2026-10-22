import React, { useState } from 'react';
import { StockQuote, PortfolioSummary } from '../types/market';
import { X, Plus, Minus, Info } from 'lucide-react';

interface OrderExecutionSheetProps {
  stock: StockQuote;
  initialIsBuy?: boolean;
  portfolioSummary: PortfolioSummary;
  onDismiss: () => void;
  onExecuteTrade: (
    ticker: string,
    companyName: string,
    isBuy: boolean,
    orderType: string,
    shares: number,
    limitPrice: number | null
  ) => void;
}

export const OrderExecutionSheet: React.FC<OrderExecutionSheetProps> = ({
  stock,
  initialIsBuy = true,
  portfolioSummary,
  onDismiss,
  onExecuteTrade
}) => {
  const [isBuy, setIsBuy] = useState(initialIsBuy);
  const [orderType, setOrderType] = useState<'MARKET' | 'LIMIT'>('MARKET');
  const [sharesText, setSharesText] = useState('10');
  const [limitPriceText, setLimitPriceText] = useState(stock.price.toFixed(2));

  const cashAvailable = portfolioSummary.cashBalance;
  const currentPosition = portfolioSummary.positions.find((p) => p.ticker === stock.symbol);
  const sharesOwned = currentPosition ? currentPosition.shares : 0;

  const sharesNum = parseFloat(sharesText) || 0;
  const activePrice = isBuy ? stock.ask : stock.bid;
  const targetPrice = orderType === 'LIMIT' ? (parseFloat(limitPriceText) || activePrice) : activePrice;
  const estimatedTotal = Math.round(sharesNum * targetPrice * 100) / 100;

  const hasSufficientBalance = isBuy
    ? cashAvailable >= estimatedTotal && estimatedTotal > 0
    : sharesOwned >= sharesNum && sharesNum > 0;

  const handlePercentage = (pct: number) => {
    if (isBuy) {
      const allocatableCash = cashAvailable * (pct / 100);
      const calcShares = Math.max(1, Math.floor(allocatableCash / targetPrice));
      setSharesText(calcShares.toString());
    } else {
      const calcShares = Math.max(0, Math.floor(sharesOwned * (pct / 100)));
      setSharesText(calcShares.toString());
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (sharesNum > 0 && hasSufficientBalance) {
      onExecuteTrade(
        stock.symbol,
        stock.name,
        isBuy,
        orderType,
        sharesNum,
        orderType === 'LIMIT' ? targetPrice : null
      );
      onDismiss();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/70 backdrop-blur-xs p-0 sm:p-4">
      <div className="w-full max-w-lg bg-[#0F172A] border border-[#2B3A58] rounded-t-2xl sm:rounded-2xl shadow-2xl overflow-hidden animate-in fade-in slide-in-from-bottom duration-200">
        {/* Drag handle for mobile */}
        <div className="sm:hidden flex justify-center pt-3 pb-1">
          <div className="w-10 h-1 bg-[#2B3A58] rounded-full" />
        </div>

        {/* Header */}
        <div className="px-5 py-4 border-b border-[#2B3A58]/60 flex items-center justify-between">
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xl font-bold text-[#F8FAFC]">{stock.symbol}</span>
              <span className="bg-[#1E2942] text-[#00E5FF] text-[11px] font-semibold px-2 py-0.5 rounded">
                NASDAQ
              </span>
            </div>
            <div className="text-xs text-slate-400 truncate max-w-xs">{stock.name}</div>
          </div>
          <button
            onClick={onDismiss}
            className="p-1.5 rounded-full hover:bg-[#1E2942] text-slate-400 hover:text-white transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4 max-h-[85vh] overflow-y-auto">
          {/* Buy vs Sell Toggle */}
          <div className="grid grid-cols-2 p-1 bg-[#172036] rounded-xl border border-[#2B3A58]/50">
            <button
              type="button"
              data-testid="trade_toggle_buy"
              onClick={() => setIsBuy(true)}
              className={`py-2.5 rounded-lg font-bold text-sm transition-all ${
                isBuy
                  ? 'bg-[#00E676] text-[#080D1A] shadow-md'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              BUY
            </button>
            <button
              type="button"
              data-testid="trade_toggle_sell"
              onClick={() => setIsBuy(false)}
              className={`py-2.5 rounded-lg font-bold text-sm transition-all ${
                !isBuy
                  ? 'bg-[#FF3D57] text-[#F8FAFC] shadow-md'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              SELL
            </button>
          </div>

          {/* Order Type Toggle */}
          <div className="grid grid-cols-2 gap-2">
            <button
              type="button"
              data-testid="order_type_MARKET"
              onClick={() => setOrderType('MARKET')}
              className={`py-2 rounded-lg text-xs font-semibold border transition-all ${
                orderType === 'MARKET'
                  ? 'bg-[#00E5FF]/15 border-[#00E5FF] text-[#00E5FF]'
                  : 'bg-[#1E2942] border-[#2B3A58] text-slate-400 hover:text-slate-200'
              }`}
            >
              Market Order
            </button>
            <button
              type="button"
              data-testid="order_type_LIMIT"
              onClick={() => setOrderType('LIMIT')}
              className={`py-2 rounded-lg text-xs font-semibold border transition-all ${
                orderType === 'LIMIT'
                  ? 'bg-[#00E5FF]/15 border-[#00E5FF] text-[#00E5FF]'
                  : 'bg-[#1E2942] border-[#2B3A58] text-slate-400 hover:text-slate-200'
              }`}
            >
              Limit Order
            </button>
          </div>

          {/* Price Row */}
          <div className="flex items-center justify-between p-3 bg-[#172036] rounded-xl border border-[#2B3A58]/40">
            <span className="text-xs text-slate-300">
              {orderType === 'MARKET'
                ? `Current ${isBuy ? 'Ask' : 'Bid'} Price`
                : 'Limit Target Price'}
            </span>
            {orderType === 'MARKET' ? (
              <span className="text-base font-bold text-[#F8FAFC] font-mono">
                ${targetPrice.toFixed(2)}
              </span>
            ) : (
              <div className="flex items-center gap-1 bg-[#1E2942] border border-[#2B3A58] focus-within:border-[#00E5FF] rounded-lg px-2 py-1 w-32">
                <span className="text-xs text-slate-400">$</span>
                <input
                  type="number"
                  step="0.01"
                  data-testid="limit_price_input"
                  value={limitPriceText}
                  onChange={(e) => setLimitPriceText(e.target.value)}
                  className="w-full bg-transparent text-sm font-bold text-white focus:outline-none font-mono text-right"
                />
              </div>
            )}
          </div>

          {/* Shares Input with Stepper */}
          <div className="flex items-center justify-between p-3 bg-[#172036] rounded-xl border border-[#2B3A58]/40">
            <span className="text-xs text-slate-300">Number of Shares</span>
            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={() => {
                  const cur = parseInt(sharesText, 10) || 0;
                  if (cur > 1) setSharesText((cur - 1).toString());
                }}
                className="w-8 h-8 rounded-lg bg-[#1E2942] hover:bg-[#2B3A58] flex items-center justify-center text-slate-200 transition-colors"
              >
                <Minus className="w-4 h-4" />
              </button>
              <input
                type="number"
                min="1"
                data-testid="shares_input"
                value={sharesText}
                onChange={(e) => setSharesText(e.target.value)}
                className="w-16 bg-[#1E2942] border border-[#2B3A58] focus:border-[#00E5FF] rounded-lg py-1 text-center font-bold text-white text-sm focus:outline-none"
              />
              <button
                type="button"
                onClick={() => {
                  const cur = parseInt(sharesText, 10) || 0;
                  setSharesText((cur + 1).toString());
                }}
                className="w-8 h-8 rounded-lg bg-[#1E2942] hover:bg-[#2B3A58] flex items-center justify-center text-slate-200 transition-colors"
              >
                <Plus className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Quick percentage chips */}
          <div className="grid grid-cols-4 gap-2">
            {[25, 50, 75, 100].map((pct) => (
              <button
                key={pct}
                type="button"
                onClick={() => handlePercentage(pct)}
                className="py-1.5 text-xs font-semibold bg-[#1E2942] hover:bg-[#2B3A58] text-slate-300 rounded-lg transition-colors"
              >
                {pct === 100 ? 'MAX' : `${pct}%`}
              </button>
            ))}
          </div>

          {/* Financial Balance Summary Card */}
          <div className="p-3 bg-[#172036] rounded-xl border border-[#2B3A58]/60 space-y-2">
            <div className="flex justify-between text-xs">
              <span className="text-slate-400">
                {isBuy ? 'Available Simulated Cash' : 'Shares Currently Owned'}
              </span>
              <span className="font-semibold text-white">
                {isBuy
                  ? `$${cashAvailable.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                  : `${sharesOwned} shares`}
              </span>
            </div>
            <div className="flex justify-between items-baseline pt-1 border-t border-[#2B3A58]/50">
              <span className="text-xs font-semibold text-slate-200">
                {isBuy ? 'Estimated Total Cost' : 'Estimated Proceeds'}
              </span>
              <span className={`text-base font-bold font-mono ${isBuy ? 'text-[#00E676]' : 'text-[#00E5FF]'}`}>
                ${estimatedTotal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </span>
            </div>
          </div>

          {/* Educational Mechanics Explainer Box */}
          <div className="p-3 bg-[#1E2942]/60 rounded-xl border border-[#2B3A58]/40 flex gap-2.5 items-start">
            <Info className="w-4 h-4 text-[#FFD166] shrink-0 mt-0.5" />
            <p className="text-[11px] text-slate-300 leading-relaxed">
              {orderType === 'MARKET' ? (
                isBuy ? (
                  <>
                    <strong className="text-[#FFD166]">Educational Tip:</strong> Market Buy crosses the spread to buy at the current Ask (${stock.ask.toFixed(2)}). Real-world markets can experience slippage.
                  </>
                ) : (
                  <>
                    <strong className="text-[#FFD166]">Educational Tip:</strong> Market Sell executes immediately at current Bid (${stock.bid.toFixed(2)}). Zero brokerage fees simulated.
                  </>
                )
              ) : (
                isBuy ? (
                  <>
                    <strong className="text-[#FFD166]">Educational Tip:</strong> Buy Limit (${targetPrice.toFixed(2)}) guarantees you pay no higher than this price. Funds are reserved until filled or cancelled.
                  </>
                ) : (
                  <>
                    <strong className="text-[#FFD166]">Educational Tip:</strong> Sell Limit (${targetPrice.toFixed(2)}) guarantees you sell at or above this price. Order will trigger automatically.
                  </>
                )
              )}
            </p>
          </div>

          {/* CTA Submit Button */}
          <button
            type="submit"
            disabled={!hasSufficientBalance || sharesNum <= 0}
            data-testid="confirm_trade_button"
            className={`w-full py-3.5 rounded-xl font-bold text-sm transition-all shadow-lg ${
              !hasSufficientBalance || sharesNum <= 0
                ? 'bg-[#1E2942] text-slate-500 cursor-not-allowed border border-[#2B3A58]'
                : isBuy
                ? 'bg-[#00E676] hover:bg-[#00E676]/90 text-[#080D1A]'
                : 'bg-[#FF3D57] hover:bg-[#FF3D57]/90 text-white'
            }`}
          >
            {!hasSufficientBalance ? (
              isBuy ? `Insufficient Cash ($${estimatedTotal.toFixed(2)} required)` : 'Not Enough Shares'
            ) : (
              `Confirm ${isBuy ? 'Buy' : 'Sell'} ($${estimatedTotal.toFixed(2)})`
            )}
          </button>
        </form>
      </div>
    </div>
  );
};
