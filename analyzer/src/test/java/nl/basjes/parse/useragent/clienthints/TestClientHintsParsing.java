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

import nl.basjes.parse.useragent.clienthints.ClientHints.BrandVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TestClientHintsParsing {

    private static final Logger LOG = LogManager.getFormatterLogger(TestClientHintsParsing.class);

    // ------------------------------------------

    @Test
    void testSecChUa1() {
        ClientHints clientHints = parse("sEc-cH-uA", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"99\", \"Google Chrome\";v=\"99\"");
        List<BrandVersion> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Chromium",        brands.get(0).getBrand());
        assertEquals("99",              brands.get(0).getVersion());
        assertEquals("Google Chrome",   brands.get(1).getBrand());
        assertEquals("99",              brands.get(1).getVersion());
    }

    @Test
    void testSecChUa2() {
        ClientHints clientHints = parse("sEc-cH-uA", "\"\\Not;A Brand\";v=\"99\", \"Google Chrome\";v=\"85\", \"Chromium\";v=\"85\"");
        List<BrandVersion> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Google Chrome",   brands.get(0).getBrand());
        assertEquals("85",              brands.get(0).getVersion());
        assertEquals("Chromium",        brands.get(1).getBrand());
        assertEquals("85",              brands.get(1).getVersion());
    }

    @Test
    void testSecChUa3() {
        ClientHints clientHints = parse("sEc-cH-uA", "\"Chrome\"; v=\"73\", \"(Not;Browser\"; v=\"12\"");
        List<BrandVersion> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(1, brands.size());
        assertEquals("Chrome",          brands.get(0).getBrand());
        assertEquals("73",              brands.get(0).getVersion());
    }

    @Test
    void testSecChUa4() {
        ClientHints clientHints = parse("sEc-cH-uA", "\"Chrome\"; v=\"73\", \"(Not;Browser\"; v=\"12\", \"Chromium\"; v=\"74\"");
        List<BrandVersion> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Chrome",          brands.get(0).getBrand());
        assertEquals("73",              brands.get(0).getVersion());
        assertEquals("Chromium",        brands.get(1).getBrand());
        assertEquals("74",              brands.get(1).getVersion());
    }

    @Test
    void testSecChUa5() {
        ClientHints clientHints = parse("sEc-cH-uA", "\"(Not;Browser\"; v=\"12\", \"Chromium\"; v=\"73\"");
        List<BrandVersion> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(1, brands.size());
        assertEquals("Chromium",        brands.get(0).getBrand());
        assertEquals("73",              brands.get(0).getVersion());
    }

    @Test
    void testSecChUa6() {
        ClientHints clientHints = parse("sEc-cH-uA", "\"Chrome\"; v=\"73\", \"Xwebs mega\"; v=\"60\", \"Chromium\"; v=\"73\", \"(Not;Browser\"; v=\"12\"");
        List<BrandVersion> brands = clientHints.getBrands();
        assertNotNull(brands);
        assertEquals(3, brands.size());
        assertEquals("Chrome",          brands.get(0).getBrand());
        assertEquals("73",              brands.get(0).getVersion());
        assertEquals("Xwebs mega",      brands.get(1).getBrand());
        assertEquals("60",              brands.get(1).getVersion());
        assertEquals("Chromium",        brands.get(2).getBrand());
        assertEquals("73",              brands.get(2).getVersion());
    }

    @Test
    void testSecChUaBadValue() {
        assertNull(parse("sEc-cH-uA", "xxx").getBrands());
    }

    @Test
    void testSecChUaAlmostEmpty() {
        assertNull(parse("sEc-cH-uA", "\"(Not;Browser\"; v=\"12\"").getBrands());
    }

    @Test
    void testSecChUaEmpty() {
        assertNull(parse("sEc-cH-uA", "").getBrands());
    }

    @Test
    void testSecChUaNull() {
        assertNull(parse("sEc-cH-uA", null).getBrands());
    }

    // ------------------------------------------

    @Test
    void testSecChUaArch() {
        ClientHints clientHints = parse("sEc-cH-uA-Arch", "\"x86\"");
        String architecture = clientHints.getArchitecture();
        assertNotNull(architecture);
        assertEquals("x86", architecture);
    }

    @Test
    void testSecChUaArchEmpty() {
        assertNull(parse("sEc-cH-uA-Arch", "\"\"").getArchitecture());
    }

    @Test
    void testSecChUaArchNull() {
        assertNull(parse("sEc-cH-uA-Arch", null).getArchitecture());
    }

    // ------------------------------------------

    @Test
    void testSecChUaFullVersionList() {
        ClientHints clientHints = parse("sEc-cH-uA-Full-Version-List", "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"99.0.4844.51\", \"Google Chrome\";v=\"99.0.4844.51\"");
        List<BrandVersion> brands = clientHints.getFullVersionList();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Chromium",        brands.get(0).getBrand());
        assertEquals("99.0.4844.51",    brands.get(0).getVersion());
        assertEquals("Google Chrome",   brands.get(1).getBrand());
        assertEquals("99.0.4844.51",    brands.get(1).getVersion());
    }

    @Test
    void testSecChUaFullVersionListExtraSpaces() {
        ClientHints clientHints = parse("sEc-cH-uA-Full-Version-List", "  \" Not A;Brand\"  ;   v=\"99.0.0.0\"  ,   \"Chromium\"  ;  v  =  \"99.0.4844.51\"   ,    \"Google Chrome\"   ;   v  =  \"99.0.4844.51\"   ");
        List<BrandVersion> brands = clientHints.getFullVersionList();
        assertNotNull(brands);
        assertEquals(2, brands.size());
        assertEquals("Chromium",        brands.get(0).getBrand());
        assertEquals("99.0.4844.51",    brands.get(0).getVersion());
        assertEquals("Google Chrome",   brands.get(1).getBrand());
        assertEquals("99.0.4844.51",    brands.get(1).getVersion());
    }

    @Test
    void testSecChUaFullVersionBadValue() {
        assertNull(parse("sEc-cH-uA-Full-Version-List", "xxx").getFullVersionList());
    }

    @Test
    void testSecChUaFullVersionAlmostEmpty() {
        assertNull(parse("sEc-cH-uA-Full-Version-List", "\" Not A;Brand\";v=\"99.0.0.0\"").getFullVersionList());
    }

    @Test
    void testSecChUaFullVersionEmpty() {
        assertNull(parse("sEc-cH-uA-Full-Version-List", "").getFullVersionList());
    }

    @Test
    void testSecChUaFullVersionNull() {
        assertNull(parse("sEc-cH-uA-Full-Version-List", null).getFullVersionList());
    }

    // ------------------------------------------

    @Test
    void testSecChUaMobileT() {
        ClientHints clientHints = parse("sEc-cH-uA-Mobile", "?1");
        Boolean mobile = clientHints.getMobile();
        assertNotNull(mobile);
        assertEquals(TRUE, mobile);
    }

    @Test
    void testSecChUaMobileF() {
        ClientHints clientHints = parse("sEc-cH-uA-Mobile", "?0");
        Boolean mobile = clientHints.getMobile();
        assertNotNull(mobile);
        assertEquals(FALSE, mobile);
    }

    @Test
    void testSecChUaMobileBadValue() {
        assertNull(parse("sEc-cH-uA-Mobile", "xx").getMobile());
    }

    @Test
    void testSecChUaMobileEmpty() {
        assertNull(parse("sEc-cH-uA-Mobile", "").getMobile());
    }

    @Test
    void testSecChUaMobileNull() {
        assertNull(parse("sEc-cH-uA-Mobile", null).getMobile());
    }

    // ------------------------------------------

    @Test
    void testSecChUaModel() {
        ClientHints clientHints = parse("sEc-cH-uA-Model", "\"Nokia 7.2\"");
        String model = clientHints.getModel();
        assertNotNull(model);
        assertEquals("Nokia 7.2", model);
    }

    @Test
    void testSecChUaModelBadValueNoQuotes() {
        assertNull(parse("sEc-cH-uA-Model", "xxx").getModel());
    }
    @Test
    void testSecChUaModelBadValueOneQuoteLeft() {
        assertNull(parse("sEc-cH-uA-Model", "\"xxx").getModel());
    }
    @Test
    void testSecChUaModelBadValueOneQuoteRight() {
        assertNull(parse("sEc-cH-uA-Model", "xxx\"").getModel());
    }

    @Test
    void testSecChUaModelEmpty() {
        assertNull(parse("sEc-cH-uA-Model", "\"\"").getModel());
    }

    @Test
    void testSecChUaModelEmptyAfterTrim() {
        assertNull(parse("sEc-cH-uA-Model", "\"   \"").getModel());
    }

    @Test
    void testSecChUaModelNull() {
        assertNull(parse("sEc-cH-uA-Model", null).getModel());
    }

    // ------------------------------------------

    @Test
    void testSecChUaPlatform() {
        ClientHints clientHints = parse("sEc-cH-uA-Platform", "\"Android\"");
        String platform = clientHints.getPlatform();
        assertNotNull(platform);
        assertEquals("Android", platform);
    }

    @Test
    void testSecChUaPlatformEmpty() {
        ClientHints clientHints = parse("sEc-cH-uA-Platform", "\"\"");
        String platform = clientHints.getPlatform();
        assertNull(platform);
    }

    @Test
    void testSecChUaPlatformNull() {
        ClientHints clientHints = parse("sEc-cH-uA-Platform", null);
        String platform = clientHints.getPlatform();
        assertNull(platform);
    }

    // ------------------------------------------


    @Test
    void testSecChUaPlatformVersion() {
        ClientHints clientHints = parse("sEc-cH-uA-Platform-Version", "\"11\"");
        String platform = clientHints.getPlatformVersion();
        assertNotNull(platform);
        assertEquals("11", platform);
    }

    @Test
    void testSecChUaPlatformVersionEmpty() {
        ClientHints clientHints = parse("sEc-cH-uA-Platform-Version", "\"\"");
        String platform = clientHints.getPlatformVersion();
        assertNull(platform);
    }

    @Test
    void testSecChUaPlatformVersionNull() {
        ClientHints clientHints = parse("sEc-cH-uA-Platform-Version", null);
        String platform = clientHints.getPlatformVersion();
        assertNull(platform);
    }

    // ------------------------------------------

//
//    @Test
//    void testSecChUaWoW64() {
//
//    }

    // ------------------------------------------

    @Test
    void testSecChUaWoW64T() {
        ClientHints clientHints = parse("sEc-cH-uA-WoW64", "?1");
        Boolean wow64 = clientHints.getWow64();
        assertNotNull(wow64);
        assertEquals(TRUE, wow64);
    }

    @Test
    void testSecChUaWoW64F() {
        ClientHints clientHints = parse("sEc-cH-uA-WoW64", "?0");
        Boolean wow64 = clientHints.getWow64();
        assertNotNull(wow64);
        assertEquals(FALSE, wow64);
    }

    @Test
    void testSecChUaWoW64BadValue() {
        assertNull(parse("sEc-cH-uA-WoW64", "xx").getWow64());
    }

    @Test
    void testSecChUaWoW64Empty() {
        assertNull(parse("sEc-cH-uA-WoW64", "").getWow64());
    }

    @Test
    void testSecChUaWoW64Null() {
        assertNull(parse("sEc-cH-uA-WoW64", null).getWow64());
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


}
