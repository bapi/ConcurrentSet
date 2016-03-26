/**
 * This code file was downloaded from Trevor Brown's home-page
 * http://www.cs.utoronto.ca/~tabrown/ and modified to remove value field from
 * node to make it fair to compare it with other set implementations. We also
 * added the methods inorder() and traversalTest() to check the sanity of the
 * code.
 */
package se.chalmers.dcs.bapic.concurrentset.Sets;

import java.util.LinkedList;
import se.chalmers.dcs.bapic.concurrentset.utils.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 *
 * @author
 */
public class EFRBLFBST implements SetADT {

//  --------------------------------------------------------------------------------
//   DICTIONARY
//  --------------------------------------------------------------------------------
    private static final AtomicReferenceFieldUpdater<Node, Node> leftUpdater
                                                                 = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
    private static final AtomicReferenceFieldUpdater<Node, Node> rightUpdater
                                                                 = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
    private static final AtomicReferenceFieldUpdater<Node, Info> infoUpdater
                                                                 = AtomicReferenceFieldUpdater.newUpdater(Node.class, Info.class, "info");
    final Node root;

    /**
     *
     */
    public EFRBLFBST() {

        // to avoid handling special case when <= 2 nodes,
        // create 2 dummy nodes, both contain key null
        // All real keys inside BST are required to be non-null
        root = new Node(K.MaxValue0, new Node(K.MaxValue1), new Node(K.MaxValue0));
    }

//  --------------------------------------------------------------------------------
//   PUBLIC METHODS:
//   - find   : boolean
//   - insert : boolean
//   - delete : boolean
//  --------------------------------------------------------------------------------
    /**
     * PRECONDITION: k CANNOT BK NULL
     *
     * @param key
     * @return
     */
    @Override
    public final boolean contains(final K key) {
        if (key == null) {
            throw new NullPointerException();
        }

        Node l = root.left;

        while (l.left != null) {
            l = key.compareTo(l.key)
                ? l.left
                : l.right;
        }

        return key.equals(l.key);
    }

    // Insert key to dictionary, returns the previous value associated with the specified key,
    // or null if there was no mapping for the key
    /**
     * PRECONDITION: k CANNOT BK NULL
     *
     * @param key
     * @return
     */
    public boolean add(final K key) {
        Node newInternal;
        Node newSibling, newNode;

        /**
         * SEARCH VARIABLES
         */
        Node p;
        Info pinfo;
        Node l;

        /**
         * END SEARCH VARIABLES
         */
        newNode = new Node(key);

        while (true) {

            /**
             * SEARCH
             */
            p = root;
            pinfo = p.info;
            l = p.left;

            while (l.left != null) {
                p = l;
                l = key.compareTo(l.key)
                    ? l.left
                    : l.right;
            }

            pinfo = p.info;                  // read pinfo once instead of every iteration

            if ((l != p.left) && (l != p.right)) {
                continue;                    // then confirm the child link to l is valid
            }                                // (just as if we'd read p's info field before the reference to l)

            /**
             * END SEARCH
             */
            if (key.equals(l.key)) {
                return false;                // key already in the tree, no duplicate allowed
            }
            else if ( ! ((pinfo == null) || (pinfo.getClass() == Clean.class))) {
                help(pinfo);
            }
            else {
                newSibling = new Node(l.key);

                if (key.compareTo(l.key)) // newinternal = max(ret.l.key, key);
                {
                    newInternal = new Node(l.key, newNode, newSibling);
                }
                else {
                    newInternal = new Node(key, newSibling, newNode);
                }

                final IInfo newPInfo = new IInfo(l, p, newInternal);

                // try to IFlag parent
                if (infoUpdater.compareAndSet(p, pinfo, newPInfo)) {
                    helpInsert(newPInfo);

                    return true;
                }
                else {

                    // if fails, help the current operation
                    // [CHECK]
                    // need to get the latest p.info since CAS doesnt return current value
                    help(p.info);
                }
            }
        }
    }

    // Delete key from dictionary, return the associated value when successful, null otherwise
    /**
     * PRECONDITION: k CANNOT BK NULL
     *
     * @param key
     * @return
     */
    public final boolean remove(final K key) {

        /**
         * SEARCH VARIABLES
         */
        Node gp;
        Info gpinfo;
        Node p;
        Info pinfo;
        Node l;

        /**
         * END SEARCH VARIABLES
         */
        while (true) {

            /**
             * SEARCH
             */
            gp = null;
            gpinfo = null;
            p = root;
            pinfo = p.info;
            l = p.left;

            while (l.left != null) {
                gp = p;
                p = l;
                l = (key.compareTo(l.key))
                    ? l.left
                    : l.right;
            }

            // note: gp can be null here, because clearly the root.left.left == null
            // when the tree is empty. however, in this case, l.key will be null,
            // and the function will return null, so this does not pose a problem.
            if (gp != null) {
                gpinfo = gp.info;    // - read gpinfo once instead of every iteration

                if ((p != gp.left) && (p != gp.right)) {
                    continue;        // then confirm the child link to p is valid
                }

                pinfo = p.info;      // (just as if we'd read gp's info field before the reference to p)

                if ((l != p.left) && (l != p.right)) {
                    continue;        // - do the same for pinfo and l
                }
            }

            /**
             * END SEARCH
             */
            if ( ! key.equals(l.key)) {
                return false;
            }

            if ( ! ((gpinfo == null) || (gpinfo.getClass() == Clean.class))) {
                help(gpinfo);
            }
            else if ( ! ((pinfo == null) || (pinfo.getClass() == Clean.class))) {
                help(pinfo);
            }
            else {

                // try to DFlag grandparent
                final DInfo newGPInfo = new DInfo(l, p, gp, pinfo);

                if (infoUpdater.compareAndSet(gp, gpinfo, newGPInfo)) {
                    if (helpDelete(newGPInfo)) {
                        return true;
                    }
                }
                else {

                    // if fails, help grandparent with its latest info value
                    help(gp.info);
                }
            }
        }
    }

//  --------------------------------------------------------------------------------
//   PRIVATK METHODS
//   - helpInsert
//   - helpDelete
//  --------------------------------------------------------------------------------
    private void helpInsert(final IInfo info) {
        ((info.p.left == info.l)
         ? leftUpdater
         : rightUpdater).compareAndSet(info.p, info.l, info.newInternal);
        infoUpdater.compareAndSet(info.p, info, new Clean());
    }

    private boolean helpDelete(final DInfo info) {
        final boolean result;

        result = infoUpdater.compareAndSet(info.p, info.pinfo, new Mark(info));

        final Info currentPInfo = info.p.info;

        // if  CAS succeed or somebody else already suceed helping, the helpMarked
        if (result || ((currentPInfo.getClass() == Mark.class) && ((Mark) currentPInfo).dinfo == info)) {
            helpMarked(info);

            return true;
        }
        else {
            help(currentPInfo);
            infoUpdater.compareAndSet(info.gp, info, new Clean());

            return false;
        }
    }

    private void help(final Info info) {
        if (info.getClass() == IInfo.class) {
            helpInsert((IInfo) info);
        }
        else if (info.getClass() == DInfo.class) {
            helpDelete((DInfo) info);
        }
        else if (info.getClass() == Mark.class) {
            helpMarked(((Mark) info).dinfo);
        }
    }

    private void helpMarked(final DInfo info) {
        final Node other = (info.p.right == info.l)
                           ? info.p.left
                           : info.p.right;

        ((info.gp.left == info.p)
         ? leftUpdater
         : rightUpdater).compareAndSet(info.gp, info.p, other);
        infoUpdater.compareAndSet(info.gp, info, new Clean());
    }

    /**
     *
     */
    protected final static class Clean extends Info {
    }

    /**
     *
     */
    protected final static class DInfo extends Info {

        final Node p;
        final Node l;
        final Node gp;
        final Info pinfo;

        DInfo(final Node leaf, final Node parent, final Node grandparent, final Info pinfo) {
            this.p = parent;
            this.l = leaf;
            this.gp = grandparent;
            this.pinfo = pinfo;
        }
    }

    /**
     *
     */
    protected final static class IInfo extends Info {

        final Node p;
        final Node l;
        final Node newInternal;

        IInfo(final Node leaf, final Node parent, final Node newInternal) {
            this.p = parent;
            this.l = leaf;
            this.newInternal = newInternal;
        }
    }

    /**
     *
     */
    protected static abstract class Info {
    }

    /**
     *
     */
    protected final static class Mark extends Info {

        final DInfo dinfo;

        Mark(final DInfo dinfo) {
            this.dinfo = dinfo;
        }
    }

    private void inorder(Node node, LinkedList<K> list) {
        if (node.left == null) {
            list.add(node.key);
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
    protected final static class Node {

        final K key;
        volatile Node left;
        volatile Node right;
        volatile Info info;

        Node(final K key) {
            this(key, null, null);
        }

        /**
         * FOR MANUAL CREATION OF NODES (only used directly by testbed)
         */
        Node(final K key, final Node left, final Node right) {
            this.key = key;
            this.left = left;
            this.right = right;
            this.info = null;
        }
    }
}
