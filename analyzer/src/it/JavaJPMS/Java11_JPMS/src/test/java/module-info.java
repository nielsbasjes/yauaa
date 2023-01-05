/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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
open module nl.example.java11tests {
    requires nl.example.java11module;

    // Needed because we do serialization with this
    requires com.esotericsoftware.kryo;

    // Additional libraries used in the test classes
    requires org.apache.logging.log4j;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.apache.commons.collections4;
}
