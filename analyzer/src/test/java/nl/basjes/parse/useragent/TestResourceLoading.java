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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester.UserAgentAnalyzerTesterBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestResourceLoading {

    @Test
    public void checkEmptyAndNormalAndOptionalMissingFile() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .dropDefaultResources()
            .addResources("classpath*:AllSteps.yaml")
            .addResources("classpath*:BadDefinitions/EmptyFile.yaml")
            .addOptionalResources("classpath*:BadDefinitions/ThisOneDoesNotExist---Really.yaml")
            .build();
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkEmptyAndNormalAndMandatoryMissingFile() {
        assertThrows(InvalidParserConfigurationException.class, () -> {
            UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
                .newBuilder()
                .dropDefaultResources()
                .addResources("classpath*:AllSteps.yaml")
                .addResources("classpath*:BadDefinitions/EmptyFile.yaml")
                .addResources("classpath*:BadDefinitions/ThisOneDoesNotExist---Really.yaml") // Should cause a failure
                .build();
            assertTrue(uaa.runTests(false, false));
            }
        );
    }

    @Test
    public void checkIfLoadingAllFilesSeparatelyWorks() {
        UserAgentAnalyzerTesterBuilder uaaB = UserAgentAnalyzerTester
            .newBuilder()
            .hideMatcherLoadStats()
            .dropDefaultResources();

        PackagedRules.getRuleFileNames().forEach(uaaB::addResources);

        UserAgentAnalyzerTester uaa = uaaB
            .withCache(10000)
            .build();

        assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkIfLoadingAllFilesSeparatelyAsOptionalWorks() {
        UserAgentAnalyzerTesterBuilder uaaB = UserAgentAnalyzerTester
            .newBuilder()
            .hideMatcherLoadStats()
            .dropDefaultResources();

        PackagedRules.getRuleFileNames().forEach(uaaB::addOptionalResources);

        UserAgentAnalyzerTester uaa = uaaB
            .withCache(10000)
            .build();

        assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkAllFieldsCallsAfterLoadingAdditionalResourceUnsorted() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .dropDefaultResources()
            .addResources("classpath*:AllSteps.yaml")
            .build();

        Set<String> fieldSet1 = uaa.getAllPossibleFieldNames();

        uaa.loadResources("CompanyInternalUserAgents.yaml");

        Set<String> fieldSet2 = uaa.getAllPossibleFieldNames();

        List<String> extraFields = new ArrayList<>();
        Collections.addAll(extraFields, "ApplicationName", "ApplicationVersion", "ApplicationInstance", "ApplicationGitCommit", "ServerName");

        assertFalse(fieldSet1.containsAll(extraFields));
        assertTrue(fieldSet2.containsAll(extraFields));
    }

    @Test
    public void checkAllFieldsCallsAfterLoadingAdditionalResourceSorted() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .dropDefaultResources()
            .addResources("classpath*:AllSteps.yaml")
            .build();

        List<String> fieldList1 = uaa.getAllPossibleFieldNamesSorted();

        uaa.loadResources("CompanyInternalUserAgents.yaml");

        List<String> fieldList2 = uaa.getAllPossibleFieldNamesSorted();

        List<String> extraFields = new ArrayList<>();
        Collections.addAll(extraFields, "ApplicationName", "ApplicationVersion", "ApplicationInstance", "ApplicationGitCommit", "ServerName");

        assertFalse(fieldList1.containsAll(extraFields));
        assertTrue(fieldList2.containsAll(extraFields));
    }

}
