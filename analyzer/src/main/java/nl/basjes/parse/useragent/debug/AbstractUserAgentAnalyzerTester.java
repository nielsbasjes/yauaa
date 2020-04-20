/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

package nl.basjes.parse.useragent.debug;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzerDirect;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.MatchesList.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;

@DefaultSerializer(AbstractUserAgentAnalyzerTester.KryoSerializer.class)
public class AbstractUserAgentAnalyzerTester extends AbstractUserAgentAnalyzer {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractUserAgentAnalyzerTester.class);

//    public UserAgentAnalyzerTester() {
//        super();
//        keepTests();
//    }

//    public UserAgentAnalyzerTester(String resourceString) {
//        this();
//        loadResources(resourceString);
//    }

    public static class KryoSerializer extends UserAgentAnalyzerDirect.KryoSerializer {
        public KryoSerializer(Kryo kryo, Class<?> type) {
            super(kryo, type);
        }
    }

    static class TestResult {
        String field;
        String expected;
        String actual;
        boolean isDefault;
        boolean pass;
        boolean warn;
        long confidence;
    }

    private void logInfo(StringBuilder errorMessageReceiver, String format, Object... args) {
        if (LOG.isInfoEnabled()) {
            final String message = MessageFormatter.arrayFormat(format, args).getMessage();
            LOG.info(message, args);
            if (errorMessageReceiver != null) {
                errorMessageReceiver.append(message).append('\n');
            }
        }
    }

    private void logWarn(StringBuilder errorMessageReceiver, String format, Object... args) {
        if (LOG.isWarnEnabled()) {
            final String message = MessageFormatter.arrayFormat(format, args).getMessage();
            LOG.warn(message, args);
            if (errorMessageReceiver != null) {
                errorMessageReceiver.append(message).append('\n');
            }
        }
    }

    private void logError(StringBuilder errorMessageReceiver, String format, Object... args) {
        if (LOG.isErrorEnabled()) {
            final String message = MessageFormatter.arrayFormat(format, args).getMessage();
            LOG.error(message, args);
            if (errorMessageReceiver != null) {
                errorMessageReceiver.append(message).append('\n');
            }
        }
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
        return runTests(showAll, failOnUnexpected, null, false, false);
    }

    public boolean runTests(boolean showAll,
                            boolean failOnUnexpected,
                            Collection<String> onlyValidateFieldNames,
                            boolean measureSpeed,
                            boolean showPassedTests) {
        return runTests(showAll, failOnUnexpected, onlyValidateFieldNames, measureSpeed, showPassedTests, null);
    }

    public boolean runTests(boolean showAll,
                            boolean failOnUnexpected,
                            Collection<String> onlyValidateFieldNames,
                            boolean measureSpeed,
                            boolean showPassedTests,
                            StringBuilder errorMessageReceiver) {
        initializeMatchers();
        if (getTestCases() == null) {
            return true;
        }
        DebugUserAgent agent = new DebugUserAgent(wantedFieldNames);

        List<TestResult> results = new ArrayList<>(32);

        String filenameHeader = "Test number and source";
        int filenameHeaderLength = filenameHeader.length();
        int maxFilenameLength = filenameHeaderLength;
        for (Map<String, Map<String, String>> test : getTestCases()) {
            Map<String, String> metaData = test.get("metaData");
            String filename = metaData.get("filename");
            maxFilenameLength = Math.max(maxFilenameLength, filename.length());
        }

        maxFilenameLength+=11;

        StringBuilder sb = new StringBuilder(1024);

        sb.append("| ").append(filenameHeader);
        for (int i = filenameHeaderLength; i < maxFilenameLength; i++) {
            sb.append(' ');
        }

        sb.append(" |S|AA|MF|");
        if (measureSpeed) {
            sb.append("  PPS| msPP|");
        }
        sb.append("--> S=Syntax Error, AA=Number of ambiguities during parse, MF=Matches Found");
        if (measureSpeed) {
            sb.append(", PPS=parses/sec, msPP=milliseconds per parse");
        }

        long fullStart = System.nanoTime();

        if (showPassedTests) {
            LOG.info("+===========================================================================================");
            LOG.info(sb.toString());
            LOG.info("+-------------------------------------------------------------------------------------------");
        }

        boolean allPass = true;
        int testcount = 0;
        for (Map<String, Map<String, String>> test : getTestCases()) {
            testcount++;
            Map<String, String> input = test.get("input");
            Map<String, String> expected = test.get("expected");

            List<String> options = null;
            if (test.containsKey("options")) {
                options = new ArrayList<>(test.get("options").keySet());
            }
            Map<String, String> metaData = test.get("metaData");
            String filename = metaData.get("filename");
            String linenumber = metaData.get("fileline");

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
                if (userAgentString.length() > 300) {
                    testName = userAgentString.substring(0, 290) + " ... ( " + userAgentString.length() + " chars)";
                } else {
                    testName = userAgentString;
                }
            }

            sb.setLength(0);

            sb.append("|").append(String.format("%5d", testcount))
              .append(".(").append(filename).append(':').append(linenumber).append(')');
            for (int i = filename.length()+linenumber.length()+7; i < maxFilenameLength; i++) {
                sb.append(' ');
            }

            agent.setUserAgentString(userAgentString);


            long measuredSpeed=-1;
            if (measureSpeed) {
                disableCaching();
                // Preheat
                for (int i = 0; i < 100; i++) {
                    parse(agent);
                }
                long startTime = System.nanoTime();
                for (int i = 0; i < 1000; i++) {
                    parse(agent);
                }
                long stopTime = System.nanoTime();
                measuredSpeed = (1000000000L*(1000))/(stopTime-startTime);
            } else {
                parse(agent);
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

            sb.append(String.format("|%2d", agent.getNumberOfAppliedMatches()));

            if (measureSpeed) {
                sb.append('|').append(String.format("%5d", measuredSpeed));
                sb.append('|').append(String.format("%5.2f", 1000.0/measuredSpeed));
            }

            sb.append("| ").append(testName);

            // We create the log line but we keep it until we know it actually must be output to the screen
            String testLogLine = sb.toString();

            sb.setLength(0);

            boolean pass = true;
            results.clear();

            if (init) {
                LOG.info(testLogLine);
                sb.append(agent.toYamlTestCase());
                LOG.info(sb.toString());
            } else {
                if (expected == null) {
                    LOG.info(testLogLine);
                    LOG.warn("| - No expectations ... ");
                    continue;
                }
            }

            int maxNameLength     = 6; // "Field".length()+1;            NOSONAR: This is not commented code.
            int maxActualLength   = 7; // "Actual".length()+1;           NOSONAR: This is not commented code.
            int maxExpectedLength = 9; // "Expected".length()+1;         NOSONAR: This is not commented code.

            if (expected != null) {
                List<String> fieldNames = agent.getAvailableFieldNamesSorted();

                if (onlyValidateFieldNames != null && onlyValidateFieldNames.isEmpty()) {
                    onlyValidateFieldNames = null;
                } else if (onlyValidateFieldNames != null) {
                    fieldNames.clear();
                    fieldNames.addAll(onlyValidateFieldNames);
                }

                for (String newFieldName: expected.keySet()) {
                    if (!fieldNames.contains(newFieldName)) {
                        fieldNames.add(newFieldName);
                    }
                }

                for (String fieldName : fieldNames) {
                    // Only check the desired fieldnames
                    if (onlyValidateFieldNames != null &&
                        !onlyValidateFieldNames.contains(fieldName)) {
                        continue;
                    }

                    TestResult result = new TestResult();
                    result.field = fieldName;
                    boolean expectedSomething;

                    // Actual value
                    result.actual = agent.getValue(result.field);
                    result.isDefault = agent.get(result.field).isDefaultValue();

                    result.confidence = agent.getConfidence(result.field);
                    if (result.actual == null) {
                        result.actual = NULL_VALUE;
                    }

                    // Expected value
                    String expectedValue = expected.get(fieldName);
                    if (expectedValue == null) {
                        expectedSomething = false;
                        if (result.isDefault) {
                            continue;
                        }
                        result.expected = "<<absent>>";
                    } else {
                        expectedSomething = true;
                        result.expected = expectedValue;
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
                                if (!SYNTAX_ERROR.equals(result.field)) {
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

                if (!agent.analyzeMatchersResult()) {
                    pass = false;
                    allPass = false;
                }
            }

            if (!init && pass && !showAll) {
                if (showPassedTests) {
                    logInfo(errorMessageReceiver, testLogLine);
                }
                continue;
            }

            if (!pass) {
                logInfo(errorMessageReceiver, testLogLine);
                logError(errorMessageReceiver, "| TEST FAILED !");
            }

            if (agent.hasAmbiguity()) {
                logInfo(errorMessageReceiver, "| Parsing problem: Ambiguity {} times. ", agent.getAmbiguityCount());
            }
            if (agent.hasSyntaxError()) {
                logInfo(errorMessageReceiver, "| Parsing problem: Syntax Error");
            }

            if (init || !pass) {
                sb.setLength(0);
                sb.append('\n');
                sb.append('\n');
                sb.append("- matcher:\n");
                sb.append("#    options:\n");
                sb.append("#    - 'verbose'\n");
                sb.append("    require:\n");
                for (String path : getAllPaths(userAgentString)) {
                    if (path.contains("=\"")) {
                        sb.append("#    - '").append(path).append("'\n");
                    }
                }
                sb.append("    extract:\n");
                sb.append("#    - 'DeviceClass                         :      1 :' \n");
                sb.append("#    - 'DeviceBrand                         :      1 :' \n");
                sb.append("#    - 'DeviceName                          :      1 :' \n");
                sb.append("#    - 'OperatingSystemClass                :      1 :' \n");
                sb.append("#    - 'OperatingSystemName                 :      1 :' \n");
                sb.append("#    - 'OperatingSystemVersion              :      1 :' \n");
                sb.append("#    - 'LayoutEngineClass                   :      1 :' \n");
                sb.append("#    - 'LayoutEngineName                    :      1 :' \n");
                sb.append("#    - 'LayoutEngineVersion                 :      1 :' \n");
                sb.append("#    - 'AgentClass                          :      1 :' \n");
                sb.append("#    - 'AgentName                           :      1 :' \n");
                sb.append("#    - 'AgentVersion                        :      1 :' \n");
                sb.append('\n');
                sb.append('\n');
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
            logInfo(errorMessageReceiver, separator);

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

            logInfo(errorMessageReceiver, sb.toString());

            logInfo(errorMessageReceiver, separator);

            Map<String, String> failComments = new HashMap<>();

            List<String> failedFieldNames = new ArrayList<>();
            for (TestResult result : results) {
                sb.setLength(0);
                if (result.pass) {
                    sb.append("|        | ");
                } else {
                    if (result.warn) {
                        sb.append("| ~warn~ | ");
                        failComments.put(result.field, "~~ Unexpected result ~~");
                    } else {
                        sb.append("| -FAIL- | ");
                        failComments.put(result.field, "FAILED; Should be '" + result.expected + "'");
                        failedFieldNames.add(result.field);
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
                    logInfo(errorMessageReceiver, sb.toString());
                } else {
                    sb.append(result.expected);
                    for (int i = result.expected.length(); i < maxExpectedLength; i++) {
                        sb.append(' ');
                    }
                    sb.append(" |");
                    if (result.warn) {
                        logWarn(errorMessageReceiver, sb.toString());
                    } else {
                        logError(errorMessageReceiver, sb.toString());
                    }
                }
            }

            logInfo(errorMessageReceiver, separator);
            logInfo(errorMessageReceiver, "");

            logInfo(errorMessageReceiver, agent.toMatchTrace(failedFieldNames));

            logInfo(errorMessageReceiver, "\n\nconfig:\n{}", agent.toYamlTestCase(!init, failComments));
            logInfo(errorMessageReceiver, "Location of failed test.({}:{})", filename, linenumber);
            if (!pass && !showAll) {
                return false;
            }
            if (init) {
                return allPass;
            }
        }

        if (showPassedTests) {
            LOG.info("+===========================================================================================");
        } else {
            LOG.info("All {} tests passed", testcount);
        }

        long fullStop = System.nanoTime();

        LOG.info("This took {} ms for {} tests : averaging to {} msec/test (This includes test validation and logging!!)",
            (fullStop - fullStart)/1000000,
            testcount,
            ((double)(fullStop - fullStart)) / (testcount * 1000000L));

        if (testcount == 0) {
            LOG.error("NO tests were run at all!!!");
            allPass = false;
        }

        return allPass;
    }

    // ===============================================================================================================

    /**
     * This function is used only for analyzing which patterns that could possibly be relevant
     * were actually relevant for the matcher actions.
     * @return The list of Matches that were possibly relevant.
     */
    public List<Match> getMatches() {
        List<Match> allMatches = new ArrayList<>(128);
        for (Matcher matcher: getAllMatchers()) {
            allMatches.addAll(matcher.getMatches());
        }
        return allMatches;
    }

    public List<Match> getUsedMatches(UserAgent userAgent) {
        // Reset all Matchers
        for (Matcher matcher : getAllMatchers()) {
            matcher.reset();
            matcher.setVerboseTemporarily(false);
        }

        flattener.parse(userAgent);

        List<Match> allMatches = new ArrayList<>(128);
        for (Matcher matcher: getAllMatchers()) {
            allMatches.addAll(matcher.getUsedMatches());
        }
        return allMatches;
    }

    public abstract static class AbstractUserAgentAnalyzerTesterBuilder<UAA extends AbstractUserAgentAnalyzerTester, B extends AbstractUserAgentAnalyzerBuilder<UAA, B>>
        extends AbstractUserAgentAnalyzerBuilder<UAA, B> {

        AbstractUserAgentAnalyzerTesterBuilder(UAA newUaa) {
            super(newUaa);
        }

        @SuppressWarnings("EmptyMethod") // We must override the method because of the generic return value.
        @Override
        public UAA build() {
            return super.build();
        }
    }


    @Override
    public String toString() {
        return "UserAgentAnalyzerTester{" + super.toString() + "} ";
    }
}
