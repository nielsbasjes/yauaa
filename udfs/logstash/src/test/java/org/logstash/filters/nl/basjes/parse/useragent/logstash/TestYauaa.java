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
import org.junit.Assert;
import org.junit.Test;
import org.logstash.Event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestYauaa {

    @Test
    public void testYauaaFilter() {
        String            sourceField = "foo";

        Map<String, String> fieldMappings = new HashMap<>();
        fieldMappings.put("DeviceClass", "DC");
        fieldMappings.put("AgentNameVersion", "ANV");

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("source", sourceField);
        configMap.put("fields", fieldMappings);

        Configuration     config      = new Configuration(configMap);

        Context context = new Context();
        Filter  filter  = new Yauaa(config, context);

        Event e = new Event();
        e.setField(sourceField,
            "Mozilla/5.0 (X11; Linux x86_64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/48.0.2564.82 Safari/537.36");

        Collection<Event> results = filter.filter(Collections.singletonList(e));

        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Desktop", e.getField("DC"));
        Assert.assertEquals("Chrome 48.0.2564.82", e.getField("ANV"));
    }

}
