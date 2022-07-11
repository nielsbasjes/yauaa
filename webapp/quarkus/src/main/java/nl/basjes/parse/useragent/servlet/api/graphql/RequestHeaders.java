package nl.basjes.parse.useragent.servlet.api.graphql;

import java.util.Map;
import java.util.TreeMap;

public class RequestHeaders {
    public String userAgent;               // User-Agent
    public String secChUa;                 // Sec-CH-UA
    public String secChUaArch;             // Sec-CH-UA-Arch
    public String secChUaBitness;          // Sec-CH-UA-Bitness
    public String secChUaFullVersion;      // Sec-CH-UA-Full-Version
    public String secChUaFullVersionList;  // Sec-CH-UA-Full-Version-List
    public String secChUaMobile;           // Sec-CH-UA-Mobile
    public String secChUaModel;            // Sec-CH-UA-Model
    public String secChUaPlatform;         // Sec-CH-UA-Platform
    public String secChUaPlatformVersion;  // Sec-CH-UA-Platform-Version
    public String secChUaWoW64;            // Sec-CH-UA-WoW64

    public Map<String, String> toMap() {
        Map<String, String> result = new TreeMap<>();
        result.put("User-Agent", userAgent);
        if (secChUa                != null) { result.put("Sec-CH-UA",                   secChUa);                }
        if (secChUaArch            != null) { result.put("Sec-CH-UA-Arch",              secChUaArch);            }
        if (secChUaBitness         != null) { result.put("Sec-CH-UA-Bitness",           secChUaBitness);         }
        if (secChUaFullVersion     != null) { result.put("Sec-CH-UA-Full-Version",      secChUaFullVersion);     }
        if (secChUaFullVersionList != null) { result.put("Sec-CH-UA-Full-Version-List", secChUaFullVersionList); }
        if (secChUaMobile          != null) { result.put("Sec-CH-UA-Mobile",            secChUaMobile);          }
        if (secChUaModel           != null) { result.put("Sec-CH-UA-Model",             secChUaModel);           }
        if (secChUaPlatform        != null) { result.put("Sec-CH-UA-Platform",          secChUaPlatform);        }
        if (secChUaPlatformVersion != null) { result.put("Sec-CH-UA-Platform-Version",  secChUaPlatformVersion); }
        if (secChUaWoW64           != null) { result.put("Sec-CH-UA-WoW64",             secChUaWoW64);           }
        return result;
    }

    @Override
    public String toString() {
        return "RequestHeaders:" + toMap();
    }
}
