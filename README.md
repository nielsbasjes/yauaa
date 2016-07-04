Parsing Useragents
===

Parsing useragents is considered by many to be a rediculously hard problem.
The main problems are:

- Although there seems to be a specification, many do not follow it.
- Useragents LIE that they are their competing predecessor with an extra flag.

The pattern the 'normal' browser builders are following is that they all LIE about the ancestor they are trying to improve upon.
A good example of this is the Edge browser:

    Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10136

Implementation
===
When looking at most implementations of analysing the useragents I see that most implementations are based around
lists of regular expressions.


Core design idea:
The parser (ANTLR4 based) will be able to parse a lot of the agents but not all.
Tests have shown that it will parse >99% of all useragents on a large website which is more than 99.99% of the traffic.
Now the ones that it is not able to parse are the ones that have been set manually to a invalid value.
So if that happens we assume you are a hacker.
In all other cases we have matchers that are triggered if a sepcific value is found by the parser.
Such a matcher then tells this class is has found a match for a certain attribute with a certain confidence level (0-100).
In the end the matcher that has found a match with the highest confidence for a value 'wins'.



License
===
    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2016 Niels Basjes

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
