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

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.core.AnyOf;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.ANY;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.ContentType.XML;
import static nl.basjes.parse.useragent.servlet.YamlToJsonFilter.ORIGINAL_CONTENT_TYPE_HEADER;
import static nl.basjes.parse.useragent.servlet.utils.Constants.TEXT_XYAML_VALUE;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

abstract class AbstractParseServletTests {
    private static final Logger LOG = LogManager.getLogger(AbstractParseServletTests.class);

    boolean runTestsThatNeedResourceFiles = true;

    abstract int getPort();

    private static int attemptsRemaining = 50;

    static boolean isRunning = false;
    @Before
    public void ensureRunning() throws InterruptedException {
        if (!isRunning) {
            LOG.info("Status: Checking status...");

            while(attemptsRemaining > 0) {
                boolean live = given().port(getPort()).accept(TEXT_PLAIN_VALUE).get("/liveness").getBody().asString().equals("YES");
                if (live) {
                    break;
                }
                LOG.warn("Status: Dead");
                // noinspection BusyWait
                Thread.sleep(100);  // NOSONAR java:S2925 Sleeping in a while loop is safe!
                attemptsRemaining--;
            }

            while(attemptsRemaining > 0) {
                boolean ready = given().port(getPort()).accept(TEXT_PLAIN_VALUE).get("/readiness").getBody().asString().equals("YES");
                if (ready) {
                    break;
                }
                LOG.warn("Status: Alive (NOT yet ready for processing)");
                // noinspection BusyWait
                Thread.sleep(100);  // NOSONAR java:S2925 Sleeping in a while loop is safe!
                attemptsRemaining--;
            }

            if (attemptsRemaining <= 0) {
                LOG.fatal("Status: Stayed dead and not ready for too long.");
                throw new IllegalStateException("Unable to initialize the parser.");
            }

            LOG.info("Status: Alive and Ready for processing");
            isRunning = true;
        }
    }

    private static final String BASEURL                     = "/yauaa/v1";
    private static final String NONSENSE_USER_AGENT         = "Niels Basjes/42";
    private static final String TEST_USER_AGENT             = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36";
    private static final String EXPECTED_AGENTNAMEVERSION   = "Chrome 84.0.4147.89";

    // ------------------------------------------
    // JSON
    @Test
    public void testGetJsonUrl() {
        given()
            .port(getPort())
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
    public void testGetJsonHeader() {
        given()
            .port(getPort())
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
    public void testPostJsonUrl() {
        given()
            .port(getPort())
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
    public void testPostJsonHeader() {
        given()
            .port(getPort())
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
    public void testGetXmlUrl() {
        given()
            .port(getPort())
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
    public void testGetXmlHeader() {
        given()
            .port(getPort())
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
    public void testPostXmlUrl() {
        given()
            .port(getPort())
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
    public void testPostXmlHeader() {
        given()
            .port(getPort())
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
    public void testGetYamlUrl() {
        given()
            .port(getPort())
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
    public void testGetYamlHeader() {
        given()
            .port(getPort())
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
    public void testPostYamlUrl() {
        given()
            .port(getPort())
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
    public void testPostYamlHeader() {
        given()
            .port(getPort())
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

    // ==========================================================================================
    // Status checks
    @Test
    public void statusLive() {
        given()
            .port(getPort())
            .accept(TEXT_PLAIN_VALUE)
            .when()
                .get("/liveness")
            .then()
                .statusCode(200)
                .contentType(TEXT_PLAIN_VALUE)
                .body(equalTo("YES"));
    }

    @Test
    public void statusReady() {
        given()
            .port(getPort())
            .accept(TEXT_PLAIN_VALUE)
            .when()
                .get("/readiness")
            .then()
                .statusCode(200)
                .contentType(TEXT_PLAIN_VALUE)
                .body(equalTo("YES"));
    }

    @Test
    public void statusRunning() {
        given()
            .port(getPort())
            .accept(TEXT_PLAIN_VALUE)
            .when()
                .get("/running")
            .then()
                .statusCode(200)
                .contentType(TEXT_PLAIN_VALUE)
                .body(equalTo("YES"));
    }

    @Test
    public void statusAppEngine() {
        given()
            .port(getPort())
            .accept(TEXT_PLAIN_VALUE)
            .when()
                .get("/_ah/health")
            .then()
                .statusCode(200)
                .contentType(TEXT_PLAIN_VALUE)
                .body(equalTo("YES"));
    }

    // ==========================================================================================
    // Testing endpoints

    @Test
    public void preheat() {
        given()
            .port(getPort())
            .accept(APPLICATION_JSON_VALUE)
            .when()
                .get(BASEURL+"/preheat")
            .then()
                .statusCode(200)
                .contentType(JSON)
                .body("status", equalTo("Ran tests"));
    }

    @Test
    public void runtests() {
        given()
            .port(getPort())
            .accept(TEXT_PLAIN_VALUE)
            .when()
                .get(BASEURL+"/runtests")
            .then()
                .statusCode(200)
                .contentType(TEXT_PLAIN_VALUE)
                .body(containsString("tests passed"));
    }

    // ==========================================================================================
    // Custom config check

    @Test
    public void testCheckCustomConfigWasLoaded() {
        assumeTrue(runTestsThatNeedResourceFiles, "Quarkus integration tests cannot use test resources");

        Response response =
            given()
                .port(getPort())
                .header("User-Agent", "TestApplication/1.2.3 (node123.datacenter.example.nl; 1234; d71922715c2bfe29343644b14a4731bf5690e66e)")
            .when()
                .get(BASEURL + "/analyze/json")
            .then()
                .statusCode(200)
                .contentType(JSON)
                .body("[0].AgentNameVersion", equalTo("TestApplication 1.2.3"))
            .extract()
                .response();
        JsonPath jsonPath = response.jsonPath();

        String failMsg = "The custom config was not applied: " + jsonPath.prettify();

        assertEquals("TestApplication",                           jsonPath.get("[0].ApplicationName"),       failMsg);
        assertEquals("1.2.3",                                     jsonPath.get("[0].ApplicationVersion"),    failMsg);
        assertEquals("node123.datacenter.example.nl",             jsonPath.get("[0].ServerName"),            failMsg);
        assertEquals("1234",                                      jsonPath.get("[0].ApplicationInstance"),   failMsg);
        assertEquals("d71922715c2bfe29343644b14a4731bf5690e66e",  jsonPath.get("[0].ApplicationGitCommit"),  failMsg);
    }

//    abstract TestRestTemplate getTestRestTemplate();
}
