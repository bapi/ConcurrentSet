/**
 * This code file was downloaded from Yujie Liu's github repository at
 * https://github.com/mfs409/nonblocking and modified to include the methods
 * traversalTest() to check the sanity of the code. We also changed the lock
 * to Java ReentrantLock
 */
package se.chalmers.dcs.bapic.concurrentset.Sets;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import se.chalmers.dcs.bapic.concurrentset.utils.*;

/**
 *
 * @author 
 */
public class HarrisLinkedList implements SetADT {

    /**
     * Internal Node class.
     */
    private static class Node {

        protected K key;
        protected volatile Node next;

        private static final AtomicReferenceFieldUpdater<Node, Node> nextUpdater
                                                                     = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");

        private Node(K k) {
            this.key = k;
        }

        private boolean casNext(Node o, Node n) {
            return nextUpdater.compareAndSet(this, o, n);
        }
    }

    private static class Marker extends Node {

        private Marker(Node n) {
            super(K.MinValue0);
            this.next = n;
        }
    }

    /**
     * Sentinel nodes.
     */
    private Node head;
    private Node tail;

    /**
     * Constructor.
     */
    public HarrisLinkedList() {
        head = new Node(K.MinValue0);
        tail = new Node(K.MaxValue0);
        head.next = tail;
    }

    /**
     * Insert specified key into the linked list set.
     *
     * @param key
     * @return
     */
    public boolean add(K key) {
        Node pred = null, curr = null, succ = null;
        retry:
        // purpose of outermost while loop is for implementing goto only..
        while (true) {
            // initialization
            pred = head;
            curr = pred.next;
            // traverse linked list
            while (true) {
                succ = curr.next;
                while (succ instanceof Marker) {
                    succ = succ.next;
                    // snip curr and marker
                    if ( ! pred.casNext(curr, succ)) {
                        continue retry;
                    }
                    curr = succ;
                    succ = succ.next;
                }
                // continue searching
                if (curr.key.compareTo(key)) {
                    pred = curr;
                    curr = succ;
                } // key exists
                else if (curr.key.equals(key)) {
                    return false;
                } // locate a window: do insert
                else {
                    Node node = new Node(key);
                    node.next = curr;
                    if (pred.casNext(curr, node)) {
                        return true;
                    }
                    else {
                        continue retry;
                    }
                }
            }
        }
    }

    /**
     * Remove specified key from the linked list set.
     *
     * @param key
     * @return
     */
    public boolean remove(K key) {
        Node pred = null, curr = null, succ = null;
//        boolean[] marked = {false};
        retry:
        // purpose of outermost while loop is for implementing goto only..
        while (true) {
            // initialization
            pred = head;
            curr = pred.next;
            // traverse linked list
            while (true) {
                succ = curr.next;
                while (succ instanceof Marker) {
                    succ = succ.next;
                    if ( ! pred.casNext(curr, succ)) {
                        continue retry;
                    }
                    curr = succ;
                    succ = succ.next;
                }
                // continue searching
                if (curr.key.compareTo(key)) {
                    pred = curr;
                    curr = succ;
                } // key found: do remove
                else if (curr.key.equals(key)) {
                    if ( ! curr.casNext(succ, new Marker(succ))) {
                        continue retry;
                    }
                    pred.casNext(curr, succ);
                    return true;
                } // key not found
                else if (key.compareTo(curr.key)) {
                    return false;
                }
            }
        }
    }

    private Node getRef(Node n) {
        return (n instanceof Marker) ? n : n.next;
    }

    /**
     *
     * @return
     */
    public boolean traversalTest() {
        Node cur = head;
        Node nex = getRef(cur.next);
        while (nex != tail) {
            cur = nex;
            nex = getRef(cur.next);
            if ( ! cur.key.compareTo(nex.key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Search for specified key in the linked list set.
     *
     * @param key
     * @return
     */
    @Override
    public boolean contains(K key) {
        Node curr = head;
        while (curr.key.compareTo(key)) {
            curr = curr.next;
        }
        return (key.equals(curr.key) && ( ! (curr.next instanceof Marker)));
    }

    /**
     *
     */
    public void dump() {
        Node curr = head.next;
        while (curr != null && ( ! (curr instanceof Marker)) && curr.next != null) {
            System.out.print(curr.key);
            System.out.print("->");
            curr = curr.next;
        }
        System.out.println();
    }
}
