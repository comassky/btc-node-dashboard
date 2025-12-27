
import type { GeneralStats } from './GeneralStats';
import type { BlockChainInfo } from './BlockChainInfo';
import type { NetworkInfoResponse } from './NetworkInfoResponse';
import type { BlockInfoResponse } from './BlockInfo';
import type { Peer } from './Peer';
import type { SubverDistribution } from './SubverDistribution';
import { MempoolInfoResponse } from './MempoolInfoResponse';

/**
 * Represents the complete dashboard data structure for the Bitcoin node dashboard.
 */
export interface DashboardData {
  /** General peer statistics */
  generalStats: GeneralStats;
  /** Blockchain info (height, headers, etc.) */
  blockchainInfoResponse: BlockChainInfo;
  /** Node info (version, protocol, etc.) */
  nodeInfo: NetworkInfoResponse;
  /** Node uptime as a formatted string */
  upTime: string;
  /** List of inbound peers */
  inboundPeer: Peer[];
  /** List of outbound peers */
  outboundPeer: Peer[];
  /** Subversion distribution for inbound and outbound peers */
  subverDistribution: {
    inbound: SubverDistribution[];
    outbound: SubverDistribution[];
  };
  /** Latest block info */
  block: BlockInfoResponse;
  /** Whether the RPC is connected */
  rpcConnected: boolean;
  /** Optional error message */
  errorMessage?: string;
  /** Mempool statistics */
  mempoolInfo: MempoolInfoResponse;
  /** Partial errors map */
  errors?: { [key: string]: string };
}
