/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar UserAgentTreeWalker;

// ===============================================================

VALUENAME       : [a-zA-Z][a-zA-Z0-9]+    ;
VALUE           : DOUBLEQUOTE ( '\\' [btnfr"'\\] | ~[\\"]  )* DOUBLEQUOTE ;

UP              : '^'           ;
NEXT            : '>'           ;
PREV            : '<'           ;
DOT             : '.'           ;
MINUS           : '-'           ;
STAR            : '*'           ;

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

matcher         : basePath                                                      #matcherPath
//                | 'Concat' BLOCKOPEN VALUE SEMICOLON matcher BLOCKCLOSE         #matcherConcat1
//                | 'Concat' BLOCKOPEN matcher SEMICOLON VALUE BLOCKCLOSE         #matcherConcat2
                | 'NormalizeBrand' BLOCKOPEN matcher BLOCKCLOSE                 #matcherNormalizeBrand
                | 'CleanVersion'   BLOCKOPEN matcher BLOCKCLOSE                 #matcherCleanVersion
                | 'IsNull'         BLOCKOPEN matcher BLOCKCLOSE                 #matcherPathIsNull
                | 'LookUp'         BLOCKOPEN lookup=VALUENAME SEMICOLON matcher (SEMICOLON defaultValue=VALUE )? BLOCKCLOSE #matcherPathLookup
                | matcher wordRange                                             #matcherWordRange
                ;

basePath        : value=VALUE                           #pathFixedValue
//                | '__SyntaxError__' EQUALS value=VALUE  #isSyntaxError
//                | 'agent'                               #pathNoWalk
                | 'agent' nextStep=path                 #pathWalk
                ;

path            : DOT numberRange name=VALUENAME  (nextStep=path)?  #stepDown
                | UP                              (nextStep=path)?  #stepUp
                | NEXT                            (nextStep=path)?  #stepNext
                | PREV                            (nextStep=path)?  #stepPrev
                | EQUALS     value=VALUE          (nextStep=path)?  #stepEqualsValue
                | NOTEQUALS  value=VALUE          (nextStep=path)?  #stepNotEqualsValue
                | STARTSWITH value=VALUE          (nextStep=path)?  #stepStartsWithValue
                | ENDSWITH   value=VALUE          (nextStep=path)?  #stepEndsWithValue
                | CONTAINS   value=VALUE          (nextStep=path)?  #stepContainsValue
                | wordRange                       (nextStep=path)?  #stepWordRange
                | BACKTOFULL                      (nextStep=path)?  #stepBackToFull
                ;

numberRange     : ( BRACEOPEN rangeStart=NUMBER MINUS rangeEnd=NUMBER BRACECLOSE ) #numberRangeStartToEnd
                | ( BRACEOPEN count=NUMBER BRACECLOSE )                            #numberRangeSingleValue
                | ( BRACEOPEN STAR BRACECLOSE )                                    #numberRangeAll
                | (  )                                                             #numberRangeEmpty
                ;

wordRange       : ( BLOCKOPEN firstWord=NUMBER MINUS lastWord=NUMBER BLOCKCLOSE )  #wordRangeStartToEnd
                | ( BLOCKOPEN                  MINUS lastWord=NUMBER BLOCKCLOSE )  #wordRangeFirstWords
                | ( BLOCKOPEN firstWord=NUMBER MINUS                 BLOCKCLOSE )  #wordRangeLastWords
                | ( BLOCKOPEN singleWord=NUMBER                      BLOCKCLOSE )  #wordRangeSingleWord
                ;
