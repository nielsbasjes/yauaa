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

package nl.basjes.parse.useragent.hive;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredJavaObject;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StandardStructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNull;

public class TestParseUserAgentBadInput {

    @Rule
    public final transient ExpectedException expectedEx = ExpectedException.none();

    private ParseUserAgent parseUserAgent = new ParseUserAgent();

    @Test
    public void testBadInputType() throws HiveException {
        expectedEx.expect(UDFArgumentException.class);
        expectedEx.expectMessage("The argument must be a string");
        StandardStructObjectInspector resultInspector = (StandardStructObjectInspector) parseUserAgent
            .initialize(new ObjectInspector[]{
                PrimitiveObjectInspectorFactory.javaBooleanObjectInspector
            });
    }

    @Test
    public void testBadInputNull() throws HiveException {
        StandardStructObjectInspector resultInspector = (StandardStructObjectInspector) parseUserAgent
            .initialize(new ObjectInspector[]{
                PrimitiveObjectInspectorFactory.javaStringObjectInspector
            });

        assertNull(parseUserAgent.evaluate(new DeferredObject[]{new DeferredJavaObject(null)}));
    }



}
