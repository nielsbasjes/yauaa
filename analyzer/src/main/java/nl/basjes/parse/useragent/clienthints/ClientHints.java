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

package nl.basjes.parse.useragent.clienthints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;

// In all cases: null means not specified
@Getter
@Setter
@ToString
public class ClientHints implements Serializable {
    // See: https://wicg.github.io/ua-client-hints/#interface
    private ArrayList<Brand> brands = null;                 // 3.1 https://wicg.github.io/ua-client-hints/#sec-ch-ua
    private String architecture = null;                     // 3.2 https://wicg.github.io/ua-client-hints/#sec-ch-ua-arch
    private String bitness = null;                          // 3.3 https://wicg.github.io/ua-client-hints/#sec-ch-ua-bitness
    private ArrayList<String> formFactors = null;           // 3.4 https://wicg.github.io/ua-client-hints/#sec-ch-ua-form-factors
    private String fullVersion = null; /* deprecated */     // 3.4 https://wicg.github.io/ua-client-hints/#sec-ch-ua-full-version
    private ArrayList<Brand> fullVersionList = null;        // 3.5 https://wicg.github.io/ua-client-hints/#sec-ch-ua-full-version-list
    private Boolean mobile = null;                          // 3.6 https://wicg.github.io/ua-client-hints/#sec-ch-ua-mobile
    private String model = null;                            // 3.7 https://wicg.github.io/ua-client-hints/#sec-ch-ua-model
    private String platform = null;                         // 3.8 https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform
    private String platformVersion = null;                  // 3.9 https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform-version
    private Boolean wow64 = null;                           // 3.10 https://wicg.github.io/ua-client-hints/#sec-ch-ua-wow64

    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class Brand implements Serializable {
        private String name;
        private String version;
    }

}

