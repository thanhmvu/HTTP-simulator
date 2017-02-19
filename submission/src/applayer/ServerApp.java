package applayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lowerlayers.TransportLayer;

/**
 * This application runs the server, find the file, send it back to the client
 * upon request.
 *
 * @author hongha912
 */
public class ServerApp {

    private final TransportLayer transportLayer;
    private final String root = "assets/server-db/";

    /**
     * Set up a server
     *
     */
    public ServerApp() {
        transportLayer = new TransportLayer(true, 0, 0);

    }

    /**
     * Listen to the incoming stream
     */
    private void listen() throws InterruptedException, IOException {
        while (true) {
            //receive message from client, and send the "received" message back.
            byte[] byteArray = transportLayer.receiveForServer();
            //if client disconnected
            if (byteArray == null) {
                break;
            }

            //convert to request packet
            String protocol = new String(byteArray);
            System.out.println(protocol);
            RequestPacket reqPacket = new RequestPacket(protocol);

            //get response
            ResponsePacket resPacket = respond(reqPacket);
            String response = resPacket.toProtocol();
            
            if(reqPacket.getVersion() == 1.2){
                //get HTTP 1.2 header
                boolean closeAfterThis = 
                        reqPacket.getValue("Close-after-this").equals("true");
                transportLayer.sendMultiForServer(response.getBytes(), 
                        reqPacket.getVersion(), closeAfterThis);
            } else {
                transportLayer.sendForServer(response.getBytes(), reqPacket.getVersion());
            }

        }
    }

    /**
     * Respond to request from client
     *
     * @param reqPacket The request packet
     * @return The ResponsePacket that is appropriate
     */
    private ResponsePacket respond(RequestPacket reqPacket) {

        ResponsePacket response = null;
        switch (reqPacket.getMethod()) {
            case GET:
                response = this.respondToGetReq(reqPacket);
                break;
            case POST:
                break;
        }
        return response;
    }

    /**
     * Draft a ResponsePacket to respond to a GET request
     *
     * @param reqPacket Request packet
     * @return Response packet
     */
    private ResponsePacket respondToGetReq(RequestPacket reqPacket) {
        String url = root + reqPacket.getUrl();
        Path path = Paths.get(url);
        byte[] encoded;

        //get cache modified time (if exist)
        String cacheMTime = reqPacket.getValue("If-modified-since");

        try {
            ResponsePacket resPacket = null;
            //check for modified time
            String fileMTime = Packet.TIME_FORMAT.format(Files.getLastModifiedTime(path).toMillis());
            if (cacheMTime != null && fileMTime.compareTo(cacheMTime) == 0) {
                resPacket = new ResponsePacket(reqPacket.getVersion(), 304, "Not Modified", "");
                resPacket.addHeading("URL",reqPacket.getUrl());
            } else {
                //read file and send file
                encoded = Files.readAllBytes(path);
                String body = new String(encoded, StandardCharsets.UTF_8);

                //create packet with last-modified
                resPacket = new ResponsePacket(reqPacket.getVersion(), 200, "OK", body);
                resPacket.addHeading("Last-Modified", fileMTime);
                resPacket.addHeading("URL",reqPacket.getUrl());
            }
            return resPacket;
        } catch (IOException ex) {
            //file not found
            ResponsePacket resPacket = new ResponsePacket(reqPacket.getVersion(), 404, "Not Found", "");
            resPacket.addHeading("URL",reqPacket.getUrl());
            return resPacket;
        }
    }

    public static void main(String[] args) throws Exception {
        ServerApp server = new ServerApp();
        System.out.println("The server is listenting ...");
        server.listen();
    }
}
