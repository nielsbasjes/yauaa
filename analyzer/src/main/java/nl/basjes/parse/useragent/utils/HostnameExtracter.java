/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

package nl.basjes.parse.useragent.utils;

import java.io.Serializable;
import java.net.URI;

public final class HostnameExtracter implements Serializable {

    private HostnameExtracter() {
        // Nothing
    }

    public static String extractHostname(String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return null; // Nothing to do here
        }

        int firstQuestionMark = uriString.indexOf('?');
        int firstAmpersand = uriString.indexOf('&');
        int cutIndex = -1;
        if (firstAmpersand != -1) {
            if (firstQuestionMark != -1) {
                cutIndex = firstQuestionMark;
            } else {
                cutIndex = firstAmpersand;
            }
        } else {
            if (firstQuestionMark != -1) {
                cutIndex = firstQuestionMark;
            }
        }
        if (cutIndex != -1) {
            uriString = uriString.substring(0, cutIndex);
        }

        URI     uri;
        try {
            if (uriString.charAt(0) == '/') {
                if (uriString.charAt(1) == '/') {
                    uri = URI.create(uriString);
                } else {
                    // So no hostname
                    return null;
                }
            } else {
                if (uriString.contains(":")) {
                    uri = URI.create(uriString);
                } else {
                    if (uriString.contains("/")) {
                        return uriString.split("/", 2)[0];
                    } else {
                        return uriString;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            return null;
        }

        return uri.getHost();
    }

}
