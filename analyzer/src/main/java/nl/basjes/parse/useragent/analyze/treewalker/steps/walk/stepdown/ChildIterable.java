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

package nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static nl.basjes.parse.useragent.analyze.treewalker.steps.Step.treeIsSeparator;

public class ChildIterable {

    private boolean privateNumberRange;
    private final int start;
    private final int end;

    private final Predicate<ParserRuleContext> isWantedClassPredicate;

    public ChildIterable(boolean privateNumberRange,
                         int start, int end,
                         Predicate<ParserRuleContext> isWantedClassPredicate) {
        this.privateNumberRange = privateNumberRange;
        this.start = start;
        this.end = end;
        this.isWantedClassPredicate = isWantedClassPredicate;
    }

    public Iterator<ParserRuleContext> iterator(ParserRuleContext treeContext) {
        return new ChildIterator(treeContext);
    }

    class ChildIterator implements Iterator<ParserRuleContext> {
        private final Iterator<ParseTree> childIterator;
        private Boolean hasNext;
        private int index = 0;
        private ParserRuleContext nextChild;

        ChildIterator(ParserRuleContext treeContext) {
            if (treeContext.children == null) {
                childIterator = null;
                nextChild = null;
                hasNext = false;
            } else {
                childIterator = treeContext.children.iterator();
                hasNext = findNext(); // We always want the first one
            }
        }

        /**
         * Find and set the nextChild
         * @return If there is a next
         */
        private boolean findNext() {
            while (childIterator.hasNext()) {
                ParseTree nextParseTree = childIterator.next();
                if (treeIsSeparator(nextParseTree)) {
                    continue;
                }
                if (!(nextParseTree instanceof ParserRuleContext)) {
                    continue;
                }
                if (!privateNumberRange) {
                    index++;
                }
                ParserRuleContext possibleNextChild = (ParserRuleContext) nextParseTree;
                if (!isWantedClassPredicate.test(possibleNextChild)) {
                    continue;
                }
                if (privateNumberRange) {
                    index++;
                }
                if (index > end) {
                    nextChild = null;
                    return false;
                }
                if (start <= index) {
                    nextChild = possibleNextChild;
                    return true;
                }
            }

            // We found nothing
            nextChild = null;
            return false;
        }

        @Override
        public boolean hasNext() {
            if (hasNext == null) {
                hasNext = findNext();
            }
            return hasNext;
        }

        @Override
        public ParserRuleContext next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            hasNext = null;
            return nextChild;
        }
    }

}
