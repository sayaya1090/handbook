const CACHE_NAME = 'handbook-v3';
const STATIC_ASSETS = [
    '/',
    '/app.html',
    '/css/agent.css',
    '/manifest.json'
];

// Install: 정적 리소스 캐시
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(STATIC_ASSETS))
            .then(() => self.skipWaiting())
    );
});

// Activate: 이전 캐시 정리
self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys()
            .then(keys => Promise.all(
                keys.filter(key => key !== CACHE_NAME)
                    .map(key => caches.delete(key))
            ))
            .then(() => self.clients.claim())
    );
});

// Fetch: Network-first, 실패 시 캐시 fallback
self.addEventListener('fetch', event => {
    const { request } = event;

    // API 요청 및 GWT bootstrap 파일은 캐시 bypass.
    // *.nocache.js 는 GWT permutation selector — 매 배포마다 바뀌므로 캐시하면 구 permutation
    // 을 계속 참조하게 되어 새 *.cache.js 해시로 도달하지 못한다.
    if (request.url.includes('/workspace/') ||
        request.url.includes('/auth/') ||
        request.url.includes('/menus') ||
        request.url.includes('/user') ||
        request.url.includes('.nocache.js') ||
        request.url.includes('.devmode.js')) {
        return;
    }

    event.respondWith(
        fetch(request)
            .then(response => {
                // 성공 시 캐시 업데이트
                if (response.ok) {
                    const clone = response.clone();
                    caches.open(CACHE_NAME).then(cache => cache.put(request, clone));
                }
                return response;
            })
            .catch(() => caches.match(request))
    );
});
