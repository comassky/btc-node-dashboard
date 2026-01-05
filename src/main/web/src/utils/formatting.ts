import { filesize } from 'filesize';
import { intervalToDuration, formatDuration } from 'date-fns';

// Memoization cache for formatted bytes (LRU-like with max size)
const bytesCache = new Map<string, string>();
const MAX_CACHE_SIZE = 100;

/**
 * Format bytes with locale-specific separators (space, dot, etc) for tooltips and display.
 * @param bytes Number of bytes
 * @returns Formatted string with locale separators
 */
export function formatBytesLocale(bytes?: number | null): string {
  if (bytes == null || isNaN(bytes)) return 'N/A';
  return bytes.toLocaleString();
}

/**
 * Format a number with a space as thousands separator (e.g. 1234567 => '1 234 567')
 * Cached for performance with frequently used values
 */
const numberCache = new Map<number, string>();
export function formatNumberWithSpace(n: number | string): string {
  const num = typeof n === 'string' ? parseFloat(n) : n;
  if (numberCache.has(num)) {
    return numberCache.get(num)!;
  }
  const result = String(n).replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
  if (numberCache.size < MAX_CACHE_SIZE) {
    numberCache.set(num, result);
  }
  return result;
}

/**
 * Formats a duration in seconds or a timestamp as a human-readable string (e.g., "2d 3h", "5m 10s").
 * @param input Duration in seconds or timestamp (seconds or ms)
 * @returns Formatted duration string
 */
export const formatDurationOrTimestamp = (input?: number | null): string => {
  if (!input || input < 1) return '<1s';
  let seconds = input;
  if (seconds > 1_000_000_000_000) seconds = Math.floor(seconds / 1000);
  if (seconds > 1_000_000_000) {
    const now = Math.floor(Date.now() / 1000);
    seconds = Math.max(0, now - seconds);
  }
  const durationObj = intervalToDuration({ start: 0, end: seconds * 1000 });
  return (
    formatDuration(durationObj, {
      format: ['days', 'hours', 'minutes', 'seconds'],
      zero: false,
      delimiter: ' ',
    }) || '<1s'
  );
};

/**
 * Formats bytes into human-readable units (B, KiB, MiB, ...).
 * Cached for frequently used values.
 * @param bytes Number of bytes
 * @param decimals Number of decimal places (default: 2)
 * @returns Formatted string with appropriate unit
 */
export const formatBytesIEC = (bytes: number, decimals = 2): string => {
  const cacheKey = `${bytes}_${decimals}`;
  if (bytesCache.has(cacheKey)) {
    return bytesCache.get(cacheKey)!;
  }

  const result = filesize(bytes, { base: 2, standard: 'iec', round: decimals }) as string;

  if (bytesCache.size >= MAX_CACHE_SIZE) {
    // Simple LRU: delete first entry when cache is full
    const firstKey = bytesCache.keys().next().value;
    if (firstKey) bytesCache.delete(firstKey);
  }
  bytesCache.set(cacheKey, result);
  return result;
};

/**
 * Formats a time offset in seconds as a string with 's' suffix.
 * @param timeoffset Time offset in seconds
 * @returns Formatted string (e.g., '0.0 s')
 */
export const formatSecondsWithSuffix = (timeoffset?: number | null): string =>
  `${Number(timeoffset ?? 0).toFixed(1)} s`;

/**
 * Formats a duration in seconds as a compact string (e.g., "1d 2h 5m 10s").
 * @param seconds Duration in seconds
 * @returns Formatted duration string
 */
export const formatDurationFull = (seconds?: number | null): string => {
  if (!seconds || seconds < 1) return '<1s';
  const durationObj = intervalToDuration({ start: 0, end: seconds * 1000 });
  return (
    formatDuration(durationObj, {
      format: ['days', 'hours', 'minutes', 'seconds'],
      zero: false,
      delimiter: ' ',
    }) || '<1s'
  );
};

/**
 * Formats a timestamp (Unix seconds or ms) as a connection time string:
 * '<1m' if < 1 minute, 'Xm' if X minutes, 'Xh Ym' if X hours and Y minutes, 'Zd Xh Ym' if Z days, X hours, Y minutes.
 * @param timestamp Timestamp in seconds or ms
 * @returns Formatted connection time string
 */
export const formatRelativeTimeSince = (timestamp?: number | null): string => {
  if (!timestamp) return 'N/A';
  const now = Math.floor(Date.now() / 1000);
  const ts = timestamp > 1_000_000_000_000 ? Math.floor(timestamp / 1000) : Math.floor(timestamp);
  const diff = Math.max(0, now - ts);
  if (diff < 60) return '<1m';
  const mins = Math.floor(diff / 60) % 60;
  const hours = Math.floor(diff / 3600) % 24;
  const days = Math.floor(diff / 86400);
  let result = '';
  if (days > 0) result += `${days}d `;
  if (hours > 0 || days > 0) result += `${hours}h `;
  result += `${mins}m`;
  return result.trim();
};

/**
 * Returns the local date/time string for now minus a duration in seconds.
 * @param durationSeconds Duration in seconds
 * @returns Local date/time string
 */
export const formatDateFromNowMinusDuration = (durationSeconds?: number | null): string => {
  if (!durationSeconds || durationSeconds < 0) return 'N/A';
  return new Date(Date.now() - durationSeconds * 1000).toLocaleString();
};

/**
 * Formats a timestamp (seconds or ms) as a local date/time string.
 * @param timestamp Timestamp in seconds or ms
 * @returns Local date/time string or 'N/A'
 */
export const formatTimestampToLocale = (timestamp?: number | null): string => {
  if (!timestamp) return 'N/A';
  return new Date(timestamp * 1000).toLocaleString();
};

/**
 * Formats ping in seconds to string with 3 decimals.
 */
export const formatPingSmart = (ping?: number | null): string => {
  if (ping == null || isNaN(ping) || ping <= 0) return 'N/A';
  // If ping < 1s, show in ms (rounded to 0 decimals), else in s (3 decimals)
  if (ping < 1) return `${Math.round(ping * 1000)} ms`;
  return `${ping.toFixed(3)} s`;
};
