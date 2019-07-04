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

import com.google.common.net.InternetDomainName;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.utils.Normalize;

import java.net.MalformedURLException;
import java.net.URL;

import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_EMAIL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_URL;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;

public class CalculateDeviceBrand implements FieldCalculator {
    @Override
    public void calculate(UserAgent userAgent) {
        // The device brand field is a mess.
        UserAgent.AgentField deviceBrand = userAgent.get(DEVICE_BRAND);
        if (deviceBrand.getConfidence() >= 0) {
            userAgent.setForced(
                DEVICE_BRAND,
                Normalize.brand(deviceBrand.getValue()),
                deviceBrand.getConfidence());
        }

        if (deviceBrand.getConfidence() < 0) {
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

        UserAgent.AgentField informationUrl = userAgent.get(AGENT_INFORMATION_URL);
        if (informationUrl != null && informationUrl.getConfidence() >= 0) {
            String hostname = informationUrl.getValue();
            try {
                URL url = new URL(hostname);
                hostname = url.getHost();
            } catch (MalformedURLException e) {
                // Ignore any exception and continue.
            }
            hostname = extractCompanyFromHostName(hostname);
            if (hostname != null) {
                return hostname;
            }
        }

        UserAgent.AgentField informationEmail = userAgent.get(AGENT_INFORMATION_EMAIL);
        if (informationEmail != null && informationEmail.getConfidence() >= 0) {
            String hostname = informationEmail.getValue();
            int atOffset = hostname.indexOf('@');
            if (atOffset >= 0) {
                hostname = hostname.substring(atOffset+1);
            }
            hostname = extractCompanyFromHostName(hostname);
            if (hostname != null) {
                return hostname;
            }
        }

        return null;
    }


    private String extractCompanyFromHostName(String hostname) {
        try {
            InternetDomainName domainName = InternetDomainName.from(hostname);
            return Normalize.brand(domainName.topPrivateDomain().parts().get(0));
        } catch (RuntimeException e) {
            return null;
        }
    }
}
