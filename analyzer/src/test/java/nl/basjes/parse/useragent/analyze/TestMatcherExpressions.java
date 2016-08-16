/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMatcherExpressions {
    private static final Logger LOG = LoggerFactory.getLogger(TestMatcherExpressions.class);

//    @Test
//    public void runSingleMatcherFile() {
//        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:**/Linux.yaml");
//        Assert.assertTrue(uaa.runTests(true, false));
//    }

    @Test
    public void runMatcherTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Matcher-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runLookupTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Lookup-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, true));
    }

    @Test
    public void runPositionalTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Positional-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, true));
    }

    @Test
    public void runWalkingTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Walking-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runAllFieldsTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:AllFields-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, true));
    }
}
