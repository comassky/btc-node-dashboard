
import { filesize } from "filesize";
import { intervalToDuration } from 'date-fns';



/**
 * Formats a duration in seconds or a timestamp as a human-readable string (e.g., "2d 3h", "5m 10s").
 * @param input Duration in seconds or timestamp (seconds or ms)
 * @returns Formatted duration string
 */
export const formatConnectionTime = (input?: number | null): string => {
  if (!input || input < 1) return '<1s';
  let seconds = input;
  // If input is a timestamp in ms, convert to seconds
  if (seconds > 1_000_000_000_000) seconds = Math.floor(seconds / 1000);
  // If input looks like a timestamp (10+ digits), calculate duration from now
  if (seconds > 1_000_000_000) {
    const now = Math.floor(Date.now() / 1000);
    seconds = now - seconds;
    if (seconds < 0) seconds = 0;
  }
  const durationObj = intervalToDuration({ start: 0, end: seconds * 1000 });
  // Show max 2 units (e.g., 1d 2h, 2h 5m, 5m 10s)
  const units = [
    durationObj.days ? `${durationObj.days}d` : null,
    durationObj.hours ? `${durationObj.hours}h` : null,
    durationObj.minutes ? `${durationObj.minutes}m` : null,
    durationObj.seconds ? `${durationObj.seconds}s` : null,
  ].filter(Boolean);
  return units.slice(0, 2).join(' ') || '<1s';
};



/**
 * Formats bytes into human-readable units (B, KiB, MiB, ...).
 * @param bytes Number of bytes
 * @param decimals Number of decimal places (default: 2)
 * @returns Formatted string with appropriate unit
 */
export const formatBytes = (bytes: number, decimals = 2): string => {
  if (bytes === undefined || bytes === null || isNaN(bytes)) return 'N/A';
  if (bytes === 0) return '0 B';
  return filesize(bytes, { standard: 'iec', base: decimals });
};

/**
 * Formats a time offset in seconds as a string with 's' suffix.
 * @param timeoffset Time offset in seconds
 * @returns Formatted string (e.g., '0.0 s')
 */
export const formatTimeOffset = (timeoffset?: number | null): string =>
  `${(timeoffset ?? 0).toFixed(1)} s`;




/**
 * Formats a duration in seconds as a compact string (e.g., "1d 2h 5m 10s").
 * @param seconds Duration in seconds
 * @returns Formatted duration string
 */
export const formatDurationShort = (seconds?: number | null): string => {
  if (!seconds || seconds < 1) return '<1s';
  const durationObj = intervalToDuration({ start: 0, end: seconds * 1000 });
  const units = [
    durationObj.days ? `${durationObj.days}d` : null,
    durationObj.hours ? `${durationObj.hours}h` : null,
    durationObj.minutes ? `${durationObj.minutes}m` : null,
    durationObj.seconds ? `${durationObj.seconds}s` : null,
  ].filter(Boolean);
  return units.length ? units.join(' ') : '<1s';
};

/**
 * Formats a timestamp (Unix seconds or ms) as a relative duration from now (e.g., "5m", "2h", "3d").
 * @param timestamp Timestamp in seconds or ms
 * @returns Formatted relative time string
 */
export const formatTimeSince = (timestamp?: number | null): string => {
  if (!timestamp) return 'N/A';
  // Normalize to seconds if ms
  const ts = timestamp > 1_000_000_000_000 ? Math.floor(timestamp / 1000) : Math.floor(timestamp);
  const now = Math.floor(Date.now() / 1000);
  const diff = Math.max(0, now - ts);
  const { days = 0, hours = 0, minutes = 0 } = intervalToDuration({ start: 0, end: diff * 1000 });
  if (days > 0) return `${days}d${hours ? ' ' + hours + 'h' : ''}${minutes ? ' ' + minutes + 'm' : ''}`.trim();
  if (hours > 0) return `${hours}h${minutes ? ' ' + minutes + 'm' : ''}`.trim();
  if (minutes > 0) return `${minutes}m`;
  return '<1m';
};

/**
 * Formats ping in seconds to string with 3 decimals.
 */
export const formatPing = (ping?: number | null): string => {
  if (ping === undefined || ping === null || isNaN(ping) || ping <= 0) return 'N/A';
  // If ping < 1s, show in ms (rounded to 0 decimals), else in s (3 decimals)
  if (ping < 1) return `${Math.round(ping * 1000)} ms`;
  return `${ping.toFixed(3)} s`;
};