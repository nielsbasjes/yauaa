/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.clienthints;

import com.esotericsoftware.kryo.Kryo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.basjes.collections.PrefixMap;
import nl.basjes.collections.prefixmap.StringPrefixMap;
import nl.basjes.parse.useragent.AgentField.MutableAgentField;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.clienthints.ClientHints.Brand;
import nl.basjes.parse.useragent.utils.VersionSplitter;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static java.lang.Boolean.TRUE;
import static nl.basjes.parse.useragent.UserAgent.AGENT_CLASS;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CPU;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CPU_BITS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_FIRMWARE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_CLASS;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.UACLIENT_HINT_ARCHITECTURE;
import static nl.basjes.parse.useragent.UserAgent.UACLIENT_HINT_BITNESS;
import static nl.basjes.parse.useragent.UserAgent.UACLIENT_HINT_BRANDS;
import static nl.basjes.parse.useragent.UserAgent.UACLIENT_HINT_FULL_VERSION;
import static nl.basjes.parse.useragent.UserAgent.UACLIENT_HINT_FULL_VERSION_LIST;
import static nl.basjes.parse.useragent.UserAgent.UACLIENT_HINT_MOBILE;
import static nl.basjes.parse.useragent.UserAgent.UACLIENT_HINT_MODEL;
import static nl.basjes.parse.useragent.UserAgent.UACLIENT_HINT_PLATFORM;
import static nl.basjes.parse.useragent.UserAgent.UACLIENT_HINT_PLATFORM_VERSION;
import static nl.basjes.parse.useragent.UserAgent.UACLIENT_HINT_WOW_64;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VALUE;
import static nl.basjes.parse.useragent.classify.DeviceClass.DESKTOP;
import static nl.basjes.parse.useragent.classify.DeviceClass.MOBILE;
import static nl.basjes.parse.useragent.classify.DeviceClass.PHONE;
import static nl.basjes.parse.useragent.classify.DeviceClass.TABLET;

public class ClientHintsAnalyzer extends ClientHintsHeadersParser {

    public ClientHintsAnalyzer() {
        super();
    }

    /**
     * This is used to configure the provided Kryo instance if Kryo serialization is desired.
     * The expected type here is Object because otherwise the Kryo library becomes
     * a mandatory dependency on any project that uses Yauaa.
     *
     * @param kryoInstance The instance of com.esotericsoftware.kryo.Kryo that needs to be configured.
     */
    public static void configureKryo(Object kryoInstance) {
        Kryo kryo = (Kryo) kryoInstance;
        kryo.register(ClientHintsAnalyzer.class);
        ClientHintsHeadersParser.configureKryo(kryoInstance);
    }

    // https://docs.microsoft.com/en-us/microsoft-edge/web-platform/how-to-detect-win11
    // Version      platformVersion
    // Win7/8/8.1   0
    // Win10        1-10
    // Win11        13+
    // https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform-version
    // If major is 6 and minor is 3 (i.e., Windows 8.1), return "0.3".
    // If major is 6 and minor is 2 (i.e., Windows 8), return "0.2".
    // If major is 6 and minor is 1 (i.e., Windows 7), return "0.1".

    @AllArgsConstructor
    private static final class OSFields implements Serializable {
        @Getter String name;              // Windows NT
        @Getter String version;           // 8.1
        @Getter String versionMajor;      // 8
        @Getter String nameVersion;       // Windows 8.1
        @Getter String nameVersionMajor;  // Windows 8
    }

    private static final PrefixMap<OSFields> WINDOWS_VERSION_MAPPING = new StringPrefixMap<>(false);
    static {
        WINDOWS_VERSION_MAPPING.put("0.1", new OSFields("Windows NT",  "7",   "7", "Windows 7",   "Windows 7"));
        WINDOWS_VERSION_MAPPING.put("0.2", new OSFields("Windows NT",  "8",   "8", "Windows 8",   "Windows 8"));
        WINDOWS_VERSION_MAPPING.put("0.3", new OSFields("Windows NT",  "8.1", "8", "Windows 8.1", "Windows 8"));
        WINDOWS_VERSION_MAPPING.put("1",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("2",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("3",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("4",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("5",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("6",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("7",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("8",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("9",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("10",  new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("13",  new OSFields("Windows NT", "11",  "11", "Windows 11",  "Windows 11"));
        WINDOWS_VERSION_MAPPING.put("14",  new OSFields("Windows NT", "11",  "11", "Windows 11",  "Windows 11"));
        WINDOWS_VERSION_MAPPING.put("15",  new OSFields("Windows NT", "11",  "11", "Windows 11",  "Windows 11"));
        WINDOWS_VERSION_MAPPING.put("16",  new OSFields("Windows NT", "11",  "11", "Windows 11",  "Windows 11"));
    }

    public MutableUserAgent merge(MutableUserAgent userAgent, ClientHints clientHints) {
        setCHBrandVersionsList(userAgent, UACLIENT_HINT_BRANDS,             clientHints.getBrands());
        setCHString(userAgent,            UACLIENT_HINT_ARCHITECTURE,       clientHints.getArchitecture());
        setCHString(userAgent,            UACLIENT_HINT_BITNESS,            clientHints.getBitness());
        setCHString(userAgent,            UACLIENT_HINT_FULL_VERSION,       clientHints.getFullVersion());
        setCHBrandVersionsList(userAgent, UACLIENT_HINT_FULL_VERSION_LIST,  clientHints.getFullVersionList());
        setCHBoolean(userAgent,           UACLIENT_HINT_MOBILE,             clientHints.getMobile());
        setCHString(userAgent,            UACLIENT_HINT_MODEL,              clientHints.getModel());
        setCHString(userAgent,            UACLIENT_HINT_PLATFORM,           clientHints.getPlatform());
        setCHString(userAgent,            UACLIENT_HINT_PLATFORM_VERSION,   clientHints.getPlatformVersion());
        setCHBoolean(userAgent,           UACLIENT_HINT_WOW_64,             clientHints.getWow64());

        improveOperatingSystem(userAgent, clientHints);
        improveMobileDeviceClass(userAgent, clientHints);
        improveDeviceBrandName(userAgent, clientHints);
        improveDeviceCPU(userAgent, clientHints);
        improveLayoutEngineAndAgentInfo(userAgent, clientHints);
        return userAgent;
    }

    private void setCHBrandVersionsList(MutableUserAgent userAgent, String baseFieldName, ArrayList<Brand> brands) {
        if (brands != null) {
            int i = 0;
            for (Brand brand : brands) {
                userAgent.set(baseFieldName + '_' + i + "_Brand",   brand.getName(),   1);
                userAgent.set(baseFieldName + '_' + i + "_Version", brand.getVersion(), 1);
                i++;
            }
        }
    }

    private void setCHString(MutableUserAgent userAgent, String fieldName, String value) {
        if (value != null) {
            userAgent.set(fieldName, value, 1);
        }
    }

    private void setCHBoolean(MutableUserAgent userAgent, String fieldName, Boolean value) {
        if (value != null) {
            userAgent.set(fieldName, TRUE.equals(value)  ? "true" : "false", 1);
        }
    }


    public void improveMobileDeviceClass(MutableUserAgent userAgent, ClientHints clientHints) {
        // Improve the Device Class if it is the vague "Mobile" thing.
        if (clientHints.getMobile() != null) {
            MutableAgentField deviceClass = userAgent.get(UserAgent.DEVICE_CLASS);
            if (MOBILE.getValue().equals(deviceClass.getValue())) {
                if (TRUE.equals(clientHints.getMobile())) {
                    deviceClass.setValue(PHONE.getValue(), deviceClass.getConfidence() + 1);
                } else {
                    deviceClass.setValue(TABLET.getValue(), deviceClass.getConfidence() + 1);
                }
            }
        }
    }

    /**
     * Some device names are "Generic" and thus must be overwritten if a Client hint is available.
     */
    private static final List<String> GENERIC_DEVICE_NAMES = Arrays.asList(
        UNKNOWN_VALUE,
        "Android Mobile",
        "Fuchsia Mobile",
        "Fuchsia Device",
        "iOS Device",
        "Desktop",
        "Linux Desktop"
    );

    public void improveDeviceBrandName(MutableUserAgent userAgent, ClientHints clientHints) {
        // Improve the Device Brand/Name if it is unknown.
        if (clientHints.getModel() != null) {
            MutableAgentField deviceName = userAgent.get(DEVICE_NAME);
            if (GENERIC_DEVICE_NAMES.contains(deviceName.getValue())) {
                overrideValue(deviceName, clientHints.getModel());
                overrideValue(userAgent.get(DEVICE_BRAND), NULL_VALUE); // There is a calculator for the device brand
            }
        }
    }

    public void improveDeviceCPU(MutableUserAgent userAgent, ClientHints clientHints) {
        // Improve the Device CPU has a better value.
        String architecture = clientHints.getArchitecture();
        String bitness = clientHints.getBitness();

        if (bitness != null && !bitness.isEmpty()) {
            overrideValue(userAgent.get(DEVICE_CPU_BITS), bitness);
        }

        if (architecture != null) {
            String newArchitecture;
            switch (architecture) {
                case "x86":
                    newArchitecture = "Intel x86_64";
                    if ("32".equals(bitness)) {
                        newArchitecture = "Intel x86";
                    }
                    break;
                case "arm":
                    newArchitecture = "ARM";
                    break;
                default:
                    newArchitecture = architecture;
                    break;
            }

            MutableAgentField deviceCpu = userAgent.get(DEVICE_CPU);
            overrideValue(deviceCpu, newArchitecture);
        }
    }


    public void improveOperatingSystem(MutableUserAgent userAgent, ClientHints clientHints) {
        // Improve the OS info.
        // https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform
        // The Sec-CH-UA-Platform request header field gives a server information about the platform on which
        // a given user agent is executing. It is a Structured Header whose value MUST be a string [RFC8941].
        // Its value SHOULD match one of the following common platform values:
        // - "Android"
        // - "Chrome OS"
        // - "iOS"
        // - "Linux"
        // - "macOS"
        // - "Windows"
        // - "Unknown"
        String platform = clientHints.getPlatform();
        String platformVersion = clientHints.getPlatformVersion();
        if (platform != null && platformVersion != null && !platform.trim().isEmpty() && !platformVersion.trim().isEmpty()) {
//            MutableAgentField osName    = (MutableAgentField) userAgent.get(UserAgent.OPERATING_SYSTEM_NAME);
            String majorVersion = VersionSplitter.getInstance().getSingleSplit(platformVersion, 1);

            switch (platform) {
                case "macOS":
                case "Mac OS X":
                    overrideValue(userAgent.get(DEVICE_BRAND), "Apple");
                    overrideValue(userAgent.get(DEVICE_CLASS), DESKTOP.getValue());
                    overrideValue(userAgent.get(OPERATING_SYSTEM_CLASS), DESKTOP.getValue());
                    overrideValue(userAgent.get(LAYOUT_ENGINE_CLASS), "Browser");
                    overrideValue(userAgent.get(AGENT_CLASS), "Browser");
                    break;

                case "iOS":
                    overrideValue(userAgent.get(DEVICE_CLASS), MOBILE.getValue());
                    overrideValue(userAgent.get(DEVICE_BRAND), "Apple");
                    break;

                case "Android":
                case "Fuchsia":
                    overrideValue(userAgent.get(DEVICE_CLASS), MOBILE.getValue());
                    overrideValue(userAgent.get(DEVICE_BRAND), NULL_VALUE);
                    break;

                case "Linux":
                    overrideValue(userAgent.get(DEVICE_CLASS), DESKTOP.getValue());
                    overrideValue(userAgent.get(DEVICE_NAME), "Linux Desktop");
                    overrideValue(userAgent.get(DEVICE_BRAND), NULL_VALUE);
                    overrideValue(userAgent.get(DEVICE_VERSION), NULL_VALUE);
                    overrideValue(userAgent.get(DEVICE_FIRMWARE_VERSION), NULL_VALUE);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_CLASS), DESKTOP.getValue());
                    overrideValue(userAgent.get(LAYOUT_ENGINE_CLASS), "Browser");
                    overrideValue(userAgent.get(AGENT_CLASS), "Browser");
                    break;

                case "Chrome OS":
                case "Windows":
                    overrideValue(userAgent.get(DEVICE_NAME), DESKTOP.getValue());
                    overrideValue(userAgent.get(DEVICE_CLASS), DESKTOP.getValue());
                    overrideValue(userAgent.get(DEVICE_BRAND), NULL_VALUE);
                    overrideValue(userAgent.get(LAYOUT_ENGINE_CLASS), "Browser");
                    overrideValue(userAgent.get(OPERATING_SYSTEM_CLASS), DESKTOP.getValue());
                    overrideValue(userAgent.get(AGENT_CLASS), "Browser");
                    break;

                case "Unknown":
                default:
                    overrideValue(userAgent.get(DEVICE_CLASS), DESKTOP.getValue());
                    overrideValue(userAgent.get(DEVICE_BRAND), NULL_VALUE);
                    overrideValue(userAgent.get(LAYOUT_ENGINE_CLASS), "Browser");
                    overrideValue(userAgent.get(OPERATING_SYSTEM_CLASS), DESKTOP.getValue());
                    overrideValue(userAgent.get(AGENT_CLASS), "Browser");
                    break;
            }

            switch (platform) {
                case "macOS":
                case "Mac OS X":
                    platform = "Mac OS";
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME),               platform);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION),            platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION_MAJOR),      majorVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION),       platform + " " + platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION_MAJOR), platform + " " + majorVersion);
                    break;

                case "Android":
                case "Fuchsia":
                case "Chrome OS":
                case "iOS":
                case "Linux":
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME),               platform);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION),            platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION_MAJOR),      majorVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION),       platform + " " + platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION_MAJOR), platform + " " + majorVersion);
                    break;

                case "Windows":
                    OSFields betterOsVersion = WINDOWS_VERSION_MAPPING.getLongestMatch(platformVersion);
                    if (betterOsVersion != null) {
                        overrideValue(userAgent.get(DEVICE_CLASS),                        DESKTOP.getValue());
                        overrideValue(userAgent.get(DEVICE_BRAND),                        NULL_VALUE);
                        overrideValue(userAgent.get(OPERATING_SYSTEM_NAME),               betterOsVersion.getName());
                        overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION),            betterOsVersion.getVersion());
                        overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION_MAJOR),      betterOsVersion.getVersionMajor());
                        overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION),       betterOsVersion.getNameVersion());
                        overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION_MAJOR), betterOsVersion.getNameVersionMajor());
                    }
                    break;

                case "Unknown":
                default:
                    platform = userAgent.getValue(OPERATING_SYSTEM_NAME);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION), platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION_MAJOR), majorVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION), platform + " " + platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION_MAJOR), platform + " " + majorVersion);
                    break;
            }
        }
    }

    private boolean newVersionIsBetter(MutableAgentField currentVersion, String version) {
        boolean currentVersionHasMinor = currentVersion.getValue().indexOf('.') >= 0;
        boolean versionHasMinor = version.indexOf('.') >= 0;
        return currentVersion.isDefaultValue() ||
            (!versionHasMinor && !currentVersionHasMinor) ||
            (versionHasMinor);
    }

    private static final Pattern DOT_SPLITTER = Pattern.compile("\\.");

    // There are some well known browsers that include their "Parent" browser also.
    // We remove those parents to allow better reporting
    private static final Map<String, String> BROWSER_ANCESTORS = new TreeMap<>();

    static {
        // <Wanted> --> <Unwanted Ancestor>
        BROWSER_ANCESTORS.put("Chrome", "Chromium");
        BROWSER_ANCESTORS.put("OperaMobile", "Opera");
    }

    // There are some browsers where we want to map the name to a more readable version
    private static final Map<String, String> BROWSER_RENAME = new TreeMap<>();

    static {
        BROWSER_RENAME.put("OperaMobile", "Opera Mobile");
        BROWSER_RENAME.put("Microsoft Edge", "Edge");
    }

    public void improveLayoutEngineAndAgentInfo(MutableUserAgent userAgent, ClientHints clientHints) {
        // Improve the Agent info.
        List<Brand> versionList = clientHints.getFullVersionList();
        if (versionList == null) {
            versionList = clientHints.getBrands();
        }

        if (versionList == null) {
            return; // Nothing to do
        }

        final Map<String, Brand> versionMap = new TreeMap<>();
        versionList.forEach(v -> versionMap.put(v.getName(), v));

        BROWSER_ANCESTORS.forEach((wanted, unwanted) -> {
            if (versionMap.containsKey(wanted)) {
                versionMap.remove(unwanted);
            }
        });

        // ========================
        Brand chromium = versionMap.get("Chromium");
        if (chromium != null) {
            String version = chromium.getVersion();
            String[] versionSplits = DOT_SPLITTER.split(version);
            String majorVersion = versionSplits[0];

            // Work around the major in minor hack/feature of Chrome ~v99
            if (versionSplits.length==4 && !"0".equals(versionSplits[1])) {
                version = versionSplits[1] + ".0." + versionSplits[2] + '.' + versionSplits[3];
                majorVersion = versionSplits[1];
            }

            // ==== Blink ?
            MutableAgentField engineName = userAgent.get(LAYOUT_ENGINE_NAME);
            if (engineName.isDefaultValue() || !"Blink".equals(engineName.getValue())) {
                overrideValue(engineName, "Blink");
            }
            MutableAgentField engineVersion = userAgent.get(LAYOUT_ENGINE_VERSION);
            MutableAgentField engineMajorVersion = userAgent.get(LAYOUT_ENGINE_VERSION_MAJOR);
            String blinkVersion = majorVersion;
            if (versionSplits.length>1) {
                blinkVersion = majorVersion + ".0";
            }
            if (newVersionIsBetter(engineVersion, blinkVersion)) {
                overrideValue(engineVersion, blinkVersion);
                overrideValue(engineMajorVersion, majorVersion);
            }

            overrideValue(userAgent.get(LAYOUT_ENGINE_NAME_VERSION), engineName.getValue() + " " + engineVersion.getValue());
            overrideValue(userAgent.get(LAYOUT_ENGINE_NAME_VERSION_MAJOR), engineName.getValue() + " " + engineMajorVersion.getValue());

            // ===== Chromium browser?
            if (versionList.size() == 1) { // NOTE: The grease was filtered out !
                // So we have "Chromium" and not "Chrome" or "Edge" or something else
                MutableAgentField currentVersion = userAgent.get(AGENT_VERSION);
                if (newVersionIsBetter(currentVersion, version)) {
                    overrideValue(userAgent.get(AGENT_NAME), "Chromium");
                    overrideValue(userAgent.get(AGENT_VERSION), version);
                    overrideValue(userAgent.get(AGENT_NAME_VERSION), "Chromium " + version);
                    overrideValue(userAgent.get(AGENT_VERSION_MAJOR), majorVersion);
                    overrideValue(userAgent.get(AGENT_NAME_VERSION_MAJOR), "Chromium " + majorVersion);
                } else {
                    // We ONLY update the name of the agent to Chromium in some cases
                    if ("Chrome".equals(userAgent.getValue(AGENT_NAME))) {
                        overrideValue(userAgent.get(AGENT_NAME), "Chromium");
                        overrideValue(userAgent.get(AGENT_NAME_VERSION), "Chromium " + currentVersion.getValue());

                        String currentMajorVersion = userAgent.getValue(AGENT_VERSION_MAJOR);
                        overrideValue(userAgent.get(AGENT_NAME_VERSION_MAJOR), "Chromium " + currentMajorVersion);
                    }
                }

                return;
            }
            versionMap.remove("Chromium");
        }

        // ========================
        Brand chrome = versionMap.get("Chrome");
        if (chrome == null) {
            chrome = versionMap.get("Google Chrome");
        }
        if (chrome != null) {
            if (versionMap.size() == 1) {
                // So we have "Chrome" and nothing else
                MutableAgentField currentVersion = userAgent.get(AGENT_VERSION);
                String version = chrome.getVersion();
                String[] versionSplits = DOT_SPLITTER.split(version);
                String majorVersion = versionSplits[0];

                // Work around the major in minor hack/feature of Chrome ~v99
                // 99.100.x.y is really 100.0.x.y
                if (versionSplits.length==4 && !"0".equals(versionSplits[1])) {
                    version = versionSplits[1] + ".0." + versionSplits[2] + '.' + versionSplits[3];
                    majorVersion = versionSplits[1];
                }

                if (newVersionIsBetter(currentVersion, version)) {
                    overrideValue(userAgent.get(AGENT_NAME), "Chrome");
                    overrideValue(currentVersion, version);
                    overrideValue(userAgent.get(AGENT_NAME_VERSION), "Chrome " + version);
                    overrideValue(userAgent.get(AGENT_VERSION_MAJOR), majorVersion);
                    overrideValue(userAgent.get(AGENT_NAME_VERSION_MAJOR), "Chrome " + majorVersion);
                    return;
                }
            }
            versionMap.remove("Chrome");
            versionMap.remove("Google Chrome");
        }
        // ========================

        // There was a bug in Opera which puts the wrong version in the client hints.
        // This has been fixed in the first release of Opera 98
        // https://blogs.opera.com/desktop/changelog-for-98/
        // So up until Opera 97 / Opera Mobile the version information was buggy.
        MutableAgentField currentAgent = userAgent.get(AGENT_NAME);
        if ("Opera".equals(currentAgent.getValue())) {
            String currentVersion = userAgent.get(AGENT_VERSION).getValue();
            // We only consider the Client Hints if the version new enough.
            String currentMajorVersion = DOT_SPLITTER.split(currentVersion, 2)[0];
            try {
                int currentMajorVersionInt = Integer.parseInt(currentMajorVersion);
                if (currentMajorVersionInt < 98) {
                    versionMap.remove("Opera");
                }
            } catch (NumberFormatException nfe) {
                // Safety net; Ignore this problem if it happens.
            }
        }

        // If anything remains then (we think) THAT is the truth...
        Map<String, Brand> sortedBrands = new TreeMap<>();

        // If we have more than 1 left we sort them by how detailed the version info is.
        int originalPosition = 0;
        for (Brand brand : versionMap.values()) {
            originalPosition++;
            String version = brand.getVersion();
            boolean versionFieldValueHasDot = version.indexOf('.') >= 0;
            boolean versionIsMajorVersionOnly = !versionFieldValueHasDot;
            if (versionFieldValueHasDot) {
                versionIsMajorVersionOnly = version.endsWith(".0.0.0");
            }
            // The 0 (with detailed version) is sorted before the 1 (only major version) in the TreeMap (which sorts by key)
            sortedBrands.put((versionIsMajorVersionOnly ?"1":"0") + "_"+ originalPosition + "_" + brand.getName(), brand);
        }

        MutableAgentField agentNameField = userAgent.get(AGENT_NAME);
        MutableAgentField agentVersionField = userAgent.get(AGENT_VERSION);
        String versionFieldValue = agentVersionField.getValue();

        boolean versionFieldValueHasDot = versionFieldValue.indexOf('.') >= 0;
        boolean versionIsMajorVersionOnly = !versionFieldValueHasDot;
        if (versionFieldValueHasDot) {
            versionIsMajorVersionOnly = versionFieldValue.endsWith(".0.0.0");
        }

        // We just pick the first one that remains.
        Optional<Map.Entry<String, Brand>> firstBrand = sortedBrands.entrySet().stream().findFirst();
        if (!firstBrand.isPresent()) {
            return;
        }

        Map.Entry<String, Brand> brandEntry = firstBrand.get();
        Brand brand = brandEntry.getValue();
        String agentName = brand.getName();

        // Sanitize the common yet unwanted names
        String renamed = BROWSER_RENAME.get(agentName);
        if (renamed != null) {
            agentName = renamed;
        }

        // We only update the version if we have a better version number.
        // We always update the AgentName because I think the Client hints are always "better"...
        String newVersion = brand.getVersion();
        String newMajorVersion = DOT_SPLITTER.split(newVersion, 2)[0];

        // The values we are going to set at the end.
        String setVersion = versionFieldValue;
        String setMajorVersion = DOT_SPLITTER.split(versionFieldValue, 2)[0];

        // If the original is a major version only then the new one is better
        if (versionIsMajorVersionOnly) {
            setVersion = newVersion;
            setMajorVersion = newMajorVersion;
        } else {
            // If current version is a full version but there is a mismatch in the major we pick the
            // ClientHints version anyway.
            if (!setMajorVersion.equals(newMajorVersion)) {
                setVersion = newVersion;
                setMajorVersion = newMajorVersion;
            }
        }

        overrideValue(agentNameField, agentName);
        overrideValue(userAgent.get(AGENT_VERSION), setVersion);
        overrideValue(userAgent.get(AGENT_NAME_VERSION), agentName + " " + setVersion);
        overrideValue(userAgent.get(AGENT_VERSION_MAJOR), setMajorVersion);
        overrideValue(userAgent.get(AGENT_NAME_VERSION_MAJOR), agentName + " " + setMajorVersion);
    }

    private static Set<String> setOfStrings(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    // In the above calculations there are fields that require additional input fields.
    private static final Map<String, Set<String>> EXTRA_FIELD_DEPENDENCIES = new TreeMap<>();
    static {
        EXTRA_FIELD_DEPENDENCIES.put(AGENT_CLASS,              setOfStrings(AGENT_NAME, AGENT_VERSION));
        EXTRA_FIELD_DEPENDENCIES.put(AGENT_NAME,               setOfStrings(AGENT_VERSION));
        EXTRA_FIELD_DEPENDENCIES.put(AGENT_VERSION,            setOfStrings(AGENT_NAME));
        EXTRA_FIELD_DEPENDENCIES.put(AGENT_NAME_VERSION,       setOfStrings(AGENT_NAME));
        EXTRA_FIELD_DEPENDENCIES.put(AGENT_VERSION_MAJOR,      setOfStrings(AGENT_NAME));
        EXTRA_FIELD_DEPENDENCIES.put(AGENT_NAME_VERSION_MAJOR, setOfStrings(AGENT_NAME));
    }

    public static Set<String> extraDependenciesNeededByClientCalculator(@Nonnull Set<String> wantedFieldNames) {
        HashSet<String> result = new HashSet<>();

        for (String wantedFieldName : wantedFieldNames) {
            Set<String> extraDependencies = EXTRA_FIELD_DEPENDENCIES.get(wantedFieldName);
            if (extraDependencies != null) {
                result.addAll(extraDependencies);
            }
        }
        return result;
    }

    private void overrideValue(MutableAgentField field, String newValue) {
        field.setValue(newValue, field.getConfidence()+1);
    }

}
