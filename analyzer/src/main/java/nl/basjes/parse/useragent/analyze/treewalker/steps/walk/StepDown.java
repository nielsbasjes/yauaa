/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
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

package nl.basjes.parse.useragent.analyze.treewalker.steps.walk;

import nl.basjes.parse.useragent.analyze.NumberRangeList;
import nl.basjes.parse.useragent.analyze.NumberRangeVisitor;
import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.parser.UserAgentBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentParser;
import nl.basjes.parse.useragent.parser.UserAgentParser.Base64Context;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentBlockContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentEntryContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentProductContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.EmailAddressContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.EmptyWordContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyNameContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyValueContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyValueProductVersionNameContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyValueVersionNameContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyWithoutValueContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.MultipleWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameEmailContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameKeyValueContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameNoVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameUuidContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionSingleWordContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionWithCommasContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.RootTextContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SiteUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UserAgentContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UuIdContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.VersionWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StepDown extends Step {

    private final int start;
    private final int end;
    private final String name;
    private transient UserAgentGetChildrenVisitor userAgentGetChildrenVisitor;

    /**
     * Initialize the transient default values
     */
    private void setDefaultFieldValues() {
        userAgentGetChildrenVisitor = new UserAgentGetChildrenVisitor();
    }

    private void readObject(java.io.ObjectInputStream stream)
        throws java.io.IOException, ClassNotFoundException {
        setDefaultFieldValues();
        stream.defaultReadObject();
    }

    public StepDown(NumberRangeContext numberRange, String name) {
        this(NumberRangeVisitor.getList(numberRange), name);
    }

    private StepDown(NumberRangeList numberRange, String name) {
        setDefaultFieldValues();
        this.name = name;
        this.start = numberRange.getStart();
        this.end = numberRange.getEnd();
    }

    @Override
    public String toString() {
        return "Down([" + start + ":" + end + "]" + name + ")";
    }

    @Override
    public String walk(ParseTree tree, String value) {
        List<? extends ParserRuleContext> children = userAgentGetChildrenVisitor.visit(tree);
        if (children != null) {
            for (ParserRuleContext child : children) {
                String childResult = walkNextStep(child, null);
                if (childResult != null) {
                    return childResult;
                }
            }
        }
        return null;
    }

    /**
     * Get the list of all children that are of the right class and have the right index.
     * Skipping the separators when counting.
     *
     * @param tree         The parent node
     * @param childClasses The required classed of the children to return
     * @return A list (possibly empty, never null) of the children
     */
    @SafeVarargs
    private final List<ParserRuleContext> getChildren(
            ParserRuleContext tree,
            Class<? extends ParserRuleContext>... childClasses) {
        return getChildren(tree, false, childClasses);
    }

    @SafeVarargs
    private final List<ParserRuleContext> getChildren(
            ParserRuleContext treeContext,
            boolean privateNumberRange,
            Class<? extends ParserRuleContext>... childClasses) {
        if (treeContext.children == null) {
            return Collections.emptyList();
        }

        List<ParserRuleContext> contexts = null;
        int index = 0;
        for (ParseTree child : treeContext.children) {
            // Skip things like Token and TerminalNode
            if (ParserRuleContext.class.isInstance(child)) {
                // Skip the separators
                if (!treeIsSeparator(child)) {
                    if (contexts == null) {
                        contexts = new ArrayList<>();
                    }
                    boolean matchesClass = false;
                    for (Class<? extends ParserRuleContext> childClass : childClasses) {
                        if (childClass.isInstance(child)) {
                            matchesClass = true;
                            break;
                        }
                    }

                    if (!privateNumberRange) {
                        index++;
                    }

                    if (!matchesClass) {
                        continue;
                    }

                    if (privateNumberRange) {
                        index++;
                    }

                    if (start <= index && index <= end) {
                        contexts.add(ParserRuleContext.class.cast(child));
                    }
                }
            }
        }

        if (contexts == null) {
            return Collections.emptyList();
        }

        return contexts;
    }


    /**
     * This visitor will return the list of requested child nodes
     */
    private class UserAgentGetChildrenVisitor extends UserAgentBaseVisitor<List<? extends ParserRuleContext>> {

        List<? extends ParserRuleContext> getChildrenByName(ParserRuleContext ctx) {
            switch (name) {
//                case "comments":
//                    return getChildren(ctx, CommentBlockContext.class);
                case "keyvalue":
                    return getChildren(ctx, KeyValueContext.class,
                                            KeyWithoutValueContext.class,
                                            ProductNameKeyValueContext.class);
                case "product":
                    return getChildren(ctx, ProductContext.class,
                                            CommentProductContext.class);
                case "uuid":
                    return getChildren(ctx, UuIdContext.class,
                                            ProductNameUuidContext.class);
                case "base64":
                    return getChildren(ctx, Base64Context.class);
                case "url":
                    return getChildren(ctx, SiteUrlContext.class,
                                            ProductNameUrlContext.class);
                case "email":
                    return getChildren(ctx, EmailAddressContext.class,
                                            ProductNameEmailContext.class);
                case "text":
                    return getChildren(ctx, MultipleWordsContext.class,
                                            VersionWordsContext.class,
                                            EmptyWordContext.class,
                                            RootTextContext.class);
                default:
                    return Collections.emptyList();
            }
        }

        @Override
        public List<? extends ParserRuleContext> visitUserAgent(UserAgentContext ctx) {
            List<? extends ParserRuleContext> children = getChildrenByName(ctx);
            if (children.isEmpty()) {
                return visitChildren(ctx);
            }
            return children;
        }

        @Override
        public List<? extends ParserRuleContext> visitRootElements(UserAgentParser.RootElementsContext ctx) {
            return getChildrenByName(ctx);
        }

        private List<? extends ParserRuleContext> visitGenericProduct(ParserRuleContext ctx) {
            switch (name) {
                case "name":
                    return getChildren(ctx, false,  ProductNameContext.class);
                case "version":
                    return getChildren(ctx, true,   ProductVersionContext.class,
                                                    ProductVersionWithCommasContext.class,
                                                    ProductVersionWordsContext.class,
                                                    ProductVersionSingleWordContext.class);
                case "comments":
                    return getChildren(ctx, true,   CommentBlockContext.class);
                default:
                    return Collections.emptyList();
            }
        }

        @Override
        public List<? extends ParserRuleContext> visitProduct(ProductContext ctx) {
            return visitGenericProduct(ctx);
        }

        @Override
        public List<? extends ParserRuleContext> visitProductNameNoVersion(ProductNameNoVersionContext ctx) {
            return visitGenericProduct(ctx);
        }

        @Override
        public List<? extends ParserRuleContext> visitCommentProduct(CommentProductContext ctx) {
            return visitGenericProduct(ctx);
        }

        @Override
        public List<? extends ParserRuleContext> visitProductName(ProductNameContext ctx) {
            return getChildrenByName(ctx);
        }

        @Override
        public List<? extends ParserRuleContext> visitProductNameKeyValue(ProductNameKeyValueContext ctx) {
            switch (name) {
                case "key":
                    return Collections.singletonList((ParserRuleContext) ctx.key);
                case "value":
                    List<? extends ParserRuleContext> children = ctx.multipleWords();
                    if (!children.isEmpty()) {
                        return children;
                    }

                    children = ctx.keyValueProductVersionName();
                    if (!children.isEmpty()) {
                        return children;
                    }

                    children = ctx.siteUrl();
                    if (!children.isEmpty()) {
                        return children;
                    }

                    children = ctx.emailAddress();
                    if (!children.isEmpty()) {
                        return children;
                    }

                    children = ctx.uuId();
                    return children;
                default:
                    return getChildrenByName(ctx);
            }
        }

        @Override
        public List<? extends ParserRuleContext> visitProductVersion(ProductVersionContext ctx) {
            return getChildrenByName(ctx);
        }

        @Override
        public List<? extends ParserRuleContext> visitProductVersionWithCommas(ProductVersionWithCommasContext ctx) {
            return getChildrenByName(ctx);
        }


        @Override
        public List<? extends ParserRuleContext> visitKeyValue(KeyValueContext ctx) {
            switch (name) {
                case "key":
                    return getChildren(ctx, KeyNameContext.class);
                case "uuid":
                    return getChildren(ctx, UuIdContext.class);
                case "url":
                    return getChildren(ctx, SiteUrlContext.class);
                case "email":
                    return getChildren(ctx, EmailAddressContext.class);
                case "text":
                    return getChildren(ctx, MultipleWordsContext.class,
                                            KeyValueVersionNameContext.class);
                case "value":
                    return getChildren(ctx, UuIdContext.class,
                                            MultipleWordsContext.class,
                                            SiteUrlContext.class,
                                            EmailAddressContext.class,
                                            KeyValueVersionNameContext.class,
                                            KeyValueProductVersionNameContext.class);
                default:
                    return Collections.emptyList();
            }
        }

        @Override
        public List<? extends ParserRuleContext> visitKeyWithoutValue(KeyWithoutValueContext ctx) {
            switch (name) {
                case "key":
                    return getChildren(ctx, KeyNameContext.class);
                default:
                    return Collections.emptyList();
            }
        }

        @Override
        public List<? extends ParserRuleContext> visitCommentBlock(CommentBlockContext ctx) {
            switch (name) {
                case "entry":
                    return getChildren(ctx, CommentEntryContext.class);
                default:
                    return Collections.emptyList();
            }
        }

        @Override
        public List<? extends ParserRuleContext> visitCommentEntry(CommentEntryContext ctx) {
            switch (name) {
                case "keyvalue":
                    return getChildren(ctx, KeyValueContext.class,
                                            KeyWithoutValueContext.class);
                case "product":
                    return getChildren(ctx, CommentProductContext.class,
                                            ProductNameNoVersionContext.class);
                case "uuid":
                    return getChildren(ctx, UuIdContext.class);
                case "url":
                    return getChildren(ctx, SiteUrlContext.class);
                case "email":
                    return getChildren(ctx, EmailAddressContext.class);
                case "text":
                    return getChildren(ctx, MultipleWordsContext.class,
                                            VersionWordsContext.class,
                                            EmptyWordContext.class);
                default:
                    return Collections.emptyList();
            }
        }

        @Override
        public List<? extends ParserRuleContext> visitSiteUrl(SiteUrlContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitEmailAddress(EmailAddressContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitUuId(UuIdContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitVersionWords(VersionWordsContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitMultipleWords(MultipleWordsContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

//        @Override
//        public List<? extends ParserRuleContext> visitEmptyWord(EmptyWordContext ctx) {
//            return Collections.emptyList(); // Cannot walk in here at all
//        }

        @Override
        public List<? extends ParserRuleContext> visitRootText(RootTextContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }
    }

}
