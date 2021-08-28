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

package nl.basjes.parse.useragent.utils;

import nl.basjes.parse.useragent.Version;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static nl.basjes.parse.useragent.Version.COPYRIGHT;
import static nl.basjes.parse.useragent.Version.LICENSE;
import static nl.basjes.parse.useragent.Version.URL;
import static nl.basjes.parse.useragent.utils.YamlUtils.getExactlyOneNodeTuple;
import static nl.basjes.parse.useragent.utils.YamlUtils.getKeyAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsSequenceNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.requireNodeInstanceOf;

public final class YauaaVersion {

    private static final Logger LOG = LogManager.getLogger(YauaaVersion.class);

    private YauaaVersion() {
    }

    public abstract static class AbstractVersion {
        public abstract String getGitCommitId();
        public abstract String getGitCommitIdDescribeShort();
        public abstract String getBuildTimeStamp();
        public abstract String getProjectVersion();
        public abstract String getCopyright();
        public abstract String getLicense();
        public abstract String getUrl();
        public abstract String getBuildJDKVersion();
        public abstract String getTargetJREVersion();

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AbstractVersion)) {
                return false;
            }
            AbstractVersion version = (AbstractVersion) o;
            return
                getGitCommitId()              .equals(version.getGitCommitId()) &&
                getGitCommitIdDescribeShort() .equals(version.getGitCommitIdDescribeShort()) &&
                getBuildTimeStamp()           .equals(version.getBuildTimeStamp()) &&
                getProjectVersion()           .equals(version.getProjectVersion()) &&
                getCopyright()                .equals(version.getCopyright()) &&
                getLicense()                  .equals(version.getLicense()) &&
                getUrl()                      .equals(version.getUrl()) &&
                getBuildJDKVersion()          .equals(version.getBuildJDKVersion()) &&
                getTargetJREVersion()         .equals(version.getTargetJREVersion());
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                getGitCommitId(),
                getGitCommitIdDescribeShort(),
                getBuildTimeStamp(),
                getProjectVersion(),
                getCopyright(),
                getLicense(),
                getUrl(),
                getBuildJDKVersion(),
                getTargetJREVersion());
        }

        @Override
        public String toString() {
            return "Yauaa " + getProjectVersion() +
                " (" + getGitCommitIdDescribeShort() + " @ " + getBuildTimeStamp() +
                " [JDK:"+getBuildJDKVersion()+";JRE:"+getTargetJREVersion()+"])";
        }
    }

    public static void logVersion(String... extraLines) {
        logVersion(Arrays.asList(extraLines));
    }

    public static void logVersion(List<String> extraLines) {
        String[] lines = {
            "For more information: " + URL,
            COPYRIGHT + " - " + LICENSE
        };
        String version = getVersion();
        int width = version.length();
        for (String line : lines) {
            width = Math.max(width, line.length());
        }
        for (String line : extraLines) {
            width = Math.max(width, line.length());
        }

        String padding = padding('-', width);

        LOG.info("");
        LOG.info("/-{}-\\", padding);
        logLine(version, width);
        LOG.info("+-{}-+", padding);
        for (String line : lines) {
            logLine(line, width);
        }
        if (!extraLines.isEmpty()) {
            LOG.info("+-{}-+", padding);
            for (String line : extraLines) {
                logLine(line, width);
            }
        }

        LOG.info("\\-{}-/", padding);
        LOG.info("");
    }

    private static String padding(char letter, int count) {
        StringBuilder sb = new StringBuilder(128);
        for (int i = 0; i < count; i++) {
            sb.append(letter);
        }
        return sb.toString();
    }

    private static void logLine(String line, int width) {
        if (LOG.isInfoEnabled()) {
            LOG.info("| {}{} |", line, padding(' ', width - line.length()));
        }
    }

    public static String getVersion() {
        return getVersion(Version.getInstance());
    }

    public static String getVersion(AbstractVersion version) {
        return getVersion(version.getProjectVersion(), version.getGitCommitIdDescribeShort(), version.getBuildTimeStamp());
    }

    public static String getVersion(String projectVersion, String gitCommitIdDescribeShort, String buildTimestamp) {
        return "Yauaa " + projectVersion + " (" + gitCommitIdDescribeShort + " @ " + buildTimestamp + ")";
    }

    public static String getVersion(String projectVersion, String gitCommitIdDescribeShort, String buildTimestamp, String buildJDKVersion, String targetJREVersion) {
        return "Yauaa " + projectVersion + " (" + gitCommitIdDescribeShort + " @ " + buildTimestamp + " [JDK:"+buildJDKVersion+";JRE:"+targetJREVersion+"])";
    }

    private static final class RulesVersion extends AbstractVersion {
        private String gitCommitId              = "<undefined>";
        private String gitCommitIdDescribeShort = "<undefined>";
        private String buildTimeStamp           = "<undefined>";
        private String projectVersion           = "<undefined>";
        private String copyright                = "<undefined>";
        private String license                  = "<undefined>";
        private String url                      = "<undefined>";
        private String buildJDKVersion          = "<undefined>";
        private String targetJREVersion         = "<undefined>";

        @Override
        public String getGitCommitId() {
            return gitCommitId;
        }

        @Override
        public String getGitCommitIdDescribeShort() {
            return gitCommitIdDescribeShort;
        }

        @Override
        public String getBuildTimeStamp() {
            return buildTimeStamp;
        }

        @Override
        public String getProjectVersion() {
            return projectVersion;
        }

        @Override
        public String getCopyright() {
            return copyright;
        }

        @Override
        public String getLicense() {
            return license;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getBuildJDKVersion() {
            return buildJDKVersion;
        }

        @Override
        public String getTargetJREVersion() {
            return targetJREVersion;
        }

        RulesVersion(NodeTuple versionNodeTuple, String filename) {
            // Check the version information from the Yaml files
            SequenceNode versionNode = getValueAsSequenceNode(versionNodeTuple, filename);

            List<Node> versionList = versionNode.getValue();
            for (Node versionEntry : versionList) {
                requireNodeInstanceOf(MappingNode.class, versionEntry, filename, "The entry MUST be a mapping");
                NodeTuple entry = getExactlyOneNodeTuple((MappingNode) versionEntry, filename);
                String key = getKeyAsString(entry, filename);
                String value = getValueAsString(entry, filename);
                switch (key) {
                    case "git_commit_id":
                        gitCommitId = value;
                        break;
                    case "git_commit_id_describe_short":
                        gitCommitIdDescribeShort = value;
                        break;
                    case "build_timestamp":
                        buildTimeStamp = value;
                        break;
                    case "project_version":
                        projectVersion = value;
                        break;
                    case "license":
                        license = value;
                        break;
                    case "copyright":
                        copyright = value;
                        break;
                    case "url":
                        url = value;
                        break;
                    case "buildJDKVersion":
                        buildJDKVersion = value;
                        break;
                    case "targetJREVersion":
                        targetJREVersion = value;
                        break;
                    default:
                        throw new InvalidParserConfigurationException(
                            "Yaml config.(" + filename + ":" + versionNode.getStartMark().getLine() + "): " +
                                "Found unexpected config entry: " + key + ", allowed are " +
                                "'git_commit_id_describe_short', 'build_timestamp' and 'project_version'");
                }
            }
        }
    }

    public static void assertSameVersion(NodeTuple versionNodeTuple, String filename) {
        RulesVersion rulesVersion = new RulesVersion(versionNodeTuple, filename);
        assertSameVersion(Version.getInstance(), rulesVersion);
    }

    public static void assertSameVersion(AbstractVersion libraryVersion, AbstractVersion rulesVersion) {
        if (libraryVersion.equals(rulesVersion)) {
            return;
        }
        LOG.error("===============================================");
        LOG.error("==========        FATAL ERROR       ===========");
        LOG.error("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
        LOG.error("");
        LOG.error("Two different Yauaa versions have been loaded:");
        LOG.error("Runtime Library: {}", libraryVersion);
        LOG.error("Rule sets      : {}", rulesVersion);
        LOG.error("");
        LOG.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        LOG.error("===============================================");

        throw new InvalidParserConfigurationException("Two different Yauaa versions have been loaded: \n" +
            "Runtime Library: " + libraryVersion + "\n" +
            "Rule sets      : " + rulesVersion + "\n");
    }

}
