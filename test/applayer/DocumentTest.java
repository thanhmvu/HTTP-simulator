/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package applayer;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author thanhvu
 */
public class DocumentTest {
    
    public DocumentTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getEmbdFiles method, of class Document.
     */
    @Test
    public void testGetEmbdFiles() {
        System.out.println("getEmbdFiles");
        String url = "./test1.txt";
        String content = "abc def \nxyz";
        Document doc = new Document(url, content);
        
        // no embd file
        ArrayList<Document> result = doc.getEmbdFiles();
        assertEquals(result.size(), 0);
        
        // one embd file
        content = "abc <<./test2.txt>> def \nxyz";
        doc = new Document(url, content);
        String[] expResult = {"./test2.txt"};
        result = doc.getEmbdFiles();
        for(int i = 0; i < result.size(); i++){
            assertEquals(expResult[i], result.get(i).getUrl());
        }
        
        // avg
        content = "abc <<./test2.txt>> def \nxyz\n <<./test3.txt>>\nx";
        doc = new Document(url, content);
        String[] expResult2 = {"./test2.txt", "./test3.txt"};
        result = doc.getEmbdFiles();
        for(int i = 0; i < result.size(); i++){
            assertEquals(expResult2[i], result.get(i).getUrl());
        }
    }
    
    @Test
    public void testConstructor_StrStr(){
        System.out.println("testConstructor_StrStr");
        String content = "abc <<./test2.txt>> def \nxyz";
        String url = "./test1.txt";
        Document doc = new Document(url, content);
        assertEquals(doc.getContent(),content);
        assertEquals(doc.getUrl(),url);
        assertEquals(doc.getNumOfEmbdFiles(),1);
    }
    
    @Test
    public void testConstructor_Str(){
        System.out.println("testConstructor_Str");
        String url = "./test1.txt";
        Document doc = new Document(url);
        assertEquals(doc.getUrl(),url);
        assertEquals(doc.getContent(),doc.getOpenSign()+ url+ doc.getCloseSign());
        assertEquals(doc.getNumOfEmbdFiles(),0);
    }
    
    @Test
    public void testAddEmbdContent(){
        System.out.println("testAddEmbdContent");
        String url = "./test1.txt";
        String content = "abc <<./test2.txt>> def \nxyz";
        String embdTxt = "123";
        Document doc = new Document(url, content);
        
        // false
        boolean expRes = false;
        boolean result = doc.addEmbdContent("./test100.txt", embdTxt);
        assertEquals(expRes, result);
        
        // true
        expRes = true;
        result = doc.addEmbdContent("./test2.txt", embdTxt);
        assertEquals(expRes, result);
        assertEquals(doc.getEmbdContent("./test2.txt"), embdTxt);
    }

    /**
     * Test of getFullText method, of class Document.
     */
    @Test
    public void testGetFullText() {
        System.out.println("getFullText");
        
        // text not available
        String url = "./test1.txt";
        Document doc = new Document(url);
        String expResult = doc.getOpenSign()+ url+ doc.getCloseSign();
        String result = doc.getFullText();
        assertEquals(expResult,result);
        
        // text not available
        url = "./test1.txt";
        String content = "abc <<./test2.txt>> def \nxyz";
        doc = new Document(url,content);
        expResult = content;
        result = doc.getFullText();
        assertEquals(expResult,result);
        
        // avg
        url = "./test1.txt";
        content = "abc <<./test2.txt>> def \nxyz\n <<./test3.txt>> \n <<./test4.txt>>";
        doc = new Document(url,content);
        doc.addEmbdContent("./test2.txt", "222");
        doc.addEmbdContent("./test4.txt", "444");
        expResult = "abc 222 def \nxyz\n <<./test3.txt>> \n 444";
        result = doc.getFullText();
        assertEquals(expResult,result);
    }

    /**
     * Test of addContent method, of class Document.
     */
//    @Test
//    public void testAddContent() {
//        System.out.println("addContent");
//        String content = "";
//        Document doc = null;
//        doc.addContent(content);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    
}
