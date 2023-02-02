/*
 * Copyright 2002-2022 the original author or authors.
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

import nl.basjes.parse.useragent.utils.springframework.core.SerializableTypeWrapper.TypeProvider;
import nl.basjes.parse.useragent.utils.springframework.util.ConcurrentReferenceHashMap;
import nl.basjes.parse.useragent.utils.springframework.util.ObjectUtils;
import nl.basjes.parse.useragent.utils.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Encapsulates a Java {link java.lang.reflect.Type}, providing access to
 * {link #getSuperType() supertypes}, {link #getInterfaces() interfaces}, and
 * {link #getGeneric(int...) generic parameters} along with the ability to ultimately
 * {link #resolve() resolve} to a {link java.lang.Class}.
 *
 * <p>A {@code ResolvableType} may be obtained from a {linkplain #forField(Field) field},
 * a {linkplain #forMethodParameter(Method, int) method parameter},
 * a {linkplain #forMethodReturnType(Method) method return type}, or a
 * {linkplain #forClass(Class) class}. Most methods on this class will themselves return
 * a {@code ResolvableType}, allowing for easy navigation. For example:
 * <pre class="code">
 * private HashMap&lt;Integer, List&lt;String&gt;&gt; myMap;
 *
 * public void example() {
 *     ResolvableType t = ResolvableType.forField(getClass().getDeclaredField("myMap"));
 *     t.getSuperType(); // AbstractMap&lt;Integer, List&lt;String&gt;&gt;
 *     t.asMap(); // Map&lt;Integer, List&lt;String&gt;&gt;
 *     t.getGeneric(0).resolve(); // Integer
 *     t.getGeneric(1).resolve(); // List
 *     t.getGeneric(1); // List&lt;String&gt;
 *     t.resolveGeneric(1, 0); // String
 * }
 * </pre>
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * see #forField(Field)
 * see #forMethodParameter(Method, int)
 * see #forMethodReturnType(Method)
 * see #forConstructorParameter(Constructor, int)
 * see #forClass(Class)
 * see #forType(Type)
 * see #forInstance(Object)
 * see ResolvableTypeProvider
 * @since 4.0
 */
public final class ResolvableType implements Serializable {

    /**
     * {@code ResolvableType} returned when no value is available. {@code NONE} is used
     * in preference to {@code null} so that multiple method calls can be safely chained.
     */
    public static final ResolvableType NONE = new ResolvableType(EmptyType.INSTANCE, null, null, 0);

    private static final ResolvableType[] EMPTY_TYPES_ARRAY = new ResolvableType[0];

    private static final ConcurrentReferenceHashMap<ResolvableType, ResolvableType> CACHE =
        new ConcurrentReferenceHashMap<>(256);


    /**
     * The underlying Java type being managed.
     */
    private final Type type;

    /**
     * Optional provider for the type.
     */
    @Nullable
    private final TypeProvider typeProvider;

    /**
     * The {@code VariableResolver} to use or {@code null} if no resolver is available.
     */
    @Nullable
    private final VariableResolver variableResolver;

    /**
     * The component type for an array or {@code null} if the type should be deduced.
     */
    @Nullable
    private final ResolvableType componentType;

    @Nullable
    private final Integer hash;

    @Nullable
    private Class<?> resolved;

    @Nullable
    private volatile ResolvableType superType;

    @Nullable
    private volatile ResolvableType[] interfaces;

    @Nullable
    private volatile ResolvableType[] generics;


    /**
     * Private constructor used to create a new {link ResolvableType} for cache key purposes,
     * with no upfront resolution.
     */
    private ResolvableType(
        Type type, @Nullable TypeProvider typeProvider, @Nullable VariableResolver variableResolver) {

        this.type = type;
        this.typeProvider = typeProvider;
        this.variableResolver = variableResolver;
        this.componentType = null;
        this.hash = calculateHashCode();
        this.resolved = null;
    }

    /**
     * Private constructor used to create a new {link ResolvableType} for cache value purposes,
     * with upfront resolution and a pre-calculated hash.
     *
     * @since 4.2
     */
    private ResolvableType(Type type, @Nullable TypeProvider typeProvider,
                           @Nullable VariableResolver variableResolver, @Nullable Integer hash) {

        this.type = type;
        this.typeProvider = typeProvider;
        this.variableResolver = variableResolver;
        this.componentType = null;
        this.hash = hash;
        this.resolved = resolveClass();
    }

    /**
     * Private constructor used to create a new {link ResolvableType} for uncached purposes,
     * with upfront resolution but lazily calculated hash.
     */
    private ResolvableType(Type type, @Nullable TypeProvider typeProvider,
                           @Nullable VariableResolver variableResolver, @Nullable ResolvableType componentType) {

        this.type = type;
        this.typeProvider = typeProvider;
        this.variableResolver = variableResolver;
        this.componentType = componentType;
        this.hash = null;
        this.resolved = resolveClass();
    }

    /**
     * Private constructor used to create a new {link ResolvableType} on a {link Class} basis.
     * <p>Avoids all {@code instanceof} checks in order to create a straight {link Class} wrapper.
     *
     * @since 4.2
     */
    private ResolvableType(@Nullable Class<?> clazz) {
        this.resolved = (clazz != null ? clazz : Object.class);
        this.type = this.resolved;
        this.typeProvider = null;
        this.variableResolver = null;
        this.componentType = null;
        this.hash = null;
    }


    /**
     * Return the underling Java {link Type} being managed.
     */
    public Type getType() {
        return SerializableTypeWrapper.unwrap(this.type);
    }


    /**
     * Return {@code true} if this type resolves to a Class that represents an array.
     *
     * see #getComponentType()
     */
    public boolean isArray() {
        if (this == NONE) {
            return false;
        }
        return ((this.type instanceof Class && ((Class<?>) this.type).isArray()) ||
            this.type instanceof GenericArrayType || resolveType().isArray());
    }

    /**
     * Return the ResolvableType representing the component type of the array or
     * {link #NONE} if this type does not represent an array.
     *
     * see #isArray()
     */
    public ResolvableType getComponentType() {
        if (this == NONE) {
            return NONE;
        }
        if (this.componentType != null) {
            return this.componentType;
        }
        if (this.type instanceof Class) {
            Class<?> componenttype = ((Class<?>) this.type).getComponentType();
            return forType(componenttype, this.variableResolver);
        }
        if (this.type instanceof GenericArrayType) {
            return forType(((GenericArrayType) this.type).getGenericComponentType(), this.variableResolver);
        }
        return resolveType().getComponentType();
    }

    /**
     * Return this type as a {link ResolvableType} of the specified class. Searches
     * {link #getSuperType() supertype} and {link #getInterfaces() interface}
     * hierarchies to find a match, returning {link #NONE} if this type does not
     * implement or extend the specified class.
     *
     * @param typE the required type (typically narrowed)
     * @return a {link ResolvableType} representing this object as the specified
     * type, or {link #NONE} if not resolvable as that type
     * see #asCollection()
     * see #asMap()
     * see #getSuperType()
     * see #getInterfaces()
     */
    public ResolvableType as(Class<?> typE) {
        if (this == NONE) {
            return NONE;
        }
        Class<?> resolveD = resolve();
        if (resolveD == null || resolveD == typE) {
            return this;
        }
        for (ResolvableType interfaceType : getInterfaces()) {
            ResolvableType interfaceAsType = interfaceType.as(typE);
            if (interfaceAsType != NONE) {
                return interfaceAsType;
            }
        }
        return getSuperType().as(typE);
    }

    /**
     * Return a {link ResolvableType} representing the direct supertype of this type.
     * <p>If no supertype is available this method returns {link #NONE}.
     * <p>Note: The resulting {link ResolvableType} instance may not be {link Serializable}.
     *
     * see #getInterfaces()
     */
    public ResolvableType getSuperType() {
        Class<?> resolveD = resolve();
        if (resolveD == null) {
            return NONE;
        }
        try {
            Type superclass = resolveD.getGenericSuperclass();
            if (superclass == null) {
                return NONE;
            }
            ResolvableType supertype = this.superType;
            if (supertype == null) {
                supertype = forType(superclass, this);
                this.superType = supertype;
            }
            return supertype;
        } catch (TypeNotPresentException ex) {
            // Ignore non-present types in generic signature
            return NONE;
        }
    }

    /**
     * Return a {link ResolvableType} array representing the direct interfaces
     * implemented by this type. If this type does not implement any interfaces an
     * empty array is returned.
     * <p>Note: The resulting {link ResolvableType} instances may not be {link Serializable}.
     *
     * see #getSuperType()
     */
    public ResolvableType[] getInterfaces() {
        Class<?> resolveD = resolve();
        if (resolveD == null) {
            return EMPTY_TYPES_ARRAY;
        }
        ResolvableType[] interfaceS = this.interfaces;
        if (interfaceS == null) {
            Type[] genericIfcs = resolveD.getGenericInterfaces();
            interfaceS = new ResolvableType[genericIfcs.length];
            for (int i = 0; i < genericIfcs.length; i++) {
                interfaceS[i] = forType(genericIfcs[i], this);
            }
            this.interfaces = interfaceS;
        }
        return interfaceS;
    }

    /**
     * Return {@code true} if this type contains generic parameters.
     *
     * see #getGeneric(int...)
     * see #getGenerics()
     */
    public boolean hasGenerics() {
        return (getGenerics().length > 0);
    }

    /**
     * Return {@code true} if this type contains unresolvable generics only,
     * that is, no substitute for any of its declared type variables.
     */
    boolean isEntirelyUnresolvable() {
        if (this == NONE) {
            return false;
        }
        ResolvableType[] genericS = getGenerics();
        for (ResolvableType generic : genericS) {
            if (!generic.isUnresolvableTypeVariable() && !generic.isWildcardWithoutBounds()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine whether the underlying type is a type variable that
     * cannot be resolved through the associated variable resolver.
     */
    private boolean isUnresolvableTypeVariable() {
        if (this.type instanceof TypeVariable) {
            if (this.variableResolver == null) {
                return true;
            }
            TypeVariable<?> variable = (TypeVariable<?>) this.type;
            ResolvableType resolveD = this.variableResolver.resolveVariable(variable);
            return resolveD == null || resolveD.isUnresolvableTypeVariable();
        }
        return false;
    }

    /**
     * Determine whether the underlying type represents a wildcard
     * without specific bounds (i.e., equal to {@code ? extends Object}).
     */
    private boolean isWildcardWithoutBounds() {
        if (this.type instanceof WildcardType) {
            WildcardType wt = (WildcardType) this.type;
            if (wt.getLowerBounds().length == 0) {
                Type[] upperBounds = wt.getUpperBounds();
                return upperBounds.length == 0 || (upperBounds.length == 1 && Object.class == upperBounds[0]);
            }
        }
        return false;
    }

    /**
     * Return an array of {link ResolvableType ResolvableTypes} representing the generic parameters of
     * this type. If no generics are available an empty array is returned. If you need to
     * access a specific generic consider using the {link #getGeneric(int...)} method as
     * it allows access to nested generics and protects against
     * {@code IndexOutOfBoundsExceptions}.
     *
     * @return an array of {link ResolvableType ResolvableTypes} representing the generic parameters
     * (never {@code null})
     * see #hasGenerics()
     * see #getGeneric(int...)
     * see #resolveGeneric(int...)
     * see #resolveGenerics()
     */
    public ResolvableType[] getGenerics() {
        if (this == NONE) {
            return EMPTY_TYPES_ARRAY;
        }
        ResolvableType[] genericS = this.generics;
        if (genericS == null) {
            if (this.type instanceof Class) {
                Type[] typeParams = ((Class<?>) this.type).getTypeParameters();
                genericS = new ResolvableType[typeParams.length];
                for (int i = 0; i < genericS.length; i++) {
                    genericS[i] = ResolvableType.forType(typeParams[i], this);
                }
            } else if (this.type instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) this.type).getActualTypeArguments();
                genericS = new ResolvableType[actualTypeArguments.length];
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    genericS[i] = forType(actualTypeArguments[i], this.variableResolver);
                }
            } else {
                genericS = resolveType().getGenerics();
            }
            this.generics = genericS;
        }
        return genericS;
    }

    /**
     * Convenience method that will {link #getGenerics() get} and {link #resolve()
     * resolve} generic parameters, using the specified {@code fallback} if any type
     * cannot be resolved.
     *
     * @param fallback the fallback class to use if resolution fails
     * @return an array of resolved generic parameters
     * see #getGenerics()
     * see #resolve()
     */
    public Class<?>[] resolveGenerics(Class<?> fallback) {
        ResolvableType[] genericS = getGenerics();
        Class<?>[] resolvedGenerics = new Class<?>[genericS.length];
        for (int i = 0; i < genericS.length; i++) {
            resolvedGenerics[i] = genericS[i].resolve(fallback);
        }
        return resolvedGenerics;
    }

    /**
     * Resolve this type to a {link java.lang.Class}, returning {@code null}
     * if the type cannot be resolved. This method will consider bounds of
     * {link TypeVariable TypeVariables} and {link WildcardType WildcardTypes} if
     * direct resolution fails; however, bounds of {@code Object.class} will be ignored.
     * <p>If this method returns a non-null {@code Class} and {link #hasGenerics()}
     * returns {@code false}, the given type effectively wraps a plain {@code Class},
     * allowing for plain {@code Class} processing if desirable.
     *
     * @return the resolved {link Class}, or {@code null} if not resolvable
     * see #resolve(Class)
     * see #resolveGeneric(int...)
     * see #resolveGenerics()
     */
    @Nullable
    public Class<?> resolve() {
        return this.resolved;
    }

    /**
     * Resolve this type to a {link java.lang.Class}, returning the specified
     * {@code fallback} if the type cannot be resolved. This method will consider bounds
     * of {link TypeVariable TypeVariables} and {link WildcardType WildcardTypes} if
     * direct resolution fails; however, bounds of {@code Object.class} will be ignored.
     *
     * @param fallback the fallback class to use if resolution fails
     * @return the resolved {link Class} or the {@code fallback}
     * see #resolve()
     * see #resolveGeneric(int...)
     * see #resolveGenerics()
     */
    public Class<?> resolve(Class<?> fallback) {
        return (this.resolved != null ? this.resolved : fallback);
    }

    @Nullable
    private Class<?> resolveClass() {
        if (this.type == EmptyType.INSTANCE) {
            return null;
        }
        if (this.type instanceof Class) {
            return (Class<?>) this.type;
        }
        if (this.type instanceof GenericArrayType) {
            Class<?> resolvedComponent = getComponentType().resolve();
            return (resolvedComponent != null ? Array.newInstance(resolvedComponent, 0).getClass() : null);
        }
        return resolveType().resolve();
    }

    /**
     * Resolve this type by a single level, returning the resolved value or {link #NONE}.
     * <p>Note: The returned {link ResolvableType} should only be used as an intermediary
     * as it cannot be serialized.
     */
    ResolvableType resolveType() {
        if (this.type instanceof ParameterizedType) {
            return forType(((ParameterizedType) this.type).getRawType(), this.variableResolver);
        }
        if (this.type instanceof WildcardType) {
            Type resolveD = resolveBounds(((WildcardType) this.type).getUpperBounds());
            if (resolveD == null) {
                resolveD = resolveBounds(((WildcardType) this.type).getLowerBounds());
            }
            return forType(resolveD, this.variableResolver);
        }
        if (this.type instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) this.type;
            // Try default variable resolution
            if (this.variableResolver != null) {
                ResolvableType resolveD = this.variableResolver.resolveVariable(variable);
                if (resolveD != null) {
                    return resolveD;
                }
            }
            // Fallback to bounds
            return forType(resolveBounds(variable.getBounds()), this.variableResolver);
        }
        return NONE;
    }

    @Nullable
    private Type resolveBounds(Type[] bounds) {
        if (bounds.length == 0 || bounds[0] == Object.class) {
            return null;
        }
        return bounds[0];
    }

    @Nullable
    private ResolvableType resolveVariable(TypeVariable<?> variable) {
        if (this.type instanceof TypeVariable) {
            return resolveType().resolveVariable(variable);
        }
        if (this.type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) this.type;
            Class<?> resolveD = resolve();
            if (resolveD == null) {
                return null;
            }
            TypeVariable<?>[] variables = resolveD.getTypeParameters();
            for (int i = 0; i < variables.length; i++) {
                if (ObjectUtils.nullSafeEquals(variables[i].getName(), variable.getName())) {
                    Type actualType = parameterizedType.getActualTypeArguments()[i];
                    return forType(actualType, this.variableResolver);
                }
            }
            Type ownerType = parameterizedType.getOwnerType();
            if (ownerType != null) {
                return forType(ownerType, this.variableResolver).resolveVariable(variable);
            }
        }
        if (this.type instanceof WildcardType) {
            ResolvableType resolveD = resolveType().resolveVariable(variable);
            if (resolveD != null) {
                return resolveD;
            }
        }
        if (this.variableResolver != null) {
            return this.variableResolver.resolveVariable(variable);
        }
        return null;
    }


    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResolvableType)) {
            return false;
        }

        ResolvableType otherType = (ResolvableType) other;
        if (!ObjectUtils.nullSafeEquals(this.type, otherType.type)) {
            return false;
        }
        if (this.typeProvider != otherType.typeProvider &&
            (this.typeProvider == null || otherType.typeProvider == null ||
                !ObjectUtils.nullSafeEquals(this.typeProvider.getType(), otherType.typeProvider.getType()))) {
            return false;
        }
        if (this.variableResolver != otherType.variableResolver &&
            (this.variableResolver == null || otherType.variableResolver == null ||
                !ObjectUtils.nullSafeEquals(this.variableResolver.getSource(), otherType.variableResolver.getSource()))) {
            return false;
        }
        return ObjectUtils.nullSafeEquals(this.componentType, otherType.componentType);
    }

    @Override
    public int hashCode() {
        return (this.hash != null ? this.hash : calculateHashCode());
    }

    private int calculateHashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(this.type);
        if (this.typeProvider != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.typeProvider.getType());
        }
        if (this.variableResolver != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.variableResolver.getSource());
        }
        if (this.componentType != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.componentType);
        }
        return hashCode;
    }

    /**
     * Adapts this {link ResolvableType} to a {link VariableResolver}.
     */
    @Nullable
    VariableResolver asVariableResolver() {
        if (this == NONE) {
            return null;
        }
        return new DefaultVariableResolver(this);
    }

    /**
     * Custom serialization support for {link #NONE}.
     */
    private Object readResolve() {
        return (this.type == EmptyType.INSTANCE ? NONE : this);
    }

    /**
     * Return a String representation of this type in its fully resolved form
     * (including any generic parameters).
     */
    @Override
    public String toString() {
        if (isArray()) {
            return getComponentType() + "[]";
        }
        if (this.resolved == null) {
            return "?";
        }
        if (this.type instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) this.type;
            if (this.variableResolver == null || this.variableResolver.resolveVariable(variable) == null) {
                // Don't bother with variable boundaries for toString()...
                // Can cause infinite recursions in case of self-references
                return "?";
            }
        }
        if (hasGenerics()) {
            return this.resolved.getName() + '<' + StringUtils.arrayToDelimitedString(getGenerics(), ", ") + '>';
        }
        return this.resolved.getName();
    }


    // Factory methods

    /**
     * Return a {link ResolvableType} for the specified {link Class},
     * using the full generic type information for assignability checks.
     * <p>For example: {@code ResolvableType.forClass(MyArrayList.class)}.
     *
     * @param clazz the class to introspect ({@code null} is semantically
     *              equivalent to {@code Object.class} for typical use cases here)
     * @return a {link ResolvableType} for the specified class
     * see #forClass(Class, Class)
     * see #forClassWithGenerics(Class, Class...)
     */
    public static ResolvableType forClass(@Nullable Class<?> clazz) {
        return new ResolvableType(clazz);
    }

    /**
     * Return a {link ResolvableType} for the specified {link Type} backed by the given
     * owner type.
     * <p>Note: The resulting {link ResolvableType} instance may not be {link Serializable}.
     *
     * @param type  the source type or {@code null}
     * @param owner the owner type used to resolve variables
     * @return a {link ResolvableType} for the specified {link Type} and owner
     * see #forType(Type)
     */
    public static ResolvableType forType(@Nullable Type type, @Nullable ResolvableType owner) {
        VariableResolver variableResolver = null;
        if (owner != null) {
            variableResolver = owner.asVariableResolver();
        }
        return forType(type, variableResolver);
    }


    /**
     * Return a {link ResolvableType} for the specified {link Type} backed by a given
     * {link VariableResolver}.
     *
     * @param type             the source type or {@code null}
     * @param variableResolver the variable resolver or {@code null}
     * @return a {link ResolvableType} for the specified {link Type} and {link VariableResolver}
     */
    static ResolvableType forType(@Nullable Type type, @Nullable VariableResolver variableResolver) {
        return forType(type, null, variableResolver);
    }

    /**
     * Return a {link ResolvableType} for the specified {link Type} backed by a given
     * {link VariableResolver}.
     *
     * @param type             the source type or {@code null}
     * @param typeProvider     the type provider or {@code null}
     * @param variableResolver the variable resolver or {@code null}
     * @return a {link ResolvableType} for the specified {link Type} and {link VariableResolver}
     */
    static ResolvableType forType(
        @Nullable Type type, @Nullable TypeProvider typeProvider, @Nullable VariableResolver variableResolver) {

        if (type == null && typeProvider != null) {
            type = SerializableTypeWrapper.forTypeProvider(typeProvider);
        }
        if (type == null) {
            return NONE;
        }

        // For simple Class references, build the wrapper right away -
        // no expensive resolution necessary, so not worth caching...
        if (type instanceof Class) {
            return new ResolvableType(type, typeProvider, variableResolver, (ResolvableType) null);
        }

        // Purge empty entries on access since we don't have a clean-up thread or the like.
        CACHE.purgeUnreferencedEntries();

        // Check the cache - we may have a ResolvableType which has been resolved before...
        ResolvableType resultType = new ResolvableType(type, typeProvider, variableResolver);
        ResolvableType cachedType = CACHE.get(resultType);
        if (cachedType == null) {
            cachedType = new ResolvableType(type, typeProvider, variableResolver, resultType.hash);
            CACHE.put(cachedType, cachedType);
        }
        resultType.resolved = cachedType.resolved;
        return resultType;
    }


    /**
     * Strategy interface used to resolve {link TypeVariable TypeVariables}.
     */
    interface VariableResolver extends Serializable {

        /**
         * Return the source of the resolver (used for hashCode and equals).
         */
        Object getSource();

        /**
         * Resolve the specified variable.
         *
         * @param variable the variable to resolve
         * @return the resolved variable, or {@code null} if not found
         */
        @Nullable
        ResolvableType resolveVariable(TypeVariable<?> variable);
    }


    private static class DefaultVariableResolver implements VariableResolver {

        private final ResolvableType source;

        DefaultVariableResolver(ResolvableType resolvableType) {
            this.source = resolvableType;
        }

        @Override
        @Nullable
        public ResolvableType resolveVariable(TypeVariable<?> variable) {
            return this.source.resolveVariable(variable);
        }

        @Override
        public Object getSource() {
            return this.source;
        }
    }


    /**
     * Internal {link Type} used to represent an empty value.
     */
    static class EmptyType implements Type, Serializable {

        static final Type INSTANCE = new EmptyType();

        Object readResolve() {
            return INSTANCE;
        }
    }

}
