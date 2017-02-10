package applayer;

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
     * Convert from protocol to a response message
     *
     * @param protocol The encoding of the message
     * @return a ResponseMessage object
     */
    public static RequestPacket fromProtocol(String protocol) {
        String[] lines = protocol.split(CRLF);

        if (lines.length >= 2) {
            String[] firstLineTokens = lines[0].split(SP);
            String method = firstLineTokens[0];
            String url = firstLineTokens[1];
            String httpVersion = firstLineTokens[2];
            double version = Double.parseDouble(httpVersion.substring(5));

            HashMap<String, String> headings = new LinkedHashMap<>();
            int i = 1;
            for (i = 1; i < lines.length && !lines[i].equals(CRLF); i++) {
                String header = lines[i].substring(0, lines[i].indexOf(':'));
                String value = lines[i].substring(lines[i].indexOf(':') + 2, lines[i].length());
                headings.put(header, value);
            }

            RequestPacket msg = new RequestPacket(version, method, url);
            msg.addHeadings(headings);
            return msg;
        }
        return new RequestPacket(1.1, "GET", protocol);

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
