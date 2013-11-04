/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Neil C Smith.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved.  This file is offered as-is,
 * without any warranty.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 */
package org.jaudiolibs.audioservers.util;

import java.util.ArrayList;

/**
 * A utility class that can be used to implement find() and findAll() support for
 * extension Objects.
 * 
 * @author Neil C Smith (http://neilcsmith.net)
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
