package lowerlayers;

public class TransportLayer {

    private final NetworkLayer networkLayer;
    boolean connectionOpen;
    boolean server;

    /**
     * Build transport layer
     *
     * @param server server is true if the application is a server (should
     * listen) or false if it is a client (should try and connect)
     * @param propDelay Propagation delay
     * @param transDelayPerByte Transmission delay per byte
     */
    public TransportLayer(boolean server, long propDelay, long transDelayPerByte) {
        networkLayer = new NetworkLayer(server, propDelay, transDelayPerByte);
        this.server = server;
        connectionOpen = false;
    }

    /**
     * Send a payload
     *
     * @param payload The payload
     * @param httpVersion HTTP version 1.0, 1.1, 1.2
     * @throws InterruptedException If the thread is disrupted
     */
    public void send(byte[] payload, double httpVersion) throws InterruptedException {
        handShakeIfNoConnection();
        if (payload == null) {
            closeConnection();
            return;
        }
        print("sending packet of size " + payload.length + "bytes");
        networkLayer.send(payload);

        // if non-persistent, Close connection when client received the packet
        if (httpVersion == 1.0 && server) {
            closeConnection();
        }
    }

    /**
     * Receive a payload from the outputstream
     *
     * @param httpVersion HTTP version - 1.0, 1.1, 1.2
     * @return a byte array that is the payload
     * @throws InterruptedException If the thread is disrupted
     */
    public byte[] receiveForClient(double httpVersion) throws InterruptedException {
        byte[] payload = receive();
        // if non-persistent, Close connection when client received the packet
        if (httpVersion == 1.0 && !server) {
            closeConnection();
        }
        return payload;
    }

    public byte[] receive() throws InterruptedException {
        handShakeIfNoConnection();
        byte[] payload = networkLayer.receive();

        //handling null payload
        if (payload == null) {
            closeConnection();
            return null;
        }

        print("receiving packet of size " + payload.length + "bytes");
        return payload;
    }

    /**
     * Close the connection established by transport layer
     */
    private void closeConnection() {
        print(server ? "Server closes connection" : "Client closes connection");
        connectionOpen = false;
    }
    
    //============SET PARAMETER METHODS=============================
    public void setPropDelay(long propDelay) {
        networkLayer.setPropDelay(propDelay);
    }

    public void setTransDelayPerByte(long transDelayPerByte) {
        networkLayer.setTransDelayPerByte(transDelayPerByte);
    }

    //===========IMPLEMENTATION OF HANDSHAKES======================
    /**
     * Main handshake method, applied to BOTH client & server. It checks if
     * there is connection. If there's not, it creates a handshake
     *
     * @throws InterruptedException When thread is interrupted.
     */
    private void handShakeIfNoConnection() throws InterruptedException {
        if (!connectionOpen) {
            if (!server) {
                clientHandshakes();
            } else {
                serverHandshakes();
            }
        }
    }

    /**
     * Implements handshake for server (listens for syn, send ack)
     *
     * @throws InterruptedException
     */
    private void serverHandshakes() throws InterruptedException {
        this.listenForHandshake("SYN");
        connectionOpen = true;
        this.sendHandshakeProtocol("ACK");
    }

    /**
     * Implements handshake for client (send ack, listen for syn)
     *
     * @throws InterruptedException
     */
    private void clientHandshakes() throws InterruptedException {
        this.sendHandshakeProtocol("SYN");
        this.listenForHandshake("ACK");
        connectionOpen = true;
    }

    /**
     * Send a handshake protocol to the other host
     *
     * @param protocol The phrasing of the protocol
     * @throws InterruptedException
     */
    private void sendHandshakeProtocol(String protocol) throws InterruptedException {
        networkLayer.send((protocol).getBytes());
        print(server ? "Server sends " + protocol : "Client sends " + protocol);
    }

    /**
     * Wait until receiving a handshake protocol from the other host
     *
     * @param protocol The phrasing of the protocol
     * @throws InterruptedException
     */
    private void listenForHandshake(String protocol) throws InterruptedException {
        while (true) {
            byte[] byteArray = networkLayer.receive();
            //if client disconnected
            if (byteArray == null) {
                break;
            }
            String str = new String(byteArray);
            if (str.equals(protocol)) {
                print(server ? "Server receives " + protocol : "Client receives " + protocol);

                break;
            }
        }

    }

    //===================HELPER METHODS==================================
    /**
     * Method to print transport layer logs in a specific format
     *
     * @param s string to print
     */
    public static void print(String s) {
        System.out.println(">>>>> [TL] " + s);
    }
}
