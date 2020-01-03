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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestAnnotationSystem {

    public static class TestRecord implements Serializable {
        final String useragent;
        String deviceClass;
        String agentNameVersion;

        public TestRecord(String useragent) {
            this.useragent = useragent;
        }
    }

    public static class MyBaseMapper
        implements UserAgentAnnotationMapper<TestRecord>, Serializable {
        private final transient UserAgentAnnotationAnalyzer<TestRecord> userAgentAnalyzer;

        public MyBaseMapper() {
            userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
            userAgentAnalyzer.initialize(this);
        }

        public TestRecord enrich(TestRecord record) {
            return userAgentAnalyzer.map(record);
        }

        @Override
        public String getUserAgentString(TestRecord record) {
            return record.useragent;
        }
    }

    // ----------------------------------------------------------------

    @SuppressWarnings("unused")
    public static class MyMapper extends MyBaseMapper {
        @YauaaField("DeviceClass")
        public void setDeviceClass(TestRecord record, String value) {
            record.deviceClass = value;
        }

        @YauaaField("AgentNameVersion")
        public void setAgentNameVersion(TestRecord record, String value) {
            record.agentNameVersion = value;
        }
    }

    @Test
    public void testAnnotationBasedParser() {
        MyMapper mapper = new MyMapper();

        TestRecord record = new TestRecord("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/48.0.2564.82 Safari/537.36");

        record = mapper.enrich(record);

        assertEquals("Desktop", record.deviceClass);
        assertEquals("Chrome 48.0.2564.82", record.agentNameVersion);
    }

    // ----------------------------------------------------------------

    public static class ImpossibleFieldMapper extends MyBaseMapper {
        @SuppressWarnings("unused")
        @YauaaField("NielsBasjes")
        public void setImpossibleField(TestRecord record, String value) {
            record.agentNameVersion = value;
        }
    }

    @Test
    public void testImpossibleField() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, ImpossibleFieldMapper::new);
        assertEquals("We cannot provide these fields:[NielsBasjes]", exception.getMessage());
    }

    // ----------------------------------------------------------------

    public static class InaccessibleSetterMapper  extends MyBaseMapper {
        @SuppressWarnings("unused")
        @YauaaField("DeviceClass")
        private void inaccessibleSetter(TestRecord record, String value) {
            fail("May NEVER call this method");
        }
    }

    @Test
    public void testInaccessibleSetter() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, InaccessibleSetterMapper::new);
        assertEquals("Method annotated with YauaaField is not public: inaccessibleSetter", exception.getMessage());
    }

    // ----------------------------------------------------------------

    public static class TooManyParameters extends MyBaseMapper {
        @SuppressWarnings("unused")
        @YauaaField("DeviceClass")
        public void wrongSetter(TestRecord record, String value, String extra) {
            fail("May NEVER call this method");
        }
    }

    @Test
    public void testTooManyParameters() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, TooManyParameters::new);
        assertTrue(exception.getMessage().contains(
            "the method [wrongSetter] has been annotated with YauaaField but it has the wrong method signature. " +
                "It must look like [ public void wrongSetter(TestRecord record, String value) ]"));
    }

    // ----------------------------------------------------------------

    public static class WrongTypeParameters1 extends MyBaseMapper {
        @SuppressWarnings("unused")
        @YauaaField("DeviceClass")
        public void wrongSetter(String record, String value) {
            fail("May NEVER call this method");
        }
    }

    @Test
    public void testWrongTypeParameters1() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, WrongTypeParameters1::new);
        assertTrue(exception.getMessage().contains(
            "the method [wrongSetter] has been annotated with YauaaField but it has the wrong method signature. " +
                "It must look like [ public void wrongSetter(TestRecord record, String value) ]"));
    }

    // ----------------------------------------------------------------

    public static class WrongTypeParameters2 extends MyBaseMapper {
        @SuppressWarnings("unused")
        @YauaaField("DeviceClass")
        public void wrongSetter(TestRecord record, Double value) {
            fail("May NEVER call this method");
        }
    }

    @Test
    public void testWrongTypeParameters2() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, WrongTypeParameters2::new);
        assertTrue(exception.getMessage().contains(
            "the method [wrongSetter] has been annotated with YauaaField but it has the wrong method signature. " +
                "It must look like [ public void wrongSetter(TestRecord record, String value) ]"));
    }

    // ----------------------------------------------------------------

    public static class MissingAnnotations extends MyBaseMapper {
        @SuppressWarnings("unused")
        public void setWasNotAnnotated(TestRecord record, Double value) {
            fail("May NEVER call this method");
        }
    }

    @Test
    public void testMissingAnnotations() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, MissingAnnotations::new);
        assertEquals("You MUST specify at least 1 field to extract.", exception.getMessage());
    }

    // ----------------------------------------------------------------

    public static class WrongReturnType extends MyBaseMapper {
        @SuppressWarnings("unused")
        @YauaaField("DeviceClass")
        public boolean nonVoidSetter(TestRecord record, String value) {
            fail("May NEVER call this method");
            return true;
        }
    }

    @Test
    public void testNonVoidSetter() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, WrongReturnType::new);
        assertTrue(exception.getMessage().contains(
            "the method [nonVoidSetter] has been annotated with YauaaField but it has the wrong method signature. " +
                "It must look like [ public void nonVoidSetter(TestRecord record, String value) ]"));
    }

    // ----------------------------------------------------------------

    private static final class PrivateTestRecord implements Serializable {
        final String useragent;

        private PrivateTestRecord(String useragent) {
            this.useragent = useragent;
        }
    }

    public static class PrivateMyBaseMapper
        implements UserAgentAnnotationMapper<PrivateTestRecord>, Serializable {
        private final transient UserAgentAnnotationAnalyzer<PrivateTestRecord> userAgentAnalyzer;

        public PrivateMyBaseMapper() {
            userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
            userAgentAnalyzer.initialize(this);
        }

        @SuppressWarnings("unused")
        public PrivateTestRecord enrich(PrivateTestRecord record) {
            return userAgentAnalyzer.map(record);
        }

        @Override
        public String getUserAgentString(PrivateTestRecord record) {
            return record.useragent;
        }
    }

    private static class InaccessibleSetterMapperClass extends PrivateMyBaseMapper {
        @SuppressWarnings("unused")
        @YauaaField("DeviceClass")
        public void correctSetter(PrivateTestRecord record, String value) {
            fail("May NEVER call this method");
        }
    }

    @Test
    public void testInaccessibleSetterClass() {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, InaccessibleSetterMapperClass::new);
        assertEquals("The class nl.basjes.parse.useragent.annotate.TestAnnotationSystem.PrivateTestRecord is not public.", exception.getMessage());
    }

    // ----------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"}) // Here we deliberately created some bad code to check the behavior.
    @Test
    public void testBadGeneric(){
        UserAgentAnnotationAnalyzer userAgentAnalyzer = new UserAgentAnnotationAnalyzer();
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () -> assertNull(userAgentAnalyzer.map("Foo")));
        assertEquals("[Map] The mapper instance is null.", exception.getMessage());
    }



}
