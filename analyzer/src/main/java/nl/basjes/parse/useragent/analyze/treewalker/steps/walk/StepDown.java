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

package nl.basjes.parse.useragent.analyze.treewalker.steps.walk;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import nl.basjes.parse.useragent.analyze.NumberRangeList;
import nl.basjes.parse.useragent.analyze.NumberRangeVisitor;
import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown.UserAgentGetChildrenVisitor;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Iterator;

@DefaultSerializer(StepDown.KryoSerializer.class)
public class StepDown extends Step {

    private final int start;
    private final int end;
    private final String name;
    private transient UserAgentGetChildrenVisitor userAgentGetChildrenVisitor;

    /**
     * Initialize the transient default values
     */
    private void setDefaultFieldValues() {
        userAgentGetChildrenVisitor = new UserAgentGetChildrenVisitor(name, start, end);
    }

    private void readObject(java.io.ObjectInputStream stream)
        throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        setDefaultFieldValues();
    }

    public static final class KryoSerializer extends FieldSerializer<StepDown> {
        public KryoSerializer(Kryo kryo, Class type) {
            super(kryo, type);
        }

        @Override
        public StepDown read(Kryo kryo, Input input, Class<StepDown> type) {
            StepDown stepDown = super.read(kryo, input, type);
            stepDown.setDefaultFieldValues();
            return stepDown;
        }
    }

    // Private constructor for serialization systems ONLY (like Kyro)
    private StepDown() {
        start = -1;
        end = -1;
        name = null;
    }

    public StepDown(NumberRangeContext numberRange, String name) {
        this(NumberRangeVisitor.getList(numberRange), name);
    }

    private StepDown(NumberRangeList numberRange, String name) {
        this.name = name;
        this.start = numberRange.getStart();
        this.end = numberRange.getEnd();
        setDefaultFieldValues();
    }

    @Override
    public WalkResult walk(ParseTree tree, String value) {
        Iterator<? extends ParseTree> children = userAgentGetChildrenVisitor.visit(tree);
        while (children.hasNext()) {
            ParseTree child = children.next();
            WalkResult childResult = walkNextStep(child, null);
            if (childResult != null) {
                return childResult;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Down([" + start + ":" + end + "]" + name + ")";
    }

}
