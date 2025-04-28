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
        for (int i = 0; i < N; i++){
            int ops = StdRandom.uniform(0, 7);
            int num = StdRandom.uniform(0, N);
            if (ops == 0) {
                lld1.addFirst(num);
                lld2.addFirst(num);
            } else if (ops == 1) {
                lld1.addLast(num);
                lld2.addLast(num);
            } else if (ops == 2) {
                assertEquals(lld1.removeFirst(),lld2.removeFirst());
            } else if (ops == 3) {
                assertEquals(lld1.removeLast(),lld2.removeLast());
            } else if (ops == 4) {
                assertEquals(lld1.get(num),lld2.get(num));
            } else if (ops == 5) {
                assertTrue(lld2.equals(lld1));
            } else if (ops == 6) {
                assertTrue(lld1.equals(lld2));
            }
        }
    }
}
