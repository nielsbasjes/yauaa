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

package nl.basjes.parse.useragent.servlet.graphql.utils;

import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeVisitor;
import graphql.schema.SchemaTransformer;
import graphql.schema.idl.SchemaGenerator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static nl.basjes.parse.useragent.servlet.graphql.utils.DefaultInstrumentations.getMaxDepthInstrumentation;

@Configuration(proxyBeanMethods = false)
public class GraphQLSchemaInitializer {

    private final ApplicationContext context;

    @Autowired
    public GraphQLSchemaInitializer(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Bean
    GraphQlSourceBuilderCustomizer graphQlSourceBuilderCustomizer() {
        return builder -> builder
            // Reduce the possibilities of sending the server into an OOM error.
            .instrumentation(List.of(
                getMaxDepthInstrumentation(6)
//                ,
//                getMaxComplexityInstrumentation(80)
            ))
            .schemaFactory(
                (typeDefinitionRegistry, runtimeWiring) -> {
                    // First we create the base Schema.
                    GraphQLSchema schema =  new SchemaGenerator()
                        .makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

                    // The ordering is essentially "Random" which may cause problems if elements are deleted
                    // or if 2 try to add the same thing.
                    for (GraphQLTypeVisitor visitor : context.getBeanProvider(GraphQLTypeVisitor.class)) {
                        schema = SchemaTransformer.transformSchema(schema, visitor);
                    }
                    return schema;
                }
            );
    }
}
