import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Candle, ChartType } from '../types/market';

interface InteractiveChartProps {
  candles: Candle[];
  chartType: ChartType;
  showSMA: boolean;
  showVolume: boolean;
  className?: string;
}

export const InteractiveChart: React.FC<InteractiveChartProps> = ({
  candles,
  chartType,
  showSMA,
  showVolume,
  className = ''
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [dimensions, setDimensions] = useState({ width: 600, height: 260 });

  // Update canvas size to match container or canvas element
  useEffect(() => {
    const updateSize = () => {
      if (canvasRef.current) {
        const canvas = canvasRef.current;
        const rect = canvas.getBoundingClientRect();
        const style = window.getComputedStyle(canvas);
        const padLeft = parseFloat(style.paddingLeft) || 0;
        const padRight = parseFloat(style.paddingRight) || 0;
        const contentWidth = rect.width > 0 ? Math.max(10, rect.width - padLeft - padRight) : (canvas.clientWidth || 600);
        setDimensions({
          width: Math.floor(contentWidth),
          height: containerRef.current?.clientHeight || 220
        });
      } else if (containerRef.current) {
        const { clientWidth, clientHeight } = containerRef.current;
        setDimensions({
          width: clientWidth || 600,
          height: clientHeight || 220
        });
      }
    };
    updateSize();
    window.addEventListener('resize', updateSize);
    let observer: ResizeObserver | null = null;
    if (typeof ResizeObserver !== 'undefined' && containerRef.current) {
      observer = new ResizeObserver(() => updateSize());
      observer.observe(containerRef.current);
    }
    return () => {
      window.removeEventListener('resize', updateSize);
      if (observer) observer.disconnect();
    };
  }, []);

  const activeCandle = selectedIndex !== null ? candles[selectedIndex] : candles[candles.length - 1];
  const firstClose = candles.length > 0 ? candles[0].close : 0;
  const currentClose = activeCandle ? activeCandle.close : 0;
  const isPositive = currentClose >= firstClose;

  // Draw chart on canvas
  const drawChart = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas || candles.length === 0) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const dpr = window.devicePixelRatio || 1;
    const width = dimensions.width;
    const height = dimensions.height;

    canvas.width = width * dpr;
    canvas.height = height * dpr;
    ctx.scale(dpr, dpr);

    // Clear
    ctx.clearRect(0, 0, width, height);

    const volumeH = showVolume ? height * 0.22 : 0;
    const priceH = height - volumeH - 24;

    const minPrice = Math.min(...candles.map((c) => c.low)) * 0.999;
    const maxPrice = Math.max(...candles.map((c) => c.high)) * 1.001;
    const priceRange = Math.max(0.01, maxPrice - minPrice);
    const maxVolume = Math.max(...candles.map((c) => c.volume), 1);

    // Market session background shading for 1-day chart (Pre-Market, Regular, After-Hours)
    const hasSessions = candles.some((c) => c.session !== undefined);
    if (hasSessions) {
      let preStart = -1, preEnd = -1;
      let regStart = -1, regEnd = -1;
      let postStart = -1, postEnd = -1;

      candles.forEach((c, i) => {
        if (c.session === 'PRE') {
          if (preStart === -1) preStart = i;
          preEnd = i;
        } else if (c.session === 'REGULAR') {
          if (regStart === -1) regStart = i;
          regEnd = i;
        } else if (c.session === 'POST') {
          if (postStart === -1) postStart = i;
          postEnd = i;
        }
      });

      const totalCandles = Math.max(1, candles.length - 1);
      const getX = (idx: number) => (idx / totalCandles) * width;

      // 1. Pre-Market Shading (distinct purple-indigo background tint)
      if (preStart !== -1) {
        const xStart = 0;
        const xEnd = regStart !== -1 ? (getX(preEnd) + getX(regStart)) / 2 : getX(preEnd);

        ctx.fillStyle = 'rgba(124, 77, 255, 0.12)';
        ctx.fillRect(xStart, 0, xEnd - xStart, height);

        // Pre-Market Session Label
        ctx.fillStyle = '#A78BFA';
        ctx.font = 'bold 9px monospace';
        ctx.textAlign = 'left';
        ctx.fillText('PRE-MARKET (4:00 - 9:30 AM)', xStart + 8, 14);

        // Divider before regular hours
        ctx.strokeStyle = '#7C4DFF77';
        ctx.lineWidth = 1;
        ctx.setLineDash([3, 3]);
        ctx.beginPath();
        ctx.moveTo(xEnd, 0);
        ctx.lineTo(xEnd, height);
        ctx.stroke();
        ctx.setLineDash([]);

        // Divider Time Marker
        ctx.fillStyle = '#A78BFA';
        ctx.font = 'bold 9px monospace';
        ctx.textAlign = 'center';
        ctx.fillText('9:30 AM ET', xEnd, height - 6);
      }

      // 2. Regular Session Shading (distinct green-tinted navy floor)
      if (regStart !== -1) {
        const xStart = preEnd !== -1 ? (getX(preEnd) + getX(regStart)) / 2 : 0;
        const xEnd = postStart !== -1 ? (getX(regEnd) + getX(postStart)) / 2 : width;

        ctx.fillStyle = 'rgba(0, 230, 118, 0.04)';
        ctx.fillRect(xStart, 0, xEnd - xStart, height);

        // Regular Session Label
        ctx.fillStyle = '#69F0AE';
        ctx.font = 'bold 9px monospace';
        ctx.textAlign = 'left';
        ctx.fillText('REGULAR HOURS (9:30 AM - 4:00 PM)', xStart + 8, 14);

        // Divider before after-hours
        if (postStart !== -1) {
          ctx.strokeStyle = '#FFD16677';
          ctx.lineWidth = 1;
          ctx.setLineDash([3, 3]);
          ctx.beginPath();
          ctx.moveTo(xEnd, 0);
          ctx.lineTo(xEnd, height);
          ctx.stroke();
          ctx.setLineDash([]);

          // Divider Time Marker
          ctx.fillStyle = '#FCD34D';
          ctx.font = 'bold 9px monospace';
          ctx.textAlign = 'center';
          ctx.fillText('4:00 PM ET', xEnd, height - 6);
        }
      }

      // 3. After-Hours Shading (distinct warm amber/gold background tint)
      if (postStart !== -1) {
        const xStart = regEnd !== -1 ? (getX(regEnd) + getX(postStart)) / 2 : getX(postStart);
        const xEnd = width;

        ctx.fillStyle = 'rgba(255, 209, 102, 0.12)';
        ctx.fillRect(xStart, 0, xEnd - xStart, height);

        // After-Hours Session Label
        ctx.fillStyle = '#FCD34D';
        ctx.font = 'bold 9px monospace';
        ctx.textAlign = 'left';
        ctx.fillText('AFTER-HOURS (4:00 - 8:00 PM)', xStart + 8, 14);
      }
    }

    // Grid lines
    ctx.strokeStyle = '#2B3A5840';
    ctx.lineWidth = 1;
    const gridRows = 4;
    for (let i = 0; i <= gridRows; i++) {
      const y = (priceH * i) / gridRows;
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(width, y);
      ctx.stroke();

      // Price labels
      const p = maxPrice - (i / gridRows) * priceRange;
      ctx.fillStyle = '#64748B';
      ctx.font = '10px monospace';
      ctx.textAlign = 'right';
      ctx.fillText(`$${p.toFixed(2)}`, width - 8, y + (i === 0 ? 12 : -4));
    }

    // Volume bars
    if (showVolume && maxVolume > 0) {
      const barW = (width / candles.length) * 0.65;
      candles.forEach((c, i) => {
        const x = (i / Math.max(1, candles.length - 1)) * width;
        const ratio = Math.min(1, Math.max(0.05, c.volume / maxVolume));
        const barH = volumeH * ratio;
        const isUp = c.close >= c.open;
        ctx.fillStyle = isUp ? '#00E67645' : '#FF3D5745';
        ctx.fillRect(x - barW / 2, height - barH, barW, barH);
      });
    }

    // Candlesticks
    if (chartType === 'CANDLESTICK') {
      const candleW = Math.max(2, (width / candles.length) * 0.68);
      candles.forEach((c, i) => {
        const x = (i / Math.max(1, candles.length - 1)) * width;
        const openY = priceH - ((c.open - minPrice) / priceRange) * priceH;
        const closeY = priceH - ((c.close - minPrice) / priceRange) * priceH;
        const highY = priceH - ((c.high - minPrice) / priceRange) * priceH;
        const lowY = priceH - ((c.low - minPrice) / priceRange) * priceH;

        const isBullish = c.close >= c.open;
        const color = isBullish ? '#00E676' : '#FF3D57';

        // Wick
        ctx.strokeStyle = color;
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.moveTo(x, highY);
        ctx.lineTo(x, lowY);
        ctx.stroke();

        // Body
        const bodyTop = Math.min(openY, closeY);
        const bodyH = Math.max(2, Math.abs(closeY - openY));
        ctx.fillStyle = color;
        ctx.fillRect(x - candleW / 2, bodyTop, candleW, bodyH);
      });
    } else {
      // Line Chart with area gradient
      if (candles.length >= 2) {
        const lineColor = isPositive ? '#00E676' : '#FF3D57';
        const grad = ctx.createLinearGradient(0, 0, 0, priceH);
        if (isPositive) {
          grad.addColorStop(0, 'rgba(0, 230, 118, 0.35)');
          grad.addColorStop(1, 'rgba(0, 230, 118, 0.00)');
        } else {
          grad.addColorStop(0, 'rgba(255, 61, 87, 0.35)');
          grad.addColorStop(1, 'rgba(255, 61, 87, 0.00)');
        }

        // Fill path
        ctx.beginPath();
        const startX = 0;
        const startY = priceH - ((candles[0].close - minPrice) / priceRange) * priceH;
        ctx.moveTo(startX, startY);

        for (let i = 1; i < candles.length; i++) {
          const prevX = ((i - 1) / (candles.length - 1)) * width;
          const prevY = priceH - ((candles[i - 1].close - minPrice) / priceRange) * priceH;
          const curX = (i / (candles.length - 1)) * width;
          const curY = priceH - ((candles[i].close - minPrice) / priceRange) * priceH;
          const cpX = (prevX + curX) / 2;
          ctx.bezierCurveTo(cpX, prevY, cpX, curY, curX, curY);
        }

        ctx.lineTo(width, priceH);
        ctx.lineTo(0, priceH);
        ctx.closePath();
        ctx.fillStyle = grad;
        ctx.fill();

        // Stroke path
        ctx.beginPath();
        ctx.moveTo(startX, startY);
        for (let i = 1; i < candles.length; i++) {
          const prevX = ((i - 1) / (candles.length - 1)) * width;
          const prevY = priceH - ((candles[i - 1].close - minPrice) / priceRange) * priceH;
          const curX = (i / (candles.length - 1)) * width;
          const curY = priceH - ((candles[i].close - minPrice) / priceRange) * priceH;
          const cpX = (prevX + curX) / 2;
          ctx.bezierCurveTo(cpX, prevY, cpX, curY, curX, curY);
        }
        ctx.strokeStyle = lineColor;
        ctx.lineWidth = 2.5;
        ctx.lineCap = 'round';
        ctx.stroke();
      }
    }

    // SMA 20 Overlay (Gold)
    if (showSMA && candles.length >= 5) {
      const period = Math.min(10, candles.length);
      ctx.beginPath();
      ctx.strokeStyle = '#FFD166';
      ctx.lineWidth = 1.5;
      ctx.setLineDash([4, 2]);

      let started = false;
      for (let i = period - 1; i < candles.length; i++) {
        let sum = 0;
        for (let k = 0; k < period; k++) {
          sum += candles[i - k].close;
        }
        const sma = sum / period;
        const x = (i / (candles.length - 1)) * width;
        const y = priceH - ((sma - minPrice) / priceRange) * priceH;

        if (!started) {
          ctx.moveTo(x, y);
          started = true;
        } else {
          ctx.lineTo(x, y);
        }
      }
      ctx.stroke();
      ctx.setLineDash([]);
    }

    // Crosshair & Scrubber Marker
    if (selectedIndex !== null && selectedIndex >= 0 && selectedIndex < candles.length) {
      const x = (selectedIndex / Math.max(1, candles.length - 1)) * width;
      const c = candles[selectedIndex];
      const y = priceH - ((c.close - minPrice) / priceRange) * priceH;

      // Vertical line
      ctx.strokeStyle = 'rgba(248, 250, 252, 0.4)';
      ctx.lineWidth = 1.5;
      ctx.setLineDash([3, 3]);
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, height);
      ctx.stroke();

      // Horizontal line
      ctx.strokeStyle = 'rgba(248, 250, 252, 0.25)';
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(width, y);
      ctx.stroke();
      ctx.setLineDash([]);

      // Point circle
      ctx.fillStyle = '#00E5FF';
      ctx.beginPath();
      ctx.arc(x, y, 5, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = '#FFFFFF';
      ctx.beginPath();
      ctx.arc(x, y, 2.5, 0, Math.PI * 2);
      ctx.fill();
    }
  }, [candles, chartType, showSMA, showVolume, dimensions, selectedIndex, isPositive]);

  useEffect(() => {
    drawChart();
  }, [drawChart]);

  const handlePointer = (e: React.PointerEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas || candles.length === 0) return;
    const rect = canvas.getBoundingClientRect();
    const style = window.getComputedStyle(canvas);
    const padLeft = parseFloat(style.paddingLeft) || 0;
    const padRight = parseFloat(style.paddingRight) || 0;
    const contentWidth = Math.max(1, rect.width - padLeft - padRight);
    const x = e.clientX - rect.left - padLeft;
    const total = candles.length;
    const ratio = Math.max(0, Math.min(1, x / contentWidth));
    const idx = Math.min(total - 1, Math.max(0, Math.round(ratio * (total - 1))));
    setSelectedIndex(idx);
  };

  const handlePointerLeave = () => {
    setSelectedIndex(null);
  };

  if (candles.length === 0) {
    return (
      <div className={`flex items-center justify-center text-slate-400 text-sm h-64 bg-[#0F172A] ${className}`}>
        Loading real-time chart data...
      </div>
    );
  }

  const delta = activeCandle ? activeCandle.close - firstClose : 0;
  const pct = firstClose > 0 ? (delta / firstClose) * 100 : 0;
  const deltaSign = delta >= 0 ? '+' : '';
  const is1D = candles.some((c) => c.session !== undefined);
  const dateFormatted = activeCandle
    ? new Date(activeCandle.timestamp).toLocaleString('en-US', {
        timeZone: 'America/New_York',
        month: 'short',
        day: 'numeric',
        hour: 'numeric',
        minute: '2-digit'
      }) + (is1D ? ' ET' : '')
    : '';

  return (
    <div className={`flex flex-col select-none ${className}`}>
      {/* Scrubber active stats display */}
      <div className="px-4 py-2 flex flex-wrap items-center justify-between bg-[#0F172A] border-b border-[#2B3A58]/50">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xl font-bold text-[#F8FAFC]">
              ${activeCandle ? activeCandle.close.toFixed(2) : '0.00'}
            </span>
            <span
              className={`text-xs font-semibold px-2 py-0.5 rounded ${
                delta >= 0 ? 'bg-[#00E676]/20 text-[#00E676]' : 'bg-[#FF3D57]/20 text-[#FF3D57]'
              }`}
            >
              {deltaSign}{delta.toFixed(2)} ({deltaSign}{pct.toFixed(2)}%)
            </span>

            {activeCandle?.session && (
              <span
                className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${
                  activeCandle.session === 'PRE'
                    ? 'bg-[#7C4DFF]/20 text-[#A78BFA] border-[#7C4DFF]/50'
                    : activeCandle.session === 'POST'
                    ? 'bg-[#FFD166]/20 text-[#FCD34D] border-[#FFD166]/50'
                    : 'bg-[#00E676]/20 text-[#69F0AE] border-[#00E676]/50'
                }`}
              >
                {activeCandle.session === 'PRE'
                  ? 'PRE-MARKET (4:00 - 9:30 AM)'
                  : activeCandle.session === 'POST'
                  ? 'AFTER-HOURS (4:00 - 8:00 PM)'
                  : 'REGULAR HOURS (9:30 AM - 4:00 PM)'}
              </span>
            )}
          </div>

          {chartType === 'CANDLESTICK' && activeCandle && (
            <div className="text-[11px] text-slate-400 flex gap-2 mt-0.5 font-mono">
              <span>O: ${activeCandle.open.toFixed(2)}</span>
              <span>H: ${activeCandle.high.toFixed(2)}</span>
              <span>L: ${activeCandle.low.toFixed(2)}</span>
              <span>Vol: {activeCandle.volume.toLocaleString()}</span>
            </div>
          )}
        </div>

        <div className="text-[11px] text-slate-400 mt-1 sm:mt-0 font-medium">
          {dateFormatted}
        </div>
      </div>

      {/* Canvas Area */}
      <div ref={containerRef} className="relative w-full h-[220px] bg-[#0F172A] overflow-x-auto">
        <canvas
          ref={canvasRef}
          data-testid="interactive_chart_canvas"
          style={{ width: '100%', paddingLeft: '30px', paddingRight: '30px', boxSizing: 'border-box' }}
          className="h-full cursor-crosshair touch-none"
          onPointerDown={handlePointer}
          onPointerMove={handlePointer}
          onPointerUp={handlePointerLeave}
          onPointerCancel={handlePointerLeave}
          onPointerLeave={handlePointerLeave}
        />
      </div>

      {/* Session legend footer for 1-day chart */}
      {candles.some((c) => c.session !== undefined) && (
        <div className="px-4 py-1.5 bg-[#0F172A] border-t border-[#2B3A58]/40 flex flex-wrap items-center justify-between gap-2 text-[11px]">
          <div className="flex flex-wrap items-center gap-3">
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-xs bg-[#7C4DFF]/40 border border-[#7C4DFF]" />
              <span className="text-slate-300 font-medium">Pre-Market <span className="text-slate-500">(4:00 - 9:30 AM)</span></span>
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-xs bg-[#00E676]/20 border border-[#00E676]" />
              <span className="text-slate-300 font-medium">Regular Session <span className="text-slate-500">(9:30 AM - 4:00 PM)</span></span>
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-xs bg-[#FFD166]/30 border border-[#FFD166]" />
              <span className="text-slate-300 font-medium">After-Hours <span className="text-slate-500">(4:00 - 8:00 PM)</span></span>
            </span>
          </div>
        </div>
      )}
    </div>
  );
};
