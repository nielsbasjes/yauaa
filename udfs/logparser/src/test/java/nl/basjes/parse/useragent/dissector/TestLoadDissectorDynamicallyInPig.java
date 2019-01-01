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

package nl.basjes.parse.useragent.dissector;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.builtin.mock.Storage;
import org.apache.pig.data.Tuple;
import org.junit.Test;

import java.util.List;

import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.apache.pig.builtin.mock.Storage.tuple;
import static org.junit.Assert.assertEquals;

public class TestLoadDissectorDynamicallyInPig {

    private static final String LOGFORMAT = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"";
    private final String logfile = getClass().getResource("/access.log").toString();

    // Here we load the dissector WITH extra patterns and prove these patterns are actually found
    @Test
    public void dynamicallyLoadedExtraRules() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        pigServer.registerQuery(
            "Clicks = " +
                "    LOAD '" + logfile + "' " +
                "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
                "            '" + LOGFORMAT + "'," +
                "            'IP:connection.client.host'," +
                "            'TIME.STAMP:request.receive.time'," +
                "    '-load:nl.basjes.parse.useragent.dissector.UserAgentDissector:| || classpath*:**/CustomPatterns.yaml | ||'," +
                "            'HTTP.USERAGENT:request.user-agent'," +
                "            'HTTP.HOST:request.user-agent.agent_information_url.host'," +
                "            'STRING:request.user-agent.my_totally_useless_value'" +
                "            )" +
                "         AS (" +
                "            ConnectionClientHost," +
                "            RequestReceiveTime," +
                "            RequestUseragent," +
                "            RequestUseragentUrlHostName," +
                "            SomeValue" +
                "            );"
        );
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks");

        assertEquals(1, out.size());
        assertEquals(tuple(
                    "172.21.13.88",
                    "07/Apr/2013:03:04:49 +0200",
                    "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.96 " +
                        "Mobile Safari/537.36" +
                        "(https://yauaa.basjes.nl:8080/something.html?aap=noot&mies=wim#zus)",
                    "yauaa.basjes.nl",
                    "Nexus 5X"
            ).toDelimitedString("><#><"),
            out.get(0).toDelimitedString("><#><"));
    }

    // Here we load the dissector WITHOUT extra patterns
    @Test
    public void dynamicallyLoadedWithoutExtraRules() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        pigServer.registerQuery(
            "Clicks = " +
                "    LOAD '" + logfile + "' " +
                "    USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
                "            '" + LOGFORMAT + "'," +
                "            'IP:connection.client.host'," +
                "            'TIME.STAMP:request.receive.time'," +
                "    '-load:nl.basjes.parse.useragent.dissector.UserAgentDissector:'," +
                "            'HTTP.USERAGENT:request.user-agent'," +
                "            'HTTP.HOST:request.user-agent.agent_information_url.host'" +
                "            )" +
                "         AS (" +
                "            ConnectionClientHost," +
                "            RequestReceiveTime," +
                "            RequestUseragent," +
                "            RequestUseragentUrlHostName" +
                "            );"
        );
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks");

        assertEquals(1, out.size());
        assertEquals(tuple(
            "172.21.13.88",
            "07/Apr/2013:03:04:49 +0200",
            "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.96 " +
                "Mobile Safari/537.36" +
                "(https://yauaa.basjes.nl:8080/something.html?aap=noot&mies=wim#zus)",
            "yauaa.basjes.nl"
            ).toDelimitedString("><#><"),
            out.get(0).toDelimitedString("><#><"));
    }
}
