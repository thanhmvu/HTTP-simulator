/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package applayer;

import util.Config;
import java.util.HashMap;
import lowerlayers.TransportLayer;

/**
 *
 * @author thanhvu
 */
public class LocalCache {
    class CachedObject{
        public String url;
        public String lastModifiedTime;
        public String content;
        
        public CachedObject (String url, String lmt, String content){
            this.url = url;
            this.lastModifiedTime = lmt;
            this.content = content;
        }
    }
    
    private String cacheDir;
    private HashMap<String,CachedObject> caches;
    TransportLayer transportLayer;
    
    public LocalCache(){
        cacheDir = "../../assets/local-cached-files/";
        caches = new HashMap<>();
        
        boolean isServer = false;
        transportLayer = new TransportLayer(isServer, Config.PROP_DELAY,Config.TRANS_DELAY_PER_BYTE);
    }
    
    public String requestAndReceive(RequestPacket reqPacket) throws InterruptedException{
        request(reqPacket);   
        String response = receive(reqPacket);
        
        return response;
    }
    
    private void request(RequestPacket reqPacket) throws InterruptedException{
        // check if object was cached before
        CachedObject cachedObj = caches.get(reqPacket.getUrl());
        if(cachedObj != null){
            reqPacket.addHeading("If-modified-since", cachedObj.lastModifiedTime);
            System.out.println("Found existing cache. Send conditional GET");
        } else {
            System.out.println("No existing cache found. Send normal GET");
        }
        
        // convert request to byte array and send to transport layer
        byte[] byteArray = reqPacket.toProtocol().getBytes();
        transportLayer.send(byteArray);
    }
    
    private String receive(RequestPacket reqPacket) throws InterruptedException{
        byte[] byteArray = transportLayer.receive();
        String response = new String(byteArray);
        ResponsePacket resPacket = new ResponsePacket(response);
        
        String requestedObj = null;
        switch (resPacket.getStatusCode()) {
            case 200: // OK
                requestedObj = resPacket.toProtocol();
                // cache the recieved object
                CachedObject objToCache = new CachedObject(reqPacket.getUrl(), 
                        resPacket.getValue("Last-Modified"), requestedObj);
                caches.put(reqPacket.getUrl(), objToCache);
                break;
            case 304: // Not modified
                CachedObject cachedObj = caches.get(reqPacket.getUrl());
                requestedObj = cachedObj.content;
                break;
            case 404: // Not found
                requestedObj = resPacket.toProtocol();
        }
        
        return requestedObj;
    }
}
