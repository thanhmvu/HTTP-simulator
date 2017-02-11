package applayer;

import util.Config;
import lowerlayers.TransportLayer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

//This class represents the client application
public class ClientApp {
    TransportLayer transportLayer;
    
    public ClientApp(int propDelay, int transDelayPerByte){
        //create a new transport layer for client (hence false)
        boolean isServer = false;
        transportLayer = new TransportLayer(isServer, propDelay, transDelayPerByte);
    }
    
    public void run(){
        try{
            //read in first line from keyboard
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = reader.readLine();

            //while line is not empty
            while (line != null && !line.equals("")) {
                request(line);
                receive();
                //read next line
                line = reader.readLine();
            }
        }catch (Exception e){
            System.out.println("Error mess: "+e);
        }
    }
    
    public void request(String request){
        try {
            //convert lines into byte array, send to transoport layer and wait for response
            byte[] byteArray = request.getBytes();
            transportLayer.send(byteArray);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void receive(){
        try {
            byte[] byteArray = transportLayer.receive();
            String str = new String(byteArray);
            display(str);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void display(String content){
        System.out.println(content);
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println();
        ClientApp client = new ClientApp(Config.PROP_DELAY,Config.TRANS_DELAY_PER_BYTE);
        System.out.println("Send requests to server:");
        client.run();
    }
}
