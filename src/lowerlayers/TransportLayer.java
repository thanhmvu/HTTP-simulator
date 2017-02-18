package lowerlayers;
import util.Config;

public class TransportLayer {

    private final NetworkLayer networkLayer;
    boolean connectionOpen;
    boolean server;

    /**
     * Build transport layer
     * @param server server is true if the application is a server (should
     * listen) or false if it is a client (should try and connect)
     * @param propDelay Propagation delay (ms)
     * @param transDelayPerByte Transmission delay per second (ms)
     */
    public TransportLayer(boolean server, int propDelay, int transDelayPerByte) {
        networkLayer = new NetworkLayer(server, propDelay, transDelayPerByte);
        print("trans prop: "+propDelay);
        print("trans trans: "+transDelayPerByte);
        this.server = server;
        connectionOpen = false;
    }

    private void sendHandshakeProtocol(String protocol) throws InterruptedException {
        networkLayer.send((protocol).getBytes());
        print(server ? "Server sends " + protocol : "Client sends " + protocol);
    }

    private void listenForHandshake(String protocol) throws InterruptedException {
        while (true) {
            byte[] byteArray = networkLayer.receive();
            //if client disconnected
            if (byteArray == null) {
                break;
            }
            String str = new String(byteArray);
            if (str.equals(protocol)) {
                print(server ? "Server receives " + protocol: "Client receives " + protocol);

                break;
            }
        }

    }

    private void handShake() throws InterruptedException {

        if (!server) {
            this.sendHandshakeProtocol("SYN");
            this.listenForHandshake("ACK");
            connectionOpen = true;
        } else {
            this.listenForHandshake("SYN");
            connectionOpen = true;
            this.sendHandshakeProtocol("ACK");
        }
    }
    
    public void send(byte[] payload) throws InterruptedException {
        if (!connectionOpen) {
            handShake();
        }
        print("sending packet of size "+ payload.length +"bytes");
        networkLayer.send(payload);
        
        // if non-persistent, Close connection when client received the packet
        if (Config.HTTP_VERSION == 1.0 && server){
            print("Server closes connection");
            connectionOpen = false;
        }
    }

    public byte[] receive() throws InterruptedException {
        if (!connectionOpen) {
            handShake();
        }
        byte[] payload = networkLayer.receive();
        print("receiving packet of size "+ payload.length +"bytes");
        
        // if non-persistent, Close connection when client received the packet
        if (Config.HTTP_VERSION == 1.0 && !server){
            print("Client closes connection");
            connectionOpen = false;
        }
        return payload;
    }
    
    /**
     * Method to print transport layer logs in a specific format
     * 
     * @param s string to print
     */
    public static void print(String s){
        System.out.println(">>>>> [TL] "+s);
    }
}
