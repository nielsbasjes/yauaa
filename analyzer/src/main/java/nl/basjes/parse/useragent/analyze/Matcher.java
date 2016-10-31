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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;

public class Matcher {
    private static final Logger LOG = LoggerFactory.getLogger(Matcher.class);

    private final Analyzer analyzer;
    private final List<MatcherAction> dynamicActions;
    private final List<MatcherAction> fixedStringActions;
    final Map<String, Map<String, String>> lookups;
    private boolean verbose;
    private boolean permanentVerbose;

    // Package private constructor for testing purposes only
    Matcher(Analyzer analyzer, Map<String, Map<String, String>> lookups) {
        this.lookups = lookups;
        this.analyzer = analyzer;
        this.fixedStringActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();
    }

    public Matcher(Analyzer analyzer,
                   Map<String, Map<String, String>> lookups,
                   Set<String> wantedFieldNames,
                   Map<String, List<String>> matcherConfig) throws UselessMatcherException {
        this.lookups = lookups;
        this.analyzer = analyzer;
        this.fixedStringActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();

        if (matcherConfig == null) {
            throw new InvalidParserConfigurationException("Got a 'null' config setting");
        }

        verbose = false;
        List<String> options = matcherConfig.get("options");
        if (options != null) {
            verbose = options.contains("verbose");
        }
        permanentVerbose = verbose;

        if (verbose) {
            LOG.info("---------------------------");
            LOG.info("- MATCHER -");
        }

        List<String> extractConfigs = matcherConfig.get("extract");

        if (extractConfigs == null) {
            throw new InvalidParserConfigurationException("Matcher does not extract anything");
        }

        // If we have a restriction on the wanted fields we check if this one is needed at all
        if (wantedFieldNames != null) {
            boolean keep = false;

            for (String extractConfig : extractConfigs) {
                String[] configParts = extractConfig.split(":", 3);

                if (configParts.length != 3) {
                    throw new InvalidParserConfigurationException("Invalid extract config line: " + extractConfig);
                }

                String attribute = configParts[0].trim();
                if (wantedFieldNames.contains(attribute)) {
                    keep=true;
                    break;
                }
            }
            if (!keep) {
                throw new UselessMatcherException("Does not extract any wanted fields");
            }
        }

        // First the requires because they are meant to fail faster
        List<String> requireConfigs = matcherConfig.get("require");
        if (requireConfigs != null) {
            for (String requireConfig : requireConfigs) {
                if (verbose) {
                    LOG.info("REQUIRE: {}", requireConfig);
                }
                dynamicActions.add(new MatcherRequireAction(requireConfig, this));
            }
        }

        for (String extractConfig : extractConfigs) {
            if (verbose) {
                LOG.info("EXTRACT: {}", extractConfig);
            }
            String[] configParts = extractConfig.split(":", 3);

            if (configParts.length != 3) {
                throw new InvalidParserConfigurationException("Invalid extract config line: "+ extractConfig);
            }

            String attribute = configParts[0].trim();
            String confidence = configParts[1].trim();
            String config = configParts[2].trim();

            boolean wantThisAttribute = true;
            if (wantedFieldNames != null && !wantedFieldNames.contains(attribute)) {
                wantThisAttribute = false;
            }

            if (wantThisAttribute) {
                MatcherExtractAction action = new MatcherExtractAction(attribute, Long.parseLong(confidence), config, this);
                if (action.isFixedValue()) {
                    fixedStringActions.add(action);
                } else {
                    dynamicActions.add(action);
                }
            } else {
                try {
                    dynamicActions.add(new MatcherRequireAction(config, this));
                } catch (InvalidParserConfigurationException e) {
                    // Ignore fixed values in require
                }
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

    public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
        if (verbose) {
            LOG.info("Requested: {}", keyPattern);
        }
        analyzer.informMeAbout(matcherAction, keyPattern);
    }

    private final UserAgent newValuesUserAgent = new UserAgent("dummy");

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
                if (!action.canPossiblyBeValid()) {
                    LOG.error("CANNOT BE VALID : {}", action.getMatchExpression());
                    good = false;
                }
            }
            newValuesUserAgent.reset();
            for (MatcherAction action : dynamicActions) {
                if (!action.obtainResult(newValuesUserAgent)) {
                    LOG.error("FAILED : {}", action.getMatchExpression());
                    good = false;
                }
            }
            if (good) {
                for (MatcherAction action : fixedStringActions) {
                    if (!action.obtainResult(newValuesUserAgent)) {
                        LOG.error("FAILED : {}", action.getMatchExpression());
                    }
                }
            } else  {
                LOG.info("INCOMPLETE ----------------------------");
                return;
            }
            LOG.info("COMPLETE ----------------------------");
        } else {
            if (!possiblyValid) {
                return;
            }
            for (MatcherAction action : dynamicActions) {
                if (!action.canPossiblyBeValid()) {
                    return; // If one of them is bad we skip the rest
                }
            }
            newValuesUserAgent.reset();
            for (MatcherAction action : dynamicActions) {
                if (!action.obtainResult(newValuesUserAgent)) {
                    return; // If one of them is bad we skip the rest
                }
            }
            for (MatcherAction action : fixedStringActions) {
                if (!action.obtainResult(newValuesUserAgent)) {
                    return; // If one of them is bad we skip the rest
                }
            }
        }
        userAgent.set(newValuesUserAgent);
    }

    public boolean getVerbose() {
        return verbose;
    }

    boolean possiblyValid = false;
    public void gotAStartingPoint() {
        possiblyValid = true;
    }

    public void reset(boolean setVerboseTemporarily) {
        // If there are no dynamic actions we have fixed strings only
        possiblyValid = dynamicActions.isEmpty();
        for (MatcherAction action : dynamicActions) {
            action.reset();
            if (setVerboseTemporarily) {
                verbose = true;
                action.setVerbose(true, true);
            } else {
                verbose = permanentVerbose;
            }
        }
    }

    public List<MatcherAction.Match> getMatches() {
        List<MatcherAction.Match> allMatches = new ArrayList<>(128);
        for (MatcherAction action : dynamicActions) {
            allMatches.addAll(action.getMatches());
        }
        return allMatches;
    }

    public List<MatcherAction.Match> getUsedMatches() {
        List<MatcherAction.Match> allMatches = new ArrayList<>(128);
        if (!possiblyValid) {
            return new ArrayList<>(); // There is NO way one of them is valid
        }
        for (MatcherAction action : dynamicActions) {
            if (!action.canPossiblyBeValid()) {
                return new ArrayList<>(); // There is NO way one of them is valid
            }
        }
        for (MatcherAction action : dynamicActions) {
            if (!action.obtainResult(newValuesUserAgent)) {
                return new ArrayList<>(); // There is NO way one of them is valid
            } else {
                allMatches.addAll(action.getMatches());
            }
        }
        return allMatches;
    }

}
