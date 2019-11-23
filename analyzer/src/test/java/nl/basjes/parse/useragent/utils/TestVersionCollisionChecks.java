/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestVersionCollisionChecks {
    @Test
    public void testBadVersion(){

        InvalidParserConfigurationException exception =

            assertThrows(InvalidParserConfigurationException.class, () ->
                UserAgentAnalyzer
            .newBuilder()
            .dropDefaultResources()
            .addResources("classpath*:Versions/BadVersion.yaml")
            .delayInitialization()
            .build());
        assertTrue(exception.getMessage().contains("Found unexpected config entry: bad"));
    }

    @Test
    public void testBadVersionNotMap(){
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () -> UserAgentAnalyzer
            .newBuilder()
            .dropDefaultResources()
            .addResources("classpath*:Versions/BadVersionNotMap.yaml")
            .delayInitialization()
            .build());
        assertTrue(exception.getMessage().contains("The value should be a string but it is a sequence"));

    }

    @Test
    public void testDifferentVersion(){
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () -> UserAgentAnalyzer
            .newBuilder()
            .delayInitialization()
            .addResources("classpath*:Versions/DifferentVersion.yaml")
            .build());
        assertTrue(exception.getMessage().contains("Two different Yauaa versions have been loaded:"));

    }

    @Test
    public void testDoubleLoadedResources(){
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () -> UserAgentAnalyzer
            .newBuilder()
            .delayInitialization()
            .addResources("classpath*:UserAgents/**/*.yaml")
            .build());
        assertTrue(exception.getMessage().startsWith("Trying to load "));
        assertTrue(exception.getMessage().endsWith(" resources for the second time"));
    }

}
