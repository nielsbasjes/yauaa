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

package nl.basjes.parse.useragent.debug;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserAgentAnalyzerTester extends UserAgentAnalyzer {
    private static final Logger LOG = LoggerFactory.getLogger(UserAgentAnalyzerTester.class);

    public UserAgentAnalyzerTester() {
        super();
    }

    public UserAgentAnalyzerTester(String resourceString) {
        super(resourceString);
    }

    class TestResult {
        String field;
        String expected;
        String actual;
        boolean pass;
        boolean warn;
        long confidence;
    }

    /**
     * Run all the test_cases available.
     *
     * @return true if all tests were successful.
     */
    @SuppressWarnings({"unused"})
    public boolean runTests() {
        return runTests(false, true);
    }

    public boolean runTests(boolean showAll, boolean failOnUnexpected) {
        return runTests(showAll, failOnUnexpected, false);
    }

    public boolean runTests(boolean showAll, boolean failOnUnexpected, boolean measureSpeed) {
        boolean allPass = true;
        if (testCases == null) {
            return allPass;
        }
        UserAgent agent = new DebugUserAgent();

        List<TestResult> results = new ArrayList<>(32);

        String filenameHeader = "Name of the testfile";
        int filenameHeaderLength = filenameHeader.length();
        int maxFilenameLength = filenameHeaderLength;
        for (Map<String, Map<String, String>> test : testCases) {
            Map<String, String> metaData = test.get("metaData");
            String filename = metaData.get("filename");
            maxFilenameLength = Math.max(maxFilenameLength, filename.length());
        }

        maxFilenameLength++;

        StringBuilder sb = new StringBuilder(1024);

        sb.append("| ").append(filenameHeader);
        for (int i = filenameHeaderLength; i < maxFilenameLength; i++) {
            sb.append(' ');
        }
        if (measureSpeed) {
            sb.append("|S|AA|  PPS| msPP|--> S=Syntax Error, AA=Number of ambiguities during parse, PPS=parses/sec, msPP=milliseconds per parse");
        } else {
            sb.append("|S|AA| --> S=Syntax Error, AA=Number of ambiguities during parse");
        }

        LOG.info("+===========================================================================================");
        LOG.info(sb.toString());
        LOG.info("+-------------------------------------------------------------------------------------------");

        for (Map<String, Map<String, String>> test : testCases) {

            Map<String, String> input = test.get("input");
            Map<String, String> expected = test.get("expected");

            @SuppressWarnings("unchecked")
            List<String> options = (List<String>) test.get("options");
            Map<String, String> metaData = test.get("metaData");
            String filename = metaData.get("filename");

            boolean init = false;

            if (options == null) {
                setVerbose(false);
                agent.setDebug(false);
            } else {
                boolean newVerbose = options.contains("verbose");
                setVerbose(newVerbose);
                agent.setDebug(newVerbose);
                init = options.contains("init");
            }
            if (expected == null || expected.size() == 0) {
                init = true;
            }

            String testName = input.get("name");
            String userAgentString = input.get("user_agent_string");

            if (testName == null) {
                testName = userAgentString;
            }

            sb.setLength(0);
            sb.append("| ").append(filename);
            for (int i = filename.length(); i < maxFilenameLength; i++) {
                sb.append(' ');
            }

            agent.setUserAgentString(userAgentString);


            long measuredSpeed=-1;
            if (measureSpeed) {
                disableCaching();
                // Preheat
                for (int i = 0; i < 100; i++) {
                    agent = parse(agent);
                }
                long startTime = System.nanoTime();
                for (int i = 0; i < 1000; i++) {
                    agent = parse(agent);
                }
                long stopTime = System.nanoTime();
                measuredSpeed = (1000000000L*(1000))/(stopTime-startTime);
            } else {
                agent = parse(agent);
            }

            sb.append('|');
            if (agent.hasSyntaxError()) {
                sb.append('S');
            } else {
                sb.append(' ');
            }
            if (agent.hasAmbiguity()) {
                sb.append(String.format("|%2d", agent.getAmbiguityCount()));
            } else {
                sb.append("|  ");
            }

            if (measureSpeed) {
                sb.append('|').append(String.format("%5d", measuredSpeed));
                sb.append('|').append(String.format("%5.2f", 1000.0/measuredSpeed));
            }

            sb.append("| ").append(testName);
            LOG.info(sb.toString());
            sb.setLength(0);

            boolean pass = true;
            results.clear();

            if (init) {
                sb.append(agent.toYamlTestCase());
                LOG.info(sb.toString());
//                return allPass;
            } else {
                if (expected == null) {
                    LOG.info("| - No expectations ... ");
                    continue;
                }
            }

            int maxNameLength = 6; // "Field".length()+1;
            int maxActualLength = 7; // "Actual".length()+1;
            int maxExpectedLength = 9; // "Expected".length()+1;

            if (expected != null) {
                List<String> fieldNames = agent.getAvailableFieldNamesSorted();
                for (String newFieldName: expected.keySet()) {
                    if (!fieldNames.contains(newFieldName)) {
                        fieldNames.add(newFieldName);
                    }
                }

                for (String fieldName : fieldNames) {

                    TestResult result = new TestResult();
                    result.field = fieldName;
                    boolean expectedSomething;

                    // Expected value
                    String expectedValue = expected.get(fieldName);
                    if (expectedValue == null) {
                        expectedSomething = false;
                        result.expected = "<<absent>>";
                    } else {
                        expectedSomething = true;
                        result.expected = expectedValue;
                    }

                    // Actual value
                    UserAgent.AgentField agentField = agent.get(result.field);
                    if (agentField != null) {
                        result.actual = agentField.getValue();
                        result.confidence = agentField.getConfidence();
                    }
                    if (result.actual == null) {
                        result.actual = "<<<null>>>";
                    }

                    result.pass = result.actual.equals(result.expected);
                    if (!result.pass) {
                        result.warn=true;
                        if (expectedSomething) {
                            result.warn=false;
                            pass = false;
                            allPass = false;
                        } else {
                            if (failOnUnexpected) {
                                // We ignore this special field
                                if (!"__SyntaxError__".equals(result.field)) {
                                    result.warn = false;
                                    pass = false;
                                    allPass = false;
                                }
                            }
                        }
                    }

                    results.add(result);

                    maxNameLength = Math.max(maxNameLength, result.field.length());
                    maxActualLength = Math.max(maxActualLength, result.actual.length());
                    maxExpectedLength = Math.max(maxExpectedLength, result.expected.length());
                }

                if (!((DebugUserAgent)agent).analyzeMatchersResult()) {
                    pass = false;
                    allPass = false;
                }
            }

            if (!init && pass && !showAll) {
                continue;
            }

            if (!pass) {
                LOG.error("| TEST FAILED !");
            }

            if (agent.hasAmbiguity()) {
                LOG.info("| Parsing problem: Ambiguity {} times. ", agent.getAmbiguityCount());
            }
            if (agent.hasSyntaxError()) {
                LOG.info("| Parsing problem: Syntax Error");
            }

            if (init || !pass) {
                sb.setLength(0);
                sb.append("\n");
                sb.append("\n");
                sb.append("- matcher:\n");
                sb.append("#    options:\n");
                sb.append("#    - 'verbose'\n");
                sb.append("    require:\n");
                for (String path : getAllPathsAnalyzer(userAgentString).getValues()) {
                    if (path.contains("=\"")) {
                        sb.append("#    - '").append(path).append("'\n");
                    }
                }
                sb.append("    extract:\n");
                sb.append("#    - 'DeviceClass           :   1:' \n");
                sb.append("#    - 'DeviceBrand           :   1:' \n");
                sb.append("#    - 'DeviceName            :   1:' \n");
                sb.append("#    - 'OperatingSystemClass  :   1:' \n");
                sb.append("#    - 'OperatingSystemName   :   1:' \n");
                sb.append("#    - 'OperatingSystemVersion:   1:' \n");
                sb.append("#    - 'LayoutEngineClass     :   1:' \n");
                sb.append("#    - 'LayoutEngineName      :   1:' \n");
                sb.append("#    - 'LayoutEngineVersion   :   1:' \n");
                sb.append("#    - 'AgentClass            :   1:' \n");
                sb.append("#    - 'AgentName             :   1:' \n");
                sb.append("#    - 'AgentVersion          :   1:' \n");
                sb.append("\n");
                sb.append("\n");
                LOG.info(sb.toString());
            }

            sb.setLength(0);
            sb.append("+--------+-");
            for (int i = 0; i < maxNameLength; i++) {
                sb.append('-');
            }
            sb.append("-+-");
            for (int i = 0; i < maxActualLength; i++) {
                sb.append('-');
            }
            sb.append("-+------------+-");
            for (int i = 0; i < maxExpectedLength; i++) {
                sb.append('-');
            }
            sb.append("-+");

            String separator = sb.toString();
            LOG.info(separator);

            sb.setLength(0);
            sb.append("| Result | Field ");
            for (int i = 6; i < maxNameLength; i++) {
                sb.append(' ');
            }
            sb.append(" | Actual ");
            for (int i = 7; i < maxActualLength; i++) {
                sb.append(' ');
            }
            sb.append(" | Confidence | Expected ");
            for (int i = 9; i < maxExpectedLength; i++) {
                sb.append(' ');
            }
            sb.append(" |");

            LOG.info(sb.toString());

            LOG.info(separator);

            for (TestResult result : results) {
                sb.setLength(0);
                if (result.pass) {
                    sb.append("|        | ");
                } else {
                    if (result.warn) {
                        sb.append("| ~warn~ | ");
                    } else {
                        sb.append("| -FAIL- | ");
                    }
                }
                sb.append(result.field);
                for (int i = result.field.length(); i < maxNameLength; i++) {
                    sb.append(' ');
                }
                sb.append(" | ");
                sb.append(result.actual);

                for (int i = result.actual.length(); i < maxActualLength; i++) {
                    sb.append(' ');
                }
                sb.append(" | ");
                sb.append(String.format("%10d", result.confidence));
                sb.append(" | ");

                if (result.pass) {
                    for (int i = 0; i < maxExpectedLength; i++) {
                        sb.append(' ');
                    }
                    sb.append(" |");
                    LOG.info(sb.toString());
                } else {
                    sb.append(result.expected);
                    for (int i = result.expected.length(); i < maxExpectedLength; i++) {
                        sb.append(' ');
                    }
                    sb.append(" |");
                    if (result.warn) {
                        LOG.warn(sb.toString());
                    } else {
                        LOG.error(sb.toString());
                    }
                }
            }

            LOG.info(separator);
            LOG.info("");

            LOG.info(((DebugUserAgent)agent).toMatchTrace());

            LOG.info("\n\nconfig:\n"+agent.toYamlTestCase());
            if (!pass && !showAll) {
//                LOG.info("+===========================================================================================");
                return false;
            }
            if (init) {
                return allPass;
            }
        }

        LOG.info("+===========================================================================================");
        return allPass;
    }
}
