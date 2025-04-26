package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import net.sf.saxon.om.Item;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE


    @Test
    public  void testThreeAddThreeRemove(){
        BuggyAList<Integer> list_complex = new BuggyAList<>();
        AListNoResizing<Integer> list_simple = new AListNoResizing<>();
        list_simple.addLast(1);
        list_simple.addLast(4);
        list_simple.addLast(7);

        list_complex.addLast(1);
        list_complex.addLast(4);
        list_complex.addLast(7);

        assertEquals(list_complex.size(), list_complex.size());

        assertEquals(list_complex.removeLast(), list_simple.removeLast());
        assertEquals(list_complex.size(), list_complex.size());

        assertEquals(list_complex.removeLast(), list_simple.removeLast());
        assertEquals(list_complex.size(), list_complex.size());

        assertEquals(list_complex.removeLast(), list_simple.removeLast());
        assertEquals(list_complex.size(), list_complex.size());

    }

    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();

        int N = 500;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            }
            else if (L.size()<1) {
                continue;
            }
            else if (operationNumber == 1) {
                int last_num = L.getLast();
                System.out.println("getLast() = " + last_num);
            }
            else if (operationNumber == 2) {
                L.removeLast();
                System.out.println("removeLast()"+"   size = " + L.size());
            }
        }
    }

    @Test
    public void randomizedTest2(){
        AListNoResizing<Integer> list_simple = new AListNoResizing<>();
        BuggyAList<Integer> list_complex = new BuggyAList<>();
        int N=50000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                int randVal = StdRandom.uniform(0, 100);
                list_simple.addLast(randVal);
                list_complex.addLast(randVal);
                assertEquals(list_simple.size(), list_complex.size());
            }
            else if (list_simple.size()<1) {
                continue;
            }
            else if (operationNumber == 1) {
                assertEquals(list_simple.getLast(), list_complex.getLast());
            }
            else if (operationNumber == 2) {
                assertEquals(list_simple.removeLast(), list_complex.removeLast());
                assertEquals(list_simple.size(), list_complex.size());
            }
        }
    }
}
