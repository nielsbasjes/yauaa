/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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

MIME_TYPE_1:  'application/json'   -> skip;
MIME_TYPE_2:  '*/*'                -> skip;

SPACE :       (' '| '\u2002' | '\u0220' |'\t'|'+') -> skip;

fragment UserAgent
    : [Uu][Ss][Ee][Rr]'-'*[Aa][Gg][Ee][Nn][Tt]
    ;

// Specialtype of leading garbage (which actually happens)
USERAGENT1   : '-'* UserAgent ' '*(COLON|EQUALS|CURLYBRACEOPEN)' '* -> skip;
USERAGENT2   : '\'' UserAgent '\'' COLON -> skip;

fragment EMailFirstLetter
    : [a-zA-Z0-9]
    ;

fragment EMailLetter
    : [a-zA-Z0-9_+-]
    ;

fragment EMailWord
    : EMailFirstLetter
      ( EMailLetter +
        | ' dash '
      )+
    ;

fragment EMailAT
    : '@'
    | '\\@'
    | '\\\\@'
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

fragment EMailWords
    : EMailWord ( EMailDOT EMailWord )*
    ;

EMAIL
    : EMailWords EMailAT EMailWords EMailDOT EMailTLD
    ;

CURLYBRACEOPEN  : '{' ;
CURLYBRACECLOSE : '}' ;
BRACEOPEN       : '(' ;
BRACECLOSE      : ')' ;
BLOCKOPEN       : '[' ;
BLOCKCLOSE      : ']' ;
SEMICOLON       : ';' ;
COLON           : ':' ;
COMMA           : ',' ;
SLASH           : '/' ;
EQUALS          : '=' ;
MINUS           : '-' ;
PLUS            : '+' ;

// HexWord is 4 hex digits long
fragment HexDigit: [a-fA-F0-9];
fragment HexWord : HexDigit HexDigit HexDigit HexDigit ;
UUID  // 550e8400-e29b-41d4-a716-446655440000
    : HexWord HexWord '-' HexWord '-' HexWord '-' HexWord '-' HexWord HexWord HexWord
    ;

// The TLDs extracted from https://publicsuffix.org/list/public_suffix_list.dat on do  2 dec 2021 20:25:53 CET
fragment TLD :  'aaa' | 'aarp' | 'abarth' | 'abb' | 'abbott' | 'abbvie' | 'abc' | 'able' | 'abogado' | 'abudhabi' | 'ac' | 'academy' | 'accenture' | 'accountant' | 'accountants' | 'aco' | 'actor' | 'ad' | 'adac' | 'ads' | 'adult' | 'ae' | 'aeg' | 'aero' | 'aetna' | 'af' | 'afamilycompany' | 'afl' | 'africa' | 'ag' | 'agakhan' | 'agency' | 'ai' | 'aig' | 'airbus' | 'airforce' | 'airtel' | 'akdn' | 'al' | 'alfaromeo' | 'alibaba' | 'alipay' | 'allfinanz' | 'allstate' | 'ally' | 'alsace' | 'alstom' | 'am' | 'amazon' | 'americanexpress' | 'americanfamily' | 'amex' | 'amfam' | 'amica' | 'amsterdam' | 'analytics' | 'android' | 'anquan' | 'anz' | 'ao' | 'aol' | 'apartments' | 'app' | 'apple' | 'aq' | 'aquarelle' | 'ar' | 'arab' | 'aramco' | 'archi' | 'army' | 'arpa' | 'art' | 'arte' | 'as' | 'asda' | 'asia' | 'associates' | 'at' | 'athleta' | 'attorney' | 'au' | 'auction' | 'audi' | 'audible' | 'audio' | 'auspost' | 'author' | 'auto' | 'autos' | 'avianca' | 'aw' | 'aws' | 'ax' | 'axa' | 'az' | 'azure' | 'ba' | 'baby' | 'baidu' | 'banamex' | 'bananarepublic' | 'band' | 'bank' | 'bar' | 'barcelona' | 'barclaycard' | 'barclays' | 'barefoot' | 'bargains' | 'baseball' | 'basketball' | 'bauhaus' | 'bayern' | 'bb' | 'bbc' | 'bbt' | 'bbva' | 'bcg' | 'bcn' | 'bd' | 'be' | 'beats' | 'beauty' | 'beer' | 'bentley' | 'berlin' | 'best' | 'bestbuy' | 'bet' | 'bf' | 'bg' | 'bh' | 'bharti' | 'bi' | 'bible' | 'bid' | 'bike' | 'bing' | 'bingo' | 'bio' | 'biz' | 'bj' | 'black' | 'blackfriday' | 'blockbuster' | 'blog' | 'bloomberg' | 'blue' | 'bm' | 'bms' | 'bmw' | 'bn' | 'bnpparibas' | 'bo' | 'boats' | 'boehringer' | 'bofa' | 'bom' | 'bond' | 'boo' | 'book' | 'booking' | 'bosch' | 'bostik' | 'boston' | 'bot' | 'boutique' | 'box' | 'br' | 'bradesco' | 'bridgestone' | 'broadway' | 'broker' | 'brother' | 'brussels' | 'bs' | 'bt' | 'budapest' | 'bugatti' | 'build' | 'builders' | 'business' | 'buy' | 'buzz' | 'bv' | 'bw' | 'by' | 'bz' | 'bzh' | 'ca' | 'cab' | 'cafe' | 'cal' | 'call' | 'calvinklein' | 'cam' | 'camera' | 'camp' | 'cancerresearch' | 'canon' | 'capetown' | 'capital' | 'capitalone' | 'car' | 'caravan' | 'cards' | 'care' | 'career' | 'careers' | 'cars' | 'casa' | 'case' | 'cash' | 'casino' | 'cat' | 'catering' | 'catholic' | 'cba' | 'cbn' | 'cbre' | 'cbs' | 'cc' | 'cd' | 'center' | 'ceo' | 'cern' | 'cf' | 'cfa' | 'cfd' | 'cg' | 'ch' | 'chanel' | 'channel' | 'charity' | 'chase' | 'chat' | 'cheap' | 'chintai' | 'christmas' | 'chrome' | 'church' | 'ci' | 'cipriani' | 'circle' | 'cisco' | 'citadel' | 'citi' | 'citic' | 'city' | 'cityeats' | 'ck' | 'cl' | 'claims' | 'cleaning' | 'click' | 'clinic' | 'clinique' | 'clothing' | 'cloud' | 'club' | 'clubmed' | 'cm' | 'cn' | 'co' | 'coach' | 'codes' | 'coffee' | 'college' | 'cologne' | 'com' | 'comcast' | 'commbank' | 'community' | 'company' | 'compare' | 'computer' | 'comsec' | 'condos' | 'construction' | 'consulting' | 'contact' | 'contractors' | 'cooking' | 'cookingchannel' | 'cool' | 'coop' | 'corsica' | 'country' | 'coupon' | 'coupons' | 'courses' | 'cpa' | 'cr' | 'credit' | 'creditcard' | 'creditunion' | 'cricket' | 'crown' | 'crs' | 'cruise' | 'cruises' | 'csc' | 'cu' | 'cuisinella' | 'cv' | 'cw' | 'cx' | 'cy' | 'cymru' | 'cyou' | 'cz' | 'dabur' | 'dad' | 'dance' | 'data' | 'date' | 'dating' | 'datsun' | 'day' | 'dclk' | 'dds' | 'de' | 'deal' | 'dealer' | 'deals' | 'degree' | 'delivery' | 'dell' | 'deloitte' | 'delta' | 'democrat' | 'dental' | 'dentist' | 'desi' | 'design' | 'dev' | 'dhl' | 'diamonds' | 'diet' | 'digital' | 'direct' | 'directory' | 'discount' | 'discover' | 'dish' | 'diy' | 'dj' | 'dk' | 'dm' | 'dnp' | 'do' | 'docs' | 'doctor' | 'dog' | 'domains' | 'dot' | 'download' | 'drive' | 'dtv' | 'dubai' | 'duck' | 'dunlop' | 'dupont' | 'durban' | 'dvag' | 'dvr' | 'dz' | 'earth' | 'eat' | 'ec' | 'eco' | 'edeka' | 'edu' | 'education' | 'ee' | 'eg' | 'email' | 'emerck' | 'energy' | 'engineer' | 'engineering' | 'enterprises' | 'epson' | 'equipment' | 'er' | 'ericsson' | 'erni' | 'es' | 'esq' | 'estate' | 'et' | 'etisalat' | 'eu' | 'eurovision' | 'eus' | 'events' | 'exchange' | 'expert' | 'exposed' | 'express' | 'extraspace' | 'fage' | 'fail' | 'fairwinds' | 'faith' | 'family' | 'fan' | 'fans' | 'farm' | 'farmers' | 'fashion' | 'fast' | 'fedex' | 'feedback' | 'ferrari' | 'ferrero' | 'fi' | 'fiat' | 'fidelity' | 'fido' | 'film' | 'final' | 'finance' | 'financial' | 'fire' | 'firestone' | 'firmdale' | 'fish' | 'fishing' | 'fit' | 'fitness' | 'fj' | 'fk' | 'flickr' | 'flights' | 'flir' | 'florist' | 'flowers' | 'fly' | 'fm' | 'fo' | 'foo' | 'food' | 'foodnetwork' | 'football' | 'ford' | 'forex' | 'forsale' | 'forum' | 'foundation' | 'fox' | 'fr' | 'free' | 'fresenius' | 'frl' | 'frogans' | 'frontdoor' | 'frontier' | 'ftr' | 'fujitsu' | 'fun' | 'fund' | 'furniture' | 'futbol' | 'fyi' | 'ga' | 'gal' | 'gallery' | 'gallo' | 'gallup' | 'game' | 'games' | 'gap' | 'garden' | 'gay' | 'gb' | 'gbiz' | 'gd' | 'gdn' | 'ge' | 'gea' | 'gent' | 'genting' | 'george' | 'gf' | 'gg' | 'ggee' | 'gh' | 'gi' | 'gift' | 'gifts' | 'gives' | 'giving' | 'gl' | 'glade' | 'glass' | 'gle' | 'global' | 'globo' | 'gm' | 'gmail' | 'gmbh' | 'gmo' | 'gmx' | 'gn' | 'godaddy' | 'gold' | 'goldpoint' | 'golf' | 'goo' | 'goodyear' | 'goog' | 'google' | 'gop' | 'got' | 'gov' | 'gp' | 'gq' | 'gr' | 'grainger' | 'graphics' | 'gratis' | 'green' | 'gripe' | 'grocery' | 'group' | 'gs' | 'gt' | 'gu' | 'guardian' | 'gucci' | 'guge' | 'guide' | 'guitars' | 'guru' | 'gw' | 'gy' | 'hair' | 'hamburg' | 'hangout' | 'haus' | 'hbo' | 'hdfc' | 'hdfcbank' | 'health' | 'healthcare' | 'help' | 'helsinki' | 'here' | 'hermes' | 'hgtv' | 'hiphop' | 'hisamitsu' | 'hitachi' | 'hiv' | 'hk' | 'hkt' | 'hm' | 'hn' | 'hockey' | 'holdings' | 'holiday' | 'homedepot' | 'homegoods' | 'homes' | 'homesense' | 'honda' | 'horse' | 'hospital' | 'host' | 'hosting' | 'hot' | 'hoteles' | 'hotels' | 'hotmail' | 'house' | 'how' | 'hr' | 'hsbc' | 'ht' | 'hu' | 'hughes' | 'hyatt' | 'hyundai' | 'ibm' | 'icbc' | 'ice' | 'icu' | 'id' | 'ie' | 'ieee' | 'ifm' | 'ikano' | 'il' | 'im' | 'imamat' | 'imdb' | 'immo' | 'immobilien' | 'in' | 'inc' | 'industries' | 'infiniti' | 'info' | 'ing' | 'ink' | 'institute' | 'insurance' | 'insure' | 'int' | 'international' | 'intuit' | 'investments' | 'io' | 'ipiranga' | 'iq' | 'ir' | 'irish' | 'is' | 'ismaili' | 'ist' | 'istanbul' | 'it' | 'itau' | 'itv' | 'jaguar' | 'java' | 'jcb' | 'je' | 'jeep' | 'jetzt' | 'jewelry' | 'jio' | 'jll' | 'jm' | 'jmp' | 'jnj' | 'jo' | 'jobs' | 'joburg' | 'jot' | 'joy' | 'jp' | 'jpmorgan' | 'jprs' | 'juegos' | 'juniper' | 'kaufen' | 'kddi' | 'ke' | 'kerryhotels' | 'kerrylogistics' | 'kerryproperties' | 'kfh' | 'kg' | 'kh' | 'ki' | 'kia' | 'kids' | 'kim' | 'kinder' | 'kindle' | 'kitchen' | 'kiwi' | 'km' | 'kn' | 'koeln' | 'komatsu' | 'kosher' | 'kp' | 'kpmg' | 'kpn' | 'kr' | 'krd' | 'kred' | 'kuokgroup' | 'kw' | 'ky' | 'kyoto' | 'kz' | 'la' | 'lacaixa' | 'lamborghini' | 'lamer' | 'lancaster' | 'lancia' | 'land' | 'landrover' | 'lanxess' | 'lasalle' | 'lat' | 'latino' | 'latrobe' | 'law' | 'lawyer' | 'lb' | 'lc' | 'lds' | 'lease' | 'leclerc' | 'lefrak' | 'legal' | 'lego' | 'lexus' | 'lgbt' | 'li' | 'lidl' | 'life' | 'lifeinsurance' | 'lifestyle' | 'lighting' | 'like' | 'lilly' | 'limited' | 'limo' | 'lincoln' | 'linde' | 'link' | 'lipsy' | 'live' | 'living' | 'lixil' | 'lk' | 'llc' | 'llp' | 'loan' | 'loans' | 'locker' | 'locus' | 'loft' | 'lol' | 'london' | 'lotte' | 'lotto' | 'love' | 'lpl' | 'lplfinancial' | 'lr' | 'ls' | 'lt' | 'ltd' | 'ltda' | 'lu' | 'lundbeck' | 'luxe' | 'luxury' | 'lv' | 'ly' | 'ma' | 'macys' | 'madrid' | 'maif' | 'maison' | 'makeup' | 'man' | 'management' | 'mango' | 'map' | 'market' | 'marketing' | 'markets' | 'marriott' | 'marshalls' | 'maserati' | 'mattel' | 'mba' | 'mc' | 'mckinsey' | 'md' | 'me' | 'med' | 'media' | 'meet' | 'melbourne' | 'meme' | 'memorial' | 'men' | 'menu' | 'merckmsd' | 'mg' | 'mh' | 'miami' | 'microsoft' | 'mil' | 'mini' | 'mint' | 'mit' | 'mitsubishi' | 'mk' | 'ml' | 'mlb' | 'mls' | 'mm' | 'mma' | 'mn' | 'mo' | 'mobi' | 'mobile' | 'moda' | 'moe' | 'moi' | 'mom' | 'monash' | 'money' | 'monster' | 'mormon' | 'mortgage' | 'moscow' | 'moto' | 'motorcycles' | 'mov' | 'movie' | 'mp' | 'mq' | 'mr' | 'ms' | 'msd' | 'mt' | 'mtn' | 'mtr' | 'mu' | 'museum' | 'music' | 'mutual' | 'mv' | 'mw' | 'mx' | 'my' | 'mz' | 'na' | 'nab' | 'nagoya' | 'name' | 'natura' | 'navy' | 'nba' | 'nc' | 'ne' | 'nec' | 'net' | 'netbank' | 'netflix' | 'network' | 'neustar' | 'new' | 'news' | 'next' | 'nextdirect' | 'nexus' | 'nf' | 'nfl' | 'ng' | 'ngo' | 'nhk' | 'ni' | 'nico' | 'nike' | 'nikon' | 'ninja' | 'nissan' | 'nissay' | 'nl' | 'no' | 'nokia' | 'northwesternmutual' | 'norton' | 'now' | 'nowruz' | 'nowtv' | 'np' | 'nr' | 'nra' | 'nrw' | 'ntt' | 'nu' | 'nyc' | 'nz' | 'obi' | 'observer' | 'off' | 'office' | 'okinawa' | 'olayan' | 'olayangroup' | 'oldnavy' | 'ollo' | 'om' | 'omega' | 'one' | 'ong' | 'onion' | 'onl' | 'online' | 'ooo' | 'open' | 'oracle' | 'orange' | 'org' | 'organic' | 'origins' | 'osaka' | 'otsuka' | 'ott' | 'ovh' | 'pa' | 'page' | 'panasonic' | 'paris' | 'pars' | 'partners' | 'parts' | 'party' | 'passagens' | 'pay' | 'pccw' | 'pe' | 'pet' | 'pf' | 'pfizer' | 'pg' | 'ph' | 'pharmacy' | 'phd' | 'philips' | 'phone' | 'photo' | 'photography' | 'photos' | 'physio' | 'pics' | 'pictet' | 'pictures' | 'pid' | 'pin' | 'ping' | 'pink' | 'pioneer' | 'pizza' | 'pk' | 'pl' | 'place' | 'play' | 'playstation' | 'plumbing' | 'plus' | 'pm' | 'pn' | 'pnc' | 'pohl' | 'poker' | 'politie' | 'porn' | 'post' | 'pr' | 'pramerica' | 'praxi' | 'press' | 'prime' | 'pro' | 'prod' | 'productions' | 'prof' | 'progressive' | 'promo' | 'properties' | 'property' | 'protection' | 'pru' | 'prudential' | 'ps' | 'pt' | 'pub' | 'pw' | 'pwc' | 'py' | 'qa' | 'qpon' | 'quebec' | 'quest' | 'qvc' | 'racing' | 'radio' | 'raid' | 're' | 'read' | 'realestate' | 'realtor' | 'realty' | 'recipes' | 'red' | 'redstone' | 'redumbrella' | 'rehab' | 'reise' | 'reisen' | 'reit' | 'reliance' | 'ren' | 'rent' | 'rentals' | 'repair' | 'report' | 'republican' | 'rest' | 'restaurant' | 'review' | 'reviews' | 'rexroth' | 'rich' | 'richardli' | 'ricoh' | 'ril' | 'rio' | 'rip' | 'rmit' | 'ro' | 'rocher' | 'rocks' | 'rodeo' | 'rogers' | 'room' | 'rs' | 'rsvp' | 'ru' | 'rugby' | 'ruhr' | 'run' | 'rw' | 'rwe' | 'ryukyu' | 'sa' | 'saarland' | 'safe' | 'safety' | 'sakura' | 'sale' | 'salon' | 'samsclub' | 'samsung' | 'sandvik' | 'sandvikcoromant' | 'sanofi' | 'sap' | 'sarl' | 'sas' | 'save' | 'saxo' | 'sb' | 'sbi' | 'sbs' | 'sc' | 'sca' | 'scb' | 'schaeffler' | 'schmidt' | 'scholarships' | 'school' | 'schule' | 'schwarz' | 'science' | 'scjohnson' | 'scot' | 'sd' | 'se' | 'search' | 'seat' | 'secure' | 'security' | 'seek' | 'select' | 'sener' | 'services' | 'ses' | 'seven' | 'sew' | 'sex' | 'sexy' | 'sfr' | 'sg' | 'sh' | 'shangrila' | 'sharp' | 'shaw' | 'shell' | 'shia' | 'shiksha' | 'shoes' | 'shop' | 'shopping' | 'shouji' | 'show' | 'showtime' | 'si' | 'silk' | 'sina' | 'singles' | 'site' | 'sj' | 'sk' | 'ski' | 'skin' | 'sky' | 'skype' | 'sl' | 'sling' | 'sm' | 'smart' | 'smile' | 'sn' | 'sncf' | 'so' | 'soccer' | 'social' | 'softbank' | 'software' | 'sohu' | 'solar' | 'solutions' | 'song' | 'sony' | 'soy' | 'spa' | 'space' | 'sport' | 'spot' | 'sr' | 'srl' | 'ss' | 'st' | 'stada' | 'staples' | 'star' | 'statebank' | 'statefarm' | 'stc' | 'stcgroup' | 'stockholm' | 'storage' | 'store' | 'stream' | 'studio' | 'study' | 'style' | 'su' | 'sucks' | 'supplies' | 'supply' | 'support' | 'surf' | 'surgery' | 'suzuki' | 'sv' | 'swatch' | 'swiftcover' | 'swiss' | 'sx' | 'sy' | 'sydney' | 'systems' | 'sz' | 'tab' | 'taipei' | 'talk' | 'taobao' | 'target' | 'tatamotors' | 'tatar' | 'tattoo' | 'tax' | 'taxi' | 'tc' | 'tci' | 'td' | 'tdk' | 'team' | 'tech' | 'technology' | 'tel' | 'temasek' | 'tennis' | 'teva' | 'tf' | 'tg' | 'th' | 'thd' | 'theater' | 'theatre' | 'tiaa' | 'tickets' | 'tienda' | 'tiffany' | 'tips' | 'tires' | 'tirol' | 'tj' | 'tjmaxx' | 'tjx' | 'tk' | 'tkmaxx' | 'tl' | 'tm' | 'tmall' | 'tn' | 'to' | 'today' | 'tokyo' | 'tools' | 'top' | 'toray' | 'toshiba' | 'total' | 'tours' | 'town' | 'toyota' | 'toys' | 'tr' | 'trade' | 'trading' | 'training' | 'travel' | 'travelchannel' | 'travelers' | 'travelersinsurance' | 'trust' | 'trv' | 'tt' | 'tube' | 'tui' | 'tunes' | 'tushu' | 'tv' | 'tvs' | 'tw' | 'tz' | 'ua' | 'ubank' | 'ubs' | 'ug' | 'uk' | 'unicom' | 'university' | 'uno' | 'uol' | 'ups' | 'us' | 'uy' | 'uz' | 'va' | 'vacations' | 'vana' | 'vanguard' | 'vc' | 've' | 'vegas' | 'ventures' | 'verisign' | 'vermögensberater' | 'vermögensberatung' | 'versicherung' | 'vet' | 'vg' | 'vi' | 'viajes' | 'video' | 'vig' | 'viking' | 'villas' | 'vin' | 'vip' | 'virgin' | 'visa' | 'vision' | 'viva' | 'vivo' | 'vlaanderen' | 'vn' | 'vodka' | 'volkswagen' | 'volvo' | 'vote' | 'voting' | 'voto' | 'voyage' | 'vu' | 'vuelos' | 'wales' | 'walmart' | 'walter' | 'wang' | 'wanggou' | 'watch' | 'watches' | 'weather' | 'weatherchannel' | 'webcam' | 'weber' | 'website' | 'wedding' | 'weibo' | 'weir' | 'wf' | 'whoswho' | 'wien' | 'wiki' | 'williamhill' | 'win' | 'windows' | 'wine' | 'winners' | 'wme' | 'wolterskluwer' | 'woodside' | 'work' | 'works' | 'world' | 'wow' | 'ws' | 'wtc' | 'wtf' | 'xbox' | 'xerox' | 'xfinity' | 'xihuan' | 'xin' | 'xxx' | 'xyz' | 'yachts' | 'yahoo' | 'yamaxun' | 'yandex' | 'ye' | 'yodobashi' | 'yoga' | 'yokohama' | 'you' | 'youtube' | 'yt' | 'yun' | 'za' | 'zappos' | 'zara' | 'zero' | 'zip' | 'zm' | 'zone' | 'zuerich' | 'zw' ;

fragment OtherTLDLikeEnds :  'htm' | 'html' | 'php';
fragment IPv4Addres     : ( [1-9]([0-9][0-9]?)?'.'[1-9]([0-9][0-9]?)?'.'[1-9]([0-9][0-9]?)?'.'[1-9]([0-9][0-9]?)? ) ;
fragment UrlHostname    :  'localhost' | ( [a-zA-Z\-_] [a-zA-Z0-9\-_]+ ('.'[a-zA-Z0-9\-_]+)* '.' ( TLD | OtherTLDLikeEnds )) ;
fragment UrlPathA       :  ('/'|'?') [a-zA-Z] [a-zA-Z0-9\-_~=?&%+.:/#]* ;
fragment UrlPathN       :  ('/'|'?') [0-9][a-zA-Z0-9\-_]* '/' [a-zA-Z0-9\-_~=?&%+.:/#]* ;
fragment UrlPathP       :  ('/'|'?') [a-zA-Z0-9\-_~=?&%+.:/#]* ;
fragment ProtoURL       :  ((('http'|'ftp') 's'? ':')  '//' )  UrlHostname (':'[0-9]+)? UrlPathP ;
fragment BasicURL       :  ((('http'|'ftp') 's'? ':')? '//' )? UrlHostname (':'[0-9]+)? (UrlPathA|UrlPathN)? ;
fragment IPv4URL        :  ((('http'|'ftp') 's'? ':')? '//' )  IPv4Addres  (':'[0-9]+)? (UrlPathA|UrlPathN)? ;

fragment AllURLs        : ProtoURL | BasicURL | IPv4URL;

URL
    : ( AllURLs | '<' AllURLs '>' | '<a href="' AllURLs '">'~[<]+'</a>' )
    ;

// Based upon https://github.com/antlr/antlr4/blob/master/doc/case-insensitive-lexing.md
fragment A : [aA]; // match either an 'a' or 'A'
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];

// In some (rare!) cases the only way to get the correct parse is by treating
// these words that appear as a 'version' (without digits!!) in a special way.
SPECIALVERSIONWORDS
    // Fixes: "Ubuntu/dapper Something/4.4.4"
//    : W A R T Y       | W A R T Y        '-' S E C U R I T Y // Codename of Ubuntu  4.10
//    | H O A R Y       | H O A R Y        '-' S E C U R I T Y // Codename of Ubuntu  5.04
//    | B R E E Z Y     | B R E E Z Y      '-' S E C U R I T Y // Codename of Ubuntu  5.10
    : D A P P E R     | D A P P E R      '-' S E C U R I T Y // Codename of Ubuntu  6.06
//    | E D G Y         | E D G Y          '-' S E C U R I T Y // Codename of Ubuntu  6.10
//    | F E I S T Y     | F E I S T Y      '-' S E C U R I T Y // Codename of Ubuntu  7.04
//    | G U T S Y       | G U T S Y        '-' S E C U R I T Y // Codename of Ubuntu  7.10
    | H A R D Y       | H A R D Y        '-' S E C U R I T Y // Codename of Ubuntu  8.04
//    | I N T R E P I D | I N T R E P I D  '-' S E C U R I T Y // Codename of Ubuntu  8.10
//    | J A U N T Y     | J A U N T Y      '-' S E C U R I T Y // Codename of Ubuntu  9.04
//    | K A R M I C     | K A R M I C      '-' S E C U R I T Y // Codename of Ubuntu  9.10
//    | L U C I D       | L U C I D        '-' S E C U R I T Y // Codename of Ubuntu 10.04
//    | M A V E R I C K | M A V E R I C K  '-' S E C U R I T Y // Codename of Ubuntu 10.10
//    | N A T T Y       | N A T T Y        '-' S E C U R I T Y // Codename of Ubuntu 11.04
//    | O N E I R I C   | O N E I R I C    '-' S E C U R I T Y // Codename of Ubuntu 11.10
//    | P R E C I S E   | P R E C I S E    '-' S E C U R I T Y // Codename of Ubuntu 12.04
//    | Q U A N T A L   | Q U A N T A L    '-' S E C U R I T Y // Codename of Ubuntu 12.10
//    | R A R I N G     | R A R I N G      '-' S E C U R I T Y // Codename of Ubuntu 13.04
//    | S A U C Y       | S A U C Y        '-' S E C U R I T Y // Codename of Ubuntu 13.10
//    | T R U S T Y     | T R U S T Y      '-' S E C U R I T Y // Codename of Ubuntu 14.04
//    | U T O P I C     | U T O P I C      '-' S E C U R I T Y // Codename of Ubuntu 14.10
//    | V I V I D       | V I V I D        '-' S E C U R I T Y // Codename of Ubuntu 15.04
//    | W I L Y         | W I L Y          '-' S E C U R I T Y // Codename of Ubuntu 15.10
//    | X E N I A L     | X E N I A L      '-' S E C U R I T Y // Codename of Ubuntu 16.04
//    | Y A K K E T Y   | Y A K K E T Y    '-' S E C U R I T Y // Codename of Ubuntu 16.10
//    | Z E S T Y       | Z E S T Y        '-' S E C U R I T Y // Codename of Ubuntu 17.04
//    | A R T F U L     | A R T F U L      '-' S E C U R I T Y // Codename of Ubuntu 17.10
//    | B I O N I C     | B I O N I C      '-' S E C U R I T Y // Codename of Ubuntu 18.04
//    | C O S M I C     | C O S M I C      '-' S E C U R I T Y // Codename of Ubuntu 18.10
//    | D I S C O       | D I S C O        '-' S E C U R I T Y // Codename of Ubuntu 19.04
    ;

UNASSIGNEDVARIABLE
    : '@'  [-_0-9a-zA-Z.]+ '@'
    | '${' [-_0-9a-zA-Z.]+ '}'
    | SPECIALVERSIONWORDS
    ;

GIBBERISH
    : '@'(~[ ;)])*(~[-+_0-9a-zA-Z. ;)])+(~[ ;)])*
    ;

ATSIGN : '@' ;

// A version is a WORD with at least 1 number in it (and that can contain a '-').
VERSION
    : (~[@0-9+;{}()\\/ \t:=[\]",])*[0-9]+([,][0-9]+)?(~[@+;{}()\\/ \t:=[\]",])*
    | UNASSIGNEDVARIABLE
    ;

fragment WORDLetter
    : (~[@0-9+;,{}()\\/ \t:=[\]"-])            // Normal letters
    | '\\x'[0-9a-f][0-9a-f]                    // Hex encoded letters \xab\x12
    | SPECIALVERSIONWORDS
    ;

WORD
    // Sometime we get '-=Some thing=-' ... because it looks cool ...?
    : '-='? MINUS* WORDLetter+ (MINUS+ WORDLetter+ )* MINUS* '=-'?
//    | SPACE MINUS SPACE
    | UNASSIGNEDVARIABLE
    ;

// Base64 Encoded strings: Note we do NOT recognize the variant where the '-' is used because that conflicts with the uuid
// We find that there are cases when a normal string looks like a Base 64 but isn't.
// So I came up with some silly (and incorrect) boundaries between "base64" and "not base64":
// - It may not start with a special character (like '/' )
fragment B64LetterBase      : [a-zA-Z0-9];
fragment B64LetterSpecial   : [+?_/];
fragment B64Letter          : B64LetterBase | B64LetterSpecial;
fragment B64FirstChunk      : B64LetterBase B64Letter B64Letter B64Letter;

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
BASE64
    : B64FirstChunk B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk B64Chunk+ B64LastChunk
    ;

// =========================================================================================
// Parser

userAgent
    : (SEMICOLON|COMMA|MINUS|PLUS|'\''|'"'|'\\'|';'|'='|BRACEOPEN|BLOCKOPEN)*    // Leading garbage
      ( (SEMICOLON|COMMA|MINUS)? ( product | rootElements ) )*
      (SEMICOLON|COMMA|MINUS|PLUS|'\''|'"'|'\\'|';'|'='|BRACECLOSE|BLOCKCLOSE)*  // Trailing garbage
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
    | WORD+
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
                    (  COLON? (SLASH+|MINUS|ATSIGN) EQUALS?      (productVersionWithCommas|productVersionSingleWord) COMMA? )*
                    (  (SLASH|ATSIGN)? (SEMICOLON|MINUS)?        commentBlock
                       ( (SLASH|ATSIGN)+  EQUALS?                (productVersionWithCommas|productVersionSingleWord) COMMA?)* )*
                    (SLASH EOF)?

    | productName   (  (SLASH|ATSIGN)? (SEMICOLON|MINUS)?        commentBlock
                       ( (SLASH|ATSIGN)+  EQUALS?                (productVersionWithCommas|productVersionSingleWord) COMMA?)* )+
                    (SLASH EOF)?

    | productName   (  COLON? (SLASH+|MINUS|ATSIGN) productVersionWords
                        ( (SLASH|ATSIGN)* productVersionWithCommas COMMA?)*
                        (SLASH|ATSIGN)? (SEMICOLON|MINUS)?       commentBlock ?    )+
                    (SLASH EOF)?

    | productName   (  COLON? (SLASH+|MINUS|ATSIGN) EQUALS?      (productVersionWithCommas|productVersionSingleWord) COMMA?)+
                    (  (SLASH|ATSIGN)? (SEMICOLON|MINUS)?        commentBlock
                       ( (SLASH|ATSIGN)+  EQUALS?                (productVersionWithCommas|productVersionSingleWord) COMMA?)* )*
                    (SLASH EOF)?

    | productName   (SLASH EOF)
    ;

commentProduct
    : productName   (                       productVersionWithCommas )+
                    (   (SLASH|ATSIGN)+  EQUALS?     (productVersionWithCommas|productVersionSingleWord) COMMA?)*
                    (   (SLASH|ATSIGN)?  MINUS?      commentBlock
                        ( (SLASH|ATSIGN)+  EQUALS?   (productVersionWithCommas|productVersionSingleWord) COMMA?)* )*

    | productName   (   (SLASH|ATSIGN)? MINUS?       commentBlock
                        ( (SLASH|ATSIGN)+  EQUALS?   (productVersionWithCommas|productVersionSingleWord) COMMA?)* )+

    | productName   (   COLON? (SLASH+|MINUS|ATSIGN) productVersionWords
                        ( (SLASH|ATSIGN)* productVersionWithCommas COMMA?)*            )+
                    (   MINUS?              commentBlock
                        ( (SLASH|ATSIGN)+  EQUALS?   (productVersionWithCommas|productVersionSingleWord) COMMA?)* )*

    | productName   (   (SLASH+|MINUS|ATSIGN)  EQUALS?     (productVersionWithCommas) COMMA?)+
                    (   MINUS?              commentBlock
                        ( (SLASH|ATSIGN)+  EQUALS?   (productVersionWithCommas|productVersionSingleWord) COMMA?)* )*
    ;

productVersionWords
    : WORD (MINUS? WORD)*
    | UNASSIGNEDVARIABLE
    | SPECIALVERSIONWORDS
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
    | SPECIALVERSIONWORDS
    ;

productVersionWithCommas
    : keyValue
    | emailAddress
    | siteUrl
    | uuId
    | base64
    | singleVersionWithCommas
    | SPECIALVERSIONWORDS
    // This next one only occurs in a Facebook useragent
    | CURLYBRACEOPEN keyValue ( COMMA keyValue )* CURLYBRACECLOSE
    ;


productVersionSingleWord
    : WORD
    | CURLYBRACEOPEN WORD CURLYBRACECLOSE
    ;

singleVersion
    : VERSION
    ;

singleVersionWithCommas
    : VERSION (COMMA VERSION)*
    ;

productNameVersion
    : VERSION (MINUS? WORD)*
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
            UNASSIGNEDVARIABLE*
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
        (
            (CURLYBRACEOPEN ( uuId | siteUrl | emailAddress | multipleWords | base64 | keyValueVersionName ) CURLYBRACECLOSE ) |
            ( uuId | siteUrl | emailAddress | multipleWords | base64 | keyValueVersionName )
        )
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
    | WORD* GIBBERISH WORD*
    | MINUS
    | UNASSIGNEDVARIABLE
    ;

versionWords
    : VERSION+
    | SPECIALVERSIONWORDS
    ;
