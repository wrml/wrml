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
 * Copyright 2012 Mark Masse (OSS project WRML.org)
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

import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wrml.runtime.rest.ApiNavigator;

public class PrefixTreeNodeTest 
{
    private static final String VALUE_1 = "borris";
    
    private static final String VALUE_2 = "natasha";
    
    private static final String SEGMENT_1 = "address";
    
    private PrefixTreeNode<String> _Node;
    
    @Before
    public void init()
    {
        _Node = new PrefixTreeNode<>();
    }
    
    @Test
    public void emptyNodeToStringTest()
    {
        String emptyNode = _Node.toString();
        Assert.assertEquals("Value: []\nPaths: []", emptyNode);
    }
    
    @Test
    public void setValue()
    {
        _Node.setValue(VALUE_1);
        
        String node = _Node.toString();
        Assert.assertEquals("Value: [" + VALUE_1 + "]\nPaths: []", node);
    }
    
    @Test
    public void getSetValue()
    {
        String node = _Node.getValue();
        Assert.assertNull(node);
        
        _Node.setValue(VALUE_1);
        node = _Node.getValue();
        
        Assert.assertEquals(VALUE_1, node);
    }
    
    @Test
    public void getEmptyLink()
    {
        PrefixTreeNode<String> node = _Node.getLink(SEGMENT_1);
        Assert.assertNotNull(node);
        Assert.assertEquals(node, PrefixTreeNode.EMPTY_NODE);
    }
    
    @Test
    public void getSetLink()
    {
        PrefixTreeNode<String> node = _Node.addLink(SEGMENT_1, VALUE_1);
        Assert.assertNotNull(node);
        Assert.assertNotEquals(node, PrefixTreeNode.EMPTY_NODE);
        
        PrefixTreeNode<String> node2 = _Node.getLink(SEGMENT_1);
        Assert.assertNotNull(node2);
        Assert.assertEquals(node2, node);
    }
    
    @Test
    public void fullToString()
    {
        _Node.setValue(VALUE_1);
        _Node.addLink(SEGMENT_1, VALUE_2);
        
        String rep = _Node.toString();
        Assert.assertEquals("Value: [borris]\nPaths: [address, ]", rep);
    }
    
    @Test
    public void deepPrintTest()
    {
        
        _Node.setValue(VALUE_1);
        _Node.addLink(SEGMENT_1, VALUE_2);
        
        Set<String> paths = _Node.deepPrint(ApiNavigator.PATH_SEPARATOR_CHAR);
        Assert.assertEquals(paths.toString(), "[address/natasha, borris]");
    }
}
