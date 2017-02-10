package applayer;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author hongha912
 */
public class ResponsePacket extends Packet {

    protected int statusCode;
    protected String phrase;
    protected String body;

    public ResponsePacket(double version, int statusCode, String phrase, String body) {
        super(version);
        this.statusCode = statusCode;
        this.phrase = phrase;
        this.body = body;
    }

    /**
     * Convert from protocol to a response message
     * @param protocol The encoding of the message
     * @return a ResponseMessage object
     */
    public static ResponsePacket fromProtocol(String protocol) {
        String[] lines = protocol.split(CRLF);
        String[] firstLineTokens = lines[0].split(SP);
        String httpVersion = firstLineTokens[0];
        double version = Double.parseDouble(httpVersion.substring(5));
        int statusCode = Integer.parseInt(firstLineTokens[1]);
        String phrase = firstLineTokens[2];

        HashMap<String, String> headings = new LinkedHashMap<>();
        int i = 1;
        for (i = 1; i < lines.length && !lines[i].equals(CRLF); i++) {
            String header = lines[i].substring(0, lines[i].indexOf(':'));
            String value = lines[i].substring(lines[i].indexOf(':') + 2, lines[i].length());
            headings.put(header, value);
        }
        String body = "";
        for (i = i + 1; i < lines.length; i++) {
            body += CRLF + lines[i];
        }

        ResponsePacket msg = new ResponsePacket(version, statusCode, phrase, body);
        msg.addHeadings(headings);
        return msg;

    }

    /**
     * Convert a response packet to protocol
     *
     * @return Protocol The string to be transmitted to network
     */
    public String toProtocol() {
        StringBuilder protocol = new StringBuilder();
        protocol.append("HTTP/").append(version).append(SP)
                .append(statusCode).append(SP).append(phrase).append(CRLF);
        for (String heading : headings.keySet()) {
            String value = headings.get(heading);
            protocol.append(heading).append(':').append(SP).append(value).append(CRLF);
        }
        protocol.append(CRLF);
        protocol.append(body);
        return protocol.toString();
    }

}
