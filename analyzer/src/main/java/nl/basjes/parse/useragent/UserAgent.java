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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.AgentField.ImmutableAgentField;
import nl.basjes.parse.useragent.AgentField.MutableAgentField;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.parser.UserAgentBaseListener;
import nl.basjes.parse.useragent.utils.DefaultANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public interface UserAgent extends Serializable {

    String getUserAgentString();
    AgentField get(String fieldName);
    String getValue(String fieldName);
    Long getConfidence(String fieldName);
    boolean hasSyntaxError();
    boolean hasAmbiguity();
    int getAmbiguityCount();

    List<String> getAvailableFieldNamesSorted();

    String DEVICE_CLASS                         = "DeviceClass";
    String DEVICE_NAME                          = "DeviceName";
    String DEVICE_BRAND                         = "DeviceBrand";
    String DEVICE_CPU                           = "DeviceCpu";
    String DEVICE_CPU_BITS                      = "DeviceCpuBits";
    String DEVICE_FIRMWARE_VERSION              = "DeviceFirmwareVersion";
    String DEVICE_VERSION                       = "DeviceVersion";

    String OPERATING_SYSTEM_CLASS               = "OperatingSystemClass";
    String OPERATING_SYSTEM_NAME                = "OperatingSystemName";
    String OPERATING_SYSTEM_VERSION             = "OperatingSystemVersion";
    String OPERATING_SYSTEM_VERSION_MAJOR       = "OperatingSystemVersionMajor";
    String OPERATING_SYSTEM_NAME_VERSION        = "OperatingSystemNameVersion";
    String OPERATING_SYSTEM_NAME_VERSION_MAJOR  = "OperatingSystemNameVersionMajor";
    String OPERATING_SYSTEM_VERSION_BUILD       = "OperatingSystemVersionBuild";

    String LAYOUT_ENGINE_CLASS                  = "LayoutEngineClass";
    String LAYOUT_ENGINE_NAME                   = "LayoutEngineName";
    String LAYOUT_ENGINE_VERSION                = "LayoutEngineVersion";
    String LAYOUT_ENGINE_VERSION_MAJOR          = "LayoutEngineVersionMajor";
    String LAYOUT_ENGINE_NAME_VERSION           = "LayoutEngineNameVersion";
    String LAYOUT_ENGINE_NAME_VERSION_MAJOR     = "LayoutEngineNameVersionMajor";
    String LAYOUT_ENGINE_BUILD                  = "LayoutEngineBuild";

    String AGENT_CLASS                          = "AgentClass";
    String AGENT_NAME                           = "AgentName";
    String AGENT_VERSION                        = "AgentVersion";
    String AGENT_VERSION_MAJOR                  = "AgentVersionMajor";
    String AGENT_NAME_VERSION                   = "AgentNameVersion";
    String AGENT_NAME_VERSION_MAJOR             = "AgentNameVersionMajor";
    String AGENT_BUILD                          = "AgentBuild";
    String AGENT_LANGUAGE                       = "AgentLanguage";
    String AGENT_LANGUAGE_CODE                  = "AgentLanguageCode";
    String AGENT_INFORMATION_EMAIL              = "AgentInformationEmail";
    String AGENT_INFORMATION_URL                = "AgentInformationUrl";
    String AGENT_SECURITY                       = "AgentSecurity";
    String AGENT_UUID                           = "AgentUuid";

    String WEBVIEW_APP_NAME                     = "WebviewAppName";
    String WEBVIEW_APP_VERSION                  = "WebviewAppVersion";
    String WEBVIEW_APP_VERSION_MAJOR            = "WebviewAppVersionMajor";
    String WEBVIEW_APP_NAME_VERSION             = "WebviewAppNameVersion";
    String WEBVIEW_APP_NAME_VERSION_MAJOR       = "WebviewAppNameVersionMajor";

    String FACEBOOK_CARRIER                     = "FacebookCarrier";
    String FACEBOOK_DEVICE_CLASS                = "FacebookDeviceClass";
    String FACEBOOK_DEVICE_NAME                 = "FacebookDeviceName";
    String FACEBOOK_DEVICE_VERSION              = "FacebookDeviceVersion";
    String FACEBOOK_F_B_O_P                     = "FacebookFBOP";
    String FACEBOOK_F_B_S_S                     = "FacebookFBSS";
    String FACEBOOK_OPERATING_SYSTEM_NAME       = "FacebookOperatingSystemName";
    String FACEBOOK_OPERATING_SYSTEM_VERSION    = "FacebookOperatingSystemVersion";

    String ANONYMIZED                           = "Anonymized";

    String HACKER_ATTACK_VECTOR                 = "HackerAttackVector";
    String HACKER_TOOLKIT                       = "HackerToolkit";

    String KOBO_AFFILIATE                       = "KoboAffiliate";
    String KOBO_PLATFORM_ID                     = "KoboPlatformId";

    String IE_COMPATIBILITY_VERSION             = "IECompatibilityVersion";
    String IE_COMPATIBILITY_VERSION_MAJOR       = "IECompatibilityVersionMajor";
    String IE_COMPATIBILITY_NAME_VERSION        = "IECompatibilityNameVersion";
    String IE_COMPATIBILITY_NAME_VERSION_MAJOR  = "IECompatibilityNameVersionMajor";

    String SYNTAX_ERROR                         = "__SyntaxError__";
    String USERAGENT_FIELDNAME                  = "Useragent";

    String NETWORK_TYPE                         = "NetworkType";

    String SET_ALL_FIELDS                       = "__Set_ALL_Fields__";
    String NULL_VALUE                           = "<<<null>>>";
    String UNKNOWN_VALUE                        = "Unknown";
    String UNKNOWN_VERSION                      = "??";
    String UNKNOWN_NAME_VERSION                 = "Unknown ??";

    List<String> STANDARD_FIELDS = Collections.unmodifiableList(Arrays.asList(
        DEVICE_CLASS,
        DEVICE_BRAND,
        DEVICE_NAME,
        OPERATING_SYSTEM_CLASS,
        OPERATING_SYSTEM_NAME,
        OPERATING_SYSTEM_VERSION,
        OPERATING_SYSTEM_VERSION_MAJOR,
        OPERATING_SYSTEM_NAME_VERSION,
        OPERATING_SYSTEM_NAME_VERSION_MAJOR,
        LAYOUT_ENGINE_CLASS,
        LAYOUT_ENGINE_NAME,
        LAYOUT_ENGINE_VERSION,
        LAYOUT_ENGINE_VERSION_MAJOR,
        LAYOUT_ENGINE_NAME_VERSION,
        LAYOUT_ENGINE_NAME_VERSION_MAJOR,
        AGENT_CLASS,
        AGENT_NAME,
        AGENT_VERSION,
        AGENT_VERSION_MAJOR,
        AGENT_NAME_VERSION,
        AGENT_NAME_VERSION_MAJOR
    ));

    // We manually sort the list of fields to ensure the output is consistent.
    // Any unspecified fieldnames will be appended to the end.
    List<String> PRE_SORTED_FIELDS_LIST = Collections.unmodifiableList(Arrays.asList(
        DEVICE_CLASS,
        DEVICE_NAME,
        DEVICE_BRAND,
        DEVICE_CPU,
        DEVICE_CPU_BITS,
        DEVICE_FIRMWARE_VERSION,
        DEVICE_VERSION,

        OPERATING_SYSTEM_CLASS,
        OPERATING_SYSTEM_NAME,
        OPERATING_SYSTEM_VERSION,
        OPERATING_SYSTEM_VERSION_MAJOR,
        OPERATING_SYSTEM_NAME_VERSION,
        OPERATING_SYSTEM_NAME_VERSION_MAJOR,
        OPERATING_SYSTEM_VERSION_BUILD,

        LAYOUT_ENGINE_CLASS,
        LAYOUT_ENGINE_NAME,
        LAYOUT_ENGINE_VERSION,
        LAYOUT_ENGINE_VERSION_MAJOR,
        LAYOUT_ENGINE_NAME_VERSION,
        LAYOUT_ENGINE_NAME_VERSION_MAJOR,
        LAYOUT_ENGINE_BUILD,

        AGENT_CLASS,
        AGENT_NAME,
        AGENT_VERSION,
        AGENT_VERSION_MAJOR,
        AGENT_NAME_VERSION,
        AGENT_NAME_VERSION_MAJOR,
        AGENT_BUILD,
        AGENT_LANGUAGE,
        AGENT_LANGUAGE_CODE,
        AGENT_INFORMATION_EMAIL,
        AGENT_INFORMATION_URL,
        AGENT_SECURITY,
        AGENT_UUID,

        WEBVIEW_APP_NAME,
        WEBVIEW_APP_VERSION,
        WEBVIEW_APP_VERSION_MAJOR,
        WEBVIEW_APP_NAME_VERSION,
        WEBVIEW_APP_NAME_VERSION_MAJOR,

        FACEBOOK_CARRIER,
        FACEBOOK_DEVICE_CLASS,
        FACEBOOK_DEVICE_NAME,
        FACEBOOK_DEVICE_VERSION,
        FACEBOOK_F_B_O_P,
        FACEBOOK_F_B_S_S,
        FACEBOOK_OPERATING_SYSTEM_NAME,
        FACEBOOK_OPERATING_SYSTEM_VERSION,

        ANONYMIZED,

        HACKER_ATTACK_VECTOR,
        HACKER_TOOLKIT,

        KOBO_AFFILIATE,
        KOBO_PLATFORM_ID,

        IE_COMPATIBILITY_VERSION,
        IE_COMPATIBILITY_VERSION_MAJOR,
        IE_COMPATIBILITY_NAME_VERSION,
        IE_COMPATIBILITY_NAME_VERSION_MAJOR,

        SYNTAX_ERROR
    ));

    default String escapeYaml(String input) {
        if (input == null) {
            return NULL_VALUE;
        }
        return input.replace("'", "''");
    }

    default String toYamlTestCase() {
        return toYamlTestCase(false, null);
    }

    default String toYamlTestCase(boolean showConfidence) {
        return toYamlTestCase(showConfidence, null);
    }

    default String toYamlTestCase(boolean showConfidence, Map<String, String> comments) {
        StringBuilder sb = new StringBuilder(10240);
        sb.append("\n");
        sb.append("- test:\n");
        sb.append("    input:\n");
        sb.append("      user_agent_string: '").append(escapeYaml(getUserAgentString())).append("'\n");
        sb.append("    expected:\n");

        List<String> fieldNames = getAvailableFieldNamesSorted();

        int maxNameLength  = 30;
        int maxValueLength = 0;
        for (String fieldName : fieldNames) {
            maxNameLength = Math.max(maxNameLength, fieldName.length());
        }
        for (String fieldName : fieldNames) {
            String value = escapeYaml(getValue(fieldName));
            if (value != null) {
                maxValueLength = Math.max(maxValueLength, value.length());
            }
        }

        for (String fieldName : fieldNames) {
            AgentField field = get(fieldName);
            sb.append("      ").append(fieldName);
            for (int l = fieldName.length(); l < maxNameLength + 6; l++) {
                sb.append(' ');
            }
            String value = escapeYaml(field.getValue());
            sb.append(": '").append(value).append('\'');

            if (showConfidence || comments != null) {
                int l = value.length();
                for (; l < maxValueLength + 5; l++) {
                    sb.append(' ');
                }
                sb.append("# ");
                if (showConfidence) {
                    sb.append(String.format("%8d", getConfidence(fieldName)));
                    if (field.isDefaultValue()) {
                        sb.append(" [Default]");
                    }
                }
                if (comments != null) {
                    String comment = comments.get(fieldName);
                    if (comment != null) {
                        if (!field.isDefaultValue()) {
                            sb.append("          ");
                        }
                        sb.append(" | ").append(comment);
                    }
                }
            }

            sb.append('\n');
        }
        sb.append("\n\n");

        return sb.toString();
    }

    default Map<String, String> toMap() {
        List<String> fields = new ArrayList<>();
        fields.add(USERAGENT_FIELDNAME);
        fields.addAll(getAvailableFieldNamesSorted());
        return toMap(fields);
    }

    default Map<String, String> toMap(String... fieldNames) {
        return toMap(Arrays.asList(fieldNames));
    }

    default Map<String, String> toMap(List<String> fieldNames) {
        Map<String, String> result = new TreeMap<>();

        for (String fieldName : fieldNames) {
            if (USERAGENT_FIELDNAME.equals(fieldName)) {
                result.put(USERAGENT_FIELDNAME, getUserAgentString());
            } else {
                AgentField field = get(fieldName);
                result.put(fieldName, field.getValue());
            }
        }
        return result;
    }

    default String toJson() {
        List<String> fields = new ArrayList<>();
        fields.add(USERAGENT_FIELDNAME);
        fields.addAll(getAvailableFieldNamesSorted());
        return toJson(fields);
    }

    default String toJson(String... fieldNames) {
        return toJson(Arrays.asList(fieldNames));
    }

    default String toJson(List<String> fieldNames) {
        StringBuilder sb = new StringBuilder(10240);
        sb.append("{");

        boolean addSeparator = false;
        for (String fieldName : fieldNames) {
            if (addSeparator) {
                sb.append(',');
            } else {
                addSeparator = true;
            }
            if (USERAGENT_FIELDNAME.equals(fieldName)) {
                sb
                    .append("\"Useragent\"")
                    .append(':')
                    .append('"').append(StringEscapeUtils.escapeJson(getUserAgentString())).append('"');
            } else {
                AgentField field = get(fieldName);
                sb
                    .append('"').append(StringEscapeUtils.escapeJson(fieldName)).append('"')
                    .append(':')
                    .append('"').append(StringEscapeUtils.escapeJson(field.getValue())).append('"');
            }
        }

        sb.append("}");
        return sb.toString();
    }

    default String toXML() {
        List<String> fields = new ArrayList<>();
        fields.add(USERAGENT_FIELDNAME);
        fields.addAll(getAvailableFieldNamesSorted());
        return toXML(fields);
    }

    default String toXML(String... fieldNames) {
        return toXML(Arrays.asList(fieldNames));
    }

    default String toXML(List<String> fieldNames) {
        StringBuilder sb =
            new StringBuilder(10240)
                .append("<Yauaa>");

        for (String fieldName : fieldNames) {
            if (USERAGENT_FIELDNAME.equals(fieldName)) {
                sb
                    .append("<Useragent>")
                    .append(StringEscapeUtils.escapeXml10(getUserAgentString()))
                    .append("</Useragent>");
            } else {
                AgentField field = get(fieldName);
                sb
                    .append('<').append(StringEscapeUtils.escapeXml10(fieldName)).append('>')
                    .append(StringEscapeUtils.escapeXml10(field.getValue()))
                    .append("</").append(StringEscapeUtils.escapeXml10(fieldName)).append('>');
            }
        }

        sb.append("</Yauaa>");

        return sb.toString();
    }

    default String toString(String... fieldNames) {
        return toString(Arrays.asList(fieldNames));
    }

    default String toString(List<String> fieldNames) {
        String uaFieldName = "user_agent_string";
        int    maxLength   = uaFieldName.length();
        for (String fieldName : fieldNames) {
            maxLength = Math.max(maxLength, fieldName.length());
        }
        StringBuilder sb        = new StringBuilder("  - ").append(uaFieldName);
        for (int l = uaFieldName.length(); l < maxLength + 2; l++) {
            sb.append(' ');
        }
        sb.append(": '").append(escapeYaml(getUserAgentString())).append("'\n");

        for (String fieldName : fieldNames) {
            if (!USERAGENT_FIELDNAME.equals(fieldName)) {
                AgentField field = get(fieldName);
                if (field != null) {
                    sb.append("    ").append(fieldName);
                    for (int l = fieldName.length(); l < maxLength + 2; l++) {
                        sb.append(' ');
                    }
                    sb.append(": '").append(escapeYaml(field.getValue())).append('\'');
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }

    default boolean uaEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserAgent)) {
            return false;
        }
        UserAgent agent = (UserAgent) o;
        if (!Objects.equals(getUserAgentString(), agent.getUserAgentString())) {
            return false;
        }
        List<String> fieldNamesSorted1 = getAvailableFieldNamesSorted();
        List<String> fieldNamesSorted2 = agent.getAvailableFieldNamesSorted();
        if (!Objects.equals(fieldNamesSorted1, fieldNamesSorted2)) {
            return false;
        }
        for (String fieldName: fieldNamesSorted1) {
            if (!Objects.equals(get(fieldName), agent.get(fieldName))) {
                return false;
            }
        }
        return true;
    }

    default int uaHashCode() {
        int result = Objects.hash(getUserAgentString());
        for (String fieldName: getAvailableFieldNamesSorted()) {
            result = 31 * result + get(fieldName).hashCode();
        }
        return result;
    }

    class MutableUserAgent extends UserAgentBaseListener implements UserAgent, Serializable, DefaultANTLRErrorListener {

        private static final Logger LOG                     = LogManager.getLogger(UserAgent.class);

        private static String getDefaultValueForField(String fieldName) {
            if (fieldName.contains("NameVersion")) {
                return UNKNOWN_NAME_VERSION;
            }
            if (fieldName.contains("Version")) {
                return UNKNOWN_VERSION;
            }
            return UNKNOWN_VALUE;
        }

        private Set<String> wantedFieldNames = null;
        private boolean     hasSyntaxError;
        private boolean     hasAmbiguity;
        private int         ambiguityCount;

        public void destroy() {
            wantedFieldNames = null;
        }

        public boolean hasSyntaxError() {
            return hasSyntaxError;
        }

        public boolean hasAmbiguity() {
            return hasAmbiguity;
        }

        public int getAmbiguityCount() {
            return ambiguityCount;
        }

        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {
            if (debug) {
                LOG.error("Syntax error");
                LOG.error("Source : {}", userAgentString);
                LOG.error("Message: {}", msg);
            }
            hasSyntaxError = true;
            MutableAgentField syntaxError = new MutableAgentField("false");
            syntaxError.setValue("true", 1);
            allFields.put(SYNTAX_ERROR, syntaxError);
        }

        @Override
        public void reportAmbiguity(
            Parser recognizer,
            DFA dfa,
            int startIndex,
            int stopIndex,
            boolean exact,
            BitSet ambigAlts,
            ATNConfigSet configs) {
            hasAmbiguity = true;
            ambiguityCount++;
        }

        // The original input value
        private String userAgentString = null;

        private boolean debug = false;

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean newDebug) {
            this.debug = newDebug;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object o) {
            return uaEquals(o);
        }

        @Override
        public int hashCode() {
            return uaHashCode();
        }

        private final Map<String, MutableAgentField> allFields = new HashMap<>();

        private void setWantedFieldNames(Collection<String> newWantedFieldNames) {
            if (newWantedFieldNames != null) {
                if (!newWantedFieldNames.isEmpty()) {
                    wantedFieldNames = new LinkedHashSet<>(newWantedFieldNames);
                }
            }
        }

        public MutableUserAgent() {
        }

        public MutableUserAgent(Collection<String> wantedFieldNames) {
            setWantedFieldNames(wantedFieldNames);
        }

        public MutableUserAgent(String userAgentString) {
            // wantedFieldNames == null; --> Assume we want all fields.
            setUserAgentString(userAgentString);
        }

        public MutableUserAgent(String userAgentString, Collection<String> wantedFieldNames) {
            setWantedFieldNames(wantedFieldNames);
            setUserAgentString(userAgentString);
        }

        public void setUserAgentString(String newUserAgentString) {
            this.userAgentString = newUserAgentString;
            reset();
        }

        public String getUserAgentString() {
            return userAgentString;
        }

        public void reset() {
            hasSyntaxError = false;
            hasAmbiguity = false;
            ambiguityCount = 0;

            for (MutableAgentField field : allFields.values()) {
                field.reset();
            }
        }

        public static boolean isSystemField(String fieldname) {
            switch (fieldname) {
                case SET_ALL_FIELDS:
                case SYNTAX_ERROR:
                case USERAGENT_FIELDNAME:
                    return true;
                default:
                    return false;
            }
        }

        public void processSetAll() {
            MutableAgentField setAllField = allFields.get(SET_ALL_FIELDS);
            if (setAllField == null) {
                return;
            }

            String value;
            if (setAllField.isDefaultValue()) {
                value = NULL_VALUE;
            } else {
                value = setAllField.getValue();
            }

            long confidence = setAllField.confidence;
            for (Map.Entry<String, MutableAgentField> fieldEntry : allFields.entrySet()) {
                if (!isSystemField(fieldEntry.getKey())) {
                    fieldEntry.getValue().setValue(value, confidence);
                }
            }
        }

        public void set(String attribute, String value, long confidence) {
            MutableAgentField field = allFields.get(attribute);
            if (field == null) {
                field = new MutableAgentField(getDefaultValueForField(attribute));
            }

            boolean wasEmpty = confidence == -1;
            boolean updated  = field.setValue(value, confidence);
            if (debug && !wasEmpty) {
                if (updated) {
                    LOG.info("USE  {} ({}) = {}", attribute, confidence, value);
                } else {
                    LOG.info("SKIP {} ({}) = {}", attribute, confidence, value);
                }
            }
            allFields.put(attribute, field);
        }

        public void setForced(String attribute, String value, long confidence) {
            MutableAgentField field = allFields.get(attribute);
            if (field == null) {
                field = new MutableAgentField(getDefaultValueForField(attribute));
            }

            boolean wasEmpty = confidence == -1;
            field.setValueForced(value, confidence);
            if (debug && !wasEmpty) {
                LOG.info("USE  {} ({}) = {}", attribute, confidence, value);
            }
            allFields.put(attribute, field);
        }

        // The appliedMatcher parameter is needed for development and debugging.
        public void set(MutableUserAgent newValuesUserAgent, Matcher appliedMatcher) { // NOSONAR: Unused parameter
            for (String fieldName : newValuesUserAgent.allFields.keySet()) {
                MutableAgentField field = newValuesUserAgent.allFields.get(fieldName);
                set(fieldName, field.value, field.confidence);
            }
        }

        void setImmediateForTesting(String fieldName, MutableAgentField agentField) {
            allFields.put(fieldName, agentField);
        }

        public AgentField get(String fieldName) {
            if (USERAGENT_FIELDNAME.equals(fieldName)) {
                MutableAgentField agentField = new MutableAgentField(userAgentString);
                agentField.setValue(userAgentString, 0L);
                return agentField;
            } else {
                return allFields
                    .computeIfAbsent(
                        fieldName,
                        f -> new MutableAgentField(getDefaultValueForField(fieldName)));
            }
        }

        public String getValue(String fieldName) {
            if (USERAGENT_FIELDNAME.equals(fieldName)) {
                return userAgentString;
            }
            AgentField field = allFields.get(fieldName);
            if (field == null) {
                return getDefaultValueForField(fieldName);
            }
            return field.getValue();
        }

        public Long getConfidence(String fieldName) {
            if (USERAGENT_FIELDNAME.equals(fieldName)) {
                return 0L;
            }
            AgentField field = allFields.get(fieldName);
            if (field == null) {
                return -1L;
            }
            return field.getConfidence();
        }

        @Override
        public List<String> getAvailableFieldNamesSorted() {
            List<String> fieldNames = new ArrayList<>(allFields.size() + 10);
            if (wantedFieldNames == null) {
                fieldNames.addAll(STANDARD_FIELDS);
                allFields.forEach((fieldName, field) -> {
                    if (!fieldNames.contains(fieldName)) {
                        if (!field.isDefaultValue()) {
                            fieldNames.add(fieldName);
                        }
                    }
                });
            } else {
                allFields.forEach((fieldName, field) -> {
                    if (!fieldNames.contains(fieldName)) {
                        if (!field.isDefaultValue()) {
                            if (wantedFieldNames.contains(fieldName)) {
                                fieldNames.add(fieldName);
                            }
                        }
                    }
                });
            }

            // This is not a field; this is a special operator.
            fieldNames.remove(SET_ALL_FIELDS);

            List<String> result = PRE_SORTED_FIELDS_LIST
                .stream()
                .filter(fieldNames::remove)
                .collect(Collectors.toList());

            Collections.sort(fieldNames);
            result.addAll(fieldNames);
            return result;
        }

        @Override
        public String toString() {
            return toString(getAvailableFieldNamesSorted());
        }
    }

    class ImmutableUserAgent implements UserAgent {
        private final String                            userAgentString;
        private final ImmutableAgentField               userAgentStringField;
        private final Map<String, ImmutableAgentField>  allFields;
        private final List<String>                      availableFieldNamesSorted;
        private final boolean                           hasSyntaxError;
        private final boolean                           hasAmbiguity;
        private final int                               ambiguityCount;

        public ImmutableUserAgent(MutableUserAgent userAgent) {
            userAgentString = userAgent.userAgentString;
            hasSyntaxError = userAgent.hasSyntaxError;
            hasAmbiguity = userAgent.hasAmbiguity;
            ambiguityCount = userAgent.ambiguityCount;

            userAgentStringField = new ImmutableAgentField(userAgentString, 0L, false, userAgentString);

            Map<String, ImmutableAgentField> preparingAllFields = new LinkedHashMap<>(userAgent.allFields.size());

            for (String fieldName: userAgent.getAvailableFieldNamesSorted()) {
                preparingAllFields.put(fieldName, new ImmutableAgentField((MutableAgentField) userAgent.get(fieldName)));
            }

            allFields = Collections.unmodifiableMap(preparingAllFields);
            availableFieldNamesSorted = Collections.unmodifiableList(userAgent.getAvailableFieldNamesSorted());
        }

        @Override
        public String getUserAgentString() {
            return userAgentString;
        }

        public AgentField get(String fieldName) {
            if (USERAGENT_FIELDNAME.equals(fieldName)) {
                return userAgentStringField;
            } else {
                ImmutableAgentField agentField = allFields.get(fieldName);
                if (agentField == null) {
                    agentField = new ImmutableAgentField(MutableUserAgent.getDefaultValueForField(fieldName),
                        -1,
                        true,
                        MutableUserAgent.getDefaultValueForField(fieldName));
                }
                return agentField;
            }
        }

        public String getValue(String fieldName) {
            if (USERAGENT_FIELDNAME.equals(fieldName)) {
                return userAgentString;
            }
            AgentField field = allFields.get(fieldName);
            if (field == null) {
                return MutableUserAgent.getDefaultValueForField(fieldName);
            }
            return field.getValue();
        }

        public Long getConfidence(String fieldName) {
            if (USERAGENT_FIELDNAME.equals(fieldName)) {
                return 0L;
            }
            AgentField field = allFields.get(fieldName);
            if (field == null) {
                return -1L;
            }
            return field.getConfidence();
        }

        public boolean hasSyntaxError() {
            return hasSyntaxError;
        }

        public boolean hasAmbiguity() {
            return hasAmbiguity;
        }

        public int getAmbiguityCount() {
            return ambiguityCount;
        }

        @Override
        public List<String> getAvailableFieldNamesSorted() {
            return availableFieldNamesSorted;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object o) {
            return uaEquals(o);
        }

        @Override
        public int hashCode() {
            return uaHashCode();
        }

        @Override
        public String toString() {
            return toString(getAvailableFieldNamesSorted());
        }

    }

}
