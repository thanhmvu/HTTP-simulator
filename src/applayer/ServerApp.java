package applayer;

import lowerlayers.TransportLayer;

public class ServerApp {

    private TransportLayer transportLayer;

    /**
     * Set up a server
     *
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

            //convert to request packet
            String str = new String(byteArray);
            System.out.println(str);
            RequestPacket reqPacket = RequestPacket.fromProtocol(str);

            //get response
            ResponsePacket resPacket = respond(reqPacket);
            String response = resPacket.toProtocol();
            byteArray = response.getBytes();
            transportLayer.send(byteArray);

        }
    }

    /**
     * Respond to request from client
     *
     * @param reqPacket The request packet
     * @return The ResponsePacket that is appropriate
     */
    private ResponsePacket respond(RequestPacket reqPacket) {
        //detect request type and do stuff

        //Retrieve file here
        //form new response
        ResponsePacket response = new ResponsePacket(1.1, 200, "OK", "Received"); //PLACEHOLDER - TO BE CHANGED
        return response;
    }

    public static void main(String[] args) throws Exception {
        ServerApp server = new ServerApp(100, 10);
        server.listen();
    }
}
