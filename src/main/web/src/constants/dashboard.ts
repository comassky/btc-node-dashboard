/**
 * Dashboard configuration constants
 */

// WebSocket reconnection
export const WS_RECONNECT_DELAY_MS = 3000;
export const WS_MAX_RECONNECT_ATTEMPTS = 5;

// Polling intervals
export const DATA_POLLING_INTERVAL_MS = 10000;
export const CACHE_CLEANUP_INTERVAL_MS = 60000;

// Cache configuration
export const DEFAULT_CACHE_SIZE = 1000;
export const CACHE_EXPIRATION_MINUTES = 5;

// Peer thresholds
export const MIN_OUTBOUND_PEERS = 8;
export const MIN_INBOUND_PEERS = 0;
export const HEALTHY_PEER_COUNT = 10;

// Chart configuration
export const CHART_UPDATE_DELAY_MS = 300;
export const MAX_CHART_LABELS = 20;

// Table configuration
export const DEFAULT_PAGE_SIZE = 10;
export const SORTABLE_COLUMNS = [
  'id',
  'addr',
  'subver',
  'minping',
  'bytesrecv',
  'bytessent',
] as const;

export type SortableColumn = (typeof SORTABLE_COLUMNS)[number];
export type SortOrder = 'asc' | 'desc';
