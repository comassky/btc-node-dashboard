export interface GeneralStats {
  inboundCount: number;
  outboundCount: number;
  totalPeers: number;
}

export interface BlockChainInfo {
  blocks: number;
  headers: number;
  chain: string;
  verificationprogress: number;
  difficulty: number;
  medianBlockSize: number;
}

export interface NodeInfo {
  version: string;
  protocolVersion: string;
  subversion: string;
}

export interface BlockInfo {
  time: number;
  nTx: number;
  hash?: string;
}

export interface Peer {
  id: number;
  addr: string;
  subver: string;
  version: number;
  timeoffset: number;
  conntime: number;
  network: string | null;
  connection_type: string;
  minping: number | null;
  bytesrecv: number;
  bytessent: number;
}

export interface SubverDistribution {
  server: string;
  count: number;
  percentage: number;
}

// Simplifié: une seule interface pour les données du dashboard
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