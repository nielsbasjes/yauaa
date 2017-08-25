/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.utils.YamlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;
import static nl.basjes.parse.useragent.utils.YamlUtils.getKeyAsString;

public class Matcher implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(Matcher.class);

    private final Analyzer analyzer;
    private final List<MatcherAction> dynamicActions;
    private final List<MatcherAction> fixedStringActions;

    private UserAgent newValuesUserAgent = new UserAgent();

    private long actionsThatRequireInput;
    final Map<String, Map<String, String>> lookups;
    private boolean verbose;
    private boolean permanentVerbose;

    // Used for error reporting: The filename where the config was located.
    private String filename;

    // Package private constructor for testing purposes only
    Matcher(Analyzer analyzer, Map<String, Map<String, String>> lookups) {
        this.lookups = lookups;
        this.analyzer = analyzer;
        this.fixedStringActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();
    }

    private static class ConfigLine {
        String attribute;
        Long confidence;
        String expression;

        ConfigLine(String attribute, Long confidence, String expression) {
            this.attribute = attribute;
            this.confidence = confidence;
            this.expression = expression;
        }
    }

    public Matcher(Analyzer analyzer,
                   Map<String, Map<String, String>> lookups,
                   List<String> wantedFieldNames,
                   MappingNode matcherConfig,
                   String filename) throws UselessMatcherException {
        this.lookups = lookups;
        this.analyzer = analyzer;
        this.fixedStringActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();

        this.filename = filename + ':' + matcherConfig.getStartMark().getLine();

        verbose = false;

        boolean hasActiveExtractConfigs = false;
        boolean hasDefinedExtractConfigs = false;

        // List of 'attribute', 'confidence', 'expression'
        List<ConfigLine> configLines = new ArrayList<>(16);
        for (NodeTuple nodeTuple: matcherConfig.getValue()) {
            String name = getKeyAsString(nodeTuple, filename);
            switch (name) {
                case "options":
                    List<String> options = YamlUtils.getStringValues(nodeTuple.getValueNode(), filename);
                    if (options != null) {
                        verbose = options.contains("verbose");
                    }
                    break;
                case "require":
                    for (String requireConfig : YamlUtils.getStringValues(nodeTuple.getValueNode(), filename)) {
                        configLines.add(new ConfigLine(null, null, requireConfig));
                    }
                    break;
                case "extract":
                    for (String extractConfig : YamlUtils.getStringValues(nodeTuple.getValueNode(), filename)) {
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
                            configLines.add(new ConfigLine(attribute, confidence, config));
                            hasActiveExtractConfigs = true;
                        } else {
                            configLines.add(new ConfigLine(null, null, config));
                        }
                    }
                    break;
                default:
                    // Ignore
//                    fail(nodeTuple.getKeyNode(), filename, "Unexpected " + name);
            }
        }

        permanentVerbose = verbose;

        if (verbose) {
            LOG.info("---------------------------");
            LOG.info("- MATCHER -");
        }

        if (!hasDefinedExtractConfigs) {
            throw new InvalidParserConfigurationException("Matcher does not extract anything");
        }

        if (!hasActiveExtractConfigs) {
            throw new UselessMatcherException("Does not extract any wanted fields");
        }

        for (ConfigLine configLine : configLines) {
            if (configLine.attribute == null) {
                // Require
                if (verbose) {
                    LOG.info("REQUIRE: {}", configLine.expression);
                }
                try {
                    dynamicActions.add(new MatcherRequireAction(configLine.expression, this));
                } catch (InvalidParserConfigurationException e) {
                    if (!e.getMessage().startsWith("It is useless to put a fixed value")) {// Ignore fixed values in require
                        throw e;
                    }
                }
            } else {
                // Extract
                if (verbose) {
                    LOG.info("EXTRACT: {}", configLine.expression);
                }
                MatcherExtractAction action =
                    new MatcherExtractAction(configLine.attribute, configLine.confidence, configLine.expression, this);

                // Make sure the field actually exists
                newValuesUserAgent.set(configLine.attribute, "Dummy", -9999);
                action.setResultAgentField(newValuesUserAgent.get(configLine.attribute));

                if (action.isFixedValue()) {
                    fixedStringActions.add(action);
                    action.obtainResult();
                } else {
                    dynamicActions.add(action);
                }
            }
        }

        actionsThatRequireInput = 0;
        for (MatcherAction action : dynamicActions) {
            // If an action exists which without any data can be valid, then we must force the evaluation
            action.reset();
            if (action.mustHaveMatches()) {
                actionsThatRequireInput++;
            }
        }

        if (verbose) {
            LOG.info("---------------------------");
        }

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

    /**
     * Fires all matcher actions.
     * IFF all success then we tell the userAgent
     *
     * @param userAgent The useragent that needs to analyzed
     */
    public void analyze(UserAgent userAgent) {

        if (verbose) {
            LOG.info("");
            LOG.info("--- Matcher ------------------------");
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
        sb.append("MATCHER.(").append(filename).append("):\n");
        sb.append("    REQUIRE:\n");
        for (MatcherAction action : dynamicActions) {
            if (action instanceof MatcherRequireAction) {
                sb.append("        ").append(action.getMatchExpression()).append("\n");
            }
        }
        sb.append("    EXTRACT:\n");
        for (MatcherAction action : dynamicActions) {
            if (action instanceof MatcherExtractAction) {
                sb.append("        ").append(action.toString()).append("\n");
            }
        }
        for (MatcherAction action : fixedStringActions) {
            sb.append("        ").append(action.toString()).append("\n");
        }
        return sb.toString();
    }
}
