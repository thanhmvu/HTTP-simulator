package applayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import lowerlayers.TransportLayer;

/**
 * This application runs the server, find the file, send it back to the client upon request.
 * @author hongha912
 */
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
        String url = reqPacket.getUrl();
        Path path = Paths.get(url);
        byte[] encoded;

        //get cache modified time (if exist)
        String cacheModifiedTimeStr = reqPacket.getValue("If-modified-since");
        FileTime cacheModifiedTime = null;
        if (cacheModifiedTimeStr != null) {
            long milis;
            try {
                milis = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").parse(cacheModifiedTimeStr).getTime();
                cacheModifiedTime = FileTime.fromMillis(milis);
            } catch (ParseException ex) {
            }
        }

        try {
            //check for modified time
//            FileTime fileModifiedTime = Files.getLastModifiedTime(path);
//            if (cacheModifiedTime != null && fileModifiedTime.compareTo(cacheModifiedTime) > 0) {
//                return new ResponsePacket(reqPacket.getVersion(), 304, "Not Modified", "");
//            }

            //read file and send file
            encoded = Files.readAllBytes(path);
            String body = new String(encoded, StandardCharsets.UTF_8);
            return new ResponsePacket(reqPacket.getVersion(), 200, "OK", body);
        } catch (IOException ex) {
            //file not found
            return new ResponsePacket(reqPacket.getVersion(), 404, "Not Found", "");
        } 
    }

    public static void main(String[] args) throws Exception {
        ServerApp server = new ServerApp(100, 10);
        System.out.println("The server is listenting ...");
        server.listen();
    }
}
