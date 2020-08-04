package boolexpr;

/*-
 * #%L
 * BoolExpr
 * %%
 * Copyright (C) 2020 Timothy Hoffman
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.EnumSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import boolexpr.test.BLOCK;

/**
 *
 * @author Timothy Hoffman
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
