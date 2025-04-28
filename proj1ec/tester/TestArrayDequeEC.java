package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import student.StudentArrayDeque;
import edu.princeton.cs.introcs.StdRandom;

public class TestArrayDequeEC {

    @Test
    public void randomizedTest() {
        StudentArrayDeque<Integer> studentDeque = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> solutionDeque = new ArrayDequeSolution<>();

        // 构建操作日志用于失败信息
        StringBuilder operationLog = new StringBuilder();

        int numOperations = 1000;
        for (int i = 0; i < numOperations; i++) {
            // 生成随机操作码：0-3
            // 0: addFirst, 1: addLast, 2: removeFirst, 3: removeLast
            int operationCode = StdRandom.uniform(0, 4);

            // 如果执行添加操作，需要添加的值
            Integer value = StdRandom.uniform(0, 100);

            switch (operationCode) {
                case 0:   // addFirst
                    studentDeque.addFirst(value);
                    solutionDeque.addFirst(value);
                    operationLog.append("addFirst(").append(value).append(")\n");
                    break;

                case 1:   // addLast
                    studentDeque.addLast(value);
                    solutionDeque.addLast(value);
                    operationLog.append("addLast(").append(value).append(")\n");
                    break;

                case 2:   // removeFirst - 仅在非空时执行
                    if (!studentDeque.isEmpty() && !solutionDeque.isEmpty()) {
                        Integer studentResult = studentDeque.removeFirst();
                        Integer solutionResult = solutionDeque.removeFirst();
                        operationLog.append("removeFirst()\n");
                        assertEquals(operationLog.toString(), solutionResult, studentResult);
                    }
                    break;

                case 3:   // removeLast - 仅在非空时执行
                    if (!studentDeque.isEmpty() && !solutionDeque.isEmpty()) {
                        Integer studentResult = studentDeque.removeLast();
                        Integer solutionResult = solutionDeque.removeLast();
                        operationLog.append("removeLast()\n");
                        assertEquals(operationLog.toString(), solutionResult, studentResult);
                    }
                    break;
            }

            // 每次操作后检查大小是否匹配
            assertEquals(operationLog.toString() + "size()\n",
                    solutionDeque.size(), studentDeque.size());
        }
    }
}