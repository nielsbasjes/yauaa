/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

package nl.basjes.parse.useragent.pig;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.builtin.mock.Storage;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.junit.Test;

import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.apache.pig.builtin.mock.Storage.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

// CHECKSTYLE.OFF: ParenPad
public class TestParseUserAgent {

    private final String testUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36";
    @Test
    public void testParseUserAgentPigUDF_allFields() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data storageData = resetData(pigServer);

        storageData.set("agents", "agent:chararray", tuple(testUserAgent));

        pigServer.registerQuery("define ParseUserAgent nl.basjes.parse.useragent.pig.ParseUserAgent();");
        pigServer.registerQuery("A = LOAD 'agents' USING mock.Storage();");
        pigServer.registerQuery("B = FOREACH A GENERATE ParseUserAgent(agent);");
        pigServer.registerQuery("STORE B INTO 'parsedAgents' USING mock.Storage();");
        verifyStorageData(storageData);
    }

    @Test
    public void testParseUserAgentPigUDF_Cache_allFields() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data storageData = resetData(pigServer);

        storageData.set("agents", "agent:chararray", tuple(testUserAgent));

        pigServer.registerQuery("define ParseUserAgent nl.basjes.parse.useragent.pig.ParseUserAgent('5');");
        pigServer.registerQuery("A = LOAD 'agents' USING mock.Storage();");
        pigServer.registerQuery("B = FOREACH A GENERATE ParseUserAgent(agent);");
        pigServer.registerQuery("STORE B INTO 'parsedAgents' USING mock.Storage();");
        verifyStorageData(storageData);
    }

    private void verifyStorageData(Storage.Data storageData) throws FrontendException, ExecException {

        Tuple data     = (Tuple)storageData.get("parsedAgents").get(0).get(0);
        Schema schema  = storageData.getSchema("parsedAgents").getField(0).schema;

        checkResult(data, schema, "DeviceClass",                      "Desktop"               );
        checkResult(data, schema, "DeviceName",                       "Linux Desktop"         );
        checkResult(data, schema, "OperatingSystemClass",             "Desktop"               );
        checkResult(data, schema, "OperatingSystemName",              "Linux"                 );
        checkResult(data, schema, "OperatingSystemVersion",           "Intel x86_64"          );
        checkResult(data, schema, "LayoutEngineClass",                "Browser"               );
        checkResult(data, schema, "LayoutEngineName",                 "Blink"                 );
        checkResult(data, schema, "LayoutEngineVersion",              "48.0"                  );
        checkResult(data, schema, "AgentClass",                       "Browser"               );
        checkResult(data, schema, "AgentName",                        "Chrome"                );
        checkResult(data, schema, "AgentVersion",                     "48.0.2564.82"          );
        checkResult(data, schema, "AgentVersionMajor",                "48"                    );
        checkResult(data, schema, "AgentNameVersion",                 "Chrome 48.0.2564.82"   );
        checkResult(data, schema, "AgentNameVersionMajor",            "Chrome 48"             );
        checkResult(data, schema, "AgentBuild",                       "Unknown"               );
        checkResult(data, schema, "AgentInformationEmail",            "Unknown"               );
        checkResult(data, schema, "AgentInformationUrl",              "Unknown"               );
        checkResult(data, schema, "AgentLanguage",                    "Unknown"               );
        checkResult(data, schema, "AgentSecurity",                    "Unknown"               );
        checkResult(data, schema, "AgentUuid",                        "Unknown"               );
        checkResult(data, schema, "Anonymized",                       "Unknown"               );
        checkResult(data, schema, "DeviceBrand",                      "Unknown"               );
        checkResult(data, schema, "DeviceCpu",                        "Intel x86_64"          );
        checkResult(data, schema, "DeviceFirmwareVersion",            "Unknown"               );
        checkResult(data, schema, "DeviceVersion",                    "Unknown"               );
        checkResult(data, schema, "FacebookCarrier",                  "Unknown"               );
        checkResult(data, schema, "FacebookDeviceClass",              "Unknown"               );
        checkResult(data, schema, "FacebookDeviceName",               "Unknown"               );
        checkResult(data, schema, "FacebookDeviceVersion",            "Unknown"               );
        checkResult(data, schema, "FacebookFBOP",                     "Unknown"               );
        checkResult(data, schema, "FacebookFBSS",                     "Unknown"               );
        checkResult(data, schema, "FacebookOperatingSystemName",      "Unknown"               );
        checkResult(data, schema, "FacebookOperatingSystemVersion",   "Unknown"               );
        checkResult(data, schema, "HackerAttackVector",               "Unknown"               );
        checkResult(data, schema, "HackerToolkit",                    "Unknown"               );
        checkResult(data, schema, "KoboAffiliate",                    "Unknown"               );
        checkResult(data, schema, "KoboPlatformId",                   "Unknown"               );
        checkResult(data, schema, "LayoutEngineBuild",                "Unknown"               );
        checkResult(data, schema, "OperatingSystemVersionBuild",      "Unknown"               );
    }

    @Test
    public void testParseUserAgentPigUDF_Limited_Fields() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data storageData = resetData(pigServer);

        storageData.set("agents", "agent:chararray", tuple(testUserAgent));

        pigServer.registerQuery("define ParseUserAgent nl.basjes.parse.useragent.pig.ParseUserAgent('5','DeviceClass','AgentNameVersionMajor');");
        pigServer.registerQuery("A = LOAD 'agents' USING mock.Storage();");
        pigServer.registerQuery("B = FOREACH A GENERATE ParseUserAgent(agent);");
        pigServer.registerQuery("STORE B INTO 'parsedAgents' USING mock.Storage();");

        Tuple data     = (Tuple)storageData.get("parsedAgents").get(0).get(0);
        Schema schema  = storageData.getSchema("parsedAgents").getField(0).schema;

        // The ones we requested
        checkResult(data, schema, "DeviceClass",                      "Desktop"               );
        checkResult(data, schema, "AgentNameVersionMajor",            "Chrome 48"             );

        // These are used to build the requested fields. But are NOT output
        checkAbsent(schema, "AgentName"                      );
        checkAbsent(schema, "AgentVersion"                   );
        checkAbsent(schema, "AgentVersionMajor"              );
        checkAbsent(schema, "AgentNameVersion"               );

        // The rest is 'Unknown'
        checkAbsent(schema, "DeviceName"                     );
        checkAbsent(schema, "OperatingSystemClass"           );
        checkAbsent(schema, "OperatingSystemName"            );
        checkAbsent(schema, "OperatingSystemVersion"         );
        checkAbsent(schema, "LayoutEngineClass"              );
        checkAbsent(schema, "LayoutEngineName"               );
        checkAbsent(schema, "LayoutEngineVersion"            );
        checkAbsent(schema, "AgentClass"                     );
        checkAbsent(schema, "AgentBuild"                     );
        checkAbsent(schema, "AgentInformationEmail"          );
        checkAbsent(schema, "AgentInformationUrl"            );
        checkAbsent(schema, "AgentLanguage"                  );
        checkAbsent(schema, "AgentSecurity"                  );
        checkAbsent(schema, "AgentUuid"                      );
        checkAbsent(schema, "Anonymized"                     );
        checkAbsent(schema, "DeviceBrand"                    );
        checkAbsent(schema, "DeviceCpu"                      );
        checkAbsent(schema, "DeviceFirmwareVersion"          );
        checkAbsent(schema, "DeviceVersion"                  );
        checkAbsent(schema, "FacebookCarrier"                );
        checkAbsent(schema, "FacebookDeviceClass"            );
        checkAbsent(schema, "FacebookDeviceName"             );
        checkAbsent(schema, "FacebookDeviceVersion"          );
        checkAbsent(schema, "FacebookFBOP"                   );
        checkAbsent(schema, "FacebookFBSS"                   );
        checkAbsent(schema, "FacebookOperatingSystemName"    );
        checkAbsent(schema, "FacebookOperatingSystemVersion" );
        checkAbsent(schema, "HackerAttackVector"             );
        checkAbsent(schema, "HackerToolkit"                  );
        checkAbsent(schema, "KoboAffiliate"                  );
        checkAbsent(schema, "KoboPlatformId"                 );
        checkAbsent(schema, "LayoutEngineBuild"              );
        checkAbsent(schema, "OperatingSystemVersionBuild"    );
    }

    private void checkResult(Tuple data, Schema schema, String fieldName, String value) throws FrontendException, ExecException {

        assertNotEquals("Field named "+fieldName +" is missing in the schema", schema.getField(fieldName), null);

        int position = schema.getPosition(fieldName);
        if (position == -1 && value != null) {
            fail("Field named "+fieldName +" is missing");
        }

        assertEquals("Field named "+fieldName +" should be \""+value+"\".", value, data.get(position));
    }

    private void checkAbsent(Schema schema, String fieldName) throws FrontendException {
        assertNull("Field named " + fieldName + " is present in the schema", schema.getField(fieldName));
    }

    @Test
    public void testParseUserAgentPigUDF_NULL() throws Exception {
        TupleFactory tupleFactory = TupleFactory.getInstance();
        Tuple nullInput = tupleFactory.newTuple();
        nullInput.append(null);

        ParseUserAgent udf = new ParseUserAgent();
        Tuple data = udf.exec(nullInput);
        Schema schema = udf.outputSchema(null).getField(0).schema;

        System.out.println(schema.toString());

        checkResult(data, schema, "DeviceClass",                    "Hacker"  );
        checkResult(data, schema, "DeviceName",                     "Hacker"  );
        checkResult(data, schema, "OperatingSystemClass",           "Hacker"  );
        checkResult(data, schema, "OperatingSystemName",            "Hacker"  );
        checkResult(data, schema, "OperatingSystemVersion",         "Hacker"  );
        checkResult(data, schema, "LayoutEngineClass",              "Hacker"  );
        checkResult(data, schema, "LayoutEngineName",               "Hacker"  );
        checkResult(data, schema, "LayoutEngineVersion",            "Hacker"  );
        checkResult(data, schema, "AgentClass",                     "Hacker"  );
        checkResult(data, schema, "AgentName",                      "Hacker"  );
        checkResult(data, schema, "AgentVersion",                   "Hacker"  );
        checkResult(data, schema, "HackerAttackVector",             "Unknown"  );
        checkResult(data, schema, "HackerToolkit",                  "Unknown"  );
    }


}
