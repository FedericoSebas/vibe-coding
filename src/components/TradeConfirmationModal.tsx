import React from 'react';
import { OrderEntity } from '../types/market';
import { CheckCircle2 } from 'lucide-react';

interface TradeConfirmationModalProps {
  order: OrderEntity;
  onDismiss: () => void;
}

export const TradeConfirmationModal: React.FC<TradeConfirmationModalProps> = ({
  order,
  onDismiss
}) => {
  const isBuy = order.orderType.includes('BUY');
  const isLimit = order.orderType.includes('LIMIT');

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="w-full max-w-sm bg-[#0F172A] border border-[#2B3A58] rounded-2xl p-6 text-center shadow-2xl">
        {/* Success Icon */}
        <div className="flex justify-center mb-3">
          <div
            className={`w-14 h-14 rounded-full flex items-center justify-center ${
              isBuy ? 'bg-[#00E676]/20 text-[#00E676]' : 'bg-[#00E5FF]/20 text-[#00E5FF]'
            }`}
          >
            <CheckCircle2 className="w-8 h-8" />
          </div>
        </div>

        <h3 className="text-xl font-bold text-[#F8FAFC]">
          {isLimit ? 'Limit Order Placed!' : 'Order Filled!'}
        </h3>
        <p className="text-sm font-semibold text-slate-300 mt-1">
          {isBuy ? 'Bought' : 'Sold'} {order.shares} shares of {order.ticker}
        </p>

        {/* Order Details Card */}
        <div className="my-4 p-3 bg-[#172036] rounded-xl border border-[#2B3A58]/60 text-left space-y-2 text-xs">
          <div className="flex justify-between">
            <span className="text-slate-400">Order Type</span>
            <span className="font-semibold text-[#00E5FF]">{order.orderType.replace('_', ' ')}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-400">
              {isLimit ? 'Target Limit Price' : 'Executed Fill Price'}
            </span>
            <span className="font-bold text-white font-mono">
              ${(order.executionPrice ?? order.limitPrice ?? 0).toFixed(2)}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-400">Total Value</span>
            <span className={`font-bold font-mono ${isBuy ? 'text-[#00E676]' : 'text-[#FFD166]'}`}>
              ${order.totalAmount.toFixed(2)}
            </span>
          </div>
          {order.realizedPnL !== 0 && (
            <div className="flex justify-between pt-1 border-t border-[#2B3A58]/40">
              <span className="text-slate-400">Realized Gain/Loss</span>
              <span
                className={`font-bold font-mono ${
                  order.realizedPnL >= 0 ? 'text-[#00E676]' : 'text-[#FF3D57]'
                }`}
              >
                {order.realizedPnL >= 0 ? '+' : ''}${order.realizedPnL.toFixed(2)}
              </span>
            </div>
          )}
        </div>

        <p className="text-[11px] text-slate-400 leading-relaxed mb-5">
          {isLimit
            ? 'Educational note: Limit orders remain in your Active Orders tab until market conditions reach your price target.'
            : 'Educational note: Market orders take immediate liquidity from the book. Your portfolio metrics have been updated.'}
        </p>

        <button
          onClick={onDismiss}
          data-testid="dismiss_confirmation_button"
          className="w-full py-2.5 bg-[#00E5FF] hover:bg-[#00E5FF]/90 text-[#080D1A] font-bold text-sm rounded-xl transition-all shadow-md"
        >
          Done
        </button>
      </div>
    </div>
  );
};
