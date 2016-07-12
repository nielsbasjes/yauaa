/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent.analyze.treewalker.steps.walk;

import nl.basjes.parse.useragent.UserAgentBaseVisitor;
import nl.basjes.parse.useragent.UserAgentParser.KeyWithoutValueContext;
import nl.basjes.parse.useragent.UserAgentParser.ProductNameNoVersionContext;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.NumberRangeList;
import nl.basjes.parse.useragent.analyze.NumberRangeVisitor;
import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nl.basjes.parse.useragent.UserAgentParser.UserAgentContext;
import static nl.basjes.parse.useragent.UserAgentParser.RootTextPartContext;
import static nl.basjes.parse.useragent.UserAgentParser.ProductContext;
import static nl.basjes.parse.useragent.UserAgentParser.CommentProductContext;
import static nl.basjes.parse.useragent.UserAgentParser.ProductWordVersionContext;
import static nl.basjes.parse.useragent.UserAgentParser.ProductNameContext;
import static nl.basjes.parse.useragent.UserAgentParser.ProductNameBareContext;
import static nl.basjes.parse.useragent.UserAgentParser.ProductVersionContext;
import static nl.basjes.parse.useragent.UserAgentParser.ProductNameVersionContext;
import static nl.basjes.parse.useragent.UserAgentParser.ProductNameEmailContext;
import static nl.basjes.parse.useragent.UserAgentParser.ProductNameUrlContext;
import static nl.basjes.parse.useragent.UserAgentParser.ProductNameUuidContext;
import static nl.basjes.parse.useragent.UserAgentParser.UuIdContext;
import static nl.basjes.parse.useragent.UserAgentParser.EmailAddressContext;
import static nl.basjes.parse.useragent.UserAgentParser.SiteUrlContext;
import static nl.basjes.parse.useragent.UserAgentParser.CommentSeparatorContext;
import static nl.basjes.parse.useragent.UserAgentParser.CommentBlockContext;
import static nl.basjes.parse.useragent.UserAgentParser.CommentEntryContext;
import static nl.basjes.parse.useragent.UserAgentParser.ProductNameKeyValueContext;
import static nl.basjes.parse.useragent.UserAgentParser.KeyValueProductVersionNameContext;
import static nl.basjes.parse.useragent.UserAgentParser.KeyValueContext;
import static nl.basjes.parse.useragent.UserAgentParser.KeyValueVersionNameContext;
import static nl.basjes.parse.useragent.UserAgentParser.KeyNameContext;
import static nl.basjes.parse.useragent.UserAgentParser.EmptyWordContext;
import static nl.basjes.parse.useragent.UserAgentParser.MultipleWordsContext;
import static nl.basjes.parse.useragent.UserAgentParser.VersionWordContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.NumberRangeContext;

public class StepDown extends Step {

    private int start;
    private int end;
    private String name;
    private UserAgentGetChildrenVisitor userAgentGetChildrenVisitor = new UserAgentGetChildrenVisitor();

    public StepDown(NumberRangeContext numberRange, String name) {
        this(NumberRangeVisitor.getList(numberRange), name);
    }

    private StepDown(NumberRangeList numberRange, String name) {
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
            ParseTree tree,
            Class<? extends ParserRuleContext>... childClasses) {
        return getChildren(tree, false, childClasses);
    }

    @SafeVarargs
    private final List<ParserRuleContext> getChildren(
            ParseTree tree,
            boolean privateNumberRange,
            Class<? extends ParserRuleContext>... childClasses) {
        if (!(tree instanceof ParserRuleContext)) {
            return Collections.emptyList(); // Only contexts can have children
        }
        if (childClasses.length == 0) {
            throw new InvalidParserConfigurationException("WTF");
        }

        ParserRuleContext treeContext = (ParserRuleContext) tree;
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

        @Override
        public List<? extends ParserRuleContext> visitUserAgent(UserAgentContext ctx) {
            switch (name) {
                case "product":
                    return getChildren(ctx, ProductContext.class);
                case "url":
                    return getChildren(ctx, SiteUrlContext.class);
                case "email":
                    return getChildren(ctx, EmailAddressContext.class);
                case "text":
                    return getChildren(ctx, RootTextPartContext.class);
                default:
                    return Collections.emptyList();
            }
        }


        private List<? extends ParserRuleContext> visitGenericProduct(ParserRuleContext ctx) {
            switch (name) {
                case "name":
                    return getChildren(ctx, false,  ProductNameContext.class,
                                                    ProductNameNoVersionContext.class);
                case "version":
                    return getChildren(ctx, true,   ProductVersionContext.class,
                                                    ProductWordVersionContext.class);
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
        public List<? extends ParserRuleContext> visitCommentProduct(CommentProductContext ctx) {
            return visitGenericProduct(ctx);
        }

        @Override
        public List<? extends ParserRuleContext> visitProductName(ProductNameContext ctx) {
            return Collections.emptyList();
        }

        @Override
        public List<? extends ParserRuleContext> visitProductNameBare(ProductNameBareContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitProductNameVersion(ProductNameVersionContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitProductWordVersion(ProductWordVersionContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitProductNameKeyValue(ProductNameKeyValueContext ctx) {
            switch (name) {
                case "key":
                    return Collections.singletonList((ParserRuleContext) ctx.key);
                case "value":
                    List<? extends ParserRuleContext> children = ctx.multipleWords();
                    if (children.isEmpty()) {
                        children = ctx.keyValueProductVersionName();
                        if (children.isEmpty()) {
                            children = ctx.siteUrl();
                            if (children.isEmpty()) {
                                children = ctx.emailAddress();
                                if (children.isEmpty()) {
                                    children = ctx.uuId();
                                }
                            }
                        }
                    }
                    return children;
                case "comments":
                    return getChildren(ctx, true, CommentBlockContext.class);
                default:
                    return Collections.emptyList();
            }
        }

        @Override
        public List<? extends ParserRuleContext> visitProductNameEmail(ProductNameEmailContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitProductNameUrl(ProductNameUrlContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitProductNameUuid(ProductNameUuidContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitProductVersion(ProductVersionContext ctx) {
            return visit(ctx);
        }

        @Override
        public List<? extends ParserRuleContext> visitKeyName(KeyNameContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitKeyValueVersionName(KeyValueVersionNameContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
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
                                            KeyValueVersionNameContext.class);
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
        public List<? extends ParserRuleContext> visitKeyValueProductVersionName(KeyValueProductVersionNameContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
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
                case "comments":
                    return getChildren(ctx, CommentBlockContext.class);
                case "keyvalue":
                    return getChildren(ctx, KeyValueContext.class,
                                            KeyWithoutValueContext.class);
                case "product":
                    return getChildren(ctx, CommentProductContext.class);
                case "uuid":
                    return getChildren(ctx, UuIdContext.class);
                case "url":
                    return getChildren(ctx, SiteUrlContext.class);
                case "email":
                    return getChildren(ctx, EmailAddressContext.class);
                case "text":
                    return getChildren(ctx, MultipleWordsContext.class,
                                            VersionWordContext.class,
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
        public List<? extends ParserRuleContext> visitVersionWord(VersionWordContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitMultipleWords(MultipleWordsContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitCommentSeparator(CommentSeparatorContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitEmptyWord(EmptyWordContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }

        @Override
        public List<? extends ParserRuleContext> visitRootTextPart(RootTextPartContext ctx) {
            return Collections.emptyList(); // Cannot walk in here at all
        }
    }

}
