package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class LinkedListDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {


        LinkedListDeque<String> lld1 = new LinkedListDeque<String>();

		assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
		lld1.addFirst("front");

		// The && operator is the same as "and" in Python.
		// It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

		lld1.addLast("middle");
		assertEquals(2, lld1.size());

		lld1.addLast("back");
		assertEquals(3, lld1.size());

		System.out.println("Printing out deque: ");
		lld1.printDeque();

    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {


        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
		// should be empty
		assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

		lld1.addFirst(10);
		// should not be empty
		assertFalse("lld1 should contain 1 item", lld1.isEmpty());

		lld1.removeFirst();
		// should be empty
		assertTrue("lld1 should be empty after removal", lld1.isEmpty());

    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {


        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);

    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {


        LinkedListDeque<String>  lld1 = new LinkedListDeque<String>();
        LinkedListDeque<Double>  lld2 = new LinkedListDeque<Double>();
        LinkedListDeque<Boolean> lld3 = new LinkedListDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();

    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {


        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());


    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {


        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }


    }


    @Test
    /*
      测试addFirst,addLast
         get,getRecursive
     这几个函数
     数据类型为int型
     */
    public void my_test_random_int() {
        int N=1000;
        int[] array_first = new int[N],array_last = new int[N];
        int first=0,last=N-1;
        LinkedListDeque<Integer> que_first =new LinkedListDeque<>();
        LinkedListDeque<Integer> que_last =new LinkedListDeque<>();
        //array中与que中添加N个随即元素
        for(int i=0;i<N;i++){
            int num=StdRandom.uniform(0,N);
            array_first[first]=num;
            first++;
            que_last.addLast(num);
            array_last[last]=num;
            last--;
            que_first.addFirst(num);
        }
        for(int i=0;i<N;i++){
            int num=StdRandom.uniform(0,N);
            assertEquals(array_first[num],(int)que_last.get(num));
            assertEquals(array_first[num],(int)que_last.get(num));
            assertEquals(array_last[num],(int)que_first.getRecursive(num));
            assertEquals(array_last[num],(int)que_first.getRecursive(num));
        }
    }
    @Test
    /*
      测试addFirst,addLast
         get,getRecursive
     这几个函数
     数据类型为double型
     */
    public void my_test_random_double() {
        int N=100;
        double delt=0.01;
        double[] array_first = new double[N],array_last = new double[N];
        int first=0,last=N-1;
        LinkedListDeque<Double> que_first =new LinkedListDeque<>();
        LinkedListDeque<Double> que_last =new LinkedListDeque<>();
        //array中与que中添加N个随即元素
        for(int i=0;i<N;i++){
            double num=StdRandom.uniform(0,N*1.0);
            array_first[first]=num;
            first++;
            que_last.addLast(num);
            array_last[last]=num;
            last--;
            que_first.addFirst(num);
        }
        for(int i=0;i<N;i++){
            int num=StdRandom.uniform(0,N);
            assertEquals(array_first[num],(double)que_last.get(num),delt);
            assertEquals(array_first[num],(double)que_last.get(num),delt);
            assertEquals(array_last[num],(double)que_first.getRecursive(num),delt);
            assertEquals(array_last[num],(double)que_first.getRecursive(num),delt);
        }
    }
    @Test
    /*以下三个测试了equals,isEmpty,size三个函数，分别使用了int,double,string三种进行测试*/
    public void my_test_equals_int(){
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        LinkedListDeque<Integer> lld2 = new LinkedListDeque<>();
        int N=100;
        for(int i=0;i<N;i++){
            int num=StdRandom.uniform(0,N);
            lld1.addLast(num);
            lld2.addLast(num);
            assertTrue(lld1.equals(lld2));
            assertEquals(lld1.size(),lld2.size());
        }
        assertEquals(lld1.size(),N);

        lld2.removeFirst();
        assertFalse(lld1.equals(lld2));
        assertEquals(lld1.size(),lld2.size()+1);
        lld2.addFirst(lld1.get(0)+1);
        assertEquals(lld1.size(),lld2.size());
        assertFalse(lld1.equals(lld2));
    }
    @Test
    public void my_test_equals_double(){
        LinkedListDeque<Double> lld1 = new LinkedListDeque<>();
        LinkedListDeque<Double> lld2 = new LinkedListDeque<>();
        int N=100;
        assertTrue(lld1.isEmpty());
        for(int i=0;i<N;i++){
            double num=StdRandom.uniform(0,N*1.0);
            lld1.addLast(num);
            lld2.addLast(num);
            assertTrue(lld1.equals(lld2));
            assertEquals(lld1.size(),lld2.size());
        }
        lld2.removeFirst();
        assertEquals(lld1.size(),lld2.size()+1);
        assertFalse(lld1.equals(lld2));
        lld2.addFirst(lld1.get(0)+1);
        assertEquals(lld1.size(),lld2.size());
        assertFalse(lld1.equals(lld2));

        assertFalse(lld1.isEmpty());
    }
    @Test
    public void my_test_equals_string(){
        LinkedListDeque<String> lld1 = new LinkedListDeque<>();
        LinkedListDeque<String> lld2 = new LinkedListDeque<>();
        String[] all=new String[]{"first","second","third","fourth","fifth","sixth","seventh","eighth","ninth"};
        assertTrue(lld1.isEmpty());
        for(int i=0;i<all.length;i++){
            lld1.addLast(all[i]);
            lld2.addLast(all[i]);
            assertTrue(lld1.equals(lld2));
            assertEquals(lld1.size(),lld2.size());
        }
        lld2.removeFirst();
        assertFalse(lld1.equals(lld2));
        assertEquals(lld1.size(),lld2.size()+1);
        lld2.addFirst(lld1.get(0)+"1");
        assertFalse(lld1.equals(lld2));
        assertEquals(lld1.size(),lld2.size());

        assertFalse(lld1.isEmpty());
    }

    @Test
    /* 使用两个LinkedListDeque进行对照的操作，测试下面几个函数（随机的）
    addFirst,addLast
    removeLast,removeFirst
    get,getRecursive
    equals
    然后，其中的部分函数，如iterator，在其它函数中使用了，相当于已经测试了
     */
    public void my_random_test_all_int() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        LinkedListDeque<Integer> lld2 = new LinkedListDeque<>();
        int N=1000;
        for(int i=0;i<N;i++){
            int ops=StdRandom.uniform(0,7);
            int num=StdRandom.uniform(0,N);
            if(ops==0){
                lld1.addFirst(num);
                lld2.addFirst(num);
            }
            else if(ops==1){
                lld1.addLast(num);
                lld2.addLast(num);
            }
            else if(ops==2){
                assertEquals(lld1.removeFirst(),lld2.removeFirst());
            }
            else if(ops==3){
                assertEquals(lld1.removeLast(),lld2.removeLast());
            }
            else if(ops==4){
                assertEquals(lld1.get(num),lld2.get(num));
            }
            else if(ops==5){
                assertEquals(lld1.getRecursive(num),lld2.getRecursive(num));
            }
            else if(ops==6){
                assertTrue(lld1.equals(lld2));
            }
        }
    }
    public void my_random_test_all_string() {
        LinkedListDeque<String> lld1 = new LinkedListDeque<>();
        LinkedListDeque<String> lld2 = new LinkedListDeque<>();
        String[]all= {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
                "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "first", "second", "third", "fourth", "fifth", "sixth",
                "seventh", "eighth", "ninth", "tenth", "eleventh", "twelfth",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23", "24", "25", "26", "27", "28","29",
                "30", "31", "32", "33", "34", "35", "36", "37", "38","39",
                "40", "41", "42", "43", "44", "45", "46", "47", "48", "49"
        };
        int N=all.length;
        for(int i=0;i<N*10;i++){
            int ops=StdRandom.uniform(0,7);
            int num=StdRandom.uniform(0,N);
            String str=all[num];
            if(ops==0){
                lld1.addFirst(str);
                lld2.addFirst(str);
            }
            else if(ops==1){
                lld1.addLast(str);
                lld2.addLast(str);
            }
            else if(ops==2){
                assertEquals(lld1.removeFirst(),lld2.removeFirst());
            }
            else if(ops==3){
                assertEquals(lld1.removeLast(),lld2.removeLast());
            }
            else if(ops==4){
                assertEquals(lld1.get(num),lld2.get(num));
            }
            else if(ops==5){
                assertEquals(lld1.getRecursive(num),lld2.getRecursive(num));
            }
            else if(ops==6){
                assertTrue(lld1.equals(lld2));
            }
        }
    }
}
