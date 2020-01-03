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

package nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static nl.basjes.parse.useragent.analyze.treewalker.steps.Step.treeIsSeparator;

public class ChildIterable {

    private final boolean privateNumberRange;
    private final int start;
    private final int end;

    private final Predicate<ParseTree> isWantedClassPredicate;

    public ChildIterable(boolean privateNumberRange,
                         int start, int end,
                         Predicate<ParseTree> isWantedClassPredicate) {
        this.privateNumberRange = privateNumberRange;
        this.start = start;
        this.end = end;
        this.isWantedClassPredicate = isWantedClassPredicate;
    }

    public Iterator<ParseTree> iterator(ParserRuleContext treeContext) {
        return new ChildIterator(treeContext);
    }

    class ChildIterator implements Iterator<ParseTree> {
        private final Iterator<ParseTree> childIter;
        private       Boolean             hasNext;
        private       int                 index = 0;
        private       ParseTree           nextChild;

        ChildIterator(ParserRuleContext treeContext) {
            if (treeContext.children == null) {
                childIter = null;
                nextChild = null;
                hasNext = false;
            } else {
                childIter = treeContext.children.iterator();
                hasNext = findNext(); // We always want the first one
            }
        }

        /**
         * Find and set the nextChild
         * @return If there is a next
         */
        private boolean findNext() {
            while (childIter.hasNext()) {
                ParseTree nextParseTree = childIter.next();
                if (treeIsSeparator(nextParseTree)) {
                    continue;
                }
                if (!privateNumberRange) {
                    index++;
                }
                if (!isWantedClassPredicate.test(nextParseTree)) {
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
                    nextChild = nextParseTree;
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
        public ParseTree next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            hasNext = null;
            return nextChild;
        }
    }

}
