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

package nl.basjes.parse.useragent.classify;

import nl.basjes.parse.useragent.UserAgent;

import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.classify.DeviceClass.ANONYMIZED;
import static nl.basjes.parse.useragent.classify.DeviceClass.DESKTOP;
import static nl.basjes.parse.useragent.classify.DeviceClass.E_READER;
import static nl.basjes.parse.useragent.classify.DeviceClass.GAME_CONSOLE;
import static nl.basjes.parse.useragent.classify.DeviceClass.HACKER;
import static nl.basjes.parse.useragent.classify.DeviceClass.HANDHELD_GAME_CONSOLE;
import static nl.basjes.parse.useragent.classify.DeviceClass.MOBILE;
import static nl.basjes.parse.useragent.classify.DeviceClass.PHONE;
import static nl.basjes.parse.useragent.classify.DeviceClass.ROBOT;
import static nl.basjes.parse.useragent.classify.DeviceClass.ROBOT_IMITATOR;
import static nl.basjes.parse.useragent.classify.DeviceClass.ROBOT_MOBILE;
import static nl.basjes.parse.useragent.classify.DeviceClass.SET_TOP_BOX;
import static nl.basjes.parse.useragent.classify.DeviceClass.TABLET;
import static nl.basjes.parse.useragent.classify.DeviceClass.TV;
import static nl.basjes.parse.useragent.classify.DeviceClass.UNCLASSIFIED;
import static nl.basjes.parse.useragent.classify.DeviceClass.UNKNOWN;
import static nl.basjes.parse.useragent.classify.DeviceClass.VIRTUAL_REALITY;
import static nl.basjes.parse.useragent.classify.DeviceClass.WATCH;

public final class UserAgentClassifier {
    private UserAgentClassifier(){} // Utility class

    public static DeviceClass getDeviceClass(UserAgent userAgent) {
        switch (userAgent.getValue(DEVICE_CLASS)) {
            case "Desktop":                return DESKTOP;
            case "Anonymized":             return ANONYMIZED;
            case "Mobile":                 return MOBILE;
            case "Tablet":                 return TABLET;
            case "Phone":                  return PHONE;
            case "Watch":                  return WATCH;
            case "Virtual Reality":        return VIRTUAL_REALITY;
            case "eReader":                return E_READER;
            case "Set-top box":            return SET_TOP_BOX;
            case "TV":                     return TV;
            case "Game Console":           return GAME_CONSOLE;
            case "Handheld Game Console":  return HANDHELD_GAME_CONSOLE;
            case "Robot":                  return ROBOT;
            case "Robot Mobile":           return ROBOT_MOBILE;
            case "Robot Imitator":         return ROBOT_IMITATOR;
            case "Hacker":                 return HACKER;
            case "Unknown":                return UNKNOWN;
            default:                       return UNCLASSIFIED;
        }
    }

    /**
     * @param userAgent The instance that needs to be classified.
     * @return Is this a 'normal' consumer device that can simply be bought/downloaded and used as intended.
     */
    public static boolean isNormalConsumerDevice(UserAgent userAgent) {
        switch (getDeviceClass(userAgent)) {
            case DESKTOP:
            case MOBILE:
            case TABLET:
            case PHONE:
            case WATCH:
            case VIRTUAL_REALITY:
            case E_READER:
            case SET_TOP_BOX:
            case TV:
            case GAME_CONSOLE:
            case HANDHELD_GAME_CONSOLE:
                return true;

            case ANONYMIZED:
            case ROBOT:
            case ROBOT_MOBILE:
            case ROBOT_IMITATOR:
            case HACKER:
            case UNKNOWN:
            case UNCLASSIFIED:
            default:
                return false;
        }
    }

    /**
     * @param userAgent The instance that needs to be classified.
     * @return Is this a 'mobile' device. (includes robots that want to be treated as mobile)
     */
    public static boolean isMobile(UserAgent userAgent) {
        switch (getDeviceClass(userAgent)) {
            case MOBILE:
            case TABLET:
            case PHONE:
            case WATCH:
            case VIRTUAL_REALITY:
            case E_READER:
            case HANDHELD_GAME_CONSOLE:
            case ROBOT_MOBILE:
                return true;

            case DESKTOP:
            case SET_TOP_BOX:
            case TV:
            case GAME_CONSOLE:
            case ANONYMIZED:
            case ROBOT:
            case ROBOT_IMITATOR:
            case HACKER:
            case UNKNOWN:
            case UNCLASSIFIED:
            default:
                return false;
        }
    }

    /**
     * @param userAgent The instance that needs to be classified.
     * @return If this is probably a human using the device.
     */
    public static boolean isHuman(UserAgent userAgent) {
        switch (getDeviceClass(userAgent)) {
            case DESKTOP:
            case MOBILE:
            case TABLET:
            case PHONE:
            case WATCH:
            case VIRTUAL_REALITY:
            case E_READER:
            case SET_TOP_BOX:
            case TV:
            case GAME_CONSOLE:
            case HANDHELD_GAME_CONSOLE:
            case ANONYMIZED:
                return true;

            case ROBOT:
            case ROBOT_MOBILE:
            case ROBOT_IMITATOR:
            case HACKER:
            case UNKNOWN:
            case UNCLASSIFIED:
            default:
                return false;
        }
    }

    /**
     * @param userAgent The instance that needs to be classified.
     * @return Do we see this as deliberate misuse?
     */
    public static boolean isDeliberateMisuse(UserAgent userAgent) {
        switch (getDeviceClass(userAgent)) {
            case ANONYMIZED:
            case ROBOT_IMITATOR:
            case HACKER:
                return true;

            case DESKTOP:
            case MOBILE:
            case TABLET:
            case PHONE:
            case WATCH:
            case VIRTUAL_REALITY:
            case E_READER:
            case SET_TOP_BOX:
            case TV:
            case GAME_CONSOLE:
            case HANDHELD_GAME_CONSOLE:
            case ROBOT:
            case ROBOT_MOBILE:
            case UNKNOWN:
            case UNCLASSIFIED:
            default:
                return false;
        }
    }

}
