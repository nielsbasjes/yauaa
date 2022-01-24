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

package nl.basjes.parse.useragent.servlet;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import nl.basjes.parse.useragent.Version;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI yauaaOpenAPI() {
        return new OpenAPI()
            .addServersItem(new Server().url("/"))
            .info(new Info()
                .title("Yauaa - Yet Another UserAgent Analyzer")
                .description(
                    "These basic calls allow you to retrieve the analysis output of Yauaa via a few REST interfaces.<br/>" +
                    "<br/>" +
                    "<h3><b>This MUST be treated as an <u>insecure</u> \"Proof of concept\" implementation.</b></h3>")
                .version(Version.PROJECT_VERSION)
                .license(new License().name("Apache 2.0").url("https://yauaa.basjes.nl/LICENSE.html")))
            .externalDocs(new ExternalDocumentation()
                .description("Yauaa - Yet Another UserAgent Analyzer")
                .url("https://yauaa.basjes.nl"));
    }
}
