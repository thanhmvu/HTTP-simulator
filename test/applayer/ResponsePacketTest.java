
package applayer;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hongha912
 */
public class ResponsePacketTest {
    
    public ResponsePacketTest() {
    }

    /**
     * Test of parseProtocol method, of class ResponsePacket.
     */
    @Test
    public void testParseProtocol() {
        System.out.println("parseProtocol");
        String protocol = "HTTP/1.1 304 Not Modified\r\n"
                + "heading1: value1\r\n"
                + "\r\n"
                + "body";
        ResponsePacket instance = new ResponsePacket(protocol);
        assertEquals(instance.statusCode,304);
        assertEquals(instance.body,"body");
        assertEquals(instance.getValue("heading1"), "value1");
    }

    /**
     * Test of toProtocol method, of class ResponsePacket.
     */
    @Test
    public void testToProtocol() {
        System.out.println("toProtocol");
        ResponsePacket instance = new ResponsePacket(1.0, 304, "Not Modified", "body\r\nbody2\r\n");
        String expResult = "HTTP/1.0 304 Not Modified\r\n"
                + "\r\n"
                + "body\r\nbody2\r\n";
        String result = instance.toProtocol();
        assertEquals(expResult, result);
    }

    /**
     * Test of getStatusCode method, of class ResponsePacket.
     */
    @Test
    public void testGetStatusCode() {
        System.out.println("getStatusCode");
        ResponsePacket instance = new ResponsePacket(1.0, 304, "Not Modified", "body");
        int expResult = 304;
        int result = instance.getStatusCode();
        assertEquals(expResult, result);
    }
    
}
