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

package nl.basjes.parse.useragent.calculate;

import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;

import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;

public class ConcatNONDuplicatedCalculator implements FieldCalculator {

    private final String targetName;
    private final String firstName;
    private final String secondName;

    public ConcatNONDuplicatedCalculator(String targetName, String firstName, String secondName) {
        this.targetName = targetName;
        this.firstName = firstName;
        this.secondName = secondName;

        if (targetName  == null ||
            firstName   == null ||
            secondName  == null) {
            throw new IllegalArgumentException("Target, first and second name may not be null");
        }
    }

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private ConcatNONDuplicatedCalculator() {
        targetName  = "Dummy";
        firstName   = "Dummy";
        secondName  = "Dummy";
    }

    @Override
    public void calculate(MutableUserAgent userAgent) {
        AgentField firstField  = userAgent.get(firstName);
        AgentField secondField = userAgent.get(secondName);

        String  first               = firstField.getValue();
        long    firstConfidence     = firstField.getConfidence();
        String  second              = secondField.getValue();
        long    secondConfidence    = secondField.getConfidence();
        long    confidence          = Math.max(firstConfidence, secondConfidence);

        if (firstField.isDefaultValue() && secondField.isDefaultValue()) {
            userAgent.set(targetName, NULL_VALUE, confidence);
            return;
        }

        if (first.equals(second)) {
            userAgent.setForced(targetName, first, firstConfidence);
            return;
        }

        if (second.startsWith(first)) {
            userAgent.setForced(targetName, second, secondConfidence);
            return;
        }

        String value      = first + " " + second;
        userAgent.set(targetName, value, confidence);
    }

    @Override
    public String[] getDependencies() {
        return new String[]{firstName, secondName};
    }

    @Override
    public String toString() {
        return "Calculate [ " + firstName + " + " + secondName + " ] --> " + targetName;
    }

}
