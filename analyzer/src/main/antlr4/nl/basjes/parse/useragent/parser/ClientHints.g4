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

grammar ClientHints;

options { tokenVocab=ClientHintsLexer; }

// ===============================================================

// " Not A;Brand";v="99", "Chromium";v="99", "Google Chrome";v="99"
// " Not A;Brand";v="99", "Chromium";v="99", "Microsoft Edge";v="99"
// " Not A;Brand";v="99.0.0.0", "Chromium";v="99.0.4844.51", "Google Chrome";v="99.0.4844.51"
// " Not A;Brand";v="99.0.0.0", "Chromium";v="99.0.1150.30", "Microsoft Edge";v="99.0.1150.30"
// "Not_A Brand";v="99", "Microsoft Edge";v="109", "Chromium";v="109"
// "Not_A Brand";v="99.0.0.0", "Microsoft Edge";v="109.0.1518.61", "Chromium";v="109.0.5414.87"

brandList
    : brandEntry ( COMMA brandEntry )*
    ;

brandEntry
    : ( brand | greaseEntry )
    ;

greaseEntry
    : ( KNOWNGREASE | GREASEVALUE ) SEMICOLON VERSION EQUALS ( VALUE | KNOWNGREASE | GREASEVALUE )
    ;

brand
    : name=VALUE SEMICOLON VERSION EQUALS version=VALUE
    ;
