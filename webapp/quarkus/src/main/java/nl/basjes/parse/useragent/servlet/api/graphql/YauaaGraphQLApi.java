package nl.basjes.parse.useragent.servlet.api.graphql;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.servlet.ParseService;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.inject.Inject;
import java.util.Map;

@GraphQLApi
public class YauaaGraphQLApi {

    @Inject
    ParseService parseService;

    @Query("yauaa")
    @Description("Parse the provided headers")
    public ParsedAgent parse(@Name("requestHeaders") RequestHeaders requestHeaders) {
        UserAgent userAgent = parseService.getUserAgentAnalyzer().parse(requestHeaders.toMap());
        Map<String, String> resultMap = userAgent.toMap();

        ParsedAgent result = new ParsedAgent();
        result.requestHeaders                   = requestHeaders;
        result.fields                           = resultMap;
        result.deviceClass                      = resultMap.get("DeviceClass");
        result.deviceName                       = resultMap.get("DeviceName");
        result.deviceBrand                      = resultMap.get("DeviceBrand");
        result.deviceCpu                        = resultMap.get("DeviceCpu");
        result.deviceCpuBits                    = resultMap.get("DeviceCpuBits");
        result.deviceFirmwareVersion            = resultMap.get("DeviceFirmwareVersion");
        result.deviceVersion                    = resultMap.get("DeviceVersion");

        result.operatingSystemClass             = resultMap.get("OperatingSystemClass");
        result.operatingSystemName              = resultMap.get("OperatingSystemName");
        result.operatingSystemVersion           = resultMap.get("OperatingSystemVersion");
        result.operatingSystemVersionMajor      = resultMap.get("OperatingSystemVersionMajor");
        result.operatingSystemNameVersion       = resultMap.get("OperatingSystemNameVersion");
        result.operatingSystemNameVersionMajor  = resultMap.get("OperatingSystemNameVersionMajor");

        result.layoutEngineClass                = resultMap.get("LayoutEngineClass");
        result.layoutEngineName                 = resultMap.get("LayoutEngineName");
        result.layoutEngineVersion              = resultMap.get("LayoutEngineVersion");
        result.layoutEngineVersionMajor         = resultMap.get("LayoutEngineVersionMajor");
        result.layoutEngineNameVersion          = resultMap.get("LayoutEngineNameVersion");
        result.layoutEngineNameVersionMajor     = resultMap.get("LayoutEngineNameVersionMajor");

        result.agentClass                       = resultMap.get("AgentClass");
        result.agentName                        = resultMap.get("AgentName");
        result.agentVersion                     = resultMap.get("AgentVersion");
        result.agentVersionMajor                = resultMap.get("AgentVersionMajor");
        result.agentNameVersion                 = resultMap.get("AgentNameVersion");
        result.agentNameVersionMajor            = resultMap.get("AgentNameVersionMajor");

        return result;
    }

}
