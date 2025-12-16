
/**
 * Information about a specific network interface.
 */
export interface NetworkInfo {
  /** Network name (e.g., 'ipv4') */
  name: string;
  /** Whether the network is limited */
  limited: boolean;
  /** Whether the network is reachable */
  reachable: boolean;
  /** Proxy address if used */
  proxy: string;
  /** Whether proxy credentials are randomized */
  proxy_randomize_credentials: boolean;
}

/**
 * Local address information for the node.
 */
export interface LocalAddress {
  /** IP address */
  address: string;
  /** Port number */
  port: number;
  /** Score for address selection */
  score: number;
}

/**
 * Node network information response.
 */
export interface NetworkInfoResponse {
  /** Node version */
  version: number;
  /** Node subversion string */
  subversion: string;
  /** Protocol version */
  protocolversion: number;
  /** Local services string */
  localservices: string;
  /** Names of local services */
  localservicesnames: string[];
  /** Whether local relay is enabled */
  localrelay: boolean;
  /** Time offset in seconds */
  timeoffset: number;
  /** Number of connections */
  connections: number;
  /** Whether network is active */
  networkactive: boolean;
  /** List of network interfaces */
  networks: NetworkInfo[];
  /** List of local addresses */
  localaddresses: LocalAddress[];
}
