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
import nl.basjes.parse.useragent.clienthints.ClientHints.BrandVersion;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class ParseSecChUaFullVersionList implements CHParser {

    public static final String HEADER_FIELD = "Sec-CH-UA-Full-Version-List";

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

    @Override
    public ClientHints parse(Map<String, String> clientHintsHeaders, ClientHints clientHints, String headerName) {
        String input = clientHintsHeaders.get(headerName);
        if (input == null) {
            return clientHints;
        }
        // " Not A;Brand";v="99.0.0.0", "Chromium";v="99.0.4844.51", "Google Chrome";v="99.0.4844.51"
        List<BrandVersion> brandVersions = BrandVersionListParser.parse(input);
        if (!brandVersions.isEmpty()) {
            clientHints.setFullVersionList(brandVersions);
        }
        return clientHints;
    }

    @Nonnull
    @Override
    public String inputField() {
        return HEADER_FIELD;
    }
}
