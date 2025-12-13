// Interface TypeScript correspondant à la classe Java MempoolInfoResponse
// Fournit des statistiques sur l'état actuel de la mempool des transactions non confirmées du nœud.

export interface MempoolInfoResponse {
  loaded: boolean;           // True si la mempool est complètement chargée après le démarrage/redémarrage
  size: number;              // Le nombre total de transactions dans la mempool
  bytes: number;             // Taille totale (en bytes) de toutes les transactions dans la mempool
  usage: number;             // Mémoire (en bytes) utilisée par la mempool (y compris les index)
  maxmempool: number;        // Taille maximale de la mempool configurée (en bytes)
  mempoolminfee: number;     // Taux de frais minimum (en BTC/kB) pour que les transactions soient acceptées dans la mempool
  minrelaytxfee: number;     // Le taux de frais minimum pour relayer une transaction (en BTC/kB)
  unbroadcastcount: number;  // Nombre de transactions dans la mempool qui n'ont pas encore été diffusées aux pairs
  total_fee: number;         // Les frais totaux (en BTC) de toutes les transactions dans la mempool
}
