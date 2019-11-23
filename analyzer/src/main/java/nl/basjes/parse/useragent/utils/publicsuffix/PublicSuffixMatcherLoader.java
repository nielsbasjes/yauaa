/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package nl.basjes.parse.useragent.utils.publicsuffix;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.util.DomainType;
import org.apache.http.conn.util.PublicSuffixList;
import org.apache.http.conn.util.PublicSuffixListParser;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * {@link PublicSuffixMatcher} loader.
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.SAFE)
public final class PublicSuffixMatcherLoader {

    private PublicSuffixMatcherLoader() {
        // Nothing
    }

    public static PublicSuffixMatcher load(final InputStream in) throws IOException {
        Args.notNull(in, "InputStream");
        final List<PublicSuffixList> lists = new PublicSuffixListParser().parseByType(
                new InputStreamReader(in, StandardCharsets.UTF_8));
        return new PublicSuffixMatcher(lists);
    }

    public static PublicSuffixMatcher load(final URL url) throws IOException {
        Args.notNull(url, "URL");
        try (InputStream in = url.openStream()) {
            return load(in);
        }
    }

    public static PublicSuffixMatcher load(final File file) throws IOException {
        Args.notNull(file, "File");
        try (InputStream in = new FileInputStream(file)) {
            return load(in);
        }
    }

    private static volatile PublicSuffixMatcher defaultInstance;

    public static PublicSuffixMatcher getDefault() {
        if (defaultInstance == null) {
            synchronized (PublicSuffixMatcherLoader.class) {
                if (defaultInstance == null){
                    final URL url = PublicSuffixMatcherLoader.class.getResource(
                            "/mozilla/public-suffix-list.txt");
                    if (url != null) {
                        try {
                            defaultInstance = load(url);
                        } catch (final IOException ex) {
                            // Should never happen
                            final Logger log = LoggerFactory.getLogger(PublicSuffixMatcherLoader.class);
                            if (log.isWarnEnabled()) {
                                log.warn("Failure loading public suffix list from default resource", ex);
                            }
                        }
                    } else {
                        defaultInstance = new PublicSuffixMatcher(DomainType.ICANN, Collections.singletonList("com"), null);
                    }
                }
            }
        }
        return defaultInstance;
    }

}
