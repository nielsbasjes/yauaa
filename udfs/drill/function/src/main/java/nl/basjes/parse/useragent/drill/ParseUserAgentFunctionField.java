/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

package nl.basjes.parse.useragent.drill;

import io.netty.buffer.DrillBuf;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.Output;
import org.apache.drill.exec.expr.annotations.Param;
import org.apache.drill.exec.expr.annotations.Workspace;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.VarCharHolder;

import javax.inject.Inject;

@FunctionTemplate(
    name    = "parse_user_agent_field",
    scope   = FunctionTemplate.FunctionScope.SIMPLE,
    nulls   = FunctionTemplate.NullHandling.NULL_IF_NULL
)
public class ParseUserAgentFunctionField implements DrillSimpleFunc {

    @Param
    NullableVarCharHolder useragent;

    @Param
    VarCharHolder fieldName;

    @Output
    VarCharHolder value;

    @Inject
    DrillBuf outBuffer;

    @Workspace
    nl.basjes.parse.useragent.UserAgentAnalyzer uaa;

    @Workspace
    java.util.List<String> allFields;

    public void setup() {
        uaa = nl.basjes.parse.useragent.drill.UserAgentAnalyzerPreLoader.getInstance();
        allFields = uaa.getAllPossibleFieldNamesSorted();
    }

    public void eval() {
        String userAgentString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.getStringFromVarCharHolder(useragent);
        String userAgentField = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.getStringFromVarCharHolder(fieldName);

        nl.basjes.parse.useragent.UserAgent agent = uaa.parse(userAgentString);

        String field = agent.getValue(userAgentField);

        if (field == null) {
            field = "Unknown";
        }

        byte[] rowStringBytes = field.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        outBuffer.reallocIfNeeded(rowStringBytes.length);
        outBuffer.setBytes(0, rowStringBytes);

        value.start = 0;
        value.end = rowStringBytes.length;
        value.buffer = outBuffer;
    }
}
