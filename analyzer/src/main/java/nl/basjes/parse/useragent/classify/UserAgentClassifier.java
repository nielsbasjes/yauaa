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

package nl.basjes.parse.useragent.classify;

import nl.basjes.parse.useragent.UserAgent;

import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.classify.DeviceClass.Anonymized;
import static nl.basjes.parse.useragent.classify.DeviceClass.Desktop;
import static nl.basjes.parse.useragent.classify.DeviceClass.GameConsole;
import static nl.basjes.parse.useragent.classify.DeviceClass.Hacker;
import static nl.basjes.parse.useragent.classify.DeviceClass.HandheldGameConsole;
import static nl.basjes.parse.useragent.classify.DeviceClass.Mobile;
import static nl.basjes.parse.useragent.classify.DeviceClass.Phone;
import static nl.basjes.parse.useragent.classify.DeviceClass.Robot;
import static nl.basjes.parse.useragent.classify.DeviceClass.RobotImitator;
import static nl.basjes.parse.useragent.classify.DeviceClass.RobotMobile;
import static nl.basjes.parse.useragent.classify.DeviceClass.SetTopBox;
import static nl.basjes.parse.useragent.classify.DeviceClass.TV;
import static nl.basjes.parse.useragent.classify.DeviceClass.Tablet;
import static nl.basjes.parse.useragent.classify.DeviceClass.Unclassified;
import static nl.basjes.parse.useragent.classify.DeviceClass.Unknown;
import static nl.basjes.parse.useragent.classify.DeviceClass.VirtualReality;
import static nl.basjes.parse.useragent.classify.DeviceClass.Watch;
import static nl.basjes.parse.useragent.classify.DeviceClass.eReader;

public final class UserAgentClassifier {
    private UserAgentClassifier(){} // Utility class

    public static DeviceClass getDeviceClass(UserAgent userAgent) {
        switch (userAgent.getValue(DEVICE_CLASS)) {
            case "Desktop":                return Desktop;
            case "Anonymized":             return Anonymized;
            case "Mobile":                 return Mobile;
            case "Tablet":                 return Tablet;
            case "Phone":                  return Phone;
            case "Watch":                  return Watch;
            case "Virtual Reality":        return VirtualReality;
            case "eReader":                return eReader;
            case "Set-top box":            return SetTopBox;
            case "TV":                     return TV;
            case "Game Console":           return GameConsole;
            case "Handheld Game Console":  return HandheldGameConsole;
            case "Robot":                  return Robot;
            case "Robot Mobile":           return RobotMobile;
            case "Robot Imitator":         return RobotImitator;
            case "Hacker":                 return Hacker;
            case "Unknown":                return Unknown;
            default:                       return Unclassified;
        }
    }

    /**
     * @param userAgent The instance that needs to be classified.
     * @return Is this a 'normal' consumer device that can simply be bought/downloaded and used as intended.
     */
    public static boolean isNormalConsumerDevice(UserAgent userAgent) {
        switch (getDeviceClass(userAgent)) {
            case Desktop:
            case Mobile:
            case Tablet:
            case Phone:
            case Watch:
            case VirtualReality:
            case eReader:
            case SetTopBox:
            case TV:
            case GameConsole:
            case HandheldGameConsole:
                return true;

            case Anonymized:
            case Robot:
            case RobotMobile:
            case RobotImitator:
            case Hacker:
            case Unknown:
            case Unclassified:
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
            case Mobile:
            case Tablet:
            case Phone:
            case Watch:
            case VirtualReality:
            case eReader:
            case HandheldGameConsole:
            case RobotMobile:
                return true;

            case Desktop:
            case SetTopBox:
            case TV:
            case GameConsole:
            case Anonymized:
            case Robot:
            case RobotImitator:
            case Hacker:
            case Unknown:
            case Unclassified:
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
            case Desktop:
            case Mobile:
            case Tablet:
            case Phone:
            case Watch:
            case VirtualReality:
            case eReader:
            case SetTopBox:
            case TV:
            case GameConsole:
            case HandheldGameConsole:
            case Anonymized:
                return true;

            case Robot:
            case RobotMobile:
            case RobotImitator:
            case Hacker:
            case Unknown:
            case Unclassified:
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
            case Anonymized:
            case RobotImitator:
            case Hacker:
                return true;

            case Desktop:
            case Mobile:
            case Tablet:
            case Phone:
            case Watch:
            case VirtualReality:
            case eReader:
            case SetTopBox:
            case TV:
            case GameConsole:
            case HandheldGameConsole:
            case Robot:
            case RobotMobile:
            case Unknown:
            case Unclassified:
            default:
                return false;
        }
    }

}
