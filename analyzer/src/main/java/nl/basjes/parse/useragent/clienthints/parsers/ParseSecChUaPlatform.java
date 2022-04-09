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

import javax.annotation.Nonnull;
import java.util.Map;

public class ParseSecChUaPlatform implements CHParser {

    public static final String HEADER_FIELD = "Sec-CH-UA-Platform";

    //   From https://wicg.github.io/ua-client-hints/#http-ua-hints
    //
    //   3.8. The 'Sec-CH-UA-Platform' Header Field
    //   The Sec-CH-UA-Platform request header field gives a server information about the platform on which a
    //   given user agent is executing. It is a Structured Header whose value MUST be a string [RFC8941].
    //   Its value SHOULD match one of the following common platform values:
    //      "Android", "Chrome OS", "iOS", "Linux", "macOS", "Windows", or "Unknown".
    //
    //   The header’s ABNF is:
    //
    //   Sec-CH-UA-Platform = sf-string
    //   Note: Like Sec-CH-UA above, since it’s included in the low entropy hint table, the Sec-CH-UA-Platform header
    //   will be sent by default, whether or not the server opted-into receiving the header via an Accept-CH header
    //   (although it can still be controlled by its policy controlled client hints feature).

    @Nonnull
    @Override
    public ClientHints parse(@Nonnull Map<String, String> clientHintsHeaders, @Nonnull ClientHints clientHints, @Nonnull String headerName) {
        String input = clientHintsHeaders.get(headerName);
        String value = parseSfString(input);
        if (value != null && !value.isEmpty()) {
            clientHints.setPlatform(value);
        }
        return clientHints;
    }

    @Nonnull
    @Override
    public String inputField() {
        return HEADER_FIELD;
    }
}
