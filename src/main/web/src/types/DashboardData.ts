import type { GeneralStats } from './GeneralStats';
import type { BlockChainInfo } from './BlockChainInfo';
import type { NetworkInfoResponse } from './NetworkInfoResponse';
import type { BlockInfoResponse } from './BlockInfo';
import type { Peer } from './Peer';
import type { SubverDistribution } from './SubverDistribution';
import { MempoolInfoResponse } from './MempoolInfoResponse';

export interface DashboardData {
  generalStats: GeneralStats;
  blockchainInfoResponse: BlockChainInfo;
  nodeInfo: NetworkInfoResponse;
  upTime: string;
  inboundPeer: Peer[];
  outboundPeer: Peer[];
  subverDistribution: {
    inbound: SubverDistribution[];
    outbound: SubverDistribution[];
  };
  block: BlockInfoResponse;
  rpcConnected: boolean;
  errorMessage?: string;
  mempoolInfo: MempoolInfoResponse;
}
