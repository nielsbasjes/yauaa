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

package nl.basjes.parse.useragent.calculate;

import nl.basjes.parse.useragent.UserAgent;

public class ConcatNONDuplicatedCalculator implements FieldCalculator {

    private String targetName;
    private String firstName;
    private String secondName;

    public ConcatNONDuplicatedCalculator(String targetName, String firstName, String secondName) {
        this.targetName = targetName;
        this.firstName = firstName;
        this.secondName = secondName;
    }

    // Private constructor for serialization systems ONLY (like Kyro)
    private ConcatNONDuplicatedCalculator() { }

    @Override
    public void calculate(UserAgent userAgent) {
        UserAgent.AgentField firstField = userAgent.get(firstName);
        UserAgent.AgentField secondField = userAgent.get(secondName);

        String first = null;
        long firstConfidence = -1;
        String second = null;
        long secondConfidence = -1;

        if (firstField != null) {
            first = firstField.getValue();
            firstConfidence = firstField.getConfidence();
        }
        if (secondField != null) {
            second = secondField.getValue();
            secondConfidence = secondField.getConfidence();
        }

        if (first == null && second == null) {
            return; // Nothing to do
        }

        if (second == null) {
            if (firstConfidence >= 0) {
                userAgent.set(targetName, first, firstConfidence);
            } else {
                userAgent.setForced(targetName, "Unknown", firstConfidence);
            }
            return; // Nothing to do
        } else {
            if (first == null) {
                if (secondConfidence >= 0) {
                    userAgent.set(targetName, second, secondConfidence);
                } else {
                    userAgent.setForced(targetName, "Unknown", secondConfidence);
                }
                return;
            }
        }

        if (first.equals(second)) {
            userAgent.set(targetName, first, firstConfidence);
        } else {
            if (second.startsWith(first)) {
                userAgent.set(targetName, second, secondConfidence);
            } else {
                String value = first + " " + second;
                long confidence = Math.max(firstConfidence, secondConfidence);
                if (confidence < 0) {
                    userAgent.setForced(targetName, value, confidence);
                } else {
                    userAgent.set(targetName, value, confidence);
                }
            }
        }

    }
}
