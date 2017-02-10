package applayer;

import java.util.HashMap;
import java.util.LinkedHashMap;



/**
 *
 * @author hongha912
 */
public class Packet {
    
    protected HashMap<String, String> headings;
    protected double version;
    protected static final String CRLF = "\r\n";
    protected static final String SP = " ";
    protected Packet(double version) {
        this.version = version;
        headings = new LinkedHashMap<>();
    }
    
    protected void addHeading(String heading, String value) {
        headings.put(heading, value);
    }
    
    protected void addHeadings(HashMap<String, String> headings) {
        this.headings.putAll(headings);
    }
    
    public String getValue(String heading) {
        return headings.get(heading);
    }
}
