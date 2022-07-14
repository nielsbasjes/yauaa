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

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.core.AnyOf;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.ANY;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.ContentType.XML;
import static nl.basjes.parse.useragent.servlet.YamlToJsonFilter.ORIGINAL_CONTENT_TYPE_HEADER;
import static nl.basjes.parse.useragent.servlet.utils.Constants.TEXT_XYAML_VALUE;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class ParseServiceTest {

    private static final String BASEURL                     = "/yauaa/v1";
    private static final String NONSENSE_USER_AGENT         = "Niels Basjes/42";
    private static final String TEST_USER_AGENT             = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36";
    private static final String EXPECTED_AGENTNAMEVERSION   = "Chrome 84.0.4147.89";

    // ------------------------------------------
    // JSON
    @Test
    void testGetJsonUrl() {
        given()
            .header("User-Agent", TEST_USER_AGENT)
            .accept(ANY)
            .when()
                .get(BASEURL+"/analyze/json")
            .then()
                .statusCode(200)
                .contentType(JSON)
                .body("[0].AgentNameVersion", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    void testGetJsonHeader() {
        given()
            .header("User-Agent", TEST_USER_AGENT)
            .accept(JSON)
            .when()
                .get(BASEURL+"/analyze")
            .then()
                .statusCode(200)
                .contentType(JSON)
                .body("[0].AgentNameVersion", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    void testPostJsonUrl() {
        given()
            .header("User-Agent", NONSENSE_USER_AGENT)
            .accept(ANY)
            .body(TEST_USER_AGENT)
            .when()
                .post(BASEURL+"/analyze/json")
            .then()
                .statusCode(200)
                .contentType(JSON)
                .body("[0].AgentNameVersion", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    void testPostJsonHeader() {
        given()
            .header("User-Agent", NONSENSE_USER_AGENT)
            .accept(JSON)
            .body(TEST_USER_AGENT)
            .when()
                .post(BASEURL+"/analyze")
            .then()
                .statusCode(200)
                .contentType(JSON)
                .body("[0].AgentNameVersion", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    // ------------------------------------------
    // XML
    @Test
    void testGetXmlUrl() {
        given()
            .header("User-Agent", TEST_USER_AGENT)
            .accept(ANY)
            .when()
                .get(BASEURL+"/analyze/xml")
            .then()
                .statusCode(200)
                .contentType(XML)
                .body("Yauaa.AgentNameVersion", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    void testGetXmlHeader() {
        given()
            .header("User-Agent", TEST_USER_AGENT)
            .accept(XML)
            .when()
                .get(BASEURL+"/analyze")
            .then()
                .statusCode(200)
                .contentType(XML)
                .body("Yauaa.AgentNameVersion", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    void testPostXmlUrl() {
        given()
            .header("User-Agent", NONSENSE_USER_AGENT)
            .accept(ANY)
            .body(TEST_USER_AGENT)
            .when()
                .post(BASEURL+"/analyze/xml")
            .then()
                .statusCode(200)
                .contentType(XML)
                .body("Yauaa.AgentNameVersion", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    void testPostXmlHeader() {
        given()
            .header("User-Agent", NONSENSE_USER_AGENT)
            .accept(XML)
            .body(TEST_USER_AGENT)
            .when()
                .post(BASEURL+"/analyze")
            .then()
                .statusCode(200)
                .contentType(XML)
                .body("Yauaa.AgentNameVersion", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    // ------------------------------------------
    // YAML
    private AnyOf<String> isYamlContentType = anyOf(equalTo(TEXT_XYAML_VALUE), equalTo(TEXT_XYAML_VALUE + ";charset=UTF-8"));

    @Test
    void testGetYamlUrl() {
        given()
            .filter(new YamlToJsonFilter())
            .header("User-Agent", TEST_USER_AGENT)
            .accept(ANY)
            .when()
                .get(BASEURL+"/analyze/yaml")
            .then()
                .statusCode(200)
                .header(ORIGINAL_CONTENT_TYPE_HEADER, isYamlContentType)
//                .contentType(TEXT_XYAML_VALUE) --> Was filtered into JSON
                .body("test.expected.AgentNameVersion[0]", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    void testGetYamlHeader() {
        given()
            .filter(new YamlToJsonFilter())
            .header("User-Agent", TEST_USER_AGENT)
            .accept(TEXT_XYAML_VALUE)
            .when()
                .get(BASEURL+"/analyze")
            .then()
                .statusCode(200)
                .header(ORIGINAL_CONTENT_TYPE_HEADER, isYamlContentType)
//                .contentType(TEXT_XYAML_VALUE) --> Was filtered into JSON
                .body("test.expected.AgentNameVersion[0]", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    void testPostYamlUrl() {
        given()
            .filter(new YamlToJsonFilter())
            .header("User-Agent", NONSENSE_USER_AGENT)
            .accept(ANY)
            .body(TEST_USER_AGENT)
            .when()
                .post(BASEURL+"/analyze/yaml")
            .then()
                .statusCode(200)
                .header(ORIGINAL_CONTENT_TYPE_HEADER, isYamlContentType)
//                .contentType(TEXT_XYAML_VALUE) --> Was filtered into JSON
                .body("test.expected.AgentNameVersion[0]", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    @Test
    void testPostYamlHeader() {
        given()
            .filter(new YamlToJsonFilter())
            .header("User-Agent", NONSENSE_USER_AGENT)
            .accept(TEXT_XYAML_VALUE)
            .body(TEST_USER_AGENT)
            .when()
                .post(BASEURL+"/analyze")
            .then()
                .statusCode(200)
                .header(ORIGINAL_CONTENT_TYPE_HEADER, isYamlContentType)
//                .contentType(TEXT_XYAML_VALUE) --> Was filtered into JSON
                .body("test.expected.AgentNameVersion[0]", equalTo(EXPECTED_AGENTNAMEVERSION));
    }

    // ------------------------------------------

// FIXME: This test failes in the IT

//    @Test
//    void testCheckCustomConfigWasLoaded() {
//        Response response =
//            given()
//                .header("User-Agent", "TestApplication/1.2.3 (node123.datacenter.example.nl; 1234; d71922715c2bfe29343644b14a4731bf5690e66e)")
//            .when()
//                .get(BASEURL + "/analyze/json")
//            .then()
//                .statusCode(200)
//                .contentType(JSON)
//                .body("[0].AgentNameVersion", equalTo("TestApplication 1.2.3"))
//            .extract()
//                .response();
//        JsonPath jsonPath = response.jsonPath();
//
//        assertEquals("TestApplication",                              jsonPath.get("[0].ApplicationName"),       "The custom config was not applied.");
//        assertEquals("1.2.3",                                        jsonPath.get("[0].ApplicationVersion"),    "The custom config was not applied.");
//        assertEquals("node123.datacenter.example.nl",                jsonPath.get("[0].ServerName"),            "The custom config was not applied.");
//        assertEquals("1234",                                         jsonPath.get("[0].ApplicationInstance"),   "The custom config was not applied.");
//        assertEquals("d71922715c2bfe29343644b14a4731bf5690e66e",     jsonPath.get("[0].ApplicationGitCommit"),  "The custom config was not applied.");
//    }

}
