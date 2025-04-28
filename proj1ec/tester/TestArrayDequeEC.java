package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.introcs.StdRandom;
import jh61b.junit.In;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void testWithNoMessage() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> happy = new ArrayDequeSolution<>();
        int N = 1000;
        for(int i = 0; i < N; i++) {
            int ops = StdRandom.uniform(0,3);
            if(ops == 0) {
                sad.addFirst(i);
                happy.addFirst(i);
            }
            else if(ops == 1) {
                sad.addLast(i);
                happy.addLast(i);
            }
            else if (sad.isEmpty()||happy.isEmpty()) {
                continue;
            }
            else if(ops == 2) {
                int sadFirst = sad.removeFirst();
                int happyFirst = happy.removeFirst();
                assertEquals(sadFirst,happyFirst);
            }
            else if(ops == 3) {
                int sadLast = sad.removeLast();
                int happyLast = happy.removeLast();
                assertEquals(sadLast,happyLast);
            }
            int sadSize = sad.size();
            int happySize = happy.size();
            assertEquals(sadSize,happySize);
        }

        for(int i = 0;i < N;i++) {
            int num = StdRandom.uniform(0,sad.size());
            int sadNum = sad.get(num);
            int happyNum = happy.get(num);
            assertEquals(sadNum,happyNum);
        }
    }
    @Test
    public void testWithMessage() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> happy = new ArrayDequeSolution<>();
        int N = 1000;
        String msg = "";
        for(int i = 0; i < N; i++) {
            int ops = StdRandom.uniform(0,4);
            if(ops == 0) {
                sad.addFirst(i);
                happy.addFirst(i);
                msg += "addFirst("+i+")\n";
            }
            else if(ops == 1) {
                sad.addLast(i);
                happy.addLast(i);
                msg += "addLast("+i+")\n";
            }
            else if (sad.isEmpty()||happy.isEmpty()) {
                continue;
            }
            else if(ops == 2) {
                int sadFirst = sad.removeFirst();
                int happyFirst = happy.removeFirst();
                msg += "removeFirst()\n";
                assertEquals(msg,sadFirst,happyFirst);
            }
            else if(ops == 3) {
                int sadLast = sad.removeLast();
                int happyLast = happy.removeLast();
                msg += "removeLast()\n";
                assertEquals(msg,happyLast ,sadLast);
            }
            //这个就不用了，会显得十分冗余
//            int sadSize = sad.size();
//            int happySize = happy.size();
//            msg += "size()\n";
//            assertEquals(msg,happySize, sadSize);
        }


    }

}
