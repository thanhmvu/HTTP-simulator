package lowerlayers;

public class TransportLayer {

    private final NetworkLayer networkLayer;
    boolean connectionOpen;

    //server is true if the application is a server (should listen) or false if it is a client (should try and connect)
    public TransportLayer(boolean server, int propDelay, int transDelayPerByte) {
        networkLayer = new NetworkLayer(server, propDelay, transDelayPerByte);
        connectionOpen = false;
    }

    private void syn() throws InterruptedException {
        networkLayer.send("SYN".getBytes());
    }

    private void ack() throws InterruptedException {
        networkLayer.send("ACK".getBytes());
    }

    public void send(byte[] payload) throws InterruptedException {
        if (!connectionOpen) {
            
        }
        networkLayer.send(payload);
    }

    public byte[] receive() throws InterruptedException {
        byte[] payload = networkLayer.receive();
        return payload;
    }
}
