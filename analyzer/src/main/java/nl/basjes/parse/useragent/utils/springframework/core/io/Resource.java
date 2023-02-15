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

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Interface for a resource descriptor that abstracts from the actual
 * type of underlying resource, such as a file or class path resource.
 *
 * <p>An InputStream can be opened for every resource if it exists in
 * physical form, but a URL or File handle can just be returned for
 * certain resources. The actual behavior is implementation-specific.
 *
 * @author Juergen Hoeller
 * see #getInputStream()
 * see #getURL()
 * see #getURI()
 * see #getFile()
 * see WritableResource
 * see ContextResource
 * see UrlResource
 * see FileUrlResource
 * see FileSystemResource
 * see ClassPathResource
 * see ByteArrayResource
 * see InputStreamResource
 * @since 28.12.2003
 */
public interface Resource extends InputStreamSource {

    /**
     * Determine whether this resource actually exists in physical form.
     * <p>This method performs a definitive existence check, whereas the
     * existence of a {@code Resource} handle only guarantees a valid
     * descriptor handle.
     */
    boolean exists();

    /**
     * Indicate whether this resource represents a handle with an open stream.
     * If {@code true}, the InputStream cannot be read multiple times,
     * and must be read and closed to avoid resource leaks.
     * <p>Will be {@code false} for typical resource descriptors.
     */
    default boolean isOpen() {
        return false;
    }

    /**
     * Determine whether this resource represents a file in a file system.
     * <p>A value of {@code true} strongly suggests (but does not guarantee)
     * that a {link #getFile()} call will succeed.
     * <p>This is conservatively {@code false} by default.
     *
     * @since 5.0
     * see #getFile()
     */
    default boolean isFile() {
        return false;
    }

    /**
     * Return a URL handle for this resource.
     *
     * @throws IOException if the resource cannot be resolved as URL,
     *                     i.e. if the resource is not available as a descriptor
     */
    URL getURL() throws IOException;

    /**
     * Return a URI handle for this resource.
     *
     * @throws IOException if the resource cannot be resolved as URI,
     *                     i.e. if the resource is not available as a descriptor
     * @since 2.5
     */
    URI getURI() throws IOException;

    /**
     * Return a File handle for this resource.
     *
     * @throws java.io.FileNotFoundException if the resource cannot be resolved as
     *                                       absolute file path, i.e. if the resource is not available in a file system
     * @throws IOException                   in case of general resolution/reading failures
     * see #getInputStream()
     */
    File getFile() throws IOException;

    /**
     * Determine the content length for this resource.
     *
     * @throws IOException if the resource cannot be resolved
     *                     (in the file system or as some other known physical resource type)
     */
    long contentLength() throws IOException;

    /**
     * Create a resource relative to this resource.
     *
     * @param relativePath the relative path (relative to this resource)
     * @return the resource handle for the relative resource
     * @throws IOException if the relative resource cannot be determined
     */
    Resource createRelative(String relativePath) throws IOException;

    /**
     * Determine a filename for this resource, i.e. typically the last
     * part of the path: for example, "myfile.txt".
     * <p>Returns {@code null} if this type of resource does not
     * have a filename.
     */
    @Nullable
    String getFilename();

    /**
     * Return a description for this resource,
     * to be used for error output when working with the resource.
     * <p>Implementations are also encouraged to return this value
     * from their {@code toString} method.
     *
     * see Object#toString()
     */
    String getDescription();

}
