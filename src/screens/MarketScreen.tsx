import React, { useState, useMemo } from 'react';
import { useTrading } from '../context/TradingContext';
import { SECTORS, Sector, StockQuote } from '../types/market';
import { MiniSparkline } from '../components/MiniSparkline';
import { Search, Star, TrendingUp, TrendingDown } from 'lucide-react';

export const MarketScreen: React.FC = () => {
  const {
    quotes,
    watchlist,
    toggleWatchlist,
    selectedSector,
    setSectorFilter,
    searchQuery,
    setSearchQuery,
    selectStock,
    setActiveDetailTicker
  } = useTrading();

  const [showOnlyWatchlist, setShowOnlyWatchlist] = useState(false);

  const qqqQuote = quotes['QQQ'];

  // Filter quotes based on search query, sector, and watchlist
  const filteredQuotes = useMemo(() => {
    const list = Object.values(quotes);
    const q = searchQuery.trim().toLowerCase();

    return list
      .filter((stock) => {
        const matchesSearch =
          !q ||
          stock.symbol.toLowerCase().includes(q) ||
          stock.name.toLowerCase().includes(q);
        const matchesSector = selectedSector === 'ALL' || stock.sector === selectedSector;
        const matchesWatchlist = !showOnlyWatchlist || watchlist.has(stock.symbol);
        return matchesSearch && matchesSector && matchesWatchlist;
      })
      .sort((a, b) => b.volume - a.volume);
  }, [quotes, searchQuery, selectedSector, showOnlyWatchlist, watchlist]);

  const handleStockClick = (symbol: string) => {
    selectStock(symbol);
    setActiveDetailTicker(symbol);
  };

  return (
    <div className="flex flex-col h-full bg-[#080D1A] overflow-hidden">
      {/* Header & Benchmark Banner */}
      <div className="bg-[#0F172A] border-b border-[#2B3A58]/50 px-4 pt-3 pb-3 shrink-0">
        <div className="flex items-center justify-between mb-2.5">
          <div>
            <h1 className="text-lg font-bold text-[#F8FAFC]">NASDAQ Live Markets</h1>
            <p className="text-xs text-slate-400">3,400+ stocks • Real-Time Paper Trading</p>
          </div>

          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#00E676]/15 border border-[#00E676]/40 text-[#00E676] text-[11px] font-bold">
            <span className="w-2 h-2 rounded-full bg-[#00E676] animate-pulse" />
            LIVE FEED
          </div>
        </div>

        {/* QQQ Benchmark Card */}
        {qqqQuote && (
          <div
            data-testid="qqq_banner_card"
            onClick={() => handleStockClick('QQQ')}
            className="p-3 bg-[#172036] hover:bg-[#1E2942] border border-[#2B3A58] rounded-xl cursor-pointer transition-colors flex items-center justify-between"
          >
            <div>
              <div className="flex items-center gap-2">
                <span className="font-bold text-[#F8FAFC] text-sm">QQQ</span>
                <span className="text-[11px] text-slate-400 font-medium">NASDAQ-100 Benchmark</span>
              </div>
              <div className="text-[10px] text-slate-400 font-mono mt-0.5">
                Vol: {qqqQuote.volume.toLocaleString()}
              </div>
            </div>

            <MiniSparkline
              points={qqqQuote.sparkline}
              isPositive={qqqQuote.change >= 0}
              width={64}
              height={26}
            />

            <div className="text-right">
              <div className="font-bold text-white text-sm font-mono">
                ${qqqQuote.price.toFixed(2)}
              </div>
              <div
                className={`text-[11px] font-semibold ${
                  qqqQuote.change >= 0 ? 'text-[#00E676]' : 'text-[#FF3D57]'
                }`}
              >
                {qqqQuote.change >= 0 ? '+' : ''}
                {qqqQuote.change.toFixed(2)} ({qqqQuote.change >= 0 ? '+' : ''}
                {qqqQuote.changePercent.toFixed(2)}%)
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Search Input */}
      <div className="px-4 py-2 shrink-0">
        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            data-testid="stock_search_input"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search 3,400+ tickers (e.g. SHFS, PLTR, WHLR, NVDA)..."
            className="w-full pl-9 pr-3 py-2 bg-[#0F172A] border border-[#2B3A58] focus:border-[#00E5FF] rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none transition-colors"
          />
        </div>
      </div>

      {/* Sector and Watchlist Filters */}
      <div className="px-4 pb-2 flex gap-2 overflow-x-auto no-scrollbar shrink-0">
        {/* Watchlist Filter Chip */}
        <button
          type="button"
          data-testid="filter_chip_watchlist"
          onClick={() => setShowOnlyWatchlist(!showOnlyWatchlist)}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap border transition-all ${
            showOnlyWatchlist
              ? 'bg-[#FFD166]/20 border-[#FFD166] text-[#FFD166]'
              : 'bg-[#0F172A] border-[#2B3A58] text-slate-400 hover:text-slate-200'
          }`}
        >
          <Star className={`w-3.5 h-3.5 ${showOnlyWatchlist ? 'fill-[#FFD166]' : ''}`} />
          Watchlist ({watchlist.size})
        </button>

        {/* Sector Chips */}
        {SECTORS.map((sec) => {
          const isSelected = !showOnlyWatchlist && selectedSector === sec.key;
          return (
            <button
              key={sec.key}
              type="button"
              data-testid={`filter_sector_${sec.key}`}
              onClick={() => {
                setShowOnlyWatchlist(false);
                setSectorFilter(sec.key);
              }}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap border transition-all ${
                isSelected
                  ? 'bg-[#00E5FF]/15 border-[#00E5FF] text-[#00E5FF]'
                  : 'bg-[#0F172A] border-[#2B3A58] text-slate-400 hover:text-slate-200'
              }`}
            >
              {sec.displayName}
            </button>
          );
        })}
      </div>

      {/* Stock Cards List */}
      <div className="flex-1 overflow-y-auto px-4 py-1 space-y-2 pb-24 sm:pb-6">
        {filteredQuotes.length === 0 ? (
          <div className="text-center py-16 text-slate-400">
            <p className="font-semibold text-sm">
              {showOnlyWatchlist ? 'Your Watchlist is empty' : 'No matching NASDAQ stocks found'}
            </p>
            <p className="text-xs text-slate-500 mt-1">
              {showOnlyWatchlist
                ? 'Tap the star icon on any stock to add it here.'
                : 'Try searching by symbol or select another sector.'}
            </p>
          </div>
        ) : (
          filteredQuotes.map((stock) => {
            const isWatchlisted = watchlist.has(stock.symbol);
            const isGain = stock.change >= 0;

            return (
              <div
                key={stock.symbol}
                data-testid={`stock_item_${stock.symbol}`}
                onClick={() => handleStockClick(stock.symbol)}
                className="flex items-center justify-between p-3 bg-[#0F172A] hover:bg-[#172036] border border-[#2B3A58]/60 rounded-xl cursor-pointer transition-colors"
              >
                {/* Watchlist toggle */}
                <button
                  type="button"
                  data-testid={`star_watchlist_${stock.symbol}`}
                  onClick={(e) => {
                    e.stopPropagation();
                    toggleWatchlist(stock.symbol);
                  }}
                  className="p-1 rounded-md hover:bg-[#1E2942] text-slate-400 mr-2 transition-colors"
                >
                  <Star
                    className={`w-4 h-4 ${
                      isWatchlisted
                        ? 'text-[#FFD166] fill-[#FFD166]'
                        : 'text-slate-500 hover:text-slate-300'
                    }`}
                  />
                </button>

                {/* Ticker and Info */}
                <div className="flex-1 min-w-0 mr-2">
                  <div className="flex items-center gap-1.5">
                    <span className="font-bold text-white text-sm">{stock.symbol}</span>
                    <span className="text-[10px] text-slate-400 font-mono">{stock.marketCap}</span>
                  </div>
                  <div className="text-xs text-slate-400 truncate">{stock.name}</div>
                </div>

                {/* Sparkline */}
                <div className="hidden sm:block mr-3">
                  <MiniSparkline
                    points={stock.sparkline}
                    isPositive={isGain}
                    width={56}
                    height={22}
                  />
                </div>

                {/* Price & Change Badge */}
                <div className="text-right">
                  <div className="font-bold text-white text-sm font-mono">
                    ${stock.price.toFixed(2)}
                  </div>
                  <div
                    className={`inline-flex items-center gap-0.5 px-1.5 py-0.5 rounded text-[11px] font-bold mt-0.5 ${
                      isGain
                        ? 'bg-[#00E676]/15 text-[#00E676]'
                        : 'bg-[#FF3D57]/15 text-[#FF3D57]'
                    }`}
                  >
                    {isGain ? <TrendingUp className="w-3 h-3" /> : <TrendingDown className="w-3 h-3" />}
                    <span>
                      {isGain ? '+' : ''}
                      {stock.changePercent.toFixed(2)}%
                    </span>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};
