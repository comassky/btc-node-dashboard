/**
 * Formats a duration in seconds as "X days, Y hours" or "Y minutes, Z seconds" if < 1 day.
 */
export const formatConnectionTime = (seconds?: number | null): string => {
  if (!seconds || seconds < 1) return '<1s';
  const d = Math.floor(seconds / 86400);
  const h = Math.floor((seconds % 86400) / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  if (d > 0) {
    return `${d}d${h ? ` ${h}h` : ''}${m ? ` ${m}m` : ''}`.trim();
  }
  if (h > 0) {
    return `${h}h${m ? ` ${m}m` : ''}${s ? ` ${s}s` : ''}`.trim();
  }
  if (m > 0) {
    return `${m}m${s ? ` ${s}s` : ''}`.trim();
  }
  return `${s}s`;
};


/**
 * Formats bytes into human-readable units using pretty-bytes.
 */
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  const value = bytes / Math.pow(k, i);
  return `${parseFloat(value.toFixed(decimals))} ${sizes[i]}`;
};

/**
 * Formats a time offset in seconds.
 */
export const formatTimeOffset = (timeoffset?: number | null): string =>
  `${(timeoffset ?? 0).toFixed(1)} s`;

/**
 * Formats a timestamp or duration in seconds to a human-readable relative time using native JS.
 * If input is a timestamp (Unix seconds), returns distance to now. If duration, formats as duration.
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