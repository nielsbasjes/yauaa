package nl.basjes.parse.useragent.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.restassured.builder.ResponseBuilder;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.commons.lang3.exception.ExceptionUtils;

// Adapted from this to retain the original content type header
// https://github.com/eclipse/microprofile-open-api/blob/master/tck/src/main/java/org/eclipse/microprofile/openapi/tck/utils/YamlToJsonFilter.java
public class YamlToJsonFilter implements OrderedFilter {
    public YamlToJsonFilter() {
    }

    public static String ORIGINAL_CONTENT_TYPE_HEADER = "XXContentTypeXX";

    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        try {
            Response response = ctx.next(requestSpec, responseSpec);

            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(response.getBody().asString(), Object.class);

            ObjectMapper jsonWriter = new ObjectMapper();
            String json = jsonWriter.writeValueAsString(obj);

            ResponseBuilder builder = new ResponseBuilder();
            builder.clone(response);
            builder.setBody(json);
            builder.setContentType(ContentType.JSON);

            // We add this fake header so we can test on the correct content type from the API.
            builder.setHeader(ORIGINAL_CONTENT_TYPE_HEADER, response.getContentType());

            return builder.build();
        } catch (Exception var10) {
            throw new IllegalStateException("Failed to convert the request: " + ExceptionUtils.getMessage(var10), var10);
        }
    }

    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
