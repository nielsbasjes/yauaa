/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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
package nl.basjes.parse.useragent.platfora;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestPlatforaUDF {

    private AnalyzeUserAgent udf = new AnalyzeUserAgent();

    private static final String USERAGENT =
        "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36";

    @Test
    public void checkBasicParse(){
        List<String> arguments = new ArrayList<>();
        arguments.add(USERAGENT);
        arguments.add("DeviceName");
        assertEquals("Google Nexus 6", udf.compute(arguments));
    }

    @Test
    public void checkVersion(){
        List<String> arguments = new ArrayList<>();
        arguments.add(USERAGENT);
        arguments.add("__version__");
        assertTrue(udf.compute(arguments).startsWith("Yauaa"));
    }

}
