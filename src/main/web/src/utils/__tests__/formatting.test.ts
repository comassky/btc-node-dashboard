import { describe, it, expect } from 'vitest';
import {
  formatDurationOrTimestamp,
  formatBytesIEC,
  formatSecondsWithSuffix,
  formatRelativeTimeSince,
  formatDurationFull,
  formatPingSmart,
  formatTimestampToLocale,
  formatDateFromNowMinusDuration,
} from '../formatting';

describe('formatConnectionTime', () => {
  it('returns <1s for falsy or <1', () => {
    expect(formatConnectionTime()).toBe('<1s');
    expect(formatConnectionTime(0)).toBe('<1s');
    expect(formatConnectionTime(null)).toBe('<1s');
  });
  it('formats seconds, minutes, hours, days', () => {
    expect(formatConnectionTime(5)).toBe('5s');
    expect(formatConnectionTime(65)).toBe('1m 5s');
    expect(formatConnectionTime(3605)).toBe('1h 5s');
    expect(formatConnectionTime(3665)).toBe('1h 1m');
    expect(formatConnectionTime(90061)).toBe('1d 1h'); // date-fns limite à 2 unités
  });
});

describe('formatBytes', () => {
  it('formats bytes to human units (IEC)', () => {
    expect(formatBytes(0)).toBe('0 B');
    expect(formatBytes(500)).toBe('500 B');
    expect(formatBytes(1024)).toBe('1 KiB');
    expect(formatBytes(1048576)).toBe('1 MiB');
    expect(formatBytes(1073741824)).toBe('1 GiB');
    expect(formatBytes(1536)).toBe('1.5 KiB');
  });
});

describe('formatTimeOffset', () => {
  it('formats time offset with 1 decimal', () => {
    expect(formatTimeOffset()).toBe('0.0 s');
    expect(formatTimeOffset(1)).toBe('1.0 s');
    expect(formatTimeOffset(-2)).toBe('-2.0 s');
    expect(formatTimeOffset(1.234)).toBe('1.2 s');
  });
});

describe('formatDurationShort', () => {
  it('returns <1s for falsy', () => {
    expect(formatDurationShort()).toBe('<1s');
    expect(formatDurationShort(0)).toBe('<1s');
    expect(formatDurationShort(null)).toBe('<1s');
  });
  it('formats duration in seconds', () => {
    expect(formatDurationShort(5)).toBe('5s');
    expect(formatDurationShort(65)).toBe('1m 5s');
    expect(formatDurationShort(3665)).toBe('1h 1m 5s');
    expect(formatDurationShort(90061)).toBe('1d 1h 1m 1s');
  });
});

describe('formatTimeSince', () => {
  it('returns N/A for falsy', () => {
    expect(formatTimeSince()).toBe('N/A');
  });
  it('formats timestamp as relative', () => {
    const now = Math.floor(Date.now() / 1000);
    expect(formatTimeSince(now)).toBe('<1m');
    expect(formatTimeSince(now - 10)).toBe('<1m');
    expect(formatTimeSince(now - 59)).toBe('<1m');
    expect(formatTimeSince(now - 70)).toBe('1m');
    expect(formatTimeSince(now - 3700)).toBe('1h 1m');
    expect(formatTimeSince(now - 90000)).toBe('1d 1h');
  });
});

describe('formatPing', () => {
  it('returns N/A for falsy or 0', () => {
    expect(formatPing()).toBe('N/A');
    expect(formatPing(0)).toBe('N/A');
  });
  it('formats ping with 3 decimals', () => {
    expect(formatPing(0.12345)).toBe('123 ms');
    expect(formatPing(1)).toBe('1.000 s');
  });
});
