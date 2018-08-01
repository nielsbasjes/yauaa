On 2017-02-06 from http://www-01.sil.org/iso639-3/download.asp I downloaded the file http://www-01.sil.org/iso639-3/iso-639-3.tab

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

In addition language codes from serveral sources
  http://www.localeplanet.com/icu/index.html
  https://lingohub.com/documentation/developers/supported-locales/language-designators-with-regions/
