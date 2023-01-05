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

package nl.basjes.parse.useragent.clienthints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;

// In call cases: null means not specified
@ToString
public class ClientHints implements Serializable {
    // See: https://wicg.github.io/ua-client-hints/#interface
    @Getter @Setter private ArrayList<Brand> brands = null;                 // 3.1 https://wicg.github.io/ua-client-hints/#sec-ch-ua
    @Getter @Setter private String architecture = null;                     // 3.2 https://wicg.github.io/ua-client-hints/#sec-ch-ua-arch
    @Getter @Setter private String bitness = null;                          // 3.3 https://wicg.github.io/ua-client-hints/#sec-ch-ua-bitness
    @Getter @Setter private String fullVersion = null; /* deprecated */     // 3.4 https://wicg.github.io/ua-client-hints/#sec-ch-ua-full-version
    @Getter @Setter private ArrayList<Brand> fullVersionList = null;        // 3.5 https://wicg.github.io/ua-client-hints/#sec-ch-ua-full-version-list
    @Getter @Setter private Boolean mobile = null;                          // 3.6 https://wicg.github.io/ua-client-hints/#sec-ch-ua-mobile
    @Getter @Setter private String model = null;                            // 3.7 https://wicg.github.io/ua-client-hints/#sec-ch-ua-model
    @Getter @Setter private String platform = null;                         // 3.8 https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform
    @Getter @Setter private String platformVersion = null;                  // 3.9 https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform-version
    @Getter @Setter private Boolean wow64 = null;                           // 3.10 https://wicg.github.io/ua-client-hints/#sec-ch-ua-wow64

    @AllArgsConstructor
    @ToString
    public static class Brand implements Serializable {
        @Getter @Setter private String name;
        @Getter @Setter private String version;
    }

}

