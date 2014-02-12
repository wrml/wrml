/**
 * WRML - Web Resource Modeling Language
 *  __     __   ______   __    __   __
 * /\ \  _ \ \ /\  == \ /\ "-./  \ /\ \
 * \ \ \/ ".\ \\ \  __< \ \ \-./\ \\ \ \____
 *  \ \__/".~\_\\ \_\ \_\\ \_\ \ \_\\ \_____\
 *   \/_/   \/_/ \/_/ /_/ \/_/  \/_/ \/_____/
 *
 * http://www.wrml.org
 *
 * Copyright (C) 2011 - 2013 Mark Masse <mark@wrml.org> (OSS project WRML.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wrml.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wrml.runtime.rest.ApiNavigator;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimplePrefixTreeTest {
    private SimplePrefixTree<String> _Tree;

    private String PATH_1 = "moose/squirrel";

    private String PATH_1_PRE = PrefixTreeBase.DEFAULT_PATH_SEPARATOR + PATH_1;

    private String PATH_1_POST = PATH_1 + PrefixTreeBase.DEFAULT_PATH_SEPARATOR;

    private String PATH_1_PREPOST = PrefixTreeBase.DEFAULT_PATH_SEPARATOR + PATH_1 + PrefixTreeBase.DEFAULT_PATH_SEPARATOR;

    private String PATH_1_VAL = "natasha";

    private String PATH_2 = "*/russian";

    private String PATH_2_VAL = "borris";

    @Before
    public void setUp() {

        _Tree = new SimplePrefixTree<>();
    }

    @After
    public void tearDown() {

        _Tree = null;
    }

    @Test
    public void blankDeepPrint() {

        String rep = _Tree.toString();
        System.out.println("Current represenation is : \n" + rep);
        assertTrue(rep.equals(""));
    }

    @Test
    public void simpleDeepPrint() {

        _Tree.setPathValue(PATH_1, PATH_1_VAL);
        String rep = _Tree.toString();
        System.out.println("Current represenation is : \n" + rep);
        assertTrue(rep.equals(PATH_1 + ApiNavigator.PATH_SEPARATOR + PATH_1_VAL + "\n"));
    }

    @Test
    public void setPath() {

        _Tree.setPathValue(PATH_1, PATH_1_VAL);
    }

    @Test
    public void setPathTwice() {

        _Tree.setPathValue(PATH_1, PATH_1_VAL);
        _Tree.setPathValue(PATH_1, PATH_2_VAL);

        String val = _Tree.getPathValue(PATH_1);
        assertEquals(val, PATH_2_VAL);
    }

    /*
     * SimplePrefixTree just interprets this as a segment.
     */
    public void setPathNonTerminalWild() {

        _Tree.setPathValue(PATH_2, PATH_2_VAL);
    }

    @Test
    public void addAndMatch() {

        setPath();

        String match = _Tree.getPathValue(PATH_1);

        assertTrue(match != null);
        assertTrue(match.equals(PATH_1_VAL));
    }

    @Test
    public void addAndMatchCapturesNull() {

        setPath();

        String match = _Tree.getPathValue(PATH_1);

        assertTrue(match != null);
        assertTrue(match.equals(PATH_1_VAL));
    }

    @Test
    public void segmentPathEquality() {

        List<String> basic = _Tree.segmentPath(PATH_1);
        List<String> pre = _Tree.segmentPath(PATH_1_PRE);
        List<String> post = _Tree.segmentPath(PATH_1_POST);
        List<String> prepost = _Tree.segmentPath(PATH_1_PREPOST);

        assertEquals(basic.size(), pre.size());
        assertEquals(basic.size(), post.size());
        assertEquals(basic.size(), prepost.size());

        for (int i = 0; i < basic.size(); i++) {
            String segment = basic.get(i);
            assertTrue(segment.equals(pre.get(i)));
            assertTrue(segment.equals(post.get(i)));
            assertTrue(segment.equals(prepost.get(i)));
        }
    }
}
