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

package nl.basjes.parse.useragent.servlet.graphql;

import org.eclipse.microprofile.graphql.Description;

import java.util.Map;

@Description("The analysis results of a User-Agent and other headers.")
public class ParsedAgent {
    @Description("The full set of all fields in a map")
    public Map<String, String> fields;

    public String deviceClass                         ;
    public String deviceName                          ;
    public String deviceBrand                         ;
    public String deviceCpu                           ;
    public String deviceCpuBits                       ;
    public String deviceFirmwareVersion               ;
    public String deviceVersion                       ;
    public String operatingSystemClass                ;
    public String operatingSystemName                 ;
    public String operatingSystemVersion              ;
    public String operatingSystemVersionMajor         ;
    public String operatingSystemNameVersion          ;
    public String operatingSystemNameVersionMajor     ;
    public String layoutEngineClass                   ;
    public String layoutEngineName                    ;
    public String layoutEngineVersion                 ;
    public String layoutEngineVersionMajor            ;
    public String layoutEngineNameVersion             ;
    public String layoutEngineNameVersionMajor        ;
    public String agentClass                          ;
    public String agentName                           ;
    public String agentVersion                        ;
    public String agentVersionMajor                   ;
    public String agentNameVersion                    ;
    public String agentNameVersionMajor               ;

    @Description("The provided input can be requested back (usefull in batch analysis scenarios)")
    public RequestHeaders requestHeaders;
}
