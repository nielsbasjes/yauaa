/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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

package nl.basjes.parse.useragent;

import lombok.AllArgsConstructor;
import lombok.ToString;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch7SinkBuilder;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

public final class LoadIntoES {

    private static final Logger LOG = LogManager.getLogger(LoadIntoES.class);

    public static final String INDEX_NAME = "useragents";

    private LoadIntoES() {
        // Dummy
    }

    @AllArgsConstructor
    @ToString
    public static final class UARecord {
        LogRecord logRecord;
        Map<String, String> parseResults;
    }

    @SuppressWarnings({"checkstyle:Indentation", "deprecation"})
    public static void main(String[] args) throws Exception {

        Runtime.Version version = Runtime.version();
        if (version.feature() != 11) {
            LOG.fatal("The runtime Java MUST be version 11. Now using version {}", version);
            return;
        }

        // Because of licensing issues with ElasticSearch; Flink only supports up to 7.10.2 (the last with the Apache license)
        // This is technically ALMOST compatible API compatible with the latest 7.x client but NOT with the 8x server.
        // The "almost" is in the fact that they have moved the "TimeValue" class to a different package :(
        // To make the latest (licensing INcompatible) 7.x work with an 8.x server you need to set this environment variable:
        updateEnv(RestHighLevelClient.API_VERSIONING_ENV_VARIABLE, "true");

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setMaxParallelism(8);
        env.setParallelism(8);

        String logFormat1 = "%a %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Sec-CH-UA}i\" \"%{Sec-CH-UA-Arch}i\" \"%{Sec-CH-UA-Bitness}i\" \"%{Sec-CH-UA-Full-Version}i\" \"%{Sec-CH-UA-Full-Version-List}i\" \"%{Sec-CH-UA-Mobile}i\" \"%{Sec-CH-UA-Model}i\" \"%{Sec-CH-UA-Platform}i\" \"%{Sec-CH-UA-Platform-Version}i\" \"%{Sec-CH-UA-WoW64}i\" %V";
        HttpdLoglineParser<LogRecord> logLineParser = new HttpdLoglineParser<>(LogRecord.class, logFormat1);

        ParseUserAgent parseUserAgent = new ParseUserAgent();

        File dir = new File(".");
        FileFilter fileFilter = new WildcardFileFilter("*.txt");
        File[] files = dir.listFiles(fileFilter);

        if (files == null || files.length==0) {
            LOG.error("NO files found");
            return;
        }

        List<Path> paths = new ArrayList<>();
        for (File file : files) {
            paths.add(Path.fromLocalFile(file));
        }

        final FileSource<String> source =
            FileSource
                .forRecordStreamFormat(
                    new TextLineInputFormat(),
                    paths.toArray(new Path[0]))
                .build();

        env
            .fromSource(source, WatermarkStrategy.noWatermarks(), "file-source")

// For testing:
//        env
//            .fromElements(
//                "10.10.10.10 - - [25/Apr/2023:08:47:42 +0200] \"GET /bug.svg HTTP/1.1\" 200 1124 \"https://try.yauaa.basjes.nl/style.css?dc861a53\" \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 OPR/97.0.0.0\" \"\\\"Not?A_Brand\\\";v=\\\"99\\\", \\\"Opera\\\";v=\\\"97\\\", \\\"Chromium\\\";v=\\\"111\\\"\" \"\\\"x86\\\"\" \"\\\"64\\\"\" \"\\\"97.0.4719.51\\\"\" \"\\\"Not?A_Brand\\\";v=\\\"99.0.0.0\\\", \\\"Opera\\\";v=\\\"97.0.4719.51\\\", \\\"Chromium\\\";v=\\\"111.0.5563.147\\\"\" \"?0\" \"\" \"\\\"Windows\\\"\" \"\\\"14.0.0\\\"\" \"?0\" try.yauaa.basjes.nl",
//                "10.10.10.10 - - [13/Mar/2022:16:05:37 +0100] \"GET /bug.svg HTTP/1.1\" 200 1124 \"https://try.yauaa.basjes.nl/style.css\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.0.0 Safari/537.36\" \"\\\" Not A;Brand\\\";v=\\\"99\\\", \\\"Chromium\\\";v=\\\"99\\\", \\\"Google Chrome\\\";v=\\\"99\\\"\" \"-\" \"-\" \"?0\" \"-\" \"\\\"Linux\\\"\" \"-\" try.yauaa.basjes.nl",
//                "10.10.10.10 - - [13/Mar/2022:16:05:37 +0100] \"GET /style.css HTTP/1.1\" 200 4954 \"https://try.yauaa.basjes.nl/\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.0.0 Safari/537.36\" \"\\\" Not A;Brand\\\";v=\\\"99\\\", \\\"Chromium\\\";v=\\\"99\\\", \\\"Google Chrome\\\";v=\\\"99\\\"\" \"-\" \"-\" \"?0\" \"-\" \"\\\"Linux\\\"\" \"-\" try.yauaa.basjes.nl"
//            )

            .shuffle() // Load balancing the parsing step

//            // Output COUNTS per task
//            .map(new RichMapFunction<String, String>() {
//                int pId = -1;
//                @Override
//                public void open(Configuration parameters) {
//                    pId = getRuntimeContext().getIndexOfThisSubtask();
//                }
//
//                private long recordsOk = 0;
//                private long recordsBad = 0;
//                @Override
//                public String map(String value) {
//                    if (value == null) {
//                        recordsBad++;
//                    } else {
//                        recordsOk++;
//                    }
//                    if ((recordsOk + recordsBad) % 10000 == 0) {
//                        LOG.info("Input {} : Ok: {} | Bad: {}", pId, recordsOk, recordsBad);
//                    }
//                    return value;
//                }
//            })

// For different file formats:
//            .map((MapFunction<String, UARecord>) value -> {
//                String[] split = value.split("\t", 5);
//                UARecord uaRecord = new UARecord();
//                uaRecord.year      = Integer.parseInt(split[0]);
//                uaRecord.month     = Integer.parseInt(split[1]);
//                uaRecord.day       = Integer.parseInt(split[2]);
//                uaRecord.count     = Long.parseLong(split[3]);
//                uaRecord.userAgent = split[4];
//                return uaRecord;
//            })

            .map(logRecord -> {
                try {
                    return logLineParser.parse(logRecord);
                } catch (DissectionFailure df) {
                    LOG.error("{}", logRecord);
                    return null; // Do not fail the job on a parse error.
                }
            })
            // Output COUNTS per task
            .map(new RichMapFunction<LogRecord, LogRecord>() {
                int pId = -1;
                @Override
                public void open(Configuration parameters) {
                    pId = getRuntimeContext().getIndexOfThisSubtask();
                }

                private long recordsOk = 0;
                private long recordsBad = 0;
                @Override
                public LogRecord map(LogRecord value) {
                    if (value == null) {
                        recordsBad++;
                    } else {
                        recordsOk++;
                    }
                    if ((recordsOk + recordsBad) % 10000 == 0) {
                        LOG.info("Parsed {} : Ok: {} | Bad: {}", pId, recordsOk, recordsBad);
                    }
                    return value;
                }
            })
            .filter(Objects::nonNull) // Just discard all the parse problems

//            // Optimize usage of the Yauaa cache
            .keyBy((KeySelector<LogRecord, Integer>) LogRecord::getUseragentCacheKey)
            // Parse it
            .map(parseUserAgent)


//            .map(x -> x.logRecord.toJson())
//            .print();

            .sinkTo(
                new Elasticsearch7SinkBuilder<UARecord>()
                    .setBulkFlushMaxActions(1000)
//                    .setBulkFlushInterval(1000)
                    .setHosts(new HttpHost("127.0.0.1", 9200, "http"))
                    .setEmitter(
                        (element, context, indexer) ->
                            indexer.add(createIndexRequest(element)))
                    .build()
            );

        // Execute program, beginning computation.
        env.execute("Feed into ES");
    }

    public static final class ParseUserAgent implements MapFunction<LogRecord, UARecord> {
        UserAgentAnalyzer analyzer;

        ParseUserAgent() {
            analyzer = UserAgentAnalyzer
                .newBuilder()
                .showMatcherLoadStats()
                .addOptionalResources("file:UserAgents*/*.yaml")
                .addOptionalResources("classpath*:UserAgents-*/*.yaml")
                .immediateInitialization()
                .dropTests()
                .build();
        }

        @Override
        public UARecord map(LogRecord logRecord) {
            UserAgent userAgent = analyzer.parse(logRecord.asHeadersMap());
            Map<String, String> parseResults = userAgent.toMap(userAgent.getAvailableFieldNamesSorted());
            return new UARecord(logRecord, parseResults);
        }
    }

    private static final DateTimeFormatter INDEX_DATE = new DateTimeFormatterBuilder()
                .appendLiteral(INDEX_NAME + '_')
                .appendValue(YEAR, 4, 10, SignStyle.NEVER)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 2)
//                .appendLiteral('-')
//                .appendValue(DAY_OF_MONTH, 2)
                .toFormatter();

    private static IndexRequest createIndexRequest(UARecord element) {
        Instant eventTimestamp = Instant.ofEpochMilli(Long.parseLong(element.logRecord.getEpoch()));
        ZonedDateTime elementTimestamp = ZonedDateTime.ofInstant(eventTimestamp, UTC);

        String inputJson = element.logRecord.toJson();
        // If the length of the inputJson is > 255 things behave differently in ElasticSearch.
        // Normally the "ua" is filled and the ua.keyword is the same.
        // if too long then the "ua" is filled but the ua.keyword is missing.
        if (inputJson.length() > 255) {
            // Truncate it
            inputJson = inputJson.substring(0, 250) + "|...";
        }

        String jsonString = new JSONObject()
            .put("@timestamp",  ISO_OFFSET_DATE_TIME.format(elementTimestamp))
            .put("count",       1)
            .put("ua", inputJson)
            .put("parsed",      element.parseResults)
            .toString();

        return
            Requests.indexRequest()
            .index(INDEX_DATE.format(elementTimestamp))
            .opType(DocWriteRequest.OpType.CREATE)
            .source(jsonString, XContentType.JSON);
    }

    // https://stackoverflow.com/a/496849
    @SuppressWarnings({ "unchecked" })
    public static void updateEnv(String name, String val) throws ReflectiveOperationException {
        Map<String, String> env = System.getenv();
        Field field = env.getClass().getDeclaredField("m");
        field.setAccessible(true);
        ((Map<String, String>) field.get(env)).put(name, val);
    }

}
