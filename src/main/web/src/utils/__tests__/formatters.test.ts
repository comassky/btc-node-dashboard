import { describe, it, expect } from 'vitest';

describe('Number formatters', () => {
  const formatNumber = (num: number): string => {
    return new Intl.NumberFormat('en-US').format(num);
  };

  const formatHashRate = (difficulty: number): string => {
    const hashRate = (difficulty * Math.pow(2, 32)) / 600;
    const units = ['H/s', 'KH/s', 'MH/s', 'GH/s', 'TH/s', 'PH/s', 'EH/s'];
    let unitIndex = 0;
    let rate = hashRate;

    while (rate >= 1000 && unitIndex < units.length - 1) {
      rate /= 1000;
      unitIndex++;
    }

    return `${rate.toFixed(2)} ${units[unitIndex]}`;
  };

  it('should format large numbers with commas', () => {
    expect(formatNumber(1000)).toBe('1,000');
    expect(formatNumber(1000000)).toBe('1,000,000');
    expect(formatNumber(870000)).toBe('870,000');
  });

  it('should format small numbers correctly', () => {
    expect(formatNumber(0)).toBe('0');
    expect(formatNumber(1)).toBe('1');
    expect(formatNumber(99)).toBe('99');
  });

  it('should handle negative numbers', () => {
    expect(formatNumber(-1000)).toBe('-1,000');
    expect(formatNumber(-500)).toBe('-500');
  });

  it('should format hash rate correctly', () => {
    const result = formatHashRate(50000000000000);
    expect(result).toContain('EH/s');
    expect(result).toMatch(/\d+\.\d{2}/);
  });

  it('should handle zero difficulty', () => {
    const result = formatHashRate(0);
    expect(result).toBe('0.00 H/s');
  });

  it('should scale hash rate units appropriately', () => {
    const lowDifficulty = formatHashRate(1000);
    const highDifficulty = formatHashRate(100000000000000);
    
    expect(lowDifficulty).not.toBe(highDifficulty);
  });
});

describe('Date formatters', () => {
  const formatTimestamp = (timestamp: number): string => {
    return new Date(timestamp * 1000).toLocaleString();
  };

  it('should format Unix timestamp correctly', () => {
    const timestamp = 1733443200;
    const result = formatTimestamp(timestamp);
    
    expect(result).toBeTruthy();
    expect(result).toContain('2024');
  });

  it('should handle zero timestamp', () => {
    const result = formatTimestamp(0);
    expect(result).toContain('1970');
  });

  it('should handle recent timestamp', () => {
    const recentTimestamp = Math.floor(Date.now() / 1000);
    const result = formatTimestamp(recentTimestamp);
    
    expect(result.length).toBeGreaterThan(0);
    expect(typeof result).toBe('string');
  });
});

describe('Percentage formatters', () => {
  const formatPercentage = (value: number): string => {
    return `${(value * 100).toFixed(2)}%`;
  };

  it('should format decimal to percentage', () => {
    expect(formatPercentage(0.9999)).toBe('99.99%');
    expect(formatPercentage(0.5)).toBe('50.00%');
    expect(formatPercentage(0.0)).toBe('0.00%');
  });

  it('should handle values greater than 1', () => {
    expect(formatPercentage(1.5)).toBe('150.00%');
  });

  it('should round to 2 decimal places', () => {
    expect(formatPercentage(0.123456)).toBe('12.35%');
    expect(formatPercentage(0.666666)).toBe('66.67%');
  });
});
