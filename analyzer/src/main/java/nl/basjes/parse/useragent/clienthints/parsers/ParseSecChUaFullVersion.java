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

package nl.basjes.parse.useragent.clienthints.parsers;

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.ClientHintsCacheInstantiator;
import nl.basjes.parse.useragent.clienthints.ClientHints;
import nl.basjes.parse.useragent.clienthints.ClientHints.Brand;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;

public class ParseSecChUaFullVersion implements CHParser {

    public static final String HEADER_FIELD       = "Sec-CH-UA-Full-Version";
    public static final String HEADER_SPEC_URL    = "https://wicg.github.io/ua-client-hints/#sec-ch-ua-full-version";
    public static final String HEADER_SPEC        = "The Sec-CH-UA-Full-Version request header field gives a server information about the user agent’s full version. Sec-CH-UA-Full-Version is deprecated and will be removed in the future. Developers should use Sec-CH-UA-Full-Version-List instead.";

    private transient Map<String, ArrayList<Brand>> cache;

    public ParseSecChUaFullVersion() {
        // Nothing to do right now
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initializeCache(@Nonnull ClientHintsCacheInstantiator<?> clientHintsCacheInstantiator, int cacheSize) {
        if (cacheSize <= 0) {
            cache = null;
        } else {
            cache = (Map<String, ArrayList<Brand>>) clientHintsCacheInstantiator.instantiateCache(cacheSize);
        }
    }

    @Override
    public void clearCache() {
        if (cache != null) {
            cache.clear();
        }
    }

    //   From https://wicg.github.io/ua-client-hints/#http-ua-hints
    //
    //   3.4. The 'Sec-CH-UA-Full-Version' Header Field
    //   Sec-CH-UA-Full-Version is deprecated and will be removed in the future.
    //   Developers should use Sec-CH-UA-Full-Version-List instead.
    //
    //   The Sec-CH-UA-Full-Version request header field gives a server information about the user agent’s full version.
    //   It is a Structured Header whose value MUST be a string [RFC8941].
    //
    //   The header’s ABNF is:
    //
    //     Sec-CH-UA-Full-Version = sf-string
    //

    @Nonnull
    @Override
    public ClientHints parse(@Nonnull Map<String, String> clientHintsHeaders, @Nonnull ClientHints clientHints, @Nonnull String headerName) {
        String input = clientHintsHeaders.get(headerName);
        String value = parseSfString(input);
        if (value != null && !value.isEmpty()) {
            clientHints.setFullVersion(value);
        }
        return clientHints;
    }

    @Nonnull
    @Override
    public String inputField() {
        return HEADER_FIELD;
    }
}
