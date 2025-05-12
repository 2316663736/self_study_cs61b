package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class inNode {
        private K key;
        private V value;
        private inNode right = null;
        private inNode left = null;
        private inNode(K k, V v) {
            key = k;
            value = v;
        }
    }

    private inNode root = null;
    private  int size = 0;

    /**
     * @param key
     * @return 等于key的节点，没有就是取最接近的
     */
    private inNode find (K key) {
        inNode pre = null;
        inNode now = root;
        while (now != null) {
            if (now.key.compareTo(key) == 0) {
                return now;
            } else if (now.key.compareTo(key) < 0) {
                pre = now;
                now = now.right;
            } else {
                pre = now;
                now = now.left;
            }
        }
        return pre;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    /* Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        inNode res = find(key);
        return res != null && res.key.compareTo(key) == 0;
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        inNode res = find(key);
        if (res != null && res.key.compareTo(key) == 0) {
            return res.value;
        }
        return null;
    }

    /* Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size;
    }

    /* Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value) {
        inNode res = find(key);
        if (res == null) {
            ++size;
            res = new inNode(key, value);
            root = res;
        } else if (res.key.compareTo(key) == 0) {
            res.value = value;
        } else if (res.key.compareTo(key) < 0) {
            ++size;
            res.right = new inNode(key, value);
        } else if (res.key.compareTo(key) > 0) {
            ++size;
            res.left = new inNode(key, value);
        }

    }

    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }
}
