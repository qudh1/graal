/*
 * Copyright (c) 2018, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.jdk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.core.hub.ClassForNameSupport;
import com.oracle.svm.core.jdk.JavaLangSubstitutions.ClassLoaderSupport;
import com.oracle.svm.core.snippets.KnownIntrinsics;

@TargetClass(ClassLoader.class)
@Substitute
@SuppressWarnings("static-method")
public final class Target_java_lang_ClassLoader {

    private Target_java_lang_ClassLoader parent;

    @Substitute
    public Target_java_lang_ClassLoader() {
        parent = null;
    }

    @Substitute
    public Target_java_lang_ClassLoader getParent() {
        return parent;
    }

    @Substitute
    public Target_java_lang_ClassLoader(Target_java_lang_ClassLoader parent) {
        this.parent = parent;
    }

    /*
     * Substituting the whole class allows us to have fields of declared type ClassLoader, but still
     * get an error if anyone tries to access a field or call a method on it that we have not
     * explicitly substituted below.
     */

    @Substitute
    private InputStream getResourceAsStream(String name) {
        return getSystemResourceAsStream(name);
    }

    @Substitute
    private static InputStream getSystemResourceAsStream(String name) {
        List<byte[]> arr = Resources.get(name);
        return arr == null ? null : new ByteArrayInputStream(arr.get(0));
    }

    @Substitute
    private URL getResource(String name) {
        return getSystemResource(name);
    }

    @Substitute
    private static URL getSystemResource(String name) {
        List<byte[]> arr = Resources.get(name);
        return arr == null ? null : Resources.createURL(name, new ByteArrayInputStream(arr.get(0)));
    }

    @Substitute
    private Enumeration<URL> getResources(String name) {
        return getSystemResources(name);
    }

    @Substitute
    private static Enumeration<URL> getSystemResources(String name) {
        List<byte[]> arr = Resources.get(name);
        if (arr == null) {
            return Collections.emptyEnumeration();
        }
        List<URL> res = new ArrayList<>(arr.size());
        for (byte[] data : arr) {
            res.add(Resources.createURL(name, new ByteArrayInputStream(data)));
        }
        return Collections.enumeration(res);
    }

    @Substitute
    public static ClassLoader getSystemClassLoader() {
        return KnownIntrinsics.unsafeCast(ClassLoaderSupport.getInstance().systemClassLoader, ClassLoader.class);
    }

    @Substitute
    @SuppressWarnings("unused")
    static void loadLibrary(Class<?> fromClass, String name, boolean isAbsolute) {
        NativeLibrarySupport.singleton().loadLibrary(name, isAbsolute);
    }

    @Substitute
    private Class<?> loadClass(String name) throws ClassNotFoundException {
        return ClassForNameSupport.forName(name);
    }

    @Substitute
    @SuppressWarnings("unused")
    static void checkClassLoaderPermission(ClassLoader cl, Class<?> caller) {
    }

}
