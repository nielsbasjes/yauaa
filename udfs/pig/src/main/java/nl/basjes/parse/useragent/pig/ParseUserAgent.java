/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.pig;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.pig.FuncSpec;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParseUserAgent extends org.apache.pig.EvalFunc<Tuple>  {

    private static final TupleFactory TUPLE_FACTORY = TupleFactory.getInstance();
    private UserAgentAnalyzer analyzer = null;
    private static List<String> allFieldNames = null;

    private int cacheSize = -1;
    private Set<String> requestedFields = null;

    private boolean initialized = false;
    private void initialize() {
        if (!initialized) {
            UserAgentAnalyzer.Builder analyzerBuilder = UserAgentAnalyzer.newBuilder();

            analyzerBuilder.hideMatcherLoadStats();

            if (cacheSize >= 0) {
                analyzerBuilder.withCache(cacheSize);
            }
            if (requestedFields != null) {
                for (String requestedField : requestedFields) {
                    analyzerBuilder.withField(requestedField);
                }
            }
            analyzer = analyzerBuilder.build();
            allFieldNames = analyzer.getAllPossibleFieldNamesSorted();
            initialized = true;
        }
    }

    private List<String> getAllFieldNamesSorted() {
        initialize();
        return allFieldNames;
    }

    public ParseUserAgent() {
    }

    public ParseUserAgent(String ... parameters) {
        boolean firstParam = true;
        for (String parameter: parameters) {
            if (firstParam) {
                firstParam = false;
                cacheSize = Integer.parseInt(parameter);
            } else {
                if (requestedFields == null) {
                    requestedFields = new HashSet<>(32);
                }
                requestedFields.add(parameter);
            }
        }
    }

    @Override
    public Tuple exec(Tuple tuple) throws IOException {
        initialize();
        String userAgentString = (String) tuple.get(0);

        UserAgent agent = analyzer.parse(userAgentString);
        Tuple result = TUPLE_FACTORY.newTuple();
        for (String fieldName: getAllFieldNamesSorted()) {
            result.append(agent.getValue(fieldName));
        }
        return result;
    }

    @Override
    public Schema outputSchema(Schema input) {
        try {
            Schema tupleSchema = new Schema();
            for (String fieldName: getAllFieldNamesSorted()) {
                tupleSchema.add(new Schema.FieldSchema(fieldName, DataType.CHARARRAY));
            }
            return new Schema(new Schema.FieldSchema("UserAgent", tupleSchema, DataType.TUPLE));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<FuncSpec> getArgToFuncMapping() throws FrontendException {
        List<FuncSpec> funcList = new ArrayList<>();
        Schema s = new Schema();
        s.add(new Schema.FieldSchema(null, DataType.CHARARRAY));
        funcList.add(new FuncSpec(this.getClass().getName(), s));
        return funcList;
    }

}
