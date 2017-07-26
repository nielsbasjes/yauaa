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
QUOTE1:       '\\"'     -> skip;
QUOTE2:       '"'       -> skip;
QUOTE3:       '\\\\'    -> skip;
QUOTE4:       '\''      -> skip;
BAD_ESC_TAB:  '\\t'     -> skip;

SPACE :       (' '|'\t'|'+') -> skip;

// Specialtype of leading garbage (which actually happens)
USERAGENT1   : '-'*[Uu][Ss][Ee][Rr]'-'*[Aa][Gg][Ee][Nn][Tt]' '*(COLON|EQUALS|CURLYBRACEOPEN)' '* -> skip;
USERAGENT2   : '\''[Uu][Ss][Ee][Rr]'-'*[Aa][Gg][Ee][Nn][Tt]'\'' COLON -> skip;

fragment EMailLetter
    : [a-zA-Z0-9_+-]
    ;

fragment EMailWord
    : ( EMailLetter +
        | ' dash '
      )+
    ;

fragment EMailAT
    : '@'
    | ' '+ 'at' ' '+
    | ' '* '[at]' ' '*
    | '\\\\at' '\\\\'?
    | '[\\xc3\\xa07]' // BuiBui-Bot/1.0 (3m4il: buibui[dot]bot[\xc3\xa07]moquadv[dot]com)
    ;

fragment EMailDOT
    : '.'
    | ' '+ 'dot' ' '+
    | ' '* '[dot]' ' '*
    | '\\\\dot' '\\\\'?
    ;

fragment EMailTLD
    :  [a-zA-Z]+  // No tld has numbers in it
    ;

EMAIL       :        (EMailWord EMailDOT?)+ EMailAT EMailWord ( EMailDOT EMailWord )* EMailDOT EMailTLD;

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

// HexWord is 4 hex digits long
fragment HexDigit: [a-fA-F0-9];
fragment HexWord : HexDigit HexDigit HexDigit HexDigit ;
UUID        :        // 550e8400-e29b-41d4-a716-446655440000
                     HexWord HexWord '-' HexWord '-' HexWord '-' HexWord '-' HexWord HexWord HexWord;

fragment BareHostname:  [a-zA-Z0-9\-_]+ ('.'[a-zA-Z0-9\-_]+)*;
fragment UrlPath     :  [a-zA-Z0-9\-_~=?&%+.:/#]*;
fragment BasicURL    :  ('http'|'ftp') 's'? '://' BareHostname UrlPath ;
fragment HTMLURL     :  '<a href="' BasicURL '">'~[<]+'</a>';
URL         :        ( '<'? ('www.'BareHostname UrlPath|BasicURL) '>'? | HTMLURL | 'index.htm' UrlPath);

GIBBERISH   : '@'(~[ ;])*;

// A version is a WORD with at least 1 number in it (and that can contain a '-').
VERSION
    : (~[0-9+;{}()\\/ \t:=[\]"])*[0-9]+(~[+;{}()\\/ \t:=[\]"])*
    ;

fragment WORDLetter
    : (~[0-9+;,{}()\\/ \t:=[\]"-])             // Normal letters
    | '\\x'[0-9a-f][0-9a-f]                    // Hex encoded letters \xab\x12
    ;
WORD
    : WORDLetter+ MINUS*
    | WORDLetter+ (MINUS+ WORDLetter+ )+ MINUS*
    | SPACE MINUS SPACE
    ;

// Base64 Encoded strings: Note we do NOT recognize the variant where the '-' is used because that conflicts with the uuid
// We find that there are cases when a normal string looks like a Base 64 but isn't.
// So I came up with some silly (and incorrect) boundaries between "base64" and "not base64":
// - It may not start with a special character (like '/' )
fragment B64LetterBase      : [a-zA-Z0-9];
fragment B64LetterSpecial   : [+?_/];
fragment B64Letter          : [a-zA-Z0-9+?_/];
fragment B64FirstChunk
    : B64LetterBase B64Letter B64Letter B64Letter
    ;

fragment B64Chunk
    : B64Letter B64Letter B64Letter B64Letter
    ;

fragment B64LastChunk
    : B64Letter B64Letter B64Letter B64Letter
    | B64Letter B64Letter B64Letter '='
    | B64Letter B64Letter '='       '='
    | B64Letter '='       '='       '='
    ;

// We want to avoid matching against normal names and uuids so a BASE64 needs to be pretty long
BASE64: B64FirstChunk B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk+ B64LastChunk;

// =========================================================================================
// Parser

userAgent
    : (SEMICOLON|COMMA|MINUS|'\''|'"'|'\\'|';'|'='|BRACEOPEN|BLOCKOPEN)*                // Leading garbage
      ( (SEMICOLON|COMMA|MINUS)? ( product | rootElements ) )*
      (SEMICOLON|COMMA|MINUS|PLUS|'\''|'"'|'\\'|';'|'='|BRACECLOSE|BLOCKCLOSE)*       // Trailing garbage
    ;

rootElements
    : keyValue
    | siteUrl
    | emailAddress
    | uuId
    | rootText
    ;

rootText
    : VERSION
    | (MINUS* WORD)+ MINUS*
    | GIBBERISH
    | MINUS
    ;

/**
A product has the form :  name / version (comments) /version
However we must have atleast a version or a comment to be a product
There can be multiple comments and multiple versions
Not everyone uses the / as the version separator
And then there are messy edge cases like "foo 1.0 rv:23 (bar)"
*/
product
    : productName   (                                   productVersion )+
                    (  COLON? (SLASH+|' - ') EQUALS?    (productVersionWithCommas|productVersionSingleWord) )*
                    (  SLASH? (SEMICOLON|MINUS)?        commentBlock
                       ( SLASH+  EQUALS?                (productVersionWithCommas|productVersionSingleWord) )* )*
                    (SLASH EOF)?

    | productName   (  SLASH? (SEMICOLON|MINUS)?        commentBlock
                       ( SLASH+  EQUALS?                (productVersionWithCommas|productVersionSingleWord) )* )+
                    (SLASH EOF)?

    | productName   (  COLON? (SLASH+|' - ') productVersionWords
                        ( SLASH* productVersionWithCommas )*
                        SLASH? (SEMICOLON|MINUS)?       commentBlock ?    )+
                    (SLASH EOF)?

    | productName   (  COLON? (SLASH+|' - ') EQUALS?    (productVersionWithCommas|productVersionSingleWord) )+
                    (  SLASH? (SEMICOLON|MINUS)?        commentBlock
                       ( SLASH+  EQUALS?                (productVersionWithCommas|productVersionSingleWord) )* )*
                    (SLASH EOF)?

    | productName   (SLASH EOF)
    ;

commentProduct
    : productName   (                       productVersionWithCommas )+
                    (   SLASH+  EQUALS?     (productVersionWithCommas|productVersionSingleWord) )*
                    (   SLASH?  MINUS?      commentBlock
                        ( SLASH+  EQUALS?   (productVersionWithCommas|productVersionSingleWord) )* )*

    | productName   (   SLASH? MINUS?       commentBlock
                        ( SLASH+  EQUALS?   (productVersionWithCommas|productVersionSingleWord) )* )+

    | productName   (   COLON? (SLASH+|' - ') productVersionWords
                        ( SLASH* productVersionWithCommas )*            )+
                    (   MINUS?              commentBlock
                        ( SLASH+  EQUALS?   (productVersionWithCommas|productVersionSingleWord) )* )*

    | productName   (   (SLASH+|' - ')  EQUALS?     (productVersionWithCommas) )+
                    (   MINUS?              commentBlock
                        ( SLASH+  EQUALS?   (productVersionWithCommas|productVersionSingleWord) )* )*
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
    | productNameWords
    ;

productNameWords
    : WORD ((MINUS|COMMA)* WORD)*
    | (WORD ((MINUS|COMMA)* WORD)*)? CURLYBRACEOPEN WORD ((MINUS|COMMA)* WORD)* CURLYBRACECLOSE (WORD ((MINUS|COMMA)* WORD)*)?
    ;

productVersion
    : keyValue
    | emailAddress
    | siteUrl
    | uuId
    | base64
    | singleVersion
    ;

productVersionWithCommas
    : keyValue
    | emailAddress
    | siteUrl
    | uuId
    | base64
    | singleVersionWithCommas
    ;


productVersionSingleWord
    : WORD
    | CURLYBRACEOPEN WORD CURLYBRACECLOSE
    ;

singleVersion
    : VERSION
    ;

singleVersionWithCommas
    : VERSION (COMMA VERSION)?
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
    ;

commentBlock
    : ( BRACEOPEN  commentEntry (commentSeparator commentEntry)*  (BRACECLOSE | EOF)) // Sometimes the last closing brace is just missing
    | ( BLOCKOPEN  commentEntry (commentSeparator commentEntry)*  (BLOCKCLOSE | EOF)) // Sometimes the last closing block is just missing
    ;

commentEntry
    :   ( emptyWord )
    |   (
            ( commentProduct
            | keyValue
            | uuId
            | siteUrl
            | emailAddress
            | versionWords
            | base64
            | CURLYBRACEOPEN commentProduct  CURLYBRACECLOSE
            | CURLYBRACEOPEN keyValue        CURLYBRACECLOSE
            | CURLYBRACEOPEN uuId            CURLYBRACECLOSE
            | CURLYBRACEOPEN siteUrl         CURLYBRACECLOSE
            | CURLYBRACEOPEN emailAddress    CURLYBRACECLOSE
            | CURLYBRACEOPEN multipleWords   CURLYBRACECLOSE
            | CURLYBRACEOPEN versionWords    CURLYBRACECLOSE
            | CURLYBRACEOPEN base64          CURLYBRACECLOSE
            | commentBlock
            )
            (MINUS*)
        )+
        ( multipleWords
        | productNameNoVersion
        | keyWithoutValue
        | CURLYBRACEOPEN productNameNoVersion CURLYBRACECLOSE
        | CURLYBRACEOPEN keyWithoutValue CURLYBRACECLOSE
        )?
    |   ( multipleWords
        | productNameNoVersion
        | keyWithoutValue
        | CURLYBRACEOPEN productNameNoVersion CURLYBRACECLOSE
        | CURLYBRACEOPEN keyWithoutValue CURLYBRACECLOSE
        )
    ;

productNameKeyValue
    :  key=keyName
       (
         (COLON|EQUALS)+
         ( uuId | siteUrl | emailAddress | multipleWords | base64 | keyValueProductVersionName )
       )+
    ;

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
        (COLON|EQUALS)+
        ( uuId | siteUrl | emailAddress | multipleWords | base64 | keyValueVersionName )
      )+
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

versionWords
    : VERSION+
    ;
