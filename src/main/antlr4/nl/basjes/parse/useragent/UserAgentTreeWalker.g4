/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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

SPACE           : (' '|'\t')+   ;
NOTEQUALS       : '!='          ;
EQUALS          : '='           ;
CONTAINS        : '~'           ;
STARTSWITH      : '{'           ;
ENDSWITH        : '}'           ;

FIRSTWORDS      : '#'           ;
SINGLEWORD      : '%'           ;
BACKTOFULL      : '@'           ;

// ===============================================================

// TridentName[agent.(1)product.([2-4])comments.(*)product.name="Trident"^.(*)version~"7.";"DefaultValue"]
// LookUp[TridentName;agent.(1)product.([2-4])comments.(*)product.name#1="Trident"^.(*)version%1="7.";"DefaultValue"]

matcher         : matcherLookup                                                 #matcherNextLookup
                | 'Concat' BLOCKOPEN VALUE SEMICOLON matcherLookup BLOCKCLOSE   #matcherConcat1
                | 'Concat' BLOCKOPEN matcherLookup SEMICOLON VALUE BLOCKCLOSE   #matcherConcat2
                | 'CleanVersion' BLOCKOPEN matcherLookup BLOCKCLOSE             #matcherCleanVersion
                | 'IsNull' BLOCKOPEN matcherLookup BLOCKCLOSE                   #matcherPathIsNull
                ;

matcherLookup   : basePath                                                                                           #matcherPath
                | 'LookUp' BLOCKOPEN lookup=VALUENAME SEMICOLON matcherLookup (SEMICOLON defaultValue=VALUE )? BLOCKCLOSE #matcherPathLookup
                ;

basePath        : value=VALUE                           #pathFixedValue
                | ('__SyntaxError__'|'agent')           #pathNoWalk
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
                | FIRSTWORDS NUMBER               (nextStep=path)?  #stepFirstWords
                | SINGLEWORD NUMBER               (nextStep=path)?  #stepSingleWord
                | BACKTOFULL                      (nextStep=path)?  #stepBackToFull
                ;

numberRange     : ( BRACEOPEN BLOCKOPEN rangeStart=NUMBER MINUS rangeEnd=NUMBER BLOCKCLOSE BRACECLOSE ) #numberRangeStartToEnd
                | ( BRACEOPEN count=NUMBER BRACECLOSE )                                                 #numberRangeSingleValue
                | ( BRACEOPEN STAR BRACECLOSE )                                                         #numberRangeAll
                | (  )                                                                                  #numberRangeEmpty
                ;
