/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2020 Neil C Smith.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.jaudiolibs.audioservers.util;

import java.util.ArrayList;

/**
 * A utility class that can be used to implement find() and findAll() support for
 * extension Objects.
 * 
 */
public final class ObjectLookup {
    
    public final static ObjectLookup EMPTY = new ObjectLookup(new Object[0]);

    private final ObjectLookup parent;
    private final Object[] objs;

    /**
     * Create an ObjectLookup wrapping the provided collection of Objects.
     * 
     * Objects may not be null. The order of Objects is maintained in results.
     * 
     * @param objs
     */
    public ObjectLookup(Object ... objs) {
        this(null, objs);
    }
    
    
    /**
     * Create an ObjectLookup wrapping the provided collection of Objects.
     * 
     * Results will be combined with the results from the optional parent lookup.
     * Object within this lookup will be returned first.
     * 
     * Objects may not be null. The order of Objects is maintained in results.
     * 
     * @param parent
     * @param objs
     */
    public ObjectLookup(ObjectLookup parent, Object ... objs) {
        for (Object o : objs) {
            if (o == null) {
                throw new NullPointerException();
            }
        }
        this.parent = parent;
        this.objs = objs.clone();
    }

    /**
     * Find the first Object of the given type, or null.
     *
     * @param <T>
     * @param type
     * @return Object of type, or null
     */
    public <T> T find(Class<T> type) {
        for (Object obj : objs) {
            if (type.isInstance(obj)) {
                return type.cast(obj);
            }
        }
        return parent == null ? null : parent.find(type);
    }

    /**
     * Find all Objects of the given type.
     * 
     * @param <T>
     * @param type
     * @return Iterable of the given type, never null
     */
    public <T> Iterable<T> findAll(Class<T> type) {
        ArrayList<T> list = new ArrayList<T>();
        for (Object obj : objs) {
            if (type.isInstance(obj)) {
                list.add(type.cast(obj));
            }
        }
        if (parent != null) {
            for (T obj : parent.findAll(type)) {
                list.add(obj);
            }
        }
        return list;
    }
    
}
