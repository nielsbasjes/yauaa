On 2017-02-06 from http://www-01.sil.org/iso639-3/download.asp I downloaded the file http://www-01.sil.org/iso639-3/iso-639-3.tab

On 2021-04-22 redownloaded http://www-01.sil.org/iso639-3/iso-639-3.tab and fixed some trailing spaces and bad line endings.
NOTE: I kept the "removals" to ensure being able to recognise "old" things.

On 2025-02-01 redownloaded https://iso639-3.sil.org/sites/iso639-3/files/downloads/iso-639-3.tab and fixed some trailing spaces/tabs.
NOTE: I kept the "removals" to ensure being able to recognise "old" things.

Schema of this file (tab separated)

    Id        char(3) NOT NULL,        -- The three-letter 639-3 identifier
    Part2B    char(3) NULL,            -- Equivalent 639-2 identifier of the bibliographic applications
                                       -- code set, if there is one
    Part2T    char(3) NULL,            -- Equivalent 639-2 identifier of the terminology applications code
                                       -- set, if there is one
    Part1     char(2) NULL,            -- Equivalent 639-1 identifier, if there is one
    Scope     char(1) NOT NULL,        -- I(ndividual), M(acrolanguage), S(pecial)
    Type      char(1) NOT NULL,        -- A(ncient), C(onstructed),
                                       -- E(xtinct), H(istorical), L(iving), S(pecial)
    Ref_Name  varchar(150) NOT NULL,   -- Reference language name
    Comment   varchar(150) NULL)       -- Comment relating to one or more of the columns

In addition, language codes from several sources
  http://www.localeplanet.com/icu/index.html
  https://lingohub.com/documentation/developers/supported-locales/language-designators-with-regions/
