package applayer;

import java.util.Scanner;

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
     * Create a respond packet using input protocol string
     *
     * @param protocol The encoding of the message
     */
    public ResponsePacket(String protocol) {
        super();
        parseProtocol(protocol);
    }

    /**
     * Parse input parameters from protocol string
     *
     * Protocol format is as below:
     * [HTTP/version][SP][statusCode][SP][phrase][CRLF]
     * [heading1:][SP][value1][CRLF] [heading2:][SP][value2][CRLF] [CRLF] [body]
     *
     * @param protocol The encoding of the message
     */
    public final void parseProtocol(String protocol) {
        try (Scanner sc = new Scanner(protocol)) {
            //parse first line
            String httpVersion = sc.next();
            this.version = Double.parseDouble(httpVersion.substring(5));
            this.statusCode = sc.nextInt();
            this.phrase = sc.next() + sc.nextLine();

            while (sc.hasNext("\\w+:")) {
                String header = sc.next();
                header = header.substring(0, header.indexOf(':'));
                String value = sc.next() + sc.nextLine();
                this.headings.put(header, value);
            }
        }

        String[] headerBody = protocol.split(CRLF + CRLF);
        if (headerBody.length > 1) {
            this.body = headerBody[1];
        }
    }

    /**
     * Convert a response packet to protocol
     *
     * Protocol format is as below:
     * [HTTP/version][SP][statusCode][SP][phrase][CRLF]
     * [heading1:][SP][value1][CRLF] 
     * [heading2:][SP][value2][CRLF] 
     * [CRLF] 
     * [body]
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

    public int getStatusCode() {
        return statusCode;
    }
}
