package deque;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

public class DequeEqualTest {
    @Test
    public void testNoraml() {
        ArrayDeque<Integer> arr = new ArrayDeque<>();
        LinkedListDeque<Integer> ll = new LinkedListDeque<>();
        arr.addFirst(1);
        ll.addFirst(1);
        assertEquals("ArrayDeque doesn't LinkedListDeque ", arr, ll);
    }
    @Test
    /* 使用两个LinkedListDeque进行对照的操作，测试下面几个函数（随机的）
    addFirst,addLast
    removeLast,removeFirst
    get,getRecursive
    equals
    然后，其中的部分函数，如iterator，在其它函数中使用了，相当于已经测试了
     */
    public void randomTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        ArrayDeque<Integer> lld2 = new ArrayDeque<>();
        int N = 1000;
        for (int i = 0; i < N; i++) {
            int ops = StdRandom.uniform(0, 7);
            int num = StdRandom.uniform(0, N);
            if (ops == 0) {
                lld1.addFirst(num);
                lld2.addFirst(num);
            } else if (ops == 1) {
                lld1.addLast(num);
                lld2.addLast(num);
            } else if (ops == 2) {
                assertEquals(lld1.removeFirst(), lld2.removeFirst());
            } else if (ops == 3) {
                assertEquals(lld1.removeLast(), lld2.removeLast());
            } else if (ops == 4) {
                assertEquals(lld1.get(num), lld2.get(num));
            } else if (ops == 5) {
                assertTrue(lld2.equals(lld1));
            } else if (ops == 6) {
                assertTrue(lld1.equals(lld2));
            }
        }
    }

    @Test
    public void testMixedDequeEquality() {
        // 创建一个ArrayDeque和一个LinkedListDeque
        ArrayDeque<String> arrayDeque = new ArrayDeque<>();
        LinkedListDeque<String> linkedDeque = new LinkedListDeque<>();

        // 添加相同的元素
        arrayDeque.addLast("first");
        arrayDeque.addLast("second");
        arrayDeque.addLast("third");

        linkedDeque.addLast("first");
        linkedDeque.addLast("second");
        linkedDeque.addLast("third");

        // 测试基本相等性
        assertTrue("ArrayDeque应该等于LinkedListDeque", arrayDeque.equals(linkedDeque));
        assertTrue("LinkedListDeque应该等于ArrayDeque", linkedDeque.equals(arrayDeque));

        // 测试与null的比较
        arrayDeque.addLast(null);
        linkedDeque.addLast(null);

        assertTrue("添加null后ArrayDeque应该等于LinkedListDeque", arrayDeque.equals(linkedDeque));
        assertTrue("添加null后LinkedListDeque应该等于ArrayDeque", linkedDeque.equals(arrayDeque));

        // 测试自定义对象
        class CustomClass {
            private final int value;

            public CustomClass(int value) {
                this.value = value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof CustomClass)) return false;
                CustomClass that = (CustomClass) o;
                return value == that.value;
            }
        }

        ArrayDeque<CustomClass> arrayDeque2 = new ArrayDeque<>();
        LinkedListDeque<CustomClass> linkedDeque2 = new LinkedListDeque<>();

        CustomClass obj1 = new CustomClass(42);
        CustomClass obj2 = new CustomClass(42); // 不同实例，相同值

        arrayDeque2.addLast(obj1);
        linkedDeque2.addLast(obj2);

        assertTrue("含有自定义对象的ArrayDeque应该等于LinkedListDeque", arrayDeque2.equals(linkedDeque2));
        assertTrue("含有自定义对象的LinkedListDeque应该等于ArrayDeque", linkedDeque2.equals(arrayDeque2));

        // 测试混合类型
        class SubClass extends CustomClass {
            public SubClass(int value) {
                super(value);
            }

            @Override
            public boolean equals(Object o) {
                return super.equals(o);
            }
        }

        ArrayDeque<CustomClass> arrayDeque3 = new ArrayDeque<>();
        LinkedListDeque<CustomClass> linkedDeque3 = new LinkedListDeque<>();

        arrayDeque3.addLast(new CustomClass(99));
        linkedDeque3.addLast(new SubClass(99)); // 不同类，但equals相等

        assertTrue("含有继承类对象的ArrayDeque应该等于LinkedListDeque", arrayDeque3.equals(linkedDeque3));
        assertTrue("含有继承类对象的LinkedListDeque应该等于ArrayDeque", linkedDeque3.equals(arrayDeque3));

        // 测试异构集合
        ArrayDeque<Object> arrayDeque4 = new ArrayDeque<>();
        LinkedListDeque<Object> linkedDeque4 = new LinkedListDeque<>();

        arrayDeque4.addLast("string");
        arrayDeque4.addLast(123);
        arrayDeque4.addLast(new CustomClass(42));

        linkedDeque4.addLast("string");
        linkedDeque4.addLast(123);
        linkedDeque4.addLast(new CustomClass(42));

        assertTrue("含有混合类型的ArrayDeque应该等于LinkedListDeque", arrayDeque4.equals(linkedDeque4));
        assertTrue("含有混合类型的LinkedListDeque应该等于ArrayDeque", linkedDeque4.equals(arrayDeque4));
    }

    @Test
    public void testEdgeCases() {
        // 空队列测试
        ArrayDeque<Integer> emptyArray = new ArrayDeque<>();
        LinkedListDeque<Integer> emptyLinked = new LinkedListDeque<>();

        assertTrue("空的ArrayDeque应该等于空的LinkedListDeque", emptyArray.equals(emptyLinked));
        assertTrue("空的LinkedListDeque应该等于空的ArrayDeque", emptyLinked.equals(emptyArray));

        // 只有一个元素的队列
        ArrayDeque<Integer> singleArray = new ArrayDeque<>();
        LinkedListDeque<Integer> singleLinked = new LinkedListDeque<>();

        singleArray.addFirst(42);
        singleLinked.addFirst(42);

        assertTrue("单元素ArrayDeque应该等于单元素LinkedListDeque", singleArray.equals(singleLinked));
        assertTrue("单元素LinkedListDeque应该等于单元素ArrayDeque", singleLinked.equals(singleArray));

        // 一个包含null的队列
        ArrayDeque<Integer> nullArray = new ArrayDeque<>();
        LinkedListDeque<Integer> nullLinked = new LinkedListDeque<>();

        nullArray.addFirst(null);
        nullLinked.addFirst(null);

        assertTrue("含null的ArrayDeque应该等于含null的LinkedListDeque", nullArray.equals(nullLinked));
        assertTrue("含null的LinkedListDeque应该等于含null的ArrayDeque", nullLinked.equals(nullArray));
    }

    @Test
    public void testFailureCases() {
        // 不同长度队列
        ArrayDeque<String> arrayDeque1 = new ArrayDeque<>();
        LinkedListDeque<String> linkedDeque1 = new LinkedListDeque<>();

        arrayDeque1.addLast("first");
        arrayDeque1.addLast("second");

        linkedDeque1.addLast("first");

        assertFalse("不同长度的队列不应该相等", arrayDeque1.equals(linkedDeque1));
        assertFalse("不同长度的队列不应该相等", linkedDeque1.equals(arrayDeque1));

        // 相同长度但不同元素
        LinkedListDeque<String> linkedDeque2 = new LinkedListDeque<>();
        linkedDeque2.addLast("first");
        linkedDeque2.addLast("different");

        assertFalse("内容不同的队列不应该相等", arrayDeque1.equals(linkedDeque2));
        assertFalse("内容不同的队列不应该相等", linkedDeque2.equals(arrayDeque1));

        // 不同顺序
        ArrayDeque<String> arrayDeque2 = new ArrayDeque<>();
        LinkedListDeque<String> linkedDeque3 = new LinkedListDeque<>();

        arrayDeque2.addLast("first");
        arrayDeque2.addLast("second");

        linkedDeque3.addLast("second");
        linkedDeque3.addLast("first");

        assertFalse("顺序不同的队列不应该相等", arrayDeque2.equals(linkedDeque3));
        assertFalse("顺序不同的队列不应该相等", linkedDeque3.equals(arrayDeque2));
    }


    @Test
    public void testMixedDequeEquality2() {
        // 创建一个ArrayDeque和一个LinkedListDeque
        ArrayDeque<String> arrayDeque = new ArrayDeque<>();
        LinkedListDeque<String> linkedDeque = new LinkedListDeque<>();

        // 添加相同的元素
        arrayDeque.addLast("first");
        arrayDeque.addLast("second");
        arrayDeque.addLast("third");

        linkedDeque.addLast("first");
        linkedDeque.addLast("second");
        linkedDeque.addLast("third");

        // 测试基本相等性
        assertTrue("ArrayDeque应该等于LinkedListDeque", arrayDeque.equals(linkedDeque));
        assertTrue("LinkedListDeque应该等于ArrayDeque", linkedDeque.equals(arrayDeque));

        // 测试与null的比较
        arrayDeque.addLast(null);
        linkedDeque.addLast(null);

        assertTrue("添加null后ArrayDeque应该等于LinkedListDeque", arrayDeque.equals(linkedDeque));
        assertTrue("添加null后LinkedListDeque应该等于ArrayDeque", linkedDeque.equals(arrayDeque));

        // 测试自定义对象
        class CustomClass {
            private final int value;

            public CustomClass(int value) {
                this.value = value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof CustomClass)) return false;
                CustomClass that = (CustomClass) o;
                return value == that.value;
            }
        }

        ArrayDeque<CustomClass> arrayDeque2 = new ArrayDeque<>();
        LinkedListDeque<CustomClass> linkedDeque2 = new LinkedListDeque<>();

        CustomClass obj1 = new CustomClass(42);
        CustomClass obj2 = new CustomClass(42); // 不同实例，相同值

        arrayDeque2.addLast(obj1);
        linkedDeque2.addLast(obj2);

        assertTrue("含有自定义对象的ArrayDeque应该等于LinkedListDeque", arrayDeque2.equals(linkedDeque2));
        assertTrue("含有自定义对象的LinkedListDeque应该等于ArrayDeque", linkedDeque2.equals(arrayDeque2));

        // 测试混合类型
        class SubClass extends CustomClass {
            public SubClass(int value) {
                super(value);
            }

            @Override
            public boolean equals(Object o) {
                return super.equals(o);
            }
        }

        ArrayDeque<CustomClass> arrayDeque3 = new ArrayDeque<>();
        LinkedListDeque<CustomClass> linkedDeque3 = new LinkedListDeque<>();

        arrayDeque3.addLast(new CustomClass(99));
        linkedDeque3.addLast(new SubClass(99)); // 不同类，但equals相等

        assertTrue("含有继承类对象的ArrayDeque应该等于LinkedListDeque", arrayDeque3.equals(linkedDeque3));
        assertTrue("含有继承类对象的LinkedListDeque应该等于ArrayDeque", linkedDeque3.equals(arrayDeque3));

        // 测试异构集合
        ArrayDeque<Object> arrayDeque4 = new ArrayDeque<>();
        LinkedListDeque<Object> linkedDeque4 = new LinkedListDeque<>();

        arrayDeque4.addLast("string");
        arrayDeque4.addLast(123);
        arrayDeque4.addLast(new CustomClass(42));

        linkedDeque4.addLast("string");
        linkedDeque4.addLast(123);
        linkedDeque4.addLast(new CustomClass(42));

        assertTrue("含有混合类型的ArrayDeque应该等于LinkedListDeque", arrayDeque4.equals(linkedDeque4));
        assertTrue("含有混合类型的LinkedListDeque应该等于ArrayDeque", linkedDeque4.equals(arrayDeque4));
    }

    @Test
    public void testEdgeCases2() {
        // 空队列测试
        ArrayDeque<Integer> emptyArray = new ArrayDeque<>();
        LinkedListDeque<Integer> emptyLinked = new LinkedListDeque<>();

        assertTrue("空的ArrayDeque应该等于空的LinkedListDeque", emptyArray.equals(emptyLinked));
        assertTrue("空的LinkedListDeque应该等于空的ArrayDeque", emptyLinked.equals(emptyArray));

        // 只有一个元素的队列
        ArrayDeque<Integer> singleArray = new ArrayDeque<>();
        LinkedListDeque<Integer> singleLinked = new LinkedListDeque<>();

        singleArray.addFirst(42);
        singleLinked.addFirst(42);

        assertTrue("单元素ArrayDeque应该等于单元素LinkedListDeque", singleArray.equals(singleLinked));
        assertTrue("单元素LinkedListDeque应该等于单元素ArrayDeque", singleLinked.equals(singleArray));

        // 一个包含null的队列
        ArrayDeque<Integer> nullArray = new ArrayDeque<>();
        LinkedListDeque<Integer> nullLinked = new LinkedListDeque<>();

        nullArray.addFirst(null);
        nullLinked.addFirst(null);

        assertTrue("含null的ArrayDeque应该等于含null的LinkedListDeque", nullArray.equals(nullLinked));
        assertTrue("含null的LinkedListDeque应该等于含null的ArrayDeque", nullLinked.equals(nullArray));
    }

    @Test
    public void testCircularArrayWithFrontRear() {
        // 测试循环数组的情况，front和rear在不同位置
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();
        LinkedListDeque<Integer> linkedListDeque = new LinkedListDeque<>();

        // 添加元素使ArrayDeque的内部数组发生循环
        for (int i = 0; i < 10; i++) {
            arrayDeque.addLast(i);
        }

        // 删除一些前端元素，改变front的位置
        for (int i = 0; i < 5; i++) {
            arrayDeque.removeFirst();
        }

        // 添加一些元素，使rear移动
        for (int i = 10; i < 15; i++) {
            arrayDeque.addLast(i);
        }

        // LinkedListDeque添加相同的元素
        for (int i = 5; i < 15; i++) {
            linkedListDeque.addLast(i);
        }

        assertEquals("循环数组结构的ArrayDeque应该与LinkedListDeque相等", arrayDeque, linkedListDeque);
        assertEquals("LinkedListDeque应该与循环数组结构的ArrayDeque相等", linkedListDeque, arrayDeque);
    }

    @Test
    public void testResize() {
        // 测试ArrayDeque在调整大小后的情况
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();
        LinkedListDeque<Integer> linkedListDeque = new LinkedListDeque<>();

        // 添加足够多的元素触发ArrayDeque的扩容
        for (int i = 0; i < 20; i++) {
            arrayDeque.addLast(i);
        }

        // 删除一些元素触发ArrayDeque的缩容
        for (int i = 0; i < 15; i++) {
            arrayDeque.removeFirst();
        }

        // 添加一些新元素
        for (int i = 20; i < 25; i++) {
            arrayDeque.addLast(i);
        }

        // LinkedListDeque添加相同的元素
        for (int i = 15; i < 25; i++) {
            linkedListDeque.addLast(i);
        }

        assertEquals("经过扩容和缩容的ArrayDeque应该与LinkedListDeque相等", arrayDeque, linkedListDeque);
        assertEquals("LinkedListDeque应该与经过扩容和缩容的ArrayDeque相等", linkedListDeque, arrayDeque);
    }

    @Test
    public void testManyOperations() {
        // 测试经过多次操作后的情况
        ArrayDeque<String> arrayDeque = new ArrayDeque<>();
        LinkedListDeque<String> linkedListDeque = new LinkedListDeque<>();

        // 添加元素
        arrayDeque.addFirst("A");
        arrayDeque.addLast("B");
        arrayDeque.addFirst("C");
        arrayDeque.addLast("D");

        // 删除元素
        arrayDeque.removeFirst();
        arrayDeque.removeLast();

        // 再添加元素
        arrayDeque.addFirst("E");
        arrayDeque.addLast("F");

        // LinkedListDeque执行相同的操作
        linkedListDeque.addFirst("A");
        linkedListDeque.addLast("B");
        linkedListDeque.addFirst("C");
        linkedListDeque.addLast("D");
        linkedListDeque.removeFirst();
        linkedListDeque.removeLast();
        linkedListDeque.addFirst("E");
        linkedListDeque.addLast("F");

        assertEquals("经过多次操作后的ArrayDeque应该与LinkedListDeque相等", arrayDeque, linkedListDeque);
        assertEquals("经过多次操作后的LinkedListDeque应该与ArrayDeque相等", linkedListDeque, arrayDeque);
    }

    @Test
    public void testWithCustomObjectEquals() {
        // 测试自定义对象的equals()的情况
        class CustomObject {
            private final String value;

            public CustomObject(String value) {
                this.value = value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                CustomObject that = (CustomObject) o;
                return value.equals(that.value);
            }
        }

        ArrayDeque<CustomObject> arrayDeque = new ArrayDeque<>();
        LinkedListDeque<CustomObject> linkedListDeque = new LinkedListDeque<>();

        arrayDeque.addLast(new CustomObject("test1"));
        arrayDeque.addLast(new CustomObject("test2"));

        linkedListDeque.addLast(new CustomObject("test1"));
        linkedListDeque.addLast(new CustomObject("test2"));

        assertEquals("包含自定义对象的ArrayDeque应该与LinkedListDeque相等", arrayDeque, linkedListDeque);
        assertEquals("包含自定义对象的LinkedListDeque应该与ArrayDeque相等", linkedListDeque, arrayDeque);
    }

    @Test
    public void testDifferentIteratorImplementations() {
        // 测试迭代器实现不同的情况

        // 创建一个有自定义迭代器的模拟LinkedListDeque
        class MockLinkedListDeque<T> implements Deque<T>, Iterable<T> {
            private LinkedListDeque<T> delegate = new LinkedListDeque<>();

            @Override
            public void addFirst(T item) { delegate.addFirst(item); }

            @Override
            public void addLast(T item) { delegate.addLast(item); }

            @Override
            public int size() { return delegate.size(); }

            @Override
            public void printDeque() { delegate.printDeque(); }

            @Override
            public T removeFirst() { return delegate.removeFirst(); }

            @Override
            public T removeLast() { return delegate.removeLast(); }

            @Override
            public T get(int index) { return delegate.get(index); }

            @Override
            public java.util.Iterator<T> iterator() {
                // 返回一个不同实现的迭代器，但顺序相同
                return new java.util.Iterator<T>() {
                    private java.util.Iterator<T> it = delegate.iterator();

                    @Override
                    public boolean hasNext() { return it.hasNext(); }

                    @Override
                    public T next() { return it.next(); }
                };
            }
        }

        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();
        MockLinkedListDeque<Integer> mockDeque = new MockLinkedListDeque<>();

        for (int i = 0; i < 5; i++) {
            arrayDeque.addLast(i);
            mockDeque.addLast(i);
        }

        // 这个测试检查ArrayDeque的equals()是否能正确处理不同的迭代器实现
        assertEquals("ArrayDeque应该与具有不同迭代器实现的Deque相等", arrayDeque, mockDeque);
    }

    @Test
    public void testNonDequeObject() {
        // 测试与非Deque对象比较的情况
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();
        arrayDeque.addLast(1);

        Object nonDequeObject = new Object();

        assertFalse("ArrayDeque不应该等于非Deque对象", arrayDeque.equals(nonDequeObject));
    }
}
