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

package nl.basjes.parse.useragent.utils.springframework.core.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Convenience base class for {link Resource} implementations,
 * pre-implementing typical behavior.
 *
 * <p>The "exists" method will check whether a File or InputStream can
 * be opened; "isOpen" will always return false; "getURL" and "getFile"
 * throw an exception; and "toString" will return the description.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 28.12.2003
 */
public abstract class AbstractResource implements Resource {

    /**
     * This implementation throws a FileNotFoundException, assuming
     * that the resource cannot be resolved to a URL.
     */
    @Override
    public URL getURL() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
    }

    /**
     * This implementation throws a FileNotFoundException, assuming
     * that the resource cannot be resolved to an absolute file path.
     */
    @Override
    public File getFile() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
    }

    /**
     * This method reads the entire InputStream to determine the content length.
     * <p>For a custom subclass of {@code InputStreamResource}, we strongly
     * recommend overriding this method with a more optimal implementation, e.g.
     * checking File length, or possibly simply returning -1 if the stream can
     * only be read once.
     *
     * see #getInputStream()
     */
    @Override
    public long contentLength() throws IOException {
        InputStream is = getInputStream();
        try {
            long size = 0;
            byte[] buf = new byte[256];
            int read;
            while ((read = is.read(buf)) != -1) {
                size += read;
            }
            return size;
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger logger = LogManager.getLogger(getClass());
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not close content-length InputStream for {}", getDescription(), ex);
                }
            }
        }
    }

    /**
     * This implementation throws a FileNotFoundException, assuming
     * that relative resources cannot be created for this resource.
     */
    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
    }

    /**
     * This implementation always returns {@code null},
     * assuming that this resource type does not have a filename.
     */
    @Override
    @Nullable
    public String getFilename() {
        return null;
    }


    /**
     * This implementation compares description strings.
     *
     * see #getDescription()
     */
    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof Resource &&
            ((Resource) other).getDescription().equals(getDescription())));
    }

    /**
     * This implementation returns the description's hash code.
     *
     * see #getDescription()
     */
    @Override
    public int hashCode() {
        return getDescription().hashCode();
    }

    /**
     * This implementation returns the description of this resource.
     *
     * see #getDescription()
     */
    @Override
    public String toString() {
        return getDescription();
    }

}
