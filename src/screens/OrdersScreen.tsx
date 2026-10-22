import React, { useState } from 'react';
import { useTrading } from '../context/TradingContext';
import { OrderEntity } from '../types/market';
import { Receipt } from 'lucide-react';

export const OrdersScreen: React.FC = () => {
  const {
    orderHistory,
    pendingOrders,
    quotes,
    cancelPendingOrder,
    selectStock,
    setActiveDetailTicker
  } = useTrading();

  const [selectedTab, setSelectedTab] = useState<0 | 1>(0);

  const filledOrders = orderHistory.filter((o) => o.status === 'FILLED');

  const handleStockClick = (symbol: string) => {
    selectStock(symbol);
    setActiveDetailTicker(symbol);
  };

  return (
    <div className="flex flex-col h-full bg-[#080D1A] overflow-hidden">
      {/* Header */}
      <div className="bg-[#0F172A] border-b border-[#2B3A58]/50 px-4 py-3 shrink-0">
        <h1 className="text-lg font-bold text-[#F8FAFC]">Trade Orders & Journal</h1>
        <p className="text-xs text-slate-400">Track active pending limit orders and execution records</p>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-[#2B3A58]/60 bg-[#0F172A] px-4 shrink-0">
        <button
          type="button"
          data-testid="tab_pending_orders"
          onClick={() => setSelectedTab(0)}
          className={`flex items-center gap-2 py-3 px-3 text-xs font-semibold border-b-2 transition-all ${
            selectedTab === 0
              ? 'border-[#00E5FF] text-[#00E5FF]'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <span>Active Limit Orders</span>
          {pendingOrders.length > 0 && (
            <span className="px-1.5 py-0.5 rounded-full bg-[#FFD166] text-[#080D1A] text-[10px] font-bold">
              {pendingOrders.length}
            </span>
          )}
        </button>

        <button
          type="button"
          data-testid="tab_execution_history"
          onClick={() => setSelectedTab(1)}
          className={`py-3 px-3 text-xs font-semibold border-b-2 transition-all ${
            selectedTab === 1
              ? 'border-[#00E5FF] text-[#00E5FF]'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          Execution History ({filledOrders.length})
        </button>
      </div>

      {/* List Content */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3 pb-24 sm:pb-6">
        {selectedTab === 0 ? (
          pendingOrders.length === 0 ? (
            <div className="p-8 bg-[#0F172A] border border-[#2B3A58] rounded-2xl text-center">
              <div className="w-12 h-12 rounded-full bg-[#1E2942] flex items-center justify-center text-[#00E5FF] mx-auto mb-3">
                <Receipt className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-white text-base">No Pending Limit Orders</h3>
              <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto leading-relaxed">
                When you place a Limit Order, it stays queued here until the market price reaches your specified target.
              </p>
            </div>
          ) : (
            pendingOrders.map((order) => {
              const isBuy = order.orderType.includes('BUY');
              const curQuote = quotes[order.ticker];
              const curPrice = curQuote ? curQuote.price : 0;

              return (
                <div
                  key={order.id}
                  data-testid={`pending_order_${order.id}`}
                  onClick={() => handleStockClick(order.ticker)}
                  className="p-4 bg-[#0F172A] border border-[#2B3A58] rounded-xl hover:border-[#00E5FF]/40 cursor-pointer transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span
                        className={`text-[11px] font-bold px-2 py-0.5 rounded ${
                          isBuy
                            ? 'bg-[#00E676]/15 text-[#00E676]'
                            : 'bg-[#FF3D57]/15 text-[#FF3D57]'
                        }`}
                      >
                        {order.orderType.replace('_', ' ')}
                      </span>
                      <span className="font-bold text-white text-sm">{order.ticker}</span>
                    </div>

                    <button
                      type="button"
                      data-testid={`cancel_order_${order.id}`}
                      onClick={(e) => {
                        e.stopPropagation();
                        cancelPendingOrder(order.id);
                      }}
                      className="px-2.5 py-1 text-xs font-semibold rounded-lg border border-[#FF3D57]/50 text-[#FF3D57] hover:bg-[#FF3D57]/15 transition-colors"
                    >
                      Cancel
                    </button>
                  </div>

                  <div className="grid grid-cols-4 gap-2 mt-3 text-xs font-mono">
                    <div>
                      <div className="text-[10px] text-slate-400 font-sans">Shares</div>
                      <div className="font-semibold text-white">{order.shares}</div>
                    </div>
                    <div>
                      <div className="text-[10px] text-slate-400 font-sans">Limit Price</div>
                      <div className="font-semibold text-[#00E5FF]">
                        ${(order.limitPrice ?? 0).toFixed(2)}
                      </div>
                    </div>
                    <div>
                      <div className="text-[10px] text-slate-400 font-sans">Current Market</div>
                      <div className="font-semibold text-white">${curPrice.toFixed(2)}</div>
                    </div>
                    <div className="text-right">
                      <div className="text-[10px] text-slate-400 font-sans">Reserved</div>
                      <div className="font-semibold text-[#FFD166]">
                        ${order.totalAmount.toFixed(2)}
                      </div>
                    </div>
                  </div>

                  <div className="mt-2 text-[11px] text-slate-400 pt-2 border-t border-[#2B3A58]/40">
                    {isBuy
                      ? `Condition: Executes when market Ask drops to $${(order.limitPrice ?? 0).toFixed(2)} or lower`
                      : `Condition: Executes when market Bid reaches $${(order.limitPrice ?? 0).toFixed(2)} or higher`}
                  </div>
                </div>
              );
            })
          )
        ) : filledOrders.length === 0 ? (
          <div className="p-8 bg-[#0F172A] border border-[#2B3A58] rounded-2xl text-center">
            <div className="w-12 h-12 rounded-full bg-[#1E2942] flex items-center justify-center text-[#00E5FF] mx-auto mb-3">
              <Receipt className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-white text-base">No Trade History Yet</h3>
            <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto leading-relaxed">
              Executed trades and real-time paper order fills will appear here with timestamps, prices, and realized gains/losses.
            </p>
          </div>
        ) : (
          filledOrders.map((order) => {
            const isBuy = order.orderType.includes('BUY');
            const timeStr = new Date(order.timestamp).toLocaleString('en-US', {
              month: 'short',
              day: 'numeric',
              year: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            });

            return (
              <div
                key={order.id}
                onClick={() => handleStockClick(order.ticker)}
                className="p-3.5 bg-[#0F172A] border border-[#2B3A58]/60 rounded-xl hover:border-[#2B3A58] cursor-pointer transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                        isBuy
                          ? 'bg-[#00E676]/15 text-[#00E676]'
                          : 'bg-[#FF3D57]/15 text-[#FF3D57]'
                      }`}
                    >
                      {order.orderType.replace('_', ' ')}
                    </span>
                    <span className="font-bold text-white text-sm">{order.ticker}</span>
                  </div>
                  <span className="font-bold text-white text-sm font-mono">
                    ${order.totalAmount.toFixed(2)}
                  </span>
                </div>

                <div className="flex items-center justify-between text-xs mt-2 text-slate-300 font-mono">
                  <span>
                    {order.shares} shares @ ${(order.executionPrice ?? 0).toFixed(2)}
                  </span>

                  {order.realizedPnL !== 0 && (
                    <span
                      className={`font-semibold ${
                        order.realizedPnL >= 0 ? 'text-[#00E676]' : 'text-[#FF3D57]'
                      }`}
                    >
                      Realized: {order.realizedPnL >= 0 ? '+' : ''}${order.realizedPnL.toFixed(2)}
                    </span>
                  )}
                </div>

                <div className="text-[10px] text-slate-500 mt-1">{timeStr}</div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
