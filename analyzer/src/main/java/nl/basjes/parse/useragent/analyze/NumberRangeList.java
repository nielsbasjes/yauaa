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

package nl.basjes.parse.useragent.analyze;

import java.util.AbstractList;

public class NumberRangeList extends AbstractList<Integer> {
    private final int start;
    private final int end;

    public NumberRangeList(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Integer get(int index) {
        return start + index;
    }

    @Override
    public int size() {
        return end - start + 1;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
