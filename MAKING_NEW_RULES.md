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

and let's just assume we want to set a field called MinorFooVersion if
the minor version of Foo is 1

To start we put the agent as a new test in one of the yaml files.
If we choose to create a new file make sure is starts with "config:"

    - test:
        input:
          user_agent_string: 'Mozilla/5.0 (compatible; Foo/3.1; Bar)'

Now we run the unit test "TestPredefinedBrowsers".
On a normal computer this will take only about 1-5 seconds.

The output contains all the field values for this useragent in the exact form of a unit test.

So once you are happy with the result you can simply copy and past this into the yaml file.

In this case is looks something like this:

    - test:
    #    options:
    #    - 'verbose'
    #    - 'init'
    #    - 'only'
        input:
          user_agent_string: 'Mozilla/5.0 (compatible; Foo/3.1; Bar)'
        expected:
          DeviceClass                          : 'Unknown'
          DeviceName                           : 'Unknown'
          OperatingSystemClass                 : 'Unknown'
          OperatingSystemName                  : 'Unknown'
          OperatingSystemVersion               : '??'
          LayoutEngineClass                    : 'Browser'
          LayoutEngineName                     : 'Mozilla'
          LayoutEngineVersion                  : '5.0'
          LayoutEngineVersionMajor             : '5'
          LayoutEngineNameVersion              : 'Mozilla 5.0'
          LayoutEngineNameVersionMajor         : 'Mozilla 5'
          AgentClass                           : 'Browser'
          AgentName                            : 'Foo'
          AgentVersion                         : '3.1'
          AgentVersionMajor                    : '3'
          AgentNameVersion                     : 'Foo 3.1'
          AgentNameVersionMajor                : 'Foo 3'

The output also contains all hard checked paths in the tree in thr rough form
of a matcher.

    - matcher:
    #    options:
    #    - 'verbose'
        require:
    #    - '__SyntaxError__="false"'
    #    - 'agent="Mozilla/5.0 (compatible; Foo/3.1; Bar)"'
    #    - 'agent.(1)product="Mozilla/5.0 (compatible; Foo/3.1; Bar)"'
    #    - 'agent.(1)product.(1)name="Mozilla"'
    #    - 'agent.(1)product.(1)name#1="Mozilla"'
    #    - 'agent.(1)product.(1)name%1="Mozilla"'
    #    - 'agent.(1)product.(1)version="5.0"'
    #    - 'agent.(1)product.(1)version#1="5"'
    #    - 'agent.(1)product.(1)version%1="5"'
    #    - 'agent.(1)product.(1)version#2="5.0"'
    #    - 'agent.(1)product.(1)version%2="0"'
    #    - 'agent.(1)product.(1)comments="(compatible; Foo/3.1; Bar)"'
    #    - 'agent.(1)product.(1)comments.(1)entry="compatible"'
    #    - 'agent.(1)product.(1)comments.(1)entry#1="compatible"'
    #    - 'agent.(1)product.(1)comments.(1)entry%1="compatible"'
    #    - 'agent.(1)product.(1)comments.(1)entry.(1)text="compatible"'
    #    - 'agent.(1)product.(1)comments.(1)entry.(1)text#1="compatible"'
    #    - 'agent.(1)product.(1)comments.(1)entry.(1)text%1="compatible"'
    #    - 'agent.(1)product.(1)comments.(2)entry="Foo/3.1"'
    #    - 'agent.(1)product.(1)comments.(2)entry#1="Foo"'
    #    - 'agent.(1)product.(1)comments.(2)entry%1="Foo"'
    #    - 'agent.(1)product.(1)comments.(2)entry#2="Foo/3"'
    #    - 'agent.(1)product.(1)comments.(2)entry%2="3"'
    #    - 'agent.(1)product.(1)comments.(2)entry#3="Foo/3.1"'
    #    - 'agent.(1)product.(1)comments.(2)entry%3="1"'
    #    - 'agent.(1)product.(1)comments.(2)entry.(1)product="Foo/3.1"'
    #    - 'agent.(1)product.(1)comments.(2)entry.(1)product.(1)name="Foo"'
    #    - 'agent.(1)product.(1)comments.(2)entry.(1)product.(1)name#1="Foo"'
    #    - 'agent.(1)product.(1)comments.(2)entry.(1)product.(1)name%1="Foo"'
    #    - 'agent.(1)product.(1)comments.(2)entry.(1)product.(1)version="3.1"'
    #    - 'agent.(1)product.(1)comments.(2)entry.(1)product.(1)version#1="3"'
    #    - 'agent.(1)product.(1)comments.(2)entry.(1)product.(1)version%1="3"'
    #    - 'agent.(1)product.(1)comments.(2)entry.(1)product.(1)version#2="3.1"'
    #    - 'agent.(1)product.(1)comments.(2)entry.(1)product.(1)version%2="1"'
    #    - 'agent.(1)product.(1)comments.(3)entry="Bar"'
    #    - 'agent.(1)product.(1)comments.(3)entry#1="Bar"'
    #    - 'agent.(1)product.(1)comments.(3)entry%1="Bar"'
    #    - 'agent.(1)product.(1)comments.(3)entry.(1)text="Bar"'
    #    - 'agent.(1)product.(1)comments.(3)entry.(1)text#1="Bar"'
    #    - 'agent.(1)product.(1)comments.(3)entry.(1)text%1="Bar"'
        extract:
    #    - 'DeviceClass           :   1:'
    #    - 'DeviceBrand           :   1:'
    #    - 'DeviceName            :   1:'
    #    - 'OperatingSystemClass  :   1:'
    #    - 'OperatingSystemName   :   1:'
    #    - 'OperatingSystemVersion:   1:'
    #    - 'LayoutEngineClass     :   1:'
    #    - 'LayoutEngineName      :   1:'
    #    - 'LayoutEngineVersion   :   1:'
    #    - 'AgentClass            :   1:'
    #    - 'AgentName             :   1:'
    #    - 'AgentVersion          :   1:'

In our case we are only interested in the second digit of the version of the product named "Foo"

So we copy this and make it like this:

    - matcher:
        extract:
        - 'MinorFooVersion :   1:agent.(1)product.(1)comments.entry.(1)product.(1)name="Foo"^.version%2'

What this does is that in the first product, in the first set of comments there is at any position an entry
that contains as the first element a product who's name is "Foo" that we go up in the tree and then down
to the version of that product and then take the second word.

Now if we run the unit test again we will see this:

    - test:
    #    options:
    #    - 'verbose'
    #    - 'init'
    #    - 'only'
        input:
          user_agent_string: 'Mozilla/5.0 (compatible; Foo/3.1; Bar)'
        expected:
          DeviceClass                          : 'Unknown'
          DeviceName                           : 'Unknown'
          OperatingSystemClass                 : 'Unknown'
          OperatingSystemName                  : 'Unknown'
          OperatingSystemVersion               : '??'
          LayoutEngineClass                    : 'Browser'
          LayoutEngineName                     : 'Mozilla'
          LayoutEngineVersion                  : '5.0'
          LayoutEngineVersionMajor             : '5'
          LayoutEngineNameVersion              : 'Mozilla 5.0'
          LayoutEngineNameVersionMajor         : 'Mozilla 5'
          AgentClass                           : 'Browser'
          AgentName                            : 'Foo'
          AgentVersion                         : '3.1'
          AgentVersionMajor                    : '3'
          AgentNameVersion                     : 'Foo 3.1'
          AgentNameVersionMajor                : 'Foo 3'
          MinorFooVersion                      : '1'

As you can see this clearly shows that a new field has been added with the value we wanted.


**TODO**


Where the rules are located
===========================
Under the main resources there is a folder called *UserAgents* .
In the folder a collection of yaml files are located.
Each of those files contains matchers, lookups, tests or any combination of them
in any order. This means that in many cases you'll find a relevant test case close
to the rules.

The overall structure is this:

    config:
    - lookup:
      name: 'lookupname'
      map:
        "From1" : "To1"
        "From2" : "To2"
        "From3" : "To3"

    - matcher:
        options:
        - 'verbose'
        require:
        - 'Require pattern'
        - 'Require pattern'
        extract:
        - 'Extract pattern'
        - 'Extract pattern'

    - test:
        options:
        - 'verbose'
        - 'init'
        input:
          user_agent_string: 'Useragent'
          name: 'The name of the test'
        expected:
          FieldName     : 'ExpectedValue'
          FieldName     : 'ExpectedValue'
          FieldName     : 'ExpectedValue'




