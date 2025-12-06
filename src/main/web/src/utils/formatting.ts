const BYTE_UNITS = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'] as const;
const BYTE_MULTIPLIER = 1024;
const LOG_1024 = Math.log(BYTE_MULTIPLIER);

/**
 * Formats bytes into human-readable units (KB, MB, GB, etc.).
 */
export const formatBytes = (bytes: number, decimals = 2): string => {
  if (bytes === 0) return '0 B';
  
  const unitIndex = Math.min(Math.floor(Math.log(bytes) / LOG_1024), BYTE_UNITS.length - 1);
  const value = bytes / Math.pow(BYTE_MULTIPLIER, unitIndex);
  
  return `${value.toFixed(decimals)} ${BYTE_UNITS[unitIndex]}`;
};

export const formatTimeOffset = (timeoffset?: number | null): string => 
  `${(timeoffset ?? 0).toFixed(1)} s`;

const SECONDS_IN_DAY = 86400;
const SECONDS_IN_HOUR = 3600;
const SECONDS_IN_MINUTE = 60;

/**
 * Formats time duration or Unix timestamp into human-readable format (e.g., "2d 5h").
 */
export const formatTimeSince = (timestampOrTotalSeconds?: number | null): string => {
  if (!timestampOrTotalSeconds) return 'N/A';

  let totalSeconds = timestampOrTotalSeconds > 1_000_000_000
    ? Math.max(0, Date.now() / 1000 - timestampOrTotalSeconds)
    : timestampOrTotalSeconds;

  if (totalSeconds < 1) return '<1s';

  const d = Math.floor(totalSeconds / SECONDS_IN_DAY);
  const remainderAfterDays = totalSeconds % SECONDS_IN_DAY;
  const h = Math.floor(remainderAfterDays / SECONDS_IN_HOUR);
  const remainderAfterHours = remainderAfterDays % SECONDS_IN_HOUR;
  const m = Math.floor(remainderAfterHours / SECONDS_IN_MINUTE);
  const s = Math.floor(remainderAfterHours % SECONDS_IN_MINUTE);

  if (d > 0) return h > 0 ? `${d}d ${h}h` : `${d}d`;
  if (h > 0) return m > 0 ? `${h}h ${m}m` : `${h}h`;
  if (m > 0) return `${m}m`;
  return `${s}s`;
};

export const formatPing = (minping?: number | null): string => 
  (!minping || minping === 0) ? 'N/A' : `${minping.toFixed(3)} s`;