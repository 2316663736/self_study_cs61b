package hashmap;

import javax.print.attribute.standard.Finishings;
import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    int size;
    double loadFactor ;
    int iniSize;

    /** Constructors */
    public MyHashMap() {
        size = 0;
        iniSize = 10;
        loadFactor = 1.5;
        buckets = createTable(iniSize);
    }

    public MyHashMap(int initialSize) {
        size = 0;
        iniSize = initialSize;
        loadFactor = 1.5;
        buckets = createTable(iniSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        size = 0;
        iniSize = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(iniSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<Node>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection[] res = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            res[i] = createBucket();
        }
        return res;
    }

    /**
     * @param key 键值
     * @return 返回哈希值
     */
    private int hash(K key) {
        return Math.floorMod(key.hashCode(),iniSize);
    }


    private Collection<Node>[] copyToNew (Collection<Node>[] newBuckets) {
        for (int i = 0; i < buckets.length; i++) {
            for (Node n : buckets[i]) {
                newBuckets[hash(n.key)].add(n);
            }
        }
        return newBuckets;
    }


    // : Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!
    @Override
    public void clear() {
        size = 0;
        iniSize = 10;
        buckets = createTable(iniSize);
    }

    @Override
    public boolean containsKey(K key) {
        int haskKey = hash(key);
        for (Node n : buckets[haskKey]) {
            if (n.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int haskKey = hash(key);
        for (Node n : buckets[haskKey]) {
            if (n.key.equals(key)) {
                return n.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        int haskKey = hash(key);
        boolean found = false;
        for (Node n : buckets[haskKey]) {
            if (n.key.equals(key)) {
                n.value = value;
                found = true;
            }
        }
        if (!found) {
            Node n = createNode(key, value);
            buckets[haskKey].add(n);
            ++size;
        }
        if (1.0 * size / iniSize > loadFactor) {
            iniSize *= 2;
            Collection<Node>[] newBuckets = createTable(iniSize);
            buckets = copyToNew(newBuckets);
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (int i = 0; i < buckets.length; i++) {
            for (Node n : buckets[i]) {
                keys.add(n.key);
            }
        }
        return keys;
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }
    private class  HashMapIter implements Iterator<K> {
        Iterator<K> iter;
        HashMapIter() {
            iter = keySet().iterator();
        }
        public boolean hasNext() {
            return iter.hasNext();
        }
        public K next() {
            return iter.next();
        }
    }
    @Override
    public Iterator<K> iterator() {
        return new HashMapIter();
    }

}
