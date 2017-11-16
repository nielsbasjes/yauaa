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
        String testUa = "Mozilla/5.0 (Linux; Android 4.4.4; 2014811 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 YaBrowser/17.6.1.345.00 Mobile Safari/537.36";
        System.out.println(createSQL(testUa));
        */
        
        readFileAndCreateSQL("/tmp/uas.txt", "/tmp/uas.sql");
    }
    
    private static void analyzeAndPrint(String uaString) {
        
        System.out.println("Analyzing...\n");
        UserAgent ua = USER_AGENT_ANALYZER.parse(uaString);
        
        for (String key : ua.getAvailableFieldNamesSorted())
            System.out.println(key + ": " + ua.getValue(key));
        
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
