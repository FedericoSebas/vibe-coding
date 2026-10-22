export type Timeframe = '1D' | '1W' | '1M' | '1Y' | 'ALL';

export type IntradayInterval = '1m' | '5m' | '10m';

export type ChartType = 'LINE' | 'CANDLESTICK';

export type Sector = 
  | 'ALL'
  | 'MEGA_TECH'
  | 'SEMICONDUCTORS'
  | 'AI_CLOUD'
  | 'CONSUMER'
  | 'INDEX';

export interface SectorMeta {
  key: Sector;
  displayName: string;
}

export const SECTORS: SectorMeta[] = [
  { key: 'ALL', displayName: 'All' },
  { key: 'MEGA_TECH', displayName: 'Mega Tech' },
  { key: 'SEMICONDUCTORS', displayName: 'Semiconductors' },
  { key: 'AI_CLOUD', displayName: 'AI & Cloud' },
  { key: 'CONSUMER', displayName: 'Consumer Tech' },
  { key: 'INDEX', displayName: 'Indices & ETFs' },
];

export interface StockQuote {
  symbol: string;
  name: string;
  sector: Sector;
  price: number;
  previousClose: number;
  change: number;
  changePercent: number;
  dayHigh: number;
  dayLow: number;
  volume: number;
  peRatio: number;
  marketCap: string;
  bid: number;
  ask: number;
  lastUpdated: number;
  isMarketOpen: boolean;
  sparkline: number[];
}

export type MarketSession = 'PRE' | 'REGULAR' | 'POST';

export interface Candle {
  timestamp: number;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  session?: MarketSession;
}

export interface MarketDepthLevel {
  bidPrice: number;
  bidSize: number;
  askPrice: number;
  askSize: number;
}

export interface PortfolioPositionItem {
  ticker: string;
  companyName: string;
  shares: number;
  averageCost: number;
  currentPrice: number;
  marketValue: number;
  totalInvested: number;
  unrealizedPnL: number;
  unrealizedPnLPercent: number;
  dayPnL: number;
  dayPnLPercent: number;
}

export interface PortfolioSummary {
  totalEquity: number;
  cashBalance: number;
  positionsValue: number;
  totalUnrealizedPnL: number;
  totalUnrealizedPnLPercent: number;
  todayPnL: number;
  todayPnLPercent: number;
  startingCapital: number;
  positions: PortfolioPositionItem[];
}

export interface EducationalLesson {
  id: string;
  title: string;
  category: string;
  readingTime: string;
  iconName: string;
  shortSummary: string;
  detailedBody: string;
  keyTakeaways: string[];
  quizQuestion: string;
  quizOptions: string[];
  correctOptionIndex: number;
  explanation: string;
}

export interface OrderEntity {
  id: number;
  ticker: string;
  companyName: string;
  orderType: 'MARKET_BUY' | 'MARKET_SELL' | 'LIMIT_BUY' | 'LIMIT_SELL';
  shares: number;
  limitPrice: number | null;
  executionPrice: number | null;
  totalAmount: number;
  status: 'FILLED' | 'PENDING' | 'CANCELLED';
  timestamp: number;
  realizedPnL: number;
}

export interface UserAccount {
  id: number;
  cashBalance: number;
  startingCapital: number;
  resetCount: number;
}

export interface PositionEntity {
  ticker: string;
  companyName: string;
  shares: number;
  averageCost: number;
  totalInvested: number;
  lastUpdated: number;
}

export type AppTab = 'MARKET' | 'PORTFOLIO' | 'ORDERS' | 'ACADEMY';
