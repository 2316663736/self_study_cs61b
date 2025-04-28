package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.introcs.StdRandom;
import jh61b.junit.In;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void test_with_no_message() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> happy = new ArrayDequeSolution<>();
        int N=1000;
        for(int i=0;i<N;i++) {
            int ops= StdRandom.uniform(0,3);
            if(ops==0) {
                sad.addFirst(i);
                happy.addFirst(i);
            }
            else if(ops==1) {
                sad.addLast(i);
                happy.addLast(i);
            }
            else if (sad.isEmpty()||happy.isEmpty()) {
                continue;
            }
            else if(ops==2) {
                int sad_first=sad.removeFirst();
                int happy_first=happy.removeFirst();
                assertEquals(sad_first,happy_first);
            }
            else if(ops==3) {
                int sad_last=sad.removeLast();
                int happy_last=happy.removeLast();
                assertEquals(sad_last,happy_last);
            }
            int sad_size = sad.size();
            int happy_size = happy.size();
            assertEquals(sad_size,happy_size);
        }

        for(int i=0;i<N;i++) {
            int num= StdRandom.uniform(0,sad.size());
            int sad_num= sad.get(num);
            int happy_num= happy.get(num);
            assertEquals(sad_num,happy_num);
        }
    }
    @Test
    public void test_with_message() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> happy = new ArrayDequeSolution<>();
        int N=1000;
        String msg="\n";
        for(int i=0;i<N;i++) {
            int ops= StdRandom.uniform(0,3);
            if(ops==0) {
                sad.addFirst(i);
                happy.addFirst(i);
                msg+="addFirst("+i+")\n";
            }
            else if(ops==1) {
                sad.addLast(i);
                happy.addLast(i);
                msg+="addLast("+i+")\n";
            }
            else if (sad.isEmpty()||happy.isEmpty()) {
                continue;
            }
            else if(ops==2) {
                int sad_first=sad.removeFirst();
                int happy_first=happy.removeFirst();
                msg+="removeFirst()\n";
                assertEquals(msg,sad_first,happy_first);
            }
            else if(ops==3) {
                int sad_last=sad.removeLast();
                int happy_last=happy.removeLast();
                msg+="removeLast()\n";
                assertEquals(msg, sad_last,happy_last);
            }
            //这个就不用了，会显得十分冗余
//            int sad_size = sad.size();
//            int happy_size = happy.size();
//            msg+="size()\n";
//            assertEquals(msg, sad_size,happy_size);
        }

        for(int i=0;i<N;i++) {
            int num= StdRandom.uniform(0,sad.size());
            int sad_num= sad.get(num);
            int happy_num= happy.get(num);
            msg+="get("+num+")\n";
            assertEquals(msg, sad_num,happy_num);
        }
    }

}
