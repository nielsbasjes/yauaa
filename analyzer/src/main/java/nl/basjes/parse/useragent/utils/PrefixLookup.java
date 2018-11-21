package nl.basjes.parse.useragent.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PrefixLookup implements Serializable {

    private final List<Pair<String, String>> sortedPrefixList;

    public PrefixLookup(Map<String, String> prefixList) {
        sortedPrefixList = new ArrayList<>(prefixList.size());

        // Translate the map into a different structure and lowercase the key.
        prefixList.forEach((key, value) -> sortedPrefixList.add(Pair.of(key.toLowerCase(Locale.ENGLISH), value)));

        // Sort the list to work best with the chosen algorithm.
        // Sort values by length (longest first), then alphabetically.
        sortedPrefixList.sort((o1, o2) -> {
            String k1 = o1.getKey();
            String k2 = o2.getKey();
            int    l1 = k1.length();
            int    l2 = k2.length();
            if (l1 == l2) {
                return k1.compareTo(k2);
            }
            return (l1 < l2) ? 1 : -1;
        });
    }

    public String findLongestMatchingPrefix(String input) {

        String lowerInput = input.toLowerCase(Locale.ENGLISH);

        // TODO: Experiment if a different datastructur is significantly faster (This implementation is SILLY simple)
        for (Pair<String, String> prefix: sortedPrefixList) {
            if (lowerInput.startsWith(prefix.getKey())) {
                return prefix.getRight();
            }
        }
        return null;
    }

}
