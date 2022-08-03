/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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

package nl.basjes.parse.useragent.servlet.graphql;

import lombok.Getter;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class YauaaVersion {

//    @Query("version")
//    @Description("Get the version information of the Yauaa engine that is used")
    @QueryMapping
    public Version version() {
        return new Version();
    }

    public static final class Version {
        /* @Description("The git commit id of the Yauaa engine that is used")       */  @Getter private final String gitCommitId =              nl.basjes.parse.useragent.Version.GIT_COMMIT_ID;
        /* @Description("The git describe short of the Yauaa engine that is used")  */  @Getter private final String gitCommitIdDescribeShort = nl.basjes.parse.useragent.Version.GIT_COMMIT_ID_DESCRIBE_SHORT;
        /* @Description("Timestamp when the engine was built.")                     */  @Getter private final String buildTimeStamp =           nl.basjes.parse.useragent.Version.BUILD_TIME_STAMP;
        /* @Description("Version of the yauaa engine")                              */  @Getter private final String projectVersion =           nl.basjes.parse.useragent.Version.PROJECT_VERSION;
        /* @Description("Copyright notice of the Yauaa engine that is used")        */  @Getter private final String copyright =                nl.basjes.parse.useragent.Version.COPYRIGHT;
        /* @Description("The software license Yauaa engine that is used")           */  @Getter private final String license =                  nl.basjes.parse.useragent.Version.LICENSE;
        /* @Description("Project url")                                              */  @Getter private final String url =                      nl.basjes.parse.useragent.Version.URL;
        /* @Description("Yauaa was build using this JDK version")                   */  @Getter private final String buildJDKVersion =          nl.basjes.parse.useragent.Version.BUILD_JDK_VERSION;
        /* @Description("Yauaa was build using for this target JRE version")        */  @Getter private final String targetJREVersion =         nl.basjes.parse.useragent.Version.TARGET_JRE_VERSION;
    }
}
