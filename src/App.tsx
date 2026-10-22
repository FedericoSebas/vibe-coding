import React from 'react';
import { useTrading } from './context/TradingContext';
import { MarketScreen } from './screens/MarketScreen';
import { StockDetailScreen } from './screens/StockDetailScreen';
import { PortfolioScreen } from './screens/PortfolioScreen';
import { OrdersScreen } from './screens/OrdersScreen';
import { AcademyScreen } from './screens/AcademyScreen';
import { TradeConfirmationModal } from './components/TradeConfirmationModal';
import { LineChart, Wallet, Receipt, GraduationCap, X } from 'lucide-react';

export const AppContent: React.FC = () => {
  const {
    currentTab,
    setCurrentTab,
    activeDetailTicker,
    activeLessonId,
    pendingOrders,
    feedbackMessage,
    dismissFeedbackMessage,
    tradeConfirmationOrder,
    dismissTradeConfirmation
  } = useTrading();

  // Show bottom navigation bar when not inside a full stock detail or lesson detail
  const showNav = activeDetailTicker === null && activeLessonId === null;

  return (
    <div className="flex flex-col h-screen w-screen bg-[#080D1A] text-[#F8FAFC] overflow-hidden select-none">
      {/* Toast Feedback Notification */}
      {feedbackMessage && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 max-w-md w-11/12 bg-[#1E2942] border border-[#00E5FF]/40 text-[#F8FAFC] px-4 py-2.5 rounded-xl shadow-2xl flex items-center justify-between gap-3 text-xs font-semibold animate-in fade-in slide-in-from-top duration-200">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-[#00E5FF]" />
            <span>{feedbackMessage}</span>
          </div>
          <button
            onClick={dismissFeedbackMessage}
            className="p-1 rounded-md text-slate-400 hover:text-white"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      )}

      {/* Main View Area */}
      <div className="flex-1 overflow-hidden relative">
        {activeDetailTicker !== null ? (
          <StockDetailScreen />
        ) : (
          <>
            {currentTab === 'MARKET' && <MarketScreen />}
            {currentTab === 'PORTFOLIO' && <PortfolioScreen />}
            {currentTab === 'ORDERS' && <OrdersScreen />}
            {currentTab === 'ACADEMY' && <AcademyScreen />}
          </>
        )}
      </div>

      {/* Navigation Bar */}
      {showNav && (
        <nav
          data-testid="bottom_nav_bar"
          className="h-16 bg-[#0F172A] border-t border-[#2B3A58]/70 flex items-center justify-around px-2 shrink-0 z-40"
        >
          {/* Market Tab */}
          <button
            type="button"
            data-testid="nav_tab_market"
            onClick={() => setCurrentTab('MARKET')}
            className={`flex flex-col items-center justify-center flex-1 py-1 transition-colors ${
              currentTab === 'MARKET' ? 'text-[#00E5FF]' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <LineChart className="w-5 h-5 mb-1" />
            <span className="text-[11px] font-semibold">Market</span>
          </button>

          {/* Portfolio Tab */}
          <button
            type="button"
            data-testid="nav_tab_portfolio"
            onClick={() => setCurrentTab('PORTFOLIO')}
            className={`flex flex-col items-center justify-center flex-1 py-1 transition-colors ${
              currentTab === 'PORTFOLIO' ? 'text-[#00E5FF]' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Wallet className="w-5 h-5 mb-1" />
            <span className="text-[11px] font-semibold">Portfolio</span>
          </button>

          {/* Orders Tab */}
          <button
            type="button"
            data-testid="nav_tab_orders"
            onClick={() => setCurrentTab('ORDERS')}
            className={`flex flex-col items-center justify-center flex-1 py-1 relative transition-colors ${
              currentTab === 'ORDERS' ? 'text-[#00E5FF]' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <div className="relative">
              <Receipt className="w-5 h-5 mb-1" />
              {pendingOrders.length > 0 && (
                <span className="absolute -top-1.5 -right-2 px-1.5 py-0.2 bg-[#FFD166] text-[#080D1A] rounded-full text-[9px] font-bold">
                  {pendingOrders.length}
                </span>
              )}
            </div>
            <span className="text-[11px] font-semibold">Orders</span>
          </button>

          {/* Learn / Academy Tab */}
          <button
            type="button"
            data-testid="nav_tab_academy"
            onClick={() => setCurrentTab('ACADEMY')}
            className={`flex flex-col items-center justify-center flex-1 py-1 transition-colors ${
              currentTab === 'ACADEMY' ? 'text-[#00E5FF]' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <GraduationCap className="w-5 h-5 mb-1" />
            <span className="text-[11px] font-semibold">Learn</span>
          </button>
        </nav>
      )}

      {/* Trade Confirmation Modal */}
      {tradeConfirmationOrder && (
        <TradeConfirmationModal
          order={tradeConfirmationOrder}
          onDismiss={dismissTradeConfirmation}
        />
      )}
    </div>
  );
};
