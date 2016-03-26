/**
 * This code file was downloaded from Yujie Liu's github repository at
 * https://github.com/mfs409/nonblocking and modified to include the methods
 * traversalTest() to check the sanity of the code. We also changed the lock to
 * Java ReentrantLock from a CAS based simple spin lock.
 */
package se.chalmers.dcs.bapic.concurrentset.Sets;

import se.chalmers.dcs.bapic.concurrentset.utils.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author 
 */
public class LazyList implements SetADT {

    private static class Node {

        private K key;
        private Node next;

        /**
         * If true, Node is logically deleted.
         */
        private boolean marked;
        /**
         * Synchronizes Node.
         */
        Lock lock;

        private Node(K k) {
            this.key = k;
            this.next = null;
            this.marked = false;
            this.lock = new ReentrantLock();
        }

        /**
         * Lock Node
         */
        void lock() {
            lock.lock();
        }

        /**
         * Unlock Node
         */
        void unlock() {
            lock.unlock();
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
    public LazyList() {
        head = new Node(K.MinValue);
        tail = new Node(K.MaxValue0);
        head.next = tail;
    }

    private boolean validate(Node pred, Node curr) {
        return  ! pred.marked &&  ! curr.marked && pred.next == curr;
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean add(K key) {
        while (true) {
            Node pred = head;
            Node curr = head.next;
            while (curr.key.compareTo(key)) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock();
            curr.lock();
            if (validate(pred, curr)) {
                if (curr.key.equals(key)) {
                    curr.unlock();
                    pred.unlock();
                    return false;
                }
                else {
                    Node n = new Node(key);
                    n.next = curr;
                    pred.next = n;
                    curr.unlock();
                    pred.unlock();
                    return true;
                }
            }
            curr.unlock();
            pred.unlock();
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean remove(K key) {
        while (true) {
            Node pred = head;
            Node curr = head.next;
            while (curr.key.compareTo(key)) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock();
            curr.lock();
            if (validate(pred, curr)) {
                if ( ! curr.key.equals(key)) {
                    curr.unlock();
                    pred.unlock();
                    return false;
                }
                else {
                    curr.marked = true;
                    pred.next = curr.next;
                    curr.unlock();
                    pred.unlock();
                    return true;
                }
            }
            curr.unlock();
            pred.unlock();
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public boolean contains(K key) {
        Node curr = head;
        while (curr.key.compareTo(key)) {
            curr = curr.next;
        }
        return curr.key.equals(key) &&  ! curr.marked;
    }

    /**
     *
     * @return
     */
    public boolean traversalTest() {
        Node cur = head;
        Node nex = (cur.next);
        while (nex != tail) {
            cur = nex;
            nex = (cur.next);
            if ( ! cur.key.compareTo(nex.key)) {
                return false;
            }
        }
        return true;
    }
}
