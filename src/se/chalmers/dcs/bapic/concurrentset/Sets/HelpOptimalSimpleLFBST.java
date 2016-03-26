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

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import se.chalmers.dcs.bapic.concurrentset.utils.*;

/**
 *
 * @author bapic
 */
public class HelpOptimalSimpleLFBST implements SetADT {

    private static final AtomicReferenceFieldUpdater<Node, Node> leftUpdater
                                                                 = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
    private static final AtomicReferenceFieldUpdater<Node, Node> rightUpdater
                                                                 = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
    final Node root;

    /**
     *
     */
    public HelpOptimalSimpleLFBST() {
        root = new Node(K.MaxValue0, new Node(K.MaxValue1), new Node(K.MaxValue0));
    }

    Node getChild(final Node n, boolean childDir) {
        return childDir
               ? n.left
               : n.right;
    }

    boolean casChild(Node n, Node cmp, Node newNode, boolean childDir) {
        return childDir
               ? (n.left == cmp ? leftUpdater.compareAndSet(n, cmp, newNode) : false)
               : (n.right == cmp ? rightUpdater.compareAndSet(n, cmp, newNode) : false);
    }

    /**
     *
     * @param key
     * @return
     */
    public final boolean contains(final K key) {
        Node current = root;

        while (current.left != null) {
            current = getChild(current, key.compareTo(current.key));
        }

        return (key.equals(current.key)) && current.right != current;
    }

    /**
     *
     * @param key
     * @return
     */
    public final boolean add(K key) {
        Node newInternal;
        Node parent = root;
        Node current = root.right;
        while (true) {
            while (current.left != null) {
                parent = current;
                current = getChild(parent, key.compareTo(parent.key));
            }
            if (current.right == null) {
                if (key.equals(current.key)) {
                    return false;
                }
                newInternal = key.compareTo(current.key) ? new Node(current.key, new Node(key), current) : new Node(key, current, new Node(key));
                if (casChild(parent, current, newInternal, key.compareTo(parent.key))) {
                    return true;
                }
            }
            else if (casChild(parent, current, new Node(key), key.compareTo(parent.key))) {
                return true;
            }
            current = getChild(parent, key.compareTo(parent.key));
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public final boolean remove(K key) {
        Node current = root;
        while (current.left != null) {
            current = getChild(current, key.compareTo(current.key));
        }
        if (current.right == current ||  ! key.equals(current.key)) {
            return false;
        }
        return casChild(current, null, current, false);
    }

    private void inorder(Node node, LinkedList<K> list) {
        if (node.left == null) {
            if (node.right == null) {//if the node is not removed and is leaf
                list.add(node.key);
            }
            return;
        }
        inorder(node.left, list);
        inorder(node.right, list);
    }

    /**
     *
     * @return
     */
    public boolean traversalTest() {
        LinkedList<K> keys = new LinkedList();
        inorder(root, keys);
        for (int i = 0; i < keys.size() - 1; i ++) {
            if ( ! keys.get(i).compareTo(keys.get(i + 1))) {
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
        volatile Node left, right;

        Node(final K key) {
            this(key, null, null);
        }

        Node(final K key, final Node l, final Node r) {
            this.key = key;
            this.left = l;
            this.right = r;
        }
    }
}
