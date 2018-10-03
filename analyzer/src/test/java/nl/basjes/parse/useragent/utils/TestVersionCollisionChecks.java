/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.core.StringStartsWith.startsWith;

public class TestVersionCollisionChecks {
    @Rule
    public final ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testBadVersion(){
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(containsString("Found unexpected config entry: bad"));

        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .dropDefaultResources()
            .delayInitialization()
            .build();

        uaa.loadResources("classpath*:Versions/BadVersion.yaml");
    }

    @Test
    public void testBadVersionNotMap(){
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(containsString("The value should be a string but it is a sequence"));

        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .dropDefaultResources()
            .delayInitialization()
            .build();

        uaa.loadResources("classpath*:Versions/BadVersionNotMap.yaml");
    }

    @Test
    public void testDifferentVersion(){
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(containsString("Two different Yauaa versions have been loaded:"));

        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .delayInitialization()
            .build();

        uaa.loadResources("classpath*:Versions/DifferentVersion.yaml");
    }

    @Test
    public void testDoubleLoadedResources(){
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(allOf(startsWith("Trying to load "), endsWith(" resources for the second time")));

        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .delayInitialization()
            .build();

        uaa.loadResources("classpath*:UserAgents/**/*.yaml");
    }

}
