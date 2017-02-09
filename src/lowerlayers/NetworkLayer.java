package lowerlayers;

import java.util.concurrent.TimeUnit;

public class NetworkLayer {

    private LinkLayer linkLayer;
    private final static int DEFAULT_PROP_DELAY = 1000;
    private final static int DEFAULT_TRANS_DELAY_PER_BYTE = 10;
    private int propDelay, transDelayPerByte;

    /**
     * Default constructor
     * @param server Whether the server is up
     */
    public NetworkLayer(boolean server) {
        linkLayer = new LinkLayer(server);
        propDelay = DEFAULT_PROP_DELAY;
        transDelayPerByte = DEFAULT_TRANS_DELAY_PER_BYTE;

    }
    
    /**
     * Constructor with delay parameters
     * @param server Whether the server is up
     * @param propDelay Propagation delay (ms)
     * @param transDelayPerByte Transmission delay (ms)
     */
    public NetworkLayer(boolean server, int propDelay, int transDelayPerByte) {
        linkLayer = new LinkLayer(server);
        this.propDelay = propDelay;
        this.transDelayPerByte = transDelayPerByte;
    }

    /**
     * Send the payload
     * @param payload Payload
     * @throws InterruptedException Exception that happens when thread is interrupted
     */
    public void send(byte[] payload) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(propDelay);
        TimeUnit.MILLISECONDS.sleep(transDelayPerByte * payload.length);
        linkLayer.send(payload);
    }

    public byte[] receive() {
        byte[] payload = linkLayer.receive();
        return payload;
    }
}
