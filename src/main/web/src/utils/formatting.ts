const BYTE_UNITS = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'] as const;
const BYTE_MULTIPLIER = 1024;

export const formatBytes = (bytes: number, decimals = 2): string => {
  if (bytes === 0) return '0 B';
  
  const unitIndex = Math.floor(Math.log(bytes) / Math.log(BYTE_MULTIPLIER));
  const value = bytes / Math.pow(BYTE_MULTIPLIER, unitIndex);
  
  return `${value.toFixed(decimals)} ${BYTE_UNITS[unitIndex]}`;
};

export const formatTimeOffset = (timeoffset?: number | null): string => 
  `${(timeoffset ?? 0).toFixed(1)} s`;

export const formatTimeSince = (timestampOrTotalSeconds?: number | null): string => {
  if (!timestampOrTotalSeconds) return 'N/A';

  let totalSeconds = timestampOrTotalSeconds > 1_000_000_000
    ? Math.max(0, Date.now() / 1000 - timestampOrTotalSeconds)
    : timestampOrTotalSeconds;

  if (totalSeconds < 1) return '<1s';

  const d = Math.floor(totalSeconds / 86400);
  const h = Math.floor((totalSeconds % 86400) / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = Math.floor(totalSeconds % 60);

  const parts: string[] = [];
  if (d > 0) parts.push(`${d}d`);
  if (h > 0) parts.push(`${h}h`);
  if (m > 0) parts.push(`${m}m`);
  if (parts.length === 0 || totalSeconds < 60) parts.push(`${s}s`);

  return parts.slice(0, 2).join(' ');
};

export const formatPing = (minping?: number | null): string => 
  (!minping || minping === 0) ? 'N/A' : `${minping.toFixed(3)} s`;