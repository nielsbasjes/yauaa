/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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

package nl.basjes.parse.useragent.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class TestStringTable {

    private static final Logger LOG = LogManager.getLogger(TestStringTable.class);

    @Test
    void testStringTable() {
        StringTable table = new StringTable();
        LOG.error("\n{}", table
            .withHeaders("One", "Two", "Three")
            .addRow("1", "2", "3", "4")
            .addRowSeparator()
            .addRow("11",       "22")
            .addRow("1111",     "2222",     "33")
            .addRow("111111",   "222222",   "3333",     "444444444")
            .addRow("11111111", "22222222", "333333",   "4444", "55"));
    }

}
