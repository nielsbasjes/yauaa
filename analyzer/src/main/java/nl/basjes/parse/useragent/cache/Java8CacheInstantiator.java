/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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
package nl.basjes.parse.useragent.cache;

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.CacheInstantiator;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.collections4.map.LRUMap;

import java.util.Collections;
import java.util.Map;

public class Java8CacheInstantiator implements CacheInstantiator {
    @Override
    public Map<String, UserAgent.ImmutableUserAgent> instantiateCache(int cacheSize) {
        return Collections.synchronizedMap(new LRUMap<>(cacheSize));
    }
}
