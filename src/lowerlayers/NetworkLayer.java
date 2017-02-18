package lowerlayers;

import java.util.concurrent.TimeUnit;

public class NetworkLayer {

    private LinkLayer linkLayer;
    private long propDelay, transDelayPerByte;

    /**
     * Constructor with delay parameters
     *
     * @param server Whether the server is up
     * @param propDelay Propagation delay
     * @param transDelayPerByte Transmission delay
     */
    public NetworkLayer(boolean server, long propDelay, long transDelayPerByte) {
        linkLayer = new LinkLayer(server);
        this.propDelay = propDelay;
        this.transDelayPerByte = transDelayPerByte;

    }
    
    public void setPropDelay(long propDelay) {
        this.propDelay = propDelay;
    }
    
    public void setTransDelayPerByte(long transDelayPerByte) {
        this.transDelayPerByte = transDelayPerByte;
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

    /**
     * Receive and return what is sent
     *
     * @return A payload
     * @throws InterruptedException Exception that happens when thread is
     * interrupted
     */
    public byte[] receive() throws InterruptedException {
        byte[] payload = linkLayer.receive();
        this.delay(payload);
        return payload;
    }

    /**
     * Delay the process
     *
     * @param payload
     * @throws InterruptedException
     */
    private void delay(byte[] payload) throws InterruptedException {
        if (payload != null) {
            TimeUnit.MILLISECONDS.sleep(propDelay);
            TimeUnit.MILLISECONDS.sleep(transDelayPerByte * payload.length);
        }

    }
}
