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

package nl.basjes.parse.useragent.clienthints;

import nl.basjes.parse.useragent.clienthints.ClientHints.Brand;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUa;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaArch;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaBitness;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaFullVersion;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaFullVersionList;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaMobile;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaModel;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaPlatform;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaPlatformVersion;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaWoW64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TestClientHintsParsing {

    private static final Logger LOG = LogManager.getFormatterLogger(TestClientHintsParsing.class);
    private static final Random RANDOM = new Random(42); // Repeatable random !

    // ------------------------------------------

    private static final String SEC_CH_UA = randomCase(ParseSecChUa.HEADER_FIELD);

    @Test
    void testSecChUa1() {
        ClientHints clientHints = parse(SEC_CH_UA, "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"99\", \"Google Chrome\";v=\"99\"");
        List<Brand> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Chromium",        brands.get(0).getName());
        assertEquals("99",              brands.get(0).getVersion());
        assertEquals("Google Chrome",   brands.get(1).getName());
        assertEquals("99",              brands.get(1).getVersion());
    }

    @Test
    void testSecChUa2() {
        ClientHints clientHints = parse(SEC_CH_UA, "\"\\Not;A Brand\";v=\"99\", \"Google Chrome\";v=\"85\", \"Chromium\";v=\"85\"");
        List<Brand> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Google Chrome",   brands.get(0).getName());
        assertEquals("85",              brands.get(0).getVersion());
        assertEquals("Chromium",        brands.get(1).getName());
        assertEquals("85",              brands.get(1).getVersion());
    }

    @Test
    void testSecChUa3() {
        ClientHints clientHints = parse(SEC_CH_UA, "\"Chrome\"; v=\"73\", \"(Not;Browser\"; v=\"12\"");
        List<Brand> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(1, brands.size());
        assertEquals("Chrome",          brands.get(0).getName());
        assertEquals("73",              brands.get(0).getVersion());
    }

    @Test
    void testSecChUa4() {
        ClientHints clientHints = parse(SEC_CH_UA, "\"Chrome\"; v=\"73\", \"(Not;Browser\"; v=\"12\", \"Chromium\"; v=\"74\"");
        List<Brand> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Chrome",          brands.get(0).getName());
        assertEquals("73",              brands.get(0).getVersion());
        assertEquals("Chromium",        brands.get(1).getName());
        assertEquals("74",              brands.get(1).getVersion());
    }

    @Test
    void testSecChUa5() {
        ClientHints clientHints = parse(SEC_CH_UA, "\"(Not;Browser\"; v=\"12\", \"Chromium\"; v=\"73\"");
        List<Brand> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(1, brands.size());
        assertEquals("Chromium",        brands.get(0).getName());
        assertEquals("73",              brands.get(0).getVersion());
    }

    @Test
    void testSecChUa6() {
        ClientHints clientHints = parse(SEC_CH_UA, "\"Chrome\"; v=\"73\", \"Xwebs mega\"; v=\"60\", \"Chromium\"; v=\"73\", \"(Not;Browser\"; v=\"12\"");
        List<Brand> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(3, brands.size());
        assertEquals("Chrome",          brands.get(0).getName());
        assertEquals("73",              brands.get(0).getVersion());
        assertEquals("Xwebs mega",      brands.get(1).getName());
        assertEquals("60",              brands.get(1).getVersion());
        assertEquals("Chromium",        brands.get(2).getName());
        assertEquals("73",              brands.get(2).getVersion());
    }

    @Test
    void testSecChUa7() {
        // From https://wicg.github.io/ua-client-hints/#examples
        ClientHints clientHints = parse(SEC_CH_UA, "\"Examplary Browser\"; v=\"73\", \";Not?A.Brand\"; v=\"27\"");
        List<Brand> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(1, brands.size());
        assertEquals("Examplary Browser",   brands.get(0).getName());
        assertEquals("73",                  brands.get(0).getVersion());
    }

    @Test
    void testSecChUa8() {
        ClientHints clientHints = parse(SEC_CH_UA, "\"Examplary Browser\"; v=\"73\"");
        List<Brand> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(1, brands.size());
        assertEquals("Examplary Browser",   brands.get(0).getName());
        assertEquals("73",                  brands.get(0).getVersion());
    }

    @Test
    void testSecChUaBadValue() {
        assertNull(parse(SEC_CH_UA, "xxx").getBrands());
    }

    @Test
    void testSecChUaAlmostEmpty() {
        assertNull(parse(SEC_CH_UA, "\"(Not;Browser\"; v=\"12\"").getBrands());
    }

    @Test
    void testSecChUaEmpty() {
        assertNull(parse(SEC_CH_UA, "").getBrands());
    }

    @Test
    void testSecChUaNull() {
        assertNull(parse(SEC_CH_UA, null).getBrands());
    }

    // ------------------------------------------

    private static final String SEC_CH_UA_ARCH = randomCase(ParseSecChUaArch.HEADER_FIELD);

    @Test
    void testSecChUaArch() {
        ClientHints clientHints = parse(SEC_CH_UA_ARCH, "\"x86\"");
        String architecture = clientHints.getArchitecture();
        assertNotNull(architecture);
        assertEquals("x86", architecture);
    }

    @Test
    void testSecChUaArchEmpty() {
        assertNull(parse(SEC_CH_UA_ARCH, "\"\"").getArchitecture());
    }

    @Test
    void testSecChUaArchNull() {
        assertNull(parse(SEC_CH_UA_ARCH, null).getArchitecture());
    }
    // ------------------------------------------

    private static final String SEC_CH_UA_BITNESS = randomCase(ParseSecChUaBitness.HEADER_FIELD);

    @Test
    void testSecChUaBitness() {
        ClientHints clientHints = parse(SEC_CH_UA_BITNESS, "\"64\"");
        String bitnessitecture = clientHints.getBitness();
        assertNotNull(bitnessitecture);
        assertEquals("64", bitnessitecture);
    }

    @Test
    void testSecChUaBitnessEmpty() {
        assertNull(parse(SEC_CH_UA_BITNESS, "\"\"").getBitness());
    }

    @Test
    void testSecChUaBitnessNull() {
        assertNull(parse(SEC_CH_UA_BITNESS, null).getBitness());
    }

    // ------------------------------------------

    private static final String SEC_CH_UA_FULL_VERSION = randomCase(ParseSecChUaFullVersion.HEADER_FIELD);

    @Test
    void testSecChUaFullVersion() {
        ClientHints clientHints = parse(SEC_CH_UA_FULL_VERSION, "\"100.0.4896.75\"");
        String architecture = clientHints.getFullVersion();
        assertEquals("100.0.4896.75", architecture);
    }

    @Test
    void testSecChUaFullVersionEmpty() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION, "\"\"").getFullVersion());
    }

    @Test
    void testSecChUaFullVersionNull() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION, null).getFullVersion());
    }

    // ------------------------------------------

    private static final String SEC_CH_UA_FULL_VERSION_LIST = randomCase(ParseSecChUaFullVersionList.HEADER_FIELD);

    @Test
    void testSecChUaFullVersionList() {
        ClientHints clientHints = parse(SEC_CH_UA_FULL_VERSION_LIST, "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"99.0.4844.51\", \"Google Chrome\";v=\"99.0.4844.51\"");
        List<Brand> brands = clientHints.getFullVersionList();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Chromium",        brands.get(0).getName());
        assertEquals("99.0.4844.51",    brands.get(0).getVersion());
        assertEquals("Google Chrome",   brands.get(1).getName());
        assertEquals("99.0.4844.51",    brands.get(1).getVersion());
    }

    @Test
    void testSecChUaFullVersionListExtraSpaces() {
        ClientHints clientHints = parse(SEC_CH_UA_FULL_VERSION_LIST, "  \" Not A;Brand\"  ;   v=\"99.0.0.0\"  ,   \"Chromium\"  ;  v  =  \"99.0.4844.51\"   ,    \"Google Chrome\"   ;   v  =  \"99.0.4844.51\"   ");
        List<Brand> brands = clientHints.getFullVersionList();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Chromium",        brands.get(0).getName());
        assertEquals("99.0.4844.51",    brands.get(0).getVersion());
        assertEquals("Google Chrome",   brands.get(1).getName());
        assertEquals("99.0.4844.51",    brands.get(1).getVersion());
    }

    @Test
    void testSecChUaFullVersionListBadValue() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION_LIST, "xxx").getFullVersionList());
    }

    @Test
    void testSecChUaFullVersionListAlmostEmpty() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION_LIST, "\" Not A;Brand\";v=\"99.0.0.0\"").getFullVersionList());
    }

    @Test
    void testSecChUaFullVersionListEmpty() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION_LIST, "").getFullVersionList());
    }

    @Test
    void testSecChUaFullVersionListNull() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION_LIST, null).getFullVersionList());
    }

    // ------------------------------------------

    private static final String SEC_CH_UA_MOBILE = randomCase(ParseSecChUaMobile.HEADER_FIELD);

    @Test
    void testSecChUaMobileT() {
        ClientHints clientHints = parse(SEC_CH_UA_MOBILE, "?1");
        Boolean mobile = clientHints.getMobile();
        assertEquals(TRUE, mobile);
    }

    @Test
    void testSecChUaMobileF() {
        ClientHints clientHints = parse(SEC_CH_UA_MOBILE, "?0");
        Boolean mobile = clientHints.getMobile();
        assertEquals(FALSE, mobile);
    }

    @Test
    void testSecChUaMobileBadValue() {
        assertNull(parse(SEC_CH_UA_MOBILE, "xx").getMobile());
    }

    @Test
    void testSecChUaMobileEmpty() {
        assertNull(parse(SEC_CH_UA_MOBILE, "").getMobile());
    }

    @Test
    void testSecChUaMobileNull() {
        assertNull(parse(SEC_CH_UA_MOBILE, null).getMobile());
    }

    // ------------------------------------------

    private static final String SEC_CH_UA_MODEL = randomCase(ParseSecChUaModel.HEADER_FIELD);

    @Test
    void testSecChUaModel() {
        ClientHints clientHints = parse(SEC_CH_UA_MODEL, "\"Nokia 7.2\"");
        String model = clientHints.getModel();
        assertEquals("Nokia 7.2", model);
    }

    @Test
    void testSecChUaModelBadValueNoQuotes() {
        assertNull(parse(SEC_CH_UA_MODEL, "xxx").getModel());
    }

    @Test
    void testSecChUaModelBadValueOneQuoteLeft() {
        assertNull(parse(SEC_CH_UA_MODEL, "\"xxx").getModel());
    }

    @Test
    void testSecChUaModelBadValueOneQuoteRight() {
        assertNull(parse(SEC_CH_UA_MODEL, "xxx\"").getModel());
    }

    @Test
    void testSecChUaModelEmpty() {
        assertNull(parse(SEC_CH_UA_MODEL, "\"\"").getModel());
    }

    @Test
    void testSecChUaModelEmptyAfterTrim() {
        assertNull(parse(SEC_CH_UA_MODEL, "\"   \"").getModel());
    }

    @Test
    void testSecChUaModelNull() {
        assertNull(parse(SEC_CH_UA_MODEL, null).getModel());
    }

    // ------------------------------------------

    private static final String SEC_CH_UA_PLATFORM = randomCase(ParseSecChUaPlatform.HEADER_FIELD);

    @Test
    void testSecChUaPlatform() {
        ClientHints clientHints = parse(SEC_CH_UA_PLATFORM, "\"Android\"");
        String platform = clientHints.getPlatform();
        assertEquals("Android", platform);
    }

    @Test
    void testSecChUaPlatformEmpty() {
        ClientHints clientHints = parse(SEC_CH_UA_PLATFORM, "\"\"");
        String platform = clientHints.getPlatform();
        assertNull(platform);
    }

    @Test
    void testSecChUaPlatformNull() {
        ClientHints clientHints = parse(SEC_CH_UA_PLATFORM, null);
        String platform = clientHints.getPlatform();
        assertNull(platform);
    }

    // ------------------------------------------

    private static final String SEC_CH_UA_PLATFORM_VERSION = randomCase(ParseSecChUaPlatformVersion.HEADER_FIELD);

    @Test
    void testSecChUaPlatformVersion() {
        ClientHints clientHints = parse(SEC_CH_UA_PLATFORM_VERSION, "\"11\"");
        String platform = clientHints.getPlatformVersion();
        assertEquals("11", platform);
    }

    @Test
    void testSecChUaPlatformVersionEmpty() {
        ClientHints clientHints = parse(SEC_CH_UA_PLATFORM_VERSION, "\"\"");
        String platform = clientHints.getPlatformVersion();
        assertNull(platform);
    }

    @Test
    void testSecChUaPlatformVersionNull() {
        ClientHints clientHints = parse(SEC_CH_UA_PLATFORM_VERSION, null);
        String platform = clientHints.getPlatformVersion();
        assertNull(platform);
    }

    // ------------------------------------------

    private static final String SEC_CH_UA_WOW64 = randomCase(ParseSecChUaWoW64.HEADER_FIELD);

    @Test
    void testSecChUaWoW64T() {
        ClientHints clientHints = parse(SEC_CH_UA_WOW64, "?1");
        Boolean wow64 = clientHints.getWow64();
        assertEquals(TRUE, wow64);
    }

    @Test
    void testSecChUaWoW64F() {
        ClientHints clientHints = parse(SEC_CH_UA_WOW64, "?0");
        Boolean wow64 = clientHints.getWow64();
        assertEquals(FALSE, wow64);
    }

    @Test
    void testSecChUaWoW64BadValue() {
        assertNull(parse(SEC_CH_UA_WOW64, "xx").getWow64());
    }

    @Test
    void testSecChUaWoW64Empty() {
        assertNull(parse(SEC_CH_UA_WOW64, "").getWow64());
    }

    @Test
    void testSecChUaWoW64Null() {
        assertNull(parse(SEC_CH_UA_WOW64, null).getWow64());
    }


    // ------------------------------------------

    private ClientHints parse(String header, String value) {
        ClientHintsHeadersParser parser = new ClientHintsHeadersParser();
        parser.initializeCache();
        Map<String, String> headers = new TreeMap<>();
        headers.put(header, value);
        LOG.info("Testing %-30s: %s", header, value);
        return parser.parse(headers);
    }

    private static String randomCase(String input) {
        StringBuilder output = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            if (RANDOM.nextBoolean()) {
                output.append(Character.toLowerCase(c));
            } else {
                output.append(Character.toUpperCase(c));
            }
        }
        return output.toString();
    }

}
