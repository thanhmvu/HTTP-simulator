package applayer;

import util.Config;
import static applayer.Packet.CRLF;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author hongha912
 */
public class RequestPacket extends Packet {

    protected enum Method {
        GET, POST;
    }

    protected Method method;
    protected String url;

    public RequestPacket(double version, String method, String url) {
        super(version);
        this.method = Method.valueOf(method);
        this.url = url;
    }
    /**
     * Create a request packet using input protocol string
     * @param protocol The encoding of the message
     */
    public RequestPacket(String protocol) {
        super();
        parseProtocol(protocol);
    }
    
    /**
     * Parse input parameters from protocol string
     * @param protocol The encoding of the message
     */
    private void parseProtocol(String protocol) {
        String[] lines = protocol.split(CRLF);

        if (lines.length > 0) {
            String[] firstLineTokens = lines[0].split(SP);
            this.method = Method.valueOf(firstLineTokens[0]);
            this.url = firstLineTokens[1];
            String httpVersion = firstLineTokens[2];
            this.version = Double.parseDouble(httpVersion.substring(5));

            int i = 1;
            for (i = 1; i < lines.length && !lines[i].equals(CRLF); i++) {
                String header = lines[i].substring(0, lines[i].indexOf(':'));
                String value = lines[i].substring(lines[i].indexOf(':') + 2, lines[i].length());
                this.headings.put(header, value);
            }
        } else {
            this.version = Config.HTTP_VERSION;
            this.method = Method.valueOf("GET");
            this.url = protocol;
        }
    }

    /**
     * Convert a request packet to protocol
     *
     * @return Protocol The string to be transmitted to network
     */
    public String toProtocol() {
        StringBuilder protocol = new StringBuilder();
        protocol.append(method).append(SP).append(url).append(SP)
                .append("HTTP/").append(version)
                .append(CRLF);
        for (String heading : headings.keySet()) {
            String value = headings.get(heading);
            protocol.append(heading).append(':').append(SP).append(value).append(CRLF);
        }
        protocol.append(CRLF);
        return protocol.toString();
    }

    public Method getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

}
