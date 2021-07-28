/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_EMAIL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_URL;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.utils.HostnameExtracter.extractBrandFromEmail;
import static nl.basjes.parse.useragent.utils.HostnameExtracter.extractBrandFromUrl;

public class CalculateDeviceBrand extends FieldCalculator {

    @Override
    public void calculate(MutableUserAgent userAgent) {
        // The device brand field is a mess.
        AgentField deviceBrand = userAgent.get(DEVICE_BRAND);
        if (deviceBrand.isDefaultValue()) {
            // If no brand is known then try to extract something that looks like a Brand from things like URL and Email addresses.
            String newDeviceBrand = determineDeviceBrand(userAgent);
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

        return deviceBrand;
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
