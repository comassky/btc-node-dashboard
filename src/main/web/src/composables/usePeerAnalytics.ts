import { computed, type Ref } from 'vue';
import type { Peer } from '@types';

interface PeerAverages {
  minping: number | null;
  bytesrecv: number | null;
  bytessent: number | null;
  timeoffset: number | null;
  conntime: number | null;
}

/**
 * Composable for peer analytics and statistics
 * Optimized single-pass calculation of averages
 */
export function usePeerAnalytics(peers: Ref<Peer[]>) {
  function average(arr: number[]): number | null {
    return arr.length ? arr.reduce((a, b) => a + b, 0) / arr.length : null;
  }

  const peerAverages = computed<PeerAverages>(() => {
    const stats = {
      minping: [] as number[],
      bytesrecv: [] as number[],
      bytessent: [] as number[],
      timeoffset: [] as number[],
      conntime: [] as number[],
    };

    for (const peer of peers.value) {
      if (typeof peer.minping === 'number' && !isNaN(peer.minping))
        stats.minping.push(peer.minping);
      if (typeof peer.bytesrecv === 'number' && !isNaN(peer.bytesrecv))
        stats.bytesrecv.push(peer.bytesrecv);
      if (typeof peer.bytessent === 'number' && !isNaN(peer.bytessent))
        stats.bytessent.push(peer.bytessent);
      if (typeof peer.timeoffset === 'number' && !isNaN(peer.timeoffset))
        stats.timeoffset.push(peer.timeoffset);
      if (typeof peer.conntime === 'number' && !isNaN(peer.conntime))
        stats.conntime.push(peer.conntime);
    }

    return {
      minping: average(stats.minping),
      bytesrecv: average(stats.bytesrecv),
      bytessent: average(stats.bytessent),
      timeoffset: average(stats.timeoffset),
      conntime: average(stats.conntime),
    };
  });

  const totalPeers = computed(() => peers.value.length);

  const healthStatus = computed(() => {
    const count = totalPeers.value;
    if (count >= 10) return 'healthy';
    if (count >= 5) return 'warning';
    return 'critical';
  });

  return {
    peerAverages,
    totalPeers,
    healthStatus,
  };
}
