package lowerlayers;

import util.Config;

public class TransportLayer {

    private final NetworkLayer networkLayer;
    boolean connectionOpen;
    boolean server;

    /**
     * Build transport layer
     *
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

    /**
     * Send a payload
     *
     * @param payload The payload
     * @throws InterruptedException
     */
    public void send(byte[] payload) throws InterruptedException {
        handShakeIfNoConnection();
        networkLayer.send(payload);

        // if non-persistent, Close connection when client received the packet
        if (Config.HTTP_VERSION == 1.0 && server) {
            closeConnection();
        }
    }

    public byte[] receive() throws InterruptedException {
        handShakeIfNoConnection();
        byte[] payload = networkLayer.receive();

        // if non-persistent, Close connection when client received the packet
        if (Config.HTTP_VERSION == 1.0 && !server) {
            closeConnection();
        }
        return payload;
    }

    private void closeConnection() {
        System.out.println(server ? "Server closes connection" : "Client closes connection");
        connectionOpen = false;
    }

    private void handShakeIfNoConnection() throws InterruptedException {
        if (!connectionOpen) {
            if (!server) {
                clientHandshakes();
            } else {
                serverHandshakes();
            }
        }

    }

    private void serverHandshakes() throws InterruptedException {
        this.sendHandshakeProtocol("SYN");
        this.listenForHandshake("ACK");
        connectionOpen = true;
    }

    private void clientHandshakes() throws InterruptedException {
        this.listenForHandshake("SYN");
        connectionOpen = true;
        this.sendHandshakeProtocol("ACK");
    }

    /**
     * Send a handshake protocol to the other host
     *
     * @param protocol The phrasing of the protocol
     * @throws InterruptedException
     */
    private void sendHandshakeProtocol(String protocol) throws InterruptedException {
        networkLayer.send((protocol).getBytes());
        System.out.println(server ? "Server sends " + protocol : "Client sends " + protocol);
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
                System.out.println(server ? "Server receives " + protocol : "Client receives " + protocol);
                break;
            }
        }

    }
}
