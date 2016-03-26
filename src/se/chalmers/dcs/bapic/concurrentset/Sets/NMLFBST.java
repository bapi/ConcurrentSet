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
import se.chalmers.dcs.bapic.concurrentset.utils.*;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 *
 * @author bapic
 */
public class NMLFBST implements SetADT {

    private static final AtomicReferenceFieldUpdater<Node, Node> leftUpdater
                                                                 = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
    private static final AtomicReferenceFieldUpdater<Node, Node> rightUpdater
                                                                 = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
    final Node pRoot, cRoot;

    /**
     *
     */
    public NMLFBST() {
        cRoot = new Node(K.MaxValue1, new Node(K.MaxValue2), new Node(K.MaxValue1));
        pRoot = new Node(K.MaxValue0, cRoot, new Node(K.MaxValue0));
    }

    private Node getMarked(Node current) {
        Node markedLink = new Node(current.key);
        markedLink.right = markedLink;
        return markedLink;
    }

    boolean isRoute(Node n) {
        return getRef(n).left != null;
    }

    boolean isRouteNode(Node n) {

        /**
         * It assumes that it has already been checked that the node passes
         * isRoute(n).
         */
        return n.left != n;
    }

    boolean isFNode(Node n) {
        return n.left == n;
    }

    boolean isMNode(Node n) {
        return n.right == n;
    }

    boolean isDNode(Node n) {
        return (n.getClass() == Node.class) && (n.left == null);
    }

    Node getRef(Node n) {
        if (isFNode(n)) {
            return n.right;
        }

        return n;
    }

    Node getChild(final Node n, boolean childDir) {
        return childDir
               ? getRef(n.left)
               : getRef(n.right);
    }

    Node getChildLink(final Node n, boolean childDir) {
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
     * Constructor for a flag node. Here we save the existing child in the left
     * child of the flagged node and on cleaning that comes to become the child
     * of the grandparent. The flagged node is given key as null as that would
     * ensure the traversal to go leftwards.
     *
     * @param n
     */
    Node flag(Node n) {
        Node retNode = new Node(n.key);

        retNode.left = retNode;
        retNode.right = n;

        return retNode;
    }

    /**
     * flags a child given by child direction
     *
     * @param childDir
     * @return the old value of the child
     */
    Node appendFlag(Node n, boolean childDir) {
        while (true) {
            Node child = getChildLink(n, childDir);

            if (isFNode(child)) {
                return getRef(child);
            }
            else if (casChild(n, child, flag(child), childDir)) {
                return child;
            }
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public final boolean contains(final K key) {
        Node current = cRoot;

        while (isRoute(current)) {
            current = getChild(current, key.compareTo(current.key));
        }

        return (key.equals(current.key));
    }

    /**
     *
     * @param key
     * @return
     */
    public final boolean add(K key) {
        Node newInternal, parent, leafLink, ancestor, child, leaf, successor;

        while (true) {
            ancestor = pRoot;
            successor = parent = cRoot;
            leafLink = parent.left;

            while (isRoute(leafLink)) {

                /**
                 * The While loop implements the seek() method of the paper
                 */
                if (leafLink.left != leafLink) {
                    ancestor = parent;
                    successor = leafLink;
                }

                parent = getRef(leafLink);
                leafLink = getChildLink(parent, key.compareTo(parent.key));
            }
            leaf = getRef(leafLink);
            if (key.equals(leaf.key)) {
                return false;
            }
            boolean childDir = key.compareTo(parent.key);
            child = getChildLink(parent, childDir);
            newInternal = key.compareTo(leaf.key)
                          ? new Node(leaf.key, new Node(key), leaf)
                          : new Node(key, leaf, new Node(key));

            if (leaf.right == null && child == leaf) {
                if (casChild(parent, leafLink, newInternal, childDir)) {
                    return true;
                }
            }
            else if (getRef(child) == leaf && (child.left == child || leaf.right == leaf)) {
                cleanUp(ancestor, successor, parent, key);
            }
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public final boolean remove(K key) {
        int mode = 1;    // 1:- INJECTION, 2:- CLEANUP
        Node parent, leafLink, ancestor, successor,
                marker = null, child, leaf;

        while (true) {
            ancestor = pRoot;
            successor = parent = cRoot;
            leafLink = parent.left;

            while (isRoute(leafLink)) {

                /**
                 * The While loop implements the seek() method of the paper
                 */
                if (leafLink.left != leafLink) {
                    ancestor = parent;
                    successor = leafLink;
                }

                parent = getRef(leafLink);
                leafLink = getChildLink(parent, key.compareTo(parent.key));
            }

            if (mode == 1) {
                leaf = getRef(leafLink);
                if ( ! key.equals(leaf.key)) {
                    return false;
                }

                boolean childDir = key.compareTo(parent.key);
                child = getChildLink(parent, childDir);
                marker = getMarked(leaf);
                if (leaf.right == null && child == leaf) {

                    if (casChild(parent, leaf, marker, childDir)) {
                        mode = 2;

                        if (cleanUp(ancestor, successor, parent, key)) {
                            return true;
                        }
                    }
                }
                else if (getRef(child) == leaf && (child.left == child || leaf.right == leaf)) {
                    cleanUp(ancestor, successor, parent, key);
                }
            }
            else if (mode == 2) {
                if ((getRef(leafLink).equals(marker))) {
                    if (cleanUp(ancestor, successor, parent, key)) {
                        return true;
                    }
                }
                else {
                    return true;
                }
            }
        }
    }

    /**
     *
     * @param ancestor
     * @param successor
     * @param parent
     * @param key
     * @return
     */
    protected final boolean cleanUp(Node ancestor, Node successor, Node parent, K key) {
        Node sibling = isMNode(parent.right)
                       ? appendFlag(parent, true)
                       : appendFlag(parent, false);
        boolean parentDir = key.compareTo(ancestor.key);
        Node parentLink = parentDir ? ancestor.left : ancestor.right;
        if (parentLink == successor) {
            return casChild(ancestor, parentLink, sibling, parentDir);
        }
        else {
            return false;
        }
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
        inorder(pRoot, keys);
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
