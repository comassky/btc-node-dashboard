import type { GeneralStats } from './GeneralStats';
import type { BlockChainInfo } from './BlockChainInfo';
import type { NodeInfo } from './NodeInfo';
import type { BlockInfo } from './BlockInfo';
import type { Peer } from './Peer';
import type { SubverDistribution } from './SubverDistribution';

export interface DashboardData {
  generalStats: GeneralStats;
  blockchainInfo: BlockChainInfo;
  nodeInfo: NodeInfo;
  upTime: string;
  inboundPeer: Peer[];
  outboundPeer: Peer[];
  subverDistribution: {
    inbound: SubverDistribution[];
    outbound: SubverDistribution[];
  };
  block: BlockInfo;
  rpcConnected: boolean;
  errorMessage?: string;
}
