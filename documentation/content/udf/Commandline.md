+++
title = "Commandline usage"
+++
## Deprecated
With version 6.0 the dedicated commandline tool was removed.

Primary reason is that it was not getting any attention,
and it did not perform well (mainly due to the relatively big startup overhead).

So if you have the need to use Yauaa from a commandline perspective the easiest way to do this is by starting
the docker based webservlet locally (and leave it running "for a long time") and use something like curl
to get the information you are looking for.

## Initial startup

Simply start the webservlet using docker and run it in the background (takes a few seconds):

```bash
docker pull nielsbasjes/yauaa:{{%YauaaVersion%}}
docker run --detach -p8080:8080 nielsbasjes/yauaa:{{%YauaaVersion%}}
```

## Doing a single value

```bash
AGENT="Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"

curl -X POST \
    -H "Content-Type: text/plain" \
    http://localhost:8080/yauaa/v1/analyze/yaml \
    --data-binary "${AGENT}"
```

Which outputs this:

```yaml
- test:
    input:
      user_agent_string: 'Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36'
    expected:
      DeviceClass                          : 'Phone'
      DeviceName                           : 'Google Nexus 6'
      DeviceBrand                          : 'Google'
      OperatingSystemClass                 : 'Mobile'
      OperatingSystemName                  : 'Android'
      OperatingSystemVersion               : '7.0'
      OperatingSystemVersionMajor          : '7'
      OperatingSystemNameVersion           : 'Android 7.0'
      OperatingSystemNameVersionMajor      : 'Android 7'
      OperatingSystemVersionBuild          : 'NBD90Z'
      LayoutEngineClass                    : 'Browser'
      LayoutEngineName                     : 'Blink'
      LayoutEngineVersion                  : '53.0'
      LayoutEngineVersionMajor             : '53'
      LayoutEngineNameVersion              : 'Blink 53.0'
      LayoutEngineNameVersionMajor         : 'Blink 53'
      AgentClass                           : 'Browser'
      AgentName                            : 'Chrome'
      AgentVersion                         : '53.0.2785.124'
      AgentVersionMajor                    : '53'
      AgentNameVersion                     : 'Chrome 53.0.2785.124'
      AgentNameVersionMajor                : 'Chrome 53'
```

## Doing a large number of values

You can upload a file with useragents to the rest interface (there is a size limitation for this).

```bash
curl -X POST \
    -H "Content-Type: text/plain" \
    http://localhost:8080/yauaa/v1/analyze/yaml \
    --data-binary "@useragents.txt"
```
