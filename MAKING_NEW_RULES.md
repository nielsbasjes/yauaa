Detecting new useragent patterns
===============================================================
When you find a useragent for which one or more of the fields are wrong there is the need to change the patterns and rules
that are used by this system for classifying these attributes.
In order to write rules this first described how the system works and what tools have been created to make writing new rules easier.

Base problem: They all lie
==========================
When looking at useragents it is clear that almost all of them include the name of predecessors/competitors
with which they are supposed to be compatible with.

So in general there is a ranking in the patterns; some are more true than others.

Solution overview
=================
The way this system solves all of this is by employing several steps:

1. The user agent string is parsed into a tree using Antlr4.
2. This tree is matched against a set of "Matchers"
   A matcher is
   * a set of patterns that must be present in the tree
   * a set of field/value combinations with a 'weight'
     the value can be either a fixed value or a part of the tree.
3. For all matchers where ALL required patterns were present the
   field/value/weight results are all combined. For fields where
   multiple values were present the value with the highest weight 'wins'

All matchers are with tests placed in yaml files in the UserAgents directory.
Because of the system with the weights the order of the matchers is not
checked or guaranteed in any way. So if the two matchers may set the same
field to a different value then better make sure they have different weights.

Useragent parse tree model
==========================
**TODO**

Flattened form of the tree
==========================
**TODO**

Walking around the tree
==========================
**TODO**

Creating a new rule
===================
Assume we got this useragent

    Mozilla/5.0 (compatible; Foo/3.1; Bar)

and let's just assume we want to set a field if the minor version of Foo is 1

To start we put the agent as a new test in one of the yaml files.
If we choose to create a new file make sure is starts with "config:"

      - test:
          input:
    #        name: 'You can give the test case a name'
            user_agent_string: 'Mozilla/5.0 (compatible; Foo/3.1; Bar)'

**TODO**




