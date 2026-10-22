import { Sector } from '../types/market';

export interface BaseNasdaqStock {
  symbol: string;
  name: string;
  sector: Sector;
  basePrice: number;
  marketCap: string;
  peRatio: number;
  avgVolume: number;
}

// Well-known NASDAQ stocks metadata
export const KNOWN_METADATA: Record<string, { name: string; sector: Sector; basePrice: number }> = {
  "QQQ": { name: "Invesco QQQ Trust", sector: "INDEX", basePrice: 510.45 },
  "AAPL": { name: "Apple Inc.", sector: "MEGA_TECH", basePrice: 235.80 },
  "NVDA": { name: "NVIDIA Corporation", sector: "SEMICONDUCTORS", basePrice: 142.60 },
  "MSFT": { name: "Microsoft Corporation", sector: "MEGA_TECH", basePrice: 428.15 },
  "AMZN": { name: "Amazon.com Inc.", sector: "AI_CLOUD", basePrice: 194.50 },
  "GOOGL": { name: "Alphabet Inc. Class A", sector: "AI_CLOUD", basePrice: 172.90 },
  "GOOG": { name: "Alphabet Inc. Class C", sector: "AI_CLOUD", basePrice: 172.90 },
  "META": { name: "Meta Platforms Inc.", sector: "MEGA_TECH", basePrice: 592.40 },
  "TSLA": { name: "Tesla Inc.", sector: "CONSUMER", basePrice: 228.30 },
  "PLTR": { name: "Palantir Technologies Inc.", sector: "AI_CLOUD", basePrice: 44.50 },
  "COIN": { name: "Coinbase Global Inc.", sector: "AI_CLOUD", basePrice: 215.20 },
  "HOOD": { name: "Robinhood Markets Inc.", sector: "CONSUMER", basePrice: 27.80 },
  "SOFI": { name: "SoFi Technologies Inc.", sector: "CONSUMER", basePrice: 11.20 },
  "MSTR": { name: "MicroStrategy Incorporated", sector: "MEGA_TECH", basePrice: 220.00 },
  "RIVN": { name: "Rivian Automotive Inc.", sector: "CONSUMER", basePrice: 10.50 },
  "SMCI": { name: "Super Micro Computer Inc.", sector: "SEMICONDUCTORS", basePrice: 48.00 },
  "AVGO": { name: "Broadcom Inc.", sector: "SEMICONDUCTORS", basePrice: 175.20 },
  "AMD": { name: "Advanced Micro Devices Inc.", sector: "SEMICONDUCTORS", basePrice: 156.80 },
  "NFLX": { name: "Netflix Inc.", sector: "CONSUMER", basePrice: 755.60 },
  "COST": { name: "Costco Wholesale Corp.", sector: "CONSUMER", basePrice: 915.20 },
  "QCOM": { name: "Qualcomm Inc.", sector: "SEMICONDUCTORS", basePrice: 168.90 },
  "ADBE": { name: "Adobe Inc.", sector: "AI_CLOUD", basePrice: 498.30 },
  "ASML": { name: "ASML Holding N.V.", sector: "SEMICONDUCTORS", basePrice: 735.40 },
  "INTC": { name: "Intel Corporation", sector: "SEMICONDUCTORS", basePrice: 22.75 },
  "ARM": { name: "Arm Holdings plc", sector: "SEMICONDUCTORS", basePrice: 148.10 },
  "PANW": { name: "Palo Alto Networks", sector: "AI_CLOUD", basePrice: 372.50 },
  "CRWD": { name: "CrowdStrike Holdings Inc.", sector: "AI_CLOUD", basePrice: 320.00 },
  "RKLB": { name: "Rocket Lab USA Inc.", sector: "AI_CLOUD", basePrice: 14.80 },
  "MARA": { name: "MARA Holdings Inc.", sector: "AI_CLOUD", basePrice: 18.50 },
  "CLSK": { name: "CleanSpark Inc.", sector: "AI_CLOUD", basePrice: 12.30 },
  "HUT": { name: "Hut 8 Corp.", sector: "AI_CLOUD", basePrice: 14.10 },
  "IREN": { name: "Iris Energy Limited", sector: "AI_CLOUD", basePrice: 9.80 },
  "WULF": { name: "TeraWulf Inc.", sector: "AI_CLOUD", basePrice: 5.70 },
  "BTBT": { name: "Bit Digital Inc.", sector: "AI_CLOUD", basePrice: 3.85 },
  "LCID": { name: "Lucid Group Inc.", sector: "CONSUMER", basePrice: 3.20 },
  "DASH": { name: "DoorDash Inc.", sector: "CONSUMER", basePrice: 148.30 },
  "DKNG": { name: "DraftKings Inc.", sector: "CONSUMER", basePrice: 41.20 },
  "PYPL": { name: "PayPal Holdings Inc.", sector: "CONSUMER", basePrice: 79.50 },
  "SHOP": { name: "Shopify Inc.", sector: "AI_CLOUD", basePrice: 83.40 },
  "ROKU": { name: "Roku Inc.", sector: "CONSUMER", basePrice: 72.10 },
  "DOCU": { name: "DocuSign Inc.", sector: "AI_CLOUD", basePrice: 66.80 },
  "ZM": { name: "Zoom Video Communications", sector: "AI_CLOUD", basePrice: 74.30 },
  "BIDU": { name: "Baidu Inc.", sector: "AI_CLOUD", basePrice: 88.50 },
  "MRVL": { name: "Marvell Technology Inc.", sector: "SEMICONDUCTORS", basePrice: 86.40 },
  "MU": { name: "Micron Technology Inc.", sector: "SEMICONDUCTORS", basePrice: 112.50 },
  "FTNT": { name: "Fortinet Inc.", sector: "AI_CLOUD", basePrice: 81.20 },
  "ZS": { name: "Zscaler Inc.", sector: "AI_CLOUD", basePrice: 189.40 },
  "MDB": { name: "MongoDB Inc.", sector: "AI_CLOUD", basePrice: 288.50 },
  "DDOG": { name: "Datadog Inc.", sector: "AI_CLOUD", basePrice: 128.20 },
  "OKTA": { name: "Okta Inc.", sector: "AI_CLOUD", basePrice: 77.90 },
  "TEAM": { name: "Atlassian Corporation", sector: "AI_CLOUD", basePrice: 198.30 },
  "CSCO": { name: "Cisco Systems Inc.", sector: "MEGA_TECH", basePrice: 56.40 },
  "PEP": { name: "PepsiCo Inc.", sector: "CONSUMER", basePrice: 168.20 },
  "ISRG": { name: "Intuitive Surgical Inc.", sector: "CONSUMER", basePrice: 488.70 },
  "AMAT": { name: "Applied Materials Inc.", sector: "SEMICONDUCTORS", basePrice: 196.50 },
  "LRCX": { name: "Lam Research Corp.", sector: "SEMICONDUCTORS", basePrice: 78.40 },
  "KLAC": { name: "KLA Corporation", sector: "SEMICONDUCTORS", basePrice: 672.30 },
  "SNPS": { name: "Synopsys Inc.", sector: "AI_CLOUD", basePrice: 524.80 },
  "CDNS": { name: "Cadence Design Systems", sector: "AI_CLOUD", basePrice: 278.40 },
  "SHFS": { name: "SHF Holdings Inc.", sector: "CONSUMER", basePrice: 2.15 },
  "XHLD": { name: "Xcel Brands Inc.", sector: "CONSUMER", basePrice: 1.45 },
  "WHLR": { name: "Wheeler Real Estate Trust", sector: "CONSUMER", basePrice: 8.90 },
  "BETR": { name: "Better Home & Finance", sector: "CONSUMER", basePrice: 14.20 },
  "PDSB": { name: "PDS Biotechnology Corp.", sector: "CONSUMER", basePrice: 3.10 },
  "BRLS": { name: "Borealis Foods Inc.", sector: "CONSUMER", basePrice: 2.80 },
  "INVE": { name: "Identiv Inc.", sector: "SEMICONDUCTORS", basePrice: 4.60 },
  "KPTI": { name: "Karyopharm Therapeutics", sector: "CONSUMER", basePrice: 1.15 },
  "ENVX": { name: "Enovix Corporation", sector: "SEMICONDUCTORS", basePrice: 9.40 },
  "APLD": { name: "Applied Digital Corp.", sector: "AI_CLOUD", basePrice: 7.80 },
  "MNDR": { name: "Mobile Infrastructure", sector: "CONSUMER", basePrice: 3.40 },
  "DJT": { name: "Trump Media & Technology", sector: "CONSUMER", basePrice: 32.50 },
  "IBKR": { name: "Interactive Brokers Group", sector: "CONSUMER", basePrice: 144.60 },
  "ASTS": { name: "AST SpaceMobile Inc.", sector: "AI_CLOUD", basePrice: 26.40 },
  "LUNR": { name: "Intuitive Machines Inc.", sector: "AI_CLOUD", basePrice: 8.65 }
};

function stringHashCode(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

function calculateDeterministicPrice(sym: string): number {
  const hash = stringHashCode(sym);
  let raw: number;
  if (sym.length >= 5 && sym.endsWith('U')) {
    raw = 10.0 + (hash % 100) / 100.0;
  } else if (sym.length >= 5 && sym.endsWith('W')) {
    raw = 1.50 + (hash % 200) / 100.0;
  } else if (hash % 10 < 3) {
    raw = 2.50 + (hash % 1500) / 100.0;
  } else if (hash % 10 < 7) {
    raw = 20.0 + (hash % 8000) / 100.0;
  } else {
    raw = 100.0 + (hash % 25000) / 100.0;
  }
  return Math.round(raw * 100) / 100;
}

function calculateMarketCap(sym: string, price: number): string {
  const hash = stringHashCode(sym);
  if (price > 200) return `$${(hash % 900) + 100}B`;
  if (price > 50) return `$${(hash % 90) + 10}B`;
  if (price > 10) return `$${(hash % 800) + 100}M`;
  return `$${(hash % 90) + 10}M`;
}

function calculatePeRatio(sym: string): number {
  const hash = stringHashCode(sym);
  return Math.round((14.0 + (hash % 500) / 10.0) * 10) / 10;
}

function calculateVolume(sym: string): number {
  const hash = stringHashCode(sym);
  return 200000 + (hash % 15000000);
}

let cachedSymbols: string[] | null = null;

export async function loadAllSymbols(): Promise<string[]> {
  if (cachedSymbols && cachedSymbols.length > 0) return cachedSymbols;

  const loaded: string[] = [];
  try {
    const res = await fetch('/nasdaq_symbols.txt');
    if (res.ok) {
      const text = await res.text();
      const lines = text.split('\n');
      for (const line of lines) {
        const trimmed = line.trim().toUpperCase();
        if (trimmed) loaded.push(trimmed);
      }
    }
  } catch (_e) {
    // Fall back to known symbols
  }

  if (loaded.length === 0) {
    loaded.push(...Object.keys(KNOWN_METADATA));
  }

  // Ensure QQQ is always first
  const qqqIdx = loaded.indexOf('QQQ');
  if (qqqIdx > -1) {
    loaded.splice(qqqIdx, 1);
  }
  loaded.unshift('QQQ');

  cachedSymbols = loaded;
  return loaded;
}

export function getBaseStock(symbol: string): BaseNasdaqStock {
  const sym = symbol.toUpperCase();
  const known = KNOWN_METADATA[sym];
  if (known) {
    return {
      symbol: sym,
      name: known.name,
      sector: known.sector,
      basePrice: known.basePrice,
      marketCap: calculateMarketCap(sym, known.basePrice),
      peRatio: calculatePeRatio(sym),
      avgVolume: calculateVolume(sym)
    };
  }

  const hash = stringHashCode(sym);
  const name = `${sym} Corporation`;
  let sector: Sector = 'MEGA_TECH';
  const sectorIdx = hash % 4;
  if (sectorIdx === 1) sector = 'SEMICONDUCTORS';
  else if (sectorIdx === 2) sector = 'AI_CLOUD';
  else if (sectorIdx === 3) sector = 'CONSUMER';

  const basePrice = calculateDeterministicPrice(sym);

  return {
    symbol: sym,
    name,
    sector,
    basePrice,
    marketCap: calculateMarketCap(sym, basePrice),
    peRatio: calculatePeRatio(sym),
    avgVolume: calculateVolume(sym)
  };
}
