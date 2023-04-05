/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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

package nl.example;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import java.io.Serializable;

public class Demo implements Serializable {

    private final UserAgentAnalyzer uaa;

    public Demo() {
        uaa = UserAgentAnalyzer
            .newBuilder()
            .withCache(1234)
            .withField("DeviceName")
            .build();
    }

    public UserAgent parse(String userAgent) {
        return uaa.parse(userAgent);
    }

    @Override
    public String toString() {
        uaa.reset(); // To avoid mismatching on the old Matches.
        return "Demo{" +
            "uaa=" + uaa +
            '}';
    }
}
