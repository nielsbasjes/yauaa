/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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

package nl.basjes.parse.useragent.annotate;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAgentAnnotationAnalyzer<T> {
    private UserAgentAnnotationMapper<T> mapper = null;
    private UserAgentAnalyzer userAgentAnalyzer = null;
    private static final Logger LOG = LoggerFactory.getLogger(UserAgentAnnotationAnalyzer.class);

    private final Map<String, List<Method>> fieldSetters = new HashMap<>();

    public void initialize(UserAgentAnnotationMapper<T> theMapper) {
        mapper = theMapper;

        if (mapper == null) {
            throw new InvalidParserConfigurationException("[Initialize] The mapper instance is null.");
        }

        Class[] classOfTArray = GenericTypeResolver.resolveTypeArguments(mapper.getClass(), UserAgentAnnotationMapper.class);
        if (classOfTArray == null) {
            throw new InvalidParserConfigurationException("Couldn't find the used generic type of the UserAgentAnnotationMapper.");
        }

        Class classOfT = classOfTArray[0];

        // Get all methods of the correct signature that have been annotated with YauaaField
        for (final Method method : mapper.getClass().getDeclaredMethods()) {
            final YauaaField field = method.getAnnotation(YauaaField.class);
            if (field != null) {
                final Class<?> returnType = method.getReturnType();
                final Class<?>[] parameters = method.getParameterTypes();
                if (returnType.getCanonicalName().equals("void") &&
                    parameters.length == 2 &&
                    parameters[0] == classOfT &&
                    parameters[1] == String.class) {

                    if (!Modifier.isPublic(classOfT.getModifiers())) {
                        throw new InvalidParserConfigurationException("The class " + classOfT.getCanonicalName() + " is not public.");
                    }

                    if (!Modifier.isPublic(method.getModifiers())) {
                        throw new InvalidParserConfigurationException("Method annotated with YauaaField is not public: " +
                            method.getName());
                    }

                    if (method.getDeclaringClass().isAnonymousClass()) {
                        String methodName =
                            method.getReturnType().getName() + " " +
                                method.getName() + "(" +
                                parameters[0].getSimpleName()+ " ," +
                                parameters[1].getSimpleName()+ ");";
                        LOG.warn("Trying to make anonymous {} {} accessible.", method.getDeclaringClass(), methodName);
                        method.setAccessible(true);
                    }

                    for (String fieldName : field.value()) {
                        List<Method> methods = fieldSetters
                            .computeIfAbsent(fieldName, k -> new ArrayList<>());
                        methods.add(method);
                    }
                } else {
                    throw new InvalidParserConfigurationException(
                        "In class [" + method.getDeclaringClass() + "] the method [" + method.getName() + "] " +
                        "has been annotated with YauaaField but it has the wrong method signature. " +
                        "It must look like [ public void " + method.getName() + "(" + classOfT.getSimpleName() + " record, String value) ]");
                }
            }
        }

        if (fieldSetters.isEmpty()) {
            throw new InvalidParserConfigurationException("You MUST specify at least 1 field to extract.");
        }

        UserAgentAnalyzerBuilder<?, ?> builder = UserAgentAnalyzer.newBuilder();
        builder.hideMatcherLoadStats();
        builder.withFields(fieldSetters.keySet());
        userAgentAnalyzer = builder.build();
    }

    public T map(T record) {
        if (record == null) {
            return null;
        }
        if (mapper == null) {
            throw new InvalidParserConfigurationException("[Map] The mapper instance is null.");
        }

        UserAgent userAgent = userAgentAnalyzer.parse(mapper.getUserAgentString(record));

        for (Map.Entry<String, List<Method>> fieldSetter : fieldSetters.entrySet()) {
            String value = userAgent.getValue(fieldSetter.getKey());
            for (Method method : fieldSetter.getValue()) {
                try {
                    method.invoke(mapper, record, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new InvalidParserConfigurationException("A problem occurred while calling the requested setter", e);
                }
            }
        }
        return record;
    }
}
