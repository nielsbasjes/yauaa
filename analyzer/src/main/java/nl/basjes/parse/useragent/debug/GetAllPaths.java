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
