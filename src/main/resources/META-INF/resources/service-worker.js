const CACHE_NAME = 'btc-dashboard-cache-v1';

// Liste des fichiers à mettre en cache lors de l'installation
// Incluez ici toutes les dépendances critiques et ressources statiques locales.
const urlsToCache = [
    './', // L'URL racine (votre dashboard.html rendu par Qute)
    'manifest.json', // Servi par votre contrôleur REST/Qute
    // Les ressources statiques se trouvant dans META-INF/resources/
    '/css/style.css', // Exemple de CSS
    '/images/icon-192x192.png',
    '/images/icon-512x512.png',
    // Ajoutez ici toutes vos dépendances CDN critiques si vous voulez qu'elles fonctionnent hors ligne.
    'https://unpkg.com/vue@3/dist/vue.global.js',
    'https://cdn.jsdelivr.net/npm/chart.js',
];

// --- 1. Événement 'install' (Mise en cache des ressources) ---
self.addEventListener('install', event => {
    console.log('Service Worker: Installation...');
    event.waitUntil(
        caches.open(CACHE_NAME)
        .then(cache => {
            console.log('Service Worker: Mise en cache des ressources...');
            return cache.addAll(urlsToCache);
        })
        .catch(err => console.error('Service Worker: Échec de la mise en cache:', err))
    );
});


// --- 2. Événement 'fetch' (Stratégie de mise en cache) ---
self.addEventListener('fetch', event => {

    // 🛑 RÈGLE CRUCIALE POUR QUARKUS/WEBSOCKET :
    // Nous devons ignorer toutes les requêtes WebSocket (qui ne peuvent pas être mises en cache)
    const url = new URL(event.request.url);
    if (url.pathname.includes('/ws/dashboard') || url.protocol === 'ws:' || url.protocol === 'wss:') {
        return;
    }

    // Stratégie: Cache-First (Essaie de servir depuis le cache, sinon va sur le réseau)
    event.respondWith(
        caches.match(event.request)
        .then(response => {
            // Si une correspondance est trouvée dans le cache, la renvoyer
            if (response) {
                return response;
            }
            // Sinon, effectuer une requête réseau
            return fetch(event.request);
        })
    );
});


// --- 3. Événement 'activate' (Nettoyage des anciens caches) ---
self.addEventListener('activate', event => {
    console.log('Service Worker: Activation (nettoyage ancien cache)...');
    const cacheWhitelist = [CACHE_NAME];

    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    // Supprimer les caches qui ne sont pas dans la liste blanche (whitelist)
                    if (cacheWhitelist.indexOf(cacheName) === -1) {
                        console.log('Service Worker: Suppression du vieux cache:', cacheName);
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
});