package applayer;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * A packet is the basis for a message sent through the network
 * @author hongha912
 */
public class Packet {

    protected HashMap<String, String> headings;
    protected double version;
    protected static final String CRLF = "\r\n";
    protected static final String SP = " ";

    /**
     * Create a packet
     * @param version HTTP version (1.0 or 1.1)
     */
    protected Packet(double version) {
        this.version = version;
        headings = new LinkedHashMap<>();
    }

    /**
     * Add a heading and value to the packet
     * @param heading Add a heading
     * @param value Add a value
     */
    protected void addHeading(String heading, String value) {
        headings.put(heading, value);
    }

    /**
     * Add many heading-value pairs to the packet
     * @param headings an existing heading-value map
     */
    protected void addHeadings(HashMap<String, String> headings) {
        this.headings.putAll(headings);
    }

    public String getValue(String heading) {
        return headings.get(heading);
    }
}
