package nl.basjes.parse.useragent.servlet.mcp;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@RestController
public class HumanMcpInstructions {
    public static String getFullMCPUrl() {
        return ServletUriComponentsBuilder
            .fromCurrentRequestUri()
            .replacePath(null)
            .build()
            .toUriString() + "/mcp";
    }

//    @Bean
//    @Order(HIGHEST_PRECEDENCE)
//    public RouterFunction<?> mcpHumanHelperPage() {
//        return RouterFunctions.route()
//            .GET("/mcp", RequestPredicates.accept(MediaType.TEXT_HTML), (serverRequest) ->
//                ServerResponse
//                    .status(HttpStatus.METHOD_NOT_ALLOWED)
//                    .contentType(MediaType.TEXT_HTML)
//                    .body("""
//                        <!DOCTYPE html>
//                        <html>
//                        <head>
//                            <title>MCP Tool Access</title>
//                        </head>
//                        <body>
//                            <h1>Only for MCP Tools</h1>
//                            <p>You've reached the MCP tool endpoint. This page is only usable for AI clients like LM-studio and Claude Desktop.</p>
//                            <h2>What to do instead:</h2>
//                            <ul>
//                                <li>Configure your <strong>AI Client</strong> to use this URL: <b><code>""" +
//                        ServletUriComponentsBuilder
//                            .fromCurrentRequestUri()
//                            .replacePath(null)
//                            .build()
//                            .toUriString() + "/mcp" +
//                        """
//                            </b></code></li>
//                                    <li>Or just use the <a href="/">main page</a> to directly interact with the UI.</li>
//                                </ul>
//                            </body>
//                            </html>
//                            """))
//            .build();
//    }


    @GetMapping(value = "/mcp", produces = MediaType.TEXT_HTML_VALUE)
    public String showMcpInstructions() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>MCP Tool Access</title>
            </head>
            <body>
                <h1>Only for MCP Tools</h1>
                <p>You've reached the MCP tool endpoint. This page is only usable for AI clients like LM-studio and Claude Desktop.</p>
                <h2>What to do instead:</h2>
                <ul>
                    <li>Configure your <strong>AI Client</strong> to use this URL: <b><code>""" + getFullMCPUrl() + """
            </b></code></li>
                    <li>Or just use the <a href="/">main page</a> to directly interact with the UI.</li>
                </ul>
            </body>
            </html>
            """;
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
//        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }
}
