package boolexpr;

import java.util.EnumSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import boolexpr.test.BLOCK;

/**
 *
 * @author Timothy
 */
public class DisjunctiveNormalFormEnumTest extends NormalFormTestBase<EnumSet<BLOCK>, BLOCK, DisjunctiveNormalFormEnum<BLOCK>> {

    public DisjunctiveNormalFormEnumTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    //called before each test method
    @Before
    public void setUp() {
    }

    //called after each test method
    @After
    public void tearDown() {
    }

    @Override
    protected Construction<EnumSet<BLOCK>, BLOCK, DisjunctiveNormalFormEnum<BLOCK>> getCons() {
        return Construction.DNF_ENUM;
    }
}
