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

public class ParseSecChUa implements CHParser {

    public static final String HEADER_FIELD         = "Sec-CH-UA";
    public static final String HEADER_SPEC_URL      = "https://wicg.github.io/ua-client-hints/#sec-ch-ua";
    public static final String HEADER_SPEC          = "The Sec-CH-UA request header field gives a server information about a user agent's branding and version.";

    private transient Map<String, ArrayList<Brand>> cache = null;

    public ParseSecChUa() {
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
    //   3.1. The 'Sec-CH-UA' Header Field
    //   The Sec-CH-UA request header field gives a server information about a user agent's branding and version.
    //   It is a Structured Header whose value MUST be a list [RFC8941].
    //   The list’s items MUST be string.
    //   The value of each item SHOULD include a "v" parameter, indicating the user agent's version.
    //   The header’s ABNF is:
    //   Sec-CH-UA = sf-list
    //   To return the Sec-CH-UA value for a request, perform the following steps:
    //   Let brands be the result of running create brands with "significant version".
    //   Let list be the result of creating a brand-version list, with brands and "significant version".
    //
    //   Return the output of running serializing a list with list.
    //   Note: Unlike most Client Hints, since it’s included in the low entropy hint table,
    //          the Sec-CH-UA header will be sent by default, whether or not the server opted-into receiving
    //          the header via an Accept-CH header (although it can still be controlled by it’s policy controlled
    //          client hints feature. It is considered low entropy because it includes only the user agent's
    //          branding information, and the significant version number (both of which are fairly clearly sniffable
    //          by "examining the structure of other headers and by testing for the availability and semantics of
    //          the features introduced or modified between releases of a particular browser" [Janc2014]).

    @Nonnull
    @Override
    public ClientHints parse(@Nonnull Map<String, String> clientHintsHeaders, @Nonnull ClientHints clientHints, @Nonnull String headerName) {
        String input = clientHintsHeaders.get(headerName);
        if (input == null) {
            return clientHints;
        }
        // " Not A;Brand";v="99", "Chromium";v="99", "Google Chrome";v="99"

        ArrayList<Brand> brands;
        // Do we even have a cache?
        if (cache == null) {
            brands = BrandListParser.parse(input);
        } else {
            brands = cache.computeIfAbsent(input, value -> BrandListParser.parse(input));
        }

        if (!brands.isEmpty()) {
            clientHints.setBrands(brands);
        }

        return clientHints;
    }

    @Nonnull
    @Override
    public String inputField() {
        return HEADER_FIELD;
    }
}
