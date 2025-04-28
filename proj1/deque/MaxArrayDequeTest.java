package deque;

import com.sun.tools.doclets.standard.Standard;
import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;

public class MaxArrayDequeTest {
    @Test
    public void test_int() {
        MaxArrayDeque<Integer> que=new MaxArrayDeque<>(comp_int_1);
        int max_1,max_2;
        int N=10000;
        int start_n= StdRandom.uniform(-N,N);
        max_1=max_2=start_n;
        que.addFirst(start_n);
        for(int i=0;i<N;i++){
            int ops=StdRandom.uniform(0,4);
            int num=StdRandom.uniform(-N,N);
            if (ops==0){
                que.addFirst(num);
                max_1=((comp_int_1.compare(num,max_1))>0?num:max_1);
                max_2=((comp_int_2.compare(num,max_2)>0)?num:max_2);
            }
            else if (ops==1){
                que.addLast(num);
                max_1=((comp_int_1.compare(num,max_1))>0?num:max_1);
                max_2=((comp_int_2.compare(num,max_2)>0)?num:max_2);
            }
            else if (ops==2){
                assertEquals("希望在comp_int_1比较下最大值为"+max_1+"实际为"+que.max(),max_1,(int)que.max());
            }
            else if (ops==3){
                assertEquals("希望在comp_int_2比较下最大值为"+max_2+"实际为"+que.max(comp_int_2),max_2,(int)que.max(comp_int_2));
            }
        }
    }



    Comparator<Integer> comp_int_1 = new Comparator<>(){
        @Override
        public int compare(Integer o1, Integer o2) {
            if(o1*o1 > o2*o2)
                return 1;
            else if (o1*o1 < o2*o2)
                return -1;
            return 0;
        }
    };
    Comparator<Integer> comp_int_2 = new Comparator<>(){
        @Override
        public int compare(Integer o1, Integer o2) {
            if(Math.abs(o1) > Math.abs(o2))
                return 1;
            else if (Math.abs(o1) < Math.abs(o2))
                return -1;
            return 0;
        }
    };
}
