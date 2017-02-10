package lowerlayers;

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
        this.server = server;
        connectionOpen = false;
    }

    private void sendHandshakeProtocol(String protocol) throws InterruptedException {
        networkLayer.send((protocol).getBytes());
        System.out.println(server ? "Server" : "Client" + " sends " + protocol);
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
                System.out.println(server ? "Server" : "Client" + " receives " + protocol);

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
        networkLayer.send(payload);
    }

    public byte[] receive() throws InterruptedException {
        if (!connectionOpen) {
            handShake();
        }
        byte[] payload = networkLayer.receive();
        return payload;
    }
}
