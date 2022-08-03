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

import lombok.Getter;
import lombok.Setter;
import nl.basjes.parse.useragent.UserAgent;

import java.util.Map;
import java.util.TreeMap;

//@Description("The analysis results of a User-Agent and other headers.")
public class AnalysisResult {

    private UserAgent userAgent;

    public AnalysisResult(UserAgent userAgent) {
        this.userAgent = userAgent;
    }

    public UserAgent getUserAgent() {
        return userAgent;
    }

    private Map<String, FieldResult> fieldsMap = null;
    public synchronized Map<String, FieldResult> getFields(){
        if (fieldsMap == null) {
            fieldsMap = new TreeMap<>();

            for (String fieldName : userAgent.getAvailableFieldNamesSorted()) {
                fieldsMap.put(fieldName, new FieldResult(fieldName, userAgent.getValue(fieldName)));
            }
        }
        return fieldsMap;
    }

    public String getDeviceClass()                     {
        return userAgent.getValue("DeviceClass");
    }
    public String getDeviceName()                      {
        return userAgent.getValue("DeviceName");
    }
    public String getDeviceBrand()                     {
        return userAgent.getValue("DeviceBrand");
    }
    public String getDeviceCpu()                       {
        return userAgent.getValue("DeviceCpu");
    }
    public String getDeviceCpuBits()                   {
        return userAgent.getValue("DeviceCpuBits");
    }
    public String getDeviceFirmwareVersion()           {
        return userAgent.getValue("DeviceFirmwareVersion");
    }
    public String getDeviceVersion()                   {
        return userAgent.getValue("DeviceVersion");
    }
    public String getOperatingSystemClass()            {
        return userAgent.getValue("OperatingSystemClass");
    }
    public String getOperatingSystemName()             {
        return userAgent.getValue("OperatingSystemName");
    }
    public String getOperatingSystemVersion()          {
        return userAgent.getValue("OperatingSystemVersion");
    }
    public String getOperatingSystemVersionMajor()     {
        return userAgent.getValue("OperatingSystemVersionMajor");
    }
    public String getOperatingSystemNameVersion()      {
        return userAgent.getValue("OperatingSystemNameVersion");
    }
    public String getOperatingSystemNameVersionMajor() {
        return userAgent.getValue("OperatingSystemNameVersionMajor");
    }
    public String getLayoutEngineClass()               {
        return userAgent.getValue("LayoutEngineClass");
    }
    public String getLayoutEngineName()                {
        return userAgent.getValue("LayoutEngineName");
    }
    public String getLayoutEngineVersion()             {
        return userAgent.getValue("LayoutEngineVersion");
    }
    public String getLayoutEngineVersionMajor()        {
        return userAgent.getValue("LayoutEngineVersionMajor");
    }
    public String getLayoutEngineNameVersion()         {
        return userAgent.getValue("LayoutEngineNameVersion");
    }
    public String getLayoutEngineNameVersionMajor()    {
        return userAgent.getValue("LayoutEngineNameVersionMajor");
    }
    public String getAgentClass()                      {
        return userAgent.getValue("AgentClass");
    }
    public String getAgentName()                       {
        return userAgent.getValue("AgentName");
    }
    public String getAgentVersion()                    {
        return userAgent.getValue("AgentVersion");
    }
    public String getAgentVersionMajor()               {
        return userAgent.getValue("AgentVersionMajor");
    }
    public String getAgentNameVersion()                {
        return userAgent.getValue("AgentNameVersion");
    }
    public String getAgentNameVersionMajor()           {
        return userAgent.getValue("AgentNameVersionMajor");
    }
    public String getRemarkablePattern()               {
        return userAgent.getValue("RemarkablePattern");
    }

//    @Description("The provided input can be requested back (useful in batch analysis scenarios)")
    @Getter @Setter private RequestHeaders requestHeaders;

}
