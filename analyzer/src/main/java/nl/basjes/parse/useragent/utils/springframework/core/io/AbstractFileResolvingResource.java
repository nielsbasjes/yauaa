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

import nl.basjes.parse.useragent.utils.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * Abstract base class for resources which resolve URLs into File references,
 * such as {link UrlResource} or {link ClassPathResource}.
 *
 * <p>Detects the "file" protocol as well as the JBoss "vfs" protocol in URLs,
 * resolving file system references accordingly.
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public abstract class AbstractFileResolvingResource extends AbstractResource {

    public boolean exists() {
        try {
            URL url = getURL();
            if (ResourceUtils.isFileURL(url)) {
                // Proceed with file system resolution
                return getFile().exists();
            } else {
                // Try a URL connection content-length header
                URLConnection con = url.openConnection();
                customizeConnection(con);
                HttpURLConnection httpCon =
                    (con instanceof HttpURLConnection ? (HttpURLConnection) con : null);
                if (httpCon != null) {
                    httpCon.setRequestMethod("HEAD");
                    int code = httpCon.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        return true;
                    } else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        return false;
                    }
                }
                if (con.getContentLengthLong() > 0) {
                    return true;
                }
                if (httpCon != null) {
                    // No HTTP OK status, and no content-length header: give up
                    httpCon.disconnect();
                    return false;
                } else {
                    // Fall back to stream existence: can we open the stream?
                    getInputStream().close();
                    return true;
                }
            }
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * This implementation returns a File reference for the underlying class path
     * resource, provided that it refers to a file in the file system.
     *
     * see org.springframework.util.ResourceUtils#getFile(java.net.URL, String)
     */
    @Override
    public File getFile() throws IOException {
        URL url = getURL();
        return ResourceUtils.getFile(url, getDescription());
    }

    /**
     * This implementation returns a File reference for the given URI-identified
     * resource, provided that it refers to a file in the file system.
     *
     * see org.springframework.util.ResourceUtils#getFile(java.net.URI, String)
     */
    protected File getFile(URI uri) throws IOException {
        if (uri.getScheme().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
            throw new IllegalArgumentException("JBoss VFS is not supported");
        }
        return ResourceUtils.getFile(uri, getDescription());
    }

    @Override
    public long contentLength() throws IOException {
        URL url = getURL();
        if (ResourceUtils.isFileURL(url)) {
            // Proceed with file system resolution
            File file = getFile();
            long length = file.length();
            if (length == 0L && !file.exists()) {
                throw new FileNotFoundException(getDescription() +
                    " cannot be resolved in the file system for checking its content length");
            }
            return length;
        } else {
            // Try a URL connection content-length header
            URLConnection con = url.openConnection();
            customizeConnection(con);
            if (con instanceof HttpURLConnection) {
                HttpURLConnection httpCon = (HttpURLConnection) con;
                httpCon.setRequestMethod("HEAD");
            }
            return con.getContentLengthLong();
        }
    }

    /**
     * Customize the given {link URLConnection} before fetching the resource.
     * <p>Calls {link ResourceUtils#useCachesIfNecessary(URLConnection)} and
     * delegates to {link #customizeConnection(HttpURLConnection)} if possible.
     * Can be overridden in subclasses.
     *
     * @param con the URLConnection to customize
     * @throws IOException if thrown from URLConnection methods
     */
    protected void customizeConnection(URLConnection con) throws IOException {
        ResourceUtils.useCachesIfNecessary(con);
        if (con instanceof HttpURLConnection) {
            customizeConnection((HttpURLConnection) con);
        }
    }

    /**
     * Customize the given {link HttpURLConnection} before fetching the resource.
     * <p>Can be overridden in subclasses for configuring request headers and timeouts.
     *
     * @param ignoredCon the HttpURLConnection to customize
     * @throws IOException if thrown from HttpURLConnection methods
     */
    protected void customizeConnection(HttpURLConnection ignoredCon) throws IOException {
    }

}
