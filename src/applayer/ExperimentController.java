/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package applayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thanhvu
 */
public class ExperimentController {

    private ClientApp client;

    private class ExperimentResults {

        private String nameOfExperiment;
        private HashMap<String, List<String>> cols;

        ExperimentResults(String nameOfExp) {
            System.out.println("Start experiment " + nameOfExp);
            nameOfExperiment = nameOfExp;
            cols = new LinkedHashMap<>();
        }

        void add(String colName, String colValue) {
            List<String> col = cols.get(colName);
            if (col == null) {
                col = new ArrayList<>();
                cols.put(colName, col);
            }
            col.add(colValue);
        }

        /**
         * Convert Experiment Results into a CSV String
         *
         * @return a CSV String
         */
        String toCsvString() {
            StringBuilder result = new StringBuilder();
            result.append(nameOfExperiment).append("\n");
            for (String colName : cols.keySet()) {
                result.append(colName);
                for (String val : cols.get(colName)) {
                    result.append(";").append(val);
                }
                result.append("\n");
            }
            return result.toString();
        }

    }

    public ExperimentController() {
        client = new ClientApp(1.0, 100, 2);
    }

    /**
     * Experiment method that runs the experiment for transport delay
     *
     * @param initDelay
     * @param numRuns
     * @param increment
     * @param numTrialsPerRun
     * @param httpVersion
     * @return a list of the runtime
     */
    private ExperimentResults runTransExp(int initDelay, int numRuns, int increment, int numTrialsPerRun, double httpVersion, boolean includeCache) {
        ExperimentResults results = new ExperimentResults("Transmission Delay vs. Response Time");
        for (int i = 0; i < numRuns; i++) {
            long transDelayPerByte = initDelay + i * increment;
            long unCachedSum = 0;
            long cachedSum = 0;
            for (int j = 0; j < numTrialsPerRun; j++) {
                client.reset();
                client.setHttpVersion(httpVersion);
                client.setTransDelayPerByte(transDelayPerByte);
                unCachedSum += downloadWebPages("pA0.txt", "pB0.txt");
                if (includeCache) {
                    cachedSum += downloadWebPages("pA0.txt", "pB0.txt");
                }
            }
            // take the average
            long unCachedTime = unCachedSum / numTrialsPerRun;

            //add to result table
            results.add("HTTP Version", Double.toString(httpVersion));
            results.add("Transmission Delay Per Byte", Long.toString(transDelayPerByte));
            results.add("Uncached Response Time", Long.toString(unCachedTime));
            if (includeCache) {
                results.add("Cached Response Time", Long.toString(cachedSum / numTrialsPerRun));
            }
        }

        return results;
    }

    /**
     * Experiment method that runs the experiment for propagation delay
     *
     * @param initDelay
     * @param numRuns
     * @param increment
     * @param numTrialsPerRun
     * @param httpVersion
     * @return
     */
    private ExperimentResults runPropExp(int initDelay, int numRuns, int increment, int numTrialsPerRun, double httpVersion) {
        ExperimentResults results = new ExperimentResults("Propagation Delay vs. Response Time");

        long sum = 0;
        for (int i = 0; i < numRuns; i++) {
            long propDelay = initDelay + i * increment;
            for (int j = 0; j < numTrialsPerRun; j++) {
                client.reset();
                client.setHttpVersion(httpVersion);
                client.setPropDelay(propDelay);
                sum += this.downloadWebPages("pA0.txt", "pB0.txt");
            }
            // take the average
            long t = sum / numTrialsPerRun;
            //add to result table
            results.add("HTTP Version", Double.toString(httpVersion));
            results.add("Propagation Delay", Long.toString(propDelay));
            results.add("Uncached Response Time", Long.toString(t));
        }
        return results;

    }

    /**
     * Experiment method that runs the experiment for number of requested files
     */
    private ExperimentResults runObjNumberExp(int numRuns, int numTrialsPerRun, double httpVersion) {
        ExperimentResults results = new ExperimentResults("Number of Objects vs. Response Time");

        long sum = 0;
        for (int i = 0; i < numRuns; i++) {
            String urlA = "pA" + i + ".txt";
            String urlB = "pB" + i + ".txt";
            for (int j = 0; j < numTrialsPerRun; j++) {
                client.reset();
                client.setHttpVersion(httpVersion);
                sum += this.downloadWebPages(urlA, urlB);
            }
            // take the average
            long t = sum / numTrialsPerRun;
            //add to result table
            results.add("HTTP Version", Double.toString(httpVersion));
            results.add("Number of parent objects", "2");
            results.add("Number of children per parent", Integer.toString(i));
            results.add("Total number of objects", Integer.toString(i * 2 + 2));
            results.add("Uncached Response Time", Long.toString(t));
        }
        return results;

    }

    public void runExperiment(String outputFile) throws FileNotFoundException {

        
        // ================ Transmission Delay ================ //
        int initDelay = 2;
        int runs = 2;
        int increment = 3;
        int trialPerRun = 2;

        // Non-Persistent + Cached
        ExperimentResults nonPersTransResults = this.runTransExp(initDelay, runs, increment, trialPerRun, 1.0, true);
        // Persistent
        ExperimentResults persTransResults = this.runTransExp(initDelay, runs, increment, trialPerRun, 1.1, false);
        // Multiplex
        //ExperimentResults mulTransResults = this.runTransExp(initDelay, runs, increment, trialPerRun, 1.2, false);
        
        // ================ Propagation Delay ================ //
        
        initDelay = 100;
        increment = 20;

        // Non-Persistent
        ExperimentResults nonPersPropResults = this.runPropExp(initDelay, runs, increment, trialPerRun, 1.0);
        // Persistent
        ExperimentResults persPropResults = this.runPropExp(initDelay, runs, increment, trialPerRun, 1.1);
        // Multiplex
        //ExperimentResults mulPropResults = this.runPropExp(initDelay, runs, increment, trialPerRun, 1.2);

        // ================ Object Number ================ //
        
        // Non-Persistent
        ExperimentResults nonPersObjsResults = this.runObjNumberExp(runs, trialPerRun, 1.0);
        // Persistent
        ExperimentResults persObjsResults = this.runObjNumberExp(runs, trialPerRun, 1.1);
        // Multiplex
        //ExperimentResults mulObjsResults = this.runObjNumberExp(runs, trialPerRun, 1.2);
        
        // ================ Print To File ================ //
        String finalResult = nonPersTransResults.toCsvString() 
                + persTransResults.toCsvString() 
                //+ mulTransResults.toCsvString()
                + nonPersPropResults.toCsvString() 
                + persPropResults.toCsvString() 
                //+ mulPropResults.toCsvString()
                + nonPersObjsResults.toCsvString() 
                + persObjsResults.toCsvString() 
                ;
        try {
            this.printToFile(outputFile, finalResult);
        } catch (IOException ex) {
            Logger.getLogger(ExperimentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("done!");
    }

    private long downloadWebPages(String url1, String url2) {
        return client.downloadWebPage(url1) + client.downloadWebPage(url2);
    }

    /**
     * Print a string to a new file
     *
     * @param filePath The path of the file
     * @param text The text to be printed
     * @throws IOException Input output exception
     */
    private void printToFile(String filePath, String text) throws IOException {
        Path path = Paths.get(filePath);
        List<String> lines = new ArrayList<>();
        lines.add(text);
        Files.write(path, lines, Charset.defaultCharset());
    }

    /**
     * =============================== Main ============================= *
     */
    public static void main(String[] args) throws Exception {
        ExperimentController ec = new ExperimentController();
        ec.runExperiment("results.csv");
    }
}
