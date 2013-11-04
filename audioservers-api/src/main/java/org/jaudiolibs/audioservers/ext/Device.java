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
package org.jaudiolibs.audioservers.ext;

import java.util.Collections;

/**
 * Represents a Device (eg. sound card) made available by the underlying audio
 * library.
 * 
 * Devices can also be extended by arbitrary extension Objects.
 * 
 * @author Neil C Smith
 */
public abstract class Device {
    
    private final String name;
    private final int maxInputChannels;
    private final int maxOutputChannels;    

    public Device(
            String name,
            int maxInputChannels,
            int maxOutputChannels) {
        
        if (name == null) {
            throw new NullPointerException("Name parameter cannot be null");
        }
        this.name = name;
        
        if (maxInputChannels < 0 || maxOutputChannels < 0) {
            throw new IllegalArgumentException("Channel count cannot be less than zero");
        }
        this.maxInputChannels = maxInputChannels;
        this.maxOutputChannels = maxOutputChannels;
    }
    
    /**
     * The name of the device. Never null.
     * @return
     */
    public final String getName() {
        return name;
    }
    
    /**
     * The maximum number of input channels supported by this device.
     * @return
     */
    public final int getMaxInputChannels() {
        return maxInputChannels;
    }
    
    /**
     * The maximum number of output channels supported by this device.
     * @return
     */
    public final int getMaxOutputChannels() {
        return maxOutputChannels;
    }
    
    /**
     * Find and return the first extension Object of the given type.
     *
     * The default implementation always returns null. Subclasses of this class
     * should override this method and the findAll() method as necessary.
     * 
     * @param <T>
     * @param type
     * @return Object or null
     */
    public <T> T find(Class<T> type) {
        return null;
    }

    /**
     * Find and return all extension Objects of the given type.
     * 
     * The default implementation always returns an empty Iterable.
     * Subclasses of this class should override this method and the find() 
     * method as necessary.
     * 
     * @param <T>
     * @param type
     * @return
     */
    public <T> Iterable<T> findAll(Class<T> type) {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return name;
    }
    
    
    
}
