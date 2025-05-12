package bstmap;

import static org.junit.Assert.*;

import jh61b.junit.In;
import org.junit.Test;

/** Tests by Brendan Hu, Spring 2015, revised for 2016 by Josh Hug */
public class TestBSTMap {

  	@Test
    public void sanityGenericsTest() {
    	try {
    		BSTMap<String, String> a = new BSTMap<String, String>();
	    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
	    	BSTMap<Integer, String> c = new BSTMap<Integer, String>();
	    	BSTMap<Boolean, Integer> e = new BSTMap<Boolean, Integer>();
	    } catch (Exception e) {
	    	fail();
	    }
    }

    //assumes put/size/containsKey/get work
    @Test
    public void sanityClearTest() {
    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        for (int i = 0; i < 455; i++) {
            b.put("hi" + i, 1+i);
            //make sure put is working via containsKey and get
            assertTrue( null != b.get("hi" + i) && (b.get("hi"+i).equals(1+i))
                        && b.containsKey("hi" + i));
        }
        assertEquals(455, b.size());
        b.clear();
        assertEquals(0, b.size());
        for (int i = 0; i < 455; i++) {
            assertTrue(null == b.get("hi" + i) && !b.containsKey("hi" + i));
        }
    }

    // assumes put works
    @Test
    public void sanityContainsKeyTest() {
    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        assertFalse(b.containsKey("waterYouDoingHere"));
        b.put("waterYouDoingHere", 0);
        assertTrue(b.containsKey("waterYouDoingHere"));
    }

    // assumes put works
    @Test
    public void sanityGetTest() {
    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        assertEquals(null,b.get("starChild"));
        assertEquals(0, b.size());
        b.put("starChild", 5);
        assertTrue(((Integer) b.get("starChild")).equals(5));
        b.put("KISS", 5);
        assertTrue(((Integer) b.get("KISS")).equals(5));
        assertNotEquals(null,b.get("starChild"));
        assertEquals(2, b.size());
    }

    // assumes put works
    @Test
    public void sanitySizeTest() {
    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        assertEquals(0, b.size());
        b.put("hi", 1);
        assertEquals(1, b.size());
        for (int i = 0; i < 455; i++)
            b.put("hi" + i, 1);
        assertEquals(456, b.size());
    }

    //assumes get/containskey work
    @Test
    public void sanityPutTest() {
    	BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        b.put("hi", 1);
        assertTrue(b.containsKey("hi") && b.get("hi") != null);
    }

    //assumes put works
    @Test
    public void containsKeyNullTest() {
        BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        b.put("hi", null);
        assertTrue(b.containsKey("hi"));
    }

    @Test
    public void printInOrderTest() {
          BSTMap<String, Integer> b = new BSTMap<>();
          b.put("ha", 1);
          b.put("ge", -1);
          b.put("ga", 1);
          b.put("gb", 1);
          b.put("hb", 2);
          b.put("he", 5);
          b.put("hf", 6);
          b.put("hc", 3);
          b.put("hd", 4);
          b.printInOrder();
    }

    @Test
    public void removeOnlyRootTest() {
          BSTMap<String, Integer> b = new BSTMap<>();
          b.put("hi", 12);
          b.put("ge", -1);
          b.put("ga", 1);
          b.put("gb", 1);
          b.put("hz", 2);
          b.put("he", 5);
          b.printInOrder();
          assertTrue("希望此时Map中还有hi",b.containsKey("hi"));
          int re = b.remove("hi");
          assertEquals("移除he，希望是12，实际是"+re,12,re);
          System.out.println();
          b.printInOrder();
          assertFalse("希望此时Map中没有hi",b.containsKey("hi"));
    }
    @Test
    public void removeNoChildTest() {
        BSTMap<String, Integer> b = new BSTMap<>();
        b.put("hi", 12);
        b.put("ge", -1);
        b.put("ga", 1);
        b.put("gb", 1);
        b.put("hz", 2);
        b.put("he", 5);
        b.printInOrder();
        assertTrue("希望此时Map中还有he",b.containsKey("he"));
        int re = b.remove("he");
        assertEquals("移除he，希望是5，实际是"+re,5,re);
        System.out.println();
        b.printInOrder();
        assertFalse("希望此时Map中没有he",b.containsKey("he"));

        assertTrue("希望此时Map中还有hi",b.containsKey("hi"));
        re = b.remove("hi");
        assertEquals("移除he，希望是12，实际是"+re,12,re);
        System.out.println();
        b.printInOrder();
        assertFalse("希望此时Map中没有hi",b.containsKey("hi"));
    }
    @Test
    public void testRemoveComprehensive() {
        BSTMap<Integer, String> b = new BSTMap<>();

        // 构建树结构:
        //        10
        //       /  \
        //      5    15
        //     / \   / \
        //    3   7 12  20
        //   /       \
        //  1        13

        b.put(10, "十");
        b.put(5, "五");
        b.put(15, "十五");
        b.put(3, "三");
        b.put(7, "七");
        b.put(12, "十二");
        b.put(20, "二十");
        b.put(1, "一");
        b.put(13, "十三");

        assertEquals(9, b.size());

        // 测试1: 删除叶子节点
        String result1 = b.remove(1);
        assertEquals("一", result1);
        assertFalse(b.containsKey(1));
        assertEquals(8, b.size());

        // 测试2: 删除只有一个子节点的节点
        String result2 = b.remove(3);
        assertEquals("三", result2);
        assertFalse(b.containsKey(3));
        assertTrue(b.containsKey(5)); // 确保父节点仍然存在
        assertEquals(7, b.size());

        // 测试3: 删除有两个子节点的节点
        String result3 = b.remove(15);
        assertEquals("十五", result3);
        assertFalse(b.containsKey(15));
        assertTrue(b.containsKey(12)); // 确保子节点被保留
        assertTrue(b.containsKey(20)); // 确保子节点被保留
        assertEquals(6, b.size());

        // 测试4: 删除根节点
        String result4 = b.remove(10);
        assertEquals("十", result4);
        assertFalse(b.containsKey(10));
        assertEquals(5, b.size());

        // 测试5: 删除不存在的键
        String result5 = b.remove(100);
        assertNull(result5);
        assertEquals(5, b.size());

        // 测试6: 连续删除所有节点
        b.remove(5);
        b.remove(7);
        b.remove(12);
        b.remove(13);
        b.remove(20);
        assertEquals(0, b.size());
    }
}
