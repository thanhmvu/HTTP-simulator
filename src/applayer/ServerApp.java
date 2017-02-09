package applayer;

import lowerlayers.TransportLayer;


public class ServerApp {
    private TransportLayer transportLayer;
    
    /**
     * Set up a server
     */
    public ServerApp() {
        transportLayer = new TransportLayer(true);
    }
    
    /**
     * Listen to the incoming stream
     */
    private void listen() {
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
        ServerApp server = new ServerApp();
        server.listen();
    }
}
