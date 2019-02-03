/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

grammar UserAgentTreeWalker;

// ===============================================================

PATHTOKENNAME   : ('agent' | 'product' | 'name' | 'version' | 'comments' | 'entry' | 'text' | 'url' | 'email' | 'base64' | 'uuid' | 'keyvalue' | 'key' | 'value' );

VALUENAME       : PATHTOKENNAME | [a-zA-Z][a-zA-Z0-9]+    ;

VALUE           : DOUBLEQUOTE ( '\\' [btnfr"'\\] | ~[\\"]  )* DOUBLEQUOTE ;

UP              : '^'           ;
NEXT            : '>'           ;
NEXT2           : '>>'          ;
NEXT3           : '>>>'         ;
NEXT4           : '>>>>'        ;
PREV            : '<'           ;
PREV2           : '<<'          ;
PREV3           : '<<<'         ;
PREV4           : '<<<<'        ;
DOT             : '.'           ;
MINUS           : '-'           ;
STAR            : '*'           ;
IN              : '?'           ;

NUMBER          : [0-9]+        ;
BLOCKOPEN       : '['           ;
BLOCKCLOSE      : ']'           ;
BRACEOPEN       : '('           ;
BRACECLOSE      : ')'           ;
DOUBLEQUOTE     : '"'           ;
COLON           : ':'           ;
SEMICOLON       : ';'           ;

SPACE           : (' '|'\t')+   -> skip;
NOTEQUALS       : '!='          ;
EQUALS          : '='           ;
CONTAINS        : '~'           ;
STARTSWITH      : '{'           ;
ENDSWITH        : '}'           ;

BACKTOFULL      : '@'           ;

// ===============================================================

// TridentName[agent.(1)product.(2-4)comments.(*)product.name="Trident"^.(*)version~"7.";"DefaultValue"]
// LookUp[TridentName;agent.(1)product.(2-4)comments.(*)product.name#1="Trident"^.(*)version%1="7.";"DefaultValue"]

matcherRequire  : matcher                                        EOF   #matcherBase
//                | '__SyntaxError__' EQUALS value=VALUE                   #isSyntaxError
                | 'IsNull'         BLOCKOPEN matcher BLOCKCLOSE  EOF   #matcherPathIsNull
                ;

matcherExtract  : expression=matcher EOF;

// Is a bit duplicate but this gives a lot of clarity in the code
matcherVariable : expression=matcher EOF;

matcher         : basePath                                                                              #matcherPath
                | 'Concat' BLOCKOPEN prefix=VALUE SEMICOLON matcher SEMICOLON postfix=VALUE BLOCKCLOSE  #matcherConcat
                | 'Concat' BLOCKOPEN prefix=VALUE SEMICOLON matcher                         BLOCKCLOSE  #matcherConcatPrefix
                | 'Concat' BLOCKOPEN                        matcher SEMICOLON postfix=VALUE BLOCKCLOSE  #matcherConcatPostfix
                | 'NormalizeBrand'   BLOCKOPEN matcher BLOCKCLOSE                                         #matcherNormalizeBrand
                | 'CleanVersion'     BLOCKOPEN matcher BLOCKCLOSE                                         #matcherCleanVersion
                | 'LookUp'           BLOCKOPEN lookup=VALUENAME SEMICOLON matcher (SEMICOLON defaultValue=VALUE )? BLOCKCLOSE #matcherPathLookup
                | 'LookUpPrefix'     BLOCKOPEN lookup=VALUENAME SEMICOLON matcher (SEMICOLON defaultValue=VALUE )? BLOCKCLOSE #matcherPathLookupPrefix
                | 'IsInLookUpPrefix' BLOCKOPEN lookup=VALUENAME SEMICOLON matcher                                  BLOCKCLOSE #matcherPathIsInLookupPrefix
                | matcher wordRange                                                                     #matcherWordRange
                ;

basePath        : value=VALUE                              #pathFixedValue
                | '@' variable=VALUENAME (nextStep=path)?  #pathVariable
                | 'agent'                (nextStep=path)?  #pathWalk
                ;

path            : DOT numberRange name=PATHTOKENNAME  (nextStep=path)?  #stepDown
                | UP                              (nextStep=path)?  #stepUp
                | NEXT                            (nextStep=path)?  #stepNext
                | NEXT2                           (nextStep=path)?  #stepNext2
                | NEXT3                           (nextStep=path)?  #stepNext3
                | NEXT4                           (nextStep=path)?  #stepNext4
                | PREV                            (nextStep=path)?  #stepPrev
                | PREV2                           (nextStep=path)?  #stepPrev2
                | PREV3                           (nextStep=path)?  #stepPrev3
                | PREV4                           (nextStep=path)?  #stepPrev4
                | EQUALS     value=VALUE          (nextStep=path)?  #stepEqualsValue
                | NOTEQUALS  value=VALUE          (nextStep=path)?  #stepNotEqualsValue
                | STARTSWITH value=VALUE          (nextStep=path)?  #stepStartsWithValue
                | ENDSWITH   value=VALUE          (nextStep=path)?  #stepEndsWithValue
                | CONTAINS   value=VALUE          (nextStep=path)?  #stepContainsValue
                | IN         set=VALUENAME        (nextStep=path)?  #stepIsInSet
                | wordRange                       (nextStep=path)?  #stepWordRange
                | BACKTOFULL                      (nextStep=path)?  #stepBackToFull
                ;

numberRange     : ( BRACEOPEN rangeStart=NUMBER MINUS rangeEnd=NUMBER BRACECLOSE ) #numberRangeStartToEnd
                | ( BRACEOPEN                   MINUS rangeEnd=NUMBER BRACECLOSE ) #numberRangeOpenStartToEnd
                | ( BRACEOPEN rangeStart=NUMBER MINUS                 BRACECLOSE ) #numberRangeStartToOpenEnd
                | ( BRACEOPEN count=NUMBER BRACECLOSE )                            #numberRangeSingleValue
                | ( BRACEOPEN STAR BRACECLOSE )                                    #numberRangeAll
                | (  )                                                             #numberRangeEmpty
                ;

wordRange       : ( BLOCKOPEN firstWord=NUMBER MINUS lastWord=NUMBER BLOCKCLOSE )  #wordRangeStartToEnd
                | ( BLOCKOPEN                  MINUS lastWord=NUMBER BLOCKCLOSE )  #wordRangeFirstWords
                | ( BLOCKOPEN firstWord=NUMBER MINUS                 BLOCKCLOSE )  #wordRangeLastWords
                | ( BLOCKOPEN singleWord=NUMBER                      BLOCKCLOSE )  #wordRangeSingleWord
                ;
