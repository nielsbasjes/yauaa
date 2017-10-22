/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
 *
 * Licensed under the Apache License,Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.parse;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzerDirect.GetAllPathsAnalyzer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.fail;

public class TestUserAgentFlattening {

    private final Logger LOG = LoggerFactory.getLogger(TestUserAgentFlattening.class);

    @Test
    public void testFlatteningProduct() throws Exception {
        validateUserAgent(
            "Mozilla/5.0"
            ,"agent=\"Mozilla/5.0\""
            ,"agent.(1)product=\"Mozilla/5.0\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
        );

        // Special case with multiple spaces between words
        validateUserAgent(
            "one two  three   four/1"
            ,"agent=\"one two  three   four/1\""
            ,"agent.(1)product=\"one two  three   four/1\""
            ,"agent.(1)product.(1)name=\"one two  three   four\""
            ,"agent.(1)product.(1)version=\"1\""
        );

        // Edge case about where to break product and version info
        validateUserAgent(
            "one/two 3 four five/6 (one/two 3 four five/6)"
            ,"agent=\"one/two 3 four five/6 (one/two 3 four five/6)\""
            ,"agent.(1)product=\"one/two 3\""
            ,"agent.(1)product.(1)name=\"one\""
            ,"agent.(1)product.(1)version=\"two\""
            ,"agent.(1)product.(2)version=\"3\""

            ,"agent.(2)product=\"four five/6 (one/two 3 four five/6)\""
            ,"agent.(2)product.(1)name=\"four five\""
            ,"agent.(2)product.(1)version=\"6\""

            ,"agent.(2)product.(1)comments.(1)entry=\"one/two 3 four five/6\""

            ,"agent.(2)product.(1)comments.(1)entry.(1)product=\"one/two 3\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)product.(1)name=\"one\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)product.(1)version=\"two\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)product.(2)version=\"3\""

            ,"agent.(2)product.(1)comments.(1)entry.(2)product=\"four five/6\""
            ,"agent.(2)product.(1)comments.(1)entry.(2)product.(1)name=\"four five\""
            ,"agent.(2)product.(1)comments.(1)entry.(2)product.(1)version=\"6\""
        );

        validateUserAgent(
            "Foo 1/A (Bar 2/B)"
            ,"agent=\"Foo 1/A (Bar 2/B)\""
            ,"agent.(1)product=\"Foo 1/A (Bar 2/B)\""
            ,"agent.(1)product.(1)name=\"Foo\""
            ,"agent.(1)product.(1)version=\"1\""
            ,"agent.(1)product.(2)version=\"A\""
            ,"agent.(1)product.(1)comments=\"(Bar 2/B)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Bar 2/B\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product=\"Bar 2/B\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)name=\"Bar\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)version=\"2\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(2)version=\"B\""
        );

        validateUserAgent(
            "Mozilla/5.0 (foo)"
            ,"agent=\"Mozilla/5.0 (foo)\""
            ,"agent.(1)product=\"Mozilla/5.0 (foo)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(foo)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"foo\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"foo\""
        );

        validateUserAgent(
            "Mozilla (foo)"
            ,"agent=\"Mozilla (foo)\""
            ,"agent.(1)product=\"Mozilla (foo)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)comments=\"(foo)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"foo\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"foo\""
        );

        validateUserAgent(
            "The name 1 2 (foo bar baz) (one two three)"
            ,"agent=\"The name 1 2 (foo bar baz) (one two three)\""
            ,"agent.(1)product=\"The name 1 2 (foo bar baz) (one two three)\""
            ,"agent.(1)product.(1)name=\"The name\""
            ,"agent.(1)product.(1)version=\"1\""
            ,"agent.(1)product.(2)version=\"2\""
            ,"agent.(1)product.(1)comments=\"(foo bar baz)\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"foo bar baz\""
            ,"agent.(1)product.(2)comments=\"(one two three)\""
            ,"agent.(1)product.(2)comments.(1)entry.(1)text=\"one two three\""
        );

        validateUserAgent(
            "One 2 Three four 5 /6"
            ,"agent=\"One 2 Three four 5 /6\""
            ,"agent.(1)product=\"One 2\""
            ,"agent.(1)product.(1)name=\"One\""
            ,"agent.(1)product.(1)version=\"2\""
            ,"agent.(2)product=\"Three four 5 /6\""
            ,"agent.(2)product.(1)name=\"Three four\""
            ,"agent.(2)product.(1)version=\"5\""
            ,"agent.(2)product.(2)version=\"6\""
        );

        validateUserAgent(
            "One two 1 2 3/4/5.6"
            ,"agent=\"One two 1 2 3/4/5.6\""
            ,"agent.(1)product=\"One two 1 2 3/4/5.6\""
            ,"agent.(1)product.(1)name=\"One two\""
            ,"agent.(1)product.(1)version=\"1\""
            ,"agent.(1)product.(2)version=\"2\""
            ,"agent.(1)product.(3)version=\"3\""
            ,"agent.(1)product.(4)version=\"4\""
            ,"agent.(1)product.(5)version=\"5.6\""
        );

        // Product variations
        validateUserAgent("One=1 1 rv:2 (One; Numb3r)/3 (foo) Two Two 4 rv:5 (Two)/6 (bar)/7 (baz)/8"
            ,"agent=\"One=1 1 rv:2 (One; Numb3r)/3 (foo) Two Two 4 rv:5 (Two)/6 (bar)/7 (baz)/8\""
            ,"agent.(1)product=\"One=1 1 rv:2 (One; Numb3r)/3 (foo)\""
            ,"agent.(1)product.(1)name=\"One=1\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)key=\"One\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)version=\"1\""
            ,"agent.(1)product.(1)version=\"1\""
            ,"agent.(1)product.(2)version=\"rv:2\""
            ,"agent.(1)product.(2)version.(1)keyvalue=\"rv:2\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)version=\"2\""
            ,"agent.(1)product.(1)comments=\"(One; Numb3r)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"One\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"One\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Numb3r\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)text=\"Numb3r\""
            ,"agent.(1)product.(3)version=\"3\""
            ,"agent.(1)product.(2)comments=\"(foo)\""
            ,"agent.(1)product.(2)comments.(1)entry=\"foo\""
            ,"agent.(1)product.(2)comments.(1)entry.(1)text=\"foo\""
            ,"agent.(2)product=\"Two Two 4 rv:5 (Two)/6 (bar)/7 (baz)/8\""
            ,"agent.(2)product.(1)name=\"Two Two\""
            ,"agent.(2)product.(1)version=\"4\""
            ,"agent.(2)product.(2)version=\"rv:5\""
            ,"agent.(2)product.(2)version.(1)keyvalue=\"rv:5\""
            ,"agent.(2)product.(2)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(2)product.(2)version.(1)keyvalue.(1)version=\"5\""
            ,"agent.(2)product.(1)comments=\"(Two)\""
            ,"agent.(2)product.(1)comments.(1)entry=\"Two\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)text=\"Two\""
            ,"agent.(2)product.(3)version=\"6\""
            ,"agent.(2)product.(2)comments=\"(bar)\""
            ,"agent.(2)product.(2)comments.(1)entry=\"bar\""
            ,"agent.(2)product.(2)comments.(1)entry.(1)text=\"bar\""
            ,"agent.(2)product.(4)version=\"7\""
            ,"agent.(2)product.(3)comments=\"(baz)\""
            ,"agent.(2)product.(3)comments.(1)entry=\"baz\""
            ,"agent.(2)product.(3)comments.(1)entry.(1)text=\"baz\""
            ,"agent.(2)product.(5)version=\"8\""
        );

        // Special product names
        validateUserAgent("TextName 1 (a) Versi0nName 2 (b) em@il.name 3 (c) key=value 4 (d)"
            ,"agent=\"TextName 1 (a) Versi0nName 2 (b) em@il.name 3 (c) key=value 4 (d)\""
        );

    }

    @Test
    public void testFlatteningComment() throws Exception {
        // Comment variations
        validateUserAgent("One/1 (;Key=Value;; Key=Two Value;OneWord;Two words; Numb3rWord ; TwoNumb3r W0rds ; em@il.nl ; http://web.site ; Sub product/2(Sub Comment,))"
            ,"agent=\"One/1 (;Key=Value;; Key=Two Value;OneWord;Two words; Numb3rWord ; TwoNumb3r W0rds ; em@il.nl ; http://web.site ; Sub product/2(Sub Comment,))\""
            ,"agent.(1)product=\"One/1 (;Key=Value;; Key=Two Value;OneWord;Two words; Numb3rWord ; TwoNumb3r W0rds ; em@il.nl ; http://web.site ; Sub product/2(Sub Comment,))\""
            ,"agent.(1)product.(1)name=\"One\""
            ,"agent.(1)product.(1)version=\"1\""
            ,"agent.(1)product.(1)comments=\"(;Key=Value;; Key=Two Value;OneWord;Two words; Numb3rWord ; TwoNumb3r W0rds ; em@il.nl ; http://web.site ; Sub product/2(Sub Comment,))\""
            ,"agent.(1)product.(1)comments.(1)entry=\"\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Key=Value\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)keyvalue=\"Key=Value\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)keyvalue.(1)key=\"Key\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)keyvalue.(2)text=\"Value\""
            ,"agent.(1)product.(1)comments.(3)entry=\"\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)text=\"\""
            ,"agent.(1)product.(1)comments.(4)entry=\"Key=Two Value\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue=\"Key=Two Value\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue.(1)key=\"Key\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue.(2)text=\"Two Value\""
            ,"agent.(1)product.(1)comments.(5)entry=\"OneWord\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)text=\"OneWord\""
            ,"agent.(1)product.(1)comments.(6)entry=\"Two words\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)text=\"Two words\""
            ,"agent.(1)product.(1)comments.(7)entry=\"Numb3rWord\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)text=\"Numb3rWord\""
            ,"agent.(1)product.(1)comments.(8)entry=\"TwoNumb3r W0rds\""
            ,"agent.(1)product.(1)comments.(8)entry.(1)product=\"TwoNumb3r W0rds\""
            ,"agent.(1)product.(1)comments.(8)entry.(1)product.(1)name=\"TwoNumb3r\""
            ,"agent.(1)product.(1)comments.(8)entry.(1)product.(1)version=\"W0rds\""
            ,"agent.(1)product.(1)comments.(9)entry=\"em@il.nl\""
            ,"agent.(1)product.(1)comments.(9)entry.(1)email=\"em@il.nl\""
            ,"agent.(1)product.(1)comments.(10)entry=\"http://web.site\""
            ,"agent.(1)product.(1)comments.(10)entry.(1)url=\"http://web.site\""
            ,"agent.(1)product.(1)comments.(11)entry=\"Sub product/2(Sub Comment,)\""
            ,"agent.(1)product.(1)comments.(11)entry.(1)product=\"Sub product/2(Sub Comment,)\""
            ,"agent.(1)product.(1)comments.(11)entry.(1)product.(1)name=\"Sub product\""
            ,"agent.(1)product.(1)comments.(11)entry.(1)product.(1)version=\"2\""
            ,"agent.(1)product.(1)comments.(11)entry.(1)product.(1)comments=\"(Sub Comment,)\""
            ,"agent.(1)product.(1)comments.(11)entry.(1)product.(1)comments.(1)entry=\"Sub Comment\""
            ,"agent.(1)product.(1)comments.(11)entry.(1)product.(1)comments.(1)entry.(1)text=\"Sub Comment\""
            ,"agent.(1)product.(1)comments.(11)entry.(1)product.(1)comments.(2)entry=\"\""
            ,"agent.(1)product.(1)comments.(11)entry.(1)product.(1)comments.(2)entry.(1)text=\"\""
        );
    }

    @Test
    public void testFlatteningKeyValue() throws Exception {

        // KeyValue variations

        // Multiple versions and comments for a single product
        // This is to validate the numbering schema !!
        validateUserAgent("One one 1 rv:2 (One; Numb3r)/3 (foo) Two Two 4 rv:5 (Two)/6 (bar)/7 (baz)/8"
            ,"agent=\"One one 1 rv:2 (One; Numb3r)/3 (foo) Two Two 4 rv:5 (Two)/6 (bar)/7 (baz)/8\""
            ,"agent.(1)product=\"One one 1 rv:2 (One; Numb3r)/3 (foo)\""
            ,"agent.(1)product.(1)name=\"One one\""
            ,"agent.(1)product.(1)version=\"1\""
            ,"agent.(1)product.(2)version=\"rv:2\""
            ,"agent.(1)product.(2)version.(1)keyvalue=\"rv:2\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)version=\"2\""
            ,"agent.(1)product.(1)comments=\"(One; Numb3r)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"One\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"One\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Numb3r\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)text=\"Numb3r\""
            ,"agent.(1)product.(3)version=\"3\""
            ,"agent.(1)product.(2)comments=\"(foo)\""
            ,"agent.(1)product.(2)comments.(1)entry=\"foo\""
            ,"agent.(1)product.(2)comments.(1)entry.(1)text=\"foo\""
            ,"agent.(2)product=\"Two Two 4 rv:5 (Two)/6 (bar)/7 (baz)/8\""
            ,"agent.(2)product.(1)name=\"Two Two\""
            ,"agent.(2)product.(1)version=\"4\""
            ,"agent.(2)product.(2)version=\"rv:5\""
            ,"agent.(2)product.(2)version.(1)keyvalue=\"rv:5\""
            ,"agent.(2)product.(2)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(2)product.(2)version.(1)keyvalue.(1)version=\"5\""
            ,"agent.(2)product.(1)comments=\"(Two)\""
            ,"agent.(2)product.(1)comments.(1)entry=\"Two\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)text=\"Two\""
            ,"agent.(2)product.(3)version=\"6\""
            ,"agent.(2)product.(2)comments=\"(bar)\""
            ,"agent.(2)product.(2)comments.(1)entry=\"bar\""
            ,"agent.(2)product.(2)comments.(1)entry.(1)text=\"bar\""
            ,"agent.(2)product.(4)version=\"7\""
            ,"agent.(2)product.(3)comments=\"(baz)\""
            ,"agent.(2)product.(3)comments.(1)entry=\"baz\""
            ,"agent.(2)product.(3)comments.(1)entry.(1)text=\"baz\""
            ,"agent.(2)product.(5)version=\"8\""
        );

        // KeyValue variations
        validateUserAgent("k1:v1 k2:v k3:v3 (c)"
            ,"agent=\"k1:v1 k2:v k3:v3 (c)\""
            ,"agent.(1)product=\"k1:v1 k2:v k3:v3 (c)\""
            ,"agent.(1)product.(1)name=\"k1:v1\""
            ,"agent.(1)product.(1)name.(1)keyvalue=\"k1:v1\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)key=\"k1\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)version=\"v1\""
            ,"agent.(1)product.(1)version=\"k2:v\""
            ,"agent.(1)product.(1)version.(1)keyvalue=\"k2:v\""
            ,"agent.(1)product.(1)version.(1)keyvalue.(1)key=\"k2\""
            ,"agent.(1)product.(1)version.(1)keyvalue.(2)text=\"v\""
            ,"agent.(1)product.(2)version=\"k3:v3\""
            ,"agent.(1)product.(2)version.(1)keyvalue=\"k3:v3\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)key=\"k3\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)version=\"v3\""
            ,"agent.(1)product.(1)comments=\"(c)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"c\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"c\""
        ); // k3 bad

        validateUserAgent("k1:v1 k2:v k3:vv (c)"
            ,"agent=\"k1:v1 k2:v k3:vv (c)\""
            ,"agent.(1)product=\"k1:v1 k2:v k3:vv (c)\""
            ,"agent.(1)product.(1)name=\"k1:v1\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)key=\"k1\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)version=\"v1\""
            ,"agent.(1)product.(1)version=\"k2:v\""
            ,"agent.(1)product.(1)version.(1)keyvalue=\"k2:v\""
            ,"agent.(1)product.(1)version.(1)keyvalue.(1)key=\"k2\""
            ,"agent.(1)product.(1)version.(1)keyvalue.(2)text=\"v\""
            ,"agent.(1)product.(2)version=\"k3:vv\""
            ,"agent.(1)product.(2)version.(1)keyvalue=\"k3:vv\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)key=\"k3\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(2)text=\"vv\""
            ,"agent.(1)product.(1)comments=\"(c)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"c\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"c\""
        ); // k3 good

        validateUserAgent("k1:1 rv:2 (rv:3; rv:4)/rv:5"
            ,"agent=\"k1:1 rv:2 (rv:3; rv:4)/rv:5\""
            ,"agent.(1)product=\"k1:1 rv:2 (rv:3; rv:4)/rv:5\""
            ,"agent.(1)product.(1)name=\"k1:1\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)key=\"k1\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)version=\"1\""
            ,"agent.(1)product.(1)version=\"rv:2\""
            ,"agent.(1)product.(1)version.(1)keyvalue=\"rv:2\""
            ,"agent.(1)product.(1)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(1)version.(1)keyvalue.(1)version=\"2\""
            ,"agent.(1)product.(1)comments=\"(rv:3; rv:4)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"rv:3\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)keyvalue=\"rv:3\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)keyvalue.(1)version=\"3\""
            ,"agent.(1)product.(1)comments.(2)entry=\"rv:4\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)keyvalue=\"rv:4\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)keyvalue.(1)version=\"4\""
            ,"agent.(1)product.(2)version=\"rv:5\""
            ,"agent.(1)product.(2)version.(1)keyvalue=\"rv:5\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)version=\"5\""
        );

        validateUserAgent("One/1 (Empty=;Equals=EValue;Colon:CValue;MultiWord=More Value;Email=em@il.nl;Web=https://web.site;Uuid=550e8400-e29b-41d4-a716-446655440000;)"
            ,"agent=\"One/1 (Empty=;Equals=EValue;Colon:CValue;MultiWord=More Value;Email=em@il.nl;Web=https://web.site;Uuid=550e8400-e29b-41d4-a716-446655440000;)\""
            ,"agent.(1)product=\"One/1 (Empty=;Equals=EValue;Colon:CValue;MultiWord=More Value;Email=em@il.nl;Web=https://web.site;Uuid=550e8400-e29b-41d4-a716-446655440000;)\""
            ,"agent.(1)product.(1)name=\"One\""
            ,"agent.(1)product.(1)version=\"1\""
            ,"agent.(1)product.(1)comments=\"(Empty=;Equals=EValue;Colon:CValue;MultiWord=More Value;Email=em@il.nl;Web=https://web.site;Uuid=550e8400-e29b-41d4-a716-446655440000;)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Empty=\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)keyvalue=\"Empty=\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)keyvalue.(1)key=\"Empty\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Equals=EValue\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)keyvalue=\"Equals=EValue\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)keyvalue.(1)key=\"Equals\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)keyvalue.(2)text=\"EValue\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Colon:CValue\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)keyvalue=\"Colon:CValue\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)keyvalue.(1)key=\"Colon\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)keyvalue.(2)text=\"CValue\""
            ,"agent.(1)product.(1)comments.(4)entry=\"MultiWord=More Value\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue=\"MultiWord=More Value\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue.(1)key=\"MultiWord\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue.(2)text=\"More Value\""
            ,"agent.(1)product.(1)comments.(5)entry=\"Email=em@il.nl\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue=\"Email=em@il.nl\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue.(1)key=\"Email\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue.(2)email=\"em@il.nl\""
            ,"agent.(1)product.(1)comments.(6)entry=\"Web=https://web.site\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)keyvalue=\"Web=https://web.site\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)keyvalue.(1)key=\"Web\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)keyvalue.(2)url=\"https://web.site\""
            ,"agent.(1)product.(1)comments.(7)entry=\"Uuid=550e8400-e29b-41d4-a716-446655440000\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)keyvalue=\"Uuid=550e8400-e29b-41d4-a716-446655440000\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)keyvalue.(1)key=\"Uuid\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)keyvalue.(2)uuid=\"550e8400-e29b-41d4-a716-446655440000\""
            ,"agent.(1)product.(1)comments.(8)entry=\"\""
            ,"agent.(1)product.(1)comments.(8)entry.(1)text=\"\""
        );

        validateUserAgent(
            "One two 1.2.3 rv:4.5.6 (One; Two Two 1.2.3/4 (sub one 1.0 ,sub two)/5; Three Three)/1.2"

            ,"agent.(1)product=\"One two 1.2.3 rv:4.5.6 (One; Two Two 1.2.3/4 (sub one 1.0 ,sub two)/5; Three Three)/1.2\""
            ,"agent.(1)product.(1)name=\"One two\""
            ,"agent.(1)product.(1)version=\"1.2.3\""
            ,"agent.(1)product.(2)version=\"rv:4.5.6\""
            ,"agent.(1)product.(2)version.(1)keyvalue=\"rv:4.5.6\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(2)version.(1)keyvalue=\"rv:4.5.6\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)version=\"4.5.6\""
            ,"agent.(1)product.(1)comments=\"(One; Two Two 1.2.3/4 (sub one 1.0 ,sub two)/5; Three Three)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"One\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"One\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Two Two 1.2.3/4 (sub one 1.0 ,sub two)/5\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"Two Two 1.2.3/4 (sub one 1.0 ,sub two)/5\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"Two Two\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"1.2.3\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(2)version=\"4\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)comments=\"(sub one 1.0 ,sub two)\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)comments.(1)entry=\"sub one 1.0\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)comments.(1)entry.(1)product=\"sub one 1.0\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)comments.(1)entry.(1)product.(1)name=\"sub one\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)comments.(1)entry.(1)product.(1)version=\"1.0\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)comments.(2)entry=\"sub two\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)comments.(2)entry.(1)text=\"sub two\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(3)version=\"5\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Three Three\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)text=\"Three Three\""
            ,"agent.(1)product.(3)version=\"1.2\""
        );

        // Special key names
        validateUserAgent("Foo/5.0 (Some-Thing=true) Some-Thing=true"
            ,"agent=\"Foo/5.0 (Some-Thing=true) Some-Thing=true\""
            ,"agent.(1)product=\"Foo/5.0 (Some-Thing=true)\""
            ,"agent.(1)product.(1)name=\"Foo\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)keyvalue=\"Some-Thing=true\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)keyvalue.(1)key=\"Some-Thing\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)keyvalue.(2)text=\"true\""
            ,"agent.(2)keyvalue=\"Some-Thing=true\""
            ,"agent.(2)keyvalue.(1)key=\"Some-Thing\""
            ,"agent.(2)keyvalue.(2)text=\"true\""
        );

    }

    @Test
    public void testRootCases() throws Exception {
        validateUserAgent(
            "FooBar"
            ,"agent=\"FooBar\""
            ,"agent.(1)text=\"FooBar\""
        );

        validateUserAgent(
            "Foo-Bar"
            ,"agent=\"Foo-Bar\""
            ,"agent.(1)text=\"Foo-Bar\""
        );


        validateUserAgent(
            "F00 Bar"
            ,"agent=\"F00 Bar\""
            ,"agent.(1)text=\"F00\""
            ,"agent.(2)text=\"Bar\""
        );

        validateUserAgent(
            "F00-Bar-1_2_3"
            ,"agent=\"F00-Bar-1_2_3\""
            ,"agent.(1)text=\"F00-Bar-1_2_3\""
        );

        validateUserAgent(
            "Foo=Bar"
            ,"agent=\"Foo=Bar\""
            ,"agent.(1)keyvalue=\"Foo=Bar\""
            ,"agent.(1)keyvalue.(1)key=\"Foo\""
            ,"agent.(1)keyvalue.(2)text=\"Bar\""
        );

        validateUserAgent(
            "F00=Bar"
            ,"agent.(1)keyvalue=\"F00=Bar\""
            ,"agent.(1)keyvalue.(1)key=\"F00\""
            ,"agent.(1)keyvalue.(2)text=\"Bar\""
        );

        validateUserAgent(
            "F00@Bar.com"
            ,"agent=\"F00@Bar.com\""
            ,"agent.(1)email=\"F00@Bar.com\""
        );

        validateUserAgent(
            "http://F00.Bar.com"
            ,"agent=\"http://F00.Bar.com\""
            ,"agent.(1)url=\"http://F00.Bar.com\""
        );
    }

    @Test
    public void testFlatteningSpecialCases() throws Exception {
        validateUserAgent(
            "Mozilla/5.0 (Linux; Android 4.4; Nexus 7/JSS15R) AppleWebKit/537.36 (KHTML,like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36"
            ,"agent=\"Mozilla/5.0 (Linux; Android 4.4; Nexus 7/JSS15R) AppleWebKit/537.36 (KHTML,like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36\""
            ,"agent.(1)product=\"Mozilla/5.0 (Linux; Android 4.4; Nexus 7/JSS15R)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(Linux; Android 4.4; Nexus 7/JSS15R)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Linux\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"Linux\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Android 4.4\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"Android 4.4\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"Android\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"4.4\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Nexus 7/JSS15R\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Nexus 7/JSS15R\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Nexus\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"7\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(2)version=\"JSS15R\""
            ,"agent.(2)product=\"AppleWebKit/537.36 (KHTML,like Gecko)\""
            ,"agent.(2)product.(1)name=\"AppleWebKit\""
            ,"agent.(2)product.(1)version=\"537.36\""
            ,"agent.(2)product.(1)comments=\"(KHTML,like Gecko)\""
            ,"agent.(2)product.(1)comments.(1)entry=\"KHTML\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)text=\"KHTML\""
            ,"agent.(2)product.(1)comments.(2)entry=\"like Gecko\""
            ,"agent.(2)product.(1)comments.(2)entry.(1)text=\"like Gecko\""
            ,"agent.(3)product=\"Chrome/34.0.1847.114\""
            ,"agent.(3)product.(1)name=\"Chrome\""
            ,"agent.(3)product.(1)version=\"34.0.1847.114\""
            ,"agent.(4)product=\"Mobile Safari/537.36\""
            ,"agent.(4)product.(1)name=\"Mobile Safari\""
            ,"agent.(4)product.(1)version=\"537.36\""
        );

        validateUserAgent(
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; SIMBAR=0; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 1.0.3705; .NET CLR 1.1.4322; InfoPath.1; IEMB3; IEMB3)"
            ,"agent=\"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; SIMBAR=0; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 1.0.3705; .NET CLR 1.1.4322; InfoPath.1; IEMB3; IEMB3)\""
            ,"agent.(1)product=\"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; SIMBAR=0; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 1.0.3705; .NET CLR 1.1.4322; InfoPath.1; IEMB3; IEMB3)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"4.0\""
            ,"agent.(1)product.(1)comments=\"(compatible; MSIE 7.0; Windows NT 5.1; SIMBAR=0; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 1.0.3705; .NET CLR 1.1.4322; InfoPath.1; IEMB3; IEMB3)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"compatible\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"compatible\""
            ,"agent.(1)product.(1)comments.(2)entry=\"MSIE 7.0\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"MSIE 7.0\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"MSIE\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"7.0\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Windows NT 5.1\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Windows NT 5.1\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Windows NT\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"5.1\""
            ,"agent.(1)product.(1)comments.(4)entry=\"SIMBAR=0\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue=\"SIMBAR=0\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue.(1)key=\"SIMBAR\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue.(1)version=\"0\""
            ,"agent.(1)product.(1)comments.(5)entry=\"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product=\"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)version=\"4.0\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments=\"(compatible; MSIE 6.0; Windows NT 5.1; SV1)\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(1)entry=\"compatible\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(1)entry.(1)text=\"compatible\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(2)entry=\"MSIE 6.0\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(2)entry.(1)product=\"MSIE 6.0\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"MSIE\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"6.0\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(3)entry=\"Windows NT 5.1\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(3)entry.(1)product=\"Windows NT 5.1\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Windows NT\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"5.1\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(4)entry=\"SV1\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)comments.(4)entry.(1)text=\"SV1\""
            ,"agent.(1)product.(1)comments.(6)entry=\".NET CLR 1.0.3705\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product=\".NET CLR 1.0.3705\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)name=\".NET CLR\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)version=\"1.0.3705\""
            ,"agent.(1)product.(1)comments.(7)entry=\".NET CLR 1.1.4322\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)product=\".NET CLR 1.1.4322\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)product.(1)name=\".NET CLR\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)product.(1)version=\"1.1.4322\""
            ,"agent.(1)product.(1)comments.(8)entry=\"InfoPath.1\""
            ,"agent.(1)product.(1)comments.(8)entry.(1)text=\"InfoPath.1\""
            ,"agent.(1)product.(1)comments.(9)entry=\"IEMB3\""
            ,"agent.(1)product.(1)comments.(9)entry.(1)text=\"IEMB3\""
            ,"agent.(1)product.(1)comments.(10)entry=\"IEMB3\""
            ,"agent.(1)product.(1)comments.(10)entry.(1)text=\"IEMB3\""
        );

        // Special Test cases
        validateUserAgent(
            "Name/www@basjes.nl(www.example.nl)"
            ,"agent=\"Name/www@basjes.nl(www.example.nl)\""
            ,"agent.(1)product=\"Name/www@basjes.nl(www.example.nl)\""
            ,"agent.(1)product.(1)name=\"Name\""
            ,"agent.(1)product.(1)version=\"www@basjes.nl\""
            ,"agent.(1)product.(1)version.(1)email=\"www@basjes.nl\""
            ,"agent.(1)product.(1)comments=\"(www.example.nl)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"www.example.nl\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)url=\"www.example.nl\""
        );
        validateUserAgent(
            "Sogou web spider/4.0(+http://www.sogou.com/docs/help/webmasters.htm#07)"
            ,"agent=\"Sogou web spider/4.0(+http://www.sogou.com/docs/help/webmasters.htm#07)\""
            ,"agent.(1)product=\"Sogou web spider/4.0(+http://www.sogou.com/docs/help/webmasters.htm#07)\""
            ,"agent.(1)product.(1)name=\"Sogou web spider\""
            ,"agent.(1)product.(1)version=\"4.0\""
            ,"agent.(1)product.(1)comments=\"(+http://www.sogou.com/docs/help/webmasters.htm#07)\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)url=\"http://www.sogou.com/docs/help/webmasters.htm#07\""
        );
        validateUserAgent("LWP::Simple/6.00 libwww-perl/6.05"
            ,"agent=\"LWP::Simple/6.00 libwww-perl/6.05\""
            ,"agent.(1)product=\"LWP::Simple/6.00\""
            ,"agent.(1)product.(1)name=\"LWP::Simple\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)key=\"LWP\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(2)text=\"Simple\""
            ,"agent.(1)product.(1)version=\"6.00\""
            ,"agent.(2)product=\"libwww-perl/6.05\""
            ,"agent.(2)product.(1)name=\"libwww-perl\""
            ,"agent.(2)product.(1)version=\"6.05\""
        );
        validateUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; Name = MASB; rv:11.0) like Gecko"
            ,"agent=\"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; Name = MASB; rv:11.0) like Gecko\""
            ,"agent.(1)product=\"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; Name = MASB; rv:11.0)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(Windows NT 6.1; WOW64; Trident/7.0; Name = MASB; rv:11.0)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Windows NT 6.1\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product=\"Windows NT 6.1\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)name=\"Windows NT\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)version=\"6.1\""
            ,"agent.(1)product.(1)comments.(2)entry=\"WOW64\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)text=\"WOW64\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Trident/7.0\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Trident/7.0\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Trident\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"7.0\""
            ,"agent.(1)product.(1)comments.(4)entry=\"Name = MASB\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue=\"Name = MASB\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue.(1)key=\"Name\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue.(2)text=\"MASB\""
            ,"agent.(1)product.(1)comments.(5)entry=\"rv:11.0\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue=\"rv:11.0\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue.(1)version=\"11.0\""
            ,"agent.(2)text=\"like Gecko\""
        );

        validateUserAgent(
            "Voordeel 1.3.0 rv:1.30 (iPhone; iPhone OS 7.1.1; nl_NL)"
            ,"agent=\"Voordeel 1.3.0 rv:1.30 (iPhone; iPhone OS 7.1.1; nl_NL)\""
            ,"agent.(1)product=\"Voordeel 1.3.0 rv:1.30 (iPhone; iPhone OS 7.1.1; nl_NL)\""
            ,"agent.(1)product.(1)name=\"Voordeel\""
            ,"agent.(1)product.(1)version=\"1.3.0\""
            ,"agent.(1)product.(2)version=\"rv:1.30\""
            ,"agent.(1)product.(2)version.(1)keyvalue=\"rv:1.30\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)version=\"1.30\""
            ,"agent.(1)product.(1)comments=\"(iPhone; iPhone OS 7.1.1; nl_NL)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"iPhone\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"iPhone\""
            ,"agent.(1)product.(1)comments.(2)entry=\"iPhone OS 7.1.1\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"iPhone OS 7.1.1\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"iPhone OS\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"7.1.1\""
            ,"agent.(1)product.(1)comments.(3)entry=\"nl_NL\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)text=\"nl_NL\""

        );

        validateUserAgent(
            "aggregator:Spinn3r (Spinn3r 3.1)"
            ,"agent=\"aggregator:Spinn3r (Spinn3r 3.1)\""
            ,"agent.(1)product=\"aggregator:Spinn3r (Spinn3r 3.1)\""
            ,"agent.(1)product.(1)name=\"aggregator:Spinn3r\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)key=\"aggregator\""
            ,"agent.(1)product.(1)name.(1)keyvalue.(1)version=\"Spinn3r\""
            ,"agent.(1)product.(1)comments=\"(Spinn3r 3.1)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Spinn3r 3.1\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product=\"Spinn3r 3.1\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)name=\"Spinn3r\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)version=\"3.1\""
        );

        validateUserAgent(
            "Mozilla/5.0 (aggregator:Spinn3r (Spinn3r 3.1))"
            ,"agent=\"Mozilla/5.0 (aggregator:Spinn3r (Spinn3r 3.1))\""
            ,"agent.(1)product=\"Mozilla/5.0 (aggregator:Spinn3r (Spinn3r 3.1))\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(aggregator:Spinn3r (Spinn3r 3.1))\""
            ,"agent.(1)product.(1)comments.(1)entry=\"aggregator:Spinn3r (Spinn3r 3.1)\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product=\"aggregator:Spinn3r (Spinn3r 3.1)\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)name=\"aggregator:Spinn3r\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)name.(1)keyvalue.(1)key=\"aggregator\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)name.(1)keyvalue.(1)version=\"Spinn3r\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)comments=\"(Spinn3r 3.1)\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)comments.(1)entry=\"Spinn3r 3.1\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)comments.(1)entry.(1)product=\"Spinn3r 3.1\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)comments.(1)entry.(1)product.(1)name=\"Spinn3r\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)comments.(1)entry.(1)product.(1)version=\"3.1\""
        );

        validateUserAgent(
            "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.19; aggregator:Spinn3r (Spinn3r 3.1); http://spinn3r.com/robot) Gecko/2010040121 Firefox/3.0.19"
            ,"agent=\"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.19; aggregator:Spinn3r (Spinn3r 3.1); http://spinn3r.com/robot) Gecko/2010040121 Firefox/3.0.19\""
            ,"agent.(1)product=\"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.19; aggregator:Spinn3r (Spinn3r 3.1); http://spinn3r.com/robot)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(X11; U; Linux x86_64; en-US; rv:1.9.0.19; aggregator:Spinn3r (Spinn3r 3.1); http://spinn3r.com/robot)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"X11\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"X11\""

            ,"agent.(1)product.(1)comments.(2)entry=\"U\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)text=\"U\""

            ,"agent.(1)product.(1)comments.(3)entry=\"Linux x86_64\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Linux x86_64\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Linux\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"x86_64\""

            ,"agent.(1)product.(1)comments.(4)entry=\"en-US\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)text=\"en-US\""

            ,"agent.(1)product.(1)comments.(5)entry=\"rv:1.9.0.19\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue=\"rv:1.9.0.19\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue.(1)version=\"1.9.0.19\""

            ,"agent.(1)product.(1)comments.(6)entry=\"aggregator:Spinn3r (Spinn3r 3.1)\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product=\"aggregator:Spinn3r (Spinn3r 3.1)\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)name=\"aggregator:Spinn3r\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)name.(1)keyvalue.(1)key=\"aggregator\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)name.(1)keyvalue.(1)version=\"Spinn3r\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)comments=\"(Spinn3r 3.1)\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)comments.(1)entry=\"Spinn3r 3.1\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)comments.(1)entry.(1)product=\"Spinn3r 3.1\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)comments.(1)entry.(1)product.(1)name=\"Spinn3r\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)comments.(1)entry.(1)product.(1)version=\"3.1\""

            ,"agent.(1)product.(1)comments.(7)entry=\"http://spinn3r.com/robot\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)url=\"http://spinn3r.com/robot\""

            ,"agent.(2)product=\"Gecko/2010040121\""
            ,"agent.(2)product.(1)name=\"Gecko\""
            ,"agent.(2)product.(1)version=\"2010040121\""

            ,"agent.(3)product=\"Firefox/3.0.19\""
            ,"agent.(3)product.(1)name=\"Firefox\""
            ,"agent.(3)product.(1)version=\"3.0.19\""
        );

        validateUserAgent(
            "Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; nl-nl) AppleWebKit/531.21.10 (KHTML,like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.102011-10-16 20:23:10"
            ,"agent=\"Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; nl-nl) AppleWebKit/531.21.10 (KHTML,like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.102011-10-16 20:23:10\""
            ,"agent.(1)product=\"Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; nl-nl)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(iPad; U; CPU OS 3_2 like Mac OS X; nl-nl)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"iPad\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"iPad\""
            ,"agent.(1)product.(1)comments.(2)entry=\"U\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)text=\"U\""
            ,"agent.(1)product.(1)comments.(3)entry=\"CPU OS 3_2 like Mac OS X\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"CPU OS 3_2\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"CPU OS\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"3_2\""
            ,"agent.(1)product.(1)comments.(3)entry.(2)text=\"like Mac OS X\""
            ,"agent.(1)product.(1)comments.(4)entry=\"nl-nl\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)text=\"nl-nl\""
            ,"agent.(2)product=\"AppleWebKit/531.21.10 (KHTML,like Gecko)\""
            ,"agent.(2)product.(1)name=\"AppleWebKit\""
            ,"agent.(2)product.(1)version=\"531.21.10\""
            ,"agent.(2)product.(1)comments=\"(KHTML,like Gecko)\""
            ,"agent.(2)product.(1)comments.(1)entry=\"KHTML\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)text=\"KHTML\""
            ,"agent.(2)product.(1)comments.(2)entry=\"like Gecko\""
            ,"agent.(2)product.(1)comments.(2)entry.(1)text=\"like Gecko\""
            ,"agent.(3)product=\"Version/4.0.4\""
            ,"agent.(3)product.(1)name=\"Version\""
            ,"agent.(3)product.(1)version=\"4.0.4\""
            ,"agent.(4)product=\"Mobile/7B334b\""
            ,"agent.(4)product.(1)name=\"Mobile\""
            ,"agent.(4)product.(1)version=\"7B334b\""
            ,"agent.(5)product=\"Safari/531.21.102011-10-16\""
            ,"agent.(5)product.(1)name=\"Safari\""
            ,"agent.(5)product.(1)version=\"531.21.102011-10-16\""
            ,"agent.(6)keyvalue=\"20:23:10\""
            ,"agent.(6)keyvalue.(1)key=\"20\""
            ,"agent.(6)keyvalue.(1)version=\"23\""
            ,"agent.(6)keyvalue.(2)version=\"10\""
        );
        validateUserAgent(
            "Airmail 1.3.3 rv:237 (Macintosh; Mac OS X 10.9.3; nl_NL)"
            ,"agent=\"Airmail 1.3.3 rv:237 (Macintosh; Mac OS X 10.9.3; nl_NL)\""
            ,"agent.(1)product=\"Airmail 1.3.3 rv:237 (Macintosh; Mac OS X 10.9.3; nl_NL)\""
            ,"agent.(1)product.(1)name=\"Airmail\""
            ,"agent.(1)product.(1)version=\"1.3.3\""
            ,"agent.(1)product.(2)version=\"rv:237\""
            ,"agent.(1)product.(2)version.(1)keyvalue=\"rv:237\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)version=\"237\""
            ,"agent.(1)product.(1)comments=\"(Macintosh; Mac OS X 10.9.3; nl_NL)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Macintosh\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"Macintosh\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Mac OS X 10.9.3\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"Mac OS X 10.9.3\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"Mac OS X\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"10.9.3\""
            ,"agent.(1)product.(1)comments.(3)entry=\"nl_NL\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)text=\"nl_NL\""
        );
        validateUserAgent(
            "\"\\\"\"Mozilla/5.0 (Linux; Android 4.4; Nexus 7/JSS15R) AppleWebKit/537.36 (KHTML,like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36\\\"\"\""
            ,"agent=\"\"\\\"\"Mozilla/5.0 (Linux; Android 4.4; Nexus 7/JSS15R) AppleWebKit/537.36 (KHTML,like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36\\\"\"\"\""
            ,"agent.(1)product=\"Mozilla/5.0 (Linux; Android 4.4; Nexus 7/JSS15R)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(Linux; Android 4.4; Nexus 7/JSS15R)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Linux\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"Linux\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Android 4.4\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"Android 4.4\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"Android\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"4.4\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Nexus 7/JSS15R\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Nexus 7/JSS15R\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Nexus\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"7\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(2)version=\"JSS15R\""
            ,"agent.(2)product=\"AppleWebKit/537.36 (KHTML,like Gecko)\""
            ,"agent.(2)product.(1)name=\"AppleWebKit\""
            ,"agent.(2)product.(1)version=\"537.36\""
            ,"agent.(2)product.(1)comments=\"(KHTML,like Gecko)\""
            ,"agent.(2)product.(1)comments.(1)entry=\"KHTML\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)text=\"KHTML\""
            ,"agent.(2)product.(1)comments.(2)entry=\"like Gecko\""
            ,"agent.(2)product.(1)comments.(2)entry.(1)text=\"like Gecko\""
            ,"agent.(3)product=\"Chrome/34.0.1847.114\""
            ,"agent.(3)product.(1)name=\"Chrome\""
            ,"agent.(3)product.(1)version=\"34.0.1847.114\""
            ,"agent.(4)product=\"Mobile Safari/537.36\""
            ,"agent.(4)product.(1)name=\"Mobile Safari\""
            ,"agent.(4)product.(1)version=\"537.36\""
        );

        validateUserAgent(
            "Mozilla/5.0 (Linux; U; Android 4.1.1; nl-nl; bq Edison 2 Build/1.0.1_20130805-14:02) AppleWebKit/534.30 (KHTML,like Gecko) Version/4.0 Safari/534.30"
            ,"agent=\"Mozilla/5.0 (Linux; U; Android 4.1.1; nl-nl; bq Edison 2 Build/1.0.1_20130805-14:02) AppleWebKit/534.30 (KHTML,like Gecko) Version/4.0 Safari/534.30\""
            ,"agent.(1)product=\"Mozilla/5.0 (Linux; U; Android 4.1.1; nl-nl; bq Edison 2 Build/1.0.1_20130805-14:02)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(Linux; U; Android 4.1.1; nl-nl; bq Edison 2 Build/1.0.1_20130805-14:02)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Linux\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"Linux\""
            ,"agent.(1)product.(1)comments.(2)entry=\"U\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)text=\"U\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Android 4.1.1\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Android 4.1.1\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Android\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"4.1.1\""
            ,"agent.(1)product.(1)comments.(4)entry=\"nl-nl\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)text=\"nl-nl\""
            ,"agent.(1)product.(1)comments.(5)entry=\"bq Edison 2 Build/1.0.1_20130805-14:02\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product=\"bq Edison 2\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)name=\"bq Edison\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)version=\"2\""
            ,"agent.(1)product.(1)comments.(5)entry.(2)product=\"Build/1.0.1_20130805-14:02\""
            ,"agent.(1)product.(1)comments.(5)entry.(2)product.(1)name=\"Build\""
            ,"agent.(1)product.(1)comments.(5)entry.(2)product.(1)version=\"1.0.1_20130805-14:02\""
            ,"agent.(1)product.(1)comments.(5)entry.(2)product.(1)version.(1)keyvalue=\"1.0.1_20130805-14:02\""
            ,"agent.(1)product.(1)comments.(5)entry.(2)product.(1)version.(1)keyvalue.(1)key=\"1.0.1_20130805-14\""
            ,"agent.(1)product.(1)comments.(5)entry.(2)product.(1)version.(1)keyvalue.(1)version=\"02\""
            ,"agent.(2)product=\"AppleWebKit/534.30 (KHTML,like Gecko)\""
            ,"agent.(2)product.(1)name=\"AppleWebKit\""
            ,"agent.(2)product.(1)version=\"534.30\""
            ,"agent.(2)product.(1)comments=\"(KHTML,like Gecko)\""
            ,"agent.(2)product.(1)comments.(1)entry=\"KHTML\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)text=\"KHTML\""
            ,"agent.(2)product.(1)comments.(2)entry=\"like Gecko\""
            ,"agent.(2)product.(1)comments.(2)entry.(1)text=\"like Gecko\""
            ,"agent.(3)product=\"Version/4.0\""
            ,"agent.(3)product.(1)name=\"Version\""
            ,"agent.(3)product.(1)version=\"4.0\""
            ,"agent.(4)product=\"Safari/534.30\""
            ,"agent.(4)product.(1)name=\"Safari\""
            ,"agent.(4)product.(1)version=\"534.30\""
        );

        validateUserAgent(
            "Mozilla/5.0 (Series40; Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1; Profile/MIDP-2.1 Configuration/CLDC-1.1) Gecko/20100401 S40OviBrowser/3.1.1.0.27"
            ,"agent=\"Mozilla/5.0 (Series40; Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1; Profile/MIDP-2.1 Configuration/CLDC-1.1) Gecko/20100401 S40OviBrowser/3.1.1.0.27\""
            ,"agent.(1)product=\"Mozilla/5.0 (Series40; Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1; Profile/MIDP-2.1 Configuration/CLDC-1.1)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(Series40; Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1; Profile/MIDP-2.1 Configuration/CLDC-1.1)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Series40\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"Series40\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"Nokia501.2\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"11.1.3\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(2)version=\"java_runtime_version=Nokia_Asha_1_1_1\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(2)version.(1)keyvalue=\"java_runtime_version=Nokia_Asha_1_1_1\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(2)version.(1)keyvalue.(1)key=\"java_runtime_version\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(2)version.(1)keyvalue.(1)version=\"Nokia_Asha_1_1_1\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Profile/MIDP-2.1 Configuration/CLDC-1.1\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Profile/MIDP-2.1\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Profile\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"MIDP-2.1\""
            ,"agent.(1)product.(1)comments.(3)entry.(2)product=\"Configuration/CLDC-1.1\""
            ,"agent.(1)product.(1)comments.(3)entry.(2)product.(1)name=\"Configuration\""
            ,"agent.(1)product.(1)comments.(3)entry.(2)product.(1)version=\"CLDC-1.1\""
            ,"agent.(2)product=\"Gecko/20100401\""
            ,"agent.(2)product.(1)name=\"Gecko\""
            ,"agent.(2)product.(1)version=\"20100401\""
            ,"agent.(3)product=\"S40OviBrowser/3.1.1.0.27\""
            ,"agent.(3)product.(1)name=\"S40OviBrowser\""
            ,"agent.(3)product.(1)version=\"3.1.1.0.27\""
        );

        validateUserAgent(
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; " +
                "IWSS25:J6HBo2OOPD50bdr79CgSjLigxKUK+idfrxaKO1+FNCY=; " +
                "GTB7.4; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C; .NET4.0E)"
            ,"agent=\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; IWSS25:J6HBo2OOPD50bdr79CgSjLigxKUK+idfrxaKO1+FNCY=; GTB7.4; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C; .NET4.0E)\""
            ,"agent.(1)product=\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; IWSS25:J6HBo2OOPD50bdr79CgSjLigxKUK+idfrxaKO1+FNCY=; GTB7.4; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C; .NET4.0E)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"4.0\""
            ,"agent.(1)product.(1)comments=\"(compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; IWSS25:J6HBo2OOPD50bdr79CgSjLigxKUK+idfrxaKO1+FNCY=; GTB7.4; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C; .NET4.0E)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"compatible\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"compatible\""
            ,"agent.(1)product.(1)comments.(2)entry=\"MSIE 8.0\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"MSIE 8.0\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"MSIE\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"8.0\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Windows NT 5.1\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Windows NT 5.1\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Windows NT\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"5.1\""
            ,"agent.(1)product.(1)comments.(4)entry=\"Trident/4.0\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product=\"Trident/4.0\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product.(1)name=\"Trident\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product.(1)version=\"4.0\""
            ,"agent.(1)product.(1)comments.(5)entry=\"IWSS25:J6HBo2OOPD50bdr79CgSjLigxKUK+idfrxaKO1+FNCY=\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue=\"IWSS25:J6HBo2OOPD50bdr79CgSjLigxKUK+idfrxaKO1+FNCY=\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue.(1)key=\"IWSS25\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue.(2)base64=\"J6HBo2OOPD50bdr79CgSjLigxKUK+idfrxaKO1+FNCY=\""
            ,"agent.(1)product.(1)comments.(6)entry=\"GTB7.4\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)text=\"GTB7.4\""
            ,"agent.(1)product.(1)comments.(7)entry=\".NET CLR 2.0.50727\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)product=\".NET CLR 2.0.50727\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)product.(1)name=\".NET CLR\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)product.(1)version=\"2.0.50727\""
            ,"agent.(1)product.(1)comments.(8)entry=\".NET CLR 3.0.4506.2152\""
            ,"agent.(1)product.(1)comments.(8)entry.(1)product=\".NET CLR 3.0.4506.2152\""
            ,"agent.(1)product.(1)comments.(8)entry.(1)product.(1)name=\".NET CLR\""
            ,"agent.(1)product.(1)comments.(8)entry.(1)product.(1)version=\"3.0.4506.2152\""
            ,"agent.(1)product.(1)comments.(9)entry=\".NET CLR 3.5.30729\""
            ,"agent.(1)product.(1)comments.(9)entry.(1)product=\".NET CLR 3.5.30729\""
            ,"agent.(1)product.(1)comments.(9)entry.(1)product.(1)name=\".NET CLR\""
            ,"agent.(1)product.(1)comments.(9)entry.(1)product.(1)version=\"3.5.30729\""
            ,"agent.(1)product.(1)comments.(10)entry=\".NET4.0C\""
            ,"agent.(1)product.(1)comments.(10)entry.(1)text=\".NET4.0C\""
            ,"agent.(1)product.(1)comments.(11)entry=\".NET4.0E\""
            ,"agent.(1)product.(1)comments.(11)entry.(1)text=\".NET4.0E\""
        );

        validateUserAgent(
            "Airmail 1.3.3 rv:237 (Macintosh; Mac OS X 10.9.3; nl_NL)"
            ,"agent=\"Airmail 1.3.3 rv:237 (Macintosh; Mac OS X 10.9.3; nl_NL)\""
            ,"agent.(1)product=\"Airmail 1.3.3 rv:237 (Macintosh; Mac OS X 10.9.3; nl_NL)\""
            ,"agent.(1)product.(1)name=\"Airmail\""
            ,"agent.(1)product.(1)version=\"1.3.3\""
            ,"agent.(1)product.(2)version=\"rv:237\""
            ,"agent.(1)product.(2)version.(1)keyvalue=\"rv:237\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(2)version.(1)keyvalue.(1)version=\"237\""
            ,"agent.(1)product.(1)comments=\"(Macintosh; Mac OS X 10.9.3; nl_NL)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Macintosh\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"Macintosh\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Mac OS X 10.9.3\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"Mac OS X 10.9.3\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"Mac OS X\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"10.9.3\""
            ,"agent.(1)product.(1)comments.(3)entry=\"nl_NL\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)text=\"nl_NL\""
        );
        validateUserAgent(
            " --user-agent=Mozilla/5.0 (Linux; U; Android 4.0.3; en-gb; ARCHOS 80G9 Build/Deode@4.0.7) AppleWebKit/534.30 (KHTML,like Gecko) Version/4.0 Safari/534.30"
            ,"agent=\"--user-agent=Mozilla/5.0 (Linux; U; Android 4.0.3; en-gb; ARCHOS 80G9 Build/Deode@4.0.7) AppleWebKit/534.30 (KHTML,like Gecko) Version/4.0 Safari/534.30\""
            ,"agent.(1)product=\"Mozilla/5.0 (Linux; U; Android 4.0.3; en-gb; ARCHOS 80G9 Build/Deode@4.0.7)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(Linux; U; Android 4.0.3; en-gb; ARCHOS 80G9 Build/Deode@4.0.7)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Linux\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"Linux\""
            ,"agent.(1)product.(1)comments.(2)entry=\"U\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)text=\"U\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Android 4.0.3\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Android 4.0.3\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Android\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"4.0.3\""
            ,"agent.(1)product.(1)comments.(4)entry=\"en-gb\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)text=\"en-gb\""
            ,"agent.(1)product.(1)comments.(5)entry=\"ARCHOS 80G9 Build/Deode@4.0.7\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product=\"ARCHOS 80G9\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)name=\"ARCHOS\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)version=\"80G9\""
            ,"agent.(1)product.(1)comments.(5)entry.(2)product=\"Build/Deode@4.0.7\""
            ,"agent.(1)product.(1)comments.(5)entry.(2)product.(1)name=\"Build\""
            ,"agent.(1)product.(1)comments.(5)entry.(2)product.(1)version=\"Deode@4.0.7\""
            ,"agent.(2)product=\"AppleWebKit/534.30 (KHTML,like Gecko)\""
            ,"agent.(2)product.(1)name=\"AppleWebKit\""
            ,"agent.(2)product.(1)version=\"534.30\""
            ,"agent.(2)product.(1)comments=\"(KHTML,like Gecko)\""
            ,"agent.(2)product.(1)comments.(1)entry=\"KHTML\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)text=\"KHTML\""
            ,"agent.(2)product.(1)comments.(2)entry=\"like Gecko\""
            ,"agent.(2)product.(1)comments.(2)entry.(1)text=\"like Gecko\""
            ,"agent.(3)product=\"Version/4.0\""
            ,"agent.(3)product.(1)name=\"Version\""
            ,"agent.(3)product.(1)version=\"4.0\""
            ,"agent.(4)product=\"Safari/534.30\""
            ,"agent.(4)product.(1)name=\"Safari\""
            ,"agent.(4)product.(1)version=\"534.30\""
        );

        // Combined testcase (does 'everything')
        validateUserAgent(
            "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; Name = MASB; rv:11.0; Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1; SIMBAR={A43F3165-FAC1-11E1-8828-00123F6EDBB1}; ;http://bla.bla.com/page.html ; email:aap@noot.nl) like Gecko"
            ,"agent=\"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; Name = MASB; rv:11.0; Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1; SIMBAR={A43F3165-FAC1-11E1-8828-00123F6EDBB1}; ;http://bla.bla.com/page.html ; email:aap@noot.nl) like Gecko\""
            ,"agent.(1)product=\"Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; Name = MASB; rv:11.0; Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1; SIMBAR={A43F3165-FAC1-11E1-8828-00123F6EDBB1}; ;http://bla.bla.com/page.html ; email:aap@noot.nl)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(Windows NT 6.1; WOW64; Trident/7.0; Name = MASB; rv:11.0; Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1; SIMBAR={A43F3165-FAC1-11E1-8828-00123F6EDBB1}; ;http://bla.bla.com/page.html ; email:aap@noot.nl)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Windows NT 6.1\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product=\"Windows NT 6.1\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)name=\"Windows NT\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)version=\"6.1\""
            ,"agent.(1)product.(1)comments.(2)entry=\"WOW64\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)text=\"WOW64\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Trident/7.0\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Trident/7.0\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Trident\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"7.0\""
            ,"agent.(1)product.(1)comments.(4)entry=\"Name = MASB\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue=\"Name = MASB\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue.(1)key=\"Name\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)keyvalue.(2)text=\"MASB\""
            ,"agent.(1)product.(1)comments.(5)entry=\"rv:11.0\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue=\"rv:11.0\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue.(1)key=\"rv\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)keyvalue.(1)version=\"11.0\""
            ,"agent.(1)product.(1)comments.(6)entry=\"Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product=\"Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)name=\"Nokia501.2\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(1)version=\"11.1.3\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(2)version=\"java_runtime_version=Nokia_Asha_1_1_1\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(2)version.(1)keyvalue=\"java_runtime_version=Nokia_Asha_1_1_1\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(2)version.(1)keyvalue.(1)key=\"java_runtime_version\""
            ,"agent.(1)product.(1)comments.(6)entry.(1)product.(2)version.(1)keyvalue.(1)version=\"Nokia_Asha_1_1_1\""
            ,"agent.(1)product.(1)comments.(7)entry=\"SIMBAR={A43F3165-FAC1-11E1-8828-00123F6EDBB1}\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)keyvalue=\"SIMBAR={A43F3165-FAC1-11E1-8828-00123F6EDBB1}\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)keyvalue.(1)key=\"SIMBAR\""
            ,"agent.(1)product.(1)comments.(7)entry.(1)keyvalue.(2)uuid=\"A43F3165-FAC1-11E1-8828-00123F6EDBB1\""
            ,"agent.(1)product.(1)comments.(8)entry=\"\""
            ,"agent.(1)product.(1)comments.(8)entry.(1)text=\"\""
            ,"agent.(1)product.(1)comments.(9)entry=\"http://bla.bla.com/page.html\""
            ,"agent.(1)product.(1)comments.(9)entry.(1)url=\"http://bla.bla.com/page.html\""
            ,"agent.(1)product.(1)comments.(10)entry=\"email:aap@noot.nl\""
            ,"agent.(1)product.(1)comments.(10)entry.(1)keyvalue=\"email:aap@noot.nl\""
            ,"agent.(1)product.(1)comments.(10)entry.(1)keyvalue.(1)key=\"email\""
            ,"agent.(1)product.(1)comments.(10)entry.(1)keyvalue.(2)email=\"aap@noot.nl\""
            ,"agent.(2)text=\"like Gecko\""
        );

        // This is the pattern we encountered for the first time with the PirateBrowser
        validateUserAgent(
            "PB0.6b Mozilla/5.0 (foo)"
            ,"agent=\"PB0.6b Mozilla/5.0 (foo)\""
            ,"agent.(1)product=\"PB0.6b Mozilla/5.0 (foo)\""
            ,"agent.(1)product.(1)name=\"PB0.6b Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(foo)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"foo\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"foo\""
        );

        validateUserAgent( // The last one was parsd and flattened incorrectly
            "MT6592/V1 Linux/3.4.39 Mobile Safari/534.30 System/Android 4.2.2"
            ,"agent=\"MT6592/V1 Linux/3.4.39 Mobile Safari/534.30 System/Android 4.2.2\""
            ,"agent.(1)product=\"MT6592/V1\""
            ,"agent.(1)product.(1)name=\"MT6592\""
            ,"agent.(1)product.(1)version=\"V1\""

            ,"agent.(2)product=\"Linux/3.4.39\""
            ,"agent.(2)product.(1)name=\"Linux\""
            ,"agent.(2)product.(1)version=\"3.4.39\""

            ,"agent.(3)product=\"Mobile Safari/534.30\""
            ,"agent.(3)product.(1)name=\"Mobile Safari\""
            ,"agent.(3)product.(1)version=\"534.30\""

            ,"agent.(4)product=\"System/Android 4.2.2\""
            ,"agent.(4)product.(1)name=\"System\""
            ,"agent.(4)product.(1)version=\"Android\""
            ,"agent.(4)product.(2)version=\"4.2.2\""
        );

        validateUserAgent(
            "Mozilla/5.0 (Linux; U; Android 4.2.2; nl-; YC-3135D Build/JDQ39)"
            ,"agent=\"Mozilla/5.0 (Linux; U; Android 4.2.2; nl-; YC-3135D Build/JDQ39)\""
            ,"agent.(1)product=\"Mozilla/5.0 (Linux; U; Android 4.2.2; nl-; YC-3135D Build/JDQ39)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(Linux; U; Android 4.2.2; nl-; YC-3135D Build/JDQ39)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Linux\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"Linux\""
            ,"agent.(1)product.(1)comments.(2)entry=\"U\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)text=\"U\""
            ,"agent.(1)product.(1)comments.(3)entry=\"Android 4.2.2\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product=\"Android 4.2.2\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)name=\"Android\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)product.(1)version=\"4.2.2\""
            ,"agent.(1)product.(1)comments.(4)entry=\"nl-\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)text=\"nl-\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product=\"YC-3135D Build/JDQ39\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)name=\"YC-3135D Build\""
            ,"agent.(1)product.(1)comments.(5)entry.(1)product.(1)version=\"JDQ39\""
        );

    }

    @Test
    public void testFlatteningParsingErrorCases() throws Exception {
        validateUserAgent(
            "InetURL:/1.0"
            ,"agent=\"InetURL:/1.0\""
        );

        // Note that the last part (starting after the Safari product) is bad and causes a parse error
         validateUserAgent(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.20 (KHTML,like Gecko) Chrome/19.0.1036.7 Safari/535.20 +rr:1511) +x10955"
            ,"agent=\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.20 (KHTML,like Gecko) Chrome/19.0.1036.7 Safari/535.20 +rr:1511) +x10955\""
            ,"agent.(1)product=\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(Macintosh; Intel Mac OS X 10_7_3)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"Macintosh\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"Macintosh\""
            ,"agent.(1)product.(1)comments.(2)entry=\"Intel Mac OS X 10_7_3\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"Intel Mac OS X 10_7_3\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"Intel Mac OS X\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"10_7_3\""
            ,"agent.(2)product=\"AppleWebKit/535.20 (KHTML,like Gecko)\""
            ,"agent.(2)product.(1)name=\"AppleWebKit\""
            ,"agent.(2)product.(1)version=\"535.20\""
            ,"agent.(2)product.(1)comments=\"(KHTML,like Gecko)\""
            ,"agent.(2)product.(1)comments.(1)entry=\"KHTML\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)text=\"KHTML\""
            ,"agent.(2)product.(1)comments.(2)entry=\"like Gecko\""
            ,"agent.(2)product.(1)comments.(2)entry.(1)text=\"like Gecko\""
            ,"agent.(3)product=\"Chrome/19.0.1036.7\""
            ,"agent.(3)product.(1)name=\"Chrome\""
            ,"agent.(3)product.(1)version=\"19.0.1036.7\""
            ,"agent.(4)product=\"Safari/535.20\""
            ,"agent.(4)product.(1)name=\"Safari\""
            ,"agent.(4)product.(1)version=\"535.20\""
            ,"agent.(5)keyvalue=\"rr:1511\""
            ,"agent.(5)keyvalue.(1)key=\"rr\""
            ,"agent.(5)keyvalue.(1)version=\"1511\""
        );

        // Although VERY strange we do see this in real life
        validateUserAgent(
            "MT6582_TD/V1 Linux/3.4.67 Android/4.4.2 Release/ Browser/AppleWebKit537.36 Chrome/30.0.0.0 Mobile Safari/537.36 System/Android 4.4.2"
            ,"agent.(1)product=\"MT6582_TD/V1\""
            ,"agent.(1)product.(1)name=\"MT6582_TD\""
            ,"agent.(1)product.(1)version=\"V1\""

            ,"agent.(2)product=\"Linux/3.4.67\""
            ,"agent.(2)product.(1)name=\"Linux\""
            ,"agent.(2)product.(1)version=\"3.4.67\""

            ,"agent.(3)product=\"Android/4.4.2\""
            ,"agent.(3)product.(1)name=\"Android\""
            ,"agent.(3)product.(1)version=\"4.4.2\""

            // Although this looks incorrect to a human this is the best parse possible.
            ,"agent.(4)product=\"Release/ Browser/AppleWebKit537.36\""
            ,"agent.(4)product.(1)name=\"Release\""
            ,"agent.(4)product.(1)version=\"Browser\""
            ,"agent.(4)product.(2)version=\"AppleWebKit537.36\""

            ,"agent.(5)product=\"Chrome/30.0.0.0\""
            ,"agent.(5)product.(1)name=\"Chrome\""
            ,"agent.(5)product.(1)version=\"30.0.0.0\""

            ,"agent.(6)product=\"Mobile Safari/537.36\""
            ,"agent.(6)product.(1)name=\"Mobile Safari\""
            ,"agent.(6)product.(1)version=\"537.36\""

            ,"agent.(7)product=\"System/Android 4.4.2\""
            ,"agent.(7)product.(1)name=\"System\""
            ,"agent.(7)product.(1)version=\"Android\""
            ,"agent.(7)product.(2)version=\"4.4.2\""
        );

        // Although VERY strange we do see this in real life
        validateUserAgent(
            "Mozilla/5.0 (000000000; 00000 000 00 0 000000) DDDDDDDDDDDDDDDDDDDD DDDDDDD DDDD DDDDDD DDDDDDDDDDDDD DDDDDDDDDDDDDDDD"
            ,"agent=\"Mozilla/5.0 (000000000; 00000 000 00 0 000000) DDDDDDDDDDDDDDDDDDDD DDDDDDD DDDD DDDDDD DDDDDDDDDDDDD DDDDDDDDDDDDDDDD\""
            ,"agent.(1)product=\"Mozilla/5.0 (000000000; 00000 000 00 0 000000)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(000000000; 00000 000 00 0 000000)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"000000000\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"000000000\""
            ,"agent.(1)product.(1)comments.(2)entry=\"00000 000 00 0 000000\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"00000 000 00 0 000000\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"00000\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"000\""
            ,"agent.(2)text=\"DDDDDDDDDDDDDDDDDDDD DDDDDDD DDDD DDDDDD DDDDDDDDDDDDD DDDDDDDDDDDDDDDD\""
        );

        validateUserAgent(
            "Mozilla/5.0 (iPad; CPU OS 8_2 like Mac OS X) AppleWebKit/600.1.4 (KHTML,like Gecko) Mobile/12D508 [FBAN/FBIOS;FBAV/27.0.0.10.12;FBBV/8291884;FBDV/iPad4,1;FBMD/iPad;FBSN/iP;FBSV/8.2;FBSS/2; FBCR/;FBID/tablet;FBLC/nl_NL;FBOP/1]"
            ,"agent=\"Mozilla/5.0 (iPad; CPU OS 8_2 like Mac OS X) AppleWebKit/600.1.4 (KHTML,like Gecko) Mobile/12D508 [FBAN/FBIOS;FBAV/27.0.0.10.12;FBBV/8291884;FBDV/iPad4,1;FBMD/iPad;FBSN/iP;FBSV/8.2;FBSS/2; FBCR/;FBID/tablet;FBLC/nl_NL;FBOP/1]\""
            ,"agent.(1)product=\"Mozilla/5.0 (iPad; CPU OS 8_2 like Mac OS X)\""
            ,"agent.(1)product.(1)name=\"Mozilla\""
            ,"agent.(1)product.(1)version=\"5.0\""
            ,"agent.(1)product.(1)comments=\"(iPad; CPU OS 8_2 like Mac OS X)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"iPad\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)text=\"iPad\""
            ,"agent.(1)product.(1)comments.(2)entry=\"CPU OS 8_2 like Mac OS X\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product=\"CPU OS 8_2\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)name=\"CPU OS\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)product.(1)version=\"8_2\""
            ,"agent.(1)product.(1)comments.(2)entry.(2)text=\"like Mac OS X\""
            ,"agent.(2)product=\"AppleWebKit/600.1.4 (KHTML,like Gecko)\""
            ,"agent.(2)product.(1)name=\"AppleWebKit\""
            ,"agent.(2)product.(1)version=\"600.1.4\""
            ,"agent.(2)product.(1)comments=\"(KHTML,like Gecko)\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)text=\"KHTML\""
            ,"agent.(2)product.(1)comments.(2)entry.(1)text=\"like Gecko\""
            ,"agent.(3)product=\"Mobile/12D508 [FBAN/FBIOS;FBAV/27.0.0.10.12;FBBV/8291884;FBDV/iPad4,1;FBMD/iPad;FBSN/iP;FBSV/8.2;FBSS/2; FBCR/;FBID/tablet;FBLC/nl_NL;FBOP/1]\""
            ,"agent.(3)product.(1)name=\"Mobile\""
            ,"agent.(3)product.(1)version=\"12D508\""
            ,"agent.(3)product.(1)comments=\"[FBAN/FBIOS;FBAV/27.0.0.10.12;FBBV/8291884;FBDV/iPad4,1;FBMD/iPad;FBSN/iP;FBSV/8.2;FBSS/2; FBCR/;FBID/tablet;FBLC/nl_NL;FBOP/1]\""
            ,"agent.(3)product.(1)comments.(1)entry=\"FBAN/FBIOS\""
            ,"agent.(3)product.(1)comments.(1)entry.(1)product=\"FBAN/FBIOS\""
            ,"agent.(3)product.(1)comments.(1)entry.(1)product.(1)name=\"FBAN\""
            ,"agent.(3)product.(1)comments.(1)entry.(1)product.(1)version=\"FBIOS\""
            ,"agent.(3)product.(1)comments.(2)entry=\"FBAV/27.0.0.10.12\""
            ,"agent.(3)product.(1)comments.(2)entry.(1)product=\"FBAV/27.0.0.10.12\""
            ,"agent.(3)product.(1)comments.(2)entry.(1)product.(1)name=\"FBAV\""
            ,"agent.(3)product.(1)comments.(2)entry.(1)product.(1)version=\"27.0.0.10.12\""
            ,"agent.(3)product.(1)comments.(3)entry=\"FBBV/8291884\""
            ,"agent.(3)product.(1)comments.(3)entry.(1)product=\"FBBV/8291884\""
            ,"agent.(3)product.(1)comments.(3)entry.(1)product.(1)name=\"FBBV\""
            ,"agent.(3)product.(1)comments.(3)entry.(1)product.(1)version=\"8291884\""
            ,"agent.(3)product.(1)comments.(4)entry=\"FBDV/iPad4,1\""
            ,"agent.(3)product.(1)comments.(4)entry.(1)product=\"FBDV/iPad4,1\""
            ,"agent.(3)product.(1)comments.(4)entry.(1)product.(1)name=\"FBDV\""
            ,"agent.(3)product.(1)comments.(4)entry.(1)product.(1)version=\"iPad4,1\""
            ,"agent.(3)product.(1)comments.(5)entry=\"FBMD/iPad\""
            ,"agent.(3)product.(1)comments.(5)entry.(1)product=\"FBMD/iPad\""
            ,"agent.(3)product.(1)comments.(5)entry.(1)product.(1)name=\"FBMD\""
            ,"agent.(3)product.(1)comments.(5)entry.(1)product.(1)version=\"iPad\""
            ,"agent.(3)product.(1)comments.(6)entry=\"FBSN/iP\""
            ,"agent.(3)product.(1)comments.(6)entry.(1)product=\"FBSN/iP\""
            ,"agent.(3)product.(1)comments.(6)entry.(1)product.(1)name=\"FBSN\""
            ,"agent.(3)product.(1)comments.(6)entry.(1)product.(1)version=\"iP\""
            ,"agent.(3)product.(1)comments.(7)entry=\"FBSV/8.2\""
            ,"agent.(3)product.(1)comments.(7)entry.(1)product=\"FBSV/8.2\""
            ,"agent.(3)product.(1)comments.(7)entry.(1)product.(1)name=\"FBSV\""
            ,"agent.(3)product.(1)comments.(7)entry.(1)product.(1)version=\"8.2\""
            ,"agent.(3)product.(1)comments.(8)entry=\"FBSS/2\""
            ,"agent.(3)product.(1)comments.(8)entry.(1)product=\"FBSS/2\""
            ,"agent.(3)product.(1)comments.(8)entry.(1)product.(1)name=\"FBSS\""
            ,"agent.(3)product.(1)comments.(8)entry.(1)product.(1)version=\"2\""
            ,"agent.(3)product.(1)comments.(9)entry=\"FBCR/\""
            ,"agent.(3)product.(1)comments.(9)entry.(1)product=\"FBCR/\""
            ,"agent.(3)product.(1)comments.(9)entry.(1)product.(1)name=\"FBCR\""
            ,"agent.(3)product.(1)comments.(10)entry=\"FBID/tablet\""
            ,"agent.(3)product.(1)comments.(10)entry.(1)product=\"FBID/tablet\""
            ,"agent.(3)product.(1)comments.(10)entry.(1)product.(1)name=\"FBID\""
            ,"agent.(3)product.(1)comments.(10)entry.(1)product.(1)version=\"tablet\""
            ,"agent.(3)product.(1)comments.(11)entry=\"FBLC/nl_NL\""
            ,"agent.(3)product.(1)comments.(11)entry.(1)product=\"FBLC/nl_NL\""
            ,"agent.(3)product.(1)comments.(11)entry.(1)product.(1)name=\"FBLC\""
            ,"agent.(3)product.(1)comments.(11)entry.(1)product.(1)version=\"nl_NL\""
            ,"agent.(3)product.(1)comments.(12)entry=\"FBOP/1\""
            ,"agent.(3)product.(1)comments.(12)entry.(1)product=\"FBOP/1\""
            ,"agent.(3)product.(1)comments.(12)entry.(1)product.(1)name=\"FBOP\""
            ,"agent.(3)product.(1)comments.(12)entry.(1)product.(1)version=\"1\""
        );

    }

    @Test
    public void testFlatteningExtremeTestCase() throws Exception {

        validateUserAgent(
            "0n3 Niels Basjes/1/2(+http://niels.basjes.nl/index.html?foo=1&bar=2#marker (http://github.com) ; foo ; bar@baz.com ; Mozilla Mozilla/4.0((Windows NT,etc.)) CE-HTML/1.0 Config(L:nld,CC:BEL) NETRANGEMMH)info@example.com (Yadda)"
            ,"agent.(1)product.(1)name=\"0n3 Niels Basjes\""
            ,"agent.(1)product.(1)version=\"1\""
            ,"agent.(1)product.(2)version=\"2\""
            ,"agent.(1)product.(1)comments=\"(+http://niels.basjes.nl/index.html?foo=1&bar=2#marker (http://github.com) ; foo ; bar@baz.com ; Mozilla Mozilla/4.0((Windows NT,etc.)) CE-HTML/1.0 Config(L:nld,CC:BEL) NETRANGEMMH)\""
            ,"agent.(1)product.(1)comments.(1)entry=\"http://niels.basjes.nl/index.html?foo=1&bar=2#marker (http://github.com)\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product=\"http://niels.basjes.nl/index.html?foo=1&bar=2#marker (http://github.com)\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)name=\"http://niels.basjes.nl/index.html?foo=1&bar=2#marker\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)name.(1)url=\"http://niels.basjes.nl/index.html?foo=1&bar=2#marker\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)comments=\"(http://github.com)\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)comments.(1)entry=\"http://github.com\""
            ,"agent.(1)product.(1)comments.(1)entry.(1)product.(1)comments.(1)entry.(1)url=\"http://github.com\""
            ,"agent.(1)product.(1)comments.(2)entry=\"foo\""
            ,"agent.(1)product.(1)comments.(2)entry.(1)text=\"foo\""
            ,"agent.(1)product.(1)comments.(3)entry=\"bar@baz.com\""
            ,"agent.(1)product.(1)comments.(3)entry.(1)email=\"bar@baz.com\""
            ,"agent.(1)product.(1)comments.(4)entry=\"Mozilla Mozilla/4.0((Windows NT,etc.)) CE-HTML/1.0 Config(L:nld,CC:BEL) NETRANGEMMH\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product.(1)name=\"Mozilla Mozilla\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product.(1)version=\"4.0\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product.(1)comments=\"((Windows NT,etc.))\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product.(1)comments.(1)entry=\"(Windows NT,etc.)\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product.(1)comments.(1)entry.(1)comments=\"(Windows NT,etc.)\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product.(1)comments.(1)entry.(1)comments.(1)entry=\"Windows NT\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product.(1)comments.(1)entry.(1)comments.(1)entry.(1)text=\"Windows NT\""
            ,"agent.(1)product.(1)comments.(4)entry.(1)product.(1)comments.(1)entry.(1)comments.(2)entry.(1)text=\"etc.\""
            ,"agent.(2)product=\"info@example.com (Yadda)\""
            ,"agent.(2)product.(1)name=\"info@example.com\""
            ,"agent.(2)product.(1)name.(1)email=\"info@example.com\""
            ,"agent.(2)product.(1)comments=\"(Yadda)\""
            ,"agent.(2)product.(1)comments.(1)entry=\"Yadda\""
            ,"agent.(2)product.(1)comments.(1)entry.(1)text=\"Yadda\""
        );

    }

    private void validateUserAgent(String useragent, String... requiredValues) {

        boolean developmentMode = requiredValues.length == 0;

        if (developmentMode) {
            LOG.info("Developing {}", useragent);
        } else {
            LOG.info("Validating {}", useragent);
        }

        StringBuilder sb = new StringBuilder(2048);
        sb.append("\n");
        sb.append("|====================================== \n");
        sb.append("| ").append(useragent).append('\n');
        sb.append("|-------------------------------------- \n");

        GetAllPathsAnalyzer analyzer = UserAgentAnalyzer.getAllPathsAnalyzer(useragent);
        UserAgent parsedUseragent = analyzer.getResult();

        if (parsedUseragent.hasAmbiguity()) {
            sb.append("| Ambiguity \n");
        }
        if (parsedUseragent.hasSyntaxError()) {
            sb.append("| Syntax Error \n");
        }

        List<String> paths = analyzer.getValues();

        boolean ok = true;
        for (String value : requiredValues) {
            if (paths.contains(value)) {
                sb.append("|             : ").append(value).append('\n');
            } else {
                sb.append("| Missing --> : ").append(value).append('\n');
                ok = false;
            }
        }

        if (requiredValues.length == 0 || !ok) {
            sb.append("|-------------------------------------- \n");
            for (String value : paths) {
                if (value.contains("=")) {
                    sb.append("      ,\"").append(value.replaceAll("\\\"", "\\\\\"")).append("\"\n");
                }
            }
            sb.append("|====================================== \n");
        }

        if (developmentMode) {
            LOG.info(sb.toString());
            return;
        }
        if (!ok) {
            LOG.error(sb.toString());
            fail("Not everything was found");
        }
    }

}
