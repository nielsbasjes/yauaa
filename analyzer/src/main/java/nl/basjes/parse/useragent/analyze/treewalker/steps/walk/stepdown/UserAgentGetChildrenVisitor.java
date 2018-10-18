/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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
public class UserAgentGetChildrenVisitor extends UserAgentBaseVisitor<Iterator<? extends ParseTree>> {

    private final String name;
    private final ChildIterable childIterable;

    public UserAgentGetChildrenVisitor(String name, int start, int end) {
        this.name = name;
        switch (name) {
            case "keyvalue":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof KeyValueContext ||
                    clazz instanceof KeyWithoutValueContext ||
                    clazz instanceof ProductNameKeyValueContext));
                break;

            case "product":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof ProductContext ||
                    clazz instanceof CommentProductContext ||
                    clazz instanceof ProductNameNoVersionContext));
                break;

            case "uuid":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof UuIdContext ||
                    clazz instanceof ProductNameUuidContext));
                break;

            case "base64":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof Base64Context));
                break;

            case "url":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof SiteUrlContext ||
                    clazz instanceof ProductNameUrlContext));
                break;

            case "email":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof EmailAddressContext ||
                    clazz instanceof ProductNameEmailContext));
                break;

            case "text":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof MultipleWordsContext ||
                    clazz instanceof VersionWordsContext ||
                    clazz instanceof EmptyWordContext ||
                    clazz instanceof RootTextContext ||
                    clazz instanceof KeyValueVersionNameContext));
                break;

            case "name":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof ProductNameContext));
                break;

            case "version":
                childIterable = new ChildIterable(true, start, end, clazz -> (
                    clazz instanceof ProductVersionContext ||
                    clazz instanceof ProductVersionWithCommasContext ||
                    clazz instanceof ProductVersionWordsContext ||
                    clazz instanceof ProductVersionSingleWordContext));
                break;

            case "comments":
                childIterable = new ChildIterable(true, start, end, clazz -> (
                    clazz instanceof CommentBlockContext));
                break;

            case "key":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof KeyNameContext));
                break;

            case "value":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof UuIdContext ||
                    clazz instanceof MultipleWordsContext ||
                    clazz instanceof SiteUrlContext ||
                    clazz instanceof EmailAddressContext ||
                    clazz instanceof KeyValueVersionNameContext ||
                    clazz instanceof KeyValueProductVersionNameContext));
                break;

            case "entry":
                childIterable = new ChildIterable(false, start, end, clazz -> (
                    clazz instanceof CommentEntryContext));
                break;

            default:
                childIterable = new ChildIterable(false, start, end,  clazz -> (false));
        }
    }


    private static final Iterator<ParseTree> EMPTY = Collections.emptyListIterator();

    @Override
    protected Iterator<? extends ParseTree> defaultResult() {
        return EMPTY;
    }

    Iterator<? extends ParseTree> getChildrenByName(ParserRuleContext ctx) {
        return childIterable.iterator(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitUserAgent(UserAgentContext ctx) {
        Iterator<? extends ParseTree>  children = getChildrenByName(ctx);
        if (children.hasNext()) {
            return children;
        }
        return visitChildren(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitRootElements(RootElementsContext ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitProduct(ProductContext ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitProductNameNoVersion(ProductNameNoVersionContext ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitCommentProduct(CommentProductContext ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitProductName(ProductNameContext ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitProductNameKeyValue(ProductNameKeyValueContext ctx) {
        switch (name) {
            case "key":
                return Collections.singletonList((ParserRuleContext) ctx.key).iterator();
            case "value":
                List<? extends ParserRuleContext> children = ctx.multipleWords();
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
    public Iterator<? extends ParseTree> visitProductVersion(ProductVersionContext ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitProductVersionWithCommas(ProductVersionWithCommasContext ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitKeyValue(KeyValueContext ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitKeyWithoutValue(KeyWithoutValueContext ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitCommentBlock(CommentBlockContext ctx) {
        return getChildrenByName(ctx);
    }

    @Override
    public Iterator<? extends ParseTree> visitCommentEntry(CommentEntryContext ctx) {
        return getChildrenByName(ctx);
    }
}
