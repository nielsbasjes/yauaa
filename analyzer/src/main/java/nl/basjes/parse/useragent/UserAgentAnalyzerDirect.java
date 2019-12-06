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

package nl.basjes.parse.useragent;

import com.esotericsoftware.kryo.DefaultSerializer;

@DefaultSerializer(UserAgentAnalyzerDirect.KryoSerializer.class)
public final class UserAgentAnalyzerDirect extends AbstractUserAgentAnalyzerDirect {

    public static UserAgentAnalyzerDirectBuilder newBuilder() {
        return new UserAgentAnalyzerDirectBuilder(new UserAgentAnalyzerDirect());
    }

    public static final class UserAgentAnalyzerDirectBuilder extends AbstractUserAgentAnalyzerDirectBuilder<UserAgentAnalyzerDirect, UserAgentAnalyzerDirectBuilder> {

        private UserAgentAnalyzerDirectBuilder(UserAgentAnalyzerDirect newUaa) {
            super(newUaa);
        }
    }
}
