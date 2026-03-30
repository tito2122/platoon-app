var CACHE='pwa-v8';

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

self.addEventListener('message',function(e){
  if(!e.data)return;
  if(e.data.type==='SHOW_NOTIFICATION'){
    var title=e.data.title||'תזכורת';
    var body=e.data.body||'';
    var tag=e.data.tag||'platoon-reminder';
    e.waitUntil(
      self.registration.showNotification(title,{
        body:body,
        icon:'/platoon-app/icons/icon-192.png',
        badge:'/platoon-app/icons/icon-192.png',
        tag:tag,
        renotify:true,
        requireInteraction:true,
        vibrate:[300,100,300,100,600],
        silent:false,
        dir:'rtl',
        lang:'he'
      })
    );
  }
});

self.addEventListener('notificationclick',function(e){
  e.notification.close();
  e.waitUntil(
    clients.matchAll({type:'window',includeUncontrolled:true}).then(function(cls){
      for(var i=0;i<cls.length;i++){
        if(cls[i].url.includes('platoon-app')&&'focus' in cls[i]){return cls[i].focus();}
      }
      if(clients.openWindow)return clients.openWindow('/platoon-app/');
    })
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
