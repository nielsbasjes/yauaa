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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.parse.MatcherTree;
import nl.basjes.parse.useragent.utils.YamlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;

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
import static nl.basjes.parse.useragent.analyze.Matcher.ConfigLine.Type.EXTRACT;
import static nl.basjes.parse.useragent.analyze.Matcher.ConfigLine.Type.REQUIRE;
import static nl.basjes.parse.useragent.analyze.Matcher.ConfigLine.Type.VARIABLE;
import static nl.basjes.parse.useragent.utils.YamlUtils.getKeyAsString;

public class Matcher implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(Matcher.class);

    private final Analyzer analyzer;
    private final List<MatcherVariableAction> variableActions;
    private List<MatcherAction> dynamicActions;
    private final List<MatcherAction> fixedStringActions;

    private MutableUserAgent newValuesUserAgent = null;

    private long actionsThatRequireInput;
    private boolean verbose;
    private boolean permanentVerbose;

    // Used for error reporting: The filename and line number where the config was located.
    private String matcherSourceLocation;
    public String getMatcherSourceLocation() {
        return matcherSourceLocation;
    }

    // Private constructor for serialization systems ONLY (like Kyro)
    private Matcher() {
        this.analyzer = null;
        this.fixedStringActions = new ArrayList<>();
        this.variableActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();
    }

    // Package private constructor for testing purposes only
    public Matcher(Analyzer analyzer) { // FIXME: NOT IN FINAL VERSION: Making this public is bad
        this.analyzer = analyzer;
        this.fixedStringActions = new ArrayList<>();
        this.variableActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();
    }

    public Map<String, Map<String, String>> getLookups() {
        return analyzer.getLookups();
    }

    public Map<String, Set<String>> getLookupSets() {
        return analyzer.getLookupSets();
    }

    public MatcherTree getPathTreeRoot() {
        return analyzer.getMatcherTreeRoot();
    }

    static class ConfigLine {
        public enum Type {
            VARIABLE,
            REQUIRE,
            EXTRACT
        }
        final Type type;
        final String attribute;
        final Long confidence;
        final String expression;

        ConfigLine(Type type, String attribute, Long confidence, String expression) {
            this.type = type;
            this.attribute = attribute;
            this.confidence = confidence;
            this.expression = expression;
        }
    }

    public Matcher(Analyzer analyzer,
                   Collection<String> wantedFieldNames,
                   MappingNode matcherConfig,
                   String filename) throws UselessMatcherException {
        this.analyzer = analyzer;
        this.fixedStringActions = new ArrayList<>();
        this.variableActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();
        this.newValuesUserAgent = new MutableUserAgent(wantedFieldNames);

        matcherSourceLocation = filename + ':' + matcherConfig.getStartMark().getLine();

        verbose = false;

        boolean hasActiveExtractConfigs = false;
        boolean hasDefinedExtractConfigs = false;

        // List of 'attribute', 'confidence', 'expression'
        List<ConfigLine> configLines = new ArrayList<>(16);
        for (NodeTuple nodeTuple: matcherConfig.getValue()) {
            String name = getKeyAsString(nodeTuple, matcherSourceLocation);
            switch (name) {
                case "options":
                    List<String> options = YamlUtils.getStringValues(nodeTuple.getValueNode(), matcherSourceLocation);
                    verbose = options.contains("verbose");
                    break;
                case "variable":
                    for (String variableConfig : YamlUtils.getStringValues(nodeTuple.getValueNode(), matcherSourceLocation)) {
                        String[] configParts = variableConfig.split(":", 2);

                        if (configParts.length != 2) {
                            throw new InvalidParserConfigurationException("Invalid variable config line: " + variableConfig);
                        }
                        String variableName = configParts[0].trim();
                        String config = configParts[1].trim();

                        configLines.add(new ConfigLine(VARIABLE, variableName, null, config));
                    }
                    break;
                case "require":
                    for (String requireConfig : YamlUtils.getStringValues(nodeTuple.getValueNode(), matcherSourceLocation)) {
                        configLines.add(new ConfigLine(REQUIRE, null, null, requireConfig));
                    }
                    break;
                case "extract":
                    for (String extractConfig : YamlUtils.getStringValues(nodeTuple.getValueNode(), matcherSourceLocation)) {
                        String[] configParts = extractConfig.split(":", 3);

                        if (configParts.length != 3) {
                            throw new InvalidParserConfigurationException("Invalid extract config line: " + extractConfig);
                        }
                        String attribute = configParts[0].trim();
                        Long confidence = Long.parseLong(configParts[1].trim());
                        String config = configParts[2].trim();

                        hasDefinedExtractConfigs = true;
                        // If we have a restriction on the wanted fields we check if this one is needed at all
                        if (wantedFieldNames == null || wantedFieldNames.contains(attribute)) {
                            configLines.add(new ConfigLine(EXTRACT, attribute, confidence, config));
                            hasActiveExtractConfigs = true;
                        } else {
                            configLines.add(new ConfigLine(REQUIRE, null, null, config));
                        }
                    }
                    break;
                default:
                    // Ignore
            }
        }

        permanentVerbose = verbose;

        if (verbose) {
            LOG.info("---------------------------");
            LOG.info("- MATCHER -");
        }

        if (!hasDefinedExtractConfigs) {
            throw new InvalidParserConfigurationException("Matcher does not extract anything:" + matcherSourceLocation);
        }

        if (!hasActiveExtractConfigs) {
            throw new UselessMatcherException("Does not extract any wanted fields" + matcherSourceLocation);
        }

        for (ConfigLine configLine : configLines) {
            if (verbose) {
                LOG.info("{}: {}", configLine.type, configLine.expression);
            }
            switch (configLine.type) {
                case VARIABLE:
                    variableActions.add(new MatcherVariableAction(configLine.attribute, configLine.expression, this));
                    break;
                case REQUIRE:
                    dynamicActions.add(new MatcherRequireAction(configLine.expression, this));
                    break;
                case EXTRACT:
                    MatcherExtractAction action =
                        new MatcherExtractAction(configLine.attribute, configLine.confidence, configLine.expression, this);
                    dynamicActions.add(action);

                    // Make sure the field actually exists
                    newValuesUserAgent.set(configLine.attribute, "Dummy", -9999);
                    action.setResultAgentField((AgentField.MutableAgentField) newValuesUserAgent.get(configLine.attribute));
                    break;
                default:
                    break;
            }
        }

    }

    public void initialize(MatcherTree treeRoot) {
        long newEntries = 0;
        long initStart = System.nanoTime();
        try {
            for (MatcherVariableAction variableAction : variableActions) {
                newEntries += variableAction.initialize(treeRoot);
            }
        } catch (InvalidParserConfigurationException e) {
            throw new InvalidParserConfigurationException("Syntax error.(" + matcherSourceLocation + ")", e);
        }

        Set<MatcherAction> uselessRequireActions = new HashSet<>();
        for (MatcherAction dynamicAction : dynamicActions) {
            try {
                newEntries += dynamicAction.initialize(treeRoot);
            } catch (InvalidParserConfigurationException e) {
                if (!e.getMessage().startsWith("It is useless to put a fixed value")) {// Ignore fixed values in require
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

//    public void lookingForRange(String treeName, WordRangeVisitor.Range range) {
//        analyzer.lookingForRange(treeName, range);
//    }

    public void informMeAbout(MatcherAction matcherAction, MatcherTree matcherTree) {
        analyzer.informMeAbout(matcherAction, matcherTree);
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

    private long actionsThatRequireInputAndReceivedInput = 0;
    void gotMyFirstStartingPoint() {
        actionsThatRequireInputAndReceivedInput++;
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
        sb.append("    EXTRACT:\n");
        for (MatcherAction action : dynamicActions) {
            if (action instanceof MatcherExtractAction) {
                sb.append("        ").append(action.toString()).append('\n');
                if (action.getMatches() != null) {
                    sb.append("        -->").append(action.getMatches()).append('\n');
                }
            }
        }
        for (MatcherAction action : fixedStringActions) {
            sb.append("        ").append(action.toString()).append('\n');
        }
        return sb.toString();
    }
}
