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

@SuppressWarnings({ "module", "exports", "requires-automatic", "requires-transitive-automatic" })
module nl.basjes.parse.useragent {
    // Allow loading the rules via reflection and export all packages.
    opens UserAgents;
    // The packages that should be exported
    exports nl.basjes.parse.useragent;
    exports nl.basjes.parse.useragent.annotate;
    exports nl.basjes.parse.useragent.classify;
    exports nl.basjes.parse.useragent.config;

    // All packages  must be available to the tests
    exports nl.basjes.parse.useragent.analyze                                   to YauaaTESTS;
    exports nl.basjes.parse.useragent.analyze.treewalker                        to YauaaTESTS;
    exports nl.basjes.parse.useragent.analyze.treewalker.steps                  to YauaaTESTS;
    exports nl.basjes.parse.useragent.analyze.treewalker.steps.value            to YauaaTESTS;
    exports nl.basjes.parse.useragent.analyze.treewalker.steps.walk             to YauaaTESTS;
    exports nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown    to YauaaTESTS;
    exports nl.basjes.parse.useragent.analyze.treewalker.steps.lookup           to YauaaTESTS;
    exports nl.basjes.parse.useragent.analyze.treewalker.steps.compare          to YauaaTESTS;
    exports nl.basjes.parse.useragent.calculate                                 to YauaaTESTS;
    exports nl.basjes.parse.useragent.clienthints                               to YauaaTESTS;
    exports nl.basjes.parse.useragent.clienthints.parsers                       to YauaaTESTS;
    exports nl.basjes.parse.useragent.debug                                     to YauaaTESTS;
    exports nl.basjes.parse.useragent.parse                                     to YauaaTESTS;
    exports nl.basjes.parse.useragent.utils                                     to YauaaTESTS;

    // Also generated code
    exports nl.basjes.parse.useragent.parser                                    to YauaaTESTS;

    // Optional: Allow reflection by Kryo
    requires static com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent                                         to com.esotericsoftware.kryo, YauaaTESTS;
    opens nl.basjes.parse.useragent.analyze                                 to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.analyze.treewalker                      to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.analyze.treewalker.steps                to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.analyze.treewalker.steps.value          to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.analyze.treewalker.steps.walk           to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown  to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.analyze.treewalker.steps.lookup         to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.analyze.treewalker.steps.compare        to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.annotate                                ;//to com.esotericsoftware.kryo, YauaaTESTS;
    opens nl.basjes.parse.useragent.calculate                               to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.classify                                to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.clienthints                             to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.clienthints.parsers                     to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.config                                  to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.debug                                   to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.parse                                   to com.esotericsoftware.kryo;
    opens nl.basjes.parse.useragent.utils                                   to com.esotericsoftware.kryo;
    // Nullability annotatons
    requires static org.jetbrains.annotations;

    // Code generation annotations
    requires static lombok;

    // Logging
    requires static org.apache.logging.log4j;

    requires com.github.benmanes.caffeine;          // Caching
    requires nl.basjes.collections.prefixmap;       // Lookup data structure

    // Automatic modules :(
    requires org.antlr.antlr4.runtime; // #SHADED : Shaded and relocated
    requires org.yaml.snakeyaml;       // #SHADED : Shaded and relocated
    requires java.logging;             // Needed for snakeyaml after being shaded
    requires org.apache.commons.text;
    requires org.apache.commons.lang3;

}
