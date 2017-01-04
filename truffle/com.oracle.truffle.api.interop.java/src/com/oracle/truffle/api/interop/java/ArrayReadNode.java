/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.api.interop.java;

import java.lang.reflect.Array;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;

abstract class ArrayReadNode extends Node {
    protected abstract Object executeWithTarget(VirtualFrame frame, JavaObject receiver, Object index);

    @SuppressWarnings("unchecked")
    @Specialization(guards = "index.getClass() == clazz")
    protected static Object doNumber(JavaObject receiver, Number index, @Cached("index.getClass()") Class<?> clazz) {
        Class<Number> numberClazz = (Class<Number>) clazz;
        return doArrayAccess(receiver, numberClazz.cast(index).intValue());
    }

    @Specialization(contains = "doNumber")
    protected static Object doNumberGeneric(JavaObject receiver, Number index) {
        return doArrayAccess(receiver, index.intValue());
    }

    private static Object doArrayAccess(JavaObject object, int index) {
        Object obj = object.obj;
        Object val = null;
        try {
            val = Array.get(obj, index);
        } catch (IllegalArgumentException notAnArr) {
            throw UnsupportedMessageException.raise(Message.READ);
        }
        if (ToJavaNode.isPrimitive(val)) {
            return val;
        }
        return JavaInterop.asTruffleObject(val);
    }
}