/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package experiment;

import applayer.ClientApp;
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

    /**
     * This class holds all the experiment results
     */
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

//================CORRECTNESS CHECKING=======================================
    /**
     * Check correctness of experiment
     *
     * @param outputFile The name of the output file
     */
    public void checkCorrectness(String outputFile) {

        long propDelay = 200;
        long transDelayPerByte = 2;
        int numTrials = 3;
        ExperimentResults nonPersCache = this.checkCorrectnessForVer(1.0, propDelay, transDelayPerByte, true, numTrials);
        ExperimentResults pers = this.checkCorrectnessForVer(1.1, propDelay, transDelayPerByte, false, numTrials);
        ExperimentResults mul = this.checkCorrectnessForVer(1.2, propDelay, transDelayPerByte, false, numTrials);

        String finalResult = nonPersCache.toCsvString()
                + pers.toCsvString() 
                + mul.toCsvString();
        try {
            this.printToFile(outputFile, finalResult);
        } catch (IOException ex) {
            Logger.getLogger(ExperimentController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Experiment to check for correctness
     *
     * @param httpVersion Version of HTTP
     * @param propDelay Prop delay
     * @param transDelayPerByte Trans delay per byte
     * @param hasCache Whether the experiment should have cache
     * @return An experiment result
     */
    private ExperimentResults checkCorrectnessForVer(double httpVersion, long propDelay, long transDelayPerByte, boolean hasCache, int numTrials) {

        ExperimentResults results = new ExperimentResults("Correctness for HTTP " + httpVersion + (hasCache ? " with caching" : ""));
        long unCachedSum = 0;
        long cachedSum = 0;
        for (int i = 0; i < numTrials; i++) {
            client.reset();
            client.setPropDelay(propDelay);
            client.setTransDelayPerByte(transDelayPerByte);
            client.setHttpVersion(httpVersion);
            unCachedSum += downloadTwoWebpages(2);
            if (hasCache) {
                cachedSum += downloadTwoWebpages(2);
            }
        }
        //add to result table
        results.add("HTTP Version", Double.toString(httpVersion));
        results.add("Propagation Delay", Long.toString(propDelay));
        results.add("Transmission Delay Per Byte", Long.toString(transDelayPerByte));
        results.add("Uncached Response Time", Long.toString(unCachedSum / numTrials));

        if (hasCache) {
            results.add("Cached Response Time", Long.toString(cachedSum / numTrials));
        }
        return results;
    }
//==================EXPERIMENT=====================================

    /**
     * Run experiment
     *
     * @param outputFile The name of the output file
     * @throws FileNotFoundException
     */
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
                + persObjsResults.toCsvString() //+ mulObjsResults.toCsvString()
                ;
        try {
            this.printToFile(outputFile, finalResult);
        } catch (IOException ex) {
            Logger.getLogger(ExperimentController.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("done!");
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
                unCachedSum += downloadTwoWebpages(0);
                if (includeCache) {
                    cachedSum += downloadTwoWebpages(0);
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
                sum += this.downloadTwoWebpages(0);
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
            for (int j = 0; j < numTrialsPerRun; j++) {
                client.reset();
                client.setHttpVersion(httpVersion);
                sum += this.downloadTwoWebpages(i);
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

    /**
     * Download webpages that have the same number of children
     *
     * @param numOfChildren The number of children, which affects the file name.
     * For example: 0 will download pA0.txt and pB0.txt
     * @return The total response time
     */
    private long downloadTwoWebpages(int numOfChildren) {
        ArrayList<String> urls = new ArrayList<>();
        urls.add("pA" + numOfChildren + ".txt");
        urls.add("pB" + numOfChildren + ".txt");
        return client.downloadWebPages(urls);
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
        ec.checkCorrectness("correctness.csv");
        // ec.runExperiment("expResults.csv");
    }
}
