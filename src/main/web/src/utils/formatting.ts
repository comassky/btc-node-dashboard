import { filesize } from "filesize";

/**
 * Formats a duration in seconds as a human-readable string (e.g., "X days Y hours", "Y minutes Z seconds").
 * If input is a timestamp, converts to duration from now.
 * @param input Duration in seconds or timestamp
 * @returns Formatted duration string
 */
export const formatConnectionTime = (input?: number | null): string => {
  if (!input || input < 1) return '<1s';
  let seconds = input;
  // If input looks like a timestamp (seconds or ms), convert to duration
  if (seconds > 1_000_000_000_000) seconds = Math.floor(seconds / 1000); // ms to s
  if (seconds > 1_000_000_000) {
    const now = Math.floor(Date.now() / 1000);
    seconds = now - seconds;
    if (seconds < 0) seconds = 0;
  }
  const d = Math.floor(seconds / 86400);
  const h = Math.floor((seconds % 86400) / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  if (d > 0) {
    return `${d}d${h ? ` ${h}h` : ''}${m ? ` ${m}m` : ''}`.trim();
  }
  if (h > 0) {
    // If both hours and minutes, hide seconds
    return m > 0 ? `${h}h ${m}m` : (s > 0 ? `${h}h ${s}s` : `${h}h`);
  }
  if (m > 0) {
    return s > 0 ? `${m}m ${s}s` : `${m}m`;
  }
  return `${s}s`;
};



/**
 * Formats bytes into human-readable units (B, KB, MB, ...).
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
 * Formats a timestamp or duration in seconds to a human-readable relative time using native JS.
 * If input is a timestamp (Unix seconds), returns distance to now. If duration, formats as duration.
 * @param timestampOrTotalSeconds Timestamp or duration in seconds
 * @returns Formatted relative time string
 */
export const formatTimeSince = (timestampOrTotalSeconds?: number | null): string => {
  if (!timestampOrTotalSeconds) return 'N/A';
  // If value looks like a timestamp (10+ digits), treat as Unix timestamp
  if (timestampOrTotalSeconds > 1_000_000_000) {
    const now = Date.now();
    const then = timestampOrTotalSeconds * 1000;
    const diff = Math.floor((now - then) / 1000);
    if (diff < 60) return `${diff}s ago`;
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    return `${Math.floor(diff / 86400)}d ago`;
  }
  // Otherwise, treat as duration in seconds
  const d = Math.floor(timestampOrTotalSeconds / 86400);
  const h = Math.floor((timestampOrTotalSeconds % 86400) / 3600);
  const m = Math.floor((timestampOrTotalSeconds % 3600) / 60);
  const s = Math.floor(timestampOrTotalSeconds % 60);
  let parts = [];
  if (d > 0) parts.push(`${d}d`);
  if (h > 0) parts.push(`${h}h`);
  if (m > 0) parts.push(`${m}m`);
  if (s > 0) parts.push(`${s}s`);
  return parts.length ? parts.join(' ') : '<1s';
};

/**
 * Formats ping in seconds to string with 3 decimals.
 */
export const formatPing = (minping?: number | null): string =>
  (!minping || minping === 0) ? 'N/A' : `${minping.toFixed(3)} s`;