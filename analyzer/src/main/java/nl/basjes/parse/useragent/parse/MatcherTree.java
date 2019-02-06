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
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown.UserAgentGetChildrenVisitor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Locale.ENGLISH;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.EQUALS;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.STARTSWITH;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.WORDRANGE;

public class MatcherTree implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(MatcherTree.class);

    private MatcherTree parent = null;

    private void setParent(MatcherTree nParent) {
        this.fragmentName = ".(" + (index+1) + ")" + fragment.name().toLowerCase(ENGLISH);
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

    private EnumMap<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor>> children;

    public MatcherTree(AgentPathFragment fragment, int index) {
        this.fragment = fragment;
        this.fragmentName = fragment.name().toLowerCase(ENGLISH);
        this.index = index;

        children = new EnumMap<>(AgentPathFragment.class);

//        for (AgentPathFragment childFragment: AgentPathFragment.values()) {
//            children.put(childFragment, new ArrayList<>());
//        }
    }

    public void makeItWordRange(int nFirstWord, int nLastWord) {
        if (this.fragment != WORDRANGE) {
            throw new IllegalArgumentException("When you specify a first/last word it MUST be WORDRANGE");
        }
        this.firstWord = nFirstWord;
        this.lastWord = nLastWord;
        this.fragmentName = "[" + firstWord + "-" + lastWord + "]";
    }

    public void makeItEquals(String nMatchString) {
        if (fragment != EQUALS) {
            throw new IllegalArgumentException("When you specify a matchString it MUST be either EQUALS or STARTSWITH 1"); // FIXME
        }
        this.matchString= nMatchString;
        fragmentName = "=\"" + matchString + "\"";
//        LOG.warn("Setting equals to path {}", this);
    }

    public void makeItStartsWith(String nMatchString) {
        if (fragment != STARTSWITH) {
            throw new IllegalArgumentException("When you specify a matchString it MUST be either EQUALS or STARTSWITH 2 "); // FIXME
        }
        this.matchString= nMatchString;
        fragmentName = "{\"" + matchString + "\"";
//        LOG.warn("Setting startWith to path {}", this);
    }

    private Set<MatcherAction> actions = new HashSet<>();

    public Set<MatcherAction> getActions() {
        return actions;
    }

    public void addMatcherAction(MatcherAction action) {
        actions.add(action);
//        LOG.warn("Adding to path \"{}\" action:    {}", this, action.getMatchExpression());
    }

//    public void addMatcherAction(Set<MatcherAction> newActions) {
//        actions.addAll(newActions);
//    }

    public MatcherTree getOrCreateChild(AgentPathFragment newChildFragment, int newChildIndex) {
//        LOG.info("[getOrCreateChild]>: {} --- {} --- {}", this, newChildFragment, newChildIndex);
        Pair<List<MatcherTree>, UserAgentGetChildrenVisitor> childrenn = children
            .computeIfAbsent(newChildFragment, k ->
                Pair.of(new ArrayList<>(10), new UserAgentGetChildrenVisitor(newChildFragment, 0, 20)));
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

    public EnumMap<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor>> getChildren() {
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
            results.add(fragmentName + "                           -- { actions : "+ actions.size()+" }");
        }

        for (Map.Entry<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor>> childrenPerType : children.entrySet()) {
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
            if (thisShouldBeThis == null || thisShouldBeThis != this) {
                LOG.error("Verify FAIL: Parent ({}) has the wrong {} child at {} for {}", parent, fragment, index, this);
                return false;
            }
        }

        for (Map.Entry<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor>> childrenPerType : children.entrySet()) {
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
        for (Map.Entry<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor>> childrenPerType : children.entrySet()) {
            for (MatcherTree child : childrenPerType.getValue().getKey()) {
                if (child != null) {
                    size += child.size();
                }
            }
        }
        return size;
    }
}
