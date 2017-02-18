package lowerlayers;

import java.util.concurrent.TimeUnit;
import util.Config;

public class NetworkLayer {

    private LinkLayer linkLayer;

    /**
     * Constructor with delay parameters
     *
     * @param server Whether the server is up
     * @param propDelay Propagation delay (ms)
     * @param transDelayPerByte Transmission delay (ms)
     */
    public NetworkLayer(boolean server) {
        linkLayer = new LinkLayer(server);
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
     * @return A payload
     * @throws InterruptedException Exception that happens when thread is
     * interrupted
     */
    public byte[] receive() throws InterruptedException {
        byte[] payload = linkLayer.receive();
        return payload;
    }

    /**
     * Delay the process
     * @param payload
     * @throws InterruptedException 
     */
    private void delay(byte[] payload) throws InterruptedException {
        if (payload != null) {
            TimeUnit.MILLISECONDS.sleep(Config.PROP_DELAY);
            TimeUnit.MILLISECONDS.sleep(Config.TRANS_DELAY_PER_BYTE * payload.length);
        }

    }
}
