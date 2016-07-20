/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent;

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

            // Open the file
            FileInputStream fstream = new FileInputStream(commandlineOptions.inFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            if (commandlineOptions.csvFormat) {
                for (String field : UserAgent.STANDARD_FIELDS) {
                    System.out.print(field);
                    System.out.print("\t");
                }
                System.out.println("Useragent");
            }

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
                    if (agent.get(field).confidence < 0) {
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

                if (linesTotal % 1000 == 0) {
                    long nowTime = System.nanoTime();
                    long speed = (1000000000L*(linesTotal-segmentStartLines))/(nowTime-segmentStartTime);
                    System.err.println("Lines = "+linesTotal + "   Speed = " + speed + "/sec.");
                    segmentStartTime = nowTime;
                    segmentStartLines = linesTotal;
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
                    for (String field : UserAgent.STANDARD_FIELDS) {
                        System.out.print(agent.get(field).getValue());
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

    @SuppressWarnings({"PMD.ImmutableField"})
    private static class CommandOptions {
        @Option(name = "-in", usage = "Location of input file", required = true)
        private String inFile;

        @Option(name = "-testAll", usage = "Run the tests against all built in testcases", required = false)
        private boolean testAll = false;

        @Option(name = "-yaml", usage = "Output in yaml testcase format", required = false, forbids = {"-csv", "-json"})
        private boolean yamlFormat = false;

        @Option(name = "-csv", usage = "Output in csv format", required = false, forbids = {"-yaml", "-json"})
        private boolean csvFormat = false;

        @Option(name = "-json", usage = "Output in json format", required = false, forbids = {"-yaml", "-csv"})
        private boolean jsonFormat = false;

        @Option(name = "-bad", usage = "Output only cases that have a problem", required = false)
        private boolean outputOnlyBadResults = false;

        @Option(name = "-debug", usage = "Set to enable debugging.", required = false)
        private boolean debug = false;

        @Option(name = "-stats", usage = "Set to enable statistics.", required = false)
        private boolean stats = false;

        @Option(name = "-fullFlatten", usage = "Set to flatten each parsed agent string.", required = false)
        private boolean fullFlatten = false;

        @Option(name = "-matchedFlatten", usage = "Set to get the flattened values that were relevant for the Matchers.", required = false)
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
