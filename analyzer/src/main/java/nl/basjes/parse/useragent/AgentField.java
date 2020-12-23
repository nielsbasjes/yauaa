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

package nl.basjes.parse.useragent;

import java.io.Serializable;
import java.util.Objects;

import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;

public interface AgentField extends Serializable {
    String getValue();

    long getConfidence();

    boolean isDefaultValue();

    String getDefaultValue();

    default boolean afEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgentField)) {
            return false;
        }
        AgentField agentField = (AgentField) o;
        return
            Objects.equals(getConfidence(),     agentField.getConfidence()) &&
            Objects.equals(getValue(),          agentField.getValue()) &&
            Objects.equals(getDefaultValue(),   agentField.getDefaultValue()) &&
            Objects.equals(isDefaultValue(),    agentField.isDefaultValue());
    }

    default int afHashCode() {
        return Objects.hash(getConfidence(), getValue(), getDefaultValue(), isDefaultValue());
    }

    default String afToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ value:'").append(getValue()).append("', confidence:'").append(getConfidence()).append("', ");

        String defaultValue = getDefaultValue();
        if (defaultValue == null) {
            sb.append("default:null, ");
        } else {
            sb.append("default:'").append(getDefaultValue()).append("', ");
        }
        sb.append("isDefault:").append(isDefaultValue()).append(" }");
        return sb.toString();
    }

    class MutableAgentField implements AgentField {
        final String defaultValue;
        String value;
        long confidence;

        @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
        private MutableAgentField() {
            defaultValue = null;
        }

        MutableAgentField(String defaultValue) {
            this.defaultValue = defaultValue;
            reset();
        }

        public void reset() {
            value = null;
            confidence = -1;
        }

        public String getValue() {
            if (value == null) {
                return defaultValue;
            }
            return value;
        }

        public boolean isDefaultValue() {
            return confidence < 0 || value == null;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

        public long getConfidence() {
            return confidence;
        }

        public boolean setValue(MutableAgentField field) {
            return setValue(field.value, field.confidence);
        }

        public boolean setValue(String newValue, long newConfidence) {
            if (newConfidence > this.confidence) {
                this.confidence = newConfidence;

                if (NULL_VALUE.equals(newValue)) {
                    this.value = null;
                } else {
                    this.value = newValue;
                }
                return true;
            }
            return false;
        }

        public void setValueForced(String newValue, long newConfidence) {
            this.confidence = newConfidence;

            if (NULL_VALUE.equals(newValue)) {
                this.value = null;
            } else {
                this.value = newValue;
            }
        }

        @Override
        public boolean equals(Object o) {
            return afEquals(o);
        }

        @Override
        public int hashCode() {
            return afHashCode();
        }

        @Override
        public String toString() {
            return afToString();
        }
    }

    class ImmutableAgentField implements AgentField {
        private final String value;
        private final long confidence;
        private final boolean isDefaultValue;
        private final String defaultValue;

        public ImmutableAgentField(String value, long confidence, boolean isDefaultValue, String defaultValue) {
            this.value = value;
            this.confidence = confidence;
            this.isDefaultValue = isDefaultValue;
            this.defaultValue = defaultValue;
        }

        public ImmutableAgentField(MutableAgentField agentField) {
            value = agentField.getValue();
            confidence = agentField.getConfidence();
            isDefaultValue = agentField.isDefaultValue();
            defaultValue = agentField.getDefaultValue();
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public long getConfidence() {
            return confidence;
        }

        @Override
        public boolean isDefaultValue() {
            return isDefaultValue;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public boolean equals(Object o) {
            return afEquals(o);
        }

        @Override
        public int hashCode() {
            return afHashCode();
        }

        @Override
        public String toString() {
            return afToString();
        }
    }
}
