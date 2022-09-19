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

import io.restassured.response.ValidatableResponse;
import nl.basjes.parse.useragent.Version;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

abstract class AbstractParseServletGraphQlTests extends AbstractTestingBase {

    private ValidatableResponse getGraphQLResponse(String body) {
        return
            given()
                .port(getPort())
                .header("User-Agent", NONSENSE_USER_AGENT)
                .accept(JSON)
                .contentType(JSON)
                .body("{\"query\":\""+ StringEscapeUtils.escapeJson(body)+"\"}")
            .when()
                .post("/graphql")
            .then();
    }

    // ------------------------------------------
    @Test
    public void testVersion() {
        LOG.info("Testing GraphQL: version");
        getGraphQLResponse(
            "{" +
            "  version {" +
            "    projectVersion" +
            "    url" +
            "    buildJDKVersion" +
            "    targetJREVersion" +
            "  }" +
            "}")
            .statusCode(200)
            .contentType(JSON)
            .body("data.version.projectVersion", equalTo(Version.PROJECT_VERSION))
            .body("data.version.url", equalTo(Version.URL))
            .body("data.version.buildJDKVersion", equalTo(Version.BUILD_JDK_VERSION))
            .body("data.version.targetJREVersion", equalTo(Version.TARGET_JRE_VERSION));
    }

    @Test
    public void testAnalyzeDirectField() {
        LOG.info("Testing GraphQL: direct field");
        getGraphQLResponse(
            "{" +
            "  analyze(requestHeaders: {userAgent: \""+TEST_USER_AGENT+"\"}) {" +
            "    agentNameVersion" +
            "  }" +
            "}")
            .statusCode(200)
            .contentType(JSON)
            .body("data.analyze.agentNameVersion",   equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    public void testAnalyzeField() {
        LOG.info("Testing GraphQL: single field");
        getGraphQLResponse(
                "{" +
                "  analyze(requestHeaders: {userAgent: \""+TEST_USER_AGENT+"\"}) {" +
                "    field(fieldName: \"AgentNameVersion\") {" +
                "      fieldName" +
                "      value" +
                "    }" +
                "    AgentNameVersion:field(fieldName: \"AgentNameVersion\") {" +
                "      fieldName" +
                "      value" +
                "    }" +
                "  }" +
                "}")
            .statusCode(200)
            .contentType(JSON)
            .body("data.analyze.field.fieldName",               equalTo("AgentNameVersion"))
            .body("data.analyze.field.value",                   equalTo(EXPECTED_AGENTNAMEVERSION))
            .body("data.analyze.AgentNameVersion.fieldName",    equalTo("AgentNameVersion"))
            .body("data.analyze.AgentNameVersion.value",        equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    public void testAnalyzeFields() {
        LOG.info("Testing GraphQL: array fields");
        getGraphQLResponse(
            "{" +
            "  analyze(requestHeaders: {userAgent: \""+TEST_USER_AGENT+"\"}) {" +
            "    fields(fieldNames: [\"AgentNameVersion\"]) {" +
            "      fieldName" +
            "      value" +
            "    }" +
            "  }" +
            "}")
            .statusCode(200)
            .contentType(JSON)
            .body("data.analyze.fields[0].fieldName",        equalTo("AgentNameVersion"))
            .body("data.analyze.fields[0].value",            equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    public void testCheckCustomConfigWasLoaded() {
        assumeTrue(runTestsThatNeedResourceFiles, "Some integration tests cannot use test resources");

        LOG.info("Testing GraphQL: Custom resources");
        getGraphQLResponse(
            "{" +
            "    analyze(requestHeaders: {" +
            "        userAgent: \"TestApplication/1.2.3 (node123.datacenter.example.nl; 1234; d71922715c2bfe29343644b14a4731bf5690e66e)\"" +
            "    }) {" +
            "        ApplicationName:       field(fieldName: \"ApplicationName\")      { value }" +
            "        ApplicationVersion:    field(fieldName: \"ApplicationVersion\")   { value }" +
            "        ApplicationInstance:   field(fieldName: \"ApplicationInstance\")  { value }" +
            "        ApplicationGitCommit:  field(fieldName: \"ApplicationGitCommit\") { value }" +
            "        ServerName:            field(fieldName: \"ServerName\")           { value }" +
            "    }" +
            "}")
            .statusCode(200)
            .contentType(JSON)
            .body("data.analyze.ApplicationName.value",       equalTo("TestApplication"))
            .body("data.analyze.ApplicationVersion.value",    equalTo("1.2.3"))
            .body("data.analyze.ServerName.value",            equalTo("node123.datacenter.example.nl"))
            .body("data.analyze.ApplicationInstance.value",   equalTo("1234"))
            .body("data.analyze.ApplicationGitCommit.value",  equalTo("d71922715c2bfe29343644b14a4731bf5690e66e"));
    }
}
