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

package nl.basjes.parse.useragent.commandline;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.analyze.MatchesList.Match;
import nl.basjes.parse.useragent.debug.FlattenPrinter;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester.UserAgentAnalyzerTesterBuilder;
import nl.basjes.parse.useragent.parse.UserAgentTreeFlattener;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_FIELDNAME;
import static nl.basjes.parse.useragent.commandline.Main.OutputFormat.CSV;
import static nl.basjes.parse.useragent.commandline.Main.OutputFormat.JSON;
import static nl.basjes.parse.useragent.commandline.Main.OutputFormat.YAML;
import static nl.basjes.parse.useragent.utils.YauaaVersion.logVersion;

public final class Main {
    private Main() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    enum OutputFormat {
        CSV, JSON, YAML
    }

    private static void printHeader(OutputFormat outputFormat, List<String> fields) {
        switch (outputFormat) {
            case CSV:
                boolean doSeparator = false;
                for (String field : fields) {
                    if (doSeparator) {
                        System.out.print("\t");
                    } else {
                        doSeparator = true;
                    }
                    System.out.print(field);
                }
                System.out.println();
                break;
            default:
                break;
        }
    }

    private static void printAgent(OutputFormat outputFormat, List<String> fields, UserAgent agent) {
        switch (outputFormat) {
            case CSV:
                boolean doSeparator = false;
                for (String field : fields) {
                    if (doSeparator) {
                        System.out.print("\t");
                    } else {
                        doSeparator = true;
                    }
                    String value = agent.getValue(field);
                    if (value != null) {
                        System.out.print(value);
                    }
                }
                System.out.println();
                break;
            case JSON:
                System.out.println(agent.toJson(fields));
                break;
            case YAML:
                System.out.println(agent.toYamlTestCase());
                break;
            default:
        }
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        int returnValue = 0;
        final CommandOptions commandlineOptions = new CommandOptions();
        final CmdLineParser parser = new CmdLineParser(commandlineOptions);
        try {
            parser.parseArgument(args);

            if (commandlineOptions.useragent == null && commandlineOptions.inFile == null) {
                throw new CmdLineException(parser, "No input specified."); // NOSONAR: Deprecated
            }

            OutputFormat outputFormat = YAML;
            if (commandlineOptions.csvFormat) {
                outputFormat = CSV;
            } else {
                if (commandlineOptions.jsonFormat) {
                    outputFormat = JSON;
                }
            }

            UserAgentAnalyzerTesterBuilder builder = UserAgentAnalyzerTester.newBuilder();
            builder.hideMatcherLoadStats();
            builder.withCache(commandlineOptions.cacheSize);
            if (commandlineOptions.fields != null) {
                for (String field: commandlineOptions.fields) {
                    builder.withField(field);
                }
            }
            UserAgentAnalyzerTester uaa = builder.build();

            UserAgentTreeFlattener flattenPrinter = new UserAgentTreeFlattener(new FlattenPrinter(System.out));

            List<String> fields;
            if (commandlineOptions.fields == null) {
                fields = uaa.getAllPossibleFieldNamesSorted();
                fields.add(USERAGENT_FIELDNAME);
            } else {
                fields = commandlineOptions.fields;
            }
            printHeader(outputFormat, fields);

            if (commandlineOptions.useragent != null) {
                UserAgent agent = uaa.parse(commandlineOptions.useragent);
                printAgent(outputFormat, fields, agent);
                return;
            }

            // Open the file (or stdin)
            InputStream inputStream = System.in;
            if (!"-".equals(commandlineOptions.inFile)) {
                inputStream = new FileInputStream(commandlineOptions.inFile);
            }

            try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

                String strLine;

                long ambiguities  = 0;
                long syntaxErrors = 0;

                long linesTotal   = 0;
                long hitsTotal    = 0;
                long linesOk      = 0;
                long hitsOk       = 0;
                long linesMatched = 0;
                long hitsMatched  = 0;
                long start        = System.nanoTime();

                long segmentStartTime  = start;
                long segmentStartLines = linesTotal;

                //Read File Line By Line
                while ((strLine = br.readLine()) != null) {
                    if (strLine.startsWith(" ") || strLine.startsWith("#") || strLine.isEmpty()) {
                        continue;
                    }

                    long   hits     = 1;
                    String agentStr = strLine;

                    if (strLine.contains("\t")) {
                        String[] parts = strLine.split("\t", 2);
                        try { //NOSONAR: Not a separate method
                            hits = Long.parseLong(parts[0]);
                            agentStr = parts[1];
                        } catch (NumberFormatException nfe) {
                            agentStr = strLine;
                        }
                    }

                    if (commandlineOptions.fullFlatten) {
                        flattenPrinter.parse(agentStr);
                        continue;
                    }

                    if (commandlineOptions.matchedFlatten) {
                        for (Match match : uaa.getUsedMatches(new UserAgent(agentStr))) {
                            System.out.println(match.getKey() + " " + match.getValue());
                        }
                        continue;
                    }

                    UserAgent agent = uaa.parse(agentStr);

                    boolean hasBad = false;
                    for (String field : UserAgent.STANDARD_FIELDS) {
                        if (agent.getConfidence(field) < 0) {
                            hasBad = true;
                            break;
                        }
                    }

                    linesTotal++;
                    hitsTotal += hits;

                    if (agent.hasSyntaxError()) {
                        if (outputFormat == YAML) {
                            System.out.println("# Syntax error: " + agentStr);
                        }
                    } else {
                        linesOk++;
                        hitsOk += hits;
                    }

                    if (!hasBad) {
                        linesMatched++;
                        hitsMatched += hits;
                    }

                    if (agent.hasAmbiguity()) {
                        ambiguities++;
                    }
                    if (agent.hasSyntaxError()) {
                        syntaxErrors++;
                    }

                    if (linesTotal % 1000 == 0) {
                        long nowTime = System.nanoTime();
                        long speed   = (1000000000L * (linesTotal - segmentStartLines)) / (nowTime - segmentStartTime);
                        System.err.println(
                            String.format("Lines = %8d (Ambiguities: %5d ; SyntaxErrors: %5d) Analyze speed = %5d/sec.",
                                linesTotal, ambiguities, syntaxErrors, speed));
                        segmentStartTime = nowTime;
                        segmentStartLines = linesTotal;
                        ambiguities = 0;
                        syntaxErrors = 0;
                    }

                    if (commandlineOptions.outputOnlyBadResults) {
                        if (hasBad) {
                            continue;
                        }
                    }

                    printAgent(outputFormat, fields, agent);
                }

                long stop = System.nanoTime();

                LOG.info("-------------------------------------------------------------");
                LOG.info("Performance: {} in {} sec --> {}/sec",
                    linesTotal, (stop - start) / 1000000000L, (1000000000L * linesTotal) / (stop - start));
                LOG.info("-------------------------------------------------------------");
                LOG.info("Parse results of {} lines", linesTotal);
                LOG.info(String.format("Parsed without error: %8d (=%6.2f%%)",
                    linesOk, 100.0 * (double) linesOk / (double) linesTotal));
                LOG.info(String.format("Parsed with    error: %8d (=%6.2f%%)",
                    linesTotal - linesOk, 100.0 * (double) (linesTotal - linesOk) / (double) linesTotal));
                LOG.info(String.format("Fully matched       : %8d (=%6.2f%%)",
                    linesMatched, 100.0 * (double) linesMatched / (double) linesTotal));

                if (linesTotal != hitsTotal) {
                    LOG.info("-------------------------------------------------------------");
                    LOG.info("Parse results of {} hits", hitsTotal);
                    LOG.info(String.format("Parsed without error: %8d (=%6.2f%%)",
                        hitsOk, 100.0 * (double) hitsOk / (double) hitsTotal));
                    LOG.info(String.format("Parsed with    error: %8d (=%6.2f%%)",
                        hitsTotal - hitsOk, 100.0 * (double) (hitsTotal - hitsOk) / (double) hitsTotal));
                    LOG.info(String.format("Fully matched       : %8d (=%6.2f%%)",
                        hitsMatched, 100.0 * (double) hitsMatched / (double) hitsTotal));
                    LOG.info("-------------------------------------------------------------");
                }
            }
        } catch (final CmdLineException e) {
            logVersion();
            LOG.error("Errors: {}", e.getMessage());
            LOG.error("");
            System.err.println("Usage: java jar <jar containing this class> <options>");
            parser.printUsage(System.err);
            returnValue = 1;
        } catch (final Exception e) {
            LOG.error("IOException: {}", e);
            returnValue = 1;
        }
        System.exit(returnValue);
    }

    @SuppressWarnings({"PMD.ImmutableField", "CanBeFinal", "unused"})
    private static class CommandOptions {
        @Option(name = "-ua", usage = "A single useragent string", forbids = {"-in"})
        private String useragent = null;

        @Option(name = "-in", usage = "Location of input file", forbids = {"-ua"})
        private String inFile = null;

        @Option(name = "-yaml", usage = "Output in yaml testcase format", forbids = {"-csv", "-json"})
        private boolean yamlFormat = false;

        @Option(name = "-csv", usage = "Output in csv format", forbids = {"-yaml", "-json"})
        private boolean csvFormat = false;

        @Option(name = "-json", usage = "Output in json format", forbids = {"-yaml", "-csv"})
        private boolean jsonFormat = false;

        @Option(name = "-fields", handler = StringArrayOptionHandler.class,
            usage = "A list of the desired fieldnames (use '" + USERAGENT_FIELDNAME + "' if you want the input value as well)")
        private List<String> fields = null;

        @Option(name = "-cache", usage = "The number of elements that can be cached (LRU).")
        private int cacheSize = 10000;

        @Option(name = "-bad", usage = "Output only cases that have a problem")
        private boolean outputOnlyBadResults = false;

        @Option(name = "-debug", usage = "Set to enable debugging.")
        private boolean debug = false;

        @Option(name = "-fullFlatten", usage = "Set to flatten each parsed agent string.")
        private boolean fullFlatten = false;

        @Option(name = "-matchedFlatten", usage = "Set to get the flattened values that were relevant for the Matchers.")
        private boolean matchedFlatten = false;

    }


}
