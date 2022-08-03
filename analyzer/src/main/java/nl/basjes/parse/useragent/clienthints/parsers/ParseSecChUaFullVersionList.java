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

import nl.basjes.parse.useragent.clienthints.ClientHints;
import nl.basjes.parse.useragent.clienthints.ClientHints.Brand;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;

public class ParseSecChUaFullVersionList implements CHParser {

    public static final String HEADER_FIELD       = "Sec-CH-UA-Full-Version-List";
    public static final String HEADER_SPEC_URL    = "https://wicg.github.io/ua-client-hints/#sec-ch-ua-full-version-list";
    public static final String HEADER_SPEC        = "The Sec-CH-UA-Full-Version-List request header field gives a server information about the full version for each brand in its brands list.";

    //   From https://wicg.github.io/ua-client-hints/#http-ua-hints
    //
    //   3.5. The 'Sec-CH-UA-Full-Version-List' Header Field
    //   The Sec-CH-UA-Full-Version-List request header field gives a server information about the full version for
    //   each brand in its brands list. It is a Structured Header whose value MUST be a list [RFC8941].
    //
    //   The header’s ABNF is:
    //
    //   Sec-CH-UA-Full-Version-List = sf-list
    //   To return the Sec-CH-UA-Full-Version-List value for a request, perform the following steps:
    //   - Let brands be the result of running create brands with "full version".
    //   - Let list be the result of create a brand-version list, with brands and "full version".
    //   - Return the output of running serializing a list with list as input.
    //

    @Nonnull
    @Override
    public ClientHints parse(@Nonnull Map<String, String> clientHintsHeaders, @Nonnull ClientHints clientHints, @Nonnull String headerName) {
        String input = clientHintsHeaders.get(headerName);
        if (input == null) {
            return clientHints;
        }
        // " Not A;Brand";v="99.0.0.0", "Chromium";v="99.0.4844.51", "Google Chrome";v="99.0.4844.51"
        ArrayList<Brand> brands = BrandListParser.parse(input);
        if (!brands.isEmpty()) {
            clientHints.setFullVersionList(brands);
        }
        return clientHints;
    }

    @Nonnull
    @Override
    public String inputField() {
        return HEADER_FIELD;
    }
}
