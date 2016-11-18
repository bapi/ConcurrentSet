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
public class K {

    /**
     *
     */
    public static final K MinValue0 = new K( - Double.MAX_VALUE);

    /**
     *
     */
    public static final K MinValue1 = new K( - 0.95 * Double.MAX_VALUE);

    /**
     *
     */
    public static final K MaxValue0 = new K(Double.MAX_VALUE);

    /**
     *
     */
    public static final K MaxValue1 = new K(0.95 * Double.MAX_VALUE);

    /**
     *
     */
    public static final K MaxValue2 = new K(0.9 * Double.MAX_VALUE);
    final double value;

    /**
     *
     */
    public K() {
        this.value = 0;
    }

    /**
     *
     * @param value
     */
    public K(double value) {
        this.value = value;
    }

    /**
     *
     * @param o
     * @return
     */
    public boolean compareTo(K o) {
        return this.value < o.value;
    }

    /**
     *
     * @param o
     * @return
     */
    public boolean equals(K o) {
        return this.value == o.value;
    }

    /**
     *
     * @return
     */
    public double getValue() {
        return this.value;
    }

    private K simpleNew(K b) {
        return (value < b.value)
               ? b
               : this;
    }

    private K randPoint(K b) {
        return (value < b.value)
               ? new K(ThreadLocalRandom.current().nextDouble(value, b.value))
               : new K(ThreadLocalRandom.current().nextDouble(b.value, value));
    }

    /**
     *
     * @param b
     * @return
     */
    public K newPoint(K b) {
        return new K(value * 0.5 + b.value * 0.5);
//        return randPoint(b);
//        return simpleNew(b);
    }
}
