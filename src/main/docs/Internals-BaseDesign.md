Parsing Useragents
==================
Parsing useragents is considered by many to be a ridiculously hard problem.
The main problems are:

- Although there seems to be a specification, many do not follow it.
- Useragents LIE that they are their competing predecessor with an extra flag.

The pattern the 'normal' browser builders are following is that they all LIE about the ancestor they are trying to improve upon.

The reason this system (historically) works is because a lot of website builders do a very simple check to see if they can use a specific feature.

    if (useragent.contains("Chrome")) {
       // Use the chrome feature we need.
    }

Some may improve on this an actually check the (major) version that follows.

A good example of this is the Edge browser:

    Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10136

It says it:

- is Mozilla/5.0
- uses AppleWebKit/537.36
- for "compatibility" the AppleWebKit lie about being "KHTML" and that it is similar to "Gecko" are also copied
- is Chrome 42
- is Safari 537
- is Edge 12

So any website looking for the word it triggers upon will find it and enable the right features.

In 2014 an RFC for HTTP was released ([RFC 7231 section 5.5.3](https://tools.ietf.org/html/rfc7231#section-5.5.3)) which now explitly states:

    ... implementations are encouraged not to use the product
    tokens of other implementations in order to declare compatibility
    with them, as this circumvents the purpose of the field. 

... encouraged ...

How many other analyzers work
=============================
When looking at most implementations of analysing the useragents I see that most implementations are based around
lists of regular expressions.
These are (in the systems I have seen) executed in a specific order to find the first one that matches.

In this solution direction the order in which things occur determines if the patterns match or not.

Regular expressions are notoriously hard to write and debug and (unless you make them really complex) the order in which parts of the pattern occur is fixed.

Core design idea
================
I wanted to see if a completely different approach would work: Can we actually parse these things into a tree and work from there.

The parser (ANTLR4 based) will be able to parse a lot of the agents but not all.
Tests have shown that it will parse >99% of all useragents on a large website which is more than 99.99% of the traffic.

Now the ones that it is not able to parse are the ones that have been set manually to a invalid value.
So if that happens we assume you are a hacker.
In all other cases we have matchers that are triggered if a specific value is found by the parser.
Such a matcher then tells this class is has found a match for a certain attribute with a certain confidence level (0-10000).
In the end the matcher that has found a match with the highest confidence for a value 'wins'.


High level implementation overview
==================================================
The main concept of this useragent parser is that we have two things:

1. A Parser (ANTLR4) that converts the useragent into a nice tree through which we can walk along.
2. A collection of matchers.
  - A matcher triggers if a set of patterns is present in the tree.
  - Each pattern is detected by a "matcher action" that triggers and can fill a single attribute.
    If a matcher triggers a set of attributes get set with a value and a confidence level
  - All results from all triggered matchers (and actions) are combined and for each individual attribute the 'highest value' wins.

As a performance optimization we walk along the parsed tree once and fire everything we find into a precomputed hashmap that
points to all the applicable matcher actions. As a consequence

  - the matching is relatively fast even though the number of matchers already runs into the few hundreds.
  - the startup is "slow"
  - the memory footprint is pretty big due to the number of matchers, the size of the hashmap and the cache of the parsed useragents.

A much more in depth explanation can be found in the documentation on [how to create new rules](Internals-MakingNewRules.md)
