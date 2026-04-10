const CACHE_NAME = 'handbook-v1';
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

    // API 요청은 캐시하지 않음
    if (request.url.includes('/workspace/') ||
        request.url.includes('/auth/') ||
        request.url.includes('/menus') ||
        request.url.includes('/user')) {
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
