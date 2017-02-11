package applayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
        //detect request type and do stuff
        ResponsePacket response = this.respondToGet(reqPacket);
        return response;
    }

    private ResponsePacket respondToGet(RequestPacket reqPacket) {
        String url = reqPacket.getUrl();
        Path path = Paths.get(url);
        byte[] encoded;
        
        
        String cacheModifiedTimeStr = reqPacket.getValue("If-modified-since");
        //FileTime cacheModifiedTime = FileTime.from(LocalDateTime.parse(cacheModifiedTimeStr, DateTimeFormatter.ISO_DATE).toInstant(ZoneOffset.UTC));
        if (cacheModifiedTimeStr != null) {
            
                        
        }
        //ADD CHECK FOR IF-MODIFIED
        try {
            encoded = Files.readAllBytes(path);
            FileTime fileModifiedTime = Files.getLastModifiedTime(path);
            String body = new String(encoded, StandardCharsets.UTF_8);
            return new ResponsePacket(reqPacket.getVersion(), 200, "OK", body);
        } catch (IOException ex) {
            return new ResponsePacket(reqPacket.getVersion(), 404, "Not Found", "");
        }
    }

    public static void main(String[] args) throws Exception {
        ServerApp server = new ServerApp(100, 10);
        server.listen();
    }
}
