/*
 * Copyright 2002-2018 the original author or authors.
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

import nl.basjes.parse.useragent.utils.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

/**
 * Utility for detecting and accessing JBoss VFS in the classpath.
 *
 * <p>As of Spring 4.0, this class supports VFS 3.x on JBoss AS 6+
 * (package {@code org.jboss.vfs}) and is in particular compatible with
 * JBoss AS 7 and WildFly 8+.
 *
 * <p>Thanks go to Marius Bogoevici for the initial patch.
 * <b>Note:</b> This is an internal class and should not be used outside the framework.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.0.3
 */
public abstract class VfsUtils {

    private static final String VFS3_PKG = "org.jboss.vfs.";
    private static final String VFS_NAME = "VFS";

    private static final Method VFS_METHOD_GET_ROOT_URL;
    private static final Method VFS_METHOD_GET_ROOT_URI;

    private static final Method VIRTUAL_FILE_METHOD_EXISTS;
    private static final Method VIRTUAL_FILE_METHOD_GET_INPUT_STREAM;
    private static final Method VIRTUAL_FILE_METHOD_GET_SIZE;
    private static final Method VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED;
    private static final Method VIRTUAL_FILE_METHOD_TO_URL;
    private static final Method VIRTUAL_FILE_METHOD_TO_URI;
    private static final Method VIRTUAL_FILE_METHOD_GET_NAME;
    private static final Method VIRTUAL_FILE_METHOD_GET_PATH_NAME;
    private static final Method VIRTUAL_FILE_METHOD_GET_PHYSICAL_FILE;
    private static final Method VIRTUAL_FILE_METHOD_GET_CHILD;

    protected static final Class<?> VIRTUAL_FILE_VISITOR_INTERFACE;
    protected static final Method VIRTUAL_FILE_METHOD_VISIT;

    private static final Field VISITOR_ATTRIBUTES_FIELD_RECURSE;

    private static final boolean VFS_CAN_BE_USED;

    static {
        boolean isUsable = true;
        ClassLoader loader = VfsUtils.class.getClassLoader();

        Method vfsMethodGetRootUrl;
        Method vfsMethodGetRootUri;
        Method virtualFileMethodExists;
        Method virtualFileMethodGetInputStream;
        Method virtualFileMethodGetSize;
        Method virtualFileMethodGetLastModified;
        Method virtualFileMethodToUri;
        Method virtualFileMethodToUrl;
        Method virtualFileMethodGetName;
        Method virtualFileMethodGetPathName;
        Method virtualFileMethodGetPhysicalFile;
        Method virtualFileMethodGetChild;

        Class<?> virtualFileVisitorInterface;
        Method virtualFileMethodVisit;

        Field visitorAttributesFieldRecurse;

        try {
            Class<?> vfsClass = loader.loadClass(VFS3_PKG + VFS_NAME);
            vfsMethodGetRootUrl = vfsClass.getMethod("getChild", URL.class);
            vfsMethodGetRootUri = vfsClass.getMethod("getChild", URI.class);

            Class<?> virtualFile = loader.loadClass(VFS3_PKG + "VirtualFile");
            virtualFileMethodExists = virtualFile.getMethod("exists");
            virtualFileMethodGetInputStream = virtualFile.getMethod("openStream");
            virtualFileMethodGetSize = virtualFile.getMethod("getSize");
            virtualFileMethodGetLastModified = virtualFile.getMethod("getLastModified");
            virtualFileMethodToUri = virtualFile.getMethod("toURI");
            virtualFileMethodToUrl = virtualFile.getMethod("toURL");
            virtualFileMethodGetName = virtualFile.getMethod("getName");
            virtualFileMethodGetPathName = virtualFile.getMethod("getPathName");
            virtualFileMethodGetPhysicalFile = virtualFile.getMethod("getPhysicalFile");
            virtualFileMethodGetChild = virtualFile.getMethod("getChild", String.class);

            virtualFileVisitorInterface = loader.loadClass(VFS3_PKG + "VirtualFileVisitor");
            virtualFileMethodVisit = virtualFile.getMethod("visit", virtualFileVisitorInterface);

            Class<?> visitorAttributesClass = loader.loadClass(VFS3_PKG + "VisitorAttributes");
            visitorAttributesFieldRecurse = visitorAttributesClass.getField("RECURSE");
        } catch (Throwable ex) {
            isUsable = false;
            vfsMethodGetRootUrl = null;
            vfsMethodGetRootUri = null;
            virtualFileMethodExists = null;
            virtualFileMethodGetInputStream = null;
            virtualFileMethodGetSize = null;
            virtualFileMethodGetLastModified = null;
            virtualFileMethodToUri = null;
            virtualFileMethodToUrl = null;
            virtualFileMethodGetName = null;
            virtualFileMethodGetPathName = null;
            virtualFileMethodGetPhysicalFile = null;
            virtualFileMethodGetChild = null;
            virtualFileVisitorInterface = null;
            virtualFileMethodVisit = null;
            visitorAttributesFieldRecurse = null;
        }
        VFS_CAN_BE_USED = isUsable;

        VFS_METHOD_GET_ROOT_URL               = vfsMethodGetRootUrl;
        VFS_METHOD_GET_ROOT_URI               = vfsMethodGetRootUri;
        VIRTUAL_FILE_METHOD_EXISTS            = virtualFileMethodExists;
        VIRTUAL_FILE_METHOD_GET_INPUT_STREAM  = virtualFileMethodGetInputStream;
        VIRTUAL_FILE_METHOD_GET_SIZE          = virtualFileMethodGetSize;
        VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED = virtualFileMethodGetLastModified;
        VIRTUAL_FILE_METHOD_TO_URI            = virtualFileMethodToUri;
        VIRTUAL_FILE_METHOD_TO_URL            = virtualFileMethodToUrl;
        VIRTUAL_FILE_METHOD_GET_NAME          = virtualFileMethodGetName;
        VIRTUAL_FILE_METHOD_GET_PATH_NAME     = virtualFileMethodGetPathName;
        VIRTUAL_FILE_METHOD_GET_PHYSICAL_FILE = virtualFileMethodGetPhysicalFile;
        VIRTUAL_FILE_METHOD_GET_CHILD         = virtualFileMethodGetChild;
        VIRTUAL_FILE_VISITOR_INTERFACE        = virtualFileVisitorInterface;
        VIRTUAL_FILE_METHOD_VISIT             = virtualFileMethodVisit;
        VISITOR_ATTRIBUTES_FIELD_RECURSE      = visitorAttributesFieldRecurse;
    }

    protected static Object invokeVfsMethod(Method method, @Nullable Object target, Object... args) throws IOException {
        if (!VFS_CAN_BE_USED) {
            throw new IllegalStateException("Could not detect JBoss VFS infrastructure");
        }
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            Throwable targetEx = ex.getTargetException();
            if (targetEx instanceof IOException) {
                throw (IOException) targetEx;
            }
            ReflectionUtils.handleInvocationTargetException(ex);
        } catch (Exception ex) {
            ReflectionUtils.handleReflectionException(ex);
        }

        throw new IllegalStateException("Invalid code path reached");
    }

    static boolean exists(Object vfsResource) {
        try {
            return (Boolean) invokeVfsMethod(VIRTUAL_FILE_METHOD_EXISTS, vfsResource);
        } catch (IOException ex) {
            return false;
        }
    }

    static boolean isReadable(Object vfsResource) {
        try {
            return (Long) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_SIZE, vfsResource) > 0;
        } catch (IOException ex) {
            return false;
        }
    }

    static long getSize(Object vfsResource) throws IOException {
        return (Long) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_SIZE, vfsResource);
    }

    static long getLastModified(Object vfsResource) throws IOException {
        return (Long) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED, vfsResource);
    }

    static InputStream getInputStream(Object vfsResource) throws IOException {
        return (InputStream) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_INPUT_STREAM, vfsResource);
    }

    static URL getURL(Object vfsResource) throws IOException {
        return (URL) invokeVfsMethod(VIRTUAL_FILE_METHOD_TO_URL, vfsResource);
    }

    static URI getURI(Object vfsResource) throws IOException {
        return (URI) invokeVfsMethod(VIRTUAL_FILE_METHOD_TO_URI, vfsResource);
    }

    static String getName(Object vfsResource) {
        try {
            return (String) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_NAME, vfsResource);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot get resource name", ex);
        }
    }

    static Object getRelative(URL url) throws IOException {
        return invokeVfsMethod(VFS_METHOD_GET_ROOT_URL, null, url);
    }

    static Object getChild(Object vfsResource, String path) throws IOException {
        return invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_CHILD, vfsResource, path);
    }

    static File getFile(Object vfsResource) throws IOException {
        return (File) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_PHYSICAL_FILE, vfsResource);
    }

    static Object getRoot(URI url) throws IOException {
        return invokeVfsMethod(VFS_METHOD_GET_ROOT_URI, null, url);
    }

    // protected methods used by the support sub-package

    protected static Object getRoot(URL url) throws IOException {
        return invokeVfsMethod(VFS_METHOD_GET_ROOT_URL, null, url);
    }

    @Nullable
    protected static Object doGetVisitorAttributes() {
        return ReflectionUtils.getField(VISITOR_ATTRIBUTES_FIELD_RECURSE, null);
    }

    @Nullable
    protected static String doGetPath(Object resource) {
        return (String) ReflectionUtils.invokeMethod(VIRTUAL_FILE_METHOD_GET_PATH_NAME, resource);
    }

}
