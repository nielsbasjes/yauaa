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

// We must open all test packages so Junit can read them all.
@SuppressWarnings({"requires-automatic"})
open module YauaaTESTS {
    requires transitive nl.basjes.parse.useragent;

//    // Export all packages
//    exports nl.basjes.tests.parse.useragent;
//    exports nl.basjes.tests.parse.useragent.annotate;
//    exports nl.basjes.tests.parse.useragent.profile;
//    exports nl.basjes.tests.parse.useragent.parse;
//    exports nl.basjes.tests.parse.useragent.analyze;
//    exports nl.basjes.tests.parse.useragent.classify;
//    exports nl.basjes.tests.parse.useragent.serialization;
//    exports nl.basjes.tests.parse.useragent.utils;
//
//    // Open all packages
//    opens nl.basjes.tests.parse.useragent;
//    opens nl.basjes.tests.parse.useragent.annotate;
//    opens nl.basjes.tests.parse.useragent.profile;
//    opens nl.basjes.tests.parse.useragent.parse;
//    opens nl.basjes.tests.parse.useragent.analyze;
//    opens nl.basjes.tests.parse.useragent.classify;
//    opens nl.basjes.tests.parse.useragent.serialization;
//    opens nl.basjes.tests.parse.useragent.utils;
//
//    // Open all resource directories
//    opens GenerateAllPossibleSteps ;
//    opens BadDefinitions           ;
//    opens YamlParsingTests         ;
//    opens Versions                 ;
//    opens Ruletests                ;

    // The library itself and several of the required dependencies
//    requires nl.basjes.parse.useragent;
//    requires nl.basjes.collections.prefixmap;
//    requires com.github.benmanes.caffeine;

    // Needed because we do serialization with this
//    requires com.esotericsoftware.kryo;

    // Additional libraries used in the test classes
    requires org.apache.logging.log4j;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
    requires org.hamcrest;
//    requires org.junit.jupiter.params.provider;
    requires org.apache.commons.collections4;
    requires org.yaml.snakeyaml;
    requires spring.core;
    requires org.apache.logging.log4j.core;
    requires org.antlr.antlr4.runtime;
    requires org.apache.commons.lang3;

    // Nullability annotatons
    requires org.jetbrains.annotations;
    requires com.esotericsoftware.kryo;
    requires org.apache.httpcomponents.client5.httpclient5;

}



