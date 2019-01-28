/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown.ChildIterable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class TestChildIterable {

    @Test(expected = NoSuchElementException.class)
    public void testEdgeNoChildren(){
        ChildIterable ci = new ChildIterable(true, 1, 5, x -> (true));

        ParserRuleContext prc = new ParserRuleContext();

        Iterator<ParseTree> iterator = ci.iterator(prc);

        iterator.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void testEdgeFewChildrens(){
        ChildIterable ci = new ChildIterable(true, 1, 5, x -> (true));

        ParserRuleContext prc = new ParserRuleContext();
        prc.children = new ArrayList<>();
        prc.children.add(new ParserRuleContext());
        prc.children.add(new ParserRuleContext());
        prc.children.add(new ParserRuleContext());
        prc.children.add(new ParserRuleContext());

        Iterator<ParseTree> iterator = ci.iterator(prc);

        int i = 0;
        while (i < 10) {
            i++;
            iterator.next();
        }

    }

}
