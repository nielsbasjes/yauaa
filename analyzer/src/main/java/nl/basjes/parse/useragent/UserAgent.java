/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAgent extends UserAgentBaseListener implements Serializable, ANTLRErrorListener {

    private static final Logger LOG = LoggerFactory.getLogger(UserAgent.class);
    public static final String DEVICE_CLASS = "DeviceClass";
    public static final String DEVICE_BRAND = "DeviceBrand";
    public static final String DEVICE_NAME = "DeviceName";
    public static final String DEVICE_VERSION = "DeviceVersion";
    public static final String OPERATING_SYSTEM_CLASS = "OperatingSystemClass";
    public static final String OPERATING_SYSTEM_NAME = "OperatingSystemName";
    public static final String OPERATING_SYSTEM_VERSION = "OperatingSystemVersion";
    public static final String LAYOUT_ENGINE_CLASS = "LayoutEngineClass";
    public static final String LAYOUT_ENGINE_NAME = "LayoutEngineName";
    public static final String LAYOUT_ENGINE_VERSION = "LayoutEngineVersion";
    public static final String LAYOUT_ENGINE_VERSION_MAJOR = "LayoutEngineVersionMajor";
    public static final String AGENT_CLASS = "AgentClass";
    public static final String AGENT_NAME = "AgentName";
    public static final String AGENT_VERSION = "AgentVersion";
    public static final String AGENT_VERSION_MAJOR = "AgentVersionMajor";

    public static final String SYNTAX_ERROR = "__SyntaxError__";
    public static final String USERAGENT = "Useragent";

    public static final String SET_ALL_FIELDS = "__Set_ALL_Fields__";
    public static final String NULL_VALUE = "<<<null>>>";
    public static final String UNKNOWN_VALUE = "Unknown";
    public static final String UNKNOWN_VERSION = "??";

    public static final String[] STANDARD_FIELDS = {
        DEVICE_CLASS,
        DEVICE_BRAND,
        DEVICE_NAME,
        OPERATING_SYSTEM_CLASS,
        OPERATING_SYSTEM_NAME,
        OPERATING_SYSTEM_VERSION,
        LAYOUT_ENGINE_CLASS,
        LAYOUT_ENGINE_NAME,
        LAYOUT_ENGINE_VERSION,
        LAYOUT_ENGINE_VERSION_MAJOR,
        AGENT_CLASS,
        AGENT_NAME,
        AGENT_VERSION,
        AGENT_VERSION_MAJOR
    };

    private boolean hasSyntaxError;
    private boolean hasAmbiguity;
    private int     ambiguityCount;

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
//        allFields.put("__Ambiguity__",new AgentField("true"));
    }

    @Override
    public void reportAttemptingFullContext(
            Parser recognizer,
            DFA dfa,
            int startIndex,
            int stopIndex,
            BitSet conflictingAlts,
            ATNConfigSet configs) {
    }

    @Override
    public void reportContextSensitivity(
            Parser recognizer,
            DFA dfa,
            int startIndex,
            int stopIndex,
            int prediction,
            ATNConfigSet configs) {

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

    public class AgentField implements Serializable {
        private final String defaultValue;
        private String value;

        private long confidence;

        AgentField(String defaultValue) {
            this.defaultValue = defaultValue;
            reset();
        }

        public void reset() {
            value = defaultValue;
            confidence = -1;
        }

        public String getValue() {
            if (value == null) {
                return defaultValue;
            }
            return value;
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
                    this.value = defaultValue;
                } else {
                    this.value = newValue;
                }
                return true;
            }
            return false;
        }

        protected boolean setValueForced(String newValue, long newConfidence) {
            this.confidence = newConfidence;

            if (NULL_VALUE.equals(newValue)) {
                this.value = defaultValue;
            } else {
                this.value = newValue;
            }
            return true;
        }

        @Override
        public String toString() {
            return ">" + this.value + "#" + this.confidence + "<";
        }
    }

    private final Map<String, AgentField> allFields = new HashMap<>(32);


    public UserAgent() {
        init();
    }

    public UserAgent(String userAgentString) {
        init();
        setUserAgentString(userAgentString);
    }

    public UserAgent(UserAgent userAgent) {
        clone(userAgent);
    }

    public void clone(UserAgent userAgent) {
        init();
        setUserAgentString(userAgentString);
        for (Map.Entry<String, AgentField> entry : userAgent.allFields.entrySet()) {
            set(entry.getKey(), entry.getValue().getValue(), entry.getValue().confidence);
        }
    }

    private void init() {
        // Device : Family - Brand - Model
        allFields.put(DEVICE_CLASS,                  new AgentField(UNKNOWN_VALUE)); // Hacker / Cloud / Server / Desktop / Tablet / Phone / Watch
        allFields.put(DEVICE_BRAND,                  new AgentField(UNKNOWN_VALUE)); // (Google/AWS/Asure) / ????
        allFields.put(DEVICE_NAME,                   new AgentField(UNKNOWN_VALUE)); // (Google/AWS/Asure) / ????

        // Operating system
        allFields.put(OPERATING_SYSTEM_CLASS,        new AgentField(UNKNOWN_VALUE)); // Cloud, Desktop, Mobile, Embedded
        allFields.put(OPERATING_SYSTEM_NAME,         new AgentField(UNKNOWN_VALUE)); // ( Linux / Android / Windows ...)
        allFields.put(OPERATING_SYSTEM_VERSION,      new AgentField(UNKNOWN_VERSION)); // 1.2 / 43 / ...

        // Engine : Class (=None/Hacker/Robot/Browser) - Name - Version
        allFields.put(LAYOUT_ENGINE_CLASS,           new AgentField(UNKNOWN_VALUE)); // None / Hacker / Robot / Browser /
        allFields.put(LAYOUT_ENGINE_NAME,            new AgentField(UNKNOWN_VALUE)); // ( GoogleBot / Bing / ...) / (Trident / Gecko / ...)
        allFields.put(LAYOUT_ENGINE_VERSION,         new AgentField(UNKNOWN_VERSION)); // 1.2 / 43 / ...
        allFields.put(LAYOUT_ENGINE_VERSION_MAJOR,   new AgentField(UNKNOWN_VERSION)); // 1 / 43 / ...

        // Agent: Class (=Hacker/Robot/Browser) - Name - Version
        allFields.put(AGENT_CLASS,                   new AgentField(UNKNOWN_VALUE)); // Hacker / Robot / Browser /
        allFields.put(AGENT_NAME,                    new AgentField(UNKNOWN_VALUE)); // ( GoogleBot / Bing / ...) / ( Firefox / Chrome / ... )
        allFields.put(AGENT_VERSION,                 new AgentField(UNKNOWN_VERSION)); // 1.2 / 43 / ...
        allFields.put(AGENT_VERSION_MAJOR,           new AgentField(UNKNOWN_VERSION)); // 1 / 43 / ...
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

    static boolean isSystemField(String fieldname) {
        return  SET_ALL_FIELDS.equals(fieldname) ||
                SYNTAX_ERROR.equals(fieldname) ||
                USERAGENT.equals(fieldname);
    }

    public void processSetAll() {
        AgentField setAllField = allFields.get(SET_ALL_FIELDS);
        if (setAllField == null) {
            return;
        }
        String value = setAllField.getValue();
        Long confidence = setAllField.confidence;
        for (Map.Entry<String, AgentField> fieldEntry : allFields.entrySet()) {
            if (!isSystemField(fieldEntry.getKey())) {
                fieldEntry.getValue().setValue(value, confidence);
            }
        }
    }

    public void set(String attribute, String value, long confidence) {
        AgentField field = allFields.get(attribute);
        if (field == null) {
            field = new AgentField(null); // The fields we do not know get a 'null' default
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
            field = new AgentField(null); // The fields we do not know get a 'null' default
        }

        boolean wasEmpty = confidence == -1;
        boolean updated = field.setValueForced(value, confidence);
        if (debug && !wasEmpty) {
            if (updated) {
                LOG.info("USE  {} ({}) = {}", attribute, confidence, value);
            } else {
                LOG.info("SKIP {} ({}) = {}", attribute, confidence, value);
            }
        }
        allFields.put(attribute, field);
    }

    // The appliedMatcher parameter is needed for development and debugging.
    public void set(UserAgent newValuesUserAgent, Matcher appliedMatcher) {
        for (String fieldName : newValuesUserAgent.allFields.keySet()) {
            set(fieldName, newValuesUserAgent.allFields.get(fieldName));
        }
    }

    private void set(String fieldName, AgentField agentField) {
        set(fieldName, agentField.value, agentField.confidence);
    }

    public AgentField get(String fieldName) {
        if (USERAGENT.equals(fieldName)) {
            AgentField agentField = new AgentField(userAgentString);
            agentField.setValue(userAgentString, 0L);
            return agentField;
        } else {
            return allFields.get(fieldName);
        }
    }

    public String getValue(String fieldName) {
        if (USERAGENT.equals(fieldName)) {
            return userAgentString;
        }
        AgentField field = allFields.get(fieldName);
        if (field == null) {
            return UNKNOWN_VALUE;
        }
        return field.getValue();
    }

    public Long getConfidence(String fieldName) {
        if (USERAGENT.equals(fieldName)) {
            return 0L;
        }
        AgentField field = allFields.get(fieldName);
        if (field == null) {
            return -1L;
        }
        return field.getConfidence();
    }

    public String toYamlTestCase() {
        return toYamlTestCase(false);
    }

    public String toYamlTestCase(boolean showConfidence) {
        StringBuilder sb = new StringBuilder(10240);
        sb.append("\n");
        sb.append("- test:\n");
//        sb.append("#    options:\n");
//        sb.append("#    - 'verbose'\n");
//        sb.append("#    - 'init'\n");
//        sb.append("#    - 'only'\n");
        sb.append("    input:\n");
//        sb.append("#      name: 'You can give the test case a name'\n");
        sb.append("      user_agent_string: '").append(userAgentString).append("'\n");
        sb.append("    expected:\n");

        List<String> fieldNames = getAvailableFieldNamesSorted();

        int maxNameLength = 30;
        int maxValueLength = 0;
        for (String fieldName : allFields.keySet()) {
            maxNameLength = Math.max(maxNameLength, fieldName.length());
        }
        for (String fieldName : fieldNames) {
            maxValueLength = Math.max(maxValueLength, get(fieldName).getValue().length());
        }

        for (String fieldName : fieldNames) {
            sb.append("      ").append(fieldName);
            for (int l = fieldName.length(); l < maxNameLength + 7; l++) {
                sb.append(' ');
            }
            String value = get(fieldName).getValue();
            sb.append(": '").append(value).append('\'');
            if (showConfidence) {
                for (int l = value.length(); l < maxValueLength + 5; l++) {
                    sb.append(' ');
                }
                sb.append("# ").append(get(fieldName).confidence);
            }
            sb.append('\n');
        }
        sb.append("\n");
        sb.append("\n");

        return sb.toString();
    }


//    {
//        "agent": {
//            "user_agent_string": "Mozilla/5.0 (iPhone; CPU iPhone OS 9_2_1 like Mac OS X) AppleWebKit/601.1.46
//                                  (KHTML, like Gecko) Version/9.0 Mobile/13D15 Safari/601.1"
//            "AgentClass": "Browser",
//            "AgentName": "Safari",
//            "AgentVersion": "9.0",
//            "DeviceBrand": "Apple",
//            "DeviceClass": "Phone",
//            "DeviceFirmwareVersion": "13D15",
//            "DeviceName": "iPhone",
//            "LayoutEngineClass": "Browser",
//            "LayoutEngineName": "AppleWebKit",
//            "LayoutEngineVersion": "601.1.46",
//            "OperatingSystemClass": "Mobile",
//            "OperatingSystemName": "iOS",
//            "OperatingSystemVersion": "9_2_1",
//        }
//    }

    public String toJson() {
        List<String> fields = getAvailableFieldNames();
        fields.add("Useragent");
        return toJson(fields);
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
            if ("Useragent".equals(fieldName)) {
                sb
                    .append("\"Useragent\"")
                    .append(':')
                    .append('"').append(StringEscapeUtils.escapeJson(getUserAgentString())).append('"');
            } else {
                sb
                    .append('"').append(StringEscapeUtils.escapeJson(fieldName)).append('"')
                    .append(':')
                    .append('"').append(StringEscapeUtils.escapeJson(getValue(fieldName))).append('"');
            }
        }

        sb.append("}");
        return sb.toString();
    }


    @Override
    public String toString() {
        return toString(getAvailableFieldNamesSorted());
    }
    public String toString(List<String> fieldNames) {
        StringBuilder sb = new StringBuilder("  - user_agent_string: '\"" + userAgentString + "\"'\n");
        int maxLength = 0;
        for (String fieldName : fieldNames) {
            maxLength = Math.max(maxLength, fieldName.length());
        }
        for (String fieldName : fieldNames) {
            if (!"Useragent".equals(fieldName)) {
                AgentField field = allFields.get(fieldName);
                if (field.getValue() != null) {
                    sb.append("    ").append(fieldName);
                    for (int l = fieldName.length(); l < maxLength + 2; l++) {
                        sb.append(' ');
                    }
                    sb.append(": '").append(field.getValue()).append('\'');
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }

    public List<String> getAvailableFieldNames() {
        List<String> resultSet = new ArrayList<>(allFields.size()+10);
        resultSet.addAll(Arrays.asList(STANDARD_FIELDS));
        for (String fieldName : allFields.keySet()) {
            if (!resultSet.contains(fieldName)){
                AgentField field = allFields.get(fieldName);
                if (field != null && field.confidence >= 0 && field.getValue() != null) {
                    resultSet.add(fieldName);
                }
            }
        }

        // This is not a field; this is a special operator.
        resultSet.remove(SET_ALL_FIELDS);
        return resultSet;
    }

    // We manually sort the list of fields to ensure the output is consistent.
    // Any unspecified fieldnames will be appended to the end.
    public static final List<String> PRE_SORTED_FIELDS_LIST = new ArrayList<>(32);

    static {
        PRE_SORTED_FIELDS_LIST.add("DeviceClass");
        PRE_SORTED_FIELDS_LIST.add("DeviceName");
        PRE_SORTED_FIELDS_LIST.add("DeviceBrand");
        PRE_SORTED_FIELDS_LIST.add("DeviceCpu");
        PRE_SORTED_FIELDS_LIST.add("DeviceFirmwareVersion");
        PRE_SORTED_FIELDS_LIST.add("DeviceVersion");

        PRE_SORTED_FIELDS_LIST.add("OperatingSystemClass");
        PRE_SORTED_FIELDS_LIST.add("OperatingSystemName");
        PRE_SORTED_FIELDS_LIST.add("OperatingSystemVersion");
        PRE_SORTED_FIELDS_LIST.add("OperatingSystemNameVersion");
        PRE_SORTED_FIELDS_LIST.add("OperatingSystemVersionBuild");

        PRE_SORTED_FIELDS_LIST.add("LayoutEngineClass");
        PRE_SORTED_FIELDS_LIST.add("LayoutEngineName");
        PRE_SORTED_FIELDS_LIST.add("LayoutEngineVersion");
        PRE_SORTED_FIELDS_LIST.add("LayoutEngineVersionMajor");
        PRE_SORTED_FIELDS_LIST.add("LayoutEngineNameVersion");
        PRE_SORTED_FIELDS_LIST.add("LayoutEngineNameVersionMajor");
        PRE_SORTED_FIELDS_LIST.add("LayoutEngineBuild");

        PRE_SORTED_FIELDS_LIST.add("AgentClass");
        PRE_SORTED_FIELDS_LIST.add("AgentName");
        PRE_SORTED_FIELDS_LIST.add("AgentVersion");
        PRE_SORTED_FIELDS_LIST.add("AgentVersionMajor");
        PRE_SORTED_FIELDS_LIST.add("AgentNameVersion");
        PRE_SORTED_FIELDS_LIST.add("AgentNameVersionMajor");
        PRE_SORTED_FIELDS_LIST.add("AgentBuild");
        PRE_SORTED_FIELDS_LIST.add("AgentLanguage");
        PRE_SORTED_FIELDS_LIST.add("AgentLanguageCode");
        PRE_SORTED_FIELDS_LIST.add("AgentInformationEmail");
        PRE_SORTED_FIELDS_LIST.add("AgentInformationUrl");
        PRE_SORTED_FIELDS_LIST.add("AgentSecurity");
        PRE_SORTED_FIELDS_LIST.add("AgentUuid");

        PRE_SORTED_FIELDS_LIST.add("FacebookCarrier");
        PRE_SORTED_FIELDS_LIST.add("FacebookDeviceClass");
        PRE_SORTED_FIELDS_LIST.add("FacebookDeviceName");
        PRE_SORTED_FIELDS_LIST.add("FacebookDeviceVersion");
        PRE_SORTED_FIELDS_LIST.add("FacebookFBOP");
        PRE_SORTED_FIELDS_LIST.add("FacebookFBSS");
        PRE_SORTED_FIELDS_LIST.add("FacebookOperatingSystemName");
        PRE_SORTED_FIELDS_LIST.add("FacebookOperatingSystemVersion");

        PRE_SORTED_FIELDS_LIST.add("Anonymized");

        PRE_SORTED_FIELDS_LIST.add("HackerAttackVector");
        PRE_SORTED_FIELDS_LIST.add("HackerToolkit");

        PRE_SORTED_FIELDS_LIST.add("KoboAffiliate");
        PRE_SORTED_FIELDS_LIST.add("KoboPlatformId");

        PRE_SORTED_FIELDS_LIST.add("IECompatibilityVersion");
        PRE_SORTED_FIELDS_LIST.add("IECompatibilityVersionMajor");
        PRE_SORTED_FIELDS_LIST.add("IECompatibilityNameVersion");
        PRE_SORTED_FIELDS_LIST.add("IECompatibilityNameVersionMajor");

        PRE_SORTED_FIELDS_LIST.add(SYNTAX_ERROR);
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
