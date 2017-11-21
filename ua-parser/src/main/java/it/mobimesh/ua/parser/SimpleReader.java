/*
 * UserAgent Parser (Campanion)
 * Copyright (C) 2017 Davide Magni
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
package it.mobimesh.ua.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

public class SimpleReader {
    
    private static final UserAgentAnalyzer USER_AGENT_ANALYZER = new UserAgentAnalyzer();
    
    public static void main(String[] args) {
        
        /*
        String testUa = "Mozilla/5.0 (Linux; U; Android 6.0; zh-CN; CAM-TL00H Build/HONORCAM-TL00H) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/40.0.2214.89 UCBrowser/11.6.6.951 Mobile Safari/537.36";
        analyzeAndPrint(testUa);
        if (true) return;
        */
        
        readFileAndCreateSQL("/tmp/uas.txt", "/tmp/uas.sql");
    }
    
    private static void analyzeAndPrint(String uaString) {
        
        System.out.println("Analyzing...\n");
        UserAgent ua = USER_AGENT_ANALYZER.parse(uaString);
        
        for (String key : ua.getAvailableFieldNamesSorted())
            System.out.println(key + ": \"" + ua.getValue(key) + "\"");
        
        System.out.println("\n Done '" + ua.getUserAgentString() + "'.");
    }
    
    private static void readFileAndCreateSQL(String inputPath, String outputPath) {
        
        try {
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
            bw.write("");
            
            try (BufferedReader br = new BufferedReader(new FileReader(inputPath))) {

                String line;
                
                System.out.println("Working");
                int i = 0;
                
                while ((line = br.readLine()) != null) {
                    
                    bw.append(createSQL(line));
                    System.out.print(".");
                    
                    if (++i % 100 == 0) {
                        System.out.println(" " + i);
                    }
                }
                
                System.out.println(" " + i);
                System.out.println("\nDone!");
            }
            
            bw.flush();
            bw.close();
        }
        catch(Exception ex) {}        
    }
    
    private static String createSQL(String uaString) {
        
        UserAgent ua = USER_AGENT_ANALYZER.parse(uaString);
        
        String res = "UPDATE useragent SET ";
        
        res += innerSQL("AgentClass",             ua, "Classification",   null);
        res += innerSQL("AgentName",              ua, "Browser",          ", ");
        res += innerSQL("AgentVersion",           ua, "BrowserVersion",   ", ");
        res += innerSQL("OperatingSystemName",    ua, "OSName",           ", ");
        res += innerSQL("OperatingSystemVersion", ua, "OSVersion",        ", ");
        res += innerSQL("DeviceClass",            ua, "DeviceClass",      ", ");
        res += innerSQL("DeviceBrand",            ua, "DeviceBrand",      ", ");
        res += innerSQL("DeviceName",             ua, "DeviceName",       ", ");
        
        res += " WHERE UAString = '" + escapeQuotes(uaString) + "';\n";
        
        return res;
    }
    
    private static String innerSQL(String key, UserAgent ua, String column, String prefix) {
        
        String val = ua.getValue(key);
        
        if (val != null && !val.isEmpty())
            return (prefix != null ? prefix : "") + column + " = '" + escapeQuotes(val) + "'";
        else
            return (prefix != null ? prefix : "") + column + " = NULL";
    }
    
    private static String escapeQuotes(String input) {
        
        if (input == null)
            return null;
        
        return input.replaceAll("'", "''");
    }
}
