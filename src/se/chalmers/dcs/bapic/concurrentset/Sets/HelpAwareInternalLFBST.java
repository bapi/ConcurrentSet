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
public class HelpAwareInternalLFBST implements SetADT {

    private static final AtomicReferenceFieldUpdater<Node, Node> leftUpdater
            = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
    private static final AtomicReferenceFieldUpdater<Node, Node> rightUpdater
            = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
    private static final AtomicReferenceFieldUpdater<Node, Node> pLinkUpdater
            = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "preLink");
    final Node cRoot;

    public HelpAwareInternalLFBST() {
        cRoot = new Node(K.MaxValue0);
        cRoot.left = new Node(K.MaxValue2, null, cRoot, null);
    }

    boolean casChild(Node n, Node cmp, Node newNode, boolean childDir) {
        return childDir
                ? (n.left == cmp ? leftUpdater.compareAndSet(n, cmp, newNode) : false)
                : (n.right == cmp ? rightUpdater.compareAndSet(n, cmp, newNode) : false);
    }

    Node getChild(final Node n, boolean childDir) {
        return childDir ? n.left : n.right;
    }

    K getKey(Node n) {
        return n.key.equals(K.MinValue0) ? n.right.key : n.key;
    }

    Node getRef(Node n) {
        return n.key.equals(K.MinValue0) ? n.right : n;
    }

    @Override
    public boolean contains(K key) {
        Node curr = cRoot.left;

        while (curr.key.compareTo(K.MaxValue2)) {    // unless reach a thread link, run the while loop
            if (key.equals(curr.key)) {
                return curr.preLink == null;
            } else {
                curr = getChild(curr, key.compareTo(curr.key));
            }
        }
        return false;
    }

    @Override
    public boolean add(K key) {
        Node par, curr, next;
        Node node = null, preNode, replacementNode;
        par = cRoot;
        retry:
        while (true) {
            curr = getChild(par, !par.key.compareTo(key));
            while (curr.key.equals(K.MinValue0)) {
                par = curr.left;
                curr = getChild(par, !par.key.compareTo(key));
            }
            while (true) {
                if (!curr.key.compareTo(K.MaxValue2)) {
                    next = curr;
                    curr = par;
                } else {
                    next = getChild(curr, !curr.key.compareTo(key));
                    while (next.key.equals(K.MinValue0)) {
                        preNode = curr.preLink;
                        replacementNode = getReplacementInHelping(curr, par, preNode);
                        if (!casChild(par, curr, replacementNode, !par.key.compareTo(key))) {
                            continue retry;
                        }
                        curr = replacementNode;
                        next = getChild(curr, !curr.key.compareTo(key));
                    }
                }
                preNode = curr.preLink;
                boolean dir = !curr.key.compareTo(key);
                if (curr.key.equals(key) && preNode == null) {
                    return false;
                }
                if (!next.key.compareTo(K.MaxValue2)) {
                    if (next.key.equals(K.MaxValue1)) {
                        replacementNode = getReplacement(curr, next, next.left, true);
                        if (dir) {
                            casChild(par, curr, replacementNode, !par.key.compareTo(key));
                        }
                        par = par == next.right ? next.left : par;//here par itself is the node getting removed as a category 2 node
                        //so we need to go to its parent which is stored at the left of the orderlink
                    } else if (next.key.equals(K.MaxValue0)) {
                        replacementNode = getReplacementInHelping(curr, par, preNode);
                        casChild(par, curr, replacementNode, !par.key.compareTo(key));
                        par = par == curr ? next.left : par;
                    } else {
                        if (node == null) {
                            node = new Node(key);
                            node.left = new Node(K.MaxValue2, null, node, null);
                        }

                        node.right = next;
                        if (casChild(curr, next, node, dir)) {
                            return true;
                        }
                    }
                    continue retry;
                } else {
                    par = curr;
                    curr = next;
                }
            }
        }
    }

    @Override
    public boolean remove(K key) {
        Node par, curr, next;
        Node parent = null, marker, preNode, delNode = null, replacementNode;
        boolean mode = true;
        par = cRoot;
        retry:
        while (true) {
            curr = getChild(par, !par.key.compareTo(key));
            while (curr.key.equals(K.MinValue0)) {
                par = curr.left;
                curr = getChild(par, !par.key.compareTo(key));
            }
            while (true) {
                if (!curr.key.compareTo(K.MaxValue2)) {
                    next = curr;
                    curr = par;
                } else {
                    next = getChild(curr, !curr.key.compareTo(key));
                    while (next.key.equals(K.MinValue0)) {
                        preNode = curr.preLink;
                        replacementNode = getReplacementInHelping(curr, par, preNode);
                        if (!casChild(par, curr, replacementNode, !par.key.compareTo(key))) {
                            continue retry;
                        }
                        curr = replacementNode;
                        next = getChild(curr, !curr.key.compareTo(key));
                    }
                }

                if (curr.key.equals(key) && delNode == null) {
                    parent = par;
                    delNode = curr;
                }

                if (!next.key.compareTo(K.MaxValue2)) {
                    if (mode) {
                        if (next.right != delNode) {
                            return false;
                        }

                        if (next.key.equals(K.MaxValue1)) {
                            replacementNode = getReplacement(curr, next, parent, false);
                            if (replacementNode == null || casChild(parent, delNode, replacementNode, !parent.key.compareTo(key))) {
                                return false;
                            }
                            par = next.left;
                        } else if (next.key.equals(K.MaxValue0)) {
                            preNode = curr.preLink;
                            replacementNode = getReplacementInHelping(curr, par, preNode);
                            casChild(par, curr, replacementNode, !par.key.compareTo(key));
                            par = par == curr ? next.left : par;
                        } else {
                            marker = new Node(K.MaxValue1, parent, delNode, null);
                            if (casChild(curr, next, marker, !curr.key.compareTo(key))) {
                                mode = false;
                                replacementNode = getReplacement(curr, marker, parent, false);
                                if (replacementNode == null || casChild(parent, delNode, replacementNode, !parent.key.compareTo(key))) {
                                    return true;
                                }
                                par = parent;
                            }
                        }
                        continue retry;
                    } else {
                        return true;
                    }
                } else {
                    par = curr;
                    curr = next;
                }
            }
        }
    }

    private Node getReplacementInHelping(Node delNode, Node parent, Node orderNode) {
        if (orderNode == delNode) {
            return markRight(delNode, parent, true);
        } else {
            Node orderLink = orderNode.right;
            if (orderLink.right == delNode) {
                Node leftChild = markLeft(delNode, parent, true);
                Node rightChild = markRight(delNode, parent, true);
                casChild(orderNode, orderLink, rightChild, false);
                return leftChild;
            }
            return delNode.left.right;
        }
    }

    private Node getReplacement(Node orderNode, Node orderLink, Node parent, boolean isHelping) {
        Node delNode = orderLink.right;
        if (!pLinkUpdater.compareAndSet(delNode, null, orderNode) && !isHelping) {
            return null;
        }

        if (orderNode == delNode) {
            return markRight(delNode, parent, isHelping);
        } else {
            Node leftChild = markLeft(delNode, parent, isHelping);
            if (leftChild == null) {
                return null;
            } else {
                Node rightChild = markRight(delNode, parent, isHelping);
                if (rightChild == null) {
                    return null;
                } else {
                    casChild(orderNode, orderLink, rightChild, false);
                    return leftChild;
                }
            }
        }
    }

    Node markRight(Node delNode, Node parent, boolean isHelping) {
        Node rChild = delNode.right, markNode;

        while (true) {
            K rChKey = rChild.key;

            if (rChKey.equals(K.MaxValue0)) {
                return isHelping ? rChild.preLink : null;
            } else if (rChKey.equals(K.MinValue0)) {
                return isHelping ? rChild.right : null;
            } else if (rChKey.equals(K.MaxValue1)) {
                getReplacement(delNode, rChild, rChild.left, true);
            } else if (rChKey.equals(K.MaxValue2)) {
                markNode = new Node(K.MaxValue0, parent, rChild.right, rChild);
                if (casChild(delNode, rChild, markNode, false)) {
                    return rChild;
                }
            } else {
                markNode = new Node(K.MinValue0, parent, rChild, null);
                if (casChild(delNode, rChild, markNode, false)) {
                    return rChild;
                }
            }
            rChild = delNode.right;
        }
    }

    Node markLeft(Node delNode, Node parent, boolean isHelping) {
        Node lChild = delNode.left, markNode;
        while (true) {
            K lChKey = lChild.key;
            if (lChKey.equals(K.MinValue0)) {
                return isHelping ? lChild.right : null;
            } else {
                markNode = new Node(K.MinValue0, parent, lChild, null);
                if (casChild(delNode, lChild, markNode, true)) {
                    return lChild;
                }
            }

            lChild = delNode.left;
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
        inorder(cRoot, keys);
        for (int i = 0; i < keys.size() - 1; i++) {
            if (!keys.get(i).compareTo(keys.get(i + 1))) {
                return false;
            }
        }
        return true;
    }

    protected static class Node {

        final K key;
        volatile Node left, right, preLink;

        Node(K key) {
            this(key, null, null, null);
        }

        Node(K key, final Node l, final Node r, final Node p) {
            this.key = key;
            this.left = l;
            this.right = r;
            this.preLink = p;
        }
    }
}
