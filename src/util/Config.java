package util;

import java.text.SimpleDateFormat;

/**
 *
 * @author thanhvu
 */
public class Config {

    public static int PROP_DELAY = 1000;
    public static int TRANS_DELAY_PER_BYTE = 10;
    public static double HTTP_VERSION = 1.1;
    public static final SimpleDateFormat HTTP_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
    
    public static void reset(){
        PROP_DELAY = 1000;
        TRANS_DELAY_PER_BYTE = 10;
        HTTP_VERSION = 1.1;
    }
}
