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

package nl.basjes.parse.useragent.utils;

import com.esotericsoftware.kryo.Kryo;
import nl.basjes.collections.prefixmap.StringPrefixMap;
import nl.basjes.parse.useragent.AbstractUserAgentAnalyzerDirect;
import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.Analyzer;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.analyze.MatchMaker;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.MatcherExtractAction;
import nl.basjes.parse.useragent.analyze.MatcherFailIfFoundAction;
import nl.basjes.parse.useragent.analyze.MatcherList;
import nl.basjes.parse.useragent.analyze.MatcherRequireAction;
import nl.basjes.parse.useragent.analyze.MatcherVariableAction;
import nl.basjes.parse.useragent.analyze.MatchesList;
import nl.basjes.parse.useragent.analyze.UserAgentStringMatchMaker;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor;
import nl.basjes.parse.useragent.analyze.treewalker.TreeExpressionEvaluator;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepDefaultIfNull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepEndsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepEquals;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsInSet;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNotInSet;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepNotContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepNotEquals;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepStartsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsInLookupContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsInLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsNotInLookupContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsNotInLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookup;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookupContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepBackToFull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepCleanVersion;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcat;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcatPostfix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcatPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepExtractBrandFromUrl;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepNormalizeBrand;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepReplaceString;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepSegmentRange;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepWordRange;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepDown;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepNext;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepNextN;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepPrev;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepPrevN;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepUp;
import nl.basjes.parse.useragent.calculate.CalculateAgentClass;
import nl.basjes.parse.useragent.calculate.CalculateAgentEmail;
import nl.basjes.parse.useragent.calculate.CalculateAgentName;
import nl.basjes.parse.useragent.calculate.CalculateDeviceBrand;
import nl.basjes.parse.useragent.calculate.CalculateDeviceName;
import nl.basjes.parse.useragent.calculate.CalculateNetworkType;
import nl.basjes.parse.useragent.calculate.ConcatNONDuplicatedCalculator;
import nl.basjes.parse.useragent.calculate.FieldCalculator;
import nl.basjes.parse.useragent.calculate.MacOSXMajorVersionCalculator;
import nl.basjes.parse.useragent.calculate.MajorVersionCalculator;
import nl.basjes.parse.useragent.clienthints.ClientHintsAnalyzer;
import nl.basjes.parse.useragent.config.AnalyzerConfig;
import nl.basjes.parse.useragent.config.MatcherConfig;
import nl.basjes.parse.useragent.config.TestCase;
import nl.basjes.parse.useragent.parse.UserAgentTreeFlattener;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.TreeSet;

public final class KryoConfig {

    private KryoConfig() {
    }

    /**
     * This is used to configure the provided Kryo instance if Kryo serialization is desired.
     * @param kryo The instance of com.esotericsoftware.kryo.Kryo that needs to be configured.
     */
    public static void configureKryo(Kryo kryo) {
        // Since kryo 5.0.0-RC3 the default is to not use references.
        // With Yauaa you will go into a StackOverflow if you do not support references in Kryo because of
        // circulair references in the data structures.
        // See https://github.com/EsotericSoftware/kryo/issues/617
        //     https://github.com/EsotericSoftware/kryo/issues/789
        kryo.setReferences(true);

        // Let Kryo output a lot of debug information
//        Log.DEBUG();
//        kryo.setRegistrationRequired(true);
//        kryo.setWarnUnregisteredClasses(true);

        // Register the Java classes we need
        kryo.register(Collections.emptySet().getClass());
        kryo.register(Collections.emptyList().getClass());
        kryo.register(Collections.emptyMap().getClass());

        kryo.register(ArrayList.class);

        kryo.register(LinkedHashSet.class);
        kryo.register(LinkedHashMap.class);
        kryo.register(HashSet.class);
        kryo.register(HashMap.class);
        kryo.register(TreeSet.class);
        kryo.register(TreeMap.class);
        kryo.register(LinkedCaseInsensitiveMap.class);

        // This class
        kryo.register(AbstractUserAgentAnalyzerDirect.class);

        ClientHintsAnalyzer.configureKryo(kryo);

        // The config classes
        kryo.register(MatcherConfig.class);
        kryo.register(MatcherConfig.ConfigLine.class);
        kryo.register(MatcherConfig.ConfigLine.Type.class);
        kryo.register(AnalyzerConfig.class);
        kryo.register(MatcherConfig.class);
        kryo.register(TestCase.class);

        // All classes we have under this.
        kryo.register(Analyzer.class);
        kryo.register(UserAgent.ImmutableUserAgent.class);
        kryo.register(AgentField.ImmutableAgentField.class);
        kryo.register(UserAgent.MutableUserAgent.class);
        kryo.register(AgentField.MutableAgentField.class);

        kryo.register(MatchMaker.class);
        kryo.register(UserAgentStringMatchMaker.class);

        kryo.register(Matcher.class);
        kryo.register(Matcher.MatcherDemotedExtractAction.class);
        kryo.register(MatcherAction.class);
        kryo.register(MatcherList.class);
        kryo.register(MatchesList.class);
        kryo.register(MatcherExtractAction.class);
        kryo.register(MatcherVariableAction.class);
        kryo.register(MatcherRequireAction.class);
        kryo.register(MatcherFailIfFoundAction.class);
        kryo.register(WordRangeVisitor.Range.class);

        kryo.register(CalculateAgentEmail.class);
        kryo.register(CalculateAgentName.class);
        kryo.register(CalculateAgentClass.class);
        kryo.register(CalculateDeviceBrand.class);
        kryo.register(CalculateDeviceName.class);
        kryo.register(CalculateNetworkType.class);
        kryo.register(ConcatNONDuplicatedCalculator.class);
        kryo.register(FieldCalculator.class);
        kryo.register(MajorVersionCalculator.class);
        kryo.register(MacOSXMajorVersionCalculator.class);

        kryo.register(UserAgentTreeFlattener.class);
        kryo.register(TreeExpressionEvaluator.class);
        kryo.register(WalkList.class);
        kryo.register(StepContains.class);
        kryo.register(StepNotContains.class);
        kryo.register(StepDefaultIfNull.class);
        kryo.register(StepEndsWith.class);
        kryo.register(StepEquals.class);
        kryo.register(StepIsInSet.class);
        kryo.register(StepIsNotInSet.class);
        kryo.register(StepIsNull.class);
        kryo.register(StepNotEquals.class);
        kryo.register(StepStartsWith.class);
        kryo.register(StepIsInLookupContains.class);
        kryo.register(StepIsNotInLookupContains.class);
        kryo.register(StepIsInLookupPrefix.class);
        kryo.register(StepIsNotInLookupPrefix.class);
        kryo.register(StepLookup.class);
        kryo.register(StepLookupContains.class);
        kryo.register(StepLookupPrefix.class);
        kryo.register(StepBackToFull.class);
        kryo.register(StepCleanVersion.class);
        kryo.register(StepConcat.class);
        kryo.register(StepConcatPostfix.class);
        kryo.register(StepConcatPrefix.class);
        kryo.register(StepNormalizeBrand.class);
        kryo.register(StepExtractBrandFromUrl.class);
        kryo.register(StepReplaceString.class);
        kryo.register(StepSegmentRange.class);
        kryo.register(StepWordRange.class);
        kryo.register(StepDown.class);
        kryo.register(StepNext.class);
        kryo.register(StepNextN.class);
        kryo.register(StepPrev.class);
        kryo.register(StepPrevN.class);
        kryo.register(StepUp.class);

        StringPrefixMap.configureKryo(kryo);
    }
}
