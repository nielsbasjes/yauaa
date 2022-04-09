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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.config.MatcherConfig;
import nl.basjes.parse.useragent.config.MatcherConfig.ConfigLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;

public class Matcher implements Serializable {
    private static final Logger LOG = LogManager.getLogger(Matcher.class);

    private final MatchMaker analyzer;
    private final List<MatcherVariableAction> variableActions;
    private final List<MatcherAction> dynamicActions;
    private final List<MatcherAction> fixedStringActions;

    private MutableUserAgent newValuesUserAgent = null;

    private long actionsThatRequireInput;
    private boolean verbose;
    private boolean permanentVerbose;

    public String getMatcherSourceLocation() {
        return matcherSourceLocation;
    }

    private String sourceFileName;
    private Integer sourceFileLineNumber;
    public String getSourceFileName() {
        return sourceFileName;
    }
    public Integer getSourceFileLineNumber() {
        return sourceFileLineNumber;
    }

    // Used for error reporting: The filename and line number where the config was located.
    private String matcherSourceLocation;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private Matcher() {
        this.analyzer = null;
        this.fixedStringActions = new ArrayList<>();
        this.variableActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();
    }

    // Package private constructor for testing purposes only
    Matcher(MatchMaker analyzer) {
        this.analyzer = analyzer;
        this.fixedStringActions = new ArrayList<>();
        this.variableActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();
    }

    public void destroy() {
        dynamicActions.forEach(MatcherAction::destroy);
        dynamicActions.clear();

        fixedStringActions.forEach(MatcherAction::destroy);
        fixedStringActions.clear();
    }

    public Map<String, Map<String, String>> getLookups() {
        return analyzer.getLookups();
    }

    public Map<String, Set<String>> getLookupSets() {
        return analyzer.getLookupSets();
    }

    public Matcher(MatchMaker analyzer,
                   Collection<String> wantedFieldNames,
                   MatcherConfig matcherConfig) throws UselessMatcherException {
        this.analyzer = analyzer;
        this.fixedStringActions = new ArrayList<>();
        this.variableActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();
        this.newValuesUserAgent = new MutableUserAgent(wantedFieldNames);

        sourceFileName = matcherConfig.getMatcherSourceFilename();
        sourceFileLineNumber = matcherConfig.getMatcherSourceLineNumber();
        matcherSourceLocation = sourceFileName + ':' + sourceFileLineNumber;

        verbose = false;
        List<String> options = matcherConfig.getOptions();
        if (options != null && options.contains("verbose")) {
            verbose = true;
            permanentVerbose = true;
        }

        boolean hasActiveExtractConfigs = false;
        boolean hasDefinedExtractConfigs = false;

        if (verbose) {
            LOG.info("---------------------------");
            LOG.info("- MATCHER -");
        }

        for (ConfigLine configLine : matcherConfig.getConfigLines()) {
            if (verbose) {
                LOG.info("{}: {}", configLine.getType(), configLine.getExpression());
            }
            switch (configLine.getType()) {
                case VARIABLE:
                    variableActions.add(new MatcherVariableAction(configLine.getAttribute(), configLine.getExpression(), this));
                    break;
                case REQUIRE:
                    dynamicActions.add(new MatcherRequireAction(configLine.getExpression(), this));
                    break;
                case FAIL_IF_FOUND:
                    dynamicActions.add(new MatcherFailIfFoundAction(configLine.getExpression(), this));
                    break;
                case EXTRACT:
                    hasDefinedExtractConfigs = true;
                    // If we have a restriction on the wanted fields we check if this one is needed at all
                    if (wantedFieldNames == null || wantedFieldNames.contains(configLine.getAttribute())) {
                        MatcherExtractAction action =
                            new MatcherExtractAction(configLine.getAttribute(), configLine.getConfidence(), configLine.getExpression(), this);
                        dynamicActions.add(action);

                        // Make sure the field actually exists
                        newValuesUserAgent.set(configLine.getAttribute(), "Dummy", -9999);
                        action.setResultAgentField(newValuesUserAgent.get(configLine.getAttribute()));

                        hasActiveExtractConfigs = true;
                    } else {
                        dynamicActions.add(new MatcherDemotedExtractAction(configLine.getExpression(), this));
                    }
                    break;
                default:
                    break;
            }
        }

        if (!hasDefinedExtractConfigs) {
            throw new InvalidParserConfigurationException("Matcher does not extract anything:" + matcherSourceLocation);
        }

        if (!hasActiveExtractConfigs) {
            throw new UselessMatcherException("Does not extract any wanted fields" + matcherSourceLocation);
        }

    }

    public static class MatcherDemotedExtractAction extends MatcherRequireAction {
        @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
        private MatcherDemotedExtractAction() {
            super();
        }

        MatcherDemotedExtractAction(String config, Matcher matcher) {
            super(config, matcher);
        }
    }

    public void initialize() {
        long newEntries = 0;
        long initStart = System.nanoTime();
        try {
            for (MatcherVariableAction variableAction : variableActions) {
                newEntries += variableAction.initialize();
            }
        } catch (InvalidParserConfigurationException e) {
            throw new InvalidParserConfigurationException("Syntax error.(" + matcherSourceLocation + ")", e);
        }

        Set<MatcherAction> uselessRequireActions = new HashSet<>();
        for (MatcherAction dynamicAction : dynamicActions) {
            try {
                newEntries += dynamicAction.initialize();
            } catch (InvalidParserConfigurationException e) {
                // Ignore fixed values in require IFF this was caused by moving a fixed value extract
                // to a require when asking for limited set of values.
                if (!(dynamicAction instanceof MatcherDemotedExtractAction) ||
                    !e.getMessage().startsWith("It is useless to put a fixed value")) {
                    throw new InvalidParserConfigurationException("Syntax error.(" + matcherSourceLocation + ")" + e.getMessage(), e);
                }
                uselessRequireActions.add(dynamicAction);
            }
        }

        for (MatcherAction action: dynamicActions) {
            if (action instanceof MatcherExtractAction) {
                if (((MatcherExtractAction)action).isFixedValue()) {
                    fixedStringActions.add(action);
                    action.obtainResult();
                }
            }
        }

        fixedStringActions.forEach(dynamicActions::remove);
        uselessRequireActions.forEach(dynamicActions::remove);

        // Verify that a variable only contains the variables that have been defined BEFORE it (also not referencing itself).
        // If all is ok we link them
        Set<MatcherAction> seenVariables = new HashSet<>(variableActions.size());
        for (MatcherVariableAction variableAction: variableActions) {
            seenVariables.add(variableAction); // Add myself
            Set<MatcherAction> interestedActions = informMatcherActionsAboutVariables.get(variableAction.getVariableName());
            if (interestedActions != null && !interestedActions.isEmpty()) {
                variableAction.setInterestedActions(interestedActions);
                for (MatcherAction interestedAction : interestedActions) {
                    if (seenVariables.contains(interestedAction)) {
                        throw new InvalidParserConfigurationException(
                            "Syntax error (" + matcherSourceLocation + "): The line >>" + interestedAction + "<< " +
                            "is referencing variable @"+variableAction.getVariableName()+ " which is not defined yet.");
                    }
                }
            }
        }

        // Check if any variable was requested that was not defined.
        Set<String> missingVariableNames = new HashSet<>();
        Set<String> seenVariableNames = new HashSet<>();
        seenVariables.forEach(m -> seenVariableNames.add(((MatcherVariableAction)m).getVariableName()));
        for (String variableName: informMatcherActionsAboutVariables.keySet()) {
            if (!seenVariableNames.contains(variableName)) {
                missingVariableNames.add(variableName);
            }
        }
        if (!missingVariableNames.isEmpty()) {
            throw new InvalidParserConfigurationException(
                "Syntax error (" + matcherSourceLocation + "): Used, yet undefined variables: " + missingVariableNames);
        }

        // Make sure the variable actions are BEFORE the rest in the list
        dynamicActions.addAll(0, variableActions);

        actionsThatRequireInput = countActionsThatMustHaveMatches(dynamicActions);

        long initFinish = System.nanoTime();
        if (newEntries > 3000) {
            LOG.warn("Large matcher: {} in {} ms:.({})", newEntries, (initFinish-initStart)/1000000, matcherSourceLocation);
        }

        if (verbose) {
            LOG.info("---------------------------");
        }

    }

    private long countActionsThatMustHaveMatches(List<? extends MatcherAction> actions) {
        long actionsThatMustHaveMatches = 0;
        for (MatcherAction action : actions) {
            // If an action exists which without any data can be valid, then we must force the evaluation
            action.reset();
            if (action.mustHaveMatches()) {
                actionsThatMustHaveMatches++;
            }
        }
        return actionsThatMustHaveMatches;
    }


    public Set<String> getAllPossibleFieldNames() {
        Set<String> results = new TreeSet<>();
        results.addAll(getAllPossibleFieldNames(dynamicActions));
        results.addAll(getAllPossibleFieldNames(fixedStringActions));
        results.remove(SET_ALL_FIELDS);
        return results;
    }

    private Set<String> getAllPossibleFieldNames(List<MatcherAction> actions) {
        Set<String> results = new TreeSet<>();
        for (MatcherAction action: actions) {
            if (action instanceof MatcherExtractAction) {
                MatcherExtractAction extractAction = (MatcherExtractAction)action;
                results.add(extractAction.getAttribute());
            }
        }
        return results;
    }

    public void lookingForRange(String treeName, WordRangeVisitor.Range range) {
        analyzer.lookingForRange(treeName, range);
    }

    public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
        analyzer.informMeAbout(matcherAction, keyPattern);
    }

    public void informMeAboutPrefix(MatcherAction matcherAction, String keyPattern, String prefix) {
        analyzer.informMeAboutPrefix(matcherAction, keyPattern, prefix);
    }

    private final Map<String, Set<MatcherAction>> informMatcherActionsAboutVariables = new HashMap<>(8);

    void informMeAboutVariable(MatcherAction matcherAction, String variableName) {
        Set<MatcherAction> analyzerSet = informMatcherActionsAboutVariables
            .computeIfAbsent(variableName, k -> new LinkedHashSet<>());
        analyzerSet.add(matcherAction);
    }

    /**
     * Fires all matcher actions.
     * IFF all success then we tell the userAgent
     *
     * @param userAgent The useragent that needs to analyzed
     */
    public void analyze(MutableUserAgent userAgent) {

        if (verbose) {
            LOG.info("");
            LOG.info("--- Matcher.({}) ------------------------", matcherSourceLocation);
            LOG.info("ANALYSE ----------------------------");
            boolean good = true;
            for (MatcherAction action : dynamicActions) {
                if (action.cannotBeValid()) {
                    LOG.error("CANNOT BE VALID : {}", action.getMatchExpression());
                    good = false;
                }
            }
            for (MatcherAction action : dynamicActions) {
                if (!action.obtainResult()) {
                    LOG.error("FAILED : {}", action.getMatchExpression());
                    good = false;
                }
            }
            if (good) {
                LOG.info("COMPLETE ----------------------------");
            } else {
                LOG.info("INCOMPLETE ----------------------------");
                return;
            }
        } else {
            if (actionsThatRequireInput != actionsThatRequireInputAndReceivedInput) {
                return;
            }
            for (MatcherAction action : dynamicActions) {
                if (action.obtainResult()) {
                    continue;
                }
                return; // If one of them is bad we skip the rest
            }
        }
        userAgent.set(newValuesUserAgent, this);
    }

    public boolean getVerbose() {
        return verbose;
    }

    private boolean alreadyNotifiedAnalyzerWeReceivedInput = false;
    void receivedInput() {
        if (alreadyNotifiedAnalyzerWeReceivedInput) {
            return;
        }
        analyzer.receivedInput(this);
        alreadyNotifiedAnalyzerWeReceivedInput = true;
    }

    public long getActionsThatRequireInput() {
        return actionsThatRequireInput;
    }

    public long getActionsThatRequireInputAndReceivedInput() {
        return actionsThatRequireInputAndReceivedInput;
    }

    private long actionsThatRequireInputAndReceivedInput = 0;
    void gotMyFirstStartingPoint() {
        actionsThatRequireInputAndReceivedInput++;
    }

    protected void failImmediately() {
        actionsThatRequireInputAndReceivedInput = Long.MIN_VALUE; // So it will never match the expected
    }

    public void setVerboseTemporarily(boolean newVerbose) {
        for (MatcherAction action : dynamicActions) {
            action.setVerbose(newVerbose, true);
        }
    }

    public void reset() {
        // If there are no dynamic actions we have fixed strings only
        alreadyNotifiedAnalyzerWeReceivedInput = false;
        actionsThatRequireInputAndReceivedInput = 0;
        verbose = permanentVerbose;
        for (MatcherAction action : dynamicActions) {
            action.reset();
        }
    }

    public List<MatchesList.Match> getMatches() {
        List<MatchesList.Match> allMatches = new ArrayList<>(128);
        for (MatcherAction action : dynamicActions) {
            allMatches.addAll(action.getMatches());
        }
        return allMatches;
    }

    public List<MatchesList.Match> getUsedMatches() {
        List<MatchesList.Match> allMatches = new ArrayList<>(128);
        for (MatcherAction action : dynamicActions) {
            if (action.cannotBeValid()) {
                return new ArrayList<>(); // There is NO way one of them is valid
            }
        }
        for (MatcherAction action : dynamicActions) {
            if (!action.obtainResult()) {
                return new ArrayList<>(); // There is NO way one of them is valid
            } else {
                allMatches.addAll(action.getMatches());
            }
        }
        return allMatches;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("MATCHER.(").append(matcherSourceLocation).append("):\n")
          .append("    VARIABLE:\n");
        for (MatcherAction action : dynamicActions) {
            if (action instanceof MatcherVariableAction) {
                sb.append("        @").append(((MatcherVariableAction) action).getVariableName())
                    .append(":    ").append(action.getMatchExpression()).append('\n');
                sb.append("        -->").append(action.getMatches().toStrings()).append('\n');
            }
        }
        sb.append("    REQUIRE:\n");
        for (MatcherAction action : dynamicActions) {
            if (action instanceof MatcherRequireAction) {
                sb.append("        ").append(action.getMatchExpression()).append('\n');
                if (action.getMatches() != null) {
                    sb.append("        -->").append(action.getMatches().toStrings()).append('\n');
                }
            }
        }
        sb.append("    FAIL_IF_FOUND:\n");
        for (MatcherAction action : dynamicActions) {
            if (action instanceof MatcherFailIfFoundAction) {
                sb.append("        ").append(action.getMatchExpression()).append('\n');
                if (action.getMatches() != null) {
                    sb.append("        -->").append(action.getMatches().toStrings()).append('\n');
                }
            }
        }
        sb.append("    EXTRACT:\n");
        for (MatcherAction action : dynamicActions) {
            if (action instanceof MatcherExtractAction) {
                sb.append("        ").append(action).append('\n');
                if (action.getMatches() != null) {
                    sb.append("        -->").append(action.getMatches()).append('\n');
                }
            }
        }
        for (MatcherAction action : fixedStringActions) {
            sb.append("        ").append(action).append('\n');
        }
        return sb.toString();
    }
}
