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

package nl.basjes.parse.useragent.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Normalize {
    private Normalize() {}

    private static boolean isTokenSeparator(char letter) {
        switch (letter) {
            case ' ':
            case '-':
            case '_':
            case '/':
                return true;
            default:
                return false;
        }
    }

    public static String brand(String brand) {
        if (brand.length() <= 3) {
            return brand.toUpperCase(Locale.ENGLISH);
        }

        StringBuilder sb = new StringBuilder(brand.length());
        char[] nameChars = brand.toCharArray();

        StringBuilder wordBuilder = new StringBuilder(brand.length());

        int lowerChars = 0;
        boolean wordHasNumbers = false;
        for (int i = 0; i < nameChars.length; i++) {
            char thisChar = nameChars[i];
            if (Character.isDigit(thisChar)) {
                wordHasNumbers = true;
            }

            if (isTokenSeparator(thisChar)) {
                if (wordBuilder.length() <= 3 || wordHasNumbers) {
                    sb.append(wordBuilder.toString().toUpperCase(Locale.ENGLISH));
                } else {
                    sb.append(wordBuilder);
                }
                wordBuilder.setLength(0);
                lowerChars = 0; // Next word
                wordHasNumbers = false;
                sb.append(thisChar);
            } else {
                if (wordBuilder.length() == 0) { // First letter of a word
                    wordBuilder.append(Character.toUpperCase(thisChar));
                } else {
                    boolean isUpperCase = Character.isUpperCase(thisChar);

                    if (isUpperCase) {
                        if (lowerChars >= 3) {
                            wordBuilder.append(thisChar);
                        } else {
                            wordBuilder.append(Character.toLowerCase(thisChar));
                        }
                        lowerChars = 0;
                    } else {
                        wordBuilder.append(Character.toLowerCase(thisChar));
                        lowerChars++;
                    }
                }
                // This was the last letter?
                if (i == (nameChars.length-1)) {
                    if (wordBuilder.length() <= 3 || wordHasNumbers) {
                        sb.append(wordBuilder.toString().toUpperCase(Locale.ENGLISH));
                    } else {
                        sb.append(wordBuilder);
                    }
                    wordBuilder.setLength(0);
                    lowerChars = 0; // Next word
                    wordHasNumbers = false;
                }
            }
        }
        return sb.toString().trim();
    }

    public static String cleanupDeviceBrandName(String deviceBrand, String deviceName) {
        String lowerDeviceBrand = deviceBrand.toLowerCase(Locale.ENGLISH);

        deviceName = deviceName.replaceAll("_", " ");
        deviceName = deviceName.replaceAll("- +", "-");
        deviceName = deviceName.replaceAll(" +-", "-");
        deviceName = deviceName.replaceAll(" +", " ");

        String lowerDeviceName = deviceName.toLowerCase(Locale.ENGLISH);

        // In some cases it does start with the brand but without a separator following the brand
        if (lowerDeviceName.startsWith(lowerDeviceBrand)) {
            deviceName = deviceName.replaceAll("_", " ");
            // (?i) means: case insensitive
            deviceName = deviceName.replaceAll("(?i)^" + Pattern.quote(deviceBrand) + "([^ ].*)$", Matcher.quoteReplacement(deviceBrand)+" $1");
            deviceName = deviceName.replaceAll("( -| )+", " ");
        } else {
            deviceName = deviceBrand + ' ' + deviceName;
        }
        String result = Normalize.brand(deviceName);

        if (result.contains("I")) {
            result = result
                .replace("Ipad", "iPad")
                .replace("Ipod", "iPod")
                .replace("Iphone", "iPhone")
                .replace("IOS ", "iOS ");
        }
        return result;
    }

    public static String email(String email) {
        String cleaned = email;
        cleaned = cleaned.replaceAll("\\[at]", "@");

        cleaned = cleaned.replaceAll("\\[\\\\xc3\\\\xa07]", "@");
        cleaned = cleaned.replaceAll("\\[dot]", ".");
        cleaned = cleaned.replaceAll("\\\\", " ");
        cleaned = cleaned.replaceAll(" at ", "@");
        cleaned = cleaned.replaceAll("dot", ".");
        cleaned = cleaned.replaceAll(" dash ", "-");
        cleaned = cleaned.replaceAll(" ", "");
        return cleaned;
    }

}
