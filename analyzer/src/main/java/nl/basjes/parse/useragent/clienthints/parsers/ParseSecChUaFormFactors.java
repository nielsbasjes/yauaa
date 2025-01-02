/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2025 Niels Basjes
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
import java.util.ArrayList;
import java.util.Map;

public class ParseSecChUaFormFactors implements CHParser {

    public static final String HEADER_FIELD       = "Sec-CH-UA-Form-Factors";
    public static final String HEADER_SPEC_URL    = "https://wicg.github.io/ua-client-hints/#sec-ch-ua-form-factors";
    public static final String HEADER_SPEC        = "The Sec-CH-UA-Form-Factor request header field gives a server information about the user agent's form-factor.";
    public static final String FIELD_NAME         = "secChUaFormFactors";

    public ParseSecChUaFormFactors() {
        // Nothing to do right now
    }

    //   From https://wicg.github.io/ua-client-hints/#http-ua-hints
    //
    //    3.4. The 'Sec-CH-UA-Form-Factors' Header Field

    //    The Sec-CH-UA-Form-Factors request header field gives a server information about the user agent's form-factors.
    //    It is a Structured Header whose value MUST be a list [RFC8941]. In order to avoid providing additional
    //    fingerprinting entropy, the header’s values MUST be given in lexical order, and values are case-sensitive.
    //
    //    The header SHOULD describe the form-factor of the device using one or more of the following common
    //    form-factor values: "Desktop", "Automotive", "Mobile", "Tablet", "XR", "EInk", or "Watch".
    //    All applicable form-factor values SHOULD be included.
    //
    //    NOTE:
    //    The form-factor of a user-agent describes how the user interacts with the user-agent.
    //    The meanings of the allowed values are:
    //    - "Desktop"       refers to a user-agent running on a personal computer.
    //    - "Automotive"    refers to a user-agent embedded in a vehicle, where the user may be responsible for
    //                      operating the vehicle and unable to attend to small details.
    //    - "Mobile"        refers to small, touch-oriented device typically carried on a user’s person.
    //    - "Tablet"        refers to a touch-oriented device larger than "Mobile" and not typically carried on a user’s person.
    //    - "XR"            refers to immersive devices that augment or replace the environment around the user.
    //    - "EInk"          refers to a device characterized by slow screen updates and limited or no color resolution.
    //    - "Watch"         refers to a mobile device with a tiny screen (typically less than 2 in), carried in such
    //                      a way that the user can glance at it quickly.
    //
    //    A new value should be proposed and added to the specification when there is a new form-factor that users
    //    interact with in a meaningfully different way; a compelling use-case where sites would like to change how they
    //    interact with users on that device; and no reliable way to identify that new form-factor using existing hints.
    //
    //    The header’s ABNF is:
    //
    //    Sec-CH-UA-Form-Factors = sf-list

    @Nonnull
    @Override
    public ClientHints parse(@Nonnull Map<String, String> clientHintsHeaders, @Nonnull ClientHints clientHints, @Nonnull String headerName) {
        String input = clientHintsHeaders.get(headerName);
        if (input == null) {
            return clientHints;
        }
        // "Mobile", "EInk"
        ArrayList<String> formFactors = parseSfList(input);

        if (!formFactors.isEmpty()) {
            clientHints.setFormFactors(formFactors);
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
