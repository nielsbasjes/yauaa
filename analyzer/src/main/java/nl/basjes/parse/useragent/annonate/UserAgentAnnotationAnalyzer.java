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

package nl.basjes.parse.useragent.annonate;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAgentAnnotationAnalyzer<T> {
    private UseragentAnnotationMapper<T> mapper = null;
    private UserAgentAnalyzer userAgentAnalyzer = null;
    private static final Logger LOG = LoggerFactory.getLogger(UserAgentAnnotationAnalyzer.class);

    private Map<String, List<Method>> fieldSetters = new HashMap<>();

    public UserAgentAnnotationAnalyzer() {
    }

    public void initialize(UseragentAnnotationMapper<T> theMapper) {
        mapper = theMapper;

        if (mapper == null) {
            throw new InvalidParserConfigurationException("The mapper instance is null.");
        }

        Class classOfT = GenericTypeResolver.resolveTypeArguments(mapper.getClass(), UseragentAnnotationMapper.class)[0];

        if (classOfT == null) {
            throw new InvalidParserConfigurationException("Couldn't find the used annotation.");
        }

        // Get all methods of the correct signature that have been annotated with YauaaField
        for (final Method method : mapper.getClass().getMethods()) {
            final YauaaField field = method.getAnnotation(YauaaField.class);
            if (field != null) {
                final Class<?> returnType = method.getReturnType();
                final Class<?>[] parameters = method.getParameterTypes();
                if (returnType.getCanonicalName().equals("void") &&
                    parameters.length == 2 &&
                    parameters[0] == classOfT &&
                    parameters[1] == String.class) {
                    for (String fieldName : field.value()) {
                        List<Method> methods = fieldSetters
                            .computeIfAbsent(fieldName, k -> new ArrayList<>());
                        methods.add(method);

                        if (!method.isAccessible() && method.getDeclaringClass().isAnonymousClass()) {
                            String methodName =
                                method.getReturnType().getName() + " " +
                                    method.getName() + "(" +
                                    parameters[0].getName()+ " ," +
                                    parameters[1].getName()+ ");";
                            LOG.warn("Trying to make anonymous {} {} accessible.", method.getDeclaringClass(), methodName);
                            method.setAccessible(true);
                        }
                    }
                } else {
                    LOG.error("The method {} of the class {} has the YauaaField annotation " +
                        "but has the wrong method signature.", method.getName(), method.getDeclaringClass());
                }
            }
        }

        if (fieldSetters.isEmpty()) {
            throw new InvalidParserConfigurationException("You MUST specify at least 1 field to extract.");
        }

        UserAgentAnalyzer.Builder builder = UserAgentAnalyzer.newBuilder();
        builder.hideMatcherLoadStats();
        if (!fieldSetters.isEmpty()) {
            builder.withFields(fieldSetters.keySet());
        }
        userAgentAnalyzer = builder.build();
    }

    public T map(T record) {
        if (record == null) {
            return null;
        }
        if (mapper == null) {
            throw new InvalidParserConfigurationException("The mapper instance is null.");
        }

        UserAgent userAgent = userAgentAnalyzer.parse(mapper.getUserAgentString(record));

        for (Map.Entry<String, List<Method>> fieldSetter : fieldSetters.entrySet()) {
            String value = userAgent.getValue(fieldSetter.getKey());
            for (Method method : fieldSetter.getValue()) {
                try {
                    method.invoke(mapper, record, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new InvalidParserConfigurationException("Couldn't call the requested setter", e);
                }
            }
        }
        return record;
    }
}
