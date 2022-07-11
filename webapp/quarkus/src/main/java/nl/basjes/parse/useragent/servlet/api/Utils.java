/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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

package nl.basjes.parse.useragent.servlet.api;

import java.util.ArrayList;
import java.util.List;

public final class Utils {
    private Utils() {
    }

    public static List<String> splitPerFilledLine(String input) {
        String[] lines = input.split("\\r?\\n");
        List<String> result = new ArrayList<>(lines.length);
        for (String line: lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                result.add(trimmedLine);
            }
        }
        if (result.isEmpty()) {
            // Apparently the only input was an empty string, we want to keep that.
            result.add("");
        }
        return result;
    }
}
