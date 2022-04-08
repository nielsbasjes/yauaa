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

lexer grammar ClientHintsLexer;

// ===============================================================

VERSION         : 'v';
KEY             : [a-zA-Z0-9]+;
DOUBLEQUOTE     : '"' -> channel(HIDDEN), pushMode(VALUE_MODE);

SPACE           : (' '| '\u2002' | '\u0220' |'\t'|'+') -> skip;
COMMA           : ','           ;
SEMICOLON       : ';'           ;
EQUALS          : '='           ;


mode VALUE_MODE;
    VALUE_DOUBLEQUOTE: '"' -> channel(HIDDEN), type(DOUBLEQUOTE), popMode ;

    // Yes, this is an ambiguity. The clean value is first so that matches if it is clean.
    VALUE : [A-Z0-9][ _a-zA-Z0-9.-]+ ;

    // https://chromestatus.com/feature/5630916006248448
    //     If implemented, this proposal would enable additional GREASE characters
    //     (the full list includes the following ASCII characters:
    //     0x20 (SP), 0x28 (left parenthesis), 0x29 (right parenthesis),
    //     0x2D (-), 0x2E (.), 0x2F (/), 0x3A (:), 0x3B (;), 0x3D (=), 0x3F (?), 0x5F (_))
    //     and vary the arbitrary version over time.
    GREASEVALUE     :  [()/\\:;=?_a-zA-Z0-9. -]+ ;
