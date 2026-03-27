var CACHE='pwa-v6';

self.addEventListener('install',function(e){
  self.skipWaiting();
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
  // index.html — תמיד נסה רשת כדי לקבל גרסה עדכנית, fallback למטמון
  var url=new URL(e.request.url);
  if(url.pathname==='/'||url.pathname.endsWith('/index.html')){
    e.respondWith(
      fetch(e.request).then(function(res){
        if(res&&res.status===200){
          var clone=res.clone();
          caches.open(CACHE).then(function(c){c.put(e.request,clone);});
        }
        return res;
      }).catch(function(){return caches.match(e.request);})
    );
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
