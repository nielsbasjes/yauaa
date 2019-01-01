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

package nl.basjes.parse.useragent.flink;

import nl.basjes.parse.useragent.annotate.UserAgentAnnotationAnalyzer;
import nl.basjes.parse.useragent.annotate.UserAgentAnnotationMapper;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

import java.io.Serializable;

import static nl.basjes.parse.useragent.UserAgentAnalyzer.DEFAULT_PARSE_CACHE_SIZE;

public abstract class UserAgentAnalysisMapper<T> extends RichMapFunction<T, T>
    implements UserAgentAnnotationMapper<T>, Serializable {
    private transient UserAgentAnnotationAnalyzer<T> userAgentAnalyzer = null;

    private int cacheSize;

    public UserAgentAnalysisMapper() {
        this.cacheSize = DEFAULT_PARSE_CACHE_SIZE;
    }

    public UserAgentAnalysisMapper(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    public void open(Configuration parameters) {
        userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
        userAgentAnalyzer.setCacheSize(cacheSize);
        userAgentAnalyzer.initialize(this);
    }

    @Override
    public T map(T record) {
        return userAgentAnalyzer.map(record);
    }
}
