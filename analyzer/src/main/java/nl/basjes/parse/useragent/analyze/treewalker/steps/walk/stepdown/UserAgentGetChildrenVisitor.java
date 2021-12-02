/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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

package nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown;

import nl.basjes.parse.useragent.analyze.MatcherTree;
import nl.basjes.parse.useragent.parse.AgentPathFragment;
import nl.basjes.parse.useragent.parser.UserAgentBaseVisitor;
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
import nl.basjes.parse.useragent.parser.UserAgentParser.RootElementsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.RootTextContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SiteUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UserAgentContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UuIdContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.VersionWordsContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * This visitor will return the list of requested child nodes
 */
public class UserAgentGetChildrenVisitor extends UserAgentBaseVisitor<Iterator<? extends ParseTree<MatcherTree>>, MatcherTree> {

    private final AgentPathFragment pathFragment;
    private final ChildIterable childIterable;

    public UserAgentGetChildrenVisitor(AgentPathFragment pathFragment, int start, int end) {
        this.pathFragment = pathFragment;
        switch (pathFragment) {
            case KEYVALUE:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof KeyValueContext ||
                    clazz instanceof KeyWithoutValueContext ||
                    clazz instanceof ProductNameKeyValueContext));
                break;

            case PRODUCT:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof ProductContext ||
                    clazz instanceof CommentProductContext ||
                    clazz instanceof ProductNameNoVersionContext));
                break;

            case UUID:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof UuIdContext ||
                    clazz instanceof ProductNameUuidContext));
                break;

            case BASE64:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof Base64Context));
                break;

            case URL:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof SiteUrlContext ||
                    clazz instanceof ProductNameUrlContext));
                break;

            case EMAIL:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof EmailAddressContext ||
                    clazz instanceof ProductNameEmailContext));
                break;

            case TEXT:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof MultipleWordsContext ||
                    clazz instanceof VersionWordsContext ||
                    clazz instanceof EmptyWordContext ||
                    clazz instanceof RootTextContext ||
                    clazz instanceof KeyValueVersionNameContext));
                break;

            case NAME:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof ProductNameContext));
                break;

            case VERSION:
                childIterable = new ChildIterable(true, start, end, clazz -> (
                    clazz instanceof ProductVersionContext ||
                    clazz instanceof ProductVersionWithCommasContext ||
                    clazz instanceof ProductVersionWordsContext ||
                    clazz instanceof ProductVersionSingleWordContext));
                break;

            case COMMENTS:
                childIterable = new ChildIterable(true, start, end, clazz -> (
                    clazz instanceof CommentBlockContext));
                break;

            case KEY:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof KeyNameContext));
                break;

            case VALUE:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof UuIdContext ||
                    clazz instanceof MultipleWordsContext ||
                    clazz instanceof SiteUrlContext ||
                    clazz instanceof EmailAddressContext ||
                    clazz instanceof KeyValueVersionNameContext ||
                    clazz instanceof KeyValueProductVersionNameContext));
                break;

            case ENTRY:
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof CommentEntryContext));
                break;

            default:
                childIterable = new ChildIterable(false, start, end,  clazz -> (false));
        }
    }


    private final Iterator<ParseTree<MatcherTree>> emptyIterator = Collections.emptyListIterator();

    @Override
    protected Iterator<? extends ParseTree<MatcherTree>> defaultResult() {
        return emptyIterator;
    }

    Iterator<? extends ParseTree<MatcherTree>> getChildrenByName(ParserRuleContext<MatcherTree> ctx) {
        return childIterable.iterator(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitUserAgent(UserAgentContext<MatcherTree> ctx) {
        Iterator<? extends ParseTree<MatcherTree>>  children = getChildrenByName(ctx);
        if (children.hasNext()) {
            return children;
        }
        return visitChildren(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitRootElements(RootElementsContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitProduct(ProductContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitProductNameNoVersion(ProductNameNoVersionContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitCommentProduct(CommentProductContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitProductName(ProductNameContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitProductNameKeyValue(ProductNameKeyValueContext<MatcherTree> ctx) {
        switch (pathFragment) {
            case KEY:
                return Collections.singletonList((ParserRuleContext<MatcherTree>) ctx.key).iterator();
            case VALUE:
                List<? extends ParserRuleContext<MatcherTree>> children = ctx.multipleWords();
                if (!children.isEmpty()) {
                    return children.iterator();
                }

                children = ctx.keyValueProductVersionName();
                if (!children.isEmpty()) {
                    return children.iterator();
                }

                children = ctx.siteUrl();
                if (!children.isEmpty()) {
                    return children.iterator();
                }

                children = ctx.emailAddress();
                if (!children.isEmpty()) {
                    return children.iterator();
                }

                children = ctx.uuId();
                return children.iterator();
            default:
                return getChildrenByName(ctx);
        }
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitProductVersion(ProductVersionContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitProductVersionWithCommas(ProductVersionWithCommasContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitKeyValue(KeyValueContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitKeyWithoutValue(KeyWithoutValueContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitCommentBlock(CommentBlockContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<MatcherTree>> visitCommentEntry(CommentEntryContext<MatcherTree> ctx) {
        return getChildrenByName(ctx);
    }
}
