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

package nl.basjes.parse.useragent.utils;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class CaseInsensitiveStringTreeMap<V> extends TreeMap<String, V> {
    @Override
    public V put(String key, V value) {
        return super.put(key.toLowerCase(Locale.ROOT), value);
    }

    @Override
    public V get(Object key) {
        return super.get(key.toString().toLowerCase(Locale.ROOT));
    }

    public CaseInsensitiveStringTreeMap() {
        super();
    }

    public CaseInsensitiveStringTreeMap(Map<? extends String, ? extends V> m) {
        super(m);
    }
}
