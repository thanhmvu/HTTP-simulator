package applayer;

import static applayer.Packet.CRLF;
import java.util.Scanner;

/**
 * Implements the request HTTP packet
 *
 * @author hongha912
 */
public class RequestPacket extends Packet {

    /**
     * Method to be implemented
     */
    protected enum Method {
        GET, POST;
    }

    protected Method method;
    protected String url;

    /**
     * Create a new request packet
     *
     * @param version 1.0, 1.1 or 1.2 (improvements)
     * @param method The input method
     * @param url The url to be requested
     */
    public RequestPacket(double version, String method, String url) {
        super(version);
        this.method = Method.valueOf(method);
        this.url = url;
    }

    /**
     * Create a request packet using input protocol string
     *
     * @param protocol The encoding of the message
     */
    public RequestPacket(String protocol) {
        super();
        parseProtocol(protocol);
    }

    /**
     * Parse input parameters from protocol string The protocol is as below
     *
     * [Method][SP][url][SP][HTTP/version][CRLF] [heading1:][SP][value1][CRLF]
     * [heading2:][SP][value2][CRLF] [CRLF]
     *
     * @param protocol The encoding of the message
     */
    private void parseProtocol(String protocol) {
        try (Scanner sc = new Scanner(protocol)) {
            //parse first line
            this.method = Method.valueOf(sc.next());
            this.url = sc.next();
            String httpVersion = sc.next();
            this.version = Double.parseDouble(httpVersion.substring(5));

            while (sc.hasNext(".+:")) {
                String header = sc.next();
                header = header.substring(0, header.indexOf(':'));
                String value = sc.next() + sc.nextLine();
                this.headings.put(header, value);
            }
        }

    }

    /**
     * Convert a request packet to protocol. The protocol is as below
     *
     * [Method][SP][url][SP][HTTP/version][CRLF] [heading1:][SP][value1][CRLF]
     * [heading2:][SP][value2][CRLF] [CRLF]
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

    /**
     * Retrieve method of the packet
     *
     * @return method of the packet
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Retrieve URL of the packet
     *
     * @return URL of the packet
     */
    public String getUrl() {
        return url;
    }

}
