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
import se.chalmers.dcs.bapic.concurrentset.utils.K;
import se.chalmers.dcs.bapic.concurrentset.utils.SetADT;

/**
 *
 * @author bapic
 */
public class HelpOptimalLocalRestartLFBST implements SetADT {

    private static final AtomicReferenceFieldUpdater<Node, Node> leftUpdater
                                                                 = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
    private static final AtomicReferenceFieldUpdater<Node, Node> rightUpdater
                                                                 = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
    final Node pRoot, cRoot;

    /**
     *
     */
    public HelpOptimalLocalRestartLFBST() {
        cRoot = new Node(K.MaxValue1, new Node(K.MaxValue2), new Node(K.MaxValue1));
        pRoot = new Node(K.MaxValue0, cRoot, new Node(K.MaxValue0));
    }

    boolean isSplice(Node n) {
        return n.key.equals(K.MinValue);
    }

    boolean isDead(Node n) {
        return n.right == n;
    }

    Node getRef(Node n) {
        return isSplice(n) ? n.right : n;
    }

    K getKey(Node n) {
        return isSplice(n) ? n.right.key : n.key;
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

    private Node getNull(K key) {
        Node markedLink = new Node(key);
        markedLink.right = markedLink;
        return markedLink;
    }

    private Node getMarked(K key, Node ancestor) {
        Node markedLink = new Node(key);
        markedLink.right = markedLink;
        markedLink.back = ancestor;
        return markedLink;
    }

    Node getFlagged(Node child, Node ancestor) {
        return child.right == child ? getMarked(child.key, ancestor)
               : new Node(K.MinValue, child.left, child, ancestor);
    }

    Node appendFlag(Node parent, Node ancestor, boolean childDir) {
        while (true) {
            Node child = getChild(parent, childDir);
            if (child.back != null || casChild(parent, child, getFlagged(child, ancestor), childDir)) {
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

        while (current.left != null) {
            current = getChild(current, key.compareTo(current.key));
        }

        return (key.equals(getKey(current))) && current.right != current;
    }

    /**
     *
     * @param key
     * @return
     */
    public final boolean add(K key) {
        Node newInternal, parent, child, ancestor, successor;

        ancestor = pRoot;
        successor = parent = cRoot;
        child = parent.left;

        while (true) {
            while (child.left != null) {
                if (child.key.equals(K.MinValue)) {
                    parent = child.right;
                }
                else {
                    ancestor = parent;
                    parent = successor = child;
                }
                child = getChild(parent, key.compareTo(parent.key));
            }
            boolean childDir = key.compareTo(parent.key);

            if (child.right != child) {
                K childKey = getKey(child);
                if (key.equals(childKey)) {
                    return false;
                }
                newInternal = key.compareTo(childKey)
                              ? new Node(childKey.newPoint(key), new Node(key), getRef(child))
                              : new Node(childKey.newPoint(key), getRef(child), new Node(key));
                if ( ! isSplice(child)) {
                    if (casChild(parent, child, newInternal, childDir)) {
                        return true;
                    }
                }
                else if (casChild(ancestor, successor, newInternal, key.compareTo(ancestor.key))) {
                    return true;
                }
            }
            else {
                if (child.back == null) {
                    if (casChild(parent, child, new Node(key), childDir)) {
                        return true;
                    }
                }
                else {
                    Node sibling = getRef(appendFlag(parent, ancestor,  ! childDir));
                    newInternal = sibling.right == sibling ? new Node(key)
                                  : (key.compareTo(sibling.key)
                                     ? new Node(parent.key.newPoint(key), new Node(key), sibling)
                                     : new Node(parent.key.newPoint(key), sibling, new Node(key)));
                    if (casChild(ancestor, successor, newInternal, key.compareTo(ancestor.key))) {
                        return true;
                    }
                }
            }

            successor = getChild(ancestor, key.compareTo(ancestor.key));
            while (successor.back != null) {
                ancestor = successor.back;
                successor = getChild(ancestor, key.compareTo(ancestor.key));
            }
            parent = ancestor;
            child = successor;
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public final boolean remove(K key) {
        boolean mode = true;
        Node parent, child, ancestor, successor, marker = null, sibling = null;

        ancestor = pRoot;
        successor = parent = cRoot;
        child = parent.left;
        while (true) {
            while (child.left != null) {
                if (child.key.equals(K.MinValue)) {
                    parent = child.right;
                }
                else {
                    ancestor = parent;
                    parent = successor = child;
                }
                child = getChild(parent, key.compareTo(parent.key));
            }
            if (mode) {
                if ( ! key.equals(getKey(child)) || child.right == child) {
                    return false;
                }
                boolean childDir = key.compareTo(parent.key);
                if (child.right == null && parent != ancestor) {
                    marker = getMarked(key, ancestor);
                    if (casChild(parent, child, marker, childDir)) {
                        mode = false;
                        sibling = appendFlag(parent, ancestor,  ! childDir);
                        if (sibling.right == sibling) {
                            casChild(ancestor, successor, getNull(key), key.compareTo(ancestor.key));
                            return true;
                        }
                        else if (isSplice(sibling) || casChild(ancestor, successor, sibling, key.compareTo(ancestor.key))) {
                            return true;
                        }
                    }
                }
                else if (casChild(ancestor, successor, getNull(key), key.compareTo(ancestor.key))) {
                    return true;
                }
            }
            else {
                if (child.equals(marker) && parent != ancestor) {
                    if (casChild(ancestor, successor, sibling, key.compareTo(ancestor.key))) {
                        return true;
                    }
                }
                else {
                    return true;
                }
            }
            successor = getChild(ancestor, key.compareTo(ancestor.key));
            while (successor.back != null) {
                ancestor = successor.back;
                successor = getChild(ancestor, key.compareTo(ancestor.key));
            }
            parent = ancestor;
            child = successor;
        }
    }

    private void inorder(Node node, LinkedList<K> list) {
        if (node.left == null) {
            if (node.right == null) {
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
        inorder(cRoot, keys);
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
        volatile Node left, right, back;

        Node(final K key) {
            this(key, null, null);
        }

        Node(final K key, final Node l, final Node r) {
            this.key = key;
            this.left = l;
            this.right = r;
        }

        Node(K key, Node left, Node right, Node back) {
            this.key = key;
            this.left = left;
            this.right = right;
            this.back = back;
        }
    }
}
