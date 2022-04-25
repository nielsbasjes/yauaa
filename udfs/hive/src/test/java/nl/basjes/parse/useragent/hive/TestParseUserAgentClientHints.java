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

package nl.basjes.parse.useragent.hive;

import org.apache.hadoop.hive.common.StringInternUtils;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredJavaObject;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StandardStructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestParseUserAgentClientHints {

    @BeforeAll
    static void init() {
        try {
            // Trigger the static initializer of this utility class ... that fails under JDK 17
            StringInternUtils.internIfNotNull(null);
        } catch (Exception ioe) {
            //Ignore
        }
    }

    @Test
    void testBasic() throws HiveException {

        String useragent            = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36";
        String chPlatform           = "\"macOS\"";
        String chPlatformVersion    = "\"12.3.1\"";

        ParseUserAgent parseUserAgent = new ParseUserAgent();

        StandardStructObjectInspector resultInspector = (StandardStructObjectInspector) parseUserAgent
            .initialize(new ObjectInspector[]{
                PrimitiveObjectInspectorFactory.javaStringObjectInspector,
                PrimitiveObjectInspectorFactory.javaStringObjectInspector,
                PrimitiveObjectInspectorFactory.javaStringObjectInspector,
                PrimitiveObjectInspectorFactory.javaStringObjectInspector,
                PrimitiveObjectInspectorFactory.javaStringObjectInspector,
                PrimitiveObjectInspectorFactory.javaStringObjectInspector
            });

        for (int i = 0; i < 100000; i++) {
            Object row = parseUserAgent.evaluate(
                new DeferredObject[]{
                    new DeferredJavaObject("user-Agent"),                   new DeferredJavaObject(useragent),
                    new DeferredJavaObject("sec-CH-UA-Platform"),           new DeferredJavaObject(chPlatform),
                    new DeferredJavaObject("sec-CH-UA-Platform-Version"),   new DeferredJavaObject(chPlatformVersion),
                });
            checkField(resultInspector, row, "DeviceClass",                 "Desktop");
            checkField(resultInspector, row, "AgentNameVersionMajor",       "Chrome 100");
            checkField(resultInspector, row, "OperatingSystemNameVersion",  "Mac OS 12.3.1");
        }
    }

    private void checkField(StandardStructObjectInspector resultInspector, Object row, String fieldName, String expectedValue) {
        final Object result = resultInspector.getStructFieldData(row, resultInspector.getStructFieldRef(fieldName));

        if (result == null) {
            assertEquals(expectedValue, result);
        } else {
            assertEquals(expectedValue, result.toString());
        }
    }

    @Test
    void testExplain() {
        ParseUserAgent parseUserAgent = new ParseUserAgent();
        assertTrue(parseUserAgent.getDisplayString(null).contains("UserAgent"), "Wrong explanation");
    }

}
