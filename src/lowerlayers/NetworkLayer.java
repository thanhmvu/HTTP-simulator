package lowerlayers;

import java.util.concurrent.TimeUnit;

public class NetworkLayer {

    private LinkLayer linkLayer;
    private int propDelay, transDelayPerByte;

    /**
     * Constructor with delay parameters
     *
     * @param server Whether the server is up
     * @param propDelay Propagation delay (ms)
     * @param transDelayPerByte Transmission delay (ms)
     */
    public NetworkLayer(boolean server, int propDelay, int transDelayPerByte) {
        linkLayer = new LinkLayer(server);
        if (server) {
            this.propDelay = propDelay;
            this.transDelayPerByte = transDelayPerByte;
        } else {
            this.propDelay = 0;
            this.transDelayPerByte = 0;
        }
    }

    /**
     * Send the payload
     *
     * @param payload Payload
     * @throws InterruptedException Exception that happens when thread is
     * interrupted
     */
    public void send(byte[] payload) throws InterruptedException {
        this.delay(payload);
        linkLayer.send(payload);
    }

    public byte[] receive() throws InterruptedException {
        byte[] payload = linkLayer.receive();
        this.delay(payload);
        return payload;
    }
    
    private void delay(byte[] payload) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(propDelay);
        TimeUnit.MILLISECONDS.sleep(transDelayPerByte * payload.length);
    }
}
