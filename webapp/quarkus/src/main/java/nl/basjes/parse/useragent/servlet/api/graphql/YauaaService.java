///*
// * Yet Another UserAgent Analyzer
// * Copyright (C) 2013-2022 Niels Basjes
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package nl.basjes.parse.useragent.servlet.api.graphql;
//
//import nl.basjes.parse.useragent.UserAgent;
//import nl.basjes.parse.useragent.UserAgentAnalyzer;
//import nl.basjes.parse.useragent.utils.YauaaVersion;
//import org.jboss.logging.Logger;
//
//import javax.enterprise.context.ApplicationScoped;
//import java.util.Map;
//
//@ApplicationScoped
//public class YauaaService {
//    private UserAgentAnalyzer userAgentAnalyzer;
//
//    Logger LOG = Logger.getLogger(YauaaService.class);
//
//    public YauaaService() {
//        LOG.info("Yauaa: Starting " + YauaaVersion.getVersion());
//        try {
//            userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
//                .immediateInitialization()
//                .addOptionalResources("file:UserAgents*/*.yaml")
//                .addOptionalResources("classpath*:UserAgents-*/*.yaml")
//                .build();
//        } catch (Exception e) {
//            LOG.error("Yauaa: Exception: " + e);
//            throw e;
//        } catch (Error e) {
//            LOG.error("Yauaa: Error: " + e);
//            throw e;
//        }
//    }
//
//    public UserAgent parse(Map<String, String> requestHeaders) {
//        return userAgentAnalyzer.parse(requestHeaders);
//    }
//}
