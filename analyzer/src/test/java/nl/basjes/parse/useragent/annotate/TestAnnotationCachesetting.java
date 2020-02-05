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

package nl.basjes.parse.useragent.annotate;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Field;

import static nl.basjes.parse.useragent.UserAgentAnalyzer.DEFAULT_PARSE_CACHE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestAnnotationCachesetting {

    @SuppressWarnings("unused")
    public static class TestRecord implements Serializable {
        final String useragent;
        String deviceClass;
        String agentNameVersion;

        public TestRecord(String useragent) {
            this.useragent = useragent;
        }
    }

    @SuppressWarnings("unused")
    public static class MyMapper implements UserAgentAnnotationMapper<TestRecord>, Serializable {
        @Override
        public String getUserAgentString(TestRecord record) {
            return record.useragent;
        }

        @YauaaField("DeviceClass")
        public void setDeviceClass(TestRecord record, String value) {
            record.deviceClass = value;
        }

        @YauaaField("AgentNameVersion")
        public void setAgentNameVersion(TestRecord record, String value) {
            record.agentNameVersion = value;
        }
    }

    // ----------------------------------------------------------------

    @Test
    public void testAnnotationCacheSetting1() throws IllegalAccessException, NoSuchFieldException {
        UserAgentAnnotationAnalyzer<TestRecord> userAgentAnnotationAnalyzer = new UserAgentAnnotationAnalyzer<>();

        // To make sure the internals behave as expected
        Field userAgentAnalyzerField = userAgentAnnotationAnalyzer.getClass().getDeclaredField("userAgentAnalyzer");
        userAgentAnalyzerField.setAccessible(true);
        assertNull(userAgentAnalyzerField.get(userAgentAnnotationAnalyzer));

        // Initial value
        assertEquals(DEFAULT_PARSE_CACHE_SIZE, userAgentAnnotationAnalyzer.getCacheSize());


        // Setting and getting while no UserAgentAnalyzer exists
        userAgentAnnotationAnalyzer.setCacheSize(1234);
        assertEquals(1234, userAgentAnnotationAnalyzer.getCacheSize());

        userAgentAnnotationAnalyzer.disableCaching();
        assertEquals(0, userAgentAnnotationAnalyzer.getCacheSize());

        userAgentAnnotationAnalyzer.setCacheSize(4567);
        assertEquals(4567, userAgentAnnotationAnalyzer.getCacheSize());

        userAgentAnnotationAnalyzer.initialize(new MyMapper());

        UserAgentAnalyzer userAgentAnalyzer = (UserAgentAnalyzer)userAgentAnalyzerField.get(userAgentAnnotationAnalyzer);
        assertNotNull(userAgentAnalyzer);

        // Setting and getting while there IS a UserAgentAnalyzer
        userAgentAnnotationAnalyzer.setCacheSize(1234);
        assertEquals(1234, userAgentAnnotationAnalyzer.getCacheSize());
        assertEquals(1234, userAgentAnalyzer.getCacheSize());

        userAgentAnnotationAnalyzer.disableCaching();
        assertEquals(0, userAgentAnnotationAnalyzer.getCacheSize());
        assertEquals(0, userAgentAnalyzer.getCacheSize());

        userAgentAnnotationAnalyzer.setCacheSize(4567);
        assertEquals(4567, userAgentAnnotationAnalyzer.getCacheSize());
        assertEquals(4567, userAgentAnalyzer.getCacheSize());
    }


    @Test
    public void testAnnotationCacheSetting2() throws IllegalAccessException, NoSuchFieldException {
        UserAgentAnnotationAnalyzer<TestRecord> userAgentAnnotationAnalyzer = new UserAgentAnnotationAnalyzer<>();

        // To make sure the internals behave as expected
        Field userAgentAnalyzerField = userAgentAnnotationAnalyzer.getClass().getDeclaredField("userAgentAnalyzer");
        userAgentAnalyzerField.setAccessible(true);
        assertNull(userAgentAnalyzerField.get(userAgentAnnotationAnalyzer));

        // Initial value
        assertEquals(DEFAULT_PARSE_CACHE_SIZE, userAgentAnnotationAnalyzer.getCacheSize());

        userAgentAnnotationAnalyzer.initialize(new MyMapper());

        UserAgentAnalyzer userAgentAnalyzer = (UserAgentAnalyzer)userAgentAnalyzerField.get(userAgentAnnotationAnalyzer);
        assertNotNull(userAgentAnalyzer);
        // Initial value
        assertEquals(DEFAULT_PARSE_CACHE_SIZE, userAgentAnnotationAnalyzer.getCacheSize());
        assertEquals(DEFAULT_PARSE_CACHE_SIZE, userAgentAnalyzer.getCacheSize());

        // Setting and getting while there IS a UserAgentAnalyzer
        userAgentAnnotationAnalyzer.setCacheSize(1234);
        assertEquals(1234, userAgentAnnotationAnalyzer.getCacheSize());
        assertEquals(1234, userAgentAnalyzer.getCacheSize());

        userAgentAnnotationAnalyzer.disableCaching();
        assertEquals(0, userAgentAnnotationAnalyzer.getCacheSize());
        assertEquals(0, userAgentAnalyzer.getCacheSize());

        userAgentAnnotationAnalyzer.setCacheSize(4567);
        assertEquals(4567, userAgentAnnotationAnalyzer.getCacheSize());
        assertEquals(4567, userAgentAnalyzer.getCacheSize());

    }


}
