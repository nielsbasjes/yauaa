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

package nl.basjes.log2test;

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;

public final class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    private Main() {
    }

    public static void printAllPossible(String logformat) {
        Parser<Object> dummyParser = new HttpdLoglineParser<>(Object.class, logformat);
        List<String> possiblePaths = dummyParser.getPossiblePaths();
        for (String path : possiblePaths) {
            System.out.println(path);
        }
    }

    public static void main(String... args) throws IOException, MissingDissectorsException, InvalidDissectorException {
        switch (args.length) {
            case 0 -> {
                LOG.error("Usage: Provide `format` `filename`");
                return;
            }
            case 1 -> {
                printAllPossible(args[0]);
                return;
            }
            default -> {
                // Nothing
            }
        }
        String logFormat = args[0];
        List<String> fileNames = new ArrayList<>(Arrays.asList(args));
        fileNames.remove(0); // The logformat

        HttpdLoglineParser<LogRecord> parser = new HttpdLoglineParser<>(LogRecord.class, logFormat);

        Set<LogRecord> records = new TreeSet<>();
        for (String fileName : fileNames) {
            try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
                for (String line : stream
                    .filter(Objects::nonNull)
                    .filter(line -> !line.trim().isEmpty())
                    .toList()) {
                    try {
                        records.add(parser.parse(line));
                    }catch (nl.basjes.parse.core.exceptions.DissectionFailure df) {
                        LOG.error("DissectionFailure : {}", df.getMessage());
                        // Ignore and continue
                    }
                }
            }
        }

        UserAgentAnalyzer analyzer = UserAgentAnalyzer.newBuilder().immediateInitialization().withoutCache().build();

        // Some names (mostly Fediverse tools) cause an explosion in the number of useragents.
        Set<String> ignores = Set
            .of(
                "http.rb",
                "Akkoma",
                "Calckey",
                "Construct/",
                "Dendrite",
                "Friendica",
                "Misskey",
                "Pleroma"
            )
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        int fileNumber = 1;
        int recordCount = 0;
        StringBuilder content = new StringBuilder(1_000_000);
        for (LogRecord logRecord: records) {
            for (String value : logRecord.asHeadersMap().values()) {
                String lowerValue = value.toLowerCase(ROOT);
                if (ignores.stream().anyMatch(h -> h.contains(lowerValue))) {
                    break;
                }
            }

// Enable this to drop anything without Client Hints
//            if (logRecord.asHeadersMap().get("Sec-Ch-Ua") == null) {
//                continue; // Only with multiple headers
//            }

            content.append(analyzer.parse(logRecord.asHeadersMap()).toYamlTestCase());
            recordCount++;
            System.out.println(recordCount);
            if (recordCount % 1000 == 0) {
                writeToFile(String.format("New_Agents_%04d.yaml", fileNumber), content.toString());
                fileNumber++;
                content.delete(0, content.length());
            }
        }

        if (!content.isEmpty()) {
            writeToFile(String.format("New_Agents_%04d.yaml", fileNumber), content.toString());
        }
    }


    private static void writeToFile(String fileName, String content) throws IOException {
        try (FileWriter codeFileWriter = new FileWriter(fileName)) {
            codeFileWriter.write(content);
        } catch (IOException e) {
            throw new IOException("Fatal error writing code file (" + fileName + "):" + e.getMessage());
        }
    }

}
