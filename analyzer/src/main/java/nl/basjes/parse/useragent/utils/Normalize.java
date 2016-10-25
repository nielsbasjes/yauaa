/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.utils;

import java.util.Locale;

public final class Normalize {
    private Normalize() {
    }

    public static String brand(String brand) {
        if (brand.length() <= 3) {
            return brand.toUpperCase(Locale.ENGLISH);
        }

        StringBuilder sb = new StringBuilder(brand.length());
        char[] brandChars = brand.toCharArray();

        int lowerChars = 0;
        sb.append(Character.toUpperCase(brandChars[0]));
        for (int i = 1; i < brandChars.length; i++) {
            char thisChar = brandChars[i];

            boolean isUpperCase = Character.isUpperCase(thisChar);
            if (isUpperCase) {
                if (lowerChars >= 3) {
                    sb.append(thisChar);
                } else {
                    sb.append(Character.toLowerCase(thisChar));
                }
                lowerChars = 0;
            } else {
                sb.append(Character.toLowerCase(thisChar));
                lowerChars++;
            }
        }
        return sb.toString();
    }

    public static String email(String email) {
        String cleaned = email;
        cleaned = cleaned.replaceAll("\\[at\\]", "@");

        cleaned = cleaned.replaceAll("\\[\\\\xc3\\\\xa07\\]", "@");
        cleaned = cleaned.replaceAll("\\[dot\\]", ".");
        cleaned = cleaned.replaceAll("\\\\", " ");
        cleaned = cleaned.replaceAll(" at ", "@");
        cleaned = cleaned.replaceAll("dot", ".");
        cleaned = cleaned.replaceAll(" dash ", "-");
        cleaned = cleaned.replaceAll(" ", "");
        return cleaned;
    }

}
