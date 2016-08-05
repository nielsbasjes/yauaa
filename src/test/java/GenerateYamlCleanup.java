/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class GenerateYamlCleanup {

    private static final String indent1 = "";
    private static final String indent2 = "  ";
    private static final String indent3 = "    ";
    private static final String indent4 = "      ";

    @Test
    @Ignore
    public void generateYamlCleanupScript() {
        UserAgentAnalyzer uaa = new UserAgentAnalyzer();
        List<String> fieldNames = uaa.getAllPossibleFieldNamesSorted();
//        sed -i 's@^  *AgentBuild  *: *@        AgentClass                         : @' ../main/resources/UserAgents/*.yaml

        StringBuilder sb = new StringBuilder(2048);
        sb.append("\n");
        sb.append("\n");

        int maxNameLength = 0;
        for (String fieldName : fieldNames) {
            maxNameLength = Math.max(maxNameLength, fieldName.length());
        }

        for (String fieldName : fieldNames) {
            sb.append("sed -i 's@^  *")
                .append(fieldName)
                .append(" *: *@")
                .append("        ")
                .append(fieldName);
            for (int l = fieldName.length(); l < maxNameLength + 7; l++) {
                sb.append(' ');
            }
            sb.append(": @' *.yaml\n");

////            - 'DeviceClass            :  2014:"Desktop"'
//            sb.append("sed -i 's@")
//                .append("^  *\"\\([a-zA-Z]+\\) +:\\( *[0-9]+\\) *:\\(.*\\)$")
//                .append("@")
//                .append(indent4).append("\"\\1");
//            for (int l = fieldName.length(); l < maxNameLength + 5; l++) {
//                sb.append(' ');
//            }
//            sb.append(":  \\2:\\3");
//            sb.append("@' *.yaml\n");

        }
        sb.append("sed -i \"s@^  *- '@").append(indent3).append("- '@\" *.yaml\n");
        addEntry(sb, "- test:", indent1);
        addEntry(sb, "input:", indent3);
        addEntry(sb, "user_agent_string:", indent4);
        addEntry(sb, "expected:", indent3);
        addEntry(sb, "name:", indent4);
        addEntry(sb, "- options:", indent3);
        addEntry(sb, "- matcher:", indent1);
        addEntry(sb, "options:", indent3);
        addEntry(sb, "require:", indent3);
        addEntry(sb, "extract:", indent3);
        addEntry(sb, "- lookup:", indent1);
        addEntry(sb, "map:", indent4);
        sb.append("sed -i 's@^  *- \"@").append(indent3).append("- \"@' *.yaml\n");
        sb.append("\n");
        sb.append("\n");

        System.out.println(sb);
    }

    private void addEntry(StringBuilder sb, String name, String indent) {
        sb.append("sed -i 's@^  *").append(name).append("@").append(indent).append(name).append("@' *.yaml\n");
    }

}
