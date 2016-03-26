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
package se.chalmers.dcs.bapic.concurrentset.Sets;

import se.chalmers.dcs.bapic.concurrentset.utils.*;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author bapic
 */
public class ConcSkipListWrapper implements SetADT {

    ConcurrentSkipListSet<Double> sl;

    /**
     *
     */
    public ConcSkipListWrapper() {
        this.sl = new ConcurrentSkipListSet<>();
    }

    /**
     *
     * @param key
     * @return
     */
    @Override
    public boolean contains(K key) {
        return sl.contains(key.getValue());
    }

    /**
     *
     * @param key
     * @return
     */
    @Override
    public boolean add(K key) {
        return sl.add(key.getValue());
    }

    /**
     *
     * @param key
     * @return
     */
    @Override
    public boolean remove(K key) {
        return sl.remove(key.getValue());
    }

    /**
     *
     * @return
     */
    @Override
    public boolean traversalTest() {
        System.out.println("This is a wrapper of ConcurrentSkipListSet of Java and TraversalTest is not implemented by us!");
        return true;
    }

}
