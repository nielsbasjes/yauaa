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

package nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown;

import nl.basjes.parse.useragent.parse.AgentPathFragment;
import nl.basjes.parse.useragent.parser.UserAgentBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentParser;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentBlockContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentEntryContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentProductContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyValueContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyWithoutValueContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameKeyValueContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameNoVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionWithCommasContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.RootElementsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UserAgentContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyListIterator;


/**
 * This visitor will return the list of requested child nodes
 */
public class UserAgentGetChildrenVisitor<P> extends UserAgentBaseVisitor<Iterator<? extends ParseTree<P>>, P> {

    private final AgentPathFragment name;
    private final ChildIterable<P>  childIterable;

    public UserAgentGetChildrenVisitor(AgentPathFragment name, int start, int end) {
        this.name = name;
        switch (name) {
            case KEYVALUE:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.KeyValueContext ||
                    clazz instanceof UserAgentParser.KeyWithoutValueContext ||
                    clazz instanceof UserAgentParser.ProductNameKeyValueContext));
                break;

            case PRODUCT:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.ProductContext ||
                    clazz instanceof UserAgentParser.CommentProductContext ||
                    clazz instanceof UserAgentParser.ProductNameNoVersionContext));
                break;

            case UUID:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.UuIdContext ||
                    clazz instanceof UserAgentParser.ProductNameUuidContext));
                break;

            case BASE64:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.Base64Context));
                break;

            case URL:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.SiteUrlContext ||
                    clazz instanceof UserAgentParser.ProductNameUrlContext));
                break;

            case EMAIL:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.EmailAddressContext ||
                    clazz instanceof UserAgentParser.ProductNameEmailContext));
                break;

            case TEXT:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.MultipleWordsContext ||
                    clazz instanceof UserAgentParser.VersionWordsContext ||
                    clazz instanceof UserAgentParser.EmptyWordContext ||
                    clazz instanceof UserAgentParser.RootTextContext ||
                    clazz instanceof UserAgentParser.KeyValueVersionNameContext));
                break;

            case NAME:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.ProductNameContext));
                break;

            case VERSION:
                childIterable = new ChildIterable<>(true, start, end, clazz -> (
                    clazz instanceof UserAgentParser.ProductVersionContext ||
                    clazz instanceof UserAgentParser.ProductVersionWithCommasContext ||
                    clazz instanceof UserAgentParser.ProductVersionWordsContext ||
                    clazz instanceof UserAgentParser.ProductVersionSingleWordContext));
                break;

            case COMMENTS:
                childIterable = new ChildIterable<>(true, start, end, clazz -> (
                    clazz instanceof UserAgentParser.CommentBlockContext));
                break;

            case KEY:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.KeyNameContext));
                break;

            case VALUE:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.UuIdContext ||
                    clazz instanceof UserAgentParser.MultipleWordsContext ||
                    clazz instanceof UserAgentParser.SiteUrlContext ||
                    clazz instanceof UserAgentParser.EmailAddressContext ||
                    clazz instanceof UserAgentParser.KeyValueVersionNameContext ||
                    clazz instanceof UserAgentParser.KeyValueProductVersionNameContext));
                break;

            case ENTRY:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (
                    clazz instanceof UserAgentParser.CommentEntryContext));
                break;

            default:
                childIterable = new ChildIterable<>(false, start, end, clazz -> (false));
        }
    }

    @Override
    protected Iterator<? extends ParseTree<P>> defaultResult() {
        return emptyListIterator();
    }

    Iterator<? extends ParseTree<P>> getChildrenByName(ParserRuleContext<P> ctx) {
        return childIterable.iterator(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visit(ParseTree<P> tree, P parameter) {
        throw new NotImplementedException("Wrong visit usage");
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitUserAgent(UserAgentContext<P> ctx) {
        Iterator<? extends ParseTree<P>>  children = getChildrenByName(ctx);
        if (children.hasNext()) {
            return children;
        }
        return visitChildren(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitRootElements(RootElementsContext<P> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitProduct(ProductContext<P> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitProductNameNoVersion(ProductNameNoVersionContext<P> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitCommentProduct(CommentProductContext<P> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitProductName(ProductNameContext<P> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitProductNameKeyValue(ProductNameKeyValueContext<P> ctx) {
        switch (name) {
            case KEY:
                return Collections.singletonList((ParserRuleContext<P>) ctx.key).iterator();
            case VALUE:
                List<? extends ParserRuleContext<P>> children = ctx.multipleWords();
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
    public Iterator<? extends ParseTree<P>> visitProductVersion(ProductVersionContext<P> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitProductVersionWithCommas(ProductVersionWithCommasContext<P> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitKeyValue(KeyValueContext<P> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitKeyWithoutValue(KeyWithoutValueContext<P> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitCommentBlock(CommentBlockContext<P> ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree<P>> visitCommentEntry(CommentEntryContext<P> ctx) {
        return getChildrenByName(ctx);
    }
}
