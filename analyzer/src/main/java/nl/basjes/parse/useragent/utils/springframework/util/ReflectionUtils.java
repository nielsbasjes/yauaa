/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.utils.springframework.util;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Simple utility class for working with the reflection API and handling
 * reflection exceptions.
 *
 * <p>Only intended for internal use.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Costin Leau
 * @author Sam Brannen
 * @author Chris Beams
 * @since 1.2.2
 */
public abstract class ReflectionUtils {

    private ReflectionUtils() {
    }

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * Cache for {link Class#getDeclaredMethods()} plus equivalent default methods
     * from Java 8 based interfaces, allowing for fast iteration.
     */
    private static final Map<Class<?>, Method[]> DECLARED_METHODS_CACHE = new ConcurrentReferenceHashMap<>(256);

    // Exception handling

    /**
     * Handle the given reflection exception.
     * <p>Should only be called if no checked exception is expected to be thrown
     * by a target method, or if an error occurs while accessing a method or field.
     * <p>Throws the underlying RuntimeException or Error in case of an
     * InvocationTargetException with such a root cause. Throws an
     * IllegalStateException with an appropriate message or
     * UndeclaredThrowableException otherwise.
     *
     * @param ex the reflection exception to handle
     */
    public static void handleReflectionException(Exception ex) {
        if (ex instanceof NoSuchMethodException) {
            throw new IllegalStateException("Method not found: " + ex.getMessage());
        }
        if (ex instanceof IllegalAccessException) {
            throw new IllegalStateException("Could not access method or field: " + ex.getMessage());
        }
        if (ex instanceof InvocationTargetException) {
            handleInvocationTargetException((InvocationTargetException) ex);
        }
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    /**
     * Handle the given invocation target exception. Should only be called if no
     * checked exception is expected to be thrown by the target method.
     * <p>Throws the underlying RuntimeException or Error in case of such a root
     * cause. Throws an UndeclaredThrowableException otherwise.
     *
     * @param ex the invocation target exception to handle
     */
    public static void handleInvocationTargetException(InvocationTargetException ex) {
        rethrowRuntimeException(ex.getTargetException());
    }

    /**
     * Rethrow the given {link Throwable exception}, which is presumably the
     * <em>target exception</em> of an {link InvocationTargetException}.
     * Should only be called if no checked exception is expected to be thrown
     * by the target method.
     * <p>Rethrows the underlying exception cast to a {link RuntimeException} or
     * {link Error} if appropriate; otherwise, throws an
     * {link UndeclaredThrowableException}.
     *
     * @param ex the exception to rethrow
     * @throws RuntimeException the rethrown exception
     */
    public static void rethrowRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }


    // Constructor handling


    // Method handling

    /**
     * Attempt to find a {link Method} on the supplied class with the supplied name
     * and no parameters. Searches all superclasses up to {code Object}.
     * <p>Returns {code null} if no {link Method} can be found.
     *
     * @param clazz the class to introspect
     * @param name  the name of the method
     * @return the Method object, or {code null} if none found
     */
    @Nullable
    public static Method findMethod(Class<?> clazz, String name) {
        return findMethod(clazz, name, EMPTY_CLASS_ARRAY);
    }

    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied name
     * and parameter types. Searches all superclasses up to {@code Object}.
     * <p>Returns {@code null} if no {@link Method} can be found.
     *
     * @param clazz      the class to introspect
     * @param name       the name of the method
     * @param paramTypes the parameter types of the method
     *                   (may be {@code null} to indicate any signature)
     * @return the Method object, or {@code null} if none found
     */
    @Nullable
    public static Method findMethod(Class<?> clazz, String name, @Nullable Class<?>... paramTypes) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(name, "Method name must not be null");
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() :
                getDeclaredMethods(searchType, false));
            for (Method method : methods) {
                if (name.equals(method.getName()) && (paramTypes == null || hasSameParams(method, paramTypes))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    private static boolean hasSameParams(Method method, Class<?>[] paramTypes) {
        return (paramTypes.length == method.getParameterCount() &&
            Arrays.equals(paramTypes, method.getParameterTypes()));
    }

    /**
     * Invoke the specified {link Method} against the supplied target object with no arguments.
     * The target object can be {@code null} when invoking a static {link Method}.
     * <p>Thrown exceptions are handled via a call to {link #handleReflectionException}.
     *
     * @param method the method to invoke
     * @param target the target object to invoke the method on
     * @return the invocation result, if any
     * see #invokeMethod(java.lang.reflect.Method, Object, Object[])
     */
    @Nullable
    public static Object invokeMethod(Method method, @Nullable Object target) {
        return invokeMethod(method, target, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Invoke the specified {link Method} against the supplied target object with the
     * supplied arguments. The target object can be {@code null} when invoking a
     * static {link Method}.
     * <p>Thrown exceptions are handled via a call to {link #handleReflectionException}.
     *
     * @param method the method to invoke
     * @param target the target object to invoke the method on
     * @param args   the invocation arguments (may be {@code null})
     * @return the invocation result, if any
     */
    @Nullable
    public static Object invokeMethod(Method method, @Nullable Object target, @Nullable Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception ex) {
            handleReflectionException(ex);
        }
        throw new IllegalStateException("Should never get here");
    }

    private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
        Assert.notNull(clazz, "Class must not be null");
        Method[] result = DECLARED_METHODS_CACHE.get(clazz);
        if (result == null) {
            try {
                Method[] declaredMethods = clazz.getDeclaredMethods();
                List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
                if (defaultMethods != null) {
                    result = new Method[declaredMethods.length + defaultMethods.size()];
                    System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                    int index = declaredMethods.length;
                    for (Method defaultMethod : defaultMethods) {
                        result[index] = defaultMethod;
                        index++;
                    }
                } else {
                    result = declaredMethods;
                }
                DECLARED_METHODS_CACHE.put(clazz, (result.length == 0 ? EMPTY_METHOD_ARRAY : result));
            } catch (Throwable ex) {
                throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
                    "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
            }
        }
        return (result.length == 0 || !defensive) ? result : result.clone();
    }

    @Nullable
    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }

}
