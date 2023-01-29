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
package nl.basjes.parse.useragent.utils.publicsuffixlist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * {@link PublicSuffixMatcher} loader.
 *
 * @since 4.4
 */
//@Contract(threading = ThreadingBehavior.SAFE)
public final class PublicSuffixMatcherLoader {

    private PublicSuffixMatcherLoader() {
    }

    private static final Logger LOG = LogManager.getLogger(PublicSuffixMatcherLoader.class);

    private static PublicSuffixMatcher load(final InputStream in) throws IOException {
        final List<PublicSuffixList> lists = PublicSuffixListParser.INSTANCE.parseByType(
                new InputStreamReader(in, StandardCharsets.UTF_8));
        return new PublicSuffixMatcher(lists);
    }

    public static PublicSuffixMatcher load(@Nonnull final URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            return load(in);
        }
    }

    public static PublicSuffixMatcher load(@Nonnull final File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return load(in);
        }
    }

    private static volatile PublicSuffixMatcher defaultInstance;

    private static final String DEFAULT_FILENAME = "/mozilla-public-suffix-list.txt";

    public static PublicSuffixMatcher getDefault() {
        if (defaultInstance == null) {
            synchronized (PublicSuffixMatcherLoader.class) {
                if (defaultInstance == null){
                    final URL url = PublicSuffixMatcherLoader.class.getResource(DEFAULT_FILENAME);
                    if (url != null) {
                        try {
                            defaultInstance = load(url);
                        } catch (final IOException ex) {
                            // Should never happen
                            LOG.warn("Failure loading public suffix list from default resource", ex);
                        }
                    } else {
                        defaultInstance = new PublicSuffixMatcher(DomainType.ICANN, Collections.singletonList("com"), null);
                        throw new IllegalStateException("Unable to load the needed file "+ DEFAULT_FILENAME);
                    }
                }
            }
        }
        return defaultInstance;
    }

}
