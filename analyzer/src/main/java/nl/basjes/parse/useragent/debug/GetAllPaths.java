package nl.basjes.parse.useragent.debug;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.analyze.MatchMaker;
import nl.basjes.parse.useragent.parse.UserAgentTreeFlattener;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.useragent.analyze.UserAgentStringMatchMaker.MAX_PREFIX_HASH_MATCH;
import static nl.basjes.parse.useragent.analyze.UserAgentStringMatchMaker.firstCharactersForPrefixHash;

public class GetAllPaths extends MatchMaker.Dummy {
    private final List<String> values = new ArrayList<>(128);

    private final UserAgent result;

    GetAllPaths(String useragent) {
        UserAgentTreeFlattener flattener = new UserAgentTreeFlattener(this);
        result = flattener.parse(useragent);
    }

    public List<String> getValues() {
        return values;
    }

    public UserAgent getResult() {
        return result;
    }

    @Override
    public void inform(String path, String value, ParseTree ctx) {
        values.add(path);
        values.add(path + "=\"" + value + "\"");
        values.add(path + "{\"" + firstCharactersForPrefixHash(value, MAX_PREFIX_HASH_MATCH) + "\"");
    }

    public static List<String> getAllPaths(String agent) {
        return new GetAllPaths(agent).getValues();
    }

    public static GetAllPaths getAllPathsAnalyzer(String agent) {
        return new GetAllPaths(agent);
    }
}
