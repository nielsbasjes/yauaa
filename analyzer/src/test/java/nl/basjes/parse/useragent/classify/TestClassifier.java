/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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
import org.junit.Test;

import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.classify.DeviceClass.Anonymized;
import static nl.basjes.parse.useragent.classify.DeviceClass.Desktop;
import static nl.basjes.parse.useragent.classify.DeviceClass.GameConsole;
import static nl.basjes.parse.useragent.classify.DeviceClass.Hacker;
import static nl.basjes.parse.useragent.classify.DeviceClass.HandheldGameConsole;
import static nl.basjes.parse.useragent.classify.DeviceClass.Mobile;
import static nl.basjes.parse.useragent.classify.DeviceClass.Phone;
import static nl.basjes.parse.useragent.classify.DeviceClass.Robot;
import static nl.basjes.parse.useragent.classify.DeviceClass.RobotMobile;
import static nl.basjes.parse.useragent.classify.DeviceClass.SetTopBox;
import static nl.basjes.parse.useragent.classify.DeviceClass.Spy;
import static nl.basjes.parse.useragent.classify.DeviceClass.TV;
import static nl.basjes.parse.useragent.classify.DeviceClass.Tablet;
import static nl.basjes.parse.useragent.classify.DeviceClass.Unclassified;
import static nl.basjes.parse.useragent.classify.DeviceClass.Unknown;
import static nl.basjes.parse.useragent.classify.DeviceClass.VirtualReality;
import static nl.basjes.parse.useragent.classify.DeviceClass.Watch;
import static nl.basjes.parse.useragent.classify.DeviceClass.eReader;
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isDeliberateMisuse;
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isHuman;
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isMobile;
import static nl.basjes.parse.useragent.classify.UserAgentClassifier.isNormalConsumerDevice;
import static org.junit.Assert.assertEquals;

public class TestClassifier {

    @Test
    public void testEnumCreation() {
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
        verifyEnum("Spy");
        verifyEnum("Hacker");
        verifyEnum("Unknown");
    }

    private void verifyEnum(String deviceClass) {
        UserAgent userAgent = new UserAgent();
        userAgent.set(DEVICE_CLASS, deviceClass, 1);
        assertEquals(deviceClass, UserAgentClassifier.getDeviceClass(userAgent).getValue());
    }


    @Test
    public void testClassifier() {
                       // DeviceClass,          human, mobile, normal, misuse
        verifyDeviceClass(Desktop,               true,  false,  true, false);
        verifyDeviceClass(Anonymized,            true,  false, false,  true);
        verifyDeviceClass(Mobile,                true,   true,  true, false);
        verifyDeviceClass(Tablet,                true,   true,  true, false);
        verifyDeviceClass(Phone,                 true,   true,  true, false);
        verifyDeviceClass(Watch,                 true,   true,  true, false);
        verifyDeviceClass(VirtualReality,        true,   true,  true, false);
        verifyDeviceClass(eReader,               true,   true,  true, false);
        verifyDeviceClass(SetTopBox,             true,  false,  true, false);
        verifyDeviceClass(TV,                    true,  false,  true, false);
        verifyDeviceClass(GameConsole,           true,  false,  true, false);
        verifyDeviceClass(HandheldGameConsole,   true,   true,  true, false);
        verifyDeviceClass(Robot,                 false, false, false, false);
        verifyDeviceClass(RobotMobile,           false,  true, false, false);
        verifyDeviceClass(Spy,                   false, false, false,  true);
        verifyDeviceClass(Hacker,                false, false, false,  true);
        verifyDeviceClass(Unknown,               false, false, false, false);
        verifyDeviceClass(Unclassified,          false, false, false, false);
    }

    private void verifyDeviceClass(DeviceClass deviceClass, boolean human, boolean mobile, boolean normal, boolean misuse) {
        UserAgent userAgent = new UserAgent();

        userAgent.set(DEVICE_CLASS, deviceClass.getValue(), 1);
        assertEquals("For the DeviceClass " + deviceClass.toString() + " the isHuman() was incorrect.",
            human, isHuman(userAgent));
        assertEquals("For the DeviceClass " + deviceClass.toString() + " the isMobile() was incorrect.",
            mobile, isMobile(userAgent));
        assertEquals("For the DeviceClass " + deviceClass.toString() + " the isNormalConsumerDevice() was incorrect.",
            normal, isNormalConsumerDevice(userAgent));
        assertEquals("For the DeviceClass " + deviceClass.toString() + " the isDeliberateMisuse() was incorrect.",
            misuse, isDeliberateMisuse(userAgent));
    }

}
