package deque;
import java.util.Iterator;
/**
 * ArrayDeque是一个基于可调整大小数组实现的双端队列。
 * 它允许在队列的两端进行高效的添加和删除操作，具有常数级别的平摊时间复杂度。
 *
 * @param <T> 存储在队列中的元素类型
 */
public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    /** 存储元素的数组 */
    private T[] array;
    /** 队列中的元素数量 */
    private int size;
    /** 第一个元素的索引 */
    private int front = 0;
    /**最后一个元素之后的索引*/
    private int rear = 0;
    /** 数组的当前长度 */
    private int nowLen;
    /** 缩小前数组的最小大小 */
    private int minSize = 16;
    /** 缩小数组的阈值比率 */
    private double smallRate = 0.25;
    /** 扩展数组的比率 */
    private double expandRate = 1.02;
    /** 扩展时添加的最小元素数量 */
    private int minExpandNum = 2;
    /**
     * 构造一个初始容量为8的空ArrayDeque。
     */
    public ArrayDeque() {
        size = 0;
        nowLen = 8;
        array = (T[]) new Object[nowLen];
    }
    /**
     * 将数组大小调整为新的大小。
     *
     * @param newSize 数组的新大小
     */
    private void reszie(int newSize) {
        T[] newArray = (T[]) new Object[newSize];
        for (int i = 0; i < size; i++) {
            newArray[i] = array[(front + i) % nowLen];
        }
        array = newArray;
        nowLen = newSize;
        front = 0;
        rear = size;
    }
    /**
     * 在必要时扩展数组大小。
     * 当元素数量达到数组容量时，根据扩展比率增加数组大小。
     */
    private void expandSize() {
        if (size == nowLen) {
            reszie(Math.max(((int) (Math.round(nowLen * expandRate))), minExpandNum + nowLen));
        }
    }

    @Override
    public void addFirst(T item) {
        expandSize();
        front = (front - 1 + nowLen) % nowLen;
        array[front] = item;
        size++;
    }

    @Override
    public void addLast(T item) {
        expandSize();
        array[rear] = item;
        rear = (rear + 1) % nowLen;
        size++;
    }
//    /**
//     * 判断队列是否为空。
//     *
//     * @return 如果队列为空则返回true，否则返回false
//     */
//    public boolean isEmpty() {
//        return size == 0;
//    }


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
    private void smallSize() {
        if (nowLen >= minSize && size <= (int) nowLen * smallRate) {
            reszie((int) (nowLen * smallRate));
        }
    }

    /**
     * 移除并返回队列前端的元素。
     * 如果队列为空，则返回null。
     *
     * @return 队列前端的元素，如果队列为空则返回null
     */
    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T item = array[front];
        array[front] = null;  // 帮助垃圾回收
        front = (front + 1) % nowLen;  // 循环递增
        size--;
        smallSize();  // 保留现有的调整大小逻辑
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
        if (isEmpty()) {
            return null;
        }
        rear = (rear - 1 + nowLen) % nowLen;
        T item = array[rear];
        array[rear] = null;  // 帮助垃圾回收
        size--;
        smallSize();
        return item;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        return array[(front + index) % nowLen];
    }
    /**
     * ArrayDeque的迭代器实现。
     */
    private class ArrayDequeIterator implements Iterator<T> {
        /** 数组中的当前位置 */
        int now;
        /**
         * 构造一个从第一个元素开始的迭代器。
         */
        ArrayDequeIterator() {
            now = 0;
        }
        @Override
        public boolean hasNext() {
            return now < size;
        }
        @Override
        public T next() {
            T item = get(now);
            now++;
            return item;
        }
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
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
