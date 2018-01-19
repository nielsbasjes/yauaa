You can run this in docker by doing essentially the following steps:

First make sure the entire software is built

    (cd .. && mvn clean package )

Then in this directory build the image 

    mvn dockerfile:build

And finally run it with something like this

    docker run -p 8080:8080 -t nl.basjes/yauaa-webapp:latest

