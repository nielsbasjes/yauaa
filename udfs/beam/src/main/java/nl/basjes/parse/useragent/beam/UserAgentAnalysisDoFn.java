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

package nl.basjes.parse.useragent.beam;

import nl.basjes.parse.useragent.annotate.UserAgentAnnotationAnalyzer;
import nl.basjes.parse.useragent.annotate.UserAgentAnnotationMapper;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

import static nl.basjes.parse.useragent.UserAgentAnalyzer.DEFAULT_PARSE_CACHE_SIZE;

public abstract class UserAgentAnalysisDoFn<T extends Serializable> extends DoFn<T, T>
    implements UserAgentAnnotationMapper<T>, Serializable {
    private transient UserAgentAnnotationAnalyzer<T> userAgentAnalyzer = null;

    private int cacheSize;

    public UserAgentAnalysisDoFn() {
        this.cacheSize = DEFAULT_PARSE_CACHE_SIZE;
    }

    public UserAgentAnalysisDoFn(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Setup
    public void initialize() {
        userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
        userAgentAnalyzer.setCacheSize(cacheSize);
        userAgentAnalyzer.initialize(this);
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
        // Currently Beam does not allow changing the input instance.
        // So unfortunately we must clone the entire thing :(
        // See also: https://issues.apache.org/jira/browse/BEAM-1164
        c.output(userAgentAnalyzer.map(clone(c.element())));
    }

    /**
     * Clone the provided instance of T.
     * This default implementation uses a mindless brute force cloning via serialization.
     * If for your class you can do better; please override this method.
     * For AVRO you can do something like MyRecord.newBuilder(instance).build();
     * @param t The original input value
     * @return A deep copied copy of t
     */
    public T clone(T t) {
        return SerializationUtils.clone(t);
    }

}
