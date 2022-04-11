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

import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;

public interface Analyzer extends Serializable {
    /**
     * Parses and analyzes the provided useragent string without looking at ClientHints
     * @param requestHeaders The map of all relevant request headers. This must include atleast the User-Agent.
     * @return An ImmutableUserAgent record that holds all of the results.
     */
    @Nonnull
    default ImmutableUserAgent parse(Map<String, String> requestHeaders){
        return new ImmutableUserAgent(new MutableUserAgent());
    }

    /**
     * Parses and analyzes the provided useragent string
     * @param userAgentString The User-Agent String that is to be parsed and analyzed
     * @return An ImmutableUserAgent record that holds all of the results.
     */
    @Nonnull
    default ImmutableUserAgent parse(String userAgentString) {
        return parse(Collections.singletonMap(USERAGENT_HEADER, userAgentString));
    }

}
