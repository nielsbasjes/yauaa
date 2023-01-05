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

package nl.basjes.parse.useragent.servlet.graphql.utils;

import graphql.ExecutionResult;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.Selection;

import java.util.List;

import static graphql.execution.instrumentation.SimpleInstrumentationContext.noOp;

public final class DefaultInstrumentations {
    private DefaultInstrumentations() {
    }

    /*
    * When retrieving a field we need to avoid getting too much data.
    * Yet when retrieving the schema we need to allow a much bigger query.
    */
    @SuppressWarnings("rawtypes")
    private static boolean isOnlySchemaQuery(InstrumentationExecuteOperationParameters parameters) {
        OperationDefinition operationDefinition = parameters.getExecutionContext().getOperationDefinition();
        List<Selection> selections = operationDefinition.getSelectionSet().getSelections();
        if (selections.size() != 1) {
            return false;
        }
        Selection selection = selections.get(0);
        if (!(selection instanceof Field)) {
            return false;
        }
        return ((Field) selection).getName().equals("__schema");
    }

    public static MaxQueryDepthInstrumentation getMaxDepthInstrumentation(int maxDepth) {
        return new MaxQueryDepthInstrumentation(maxDepth) {
            @Override
            public InstrumentationContext<ExecutionResult>
                    beginExecuteOperation(
                        InstrumentationExecuteOperationParameters parameters,
                        InstrumentationState state) {
                if (isOnlySchemaQuery(parameters)) {
                    return noOp();
                }
                return super.beginExecuteOperation(parameters, state);
            }
        };
    }

    public static MaxQueryComplexityInstrumentation getMaxComplexityInstrumentation(int maxComplexity) {
        return new MaxQueryComplexityInstrumentation(maxComplexity) {
            @Override
            public InstrumentationContext<ExecutionResult>
                    beginExecuteOperation(
                        InstrumentationExecuteOperationParameters parameters,
                        InstrumentationState state) {
                if (isOnlySchemaQuery(parameters)) {
                    return noOp();
                }
                return super.beginExecuteOperation(parameters, state);
            }
        };
    }
}
