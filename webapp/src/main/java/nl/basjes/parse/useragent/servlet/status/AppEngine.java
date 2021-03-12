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

package nl.basjes.parse.useragent.servlet.status;

import nl.basjes.parse.useragent.servlet.api.OutputType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static nl.basjes.parse.useragent.servlet.ParseService.ensureStartedForApis;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
public class AppEngine {

    /**
     * <a href="https://cloud.google.com/appengine/docs/flexible/java/how-instances-are-managed#health_checking">
     * App Engine health checking</a> requires responding with 200 to {@code /_ah/health}.
     *
     * @return Returns a non empty message body.
     */
    @GetMapping(
        path = "/_ah/health",
        produces = TEXT_PLAIN_VALUE
    )
    public String isHealthy() {
        ensureStartedForApis(OutputType.TXT);
        return "YES";
    }

}
