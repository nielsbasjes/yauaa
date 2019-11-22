package nl.basjes.parse.useragent.utils;

import java.io.Serializable;
import java.net.URI;

public class HostnameExtracter implements Serializable {

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

}
