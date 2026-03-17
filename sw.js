self.addEventListener('install', function(e) {
  self.skipWaiting();
});
self.addEventListener('activate', function(e) {
  e.waitUntil(self.clients.claim());
});
self.addEventListener('fetch', function(e) {
  // iOS Safari נכשל כשמיירטים בקשות cross-origin — נטפל רק בבקשות מאותו domain
  if(e.request.url.startsWith(self.location.origin)) {
    e.respondWith(fetch(e.request).catch(function(){return caches.match(e.request);}));
  }
});
