/* 
 * Copyright (c) 2015-2016, Bapi Chatterjee
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice, this 
 * list of conditions and the following disclaimer in the documentation and/or other 
 * materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF 
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH 
 * DAMAGE.
 */
package se.chalmers.dcs.bapic.concurrentset.test;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import se.chalmers.dcs.bapic.concurrentset.utils.*;
import se.chalmers.dcs.bapic.concurrentset.Sets.*;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author bapic
 */
public class BenchMark {

    private static int numberOfThreads = 2;
    private static int maxRunningTime = 2;
    private static int addPercent = 50;
    private static int searchPercent = 0;
    private static int removePercent = 50;
    private static int keyRange = 100;
    private static int seed = 0;
    private static boolean testSanity = false;
    private static String setType = "HelpOptimalSimpleLFBST";
    private static int warmuptime = 2;
    private static double begin;
    private static double end;
    private static double throughput;
    private static double fairness;
    private static int[] results;
    private static int[][] sanityAdds;
    private static int[][] sanityRemoves;
    private static int[] presentKeys;
    private static SetADT set;

    private static void defineSet() {
        switch (setType) {
            case "LazyList":
                set = new LazyList();
                break;
            case "HarrisLinkedList":
                set = new HarrisLinkedList();
                break;
            case "HelpOptimalLFList":
                set = new HelpOptimalLFList();
                break;
            case "HelpOptimalSimpleLFBST":
                set = new HelpOptimalSimpleLFBST();
                break;
            case "HelpOptimalLFBST":
                set = new HelpOptimalLFBST();
                break;
            case "HelpOptimalLocalRestartLFBST":
                set = new HelpOptimalLocalRestartLFBST();
                break;
            case "NMLFBST":
                set = new NMLFBST();
                break;
            case "EFRBLFBST":
                set = new EFRBLFBST();
                break;
            case "LFSkipList":
                set = new ConcSkipListWrapper();
                break;
            default:
                set = null;
                break;
        }
    }

    private static void initializeSet() {
        Random rd = new Random(0);

        for (int i = 0; i < keyRange / 2;) {
            int key = rd.nextInt(keyRange);
            boolean added = set.add(new K(key));
            if (added) {
                i ++;
            }
            if (added && testSanity) {
                presentKeys[key] ++;
            }
        }
    }

    private static void InitializeTest(String[] args) {
        LongOpt[] longopts = new LongOpt[11];

        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("duration", LongOpt.REQUIRED_ARGUMENT, null, 'd');
        longopts[2] = new LongOpt("num-threads", LongOpt.REQUIRED_ARGUMENT, null, 'n');
        longopts[3] = new LongOpt("seed", LongOpt.REQUIRED_ARGUMENT, null, 's');
        longopts[4] = new LongOpt("search-fraction", LongOpt.REQUIRED_ARGUMENT, null, 'r');
        longopts[5] = new LongOpt("insert-update-fraction", LongOpt.REQUIRED_ARGUMENT, null, 'i');
        longopts[6] = new LongOpt("delete-fraction", LongOpt.REQUIRED_ARGUMENT, null, 'x');
        longopts[7] = new LongOpt("keyspace1-size", LongOpt.REQUIRED_ARGUMENT, null, 'k');
        longopts[8] = new LongOpt("algo", LongOpt.REQUIRED_ARGUMENT, null, 'a');
        longopts[9] = new LongOpt("sanity", LongOpt.REQUIRED_ARGUMENT, null, 't');
        longopts[10] = new LongOpt("warm", LongOpt.REQUIRED_ARGUMENT, null, 'w');

        Getopt g = new Getopt("", args, "hd:n:s:r:i:x:k:a:t:w:", longopts);
        int c;
        String arg = null;

        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                    helpUser();

                    break;

                case 'a':
                    setType = g.getOptarg();

                    break;

                case 'd':
                    arg = g.getOptarg();
                    maxRunningTime = Integer.parseInt(arg);

                    break;

                case 'n':
                    arg = g.getOptarg();
                    numberOfThreads = Integer.parseInt(arg);

                    break;

                case 's':
                    arg = g.getOptarg();
                    seed = Integer.parseInt(arg);

                    break;

                case 'r':
                    arg = g.getOptarg();
                    searchPercent = Integer.parseInt(arg);

                    break;

                case 'i':
                    arg = g.getOptarg();
                    addPercent = Integer.parseInt(arg);

                    break;

                case 't':
                    arg = g.getOptarg();
                    testSanity = Boolean.parseBoolean(arg);

                    break;

                case 'x':
                    arg = g.getOptarg();
                    removePercent = Integer.parseInt(arg);

                    break;

                case 'k':
                    arg = g.getOptarg();
                    keyRange = Integer.parseInt(arg);

                    break;

                case 'w':
                    arg = g.getOptarg();
                    warmuptime = Integer.parseInt(arg);

                    break;

                case '?':
                    System.err.println("Use -h or --help for help\n");
                    helpUser();
                    System.exit(0);
                default:
                    return;
            }
        }

        if ((addPercent + removePercent + searchPercent) > 100) {
            System.err.println("(addPercent+removePercent+searchPercent) > 100");
            System.exit(1);
        }

        results = new int[numberOfThreads];

        if (testSanity) {
            presentKeys = new int[keyRange];
            sanityAdds = new int[numberOfThreads][keyRange];
            sanityRemoves = new int[numberOfThreads][keyRange];
        }

        defineSet();
    }

    private static void warmupVM() {
        RunController.startFlag = RunController.stopFlag = false;
        try {
            Thread[] threads = new Thread[numberOfThreads];

            for (int i = 0; i < threads.length; i ++) {
                threads[i] = new Thread(new RunOperations(set, i, addPercent, removePercent, keyRange, results, sanityAdds, sanityRemoves, false));
            }

            for (Thread thread : threads) {
                thread.start();
            }

            RunController.startFlag = true;
            Thread.sleep(warmuptime * 1000);
            RunController.stopFlag = true;

            for (Thread thread : threads) {
                thread.join();
            }

            System.gc();
        }
        catch (IOException | InterruptedException ex) {
        }
        finally {
            System.gc();
        }

        RunController.startFlag = RunController.stopFlag = false;
    }

    private static void SanityTest() {
        RunController.startFlag = RunController.stopFlag = false;
        try {
            Thread[] threads = new Thread[numberOfThreads];

            for (int i = 0; i < threads.length; i ++) {
                threads[i] = new Thread(new RunOperations(set, i, addPercent, removePercent, keyRange, results, sanityAdds, sanityRemoves, true));
            }

            for (Thread thread : threads) {
                thread.start();
            }

            RunController.startFlag = true;
            Thread.sleep(maxRunningTime * 1000);
            RunController.stopFlag = true;

            for (Thread thread : threads) {
                thread.join();
            }

            System.gc();

            RunController.startFlag = RunController.stopFlag = false;
        }
        catch (IOException | InterruptedException e) {
        }
        boolean failedSanity = false;
        for (int k = 0; k < keyRange; k ++) {
            int keyAdded = presentKeys[k];
            int keyRemoved = 0;

            for (int tid = 0; tid < numberOfThreads; tid ++) {
                keyAdded += sanityAdds[tid][k];
                keyRemoved += sanityRemoves[tid][k];
            }

            if (set.contains(new K(k))) {
                if (keyAdded != keyRemoved + 1) {
                    System.out.printf("\u001B[32m" + "First Sanity Test passed at key %d, keyAdded = %d, keyRemoved = %d.\n", k,
                            keyAdded, keyRemoved);
                    failedSanity = true;
                }
            }
            else if (keyAdded != keyRemoved) {
                System.out.printf("\u001B[32m" + "Second Sanity Test passed at key %d, keyAdded = %d, keyRemoved = %d.\n", k,
                        keyAdded, keyRemoved);
                failedSanity = true;
            }
        }

        if ( ! failedSanity) {
            System.out.println("Sanity Test Complete");
        }
        System.out.printf("Traversal Test = %b\n", set.traversalTest());
    }

    private static void BenchMark() {
        double totalOps = 0, maxOps = 0, minOps;
        RunController.startFlag = RunController.stopFlag = false;
        try {
            Thread[] threads = new Thread[numberOfThreads];

            for (int i = 0; i < threads.length; i ++) {
                threads[i] = new Thread(new RunOperations(set, i, addPercent, removePercent, keyRange, results, sanityAdds, sanityRemoves, false));
            }

            for (Thread thread : threads) {
                thread.start();
            }

            begin = System.nanoTime();
            RunController.startFlag = true;
            Thread.sleep(maxRunningTime * 1000);
            RunController.stopFlag = true;

            for (Thread thread : threads) {
                thread.join();
            }

            end = System.nanoTime();
            System.gc();

            RunController.startFlag = RunController.stopFlag = false;
        }
        catch (IOException | InterruptedException e) {
        }

        double exactTime = (end - begin) * Math.pow(10, -9);

        for (int i = 0; i < numberOfThreads; i ++) {
            totalOps += results[i];
            maxOps = (maxOps < results[i])
                     ? results[i]
                     : maxOps;
        }

        minOps = maxOps;

        for (int i = 0; i < numberOfThreads; i ++) {
            minOps = (minOps > results[i])
                     ? results[i]
                     : minOps;
        }

        throughput = (totalOps) / exactTime;
        fairness = Math.min((numberOfThreads * minOps) / totalOps, totalOps / (numberOfThreads * maxOps));
    }

    private static void helpUser() {
        String help = "Concurrent Set Implementation\n" + "\n" + "Usage:\n" + "  BenchMark [options...]\n" + "\n" + "Options:\n"
                      + "  -h, --help\n" + "        Print this message\n" + "  -a, --algo  <Algorithm> (default=" + setType + ")\n"
                      + "        Available Algorithms <LazyList HarrisLinkedList HelpOptimalLFList HelpOptimalSimpleLFBST HelpOptimalLFBST HelpOptimalLocalRestartLFBST NMLFBST EFRBLFBST LFSkipList>\n" + "  -t, --test-sanity <Boolean>\n"
                      + "        Sanity check (default=" + testSanity + ")\n" + "  -d, --duration <int>\n"
                      + "        Test duration in seconds (0=infinite, default=" + maxRunningTime + "s)\n"
                      + "  -n, --num-threads <int>\n" + "        Number of threads (default=" + numberOfThreads + ")\n"
                      + "  -s, --seed <int>\n" + "        RNG seed (0=time-based, default=" + seed + ")\n"
                      + "  -r, --search-fraction <int>\n" + "        Fraction of search operations (default="
                      + searchPercent + "%)\n" + "  -i, --insert-update-fraction <int>\n"
                      + "        Fraction of insert/add operations (default=" + addPercent + "%)\n"
                      + "  -x, --delete-fraction <int>\n" + "        Fraction of delete operations (default="
                      + removePercent + "%)\n" + "  -w, --warm <int>\n"
                      + "        JVM warm up time in seconds(default=" + warmuptime + "s)\n"
                      + "  -k, --keyspace-size <int>\n" + "       Number of possible keys (default=" + keyRange + ")\n";

        System.out.println(help);
        System.exit(0);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("To see the available options of parameters, type flag -h.");
        }
        InitializeTest(args);

        System.err.printf(
                "The experiment: Algo:%s, Distribution: search %d insert %d delete %d, Duration(s):%d, Threads:%d, KeyRange(starting at 0):%d\n",
                setType, searchPercent, addPercent, removePercent, maxRunningTime, numberOfThreads, keyRange);
        initializeSet();

        if (testSanity) {
            SanityTest();
        }
        else {
            System.err.println("Starting warm up");

            long memTree = Tools.getMemUsed();

            Tools.cleanMem(memTree);
            memTree = Tools.getMemUsed();
            warmupVM();
            Tools.cleanMem(memTree);
            System.err.println("End of warm up phase");
            set = null;
            defineSet();

            Tools.cleanMem(memTree);
            memTree = Tools.getMemUsed();
            initializeSet();

            Tools.cleanMem(memTree);
            memTree = Tools.getMemUsed();
            BenchMark();

            long memOnFinish = Tools.cleanMem(memTree);
            System.out.printf("Throughput = %.0f Ops/sec\n", throughput);
            System.out.printf("Memory-footprint of operations = %d bytes\n", memOnFinish);
            System.out.printf("Fairness = %.0f percent\n", fairness * 100);
        }
    }
}
