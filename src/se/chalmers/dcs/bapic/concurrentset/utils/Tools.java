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
package se.chalmers.dcs.bapic.concurrentset.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author bapic
 */
public class Tools {

    /**
     *
     * @param a
     * @param b
     * @return
     */
    public static K midPoint(K a, K b) {
        return (b == null)
               ? null
               : new K(a.getValue() * 0.5 + b.getValue() * 0.5);
    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
    public static K randPoint(K a, K b) {
        return (b == null)
               ? null
               : new K(ThreadLocalRandom.current().nextDouble(a.getValue(), b.getValue()));
    }

    /**
     *
     * @return
     */
    public static long getMemUsed() {
        long tot1 = Runtime.getRuntime().totalMemory();
        long free1 = Runtime.getRuntime().freeMemory();
        long used1 = tot1 - free1;

        return used1;
    }

    /**
     *
     * @param txt
     * @param prev
     * @return
     */
    public static long printMemUsed(String txt, long prev) {
        long current = getMemUsed();

//        System.err.println(txt + ": " + (current - prev));
        return current - prev;
    }

    /**
     *
     * @param prevMemUsed
     * @return
     */
    public static long cleanMem(long prevMemUsed) {
        long ret = 0;

        for (int i = 0; i < 5; i ++) {
            ret = Tools.printMemUsed("MemTree", prevMemUsed);
            System.gc();

            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }
}
