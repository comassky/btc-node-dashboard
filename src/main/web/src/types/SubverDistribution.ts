/**
 * Distribution of node subversions among peers.
 */
export interface SubverDistribution {
  /** Subversion string (e.g., '/Satoshi:27.0.0/') */
  server: string;
  /** Number of peers with this subversion */
  count: number;
  /** Percentage of peers with this subversion */
  percentage: number;
}
