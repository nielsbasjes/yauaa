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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_PLAIN;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestParseServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TestParseServlet.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void ensureServiceHasStarted() throws InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(APPLICATION_JSON));
        headers.set("User-Agent", "Are we there yet?");

        HttpEntity<String> request = new HttpEntity<>("Are we there yet?", headers);

        HttpStatus statusCode = null;
        LOG.info("Is running?");
        while (statusCode != HttpStatus.OK) {
            if (statusCode != null) {
                LOG.info("No, not yet running (last code = {}).", statusCode);
            }
            Thread.sleep(100);
            ResponseEntity<String> response = this.restTemplate.exchange(getAliveURI(), GET, request, String.class);
            statusCode = response.getStatusCode();
        }
        LOG.info("Yes, it is running!");
    }

    private static final String USERAGENT = "Mozilla/5.0 (X11; Linux x86_64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/78.0.3904.97 Safari/537.36";

    private static final String EXPECT_AGENT_NAME_VERSION = "Chrome 78.0.3904.97";

    private URI getAliveURI() {
        return getURI("/running");
    }

    private URI getAnalyzeURI() {
        return getURI("/yauaa/v1/analyze");
    }

    private URI getURI(String path) {
        try {
            return new URI("http://localhost:" + port + path);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Test
    public void testGetHtml() throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(TEXT_HTML));
        headers.set("User-Agent", USERAGENT);

        HttpEntity<String> request = new HttpEntity<>("Niels Basjes", headers);

        ResponseEntity<String> response = this.restTemplate
            .exchange(getURI("/"), GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        assertThat(response.getBody()).contains("<td>Name Version</td><td>" + EXPECT_AGENT_NAME_VERSION + "</td>");
    }

    // ==========================================================================================
    // JSON

    private BasicJsonTester json = new BasicJsonTester(getClass());

    @Test
    public void testGetToJSon() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(APPLICATION_JSON));
        headers.set("User-Agent", USERAGENT);

        HttpEntity<String> request = new HttpEntity<>("Niels Basjes", headers);

        ResponseEntity<String> response = this.restTemplate.exchange(getAnalyzeURI(), GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        assertThat(json.from(response.getBody()))
            .extractingJsonPathStringValue("@.AgentNameVersion")
            .isEqualTo(EXPECT_AGENT_NAME_VERSION);
    }


    @Test
    public void testPostToJSon() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(TEXT_PLAIN);
        headers.setAccept(Collections.singletonList(APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(USERAGENT, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(getAnalyzeURI(), request, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        assertThat(json.from(response.getBody()))
            .extractingJsonPathStringValue("@.AgentNameVersion")
            .isEqualTo(EXPECT_AGENT_NAME_VERSION);
    }

    // ==========================================================================================
    // XML

    @Test
    public void testGetToXML() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(APPLICATION_XML));
        headers.set("User-Agent", USERAGENT);

        HttpEntity<String> request = new HttpEntity<>("Niels Basjes", headers);

        ResponseEntity<String> response = this.restTemplate.exchange(getAnalyzeURI(), GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        assertThat(response.getBody()).contains("<AgentNameVersion>" + EXPECT_AGENT_NAME_VERSION + "</AgentNameVersion>");
    }


    @Test
    public void testPostToXML() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(TEXT_PLAIN);
        headers.setAccept(Collections.singletonList(APPLICATION_XML));

        HttpEntity<String> request = new HttpEntity<>(USERAGENT, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(getAnalyzeURI(), request, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);

        assertThat(response.getBody()).contains("<AgentNameVersion>" + EXPECT_AGENT_NAME_VERSION + "</AgentNameVersion>");
    }

}
