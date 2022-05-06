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

package nl.basjes.parse.useragent;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;

public final class AnalyzerUtilities {

    private AnalyzerUtilities() {
        // Utility class that should not be instantiated
    }

    public static class ParsedArguments {
        @Getter @Setter private Map<String, String> requestHeaders  = new TreeMap<>();
        @Getter @Setter private List<String>        wantedFields    = new ArrayList<>();
    }

    public static @Nonnull ParsedArguments parseArguments(@Nonnull String[] args, @Nonnull List<String> allAllowedFields, @Nonnull List<String> allAllowedHeaders) {
        List<String> input = new ArrayList<>(Arrays.asList(args));
        return parseArguments(input, allAllowedFields, allAllowedHeaders);
    }

    /**
     * In several situations (mostly SQL class UDFs) we see the same need: A sequence of strings must be analyzed and
     * converted into parameters for the UserAgentAnalyzer.
     * Supported format patterns this parser will detect:
     * - useragent
     * - useragent [ headername, headervalue ]+
     * - [ headername, headervalue ]+
     *
     * Notes:
     * - The useragent and any of the headervalues can be null or empty.
     * - A trailing list of null values will be ignored
     * - When not having the useragent as the first value the Analyzers will expect a "User-Agent" header in the list.
     *
     * @param args The list of arguments passed from the user
     * @param allAllowedFields The list of all supported fieldnames (i.e. all possible outputs)
     * @param allAllowedHeaders The list of all supported headers (i.e. all supported inputs)
     * @return The list of request headers and the list of wanted fields. It is up to the calling system to choose which to use.
     */
    public static @Nonnull ParsedArguments parseArguments(@Nonnull List<String> args, @Nonnull List<String> allAllowedFields, @Nonnull List<String> allAllowedHeaders) {
        ParsedArguments parsedArguments = new ParsedArguments();

        int i = 0;
        while (i < args.size()) {
            String argument = args.get(i);
            if (argument == null) {
                if (i == 0) {
                    // First argument can be null --> null useragent
                    parsedArguments.requestHeaders.put(USERAGENT_HEADER, null);
                    i++;
                    continue;
                } else {
                    // This is in general NOT allowed
                    // except when all remaining arguments are also null then we ignore the rest of the list
                    int locationOfFirstNull = i;
                    while (i < args.size()) {
                        if (args.get(i) != null) {
                            throw new IllegalArgumentException("Null argument provided to ParseUserAgent (Argument #" + locationOfFirstNull + " [0=first]).");
                        }
                        i++;
                    }
                    // Apparently we have a list of trailing null values which we all ignore.
                    return parsedArguments;
                }
            }
            if (allAllowedFields.stream().anyMatch(argument::equalsIgnoreCase)) {
                parsedArguments.wantedFields.add(argument);
                i++;
                continue;
            }
            if (allAllowedHeaders.stream().anyMatch(argument::equalsIgnoreCase)) {
                String value;
                if (i + 1 >= args.size()) {
                    throw new IllegalArgumentException("Invalid last element in argument list (was a header name which requires a value to follow)");
                } else {
                    value = args.get(i + 1);
                    i++;
                }
                parsedArguments.requestHeaders.put(argument, value);
                i++;
                continue;
            }
            if (i == 0) {
                parsedArguments.requestHeaders.put(USERAGENT_HEADER, argument);
                i++;
                continue;
            }
            if (argument.isEmpty()) {
                throw new IllegalArgumentException("Empty argument provided to ParseUserAgent (Argument #" + i + " [0=first]).");
            }
            throw new IllegalArgumentException("Unknown argument provided to ParseUserAgent (Argument #" + i + " [0=first]).");
        }

        return parsedArguments;
    }

    // ===============================================================================================================

}
