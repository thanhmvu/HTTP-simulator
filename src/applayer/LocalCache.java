/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package applayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import util.Config;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    }
    
    private String cacheDir;
    private HashMap<String,CachedObject> caches;
    TransportLayer transportLayer;
    
    public LocalCache(){
        cacheDir = "../../assets/local-cached-files/";
        caches = new HashMap<String,CachedObject>();
        
        boolean isServer = false;
        transportLayer = new TransportLayer(isServer, Config.PROP_DELAY,Config.TRANS_DELAY_PER_BYTE);
    }
    
    public String requestAndReceive(String url, HashMap<String,String> headers) throws InterruptedException{
        RequestPacket reqPacket = new RequestPacket(Config.HTTP_VERSION,"GET",url);
        reqPacket.addHeadings(headers);
        
        // check if object was cached before
        CachedObject cachedObj = caches.get(url);
        if(cachedObj != null){
            reqPacket.addHeading("If-modified-since", cachedObj.lastModifiedTime);
            System.out.println("Found existing cache. Send conditional GET");
        } else {
            System.out.println("No existing cache found. Send normal GET");
        }
        
        // convert request to byte array and send to transport layer
        byte[] byteArray = reqPacket.toProtocol().getBytes();
        transportLayer.send(byteArray);
        
        // get and return the response
        byteArray = transportLayer.receive();
        return new String(byteArray);
    }
}
