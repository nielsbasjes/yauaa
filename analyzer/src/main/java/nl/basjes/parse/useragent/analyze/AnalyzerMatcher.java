package nl.basjes.parse.useragent.analyze;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AnalyzerMatcher {

    // We set this to 1000000 always.
    // Why?
    // At the time of writing this the actual HashMap size needed about 410K entries.
    // To keep the bins small the load factor of 0.75 already puts us at the capacity of 1048576
    public static final int INFORM_ACTIONS_HASHMAP_CAPACITY = 1000000;
    public final Map<String, Set<MatcherAction>> informMatcherActions = new LinkedHashMap<>(INFORM_ACTIONS_HASHMAP_CAPACITY);

    public synchronized void initializeMatchers() {
        if (matchersHaveBeenInitialized) {
            return;
        }
        LOG.info("Initializing Analyzer data structures");

        if (allMatchers.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

        long start = System.nanoTime();
        allMatchers.forEach(Matcher::initialize);
        long stop = System.nanoTime();

        matchersHaveBeenInitialized = true;
        LOG.info("Built in {} msec : Hashmap {}, Ranges map:{}",
            (stop - start) / 1000000,
            informMatcherActions.size(),
            informMatcherActionRanges.size());

        for (Matcher matcher: allMatchers) {
            if (matcher.getActionsThatRequireInput() == 0) {
                zeroInputMatchers.add(matcher);
            }
        }

        // Reset all Matchers
        for (Matcher matcher : allMatchers) {
            matcher.reset();
        }

        touchedMatchers = new MatcherList(32);
    }

}
