/*
 * Copyright 2002-2019 the original author or authors.
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

/**
 * Strategy interface for {@code String}-based path matching.
 *
 * <p>Used by {link org.springframework.core.io.support.PathMatchingResourcePatternResolver},
 * {link org.springframework.web.servlet.handler.AbstractUrlHandlerMapping},
 * and {link org.springframework.web.servlet.mvc.WebContentInterceptor}.
 *
 * <p>The default implementation is {link AntPathMatcher}, supporting the
 * Ant-style pattern syntax.
 *
 * @author Juergen Hoeller
 * see AntPathMatcher
 * @since 1.2
 */
public interface PathMatcher {

    /**
     * Does the given {@code path} represent a pattern that can be matched
     * by an implementation of this interface?
     * <p>If the return value is {@code false}, then the {link #match}
     * method does not have to be used because direct equality comparisons
     * on the static path Strings will lead to the same result.
     *
     * @param path the path to check
     * @return {@code true} if the given {@code path} represents a pattern
     */
    boolean isPattern(String path);

    /**
     * Match the given {@code path} against the given {@code pattern},
     * according to this PathMatcher's matching strategy.
     *
     * @param pattern the pattern to match against
     * @param path    the path to test
     * @return {@code true} if the supplied {@code path} matched,
     * {@code false} if it didn't
     */
    boolean match(String pattern, String path);

    /**
     * Match the given {@code path} against the corresponding part of the given
     * {@code pattern}, according to this PathMatcher's matching strategy.
     * <p>Determines whether the pattern at least matches as far as the given base
     * path goes, assuming that a full path may then match as well.
     *
     * @param pattern the pattern to match against
     * @param path    the path to test
     * @return {@code true} if the supplied {@code path} matched,
     * {@code false} if it didn't
     */
    boolean matchStart(String pattern, String path);

}
