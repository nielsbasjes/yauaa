/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestAnnotationBadUsages {

    @Test
    void testNullInitAnalyzer(){
        UserAgentAnnotationAnalyzer<String> userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();

        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () ->
                userAgentAnalyzer.initialize(null));

        assertEquals("[Initialize] The mapper instance is null.", exception.getMessage());
    }

    @Test
    void testNullInit(){
        UserAgentAnnotationAnalyzer<String> userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
        assertNull(userAgentAnalyzer.map(null));
    }

    @Test
    void testNoInit(){
        UserAgentAnnotationAnalyzer<String> userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();

        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () ->
                assertNull(userAgentAnalyzer.map("Foo")));

        assertEquals("[Map] The mapper instance is null.", exception.getMessage());
    }

    // ----------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"}) // Here we deliberately created some bad code to check the behavior.
    public static class MapperWithoutGenericType
        implements UserAgentAnnotationMapper, Serializable {

        MapperWithoutGenericType() {
            UserAgentAnnotationAnalyzer userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
            userAgentAnalyzer.initialize(this);
        }

        @Override
        public String getUserAgentString(Object record) {
            return null;
        }
    }

    @Test
    void testMissingTypeParameter(){
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, MapperWithoutGenericType::new);

        assertEquals("Couldn't find the used generic type of the UserAgentAnnotationMapper.", exception.getMessage());
    }

}
