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

package nl.basjes.parse.useragent.parse;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzerDirect;
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.parser.UserAgentBaseListener;
import nl.basjes.parse.useragent.parser.UserAgentLexer;
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
import nl.basjes.parse.useragent.parser.UserAgentParser.MultipleWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameEmailContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameKeyValueContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameUuidContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionWithCommasContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.RootTextContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SingleVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SingleVersionWithCommasContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SiteUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UserAgentContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UuIdContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.VersionWordsContext;
import nl.basjes.parse.useragent.utils.Splitter;
import nl.basjes.parse.useragent.utils.VersionSplitter;
import nl.basjes.parse.useragent.utils.WordSplitter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.Serializable;

import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.utils.AntlrUtils.getSourceText;

public class UserAgentTreeFlattener extends UserAgentBaseListener<MatcherTree> implements Serializable {
    private final Analyzer               analyzer;

    private static final String AGENT    = "agent";
    private static final String PRODUCT  = "product";
    private static final String NAME     = "name";
    private static final String VERSION  = "version";
    private static final String COMMENTS = "comments";
    private static final String KEYVALUE = "keyvalue";
    private static final String KEY      = "key";
    private static final String TEXT     = "text";
    private static final String URL      = "url";
    private static final String UUID     = "uuid";
    private static final String EMAIL    = "email";
    private static final String BASE64   = "base64";

    enum PathType {
        CHILD,
        COMMENT,
        VERSION
    }

    public class State {
        long child = 0;
        long version = 0;
        long comment = 0;
        final String name;
        String path;
        ParseTree<MatcherTree> ctx = null;

        @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
        private State() {
            name = null;
        }

        public State(String name) {
            this.name = name;
        }

        public State(ParseTree<MatcherTree> ctx, String name) {
            this.ctx = ctx;
            this.name = name;
        }

        public String calculatePath(PathType type, boolean fakeChild) {
            ParseTree<MatcherTree> node = ctx;
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

    private transient ParseTreeProperty<State> state;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private UserAgentTreeFlattener() {
        analyzer = new UserAgentAnalyzerDirect(); // Set unused value
    }

    public UserAgentTreeFlattener(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void clear() {
        state = null;
    }

    private boolean verbose = false;

    public void setVerbose(boolean newVerbose) {
        this.verbose = newVerbose;
    }

    public UserAgent parse(String userAgentString) {
        MutableUserAgent userAgent = new MutableUserAgent(userAgentString);
        return parseIntoCleanUserAgent(userAgent);
    }

    public MutableUserAgent parse(MutableUserAgent userAgent) {
        userAgent.reset();
        return parseIntoCleanUserAgent(userAgent);
    }

    /**
     * Parse the useragent and return every part that was found.
     *
     * @param userAgent The useragent instance that needs to be parsed
     * @return If the parse was valid (i.e. were there any parser errors: true=valid; false=has errors
     */
    private MutableUserAgent parseIntoCleanUserAgent(MutableUserAgent userAgent) {
        if (userAgent.getUserAgentString() == null) {
            userAgent.set(SYNTAX_ERROR, "true", 1);
            return userAgent; // Cannot parse this
        }

        // Parse the userAgent into tree
        UserAgentContext<MatcherTree> userAgentContext = parseUserAgent(userAgent);

        // Walk the tree an inform the calling analyzer about all the nodes found
        state = new ParseTreeProperty<>();

        State rootState = new State(AGENT);
        rootState.calculatePath(PathType.CHILD, false);
        state.put(userAgentContext, rootState);

        if (userAgent.hasSyntaxError()) {
            inform(null, SYNTAX_ERROR, "true");
        } else {
            inform(null, SYNTAX_ERROR, "false");
        }

        new ParseTreeWalker<MatcherTree>().walk(this, userAgentContext);
        return userAgent;
    }

    // =================================================================================

    private String inform(ParseTree<MatcherTree> ctx, String path) {
        return inform(ctx, path, getSourceText((ParserRuleContext<MatcherTree>)ctx));
    }

    private String inform(ParseTree<MatcherTree> ctx, String name, String value) {
        return inform(ctx, ctx, name, value, false);
    }

    private String inform(ParseTree<MatcherTree> ctx, String name, String value, boolean fakeChild) {
        return inform(ctx, ctx, name, value, fakeChild);
    }

    private String inform(ParseTree<MatcherTree> stateCtx, ParseTree<MatcherTree> ctx, String name, String value, boolean fakeChild) {
        String path = name;
        if (stateCtx != null) {
            State myState = new State(stateCtx, name);

            if (!fakeChild) {
                state.put(stateCtx, myState);
            }

            PathType childType;
            switch (name) {
                case COMMENTS:
                    childType = PathType.COMMENT;
                    break;
                case VERSION:
                    childType = PathType.VERSION;
                    break;
                default:
                    childType = PathType.CHILD;
            }

            path = myState.calculatePath(childType, fakeChild);
        }
        analyzer.inform(path, value, ctx);
        return path;
    }

//  =================================================================================

    private UserAgentContext<MatcherTree> parseUserAgent(MutableUserAgent userAgent) {
        String userAgentString = EvilManualUseragentStringHacks.fixIt(userAgent.getUserAgentString());

        CodePointCharStream input = CharStreams.fromString(userAgentString);
        UserAgentLexer lexer = new UserAgentLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        UserAgentParser<MatcherTree> parser = new UserAgentParser<>(tokens);

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
    public void enterUserAgent(UserAgentContext<MatcherTree> ctx) {
        // In case of a parse error the 'parsed' version of agent can be incomplete
        inform(ctx, AGENT, ctx.start.getTokenSource().getInputStream().toString());
    }

    @Override
    public void enterRootText(RootTextContext<MatcherTree> ctx) {
        informSubstrings(ctx, TEXT);
    }

    @Override
    public void enterProduct(ProductContext<MatcherTree> ctx) {
        informSubstrings(ctx, PRODUCT);
    }

    @Override
    public void enterCommentProduct(CommentProductContext<MatcherTree> ctx) {
        informSubstrings(ctx, PRODUCT);
    }

    @Override
    public void enterProductNameNoVersion(UserAgentParser.ProductNameNoVersionContext<MatcherTree> ctx) {
        informSubstrings(ctx, PRODUCT);
    }

    @Override
    public void enterProductNameEmail(ProductNameEmailContext<MatcherTree> ctx) {
        inform(ctx, NAME);
    }

    @Override
    public void enterProductNameUrl(ProductNameUrlContext<MatcherTree> ctx) {
        inform(ctx, NAME);
    }

    @Override
    public void enterProductNameWords(ProductNameWordsContext<MatcherTree> ctx) {
        informSubstrings(ctx, NAME);
    }

    @Override
    public void enterProductNameKeyValue(ProductNameKeyValueContext<MatcherTree> ctx) {
        inform(ctx, "name.(1)keyvalue", ctx.getText(), false);
        informSubstrings(ctx, NAME, true);
    }

    @Override
    public void enterProductNameVersion(ProductNameVersionContext<MatcherTree> ctx) {
        informSubstrings(ctx, NAME);
    }

    @Override
    public void enterProductNameUuid(ProductNameUuidContext<MatcherTree> ctx) {
        inform(ctx, NAME);
    }

    @Override
    public void enterProductVersion(ProductVersionContext<MatcherTree> ctx) {
        enterProductVersion((ParseTree<MatcherTree>)ctx);
    }

    @Override
    public void enterProductVersionWithCommas(ProductVersionWithCommasContext<MatcherTree> ctx) {
        enterProductVersion(ctx);
    }

    private void enterProductVersion(ParseTree<MatcherTree> ctx) {
        ParseTree<MatcherTree> child = ctx.getChild(0);
        // Only for the SingleVersion edition we want to have splits of the version.
        if (child instanceof SingleVersionContext || child instanceof SingleVersionWithCommasContext) {
            return;
        }

        inform(ctx, VERSION);
    }

    @Override
    public void enterProductVersionSingleWord(UserAgentParser.ProductVersionSingleWordContext<MatcherTree> ctx) {
        inform(ctx, VERSION);
    }

    @Override
    public void enterSingleVersion(SingleVersionContext<MatcherTree> ctx) {
        informSubVersions(ctx);
    }

    @Override
    public void enterSingleVersionWithCommas(SingleVersionWithCommasContext<MatcherTree> ctx) {
        informSubVersions(ctx);
    }

    @Override
    public void enterProductVersionWords(ProductVersionWordsContext<MatcherTree> ctx) {
        informSubstrings(ctx, VERSION);
    }

    @Override
    public void enterKeyValueProductVersionName(KeyValueProductVersionNameContext<MatcherTree> ctx) {
        informSubstrings(ctx, VERSION);
    }

    @Override
    public void enterCommentBlock(CommentBlockContext<MatcherTree> ctx) {
        inform(ctx, COMMENTS);
    }

    @Override
    public void enterCommentEntry(CommentEntryContext<MatcherTree> ctx) {
        informSubstrings(ctx, "entry");
    }

    private void informSubstrings(ParserRuleContext<MatcherTree> ctx, String name) {
        informSubstrings(ctx, name, false);
    }

    private void informSubstrings(ParserRuleContext<MatcherTree> ctx, String name, boolean fakeChild) {
        informSubstrings(ctx, name, fakeChild, WordSplitter.getInstance());
    }

    private void informSubVersions(ParserRuleContext<MatcherTree> ctx) {
        informSubstrings(ctx, VERSION, false, VersionSplitter.getInstance());
    }

    private void informSubstrings(ParserRuleContext<MatcherTree> ctx, String name, boolean fakeChild, Splitter splitter) {
        String text = getSourceText(ctx);
        String path = inform(ctx, name, text, fakeChild);
//        Set<Range> ranges = analyzer.getRequiredInformRanges(path);

//        if (ranges.size() > 4) { // Benchmarks showed this to be the breakeven point. (see below)
//            List<Pair<Integer, Integer>> splitList = splitter.createSplitList(text);
//            for (Range range : ranges) {
//                String value = splitter.getSplitRange(text, splitList, range);
//                if (value != null) {
//                    inform(ctx, ctx, name + range, value, true);
//                }
//            }
//        } else {
//            for (Range range : ranges) {
//                String value = splitter.getSplitRange(text, range);
//                if (value != null) {
//                    inform(ctx, ctx, name + range, value, true);
//                }
//            }
//        }
    }

    // # Ranges | Direct                   |  SplitList
    // 1        |    1.664 ± 0.010  ns/op  |    99.378 ± 1.548  ns/op
    // 2        |   38.103 ± 0.479  ns/op  |   115.808 ± 1.055  ns/op
    // 3        |  109.023 ± 0.849  ns/op  |   141.473 ± 6.702  ns/op
    // 4        |  162.917 ± 1.842  ns/op  |   166.120 ± 7.166  ns/op  <-- Break even
    // 5        |  264.877 ± 6.264  ns/op  |   176.334 ± 3.999  ns/op
    // 6        |  356.914 ± 2.573  ns/op  |   196.640 ± 1.306  ns/op
    // 7        |  446.930 ± 3.329  ns/op  |   215.499 ± 3.410  ns/op
    // 8        |  533.153 ± 2.250  ns/op  |   233.241 ± 5.311  ns/op
    // 9        |  519.130 ± 3.495  ns/op  |   250.921 ± 6.107  ns/op

    @Override
    public void enterMultipleWords(MultipleWordsContext<MatcherTree> ctx) {
        informSubstrings(ctx, TEXT);
    }

    @Override
    public void enterKeyValue(KeyValueContext<MatcherTree> ctx) {
        inform(ctx, KEYVALUE);
    }

    @Override
    public void enterKeyWithoutValue(UserAgentParser.KeyWithoutValueContext<MatcherTree> ctx) {
        inform(ctx, KEYVALUE);
    }

    @Override
    public void enterKeyName(KeyNameContext<MatcherTree> ctx) {
        informSubstrings(ctx, KEY);
    }

    @Override
    public void enterKeyValueVersionName(KeyValueVersionNameContext<MatcherTree> ctx) {
        informSubstrings(ctx, VERSION);
    }

    @Override
    public void enterVersionWords(VersionWordsContext<MatcherTree> ctx) {
        informSubstrings(ctx, TEXT);
    }

    @Override
    public void enterSiteUrl(SiteUrlContext<MatcherTree> ctx) {
        inform(ctx, URL, ctx.url.getText());
    }

    @Override
    public void enterUuId(UuIdContext<MatcherTree> ctx) {
        inform(ctx, UUID, ctx.uuid.getText());
    }

    @Override
    public void enterEmailAddress(EmailAddressContext<MatcherTree> ctx) {
        inform(ctx, EMAIL, ctx.email.getText());
    }

    @Override
    public void enterBase64(Base64Context<MatcherTree> ctx) {
        inform(ctx, BASE64, ctx.value.getText());
    }

    @Override
    public void enterEmptyWord(EmptyWordContext<MatcherTree> ctx) {
        inform(ctx, TEXT, "");
    }

    @Override
    public String toString() {
        return "UserAgentTreeFlattener{" +
            " verbose=" + verbose +
            "} ";
    }
}
