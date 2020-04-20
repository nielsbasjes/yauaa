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

import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.parser.UserAgentBaseListener;
import nl.basjes.parse.useragent.utils.DefaultANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class UserAgent extends UserAgentBaseListener implements Serializable, DefaultANTLRErrorListener {

    private static final Logger LOG = LoggerFactory.getLogger(UserAgent.class);
    public static final String DEVICE_CLASS                            = "DeviceClass";
    public static final String DEVICE_NAME                             = "DeviceName";
    public static final String DEVICE_BRAND                            = "DeviceBrand";
    public static final String DEVICE_CPU                              = "DeviceCpu";
    public static final String DEVICE_CPU_BITS                         = "DeviceCpuBits";
    public static final String DEVICE_FIRMWARE_VERSION                 = "DeviceFirmwareVersion";
    public static final String DEVICE_VERSION                          = "DeviceVersion";

    public static final String OPERATING_SYSTEM_CLASS                  = "OperatingSystemClass";
    public static final String OPERATING_SYSTEM_NAME                   = "OperatingSystemName";
    public static final String OPERATING_SYSTEM_VERSION                = "OperatingSystemVersion";
    public static final String OPERATING_SYSTEM_VERSION_MAJOR          = "OperatingSystemVersionMajor";
    public static final String OPERATING_SYSTEM_NAME_VERSION           = "OperatingSystemNameVersion";
    public static final String OPERATING_SYSTEM_NAME_VERSION_MAJOR     = "OperatingSystemNameVersionMajor";
    public static final String OPERATING_SYSTEM_VERSION_BUILD          = "OperatingSystemVersionBuild";

    public static final String LAYOUT_ENGINE_CLASS                     = "LayoutEngineClass";
    public static final String LAYOUT_ENGINE_NAME                      = "LayoutEngineName";
    public static final String LAYOUT_ENGINE_VERSION                   = "LayoutEngineVersion";
    public static final String LAYOUT_ENGINE_VERSION_MAJOR             = "LayoutEngineVersionMajor";
    public static final String LAYOUT_ENGINE_NAME_VERSION              = "LayoutEngineNameVersion";
    public static final String LAYOUT_ENGINE_NAME_VERSION_MAJOR        = "LayoutEngineNameVersionMajor";
    public static final String LAYOUT_ENGINE_BUILD                     = "LayoutEngineBuild";

    public static final String AGENT_CLASS                             = "AgentClass";
    public static final String AGENT_NAME                              = "AgentName";
    public static final String AGENT_VERSION                           = "AgentVersion";
    public static final String AGENT_VERSION_MAJOR                     = "AgentVersionMajor";
    public static final String AGENT_NAME_VERSION                      = "AgentNameVersion";
    public static final String AGENT_NAME_VERSION_MAJOR                = "AgentNameVersionMajor";
    public static final String AGENT_BUILD                             = "AgentBuild";
    public static final String AGENT_LANGUAGE                          = "AgentLanguage";
    public static final String AGENT_LANGUAGE_CODE                     = "AgentLanguageCode";
    public static final String AGENT_INFORMATION_EMAIL                 = "AgentInformationEmail";
    public static final String AGENT_INFORMATION_URL                   = "AgentInformationUrl";
    public static final String AGENT_SECURITY                          = "AgentSecurity";
    public static final String AGENT_UUID                              = "AgentUuid";

    public static final String WEBVIEW_APP_NAME                        = "WebviewAppName";
    public static final String WEBVIEW_APP_VERSION                     = "WebviewAppVersion";
    public static final String WEBVIEW_APP_VERSION_MAJOR               = "WebviewAppVersionMajor";
    public static final String WEBVIEW_APP_NAME_VERSION_MAJOR          = "WebviewAppNameVersionMajor";

    public static final String FACEBOOK_CARRIER                        = "FacebookCarrier";
    public static final String FACEBOOK_DEVICE_CLASS                   = "FacebookDeviceClass";
    public static final String FACEBOOK_DEVICE_NAME                    = "FacebookDeviceName";
    public static final String FACEBOOK_DEVICE_VERSION                 = "FacebookDeviceVersion";
    public static final String FACEBOOK_F_B_O_P                        = "FacebookFBOP";
    public static final String FACEBOOK_F_B_S_S                        = "FacebookFBSS";
    public static final String FACEBOOK_OPERATING_SYSTEM_NAME          = "FacebookOperatingSystemName";
    public static final String FACEBOOK_OPERATING_SYSTEM_VERSION       = "FacebookOperatingSystemVersion";

    public static final String ANONYMIZED                              = "Anonymized";

    public static final String HACKER_ATTACK_VECTOR                    = "HackerAttackVector";
    public static final String HACKER_TOOLKIT                          = "HackerToolkit";

    public static final String KOBO_AFFILIATE                          = "KoboAffiliate";
    public static final String KOBO_PLATFORM_ID                        = "KoboPlatformId";

    public static final String IE_COMPATIBILITY_VERSION                = "IECompatibilityVersion";
    public static final String IE_COMPATIBILITY_VERSION_MAJOR          = "IECompatibilityVersionMajor";
    public static final String IE_COMPATIBILITY_NAME_VERSION           = "IECompatibilityNameVersion";
    public static final String IE_COMPATIBILITY_NAME_VERSION_MAJOR     = "IECompatibilityNameVersionMajor";

    public static final String SYNTAX_ERROR                            = "__SyntaxError__";
    public static final String USERAGENT_FIELDNAME                     = "Useragent";

    public static final String NETWORK_TYPE                            = "NetworkType";

    public static final String SET_ALL_FIELDS                          = "__Set_ALL_Fields__";
    public static final String NULL_VALUE                              = "<<<null>>>";
    public static final String UNKNOWN_VALUE                           = "Unknown";
    public static final String UNKNOWN_VERSION                         = "??";
    public static final String UNKNOWN_NAME_VERSION                    = "Unknown ??";

    public static final List<String> STANDARD_FIELDS = Collections.unmodifiableList(Arrays.asList(
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
        if (wantedFieldNames != null) {
            wantedFieldNames.clear();
        }
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
        AgentField syntaxError = new AgentField("false");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserAgent)) {
            return false;
        }
        UserAgent agent = (UserAgent) o;
        return Objects.equals(userAgentString, agent.userAgentString) &&
               Objects.equals(allFields, agent.allFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userAgentString, allFields);
    }

    public static class AgentField implements Serializable {
        private final String defaultValue;
        private String value;

        private long confidence;

        @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
        private AgentField() {
            defaultValue = null;
        }

        AgentField(String defaultValue) {
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

        public long getConfidence() {
            if (value == null) {
                return -1; // Lie in case the value was wiped.
            }
            return confidence;
        }

        public boolean setValue(AgentField field) {
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
            if (this == o) {
                return true;
            }
            if (!(o instanceof AgentField)) {
                return false;
            }
            AgentField that = (AgentField) o;
            return confidence == that.confidence &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(defaultValue, value, confidence);
        }

        @Override
        public String toString() {
            if (defaultValue == null) {
                return "{ value:'" + value + "', confidence:'" + confidence + "', default:null }";
            }
            return "{ value:'" + value + "', confidence:'" + confidence + "', default:'" + defaultValue + "' }";
        }
    }

    private final Map<String, AgentField> allFields = new HashMap<>();

    private void setWantedFieldNames(Collection<String> newWantedFieldNames) {
        if (newWantedFieldNames != null) {
            if (!newWantedFieldNames.isEmpty()) {
                wantedFieldNames = new LinkedHashSet<>(newWantedFieldNames);
            }
        }
    }

    public UserAgent() {
        init();
    }

    public UserAgent(Collection<String> wantedFieldNames) {
        setWantedFieldNames(wantedFieldNames);
        init();
    }

    public UserAgent(String userAgentString) {
        // wantedFieldNames == null; --> Assume we want all fields.
        init();
        setUserAgentString(userAgentString);
    }

    public UserAgent(String userAgentString, Collection<String> wantedFieldNames) {
        setWantedFieldNames(wantedFieldNames);
        init();
        setUserAgentString(userAgentString);
    }

    public UserAgent(UserAgent userAgent) {
        clone(userAgent);
    }

    public void clone(UserAgent userAgent) {
        userAgentString  = userAgent.userAgentString;
        wantedFieldNames = userAgent.wantedFieldNames;
        debug            = userAgent.debug;
        hasSyntaxError   = userAgent.hasSyntaxError;
        hasAmbiguity     = userAgent.hasAmbiguity;
        ambiguityCount   = userAgent.ambiguityCount;

        init(); // Making sure the default values are copied correctly.

        for (Map.Entry<String, AgentField> entry : userAgent.allFields.entrySet()) {
            set(entry.getKey(), entry.getValue().value, entry.getValue().confidence);
        }
    }

    private void init() {
        if (wantedFieldNames == null) {
            STANDARD_FIELDS.forEach(this::get); // Just getting a field initializes the AgentField
        } else {
            wantedFieldNames.forEach(this::get);
        }
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

        for (AgentField field : allFields.values()) {
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
        AgentField setAllField = allFields.get(SET_ALL_FIELDS);
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
        for (Map.Entry<String, AgentField> fieldEntry : allFields.entrySet()) {
            if (!isSystemField(fieldEntry.getKey())) {
                fieldEntry.getValue().setValue(value, confidence);
            }
        }
    }

    public void set(String attribute, String value, long confidence) {
        AgentField field = allFields.get(attribute);
        if (field == null) {
            field = new AgentField(getDefaultValueForField(attribute));
        }

        boolean wasEmpty = confidence == -1;
        boolean updated = field.setValue(value, confidence);
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
        AgentField field = allFields.get(attribute);
        if (field == null) {
            field = new AgentField(getDefaultValueForField(attribute));
        }

        boolean wasEmpty = confidence == -1;
        field.setValueForced(value, confidence);
        if (debug && !wasEmpty) {
            LOG.info("USE  {} ({}) = {}", attribute, confidence, value);
        }
        allFields.put(attribute, field);
    }

    // The appliedMatcher parameter is needed for development and debugging.
    public void set(UserAgent newValuesUserAgent, Matcher appliedMatcher) { // NOSONAR: Unused parameter
        for (String fieldName : newValuesUserAgent.allFields.keySet()) {
            AgentField field = newValuesUserAgent.allFields.get(fieldName);
            set(fieldName, field.value, field.confidence);
        }
    }

    void setImmediateForTesting(String fieldName, AgentField agentField) {
        allFields.put(fieldName, agentField);
    }

    public AgentField get(String fieldName) {
        if (USERAGENT_FIELDNAME.equals(fieldName)) {
            AgentField agentField = new AgentField(userAgentString);
            agentField.setValue(userAgentString, 0L);
            return agentField;
        } else {
            return allFields
                .computeIfAbsent(
                    fieldName,
                    f -> new AgentField(getDefaultValueForField(fieldName)));
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

    public String escapeYaml(String input) {
        if (input == null) {
            return NULL_VALUE;
        }
        return input.replace("'", "''");
    }

    public String toYamlTestCase() {
        return toYamlTestCase(false, null);
    }
    public String toYamlTestCase(boolean showConfidence) {
        return toYamlTestCase(showConfidence, null);
    }
    public String toYamlTestCase(boolean showConfidence, Map<String, String> comments) {
        StringBuilder sb = new StringBuilder(10240);
        sb.append("\n");
        sb.append("- test:\n");
//        sb.append("#    options:\n");
//        sb.append("#    - 'verbose'\n");
//        sb.append("#    - 'init'\n");
//        sb.append("#    - 'only'\n");
        sb.append("    input:\n");
        sb.append("      user_agent_string: '").append(escapeYaml(userAgentString)).append("'\n");
        sb.append("    expected:\n");

        List<String> fieldNames = getAvailableFieldNamesSorted();

        int maxNameLength = 30;
        int maxValueLength = 0;
        for (String fieldName : allFields.keySet()) {
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
            if (field.isDefaultValue()) {
                continue;
            }
            sb.append("      ").append(fieldName);
            for (int l = fieldName.length(); l < maxNameLength + 7; l++) {
                sb.append(' ');
            }
            String value = escapeYaml(field.getValue());
            sb.append(": '").append(value).append('\'');
            if (showConfidence) {
                int l = value.length();
                for (; l < maxValueLength + 5; l++) {
                    sb.append(' ');
                }
                sb.append("# ").append(String.format("%8d", getConfidence(fieldName)));
            }
            if (comments != null) {
                String comment = comments.get(fieldName);
                if (comment != null) {
                    sb.append(" | ").append(comment);
                }
            }
            sb.append('\n');
        }
        sb.append("\n\n");

        return sb.toString();
    }

    public Map<String, String> toMap() {
        List<String> fields = new ArrayList<>();
        fields.add(USERAGENT_FIELDNAME);
        fields.addAll(getAvailableFieldNamesSorted());
        return toMap(fields);
    }

    public Map<String, String> toMap(String... fieldNames) {
        return toMap(Arrays.asList(fieldNames));
    }

    public Map<String, String> toMap(List<String> fieldNames) {
        Map<String, String> result = new TreeMap<>();

        for (String fieldName : fieldNames) {
            if (USERAGENT_FIELDNAME.equals(fieldName)) {
                result.put(USERAGENT_FIELDNAME, getUserAgentString());
            } else {
                AgentField field = get(fieldName);
                if (field.isDefaultValue()) {
                    continue;
                }
                result.put(fieldName, field.getValue());
            }
        }
        return result;
    }

    public String toJson() {
        List<String> fields = new ArrayList<>();
        fields.add(USERAGENT_FIELDNAME);
        fields.addAll(getAvailableFieldNamesSorted());
        return toJson(fields);
    }

    public String toJson(String... fieldNames) {
        return toJson(Arrays.asList(fieldNames));
    }

    public String toJson(List<String> fieldNames) {
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
                if (field.isDefaultValue()) {
                    continue;
                }
                sb
                    .append('"').append(StringEscapeUtils.escapeJson(fieldName)).append('"')
                    .append(':')
                    .append('"').append(StringEscapeUtils.escapeJson(field.getValue())).append('"');
            }
        }

        sb.append("}");
        return sb.toString();
    }

    public String toXML() {
        List<String> fields = new ArrayList<>();
        fields.add(USERAGENT_FIELDNAME);
        fields.addAll(getAvailableFieldNamesSorted());
        return toXML(fields);
    }

    public String toXML(String... fieldNames) {
        return toXML(Arrays.asList(fieldNames));
    }

    public String toXML(List<String> fieldNames) {
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
                if (field.isDefaultValue()) {
                    continue;
                }
                sb
                    .append('<').append(StringEscapeUtils.escapeXml10(fieldName)).append('>')
                    .append(StringEscapeUtils.escapeXml10(field.getValue()))
                    .append("</").append(StringEscapeUtils.escapeXml10(fieldName)).append('>');
            }
        }

        sb.append("</Yauaa>");

        return sb.toString();
    }


    @Override
    public String toString() {
        return toString(getAvailableFieldNamesSorted());
    }

    public String toString(String... fieldNames) {
        return toString(Arrays.asList(fieldNames));
    }

    public String toString(List<String> fieldNames) {
        StringBuilder sb = new StringBuilder("  - user_agent_string: '" + escapeYaml(userAgentString) + "'\n");
        int maxLength = 0;
        for (String fieldName : fieldNames) {
            maxLength = Math.max(maxLength, fieldName.length());
        }
        for (String fieldName : fieldNames) {
            if (!USERAGENT_FIELDNAME.equals(fieldName)) {
                AgentField field = allFields.get(fieldName);
                if (field != null && !field.isDefaultValue()) {
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

    public List<String> getAvailableFieldNames() {
        List<String> resultSet = new ArrayList<>(allFields.size()+10);
        if (wantedFieldNames == null) {
            resultSet.addAll(STANDARD_FIELDS);
        }

        allFields.forEach((fieldName, value) -> {
            if (!resultSet.contains(fieldName)) {
                AgentField field = allFields.get(fieldName);
                if (field != null && !field.isDefaultValue()) {
                    if (wantedFieldNames == null || wantedFieldNames.contains(fieldName)) {
                        resultSet.add(fieldName);
                    }
                }
            }
        });

        // This is not a field; this is a special operator.
        resultSet.remove(SET_ALL_FIELDS);
        return resultSet;
    }

    // We manually sort the list of fields to ensure the output is consistent.
    // Any unspecified fieldnames will be appended to the end.
    protected static final List<String> PRE_SORTED_FIELDS_LIST;

    static {
        List<String> mutablePreSortedFields = new ArrayList<>(32);

        mutablePreSortedFields.add(DEVICE_CLASS);
        mutablePreSortedFields.add(DEVICE_NAME);
        mutablePreSortedFields.add(DEVICE_BRAND);
        mutablePreSortedFields.add(DEVICE_CPU);
        mutablePreSortedFields.add(DEVICE_CPU_BITS);
        mutablePreSortedFields.add(DEVICE_FIRMWARE_VERSION);
        mutablePreSortedFields.add(DEVICE_VERSION);

        mutablePreSortedFields.add(OPERATING_SYSTEM_CLASS);
        mutablePreSortedFields.add(OPERATING_SYSTEM_NAME);
        mutablePreSortedFields.add(OPERATING_SYSTEM_VERSION);
        mutablePreSortedFields.add(OPERATING_SYSTEM_VERSION_MAJOR);
        mutablePreSortedFields.add(OPERATING_SYSTEM_NAME_VERSION);
        mutablePreSortedFields.add(OPERATING_SYSTEM_NAME_VERSION_MAJOR);
        mutablePreSortedFields.add(OPERATING_SYSTEM_VERSION_BUILD);

        mutablePreSortedFields.add(LAYOUT_ENGINE_CLASS);
        mutablePreSortedFields.add(LAYOUT_ENGINE_NAME);
        mutablePreSortedFields.add(LAYOUT_ENGINE_VERSION);
        mutablePreSortedFields.add(LAYOUT_ENGINE_VERSION_MAJOR);
        mutablePreSortedFields.add(LAYOUT_ENGINE_NAME_VERSION);
        mutablePreSortedFields.add(LAYOUT_ENGINE_NAME_VERSION_MAJOR);
        mutablePreSortedFields.add(LAYOUT_ENGINE_BUILD);

        mutablePreSortedFields.add(AGENT_CLASS);
        mutablePreSortedFields.add(AGENT_NAME);
        mutablePreSortedFields.add(AGENT_VERSION);
        mutablePreSortedFields.add(AGENT_VERSION_MAJOR);
        mutablePreSortedFields.add(AGENT_NAME_VERSION);
        mutablePreSortedFields.add(AGENT_NAME_VERSION_MAJOR);
        mutablePreSortedFields.add(AGENT_BUILD);
        mutablePreSortedFields.add(AGENT_LANGUAGE);
        mutablePreSortedFields.add(AGENT_LANGUAGE_CODE);
        mutablePreSortedFields.add(AGENT_INFORMATION_EMAIL);
        mutablePreSortedFields.add(AGENT_INFORMATION_URL);
        mutablePreSortedFields.add(AGENT_SECURITY);
        mutablePreSortedFields.add(AGENT_UUID);

        mutablePreSortedFields.add(WEBVIEW_APP_NAME);
        mutablePreSortedFields.add(WEBVIEW_APP_VERSION);
        mutablePreSortedFields.add(WEBVIEW_APP_VERSION_MAJOR);
        mutablePreSortedFields.add(WEBVIEW_APP_NAME_VERSION_MAJOR);

        mutablePreSortedFields.add(FACEBOOK_CARRIER);
        mutablePreSortedFields.add(FACEBOOK_DEVICE_CLASS);
        mutablePreSortedFields.add(FACEBOOK_DEVICE_NAME);
        mutablePreSortedFields.add(FACEBOOK_DEVICE_VERSION);
        mutablePreSortedFields.add(FACEBOOK_F_B_O_P);
        mutablePreSortedFields.add(FACEBOOK_F_B_S_S);
        mutablePreSortedFields.add(FACEBOOK_OPERATING_SYSTEM_NAME);
        mutablePreSortedFields.add(FACEBOOK_OPERATING_SYSTEM_VERSION);

        mutablePreSortedFields.add(ANONYMIZED);

        mutablePreSortedFields.add(HACKER_ATTACK_VECTOR);
        mutablePreSortedFields.add(HACKER_TOOLKIT);

        mutablePreSortedFields.add(KOBO_AFFILIATE);
        mutablePreSortedFields.add(KOBO_PLATFORM_ID);

        mutablePreSortedFields.add(IE_COMPATIBILITY_VERSION);
        mutablePreSortedFields.add(IE_COMPATIBILITY_VERSION_MAJOR);
        mutablePreSortedFields.add(IE_COMPATIBILITY_NAME_VERSION);
        mutablePreSortedFields.add(IE_COMPATIBILITY_NAME_VERSION_MAJOR);

        mutablePreSortedFields.add(SYNTAX_ERROR);

        PRE_SORTED_FIELDS_LIST = Collections.unmodifiableList(mutablePreSortedFields);
    }

    public List<String> getAvailableFieldNamesSorted() {
        List<String> fieldNames = new ArrayList<>(getAvailableFieldNames());

        List<String> result = new ArrayList<>();
        for (String fieldName : PRE_SORTED_FIELDS_LIST) {
            if (fieldNames.remove(fieldName)) {
                result.add(fieldName);
            }
        }

        Collections.sort(fieldNames);
        result.addAll(fieldNames);
        return result;

    }

}
