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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Matcher {
    private static final Logger LOG = LoggerFactory.getLogger(Matcher.class);

    private final Analyzer analyzer;
    private final List<MatcherAction> dynamicActions;
    private final List<MatcherAction> fixedStringActions;
    final Map<String, Map<String, String>> lookups;
    private boolean verbose;

    // Package private constructor for testing purposes only
    Matcher(Analyzer analyzer, Map<String, Map<String, String>> lookups) {
        this.lookups = lookups;
        this.analyzer = analyzer;
        this.fixedStringActions = new ArrayList<>();
        this.dynamicActions = new ArrayList<>();
    }

    public Matcher(Analyzer analyzer, Map<String, Map<String, String>> lookups, Map<String, List<String>> matcherConfig) {
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

        if (verbose) {
            LOG.info("---------------------------");
            LOG.info("- MATCHER -");
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

        List<String> extractConfigs = matcherConfig.get("extract");
        if (extractConfigs != null) {
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
                MatcherExtractAction action = new MatcherExtractAction(attribute, Long.parseLong(confidence), config, this);
                if (action.isFixedValue()) {
                    fixedStringActions.add(action);
                } else {
                    dynamicActions.add(action);
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

        newValuesUserAgent.reset();

        if (verbose) {
            LOG.info("");
            LOG.info("--- Matcher ------------------------");
            LOG.info("ANALYSE ----------------------------");
            boolean good = true;
            for (MatcherAction action : dynamicActions) {
                if (!action.canPossiblyBeValid()) {
                    LOG.error("CANNOT BE VALID : {}", action.getMatchExpression());
                }
            }
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
            for (MatcherAction action : dynamicActions) {
                if (!action.canPossiblyBeValid()) {
                    return; // If one of them is bad we skip the rest
                }
            }
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
        possiblyValid = false;
        for (MatcherAction action : dynamicActions) {
            action.reset();
            if (setVerboseTemporarily) {
                verbose = true;
                action.setVerbose(true, true);
            } else {
                verbose = false;
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
        if (dynamicActions.size() > 0) {
            if (!possiblyValid) {
                return new ArrayList<>(); // There is NO way one of them is valid
            }
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
