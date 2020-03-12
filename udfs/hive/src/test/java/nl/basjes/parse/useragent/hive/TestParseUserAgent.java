/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredJavaObject;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StandardStructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestParseUserAgent {

    @Test
    public void testBasic() throws HiveException {
        // This is an edge case where the webview fields are calulcated AND wiped again.
        String userAgent = "Mozilla/5.0 (Linux; Android 5.1.1; KFFOWI Build/LMY47O) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Version/4.0 Chrome/41.51020.2250.0246 Mobile Safari/537.36 cordova-amazon-fireos/3.4.0 AmazonWebAppPlatform/3.4.0;2.0";

        ParseUserAgent parseUserAgent = new ParseUserAgent();

        StandardStructObjectInspector resultInspector = (StandardStructObjectInspector) parseUserAgent
            .initialize(new ObjectInspector[]{
                PrimitiveObjectInspectorFactory.javaStringObjectInspector
            });

        for (int i = 0; i < 100000; i++) {
            Object row = parseUserAgent.evaluate(new DeferredObject[]{new DeferredJavaObject(userAgent)});
            checkField(resultInspector, row, "DeviceClass", "Tablet");
            checkField(resultInspector, row, "OperatingSystemNameVersion", "FireOS 3.4.0");
            checkField(resultInspector, row, "WebviewAppName", "Unknown");
        }
    }

    private void checkField(StandardStructObjectInspector resultInspector, Object row, String fieldName, String expectedValue) {
        final Object result = resultInspector.getStructFieldData(row, resultInspector.getStructFieldRef(fieldName));

        if (result == null) {
            assertNull(expectedValue);
        } else {
            assertEquals(expectedValue, result.toString());
        }
    }

    @Test
    public void testExplain() {
        ParseUserAgent parseUserAgent = new ParseUserAgent();
        assertTrue(parseUserAgent.getDisplayString(null).contains("UserAgent"), "Wrong explanation");
    }

}
