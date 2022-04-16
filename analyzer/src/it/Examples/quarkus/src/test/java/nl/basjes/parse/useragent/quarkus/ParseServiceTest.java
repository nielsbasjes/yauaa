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

package nl.basjes.parse.useragent.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.matchesRegex;

@QuarkusTest
class ParseServiceTest {

    @Test
    void testParserEndpoint() {
        given()
            .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36")
            .when()
                .get("/parse")
            .then()
                .statusCode(200)
                .body(containsString("Chrome 84.0.4147.89"));
    }

    @Test
    void testCheckCustomConfigWasLoaded() {
        given()
            .header("User-Agent", "TestApplication/1.2.3 (node123.datacenter.example.nl; 1234; d71922715c2bfe29343644b14a4731bf5690e66e)")
        .when()
            .get("/parse")
        .then()
            .statusCode(200)
            .body(
                allOf(
                    containsString("\"ApplicationGitCommit\":\"d71922715c2bfe29343644b14a4731bf5690e66e\""),
                    containsString("\"ApplicationInstance\":\"1234\""),
                    containsString("\"ApplicationName\":\"TestApplication\""),
                    containsString("\"ApplicationVersion\":\"1.2.3\""),
                    containsString("\"ServerName\":\"node123.datacenter.example.nl\"")
                ));

    }

}
