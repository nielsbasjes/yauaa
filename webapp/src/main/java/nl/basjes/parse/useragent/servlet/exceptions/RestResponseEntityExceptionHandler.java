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

package nl.basjes.parse.useragent.servlet.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static nl.basjes.parse.useragent.servlet.ParseService.getInitStartMoment;
import static nl.basjes.parse.useragent.servlet.ParseService.getUserAgentAnalyzerFailureMessage;
import static org.apache.commons.text.StringEscapeUtils.escapeJson;
import static org.apache.commons.text.StringEscapeUtils.escapeXml10;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@ControllerAdvice
public class RestResponseEntityExceptionHandler
    extends ResponseEntityExceptionHandler {

    @ExceptionHandler({YauaaIsBusyStarting.class})
    public ResponseEntity<Object> handleYauaaIsStarting(
        Exception ex,
        @SuppressWarnings("unused") WebRequest request) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Retry-After", "5"); // Retry after 5 seconds.

        YauaaIsBusyStarting yauaaIsBusyStarting = (YauaaIsBusyStarting) ex;

        long timeSinceStart = System.currentTimeMillis() - getInitStartMoment();
        String message;

        String userAgentAnalyzerFailureMessage = getUserAgentAnalyzerFailureMessage();
        if (userAgentAnalyzerFailureMessage == null) {
            switch (yauaaIsBusyStarting.getOutputType()) {
                case YAML:
                    message = "status: \"Starting\"\ntimeInMs: " + timeSinceStart + "\n";
                    break;
                case TXT:
                    message = "NO";
                    break;
                case JSON:
                    message = "{ \"status\": \"Starting\", \"timeInMs\": " + timeSinceStart + " }";
                    break;
                case XML:
                    message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Starting</status><timeInMs>" + timeSinceStart + "</timeInMs>";
                    break;
                case HTML:
                default:
                    message = "Yauaa has been starting up for " + timeSinceStart + " seconds now.";
                    break;
            }
            return new ResponseEntity<>(message, httpHeaders, SERVICE_UNAVAILABLE);
        } else {
            switch (yauaaIsBusyStarting.getOutputType()) {
                case YAML:
                    message = "status: \"Failed\"\nerrorMessage: |\n" + userAgentAnalyzerFailureMessage + "\n";
                    break;
                case TXT:
                    message = "FAILED: \n" + userAgentAnalyzerFailureMessage;
                    break;
                case JSON:
                    message = "{ \"status\": \"Failed\", \"errorMessage\": " + escapeJson(userAgentAnalyzerFailureMessage) + " }";
                    break;
                case XML:
                    message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Failed</status><errorMessage>" + escapeXml10(userAgentAnalyzerFailureMessage) + "</errorMessage>";
                    break;
                case HTML:
                default:
                    message = "Yauaa start up has failed with message \n" + userAgentAnalyzerFailureMessage;
                    break;
            }
            return new ResponseEntity<>(message, httpHeaders, INTERNAL_SERVER_ERROR);
        }

    }

    @ExceptionHandler({YauaaTestsFailed.class})
    public ResponseEntity<Object> handleYauaaTestsInError(
        Exception ex,
        @SuppressWarnings("unused") WebRequest request) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(ex.getMessage(), httpHeaders, INTERNAL_SERVER_ERROR);
    }

}
