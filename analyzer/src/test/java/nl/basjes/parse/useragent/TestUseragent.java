/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.AgentField.MutableAgentField;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_EMAIL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_URL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_CLASS;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VALUE;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestUseragent {

    @Test
    void testUseragent() {
        String    uaString = "Foo Bar";
        UserAgent agent    = new MutableUserAgent(uaString);
        assertEquals(uaString, agent.get(UserAgent.USERAGENT_FIELDNAME).getValue());
        assertEquals(0, agent.get(UserAgent.USERAGENT_FIELDNAME).getConfidence());
        assertEquals(uaString, agent.getValue(UserAgent.USERAGENT_FIELDNAME));
        assertEquals(0L, agent.getConfidence(UserAgent.USERAGENT_FIELDNAME).longValue());
    }

    @Test
    void testUseragentValues() {
        testUseragentValuesDebug(true);
        testUseragentValuesDebug(false);
    }

    private void testUseragentValuesDebug(boolean debug) {
        String    name     = "Attribute";
        String    uaString = "Foo Bar";
        MutableUserAgent agent    = new MutableUserAgent(uaString);
        agent.setDebug(debug);

        // Setting unknown new attributes
        assertNotNull(agent.get("UnknownOne"));
        assertEquals("Unknown", agent.getValue("UnknownOne"));
        assertEquals(-1, agent.getConfidence("UnknownOne").longValue());
        agent.set("UnknownOne", "One", 111);
        check(agent, "UnknownOne", "One", 111);
        agent.get("UnknownOne").reset();
        check(agent, "UnknownOne", "Unknown", -1);

        // Setting unknown new attributes FORCED
        assertNotNull(agent.get("UnknownTwo"));
        assertEquals("Unknown", agent.getValue("UnknownTwo"));
        assertEquals(-1, agent.getConfidence("UnknownTwo").longValue());
        agent.setForced("UnknownTwo", "Two", 222);
        check(agent, "UnknownTwo", "Two", 222);
        agent.get("UnknownTwo").reset();
        check(agent, "UnknownTwo", "Unknown", -1);

        // Setting known attributes
        check(agent, "AgentClass", "Unknown", -1);
        agent.set("AgentClass", "One", 111);
        check(agent, "AgentClass", "One", 111);
        agent.get("AgentClass").reset();
        check(agent, "AgentClass", "Unknown", -1);

        // Setting known attributes FORCED
        check(agent, "AgentVersion", "??", -1);
        agent.setForced("AgentVersion", "Two", 222);
        check(agent, "AgentVersion", "Two", 222);
        agent.get("AgentVersion").reset();
        check(agent, "AgentVersion", "??", -1);

        agent.set(name, "One", 111);
        check(agent, name, "One", 111);

        agent.set(name, "Two", 22); // Should be ignored
        check(agent, name, "One", 111);

        agent.set(name, "Three", 333); // Should be used
        check(agent, name, "Three", 333);

        agent.setForced(name, "Four", 4); // Should be used
        check(agent, name, "Four", 4);

        agent.set(name, "<<<null>>>", 2); // Should be ignored
        check(agent, name, "Four", 4);

        agent.set(name, "<<<null>>>", 5); // Should be used
        check(agent, name, "Unknown", 5);

        agent.set(name, "Four", 4); // Should be IGNORED
        check(agent, name, "Unknown", 5);

        // Set a 'normal' value again.
        agent.set(name, "Three", 333); // Should be used
        check(agent, name, "Three", 333);

        MutableAgentField field = agent.get(name);
        field.setValueForced("Five", 5); // Should be used
        check(agent, name, "Five", 5);
        field.setValueForced("<<<null>>>", 4); // Should be used
        check(agent, name, "Unknown", 4);
    }


    private void check(UserAgent agent, String name, String expectedValue, long expectedConfidence) {
        assertEquals(expectedValue, agent.get(name).getValue());
        assertEquals(expectedValue, agent.getValue(name));
        assertEquals(expectedConfidence, agent.get(name).getConfidence());
        assertEquals(expectedConfidence, agent.getConfidence(name).longValue());
    }

    @Test
    void testDefaults() {
        MutableUserAgent agent = new MutableUserAgent();
        // Defaults for known fields
        assertEquals(UNKNOWN_VALUE,     agent.getValue("DeviceClass"));
        assertEquals(UNKNOWN_VERSION,   agent.getValue("AgentVersion"));

        // The field is not present
        assertEquals(UNKNOWN_VALUE,   agent.getValue("SomethingNew"));

        // The field is present
        agent.set("SomethingNew", "A value", 10);
        assertEquals("A value",   agent.getValue("SomethingNew"));

        agent.set("SomethingNew", NULL_VALUE, 20);
        assertEquals(UNKNOWN_VALUE,   agent.getValue("SomethingNew"));

        agent.reset();
        assertEquals(UNKNOWN_VALUE,   agent.getValue("SomethingNew"));
    }

    @Test
    void testCopying() {
        MutableAgentField origNull = new MutableAgentField((String)null);
        origNull.setValue("One", 1);
        MutableAgentField copyNull = new MutableAgentField("Foo"); // Different default!
        assertTrue(copyNull.setValue(origNull));

        assertEquals("One", copyNull.getValue());
        assertEquals(1, copyNull.getConfidence());
        copyNull.reset();
        assertEquals("Foo", copyNull.getValue()); // The default should NOT be modified
        assertEquals(-1, copyNull.getConfidence());


        MutableAgentField origFoo = new MutableAgentField("Foo");
        origFoo.setValue("Two", 2);
        MutableAgentField copyFoo = new MutableAgentField((String)null); // Different default!
        copyFoo.setValue(origFoo);

        assertEquals("Two", copyFoo.getValue());
        assertEquals(2, copyFoo.getConfidence());
        copyFoo.reset();
        assertNull(copyFoo.getValue()); // The default should NOT be modified
        assertEquals(-1, copyFoo.getConfidence());
    }

    @Test
    void testCopying2() {
        MutableUserAgent mutableUserAgent = new MutableUserAgent("Agent");
        mutableUserAgent.addHeader("Header One", "One");
        mutableUserAgent.set("Field One", "Value One", 1);
        mutableUserAgent.set("Field Two", "Value Two", 2);

        assertEquals(2,              mutableUserAgent.getHeaders().values().size());
        assertEquals("Agent",        mutableUserAgent.getHeaders().get("User-Agent"));
        assertEquals("Agent",        mutableUserAgent.getHeaders().get("User-Agent"));
        assertEquals("One",          mutableUserAgent.getHeaders().get("Header One"));
        assertEquals("One",          mutableUserAgent.getHeaders().get("Header One"));

        assertEquals("Value One",    mutableUserAgent.getValue("Field One"));
        assertEquals("Value Two",    mutableUserAgent.getValue("Field Two"));
        assertEquals(1,              mutableUserAgent.getConfidence("Field One"));
        assertEquals(2,              mutableUserAgent.getConfidence("Field Two"));

        assertEquals(mutableUserAgent, mutableUserAgent);

        ImmutableUserAgent immutableUserAgent = new ImmutableUserAgent(mutableUserAgent);
        assertEquals(mutableUserAgent, immutableUserAgent);

        assertEquals(2,              immutableUserAgent.getHeaders().values().size());
        assertEquals("Agent",        immutableUserAgent.getHeaders().get("User-Agent"));
        assertEquals("Agent",        immutableUserAgent.getHeaders().get("User-Agent"));
        assertEquals("One",          immutableUserAgent.getHeaders().get("Header One"));
        assertEquals("One",          immutableUserAgent.getHeaders().get("Header One"));

        assertEquals("Value One",    immutableUserAgent.getValue("Field One"));
        assertEquals("Value Two",    immutableUserAgent.getValue("Field Two"));
        assertEquals(1,              immutableUserAgent.getConfidence("Field One"));
        assertEquals(2,              immutableUserAgent.getConfidence("Field Two"));

        MutableUserAgent mutableUserAgent2 = new MutableUserAgent(immutableUserAgent);
        assertEquals(mutableUserAgent2, immutableUserAgent);

        assertEquals(2,              mutableUserAgent2.getHeaders().values().size());
        assertEquals("Agent",        mutableUserAgent2.getHeaders().get("User-Agent"));
        assertEquals("Agent",        mutableUserAgent2.getHeaders().get("User-Agent"));
        assertEquals("One",          mutableUserAgent2.getHeaders().get("Header One"));
        assertEquals("One",          mutableUserAgent2.getHeaders().get("Header One"));

        assertEquals("Value One",    mutableUserAgent2.getValue("Field One"));
        assertEquals("Value Two",    mutableUserAgent2.getValue("Field Two"));
        assertEquals(1,              mutableUserAgent2.getConfidence("Field One"));
        assertEquals(2,              mutableUserAgent2.getConfidence("Field Two"));
    }

    @Test
    void comparingUserAgents() {
        MutableUserAgent baseAgent = new MutableUserAgent("Something 2");
        MutableUserAgent mutableAgent0    = new MutableUserAgent("Something 2");
        MutableUserAgent mutableAgent1    = new MutableUserAgent("Something 1");
        MutableUserAgent mutableAgent2    = new MutableUserAgent("Something 2");
        MutableUserAgent mutableAgent3    = new MutableUserAgent("Something 2");
        MutableUserAgent mutableAgent4    = new MutableUserAgent("Something 2");

        MutableAgentField field0 = new MutableAgentField("Foo");
        field0.setValue("One", 1);

        MutableAgentField field1 = new MutableAgentField("Foo");
        field1.setValue("One", 1);

        MutableAgentField field2 = new MutableAgentField("Foo"); // Same, different value
        field2.setValue("Two", 1);

        MutableAgentField field3 = new MutableAgentField("Foo"); // Same, different confidence
        field3.setValue("One", 2);

        MutableAgentField field4 = new MutableAgentField((String)null); // Same, different default
        field4.setValue("One", 1);

        // We compare the base agent with 4 variations
        baseAgent.setImmediateForTesting("Field", field0);
        mutableAgent0.setImmediateForTesting("Field", field1); // Same
        mutableAgent1.setImmediateForTesting("Field", field1); // Different useragent
        mutableAgent2.setImmediateForTesting("Field", field2); // Different field value
        mutableAgent3.setImmediateForTesting("Field", field3); // Different field confidence
        mutableAgent4.setImmediateForTesting("Field", field4); // Different field default value

        // Check em
        assertEquals(baseAgent, baseAgent); // Ensure that equals on the same results in true
        assertEquals(baseAgent, mutableAgent0);
        assertEquals(mutableAgent0, baseAgent);
        assertEquals(baseAgent.hashCode(), mutableAgent0.hashCode());

        assertNotEquals(baseAgent, mutableAgent2);
        assertNotEquals(baseAgent, mutableAgent3);
        assertNotEquals(baseAgent, mutableAgent4);

        assertNotEquals("String", mutableAgent1);
        assertNotEquals(mutableAgent1, "String");

        UserAgent immutableAgent0 = new ImmutableUserAgent(mutableAgent0);
        UserAgent immutableAgent1 = new ImmutableUserAgent(mutableAgent1);
        UserAgent immutableAgent2 = new ImmutableUserAgent(mutableAgent2);
        UserAgent immutableAgent3 = new ImmutableUserAgent(mutableAgent3);
        UserAgent immutableAgent4 = new ImmutableUserAgent(mutableAgent4);

        assertEquals(mutableAgent0.hashCode(), immutableAgent0.hashCode());
        assertEquals(mutableAgent1.hashCode(), immutableAgent1.hashCode());
        assertEquals(mutableAgent2.hashCode(), immutableAgent2.hashCode());
        assertEquals(mutableAgent3.hashCode(), immutableAgent3.hashCode());
        assertEquals(mutableAgent4.hashCode(), immutableAgent4.hashCode());

        assertEquals(mutableAgent0, immutableAgent0);
        assertEquals(mutableAgent1, immutableAgent1);
        assertEquals(mutableAgent2, immutableAgent2);
        assertEquals(mutableAgent3, immutableAgent3);
        assertEquals(mutableAgent4, immutableAgent4);

        assertEquals(immutableAgent0, mutableAgent0);
        assertEquals(immutableAgent1, mutableAgent1);
        assertEquals(immutableAgent2, mutableAgent2);
        assertEquals(immutableAgent3, mutableAgent3);
        assertEquals(immutableAgent4, mutableAgent4);

        assertEquals(mutableAgent0.toString(), immutableAgent0.toString());
        assertEquals(mutableAgent1.toString(), immutableAgent1.toString());
        assertEquals(mutableAgent2.toString(), immutableAgent2.toString());
        assertEquals(mutableAgent3.toString(), immutableAgent3.toString());
        assertEquals(mutableAgent4.toString(), immutableAgent4.toString());

        assertEquals(mutableAgent0.toXML(), immutableAgent0.toXML());
        assertEquals(mutableAgent1.toXML(), immutableAgent1.toXML());
        assertEquals(mutableAgent2.toXML(), immutableAgent2.toXML());
        assertEquals(mutableAgent3.toXML(), immutableAgent3.toXML());
        assertEquals(mutableAgent4.toXML(), immutableAgent4.toXML());

        assertEquals(mutableAgent0.toYamlTestCase(), immutableAgent0.toYamlTestCase());
        assertEquals(mutableAgent1.toYamlTestCase(), immutableAgent1.toYamlTestCase());
        assertEquals(mutableAgent2.toYamlTestCase(), immutableAgent2.toYamlTestCase());
        assertEquals(mutableAgent3.toYamlTestCase(), immutableAgent3.toYamlTestCase());
        assertEquals(mutableAgent4.toYamlTestCase(), immutableAgent4.toYamlTestCase());

    }

    @Test
    void comparingUserAgentFields() {
        MutableAgentField field0 = new MutableAgentField("Foo");
        field0.setValue("One", 1);

        MutableAgentField field1 = new MutableAgentField("Foo");
        field1.setValue("One", 1);

        MutableAgentField field2 = new MutableAgentField("Foo"); // Same, different value
        field2.setValue("Two", 1);

        MutableAgentField field3 = new MutableAgentField("Foo"); // Same, different confidence
        field3.setValue("One", 2);

        MutableAgentField field4 = new MutableAgentField((String)null); // Same, different default
        field4.setValue("One", 1);

        // This is mainly used when rendering in a debugger.
        assertEquals("{ value:'One', confidence:'1', default:'Foo', isDefault:false }", field0.toString());
        assertEquals("{ value:'One', confidence:'1', default:'Foo', isDefault:false }", field1.toString());
        assertEquals("{ value:'Two', confidence:'1', default:'Foo', isDefault:false }", field2.toString());
        assertEquals("{ value:'One', confidence:'2', default:'Foo', isDefault:false }", field3.toString());
        assertEquals("{ value:'One', confidence:'1', default:null, isDefault:false }", field4.toString());

        assertEquals(field1, field1);
        assertEquals(field1.hashCode(), field1.hashCode());

        assertEquals(field0, field1);
        assertEquals(field1, field0);

        assertNotEquals("String", field1);
        assertNotEquals(field1, "String");

        assertNotEquals(field1.hashCode(), field2.hashCode());
        assertNotEquals(field1.hashCode(), field3.hashCode());
        assertNotEquals(field1.hashCode(), field4.hashCode());

        assertNotEquals(field1, field2);
        assertNotEquals(field1, field3);
        assertNotEquals(field1, field4);

    }

    @Test
    void fullToString() {
        MutableUserAgent userAgent1 = new MutableUserAgent("Some'Agent");
        userAgent1.get("Niels").setValue("Basjes", 42);

        userAgent1.get("BackToDefault").setValue("One", 42);
        assertEquals("One", userAgent1.getValue("BackToDefault"));
        assertEquals(42, userAgent1.getConfidence("BackToDefault"));

        userAgent1.get("BackToDefault").setValue("<<<null>>>", 84);
        assertTrue(userAgent1.get("BackToDefault").isDefaultValue());
        assertEquals("Unknown", userAgent1.getValue("BackToDefault"));
        assertEquals(84, userAgent1.getConfidence("BackToDefault"));

        assertEquals("Basjes", userAgent1.getValue("Niels"));
        assertEquals(42, userAgent1.getConfidence("Niels"));

        UserAgent userAgent2 = new ImmutableUserAgent(userAgent1);

        assertEquals("Basjes", userAgent2.getValue("Niels"));
        assertEquals(42, userAgent2.getConfidence("Niels"));

        // Also values that are "Default" are copied.
        assertTrue(userAgent2.get("BackToDefault").isDefaultValue());
        assertEquals("Unknown", userAgent2.getValue("BackToDefault"));
        // You may have a default value ("<<<null>>>") with a confidence.
        assertEquals(84, userAgent2.getConfidence("BackToDefault"));

        assertEquals(
            // You get the fields in the order you ask them!
            "\n" +
            "- test:\n" +
            "    input:\n" +
            "      'User-Agent'      : 'Some''Agent'\n" +
            "    expected:\n" +
            "      Niels                               : 'Basjes'\n" +
            "      DeviceClass                         : 'Unknown'\n",
            userAgent1.toString("Niels", "DeviceClass"));

        assertEquals(
            "\n" +
            "- test:\n" +
            "    input:\n" +
            "      'User-Agent'      : 'Some''Agent'\n" +
            "    expected:\n" +
            "      DeviceClass                          : 'Unknown'\n" +
            "      DeviceName                           : 'Unknown'\n" +
            "      DeviceBrand                          : 'Unknown'\n" +
            "      OperatingSystemClass                 : 'Unknown'\n" +
            "      OperatingSystemName                  : 'Unknown'\n" +
            "      OperatingSystemVersion               : '??'\n" +
            "      OperatingSystemVersionMajor          : '??'\n" +
            "      OperatingSystemNameVersion           : 'Unknown ??'\n" +
            "      OperatingSystemNameVersionMajor      : 'Unknown ??'\n" +
            "      LayoutEngineClass                    : 'Unknown'\n" +
            "      LayoutEngineName                     : 'Unknown'\n" +
            "      LayoutEngineVersion                  : '??'\n" +
            "      LayoutEngineVersionMajor             : '??'\n" +
            "      LayoutEngineNameVersion              : 'Unknown ??'\n" +
            "      LayoutEngineNameVersionMajor         : 'Unknown ??'\n" +
            "      AgentClass                           : 'Unknown'\n" +
            "      AgentName                            : 'Unknown'\n" +
            "      AgentVersion                         : '??'\n" +
            "      AgentVersionMajor                    : '??'\n" +
            "      AgentNameVersion                     : 'Unknown ??'\n" +
            "      AgentNameVersionMajor                : 'Unknown ??'\n" +
            "      BackToDefault                        : 'Unknown'\n" +
            "      Niels                                : 'Basjes'\n" +
            "      __SyntaxError__                      : 'false'\n",
            userAgent1.toString());

        assertEquals(userAgent1.getAvailableFieldNamesSorted(), userAgent2.getAvailableFieldNamesSorted());

        assertEquals(userAgent1.toString(),         userAgent2.toString());
        assertEquals(userAgent1.toXML(),            userAgent2.toXML());
        assertEquals(userAgent1.toYamlTestCase(),   userAgent2.toYamlTestCase());
    }

    @Test
    void limitedToString() {
        List<String> wanted = Arrays.asList("Unsure", "DeviceClass", "AgentVersion");

        // When only asking for a limited set of fields then the internal datastructures are
        // initialized with only the known attributes for which we have 'non standard' default values.
        MutableUserAgent userAgent = new MutableUserAgent("Some Agent", wanted);

        assertEquals(
            "\n" +
            "- test:\n" +
            "    input:\n" +
            "      'User-Agent'      : 'Some Agent'\n" +
            "    expected:\n" +
            "      DeviceClass                         : 'Unknown'\n" +
            "      AgentVersion                        : '??'\n" +
            "      Unsure                              : 'Unknown'\n" +
            "      __SyntaxError__                     : 'false'\n",
            userAgent.toString());

        // We set the wanted field
        userAgent.get("Unsure").setValueForced("--> Unsure",       -1); // --> isDefault !
        userAgent.set("DeviceClass",   "--> DeviceClass",   1);
        userAgent.set("AgentVersion",  "--> AgentVersion",  2);
        // We also set an unwanted field
        userAgent.set("SomethingElse", "--> SomethingElse", 3);

        // We should be able to retrieve all of them
        assertEquals("--> Unsure",          userAgent.getValue("Unsure"));
        assertEquals("--> DeviceClass",     userAgent.getValue("DeviceClass"));
        assertEquals("--> AgentVersion",    userAgent.getValue("AgentVersion"));
        assertEquals("--> SomethingElse",   userAgent.getValue("SomethingElse"));

        // Default value?
        assertTrue(userAgent.get("Unsure").isDefaultValue());
        assertFalse(userAgent.get("DeviceClass").isDefaultValue());
        assertFalse(userAgent.get("AgentVersion").isDefaultValue());
        assertFalse(userAgent.get("SomethingElse").isDefaultValue());

        // The output map if requested should only contain the wanted fields.
        assertEquals(
            "\n" +
            "- test:\n" +
            "    input:\n" +
            "      'User-Agent'      : 'Some Agent'\n" +
            "    expected:\n" +
            "      DeviceClass                         : '--> DeviceClass'\n" +
            "      AgentVersion                        : '--> AgentVersion'\n" +
            "      Unsure                              : '--> Unsure'\n" +
            "      __SyntaxError__                     : 'false'\n",
            userAgent.toString());

        userAgent.destroy();
    }

    // https://github.com/nielsbasjes/yauaa/issues/426
    // When asking for only specific fields they should always
    // be in the end result even if the output is not calculated by anything (i.e. "Unknown").
    @Test
    void bugReport426() {
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .withField(DEVICE_BRAND)
            .build();
        UserAgent result = uaa.parse("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36 Edg/96.0.1054.2");
        Map<String, String> resultMap = result.toMap();
        // As requested: DeviceBrand
        assertEquals("Unknown", resultMap.get(DEVICE_BRAND));
        // Needed specifically for DeviceBrand calculation
        assertEquals("Unknown", resultMap.get(AGENT_INFORMATION_URL));
        assertEquals("Unknown", resultMap.get(AGENT_INFORMATION_EMAIL));
        // Always needed
        assertEquals("Desktop", resultMap.get(DEVICE_CLASS));

        // Not requested and not needed so not present in the produced Map.
        assertNull(resultMap.get(AGENT_NAME));

        // When retrieving directly from the UserAgent requesting a non-exsiting field will yield a Default answer.
        assertFalse(result.getAvailableFieldNamesSorted().contains(AGENT_NAME));
        AgentField agentField = result.get(AGENT_NAME);
        assertTrue(agentField.isDefaultValue());
        assertEquals("Unknown", agentField.getValue());
        assertEquals(-1, agentField.getConfidence());
    }

    /**
     * If a field has never been extracted yet it must still be present.
     */
    @Test
    void verifyThatUnextractedFieldsArePresent() {
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .immediateInitialization()
            .withField(DEVICE_BRAND)
            .withField(AGENT_INFORMATION_URL)
            .withField(AGENT_INFORMATION_EMAIL)
            .withField(DEVICE_CLASS)
            .withField(AGENT_NAME)
            .build();
        UserAgent result = uaa.parse("Mozilla/5.0 (SM-123) Niels Basjes/42");
        Map<String, String> resultMap = result.toMap();
        // As requested: DeviceBrand
        assertEquals("Samsung", resultMap.get(DEVICE_BRAND));
        assertEquals("Unknown", resultMap.get(AGENT_INFORMATION_URL));
        assertEquals("Unknown", resultMap.get(AGENT_INFORMATION_EMAIL));
        assertEquals("Unknown", resultMap.get(DEVICE_CLASS));
        assertEquals("Niels Basjes", resultMap.get(AGENT_NAME));

        // And fields that are not implicitly calculated and not asked for are absent
        assertNull(resultMap.get(DEVICE_NAME));
        assertNull(resultMap.get(OPERATING_SYSTEM_CLASS));
    }

    /**
     * If a field has never been extracted yet it must still be present.
     */
    @Test
    void testCaseInsensitiveHeaders() {
        MutableUserAgent mutableUserAgent = new MutableUserAgent();
        mutableUserAgent.addHeader("ONE", "1");
        mutableUserAgent.addHeader("TWO", "2");

        assertEquals("1", mutableUserAgent.getHeaders().get("oNe"));
        assertEquals("1", mutableUserAgent.getHeaders().get("OnE"));
        assertEquals("2", mutableUserAgent.getHeaders().get("tWo"));
        assertEquals("2", mutableUserAgent.getHeaders().get("TwO"));

        ImmutableUserAgent immutableUserAgent = new ImmutableUserAgent(mutableUserAgent);

        assertEquals("1", immutableUserAgent.getHeaders().get("oNe"));
        assertEquals("1", immutableUserAgent.getHeaders().get("OnE"));
        assertEquals("2", immutableUserAgent.getHeaders().get("tWo"));
        assertEquals("2", immutableUserAgent.getHeaders().get("TwO"));
    }




}
