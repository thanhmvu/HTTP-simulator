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
     */
    public TransportLayer(boolean server) {
        networkLayer = new NetworkLayer(server);
        this.server = server;
        connectionOpen = false;
    }

    /**
     * Send a payload
     *
     * @param payload The payload
     * @throws InterruptedException If the thread is disrupted
     */
    public void send(byte[] payload) throws InterruptedException {
        handShakeIfNoConnection();
        if (payload == null) {
            closeConnection();
            return;
        }
        print("sending packet of size " + payload.length + "bytes");
        networkLayer.send(payload);

        // if non-persistent, Close connection when client received the packet
        if (Config.HTTP_VERSION == 1.0 && server) {
            closeConnection();
        }

    }

    /**
     * Receive a payload from the outputstream
     *
     * @return a byte array that is the payload
     * @throws InterruptedException If the thread is disrupted
     */
    public byte[] receive() throws InterruptedException {
        handShakeIfNoConnection();
        byte[] payload = networkLayer.receive();

        //handling null payload
        if (payload == null) {
            closeConnection();
            return null;
        }

        print("receiving packet of size " + payload.length + "bytes");

        // if non-persistent, Close connection when client received the packet
        if (Config.HTTP_VERSION == 1.0 && !server) {
            closeConnection();
        }
        return payload;
    }

    /**
     * Close the connection established by transport layer
     */
    private void closeConnection() {
        print(server ? "Server closes connection" : "Client closes connection");
        connectionOpen = false;
    }

    //===========IMPLEMENTATION OF HANDSHAKES======================
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
