package applayer;

import util.Config;
//import lowerlayers.TransportLayer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class represents a client application
 * who requests files from the server
 *
 * @author Thanh Vu
 */
public class ClientApp {
    // TransportLayer transportLayer;
    LocalCache localCache;
    
    /** ============================ Constructor ========================== **/
    /**
     * Default constructor to create client
     */
    public ClientApp(){
        //create a new transport layer for client (hence false)
        boolean isServer = false;
        //  transportLayer = new TransportLayer(isServer, propDelay, transDelayPerByte);
        localCache = new LocalCache(); 
    }
    
    
    /** ============================== Methods ============================ **/
    /**
     * Request and retrieve a Document object of a file
     * 
     * @param url The URL of the file
     * @return The document representing the file
     */
    public Document request(String url){
        Document doc = null;
        try {
            long start = System.currentTimeMillis();
            RequestPacket reqPacket = new RequestPacket(Config.HTTP_VERSION,"GET",url);
            String content = localCache.requestAndReceive(reqPacket);
            long end = System.currentTimeMillis();
            System.out.println("Time to get "+ url +" : "+ (end-start) + " ms");
            
            // Convert content to Document
            doc = new Document(url,content);
            
            // recursively retrieve embedded files
            doc.getEmbdFiles().forEach((Document file) -> {
                Document embdContent = this.request(file.getUrl());
                file.addEmbdDoc(url,embdContent);
            });
            
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return doc;
    }
    
    /**
     * Method that runs the client application,
     * accepting input requests from users via standard input
     */
    public void run(){
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
                System.out.println("Response time: "+ (end-start) + " ms");
                
                System.out.println(content);
                
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
        System.out.println("Server responses: "+doc.getFullText());
    }
    
    
    /** ========================= Experiment Methods ======================= **/
    /**
     * Experiment method that examines response time of the network simulation
     */
    public void runExperiment(){
        String url = "parent.txt";
        Document doc = request(url);
        display(doc);
    }
    
    /**
     * Experiment method that runs the experiment for transport delay
     */
    public void runTransExp(){
    
    }
    
    /**
     * Experiment method that runs the experiment for propagation delay
     */
    public void runPropExp(){
    
    }
    
    /**
     * Experiment method that runs the experiment for number of requested files
     */
    public void runFileNumExp(){
    
    }
    
    
    /** =============================== Main ============================= **/
    public static void main(String[] args) throws Exception {
        System.out.println();
        ClientApp client = new ClientApp();
        System.out.println("This is Client App. Request to server:");
        client.run();
    }
}
