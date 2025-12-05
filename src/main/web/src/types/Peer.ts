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
