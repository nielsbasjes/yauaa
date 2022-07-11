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

package nl.basjes.parse.useragent.servlet.api;

import nl.basjes.parse.useragent.UserAgent;

import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isDeliberateMisuse;
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isHuman;
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isMobile;
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isNormalConsumerDevice;

public final class DetermineUserAgentTags {

    private DetermineUserAgentTags() {
        // Utility class
    }

    public static List<String> getTags(UserAgent userAgent) {
        List<String> tags = new ArrayList<>();
        if (isHuman(userAgent)) {
            tags.add("Human");
        } else {
            tags.add("NOT Human");
        }
        if (isNormalConsumerDevice(userAgent)) {
            tags.add("Normal consumer device");
        }
        if (isMobile(userAgent)) {
            tags.add("Mobile");
        }
        if (isDeliberateMisuse(userAgent)) {
            tags.add("Deliberate Misuse");
        }

        return tags;
    }

}
