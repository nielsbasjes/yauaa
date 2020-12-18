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

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TestAnnotationSystemAnonymous {

    TestRecord record = new TestRecord();

    public static class TestRecord implements Serializable {
        final String useragent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36";
        String deviceClass;
        String agentNameVersion;
    }

    public abstract static class MyMapper<T>
        implements UserAgentAnnotationMapper<T>, Serializable {
        private final transient UserAgentAnnotationAnalyzer<T> userAgentAnalyzer;

        MyMapper() {
            userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
            userAgentAnalyzer.initialize(this);
        }

        public T enrich(T record) {
            return userAgentAnalyzer.map(record);
        }
    }

    @Test
    void testAnnotationBasedParser(){
        record =
            new MyMapper<TestRecord>() {
                @Override
                public String getUserAgentString(TestRecord testRecord) {
                    return testRecord.useragent;
                }

                @SuppressWarnings("unused") // Called via the annotation
                @YauaaField("DeviceClass")
                public void setDeviceClass(TestRecord testRecord, String value) {
                    testRecord.deviceClass = value;
                }

                @SuppressWarnings("unused") // Called via the annotation
                @YauaaField("AgentNameVersion")
                public void setAgentNameVersion(TestRecord testRecord, String value) {
                    testRecord.agentNameVersion = value;
                }
            } .enrich(record);

        assertEquals("Desktop", record.deviceClass);
        assertEquals("Chrome 48.0.2564.82", record.agentNameVersion);
    }

    // ----------------------------------------------------------------

    public abstract static class MyErrorMapper extends MyMapper<TestRecord> {
        @Override
        public String getUserAgentString(TestRecord record) {
            return record.useragent;
        }
    }

    @Test
    void testImpossibleField() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () -> record =
            new MyErrorMapper() {
                @SuppressWarnings("unused") // Called via the annotation
                @YauaaField("NielsBasjes")
                public void setImpossibleField(TestRecord testRecord, String value) {
                    fail("May NEVER call this method");
                }
            } .enrich(record));
        assertEquals("We cannot provide these fields:[NielsBasjes]", exception.getMessage());
    }

    // ----------------------------------------------------------------

    @Test
    void testWrongReturnType() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () -> record =
            new MyErrorMapper() {
                @SuppressWarnings("unused") // Called via the annotation
                @YauaaField("DeviceClass")
                public boolean wrongSetter(TestRecord testRecord, Double value) {
                    fail("May NEVER call this method");
                    return false;
                }
            } .enrich(record));
        assertTrue(exception.getMessage().contains("the method [wrongSetter] " +
            "has been annotated with YauaaField but it has the wrong method signature. It must look like " +
            "[ public void wrongSetter(TestRecord record, String value) ]"));
    }

    // ----------------------------------------------------------------

    @Test
    void testInaccessibleSetter() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () -> record =
            new MyErrorMapper() {
                @SuppressWarnings("unused") // Called via the annotation
                @YauaaField("DeviceClass")
                private void inaccessibleSetter(TestRecord testRecord, String value) {
                    fail("May NEVER call this method");
                }
            } .enrich(record));
        assertEquals("Method annotated with YauaaField is not public: inaccessibleSetter", exception.getMessage());
    }

    // ----------------------------------------------------------------

    @Test
    void testTooManyParameters() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () ->
            record =
            new MyErrorMapper() {
                @SuppressWarnings("unused") // Called via the annotation
                @YauaaField("DeviceClass")
                public void wrongSetter(TestRecord testRecord, String value, String extra) {
                    fail("May NEVER call this method");
                }
            } .enrich(record));
        assertTrue(exception.getMessage().contains("the method [wrongSetter] " +
            "has been annotated with YauaaField but it has the wrong method signature. It must look like " +
            "[ public void wrongSetter(TestRecord record, String value) ]"));
    }

    // ----------------------------------------------------------------

    @Test
    void testWrongTypeParameters1() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () ->
            record =
            new MyErrorMapper() {
                @SuppressWarnings("unused") // Called via the annotation
                @YauaaField("DeviceClass")
                public void wrongSetter(String string, String value) {
                    fail("May NEVER call this method");
                }
            } .enrich(record));
        assertTrue(exception.getMessage().contains("the method [wrongSetter] " +
            "has been annotated with YauaaField but it has the wrong method signature. It must look like " +
            "[ public void wrongSetter(TestRecord record, String value) ]"));
    }

    // ----------------------------------------------------------------

    @Test
    void testWrongTypeParameters2() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () ->
            record =
            new MyErrorMapper() {
                @SuppressWarnings("unused") // Called via the annotation
                @YauaaField("DeviceClass")
                public void wrongSetter(TestRecord testRecord, Double value) {
                    fail("May NEVER call this method");
                }
            } .enrich(record));
        assertTrue(exception.getMessage().contains("the method [wrongSetter] " +
            "has been annotated with YauaaField but it has the wrong method signature. It must look like " +
            "[ public void wrongSetter(TestRecord record, String value) ]"));
    }

    // ----------------------------------------------------------------

    @Test
    void testMissingAnnotations() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () ->
            record =
            new MyErrorMapper() {
                @SuppressWarnings("unused") // Called via the annotation
                public void setWasNotAnnotated(TestRecord testRecord, String value) {
                    fail("May NEVER call this method");
                }
            } .enrich(record));
        assertEquals("You MUST specify at least 1 field to extract.", exception.getMessage());
    }

    // ----------------------------------------------------------------

    @Test
    void testSetterFailure() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () ->
            record =
            new MyErrorMapper() {
                @SuppressWarnings("unused") // Called via the annotation
                @YauaaField("DeviceClass")
                public void failingSetter(TestRecord testRecord, String value) {
                    throw new IllegalStateException("Just testing the error handling");
                }
            } .enrich(record));
        assertEquals("A problem occurred while calling the requested setter", exception.getMessage());
    }

    // ----------------------------------------------------------------

}
