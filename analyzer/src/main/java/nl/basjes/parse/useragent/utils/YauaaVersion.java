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

package nl.basjes.parse.useragent.utils;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.util.Arrays;
import java.util.List;

import static nl.basjes.parse.useragent.Version.BUILD_TIME_STAMP;
import static nl.basjes.parse.useragent.Version.COPYRIGHT;
import static nl.basjes.parse.useragent.Version.GIT_COMMIT_ID_DESCRIBE_SHORT;
import static nl.basjes.parse.useragent.Version.LICENSE;
import static nl.basjes.parse.useragent.Version.PROJECT_VERSION;
import static nl.basjes.parse.useragent.Version.URL;
import static nl.basjes.parse.useragent.utils.YamlUtils.getExactlyOneNodeTuple;
import static nl.basjes.parse.useragent.utils.YamlUtils.getKeyAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsSequenceNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.requireNodeInstanceOf;

public final class YauaaVersion {

    private static final Logger LOG = LoggerFactory.getLogger(YauaaVersion.class);

    private YauaaVersion() {
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

        LOG.info("");
        LOG.info("/-{}-\\", padding('-', width));
        logLine(version, width);
        LOG.info("+-{}-+", padding('-', width));
        for (String line : lines) {
            logLine(line, width);
        }
        if (!extraLines.isEmpty()) {
            LOG.info("+-{}-+", padding('-', width));
            for (String line : extraLines) {
                logLine(line, width);
            }
        }

        LOG.info("\\-{}-/", padding('-', width));
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
        LOG.info("| {}{} |", line, padding(' ', width - line.length()));
    }

    public static String getVersion() {
        return getVersion(PROJECT_VERSION, GIT_COMMIT_ID_DESCRIBE_SHORT, BUILD_TIME_STAMP);
    }

    public static String getVersion(String projectVersion, String gitCommitIdDescribeShort, String buildTimestamp) {
        return "Yauaa " + projectVersion + " (" + gitCommitIdDescribeShort + " @ " + buildTimestamp + ")";
    }

    public static void assertSameVersion(NodeTuple versionNodeTuple, String filename) {
        // Check the version information from the Yaml files
        SequenceNode versionNode = getValueAsSequenceNode(versionNodeTuple, filename);
        String gitCommitIdDescribeShort = null;
        String buildTimestamp = null;
        String projectVersion = null;

        List<Node> versionList = versionNode.getValue();
        for (Node versionEntry : versionList) {
            requireNodeInstanceOf(MappingNode.class, versionEntry, filename, "The entry MUST be a mapping");
            NodeTuple entry = getExactlyOneNodeTuple((MappingNode) versionEntry, filename);
            String key = getKeyAsString(entry, filename);
            String value = getValueAsString(entry, filename);
            switch (key) {
                case "git_commit_id_describe_short":
                    gitCommitIdDescribeShort = value;
                    break;
                case "build_timestamp":
                    buildTimestamp = value;
                    break;
                case "project_version":
                    projectVersion = value;
                    break;
                case "copyright":
                case "license":
                case "url":
                    // Ignore those two when comparing.
                    break;
                default:
                    throw new InvalidParserConfigurationException(
                        "Yaml config.(" + filename + ":" + versionNode.getStartMark().getLine() + "): " +
                            "Found unexpected config entry: " + key + ", allowed are " +
                            "'git_commit_id_describe_short', 'build_timestamp' and 'project_version'");
            }
        }
        assertSameVersion(gitCommitIdDescribeShort, buildTimestamp, projectVersion);
    }

    public static void assertSameVersion(String gitCommitIdDescribeShort, String buildTimestamp, String projectVersion) {
        if (GIT_COMMIT_ID_DESCRIBE_SHORT.equals(gitCommitIdDescribeShort) &&
            BUILD_TIME_STAMP.equals(buildTimestamp) &&
            PROJECT_VERSION.equals(projectVersion)) {
            return;
        }

        String libraryVersion = getVersion(PROJECT_VERSION, GIT_COMMIT_ID_DESCRIBE_SHORT, BUILD_TIME_STAMP);
        String rulesVersion = getVersion(projectVersion, gitCommitIdDescribeShort, buildTimestamp);

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
