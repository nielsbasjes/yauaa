///*
// * Yet Another UserAgent Analyzer
// * Copyright (C) 2013-2016 Niels Basjes
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package nl.basjes.parse.useragent.pig;
//
//import org.apache.pig.ExecType;
//import org.apache.pig.PigServer;
//import org.apache.pig.backend.executionengine.ExecException;
//import org.apache.pig.builtin.mock.Storage;
//import org.apache.pig.data.Tuple;
//import org.apache.pig.data.TupleFactory;
//import org.apache.pig.impl.logicalLayer.FrontendException;
//import org.apache.pig.impl.logicalLayer.schema.Schema;
//import org.junit.Test;
//
//import static org.apache.pig.builtin.mock.Storage.resetData;
//import static org.apache.pig.builtin.mock.Storage.tuple;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotEquals;
//import static org.junit.Assert.fail;
//
//public class TestParseUserAgent {
//    @Test
//    public void testParseUserAgentPigUDF() throws Exception {
//        PigServer pigServer = new PigServer(ExecType.LOCAL);
//        Storage.Data storageData = resetData(pigServer);
//
//        storageData.set("agents", "agent:chararray",
//            tuple("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36")
//        );
//
//        pigServer.registerQuery("A = LOAD 'agents' USING mock.Storage();");
//        pigServer.registerQuery("B = FOREACH A GENERATE nl.basjes.parse.useragent.pig.ParseUserAgent(agent);");
//        pigServer.registerQuery("STORE B INTO 'parsedAgents' USING mock.Storage();");
//
//
//        Tuple data     = (Tuple)storageData.get("parsedAgents").get(0).get(0);
//        Schema schema  = storageData.getSchema("parsedAgents").getField(0).schema;
//
//        checkResult(data, schema, "DeviceClass",                      "Desktop"               );
//        checkResult(data, schema, "DeviceName",                       "Linux Desktop"         );
//        checkResult(data, schema, "OperatingSystemClass",             "Desktop"               );
//        checkResult(data, schema, "OperatingSystemName",              "Linux"                 );
//        checkResult(data, schema, "OperatingSystemVersion",           "Intel x86_64"          );
//        checkResult(data, schema, "LayoutEngineClass",                "Browser"               );
//        checkResult(data, schema, "LayoutEngineName",                 "Blink"                 );
//        checkResult(data, schema, "LayoutEngineVersion",              "48.0"                  );
//        checkResult(data, schema, "AgentClass",                       "Browser"               );
//        checkResult(data, schema, "AgentName",                        "Chrome"                );
//        checkResult(data, schema, "AgentVersion",                     "48.0.2564.82"          );
//        checkResult(data, schema, "AgentVersionMajor",                "48"                    );
//        checkResult(data, schema, "AgentNameVersion",                 "Chrome 48.0.2564.82"   );
//        checkResult(data, schema, "AgentBuild",                       null                    );
//        checkResult(data, schema, "AgentInformationEmail",            null                    );
//        checkResult(data, schema, "AgentInformationUrl",              null                    );
//        checkResult(data, schema, "AgentLanguage",                    null                    );
//        checkResult(data, schema, "AgentSecurity",                    null                    );
//        checkResult(data, schema, "AgentUuid",                        null                    );
//        checkResult(data, schema, "Anonymized",                       null                    );
//        checkResult(data, schema, "DeviceBrand",                      "Unknown"               );
//        checkResult(data, schema, "DeviceCpu",                        "Intel x86_64"          );
//        checkResult(data, schema, "DeviceFirmwareVersion",            null                    );
//        checkResult(data, schema, "DeviceVersion",                    null                    );
//        checkResult(data, schema, "FacebookCarrier",                  null                    );
//        checkResult(data, schema, "FacebookDeviceClass",              null                    );
//        checkResult(data, schema, "FacebookDeviceName",               null                    );
//        checkResult(data, schema, "FacebookDeviceVersion",            null                    );
//        checkResult(data, schema, "FacebookFBOP",                     null                    );
//        checkResult(data, schema, "FacebookFBSS",                     null                    );
//        checkResult(data, schema, "FacebookOperatingSystemName",      null                    );
//        checkResult(data, schema, "FacebookOperatingSystemVersion",   null                    );
//        checkResult(data, schema, "HackerAttackVector",               null                    );
//        checkResult(data, schema, "HackerToolkit",                    null                    );
//        checkResult(data, schema, "KoboAffiliate",                    null                    );
//        checkResult(data, schema, "KoboPlatformId",                   null                    );
//        checkResult(data, schema, "LayoutEngineBuild",                null                    );
//        checkResult(data, schema, "OperatingSystemVersionBuild",      null                    );
//    }
//
//    private void checkResult(Tuple data, Schema schema, String fieldName, String value) throws FrontendException, ExecException {
//
//        assertNotEquals("Field named "+fieldName +" is missing in the schema",schema.getField(fieldName),-1);
//
//        int position = schema.getPosition(fieldName);
//        if (position == -1 && value != null) {
//            fail("Field named "+fieldName +" is missing");
//        }
//
//        assertEquals("Field named "+fieldName +" should be \""+value+"\".", value, data.get(position));
//    }
//
//
//    @Test
//    public void testParseUserAgentPigUDF_NULL() throws Exception {
//        TupleFactory tupleFactory = TupleFactory.getInstance();
//        Tuple nullInput = tupleFactory.newTuple();
//        nullInput.append(null);
//
//        ParseUserAgent udf = new ParseUserAgent();
//        Tuple data = udf.exec(nullInput);
//        Schema schema = udf.outputSchema(null).getField(0).schema;
//
//        System.out.println(schema.toString());
//
//        checkResult(data, schema, "DeviceClass",                    "Hacker"  );
//        checkResult(data, schema, "DeviceName",                     "Hacker"  );
//        checkResult(data, schema, "OperatingSystemClass",           "Hacker"  );
//        checkResult(data, schema, "OperatingSystemName",            "Hacker"  );
//        checkResult(data, schema, "OperatingSystemVersion",         "Hacker"  );
//        checkResult(data, schema, "LayoutEngineClass",              "Hacker"  );
//        checkResult(data, schema, "LayoutEngineName",               "Hacker"  );
//        checkResult(data, schema, "LayoutEngineVersion",            "Hacker"  );
//        checkResult(data, schema, "AgentClass",                     "Hacker"  );
//        checkResult(data, schema, "AgentName",                      "Hacker"  );
//        checkResult(data, schema, "AgentVersion",                   "Hacker"  );
//        checkResult(data, schema, "AgentBuild",                     null       );
//        checkResult(data, schema, "AgentInformationEmail",          null       );
//        checkResult(data, schema, "AgentInformationUrl",            null       );
//        checkResult(data, schema, "AgentLanguage",                  null       );
//        checkResult(data, schema, "AgentSecurity",                  null       );
//        checkResult(data, schema, "AgentUuid",                      null       );
//        checkResult(data, schema, "Anonymized",                     null       );
//        checkResult(data, schema, "DeviceBrand",                    "Hacker"   );
//        checkResult(data, schema, "DeviceCpu",                      null       );
//        checkResult(data, schema, "DeviceFirmwareVersion",          null       );
//        checkResult(data, schema, "DeviceVersion",                  "Hacker"   );
//        checkResult(data, schema, "FacebookCarrier",                null       );
//        checkResult(data, schema, "FacebookDeviceClass",            null       );
//        checkResult(data, schema, "FacebookDeviceName",             null       );
//        checkResult(data, schema, "FacebookDeviceVersion",          null       );
//        checkResult(data, schema, "FacebookFBOP",                   null       );
//        checkResult(data, schema, "FacebookFBSS",                   null       );
//        checkResult(data, schema, "FacebookOperatingSystemName",    null       );
//        checkResult(data, schema, "FacebookOperatingSystemVersion", null       );
//        checkResult(data, schema, "HackerAttackVector",             "Unknown"  );
//        checkResult(data, schema, "HackerToolkit",                  "Unknown"  );
//        checkResult(data, schema, "KoboAffiliate",                  null       );
//        checkResult(data, schema, "KoboPlatformId",                 null       );
//        checkResult(data, schema, "LayoutEngineBuild",              null       );
//        checkResult(data, schema, "OperatingSystemVersionBuild",    null       );
//    }
//
//
//}
