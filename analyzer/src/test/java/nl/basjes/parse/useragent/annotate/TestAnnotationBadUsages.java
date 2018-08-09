/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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

package nl.basjes.parse.useragent.annotate;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import static org.junit.Assert.assertNull;

public class TestAnnotationBadUsages {

    private static final Logger LOG = LoggerFactory.getLogger(TestAnnotationBadUsages.class);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testNullInitAnalyzer(){
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("[Initialize] The mapper instance is null.");
        UserAgentAnnotationAnalyzer<String> userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
        userAgentAnalyzer.initialize(null);
    }

    @Test
    public void testNullInit(){
        UserAgentAnnotationAnalyzer<String> userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
        assertNull(userAgentAnalyzer.map(null));
    }

    @Test
    public void testNoInit(){
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("[Map] The mapper instance is null.");
        UserAgentAnnotationAnalyzer<String> userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
        assertNull(userAgentAnalyzer.map("Foo"));
    }

    // ----------------------------------------------------------------

    public static class MapperWithoutGenericType
        implements UserAgentAnnotationMapper, Serializable {
        private final transient UserAgentAnnotationAnalyzer userAgentAnalyzer;

        @SuppressWarnings("unchecked") // Here we deliberately created some bad code to check the behavior.
        public MapperWithoutGenericType() {
            userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
            userAgentAnalyzer.initialize(this);
        }

        public Object enrich(Object record) {
            return record;
        }

        @Override
        public String getUserAgentString(Object record) {
            return null;
        }
    }

    @Test
    public void testMissingTypeParameter(){
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("Couldn't find the used generic type of the UserAgentAnnotationMapper.");
        new MapperWithoutGenericType();
    }

}
