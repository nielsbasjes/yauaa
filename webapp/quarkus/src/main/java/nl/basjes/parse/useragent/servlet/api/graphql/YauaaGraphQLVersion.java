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

package nl.basjes.parse.useragent.servlet.api.graphql;

import nl.basjes.parse.useragent.Version;

public class YauaaGraphQLVersion {

    private static final YauaaGraphQLVersion INSTANCE;

    static {
        INSTANCE = new YauaaGraphQLVersion();
    }

    public static YauaaGraphQLVersion getInstance() {
        return INSTANCE;
    }

    public final String gitCommitId              = Version.GIT_COMMIT_ID;
    public final String gitCommitIdDescribeShort = Version.GIT_COMMIT_ID_DESCRIBE_SHORT;
    public final String buildTimeStamp           = Version.BUILD_TIME_STAMP;
    public final String projectVersion           = Version.PROJECT_VERSION;
    public final String copyright                = Version.COPYRIGHT;
    public final String license                  = Version.LICENSE;
    public final String url                      = Version.URL;
    public final String buildJDKVersion          = Version.BUILD_JDK_VERSION;
    public final String targetJREVersion         = Version.TARGET_JRE_VERSION;
}
