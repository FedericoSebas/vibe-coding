import React, { useMemo } from 'react';

interface MiniSparklineProps {
  points: number[];
  isPositive: boolean;
  width?: number;
  height?: number;
  className?: string;
}

export const MiniSparkline: React.FC<MiniSparklineProps> = ({
  points,
  isPositive,
  width = 68,
  height = 26,
  className = ''
}) => {
  const pathD = useMemo(() => {
    if (!points || points.length < 2) return '';
    const minP = Math.min(...points);
    const maxP = Math.max(...points);
    const range = Math.max(0.01, maxP - minP);

    return points
      .map((p, i) => {
        const x = (i / (points.length - 1)) * width;
        const y = height - ((p - minP) / range) * (height - 4) - 2;
        return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
      })
      .join(' ');
  }, [points, width, height]);

  if (!points || points.length < 2) {
    return <div style={{ width, height }} className={className} />;
  }

  const strokeColor = isPositive ? '#00E676' : '#FF3D57';

  return (
    <svg width={width} height={height} className={`overflow-visible ${className}`}>
      <path
        d={pathD}
        fill="none"
        stroke={strokeColor}
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
};
