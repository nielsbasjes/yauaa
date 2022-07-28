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
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.TypeRuntimeWiring;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@Configuration(proxyBeanMethods = false)
public class Experimental {

    @Bean
    GraphQlSourceBuilderCustomizer graphQlSourceBuilderCustomizer() {
        return builder -> {

            GraphQLFieldDefinition commitField = newFieldDefinition()
                .name("commit")
                .description("The commit hash")
                .type(Scalars.GraphQLString)
                .build();

            GraphQLFieldDefinition nameField = newFieldDefinition()
                .name("url")
                .description("Where can we find it")
                .type(Scalars.GraphQLString)
                .build();

            GraphQLObjectType version = GraphQLObjectType
                .newObject()
                .name("Version")
                .description("The version info")
                .field(commitField)
                .field(nameField)
                .build()
                ;

            GraphQLObjectType query = GraphQLObjectType
                .newObject()
                .name("Query")
                .description("The root query")
                .field(
                    newFieldDefinition()
                        .name("DoSomething")
                        .description("It should do something here")
                        .argument(GraphQLArgument.newArgument().name("thing").type(Scalars.GraphQLString).build())
                        .type(version)
                )
                .build();

            builder
                .schemaFactory(
                    (typeDefinitionRegistry, runtimeWiring) ->
                        GraphQLSchema
                            .newSchema()
                            .query(query)
                            .build()
                )
                .configureRuntimeWiring(
                    runtimeWiringBuilder -> runtimeWiringBuilder
                        .type(
                            TypeRuntimeWiring
                                .newTypeWiring("Query")
                                .dataFetcher("Query", testDataFetcher)
                                .dataFetcher("DoSomething", testDataFetcher)
                                .dataFetcher("Version", testDataFetcher)
                                .dataFetcher("commit", testDataFetcher)
                                .dataFetcher("url", testDataFetcher)
                        )
                        .type(
                            TypeRuntimeWiring
                                .newTypeWiring("DoSomething")
                                .dataFetcher("Query", testDataFetcher)
                                .dataFetcher("DoSomething", testDataFetcher)
                                .dataFetcher("Version", testDataFetcher)
                                .dataFetcher("commit", testDataFetcher)
                                .dataFetcher("url", testDataFetcher)
                        )
                        .type(
                            TypeRuntimeWiring
                                .newTypeWiring("Version")
                                .dataFetcher("Query", testDataFetcher)
                                .dataFetcher("DoSomething", testDataFetcher)
                                .dataFetcher("Version", testDataFetcher)
                                .dataFetcher("commit", testDataFetcher)
                                .dataFetcher("url", testDataFetcher)
                        )
                )
            ;
        };
    }

    static DataFetcher<?> testDataFetcher = environment -> "Something";
//        "The arguments we got:" + environment
//            .getArguments()
//            .entrySet()
//            .stream()
//            .map(entry -> "{ " + entry.getKey() + " = " + entry.getValue().toString() + " }")
//            .collect(Collectors.joining(" | "));

}
