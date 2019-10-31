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

package nl.basjes.parse.useragent.parse;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown.UserAgentGetChildrenVisitor;
import nl.basjes.parse.useragent.parser.UserAgentBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentLexer;
import nl.basjes.parse.useragent.parser.UserAgentParser;
import nl.basjes.parse.useragent.parser.UserAgentParser.Base64Context;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentBlockContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentEntryContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentProductContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentSeparatorContext;
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
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionSingleWordContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionWithCommasContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.RootElementsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.RootTextContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SingleVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SingleVersionWithCommasContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SiteUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UserAgentContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UuIdContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.VersionWordsContext;
import nl.basjes.parse.useragent.utils.AntlrUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.AGENT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.BASE64;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.COMMENTS;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.EMAIL;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.ENTRY;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.KEY;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.KEYVALUE;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.NAME;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.PRODUCT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.TEXT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.URL;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.UUID;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.VERSION;

// FIXME: Checkstyle cleanup
// CHECKSTYLE.OFF: LineLength
//@SuppressWarnings({"ALL"})
public class UserAgentTreeFlattener implements Serializable {

    private final Analyzer analyzer;

    private static class MatchFinder extends UserAgentBaseVisitor<Void, MatcherTree> {

        // CHECKSTYLE.OFF: NoWhitespaceAfter
        // CHECKSTYLE.OFF: NoWhitespaceBefore
        // CHECKSTYLE.OFF: WhitespaceAfter
        // CHECKSTYLE.OFF: MethodParamPad
        // CHECKSTYLE.OFF: ParenPad
        // CHECKSTYLE.OFF: LeftCurly

        // The root case
        // In case of a parse error the 'parsed' version of agent can be incomplete
        @Override        public Void visitUserAgent(                    UserAgentContext                    <MatcherTree> uaTree, MatcherTree mTree) { return match(AGENT,    uaTree, mTree, uaTree.start.getTokenSource().getInputStream().toString()); }

        @Override        public Void visitRootText(                     RootTextContext                     <MatcherTree> uaTree, MatcherTree mTree) { return match(TEXT,     uaTree, mTree, null); }
        @Override        public Void visitProduct(                      ProductContext                      <MatcherTree> uaTree, MatcherTree mTree) { return match(PRODUCT,  uaTree, mTree, null); }
        @Override        public Void visitCommentProduct(               CommentProductContext               <MatcherTree> uaTree, MatcherTree mTree) { return match(PRODUCT,  uaTree, mTree, null); }
        @Override        public Void visitProductNameNoVersion(         ProductNameNoVersionContext         <MatcherTree> uaTree, MatcherTree mTree) { return match(PRODUCT,  uaTree, mTree, null); }
        @Override        public Void visitProductName(                  ProductNameContext                  <MatcherTree> uaTree, MatcherTree mTree) { return match(NAME,     uaTree, mTree, null); }
        @Override        public Void visitProductNameEmail(             ProductNameEmailContext             <MatcherTree> uaTree, MatcherTree mTree) { return match(NAME,     uaTree, mTree, uaTree.emailAddress().getText()); }
        @Override        public Void visitProductNameUrl(               ProductNameUrlContext               <MatcherTree> uaTree, MatcherTree mTree) { return match(NAME,     uaTree, mTree, uaTree.siteUrl().getText()); }
        @Override        public Void visitProductNameWords(             ProductNameWordsContext             <MatcherTree> uaTree, MatcherTree mTree) { return match(NAME,     uaTree, mTree, null); }
        @Override        public Void visitProductNameVersion(           ProductNameVersionContext           <MatcherTree> uaTree, MatcherTree mTree) { return match(NAME,     uaTree, mTree, null); }
        @Override        public Void visitProductNameUuid(              ProductNameUuidContext              <MatcherTree> uaTree, MatcherTree mTree) { return match(NAME,     uaTree, mTree, null); }
        @Override        public Void visitProductVersionSingleWord(     ProductVersionSingleWordContext     <MatcherTree> uaTree, MatcherTree mTree) { return match(VERSION,  uaTree, mTree, null); }
        @Override        public Void visitSingleVersion(                SingleVersionContext                <MatcherTree> uaTree, MatcherTree mTree) { return match(VERSION,  uaTree, mTree, null); }
        @Override        public Void visitSingleVersionWithCommas(      SingleVersionWithCommasContext      <MatcherTree> uaTree, MatcherTree mTree) { return match(VERSION,  uaTree, mTree, null); }
        @Override        public Void visitProductVersionWords(          ProductVersionWordsContext          <MatcherTree> uaTree, MatcherTree mTree) { return match(VERSION,  uaTree, mTree, null); }
        @Override        public Void visitKeyValueProductVersionName(   KeyValueProductVersionNameContext   <MatcherTree> uaTree, MatcherTree mTree) { return match(VERSION,  uaTree, mTree, null); }
        @Override        public Void visitCommentBlock(                 CommentBlockContext                 <MatcherTree> uaTree, MatcherTree mTree) { return match(COMMENTS, uaTree, mTree, null); }
        @Override        public Void visitCommentEntry(                 CommentEntryContext                 <MatcherTree> uaTree, MatcherTree mTree) { return match(ENTRY,    uaTree, mTree, null); }
        @Override        public Void visitMultipleWords(                MultipleWordsContext                <MatcherTree> uaTree, MatcherTree mTree) { return match(TEXT,     uaTree, mTree, null); }
        @Override        public Void visitKeyValue(                     KeyValueContext                     <MatcherTree> uaTree, MatcherTree mTree) { return match(KEYVALUE, uaTree, mTree, null); }
        @Override        public Void visitKeyWithoutValue(              KeyWithoutValueContext              <MatcherTree> uaTree, MatcherTree mTree) { return match(KEYVALUE, uaTree, mTree, null); }
        @Override        public Void visitKeyName(                      KeyNameContext                      <MatcherTree> uaTree, MatcherTree mTree) { return match(KEY,      uaTree, mTree, null); }
        @Override        public Void visitKeyValueVersionName(          KeyValueVersionNameContext          <MatcherTree> uaTree, MatcherTree mTree) { return match(VERSION,  uaTree, mTree, null); }
        @Override        public Void visitVersionWords(                 VersionWordsContext                 <MatcherTree> uaTree, MatcherTree mTree) { return match(TEXT,     uaTree, mTree, null); }
        @Override        public Void visitSiteUrl(                      SiteUrlContext                      <MatcherTree> uaTree, MatcherTree mTree) { return match(URL,      uaTree, mTree, uaTree.url.getText());   }
        @Override        public Void visitUuId(                         UuIdContext                         <MatcherTree> uaTree, MatcherTree mTree) { return match(UUID,     uaTree, mTree, uaTree.uuid.getText());  }
        @Override        public Void visitEmailAddress(                 EmailAddressContext                 <MatcherTree> uaTree, MatcherTree mTree) { return match(EMAIL,    uaTree, mTree, uaTree.email.getText()); }
        @Override        public Void visitBase64(                       Base64Context                       <MatcherTree> uaTree, MatcherTree mTree) { return match(BASE64,   uaTree, mTree, uaTree.value.getText()); }
        @Override        public Void visitEmptyWord(                    EmptyWordContext                    <MatcherTree> uaTree, MatcherTree mTree) { return match(TEXT,     uaTree, mTree, "");   }

        // FIXME: Fakechild = false ...  inform(ctx, "name.(1)keyvalue", ctx.getText(), false);
        // FIXME: Fakechild = true....
//        @Override
//        public void enterProductNameKeyValue(ProductNameKeyValueContext ctx) {
//            inform(ctx, "name.(1)keyvalue", ctx.getText(), false);
//            informSubstrings(ctx, NAME, true);
//        }


        @Override        public Void visitProductNameKeyValue(          ProductNameKeyValueContext          <MatcherTree> uaTree, MatcherTree mTree) {
            return match(TEXT,     uaTree, mTree, null);
        }
        @Override        public Void visitProductVersion(               ProductVersionContext               <MatcherTree> uaTree, MatcherTree mTree) { return match(VERSION,  uaTree, mTree, null); }
        @Override        public Void visitProductVersionWithCommas(     ProductVersionWithCommasContext     <MatcherTree> uaTree, MatcherTree mTree) { return match(VERSION,  uaTree, mTree, null); }
        // SAME AS DEFAULT IMPLEMENTATION
        @Override        public Void visitRootElements(                 RootElementsContext                 <MatcherTree> uaTree, MatcherTree mTree) { return visitChildren(uaTree, mTree); }
        @Override        public Void visitCommentSeparator(             CommentSeparatorContext             <MatcherTree> uaTree, MatcherTree mTree) { return visitChildren(uaTree, mTree); }

        private Void match(AgentPathFragment uaTreeFragment, RuleNode<MatcherTree> uaTree, MatcherTree mTree, String value) {
//            LOG.warn("[match] Fragment:{} \t| Match:{} \t| Useragent:{} \t| Value:{} |", uaTreeFragment, mTree, AntlrUtils.getSourceText(uaTree), value);

            if (mTree == null) {// || uaTree == null) {
                return null; // Nothing can be here.
            }

            mTree.fireMatchingActions(uaTree, value);

            // For each of the possible child fragments
            for (Map.Entry<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor<MatcherTree>>> agentPathFragment : mTree.getChildren().entrySet()) {

                // Find the subnodes for which we actually have patterns
                List<MatcherTree>                          relevantMatcherSubTrees = agentPathFragment.getValue().getKey();

                Iterator<? extends ParseTree<MatcherTree>> children                = agentPathFragment.getValue().getValue().visit(uaTree);

//                // ============================================
//                // !!! Construct LIST FOR DEBUGGING !!!
////                List<ParseTree<MatcherTree>> childrenList = new ArrayList<>(32);
//                for (int i = 0 ; i < 32 ; i++) {
//                    childrenList.add(0, null);  // FIXME: YUCK ! YUCK ! YUCK ! YUCK !
//                }
//                int i = 0;
//                final Iterator<? extends ParseTree<MatcherTree>> childrenForList = agentPathFragment.getValue().getValue().visit(uaTree);
//                while (childrenForList.hasNext()) {
//                    i++;
//                    final ParseTree<MatcherTree> next = childrenForList.next();
//                    childrenList.add(i, next);
//                }
//                // ============================================

                int maxMatcherIndex = relevantMatcherSubTrees.size()-1;

                int index = 0;

                // FIXME: Workaroud: For keyvalue we want the values to start at '2'...
                if (mTree.fragment == KEYVALUE && agentPathFragment.getKey() != KEY) {
                    index = 1;
                }

                while (children.hasNext() && index < maxMatcherIndex) {
                    index++;
                    ParseTree<MatcherTree> uaSubTree = children.next();
                    if (uaSubTree == null) {
                        continue;
                    }

                    MatcherTree mSubTree = relevantMatcherSubTrees.get(index);
                    if (mSubTree == null) {
                        continue;
                    }

                    uaSubTree.accept(this, mSubTree);
                }
            }
            return null;
        }

        private Void verifyMatchProductVersion(RuleNode<MatcherTree> uaTree, MatcherTree mTree) {
            return match(VERSION, uaTree, mTree, null);
//            if (uaTree.getChildCount() != 1) {
////                 These are the specials with multiple children like keyvalue, etc.
//                return match(VERSION, uaTree, mTree,  null);
//            }
//
//            ParserRuleContext<MatcherTree> child = (ParserRuleContext<MatcherTree>) uaTree.getChild(0);
////             Only for the SingleVersion edition we want to have splits of the version.
//            if (child instanceof SingleVersionContext || child instanceof SingleVersionWithCommasContext) {
//                return null;
//            }
//
//            return match(VERSION, child, mTree, null);
        }

    }




    private static final Logger LOG = LoggerFactory.getLogger(UserAgentTreeFlattener.class);



    // FIXME: Later ... private constructor for serialization systems ONLY (like Kyro)
//    private UserAgentTreeFlattener() {
//        analyzer = null;
//    }

    public UserAgentTreeFlattener(Analyzer analyzer) {
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
        UserAgentContext<MatcherTree> userAgentTree = parseUserAgent(userAgent);

        // Match the agent against the patterns
        new MatchFinder().visit(userAgentTree, analyzer.getMatcherTreeRoot());
//        match(AGENT, , userAgent.getUserAgentString());

//        if (userAgent.hasSyntaxError()) {
//            inform(null, SYNTAX_ERROR, "true");
//        } else {
//            inform(null, SYNTAX_ERROR, "false");
//        }

        return userAgent;
    }

    // =================================================================================

//    private String inform(ParseTree<Void> ctx, AgentPathFragment path) {
//        return inform(ctx, path, getSourceText((ParserRuleContext)ctx));
//    }

//    private String inform(ParseTree<Void> ctx, AgentPathFragment name, String value) {
//        return inform(ctx, ctx, name, value, false);
//    }

//    private String inform(ParseTree<Void> ctx, AgentPathFragment name, String value, boolean fakeChild) {
//        return inform(ctx, ctx, name, value, fakeChild);
//    }

//    private String inform(ParseTree<Void> stateCtx, ParseTree<Void> ctx, AgentPathFragment name, String value, boolean fakeChild) {
//        AgentPathFragment path = name;
//        if (stateCtx == null) {
//            analyzer.inform(path, value, ctx);
//        } else {
//            State myState = new State(stateCtx, name);
//
//            if (!fakeChild) {
//                state.put(stateCtx, myState);
//            }
//
//            PathType childType;
//            switch (name) {
//                case COMMENTS:
//                    childType = PathType.COMMENT;
//                    break;
//                case VERSION:
//                    childType = PathType.VERSION;
//                    break;
//                default:
//                    childType = PathType.CHILD;
//            }
//
//            path = myState.calculatePath(childType, fakeChild);
//            analyzer.inform(path, value, ctx);
//        }
//        return path.toString();
//    }

//  =================================================================================

    private UserAgentContext<MatcherTree> parseUserAgent(UserAgent userAgent) {
        String userAgentString = EvilManualUseragentStringHacks.fixIt(userAgent.getUserAgentString());

        CodePointCharStream input = CharStreams.fromString(userAgentString);
        UserAgentLexer      lexer = new UserAgentLexer(input);

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


//    private void informSubstrings(ParserRuleContext<Void> ctx, AgentPathFragment name) {
//        informSubstrings(ctx, name, false);
//    }

//    private void informSubstrings(ParserRuleContext<Void> ctx, AgentPathFragment name, boolean fakeChild) {
//        informSubstrings(ctx, name, fakeChild, WordSplitter.getInstance());
//    }

//    private void informSubVersions(ParserRuleContext<Void> ctx, AgentPathFragment name) {
//        informSubstrings(ctx, name, false, VersionSplitter.getInstance());
//    }

//    private void informSubstrings(ParserRuleContext<Void> ctx, AgentPathFragment name, boolean fakeChild, Splitter splitter) {
//        String text = getSourceText(ctx);
//        String path = inform(ctx, name, text, fakeChild);
//        Set<Range> ranges = analyzer.getRequiredInformRanges(path);
//
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
//    }

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

}
