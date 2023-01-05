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

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
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
import org.elasticsearch.xcontent.XContentType;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

public final class LoadIntoES {

    private static final Logger LOG = LogManager.getLogger(LoadIntoES.class);

    public static final String INDEX_NAME = "useragents";

    private LoadIntoES() {
        // Dummy
    }

    public static final class UARecord {
        int year;
        int month;
        int day;
        long count;
        String userAgent;
        Map<String, String> parseResults;

        @Override
        public String toString() {
            return "UARecord{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", count=" + count +
                ", userAgent='" + userAgent + '\'' +
                '}';
        }
    }

    @SuppressWarnings("checkstyle:Indentation")
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setMaxParallelism(5);
        env.setParallelism(5);

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

            .shuffle()

            .map((MapFunction<String, UARecord>) value -> {
                String[] split = value.split("\t", 5);
                UARecord uaRecord = new UARecord();
                uaRecord.year      = Integer.parseInt(split[0]);
                uaRecord.month     = Integer.parseInt(split[1]);
                uaRecord.day       = Integer.parseInt(split[2]);
                uaRecord.count     = Long.parseLong(split[3]);
                uaRecord.userAgent = split[4];
                return uaRecord;
            })

            .map(
                new ParseUserAgent()
            )

            .sinkTo(
                new Elasticsearch7SinkBuilder<UARecord>()
                    .setBulkFlushMaxActions(10000)
                    .setHosts(new HttpHost("127.0.0.1", 9200, "http"))
                    .setEmitter(
                        (element, context, indexer) ->
                            indexer.add(createIndexRequest(element)))
                    .useApiCompatibilityMode() // Make it compatible with ES 8
                    .build()
            );

        // Execute program, beginning computation.
        env.execute("Feed into ES");
    }

    public static final class ParseUserAgent implements MapFunction<UARecord, UARecord> {
        UserAgentAnalyzer analyzer;

        ParseUserAgent() {
            analyzer = UserAgentAnalyzer
                .newBuilder()
                .showMatcherLoadStats()
                .addOptionalResources("file:UserAgents*/*.yaml")
                .addOptionalResources("classpath*:UserAgents-*/*.yaml")
                .immediateInitialization()
                .keepTests()
                .build();
        }

        @Override
        public UARecord map(UARecord value) {
            UserAgent userAgent = analyzer.parse(value.userAgent);
            value.parseResults = userAgent.toMap(userAgent.getAvailableFieldNamesSorted());
            return value;
        }
    }

    private static final DateTimeFormatter INDEX_DATE = new DateTimeFormatterBuilder()
                .appendLiteral(INDEX_NAME + '_')
                .appendValue(YEAR, 4, 10, SignStyle.NEVER)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 2)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH, 2)
                .toFormatter();

    private static IndexRequest createIndexRequest(UARecord element) {
        ZonedDateTime elementTimestamp = ZonedDateTime.of(element.year, element.month, element.day, 12, 0, 0, 0, ZoneId.of("UTC"));
        String jsonString = new JSONObject()
            .put("@timestamp", ISO_OFFSET_DATE_TIME.format(elementTimestamp))
            .put("count", element.count)
            .put("ua", element.userAgent)
            .put("parsed", element.parseResults)
            .toString();

        return
            Requests.indexRequest()
            .index(INDEX_DATE.format(elementTimestamp))
            .opType(DocWriteRequest.OpType.CREATE)
            .source(jsonString, XContentType.JSON);
    }

}
