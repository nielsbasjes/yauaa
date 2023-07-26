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

package nl.basjes.parse.useragent.calculate;

import nl.basjes.collections.PrefixMap;
import nl.basjes.collections.prefixmap.StringPrefixMap;
import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.config.AnalyzerConfigHolder;
import nl.basjes.parse.useragent.utils.Normalize;
import nl.basjes.parse.useragent.utils.WordSplitter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_EMAIL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_URL;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VALUE;
import static nl.basjes.parse.useragent.utils.HostnameExtracter.extractBrandFromEmail;
import static nl.basjes.parse.useragent.utils.HostnameExtracter.extractBrandFromUrl;

public class CalculateDeviceBrand extends FieldCalculator {

//    private final PrefixMap<String> mobileBrands = new StringPrefixMap<>(false);
    private final PrefixMap<String> mobileBrandPrefixes = new StringPrefixMap<>(false);

    public CalculateDeviceBrand() {

    }

    public CalculateDeviceBrand(AnalyzerConfigHolder analyzerConfig) {
        Map<String, String> mobileBrandsLookup = analyzerConfig.getLookups().get("MobileBrands");
        if (mobileBrandsLookup != null) {
            mobileBrandPrefixes.putAll(mobileBrandsLookup);
        }

        Map<String, String> mobileBrandPrefixesLookup = analyzerConfig.getLookups().get("MobileBrandPrefixes");
        if (mobileBrandPrefixesLookup != null) {
            mobileBrandPrefixes.putAll(mobileBrandPrefixesLookup);
        }
    }

    @Override
    public void calculate(MutableUserAgent userAgent) {
        // The device brand field is a mess.
        AgentField deviceBrand = userAgent.get(DEVICE_BRAND);

        if (deviceBrand.isDefaultValue()) {
            // If no brand is known then first try to extract it from the raw deviceName.
            AgentField deviceName = userAgent.get(DEVICE_NAME);

            String newDeviceBrand = mobileBrandPrefixes.getLongestMatch(deviceName.getValue());
            if (newDeviceBrand != null) {
                userAgent.setForced(
                    DEVICE_BRAND,
                    newDeviceBrand,
                    0);
                return;
            }

            // If no brand is known then try to extract something that looks like a Brand from things like URL and Email addresses.
            newDeviceBrand = determineDeviceBrand(userAgent);
            userAgent.setForced(
                DEVICE_BRAND,
                newDeviceBrand == null ? NULL_VALUE : newDeviceBrand,
                0);
        } else {
            userAgent.setForced(
                DEVICE_BRAND,
                Normalize.brand(deviceBrand.getValue()),
                deviceBrand.getConfidence());
        }
    }

    /**
     * Some device names are "Generic" so we ignore them when trying to extract a Brand.
     */
    private static final List<String> GENERIC_DEVICE_NAMES = Arrays.asList(
        UNKNOWN_VALUE,
        "android mobile",
        "fuchsia mobile",
        "fuchsia device",
        "ios device",
        "linux desktop",
        "desktop",
        "laptop",
        "server",
        "phone",
        "tv",
        "imitator",
        "bot",
        "tablet",
        "mobile",
        "device",
        "generic",
        "windows",
        "linux",
        "android",
        "ios",
        "fuchsia"
    );


    private String determineDeviceBrand(UserAgent userAgent) {
        // If no brand is known but we do have a URL then we assume the hostname to be the brand.
        // We put this AFTER the creation of the DeviceName because we choose to not have
        // this brandname in the DeviceName.

        String deviceBrand = null;

        AgentField informationUrl = userAgent.get(AGENT_INFORMATION_URL);
        if (!informationUrl.isDefaultValue()) {
            deviceBrand = extractBrandFromUrl(informationUrl.getValue());
        }

        if (deviceBrand != null) {
            return deviceBrand;
        }

        AgentField informationEmail = userAgent.get(AGENT_INFORMATION_EMAIL);
        if (!informationEmail.isDefaultValue()) {
            deviceBrand = extractBrandFromEmail(informationEmail.getValue());
        }

        if (deviceBrand != null) {
            return deviceBrand;
        }

        AgentField deviceName = userAgent.get(DEVICE_NAME);
        if (!deviceName.isDefaultValue()) {
            if (!GENERIC_DEVICE_NAMES.contains(deviceName.getValue())) {
                // Only try the non generic names.
                List<Pair<Integer, Integer>> splitList = WordSplitter.getInstance().createSplitList(deviceName.getValue());
                if (splitList.size() > 1) {
                    // If one of the parts is too generic we skip it.
                    List<String> splits = WordSplitter.getInstance().getSplits(deviceName.getValue(), splitList, 1, 5);
                    boolean update = true;
                    for (String split : splits) {
                        if (GENERIC_DEVICE_NAMES.contains(split.toLowerCase(Locale.ROOT))) {
                            update = false;
                            break;
                        }
                    }
                    if (update) {
                        // If we have at least 2 parts we assume the first one is the Brand name
                        deviceBrand = Normalize.brand(splits.get(0));
                    }
                }
            }
        }

        return deviceBrand;
    }

    @Override
    public String getCalculatedFieldName() {
        return DEVICE_BRAND;
    }

    @Override
    public Set<String> getDependencies() {
        // NOTE: We NEED the DeviceName also but that would create a circular dependency.
        return new HashSet<>(Arrays.asList(AGENT_INFORMATION_URL, AGENT_INFORMATION_EMAIL));
    }
}
