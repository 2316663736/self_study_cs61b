package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * 优化版BSTMap实现，使用AVL平衡树算法
 */
public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class Node {
        private K key;
        private V value;
        private Node right = null;
        private Node left = null;
        private int height = 1; // 用于AVL树平衡

        private Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    private Node root = null;
    private int size = 0;

    /**
     * 包含节点及其父节点的信息类
     */
    private class NodeInfo {
        Node node;
        Node parent;

        NodeInfo(Node n, Node p) {
            node = n;
            parent = p;
        }
    }

    /**
     * 查找节点并返回节点及其父节点信息
     */
    private NodeInfo findWithParent(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key不能为null");
        }

        Node parent = null;
        Node current = root;

        while (current != null) {
            int cmp = current.key.compareTo(key);
            if (cmp == 0) {
                return new NodeInfo(current, parent);
            } else if (cmp < 0) {
                parent = current;
                current = current.right;
            } else {
                parent = current;
                current = current.left;
            }
        }

        return new NodeInfo(null, parent);
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key不能为null");
        }
        NodeInfo info = findWithParent(key);
        return info.node != null;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key不能为null");
        }
        NodeInfo info = findWithParent(key);
        return (info.node != null) ? info.node.value : null;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * 获取节点高度，用于AVL平衡
     */
    private int height(Node node) {
        return (node == null) ? 0 : node.height;
    }

    /**
     * 更新节点高度
     */
    private void updateHeight(Node node) {
        if (node != null) {
            node.height = 1 + Math.max(height(node.left), height(node.right));
        }
    }

    /**
     * 获取平衡因子
     */
    private int balanceFactor(Node node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    /**
     * 右旋转
     */
    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        // 执行旋转
        x.right = y;
        y.left = T2;

        // 更新高度
        updateHeight(y);
        updateHeight(x);

        return x;
    }

    /**
     * 左旋转
     */
    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        // 执行旋转
        y.left = x;
        x.right = T2;

        // 更新高度
        updateHeight(x);
        updateHeight(y);

        return y;
    }

    /**
     * 递归插入并保持AVL平衡
     */
    private Node insertRecursive(Node node, K key, V value) {
        // 标准BST插入
        if (node == null) {
            size++;
            return new Node(key, value);
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = insertRecursive(node.left, key, value);
        } else if (cmp > 0) {
            node.right = insertRecursive(node.right, key, value);
        } else {
            // 键已存在，更新值
            node.value = value;
            return node;
        }

        // 更新当前节点高度
        updateHeight(node);

        // 获取平衡因子
        int balance = balanceFactor(node);

        // 左左情况
        if (balance > 1 && key.compareTo(node.left.key) < 0) {
            return rotateRight(node);
        }

        // 右右情况
        if (balance < -1 && key.compareTo(node.right.key) > 0) {
            return rotateLeft(node);
        }

        // 左右情况
        if (balance > 1 && key.compareTo(node.left.key) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // 右左情况
        if (balance < -1 && key.compareTo(node.right.key) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key不能为null");
        }
        root = insertRecursive(root, key, value);
    }

    /**
     * 查找最小值节点
     */
    private Node findMin(Node node) {
        Node current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    /**
     * 递归删除节点并保持AVL平衡
     */
    private Node deleteRecursive(Node node, K key, boolean countSize) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = deleteRecursive(node.left, key, countSize);
        } else if (cmp > 0) {
            node.right = deleteRecursive(node.right, key, countSize);
        } else {
            // 找到要删除的节点
            if (countSize) {
                size--;
            }

            // 情况1: 叶节点
            if (node.left == null && node.right == null) {
                return null;
            }
            // 情况2: 只有一个子节点
            else if (node.left == null) {
                return node.right;
            } else if (node.right == null) {
                return node.left;
            }
            // 情况3: 有两个子节点
            else {
                // 找右子树中的最小节点
                Node successor = findMin(node.right);

                // 用后继节点的值替换当前节点
                node.key = successor.key;
                node.value = successor.value;

                // 删除后继节点，但不再减少size
                node.right = deleteRecursive(node.right, successor.key, false);
            }
        }

        if (node == null) {
            return null;
        }

        // 更新高度
        updateHeight(node);

        // 检查并恢复平衡
        int balance = balanceFactor(node);

        // 左左情况
        if (balance > 1 && balanceFactor(node.left) >= 0) {
            return rotateRight(node);
        }

        // 左右情况
        if (balance > 1 && balanceFactor(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // 右右情况
        if (balance < -1 && balanceFactor(node.right) <= 0) {
            return rotateLeft(node);
        }

        // 右左情况
        if (balance < -1 && balanceFactor(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    @Override
    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key不能为null");
        }

        // 先找到节点值
        NodeInfo info = findWithParent(key);
        if (info.node == null) {
            return null;
        }

        V value = info.node.value;
        root = deleteRecursive(root, key, true); // 传入true表示需要计数size
        return value;
    }

    @Override
    public V remove(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key不能为null");
        }

        NodeInfo info = findWithParent(key);
        if (info.node != null && info.node.value.equals(value)) {
            return remove(key);
        }
        return null;
    }

    @Override
    public Set<K> keySet() {
        Set<K> allKeys = new HashSet<>();
        inorderTraversal(root, allKeys);
        return allKeys;
    }

    private void inorderTraversal(Node node, Set<K> keys) {
        if (node != null) {
            inorderTraversal(node.left, keys);
            keys.add(node.key);
            inorderTraversal(node.right, keys);
        }
    }

    private class BSTMapIter implements Iterator<K> {
        Stack<Node> stack;
        Node current;

        BSTMapIter() {
            stack = new Stack<>();
            current = root;
        }

        public boolean hasNext() {
            return (current != null || !stack.isEmpty());
        }

        public K next() {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }

            current = stack.pop();
            K key = current.key;
            current = current.right;

            return key;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTMapIter();
    }

    public void printInOrder() {
        inorderPrint(root);
    }

    private void inorderPrint(Node node) {
        if (node != null) {
            inorderPrint(node.left);
            System.out.print(node.key + "<=>" + node.value + "\n");
            inorderPrint(node.right);
        }
    }

//// 非递归中序遍历打印（备选方法）
//    public void printInOrderIterative() {
//        Node current = root;
//        Stack<Node> stack = new Stack<>();
//
//        while (current != null || !stack.isEmpty()) {
//            while (current != null) {
//                stack.push(current);
//                current = current.left;
//            }
//
//            current = stack.pop();
//            System.out.print(current.key + "<=>" + current.value + "\n");
//            current = current.right;
//        }
//    }
}
//package bstmap;
//
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//import java.util.Stack;
//
//
//public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
//
//    private class inNode {
//        private K key;
//        private V value;
//        private inNode right = null;
//        private inNode left = null;
//        private inNode(K k, V v) {
//            key = k;
//            value = v;
//        }
//    }
//
//    private inNode root = null;
//    private  int size = 0;
//
//    /**
//     * @param key
//     * @return 等于key的节点，没有就是取最接近的
//     */
//    private  inNode find (K key) {
//        inNode pre = null;
//        inNode now = root;
//        while (now != null) {
//            if (now.key.compareTo(key) == 0) {
//                return now;
//            } else if (now.key.compareTo(key) < 0) {
//                pre = now;
//                now = now.right;
//            } else {
//                pre = now;
//                now = now.left;
//            }
//        }
//        return pre;
//    }
//
//    @Override
//    public void clear() {
//        root = null;
//        size = 0;
//    }
//
//    /* Returns true if this map contains a mapping for the specified key. */
//    @Override
//    public boolean containsKey(K key) {
//        inNode res = find(key);
//        return res != null && res.key.compareTo(key) == 0;
//    }
//
//    /* Returns the value to which the specified key is mapped, or null if this
//     * map contains no mapping for the key.
//     */
//    @Override
//    public V get(K key) {
//        inNode res = find(key);
//        if (res != null && res.key.compareTo(key) == 0) {
//            return res.value;
//        }
//        return null;
//    }
//
//    /* Returns the number of key-value mappings in this map. */
//    @Override
//    public int size() {
//        return size;
//    }
//
//    /* Associates the specified value with the specified key in this map. */
//    @Override
//    public void put(K key, V value) {
//        inNode res = find(key);
//        if (res == null) {
//            ++size;
//            res = new inNode(key, value);
//            root = res;
//        } else if (res.key.compareTo(key) == 0) {
//            res.value = value;
//        } else if (res.key.compareTo(key) < 0) {
//            ++size;
//            res.right = new inNode(key, value);
//        } else if (res.key.compareTo(key) > 0) {
//            ++size;
//            res.left = new inNode(key, value);
//        }
//
//    }
//
//    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
//     * If you don't implement this, throw an UnsupportedOperationException. */
//    @Override
//    public Set<K> keySet() {
//        Set<K> allKeys = new HashSet<>();
//        Stack<inNode> stack = new Stack<>();
//        inNode now = root;
//        while (now != null || !stack.isEmpty()) {
//            while (now != null) {
//                stack.push(now);
//                now = now.left;
//            }
//            now = stack.pop();
//            allKeys.add(now.key);
//            now = now.right;
//        }
//        return allKeys;
//    }
//
//    /**
//     * @param key
//     * @return 找到的，左/右孩子索引是key的节点
//     */
//    private inNode getFather(K key) {
//        inNode pre = null;
//        inNode now = root;
//        while (now != null) {
//            if (now.key.compareTo(key) == 0) {
//                return pre;
//            } else if (now.key.compareTo(key) < 0) {
//                pre = now;
//                now = now.right;
//            } else {
//                pre = now;
//                now = now.left;
//            }
//        }
//        return null;
//    }
//    /* Removes the mapping for the specified key from this map if present.
//     * Not required for Lab 7. If you don't implement this, throw an
//     * UnsupportedOperationException. */
//    @Override
//    public V remove(K key) {
//        inNode now = find(key);
//        if (now == null || now.key.compareTo(key) != 0) {
//            return null;
//        }
//        --size;
//        V value = now.value;
//        inNode pre = now;
//        inNode next ;
//        if (now.left != null) {
//            next = now.left;
//            while (next.right != null) {
//                pre = next;
//                next = next.right;
//            }
//            if (pre == now) {
//                pre.left = next.left;
//            } else {
//                pre.right = next.left;
//            }
//            now.key = next.key;
//            now.value = next.value;
//        } else if (now.right != null) {
//            next = now.right;
//            while (next.left != null) {
//                pre = next;
//                next = next.left;
//            }
//            if (pre == now) {
//                pre.right = next.right;
//            }  else {
//                pre.left = next.right;
//            }
//
//            now.key = next.key;
//            now.value = next.value;
//        } else {
//            inNode father = getFather(key);
//            if (father == null) {//说明now是root节点
//                assert size == 0 ;
//                root = null;
//            } else if (father.left == now) {
//                father.left = null;
//            } else {
//                father.right = null;
//            }
//
//        }
//
//        return value;
//    }
//
//    /* Removes the entry for the specified key only if it is currently mapped to
//     * the specified value. Not required for Lab 7. If you don't implement this,
//     * throw an UnsupportedOperationException.*/
//    @Override
//    public V remove(K key, V value) {
//        inNode now = find(key);
//        if (now != null && now.key.compareTo(key) == 0 && now.value.equals(value)) {
//            return remove(key);
//        } else {
//            return null;
//        }
//    }
//
//    private class BSTMapiter implements Iterator<K> {
//        inNode now ;
//        Stack<inNode> stack ;
//        BSTMapiter() {
//            now = root;
//            stack = new Stack<>();
//        }
//        public boolean hasNext() {
//            return now != null || !stack.isEmpty();
//        }
//        public K next() {
//                while (now != null) {
//                    stack.push(now);
//                    now = now.left;
//                }
//                now = stack.pop();
//                K res = now.key;
//                now = now.right;
//                return res;
//        }
//
//    }
//    @Override
//    public Iterator<K> iterator() {
//        return new BSTMapiter();
//    }
////    private void printInorderHelp(inNode the) {
////        if (the == null) {
////            return;
////        }
////        printInorderHelp(the.left);
////        System.out.print(the.key + "<=>" + the.value + '\n');
////        printInorderHelp(the.right);
////    }
////    public  void printInOrder() {
////        if (root == null) {
////            return;
////        }
////        printInorderHelp(root.left);
////        System.out.print(root.key + "<=>" + root.value + '\n');
////        printInorderHelp(root.right);
////    }
//    public void printInOrder() {
//        inNode now = root;
//        Stack<inNode> stack = new Stack<>();
//        while (now != null || !stack.isEmpty()) {
//            while (now != null) {
//                stack.push(now);
//                now = now.left;
//            }
//            now = stack.pop();
//            System.out.print(now.key + "<=>" + now.value + "\n") ;
//            now = now.right;
//        }
//    }
//}
