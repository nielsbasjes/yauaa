/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.utils.Normalize;
import org.apache.hc.client5.http.psl.PublicSuffixMatcherLoader;

import java.util.HashSet;
import java.util.Set;

import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_EMAIL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_URL;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.utils.HostnameExtracter.extractHostname;
import static org.apache.hc.client5.http.psl.DomainType.ICANN;

public class CalculateDeviceBrand implements FieldCalculator {

    private final Set<String> unwantedUrlBrands;
    private final Set<String> unwantedEmailBrands;

    public CalculateDeviceBrand() {
        unwantedUrlBrands = new HashSet<>();
        unwantedUrlBrands.add("Localhost");
        unwantedUrlBrands.add("Github");
        unwantedUrlBrands.add("Gitlab");

        unwantedEmailBrands = new HashSet<>();
        unwantedEmailBrands.add("Localhost");
        unwantedEmailBrands.add("Gmail");
        unwantedEmailBrands.add("Outlook");
    }

    @Override
    public void calculate(MutableUserAgent userAgent) {
        // The device brand field is a mess.
        AgentField deviceBrand = userAgent.get(DEVICE_BRAND);
        if (deviceBrand.isDefaultValue()) {
            // If no brand is known then try to extract something that looks like a Brand from things like URL and Email addresses.
            String newDeviceBrand = determineDeviceBrand(userAgent);
            if (newDeviceBrand != null) {
                userAgent.setForced(
                    DEVICE_BRAND,
                    newDeviceBrand,
                    0);
            } else {
                userAgent.setForced(
                    DEVICE_BRAND,
                    NULL_VALUE,
                    0);
            }
        } else {
            userAgent.setForced(
                DEVICE_BRAND,
                Normalize.brand(deviceBrand.getValue()),
                deviceBrand.getConfidence());
        }
    }

    private String determineDeviceBrand(UserAgent userAgent) {
        // If no brand is known but we do have a URL then we assume the hostname to be the brand.
        // We put this AFTER the creation of the DeviceName because we choose to not have
        // this brandname in the DeviceName.

        String deviceBrand = null;

        AgentField informationUrl = userAgent.get(AGENT_INFORMATION_URL);
        if (!informationUrl.isDefaultValue()) {
            String hostname = extractHostname(informationUrl.getValue());
            deviceBrand = extractCompanyFromHostName(hostname, unwantedUrlBrands);
        }

        if (deviceBrand != null) {
            return deviceBrand;
        }

        AgentField informationEmail = userAgent.get(AGENT_INFORMATION_EMAIL);
        if (!informationEmail.isDefaultValue()) {
            String hostname = informationEmail.getValue();
            int    atOffset = hostname.indexOf('@');
            if (atOffset >= 0) {
                hostname = hostname.substring(atOffset + 1);
            }
            deviceBrand = extractCompanyFromHostName(hostname, unwantedEmailBrands);
        }

        return deviceBrand;
    }

    private String extractCompanyFromHostName(String hostname, Set<String> blackList) {
        String root = PublicSuffixMatcherLoader.getDefault().getDomainRoot(hostname, ICANN);

        if (root == null) {
            return null;
        }
        String brand = Normalize.brand(root.split("\\.", 2)[0]);
        if (blackList.contains(brand)) {
            return null;
        }
        return brand;
    }

    @Override
    public String[] getDependencies() {
        return new String[]{AGENT_INFORMATION_URL, AGENT_INFORMATION_EMAIL};
    }

    @Override
    public String toString() {
        return "Calculate " + DEVICE_BRAND;
    }

}
