package lowerlayers;

public class TransportLayer {

    private final NetworkLayer networkLayer;
    

    //server is true if the application is a server (should listen) or false if it is a client (should try and connect)
    public TransportLayer(boolean server) {
        networkLayer = new NetworkLayer(server);
    }
    
    public TransportLayer(boolean server, int propDelay, int transDelayPerByte) {
        networkLayer = new NetworkLayer(server, propDelay, transDelayPerByte);
    }

    public void send(byte[] payload) throws InterruptedException {
        
        networkLayer.send(payload);
    }

    public byte[] receive() {
        byte[] payload = networkLayer.receive();
        return payload;
    }
}
