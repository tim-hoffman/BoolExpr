package boolexpr.util;

import org.junit.*;

/**
 *
 * @author Timothy
 */
public class EnumBusterTest {

    public EnumBusterTest() {
    }

    /**
     * A copy of the {@link boolexpr.test.BLOCK} class that is only used within
     * this test. If the shared version were used instead, this test would fail
     * if executed after some test that calls {@link BLOCK#valueOf(String)}.
     */
    private enum BLOCK {
        B0, B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20, B21, B22, B23, B24, B25, B26, B27, B28, B29, B30, B31, B32, B33, B34, B35, B36, B37, B38, B39, B40, B41, B42, B43, B44, B45, B46, B47, B48, B49, B50, B51, B52, B53, B54, B55, B56, B57, B58, B59, B60, B61, B62, B63;

        @Override
        public String toString() {
            return String.valueOf(ordinal());
        }
    }

    private String stringifyBlock(BLOCK input) {
        return input.toString();
    }

    /**
     * NOTE: must do expansion before {@link BLOCK#valueOf(String)} is ever
     * called or else the expansion doesn't work.
     */
    @Test
    public void enumBusterTest() {
        EnumBuster<BLOCK> blockBuster = new EnumBuster<>(BLOCK.class);

        /*
         *  TESTING :: expansion
         */
        //keep original list of values
        BLOCK[] originalValues = BLOCK.values();
        Assert.assertEquals(64, originalValues.length);
        //add some new blocks
        for (int i = 64; i < 100; i++) {
            BLOCK newBlock = blockBuster.make("B" + i);
            blockBuster.addByValue(newBlock);
            Assert.assertEquals(Integer.toString(i), newBlock.toString());
        }
        BLOCK[] newValues = BLOCK.values();
        Assert.assertEquals(100, newValues.length);
        Assert.assertTrue(originalValues.length != newValues.length);

        //test if all blocks beyond original have been added
        int missingBlockCount = 0;
        for (int i = 0; i < 100; i++) {
            try {
                BLOCK b = BLOCK.valueOf("B" + i);
                Assert.assertEquals(Integer.toString(i), b.toString());
            } catch (IllegalArgumentException ex) {
                System.out.println("[Exception 1] " + ex);
                missingBlockCount++;
            }
        }

        Assert.assertEquals(0, missingBlockCount);

        /*
         *  TESTING :: upper and lower bounds
         */
        //test predefined lower bound
        Assert.assertEquals("0", stringifyBlock(BLOCK.B0));
        Assert.assertEquals("0", stringifyBlock(BLOCK.valueOf("B0")));

        //test predefined upper bound
        Assert.assertEquals("63", stringifyBlock(BLOCK.B63));
        Assert.assertEquals("63", stringifyBlock(BLOCK.valueOf("B63")));

        //test resized upper bound
        Assert.assertEquals("99", stringifyBlock(BLOCK.valueOf("B99")));

        //test beyond resized upper bound
        try {
            BLOCK errBlock = BLOCK.valueOf("B100");//throws exception
            System.out.println(errBlock);
            Assert.fail("upper bound too high");//This means the test case itself is broken
        } catch (IllegalArgumentException ex) {
            System.out.println("[Exception 2] " + ex);
        }

        //test restoring
        blockBuster.restore();
        Assert.assertArrayEquals(originalValues, BLOCK.values());
    }
}
