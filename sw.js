var CACHE='pwa-v3';
var PRECACHE=[
  '/',
  '/index.html',
  '/manifest.json',
  '/icon-192.png',
  '/icon-512.png',
  'https://www.gstatic.com/firebasejs/8.10.1/firebase-app.js',
  'https://www.gstatic.com/firebasejs/8.10.1/firebase-firestore.js'
];

self.addEventListener('install',function(e){
  self.skipWaiting();
  e.waitUntil(
    caches.open(CACHE).then(function(cache){return cache.addAll(PRECACHE);})
  );
});

self.addEventListener('activate',function(e){
  e.waitUntil(
    caches.keys().then(function(keys){
      return Promise.all(keys.filter(function(k){return k!==CACHE;}).map(function(k){return caches.delete(k);}));
    }).then(function(){return self.clients.claim();})
  );
});

self.addEventListener('fetch',function(e){
  // Firestore API calls — תמיד נסה רשת, אל תחסום
  if(e.request.url.includes('firestore.googleapis.com')||e.request.url.includes('firebase')){
    e.respondWith(fetch(e.request).catch(function(){return new Response('',{status:503});}));
    return;
  }
  // שאר הקבצים — cache first, fallback לרשת
  e.respondWith(
    caches.match(e.request).then(function(cached){
      if(cached)return cached;
      return fetch(e.request).then(function(res){
        if(res&&res.status===200){
          var clone=res.clone();
          caches.open(CACHE).then(function(c){c.put(e.request,clone);});
        }
        return res;
      }).catch(function(){return cached||new Response('',{status:503});});
    })
  );
});
