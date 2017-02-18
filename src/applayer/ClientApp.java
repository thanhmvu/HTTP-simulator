package applayer;

import util.Config;
//import lowerlayers.TransportLayer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * This class represents a client application
 * who requests files from the server
 *
 * @author Thanh Vu
 */
public class ClientApp {
    // TransportLayer transportLayer;
    LocalCache localCache;
    
    /**
     * Default constructor to create client
     */
    public ClientApp(){
        //create a new transport layer for client (hence false)
        boolean isServer = false;
        //  transportLayer = new TransportLayer(isServer, propDelay, transDelayPerByte);
        localCache = new LocalCache(); 
    }
    
    /**
     * Request and retrieve a Document object of a file
     * 
     * @param url The URL of the file
     * @return The document representing the file
     */
    public Document request(String url){
        Document doc = null;
        try {
//            long start = System.currentTimeMillis();
            RequestPacket reqPacket = new RequestPacket(Config.HTTP_VERSION,"GET",url);
            String content = localCache.requestAndReceive(reqPacket);
//            long end = System.currentTimeMillis();
//            print("Time to get "+ url +" : "+ (end-start) + " ms");
            
            // Convert content to Document
            doc = new Document(url,content);
            
            // recursively retrieve embedded files
            for(Document file: doc.getEmbdFiles()) {
                Document embdContent = this.request(file.getUrl());
                doc.addEmbdDoc(file.getUrl(),embdContent);
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return doc;
    }
    
    /**
     * Method that runs the client application,
     * accepting input requests from users via standard input
     */
    public void test(){
        try{
            //read in first line from keyboard
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = reader.readLine();

            //while line is not empty
            while (line != null && !line.equals("")) {
                String url = line;
                
                long start = System.currentTimeMillis();
                RequestPacket reqPacket = new RequestPacket(Config.HTTP_VERSION,"GET",url);
                String content = localCache.requestAndReceive(reqPacket);
                long end = System.currentTimeMillis();
                print("Response time: "+ (end-start) + " ms");
                
                print(content);
                
                //read next line
                line = reader.readLine();
            }
        }catch (IOException | InterruptedException ex){
            Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Method to display a document
     * 
     * @param doc the document object to display
     */
    public void display(Document doc){
        print("Server responses: \n"+doc.getFullText());
    }
    
    /**
     * Method to print client logs in a specific format
     * 
     * @param s string to print
     */
    public static void print(String s){
        System.out.println(">>>>> [CA] "+s);
    }
    
    /**
     * Experiment method that examines response time of the network simulation
     * 
     * @return time to get the whole web page
     */
    public long downloadWebpage(){
        String url = "parent.txt";
        print("requesting "+url+" ...");
        
        long start = System.currentTimeMillis();
        Document doc = request(url);
        long end = System.currentTimeMillis();
        long responseTime = (end-start);
        print("Response time: "+ responseTime + " ms");
        
        display(doc);
        return responseTime;
    }
    
    /** =============================== Main ============================= **/
    public static void main(String[] args) throws Exception {
        System.out.println();
        ClientApp client = new ClientApp();
        print("This is Client App:");
        client.downloadWebpage();
    }
}
