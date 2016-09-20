/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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
}
