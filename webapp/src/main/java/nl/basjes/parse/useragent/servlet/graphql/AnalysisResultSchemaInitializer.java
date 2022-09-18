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
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.schema.SchemaTransformer;
import graphql.schema.idl.SchemaGenerator;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import nl.basjes.parse.useragent.servlet.ParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.util.TraversalControl.CONTINUE;

@Configuration(proxyBeanMethods = false)
public class AnalysisResultSchemaInitializer {

    private final ParseService parseService;

    @Autowired
    public AnalysisResultSchemaInitializer(ParseService parseService) {
        this.parseService = parseService;
    }

    private static String getSchemaFieldName(String fieldName) {
        StringBuilder sb = new StringBuilder(fieldName);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    private static String getFieldName(String schemaFieldName) {
        StringBuilder sb = new StringBuilder(schemaFieldName);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
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
    GraphQlSourceBuilderCustomizer graphQlSourceBuilderCustomizer() {
        return builder ->
            builder
                .schemaFactory(
                    (typeDefinitionRegistry, runtimeWiring) -> {
                        // First we create the base Schema.
                        GraphQLSchema baseSchema =  new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

                        // Make a visitor that can change the schema.
                        GraphQLTypeVisitorStub visitor = new GraphQLTypeVisitorStub() {
                            @Override
                            public TraversalControl visitGraphQLObjectType(GraphQLObjectType objectType, TraverserContext<GraphQLSchemaElement> context) {

                                if (objectType.getName().equals("AnalysisResult")) {
                                    // Adding all possible results dynamically: an extra field with data fetcher

                                    // Get the list of all fields we want to expose from the analyzer.
                                    List<String> allFieldNames = parseService
                                        .getUserAgentAnalyzer()
                                        .getAllPossibleFieldNamesSorted()
                                        // Avoiding this error:
                                        // "__SyntaxError__" in "AnalysisResult" must not begin with "__", which is reserved by GraphQL introspection.
                                        // The field "__SyntaxError__" is handled separately.
                                        .stream()
                                        .filter(name -> !name.startsWith("__"))
                                        .toList();

                                    GraphQLObjectType updatedAnalysisResult = objectType;
                                    GraphQLCodeRegistry.Builder codeRegistry = context.getVarFromParents(GraphQLCodeRegistry.Builder.class);

                                    for (String fieldName : allFieldNames) {
                                        String schemaFieldName = getSchemaFieldName(fieldName);

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
                        };

                        // Let the visitor transform the base schema into the final schema.
                        return SchemaTransformer.transformSchema(baseSchema, visitor);
                    }
            );
    }
}
