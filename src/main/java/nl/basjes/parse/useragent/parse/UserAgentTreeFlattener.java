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

package nl.basjes.parse.useragent.parse;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentBaseListener;
import nl.basjes.parse.useragent.UserAgentLexer;
import nl.basjes.parse.useragent.UserAgentParser;
import nl.basjes.parse.useragent.UserAgentParser.UserAgentContext;
import nl.basjes.parse.useragent.UserAgentParser.ProductContext;
import nl.basjes.parse.useragent.UserAgentParser.CommentProductContext;
import nl.basjes.parse.useragent.UserAgentParser.ProductWordVersionContext;
import nl.basjes.parse.useragent.UserAgentParser.ProductNameBareContext;
import nl.basjes.parse.useragent.UserAgentParser.ProductVersionContext;
import nl.basjes.parse.useragent.UserAgentParser.SimpleVersionContext;
import nl.basjes.parse.useragent.UserAgentParser.ProductNameVersionContext;
import nl.basjes.parse.useragent.UserAgentParser.ProductNameEmailContext;
import nl.basjes.parse.useragent.UserAgentParser.ProductNameUrlContext;
import nl.basjes.parse.useragent.UserAgentParser.ProductNameUuidContext;
import nl.basjes.parse.useragent.UserAgentParser.UuIdContext;
import nl.basjes.parse.useragent.UserAgentParser.EmailAddressContext;
import nl.basjes.parse.useragent.UserAgentParser.SiteUrlContext;
import nl.basjes.parse.useragent.UserAgentParser.Base64Context;
import nl.basjes.parse.useragent.UserAgentParser.CommentBlockContext;
import nl.basjes.parse.useragent.UserAgentParser.CommentEntryContext;
import nl.basjes.parse.useragent.UserAgentParser.ProductNameKeyValueContext;
import nl.basjes.parse.useragent.UserAgentParser.KeyValueProductVersionNameContext;
import nl.basjes.parse.useragent.UserAgentParser.KeyValueContext;
import nl.basjes.parse.useragent.UserAgentParser.KeyValueVersionNameContext;
import nl.basjes.parse.useragent.UserAgentParser.KeyNameContext;
import nl.basjes.parse.useragent.UserAgentParser.EmptyWordContext;
import nl.basjes.parse.useragent.UserAgentParser.MultipleWordsContext;
import nl.basjes.parse.useragent.UserAgentParser.VersionWordContext;
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.utils.VersionSplitter;
import nl.basjes.parse.useragent.utils.WordSplitter;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.utils.AntlrUtils.getSourceText;

public class UserAgentTreeFlattener extends UserAgentBaseListener {
    private ParseTreeWalker walker;
    private Analyzer analyzer;

    enum PathType {
        CHILD,
        COMMENT,
        VERSION
    }

    public class State {
        long child = 0;
        long version = 0;
        long comment = 0;
        String name;
        String path;
        ParseTree ctx = null;

        public State(String name) {
            this.name = name;
        }

        public State(ParseTree ctx, String name) {
            this.ctx = ctx;
            this.name = name;
        }

        public String calculatePath(PathType type, boolean fakeChild) {
            ParseTree node = ctx;
            path = name;
            if (node == null) {
                return path;
            }
            State parentState = null;

            while (parentState == null) {
                node = node.getParent();
                if (node == null) {
                    return path;
                }
                parentState = state.get(node);
            }

            long counter = 0;
            switch (type) {
                case CHILD:
                    if (!fakeChild) {
                        parentState.child++;
                    }
                    counter = parentState.child;
                    break;
                case COMMENT:
                    if (!fakeChild) {
                        parentState.comment++;
                    }
                    counter = parentState.comment;
                    break;
                case VERSION:
                    if (!fakeChild) {
                        parentState.version++;
                    }
                    counter = parentState.version;
                    break;
                default:
            }

            this.path = parentState.path + ".(" + counter + ')' + name;

            return this.path;
        }
    }

    private ParseTreeProperty<State> state;

    public UserAgentTreeFlattener(Analyzer analyzer) {
        walker = new ParseTreeWalker();
        this.analyzer = analyzer;
    }

    private boolean verbose = false;

    public void setVerbose(boolean newVerbose) {
        this.verbose = newVerbose;
    }

    public UserAgent parse(String userAgentString) {
        UserAgent userAgent = new UserAgent(userAgentString);
        return parseIntoCleanUserAgent(userAgent);
    }

    public UserAgent parse(UserAgent userAgent) {
        userAgent.reset();
        return parseIntoCleanUserAgent(userAgent);
    }

    /**
     * Parse the useragent and return every part that was found.
     *
     * @param userAgent The useragent instance that needs to be parsed
     * @return If the parse was valid (i.e. were there any parser errors: true=valid; false=has errors
     */
    private UserAgent parseIntoCleanUserAgent(UserAgent userAgent) {
        if (userAgent.getUserAgentString() == null) {
            userAgent.set(SYNTAX_ERROR, "true", 1);
            return userAgent; // Cannot parse this
        }

        // Parse the userAgent into tree
        UserAgentContext userAgentContext = parseUserAgent(userAgent);

        // Walk the tree an inform the calling analyzer about all the nodes found
        state = new ParseTreeProperty<>();

        State rootState = new State("agent");
        rootState.calculatePath(PathType.CHILD, false);
        state.put(userAgentContext, rootState);

        if (userAgent.hasSyntaxError()) {
            inform(null, "__SyntaxError__", "true");
        } else {
            inform(null, "__SyntaxError__", "false");
        }

        walker.walk(this, userAgentContext);
        return userAgent;
    }

    // =================================================================================

    private void inform(ParseTree ctx, String path) {
        inform(ctx, path, getSourceText(ctx));
    }

    private void inform(ParseTree ctx, String name, String value) {
        inform(ctx, ctx, name, value, false);
    }

    private void inform(ParseTree ctx, String name, String value, boolean fakeChild) {
        inform(ctx, ctx, name, value, fakeChild);
    }

    private void inform(ParseTree stateCtx, ParseTree ctx, String name, String value, boolean fakeChild) {
        State myState = new State(stateCtx, name);

        if (!fakeChild) {
            state.put(stateCtx, myState);
        }

        PathType childType;
        switch (name) {
            case "comments":
                childType = PathType.COMMENT;
                break;
            case "version":
                childType = PathType.VERSION;
                break;
            default:
                childType = PathType.CHILD;
        }

        String path = myState.calculatePath(childType, fakeChild);
        analyzer.inform(path, value, ctx);
    }

//  =================================================================================

    private UserAgentContext parseUserAgent(UserAgent userAgent) {
        String userAgentString = EvilManualUseragentStringHacks.fixIt(userAgent.getUserAgentString());

        ANTLRInputStream input = new ANTLRInputStream(userAgentString);
        UserAgentLexer lexer = new UserAgentLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        UserAgentParser parser = new UserAgentParser(tokens);

        if (!verbose) {
            lexer.removeErrorListeners();
            parser.removeErrorListeners();
        }
        lexer.addErrorListener(userAgent);
        parser.addErrorListener(userAgent);

        return parser.userAgent();
    }

    //  =================================================================================

    @Override
    public void enterUserAgent(UserAgentContext ctx) {
        inform(ctx, "agent");
    }

    @Override
    public void enterProduct(ProductContext ctx) {
        inform(ctx, "product");
    }

    @Override
    public void enterCommentProduct(CommentProductContext ctx) {
        inform(ctx, "product");
    }

    @Override
    public void enterProductNameEmail(ProductNameEmailContext ctx) {
        inform(ctx, "name");
        inform(ctx, "name.(1)email", ctx.getText(), true);
    }

    @Override
    public void enterProductNameUrl(ProductNameUrlContext ctx) {
        inform(ctx, "name");
        inform(ctx, "name.(1)url", ctx.getText(), true);
    }

    @Override
    public void enterProductNameBare(ProductNameBareContext ctx) {
        informSubstrings(ctx, "name");
    }

    @Override
    public void enterProductNameKeyValue(ProductNameKeyValueContext ctx) {
        informSubstrings(ctx, "name");
    }

    @Override
    public void enterProductNameVersion(ProductNameVersionContext ctx) {
        informSubstrings(ctx, "name");
    }

    @Override
    public void enterProductNameUuid(ProductNameUuidContext ctx) {
        inform(ctx, "name");
    }

    @Override
    public void enterProductVersion(ProductVersionContext ctx) {
        if (ctx.getChildCount() != 1 ||
            !(ctx.getChild(0) instanceof SimpleVersionContext)) {
            inform(ctx, "version");
        }
    }

    @Override
    public void enterSimpleVersion(SimpleVersionContext ctx) {
        informSubVersions(ctx, "version");
    }

    @Override
    public void enterProductWordVersion(ProductWordVersionContext ctx) {
        inform(ctx, "version");
    }

    @Override
    public void enterKeyValueProductVersionName(KeyValueProductVersionNameContext ctx) {
        informSubstrings(ctx, "version");
    }

    @Override
    public void enterCommentBlock(CommentBlockContext ctx) {
        inform(ctx, "comments");
    }

    @Override
    public void enterCommentEntry(CommentEntryContext ctx) {
        informSubstrings(ctx, "entry");
    }

    private void informSubstrings(ParserRuleContext ctx, String name) {
        informSubstrings(ctx, name, 10);
    }

    private void informSubstrings(ParserRuleContext ctx, String name, int maxSubStrings) {
        String text = getSourceText(ctx);
        inform(ctx, name, text, false);

        int startOffsetPrevious = 0;
        int count = 1;
        char[] chars = text.toCharArray();
        String firstWords;
        while((firstWords = WordSplitter.getFirstWords(text, count))!=null) {
            inform(ctx, ctx, name + "#" + count, firstWords, true);
//            inform(ctx, ctx, name + "%" + count, WordSplitter.getSingleWord(text, count), true);
            inform(ctx, ctx, name + "%" + count, firstWords.substring(startOffsetPrevious), true);
            count++;
            if (count > maxSubStrings) {
                return;
            }
            startOffsetPrevious = WordSplitter.findNextWordStart(chars, firstWords.length());
        }
    }

    private void informSubVersions(ParserRuleContext ctx, String name) {
        informSubVersions(ctx, name, 3);
    }

    private void informSubVersions(ParserRuleContext ctx, String name, int maxSubStrings) {
        String text = getSourceText(ctx);
        inform(ctx, name, text, false);

        int startOffsetPrevious = 0;
        int count = 1;
        char[] chars = text.toCharArray();
        String firstVersions;
        while((firstVersions = VersionSplitter.getFirstVersions(text, count))!=null) {
            inform(ctx, ctx, name + "#" + count, firstVersions, true);
            inform(ctx, ctx, name + "%" + count, firstVersions.substring(startOffsetPrevious), true);
            count++;
            if (count > maxSubStrings) {
                return;
            }
            startOffsetPrevious = VersionSplitter.findNextVersionStart(chars, firstVersions.length());
        }
    }


    @Override
    public void enterMultipleWords(MultipleWordsContext ctx) {
        informSubstrings(ctx, "text");
    }


    @Override
    public void enterKeyValue(KeyValueContext ctx) {
        inform(ctx, "keyvalue");
    }

    @Override
    public void enterKeyName(KeyNameContext ctx) {
        inform(ctx, "key");
    }

    @Override
    public void enterKeyValueVersionName(KeyValueVersionNameContext ctx) {
        informSubstrings(ctx, "value");
    }

    @Override
    public void enterVersionWord(VersionWordContext ctx) {
        informSubstrings(ctx, "text");
    }

    @Override
    public void enterSiteUrl(SiteUrlContext ctx) {
        inform(ctx, "url", ctx.url.getText());
    }

    @Override
    public void enterUuId(UuIdContext ctx) {
        inform(ctx, "uuid", ctx.uuid.getText());
    }

    @Override
    public void enterEmailAddress(EmailAddressContext ctx) {
        inform(ctx, "email", ctx.email.getText());
    }

    @Override
    public void enterBase64(Base64Context ctx) {
        inform(ctx, "base64", ctx.value.getText());
    }

    @Override
    public void enterEmptyWord(EmptyWordContext ctx) {
        inform(ctx, "text", "");
    }
}
