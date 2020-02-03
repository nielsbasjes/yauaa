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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.UserAgent.AgentField;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestUseragent {

    private static final Logger LOG = LoggerFactory.getLogger(TestUseragent.class);

    @Test
    public void testUseragent() {
        String    uaString = "Foo Bar";
        UserAgent agent    = new UserAgent(uaString);
        assertEquals(uaString, agent.get(UserAgent.USERAGENT_FIELDNAME).getValue());
        assertEquals(0, agent.get(UserAgent.USERAGENT_FIELDNAME).getConfidence());
        assertEquals(uaString, agent.getValue(UserAgent.USERAGENT_FIELDNAME));
        assertEquals(0L, agent.getConfidence(UserAgent.USERAGENT_FIELDNAME).longValue());
    }

    @Test
    public void testUseragentValues() {
        testUseragentValuesDebug(true);
        testUseragentValuesDebug(false);
    }

    private void testUseragentValuesDebug(boolean debug) {
        String    name     = "Attribute";
        String    uaString = "Foo Bar";
        UserAgent agent    = new UserAgent(uaString);
        agent.setDebug(debug);

        // Setting unknown new attributes
        assertNull(agent.get("UnknownOne"));
        assertEquals("Unknown", agent.getValue("UnknownOne"));
        assertEquals(-1, agent.getConfidence("UnknownOne").longValue());
        agent.set("UnknownOne", "One", 111);
        check(agent, "UnknownOne", "One", 111);
        agent.get("UnknownOne").reset();
        check(agent, "UnknownOne", null, -1);

        // Setting unknown new attributes FORCED
        assertNull(agent.get("UnknownTwo"));
        assertEquals("Unknown", agent.getValue("UnknownTwo"));
        assertEquals(-1, agent.getConfidence("UnknownTwo").longValue());
        agent.setForced("UnknownTwo", "Two", 222);
        check(agent, "UnknownTwo", "Two", 222);
        agent.get("UnknownTwo").reset();
        check(agent, "UnknownTwo", null, -1);

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
        check(agent, name, null, -1); // -1 --> SPECIAL CASE!!!

        agent.set(name, "Four", 4); // Should be IGNORED (special case remember)
        check(agent, name, null, -1); // -1 --> SPECIAL CASE!!!

        // Set a 'normal' value again.
        agent.set(name, "Three", 333); // Should be used
        check(agent, name, "Three", 333);

        AgentField field = agent.get(name);
        field.setValueForced("Five", 5); // Should be used
        check(agent, name, "Five", 5);
        field.setValueForced("<<<null>>>", 4); // Should be used
        check(agent, name, null, -1); // -1 --> SPECIAL CASE!!!
    }


    private void check(UserAgent agent, String name, String expectedValue, long expectedConfidence) {
        assertEquals(expectedValue, agent.get(name).getValue());
        assertEquals(expectedValue, agent.getValue(name));
        assertEquals(expectedConfidence, agent.get(name).getConfidence());
        assertEquals(expectedConfidence, agent.getConfidence(name).longValue());
    }

    @Test
    public void testCopying() {
        String name = "Foo";

        AgentField origNull = new AgentField(null);
        origNull.setValue("One", 1);
        AgentField copyNull = new AgentField("Foo"); // Different default!
        copyNull.setValue(origNull);

        assertEquals("One", copyNull.getValue());
        assertEquals(1, copyNull.getConfidence());
        copyNull.reset();
        assertEquals("Foo", copyNull.getValue()); // The default should NOT be modified
        assertEquals(-1, copyNull.getConfidence());


        AgentField origFoo = new AgentField("Foo");
        origFoo.setValue("Two", 2);
        AgentField copyFoo = new AgentField(null); // Different default!
        copyFoo.setValue(origFoo);

        assertEquals("Two", copyFoo.getValue());
        assertEquals(2, copyFoo.getConfidence());
        copyFoo.reset();
        assertNull(copyFoo.getValue()); // The default should NOT be modified
        assertEquals(-1, copyFoo.getConfidence());
    }

    @Test
    public void comparingUserAgents() {
        UserAgent baseAgent = new UserAgent("Something 2");
        UserAgent agent0    = new UserAgent("Something 2");
        UserAgent agent1    = new UserAgent("Something 1");
        UserAgent agent2    = new UserAgent("Something 2");
        UserAgent agent3    = new UserAgent("Something 2");
        UserAgent agent4    = new UserAgent("Something 2");

        AgentField field0 = new AgentField("Foo");
        field0.setValue("One", 1);

        AgentField field1 = new AgentField("Foo");
        field1.setValue("One", 1);

        AgentField field2 = new AgentField("Foo"); // Same, different value
        field2.setValue("Two", 1);

        AgentField field3 = new AgentField("Foo"); // Same, different confidence
        field3.setValue("One", 2);

        AgentField field4 = new AgentField(null); // Same, different default
        field4.setValue("One", 1);

        // We compare the base agent with 4 variations
        baseAgent.setImmediateForTesting("Field", field0);
        agent0.setImmediateForTesting("Field", field1); // Same
        agent1.setImmediateForTesting("Field", field1); // Different useragent
        agent2.setImmediateForTesting("Field", field2); // Different field value
        agent3.setImmediateForTesting("Field", field3); // Different field confidence
        agent4.setImmediateForTesting("Field", field4); // Different field default value

        // Check em
        assertEquals(baseAgent, baseAgent);
        assertEquals(baseAgent, agent0);
        assertEquals(agent0, baseAgent);
        assertEquals(baseAgent.hashCode(), agent0.hashCode());

        LOG.info(baseAgent.toString("Field"));

        assertNotEquals(baseAgent, agent2);
        assertNotEquals(baseAgent, agent3);
        assertNotEquals(baseAgent, agent4);

        assertNotEquals("String", agent1);
        assertNotEquals(agent1, "String");
    }

    @Test
    public void comparingUserAgentFields() {
        AgentField field0 = new AgentField("Foo");
        field0.setValue("One", 1);

        AgentField field1 = new AgentField("Foo");
        field1.setValue("One", 1);

        AgentField field2 = new AgentField("Foo"); // Same, different value
        field2.setValue("Two", 1);

        AgentField field3 = new AgentField("Foo"); // Same, different confidence
        field3.setValue("One", 2);

        AgentField field4 = new AgentField(null); // Same, different default
        field4.setValue("One", 1);

        // This is mainly used when rendering in a debugger.
        assertEquals("{ value:'One', confidence:'1', default:'Foo' }", field0.toString());
        assertEquals("{ value:'One', confidence:'1', default:'Foo' }", field1.toString());
        assertEquals("{ value:'Two', confidence:'1', default:'Foo' }", field2.toString());
        assertEquals("{ value:'One', confidence:'2', default:'Foo' }", field3.toString());
        assertEquals("{ value:'One', confidence:'1', default:null }", field4.toString());

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
    public void fullToString() {
        UserAgent userAgent = new UserAgent("Some'Agent");

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
            "    AgentNameVersionMajor            : 'Unknown ??'\n",
            userAgent.toString());
    }

    @Test
    public void limitedToString() {
        List<String> wanted = Arrays.asList("DeviceClass", "AgentVersion", "SomethingElse");

        // When only asking for a limited set of fields then the internal datastructures are
        // initialized with only the known attributes for which we have 'non standard' default values.
        UserAgent userAgent = new UserAgent("Some Agent", wanted);

        assertEquals(
            "  - user_agent_string: 'Some Agent'\n" +
            "    DeviceClass   : 'Unknown'\n" +
            "    AgentVersion  : '??'\n",
            userAgent.toString());

        assertEquals("Unknown", userAgent.getValue("DeviceClass"));
        assertEquals("??",      userAgent.getValue("AgentVersion"));
        assertEquals("Unknown", userAgent.getValue("SomethingElse"));
    }
}
