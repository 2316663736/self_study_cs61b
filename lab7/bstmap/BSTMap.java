package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;


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
    private  inNode find (K key) {
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
        Set<K> allKeys = new HashSet<>();
        Stack<inNode> stack = new Stack<>();
        inNode now = root;
        while (now != null || !stack.isEmpty()) {
            while (now != null) {
                stack.push(now);
                now = now.left;
            }
            now = stack.pop();
            allKeys.add(now.key);
            now = now.right;
        }
        return allKeys;
    }

    /**
     * @param key
     * @return 找到的，左/右孩子索引是key的节点
     */
    private inNode getFather(K key) {
        inNode pre = null;
        inNode now = root;
        while (now != null) {
            if (now.key.compareTo(key) == 0) {
                return pre;
            } else if (now.key.compareTo(key) < 0) {
                pre = now;
                now = now.right;
            } else {
                pre = now;
                now = now.left;
            }
        }
        return null;
    }
    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    @Override
    public V remove(K key) {
        inNode now = find(key);
        if (now == null || now.key.compareTo(key) != 0) {
            return null;
        }
        --size;
        V value = now.value;
        inNode pre = now;
        inNode next ;
        if (now.left != null) {
            next = now.left;
            while (next.right != null) {
                pre = next;
                next = next.right;
            }
            if (pre == now) {
                pre.left = null;
            } else {
                pre.right = null;
            }
            now.key = next.key;
            now.value = next.value;
        } else if (now.right != null) {
            next = now.right;
            while (next.left != null) {
                pre = next;
                next = next.left;
            }
            if (pre == now) {
                pre.right = null;
            }  else {
                pre.left = null;
            }
            now.key = next.key;
            now.value = next.value;
        } else {
            inNode father = getFather(key);
            if (father == null) {//说明now是root节点
                assert size == 0 ;
                root = null;
            } else if (father.left == now) {
                father.left = null;
            } else {
                father.right = null;
            }

        }

        return value;
    }

    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    @Override
    public V remove(K key, V value) {
        inNode now = find(key);
        if (now != null && now.key.compareTo(key) == 0 && now.value.equals(value)) {
            return remove(key);
        } else {
            return null;
        }
    }

    private class BSTMapiter implements Iterator<K> {
        inNode now ;
        Stack<inNode> stack ;
        BSTMapiter() {
            now = root;
            stack = new Stack<>();
        }
        public boolean hasNext() {
            return now != null || !stack.isEmpty();
        }
        public K next() {
                while (now != null) {
                    stack.push(now);
                    now = now.left;
                }
                now = stack.pop();
                K res = now.key;
                now = now.right;
                return res;
        }

    }
    @Override
    public Iterator<K> iterator() {
        return new BSTMapiter();
    }
//    private void printInorderHelp(inNode the) {
//        if (the == null) {
//            return;
//        }
//        printInorderHelp(the.left);
//        System.out.print(the.key + "<=>" + the.value + '\n');
//        printInorderHelp(the.right);
//    }
//    public  void printInOrder() {
//        if (root == null) {
//            return;
//        }
//        printInorderHelp(root.left);
//        System.out.print(root.key + "<=>" + root.value + '\n');
//        printInorderHelp(root.right);
//    }
    public void printInOrder() {
        inNode now = root;
        Stack<inNode> stack = new Stack<>();
        while (now != null || !stack.isEmpty()) {
            while (now != null) {
                stack.push(now);
                now = now.left;
            }
            now = stack.pop();
            System.out.print(now.key + "<=>" + now.value + "\n") ;
            now = now.right;
        }
    }
}
