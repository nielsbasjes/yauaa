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

import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown.UserAgentGetChildrenVisitor;
import nl.basjes.parse.useragent.utils.AntlrUtils;
import nl.basjes.parse.useragent.utils.WordSplitter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static java.util.Locale.ENGLISH;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.EQUALS;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.STARTSWITH;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.WORDRANGE;

public class MatcherTree implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(MatcherTree.class);

    private MatcherTree parent = null;

    private void setParent(MatcherTree nParent) {
        this.fragmentName = ".(" + index + ")" + fragment.name().toLowerCase(ENGLISH);
        this.parent = nParent;
    }

    // MY position with my parent.
    // Root element = agent (1)
    private final AgentPathFragment fragment;
    private String                  fragmentName;
    private final int               index;

    // TODO: A tree structure with all words separately
    // IFF fragment == WORDRANGE
    private int firstWord = -1;
    private int lastWord = -1;

    // IFF fragment == EQUALS or STARTSWITH
    private String matchString = "";

    // Assume "agent.(5)product" and this node is "agent"
    // Then the enum 'product' is the index in the children.
    // The result is a list of child trees and a precomputed visitor for 'product'
    // Because we want '(5)product' we must do a get(5) on the list to find the correct subtree to recurse into
    private EnumMap<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor<MatcherTree>>> children;

    // Each node in the tree has an optional set of actions that need to be fired if this is found.
    // Fire always
    private Set<MatcherAction> actions = new HashSet<>();

    // Fire if equals
    private Map<String, List<MatcherAction>> equalsActions = new TreeMap<>();

    // Fire if equals
    private Map<String, List<MatcherAction>> startsWithActions = new TreeMap<>();

    // Fire for each range of words
    private Map<Range, List<MatcherAction>> wordRangeActions = new HashMap<>();

    public MatcherTree(AgentPathFragment fragment, int index) {
        this.fragment = fragment;
        this.fragmentName = fragment.name().toLowerCase(ENGLISH);
        this.index = index;

        children = new EnumMap<>(AgentPathFragment.class);
    }

//    public void makeItWordRange(int nFirstWord, int nLastWord) {
//        if (this.fragment != WORDRANGE) {
//            throw new IllegalArgumentException("When you specify a first/last word it MUST be WORDRANGE");
//        }
//        this.firstWord = nFirstWord;
//        this.lastWord = nLastWord;
//        this.fragmentName = "[" + firstWord + "-" + lastWord + "]";
//    }

    boolean hasAnyActions = false;

    public Set<MatcherAction> getActions() {
        return actions;
    }

    public void addMatcherAction(MatcherAction action) {
        actions.add(action);
        hasAnyActions = true;
    }

    public void addEqualsMatcherAction(String nMatchString, MatcherAction action) {
        List<MatcherAction> actionList = equalsActions.computeIfAbsent(nMatchString, e -> new ArrayList<>());
        actionList.add(action);
        hasAnyActions = true;
    }

    public void addStartsWithMatcherAction(String nMatchString, MatcherAction action) {
        List<MatcherAction> actionList = startsWithActions.computeIfAbsent(nMatchString, e -> new ArrayList<>());
        actionList.add(action);
        hasAnyActions = true;
    }

    public void addWordRangeMatcherAction(Range range, MatcherAction action) {
        List<MatcherAction> actionList = wordRangeActions.computeIfAbsent(range, e -> new ArrayList<>());
        actionList.add(action);
        hasAnyActions = true;
    }

    public void fireMatchingActions(ParseTree<MatcherTree> uaTree, String value) {
        if (!hasAnyActions) {
            return; // Nothing to fire
        }
        // Inform the actions at THIS level that need to be informed.
        String informValue = value == null ? AntlrUtils.getSourceText(uaTree) : value;

        actions.forEach(action ->
            action.inform(this, uaTree, informValue)
        );

        equalsActions.forEach((text, actionList) -> {
            if (informValue.equals(text)) {
                actionList.forEach(action ->
                    action.inform(this, uaTree, informValue)
                );
            }
        });

        startsWithActions.forEach((text, actionList) -> {
            if (informValue.startsWith(text)) {
                actionList.forEach(action ->
                    action.inform(this, uaTree, informValue)
                );
            }
        });

        wordRangeActions.forEach((wordRange, actionList) -> {
            String words = WordSplitter.getInstance().getSplitRange(informValue, wordRange);
            actionList.forEach(action ->
                action.inform(this, uaTree, words)
            );
        });
    }

    public MatcherTree getOrCreateChild(AgentPathFragment newChildFragment, int newChildIndex) {
//        LOG.info("[getOrCreateChild]>: {} --- {} --- {}", this, newChildFragment, newChildIndex);
        Pair<List<MatcherTree>, UserAgentGetChildrenVisitor<MatcherTree>> childrenn = children
            .computeIfAbsent(newChildFragment, k ->
                Pair.of(new ArrayList<>(10), new UserAgentGetChildrenVisitor<>(newChildFragment, 0, 20)));
        List<MatcherTree> childrenList =  childrenn.getKey();

        MatcherTree child = null;

        if (childrenList.size() > newChildIndex) {
            child = childrenList.get(newChildIndex);
        }
        if (child == null) {
            child = new MatcherTree(newChildFragment, newChildIndex);

            // WTF: If you have an ArrayList with a capacity of N elements, you CANNOT set an element at
            // an index within this range which is higher than the number of element already in there.
            while (childrenList.size() <= newChildIndex) {
                childrenList.add(null);
            }

            childrenList.set(newChildIndex, child);
            child.setParent(this);
        }
//        LOG.info("[getOrCreateChild]<: {}", child);
        return child;
    }

    public MatcherTree getChild(AgentPathFragment child, int childIndex) {
        List<MatcherTree> childList = children.get(child).getKey();
        if (childList == null) {
            return null;
        }
        return childList.get(childIndex);
    }

    public EnumMap<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor<MatcherTree>>> getChildren() {
        return children;
    }

    private String myToString = null;

    @Override
    public String toString() {
        if (myToString == null) {
            if (parent == null) {
                myToString = fragmentName;
            } else {
                myToString = parent.toString() + fragmentName;
            }
        }
        return myToString;
    }

    public List<String> getChildrenStrings() {
        List<String> results = new ArrayList<>();

        if (children.isEmpty() || !actions.isEmpty()) {
            results.add(fragmentName
                + "                           -- { actions : "+ actions.size()+" }"
            );
        }

        for (Map.Entry<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor<MatcherTree>>> childrenPerType : children.entrySet()) {
            for (MatcherTree child : childrenPerType.getValue().getKey()) {
                if (child != null) {
                    final String fn = fragmentName;
                    child.getChildrenStrings().forEach(cs -> results.add(fn + cs));
                }
            }
        }
        return results;
    }

    public boolean verifyTree() {
        if (parent != null) {
            List<MatcherTree> siblings = parent.children.get(fragment).getKey();
            if (siblings.size() <= index) {
                LOG.error("Verify FAIL: Parent does not have enough siblings: {}", this);
                return false;
            }
            MatcherTree thisShouldBeThis = siblings.get(index);
            if (thisShouldBeThis != this) {
                LOG.error("Verify FAIL: Parent ({}) has the wrong {} child at {} for {}", parent, fragment, index, this);
                return false;
            }
        }

        for (Map.Entry<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor<MatcherTree>>> childrenPerType : children.entrySet()) {
            List<MatcherTree> childrenPerTypeValue = childrenPerType.getValue().getKey();
            for (MatcherTree child : childrenPerTypeValue) {
                if (child != null) {
                    if (!child.verifyTree()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public long size() {
        long size = 1;
        for (Map.Entry<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor<MatcherTree>>> childrenPerType : children.entrySet()) {
            for (MatcherTree child : childrenPerType.getValue().getKey()) {
                if (child != null) {
                    size += child.size();
                }
            }
        }
        return size;
    }

}
