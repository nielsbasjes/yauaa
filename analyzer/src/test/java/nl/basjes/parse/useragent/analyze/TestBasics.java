/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.Test;

import static nl.basjes.parse.useragent.UserAgentAnalyzer.DEFAULT_USER_AGENT_MAX_LENGTH;
import static org.junit.Assert.assertEquals;

public class TestBasics {

    @Test
    public void testCacheSetter() {
        UserAgentAnalyzer userAgentAnalyzer = new UserAgentAnalyzer("classpath*:AllFields-tests.yaml");

        assertEquals("Incorrect default cache size", 10000, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setCacheSize(50);
        assertEquals("Incorrect cache size", 50, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setCacheSize(50000);
        assertEquals("Incorrect cache size", 50000, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setCacheSize(-5);
        assertEquals("Incorrect cache size", 0, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setCacheSize(50);
        assertEquals("Incorrect cache size", 50, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setCacheSize(50000);
        assertEquals("Incorrect cache size", 50000, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setUserAgentMaxLength(555);
        assertEquals("Incorrect user agent max length", 555, userAgentAnalyzer.getUserAgentMaxLength());
    }

    @Test
    public void testUserAgentMaxLengthSetter() {
        UserAgentAnalyzer userAgentAnalyzer = new UserAgentAnalyzer("classpath*:AllFields-tests.yaml");

        assertEquals("Incorrect default user agent max length", DEFAULT_USER_AGENT_MAX_LENGTH, userAgentAnalyzer.getUserAgentMaxLength());

        userAgentAnalyzer.setUserAgentMaxLength(250);
        assertEquals("Incorrect default user agent max length", 250, userAgentAnalyzer.getUserAgentMaxLength());

        userAgentAnalyzer.setUserAgentMaxLength(-100);
        assertEquals("Incorrect default user agent max length", DEFAULT_USER_AGENT_MAX_LENGTH, userAgentAnalyzer.getUserAgentMaxLength());
    }

    @Test
    public void testUseragent(){
        String uaString = "Foo Bar";
        UserAgent agent = new UserAgent(uaString);
        assertEquals(agent.get(UserAgent.USERAGENT).getValue(), uaString);
        assertEquals(agent.get(UserAgent.USERAGENT).getConfidence(), 0);
    }


}
