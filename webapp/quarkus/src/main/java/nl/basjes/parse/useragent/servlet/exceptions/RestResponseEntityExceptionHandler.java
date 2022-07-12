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

package nl.basjes.parse.useragent.servlet.exceptions;

import nl.basjes.parse.useragent.servlet.ParseService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.inject.Inject;

import static org.apache.commons.text.StringEscapeUtils.escapeJson;
import static org.apache.commons.text.StringEscapeUtils.escapeXml10;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@ControllerAdvice
public class RestResponseEntityExceptionHandler {

    @Inject
    ParseService parseService;

    @ExceptionHandler({YauaaIsBusyStarting.class})
    public ResponseEntity<Object> handleYauaaIsStarting(Exception ex) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Retry-After", "5"); // Retry after 5 seconds.

        YauaaIsBusyStarting yauaaIsBusyStarting;

        if (ex instanceof YauaaIsBusyStarting busyStarting) {
            yauaaIsBusyStarting = busyStarting;
        } else {
            return new ResponseEntity<>("Got unexpected exception: " + ex.getMessage(), httpHeaders, INTERNAL_SERVER_ERROR);
        }

        long timeSinceStart = System.currentTimeMillis() - parseService.getInitStartMoment();
        String message;

        String userAgentAnalyzerFailureMessage = parseService.getUserAgentAnalyzerFailureMessage();
        if (userAgentAnalyzerFailureMessage == null) {
            message = switch (yauaaIsBusyStarting.getOutputType()) {
                case YAML -> "status: \"Starting\"\ntimeInMs: " + timeSinceStart + "\n";
                case TXT  -> "NO";
                case JSON -> "{ \"status\": \"Starting\", \"timeInMs\": " + timeSinceStart + " }";
                case XML  ->  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Starting</status><timeInMs>" + timeSinceStart + "</timeInMs>";
                case HTML -> "Yauaa has been starting up for " + timeSinceStart + " seconds now.";
            };
            return new ResponseEntity<>(message, httpHeaders, SERVICE_UNAVAILABLE);
        } else {
            message = switch (yauaaIsBusyStarting.getOutputType()) {
                case YAML -> "status: \"Failed\"\nerrorMessage: |\n" + userAgentAnalyzerFailureMessage + "\n";
                case TXT  -> "FAILED: \n" + userAgentAnalyzerFailureMessage;
                case JSON -> "{ \"status\": \"Failed\", \"errorMessage\": " + escapeJson(userAgentAnalyzerFailureMessage) + " }";
                case XML  -> "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Failed</status><errorMessage>" + escapeXml10(userAgentAnalyzerFailureMessage) + "</errorMessage>";
                case HTML -> "Yauaa start up has failed with message \n" + userAgentAnalyzerFailureMessage;
            };
            return new ResponseEntity<>(message, httpHeaders, INTERNAL_SERVER_ERROR);
        }

    }

    @ExceptionHandler({YauaaTestsFailed.class})
    public ResponseEntity<Object> handleYauaaTestsInError(Exception ex) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(ex.getMessage(), httpHeaders, INTERNAL_SERVER_ERROR);
    }

}
