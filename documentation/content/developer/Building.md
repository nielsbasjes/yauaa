+++
title = "Building from source"
weight = 10
+++
## Building
Requirements:
- A Linux class machine (can be a VM)
- The normal build tools for a Java project (i.e. Maven 3.6+ and jdk 17)
  - The `./start-docker.sh` script launches a docker based build environment with all needed tools.

and then simply do:

```bash
mvn clean package
```
