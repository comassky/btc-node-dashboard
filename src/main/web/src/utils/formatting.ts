/**
 * Formats a duration in seconds as "X days, Y hours" ou "Y minutes, Z seconds" si < 1 jour.
 */
export const formatConnectionTime = (seconds?: number | null): string => {
  if (!seconds || seconds < 1) return '<1s';
  const duration = intervalToDuration({ start: 0, end: seconds * 1000 });
  if (duration.days && duration.days > 0) {
    return `${duration.days}d${duration.hours ? ` ${duration.hours}h` : ''}${duration.minutes ? ` ${duration.minutes}m` : ''}`.trim();
  }
  if (duration.hours && duration.hours > 0) {
    return `${duration.hours}h${duration.minutes ? ` ${duration.minutes}m` : ''}${duration.seconds ? ` ${duration.seconds}s` : ''}`.trim();
  }
  if (duration.minutes && duration.minutes > 0) {
    return `${duration.minutes}m${duration.seconds ? ` ${duration.seconds}s` : ''}`.trim();
  }
  return `${duration.seconds || 0}s`;
};
import prettyBytes from 'pretty-bytes';
import { formatDistanceToNow, formatDuration, intervalToDuration } from 'date-fns';

/**
 * Formats bytes into human-readable units using pretty-bytes.
 */
export const formatBytes = (bytes: number, decimals = 2): string => {
  if (!bytes || bytes === 0) return '0 B';
  return prettyBytes(bytes, { maximumFractionDigits: decimals });
};

/**
 * Formats a time offset in seconds.
 */
export const formatTimeOffset = (timeoffset?: number | null): string =>
  `${(timeoffset ?? 0).toFixed(1)} s`;

/**
 * Formats a timestamp or duration in seconds to a human-readable relative time using date-fns.
 * If input is a timestamp (Unix seconds), returns distance to now. If duration, formats as duration.
 */
export const formatTimeSince = (timestampOrTotalSeconds?: number | null): string => {
  if (!timestampOrTotalSeconds) return 'N/A';
  // If value looks like a timestamp (10+ digits), treat as Unix timestamp
  if (timestampOrTotalSeconds > 1_000_000_000) {
    return formatDistanceToNow(new Date(timestampOrTotalSeconds * 1000), { addSuffix: true });
  }
  // Otherwise, treat as duration in seconds
  const duration = intervalToDuration({ start: 0, end: timestampOrTotalSeconds * 1000 });
  return formatDuration(duration, { format: ['days', 'hours', 'minutes', 'seconds'] }) || '<1s';
};

/**
 * Formats ping in seconds to string with 3 decimals.
 */
export const formatPing = (minping?: number | null): string =>
  (!minping || minping === 0) ? 'N/A' : `${minping.toFixed(3)} s`;