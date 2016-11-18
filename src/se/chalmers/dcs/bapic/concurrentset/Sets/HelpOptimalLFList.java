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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import se.chalmers.dcs.bapic.concurrentset.utils.*;

/**
 *
 * @author bapic
 */
public class HelpOptimalLFList implements SetADT {

    final Node head, headNext, tail, tailNext;

    /**
     *
     */
    public HelpOptimalLFList() {
        this.tailNext = new Node(K.MaxValue0);
        this.tail = new Node(K.MaxValue1, tailNext);
        this.headNext = new Node(K.MinValue1, tail);
        this.head = new Node(K.MinValue0, headNext);
    }

    private Node getRef(Node n) {
        return n.back == null ? n : n.next;
    }

    private Node getNext(Node n) {
        return getRef(n.next);
    }

    /**
     *
     * @param key
     * @return
     */
    @Override
    public boolean contains(K key) {
        Node cur = getNext(headNext);
        while (cur.key.compareTo(key)) {
            cur = cur.next;
        }
        return key.equals(cur.key) && cur.next.back == null;
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean add(K key) {
        Node pre = head, suc = headNext, cur = headNext, nex = cur.next;
        while (true) {
            while (cur.key.compareTo(key)) {
                if (nex.back == null) {
                    pre = cur;
                    cur = suc = nex;
                }
                else {
                    cur = nex.next;
                }
                nex = cur.next;
            }

            if (nex.back != null) {
                while (nex.back != null) {
                    cur = nex.next;
                    nex = cur.next;
                }
            }
            else if (cur.key.equals(key)) {
                return false;
            }
            if (pre.casNext(suc, new Node(key, cur))) {
                return true;
            }
            suc = pre.next;
            while (suc.back != null) {
                pre = suc.back;
                suc = pre.next;
            }
            cur = pre;
            nex = suc;
        }
    }

    /**
     *
     * @param key
     * @return
     */
    @Override
    public boolean remove(K key) {
        Node pre = head, suc = headNext, cur = headNext, nex = cur.next, marker = null;
        boolean mode = true;
        while (true) {
            while (cur.key.compareTo(key)) {
                if (nex.back == null) {
                    pre = cur;
                    cur = suc = nex;
                }
                else {
                    cur = nex.next;
                }
                nex = cur.next;
            }
            if (mode) {
                if ( ! key.equals(cur.key) || nex.back != null) {
                    return false;
                }
                marker = new Node(pre, K.MinValue0);
                while (true) {
                    marker.next = nex;
                    if (cur.casNext(nex, marker)) {
                        if (pre.casNext(suc, nex)) {
                            return true;
                        }
                        mode = false;
                        break;
                    }
                    nex = cur.next;
                    if (nex.back != null) {
                        return false;
                    }
                }
            }
            else if (nex != marker || pre.casNext(suc, nex.next)) {
                return true;
            }

            suc = pre.next;
            while (suc.back != null) {
                pre = suc.back;
                suc = pre.next;
            }
            cur = pre;
            nex = suc;
        }
    }

    /**
     *
     * @return
     */
    public boolean traversalTest() {
        Node cur = head;
        Node nex = getRef(cur.next);
        while (cur != tail) {
            cur = nex;
            nex = getRef(cur.next);
            if ( ! cur.key.compareTo(nex.key)) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     */
    protected static class Node {

        final K key;
        volatile Node next, back;

        /**
         *
         * @param key
         * @param next
         */
        public Node(K key, Node next) {
            this.key = key;
            this.next = next;
        }

        /**
         *
         * @param pre
         * @param key
         */
        public Node(Node pre, K key) {
            this.key = key;
            this.back = pre;
        }

        /**
         *
         * @param key
         */
        public Node(K key) {
            this.key = key;
        }
        private static final AtomicReferenceFieldUpdater<Node, Node> nextUpdater
                                                                     = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");

        private boolean casNext(Node o, Node n) {
            return this.next == o && nextUpdater.compareAndSet(this, o, n);
        }
    }
}
