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

package org.logstash.filters.nl.basjes.parse.useragent.logstash;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.v0.Filter;
import org.junit.jupiter.api.Test;
import org.logstash.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestYauaa {

    @Test
    public void testNormalUse() {
        String sourceField = "foo";

        Map<String, String> fieldMappings = new HashMap<>();
        fieldMappings.put("DeviceClass", "DC");
        fieldMappings.put("AgentNameVersion", "ANV");

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("source", sourceField);
        configMap.put("fields", fieldMappings);

        Configuration config = new Configuration(configMap);

        Context context = new Context();
        Filter  filter  = new Yauaa(config, context);

        Event e = new Event();
        e.setField(sourceField,
            "Mozilla/5.0 (X11; Linux x86_64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/48.0.2564.82 Safari/537.36");

        Collection<Event> results = filter.filter(Collections.singletonList(e));

        assertEquals(1, results.size());
        assertEquals("Desktop", e.getField("DC"));
        assertEquals("Chrome 48.0.2564.82", e.getField("ANV"));
    }

    @Test
    public void testBadConfigNothing() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Map<String, Object> configMap = new HashMap<>();
            Configuration       config    = new Configuration(configMap);
            Context             context   = new Context();
            Filter              filter    = new Yauaa(config, context);
        });
        assertTrue(
            allOf(
                containsString("The \"source\" has not been specified."),
                containsString("The list of needed \"fields\" has not been specified."))
                .matches(exception.getMessage()));
    }

    @Test
    public void testBadConfigNoSource() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Map<String, String> fieldMappings = new HashMap<>();
            fieldMappings.put("DeviceClass", "DC");
            fieldMappings.put("AgentNameVersion", "ANV");

            Map<String, Object> configMap = new HashMap<>();
            configMap.put("fields", fieldMappings);

            Configuration config = new Configuration(configMap);

            Context context = new Context();
            Filter  filter  = new Yauaa(config, context);
        });
        assertTrue(exception.getMessage().contains("The \"source\" has not been specified."));
    }

    @Test
    public void testBadConfigEmptySource() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Map<String, String> fieldMappings = new HashMap<>();
            fieldMappings.put("DeviceClass", "DC");
            fieldMappings.put("AgentNameVersion", "ANV");

            Map<String, Object> configMap = new HashMap<>();
            configMap.put("source", ""); // EMPTY STRING
            configMap.put("fields", fieldMappings);

            Configuration config = new Configuration(configMap);

            Context context = new Context();
            Filter  filter  = new Yauaa(config, context);
        });
        assertTrue(exception.getMessage().contains("The \"source\" is empty."));
    }

    @Test
    public void testBadConfigNoFields() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("source", "foo");

            Configuration config = new Configuration(configMap);

            Context context = new Context();
            Filter  filter  = new Yauaa(config, context);
        });
        assertTrue(exception.getMessage().contains("The list of needed \"fields\" has not been specified."));
    }

    @Test
    public void testBadConfigFieldsEmpty() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Map<String, String> fieldMappings = new HashMap<>();

            Map<String, Object> configMap = new HashMap<>();
            configMap.put("source", "foo");
            configMap.put("fields", fieldMappings);

            Configuration config = new Configuration(configMap);

            Context context = new Context();
            Filter  filter  = new Yauaa(config, context);
        });
        assertTrue(exception.getMessage().contains("The list of needed \"fields\" is empty."));
    }

    @Test
    public void testBadConfigIllegalField() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Map<String, String> fieldMappings = new HashMap<>();
            fieldMappings.put("NoSuchField", "NSF");
            fieldMappings.put("AgentNameVersion", "ANV");

            Map<String, Object> configMap = new HashMap<>();
            configMap.put("source", "foo");
            configMap.put("fields", fieldMappings);

            Configuration config = new Configuration(configMap);

            Context context = new Context();
            Filter  filter  = new Yauaa(config, context);
        });
        assertTrue(exception.getMessage().contains("The requested field \"NoSuchField\" does not exist."));
    }

    @Test
    public void testBadConfigIllegalFieldNoSource() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Map<String, String> fieldMappings = new HashMap<>();
            fieldMappings.put("NoSuchField", "NSF");
            fieldMappings.put("AgentNameVersion", "ANV");

            Map<String, Object> configMap = new HashMap<>();
            configMap.put("fields", fieldMappings);

            Configuration config = new Configuration(configMap);

            Context context = new Context();
            Filter  filter  = new Yauaa(config, context);
        });
        assertTrue(
            allOf(
                containsString("The \"source\" has not been specified."),
                containsString("The requested field \"NoSuchField\" does not exist."))
                .matches(exception.getMessage()));
    }

    @Test
    public void testBadConfigIllegalFieldEmptySource() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Map<String, String> fieldMappings = new HashMap<>();
            fieldMappings.put("NoSuchField", "NSF");
            fieldMappings.put("AgentNameVersion", "ANV");

            Map<String, Object> configMap = new HashMap<>();
            configMap.put("source", "");
            configMap.put("fields", fieldMappings);

            Configuration config = new Configuration(configMap);

            Context context = new Context();
            Filter  filter  = new Yauaa(config, context);
        });
        assertTrue(
            allOf(
                containsString("The \"source\" is empty."),
                containsString("The requested field \"NoSuchField\" does not exist."))
                .matches(exception.getMessage()));
    }

}
