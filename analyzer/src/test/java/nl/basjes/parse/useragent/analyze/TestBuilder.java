/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestBuilder {

    @Test
    public void testLimitedFields() {
        UserAgentAnalyzer userAgentAnalyzer =
            UserAgentAnalyzer
                .newBuilder()
                .withoutCache()
                .withField("DeviceClass")
                .withField("AgentNameVersionMajor")
                .build();

        UserAgent parsedAgent = userAgentAnalyzer.parse("Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36");

        // The requested fields
        Assert.assertEquals("Phone",                    parsedAgent.getValue("DeviceClass"              )); // Phone
        Assert.assertEquals("Chrome 53",                parsedAgent.getValue("AgentNameVersionMajor"    )); // Chrome 53

        // The fields that are internally needed to build the requested fields
        Assert.assertEquals("Chrome",                   parsedAgent.getValue("AgentName"                )); // Chrome
        Assert.assertEquals("53.0.2785.124",            parsedAgent.getValue("AgentVersion"             )); // 53.0.2785.124
        Assert.assertEquals("53",                       parsedAgent.getValue("AgentVersionMajor"        )); // 53
        Assert.assertEquals("Chrome 53.0.2785.124",     parsedAgent.getValue("AgentNameVersion"         )); // Chrome 53.0.2785.124

        // The rest must be at confidence -1 (i.e. no rules fired)
        Assert.assertEquals(-1, parsedAgent.get("DeviceName"                   ).getConfidence()); // Nexus 6
        Assert.assertEquals(-1, parsedAgent.get("DeviceBrand"                  ).getConfidence()); // Google
        Assert.assertEquals(-1, parsedAgent.get("OperatingSystemClass"         ).getConfidence()); // Mobile
        Assert.assertEquals(-1, parsedAgent.get("OperatingSystemName"          ).getConfidence()); // Android
        Assert.assertEquals(-1, parsedAgent.get("OperatingSystemVersion"       ).getConfidence()); // 7.0
        Assert.assertEquals(-1, parsedAgent.get("OperatingSystemNameVersion"   ).getConfidence()); // Android 7.0
//        Assert.assertEquals(-1, parsedAgent.get("OperatingSystemVersionBuild"  ).getConfidence()); // NBD90Z
        Assert.assertEquals(-1, parsedAgent.get("LayoutEngineClass"            ).getConfidence()); // Browser
        Assert.assertEquals(-1, parsedAgent.get("LayoutEngineName"             ).getConfidence()); // Blink
        Assert.assertEquals(-1, parsedAgent.get("LayoutEngineVersion"          ).getConfidence()); // 53.0
        Assert.assertEquals(-1, parsedAgent.get("LayoutEngineVersionMajor"     ).getConfidence()); // 53
        Assert.assertEquals(-1, parsedAgent.get("LayoutEngineNameVersion"      ).getConfidence()); // Blink 53.0
        Assert.assertEquals(-1, parsedAgent.get("LayoutEngineNameVersionMajor" ).getConfidence()); // Blink 53
        Assert.assertEquals(-1, parsedAgent.get("AgentClass"                   ).getConfidence()); // Browser
    }

}
