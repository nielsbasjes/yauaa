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

package nl.basjes.parse.useragent.utils;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.util.ArrayList;
import java.util.List;

public final class YamlUtils {
    private YamlUtils() {}

    public static void requireNodeInstanceOf(Class<?> clazz, Node node, String filename, String error) {
        if (!clazz.isInstance(node)) {
            throw new InvalidParserConfigurationException(
                createErrorString(node, filename, error));
        }
    }

    public static void require(boolean condition, Node node, String filename, String error) {
        if (!condition) {
            throw new InvalidParserConfigurationException(
                createErrorString(node, filename, error));
        }
    }

    private static String createErrorString(Node node, String filename, String error) {
        return  "Yaml config problem.("+filename+":"+node.getStartMark().getLine()+"): " + error;
    }

    public static NodeTuple getExactlyOneNodeTuple(MappingNode mappingNode, String filename) {
        List<NodeTuple> nodeTupleList = mappingNode.getValue();
        require(nodeTupleList.size() == 1, mappingNode, filename,
            "There must be exactly 1 value in the list");
        return nodeTupleList.get(0);
    }

    public static String getKeyAsString(NodeTuple tuple, String filename) {
        Node keyNode = tuple.getKeyNode();
        requireNodeInstanceOf(ScalarNode.class, keyNode, filename,
            "The key should be a string but it is a " + keyNode.getNodeId().name());
        return ((ScalarNode)keyNode).getValue();
    }

    public static String getValueAsString(NodeTuple tuple, String filename) {
        Node valueNode = tuple.getValueNode();
        requireNodeInstanceOf(ScalarNode.class, valueNode, filename,
            "The value should be a string but it is a " + valueNode.getNodeId().name());
        return ((ScalarNode)valueNode).getValue();
    }

    public static MappingNode getValueAsMappingNode(NodeTuple tuple, String filename) {
        Node valueNode = tuple.getValueNode();
        requireNodeInstanceOf(MappingNode.class, valueNode, filename,
            "The value should be a map but it is a " + valueNode.getNodeId().name());
        return ((MappingNode)valueNode);
    }

    public static SequenceNode getValueAsSequenceNode(NodeTuple tuple, String filename) {
        Node valueNode = tuple.getValueNode();
        requireNodeInstanceOf(SequenceNode.class, valueNode, filename,
            "The value should be a sequence but it is a " + valueNode.getNodeId().name());
        return ((SequenceNode)valueNode);
    }

    public static List<String> getStringValues(Node sequenceNode, String filename) {
        requireNodeInstanceOf(SequenceNode.class, sequenceNode, filename,
            "The provided node must be a sequence but it is a " + sequenceNode.getNodeId().name());

        List<Node> valueNodes = ((SequenceNode)sequenceNode).getValue();
        List<String> values = new ArrayList<>(valueNodes.size());
        for (Node node: valueNodes) {
            requireNodeInstanceOf(ScalarNode.class, node, filename,
                "The value should be a string but it is a " + node.getNodeId().name());
            values.add(((ScalarNode)node).getValue());
        }
        return values;
    }

}
