/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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
import nl.basjes.parse.useragent.Version;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.utils.YauaaVersion.AbstractVersion;
import org.junit.jupiter.api.Test;

import static nl.basjes.parse.useragent.Version.BUILD_JDK_VERSION;
import static nl.basjes.parse.useragent.Version.BUILD_TIME_STAMP;
import static nl.basjes.parse.useragent.Version.COPYRIGHT;
import static nl.basjes.parse.useragent.Version.GIT_COMMIT_ID;
import static nl.basjes.parse.useragent.Version.GIT_COMMIT_ID_DESCRIBE_SHORT;
import static nl.basjes.parse.useragent.Version.LICENSE;
import static nl.basjes.parse.useragent.Version.PROJECT_VERSION;
import static nl.basjes.parse.useragent.Version.TARGET_JRE_VERSION;
import static nl.basjes.parse.useragent.Version.URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestVersionCollisionChecks {

    @Test
    void testBadVersion(){
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
    void testBadVersionNotMap(){
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
    void testDifferentVersion(){
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () -> UserAgentAnalyzer
            .newBuilder()
            .delayInitialization()
            .addResources("classpath*:Versions/DifferentVersion.yaml")
            .build());
        assertTrue(exception.getMessage().contains("Two different Yauaa versions have been loaded:"));
    }

    @Test
    void testDoubleLoadedResources(){
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () -> UserAgentAnalyzer
            .newBuilder()
            .delayInitialization()
            .addResources("classpath*:UserAgents/**/*.yaml")
            .build());
        assertTrue(exception.getMessage().startsWith("Trying to load "));
        assertTrue(exception.getMessage().endsWith(" resources for the second time"));
    }

    @Test
    void testBasics() {
        Version version1 = Version.getInstance();
        AbstractVersion version2 = new AbstractVersion() {
            @Override public String getGitCommitId() {
                return GIT_COMMIT_ID;
            }
            @Override public String getGitCommitIdDescribeShort() {
                return GIT_COMMIT_ID_DESCRIBE_SHORT;
            }
            @Override public String getBuildTimeStamp() {
                return BUILD_TIME_STAMP;
            }
            @Override public String getProjectVersion() {
                return PROJECT_VERSION;
            }
            @Override public String getCopyright() {
                return COPYRIGHT;
            }
            @Override public String getLicense() {
                return LICENSE;
            }
            @Override public String getUrl() {
                return URL;
            }
            @Override public String getBuildJDKVersion() {
                return BUILD_JDK_VERSION;
            }
            @Override public String getTargetJREVersion() {
                return TARGET_JRE_VERSION;
            }
        };

        assertEquals(version1, version2);
        assertEquals(version1.hashCode(), version2.hashCode());
        assertEquals(version2.toString(), version2.toString());
    }

}
