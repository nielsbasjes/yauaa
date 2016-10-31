/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.debug;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.parse.UserAgentTreeFlattener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public final class Main {
    private Main() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        int returnValue = 0;
        final CommandOptions commandlineOptions = new CommandOptions();
        final CmdLineParser parser = new CmdLineParser(commandlineOptions);
        try {
            parser.parseArgument(args);

            UserAgentAnalyzer uaa = new UserAgentAnalyzer();
            UserAgentTreeFlattener flattenPrinter = new UserAgentTreeFlattener(new FlattenPrinter());
            uaa.setVerbose(commandlineOptions.debug);

            if (commandlineOptions.useragent != null) {
                UserAgent userAgent = uaa.parse(commandlineOptions.useragent);
                System.out.println(userAgent.toYamlTestCase());
                return;
            }

            // Open the file
            FileInputStream fstream = new FileInputStream(commandlineOptions.inFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            if (commandlineOptions.csvFormat) {
                for (String field : UserAgent.PRE_SORTED_FIELDS_LIST) {
                    System.out.print(field);
                    System.out.print("\t");
                }
                System.out.println("Useragent");
            }

            long ambiguities    = 0;
            long syntaxErrors   = 0;

            long linesTotal   = 0;
            long hitsTotal    = 0;
            long ipsTotal     = 0;
            long linesOk      = 0;
            long hitsOk       = 0;
            long ipsOk        = 0;
            long linesMatched = 0;
            long hitsMatched  = 0;
            long ipsMatched   = 0;
            long start = System.nanoTime();
            LOG.info("Start @ {}", start);

            long segmentStartTime = start;
            long segmentStartLines = linesTotal;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                if (strLine.startsWith(" ") || strLine.startsWith("#") || strLine.isEmpty()) {
                    continue;
                }

                long hits = 1;
                long ips = 1;
                String agentStr = strLine;

                if (strLine.contains("\t")) {
                    String[] parts = strLine.split("\t", 3);
                    hits = Long.parseLong(parts[0]);
                    ips = Long.parseLong(parts[1]);
                    agentStr = parts[2];
                }

                if (commandlineOptions.fullFlatten) {
                    flattenPrinter.parse(agentStr);
                    continue;
                }

                if (commandlineOptions.matchedFlatten) {
                    for (MatcherAction.Match match : uaa.getUsedMatches(new UserAgent(agentStr))) {
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
                ipsTotal  += ips;

                if (agent.hasSyntaxError()) {
                    if (commandlineOptions.yamlFormat) {
                        System.out.println("# Syntax error: " + agentStr);
                    }
                } else {
                    linesOk++;
                    hitsOk += hits;
                    ipsOk += ips;
                }

                if (!hasBad) {
                    linesMatched++;
                    hitsMatched += hits;
                    ipsMatched  += ips;
                }

                if (agent.hasAmbiguity()) {
                    ambiguities++;
                }
                if (agent.hasSyntaxError()) {
                    syntaxErrors++;
                }

                if (linesTotal % 1000 == 0) {
                    long nowTime = System.nanoTime();
                    long speed = (1000000000L*(linesTotal-segmentStartLines))/(nowTime-segmentStartTime);
                    System.err.println("Lines = "+linesTotal + " (A="+ambiguities+" S="+syntaxErrors+")  Speed = " + speed + "/sec.");
                    segmentStartTime = nowTime;
                    segmentStartLines = linesTotal;
                    ambiguities=0;
                    syntaxErrors=0;
                }

                if (commandlineOptions.outputOnlyBadResults) {
                    if (hasBad) {
                        continue;
                    }
                }

                if (commandlineOptions.yamlFormat) {
                    System.out.print(agent.toYamlTestCase());
                }
                if (commandlineOptions.jsonFormat) {
                    System.out.print(agent.toJson());
                }
                if (commandlineOptions.csvFormat){
                    for (String field : UserAgent.PRE_SORTED_FIELDS_LIST) {
                        String value = agent.getValue(field);
                        if (value!=null) {
                            System.out.print(value);
                        }
                        System.out.print("\t");
                    }
                    System.out.println(agent.getUserAgentString());
                }
            }

            //Close the input stream
            br.close();
            long stop = System.nanoTime();
            LOG.info("Stop  @ {}", stop);

            LOG.info("-------------------------------------------------------------");
            LOG.info("Performance: {} in {} sec --> {}/sec", linesTotal, (stop-start)/1000000000L, (1000000000L*linesTotal)/(stop-start));
            LOG.info("-------------------------------------------------------------");
            LOG.info("Parse results of {} lines", linesTotal);
            LOG.info("Parsed without error: {} (={}%)", linesOk, 100.0*(double)linesOk/(double)linesTotal);
            LOG.info("Fully matched       : {} (={}%)", linesMatched, 100.0*(double)linesMatched/(double)linesTotal);
            LOG.info("-------------------------------------------------------------");
            LOG.info("Parse results of {} hits", hitsTotal);
            LOG.info("Parsed without error: {} (={}%)", hitsOk, 100.0*(double)hitsOk/(double)hitsTotal);
            LOG.info("Fully matched       : {} (={}%)", hitsMatched, 100.0*(double)hitsMatched/(double)hitsTotal);
            LOG.info("-------------------------------------------------------------");
            LOG.info("Parse results of {} ips", ipsTotal);
            LOG.info("Parsed without error: {} (={}%)", ipsOk, 100.0*(double)ipsOk/(double)ipsTotal);
            LOG.info("Fully matched       : {} (={}%)", ipsMatched, 100.0*(double)ipsMatched/(double)ipsTotal);
            LOG.info("-------------------------------------------------------------");

        } catch (final CmdLineException e) {
            LOG.error("Errors: " + e.getMessage());
            LOG.error("");
            System.err.println("Usage: java jar <jar containing this class> <options>");
            parser.printUsage(System.out);
            returnValue = 1;
        } catch (final Exception e) {
            LOG.error("IOException:" + e);
            returnValue = 1;
        }
        System.exit(returnValue);
    }

    @SuppressWarnings({"PMD.ImmutableField", "CanBeFinal"})
    private static class CommandOptions {
        @Option(name = "-ua", usage = "A single useragent string", forbids = {"-in"})
        private String useragent = null;

        @Option(name = "-in", usage = "Location of input file", forbids = {"-ue"})
        private String inFile = null;

//        @Option(name = "-testAll", usage = "Run the tests against all built in testcases", required = false)
//        private boolean testAll = false;

        @Option(name = "-yaml", usage = "Output in yaml testcase format", forbids = {"-csv", "-json"})
        private boolean yamlFormat = false;

        @Option(name = "-csv", usage = "Output in csv format", forbids = {"-yaml", "-json"})
        private boolean csvFormat = false;

        @Option(name = "-json", usage = "Output in json format", forbids = {"-yaml", "-csv"})
        private boolean jsonFormat = false;

        @Option(name = "-bad", usage = "Output only cases that have a problem")
        private boolean outputOnlyBadResults = false;

        @Option(name = "-debug", usage = "Set to enable debugging.")
        private boolean debug = false;
//
//        @Option(name = "-stats", usage = "Set to enable statistics.", required = false)
//        private boolean stats = false;

        @Option(name = "-fullFlatten", usage = "Set to flatten each parsed agent string.")
        private boolean fullFlatten = false;

        @Option(name = "-matchedFlatten", usage = "Set to get the flattened values that were relevant for the Matchers.")
        private boolean matchedFlatten = false;

    }

    public static class FlattenPrinter extends Analyzer {

        @Override
        public void inform(String path, String value, ParseTree ctx) {
            System.out.println(path); // + " = " + value);
        }

        @Override
        public void informMeAbout(MatcherAction matcherAction, String keyPattern) {

        }
    }

}
