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

public class ParseSecChUaPlatformVersion implements CHParser {

    public static final String HEADER_FIELD = "Sec-CH-UA-Platform-Version";

    //   From https://wicg.github.io/ua-client-hints/#http-ua-hints
    //
    //   3.9. The 'Sec-CH-UA-Platform-Version' Header Field
    //   The Sec-CH-UA-Platform-Version request header field gives a server information about the platform version
    //   on which a given user agent is executing. It is a Structured Header whose value MUST be a string [RFC8941].
    //
    //   ...
    //
    //   The header’s ABNF is:
    //
    //   Sec-CH-UA-Platform-Version = sf-string

    @Nonnull
    @Override
    public ClientHints parse(@Nonnull Map<String, String> clientHintsHeaders, @Nonnull ClientHints clientHints, @Nonnull String headerName) {
        String input = clientHintsHeaders.get(headerName);
        String value = parseSfString(input);
        if (value != null && !value.isEmpty()) {
            clientHints.setPlatformVersion(value);
        }
        return clientHints;
    }

    @Nonnull
    @Override
    public String inputField() {
        return HEADER_FIELD;
    }
}
