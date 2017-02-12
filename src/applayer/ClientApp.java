package applayer;

import util.Config;
//import lowerlayers.TransportLayer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

//This class represents the client application
public class ClientApp {
//    TransportLayer transportLayer;
    LocalCache localCache;
    
    public ClientApp(int propDelay, int transDelayPerByte){
        //create a new transport layer for client (hence false)
        boolean isServer = false;
//        transportLayer = new TransportLayer(isServer, propDelay, transDelayPerByte);
        localCache = new LocalCache(); 
    }
    
    public void run(){
        try{
            //read in first line from keyboard
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = reader.readLine();

            //while line is not empty
            while (line != null && !line.equals("")) {
                String url = line;
                RequestPacket reqPacket = new RequestPacket(Config.HTTP_VERSION,"GET",url);
                String content = localCache.requestAndReceive(reqPacket);
                display(content);
                //read next line
                line = reader.readLine();
            }
        }catch (Exception ex){
            Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void display(String content){
        System.out.println("Server responses: "+content);
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println();
        ClientApp client = new ClientApp(Config.PROP_DELAY,Config.TRANS_DELAY_PER_BYTE);
        System.out.println("This is Client App. Request to server:");
        client.run();
    }
}
