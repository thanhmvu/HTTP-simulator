/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package applayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author thanhvu
 */
public class ExperimentController {

    private ClientApp client;

    public ExperimentController() {
        client = new ClientApp(1.0, 1000, 10);
    }

    /**
     * Experiment method that runs the experiment for transport delay
     *
     * @param initDelay
     * @param runs
     * @param increment
     * @param trialPerRun
     * @return a list of the runtime
     */
    public ArrayList<Long> runTransExp(int initDelay, int runs, int increment, int trialPerRun) {
        ArrayList<Long> runtimes = new ArrayList<>();
        long transDelayPerByte;

        for (int i = 0; i < runs; i++) {
            transDelayPerByte = initDelay + i * increment;
            long sum = 0;
            for (int j = 0; j < trialPerRun; j++) {
                client.reset();
                client.setTransDelayPerByte(transDelayPerByte);
                sum += client.downloadWebpage();
            }
            // take the average
            long t = sum / trialPerRun;
            runtimes.add(t);
        }

        return runtimes;
    }

    /**
     * Experiment method that runs the experiment for propagation delay
     */
    public void runPropExp() {
        int propDelay = 1000;
        int n = 10;
        int increment = 500;
        long sum = 0;
        for (int i = 0; i < n; i++) {
            propDelay += increment;
            client.reset();
            client.setPropDelay(propDelay);
            sum += client.downloadWebpage();
            client.downloadWebpage();
        }
    }

    /**
     * Experiment method that runs the experiment for number of requested files
     */
    public void runFileNumExp() {

    }

//    public void runExperiment(String outputFile) throws FileNotFoundException {
//        PrintWriter pw = new PrintWriter(new File(outputFile));
//        StringBuilder sb = new StringBuilder();
//        sb.append("\n");
//
//        // ================ Transmission Delay ================ //
//        int initDelay = 10;
//        int runs = 5;
//        int increment = 5;
//        int trialPerRun = 3;
//
//        // Non-Persistent
//        Config.reset();
//        Config.HTTP_VERSION = 1.0;
//        ArrayList<Long> persistTimes = runTransExp(initDelay, runs, increment, trialPerRun);
//
//        // Persistent
//        Config.reset();
//        Config.HTTP_VERSION = 1.1;
//        ArrayList<Long> nonpersTimes = runTransExp(initDelay, runs, increment, trialPerRun);
//
//        // Multiplex
//        Config.reset();
//        Config.HTTP_VERSION = 1.2;
//        ArrayList<Long> multiTimes = runTransExp(initDelay, runs, increment, trialPerRun);
//
//        pw.write(sb.toString());
//        pw.close();
//        System.out.println("done!");
//    }

    /**
     * =============================== Main ============================= *
     */
    public static void main(String[] args) throws Exception {
        ExperimentController ec = new ExperimentController();
//        ec.runExperiment("results.csv");
    }
}
