/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.utils.Normalize;
import nl.basjes.parse.useragent.utils.publicsuffix.PublicSuffixMatcher;
import nl.basjes.parse.useragent.utils.publicsuffix.PublicSuffixMatcherLoader;

import java.util.HashSet;
import java.util.Set;

import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_EMAIL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_URL;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.utils.HostnameExtracter.extractHostname;
import static org.apache.http.conn.util.DomainType.ICANN;

public class CalculateDeviceBrand implements FieldCalculator {

    private final Set<String> unwantedUrlBrands;
    private final Set<String> unwantedEmailBrands;

    private transient PublicSuffixMatcher publicSuffixMatcher;

    public CalculateDeviceBrand() {
        unwantedUrlBrands = new HashSet<>();
        unwantedUrlBrands.add("Localhost");
        unwantedUrlBrands.add("Github");
        unwantedUrlBrands.add("Gitlab");

        unwantedEmailBrands = new HashSet<>();
        unwantedEmailBrands.add("Localhost");
        unwantedEmailBrands.add("Gmail");
        unwantedEmailBrands.add("Outlook");
        publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
    }

    @Override
    public void calculate(UserAgent userAgent) {
        // The device brand field is a mess.
        UserAgent.AgentField deviceBrand = userAgent.get(DEVICE_BRAND);
        if (!deviceBrand.isDefaultValue()) {
            userAgent.setForced(
                DEVICE_BRAND,
                Normalize.brand(deviceBrand.getValue()),
                deviceBrand.getConfidence());
        }

        if (deviceBrand.isDefaultValue()) {
            // If no brand is known then try to extract something that looks like a Brand from things like URL and Email addresses.
            String newDeviceBrand = determineDeviceBrand(userAgent);
            if (newDeviceBrand != null) {
                userAgent.setForced(
                    DEVICE_BRAND,
                    newDeviceBrand,
                    1);
            }
        }

    }

    private String determineDeviceBrand(UserAgent userAgent) {
        // If no brand is known but we do have a URL then we assume the hostname to be the brand.
        // We put this AFTER the creation of the DeviceName because we choose to not have
        // this brandname in the DeviceName.

        String deviceBrand = null;

        UserAgent.AgentField informationUrl = userAgent.get(AGENT_INFORMATION_URL);
        if (informationUrl != null && informationUrl.getConfidence() >= 0) {
            String hostname = extractHostname(informationUrl.getValue());
            deviceBrand = extractCompanyFromHostName(hostname, unwantedUrlBrands);
        }

        if (deviceBrand != null) {
            return deviceBrand;
        }

        UserAgent.AgentField informationEmail = userAgent.get(AGENT_INFORMATION_EMAIL);
        if (informationEmail != null && informationEmail.getConfidence() >= 0) {
            String hostname = informationEmail.getValue();
            int atOffset = hostname.indexOf('@');
            if (atOffset >= 0) {
                hostname = hostname.substring(atOffset+1);
            }
            deviceBrand = extractCompanyFromHostName(hostname, unwantedEmailBrands);
        }

        return deviceBrand;
    }

    private String extractCompanyFromHostName(String hostname, Set<String> blackList) {
        if (hostname == null) {
            return null;
        }
        if (publicSuffixMatcher == null) { // Because it is not Serializable --> transient
            publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
        }

        try {
            String root = publicSuffixMatcher.getDomainRoot(hostname, ICANN);
            if (root == null){
                return null;
            }
            String brand = Normalize.brand(root.split("\\.", 2)[0]);
            if (blackList.contains(brand)) {
                return null;
            }
            return brand;
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Calculate " + DEVICE_BRAND;
    }

}
