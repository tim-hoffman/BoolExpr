package boolexpr.test;

import boolexpr.util.EnumBuster;
import java.util.Arrays;

/**
 *
 * @author Timothy
 */
public class Main {

    public static void main(String[] args) {
        fun2();
        fun1();
    }

    private static String stringifyBlock(BLOCK input) {
        return input.toString();
    }

    private static void fun2() {
        System.out.println("\nfun2");
        EnumBuster<BLOCK> blockBuster = new EnumBuster<>(BLOCK.class);

        System.out.println(stringifyBlock(BLOCK.B0));
        System.out.println(stringifyBlock(BLOCK.B63));

        for (int i = 64; i < 100; i++) {
            BLOCK newBlock = blockBuster.make("B" + i);
            blockBuster.addByValue(newBlock);
        }

        System.out.println(Arrays.toString(BLOCK.values()));
        System.out.println(BLOCK.values().length);

        System.out.println(stringifyBlock(BLOCK.valueOf("B99")));

        try {
            System.out.println(stringifyBlock(BLOCK.valueOf("B100")));
        } catch (java.lang.IllegalArgumentException ex) {
            System.out.println(ex);
        }

        System.out.println("Restoring E");
        blockBuster.restore();
        System.out.println(Arrays.toString(BLOCK.values()));
    }

    private static void fun1() {
        System.out.println("\nfun1");
        EnumBuster<BLOCK> blockBuster = new EnumBuster<>(BLOCK.class);

        //test predefined lower bound
        //    assertEquals("B0", stringifyBlock(BLOCK.B0));
        //     assertEquals("B0", stringifyBlock(BLOCK.valueOf("B0")));
        //test predefined upper bound
        //  assertEquals("B63", stringifyBlock(BLOCK.B63));
        ////   assertEquals("B63", stringifyBlock(BLOCK.valueOf("B63")));
        //test beyond upper bound
        try {
            BLOCK.valueOf("B64");
            //      fail("upper bound too high");   //This means the test case itself is broken
        } catch (java.lang.IllegalArgumentException ex) {

        }

        //keep original list of values
        BLOCK[] originalValues = BLOCK.values();

        //add some new blocks
        for (int i = 64; i < 100; i++) {
            BLOCK newBlock = blockBuster.make("B" + i);
            blockBuster.addByValue(newBlock);
        }

        BLOCK[] newValues = BLOCK.values();
        // assertFalse(originalValues.length == newValues.length);

        //test if all blocks beyond original have been added
        int missingBlockCount = 0;
        for (int i = 64; i < 100; i++) {
            try {
                BLOCK.valueOf("B" + i);
            } catch (java.lang.IllegalArgumentException ex) {
                System.out.println(ex);
                missingBlockCount++;
            }
        }
        System.out.println("# missing blocks: " + missingBlockCount);
        // assertEquals(0, missingBlockCount);

        //test restoring
        System.out.println("Restoring E");
        blockBuster.restore();
        // assertArrayEquals(originalValues, BLOCK.values());
    }
}
