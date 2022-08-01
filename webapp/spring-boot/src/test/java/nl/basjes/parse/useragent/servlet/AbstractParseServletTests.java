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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_PLAIN;

public abstract class AbstractParseServletTests {

    private static final Logger LOG = LogManager.getLogger(AbstractParseServletTests.class);

    abstract int getPort();
    abstract TestRestTemplate getTestRestTemplate();

    private int attemptsRemaining = 50;

    @Before
    public void ensureServiceHasStarted() throws InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(APPLICATION_JSON));
        headers.set(USERAGENT_HEADER, "Are we there yet?");

        HttpEntity<String> request = new HttpEntity<>("Are we there yet?", headers);

        LOG.info("Is running?");

        boolean live = false;
        boolean ready = false;

        while (!live || !ready) {
            Thread.sleep(100); // NOSONAR java:S2925 Sleeping in a while loop is safe!
            live = this.getTestRestTemplate().exchange(getLiveURI(), GET, request, String.class).getStatusCode() == HttpStatus.OK;
            if (live) {
                ready = this.getTestRestTemplate().exchange(getReadyURI(), GET, request, String.class).getStatusCode() == HttpStatus.OK;
                if (ready) {
                    LOG.info("Status: alive and ready for processing");
                } else {
                    LOG.info("Status: alive and NOT ready for processing");
                }
            } else {
                LOG.info("Status: Dead");
            }
            if (--attemptsRemaining == 0) {
                throw new IllegalStateException("Unable to initialize the parser.");
            }
        }
        LOG.info("Yes, it is running!");
    }

    private static final String USERAGENT = "Mozilla/5.0 (X11; Linux x86_64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/78.0.3904.97 Safari/537.36";

    private static final String EXPECT_AGENT_NAME_VERSION = "Chrome 78.0.3904.97";

    private URI getLiveURI() {
        return getURI("/liveness");
    }
    private URI getReadyURI() {
        return getURI("/readiness");
    }

    private URI getAnalyzeURI() {
        return getURI("/yauaa/v1/analyze");
    }

    private URI getURI(String path) {
        try {
            return new URI("http://localhost:" + getPort() + path);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    // ==========================================================================================

    public ResponseEntity<String> doGET(MediaType mediaType) {
        return doGET(mediaType, getAnalyzeURI());
    }

    public ResponseEntity<String> doGET(MediaType mediaType, URI uri) {
        return doGET(mediaType, uri, true);
    }

    public ResponseEntity<String> doGET(MediaType mediaType, URI uri, boolean withHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(mediaType));

        // GET: Use User-Agent header and set a dummy request body
        if (withHeader) {
            headers.set(USERAGENT_HEADER, USERAGENT);
        }
        HttpEntity<String> request = new HttpEntity<>("Niels Basjes", headers);

        // Do GET
        ResponseEntity<String> response = this.getTestRestTemplate().exchange(uri, GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        return response;
    }

    public ResponseEntity<String> doPOST(MediaType mediaType) {
        return doPOST(mediaType, getAnalyzeURI());
    }

    public ResponseEntity<String> doPOST(MediaType mediaType, URI uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(mediaType));

        // POST: NO User-Agent header and use the request body
        headers.setContentType(TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>(USERAGENT, headers);

        // Do POST
        ResponseEntity<String> response = this.getTestRestTemplate().postForEntity(uri, request, String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        return response;
    }

    // ==========================================================================================
    // HTML

    private void assertHTML(ResponseEntity<String> response) {
        assertThat(response.getBody()).contains("<td>Name Version</td><td>" + EXPECT_AGENT_NAME_VERSION + "</td>");
    }

    @Test
    public void htmlGet() {
        assertHTML(doGET(TEXT_HTML, getURI("/")));
    }

    @Test
    public void htmlGetQueryParam() throws UnsupportedEncodingException {
        assertHTML(doGET(TEXT_HTML, getURI("/?ua="+URLEncoder.encode(USERAGENT, StandardCharsets.UTF_8.toString())), false));
    }

    @Test
    public void htmlPost() {
        assertHTML(doGET(TEXT_HTML, getURI("/")));
    }

    // ==========================================================================================
    // JSON
    private final BasicJsonTester json = new BasicJsonTester(getClass());

    private void assertJSon(ResponseEntity<String> response) {
        assertThat(json.from(response.getBody()))
            // Uses this expression tool https://github.com/json-path/JsonPath
            .extractingJsonPathStringValue("$[0].AgentNameVersion")
            .isEqualTo(EXPECT_AGENT_NAME_VERSION);
    }

    @Test
    public void jsonGet() {
        assertJSon(doGET(APPLICATION_JSON));
    }

    @Test
    public void jsonPost() {
        assertJSon(doPOST(APPLICATION_JSON));
    }

    // ==========================================================================================
    // XML

    private void assertXML(ResponseEntity<String> response) {
        assertThat(response.getBody()).contains("<AgentNameVersion>" + EXPECT_AGENT_NAME_VERSION + "</AgentNameVersion>");
    }

    @Test
    public void xmlGet() {
        assertXML(doGET(APPLICATION_XML));
    }

    @Test
    public void xmlPost() {
        assertXML(doPOST(APPLICATION_XML));
    }

    // ==========================================================================================
    // Yaml

    private void assertYAML(ResponseEntity<String> response) {
        assertThat(response.getBody()).contains("AgentNameVersion                     : '" + EXPECT_AGENT_NAME_VERSION + "'");
    }

    @Test
    public void yamlGet() {
        assertYAML(doGET(TEXT_PLAIN));
    }

    @Test
    public void yamlPost() {
        assertYAML(doPOST(TEXT_PLAIN));
    }

    // ==========================================================================================
    // Status checks
    @Test
    public void statusLive() {
        assertThat(doGET(TEXT_PLAIN, getURI("/liveness")).getBody()).contains("YES");
    }

    @Test
    public void statusReady() {
        assertThat(doGET(TEXT_PLAIN, getURI("/readiness")).getBody()).contains("YES");
    }

    @Test
    public void statusRunning() {
        assertThat(doGET(TEXT_PLAIN, getURI("/running")).getBody()).contains("YES");
    }

    @Test
    public void statusAppEngine() {
        assertThat(doGET(TEXT_PLAIN, getURI("/_ah/health")).getBody()).contains("YES");
    }

    // ==========================================================================================
    // Status checks

    @Test
    public void preheat() {
        assertThat(doGET(APPLICATION_JSON, getURI("/yauaa/v1/preheat")).getBody()).contains("\"Ran tests\"");
    }

    @Test
    public void runtests() {
        assertThat(doGET(TEXT_PLAIN, getURI("/yauaa/v1/runtests")).getBody()).contains("tests passed");
    }

}
