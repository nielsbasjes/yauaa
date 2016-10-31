/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
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

package nl.basjes.parse.useragent.analyze.treewalker.steps;

import nl.basjes.parse.useragent.parser.UserAgentBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import static nl.basjes.parse.useragent.utils.AntlrUtils.getSourceText;

public class GetResultValueVisitor extends UserAgentBaseVisitor<String> {

    private static final GetResultValueVisitor INSTANCE = new GetResultValueVisitor();

    public static String getResultValue(ParseTree tree) {
        return INSTANCE.visit(tree);
    }

    @Override
    public String visitUuId(UserAgentParser.UuIdContext ctx) {
        return ctx.uuid.getText();
    }

    // This way we can force all other implementations to be really simple.
    @Override
    public String visitChildren(RuleNode node) {
        return getSourceText(node);
    }
}
