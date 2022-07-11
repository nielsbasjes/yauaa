package nl.basjes.parse.useragent.servlet.api.graphql;

import java.util.Map;

public class ParsedAgent {
    public YauaaGraphQLVersion yauaaVersion = YauaaGraphQLVersion.getInstance();

    public Map<String, String> fields;

    public String deviceClass                         ;
    public String deviceName                          ;
    public String deviceBrand                         ;
    public String deviceCpu                           ;
    public String deviceCpuBits                       ;
    public String deviceFirmwareVersion               ;
    public String deviceVersion                       ;
    public String operatingSystemClass                ;
    public String operatingSystemName                 ;
    public String operatingSystemVersion              ;
    public String operatingSystemVersionMajor         ;
    public String operatingSystemNameVersion          ;
    public String operatingSystemNameVersionMajor     ;
    public String layoutEngineClass                   ;
    public String layoutEngineName                    ;
    public String layoutEngineVersion                 ;
    public String layoutEngineVersionMajor            ;
    public String layoutEngineNameVersion             ;
    public String layoutEngineNameVersionMajor        ;
    public String agentClass                          ;
    public String agentName                           ;
    public String agentVersion                        ;
    public String agentVersionMajor                   ;
    public String agentNameVersion                    ;
    public String agentNameVersionMajor               ;

    public RequestHeaders requestHeaders;
}
