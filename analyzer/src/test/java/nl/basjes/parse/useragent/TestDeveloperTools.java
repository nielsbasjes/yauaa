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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDeveloperTools {

    private static final Logger LOG = LoggerFactory.getLogger(TestDeveloperTools.class);

    @Test
    public void validateErrorSituationOutput() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester();
        uaa.initialize(false);
        uaa.eraseTestCases();
        uaa.loadResources("classpath*:**/CheckErrorOutput.yaml", true);
        assertFalse(uaa.runTests(false, true)); // This test must return an error state
    }

    @Test
    public void validateNewTestcaseSituationOutput() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester();
        uaa.initialize(false);
        uaa.eraseTestCases();
        uaa.loadResources("classpath*:**/CheckNewTestcaseOutput.yaml", true);
        assertTrue(uaa.runTests(false, true));
    }

}
