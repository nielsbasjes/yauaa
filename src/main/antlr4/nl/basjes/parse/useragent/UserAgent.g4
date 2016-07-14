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

grammar UserAgent;

//For browsers based on Mozilla, the user-agent string shall follow the format:
//   MozillaProductToken (MozillaComment) GeckoProductToken *(VendorProductToken|VendorComment)
//Applications that embed the Gecko layout engine shall have user-agent strings that follow the format:
//   ApplicationProductToken (ApplicationComment) GeckoProductToken *(VendorProductToken|VendorComment)

// Normal cases

// Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; SIMBAR=0; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; .NET CLR 1.0.3705; .NET CLR 1.1.4322; InfoPath.1; IEMB3; IEMB3)

// Special Test cases
// InetURL:/1.0
// Sogou web spider/4.0(+http://www.sogou.com/docs/help/webmasters.htm#07)
// LWP::Simple/6.00 libwww-perl/6.05
// Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; Name = MASB; rv:11.0) like Gecko
// Voordeel 1.3.0 rv:1.30 (iPhone; iPhone OS 7.1.1; nl_NL)
// Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.19; aggregator:Spinn3r (Spinn3r 3.1); http://spinn3r.com/robot) Gecko/2010040121 Firefox/3.0.19
// Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; nl-nl) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.102011-10-16 20:23:10
// Airmail 1.3.3 rv:237 (Macintosh; Mac OS X 10.9.3; nl_NL)
// "\""Mozilla/5.0 (Linux; Android 4.4; Nexus 7/JSS15R) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36\"""
// Mozilla/5.0 (Linux; U; Android 4.1.1; nl-nl; bq Edison 2 Build/1.0.1_20130805-14:02) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30
// Mozilla/5.0 (Series40; Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1; Profile/MIDP-2.1 Configuration/CLDC-1.1) Gecko/20100401 S40OviBrowser/3.1.1.0.27
// Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; IWSS25:J6HBo2OOPD50bdr79CgSjLigxKUK+idfrxaKO1+FNCY=; GTB7.4; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C; .NET4.0E)
// Airmail 1.3.3 rv:237 (Macintosh; Mac OS X 10.9.3; nl_NL)
// Mozilla/5.0 (Linux; U; Android 4.0.3; en-gb; ARCHOS 80G9 Build/Deode@4.0.7) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30
// Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.20 (KHTML, like Gecko) Chrome/19.0.1036.7 Safari/535.20 +rr:1511) +x10955


// Combined testcase (does 'everything')

// Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; Name = MASB; rv:11.0; Nokia501.2/11.1.3/java_runtime_version=Nokia_Asha_1_1_1; SIMBAR={A43F3165-FAC1-11E1-8828-00123F6EDBB1}; ;http://bla.bla.com/page.html ; email:aap@noot.nl) like Gecko

// =========================================================================================
// Lexer

// First we match the parts that are useless and make the grammar too hard:
//GFE : ',gzip(gfe)'                      -> skip;  // Seems related to Google's PageSpeed Service
//IWSS25: 'IWSS25:'[a-zA-Z0-9\+\=\/\\]+   -> skip;  // Unparsable junk (Actually, it's a Base64 encoded binary)
QUOTE1:       '\\"'     -> skip;
QUOTE2:       '"'       -> skip;
QUOTE3:       '\\\\'    -> skip;
BAD_ESC_TAB:  '\\t'     -> skip;

// We do NOT skip these because in some cases we want to check for the presence or absence of a SPACE
SPACE       :        (' '|'\t'|'+')+ -> skip;
EMAIL       :        [a-zA-Z0-9]+
                       ('@' | ' '+ 'at'  ' '+ | ' '* '[' 'at'  ']' ' '*) [a-zA-Z0-9\-]+
                     ( ('.' | ' '+ 'dot' ' '+ | ' '* '[' 'dot' ']' ' '*) [a-zA-Z0-9]+   )*
                     ( ('.' | ' '+ 'dot' ' '+ | ' '* '[' 'dot' ']' ' '*) [a-zA-Z]+      ); // No tld has numbers in it
CURLYBRACEOPEN :     '{'                 ;
CURLYBRACECLOSE:     '}'                 ;
BRACEOPEN   :        '('                 ;
BRACECLOSE  :        ')'                 ;
BLOCKOPEN   :        '['                 ;
BLOCKCLOSE  :        ']'                 ;
SEMICOLON   :        ';'                 ;
COLON       :        ':'                 ;
COMMA       :        ','                 ;
SLASH       :        '/'                 ;
EQUALS      :        '='                 ;
MINUS       :        '-'                 ;
PLUS        :        '+'                 ;

fragment HexWord: [a-fA-F0-9][a-fA-F0-9][a-fA-F0-9][a-fA-F0-9] ;
UUID        :        // 550e8400-e29b-41d4-a716-446655440000
//                     ([a-fA-F0-9]{8}'-'[a-fA-F0-9]{4}'-'[a-fA-F0-9]{4}'-'[a-fA-F0-9]{4}'-'[a-fA-F0-9]{12});
                     HexWord HexWord '-' HexWord '-' HexWord '-' HexWord '-' HexWord HexWord HexWord;

fragment BareHostname:  [a-zA-Z0-9\-_]+ ('.'[a-zA-Z0-9\-_]+)*;
fragment UrlPath     :  [a-zA-Z0-9\-_\~\=\?\&\%\+\.\:\/\#]*;
fragment BasicURL    :  ('http'|'ftp') 's'? '://' BareHostname UrlPath ;
fragment HTMLURL     :  '<a href="' BasicURL '">'~[<]+'</a>';
URL         :        ( '<'? ('www.'BareHostname UrlPath|BasicURL) '>'? |HTMLURL | 'index.htm' UrlPath);

GIBBERISH   : '@'(~[ ;])*;

// A version is a WORD with at least 1 number in it (and that can contain a '-').
VERSION
    : (~[0-9\+\;\{\}\(\)\/\ \t\:\=\[\]\"])*[0-9]+(~[\+\;\{\}\(\)\/\ \t\:\=\[\]\"])*
    ;

fragment WORDLetter
    : (~[0-9\+\;\{\}\(\)\/\ \t\:\=\[\]\"\-,])  // Normal letters
    | '\\x'[0-9a-f][0-9a-f]                    // Hex encoded letters \xab\x12
    ;
WORD
    : WORDLetter+ MINUS*
    | WORDLetter+ (MINUS+ WORDLetter+ )+ MINUS*
    | SPACE MINUS SPACE
    ;

// Base64 Encoded strings: Note we do NOT recognize the variant where the '-' is used because that conflicts with the uuid
fragment B64Letter    : [a-zA-Z0-9\+\?_/];
fragment B64Chunk     : B64Letter B64Letter B64Letter B64Letter;
fragment B64LastChunk0: B64Letter B64Letter B64Letter B64Letter ;
fragment B64LastChunk1: B64Letter B64Letter B64Letter '=';
fragment B64LastChunk2: B64Letter B64Letter '='       '=';
fragment B64LastChunk3: B64Letter '='       '='       '=';
fragment B64LastChunk: B64LastChunk0 | B64LastChunk1 | B64LastChunk2 | B64LastChunk3;

// We want to avoid matching agains normal names and uuids so a BASE64 needs to be pretty long
BASE64: B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk* B64LastChunk;

// =========================================================================================
// Parser

userAgent
    : (SEMICOLON|COMMA|MINUS|'\''|'"'|'\\'|';'|'='|BRACEOPEN|BLOCKOPEN)*                // Leading garbage
      (('user-agent'|'User-Agent'|'UserAgent') (COLON|COMMA|EQUALS|'\\t')*)?            // Leading garbage
      ( (SEMICOLON|COMMA|MINUS)? ( product | emailAddress | siteUrl | rootTextPart SEMICOLON) )*
      ( (SEMICOLON|COMMA|MINUS)? rootTextPart )*                        // Capture trailing text like "like Gecko"
        (SEMICOLON|COMMA|MINUS|PLUS|'\''|'"'|'\\'|';'|'='|BRACECLOSE|BLOCKCLOSE)*       // Trailing garbage
    ;

rootTextPart:
            keyValue | siteUrl | emailAddress | uuId | VERSION | multipleWords ;

/**
A product has the form :  name / version (comments) /version
However we must have atleast a version or a comment to be a product
There can be multiple comments and multiple versions
Not everyone uses the / as the version separator
And then there are messy edge cases like "foo 1.0 rv:23 (bar)"
*/
product
    : productName   (                           productVersion )+
                    (  COLON? SLASH+ EQUALS?    (productVersion|productVersionSingleWord) )*
                    (  SLASH? (SEMICOLON|MINUS)? commentBlock
                       ( SLASH+  EQUALS?        (productVersion|productVersionSingleWord) )* )*
                    (SLASH EOF)?

    | productName   (  SLASH? (SEMICOLON|MINUS)?       commentBlock
                       ( SLASH+  EQUALS?        (productVersion|productVersionSingleWord) )* )+
                    (SLASH EOF)?

    | productName   (  COLON? SLASH productVersionWords
                        ( SLASH* productVersion )*
                        SLASH? (SEMICOLON|MINUS)?      commentBlock ?    )+
                    (SLASH EOF)?

    | productName   (  COLON? SLASH+ EQUALS?    (productVersion|productVersionSingleWord) )+
                    (  SLASH? (SEMICOLON|MINUS)?       commentBlock
                       ( SLASH+  EQUALS?        (productVersion|productVersionSingleWord) )* )*
                    (SLASH EOF)?

    | productName   (SLASH EOF)?
    ;

commentProduct
    : productName   (                       productVersion )+
                    (   SLASH+  EQUALS?     (productVersion|productVersionSingleWord) )*
                    (   SLASH?  MINUS?      commentBlock
                        ( SLASH+  EQUALS?   (productVersion|productVersionSingleWord) )* )*

    | productName   (   SLASH? MINUS?       commentBlock
                        ( SLASH+  EQUALS?   (productVersion|productVersionSingleWord) )* )+

    | productName   (   COLON? SLASH productVersionWords
                        ( SLASH* productVersion )*            )+

    | productName   (   SLASH+  EQUALS?     (productVersion|productVersionSingleWord) )+
                    (   MINUS?              commentBlock
                        ( SLASH+  EQUALS?   (productVersion|productVersionSingleWord) )* )*

    ;

productVersionWords
    : WORD (MINUS? WORD)*
    ;

productName
    : productNameKeyValue
    | productNameEmail
    | productNameUrl
    | productNameVersion
    | productNameUuid
    | productNameBare
    ;

productNameBare
    : WORD ((MINUS|COMMA)* WORD)*
    ;

productVersion
    : keyValue
    | emailAddress
    | siteUrl
    | uuId
    | base64
    | simpleVersion
    ;

productVersionSingleWord
    : WORD
    ;

simpleVersion
    : VERSION
    ;

productNameVersion
    : VERSION ((MINUS)* WORD)*
    ;

productNameEmail
    : emailAddress
    ;

productNameUrl
    : siteUrl
    ;

productNameUuid
    : uuId
    ;

uuId
    :                uuid=UUID
    | CURLYBRACEOPEN uuid=UUID CURLYBRACECLOSE
    ;

emailAddress
    :                email=EMAIL
    | CURLYBRACEOPEN email=EMAIL CURLYBRACECLOSE
    ;

siteUrl
    :                url=URL
    | CURLYBRACEOPEN url=URL CURLYBRACECLOSE
    ;

base64
    :                value=BASE64
    | CURLYBRACEOPEN value=BASE64 CURLYBRACECLOSE
    ;

commentSeparator
    : SEMICOLON
    | COMMA
//    | MINUS
    ;

commentBlock
    : ( BRACEOPEN  commentEntry (commentSeparator commentEntry)*  (BRACECLOSE | EOF)) // Sometimes the last closing brace is just missing
    | ( BLOCKOPEN  commentEntry (commentSeparator commentEntry)*  (BLOCKCLOSE | EOF)) // Sometimes the last closing block is just missing
    ;

commentEntry
    :   ( emptyWord )
    |   (
            ( commentBlock
            | commentProduct
            | keyValue
            | uuId
            | siteUrl
            | emailAddress
            | versionWord
            | base64
            | CURLYBRACEOPEN commentProduct  CURLYBRACECLOSE
            | CURLYBRACEOPEN keyValue        CURLYBRACECLOSE
            | CURLYBRACEOPEN uuId            CURLYBRACECLOSE
            | CURLYBRACEOPEN siteUrl         CURLYBRACECLOSE
            | CURLYBRACEOPEN emailAddress    CURLYBRACECLOSE
            | CURLYBRACEOPEN multipleWords   CURLYBRACECLOSE
            | CURLYBRACEOPEN versionWord     CURLYBRACECLOSE
            | CURLYBRACEOPEN base64          CURLYBRACECLOSE
            )
            (MINUS*)
        )*
        ( multipleWords
        | productNameNoVersion
        | keyWithoutValue
        | CURLYBRACEOPEN productNameNoVersion CURLYBRACECLOSE
        | CURLYBRACEOPEN keyWithoutValue CURLYBRACECLOSE
        )?
    ;

productNameKeyValue
    :  key=keyName
       (
         (
           (COLON|EQUALS)+
           ( uuId | siteUrl | emailAddress | multipleWords | base64 | keyValueProductVersionName )
         )+
       );

productNameNoVersion
    :  productName SLASH
    ;


keyValueProductVersionName
    : VERSION (SLASH WORD)*
    | VERSION
    ;

keyValue
    : key=keyName
      (
        (
          (
            (COLON|EQUALS)+
            ( uuId | siteUrl | emailAddress | multipleWords | base64 | keyValueVersionName )
          )+
        )
      )
    ;

keyWithoutValue
    : key=keyName (COLON|EQUALS)+
    ;

keyValueVersionName
    : VERSION
    ;

keyName
    : WORD(MINUS WORD)*
    | VERSION
    ;

emptyWord
    :
    | MINUS
    ;

multipleWords
    : (MINUS* WORD)+ MINUS*
    | GIBBERISH
    | MINUS
    ;

versionWord
    : VERSION+
    ;
