+++
title = "Building from source"
weight = 10
+++
## Building
Requirements:
- A Linux class machine (can be a VM).
  - Some of the build scripts rely in bash/sed/grep and related tools, so it will not build on a Windows machine. I'm unsure if it will build on a Mac.
- The normal build tools for a Java project
  - JDK 17, 21 and 25 all need to be installed.
    - All of these are needed to ensure the code works in all UDFs.

For convenience: the `./start-docker.sh` script launches a docker based build environment with all needed tools and configs.

and then simply do:

```bash
./mvnw clean package
```

If you want to build quicker by skipping ALL code quality validations and integration tests (but still run the JUnit tests) you can do

```bash
./mvnw clean install -PbasicQuality
```

If you want to `just build` and skip ALL code quality validations, tests and integration tests you can do

```bash
./mvnw clean install -PskipQuality
```
