/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package applayer;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hongha912
 */
public class RequestPacketTest {
    
    public RequestPacketTest() {
    }

    /**
     * Test of toProtocol method, of class RequestPacket.
     */
    @Test
    public void testToProtocol() {
        System.out.println("toProtocol");
        RequestPacket instance = null;
        String expResult = "";
        String result = instance.toProtocol();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMethod method, of class RequestPacket.
     */
    @Test
    public void testGetMethod() {
        System.out.println("getMethod");
        RequestPacket instance = null;
        RequestPacket.Method expResult = null;
        RequestPacket.Method result = instance.getMethod();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUrl method, of class RequestPacket.
     */
    @Test
    public void testGetUrl() {
        System.out.println("getUrl");
        RequestPacket instance = null;
        String expResult = "";
        String result = instance.getUrl();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
