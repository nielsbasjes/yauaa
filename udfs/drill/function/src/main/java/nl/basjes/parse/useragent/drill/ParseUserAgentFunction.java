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
import org.apache.drill.exec.vector.complex.writer.BaseWriter;

import javax.inject.Inject;

@FunctionTemplate(
    name    = "parse_user_agent",
    scope   = FunctionTemplate.FunctionScope.SIMPLE,
    nulls   = FunctionTemplate.NullHandling.INTERNAL
)
public class ParseUserAgentFunction implements DrillSimpleFunc {

    @Param
    NullableVarCharHolder input;

    @Output
    BaseWriter.ComplexWriter outWriter;

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
        org.apache.drill.exec.vector.complex.writer.BaseWriter.MapWriter queryMapWriter = outWriter.rootAsMap();

        if (input.buffer == null) {
            return;
        }

        String userAgentString = org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers.toStringFromUTF8(input.start, input.end, input.buffer);

        if (userAgentString.isEmpty() || userAgentString.equals("null")) {
            userAgentString = "";
        }

        nl.basjes.parse.useragent.UserAgent agent = uaa.parse(userAgentString);

        for (String fieldName: agent.getAvailableFieldNamesSorted()) {

            org.apache.drill.exec.expr.holders.VarCharHolder rowHolder = new org.apache.drill.exec.expr.holders.VarCharHolder();
            String field = agent.getValue(fieldName);

            if (field == null) {
                field = "Unknown";
            }

            byte[] rowStringBytes = field.getBytes();
            outBuffer.reallocIfNeeded(rowStringBytes.length);
            outBuffer.setBytes(0, rowStringBytes);

            rowHolder.start = 0;
            rowHolder.end = rowStringBytes.length;
            rowHolder.buffer = outBuffer;

            queryMapWriter.varChar(fieldName).write(rowHolder);
        }
    }
}
