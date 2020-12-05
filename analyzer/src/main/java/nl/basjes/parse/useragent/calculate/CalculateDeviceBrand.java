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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_EMAIL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_URL;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.utils.HostnameExtracter.extractHostname;
import static org.apache.hc.client5.http.psl.DomainType.ICANN;

public class CalculateDeviceBrand extends FieldCalculator {

    private final Set<String> unwantedUrlBrands;
    private final Set<String> unwantedEmailBrands;

    public CalculateDeviceBrand() {
        unwantedUrlBrands = new HashSet<>();
        // Localhost ...
        unwantedUrlBrands.add("localhost");
        // Software repositories
        unwantedUrlBrands.add("github.com");
        unwantedUrlBrands.add("gitlab.com");
        // Url shortners
        unwantedUrlBrands.add("bit.ly");

        unwantedEmailBrands = new HashSet<>();
        unwantedEmailBrands.add("localhost");
        unwantedEmailBrands.add("gmail.com");
        unwantedEmailBrands.add("outlook.com");
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

            if (deviceBrand == null) {
                // Perhaps this is software repository
                deviceBrand = extractCompanyFromSoftwareRepositoryUrl(informationUrl.getValue());
            }

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

    private static final class SitePathExtract {
        final String prefix;
        final int    prefixLength;
        final int    brandSegment;

        SitePathExtract(String prefix, int brandSegment) {
            this.prefix = prefix;
            this.prefixLength = prefix.length();
            this.brandSegment = brandSegment;
        }
    }

    private static final List<SitePathExtract> SITE_PATH_EXTRACTS = Arrays.asList(
        // The 0 is array index 0 AFTER the prefix !!!
        new SitePathExtract("https://github.com/",                   0),
        new SitePathExtract("https://gitlab.com/",                   0),
        new SitePathExtract("https://sourceforge.net/projects/",    0)
    );

    private String extractCompanyFromSoftwareRepositoryUrl(String url) {
        for (SitePathExtract sitePathExtract : SITE_PATH_EXTRACTS) {
            if (url.startsWith(sitePathExtract.prefix)) {
                String path = url.substring(sitePathExtract.prefixLength);
                String[] splits = path.split("/");
                if (splits.length == 0 || splits.length < sitePathExtract.brandSegment){
                    return null;
                }
                String brand = splits[sitePathExtract.brandSegment];
                if (brand.isEmpty()) {
                    return null;
                }
                return Normalize.brand(brand);
            }
        }
        return null;
    }

    private String extractCompanyFromHostName(String hostname, Set<String> blackList) {
        if (blackList.contains(hostname)) {
            return null;
        }
        String root = PublicSuffixMatcherLoader.getDefault().getDomainRoot(hostname, ICANN);

        if (root == null) {
            return null;
        }
        return Normalize.brand(root.split("\\.", 2)[0]);
    }

    @Override
    public String getCalculatedFieldName() {
        return DEVICE_BRAND;
    }

    @Override
    public Set<String> getDependencies() {
        return new HashSet<>(Arrays.asList(AGENT_INFORMATION_URL, AGENT_INFORMATION_EMAIL));
    }
}
