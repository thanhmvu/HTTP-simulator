/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package applayer;

import java.util.HashMap;
import lowerlayers.TransportLayer;

/**
 *
 * @author thanhvu
 */
public class LocalCache {
    
    /**
     * Store a cache object
     */
    class CachedObject{
        public String url;
        public String lastModifiedTime;
        public String content;
        
        /**
         * Create a cache object
         * @param url
         * @param lmt
         * @param content 
         */
        public CachedObject (String url, String lmt, String content){
            this.url = url;
            this.lastModifiedTime = lmt;
            this.content = content;
        }
    }
    
    private HashMap<String,CachedObject> caches;
    TransportLayer transportLayer;
    
    /**
     * Create a cache in the browser
     */
    public LocalCache(){
        caches = new HashMap<>();
        

    }
    
    public String getCachedLastModifiedTime(String url) {
        return caches.get(url).lastModifiedTime;
    }
    
    public boolean existsInCache(String url) {
        return caches.containsKey(url);
    }
    
    public void cache(String url, String lmt, String content) {
        caches.put(url, new CachedObject(url, lmt, content));
    }
    
    public String getCachedContent(String url) {
        return caches.get(url).content;
    }
    
    /**
     * Empty the cache
     */
    public void empty() {
        caches.clear();
    }
}
