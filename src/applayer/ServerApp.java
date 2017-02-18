package applayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import lowerlayers.TransportLayer;

/**
 * This application runs the server, find the file, send it back to the client
 * upon request.
 *
 * @author hongha912
 */
public class ServerApp {

    private TransportLayer transportLayer;
    private String root = "assets/server-db/";

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
            byte[] byteArray = transportLayer.receive();
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

            transportLayer.send(response.getBytes(), reqPacket.getVersion());

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
        Long cacheModifiedTime = null;
        String cacheModifiedTimeStr = reqPacket.getValue("If-modified-since");
        if (cacheModifiedTimeStr != null) {
            try {
                cacheModifiedTime = Packet.HTTP_TIME_FORMAT.parse(cacheModifiedTimeStr).getTime();
            } catch (ParseException ex) {
            }
        }

        try {
            //check for modified time
            FileTime fileModifiedTime = Files.getLastModifiedTime(path);
            if (cacheModifiedTime != null && fileModifiedTime.toMillis() == cacheModifiedTime) {
                return new ResponsePacket(reqPacket.getVersion(), 304, "Not Modified", "");
            }

            //read file and send file
            encoded = Files.readAllBytes(path);
            String body = new String(encoded, StandardCharsets.UTF_8);

            //create packet with last-modified
            ResponsePacket resPacket = new ResponsePacket(reqPacket.getVersion(), 200, "OK", body);
            resPacket.addHeading("Last-Modified", Packet.HTTP_TIME_FORMAT.format(fileModifiedTime.toMillis()));
            return resPacket;
        } catch (IOException ex) {
            //file not found
            return new ResponsePacket(reqPacket.getVersion(), 404, "Not Found", "");
        }
    }


    public static void main(String[] args) throws Exception {
        ServerApp server = new ServerApp();
        System.out.println("The server is listenting ...");
        server.listen();
    }
}
