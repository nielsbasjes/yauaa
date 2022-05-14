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

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;

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

    private List<StringObjectInspector> argsOI = null;

    private static ThreadLocal<UserAgentAnalyzer> threadLocalUserAgentAnalyzer =
        ThreadLocal.withInitial(() ->
            UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                // Caffeine is a Java 11+ library.
                .useJava8CompatibleCaching()
                .immediateInitialization()
                .build());

    private List<String> fieldNames = null;
    private List<String> allHeaders = null;

    @Override
    public void close() {
        threadLocalUserAgentAnalyzer.remove();
    }

    @Override
    public ObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
        // ================================
        // Check the input
        // This UDF accepts one argument
        if (args.length == 0) {
            throw new UDFArgumentException("The argument list must be non empty");
        }

        // All arguments must be a String
        argsOI = new ArrayList<>();
        for (ObjectInspector arg : args) {
            if (!(arg instanceof StringObjectInspector)) {
                throw new UDFArgumentException("The argument must be a string");
            }
            argsOI.add((StringObjectInspector) arg);
        }

        // ================================
        UserAgentAnalyzer userAgentAnalyzer = threadLocalUserAgentAnalyzer.get();

        fieldNames = userAgentAnalyzer.getAllPossibleFieldNamesSorted();
        allHeaders = new ArrayList<>();
        allHeaders.add(USERAGENT_HEADER);
        allHeaders.addAll(userAgentAnalyzer.supportedClientHintHeaders());

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
        Map<String, String> requestHeaders = new TreeMap<>();

        int i = 0;
        while (i < argsOI.size()) {
            String parameter = argsOI.get(i).getPrimitiveJavaObject(args[i].get());
            if (parameter != null) {
                if (allHeaders.stream().anyMatch(parameter::equalsIgnoreCase)) {
                    String value;
                    if (i + 1 >= argsOI.size()) {
                        throw new IllegalArgumentException("Invalid last element in argument list (was a header name which requires a value to follow)");
                    } else {
                        value = argsOI.get(i + 1).getPrimitiveJavaObject(args[i + 1].get());
                        i++;
                    }
                    requestHeaders.put(parameter, value);
                    i++;
                    continue;
                }
            }
            if (i == 0) {
                requestHeaders.put(USERAGENT_HEADER, parameter);
                i++;
                continue;
            }
            throw new IllegalArgumentException("Bad argument list for ParseUserAgent.");
        }

        UserAgent userAgent = threadLocalUserAgentAnalyzer.get().parse(requestHeaders);
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
