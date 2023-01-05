/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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

package nl.basjes.parse.useragent.servlet.graphql;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitor;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import nl.basjes.parse.useragent.AbstractUserAgentAnalyzerDirect.HeaderSpecification;
import nl.basjes.parse.useragent.servlet.ParseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.util.TraversalControl.CONTINUE;

@Configuration(proxyBeanMethods = false)
public class RequestHeadersSchemaInitializer extends GraphQLTypeVisitorStub {

    private final ParseService parseService;

    private static final Logger LOG = LogManager.getLogger(RequestHeadersSchemaInitializer.class);

    @Autowired
    public RequestHeadersSchemaInitializer(ParseService parseService) {
        this.parseService = parseService;
    }

    // References on how this works (thanks to Brad Baker https://github.com/bbakerman):
    // https://github.com/spring-projects/spring-graphql/issues/452#issuecomment-1256798212
    // https://www.graphql-java.com/documentation/schema/#changing-schema

    @Bean
    GraphQLTypeVisitor addRequestHeadersToGraphQLSchema() {
        return this;
    }

    private String getDescription(HeaderSpecification spec) {
        return spec.getSpecificationSummary() + " See also " + spec.getSpecificationUrl();
    }

    @Override
    public TraversalControl visitGraphQLInputObjectType(GraphQLInputObjectType node, TraverserContext<GraphQLSchemaElement> context) {
        if (node.getName().equals("RequestHeadersInput")) {
            GraphQLInputObjectType updatedRequestHeaders = node.transform(GraphQLInputObjectType.Builder::clearFields);

            List<GraphQLInputObjectField> requestHeaderFields = parseService
                .getUserAgentAnalyzer()
                .getAllSupportedHeaders()
                .values()
                .stream()
                .map(spec ->
                    newInputObjectField()
                        .name(spec.getFieldName())
                        .description(getDescription(spec))
                        .type(Scalars.GraphQLString)
                        .build())
                .toList();
            for (GraphQLInputObjectField requestHeaderField : requestHeaderFields) {
                LOG.info("GraphQL Schema `{}`: Adding field `{}` ", node.getName(), requestHeaderField.getName());
                updatedRequestHeaders = updatedRequestHeaders.transform(builder -> builder.field(requestHeaderField));
            }
            return changeNode(context, updatedRequestHeaders);
        }
        return CONTINUE;
    }

    @Override
    public TraversalControl visitGraphQLObjectType(GraphQLObjectType objectType, TraverserContext<GraphQLSchemaElement> context) {
        if (objectType.getName().equals("RequestHeadersInput") || objectType.getName().equals("RequestHeadersResult")) {
            GraphQLObjectType updatedRequestHeaders = objectType.transform(GraphQLObjectType.Builder::clearFields);

            List<GraphQLFieldDefinition> requestHeaderFields = parseService
                .getUserAgentAnalyzer()
                .getAllSupportedHeaders()
                .values()
                .stream()
                .map(spec ->
                    newFieldDefinition()
                        .name(spec.getFieldName())
                        .description(getDescription(spec))
                        .type(Scalars.GraphQLString)
                        .build())
                .toList();

            for (GraphQLFieldDefinition requestHeaderField : requestHeaderFields) {
                LOG.info("GraphQL Schema `{}`: Adding field `{}` ", objectType.getName(), requestHeaderField.getName());
                updatedRequestHeaders = updatedRequestHeaders.transform(builder -> builder.field(requestHeaderField));
            }
            return changeNode(context, updatedRequestHeaders);
        }
        return CONTINUE;
    }

}
