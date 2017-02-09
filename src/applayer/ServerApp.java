package applayer;

import lowerlayers.TransportLayer;


public class ServerApp {
    private TransportLayer transportLayer;
    private int transDelayPerByte;
    
    /**
     * Set up a server
     * @param propDelay
     * @param transDelayPerByte
     */
    public ServerApp(int propDelay, int transDelayPerByte) {
        transportLayer = new TransportLayer(true, propDelay, transDelayPerByte);
        
    }
    
    /**
     * Listen to the incoming stream
     */
    private void listen() throws InterruptedException {
        while (true) {
            //receive message from client, and send the "received" message back.
            byte[] byteArray = transportLayer.receive();
            //if client disconnected
            if (byteArray == null) {
                break;
            }
            String str = new String(byteArray);
            System.out.println(str);
            String line = "received";
            byteArray = line.getBytes();
            transportLayer.send(byteArray);

        }
    }

    public static void main(String[] args) throws Exception {
        ServerApp server = new ServerApp(100, 10);
        server.listen();
    }
}
