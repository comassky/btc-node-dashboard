/**
 * Represents a peer connected to the Bitcoin node.
 */
export interface Peer {
  /** Unique peer ID */
  id: number;
  /** Peer address (IP:port) */
  addr: string;
  /** Peer subversion string */
  subver: string;
  /** Protocol version */
  version: number;
  /** Time offset in seconds */
  timeoffset: number;
  /** Connection time (Unix timestamp) */
  conntime: number;
  /** Network type (e.g., 'ipv4') */
  network: string | null;
  /** Connection type (e.g., 'inbound', 'outbound') */
  connection_type: string;
  /** Minimum ping time (seconds) */
  minping: number | null;
  /** Bytes received from peer */
  bytesrecv: number;
  /** Bytes sent to peer */
  bytessent: number;
}
