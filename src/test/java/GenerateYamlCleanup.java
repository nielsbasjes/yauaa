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

    @Test
    @Ignore
    public void generateYamlCleanupScript() {
        UserAgentAnalyzer uaa = new UserAgentAnalyzer();
        List<String> fieldNames = uaa.getAllPossibleFieldNamesSorted();
//        sed -i 's@^  *AgentBuild  *: *@        AgentClass                         : @' ../main/resources/UserAgents/*.yaml

        StringBuilder sb = new StringBuilder(2048);

        int maxNameLength = 0;
        for (String fieldName : fieldNames) {
            maxNameLength = Math.max(maxNameLength, fieldName.length());
        }

        for (String fieldName : fieldNames) {
            sb  .append("sed -i 's@^  *")
                .append(fieldName)
                .append(" *: *@")
                .append("        ")
                .append(fieldName);
            for (int l = fieldName.length(); l < maxNameLength + 5; l++) {
                sb.append(' ');
            }
            sb.append(": @' *.yaml\n");
        }
        sb.append("\n");
        sb.append("\n");

        System.out.println(sb);
    }
}
