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

package nl.basjes.parse.useragent.clienthints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;

// In call cases: null means not specified
public class ClientHints implements Serializable {
    // See: https://wicg.github.io/ua-client-hints/#interface
    @Getter @Setter private ArrayList<BrandVersion> brands = null;
    @Getter @Setter private Boolean mobile = null;
    @Getter @Setter private String architecture = null;
    @Getter @Setter private String bitness = null;
    @Getter @Setter private String model = null;
    @Getter @Setter private String platform = null;
    @Getter @Setter private String platformVersion = null;
    @Getter @Setter private Boolean wow64 = null;
    @Getter @Setter private ArrayList<BrandVersion> fullVersionList = null;

    @AllArgsConstructor
    public static class BrandVersion implements Serializable {
        @Getter @Setter private String brand;
        @Getter @Setter private String version;
    }

}

