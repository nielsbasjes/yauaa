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

package nl.basjes.parse.useragent.servlet.graphql;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitor;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import nl.basjes.parse.useragent.servlet.ParseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.util.TraversalControl.CONTINUE;
import static nl.basjes.parse.useragent.servlet.graphql.utils.FieldsAndSchema.getAllFieldsForGraphQL;
import static nl.basjes.parse.useragent.servlet.graphql.utils.FieldsAndSchema.getFieldName;
import static nl.basjes.parse.useragent.servlet.graphql.utils.FieldsAndSchema.getSchemaFieldName;

@Configuration(proxyBeanMethods = false)
public class AnalysisResultSchemaInitializer extends GraphQLTypeVisitorStub {

    private final ParseService parseService;

    private static final Logger LOG = LogManager.getLogger(AnalysisResultSchemaInitializer.class);

    @Autowired
    public AnalysisResultSchemaInitializer(ParseService parseService) {
        this.parseService = parseService;
    }

    static DataFetcher<?> analysisResultFieldDataFetcher = environment -> {
        AnalysisResult analysisResult = environment.getSource();
        String schemaFieldName = environment.getField().getName();
        return analysisResult.getValue(getFieldName(schemaFieldName));
    };

    // References on how this works (thanks to Brad Baker https://github.com/bbakerman):
    // https://github.com/spring-projects/spring-graphql/issues/452#issuecomment-1256798212
    // https://www.graphql-java.com/documentation/schema/#changing-schema

    @Bean
    GraphQLTypeVisitor addAllPossibleYauaaFieldsToAnalysisResultInGraphQLSchema() {
        return this;
    }

    @Override
    public TraversalControl visitGraphQLObjectType(GraphQLObjectType objectType, TraverserContext<GraphQLSchemaElement> context) {
        if (objectType.getName().equals("AnalysisResult")) {
            // Adding all possible results dynamically: an extra field with data fetcher

            GraphQLObjectType updatedAnalysisResult = objectType;
            GraphQLCodeRegistry.Builder codeRegistry = context.getVarFromParents(GraphQLCodeRegistry.Builder.class);

            // Handle all fields we want to expose from the analyzer.
            for (String fieldName : getAllFieldsForGraphQL(parseService)) {
                String schemaFieldName = getSchemaFieldName(fieldName);

                LOG.info("GraphQL Schema `{}`: Adding field `{}` ", objectType.getName(), fieldName);

                // Add a new field to the AnalysisResult
                updatedAnalysisResult = updatedAnalysisResult
                    .transform(builder -> builder
                        .field(
                            newFieldDefinition()
                                .name(schemaFieldName)
                                .description("The value of " + fieldName)
                                .type(Scalars.GraphQLString)
                                .build()
                        )
                    );

                // Register the 'getter' for this new field.
                codeRegistry.dataFetcher(
                    FieldCoordinates.coordinates("AnalysisResult", schemaFieldName),
                    analysisResultFieldDataFetcher
                );
            }

            return changeNode(context, updatedAnalysisResult);
        }
        return CONTINUE;
    }
}
