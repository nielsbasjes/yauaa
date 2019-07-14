# A Parameterized Visitor for ANTLR4 
In the Yauaa project I need to pass a parameter along with my visitor.

What I'm trying to do is have a tree stucture with 'patterns' and while walking recursively through 
the parse tree using the visitor match my parse result against the 'pattern tree'.

This parameter is therefor the node in the other tree.

There has been discussion about such a wish before

https://github.com/antlr/antlr4/issues/641

What this project does is simply change only the files that need to be changed and change the code generation accordingly.

# This project
I simply copy a small number of Java files (including the Java.stg) from the original Antlr 4 project.

In my application I use the Java.stg to generate different application code and 
use this as a dependency to override the official Antlr4 classes.

I did make some changes to the code that relate to coding style (i.e. tabs, using braces, silly things like that).

# LICENSE
This part of Yauaa is essentially a relatively simple (but not backward compatible!) change of the existing Antlr 4.7.2 code.

These files are simply some of the original Antlr4 files with a generic added here and there.

So this is >99% a 1-on-1 copy of the Antlr4 code.

    Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
    Use of this file is governed by the BSD 3-clause license that
    can be found in the LICENSE.txt file in the project root.

The mentioned LICENSE.txt is part of this project.

I put these trivial modifications under the exact same license.

    Copyright (c) 2019 Niels Basjes. All rights reserved.
    Use of this file is governed by the BSD 3-clause license. 

Note that I explicitly allow the Antlr4 project to pull any part of this into the original Antlr4 
and distribute it as a contribution to that project under their license.
