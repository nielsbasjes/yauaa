/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2024 Niels Basjes
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

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.ClientHintsCacheInstantiator;
import org.apache.commons.collections4.map.LRUMap;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class Java8ClientHintsCacheInstantiator<T extends Serializable> implements ClientHintsCacheInstantiator<T> {
    public Map<String, T> instantiateCache(int cacheSize) {
        return Collections.synchronizedMap(new LRUMap<>(cacheSize));
    }
}
