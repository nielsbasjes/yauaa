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

package nl.basjes.parse.useragent;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMatcherExpressions {
    private static final Logger LOG = LoggerFactory.getLogger(TestMatcherExpressions.class);

//    @Test
//    public void runSingleMatcherFile() {
//        UserAgentAnalyzer uaa = new UserAgentAnalyzer("classpath*:**/Linux.yaml");
//        Assert.assertTrue(uaa.runTests(true, false));
//    }

    @Test
    public void runMatcherTests() {
        UserAgentAnalyzer uaa = new UserAgentAnalyzer("classpath*:Matcher-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runLookupTests() {
        UserAgentAnalyzer uaa = new UserAgentAnalyzer("classpath*:Lookup-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, true));
    }

    @Test
    public void runPositionalTests() {
        UserAgentAnalyzer uaa = new UserAgentAnalyzer("classpath*:Positional-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, true));
    }

    @Test
    public void runWalkingTests() {
        UserAgentAnalyzer uaa = new UserAgentAnalyzer("classpath*:Walking-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runAllFieldsTests() {
        UserAgentAnalyzer uaa = new UserAgentAnalyzer("classpath*:AllFields-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, true));
    }
}
