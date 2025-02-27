/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2025 Niels Basjes
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
import nl.basjes.parse.useragent.clienthints.parsers.CHParser;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUa;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaArch;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaBitness;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaFormFactors;
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

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TestClientHintsParsing {

    private static final Logger LOG = LogManager.getFormatterLogger(TestClientHintsParsing.class);
    private static final Random RANDOM = new Random(42); // Repeatable random !

    private static final CHParser DUMMY_PARSER = new CHParser() {
        @Nonnull
        @Override
        public ClientHints parse(@Nonnull Map<String, String> clientHintsHeaders, @Nonnull ClientHints clientHints, @Nonnull String headerName) {
            return new ClientHints();
        }

        @Nonnull
        @Override
        public String inputField() {
            return "Dummy";
        }
    };

    // ------------------------------------------

    @Test
    void testSfString() {
        assertEquals("\\\\\\\"Windows\\\\\\\"", DUMMY_PARSER.parseSfString("\"\\\\\\\"Windows\\\\\\\"\""));
        assertEquals("Windows", DUMMY_PARSER.parseSfString("\"Windows\""));
        assertNull(DUMMY_PARSER.parseSfString("Windows")); // Missing quotes is INVALID so we refuse to parse it.
    }

    // ------------------------------------------

    private static final RandomizeCase SEC_CH_UA = new RandomizeCase(ParseSecChUa.HEADER_FIELD);

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
    void testSecChUaBadValue1() {
        assertNull(parse(SEC_CH_UA, "xxx").getBrands());
    }

    @Test
    void testSecChUaBadValue2() {
        assertNull(parse(SEC_CH_UA, "|").getBrands());
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

    private static final RandomizeCase SEC_CH_UA_ARCH = new RandomizeCase(ParseSecChUaArch.HEADER_FIELD);

    @Test
    void testSecChUaArch() {
        ClientHints clientHints = parse(SEC_CH_UA_ARCH, "\"x86\"");
        String architecture = clientHints.getArchitecture();
        assertNotNull(architecture);
        assertEquals("x86", architecture);
    }

    @Test
    void testSecChUaArchBadValueNoQuotes() {
        assertNull(parse(SEC_CH_UA_ARCH, "xxx").getArchitecture());
    }

    @Test
    void testSecChUaArchBadValueOneQuoteLeft() {
        assertNull(parse(SEC_CH_UA_ARCH, "\"xxx").getArchitecture());
    }

    @Test
    void testSecChUaArchBadValueOneQuoteRight() {
        assertNull(parse(SEC_CH_UA_ARCH, "xxx\"").getArchitecture());
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

    private static final RandomizeCase SEC_CH_UA_BITNESS = new RandomizeCase(ParseSecChUaBitness.HEADER_FIELD);

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
    void testSecChUaBitnessBadValueNoQuotes() {
        assertNull(parse(SEC_CH_UA_BITNESS, "xxx").getBitness());
    }

    @Test
    void testSecChUaBitnessBadValueOneQuoteLeft() {
        assertNull(parse(SEC_CH_UA_BITNESS, "\"xxx").getBitness());
    }

    @Test
    void testSecChUaBitnessBadValueOneQuoteRight() {
        assertNull(parse(SEC_CH_UA_BITNESS, "xxx\"").getBitness());
    }

    @Test
    void testSecChUaBitnessNull() {
        assertNull(parse(SEC_CH_UA_BITNESS, null).getBitness());
    }

    // ------------------------------------------

    private static final RandomizeCase SEC_CH_UA_FORMFACTORS = new RandomizeCase(ParseSecChUaFormFactors.HEADER_FIELD);

    @Test
    void testSecChUaFormfactor() {
        ClientHints clientHints = parse(SEC_CH_UA_FORMFACTORS, "\"Mobile\", \"EInk\"");
        List<String> formFactors = clientHints.getFormFactors();
        assertNotNull(formFactors);
        assertTrue(formFactors.contains("Mobile"));
        assertTrue(formFactors.contains("EInk"));
    }

    @Test
    void testSecChUaFormfactorEmpty() {
        assertNull(parse(SEC_CH_UA_FORMFACTORS, "\"\"").getFormFactors());
    }

    @Test
    void testSecChUaFormfactorBadValueNoQuotes() {
        assertNull(parse(SEC_CH_UA_FORMFACTORS, "xxx").getFormFactors());
    }

    @Test
    void testSecChUaFormfactorBadValueOneQuoteLeft() {
        assertNull(parse(SEC_CH_UA_FORMFACTORS, "\"xxx").getFormFactors());
    }

    @Test
    void testSecChUaFormfactorBadValueOneQuoteRight() {
        assertNull(parse(SEC_CH_UA_FORMFACTORS, "xxx\"").getFormFactors());
    }

    @Test
    void testSecChUaFormfactorNull() {
        assertNull(parse(SEC_CH_UA_FORMFACTORS, null).getFormFactors());
    }

    // ------------------------------------------

    private static final RandomizeCase SEC_CH_UA_FULL_VERSION = new RandomizeCase(ParseSecChUaFullVersion.HEADER_FIELD);

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
    void testSecChUaFullVersionBad() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION, "\"").getFullVersion());
    }

    @Test
    void testSecChUaFullVersionNull() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION, null).getFullVersion());
    }

    // ------------------------------------------

    private static final RandomizeCase SEC_CH_UA_FULL_VERSION_LIST = new RandomizeCase(ParseSecChUaFullVersionList.HEADER_FIELD);

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
    void testSecChUaFullVersionListMajorOnly() {
        ClientHints clientHints = parse(SEC_CH_UA_FULL_VERSION_LIST, "\" Not A;Brand\";v=\"2\", \"Chromium\";v=\"2\", \"Google Chrome\";v=\"3\"");
        List<Brand> brands = clientHints.getFullVersionList();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Chromium",        brands.get(0).getName());
        assertEquals("2",               brands.get(0).getVersion());
        assertEquals("Google Chrome",   brands.get(1).getName());
        assertEquals("3",               brands.get(1).getVersion());
        LOG.info("{}", clientHints);
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
    void testSecChUaFullVersionListTinyVersion() {
        ClientHints clientHints = parse(SEC_CH_UA_FULL_VERSION_LIST, "\"x\";v=\"x\"");
        List<Brand> brands = clientHints.getFullVersionList();
        assertNotNull(brands);
        assertEquals(1, brands.size());
        assertEquals("x",               brands.get(0).getName());
        assertEquals("x",               brands.get(0).getVersion());
    }

    @Test
    void testSecChUaFullVersionListEmptyVersion() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION_LIST, "\"x\";v=\"\"").getFullVersionList());
    }

    @Test
    void testSecChUaFullVersionListBadVersion() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION_LIST, "\"x\";v=\"|\"").getFullVersionList());
    }

    @Test
    void testSecChUaFullVersionListOneQuoteVersion() {
        // The parser is a tad too forgiving and still accepts this.
        ClientHints clientHints = parse(SEC_CH_UA_FULL_VERSION_LIST, "\"x\";v=\"x");
        List<Brand> brands = clientHints.getFullVersionList();
        assertNotNull(brands);
        assertEquals(1, brands.size());
        assertEquals("x",               brands.get(0).getName());
        assertEquals("x",               brands.get(0).getVersion());
    }

    @Test
    void testSecChUaFullVersionListNoQuotesVersion() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION_LIST, "\"x\";v=").getFullVersionList());
    }

    @Test
    void testSecChUaFullVersionListBadValue1() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION_LIST, "xxx").getFullVersionList());
    }

    @Test
    void testSecChUaFullVersionListBadValue2() {
        assertNull(parse(SEC_CH_UA_FULL_VERSION_LIST, "|").getFullVersionList());
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

    private static final RandomizeCase SEC_CH_UA_MOBILE = new RandomizeCase(ParseSecChUaMobile.HEADER_FIELD);

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

    private static final RandomizeCase SEC_CH_UA_MODEL = new RandomizeCase(ParseSecChUaModel.HEADER_FIELD);

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

    private static final RandomizeCase SEC_CH_UA_PLATFORM = new RandomizeCase(ParseSecChUaPlatform.HEADER_FIELD);

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
    void testSecChUaPlatformBadValueNoQuotes() {
        assertNull(parse(SEC_CH_UA_PLATFORM, "xxx").getPlatform());
    }

    @Test
    void testSecChUaPlatformBadValueOneQuoteLeft() {
        assertNull(parse(SEC_CH_UA_PLATFORM, "\"xxx").getPlatform());
    }

    @Test
    void testSecChUaPlatformBadValueOneQuoteRight() {
        assertNull(parse(SEC_CH_UA_PLATFORM, "xxx\"").getPlatform());
    }

    @Test
    void testSecChUaPlatformNull() {
        ClientHints clientHints = parse(SEC_CH_UA_PLATFORM, null);
        String platform = clientHints.getPlatform();
        assertNull(platform);
    }

    // ------------------------------------------

    private static final RandomizeCase SEC_CH_UA_PLATFORM_VERSION = new RandomizeCase(ParseSecChUaPlatformVersion.HEADER_FIELD);

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
    void testSecChUaPlatformVersionBadValueNoQuotes() {
        assertNull(parse(SEC_CH_UA_PLATFORM_VERSION, "xxx").getPlatformVersion());
    }

    @Test
    void testSecChUaPlatformVersionBadValueOneQuoteLeft() {
        assertNull(parse(SEC_CH_UA_PLATFORM_VERSION, "\"xxx").getPlatformVersion());
    }

    @Test
    void testSecChUaPlatformVersionBadValueOneQuoteRight() {
        assertNull(parse(SEC_CH_UA_PLATFORM_VERSION, "xxx\"").getPlatformVersion());
    }

    @Test
    void testSecChUaPlatformVersionNull() {
        ClientHints clientHints = parse(SEC_CH_UA_PLATFORM_VERSION, null);
        String platform = clientHints.getPlatformVersion();
        assertNull(platform);
    }

    // ------------------------------------------

    private static final RandomizeCase SEC_CH_UA_WOW64 = new RandomizeCase(ParseSecChUaWoW64.HEADER_FIELD);

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

    private ClientHints parse(Object headerobj, String value) {
        String header = headerobj.toString();
        ClientHintsHeadersParser parser = new ClientHintsHeadersParser();
        parser.initializeCache();
        Map<String, String> headers = new TreeMap<>();
        headers.put(header, value);
        LOG.info("Testing %-30s: %s", header, value);
        return parser.parse(headers);
    }

    public static class RandomizeCase {
        RandomizeCase(String baseValue) {
            this.baseValue = baseValue;
        }

        private final String baseValue;

        @Override
        public String toString() {
            StringBuilder output = new StringBuilder(baseValue.length());
            for (char c : baseValue.toCharArray()) {
                if (RANDOM.nextBoolean()) {
                    output.append(Character.toLowerCase(c));
                } else {
                    output.append(Character.toUpperCase(c));
                }
            }
            return output.toString();
        }
    }


}
