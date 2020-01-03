/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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
package nl.basjes.parse.useragent.utils;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAntlrUtils {

    @Test
    public void testEdges() {
        ParserRuleContext context = new ParserRuleContext(null, 42);

        ParseTree content = new RuleContext() {
            @Override
            public String getText() {
                return "Content";
            }
        };
        context.children = Collections.singletonList(content);

        Token start = new CommonToken(1, "Start");
        Token stop = new CommonToken(2, "Stop");

        context.start = null;
        context.stop = null;
        assertEquals("Content", AntlrUtils.getSourceText(context));

        context.start = start;
        context.stop = null;
        assertEquals("Content", AntlrUtils.getSourceText(context));

        context.start = null;
        context.stop = stop;
        assertEquals("Content", AntlrUtils.getSourceText(context));
    }

}
