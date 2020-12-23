/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

package nl.basjes.parse.useragent;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import nl.basjes.collections.prefixmap.StringPrefixMap;
import nl.basjes.parse.useragent.AgentField.ImmutableAgentField;
import nl.basjes.parse.useragent.AgentField.MutableAgentField;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.MatcherExtractAction;
import nl.basjes.parse.useragent.analyze.MatcherList;
import nl.basjes.parse.useragent.analyze.MatcherRequireAction;
import nl.basjes.parse.useragent.analyze.MatcherVariableAction;
import nl.basjes.parse.useragent.analyze.MatchesList;
import nl.basjes.parse.useragent.analyze.UselessMatcherException;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.analyze.treewalker.TreeExpressionEvaluator;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepDefaultIfNull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepEndsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepEquals;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsInSet;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNotInSet;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepNotEquals;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepStartsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsInLookupContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsInLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsNotInLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookup;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookupContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepBackToFull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepCleanVersion;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcat;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcatPostfix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcatPrefix;
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
import nl.basjes.parse.useragent.calculate.MajorVersionCalculator;
import nl.basjes.parse.useragent.parse.UserAgentTreeFlattener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.basjes.parse.useragent.UserAgent.AGENT_CLASS;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.HACKER_ATTACK_VECTOR;
import static nl.basjes.parse.useragent.UserAgent.HACKER_TOOLKIT;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.MutableUserAgent.isSystemField;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_CLASS;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.PRE_SORTED_FIELDS_LIST;
import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION_MAJOR;
import static nl.basjes.parse.useragent.utils.YamlUtils.getExactlyOneNodeTuple;
import static nl.basjes.parse.useragent.utils.YamlUtils.getKeyAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.getStringValues;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsMappingNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsSequenceNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.require;
import static nl.basjes.parse.useragent.utils.YamlUtils.requireNodeInstanceOf;
import static nl.basjes.parse.useragent.utils.YauaaVersion.assertSameVersion;
import static nl.basjes.parse.useragent.utils.YauaaVersion.logVersion;

@DefaultSerializer(AbstractUserAgentAnalyzerDirect.KryoSerializer.class)
public abstract class AbstractUserAgentAnalyzerDirect implements Analyzer, Serializable {

    // We set this to 1000000 always.
    // Why?
    // At the time of writing this the actual HashMap size needed about 410K entries.
    // To keep the bins small the load factor of 0.75 already puts us at the capacity of 1048576
    private static final int INFORM_ACTIONS_HASHMAP_CAPACITY = 1000000;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractUserAgentAnalyzerDirect.class);
    private final ArrayList<Matcher> allMatchers = new ArrayList<>(5000);
    private final ArrayList<Matcher> zeroInputMatchers = new ArrayList<>(100);

    protected List<Matcher> getAllMatchers() {
        return allMatchers;
    }

    private final Map<String, Set<MatcherAction>> informMatcherActions = new LinkedHashMap<>(INFORM_ACTIONS_HASHMAP_CAPACITY);
    private transient Map<String, List<MappingNode>> matcherConfigs = new HashMap<>();

    private boolean showMatcherStats = false;
    private boolean doingOnlyASingleTest = false;

    // If we want ALL fields this is null. If we only want specific fields this is a list of names.
    protected Set<String> wantedFieldNames = null; // NOSONAR: Only accessed via Builder.

    private final ArrayList<Map<String, Map<String, String>>> testCases = new ArrayList<>(2048);

    public List<Map<String, Map<String, String>>> getTestCases() {
        return testCases;
    }

    private Map<String, Map<String, String>> lookups = new LinkedHashMap<>(128);
    private final Map<String, Set<String>> lookupSets = new LinkedHashMap<>(128);
    private final Map<String, Set<String>> lookupSetMerge = new LinkedHashMap<>(128);

    @Override
    public Map<String, Map<String, String>> getLookups() {
        return lookups;
    }

    @Override
    public Map<String, Set<String>> getLookupSets() {
        return lookupSets;
    }

    protected UserAgentTreeFlattener flattener;

    public static final int DEFAULT_USER_AGENT_MAX_LENGTH = 2048;
    private int userAgentMaxLength = DEFAULT_USER_AGENT_MAX_LENGTH;
    private boolean loadTests = false;

    private static final String DEFAULT_RESOURCES = "classpath*:UserAgents/**/*.yaml";

    /*
     * Initialize the transient default values
     */
    void initTransientFields() {
        matcherConfigs = new HashMap<>(64);
        touchedMatchers = new MatcherList(32);
    }

    private void readObject(java.io.ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        initTransientFields();
        stream.defaultReadObject();
        showDeserializationStats();
    }

    /**
     * This is used to configure the provided Kryo instance if Kryo serialization is desired.
     * The expected type here is Object because otherwise the Kryo library becomes
     * a mandatory dependency on any project that uses Yauaa.
     * @param kryoInstance The instance of com.esotericsoftware.kryo.Kryo that needs to be configured.
     */
    public static void configureKryo(Object kryoInstance) {
        Kryo kryo = (Kryo) kryoInstance;
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

        // This class
        kryo.register(AbstractUserAgentAnalyzerDirect.class);

        // All classes we have under this.
        kryo.register(Analyzer.class);
        kryo.register(ImmutableUserAgent.class);
        kryo.register(ImmutableAgentField.class);
        kryo.register(MutableUserAgent.class);
        kryo.register(MutableAgentField.class);

        kryo.register(Matcher.class);
        kryo.register(MatcherAction.class);
        kryo.register(MatcherList.class);
        kryo.register(MatchesList.class);
        kryo.register(MatcherExtractAction.class);
        kryo.register(MatcherVariableAction.class);
        kryo.register(MatcherRequireAction.class);
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

        kryo.register(UserAgentTreeFlattener.class);
        kryo.register(TreeExpressionEvaluator.class);
        kryo.register(WalkList.class);
        kryo.register(StepContains.class);
        kryo.register(StepDefaultIfNull.class);
        kryo.register(StepEndsWith.class);
        kryo.register(StepEquals.class);
        kryo.register(StepIsInSet.class);
        kryo.register(StepIsNotInSet.class);
        kryo.register(StepIsNull.class);
        kryo.register(StepNotEquals.class);
        kryo.register(StepStartsWith.class);
        kryo.register(StepIsInLookupContains.class);
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

    public static class KryoSerializer extends FieldSerializer<AbstractUserAgentAnalyzerDirect> {
        public KryoSerializer(Kryo kryo, Class<?> type) {
            super(kryo, type);
        }

        @Override
        public void write(Kryo kryo, Output output, AbstractUserAgentAnalyzerDirect object) {
            // Get rid of needless data
            object.reset();
            super.write(kryo, output, object);
        }

        @Override
        public AbstractUserAgentAnalyzerDirect read(Kryo kryo, Input input, Class<? extends AbstractUserAgentAnalyzerDirect> type) {
            AbstractUserAgentAnalyzerDirect uaa = super.read(kryo, input, type);
            uaa.initTransientFields();
            uaa.showDeserializationStats();
            return uaa;
        }
    }

    private void showDeserializationStats() {
        List<String> lines = new ArrayList<>();
        lines.add("This Analyzer instance was deserialized.");
        lines.add("");
        lines.add("Lookups      : " + ((lookups == null) ? 0 : lookups.size()));
        lines.add("LookupSets   : " + lookupSets.size());
        lines.add("Matchers     : " + allMatchers.size());
        lines.add("Hashmap size : " + informMatcherActions.size());
        lines.add("Ranges map   : " + informMatcherActionRanges.size());
        lines.add("Testcases    : " + testCases.size());

        logVersion(lines);
    }

    protected AbstractUserAgentAnalyzerDirect() {
    }

    private boolean delayInitialization = true;
    public void delayInitialization() {
        delayInitialization = true;
    }

    public void immediateInitialization() {
        delayInitialization = false;
    }

    public AbstractUserAgentAnalyzerDirect setShowMatcherStats(boolean newShowMatcherStats) {
        this.showMatcherStats = newShowMatcherStats;
        return this;
    }

    public AbstractUserAgentAnalyzerDirect dropTests() {
        loadTests = false;
        testCases.clear();
        return this;
    }

    public AbstractUserAgentAnalyzerDirect keepTests() {
        loadTests = true;
        return this;
    }

    public boolean willKeepTests() {
        return loadTests;
    }

    public long getNumberOfTestCases() {
        return testCases.size();
    }

    // --------------------------------------------

    /**
     * In some cases it was found that simply dereferencing the instance and letting the GC clean it all up was "too hard".
     * To assist in these kinds of problem cases this method will wipe the internal data structures as much as possible.
     * After calling this method this instance becomes unusable and cannot be 'repaired'.
     * Normal applications will never need this. Simply dereferencing the analyzer will clean everything,
     * no memory leaks (that we know of).
     */
    public synchronized void destroy() {
        allMatchers.forEach(Matcher::destroy);
        allMatchers.clear();
        allMatchers.trimToSize();

        zeroInputMatchers.forEach(Matcher::destroy);
        zeroInputMatchers.clear();
        zeroInputMatchers.trimToSize();

        informMatcherActions.clear();
        matcherConfigs.clear();

        if (wantedFieldNames != null) {
            wantedFieldNames.clear();
        }

        testCases.clear();
        testCases.trimToSize();

        lookups.clear();
        lookupSets.clear();
        flattener.clear();
    }

    // --------------------------------------------

    public void loadResources(String resourceString) {
        loadResources(resourceString, true, false);
    }

    private Yaml createYaml() {
        final LoaderOptions yamlLoaderOptions = new LoaderOptions();
        yamlLoaderOptions.setMaxAliasesForCollections(100); // We use this many in the hacker/sql injection config.
        return new Yaml(yamlLoaderOptions);
    }

    public void loadResources(String resourceString, boolean showLoadMessages, boolean optionalResources) {
        if (matchersHaveBeenInitialized) {
            throw new IllegalStateException("Refusing to load additional resources after the datastructures have been initialized.");
        }

        // Because we are loading additional resources these caches must be invalidated
        allPossibleFieldNamesCache = null;
        allPossibleFieldNamesSortedCache = null;

        long startFiles = System.nanoTime();

        Yaml yaml = createYaml();

        final boolean loadingDefaultResources = DEFAULT_RESOURCES.equals(resourceString);

        Map<String, Resource> resources = new TreeMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resourceArray = resolver.getResources(resourceString);
            if (!loadingDefaultResources && showLoadMessages) {
                LOG.info("Loading {} rule files using expression: {}", resourceArray.length, resourceString);
            }
            for (Resource resource : resourceArray) {
                if (!loadingDefaultResources && showLoadMessages) {
                    LOG.info("- Preparing {} ({} bytes)", resource.getFilename(), resource.contentLength());
                }
                resources.put(resource.getFilename(), resource);
            }
        } catch (IOException e) {
            if (optionalResources) {
                LOG.error("The specified (optional) resource string is invalid: {}", resourceString);
                return;
            } else {
                throw new InvalidParserConfigurationException("Error reading resources: " + e.getMessage(), e);
            }
        }
        doingOnlyASingleTest = false;
        int maxFilenameLength = 0;

        // When using a framework like Quarkus loading resources can fail in mysterious ways.
        // Just trying to open a stream for one of the resources is enough to see if we can continue.
        if (!resources.isEmpty()) {
            Resource resource = resources.entrySet().iterator().next().getValue();
            try (InputStream ignored = resource.getInputStream()) {
                // Just seeing if opening this stream triggers an error.
                // Having a useless statement that references the 'ignored' to avoid checkstyle and compilation warnings.
                LOG.debug("Opening the resource worked. {}", ignored);
            } catch (IOException e) {
                LOG.error("Cannot load the resources (usually classloading problem).");
                LOG.error("- Resource   : {}", resource);
                LOG.error("- Filename   : {}", resource.getFilename());
                LOG.error("- Description: {}", resource.getDescription());
                if (loadingDefaultResources) {
                    LOG.warn("Falling back to the built in list of resources");
                    resources.clear();
                } else {
                    LOG.error("FATAL: Unable to load the specified resources for {}", resourceString);
                    throw new InvalidParserConfigurationException("Error reading resources (" + resourceString + "): " + e.getMessage(), e);
                }
            }
        }

        if (resources.isEmpty()) {
            if (optionalResources) {
                LOG.warn("NO optional resources were loaded from expression: {}", resourceString);
            } else {
                LOG.error("NO config files were found matching this expression: {}", resourceString);

                if (loadingDefaultResources) {
                    LOG.warn("Unable to load the default resources, usually caused by classloader problems.");
                    LOG.warn("Retrying with built in list.");
                    PackagedRules.getRuleFileNames().forEach(s -> loadResources(s, false, false));
                } else {
                    LOG.warn("If you are using wildcards in your expression then try explicitly naming all yamls files explicitly.");
                    throw new InvalidParserConfigurationException("There were no resources found for the expression: " + resourceString);
                }
            }
            return;
        }

        // We need to determine if we are trying to load the yaml files TWICE.
        // This can happen if the library is loaded twice (perhaps even two different versions).
        Set<String> resourceBasenames = resources
            .keySet().stream()
            .map(k -> k.replaceAll("^.*/", ""))
            .collect(Collectors.toSet());

        Set<String> alreadyLoadedResourceBasenames = matcherConfigs
            .keySet().stream()
            .map(k -> k.replaceAll("^.*/", ""))
            .collect(Collectors.toSet());

        alreadyLoadedResourceBasenames.retainAll(resourceBasenames);
        if (!alreadyLoadedResourceBasenames.isEmpty()) {
            LOG.error("Trying to load these {} resources for the second time: {}",
                alreadyLoadedResourceBasenames.size(), alreadyLoadedResourceBasenames);
            throw new InvalidParserConfigurationException("Trying to load " + alreadyLoadedResourceBasenames.size() +
                " resources for the second time");
        }

        for (Map.Entry<String, Resource> resourceEntry : resources.entrySet()) {
            try {
                Resource resource = resourceEntry.getValue();
                String filename = resource.getFilename();
                if (filename != null) {
                    maxFilenameLength = Math.max(maxFilenameLength, filename.length());
                    loadYaml(yaml, resource.getInputStream(), filename);
                }
            } catch (IOException e) {
                throw new InvalidParserConfigurationException("Error reading resources: " + e.getMessage(), e);
            }
        }

        long stopFiles = System.nanoTime();
        try(Formatter msg = new Formatter(Locale.ENGLISH)) {
            msg.format("- Loaded %2d files in %4d msec using expression: %s",
                resources.size(),
                (stopFiles - startFiles) / 1000000,
                resourceString);
            LOG.info("{}", msg);
        }

        if (resources.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

    }

    protected synchronized void finalizeLoadingRules() {
        logVersion();
        flattener = new UserAgentTreeFlattener(this);

        if (wantedFieldNames != null) {
            int wantedSize = wantedFieldNames.size();
            if (wantedFieldNames.contains(SET_ALL_FIELDS)) {
                wantedSize--;
            }
            LOG.info("Building all needed matchers for the requested {} fields.", wantedSize);
        } else {
            LOG.info("Building all matchers for all possible fields.");
        }

        if (matcherConfigs.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

        if (lookups != null && !lookups.isEmpty()) {
            // All compares are done in a case insensitive way. So we lowercase ALL keys of the lookups beforehand.
            Map<String, Map<String, String>> cleanedLookups = new LinkedHashMap<>(lookups.size());
            for (Map.Entry<String, Map<String, String>> lookupsEntry : lookups.entrySet()) {
                Map<String, String> cleanedLookup = new LinkedHashMap<>(lookupsEntry.getValue().size());
                for (Map.Entry<String, String> entry : lookupsEntry.getValue().entrySet()) {
                    cleanedLookup.put(entry.getKey().toLowerCase(), entry.getValue());
                }
                cleanedLookups.put(lookupsEntry.getKey(), cleanedLookup);
            }
            lookups = cleanedLookups;
        }

        if (!lookupSetMerge.isEmpty()) {
            lookupSetMerge.forEach((set, allExtraToLoad) -> {
                Set<String> theSet = lookupSets.get(set);
                if (theSet != null) {
                    allExtraToLoad.forEach(extraToLoad -> {
                        Map<String, String> extralookup = lookups.get(extraToLoad);
                        if (extralookup != null) {
                            theSet.addAll(extralookup.keySet());
                        }
                        Set<String> extralookupSet = lookupSets.get(extraToLoad);
                        if (extralookupSet != null) {
                            theSet.addAll(extralookupSet);
                        }
                    });
                }
            });
        }

        allMatchers.clear();
        for (Map.Entry<String, List<MappingNode>> matcherConfigEntry : matcherConfigs.entrySet()) {
            int skippedMatchers = 0;
            String configFilename = matcherConfigEntry.getKey();
            List<MappingNode> matcherConfig = matcherConfigEntry.getValue();
            if (matcherConfig == null) {
                continue; // No matchers in this file (probably only lookups and/or tests)
            }

            long start = System.nanoTime();
            int startSkipped = skippedMatchers;
            for (MappingNode map : matcherConfig) {
                try {
                    allMatchers.add(new Matcher(this, wantedFieldNames, map, configFilename));
                } catch (UselessMatcherException ume) {
                    skippedMatchers++;
                }
            }
            long stop = System.nanoTime();
            int stopSkipped = skippedMatchers;

            if (showMatcherStats) {
                try(Formatter msg = new Formatter(Locale.ENGLISH)) {
                    String format = "Loading %4d (dropped %4d) matchers from " +
                        "%-20s took %5d msec";
                    msg.format(format,
                        matcherConfig.size() - (stopSkipped - startSkipped),
                        stopSkipped - startSkipped,
                        configFilename,
                        (stop - start) / 1000000);
                    LOG.info("{}", msg);
                }
            }
        }

        verifyWeAreNotAskingForImpossibleFields();
        if (!delayInitialization) {
            initializeMatchers();
        }
    }

    protected void verifyWeAreNotAskingForImpossibleFields() {
        if (wantedFieldNames == null) {
            return; // Nothing to check
        }
        List<String> impossibleFields = new ArrayList<>();
        List<String> allPossibleFields = getAllPossibleFieldNamesSorted();

        for (String wantedFieldName: wantedFieldNames) {
            if (isSystemField(wantedFieldName)) {
                continue; // These are fine
            }
            if (!allPossibleFields.contains(wantedFieldName)) {
                impossibleFields.add(wantedFieldName);
            }
        }
        if (impossibleFields.isEmpty()) {
            return;
        }
        throw new InvalidParserConfigurationException("We cannot provide these fields:" + impossibleFields.toString());
    }

    private boolean matchersHaveBeenInitialized = false;
    public void initializeMatchers() {
        if (matchersHaveBeenInitialized) {
            return;
        }
        LOG.info("Initializing Analyzer data structures");

        if (allMatchers.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

        long start = System.nanoTime();
        allMatchers.forEach(Matcher::initialize);
        long stop = System.nanoTime();

        matchersHaveBeenInitialized = true;
        LOG.info("Built in {} msec : Hashmap {}, Ranges map:{}",
            (stop - start) / 1000000,
            informMatcherActions.size(),
            informMatcherActionRanges.size());

        for (Matcher matcher: allMatchers) {
            if (matcher.getActionsThatRequireInput() == 0) {
                zeroInputMatchers.add(matcher);
            }
        }

        // Reset all Matchers
        for (Matcher matcher : allMatchers) {
            matcher.reset();
        }

        touchedMatchers = new MatcherList(16);
    }

    private transient volatile Set<String> allPossibleFieldNamesCache = null; //NOSONAR: The getter avoids the java:S3077 issues
    public Set<String> getAllPossibleFieldNames() {
        if (allPossibleFieldNamesCache == null) {
            synchronized (this) {
                if (allPossibleFieldNamesCache == null) {
                    Set<String> names = new TreeSet<>(HARD_CODED_GENERATED_FIELDS);
                    for (Matcher matcher : allMatchers) {
                        names .addAll(matcher.getAllPossibleFieldNames());
                    }
                    allPossibleFieldNamesCache = names;
                }
            }
        }
        return allPossibleFieldNamesCache;
    }

    private transient volatile List<String> allPossibleFieldNamesSortedCache = null; //NOSONAR: The getter avoids the java:S3077 issues
    public List<String> getAllPossibleFieldNamesSorted() {
        if (allPossibleFieldNamesSortedCache == null) {
            synchronized (this) {
                if (allPossibleFieldNamesSortedCache == null) {
                    List<String> fieldNames = new ArrayList<>(getAllPossibleFieldNames());
                    Collections.sort(fieldNames);

                    List<String> names = new ArrayList<>();
                    for (String fieldName : PRE_SORTED_FIELDS_LIST) {
                        fieldNames.remove(fieldName);
                        names.add(fieldName);
                    }
                    names.addAll(fieldNames);
                    allPossibleFieldNamesSortedCache = names;
                }
            }
        }
        return allPossibleFieldNamesSortedCache;
    }

/*
Example of the structure of the yaml file:
----------------------------
config:
  - lookup:
    name: 'lookupname'
    map:
        "From1" : "To1"
        "From2" : "To2"
        "From3" : "To3"
  - matcher:
      options:
        - 'verbose'
        - 'init'
      require:
        - 'Require pattern'
        - 'Require pattern'
      extract:
        - 'Extract pattern'
        - 'Extract pattern'
  - test:
      input:
        user_agent_string: 'Useragent'
      expected:
        FieldName     : 'ExpectedValue'
        FieldName     : 'ExpectedValue'
        FieldName     : 'ExpectedValue'
----------------------------
*/

    void loadYaml(String yamlString, String filename) {
        loadYaml(createYaml(), new ByteArrayInputStream(yamlString.getBytes(UTF_8)), filename);
    }

    private synchronized void loadYaml(Yaml yaml, InputStream yamlStream, String filename) {
        Node loadedYaml;
        try {
            loadedYaml = yaml.compose(new UnicodeReader(yamlStream));
        } catch (Exception e) {
            throw new InvalidParserConfigurationException("Parse error in the file " + filename + ": " + e.getMessage(), e);
        }

        if (loadedYaml == null) {
            LOG.warn("The file {} is empty", filename);
            return;
        }

        // Get and check top level config
        requireNodeInstanceOf(MappingNode.class, loadedYaml, filename, "File must be a Map");

        MappingNode rootNode = (MappingNode) loadedYaml;

        NodeTuple configNodeTuple = null;
        for (NodeTuple tuple : rootNode.getValue()) {
            String name = getKeyAsString(tuple, filename);
            if ("config".equals(name)) {
                configNodeTuple = tuple;
                break;
            }
            if ("version".equals(name)) {
                // Check the version information from the Yaml files
                assertSameVersion(tuple, filename);
                return;
            }
        }

        require(configNodeTuple != null, loadedYaml, filename, "The top level entry MUST be 'config'.");

        SequenceNode configNode = getValueAsSequenceNode(configNodeTuple, filename);
        List<Node> configList = configNode.getValue();

        for (Node configEntry : configList) {
            requireNodeInstanceOf(MappingNode.class, configEntry, filename, "The entry MUST be a mapping");
            NodeTuple entry = getExactlyOneNodeTuple((MappingNode) configEntry, filename);
            MappingNode actualEntry = getValueAsMappingNode(entry, filename);
            String entryType = getKeyAsString(entry, filename);
            switch (entryType) {
                case "lookup":
                    loadYamlLookup(actualEntry, filename);
                    break;
                case "set":
                    loadYamlLookupSets(actualEntry, filename);
                    break;
                case "matcher":
                    loadYamlMatcher(actualEntry, filename);
                    break;
                case "test":
                    if (loadTests) {
                        loadYamlTestcase(actualEntry, filename);
                    }
                    break;
                default:
                    throw new InvalidParserConfigurationException(
                        "Yaml config.(" + filename + ":" + actualEntry.getStartMark().getLine() + "): " +
                            "Found unexpected config entry: " + entryType + ", allowed are 'lookup', 'set', 'matcher' and 'test'");
            }
        }
    }

    private void loadYamlLookup(MappingNode entry, String filename) {
        String name = null;
        Map<String, String> map = null;

        for (NodeTuple tuple : entry.getValue()) {
            switch (getKeyAsString(tuple, filename)) {
                case "name":
                    name = getValueAsString(tuple, filename);
                    break;
                case "map":
                    if (map == null) {
                        map = new HashMap<>();
                    }
                    List<NodeTuple> mappings = getValueAsMappingNode(tuple, filename).getValue();
                    for (NodeTuple mapping : mappings) {
                        String key = getKeyAsString(mapping, filename);
                        String value = getValueAsString(mapping, filename);

                        if (map.containsKey(key)) {
                            throw new InvalidParserConfigurationException(
                                "In the lookup \"" + name + "\" the key \"" + key + "\" appears multiple times.");
                        }

                        map.put(key, value);
                    }
                    break;
                default:
                    break;
            }
        }

        require(name != null && map != null, entry, filename, "Invalid lookup specified");

        lookups.put(name, map);
    }

    private void loadYamlLookupSets(MappingNode entry, String filename) {
        String name = null;
        Set<String> lookupSet = new LinkedHashSet<>();

        Set<String> merge = new LinkedHashSet<>();

        for (NodeTuple tuple : entry.getValue()) {
            switch (getKeyAsString(tuple, filename)) {
                case "name":
                    name = getValueAsString(tuple, filename);
                    break;
                case "merge":
                    merge.addAll(getStringValues(getValueAsSequenceNode(tuple, filename), filename));
                    break;
                case "values":
                    SequenceNode node = getValueAsSequenceNode(tuple, filename);
                    for (String value: getStringValues(node, filename)) {
                        lookupSet.add(value.toLowerCase(Locale.ENGLISH));
                    }
                    break;
                default:
                    break;
            }
        }

        if (!merge.isEmpty()) {
            lookupSetMerge.put(name, merge);
        }

        lookupSets.put(name, lookupSet);
    }

    private void loadYamlMatcher(MappingNode entry, String filename) {
        List<MappingNode> matcherConfigList = matcherConfigs
            .computeIfAbsent(filename, k -> new ArrayList<>(32));
        matcherConfigList.add(entry);
    }

    private void loadYamlTestcase(MappingNode entry, String filename) {
        if (!doingOnlyASingleTest) {
            Map<String, String> metaData = new HashMap<>();
            metaData.put("filename", filename);
            metaData.put("fileline", String.valueOf(entry.getStartMark().getLine()));

            Map<String, String> input = null;
            List<String> options = null;
            Map<String, String> expected = new LinkedHashMap<>();
            for (NodeTuple tuple : entry.getValue()) {
                String name = getKeyAsString(tuple, filename);
                switch (name) {
                    case "options":
                        options = getStringValues(tuple.getValueNode(), filename);
                        if (options.contains("only")) {
                            doingOnlyASingleTest = true;
                            testCases.clear();
                        }
                        break;
                    case "input":
                        for (NodeTuple inputTuple : getValueAsMappingNode(tuple, filename).getValue()) {
                            String inputName = getKeyAsString(inputTuple, filename);
                            if ("user_agent_string".equals(inputName)) {
                                String inputString = getValueAsString(inputTuple, filename);
                                input = new HashMap<>();
                                input.put(inputName, inputString);
                            }
                        }
                        break;
                    case "expected":
                        List<NodeTuple> mappings = getValueAsMappingNode(tuple, filename).getValue();
                        for (NodeTuple mapping : mappings) {
                            String key = getKeyAsString(mapping, filename);
                            String value = getValueAsString(mapping, filename);
                            expected.put(key, value);
                        }
                        break;
                    default:
                        // Ignore
                        break;
                }
            }

            require(input != null, entry, filename, "Test is missing input");

            if (expected.isEmpty()) {
                doingOnlyASingleTest = true;
                testCases.clear();
            }

            Map<String, Map<String, String>> testCase = new LinkedHashMap<>();

            testCase.put("input", input);
            if (!expected.isEmpty()) {
                testCase.put("expected", expected);
            }
            if (options != null) {
                Map<String, String> optionsMap = new LinkedHashMap<>(options.size());
                for (String option: options) {
                    optionsMap.put(option, option);
                }
                testCase.put("options", optionsMap);
            }
            testCase.put("metaData", metaData);
            testCases.add(testCase);
        }

    }

    // These are the actual subrange we need for the paths.
    private final Map<String, Set<Range>> informMatcherActionRanges = new HashMap<>(10000);
    @Override
    public void lookingForRange(String treeName, Range range) {
        Set<Range> ranges = informMatcherActionRanges.computeIfAbsent(treeName, k -> new LinkedHashSet<>(4));
        ranges.add(range);
    }

    // We do not want to put ALL lengths in the hashmap for performance reasons
    public static final int MAX_PREFIX_HASH_MATCH = 3;

    // Calculate the max length we will put in the hashmap.
    public static int firstCharactersForPrefixHashLength(String input, int maxChars) {
        return Math.min(maxChars, Math.min(MAX_PREFIX_HASH_MATCH, input.length()));
    }

    public static String firstCharactersForPrefixHash(String input, int maxChars) {
        return input.substring(0, firstCharactersForPrefixHashLength(input, maxChars));
    }

    // These are the paths for which we have prefix requests.
    private final Map<String, Set<Integer>> informMatcherActionPrefixesLengths = new HashMap<>(1000);

    @Override
    public void informMeAboutPrefix(MatcherAction matcherAction, String treeName, String prefix) {
        this.informMeAbout(matcherAction, treeName + "{\"" + firstCharactersForPrefixHash(prefix, MAX_PREFIX_HASH_MATCH) + "\"");
        Set<Integer> lengths = informMatcherActionPrefixesLengths.computeIfAbsent(treeName, k -> new LinkedHashSet<>(4));
        lengths.add(firstCharactersForPrefixHashLength(prefix, MAX_PREFIX_HASH_MATCH));
    }

    @Override
    public Set<Integer> getRequiredPrefixLengths(String treeName) {
        return informMatcherActionPrefixesLengths.get(treeName);
    }

    public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
        String hashKey = keyPattern.toLowerCase();
        Set<MatcherAction> analyzerSet = informMatcherActions
            .computeIfAbsent(hashKey, k -> new LinkedHashSet<>());
        analyzerSet.add(matcherAction);
    }

    private boolean verbose = false;

    public synchronized void setVerbose(boolean newVerbose) {
        this.verbose = newVerbose;
        flattener.setVerbose(newVerbose);
    }

    public void setUserAgentMaxLength(int newUserAgentMaxLength) {
        if (newUserAgentMaxLength <= 0) {
            userAgentMaxLength = DEFAULT_USER_AGENT_MAX_LENGTH;
        } else {
            userAgentMaxLength = newUserAgentMaxLength;
        }
    }

    public int getUserAgentMaxLength() {
        return this.userAgentMaxLength;
    }

    private void setAsHacker(MutableUserAgent userAgent, int confidence) {
        userAgent.set(DEVICE_CLASS,                 "Hacker",  confidence);
        userAgent.set(DEVICE_BRAND,                 "Hacker",  confidence);
        userAgent.set(DEVICE_NAME,                  "Hacker",  confidence);
        userAgent.set(DEVICE_VERSION,               "Hacker",  confidence);
        userAgent.set(OPERATING_SYSTEM_CLASS,       "Hacker",  confidence);
        userAgent.set(OPERATING_SYSTEM_NAME,        "Hacker",  confidence);
        userAgent.set(OPERATING_SYSTEM_VERSION,     "Hacker",  confidence);
        userAgent.set(LAYOUT_ENGINE_CLASS,          "Hacker",  confidence);
        userAgent.set(LAYOUT_ENGINE_NAME,           "Hacker",  confidence);
        userAgent.set(LAYOUT_ENGINE_VERSION,        "Hacker",  confidence);
        userAgent.set(LAYOUT_ENGINE_VERSION_MAJOR,  "Hacker",  confidence);
        userAgent.set(AGENT_CLASS,                  "Hacker",  confidence);
        userAgent.set(AGENT_NAME,                   "Hacker",  confidence);
        userAgent.set(AGENT_VERSION,                "Hacker",  confidence);
        userAgent.set(AGENT_VERSION_MAJOR,          "Hacker",  confidence);
        userAgent.set(HACKER_TOOLKIT,               "Unknown", confidence);
        userAgent.set(HACKER_ATTACK_VECTOR,         "Unknown", confidence);
    }

    private transient MatcherList touchedMatchers = null;

    @Override
    public void receivedInput(Matcher matcher) {
        if (zeroInputMatchers.contains(matcher)) {
            return;
        }
        touchedMatchers.add(matcher);
    }

    /**
     * Resets the state of the Analyzer to the default state.
     */
    public void reset() {
        // Reset all Matchers
        for (Matcher matcher : touchedMatchers) {
            matcher.reset();
        }
        touchedMatchers.clear();

        for (Matcher matcher : zeroInputMatchers) {
            matcher.reset();
        }
    }

    /**
     * Parses and analyzes the provided useragent string
     * @param userAgentString The User-Agent String that is to be parsed and analyzed
     * @return An ImmutableUserAgent record that holds all of the results.
     */
    public ImmutableUserAgent parse(String userAgentString) {
        MutableUserAgent userAgent = new MutableUserAgent(userAgentString, wantedFieldNames);
        return parse(userAgent);
    }

    /**
     * Parses and analyzes the useragent string provided in the MutableUserAgent instance.
     * NOTE: This method is synchronized because the way the analyzer works is not reentrant.
     * @param userAgent The MutableUserAgent instance that is to be parsed and that gets all results
     * @return An ImmutableUserAgent copy of the results that is suitable for further usage and caching.
     */
    public synchronized ImmutableUserAgent parse(MutableUserAgent userAgent) {
        initializeMatchers();
        String useragentString = userAgent.getUserAgentString();
        if (useragentString != null && useragentString.length() > userAgentMaxLength) {
            setAsHacker(userAgent, 100);
            userAgent.setForced(HACKER_ATTACK_VECTOR, "Buffer overflow", 100);
            return new ImmutableUserAgent(hardCodedPostProcessing(userAgent));
        }

        // Reset all Matchers
        reset();

        if (userAgent.isDebug()) {
            for (Matcher matcher : allMatchers) {
                matcher.setVerboseTemporarily(true);
            }
        }

        try {
            userAgent = flattener.parse(userAgent);

            inform(SYNTAX_ERROR, userAgent.getValue(SYNTAX_ERROR), null);

            if (verbose) {
                LOG.info("=========== Checking all Touched Matchers: {}", touchedMatchers.size());
            }
            // Fire all Analyzers with any input
            for (Matcher matcher : touchedMatchers) {
                matcher.analyze(userAgent);
            }

            if (verbose) {
                LOG.info("=========== Checking all Zero Input Matchers: {}", zeroInputMatchers.size());
            }
            // Fire all Analyzers that should not get input
            for (Matcher matcher : zeroInputMatchers) {
                matcher.analyze(userAgent);
            }

            userAgent.processSetAll();
        } catch (RuntimeException rte) {
            // If this occurs then someone has found a previously undetected problem.
            // So this is a safety for something that 'can' but 'should not' occur.
            userAgent.reset();
            setAsHacker(userAgent, 10000);
            userAgent.setForced(HACKER_ATTACK_VECTOR, "Yauaa Exploit", 10000);
        }
        return new ImmutableUserAgent(hardCodedPostProcessing(userAgent));
    }

    private static final List<String> HARD_CODED_GENERATED_FIELDS = new ArrayList<>();

    static {
        HARD_CODED_GENERATED_FIELDS.add(SYNTAX_ERROR);
        HARD_CODED_GENERATED_FIELDS.add(AGENT_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(LAYOUT_ENGINE_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(AGENT_NAME_VERSION);
        HARD_CODED_GENERATED_FIELDS.add(AGENT_NAME_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(LAYOUT_ENGINE_NAME_VERSION);
        HARD_CODED_GENERATED_FIELDS.add(LAYOUT_ENGINE_NAME_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(OPERATING_SYSTEM_NAME_VERSION);
        HARD_CODED_GENERATED_FIELDS.add(OPERATING_SYSTEM_NAME_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(WEBVIEW_APP_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(WEBVIEW_APP_NAME_VERSION_MAJOR);
    }

    public boolean isWantedField(String fieldName) {
        if (wantedFieldNames == null) {
            return true;
        }
        return wantedFieldNames.contains(fieldName);
    }

    public Set<String> getWantedFieldNames(){
        return wantedFieldNames;
    }

    private final List<FieldCalculator> fieldCalculators = new ArrayList<>();

    protected void setFieldCalculators(List<FieldCalculator> newFieldCalculators) {
        fieldCalculators.addAll(newFieldCalculators);
    }

    private MutableUserAgent hardCodedPostProcessing(MutableUserAgent userAgent) {
        // If it is really really bad ... then it is a Hacker.
        if ("true".equals(userAgent.getValue(SYNTAX_ERROR))) {
            if (userAgent.get(DEVICE_CLASS).getConfidence() == -1) {
                setAsHacker(userAgent, 10);
            }
        }

        // Calculate all fields that are constructed from the found ones.
        for (FieldCalculator fieldCalculator: fieldCalculators) {
            if (verbose) {
                LOG.info("Running FieldCalculator: {}", fieldCalculator);
            }
            fieldCalculator.calculate(userAgent);
        }

        return userAgent;
    }

    public Set<Range> getRequiredInformRanges(String treeName) {
        return informMatcherActionRanges.computeIfAbsent(treeName, k -> Collections.emptySet());
    }

    public void inform(String key, String value, ParseTree ctx) {
        inform(key, key, value, ctx);
        inform(key + "=\"" + value + '"', key, value, ctx);

        Set<Integer> lengths = getRequiredPrefixLengths(key);
        if (lengths != null) {
            int valueLength = value.length();
            for (Integer prefixLength : lengths) {
                if (valueLength >= prefixLength) {
                    inform(key + "{\"" + firstCharactersForPrefixHash(value, prefixLength) + '"', key, value, ctx);
                }
            }
        }
    }

    private void inform(String match, String key, String value, ParseTree ctx) {
        Set<MatcherAction> relevantActions = informMatcherActions.get(match.toLowerCase(Locale.ENGLISH));
        if (verbose) {
            if (relevantActions == null) {
                LOG.info("--- Have (0): {}", match);
            } else {
                LOG.info("+++ Have ({}): {}", relevantActions.size(), match);

                int count = 1;
                for (MatcherAction action : relevantActions) {
                    LOG.info("+++ -------> ({}): {}", count, action);
                    count++;
                }
            }
        }

        if (relevantActions != null) {
            for (MatcherAction matcherAction : relevantActions) {
                matcherAction.inform(key, value, ctx);
            }
        }
    }


    /**
     * Runs all testcases once to heat up the JVM.
     * @return Number of actually done testcases.
     */
    public long preHeat() {
        return preHeat(testCases.size(), true);
    }
    /**
     * Runs the number of specified testcases to heat up the JVM.
     * @param preheatIterations Number of desired tests to run.
     * @return Number of actually done testcases.
     */
    public long preHeat(long preheatIterations) {
        return preHeat(preheatIterations, true);
    }

    private static final long MAX_PRE_HEAT_ITERATIONS = 1_000_000L;

    /**
     * Runs the number of specified testcases to heat up the JVM.
     * @param preheatIterations Number of desired tests to run.
     * @param log Enable logging?
     * @return Number of actually done testcases.
     */
    public long preHeat(long preheatIterations, boolean log) {
        if (testCases.isEmpty()) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because there are no test cases available.");
            return 0;
        }
        if (preheatIterations <= 0) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because you asked for {} to run.", preheatIterations);
            return 0;
        }
        if (preheatIterations > MAX_PRE_HEAT_ITERATIONS) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because you asked for too many ({} > {}) to run.", preheatIterations, MAX_PRE_HEAT_ITERATIONS);
            return 0;
        }
        if (log) {
            LOG.info("Preheating JVM by running {} testcases.", preheatIterations);
        }
        long remainingIterations = preheatIterations;
        long goodResults = 0;
        while (remainingIterations > 0) {
            for (Map<String, Map<String, String>> test : testCases) {
                Map<String, String> input = test.get("input");
                String userAgentString = input.get("user_agent_string");
                remainingIterations--;
                // Calculate and use result to guarantee not optimized away.
                if(!AbstractUserAgentAnalyzerDirect.this.parse(userAgentString).hasSyntaxError()) {
                    goodResults++;
                }
                if (remainingIterations <= 0) {
                    break;
                }
            }
        }
        if (log) {
            LOG.info("Preheating JVM completed. ({} of {} were proper results)", goodResults, preheatIterations);
        }
        return preheatIterations;
    }

    // ===============================================================================================================

    public static class GetAllPathsAnalyzer implements Analyzer {
        private final List<String> values = new ArrayList<>(128);

        private final UserAgent result;

        GetAllPathsAnalyzer(String useragent) {
            UserAgentTreeFlattener flattener = new UserAgentTreeFlattener(this);
            result = flattener.parse(useragent);
        }

        public List<String> getValues() {
            return values;
        }

        public UserAgent getResult() {
            return result;
        }

        public void inform(String path, String value, ParseTree ctx) {
            values.add(path);
            values.add(path + "=\"" + value + "\"");
            values.add(path + "{\"" + firstCharactersForPrefixHash(value, MAX_PREFIX_HASH_MATCH) + "\"");
        }

        public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
            // Not needed to only get all paths
        }

        public void lookingForRange(String treeName, Range range) {
            // Not needed to only get all paths
        }

        public Set<Range> getRequiredInformRanges(String treeName) {
            // Not needed to only get all paths
            return Collections.emptySet();
        }

        @Override
        public void informMeAboutPrefix(MatcherAction matcherAction, String treeName, String prefix) {
            // Not needed to only get all paths
        }

        @Override
        public Set<Integer> getRequiredPrefixLengths(String treeName) {
            // Not needed to only get all paths
            return Collections.emptySet();
        }

        @Override
        public Map<String, Map<String, String>> getLookups() {
            // Not needed to only get all paths
            return Collections.emptyMap();
        }

        @Override
        public Map<String, Set<String>> getLookupSets() {
            // Not needed to only get all paths
            return Collections.emptyMap();
        }

        @Override
        public List<Map<String, Map<String, String>>> getTestCases() {
            return Collections.emptyList();
        }
    }

    public static List<String> getAllPaths(String agent) {
        return new GetAllPathsAnalyzer(agent).getValues();
    }

    public static GetAllPathsAnalyzer getAllPathsAnalyzer(String agent) {
        return new GetAllPathsAnalyzer(agent);
    }

    // ===============================================================================================================

    @SuppressWarnings("unchecked") // For all the casts of 'this' to 'B'
    public abstract static class AbstractUserAgentAnalyzerDirectBuilder<UAA extends AbstractUserAgentAnalyzerDirect, B extends AbstractUserAgentAnalyzerDirectBuilder<UAA, B>> {
        private final UAA uaa;
        private boolean didBuildStep = false;
        private int preheatIterations = 0;

        private final List<String>          resources         = new ArrayList<>();
        private final List<String>          optionalResources = new ArrayList<>();
        private final List<String>          yamlRules         = new ArrayList<>();
        private final List<FieldCalculator> fieldCalculators  = new ArrayList<>();

        protected void failIfAlreadyBuilt() {
            if (didBuildStep) {
                throw new IllegalStateException("A builder can provide only a single instance. It is not allowed to set values after doing build()");
            }
        }

        protected AbstractUserAgentAnalyzerDirectBuilder(UAA newUaa) {
            this.uaa = newUaa;
            this.uaa.setShowMatcherStats(false);
            resources.add(DEFAULT_RESOURCES);
        }

        /**
         * Drop the default set of rules. Useful in parsing ONLY company specific useragents.
         * @return the current Builder instance.
         */
        public B dropDefaultResources() {
            failIfAlreadyBuilt();
            resources.remove(DEFAULT_RESOURCES);
            return (B)this;
        }

        /**
         * Add a set of additional rules. Useful in handling specific cases.
         * @param resourceString The resource list that needs to be added.
         * @return the current Builder instance.
         */
        public B addResources(String resourceString) {
            failIfAlreadyBuilt();
            resources.add(resourceString);
            return (B)this;
        }

        /**
         * Add a set of additional rules. Useful in handling specific cases.
         * The startup will continue even if these do not exist.
         * @param resourceString The resource list that should to be added.
         * @return the current Builder instance.
         */
        public B addOptionalResources(String resourceString) {
            failIfAlreadyBuilt();
            optionalResources.add(resourceString);
            return (B)this;
        }

        /**
         * Add a set of additional rules. Useful in handling specific cases.
         * The startup will continue even if these do not exist.
         * @param yamlRule The Yaml expression that should to be added.
         * @return the current Builder instance.
         */
        public B addYamlRule(String yamlRule) {
            failIfAlreadyBuilt();
            yamlRules.add(yamlRule);
            return (B)this;
        }

        /**
         * Use the available testcases to preheat the jvm on this analyzer.
         * @param iterations How many testcases must be run
         * @return the current Builder instance.
         */
        public B preheat(int iterations) {
            failIfAlreadyBuilt();
            this.preheatIterations = iterations;
            return (B)this;
        }

        /**
         * Use the available testcases to preheat the jvm on this analyzer.
         * All available testcases will be run exactly once.
         * @return the current Builder instance.
         */
        public B preheat() {
            failIfAlreadyBuilt();
            this.preheatIterations = -1;
            return (B)this;
        }

        /**
         * Specify an additional field that we want to retrieve.
         * @param fieldName The name of the additional field
         * @return the current Builder instance.
         */
        public B withField(String fieldName) {
            failIfAlreadyBuilt();
            if (uaa.wantedFieldNames == null) {
                uaa.wantedFieldNames = new TreeSet<>(); // This avoids duplicates and maintains ordering.
            }
            uaa.wantedFieldNames.add(fieldName);
            return (B)this;
        }

        /**
         * Specify a set of additional fields that we want to retrieve.
         * @param fieldNames The collection of names of the additional fields
         * @return the current Builder instance.
         */
        public B withFields(Collection<String> fieldNames) {
            for (String fieldName : fieldNames) {
                withField(fieldName);
            }
            return (B)this;
        }

        /**
         * Specify a set of additional fields that we want to retrieve.
         * @param fieldNames The array of names of the additional fields
         * @return the current Builder instance.
         */
        public B withFields(String... fieldNames) {
            for (String fieldName : fieldNames) {
                withField(fieldName);
            }
            return (B)this;
        }

        /**
         * Specify that we simply want to retrieve all possible fields.
         * @return the current Builder instance.
         */
        public B withAllFields() {
            failIfAlreadyBuilt();
            uaa.wantedFieldNames = null;
            return (B)this;
        }

        /**
         * Log additional information during the startup of the analyzer.
         * @return the current Builder instance.
         */
        public B showMatcherLoadStats() {
            failIfAlreadyBuilt();
            uaa.setShowMatcherStats(true);
            return (B)this;
        }

        /**
         * Set the stats logging during the startup of the analyzer back to the default of "minimal".
         * @return the current Builder instance.
         */
        public B hideMatcherLoadStats() {
            failIfAlreadyBuilt();
            uaa.setShowMatcherStats(false);
            return (B)this;
        }

        /**
         * Set maximum length of a useragent for it to be classified as Hacker without any analysis.
         * @param newUserAgentMaxLength The new maximum length of a useragent for it to be classified as Hacker without any analysis.
         * @return the current Builder instance.
         */
        public B withUserAgentMaxLength(int newUserAgentMaxLength) {
            failIfAlreadyBuilt();
            uaa.setUserAgentMaxLength(newUserAgentMaxLength);
            return (B)this;
        }

        /**
         * Retain all testcases in memory after initialization.
         * @return the current Builder instance.
         */
        public B keepTests() {
            failIfAlreadyBuilt();
            uaa.keepTests();
            return (B)this;
        }

        /**
         * Remove all testcases in memory after initialization.
         * @return the current Builder instance.
         */
        public B dropTests() {
            failIfAlreadyBuilt();
            uaa.dropTests();
            return (B)this;
        }

        /**
         * Load all patterns and rules but do not yet build the lookup hashMaps yet.
         * For the engine to run these lookup hashMaps are needed so they will be constructed once "just in time".
         * @return the current Builder instance.
         */
        public B delayInitialization() {
            failIfAlreadyBuilt();
            uaa.delayInitialization();
            return (B)this;
        }

        /**
         * Load all patterns and rules and immediately build the lookup hashMaps.
         * @return the current Builder instance.
         */
        public B immediateInitialization() {
            failIfAlreadyBuilt();
            uaa.immediateInitialization();
            return (B)this;
        }

        protected Set<String> allFieldsForWhichACalculatorExists = new HashSet<>();

        private void registerFieldCalculator(FieldCalculator fieldCalculator) {
            String calculatedFieldName = fieldCalculator.getCalculatedFieldName();
            allFieldsForWhichACalculatorExists.add(calculatedFieldName);
            if (uaa.isWantedField(calculatedFieldName)) {
                fieldCalculators.add(fieldCalculator);
                if (uaa.wantedFieldNames != null) {
                    uaa.wantedFieldNames.addAll(fieldCalculator.getDependencies());
                }
            }
        }

        protected void verifyCalculatorDependencyOrdering() {
            // Verify calculator dependencies ordering
            Set<String> seenCalculatedFields = new HashSet<>();
            for (FieldCalculator fieldCalculator: fieldCalculators) {
                for (String dependency: fieldCalculator.getDependencies()){
                    if (allFieldsForWhichACalculatorExists.contains(dependency)) {
                        if (!seenCalculatedFields.contains(dependency)) {
                            throw new InvalidParserConfigurationException(
                                "Calculator ordering is wrong:" +
                                    "For " + fieldCalculator.getCalculatedFieldName() +
                                    " we need " + dependency + " which is a " +
                                    "calculated field but it has not yet been calculated.");
                        }
                    }
                }
                seenCalculatedFields.add(fieldCalculator.getCalculatedFieldName());
            }
        }

        /**
         * Construct the analyzer and run the preheat (if requested).
         * @return the new analyzer instance.
         */
        public UAA build() {
            failIfAlreadyBuilt();
            uaa.initTransientFields();

            // In case we only want specific fields we must all these special cases too
            if (uaa.wantedFieldNames != null) {
                // Special field that affects ALL fields.
                uaa.wantedFieldNames.add(SET_ALL_FIELDS);

                // This is always needed to determine the Hacker fallback
                uaa.wantedFieldNames.add(DEVICE_CLASS);
            }

            registerFieldCalculator(new ConcatNONDuplicatedCalculator(AGENT_NAME_VERSION_MAJOR,         AGENT_NAME,             AGENT_VERSION_MAJOR));
            registerFieldCalculator(new ConcatNONDuplicatedCalculator(AGENT_NAME_VERSION,               AGENT_NAME,             AGENT_VERSION));
            registerFieldCalculator(new MajorVersionCalculator(AGENT_VERSION_MAJOR,                     AGENT_VERSION));

            registerFieldCalculator(new ConcatNONDuplicatedCalculator(WEBVIEW_APP_NAME_VERSION_MAJOR,   WEBVIEW_APP_NAME,       WEBVIEW_APP_VERSION_MAJOR));
            registerFieldCalculator(new MajorVersionCalculator(WEBVIEW_APP_VERSION_MAJOR,               WEBVIEW_APP_VERSION));

            registerFieldCalculator(new ConcatNONDuplicatedCalculator(LAYOUT_ENGINE_NAME_VERSION_MAJOR, LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION_MAJOR));
            registerFieldCalculator(new ConcatNONDuplicatedCalculator(LAYOUT_ENGINE_NAME_VERSION,       LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION));
            registerFieldCalculator(new MajorVersionCalculator(LAYOUT_ENGINE_VERSION_MAJOR,             LAYOUT_ENGINE_VERSION));

            registerFieldCalculator(new MajorVersionCalculator(OPERATING_SYSTEM_NAME_VERSION_MAJOR,     OPERATING_SYSTEM_NAME_VERSION));
            registerFieldCalculator(new ConcatNONDuplicatedCalculator(OPERATING_SYSTEM_NAME_VERSION,    OPERATING_SYSTEM_NAME,  OPERATING_SYSTEM_VERSION));
            registerFieldCalculator(new MajorVersionCalculator(OPERATING_SYSTEM_VERSION_MAJOR,          OPERATING_SYSTEM_VERSION));

            registerFieldCalculator(new CalculateAgentClass());
            registerFieldCalculator(new CalculateAgentName());
            registerFieldCalculator(new CalculateNetworkType());
            registerFieldCalculator(new CalculateDeviceName());
            registerFieldCalculator(new CalculateDeviceBrand());
            registerFieldCalculator(new CalculateAgentEmail());

            Collections.reverse(fieldCalculators);
            uaa.setFieldCalculators(fieldCalculators);

            boolean mustDropTestsLater = !uaa.willKeepTests();
            if (preheatIterations != 0) {
                uaa.keepTests();
            }

            optionalResources.forEach(resource -> uaa.loadResources(resource, true, true));
            resources.forEach(resource -> uaa.loadResources(resource, true, false));

            int yamlRuleCount = 1;
            for (String yamlRule : yamlRules) {
                uaa.loadYaml(yamlRule, "Manually Inserted Rules " + yamlRuleCount++);
            }

            uaa.finalizeLoadingRules();
            if (preheatIterations < 0) {
                uaa.preHeat();
            } else {
                if (preheatIterations > 0) {
                    uaa.preHeat(preheatIterations);
                }
            }
            if (mustDropTestsLater) {
                uaa.dropTests();
            }
            didBuildStep = true;
            return uaa;
        }
    }

    @Override
    public String toString() {
        return "UserAgentAnalyzerDirect{" +
            "\nallMatchers=" + allMatchers +
            "\n, zeroInputMatchers=" + zeroInputMatchers +
            "\n, informMatcherActions=" + informMatcherActions +
            "\n, showMatcherStats=" + showMatcherStats +
            "\n, doingOnlyASingleTest=" + doingOnlyASingleTest +
            "\n, wantedFieldNames=" + wantedFieldNames +
            "\n, testCases=" + testCases +
            "\n, lookups=" + lookups +
            "\n, lookupSets=" + lookupSets +
            "\n, flattener=" + flattener +
            "\n, userAgentMaxLength=" + userAgentMaxLength +
            "\n, loadTests=" + loadTests +
            "\n, delayInitialization=" + delayInitialization +
            "\n, matchersHaveBeenInitialized=" + matchersHaveBeenInitialized +
//            "\n, informMatcherActionRanges=" + ToString.toString(informMatcherActionRanges) +
//            "\n, informMatcherActionPrefixesLengths=" + ToString.toString(informMatcherActionPrefixesLengths) +
            "\n, verbose=" + verbose +
            "\n, fieldCalculators=" + fieldCalculators +
            "\n}";
    }
}
