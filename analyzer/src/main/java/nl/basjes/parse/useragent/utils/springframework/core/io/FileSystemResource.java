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

package nl.basjes.parse.useragent.utils.springframework.core.io;

import nl.basjes.parse.useragent.utils.springframework.util.Assert;
import nl.basjes.parse.useragent.utils.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * {link Resource} implementation for {@code java.io.File} and
 * {@code java.nio.file.Path} handles with a file system target.
 * Supports resolution as a {@code File} and also as a {@code URL}.
 * Implements the extended {link WritableResource} interface.
 *
 * <p>Note: As of Spring Framework 5.0, this {link Resource} implementation uses
 * NIO.2 API for read/write interactions. As of 5.1, it may be constructed with a
 * {link java.nio.file.Path} handle in which case it will perform all file system
 * interactions via NIO.2, only resorting to {link File} on {link #getFile()}.
 *
 * @author Juergen Hoeller
 * see #FileSystemResource(String)
 * see #FileSystemResource(File)
 * see #FileSystemResource(Path)
 * see java.io.File
 * see java.nio.file.Files
 * @since 28.12.2003
 */
public class FileSystemResource extends AbstractResource implements Resource {

    private final String path;

    @Nullable
    private final File file;

    private final Path filePath;


    /**
     * Create a new {@code FileSystemResource} from a file path.
     * <p>Note: When building relative resources via {link #createRelative},
     * it makes a difference whether the specified resource base path here
     * ends with a slash or not. In the case of "C:/dir1/", relative paths
     * will be built underneath that root: e.g. relative path "dir2" &rarr;
     * "C:/dir1/dir2". In the case of "C:/dir1", relative paths will apply
     * at the same directory level: relative path "dir2" &rarr; "C:/dir2".
     *
     * @param path a file path
     * see #FileSystemResource(Path)
     */
    public FileSystemResource(String path) {
        Assert.notNull(path, "Path must not be null");
        this.path = StringUtils.cleanPath(path);
        this.file = new File(path);
        this.filePath = this.file.toPath();
    }

    /**
     * Create a new {@code FileSystemResource} from a {link File} handle.
     * <p>Note: When building relative resources via {link #createRelative},
     * the relative path will apply <i>at the same directory level</i>:
     * e.g. new File("C:/dir1"), relative path "dir2" &rarr; "C:/dir2"!
     * If you prefer to have relative paths built underneath the given root directory,
     * use the {link #FileSystemResource(String) constructor with a file path}
     * to append a trailing slash to the root path: "C:/dir1/", which indicates
     * this directory as root for all relative paths.
     *
     * @param filE a File handle
     * see #FileSystemResource(Path)
     * see #getFile()
     */
    public FileSystemResource(@Nonnull File filE) {
        this.path = StringUtils.cleanPath(filE.getPath());
        this.file = filE;
        this.filePath = filE.toPath();
    }

    /**
     * Create a new {@code FileSystemResource} from a {link FileSystem} handle,
     * locating the specified path.
     * <p>This is an alternative to {link #FileSystemResource(String)},
     * performing all file system interactions via NIO.2 instead of {link File}.
     *
     * @param fileSystem the FileSystem to locate the path within
     * @param path       a file path
     * see #FileSystemResource(File)
     * @since 5.1.1
     */
    public FileSystemResource(FileSystem fileSystem, String path) {
        Assert.notNull(fileSystem, "FileSystem must not be null");
        Assert.notNull(path, "Path must not be null");
        this.path = StringUtils.cleanPath(path);
        this.file = null;
        this.filePath = fileSystem.getPath(this.path).normalize();
    }


    /**
     * Return the file path for this resource.
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * This implementation returns whether the underlying file exists.
     * see java.io.File#exists()
     */
    @Override
    public boolean exists() {
        return (this.file != null ? this.file.exists() : Files.exists(this.filePath));
    }

    /**
     * This implementation opens a NIO file stream for the underlying file.
     *
     * see java.io.FileInputStream
     */
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return Files.newInputStream(this.filePath);
        } catch (NoSuchFileException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }

    /**
     * This implementation returns a URL for the underlying file.
     *
     * see java.io.File#toURI()
     */
    @Override
    public URL getURL() throws IOException {
        return (this.file != null ? this.file.toURI().toURL() : this.filePath.toUri().toURL());
    }

    /**
     * This implementation returns a URI for the underlying file.
     * see java.io.File#toURI()
     */
    @Override
    public URI getURI() throws IOException {
        return (this.file != null ? this.file.toURI() : this.filePath.toUri());
    }

    /**
     * This implementation always indicates a file.
     */
    @Override
    public boolean isFile() {
        return true;
    }

    /**
     * This implementation returns the underlying File reference.
     */
    @Override
    public File getFile() {
        return (this.file != null ? this.file : this.filePath.toFile());
    }

    /**
     * This implementation returns the underlying File/Path length.
     */
    @Override
    public long contentLength() throws IOException {
        if (this.file != null) {
            long length = this.file.length();
            if (length == 0L && !this.file.exists()) {
                throw new FileNotFoundException(getDescription() +
                    " cannot be resolved in the file system for checking its content length");
            }
            return length;
        } else {
            try {
                return Files.size(this.filePath);
            } catch (NoSuchFileException ex) {
                throw new FileNotFoundException(ex.getMessage());
            }
        }
    }

    /**
     * This implementation creates a FileSystemResource, applying the given path
     * relative to the path of the underlying file of this resource descriptor.
     *
     * see org.springframework.util.StringUtils#applyRelativePath(String, String)
     */
    @Override
    public Resource createRelative(String relativePath) {
        String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
        return (this.file != null ? new FileSystemResource(pathToUse) :
            new FileSystemResource(this.filePath.getFileSystem(), pathToUse));
    }

    /**
     * This implementation returns the name of the file.
     *
     * see java.io.File#getName()
     */
    @Override
    public String getFilename() {
        return (this.file != null ? this.file.getName() : this.filePath.getFileName().toString());
    }

    /**
     * This implementation returns a description that includes the absolute
     * path of the file.
     *
     * see java.io.File#getAbsolutePath()
     */
    @Override
    public String getDescription() {
        return "file [" + (this.file != null ? this.file.getAbsolutePath() : this.filePath.toAbsolutePath()) + "]";
    }


    /**
     * This implementation compares the underlying File references.
     */
    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof FileSystemResource &&
            this.path.equals(((FileSystemResource) other).path)));
    }

    /**
     * This implementation returns the hash code of the underlying File reference.
     */
    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

}
