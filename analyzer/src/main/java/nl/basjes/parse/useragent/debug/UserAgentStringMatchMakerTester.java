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

package nl.basjes.parse.useragent.debug;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer;
import nl.basjes.parse.useragent.AbstractUserAgentAnalyzerDirect;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.config.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.ReusableMessageFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.debug.GetAllPaths.getAllPaths;

@SuppressWarnings("PlaceholderCountMatchesArgumentCount")
@DefaultSerializer(UserAgentStringMatchMakerTester.KryoSerializer.class)
public class UserAgentStringMatchMakerTester extends AbstractUserAgentAnalyzer {
    private static final Logger LOG = LogManager.getFormatterLogger(UserAgentStringMatchMakerTester.class);

    /**
     * This is used to configure the provided Kryo instance if Kryo serialization is desired.
     * The expected type here is Object because otherwise the Kryo library becomes
     * a mandatory dependency on any project that uses Yauaa.
     * @param kryoInstance The instance of com.esotericsoftware.kryo.Kryo that needs to be configured.
     */
    public static void configureKryo(Object kryoInstance) {
        Kryo kryo = (Kryo) kryoInstance;
        kryo.register(UserAgentStringMatchMakerTester.class);
        AbstractUserAgentAnalyzer.configureKryo(kryo);
    }

    public static class KryoSerializer extends AbstractUserAgentAnalyzerDirect.KryoSerializer {
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

    private static final MessageFactory MESSAGE_FACTORY = new ReusableMessageFactory();

    private static String determineLogMessage(String format, Object... args){
        if (args == null || args.length == 0) {
            return format;
        }
        return MESSAGE_FACTORY.newMessage(format, args).getFormattedMessage();
    }

    private static void logInfo(StringBuilder errorMessageReceiver, String format, Object... args) {
        if (LOG.isInfoEnabled()) {
            final String message = determineLogMessage(format, args);
            LOG.info(message);
            if (errorMessageReceiver != null) {
                errorMessageReceiver.append(message).append('\n');
            }
        }
    }

    private static void logWarn(StringBuilder errorMessageReceiver, String format, Object... args) {
        if (LOG.isWarnEnabled()) {
            final String message = determineLogMessage(format, args);
            LOG.warn(message);
            if (errorMessageReceiver != null) {
                errorMessageReceiver.append(message).append('\n');
            }
        }
    }

    private static void logError(StringBuilder errorMessageReceiver, String format, Object... args) {
        if (LOG.isErrorEnabled()) {
            final String message = determineLogMessage(format, args);
            LOG.error(message);
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
        return runTests(this, showAll, failOnUnexpected, onlyValidateFieldNames, measureSpeed, showPassedTests, null);
    }

    public static boolean runTests(
                            AbstractUserAgentAnalyzerDirect analyzer,
                            boolean showAll,
                            boolean failOnUnexpected,
                            Collection<String> onlyValidateFieldNames,
                            boolean measureSpeed,
                            boolean showPassedTests,
                            StringBuilder errorMessageReceiver) {
        analyzer.initializeMatchers();
        if (analyzer.getTestCases() == null) {
            return true;
        }
        DebugUserAgent agent = new DebugUserAgent(analyzer.getWantedFieldNames());

        List<TestResult> results = new ArrayList<>(32);

        String filenameHeader = "Test number and source";
        int filenameHeaderLength = filenameHeader.length();
        int maxFilenameLength = filenameHeaderLength;
        for (TestCase test : analyzer.getTestCases()) {
            Map<String, String> metaData = test.getMetadata();
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
            LOG.info("%s", sb);
            LOG.info("+-------------------------------------------------------------------------------------------");
        }

        // If there are tests without any expectations then we ONLY run the first one of those.
        List<TestCase> testCases = new ArrayList<>();
        for (TestCase test : analyzer.getTestCases()) {
            if (test.getExpected().isEmpty()) {
                testCases.clear();
                testCases.add(test);
                break;
            }
            testCases.add(test);
        }

        boolean allPass = true;
        int testcount = 0;
        for (TestCase test : testCases) {
            testcount++;
            String testName = test.getTestName();
            String userAgentString = test.getUserAgent();
            Map<String, String> expected = test.getExpected();

            List<String> options = test.getOptions();
            Map<String, String> metaData = test.getMetadata();
            String filename = metaData.get("filename");
            String linenumber = metaData.get("fileline");

            boolean init = false;

            if (options == null) {
                analyzer.setVerbose(false);
                agent.setDebug(false);
            } else {
                boolean newVerbose = options.contains("verbose");
                analyzer.setVerbose(newVerbose);
                agent.setDebug(newVerbose);
                init = options.contains("init");
            }
            if (expected == null || expected.size() == 0) {
                init = true;
                expected = null;
            }

            if (testName == null) {
                if (userAgentString.length() > 200) {
                    testName = userAgentString.substring(0, 190) + " ... ( " + userAgentString.length() + " chars)";
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

            agent.setHeaders(test.getHeaders());

            UserAgent parseResult = null;
            long      measuredSpeed=-1;
            if (measureSpeed) {
                // Preheat
                for (int i = 0; i < 100; i++) {
                    analyzer.parse(agent);
                }
                long startTime = System.nanoTime();
                for (int i = 0; i < 1000; i++) {
                    parseResult = analyzer.parse(agent);
                }
                long stopTime = System.nanoTime();
                measuredSpeed = (1000000000L * (1000)) / (stopTime - startTime);
            } else {
                parseResult = analyzer.parse(agent);
            }

            sb.append('|');
            if (parseResult.hasSyntaxError()) {
                sb.append('S');
            } else {
                sb.append(' ');
            }
            if (parseResult.hasAmbiguity()) {
                sb.append(String.format("|%2d", parseResult.getAmbiguityCount()));
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
                LOG.info("%s", sb);
            }

            int maxNameLength     = 6; // "Field".length()+1;            NOSONAR: This is not commented code.
            int maxActualLength   = 7; // "Actual".length()+1;           NOSONAR: This is not commented code.
            int maxExpectedLength = 9; // "Expected".length()+1;         NOSONAR: This is not commented code.

            if (expected != null) {
                List<String> fieldNames = new ArrayList<>(parseResult.getAvailableFieldNamesSorted());

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
                    result.actual = parseResult.getValue(result.field);
                    result.isDefault = parseResult.get(result.field).isDefaultValue();

                    result.confidence = parseResult.getConfidence(result.field);
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

            if (parseResult.hasAmbiguity()) {
                logInfo(errorMessageReceiver, "| Parsing problem: Ambiguity {} times. ", parseResult.getAmbiguityCount());
            }
            if (parseResult.hasSyntaxError()) {
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
                LOG.info("%s", sb);
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
            sb.append("-+---------+------------+-");
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
            sb.append(" | Default | Confidence | Expected ");
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

                if (result.isDefault) {
                    sb.append(" | Default | ");
                } else {
                    sb.append(" |         | ");
                }
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

            logInfo(errorMessageReceiver, "\n\nconfig:\n{}", parseResult.toYamlTestCase(!init, failComments));
            if (init) {
                logInfo(errorMessageReceiver, "Location of the new test.({}:{})", filename, linenumber);
                return allPass;
            }
            logInfo(errorMessageReceiver, "Location of failed test.({}:{})", filename, linenumber);
            if (!pass && !showAll) {
                return false;
            }
        }

        if (showPassedTests) {
            LOG.info("+===========================================================================================");
        } else {
            LOG.info("All %d tests passed", testcount);
        }

        long fullStop = System.nanoTime();

        LOG.info("This took %8.3f ms for %5d tests : averaging to %6.3f msec/test (This includes test validation and logging!!)",
            (fullStop - fullStart)/1000000.0,
            testcount,
            ((double)(fullStop - fullStart)) / (testcount * 1000000L));

        if (testcount == 0) {
            LOG.error("NO tests were run at all!!!");
            allPass = false;
        }

        return allPass;
    }

    // ===============================================================================================================


    private static class MatcherImpact {
        String name;
        long neededInputs;
        long tests;
        long touched;
        long enoughInputs;
        long used;

        @Override
        public String toString() {
            return String.format(
                "%-45s --> touched= %5d (%3.0f%%), neededInputs = %2d, enoughInputs = %5d (%3.0f%%), used = %5d (%3.0f%%) %s%s%s",
                "Rule.(" + name + ")",
                touched,      100.0 * ((double)touched/tests),
                neededInputs,
                enoughInputs, touched      == 0 ? 0.0 : 100.0 * ((double)enoughInputs/touched),
                used,         enoughInputs == 0 ? 0.0 : 100.0 * ((double) used /enoughInputs),
                touched      ==  0              ? "~~~"               : "",
                enoughInputs >   0 && used == 0 ? "<-- NEVER USED "   : "",
                enoughInputs > 100 && used == 0 ? ">> SEVERE CASE <<" : "");
        }
    }

    public void analyzeMatcherImpactAllTests() {
        if (getTestCases() == null) {
            return;
        }
        initializeMatchers();
        DebugUserAgent agent = new DebugUserAgent(getWantedFieldNames());
        setVerbose(false);
        agent.setDebug(false);

        Map<String, MatcherImpact> impactOverview = new TreeMap<>();
        List<MatcherImpact> impactList = new ArrayList<>();
        getAllMatchers()
            .stream()
            .sorted(Comparator.comparing(Matcher::getSourceFileName).thenComparingLong(Matcher::getSourceFileLineNumber))
            .forEach(matcher -> {
                MatcherImpact matcherImpact = new MatcherImpact();
                matcherImpact.neededInputs = matcher.getActionsThatRequireInput();
                matcherImpact.name = matcher.getMatcherSourceLocation();
                impactOverview.put(matcher.getMatcherSourceLocation(), matcherImpact);
                impactList.add(matcherImpact);
            });

        for (TestCase test : getTestCases()) {
            String userAgentString = test.getUserAgent();

            agent.setUserAgentString(userAgentString);

            parse(agent);

            impactOverview.forEach((n, i) -> i.tests++);

            getTouchedMatchers().forEach(m -> {
                MatcherImpact impact = impactOverview.get(m.getMatcherSourceLocation());
                impact.touched++;
                if (m.getActionsThatRequireInput() == m.getActionsThatRequireInputAndReceivedInput()) {
                    impact.enoughInputs++;
                    if (!m.getUsedMatches().isEmpty()) {
                        impact.used++;
                    }
                }
            });
        }

        impactList
//            .stream().filter(mi -> mi.neededInputs > 2)
            .forEach(i -> LOG.info("%s", i));
    }

    public abstract static class AbstractUserAgentAnalyzerTesterBuilder<UAA extends UserAgentStringMatchMakerTester, B extends AbstractUserAgentAnalyzerBuilder<UAA, B>>
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
        return "UserAgentAnalyzerTester {\n" + super.toString() + "\n} ";
    }
}
