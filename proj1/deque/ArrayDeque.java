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
    private int NowLen;
    /** 缩小前数组的最小大小 */
    private int MinSize = 16;
    /** 缩小数组的阈值比率 */
    private double SmallRate = 0.25;
    /** 扩展数组的比率 */
    private double ExpandRate = 1.02;
    /** 扩展时添加的最小元素数量 */
    private int MinExpandNum = 2;
    /**
     * 构造一个初始容量为8的空ArrayDeque。
     */
    public ArrayDeque() {
        size = 0;
        NowLen = 8;
        array = (T[]) new Object[NowLen];
    }
    /**
     * 将数组大小调整为新的大小。
     *
     * @param NewSize 数组的新大小
     */
    private void reszie(int NewSize) {
        T[] NewArray = (T[]) new Object[NewSize];
        for (int i = 0; i < size; i++) {
            NewArray[i] = array[(front + i) % NowLen];
        }
        array = NewArray;
        NowLen = NewSize;
        front = 0;
        rear = size;
    }
    /**
     * 在必要时扩展数组大小。
     * 当元素数量达到数组容量时，根据扩展比率增加数组大小。
     */
    private void ExpandSize() {
        if(size == NowLen) {
            reszie(Math.max(( (int) (Math.round(NowLen * ExpandRate))), MinExpandNum + NowLen));
        }
    }

    @Override
    public void addFirst(T item) {
        ExpandSize();
        front = (front - 1 + NowLen) % NowLen;
        array[front] = item;
        size++;
    }

    @Override
    public void addLast(T item) {
        ExpandSize();
        array[rear] = item;
        rear = (rear + 1) % NowLen;
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
        while(itr.hasNext()) {
            System.out.print(itr.next() + " ");
        }

        System.out.println();
    }
    private void SmallSize() {
        if(NowLen >= MinSize && size <= (int) NowLen * SmallRate) {
            reszie((int) (NowLen * SmallRate));
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
        if(isEmpty()) {
            return null;
        }
        T item = array[front];
        array[front] = null;  // 帮助垃圾回收
        front = (front + 1) % NowLen;  // 循环递增
        size--;
        SmallSize();  // 保留现有的调整大小逻辑
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
        if(isEmpty()) {
            return null;
        }
        rear = (rear - 1 + NowLen) % NowLen;
        T item = array[rear];
        array[rear] = null;  // 帮助垃圾回收
        size--;
        SmallSize();
        return item;
    }

    @Override
    public T get(int index) {
        if(index < 0 || index >= size) {
            return null;
        }

        return array[(front + index) % NowLen];
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
        if (!(o instanceof Deque)) {
            return false;
        }
        if(((Deque<?>) o).size() != this.size()) {
            return false;
        }

        if(this.isEmpty()) {
            return true;
        }
        Iterator<?> itr1= null;
        Iterator<T> itr2 = this.iterator();

        if(o instanceof LinkedListDeque) {
            itr1 = ((LinkedListDeque<?>) o).iterator();
        }
        else if(o instanceof ArrayDeque) {
            itr1 = ((ArrayDeque<?>) o).iterator();
        }

        if(itr1 == null ) {
            return false;
        }
        while(itr1.hasNext() && itr2.hasNext()) {
            if(!itr1.next().equals(itr2.next())) {
                return false;
            }
        }

        return true;
    }
}
