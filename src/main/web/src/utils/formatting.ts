import { filesize } from 'filesize';
import { intervalToDuration, formatDuration } from 'date-fns';

// Time constants
const MS_TIMESTAMP_THRESHOLD = 1_000_000_000_000;
const UNIX_TIMESTAMP_THRESHOLD = 1_000_000_000;

// Memoization cache for formatted bytes (LRU-like with max size)
const bytesCache = new Map<string, string>();
const numberCache = new Map<number, string>();
const MAX_CACHE_SIZE = 100;

/**
 * Simple LRU cache eviction - removes the oldest entry
 */
function evictOldestEntry(cache: Map<any, any>): void {
  const firstKey = cache.keys().next().value;
  if (firstKey !== undefined) {
    cache.delete(firstKey);
  }
}

/**
 * Adds entry to cache with automatic eviction if needed
 */
function addToCache<K, V>(cache: Map<K, V>, key: K, value: V): void {
  if (cache.size >= MAX_CACHE_SIZE) {
    evictOldestEntry(cache);
  }
  cache.set(key, value);
}

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
export function formatNumberWithSpace(n: number | string): string {
  const num = typeof n === 'string' ? parseFloat(n) : n;
  if (numberCache.has(num)) {
    return numberCache.get(num)!;
  }
  const result = String(n).replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
  addToCache(numberCache, num, result);
  return result;
}

/**
 * Helper function to format a duration in seconds using date-fns.
 * @param seconds Duration in seconds
 * @returns Formatted duration string
 */
const formatDurationHelper = (seconds: number): string => {
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
 * Formats a duration in seconds or a timestamp as a human-readable string (e.g., "2d 3h", "5m 10s").
 * @param input Duration in seconds or timestamp (seconds or ms)
 * @returns Formatted duration string
 */
export const formatDurationOrTimestamp = (input?: number | null): string => {
  if (!input || input < 1) return '<1s';
  let seconds = input;
  if (seconds > MS_TIMESTAMP_THRESHOLD) seconds = Math.floor(seconds / 1000);
  if (seconds > UNIX_TIMESTAMP_THRESHOLD) {
    const now = Math.floor(Date.now() / 1000);
    seconds = Math.max(0, now - seconds);
  }
  return formatDurationHelper(seconds);
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
  addToCache(bytesCache, cacheKey, result);
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
  return formatDurationHelper(seconds);
};

/**
 * Formats a timestamp (Unix seconds or ms) as a connection time string:
 * Shows "Xd Yh Zm" if >= 1 day, "Yh Zm" if < 1 day but >= 1 hour, "Zm" if < 1 hour, or '<1m' if < 1 minute.
 * @param timestamp Timestamp in seconds or ms
 * @returns Formatted connection time string
 */
export const formatRelativeTimeSince = (timestamp?: number | null): string => {
  if (!timestamp) return 'N/A';
  const now = Date.now();
  const ts = timestamp > MS_TIMESTAMP_THRESHOLD ? timestamp : timestamp * 1000;
  const duration = intervalToDuration({ start: ts, end: now });

  const days = duration.days || 0;
  const hours = duration.hours || 0;
  const mins = duration.minutes || 0;

  if (days === 0 && hours === 0 && mins === 0) return '<1m';

  if (days > 0) {
    return `${days}d ${hours}h ${mins}m`;
  } else if (hours > 0) {
    return `${hours}h ${mins}m`;
  } else {
    return `${mins}m`;
  }
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
