/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

import io.swagger.annotations.ApiOperation;
import nl.basjes.parse.useragent.Version;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.ArrayList;
import java.util.Collections;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static springfox.documentation.builders.RequestHandlerSelectors.withMethodAnnotation;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {
    @Bean
    public Docket api() {

        final ArrayList<ResponseMessage> responseMessages = new ArrayList<>();
        responseMessages.add(new ResponseMessageBuilder()
            .code(200)
            .message("Successfully parsed the provided input")
            .build());
        responseMessages.add(new ResponseMessageBuilder()
            .code(503)
            .message("Internal error, or Yauaa is currently still busy starting up.")
            .build());

        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("yauaa-v1")
            .select()
            .apis(withMethodAnnotation(ApiOperation.class))
            .build()
            .globalResponseMessage(GET, responseMessages)
            .globalResponseMessage(POST, responseMessages)
            .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
            "Yauaa - Yet Another UserAgent Analyzer",
            "These basic calls allow you to retrieve the analysis output of Yauaa via a few REST interfaces.<br/>" +
            "<br/>" +
            "<h1><b>This MUST be treated as an <u>insecure</u> \"Proof of concept\" implementation.</b></h1>",
            Version.PROJECT_VERSION,
            null,
            new Contact("Yauaa - Yet Another UserAgent Analyzer", "https://yauaa.basjes.nl", null),
            null,
            null,
            Collections.emptyList());
    }
}
