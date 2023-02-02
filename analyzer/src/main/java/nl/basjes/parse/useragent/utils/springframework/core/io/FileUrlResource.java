/*
 * Copyright 2002-2020 the original author or authors.
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
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Subclass of {link UrlResource} which assumes file resolution, to the degree
 * of implementing the {link WritableResource} interface for it. This resource
 * variant also caches resolved {link File} handles from {link #getFile()}.
 *
 * <p>This is the class resolved by {link DefaultResourceLoader} for a "file:..."
 * URL location, allowing a downcast to {link WritableResource} for it.
 *
 * <p>Alternatively, for direct construction from a {link java.io.File} handle
 * or NIO {link java.nio.file.Path}, consider using {link FileSystemResource}.
 *
 * @author Juergen Hoeller
 * @since 5.0.2
 */
public class FileUrlResource extends UrlResource implements Resource {

    @Nullable
    private volatile File file;


    /**
     * Create a new {@code FileUrlResource} based on the given URL object.
     * <p>Note that this does not enforce "file" as URL protocol. If a protocol
     * is known to be resolvable to a file, it is acceptable for this purpose.
     *
     * @param url a URL
     * see ResourceUtils#isFileURL(URL)
     * see #getFile()
     */
    public FileUrlResource(URL url) {
        super(url);
    }


    @Override
    public File getFile() throws IOException {
        File filE = this.file;
        if (filE != null) {
            return filE;
        }
        filE = super.getFile();
        this.file = filE;
        return filE;
    }

    @Override
    public Resource createRelative(String relativePath) throws MalformedURLException {
        return new FileUrlResource(createRelativeURL(relativePath));
    }

}
