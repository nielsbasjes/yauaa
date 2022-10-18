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

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzerDirect.HeaderSpecification;
import nl.basjes.parse.useragent.clienthints.ClientHints;

import javax.annotation.Nonnull;
import java.util.Map;

public class ParseSecChUaWoW64 implements CHParser {

    public static final String HEADER_FIELD       = "Sec-CH-UA-WoW64";
    public static final String HEADER_SPEC_URL    = "https://wicg.github.io/ua-client-hints/#sec-ch-ua-wow64";
    public static final String HEADER_SPEC        = "The Sec-CH-UA-WoW64 request header field gives a server information about whether or not a user agent binary is running in 32-bit mode on 64-bit Windows.";
    public static final String FIELD_NAME         = "secChUaWoW64";

    //   From https://wicg.github.io/ua-client-hints/#http-ua-hints
    //
    //   3.10. The 'Sec-CH-UA-WoW64' Header Field
    //   The Sec-CH-UA-WoW64 request header field gives a server information about whether or not a user agent
    //   binary is running in 32-bit mode on 64-bit Windows.
    //   It is a Structured Header whose value MUST be a boolean [RFC8941].
    //
    //   The header’s ABNF is:
    //
    //   Sec-CH-UA-WoW64 = sf-boolean

    @Nonnull
    @Override
    public ClientHints parse(@Nonnull Map<String, String> clientHintsHeaders, @Nonnull ClientHints clientHints, @Nonnull String headerName) {
        String input = clientHintsHeaders.get(headerName);
        if (input == null) {
            return clientHints;
        }
        Boolean parsedBoolean = parseBoolean(input);
        if (parsedBoolean != null) {
            clientHints.setWow64(parsedBoolean);
        }
        return clientHints;
    }

    @Nonnull
    @Override
    public String inputField() {
        return HEADER_FIELD;
    }

    public static HeaderSpecification getHeaderSpecification() {
        return new HeaderSpecification(HEADER_FIELD, HEADER_SPEC_URL, HEADER_SPEC, FIELD_NAME);
    }

}
