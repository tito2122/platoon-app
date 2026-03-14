self.addEventListener('install', function(e) {
  self.skipWaiting();
});
self.addEventListener('activate', function(e) {
  e.waitUntil(clients.claim());
});
self.addEventListener('fetch', function(e) {
  // אל תחסום כלום — תן לכל הבקשות לעבור רגיל
  return;
});
