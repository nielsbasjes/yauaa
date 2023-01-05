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
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitor;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import nl.basjes.parse.useragent.Version;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.util.TraversalControl.CONTINUE;

@Configuration(proxyBeanMethods = false)
public class YauaaVersion extends GraphQLTypeVisitorStub {

    // References on how this works (thanks to Brad Baker https://github.com/bbakerman):
    // https://github.com/spring-projects/spring-graphql/issues/452#issuecomment-1256798212
    // https://www.graphql-java.com/documentation/schema/#changing-schema

    @Bean
    GraphQLTypeVisitor addYauaaVersionToGraphQLSchema() {
        return this;
    }

    private GraphQLFieldDefinition newField(String name, String description) {
        return newFieldDefinition().name(name).description(description).type(Scalars.GraphQLString).build();
    }

    @Override
    public TraversalControl visitGraphQLObjectType(GraphQLObjectType objectType, TraverserContext<GraphQLSchemaElement> context) {
        GraphQLCodeRegistry.Builder codeRegistry = context.getVarFromParents(GraphQLCodeRegistry.Builder.class);

        if (objectType.getName().equals("Query")) {
            LogManager.getLogger(YauaaVersion.class).info("Adding the `version` to the GraphQL Query.");

            // New type
            GraphQLObjectType version = GraphQLObjectType
                .newObject()
                .name("Version")
                .description("The version information of the underlying Yauaa runtime engine.")
                .field(newField("gitCommitId",                "The git commit id of the Yauaa engine that is used"))
                .field(newField("gitCommitIdDescribeShort",   "The git describe short of the Yauaa engine that is used"))
                .field(newField("buildTimeStamp",             "Timestamp when the engine was built."))
                .field(newField("projectVersion",             "Version of the yauaa engine"))
                .field(newField("copyright",                  "Copyright notice of the Yauaa engine that is used"))
                .field(newField("license",                    "The software license Yauaa engine that is used"))
                .field(newField("url",                        "Project url"))
                .field(newField("buildJDKVersion",            "Yauaa was build using this JDK version"))
                .field(newField("targetJREVersion",           "Yauaa was build using for this target JRE version"))
                .build();

            // NOTE: All data fetchers are the default getters of the Version instance.

            // New "field" to be put in Query
            GraphQLFieldDefinition getVersion = newFieldDefinition()
                .name("version")
                .description("Returns the version information of the underlying Yauaa runtime engine.")
                .type(version)
                .build();

            // Adding an extra field with a type and a data fetcher
            GraphQLObjectType updatedQuery = objectType.transform(builder -> builder.field(getVersion));

            FieldCoordinates coordinates = FieldCoordinates.coordinates(objectType.getName(), getVersion.getName());
            codeRegistry.dataFetcher(coordinates, (DataFetcher<?>)(env -> Version.getInstance()));
            return changeNode(context, updatedQuery);
        }

        return CONTINUE;
    }
}
