```graphql
query {
  yauaa(requestHeaders: {
      userAgent               : "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36"
      secChUa                 : "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\""
      secChUaArch             : "\"x86\""
      secChUaBitness          : "\"64\""
      secChUaFullVersion      : "\"103.0.5060.53\""
      secChUaFullVersionList  : "\".Not/A)Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"103.0.5060.53\", \"Chromium\";v=\"103.0.5060.53\""
      secChUaMobile           : "?0"
      secChUaModel            : "\"Basjes was here 3.14\""
      secChUaPlatform         : "\"Linux\""
      secChUaPlatformVersion  : "\"5.13.0\""
      secChUaWoW64            : "?0"
  }) {

     yauaaVersion {
      projectVersion
      gitCommitId
    }

    requestHeaders {
      userAgent
      secChUa
      secChUaArch
      secChUaBitness
      secChUaFullVersion
      secChUaFullVersionList
      secChUaMobile
      secChUaModel
      secChUaPlatform
      secChUaPlatformVersion
      secChUaWoW64
    }

    deviceClass
    deviceName
    deviceBrand
    deviceCpu
    deviceCpuBits
    operatingSystemClass
    operatingSystemName
    operatingSystemVersion
    operatingSystemVersionMajor
    operatingSystemNameVersion
    operatingSystemNameVersionMajor
    layoutEngineClass
    layoutEngineName
    layoutEngineVersion
    layoutEngineVersionMajor
    layoutEngineNameVersion
    layoutEngineNameVersionMajor
    agentClass
    agentName
    agentVersion
    agentVersionMajor
    agentNameVersion
    agentNameVersionMajor
  }
}

```
