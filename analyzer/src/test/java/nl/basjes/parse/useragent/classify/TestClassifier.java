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

package nl.basjes.parse.useragent.classify;

import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import org.junit.jupiter.api.Test;

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
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isDeliberateMisuse;
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isHuman;
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isMobile;
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isNormalConsumerDevice;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestClassifier {

    @Test
    void testEnumCreation() {
        verifyEnum("Desktop");
        verifyEnum("Anonymized");
        verifyEnum("Mobile");
        verifyEnum("Tablet");
        verifyEnum("Phone");
        verifyEnum("Watch");
        verifyEnum("Virtual Reality");
        verifyEnum("eReader");
        verifyEnum("Set-top box");
        verifyEnum("TV");
        verifyEnum("Game Console");
        verifyEnum("Handheld Game Console");
        verifyEnum("Robot");
        verifyEnum("Robot Mobile");
        verifyEnum("Robot Imitator");
        verifyEnum("Hacker");
        verifyEnum("Unknown");
    }

    private void verifyEnum(String deviceClass) {
        MutableUserAgent userAgent = new MutableUserAgent();
        userAgent.set(DEVICE_CLASS, deviceClass, 1);
        assertEquals(deviceClass, UserAgentClassifier.getDeviceClass(userAgent).getValue());
    }


    @Test
    void testClassifier() {
                       // DeviceClass,           human, mobile, normal, misuse
        verifyDeviceClass(DESKTOP,                true,  false,  true, false);
        verifyDeviceClass(ANONYMIZED,             true,  false, false,  true);
        verifyDeviceClass(MOBILE,                 true,   true,  true, false);
        verifyDeviceClass(TABLET,                 true,   true,  true, false);
        verifyDeviceClass(PHONE,                  true,   true,  true, false);
        verifyDeviceClass(WATCH,                  true,   true,  true, false);
        verifyDeviceClass(VIRTUAL_REALITY,        true,   true,  true, false);
        verifyDeviceClass(E_READER,               true,   true,  true, false);
        verifyDeviceClass(SET_TOP_BOX,            true,  false,  true, false);
        verifyDeviceClass(TV,                     true,  false,  true, false);
        verifyDeviceClass(GAME_CONSOLE,           true,  false,  true, false);
        verifyDeviceClass(HANDHELD_GAME_CONSOLE,  true,   true,  true, false);
        verifyDeviceClass(ROBOT,                 false,  false, false, false);
        verifyDeviceClass(ROBOT_MOBILE,          false,   true, false, false);
        verifyDeviceClass(ROBOT_IMITATOR,        false,  false, false,  true);
        verifyDeviceClass(HACKER,                false,  false, false,  true);
        verifyDeviceClass(UNKNOWN,               false,  false, false, false);
        verifyDeviceClass(UNCLASSIFIED,          false,  false, false, false);
    }

    private void verifyDeviceClass(DeviceClass deviceClass, boolean human, boolean mobile, boolean normal, boolean misuse) {
        MutableUserAgent userAgent = new MutableUserAgent();

        userAgent.set(DEVICE_CLASS, deviceClass.getValue(), 1);
        assertEquals(human, isHuman(userAgent),
            "For the DeviceClass " + deviceClass + " the isHuman() was incorrect.");
        assertEquals(mobile, isMobile(userAgent),
            "For the DeviceClass " + deviceClass + " the isMobile() was incorrect.");
        assertEquals(normal, isNormalConsumerDevice(userAgent),
            "For the DeviceClass " + deviceClass + " the isNormalConsumerDevice() was incorrect.");
        assertEquals(misuse, isDeliberateMisuse(userAgent),
            "For the DeviceClass " + deviceClass + " the isDeliberateMisuse() was incorrect.");
    }

}
