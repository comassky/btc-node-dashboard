import { registerSW } from 'virtual:pwa-register';

export function registerServiceWorker() {
  const updateSW = registerSW({
    onNeedRefresh() {
      if (confirm('New version available! Reload to update?')) {
        updateSW(true);
      }
    },
    onOfflineReady() {
      // App ready to work offline (silent in production)
    },
  });
}
