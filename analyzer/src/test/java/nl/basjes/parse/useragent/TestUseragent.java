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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.AgentField.MutableAgentField;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VALUE;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        ((MutableAgentField)agent.get("UnknownOne")).reset();
        check(agent, "UnknownOne", "Unknown", -1);

        // Setting unknown new attributes FORCED
        assertNotNull(agent.get("UnknownTwo"));
        assertEquals("Unknown", agent.getValue("UnknownTwo"));
        assertEquals(-1, agent.getConfidence("UnknownTwo").longValue());
        agent.setForced("UnknownTwo", "Two", 222);
        check(agent, "UnknownTwo", "Two", 222);
        ((MutableAgentField)agent.get("UnknownTwo")).reset();
        check(agent, "UnknownTwo", "Unknown", -1);

        // Setting known attributes
        check(agent, "AgentClass", "Unknown", -1);
        agent.set("AgentClass", "One", 111);
        check(agent, "AgentClass", "One", 111);
        ((MutableAgentField)agent.get("AgentClass")).reset();
        check(agent, "AgentClass", "Unknown", -1);

        // Setting known attributes FORCED
        check(agent, "AgentVersion", "??", -1);
        agent.setForced("AgentVersion", "Two", 222);
        check(agent, "AgentVersion", "Two", 222);
        ((MutableAgentField)agent.get("AgentVersion")).reset();
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

        MutableAgentField field = (MutableAgentField) agent.get(name);
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
        MutableAgentField origNull = new MutableAgentField(null);
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
        MutableAgentField copyFoo = new MutableAgentField(null); // Different default!
        copyFoo.setValue(origFoo);

        assertEquals("Two", copyFoo.getValue());
        assertEquals(2, copyFoo.getConfidence());
        copyFoo.reset();
        assertNull(copyFoo.getValue()); // The default should NOT be modified
        assertEquals(-1, copyFoo.getConfidence());
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

        MutableAgentField field4 = new MutableAgentField(null); // Same, different default
        field4.setValue("One", 1);

        // We compare the base agent with 4 variations
        baseAgent.setImmediateForTesting("Field", field0);
        mutableAgent0.setImmediateForTesting("Field", field1); // Same
        mutableAgent1.setImmediateForTesting("Field", field1); // Different useragent
        mutableAgent2.setImmediateForTesting("Field", field2); // Different field value
        mutableAgent3.setImmediateForTesting("Field", field3); // Different field confidence
        mutableAgent4.setImmediateForTesting("Field", field4); // Different field default value

        // Check em
        assertEquals(baseAgent, baseAgent);
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

        MutableAgentField field4 = new MutableAgentField(null); // Same, different default
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
        ((MutableAgentField)userAgent1.get("Niels")).setValue("Basjes", 42);

        ((MutableAgentField)userAgent1.get("BackToDefault")).setValue("One", 42);
        assertEquals("One", userAgent1.getValue("BackToDefault"));
        assertEquals(42, userAgent1.getConfidence("BackToDefault"));

        ((MutableAgentField)userAgent1.get("BackToDefault")).setValue("<<<null>>>", 84);
        assertTrue(userAgent1.get("BackToDefault").isDefaultValue());
        assertEquals("Unknown", userAgent1.getValue("BackToDefault"));
        assertEquals(84, userAgent1.getConfidence("BackToDefault"));

        assertEquals("Basjes", userAgent1.getValue("Niels"));
        assertEquals(42, userAgent1.getConfidence("Niels"));

        UserAgent userAgent2 = new ImmutableUserAgent(userAgent1);

        assertEquals("Basjes", userAgent2.getValue("Niels"));
        assertEquals(42, userAgent2.getConfidence("Niels"));

        // A values that is "Default" is not copied so when getting the confidence it will return -1
        assertTrue(userAgent2.get("BackToDefault").isDefaultValue());
        assertEquals("Unknown", userAgent2.getValue("BackToDefault"));
        assertEquals(-1, userAgent2.getConfidence("BackToDefault"));

        assertEquals(
            // You get the fields in the order you ask them!
            "  - user_agent_string: 'Some''Agent'\n" +
            "    Niels        : 'Basjes'\n" +
            "    DeviceClass  : 'Unknown'\n",
            userAgent1.toString("Niels", "DeviceClass"));

        assertEquals(
            "  - user_agent_string: 'Some''Agent'\n" +
            "    DeviceClass                      : 'Unknown'\n" +
            "    DeviceName                       : 'Unknown'\n" +
            "    DeviceBrand                      : 'Unknown'\n" +
            "    OperatingSystemClass             : 'Unknown'\n" +
            "    OperatingSystemName              : 'Unknown'\n" +
            "    OperatingSystemVersion           : '??'\n" +
            "    OperatingSystemVersionMajor      : '??'\n" +
            "    OperatingSystemNameVersion       : 'Unknown ??'\n" +
            "    OperatingSystemNameVersionMajor  : 'Unknown ??'\n" +
            "    LayoutEngineClass                : 'Unknown'\n" +
            "    LayoutEngineName                 : 'Unknown'\n" +
            "    LayoutEngineVersion              : '??'\n" +
            "    LayoutEngineVersionMajor         : '??'\n" +
            "    LayoutEngineNameVersion          : 'Unknown ??'\n" +
            "    LayoutEngineNameVersionMajor     : 'Unknown ??'\n" +
            "    AgentClass                       : 'Unknown'\n" +
            "    AgentName                        : 'Unknown'\n" +
            "    AgentVersion                     : '??'\n" +
            "    AgentVersionMajor                : '??'\n" +
            "    AgentNameVersion                 : 'Unknown ??'\n" +
            "    AgentNameVersionMajor            : 'Unknown ??'\n" +
            "    Niels                            : 'Basjes'\n",
            userAgent1.toString());

        assertEquals(userAgent1.getAvailableFieldNamesSorted(), userAgent2.getAvailableFieldNamesSorted());

        assertEquals(userAgent1.toString(),         userAgent2.toString());
        assertEquals(userAgent1.toXML(),            userAgent2.toXML());
        assertEquals(userAgent1.toYamlTestCase(),   userAgent2.toYamlTestCase());
    }

    @Test
    void limitedToString() {
        List<String> wanted = Arrays.asList("DeviceClass", "AgentVersion", "SomethingElse");

        // When only asking for a limited set of fields then the internal datastructures are
        // initialized with only the known attributes for which we have 'non standard' default values.
        MutableUserAgent userAgent = new MutableUserAgent("Some Agent", wanted);

        assertEquals(
            "  - user_agent_string: 'Some Agent'\n", // +
//            "    DeviceClass   : 'Unknown'\n" +
//            "    AgentVersion  : '??'\n",
            userAgent.toString());

        assertEquals("Unknown", userAgent.getValue("DeviceClass"));
        assertEquals("??",      userAgent.getValue("AgentVersion"));
        assertEquals("Unknown", userAgent.getValue("SomethingElse"));
        userAgent.destroy();
    }
}
