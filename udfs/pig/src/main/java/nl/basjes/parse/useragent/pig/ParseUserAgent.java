/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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
import java.util.List;

public class ParseUserAgent extends org.apache.pig.EvalFunc<Tuple>  {

    private static final TupleFactory TUPLE_FACTORY = TupleFactory.getInstance();
    private UserAgentAnalyzer analyzer = new UserAgentAnalyzer();
    private static List<String> allFieldNames = null;

    private List<String> getAllFieldNamesSorted() {
        if (allFieldNames == null) {
            allFieldNames = analyzer.getAllPossibleFieldNamesSorted();
        }
        return allFieldNames;
    }

    public ParseUserAgent() {
        analyzer = new UserAgentAnalyzer();
    }

    public ParseUserAgent(String ... parameters) {
        UserAgentAnalyzer.Builder analyzerBuilder = UserAgentAnalyzer.newBuilder();
        boolean firstParam = true;
        for (String parameter: parameters) {
            if (firstParam) {
                firstParam = false;
                analyzerBuilder.withCache(Integer.parseInt(parameter));
            } else {
                analyzerBuilder.withField(parameter);
            }
        }
        analyzer = analyzerBuilder.build();
    }

    @Override
    public Tuple exec(Tuple tuple) throws IOException {
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
