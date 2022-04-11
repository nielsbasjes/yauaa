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

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.CacheInstantiator;
import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.ClientHintsCacheInstantiator;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.io.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hive UDF for parsing the UserAgent string.
 * An example statement
 * would be:
 * <pre>
 *   ADD JAR
 ADD JAR hdfs:///yauaa-hive-2.0-SNAPSHOT-udf.jar;


 USING JAR 'hdfs:/plugins/yauaa-hive-2.0-SNAPSHOT-udf.jar';

 SELECT ParseUserAgent('Mozilla/5.0 (X11\; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36');
 SELECT ParseUserAgent(useragent) from useragents;
 *  SELECT ParseUserAgent(useragent) FROM clickLogs a;
 * </pre>
 *
 *
 */

@Description(
    name = "ParseUserAgent",
    value = "_FUNC_(str) - Parses the UserAgent into all possible pieces.",
    extended = "Example:\n" +
        "> SELECT ParseUserAgent(useragent).DeviceClass, \n" +
        "         ParseUserAgent(useragent).OperatingsystemNameVersion, \n" +
        "         ParseUserAgent(useragent).AgentNameVersionMajor \n" +
        "  FROM   clickLogs;\n" +
        "+---------------+-----------------------------+------------------------+\n" +
        "|  deviceclass  | operatingsystemnameversion  | agentnameversionmajor  |\n" +
        "+---------------+-----------------------------+------------------------+\n" +
        "| Phone         | Android 6.0                 | Chrome 46              |\n" +
        "| Tablet        | Android 5.1                 | Chrome 40              |\n" +
        "| Desktop       | Linux ??                    | Chrome 59              |\n" +
        "| Game Console  | Windows 10.0                | Edge 13                |\n" +
        "+---------------+-----------------------------+------------------------+\n")
public class ParseUserAgent extends GenericUDF {

    private StringObjectInspector useragentOI = null;
    private UserAgentAnalyzer userAgentAnalyzer = null;
    private List<String> fieldNames = null;

    @Override
    public ObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
        // ================================
        // Check the input
        // This UDF accepts one argument
        if (args.length != 1) {
            throw new UDFArgumentException("The argument list must be exactly 1 element");
        }

        // The first argument must be a String
        ObjectInspector inputOI = args[0];
        if (!(inputOI instanceof StringObjectInspector)) {
            throw new UDFArgumentException("The argument must be a string");
        }
        useragentOI = (StringObjectInspector) inputOI;

        // ================================
        // Initialize the parser
        userAgentAnalyzer = UserAgentAnalyzer
            .newBuilder()
            .hideMatcherLoadStats()
            .delayInitialization()
            // Caffeine is a Java 11+ library.
            // This is one is Java 8 compatible.
            .withCacheInstantiator(
                (CacheInstantiator) size ->
                    Collections.synchronizedMap(new LRUMap<>(size)))
            .withClientHintCacheInstantiator(
                (ClientHintsCacheInstantiator<?>) size ->
                    Collections.synchronizedMap(new LRUMap<>(size)))
            .build();

        fieldNames = userAgentAnalyzer.getAllPossibleFieldNamesSorted();

        // ================================
        // Define the output
        // https://stackoverflow.com/questions/26026027/how-to-return-struct-from-hive-udf

        // Define the field names for the struct<> and their types
        List<ObjectInspector> fieldObjectInspectors = new ArrayList<>(fieldNames.size());

        fieldNames
            .forEach(f -> fieldObjectInspectors.add(PrimitiveObjectInspectorFactory.writableStringObjectInspector));

        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldObjectInspectors);
    }

    @Override
    public Object evaluate(DeferredObject[] args) throws HiveException {
        String userAgentString = useragentOI.getPrimitiveJavaObject(args[0].get());

        if (userAgentString == null) {
            return null;
        }

        UserAgent userAgent = userAgentAnalyzer.parse(userAgentString);
        List<Object> result = new ArrayList<>(fieldNames.size());
        for (String fieldName : fieldNames) {
            String value = userAgent.getValue(fieldName);
            if (value == null) {
                result.add(null);
            } else {
                result.add(new Text(value));
            }
        }
        return result.toArray();
    }

    @Override
    public String getDisplayString(String[] args) {
        return "Parses the UserAgent into all possible pieces.";
    }
}
