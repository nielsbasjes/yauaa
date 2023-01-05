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

package nl.basjes.parse.useragent.config;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AnalyzerConfigHolder {
    /**
     * Obtain the config for this analyzer.
     * @return The instance of the config used by this analyzer.
     */
    @Nonnull
    AnalyzerConfig getConfig();

    /**
     * Obtain all available testcases packaged with this analyzer.
     * @return List of available TestCases.
     */
    default List<TestCase> getTestCases() {
        return getConfig().getTestCases();
    }

    default long getNumberOfTestCases() {
        return getTestCases().size();
    }

    default void dropTests() {
        getTestCases().clear();
    }

    default Map<String, Map<String, String>> getLookups() {
        return getConfig().getLookups();
    }

    default Map<String, Set<String>> getLookupSets() {
        return getConfig().getLookupSets();
    }

    default int getUserAgentMaxLength() {
        return getConfig().getUserAgentMaxLength();
    }

}
