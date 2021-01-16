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

package nl.basjes.parse.useragent.utils;

import org.apache.hc.client5.http.psl.PublicSuffixMatcherLoader;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.apache.hc.client5.http.psl.DomainType.ICANN;

public final class HostnameExtracter implements Serializable {

    private static final Set<String> UNWANTED_URL_BRANDS;
    private static final Set<String> UNWANTED_EMAIL_BRANDS;

    static {
        UNWANTED_URL_BRANDS = new HashSet<>();
        // Localhost ...
        UNWANTED_URL_BRANDS.add("localhost");
        // Software repositories
        UNWANTED_URL_BRANDS.add("github.com");
        UNWANTED_URL_BRANDS.add("gitlab.com");
        // Url shortners
        UNWANTED_URL_BRANDS.add("bit.ly");
        // Hosting sites
        UNWANTED_URL_BRANDS.add("wordpress.com");

        UNWANTED_EMAIL_BRANDS = new HashSet<>();
        UNWANTED_EMAIL_BRANDS.add("localhost");
        UNWANTED_EMAIL_BRANDS.add("gmail.com");
        UNWANTED_EMAIL_BRANDS.add("outlook.com");
    }

    private HostnameExtracter() {
        // Nothing
    }

    public static String extractHostname(String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return null; // Nothing to do here
        }

        int firstQuestionMark = uriString.indexOf('?');
        int firstAmpersand = uriString.indexOf('&');
        int cutIndex = -1;
        if (firstAmpersand != -1) {
            if (firstQuestionMark != -1) {
                cutIndex = firstQuestionMark;
            } else {
                cutIndex = firstAmpersand;
            }
        } else {
            if (firstQuestionMark != -1) {
                cutIndex = firstQuestionMark;
            }
        }
        if (cutIndex != -1) {
            uriString = uriString.substring(0, cutIndex);
        }

        URI     uri;
        try {
            if (uriString.charAt(0) == '/') {
                if (uriString.charAt(1) == '/') {
                    uri = URI.create(uriString);
                } else {
                    // So no hostname
                    return null;
                }
            } else {
                if (uriString.contains(":")) {
                    uri = URI.create(uriString);
                } else {
                    if (uriString.contains("/")) {
                        return uriString.split("/", 2)[0];
                    } else {
                        return uriString;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            return null;
        }

        return uri.getHost();
    }


    public static String extractBrandFromUrl(String url) {
        String hostname = extractHostname(url);
        String brand = extractCompanyFromHostName(hostname, UNWANTED_URL_BRANDS);

        if (brand == null) {
            // Perhaps this is software repository
            brand = extractCompanyFromSoftwareRepositoryUrl(url);
        }
        return brand;
    }

    public static String extractBrandFromEmail(String email) {
        String hostname = email;
        int    atOffset = hostname.indexOf('@');
        if (atOffset >= 0) {
            hostname = hostname.substring(atOffset + 1);
        }
        return extractCompanyFromHostName(hostname, UNWANTED_EMAIL_BRANDS);
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

    private static String extractCompanyFromSoftwareRepositoryUrl(String url) {
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

    private static String extractCompanyFromHostName(String hostname, Set<String> blackList) {
        if (hostname == null) {
            return null;
        }
        hostname = hostname.toLowerCase(Locale.ENGLISH);
        if (blackList.contains(hostname)) {
            return null;
        }
        String root = PublicSuffixMatcherLoader.getDefault().getDomainRoot(hostname, ICANN);

        if (root == null) {
            return null;
        }
        return Normalize.brand(root.split("\\.", 2)[0]);
    }

}
