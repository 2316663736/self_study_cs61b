package deque;
import java.util.Iterator;

/**
 * LinkedListDeque是一个基于双向链表实现的双端队列。
 * 它允许在队列的两端进行高效的添加和删除操作，具有常数级别的时间复杂度。
 *
 * @param <T> 存储在队列中的元素类型
 */
public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    /** 队列中的元素数量 */
    private int size;
    //head 和tail 始终指向固定的元素（即浪费两个元素空间，以求head和tail 不变）
    /** 标记队列前端的哨兵节点 */
    private final Node head;
    /** 标记队列后端的哨兵节点 */
    private final Node tail;

    /**
     * Node类表示链表中的单个元素。
     * 包含元素值以及指向前一个和后一个节点的引用。
     */
    private class Node {
        private final T num;
        private Node next;
        private Node prev;
        /**
         * 使用给定值创建新节点。
         *
         * @param n 存储在此节点中的值
         */
        private Node(T n) {
            num = n;
        }
        /**
         * 使用给定值和指向前一个和后一个节点的链接创建新节点。
         *
         * @param n 存储在此节点中的值
         * @param p 前一个节点
         * @param q 后一个节点
         */
        private Node(T n, Node p, Node q) {
            num = n;
            prev = p;
            next = q;
        }

    }

    /**
     * 构造一个带有前后哨兵节点的空LinkedListDeque。
     */
    public LinkedListDeque() {
        head = new Node(null);
        tail = new Node(null);
        head.next = tail;
        tail.prev = head;
        size = 0;
    }

    /**
     * 将元素添加到队列的前端。
     * 在head哨兵节点之后插入新节点。
     *
     * @param item 要添加的元素
     */
    @Override
    public void addFirst(T item) {
        Node tem = new Node(item, head, head.next);
        head.next.prev = tem;
        head.next = tem;
        size += 1;
    }

    /**
     * 将元素添加到队列的后端。
     * 在tail哨兵节点之前插入新节点。
     *
     * @param item 要添加的元素
     */
    @Override
    public void addLast(T item) {
        Node tem = new Node(item, tail.prev, tail);
        tail.prev.next = tem;
        tail.prev = tem;
        size += 1;
    }
//
//    /**
//     * 判断队列是否为空。
//     *
//     * @return 如果队列为空则返回true，否则返回false
//     */
//    public boolean isEmpty() {
//        return size == 0;
//    }

    /**
     * 返回队列中的元素数量。
     *
     * @return 队列中的元素数量
     */
    @Override
    public int size() {
        return size;
    }
    /**
     * 打印队列中从前到后的所有元素，以空格分隔。
     * 最后跟一个换行符。
     */
    @Override
    public void printDeque() {
        Iterator<T> itr = iterator();
        while (itr.hasNext()) {
            System.out.print(itr.next() + " ");
        }

        System.out.println();
    }

    /**
     * 移除并返回队列前端的元素。
     * 如果队列为空，则返回null。
     *
     * @return 队列前端的元素，如果队列为空则返回null
     */
    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        T item = head.next.num;
        head.next = head.next.next;
        head.next.prev = head;
        size -= 1;
        return item;
    }


    /**
     * 移除并返回队列后端的元素。
     * 如果队列为空，则返回null。
     *
     * @return 队列后端的元素，如果队列为空则返回null
     */
    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        T item = tail.prev.num;
        tail.prev = tail.prev.prev;
        tail.prev.next = tail;
        size -= 1;
        return item;
    }

    /**
     * 使用迭代方式获取给定索引处的元素。
     * 如果不存在这样的元素，则返回null。
     *
     * @param index 要获取的元素的索引
     * @return 指定位置的元素，如果索引无效则返回null
     */
    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        Iterator<T> itr = new LinkedListIterator();
        T item = null;
        while (index >= 0) {
            item = itr.next();
            index -= 1;
        }
        return item;
    }

    /**
     * 递归get操作的辅助方法。
     *
     * @param n 当前节点
     * @param index 到目标索引的剩余步数
     * @return 指定位置的元素
     */
    private T recursionGetIndex(Node n, int index) {
        if (index == 0) {
            return n.num;
        }
        return recursionGetIndex(n.next, index - 1);
    }
    /**
     * 使用递归方法获取给定索引处的元素。
     * 如果不存在这样的元素，则返回null。
     *
     * @param index 要获取的元素的索引
     * @return 指定位置的元素，如果索引无效则返回null
     */
    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return recursionGetIndex(head.next, index);
    }

    private class LinkedListIterator implements Iterator<T> {
        Node current;
        private LinkedListIterator() {
            current = head.next;
        }
        @Override
        public boolean hasNext() {
            return current != tail;
        }
        @Override
        public T next() {
            T item = current.num;
            current = current.next;
            return item;
        }
    }

    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    @Override
    public boolean equals(Object o) {
        // 快速路径：是同一个对象
        if (this == o) {
            return true;
        }

        // 基本类型检查：必须是Deque
        if (!(o instanceof Deque)) {
            return false;
        }

        // 大小检查
        Deque<?> otherDeque = (Deque<?>) o;
        if (this.size() != otherDeque.size()) {
            return false;
        }

        // 空集合快速路径
        if (this.isEmpty()) {
            return true;
        }

        // 获取两个队列的迭代器
        Iterator<T> thisIter = this.iterator();
        Iterator<?> otherIter;

        // 获取对方的迭代器，处理不同类型的情况

        if (o instanceof Iterable) {
            otherIter = ((Iterable<?>) o).iterator();
        } else {
            // 这种情况实际上不应该发生，因为已经确认是Deque，而Deque继承了Iterable
            return false;
        }


        // 逐个比较元素
        while (thisIter.hasNext() && otherIter.hasNext()) {
            Object thisElement = thisIter.next();
            Object otherElement = otherIter.next();

            // 两个都是null
            if (thisElement == null && otherElement == null) {
                continue;
            }

            // 只有一个是null
            if (thisElement == null || otherElement == null) {
                return false;
            }

            // 使用equals比较，捕获可能的异常

            if (!thisElement.equals(otherElement)) {
                return false;
            }

        }

        // 确保两个迭代器都已经遍历完毕
        return !thisIter.hasNext() && !otherIter.hasNext();
    }
}
