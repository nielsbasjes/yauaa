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

package nl.basjes.parse.useragent.utils.springframework.core;

import nl.basjes.parse.useragent.utils.springframework.util.ConcurrentReferenceHashMap;
import nl.basjes.parse.useragent.utils.springframework.util.ObjectUtils;
import nl.basjes.parse.useragent.utils.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Internal utility class that can be used to obtain wrapped {link Serializable}
 * variants of {link java.lang.reflect.Type java.lang.reflect.Types}.
 *
 * <p>{link #forField(Field) Fields} or {link #forMethodParameter(MethodParameter)
 * MethodParameters} can be used as the root source for a serializable type.
 * Alternatively, a regular {link Class} can also be used as source.
 *
 * <p>The returned type will either be a {link Class} or a serializable proxy of
 * {link GenericArrayType}, {link ParameterizedType}, {link TypeVariable} or
 * {link WildcardType}. With the exception of {link Class} (which is final) calls
 * to methods that return further {link Type Types} (for example
 * {link GenericArrayType#getGenericComponentType()}) will be automatically wrapped.
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.0
 */
final class SerializableTypeWrapper {

    private static final Class<?>[] SUPPORTED_SERIALIZABLE_TYPES = {
        GenericArrayType.class, ParameterizedType.class, TypeVariable.class, WildcardType.class};

    static final ConcurrentReferenceHashMap<Type, Type> CACHE = new ConcurrentReferenceHashMap<>(256);


    private SerializableTypeWrapper() {
    }


    /**
     * Unwrap the given type, effectively returning the original non-serializable type.
     *
     * @param type the type to unwrap
     * @return the original non-serializable type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Type> T unwrap(T type) {
        Type unwrapped = null;
        if (type instanceof SerializableTypeProxy) {
            unwrapped = ((SerializableTypeProxy) type).getTypeProvider().getType();
        }
        return (unwrapped != null ? (T) unwrapped : type);
    }

    /**
     * Return a {link Serializable} {link Type} backed by a {link TypeProvider} .
     * <p>If type artifacts are generally not serializable in the current runtime
     * environment, this delegate will simply return the original {@code Type} as-is.
     */
    @Nullable
    static Type forTypeProvider(TypeProvider provider) {
        Type providedType = provider.getType();
        if (providedType == null || providedType instanceof Serializable) {
            // No serializable type wrapping necessary (e.g. for java.lang.Class)
            return providedType;
        }
        if (NativeDetector.inNativeImage() || !Serializable.class.isAssignableFrom(Class.class)) {
            // Let's skip any wrapping attempts if types are generally not serializable in
            // the current runtime environment (even java.lang.Class itself, e.g. on GraalVM native images)
            return providedType;
        }

        // Obtain a serializable type proxy for the given provider...
        Type cached = CACHE.get(providedType);
        if (cached != null) {
            return cached;
        }
        for (Class<?> type : SUPPORTED_SERIALIZABLE_TYPES) {
            if (type.isInstance(providedType)) {
                ClassLoader classLoader = provider.getClass().getClassLoader();
                Class<?>[] interfaces = new Class<?>[]{type, SerializableTypeProxy.class, Serializable.class};
                InvocationHandler handler = new TypeProxyInvocationHandler(provider);
                cached = (Type) Proxy.newProxyInstance(classLoader, interfaces, handler);
                CACHE.put(providedType, cached);
                return cached;
            }
        }
        throw new IllegalArgumentException("Unsupported Type class: " + providedType.getClass().getName());
    }


    /**
     * Additional interface implemented by the type proxy.
     */
    interface SerializableTypeProxy {

        /**
         * Return the underlying type provider.
         */
        TypeProvider getTypeProvider();
    }


    /**
     * A {link Serializable} interface providing access to a {link Type}.
     */
    interface TypeProvider extends Serializable {

        /**
         * Return the (possibly non {link Serializable}) {link Type}.
         */
        @Nullable
        Type getType();

    }


    /**
     * {link Serializable} {link InvocationHandler} used by the proxied {link Type}.
     * Provides serialization support and enhances any methods that return {@code Type}
     * or {@code Type[]}.
     */
    private static class TypeProxyInvocationHandler implements InvocationHandler, Serializable {

        private final TypeProvider provider;

        TypeProxyInvocationHandler(TypeProvider provider) {
            this.provider = provider;
        }

        @Override
        @Nullable
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "equals":
                    Object other = args[0];
                    // Unwrap proxies for speed
                    if (other instanceof Type) {
                        other = unwrap((Type) other);
                    }
                    return ObjectUtils.nullSafeEquals(this.provider.getType(), other);
                case "hashCode":
                    return ObjectUtils.nullSafeHashCode(this.provider.getType());
                case "getTypeProvider":
                    return this.provider;
                default:
            }

            if (Type.class == method.getReturnType() && ObjectUtils.isEmpty(args)) {
                return forTypeProvider(new MethodInvokeTypeProvider(this.provider, method, -1));
            } else if (Type[].class == method.getReturnType() && ObjectUtils.isEmpty(args)) {
                Type[] result = new Type[((Type[]) method.invoke(this.provider.getType())).length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = forTypeProvider(new MethodInvokeTypeProvider(this.provider, method, i));
                }
                return result;
            }

            try {
                return method.invoke(this.provider.getType(), args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }


    /**
     * {link TypeProvider} for {link Type Types} obtained by invoking a no-arg method.
     */
    static class MethodInvokeTypeProvider implements TypeProvider {

        private final TypeProvider provider;

        private final String methodName;

        private final Class<?> declaringClass;

        private final int index;

        private transient Method method;

        @Nullable
        private transient volatile Object result;

        MethodInvokeTypeProvider(TypeProvider provider, Method method, int index) {
            this.provider = provider;
            this.methodName = method.getName();
            this.declaringClass = method.getDeclaringClass();
            this.index = index;
            this.method = method;
        }

        @Override
        @Nullable
        public Type getType() {
            Object resulT = this.result;
            if (resulT == null) {
                // Lazy invocation of the target method on the provided type
                resulT = ReflectionUtils.invokeMethod(this.method, this.provider.getType());
                // Cache the result for further calls to getType()
                this.result = resulT;
            }
            return (resulT instanceof Type[] ? ((Type[]) resulT)[this.index] : (Type) resulT);
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();
            Method methoD = ReflectionUtils.findMethod(this.declaringClass, this.methodName);
            if (methoD == null) {
                throw new IllegalStateException("Cannot find method on deserialization: " + this.methodName);
            }
            if (methoD.getReturnType() != Type.class && methoD.getReturnType() != Type[].class) {
                throw new IllegalStateException(
                    "Invalid return type on deserialized method - needs to be Type or Type[]: " + methoD);
            }
            this.method = methoD;
        }
    }

}
