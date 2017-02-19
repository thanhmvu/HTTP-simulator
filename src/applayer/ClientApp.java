package applayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import lowerlayers.TransportLayer;

/**
 * This class represents a client application who requests files from the server
 *
 * @author Thanh Vu
 */
public class ClientApp {

    private final TransportLayer transportLayer;
    private final LocalCache localCache;
    private double httpVersion;
    private final double initHttpVersion;
    private final long initPropDelay, initTransDelayPerByte;

    /**
     * Default constructor to create client
     *
     * @param httpVersion
     * @param propDelay
     * @param transDelayPerByte
     */
    public ClientApp(double httpVersion, long propDelay, long transDelayPerByte) {
        //create a new transport layer for client (hence false)
        boolean isServer = false;
        transportLayer = new TransportLayer(isServer, propDelay, transDelayPerByte);
        localCache = new LocalCache();
        this.httpVersion = httpVersion;
        this.initHttpVersion = httpVersion;
        this.initPropDelay = propDelay;
        this.initTransDelayPerByte = transDelayPerByte;
    }

    /**
     * Experiment method that retrieves and displays a web page
     *
     * @return time to get the whole web page
     */
    public long downloadWebPage(String url) {
        print("requesting " + url + " ...");

        long start = System.currentTimeMillis();
        Document doc = retrieveWebPage(url);
        long end = System.currentTimeMillis();
        long responseTime = (end - start);
        print("Total response time: " + responseTime + " ms");

        display(doc);
        return responseTime;
    }

    /**
     * Experiment method that retrieves and displays a web page
     *
     * @return time to get the whole web page
     */
    public long downloadWebPages(ArrayList<String> urls) {
        long start = System.currentTimeMillis();
        if (!urls.isEmpty()) {
            if (this.httpVersion == 1.2) {
                print("Version 1.2. Downloading a list of web pages ...");
                ArrayList<Document> docs = retrieveMultiWebPage(urls);
                for (Document doc : docs) {
                    display(doc);
                }
            } else {
                print("Version " + this.httpVersion + ". Downloading first web page in the list ...");
                for (String url : urls) {
                    print("requesting " + url + " ...");
                    Document doc = retrieveWebPage(url);
                    display(doc);
                }
            }
        }
        long end = System.currentTimeMillis();
        long responseTime = (end - start);
        print("Total response time: " + responseTime + " ms");
        return responseTime;
    }

    /**
     * ======================= MULTI REQUEST & RECEIVE ========================
     * *
     */
    /**
     * Request and retrieve multiple web pages
     *
     * @param url The URL of the file
     * @return The document representing the file
     */
    private ArrayList<Document> retrieveMultiWebPage(ArrayList<String> urls) {
        ArrayList<Document> docs = new ArrayList<>();
        try {
            // create a list of requests
            ArrayList<RequestPacket> reqPackets = new ArrayList<>();
            for (String url : urls) {
                RequestPacket reqPacket = new RequestPacket(httpVersion, "GET", url);
                reqPackets.add(reqPacket);
            }

            // request and receive
            requestMulti(reqPackets);
            HashMap<String, String> pages = receiveMulti(reqPackets); // <url, content>

            // recursively create Doc and retrieve embedded files
            for (String url : pages.keySet()) {
                // Convert text to Document
                Document doc = new Document(url, pages.get(url));
                ArrayList<Document> embdDocs = this.retrieveMultiWebPage(doc.getEmbdUrls());
                doc.addEmbdDocs(embdDocs);
                docs.add(doc);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return docs;
    }

    private void requestMulti(ArrayList<RequestPacket> reqPackets)
            throws InterruptedException {
        // check if object was cached before
        for (RequestPacket reqPacket : reqPackets) {
            if (localCache.existsInCache(reqPacket.getUrl())) {
                reqPacket.addHeading("If-modified-since",
                        localCache.getCachedLastModifiedTime(reqPacket.getUrl()));
                System.out.println("Found existing cache. Send conditional GET");
            } else {
                System.out.println("No existing cache found. Send normal GET");
            }
        }
        transportLayer.sendMultiForClient(reqPackets, httpVersion);
    }

    private HashMap<String, String> receiveMulti(ArrayList<RequestPacket> reqPackets)
            throws InterruptedException {
        ArrayList<ResponsePacket> resPackets
                = transportLayer.receiveMultiForClient(httpVersion);

        HashMap<String, String> pages = new HashMap<>();
        for (ResponsePacket resPacket : resPackets) {
            String url = resPacket.getValue("URL");
            switch (resPacket.getStatusCode()) {
                case 200: // OK
                    print("receiving " + url + ". Status 200");
                    pages.put(url, resPacket.getBody());
                    // cache the recieved object
                    localCache.cache(url,
                            resPacket.getValue("Last-Modified"),
                            resPacket.getBody());
                    break;
                case 304: // Not modified
                    print(url + "is not modified. Retrieved from cached.");
                    pages.put(url, localCache.getCachedContent(url));
                    break;
                case 404: // Not found
                    pages.put(url, resPacket.getBody());
                    break;
            }
        }

        return pages;
    }

    /**
     * ======================= REQUEST & RECEIVE ======================== *
     */
    /**
     * Request and retrieve a web page
     *
     * @param url The URL of the file
     * @return The document representing the file
     */
    private Document retrieveWebPage(String url) {
        Document doc = null;
        try {
            RequestPacket reqPacket = new RequestPacket(httpVersion, "GET", url);

            long start = System.currentTimeMillis();
            request(reqPacket);
            String content = receive(reqPacket);
            long end = System.currentTimeMillis();
            print("Time to get " + url + ": " + (end - start) + " ms");

            // Convert content to Document
            doc = new Document(url, content);

            // recursively retrieve embedded files
            for (Document file : doc.getEmbdFiles()) {
                Document embdContent = this.retrieveWebPage(file.getUrl());
                doc.addEmbdDoc(file.getUrl(), embdContent);
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doc;
    }

    private void request(RequestPacket reqPacket) throws InterruptedException {
//        long start = System.currentTimeMillis();
        // check if object was cached before
        if (localCache.existsInCache(reqPacket.getUrl())) {
            reqPacket.addHeading("If-modified-since",
                    localCache.getCachedLastModifiedTime(reqPacket.getUrl()));
            System.out.println("Found existing cache. Send conditional GET");
        } else {
            System.out.println("No existing cache found. Send normal GET");
        }
//        long end = System.currentTimeMillis();
//        print("Time to check cache: " + (end-start) + " ms");

        // convert request to byte array and send to transport layer
        byte[] byteArray = reqPacket.toProtocol().getBytes();
        transportLayer.sendForClient(byteArray, httpVersion);
    }

    private String receive(RequestPacket reqPacket) throws InterruptedException {
        byte[] byteArray = transportLayer.receiveForClient(httpVersion);

//        long start = System.currentTimeMillis();
        String response = new String(byteArray);
        ResponsePacket resPacket = new ResponsePacket(response);

        String requestedObj = null;
        switch (resPacket.getStatusCode()) {
            case 200: // OK
                requestedObj = resPacket.getBody();
                // cache the recieved object
                localCache.cache(reqPacket.getUrl(),
                        resPacket.getValue("Last-Modified"),
                        requestedObj);
                break;
            case 304: // Not modified
                requestedObj = localCache.getCachedContent(reqPacket.getUrl());
                break;
            case 404: // Not found
                requestedObj = resPacket.getBody();
                break;
        }
//        long end = System.currentTimeMillis();
//        print("Time to get to extract the content: " + (end-start) + " ms");

        return requestedObj;
    }

    /**
     * ============================ SETTERS ============================= *
     */
    public void setPropDelay(long propDelay) {
        transportLayer.setPropDelay(propDelay);
    }

    public void setTransDelayPerByte(long transDelayPerByte) {
        transportLayer.setTransDelayPerByte(transDelayPerByte);
    }

    public void setHttpVersion(double version) {
        this.httpVersion = version;
    }

    /**
     * Reset to how it was constructed
     */
    public void reset() {
        this.setPropDelay(initPropDelay);
        this.setTransDelayPerByte(initTransDelayPerByte);
        this.setHttpVersion(initHttpVersion);
        localCache.empty();
    }

    /**
     * ============================= HELPERS ============================ *
     */
    /**
     * Method to display a document
     *
     * @param doc the document object to display
     */
    private void display(Document doc) {
        print("Server responses: \n" + doc.getFullText());
    }

    /**
     * Method to print client logs in a specific format
     *
     * @param s string to print
     */
    public static void print(String s) {
        System.out.println(">>>>> [CA] " + s);
    }

    /**
     * Method that runs the client application, accepting input requests from
     * users via standard input
     */
    public void test() {
        try {
            //read in first line from keyboard
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = reader.readLine();

            //while line is not empty
            while (line != null && !line.equals("")) {
                String url = line;

                long start = System.currentTimeMillis();
                RequestPacket reqPacket = new RequestPacket(httpVersion, "GET", url);
                request(reqPacket);
                String content = receive(reqPacket);
                long end = System.currentTimeMillis();
                print("Response time: " + (end - start) + " ms");

                print(content);

                //read next line
                line = reader.readLine();
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ClientApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * =============================== MAIN ============================= *
     */
    public static void main(String[] args) throws Exception {
        System.out.println();
        ClientApp client = new ClientApp(1.0, 1000, 10);
        print("This is Client App:");
        ArrayList<String> urls = new ArrayList<>();
        urls.add("pA2.txt");
        client.downloadWebPages(urls);
//
//        client.reset();
//        client.setPropDelay(100);
//        client.downloadWebPage("parent.txt");
    }
}
